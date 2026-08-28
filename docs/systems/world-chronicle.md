# World chronicle

## Status
**Concept. NOT implemented.** No chronicle record, significance filter, compaction job, history viewer, or item-history bridge exists. Field Guide and knowledge presentation are FVR-200..204, while Mason outcomes are FVR-500..507. A dedicated chronicle FVR ticket must be assigned before implementation.

## Purpose
The chronicle gives a long-lived world memory without logging every block placement. It records the events that explain how a place became inhabited: a settlement was founded, a route opened, an expedition succeeded, a profession reached a milestone, a major project changed the landscape, or a notable item acquired a history.

The log must remain useful after years of play. Bounded growth, aggregation, and durable references are design requirements, not optional clean-up.

## Player experience
A player can open a settlement chronicle and read that the river outpost was founded, the mountain road was surveyed, an expedition found a new region, an apprentice learned a technique, and a named pickaxe was reforged before a major project. Entries explain who, what, where, and when, with links to the relevant place, villager, item, route, or project when it still exists.

The chronicle is a record of significance, not a surveillance feed. Building a wall one block at a time does not create thousands of entries. A major wall project may produce one proposal, one completion, and perhaps one notable milestone if it changes settlement capacity or route safety.

Players can pin entries, add a short note where permitted, and filter by settlement, person, route, profession, region, project, or item. A summary remains readable when the world has decades of in-game history.

## Core rules
- Significant categories include settlement founding, settlement split or merger, route opening, expedition, professional milestone, major project, notable item history, migration, institutional knowledge transfer, and exceptional world events.
- An event enters the chronicle only when a defined significance predicate is met. Routine block placement, ordinary inventory movement, every villager tick, and repeated identical sales do not qualify.
- Each entry stores a stable event ID, schema version, category, world time, location or stable reference, actors, a concise fact payload, and provenance.
- Entries are append-only at the logical fact level. Corrections create a superseding record or migration rather than editing history invisibly.
- A system may emit a candidate event, but the chronicle applies a central significance and deduplication policy before persistence. A duplicate server retry must not create duplicate history.
- Item history is compact and selective. A notable item may record origin, maker, path, repair, reforge, transfer, and decisive use, but not every use or condition change.
- A settlement or route can aggregate routine milestones into a period summary. The summary must retain the factual span, counts or categories, and links to pinned entries.
- Players can pin entries. Pinned entries and events referenced by an active project, contract, or migration are protected from automatic compaction.
- The log grows through bounded retention and summaries rather than an arbitrary hard deletion of a world story. Compaction must preserve meaning and be schema-versioned.
- Chronicle entries do not award power merely for existing. They can provide context, Field Guide links, and evidence for knowledge or mastery when the originating event qualifies.
- Multiplayer entries identify the credited actors and respect server permission rules. A player should not be able to rewrite another player's history through a client-only note.
- A missing referenced entity does not invalidate the fact. The entry keeps a stable name, identifier, and last known context.

The event shape is intentionally small:

| Field | Example |
|---|---|
| category | route opening |
| actors | player and contributing villagers |
| place | river station or region ID |
| fact | route connected two discovered settlements |
| source references | route revision and project ID |
| presentation | title, summary, links, and time |

## Data ownership
World-scoped saved data owns the chronicle, event IDs, compaction summaries, pinned flags, and migration versions. Item components and entity attachments own compact local history and provide stable references. The chronicle is not the source of truth for the physical item, villager, route, or project.

Entries must be serialisable without loading the referenced chunk. A schema migration can preserve old fields or place an entry in a readable legacy form. Derived indexes by category, actor, and location are rebuildable.

## Server/client responsibilities
The server decides significance, records events, deduplicates retries, compacts safely, and filters private or permissioned information. The client renders pages, filters, links, and player notes where allowed. It cannot create a final fact by sending an arbitrary title and timestamp.

Chronicle updates can be pushed as bounded deltas. Opening a history screen requests a page by cursor, category, and stable world reference. The server rechecks visibility and returns a consistent snapshot token.

## Dependencies
- FVR-200..204 document chronicle concepts, filters, event explanations, and Field Guide links.
- FVR-500..507 provide early major-project and Mason events worth recording.
- Settlements, villagers, mastery, equipment, exploration, transportation, economy, logistics, food, seasons, and animals can emit significant candidates.
- Every source system must provide a stable event ID and concise fact payload before integration.
- Principle 14 requires versioned chronicle formats. Principle 18 requires safe migration and preservation of the world save.

## Extension points
A system can register an event category with a significance predicate, stable reference fields, presentation keys, privacy policy, and compaction strategy. A new client can render the same immutable fact records without changing server authority.

A future export can produce a deterministic human-readable chronicle for a world backup. Export is read-only and must omit private data unless explicitly permitted.

## Failure cases
- A duplicate event from retry or reconnect is ignored by stable event ID.
- A malformed candidate is rejected with the source system and field named. It does not block unrelated events.
- A referenced entity, item, or location no longer exists. The entry remains readable with last-known text and a broken-reference label.
- A crash during append or compaction leaves either the prior valid log or a complete new revision. It must not truncate the world history silently.
- A migration cannot interpret a field. The entry is retained in a versioned legacy payload and shown as such rather than discarded.
- A player lacks permission for a private entry. The client receives a filtered result, not the hidden text.

## Performance constraints
The chronicle targets no more than 20,000 detailed entries per world before low-significance entries are aggregated into period summaries. Pinned entries have no automatic expiry, but their compact payload should remain below 4 KiB each.

A history page returns at most 50 detailed entries and 20 summaries. Append work is one bounded event at a time. Compaction runs as a maintenance job with no more than 256 entries per slice and never requires loading referenced chunks.

## Accessibility and documentation requirements
Every entry has a readable title, category, date or world-time description, actors, place, and summary. Filters and pinned state need text labels and icons. Links must have descriptive names, not only coloured markers.

The Field Guide must explain what is and is not recorded, how summaries preserve years of history, how item and settlement history differ, and how to find entries by category. It must state that ordinary block placement is intentionally not logged.

## Non-goals
- This is not a block-by-block replay, surveillance feed, or complete transaction ledger.
- It does not log every villager tick, item use, sale, or inventory movement.
- It does not replace source-system state or serve as a second physical inventory.
- It does not guarantee that every event remains a full detailed entry forever.
- It does not allow clients to rewrite facts or expose private multiplayer history without permission.

## Open balance questions
- Which milestones feel significant enough to remember across a year of play?
- What compaction summary preserves story without flattening meaningful individual events?
- Should players be able to promote an otherwise routine event to a pinned chronicle entry?
- How much item history is useful before tooltips become unreadable?
- Which multiplayer privacy defaults are appropriate for shared settlements and personal discoveries?

## Planned tests
- Emit founding, route, expedition, professional, project, migration, mentorship, and item-history events and verify concise entries.
- Emit repeated block placement, routine sales, and villager ticks and verify no noisy entries are created.
- Retry event writes and reconnect during append to assert stable-ID deduplication.
- Crash during append, migration, and compaction and verify no truncation, duplication, or silent discard.
- Compact a world beyond 20,000 entries and verify summaries retain categories, spans, pinned entries, and references.
- Test missing references, permission filters, paging cursors, and deterministic export.
- Verify screen-reader labels, keyboard filters, icon and text status cues, and links that remain understandable without colour.

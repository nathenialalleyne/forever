# ADR 0029: Use one contextual knowledge surface instead of many starter books

- Date: 2026-08-28
- Related principles/ADRs: [Principles 2, 12, 13, 14, 15, 16, and 18](../design-principles.md), [ADR 0002](0002-isolate-matcha-behind-adapter.md), [ADR 0022](0022-extensible-by-default.md), [ADR 0028](0028-adventure-rewards-not-essential-gates.md)

## Status

Accepted. This is the information and onboarding decision for the pack.

## Context

A modpack assembled from independent projects often gives the player several starter books, guidebooks, recipe viewers, advancement screens, and tooltips. Each surface may be correct in isolation, but together they create duplicate instructions, competing terminology, inventory clutter, and an unclear first step. Matcha also has its own guidebook and private implementation vocabulary. Suppressing a book without preserving its instructions would solve clutter by creating an information gap.

The vision requires that discovery can hide a possibility but cannot hide instructions for a process the player is expected to perform. It also requires that the client render a projection of server knowledge rather than decide what the player has learned. A single contextual surface can make the pack's vocabulary and provenance legible, but it must not become a static megabook that replaces recipe inspection, tooltips, or the source mod's live state.

There is a tension between coherence and maintenance. A unified journal must ingest or link information from upstream mods, preserve the meaning of updates, handle optional dependencies, and support translations. It may also become a larger client-facing surface than any one upstream book. The benefit is not fewer words. It is one place that tells the player what is relevant here, why it matters, and where to perform or inspect it.

## Decision

Many Roads Home will use one unified contextual knowledge surface, called the Field Journal in player-facing design, as the primary onboarding and process-explanation surface. The journal will present relevant entries from Matcha, selected mods, pack configuration, and Many Roads Integration using stable pack concepts. It may link to recipe-viewer pages, contextual information, world records, and external-provider explanations rather than copying every detail into one page.

Starter books that duplicate the journal may be suppressed or folded into the journal only after their instructions, conditions, warnings, and provenance have been captured. Suppression is a compatibility decision, not a deletion shortcut. If a source book contains information that cannot yet be represented or linked, it remains available or the journal records a visible limitation. No process expected of the player may become instructionless because an onboarding item was removed.

The journal catalogue is data-driven and accepts content from any namespace under [ADR 0022](0022-extensible-by-default.md). Entries have stable IDs, source and version metadata, a player-facing explanation, related recipes or capabilities, discovery state, and a fallback when an optional provider is absent. Persistent knowledge is versioned and server-authoritative. The client receives bounded pages and may request context, but it cannot grant discovery or complete an instruction by itself.

Context is part of the surface. An entry may appear when the player inspects an item, block, recipe, route, structure, or failed action, and the journal must explain the next valid step and any relevant alternative. Jade, a recipe viewer, and ordinary tooltips remain useful projections of the same vocabulary. They are not competing authorities and do not replace the journal's process guidance.

## Consequences

- Players have a recognisable place to learn how the assembled pack works without searching several unrelated starter books.
- The journal can connect an item, recipe, Matcha capability, route, and project while preserving the source and confidence of each explanation.
- Onboarding becomes a maintained integration surface. Upstream changes can invalidate copied text, links, identifiers, or assumptions and require compatibility review.
- Suppressing source books is risky. A missing translation, hidden exception, or optional-provider failure can make the journal less complete than the original, so the default is preserve until verified.
- A single surface may become dense for experienced players and can consume client development and localisation time. Contextual entry points and filtering are necessary, not decorative extras.
- Knowledge records create persistence and migration obligations. Removing an entry must leave a readable historical or unavailable state rather than silently changing what a player learned.
- The same concept may need a concise tooltip, a contextual overlay, a recipe-viewer explanation, and a full journal entry. This is more work than shipping one book, but it keeps the instruction available at the moment it is needed.

## Rejected alternatives

### Keep every starter book unchanged

This preserves source fidelity, but it leaves players to resolve duplicate terminology and competing onboarding flows. It also makes the pack's shared rules difficult to explain and prevents a coherent response to Matcha-specific instructions.

### Replace all sources with one static manual

A static manual would be easier to edit than a contextual system, but it would become stale when an optional mod changes and would not show the state of a recipe, route, or capability. The journal is a surface over live, versioned information, not a printed replacement for it.

### Hide instructions until discovery

Mystery can be useful for revealing that a possibility exists, but withholding the expected process forces external wiki use and conflicts with Principle 2. The journal may hide an undiscovered opportunity while still exposing the instructions for a process the player has access to.

### Use the companion mod as the sole knowledge authority

The companion does not own every upstream rule and may be absent from a compatible profile. Making it the only authority would recreate a monolith and make ordinary pack content harder to remove or update.

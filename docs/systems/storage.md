# Storage

## Status
**Concept. NOT implemented.** No warehouse registry, search index, Traveler's Cache item, early shulker integration, or inventory transfer service exists. Storage work is tracked by FVR-600..604, with Field Guide documentation in FVR-200..204.

## Purpose
Storage should reduce the repeated friction of finding and organising physical goods without making containers magical. The design keeps physical inventories as the source of truth, introduces a local searchable warehouse, and gives early access to shulker-like personal organisation without allowing infinite container nesting.

Convenience must be earned by building and registering useful infrastructure, not by degrading ordinary inventory use so that a storage feature feels necessary.

## Player experience
Shulker access remains useful early rather than being reserved for the final phase of a world. A Traveler's Cache is a small personal essentials container with 9 slots at first and an expansion path to 18 slots under ADR 0011. It is for tools, food, maps, and emergency supplies that should travel with the player.

A Traveler's Cache cannot contain another container, including another Cache, shulker, or warehouse reference. This keeps its capacity legible and prevents a recursive backpack that defeats every inventory decision.

At a settlement, the player registers a local searchable warehouse. The search can show where matching physical stacks are stored, their quantities, condition, and access rules. Visibility is remote and convenient, but bulk goods are not withdrawn from a distance by magic. A player or logistics shipment must still move the physical items.

## Core rules
- Early shulker access is an intended convenience. It must not be delayed solely to preserve a traditional end-game gate.
- Traveler's Cache starts with 9 slots and may expand to 18 through a data-defined, discoverable progression. It stores personal essentials, not arbitrary warehouse inventory.
- No container nesting. A Cache cannot hold any item that itself stores an inventory, and a warehouse index cannot be inserted into a physical container as a second inventory.
- Cache contents are physical ItemStacks. Dropping, trading, dying, or moving a Cache follows explicit vanilla-like rules and must not duplicate its contents.
- A warehouse is a registered local service associated with a settlement node or valid outpost. Its search scope is declared and bounded by physical containers and access permissions.
- Physical container inventories remain the source of truth. Search indexes, category totals, and UI summaries are derived caches that may be rebuilt.
- Search may be by item identity, tags, category, trait, condition, owner, or location. The result identifies the physical container and slot or a stable physical handle.
- Search visibility does not grant withdrawal rights. A player must be present for direct bulk access or submit a logistics request that moves real goods.
- Warehouse insertion and removal are atomic at the stack level. A failed transfer leaves the source and destination unchanged.
- A broken equipment item, preserved food, or unknown external item remains searchable by stable identity even if a specialist cannot use it.
- Warehouse registrations, permissions, and index revisions are versioned. Index rebuilds never alter physical containers unless an explicit transaction is active.
- Normal vanilla chests, barrels, shulkers, and inventories continue to work without a warehouse registration.

The physical and derived layers should be separated:

| Layer | Authority |
|---|---|
| container slots and ItemStacks | physical source of truth |
| warehouse registration | world-scoped service declaration |
| search index | rebuildable derived data |
| query result | bounded projection with location and rights |
| withdrawal or shipment | explicit server transaction |

## Data ownership
Traveler's Cache contents live in the ItemStack inventory component with a schema version and nesting validation. Warehouse registration, physical-container references, permissions, and index metadata live in world-scoped saved data.

The index stores stable container identity, slot summaries, tags, quantities, and revision stamps. It must not become the only copy of a stack. On load, missing or stale entries are marked unknown and reconciled from physical containers during bounded maintenance.

## Server/client responsibilities
The server validates Cache contents, container capability, permissions, searches, and transfers. The client displays a paged result with the physical location, availability, and whether direct access or a shipment is required.

The client cannot fill a Cache, remove a remote stack, or trust a stale quantity. A request contains a source handle, desired amount, destination, and revision. The server rechecks the physical inventory before committing.

## Dependencies
- FVR-600..604 cover warehouse registration, index maintenance, search, Cache sizing, and transfer foundations.
- FVR-200..204 document Cache rules, no nesting, local search, and physical source-of-truth behaviour.
- Settlements provide local warehouse nodes. Logistics consumes warehouse reservations and shipment handles.
- Equipment, food, economy, and Matcha-adapted items must expose stable searchable tags without leaking private implementation details.
- ADR 0011 governs the 9 to 18 slot Traveler's Cache and the no-nesting rule.

## Extension points
A storage provider can expose a physical inventory through a capability interface with stable stack identity, slot access, revision, and permission checks. A new search facet can register a bounded index field and a Field Guide explanation.

A future remote Portal Depot may join warehouse networks, but it must be an explicit physical or infrastructure connection. External storage adapters must return safe “unknown” results when they cannot guarantee atomic stack access.

## Failure cases
- A malformed Cache containing a nested container is rejected during insertion and leaves both items intact.
- A stale index points to a missing container or changed slot. The result is marked unavailable and scheduled for reconciliation rather than causing deletion.
- A direct transfer interrupted by restart commits either the source state or the destination state, never duplicate stacks.
- A warehouse loses its registration or settlement node. Existing physical containers remain accessible by normal means, while the derived service becomes unavailable.
- A permission change invalidates a pending request before commit. No remote goods move.
- An external inventory that cannot provide stable revisions is visible only as a read-only, explicitly approximate source.

## Performance constraints
A warehouse search must query an index and return at most 200 result rows per page. It must not synchronously scan 40,000 item stacks when a screen opens. Index rebuilds process at most 256 containers or 8,192 slots per server maintenance slice.

Search text and filters are bounded to 128 characters and 16 facets. A client payload should stay below 64 KiB. Direct transfers operate on a bounded number of stacks, with bulk movement delegated to logistics.

## Accessibility and documentation requirements
Search results must include item name, quantity, condition or state, physical location, access status, and a text explanation for unavailable or stale results. Cache expansion and nesting rules need both icon and text cues.

The Field Guide must say exactly what a local warehouse can see, why remote visibility is not remote withdrawal, how to recover from stale results, how early shulker access works, and why container nesting is prohibited. Keyboard navigation and screen-reader labels are required for search and transfer screens.

## Non-goals
- This is not an infinite backpack or recursive container system.
- It does not make physical goods disappear into an abstract database.
- It does not provide magical cross-dimensional withdrawal.
- It does not replace normal chests, shulkers, or vanilla inventory behaviour.
- It does not force players to build a warehouse before using ordinary storage.

## Open balance questions
- What activities expand the Cache without making its 18 slots a complete inventory?
- Which local distance or connection rule makes a warehouse feel local without requiring awkward walking?
- How much stale index state can a player tolerate before automatic reconciliation feels broken?
- Should warehouse search include approximate external inventories or hide them until an adapter is reliable?
- What access controls are useful for multiplayer without creating administrative overhead?

## Planned tests
- Verify 9-slot and 18-slot Cache versions, including migration, drop, trade, death, and restart.
- Attempt every form of container nesting and assert safe rejection without item loss.
- Index physical containers, mutate them outside the index, rebuild, and verify physical contents win.
- Search large warehouses with filters and verify bounded results and no full synchronous scan.
- Test direct transfer, stale revision, permission change, disconnect, and crash recovery.
- Confirm remote visibility never mutates a bulk inventory without a physical transfer or logistics shipment.
- Verify normal vanilla containers remain usable without registration or warehouse services.

# ADR 0011: Provide a bounded Traveler's Cache

- Date: 2026-08-28
- Related principles/ADRs: [Principles 1, 6, 8, 9, 12, 14, 15, 17, and 18](../design-principles.md), [ADR 0008](0008-hybrid-obol-currency.md), [ADR 0010](0010-explicit-route-registration.md), [ADR 0012](0012-cross-dimensional-logistics.md), [ADR 0018](0018-infrastructure-gated-journey-skipping.md)

## Status

Accepted. This is a locked player-inventory decision.

## Context

Forever wants travel to remain an adventure before infrastructure turns repeated travel
into convenience. A player still needs a few personal essentials on an expedition, but
forcing those items into the normal inventory can turn every trip into a packing exercise.
The answer must reduce incidental friction without becoming a portable warehouse that
makes roads, storage, and logistics irrelevant.

The vision explicitly rejects the anti-AFK-farm checklist as the only route to abundance,
but it does not promise that every source of friction disappears immediately. A compact
travel kit can support exploration, specialist work, and solo play while leaving bulk
materials tied to physical movement and settlement infrastructure.

A second inventory is dangerous. If it accepts arbitrary containers, it becomes a nested
storage exploit. If it has unlimited slots, it bypasses route capacity and the geographic
trade-offs in Principles 6 and 8. If it is purely cosmetic, it does not solve the narrow
problem of carrying personal essentials.

The cache is player state and must survive ordinary player movement, including dimension
changes, without becoming a world-wide shared record. Its contents need explicit rules,
validation, and migration because an item can be carried, stored, traded, or dropped over
the life of a world.

## Decision

Every player has a **Traveler's Cache** with nine slots. The cache may be expanded to a
maximum of eighteen slots through a separately balanced, in-world progression. The exact
unlock conditions, if any, are data-driven and cannot turn normal building or basic play
into a prerequisite.

The cache is for personal essentials rather than bulk cargo. Its accepted item categories
are defined by a validated data tag or equivalent allowlist, documented in the Field
Guide. The allowlist is intentionally narrower than a general container and may include
navigation, survival, repair, and personal-use supplies without accepting arbitrary
warehouse stock.

The cache must never contain another container or any item that carries an inventory,
including another Traveler's Cache. This is an invariant, not an upgrade rule. Nested
containers are rejected at insertion and on load, with an actionable error and safe
preservation of the original item where possible.

Cache contents travel with the player as compact, versioned player state. The cache is
not remotely accessible by a settlement, warehouse, shop, or route. Physical bulk goods
still move through the world and follow [ADR 0012](0012-cross-dimensional-logistics.md).
The server validates every insertion, extraction, expansion, and migration. Client UI is
a projection and cannot bypass the item rules.

The cache does not alter normal inventory use or block placement. It is an optional
convenience that supports exploration and the infrastructure arc rather than a required
progression gate.

## Consequences

- Players can carry a modest emergency kit without sacrificing the whole normal inventory
  to food, tools, navigation, and repair supplies.
- Nine slots are enough to address personal essentials while preserving meaningful packing
  choices. Eighteen is a bounded expansion, not an invitation to store a base on the move.
- The cache supports solo expeditions and specialist work without requiring another player
  to act as a pack carrier.
- Because it is personal and not remotely accessible, it cannot replace a warehouse,
  registered route, or physical shipment. Geography and infrastructure remain relevant.
- The extra inventory surface increases UI, networking, persistence, and migration work.
  A cache must not be synchronised as an unbounded item list or stored in a giant player
  blob that serialises on every ordinary change.
- The allowlist may frustrate players who reasonably view an item as an essential. Every
  rejection needs an understandable explanation, and category changes can affect existing
  saves. The rule is accepted to protect the cache's purpose.
- Players may use stacks of high-value supplies to approximate bulk storage. Stack limits,
  item tags, and balance data need tests against that behaviour rather than assuming slot
  count alone prevents it.
- Expansion creates another progression advantage to balance. It must not become a hidden
  requirement for reaching a remote region or a paid solution to an artificial inventory
  problem under Principle 17.
- Dimension travel, death, disconnects, and invalid items are save-integrity edge cases.
  Failure handling must prefer preserving a bounded cache over deleting its contents.
- Future work includes the component schema, item tags, cache screen and keybinds, expansion
  path, death and recovery rules, network summaries, and Field Guide documentation.

## Rejected alternatives

### Normal inventory only

Using only the vanilla inventory would avoid implementation cost, but it would make basic
expeditions disproportionately about packing and encourage oversized backpacks or storage
mods to become mandatory. A small cache solves a specific friction without changing the
world's cargo model.

### A large backpack or second full inventory

A 27-slot or larger backpack would be convenient, but it would carry enough bulk to
undercut warehouses, routes, and regional logistics. It would also make a player's personal
inventory a universal answer to every transport problem.

### Arbitrarily nested containers

Allowing shulker-like containers or other inventories inside the cache would multiply
capacity without a visible slot cost and make the hard limit meaningless. It is a direct
violation of the no-nesting invariant.

### A portable universal warehouse

A remotely connected cache would remove the need to visit settlements, move goods, or
register routes. That would reward the exact frictionless play pattern the infrastructure
arc is meant to replace through construction.

### A one-way emergency kit that deletes contents

A consumable kit would be simple, but destroying personal supplies on use or travel would
make the solution punitive and difficult to trust in a long-lived save. A bounded persistent
cache is easier to reason about and migrate safely.

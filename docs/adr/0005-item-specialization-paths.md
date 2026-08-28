# ADR 0005: Give items persistent, switchable specialisation paths

- Date: 2026-08-28
- Related principles/ADRs: [Principles 1, 3, 5, 6, 12, 14, 15, 17, and 18](../design-principles.md), [ADR 0004](0004-player-mastery-loadout.md), [ADR 0006](0006-repair-and-reforge-model.md), [ADR 0020](0020-private-project-license-separation.md)

## Status

Accepted. This is a locked equipment progression decision.

## Context

The vision asks Forever to make specialisation a credible route to abundance without
turning Minecraft into a class-bound action game. Player mastery gives the character a
configurable focus under [ADR 0004](0004-player-mastery-loadout.md), but equipment also
needs a way to become meaningfully suited to a domain. A generic tool should remain
usable. A crafted and maintained tool should eventually outperform it for the work it
was shaped to do. This is part of the anti-AFK-farm-checklist problem: a skilled item
should compete with generic mass production in its domain, not become a mandatory class
gate. Principles 3 and 6 make flexibility and earned convenience explicit.

An item is a long-lived object in a long-lived world. Players name tools, carry their
history, and expect them to survive being dropped, traded, stored, and repaired. Deleting
an item when it reaches zero condition would violate Principle 5. Replacing the whole
stack whenever a player changes direction would also erase identity and make exploration
feel like a penalty.

There is a tension between flexibility and specialisation. If every path is active at
once, a single item becomes a universal best tool. If an item is permanently locked to
its first path, experimentation becomes expensive and players must predict future needs.
The system needs a reversible choice with a tangible in-world transition.

Per-item state belongs on the `ItemStack`, not in a player or world-wide registry. The
architecture calls for custom item data components so condition, active path, and
craftsmanship survive containers and transfers. Those fields must be versioned and have
bounded, validated representations.

## Decision

An item may learn and retain several compatible specialisation paths. Exactly one path is
active at a time. The active path determines the item's specialised behaviour and
benefits. Known paths and their prior progress are retained when another path is selected.

Changing the active path requires reforging. Reforging is an intentional workshop or
crafting interaction, not an inventory toggle. It preserves the item's identity, name,
valid enchantments, condition record, and progress history unless a separately documented
rule says that a particular transformation cannot be preserved safely.

A path is not permission to use an ordinary Minecraft item. Normal building, mining, and
survival remain available without registering a specialisation or owning a mastered item.
Specialisation earns performance or convenience in its domain rather than manufacturing
a penalty for everyone else.

The item stores a validated path history, active-path identifier, path progress, and
craftsmanship metadata in versioned item data components. Balance values, path costs, and
thresholds live in data under Principle 12. The server is authoritative for reforging and
for every action that depends on an active path.

Reforging and repair are related but distinct concerns. Reforging restores full condition
and permits advanced changes under [ADR 0006](0006-repair-and-reforge-model.md). A repair
must not silently change the active path, and a path change must not create a new item
that discards the old item's history.

## Consequences

- Items can become part of a player's story. A named pick can carry several careers over
  years rather than being discarded at the first change of project.
- The active-path choice creates a local specialisation decision without reproducing a
  permanent character class. It complements the broader player loadout rather than
  replacing it.
- Reforging turns a repeated inconvenience into infrastructure and workshop value, which
  supports Principle 6 and the vision's earned-convenience arc.
- Generic items remain useful, so this system does not gate ordinary Minecraft building or
  force every player to learn a path before participating.
- Per-item history increases item-data size and migration complexity. Items can exist in
  chests, shipments, trades, player inventories, and dropped stacks, so every read and
  write path needs validation.
- Reforging introduces an extra trip, material cost, or time requirement. If tuned too
  harshly it will punish experimentation, while if it is free and instant it will make
  active paths meaningless. The values require playtesting in data.
- Retaining old progress can create a large advantage for veteran items and may make newly
  crafted equipment feel inferior. The design accepts that history matters, but progression
  must still give new items a viable route into the economy.
- Server-side checks must prevent path swapping in the middle of an action, duplicating
  an item through a failed transaction, or applying a path bonus to an incompatible item.
- Future work includes path schemas, compatibility rules, reforge recipes, UI and
  tooltip language, migration tests, and Field Guide entries that explain what is known,
  active, and available without relying on colour alone.
- We accept that some third-party item identities may not support this state cleanly.
  Such mechanics belong in the Matcha classification and adapter boundaries rather than
  in a hidden special case in the item system.

## Rejected alternatives

### One immutable path per item

Permanent commitment would produce strong identity, but it would also make an early
experiment or accidental choice destroy the usefulness of an old tool. It conflicts with
Principle 3's distinction between permanent learning and configurable specialisation.

### One active path with no retained history

Allowing a switch while resetting the old path would reduce data complexity, but it would
make switching a disguised respec penalty. Players would avoid exploring paths and would
replace rather than maintain meaningful equipment.

### All known paths active simultaneously

This would be convenient, but it removes the opportunity cost that makes specialisation
meaningful. It would also let one item dominate generic and specialist alternatives at
once, recreating the convergence the vision is meant to avoid.

### Replacing the item on every path change

Creating a new item would simplify some implementation details, but it would break names,
attachments, enchantments, and emotional continuity. It would also make reforging feel
like a consumption recipe rather than a craftsperson improving an existing object.

### No item specialisation, only player mastery

Character mastery alone would leave tools as interchangeable commodities and miss an
important place where craftsmanship can replace generic mass production. It would also
make workshop infrastructure less useful. Player and item specialisation address different
scales and are intentionally related rather than redundant.

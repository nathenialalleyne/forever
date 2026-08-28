# ADR 0016: Incorporate existing villages through an explicit charter

- Date: 2026-08-28
- Related principles/ADRs: [Principles 1, 7, 8, 10, 14, 15, and 18](../design-principles.md), [ADR 0009](0009-settlements-as-building-graphs.md), [ADR 0010](0010-explicit-route-registration.md), [ADR 0015](0015-conservative-worldgen-posture.md), [ADR 0018](0018-infrastructure-gated-journey-skipping.md)

## Status

Accepted. This is a locked world-integration decision.

## Context

Many Forever worlds will already contain natural Minecraft villages before a player decides
to build a settlement system. Those villages are not disposable scaffolding. They contain
player memories, modified buildings, established paths, and villagers whose careers may
become part of the world's history. Making these places useful also gives settlement
and trade a route beyond the anti-AFK-farm checklist. Principles 1, 7, and 18 protect the
player's freedom, existing work, and save.

The vision says systems should validate function rather than style. Requiring a player to
demolish a natural village and rebuild it in a recognised shape would make the system a
theme and blueprint gate. It would also contradict the promise that normal Minecraft
building remains available and that existing worlds survive feature adoption.

Automatic recognition is not a safe answer either. A natural village can be partially
raided, expanded with unrelated buildings, split by a road, or changed by another mod.
Inferring ownership, membership, and functional boundaries without player intent can
create surprising settlement records and expensive scans.

The building-graph model in [ADR 0009](0009-settlements-as-building-graphs.md) needs a
controlled way to turn existing blocks into registered nodes. The operation must be
bounded, non-destructive, inspectable, and safe to repeat or abandon. It must not hide a
world migration inside world generation, especially under [ADR 0015](0015-conservative-worldgen-posture.md).

## Decision

Existing natural villages are not automatically treated as Forever settlements. A player
or authorised settlement operator may initiate a **charter/import workflow** that inspects
a bounded candidate area and presents the buildings, villagers, services, and unresolved
issues for confirmation.

The confirmed charter creates a settlement identity and maps selected physical buildings
to registered graph nodes. It preserves blocks, paths, inventories, names, and ordinary
Minecraft use. It does not teleport villagers, rebuild structures, rewrite the landscape,
or silently claim every nearby building.

Import validation checks function and safety, such as beds, workstations, storage,
enclosure, access, and hazards. It never scores a village's architectural style or
requires a generated blueprint. A partial or heavily modified village may be chartered
with missing capabilities recorded for later work instead of being rejected as visually
incorrect.

The workflow is explicit and bounded. It records the source area, selected nodes, village
or settlement relationships, unresolved references, and schema version. Re-running an
import must be idempotent or produce a clear review rather than duplicate graph nodes.
Imported routes and outposts use the explicit registration rules in [ADR 0010](0010-explicit-route-registration.md).

Importing a village does not make every villager permanently safe or immortal. Villager
careers, mortality, and institutional knowledge continue to follow [ADR 0007](0007-villager-mortality.md).
A charter gives the graph an identity and capabilities, not immunity from the rest of the
world.

## Consequences

- Players can bring an existing village into the settlement arc without abandoning a
  long-lived world or rebuilding according to a prescribed aesthetic.
- Explicit confirmation prevents an accidental scan from claiming a player's house,
  workshop, or rival settlement and makes ownership legible in multiplayer.
- Partial import lets a damaged or unconventional village remain useful. Missing beds,
  storage, safety, or services can be repaired as a project rather than treated as failure.
- The process composes with outposts, routes, and journey skipping while retaining the
  graph's functional and performance boundaries.
- Import adds a one-time but potentially expensive inspection and a complex review UI.
  The scan must be bounded, and its result must not be confused with continuous settlement
  simulation.
- Natural villages are ambiguous. A player may be disappointed when a beautiful building
  does not meet a capability or when a village must be split into several graph nodes.
  The Field Guide and inspection report need to explain the reason precisely.
- Imported records can outlive or diverge from their source blocks. Block destruction,
  moving villagers, competing claims, and repeated imports require safe invalidation and
  migration rules.
- Existing villagers may still die through physical causes. Import does not remove the
  causal mortality trade-off or turn a village into an indestructible legacy object.
- Future work includes the charter UI, bounded candidate discovery, node mapping, ownership
  permissions, idempotent persistence, repair reports, and tests on modified and partially
  destroyed villages.
- We accept that a village which is never chartered receives no Forever settlement
  capabilities. Player intent is safer than surprising automatic conversion.

## Rejected alternatives

### Automatic adoption of every natural village

Automatic adoption would feel seamless, but it would create false settlements around
structures the player never intended to manage. It would also require repeated scanning
and ambiguous ownership decisions, conflicting with bounded work and the world-save rule.

### Require rebuilding every village from scratch

A rebuild would produce clean graph data, but it would waste existing work, erase the
character of natural villages, and make settlement progression a style-compliance task.
It directly conflicts with Principles 1 and 7.

### Treat the village as a simple radius

A radius avoids a review screen, but it cannot distinguish functional buildings from nearby
terrain or represent a separated outpost. It would also make modified villages difficult
to explain and maintain.

### Regenerate villages during world upgrade

Worldgen migration would produce a uniform result, but it could destroy player builds,
change routes, and make older saves unsafe. Existing chunks outrank a cleaner generated
layout under Principle 18.

### Copy the village into a new protected structure

Duplicating a village would preserve the original visually, but it would create duplicate
inventories, villagers, and ownership histories. Import should register the place that
exists, not manufacture a second one.

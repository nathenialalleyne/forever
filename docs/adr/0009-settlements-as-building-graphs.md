# ADR 0009: Model settlements as graphs of registered buildings

- Date: 2026-08-28
- Related principles/ADRs: [Principles 1, 6, 7, 8, 9, 10, 12, 14, and 15](../design-principles.md), [ADR 0007](0007-villager-mortality.md), [ADR 0010](0010-explicit-route-registration.md), [ADR 0016](0016-existing-village-import.md), [ADR 0018](0018-infrastructure-gated-journey-skipping.md)

## Status

Accepted. This is a locked settlement model.

## Context

Forever's long arc is the world becoming easier to inhabit. Buildings, workplaces,
warehouses, roads, rail, and outposts should accumulate into a functioning place rather
than exist only as decoration. A settlement is also one of the routes by which a player
can reach abundance without building the same anti-AFK-farm checklist infrastructure as
everyone else. Principles 6, 7, and 8 make function, convenience, and geography the
relevant forces.

A circle or radius around a centre is a poor model for that goal. It makes a distant
building count merely because it is inside an arbitrary boundary, and it cannot represent
an outpost, a road-connected workshop, or a settlement that grew along a river. It also
encourages a style-neutral system to become a hidden test of where blocks happen to be.

Forever must validate function, not theme. Beds, workstations, storage, safety, space,
services, and infrastructure are meaningful. A medieval roof, modern facade, or personal
building style is not. Ordinary block placement must remain available without registering
a site or satisfying a blueprint.

The model must also respect performance. Automatic scans of every nearby block and
permanent physical simulation of every villager would violate the architecture and
Principles 10 and 18. Registration, bounded validation, versioned world records, and
cached relationships are necessary.

## Decision

A settlement is a graph of explicitly registered building nodes and meaningful
connections. A building node records a stable identity, position or bounded footprint,
functional roles, capabilities, ownership or membership information where applicable,
and a schema version. The graph records edges such as local attachment, infrastructure
connection, or an intentional outpost link.

Proximity creates the normal attachment relationship. Distances and tolerances are
configurable data, not hard-coded circles. An outpost may be connected as a deliberate
node even when it is beyond the local attachment distance, provided its connection and
service are valid. Roads, rail, and other infrastructure can connect parts of the graph
without requiring them to occupy one compact blob.

Registration and validation inspect function. They may check enclosure, beds,
workstations, storage, safety, space, access, and supported services. They must not score
architecture, enforce a theme, require a blueprint, or prevent ordinary construction.
A player may build freely first and register a useful building later.

Graph records are world-scoped, persistent, and versioned. The server owns graph mutation
and validation. Results are cached and invalidated by explicit registration or relevant
changes rather than produced by continuous unbounded world scans. Unloaded settlements
use the bounded abstract simulation required by Principle 10 and [ADR 0007](0007-villager-mortality.md).

Settlement graphs provide capabilities to other systems. Warehouses, workshops, routes,
trade services, villager careers, and journey skipping may require a registered and
validated node or edge, but each system retains its own authority and data model.

## Consequences

- A settlement can grow organically along roads, rail, coastlines, or resource regions
  without forcing a circular town shape.
- Functional validation supports every aesthetic. A player's modern city and a scattered
  homestead can provide the same service without an architectural judgement.
- Explicit nodes and edges give infrastructure a durable identity that other systems can
  reference for trade, repair, logistics, and travel convenience.
- Registration bounds the expensive work and makes a settlement's saved state inspectable,
  which supports performance and world-save safety.
- Players must perform registration and occasionally repair stale graph relationships.
  That is deliberate infrastructure work, but it adds ceremony that a radius check would
  avoid.
- Graph merges, splits, moved buildings, destroyed blocks, and overlapping claims create
  difficult edge cases. The implementation needs clear conflict resolution and safe
  rollback rather than silently rewriting a world record.
- A flexible graph can be abused by placing minimal nodes or long artificial edges to
  unlock services. Capability requirements and validation thresholds need real playtests.
- The graph is another persistent system to migrate. Stable IDs, bounded node counts,
  invalid-reference handling, and recovery tools are future requirements.
- Outposts may feel less immediately rewarding than a single compact base because their
  benefits depend on a real connection. That cost is accepted to preserve geography and
  make infrastructure meaningful.
- Future work includes the registration interaction, function validators, graph storage,
  invalidation rules, visual inspection, import support, and integration tests for loaded
  and unloaded settlement behaviour.

## Rejected alternatives

### Circular or radius-based settlements

A radius is simple to compute, but it treats geography and function as the same thing.
It admits irrelevant blocks, excludes useful outposts, and encourages players to optimise
coordinates rather than build a functioning place.

### Style-scored blueprints

Blueprint validation could produce recognisable towns, but it would dictate the player's
aesthetic and contradict the vision's explicit rejection of style requirements. It would
also make ordinary building feel like a compliance exercise.

### One settlement per chunk

Chunk ownership would make lookup convenient, but real settlements cross chunk boundaries
and can be smaller or more distributed than a chunk. It would also couple settlement
identity to a storage implementation detail.

### Fully automatic structure recognition

Scanning every nearby block and inferring a town sounds friendly, but it is expensive,
ambiguous for modified structures, and prone to surprise ownership or graph changes.
Explicit import and registration provide player intent and bounded work.

### Manual links with no proximity model

Requiring every edge to be placed by hand would maximise control, but it would make basic
settlement growth tedious and leave no natural relation between neighbouring buildings.
Proximity handles the common case, while explicit outpost and infrastructure links handle
deliberate distance.

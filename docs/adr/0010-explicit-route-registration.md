# ADR 0010: Register routes explicitly and cache their validation

- Date: 2026-08-28
- Related principles/ADRs: [Principles 6, 8, 9, 10, 12, 14, 15, and 18](../design-principles.md), [ADR 0009](0009-settlements-as-building-graphs.md), [ADR 0012](0012-cross-dimensional-logistics.md), [ADR 0013](0013-elytra-as-glider.md), [ADR 0018](0018-infrastructure-gated-journey-skipping.md)

## Status

Accepted. This is a locked route and performance decision.

## Context

The vision treats the first trip to a regional resource as an adventure and the fiftieth
trip as a chore that infrastructure should retire. Roads and rail must therefore become
real alternatives to improvising every journey or relying on Elytra. A route cannot,
however, be an invisible global graph that scans the world continuously. That would
violate the no-unbounded-scan architecture and could turn every active settlement into a
tick-time cost. This is the transport part of the anti-AFK-farm-checklist problem, and
Principles 6, 8, and 10 require a route to be useful without becoming a server-wide scan.

Routes also need to be trustworthy. A journey skip or shipment should not claim a service
exists merely because two coordinates are close. Roads need an explicit surveyed relation
between meaningful markers. Rail needs bounded verification of station-to-station
connectivity, including the relevant track graph and service constraints.

There is a tension between correctness and maintenance. A player can change blocks after
a route is registered. A result that is never rechecked becomes stale, but a result that
is rescanned on every trip is too expensive. The design needs explicit invalidation and
cached evidence so infrastructure is both earned and operational.

The route record must compose with settlement graphs, cross-dimensional boundaries, and
journey skipping. It must not become a second warehouse, a teleport permission detached
from physical construction, or a global mutable singleton.

## Decision

Routes are persistent, versioned records registered between named or otherwise stable
markers. Registration is an explicit survey or service action by the player or settlement,
not an automatic scan of every plausible path.

Road routes use an explicit survey between markers. The survey checks a bounded corridor
and records the route's mode, endpoints, relevant capabilities, and validation result.
The route does not need to be rescanned continuously. A changed or damaged corridor is
marked stale by known invalidation events or by an explicit resurvey before a dependent
service is used.

Rail routes use bounded station-to-station graph verification. The verifier follows only
the bounded track graph relevant to the registration, checks that the stations and
required connections are usable, and caches the result. It must reject unbounded networks,
cycles, or malformed data safely rather than walking until a server tick or memory limit
is exhausted.

A cached route records a validation version or signature, endpoint identities, mode,
service capabilities, and failure or stale reasons. Consumers such as logistics and
journey skipping use the cached contract and do not infer a route from distance alone.
The route becomes eligible for journey skipping only under [ADR 0018](0018-infrastructure-gated-journey-skipping.md).

Route distances, corridor widths, validation limits, service requirements, and balance
values are data-driven. All changes to a route record are server-authoritative and use
safe, versioned world storage. Cross-dimensional edges require the explicit boundary in
[ADR 0012](0012-cross-dimensional-logistics.md).

## Consequences

- Roads and rail become deliberate infrastructure that can replace repeated manual travel
  rather than decorative paths that no system recognises.
- Registration creates a bounded, inspectable trigger for expensive work and protects
  server performance when a world contains many settlements and routes.
- Cached evidence makes journey skipping and shipments predictable. A player can see why
  a route is valid, stale, or missing a required service.
- Rail can be made better than gliding on repeated fixed routes without pretending that
  every block in the world is part of a continuously maintained transport graph.
- Players must survey and sometimes resurvey routes. A damaged road or disconnected rail
  can temporarily remove a convenience they already earned, which may feel like upkeep.
- Cached validation can be stale between invalidation events. The implementation must
  favour a safe false negative over silently using a route that no longer exists.
- Bounded rail verification has a deliberate limit. Very large networks may need multiple
  registered segments or a later aggregation model rather than one all-encompassing route.
- Explicit markers, graph references, and invalidation records increase world-save and UI
  complexity. Every reference needs migration and missing-object handling.
- Future work includes marker registration, road surveying, rail verification, cache
  invalidation, route inspection, grief-resistant permissions, and tests for edits,
  unload/reload, restart, and malformed graphs.
- We accept that route discovery is less magical than automatic pathfinding. The trade is
  an understandable, performant system in which infrastructure is an intentional act.

## Rejected alternatives

### Continuous world scanning

Scanning nearby blocks or all possible paths on every tick or trip could discover routes
without player setup, but its cost grows with world size and route count. It conflicts
with Principles 10 and 18 and would make large settlements an infrastructure liability.

### Distance-only or line-of-sight routes

A coordinate check is fast, but it rewards proximity rather than a usable road or rail
service. It would allow journey skipping across hazards, walls, and missing tracks and
would make building infrastructure optional decoration.

### Fully manual routes with no verification

Letting the player declare any two markers connected would be simple and flexible, but it
would permit teleport-like skips through unbuilt terrain and make service failures hard to
explain. Bounded verification is necessary for the promise to be honest.

### Pathfinding every journey

Running live pathfinding can reflect the current world, but it is expensive, difficult to
make deterministic across dimensions, and unnecessary after a route has been validated.
The cache turns a construction-time check into a cheap runtime capability.

### One automatic global road graph

A global graph would make later logistics convenient, but it would merge unrelated paths,
create large invalidation cascades, and undermine settlement ownership and outpost intent.
Explicit route records compose more safely with [ADR 0009](0009-settlements-as-building-graphs.md).

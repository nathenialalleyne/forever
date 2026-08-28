# ADR 0018: Gate journey skipping behind proven infrastructure

- Date: 2026-08-28
- Related principles/ADRs: [Principles 6, 8, 9, 10, 12, 14, 15, 17, and 18](../design-principles.md), [ADR 0009](0009-settlements-as-building-graphs.md), [ADR 0010](0010-explicit-route-registration.md), [ADR 0012](0012-cross-dimensional-logistics.md), [ADR 0013](0013-elytra-as-glider.md), [ADR 0016](0016-existing-village-import.md)

## Status

Accepted. This is a locked travel-progression decision.

## Context

Forever's intended long arc is explicit: a trip begins as an expedition, becomes routine,
and can eventually become routine enough to skip. The skip is the reward for making the
world easier to inhabit. If fast travel is available immediately, the player has no reason
to build roads, rail, stations, outposts, or settlement connections.

The anti-AFK-farm checklist is partly a transport problem. Elytra and rockets make every
fixed route a personal flight problem, while an instant teleport menu makes geography
irrelevant from the first hour. Neither gives infrastructure a domain in which it can
compete. This is the infrastructure answer to the anti-AFK-farm checklist, and Principles
6, 8, and 10 require the convenience to be earned without making the server scan forever.
[ADR 0013](0013-elytra-as-glider.md) preserves gliding for scouting while this
ADR defines when routine skipping becomes earned convenience.

The gate must not be an arbitrary quest chain or a multiplayer privilege. It should prove
that the player knows the destinations, has actually traversed the route, and has built a
service that can be inspected and maintained. It must also fail safely when a route is
damaged or a destination disappears.

Journey skipping consumes persistent knowledge and references to world infrastructure.
Continuous route scans would violate the performance posture. The authority and validity
therefore need to come from the explicit records in [ADR 0010](0010-explicit-route-registration.md)
and the functional graph in [ADR 0009](0009-settlements-as-building-graphs.md).

## Decision

A player may skip a journey between two destinations only after all of these conditions
are true:

1. Both destinations have been discovered by that player or through an explicitly shared
   settlement discovery rule.
2. The player has physically travelled the route at least once. Seeing coordinates,
   receiving a map, or registering markers is not enough.
3. Suitable infrastructure has been built for the route, such as a usable road, rail
   service, station connection, or a later approved cross-dimensional depot.
4. That infrastructure has been explicitly registered and boundedly validated under
   [ADR 0010](0010-explicit-route-registration.md).
5. The transport service is currently valid, permitted, and able to handle the journey.

The resulting skip capability is attached to the validated route and its endpoints, not
merely to a pair of coordinates. If an endpoint, route, permission, or service becomes
stale or unavailable, skipping is denied with an actionable reason until the service is
repaired or revalidated. No implementation continuously scans the world to preserve the
capability.

Any fares, capacity, scheduling, cooldown, or shipment rules are separate balance choices
and live in data under Principle 12. They cannot substitute for the four substantive
proofs above. The system remains achievable in solo play and does not require a second
human to operate a station.

Journey skipping is a convenience layer over real infrastructure. It does not create a
universal warehouse, bypass dimension boundaries from [ADR 0012](0012-cross-dimensional-logistics.md),
or make ordinary building require a route registration.

## Consequences

- The first trip remains meaningful, and later repeated travel can be retired by something
  the player built rather than by an unexplained menu unlock.
- Roads, rail, stations, outposts, and imported settlements gain a concrete role in the
  progression from exploration to convenience.
- A player can understand why a skip is unavailable: discovery, travel history,
  infrastructure, registration, or current service is missing.
- Solo play remains viable because the proof can be completed by one player, while
  multiplayer can share infrastructure without making another player mandatory.
- The initial route can be inconvenient. Players who want to settle far from spawn must
  physically make the trip before earning relief from repeating it. That burden is
  intentional, but it needs sensible route lengths and readable progress.
- Infrastructure can become a maintenance obligation. A broken bridge, disconnected
  station, or imported building change can temporarily remove a convenience that felt
  permanent. Safe stale-state handling is required.
- Discovery sharing, route permissions, griefing, and multiplayer ownership create edge
  cases. A player must not inherit a skip for a route they could not access, nor lose all
  progress because a shared marker was renamed.
- Cross-dimensional skips remain subject to explicit Portal Depot or equivalent service
  decisions. A discovered portal alone is not a logistics or journey service.
- Fast travel balance can still be wrong even with a gate. Capacity, costs, arrival safety,
  and route comparison need playtesting against Elytra and ordinary travel.
- Future work includes discovery records, travel-history evidence, route capability state,
  invalidation, permissions, UI explanations, Field Guide entries, and save migration tests.

## Rejected alternatives

### Instant fast travel from the start

Immediate teleportation is convenient, but it makes roads, rail, exploration, and regional
identity optional. It solves repetition by deleting the world rather than letting the
player build a service that retires it.

### Discovery-only fast travel

Requiring a map or discovered destination adds some exploration, but it does not prove that
a usable route exists or reward infrastructure. It would make a registered railway no
better than a coordinate lookup.

### A repeated ticket, fuel, or XP payment as the only gate

A consumable cost can throttle use, but it lets players skip without building anything and
risks replacing one grind with another. Payment may balance a service later, but it cannot
stand in for physical travel and validated infrastructure.

### A permanent unlock unrelated to route condition

Unlocking a destination forever after one trip would be simple, but it would make later
damage and infrastructure investment irrelevant. Route-bound validity keeps convenience
connected to the world that provides it.

### Require manual travel forever

Never allowing a skip would preserve geography, but it would make the fiftieth journey a
permanent chore and violate the vision's earned-convenience arc. Infrastructure must be
able to retire repeated inconvenience.

### NPC-only transport

Delegating all skipping to NPC carriers could create settlement flavour, but it would
make solo progression depend on finding and maintaining a particular character. The
service should be infrastructure-first, with NPCs as possible participants rather than a
mandatory gate.

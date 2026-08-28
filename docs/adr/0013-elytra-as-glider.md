# ADR 0013: Treat Elytra primarily as a glider and scouting tool

- Date: 2026-08-28
- Related principles/ADRs: [Principles 6, 8, 9, 12, 15, 17, and 18](../design-principles.md), [ADR 0003](0003-classify-matcha-mechanics-individually.md), [ADR 0010](0010-explicit-route-registration.md), [ADR 0012](0012-cross-dimensional-logistics.md), [ADR 0018](0018-infrastructure-gated-journey-skipping.md)

## Status

Accepted. This is a locked transportation balance decision.

## Context

The vision names Elytra and rockets as part of the late-game anti-AFK-farm checklist.
Once
unlimited rocket flight is available, geography, roads, rail, and many settlement
connections become optional for repeated fixed routes. A player may still build them for
style, but not because infrastructure is the better answer to a routine journey.

Forever is not anti-technical and is not trying to remove enjoyable traversal. Flight is
excellent for scouting, reaching vertical terrain, and making a large world legible. The
problem is dominance: if the most efficient answer to every trip is the same personal
flight kit, infrastructure cannot become a genuine alternative route to abundance.

The change must therefore be targeted. Principle 17 rejects manufacturing a problem merely
to sell an upgrade, but the current dominance is an existing balance problem that prevents
other systems from functioning as intended. Principle 8 says the first trip should matter
and later inconvenience may be reduced by what the player builds.

Any flight change also affects exploration, rescue, construction, dimensions, resource
routes, and accessibility. A blunt removal would punish players who use Elytra for
vertical traversal, while leaving it untouched would leave the transport problem unsolved.
Fuel costs are tempting because they are easy to describe, but a consumable tax alone does
not make rail a better service and can turn ordinary movement into inventory accounting.

## Decision

Elytra is treated primarily as a **glider** for scouting, descent, and vertical traversal.
It remains a valuable exploration tool and is not removed from the game. Rocket-assisted
movement may exist, but unlimited rocket flight must not be the dominant answer for all
long-distance or repeated travel.

Flight behaviour, boost limits, altitude or launch constraints, and related balance values
are data-driven and subject to playtesting. The contract is about the role of Elytra,
not a single hard-coded number. It must preserve useful traversal while making sustained,
repeatable point-to-point service less efficient than established infrastructure where
that infrastructure is appropriate.

An established, registered rail route must beat Elytra for repeated fixed routes. Route
registration and bounded verification follow [ADR 0010](0010-explicit-route-registration.md).
Journey skipping is permitted only after the infrastructure and service conditions in
[ADR 0018](0018-infrastructure-gated-journey-skipping.md) are met.

This ADR does not mandate a blanket fuel system. Consumable costs may be evaluated as one
balance input, but fuel is not the primary substitute for a coherent glider role and may
not become an arbitrary toll on ordinary exploration. The Field Guide explains what
Elytra is good for and why rail has a different advantage.

The server remains authoritative for any new travel service. Client indicators, warnings,
and wear or status states use text or icons as well as colour. The change must be tested
against solo play, vertical building, cross-dimensional travel, and saved equipment state.

## Consequences

- Elytra remains exciting for discovery, scouting, cliffs, construction access, and
  vertical traversal instead of becoming a useless trophy.
- Rail gains a real domain: reliable repeated movement between established stations. That
  makes infrastructure an alternative to the personal flight checklist rather than a
  decorative project.
- The change supports the vision's progression from expedition to routine to earned
  convenience. Players can still fly first and build a better service later.
- Not deleting Elytra avoids invalidating existing player goals and reduces the risk of
  making the project feel like a total conversion.
- Players who enjoy current unlimited rocket flight will experience a real loss of
  convenience. This is an accepted compatibility and preference cost because untouched
  flight would preserve the dominance the vision explicitly rejects.
- Tuning is difficult. Too much restriction makes gliding frustrating or encourages an
  alternative teleport exploit. Too little restriction leaves rail economically irrelevant.
  Representative long-route tests are required, not only short creative flights.
- Rail must be built, registered, maintained, and connected before it wins. That creates
  upfront work and may make a new world feel slower, although it is the intended exchange
  for later convenience.
- Elytra interactions with third-party flight, rockets, dimensions, and Matcha mechanics
  need classification and adapter work rather than scattered special cases.
- Future work includes the flight contract, data values, route comparison metrics, rail
  service tests, accessibility text, migration checks, and Field Guide guidance.
- We accept that a single universal travel method is no longer the goal. Different modes
  should win in different domains, with rail winning on established repeated routes.

## Rejected alternatives

### Remove Elytra entirely

Removal would eliminate the dominant flight path, but it would also discard a beloved
Minecraft exploration tool and make vertical traversal needlessly difficult. It would be a
larger change than needed to make rail meaningful and would move Forever towards a total
conversion.

### Leave Elytra and rockets untouched

This preserves compatibility and convenience, but it leaves the anti-AFK-farm checklist
intact. Rail would rarely beat a personal Elytra for any fixed route, so the infrastructure
arc would be mostly aesthetic.

### Add fuel costs as the sole solution

Fuel creates a visible cost, but it can become another consumable grind and still allow
flight to beat rail when the player has enough stock. It also shifts the problem into
inventory management rather than establishing a useful distinction between gliding and
scheduled transport.

### Ban rockets completely

A rocket ban would produce a clear glider, but it would remove a familiar part of Elytra's
play pattern and punish long-distance exploration before any rail exists. Bounded assistance
can preserve the role without allowing unlimited flight to dominate.

### Replace flight with instant teleportation

Teleportation would be even more convenient, but it would erase geography and remove the
reason to construct routes. Journey skipping is deliberately infrastructure-gated instead
under [ADR 0018](0018-infrastructure-gated-journey-skipping.md).

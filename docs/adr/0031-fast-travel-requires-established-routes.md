# ADR 0031: Require established routes for fast travel

- Date: 2026-08-28
- Related principles/ADRs: [Principles 6, 8, 9, 10, 12, 15, 17, and 18](../design-principles.md), [ADR 0010](0010-explicit-route-registration.md), [ADR 0012](0012-cross-dimensional-logistics.md), [ADR 0013](0013-elytra-as-glider.md), [ADR 0018](0018-infrastructure-gated-journey-skipping.md), [ADR 0024](0024-modpack-first-product-ownership.md)

## Status

Accepted. This is the product-level fast-travel rule that applies the route decision to the modpack composition.

## Context

A long-lived world needs a way to retire repeated travel, but immediate teleportation makes geography and infrastructure irrelevant. If a player can place a waypoint and open a global menu before visiting a destination, there is no reason to build a road, station, bridge, outpost, or service. That undermines the vision's central progression from expedition to routine to earned convenience.

[ADR 0018](0018-infrastructure-gated-journey-skipping.md) already defines the proofs for journey skipping. The modpack pivot adds a product-level risk because existing travel mods may provide convenient waypoints or teleport anchors whose defaults do not know about Forever's route records. Disabling all travel assistance would preserve the gate by making the pack unnecessarily hostile. Accepting unrestricted fast travel would preserve convenience by discarding the infrastructure design. The pack needs a rule for selecting and configuring an existing tool.

Fast travel also has authority and failure concerns. A route can be damaged, a station can be removed, or a destination can become inaccessible. A stale teleport permission is not a harmless UI issue when it bypasses a physical service or crosses dimensions. The result must be based on registered infrastructure and fail with a reason when the service is unavailable.

## Decision

Pack-provided fast travel may be used only for an established route. Before a player can skip a journey, both endpoints must be discovered, the route must have been physically travelled, suitable infrastructure must exist, the route must be explicitly registered and boundedly validated, and a current transport service must be available. These conditions apply to a waypoint, station, passenger service, teleport-like convenience, or any other feature whose purpose is to skip the physical journey.

Existing travel mods are preferred when they can be configured to respect these conditions or can expose a public integration point. A mod that grants unrestricted global travel is not part of the default baseline unless its gate can be made equivalent and its removal impact is documented. Administrative commands, development tools, and ordinary vanilla dimension mechanics remain separate from player-facing pack fast travel and must not be misrepresented as route services.

A fast-travel record points to route and endpoint identities, not only coordinates. The server revalidates current permission, destination, route state, capacity, and service conditions. A broken or stale route denies the skip with an actionable reason while leaving walking, ordinary transport, or another valid mode available. Cross-dimensional travel requires an explicit approved connection under [ADR 0012](0012-cross-dimensional-logistics.md).

Fares, capacity, schedules, cooldowns, and service times are balance data. They may make a route feel distinct, but they cannot replace discovery, physical travel, infrastructure, registration, or validation. The implementation must not continuously scan the world or force-load chunks to preserve a fast-travel option.

## Consequences

- Roads, rail, stations, and passenger services become meaningful investments rather than decoration around an instant menu.
- The first trip remains a real part of exploration, while a repaired and validated route can remove repeated chore travel.
- Players may lose a convenience temporarily when infrastructure is damaged or removed. The failure is more honest than silently teleporting through an unavailable service, but it requires clear messages and repair paths.
- Existing travel mods may need restrictive configuration, an adapter, or exclusion from the baseline. This narrows the candidate pool and adds compatibility testing.
- Route records, discovery history, and service references become persistent dependencies for fast travel. Their migration and missing-object behaviour must be tested before a persistent release.
- The rule does not make ordinary walking or vanilla play require registration. A player can still travel without a route, but cannot claim the earned-convenience shortcut before the route exists.
- Server operators who want unrestricted administrative travel must configure it outside the player-facing contract. That creates support questions when operator settings differ from the pack baseline.

## Rejected alternatives

### Unrestricted waypoints from the first visit

This is convenient and familiar, but it removes the reason to build a service and makes a discovered coordinate equivalent to a functioning route. It conflicts with the long arc and ADR 0018.

### Require only that both endpoints were discovered

Discovery proves knowledge, not infrastructure. This alternative would allow a player to skip the physical work that is supposed to earn the convenience.

### Remove all fast travel

A total ban would preserve geography but would turn every repeated journey into a permanent tax. Infrastructure would have no final convenience to unlock, and the pack would reject a useful capability instead of configuring it honestly.

### Use a coordinate pair as the route record

Coordinates do not establish a road, rail line, capacity, ownership, or current service. A coordinate-only record would also fail badly when a settlement or station moves.

### Let distance or a payment replace the route proof

A price can regulate use, but it cannot prove that a route exists or that the player has established it. Money must not become a substitute for infrastructure.

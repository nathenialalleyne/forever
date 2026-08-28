# ADR 0012: Keep logistics dimension-scoped with explicit portal connections

- Date: 2026-08-28
- Related principles/ADRs: [Principles 6, 8, 9, 10, 12, 14, 15, and 18](../design-principles.md), [ADR 0008](0008-hybrid-obol-currency.md), [ADR 0009](0009-settlements-as-building-graphs.md), [ADR 0010](0010-explicit-route-registration.md), [ADR 0011](0011-travelers-cache.md), [ADR 0018](0018-infrastructure-gated-journey-skipping.md)

## Status

Accepted. This is a locked logistics boundary.

## Context

The vision treats geography as meaningful at first and infrastructure as the way to
retire repeated inconvenience. Dimensions are part of that geography. This is the
infrastructure response to the anti-AFK-farm-checklist convergence, and Principles 6 and
8 require convenience to be earned without making place meaningless. The Nether, End,
and any later dimension should not become transparent storage rooms that can be reached
from every warehouse without construction, risk, or service.

A universal warehouse would be attractive because it removes inventory friction. It would
also erase regional identity, undermine roads and routes, and make cross-dimensional
resource acquisition a single remote-interface transaction. That would reproduce the
convergence problem in a more powerful form rather than offering a competing route to
abundance.

The architecture already separates compact player state from large world-scoped records.
A Coin Purse may travel with a player, and a Traveler's Cache has a deliberately bounded
personal purpose, but warehouses, shipments, and settlement graphs are shared physical
records. Combining them across dimensions creates additional save, performance, and
failure concerns.

Cross-dimensional transport should remain possible. Forbidding all transfer would make
dimensions isolated theme parks and would prevent infrastructure from eventually reducing
geographic inconvenience. The open question is what earns a connection and how failure
is represented without pretending that a portal is a universal magical network.

## Decision

Warehouses, storage networks, route records, and shipment state are dimension-scoped by
default. A warehouse in one dimension cannot directly expose the contents of a warehouse
in another dimension. There is no magical universal warehouse or automatic cross-dimensional
synchronisation.

Future **Portal Depots** may connect two or more dimension-scoped networks. A depot is an
explicit physical endpoint with a registered relationship, bounded capacity or throughput,
known service state, and versioned world record. It transfers goods through a defined
logistics service. It does not turn every chest, settlement, or Coin Purse into a remote
view of all dimensions.

Portal Depot registration and validation compose with [ADR 0010](0010-explicit-route-registration.md)
and [ADR 0009](0009-settlements-as-building-graphs.md). The connection must be built,
registered, and serviceable. A missing, blocked, stale, or over-capacity endpoint produces
a visible failure or pending shipment rather than silently deleting, duplicating, or
teleporting goods.

Player-attached currency and personal essentials follow their own contracts. The Coin
Purse in [ADR 0008](0008-hybrid-obol-currency.md) is a payment representation, not bulk
logistics. The Traveler's Cache in [ADR 0011](0011-travelers-cache.md) is personal and
bounded, not a way to access remote settlement storage.

All cross-dimensional records, queues, and endpoint references are server-authoritative,
bounded, and schema-versioned. Capacity, throughput, costs, and failure policies are
balance data under Principle 12. No implementation may require permanent chunk loading
of every endpoint.

## Consequences

- A dimension retains a material and geographic identity. Acquiring or moving resources
  still creates reasons to explore, build, and maintain infrastructure.
- Settlements can develop local warehouses and later earn a Portal Depot connection,
  giving infrastructure a visible progression from local convenience to wider logistics.
- Dimension-scoped state limits the blast radius of a corrupted or unavailable endpoint
  and makes save recovery more understandable than one global ledger.
- Players can still build a cross-dimensional economy without requiring another human,
  preserving the solo-play principle.
- Early play requires duplicate stock or real transport between dimensions. Some players
  will experience this as unnecessary friction, but removing it by default would undercut
  the geography and infrastructure goals.
- Portal Depots add queues, capacity, endpoint validation, partial failure, and migration
  complexity. A shipment can be in transit or blocked, so the UI and recovery tools must
  make state explicit.
- Third-party dimensions and portal implementations may not expose stable hooks. Adapters
  and capability checks are needed, and the absence of a compatible endpoint must not crash
  a world or strand goods without a recovery path.
- The model does not automatically make a remote route competitive with every flight path.
  Route throughput and service values need playtesting alongside [ADR 0013](0013-elytra-as-glider.md)
  and [ADR 0018](0018-infrastructure-gated-journey-skipping.md).
- Future work includes endpoint registration, shipment queues, atomic transfer, capacity
  accounting, cross-dimension UI, stale-route recovery, and representative corruption tests.
- We accept that convenience will arrive in stages. That is the intended long arc, not a
  defect to be patched with a global access shortcut.

## Rejected alternatives

### One universal warehouse across all dimensions

This would be easy for players to understand and easy to query, but it would erase the
physical movement of goods and make every settlement a window into the same inventory.
It conflicts directly with the geography and infrastructure principles.

### Per-item teleportation through a network

Allowing any item to move instantly whenever it is tagged for logistics would simplify
throughput and avoid shipment queues. It would also make portals, depots, roads, and
capacity largely decorative and would create a large duplication surface.

### Ender-chest semantics for all logistics

Treating every warehouse like an Ender Chest would preserve familiar behaviour, but it
would turn shared settlement infrastructure into private magical storage. It would also
make multiplayer ownership, permissions, and physical risk meaningless.

### Forbid all cross-dimensional transfer

Total isolation would protect the geographic distinction, but it would make later
infrastructure unable to retire a repeated trip and would split solo progression into
separate economies. Explicit Portal Depots retain the possibility of earned connection.

### A single global currency and item ledger

A global ledger could avoid moving physical goods, but it would collapse storage,
transport, and payment into one unbounded abstraction. The Coin Purse is intentionally
narrower, and bulk goods still need physical logistics.

# Logistics

## Status
**Concept. NOT implemented.** No remote visibility service, request ledger, reservation system, shipment state machine, receiving buffer, or crash-recovery implementation exists. Storage prerequisites are FVR-600..604, Field Guide documentation is FVR-200..204, and a dedicated logistics FVR ticket must be assigned before code.

## Purpose
Logistics turns repeated hauling into earned convenience while keeping goods physical. It lets a player see local stock remotely, request a quantity, reserve it, ship it through a registered route, and receive it at a real destination. It must never become a magical remote withdrawal system.

The design is restart-safe because a five-year world cannot lose or duplicate a shipment when a server stops halfway through moving it.

## Player experience
From a registered warehouse or route terminal, the player can see that a distant store has 480 units of stone and request 128. The interface explains source, destination, route, service time, reservation, and any transport cost. The request does not make the stone appear in the destination immediately.

The source warehouse reserves physical stacks. A carrier, vehicle, or station loads them into a shipment. The shipment travels along an existing physical or infrastructure route and arrives in a receiving buffer. The player then accepts or routes the actual stacks into local storage. Visibility is remote, but the goods still occupy a source, cargo, buffer, or destination inventory at all times.

Cross-dimension movement is not magical. A later Portal Depot may connect networks, but only as a deliberately built and validated infrastructure boundary.

## Core rules
- Remote visibility is read-only. It exposes bounded summaries from registered physical warehouses and does not grant withdrawal rights.
- A request names source, destination, item constraints, amount, requester, expiry, and a unique request ID. It is a proposal until validated.
- A reservation locks specific physical stack handles or an equivalent quantity with a source revision. A category total without a physical reservation is not enough for a shipment.
- A shipment has a manifest, route ID, cargo location, sender, receiver, service status, and transaction revision. It contains real ItemStacks or a durable cargo container.
- A receiving buffer is a physical or explicitly registered holding location. Arrival does not silently merge items into a remote inventory.
- No magical remote withdrawal of bulk goods. Direct access requires presence, while remote movement requires a shipment and a route.
- No cross-dimension magic under ADR 0012. A shipment cannot cross dimensions unless a future Portal Depot capability explicitly owns both ends and validates the transfer.
- Route validation follows bounded, explicit registration. The system does not continuously scan the world for paths or force-load chunks to move cargo.
- Prices, capacity, travel time, loss policy, and service costs are data-driven. Normal delivery must not randomly delete cargo.
- Every state transition is durable and idempotent. Replaying a transition with the same request or shipment ID has no second effect.

The transaction state machine is:

| State | Meaning | Safe restart action |
|---|---|---|
| `DRAFT` | request is being edited | retain or expire without touching goods |
| `SUBMITTED` | player has proposed terms | validate against current permissions |
| `VALIDATED` | source, destination, route, and terms are valid | create reservation or reject |
| `RESERVED` | exact physical goods are locked | retry loading or release on expiry |
| `LOADING` | cargo transfer is in progress | reconcile source, cargo, and manifest |
| `IN_TRANSIT` | cargo is in a shipment on a registered route | advance by service schedule |
| `ARRIVED` | cargo reached destination buffer | expose receiving action |
| `RECEIVING` | stacks move into the destination inventory | finish atomically per stack batch |
| `COMPLETED` | goods and payment are settled | retain audit summary |
| `CANCELLED` or `FAILED` | no delivery will occur | release reservation or preserve cargo |
| `RECONCILING` | crash or inconsistency needs review | never guess, surface operator state |

Crash recovery writes the durable state before each physical mutation. For example, loading first records `LOADING`, source revision, manifest, and reservation IDs. It then removes the reserved stacks and inserts them into the cargo container in one server transaction. On restart, a reconciliation pass compares the reservation, source revision, cargo contents, and manifest. If the cargo is present, it resumes `IN_TRANSIT`. If source goods remain reserved, it retries loading. If neither matches, it marks `RECONCILING` and preserves all evidence for safe manual or deterministic recovery.

Receiving uses the same pattern. It records the destination revision and batch, inserts the exact stacks, then marks the shipment `COMPLETED` and releases the reservation. A retry sees the completed batch ID and does not insert it twice.

## Data ownership
World-scoped saved data owns requests, reservations, shipments, routes, transaction revisions, and receiving buffers. Physical stacks remain in container inventories or cargo entities. The logistics record references them with stable handles, fingerprints, and revisions.

A derived visibility index may cache quantities and route estimates, but it is never authoritative for delivery. Every saved logistics record has a schema version and recovery status. Completed records can be compacted into bounded history after the receiving acknowledgement is durable.

## Server/client responsibilities
The server validates permissions, physical inventory, route status, dimensions, reservations, state transitions, and payment. The client shows proposals, progress, errors, and receiving actions. It cannot create a reservation or mark a shipment arrived.

A client reconnect receives a bounded projection keyed by request or shipment ID. If definitions or route data are stale, the server revalidates and may return a changed cost or failure reason before any physical mutation.

## Dependencies
- FVR-600..604 provide physical warehouse registration, search, and source-of-truth inventory semantics.
- FVR-200..204 document request, reservation, shipment, receiving, and recovery states.
- Settlements and transportation provide registered nodes and route capabilities. Economy supplies optional contracts and payments.
- ADR 0012 forbids universal cross-dimensional warehouse magic and reserves later Portal Depots as explicit infrastructure.
- World chronicle may record major shipments or route openings, but not every item transfer.

## Extension points
A carrier provider can implement walking, horse, boat, rail, or future portal movement behind a route capability. A storage provider can expose atomic physical reservation and stack insertion. A contract adapter can bind a shipment to an economic order.

A future multi-stop freight service may use the same state machine with a bounded manifest and route sequence. It must preserve exactly-once semantics at each hand-off and cannot skip physical receiving buffers.

## Failure cases
- A request sees insufficient stock or an invalid route. It remains uncommitted and names the failed condition.
- A source stack changes after reservation. The shipment pauses for reconciliation rather than replacing it with a similar item.
- A server crash during loading, transit, or receiving uses the durable state machine and manifest comparison described above. It cannot duplicate or delete cargo.
- A route breaks while a shipment is in transit. Cargo remains in the last valid physical location or a recovery buffer, and the shipment becomes `FAILED` or `RECONCILING`.
- A destination is destroyed or loses permission. The shipment enters a receiving recovery state with physical cargo intact.
- A dimension boundary is absent. Cross-dimensional delivery is rejected before reservation unless a validated Portal Depot exists.

## Performance constraints
Visibility queries use summaries and return at most 200 item rows and 32 active shipments per page. Reservation and receiving operate on bounded batches, with a default maximum of 256 stacks per transition.

Recovery examines only non-terminal requests and shipments. A server cycle may reconcile at most 64 shipments per tick budget slice, and no logistics process may force-load a chunk. Route checks are cached and invalidated by explicit infrastructure changes.

## Accessibility and documentation requirements
Every shipment screen shows human-readable state, source, destination, item, quantity, route, cost, expected service time, and the exact next action. Recovery and reconciliation need visible text and an icon, not a colour-only warning.

The Field Guide must explain read-only remote visibility, physical reservations, the state machine, receiving buffers, restart recovery, route requirements, and the cross-dimension rule. A player must be able to tell whether goods are at source, in cargo, in transit, or awaiting receipt.

## Non-goals
- This is not a magical universal warehouse or remote bulk withdrawal button.
- It does not move goods across dimensions without explicit infrastructure.
- It does not simulate unlimited autonomous freight or force-load chunks.
- It does not treat a quantity summary as proof that a physical stack is available.
- It does not hide failed or stranded shipments to keep a progress bar attractive.

## Open balance questions
- What is the right default shipment batch size for a solo player without making hand-carrying pointless?
- Which route failures should pause indefinitely and which should return cargo to a safe source buffer?
- Should receiving be automatic for a trusted local destination or always require confirmation?
- How much service cost and travel time makes infrastructure worthwhile without becoming a tax?
- What Portal Depot rules preserve geography while avoiding needless cross-dimension chores?

## Planned tests
- Execute every state transition and verify invalid transitions cannot mutate goods.
- Crash after each durable write and each physical mutation during request, reservation, loading, transit, arrival, and receiving.
- Re-run recovery multiple times and assert exactly-once stack and payment outcomes.
- Mutate source, route, destination, permissions, and dimension availability while a shipment is active.
- Verify remote visibility never withdraws bulk goods without a physical shipment.
- Bound query pages, batch sizes, recovery work, and route checks with unloaded settlements.
- Test later Portal Depot capability separately and assert ordinary cross-dimension requests remain rejected.

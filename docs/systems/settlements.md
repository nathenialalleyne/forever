# Settlements

## Status
**In Development.** FVR-400 to FVR-405 are implemented: the Settlement Charter, explicit building registration with a bounded volume cap, functional residence validation with no aesthetic scoring, the proximity-attached building graph with outposts, existing-village import, and a migration prototype. Abstract simulation of unloaded settlements is bounded and provably never kills a villager. Not yet covered: custom screens and long-run playtest tuning.

## Purpose
A settlement is a useful social and infrastructure graph, not a radius with an aesthetic score. It gives a player a durable way to turn ordinary buildings into recognised residences, workplaces, warehouses, route anchors, and outposts while leaving the act of building completely free.

The system validates function so that a settlement can support people and services without dictating whether it looks medieval, industrial, modern, improvised, or entirely unlike a village.

## Player experience
The player may build normally from the first minute. When a building is ready to serve a purpose, the player registers it in an explicit Settlement Charter. The charter names the settlement, declares its anchor and intended functions, and lists the buildings the player chooses to include.

A settlement grows as a graph. Buildings become nodes, and registered roads, bridges, rail stations, or other explicit links become edges. A remote farm or mountain cabin can be an outpost connected to the same settlement graph without being forced into one large circle.

Validation answers practical questions: Is there a safe residence with a bed? Is a workplace reachable and functional? Is storage available for the declared service? Is the node close enough to attach, or has an explicit infrastructure link been registered? It never asks whether the roof is attractive.

## Core rules
- A Settlement Charter is the authoritative declaration of identity, ownership or membership, anchor, purpose, and registered graph nodes. It has a schema version and a stable settlement ID.
- Buildings are player-defined and explicitly registered. The system does not require a blueprint, a prescribed block palette, or a minimum aesthetic score.
- Ordinary block placement and building remain available outside any charter. Registration adds services and recognition rather than permission to build.
- A building may declare one or more functional roles such as residence, workplace, warehouse, workshop, farm service, route station, meeting place, or outpost.
- Functional validation checks only the rules for the declared role. Examples include enclosure, usable interior space, bed or rest point, workstation, storage capacity, access, lighting or safety, and relevant connections.
- Membership is graph-based under ADR 0009. Proximity may attach a new node when within a configurable distance of a valid node, but distance alone cannot merge unrelated settlements across a barrier.
- An explicit surveyed road, bridge, rail segment, portal depot, or other approved edge can connect nodes beyond the proximity limit. Edge verification is bounded and cached.
- Residences provide homes for villagers and may support player rest or loadout switching. A residence is not automatically a workplace.
- Workplaces expose a service and a capacity. A villager can have one primary workplace at a time, and a workshop may reject assignments when its functional capacity is full.
- Outposts are small, purpose-limited graph nodes for resource access, route safety, observation, or temporary residence. They need not contain every settlement service.
- Existing natural villages can later enter through an import or charter workflow. They are not silently converted into Forever settlements on world load.
- Charters can be renamed, expanded, or split through explicit actions. A split must preserve ownership, records, and villager assignments or present a migration plan.
- A failed validation leaves the building and its ordinary contents intact. It removes only the claimed service until the function is repaired.

A charter should expose a concise functional record:

| Charter field | Purpose |
|---|---|
| name and stable ID | player-facing identity and save references |
| anchor and graph nodes | geographic membership |
| declared roles | services the settlement intends to provide |
| residences and workplaces | capacity and assignments |
| registered edges | roads, rail, bridges, and other links |
| validation revision | reproducible result and migration support |

## Data ownership
World-scoped saved data owns charters, graph nodes and edges, validation summaries, memberships, and service capacities. Individual building metadata belongs to the registered node and stores a bounded set of positions, role IDs, and a validation revision rather than a full copy of every block.

Villager homes and workplaces reference stable node and service IDs. Physical blocks remain the source of truth for whether a claimed building still exists. A cached validation result must be invalidated by relevant block changes or explicit revalidation.

## Server/client responsibilities
The server accepts registration requests, resolves graph membership, validates function, assigns services, and saves all mutations. The client offers a map and inspection projection with bounded nodes, reasons for validation failure, and proposed links. It cannot mark a building valid locally.

A client may preview a role checklist using server-supplied tags. It must never send an entire building scan as a trust signal. Server messages should identify the smallest failed condition, such as “no reachable bed”, instead of returning a generic rejection.

## Dependencies
- FVR-400..405 cover charter records, building registration, graph membership, validation, residences, workplaces, and outposts.
- FVR-500..507 cover the Mason slice that can create or improve functional building projects.
- FVR-200..204 document charter actions, validation rules, and graph relationships in the Field Guide.
- Villagers depend on residences, workplaces, and settlement membership, while storage and logistics depend on warehouse and route nodes.
- Routes follow ADR 0009 and the explicit survey rules in ADR 0010 where applicable.

## Extension points
A role registry can add a new functional building type with a validator, capacity model, data schema, and Field Guide page. A connection provider can add a road, rail, or future Portal Depot edge without changing proximity rules.

A settlement service may expose stable capabilities to economy, projects, transportation, and villagers. Optional imports can translate a modded village into a proposed charter for player review. No extension may add aesthetic scoring through an unreviewed role validator.

## Failure cases
- A missing anchor or corrupt node ID causes the affected node to become unregistered while the physical building remains untouched.
- A block change invalidates a service. Existing stored items and villagers are preserved, but new assignments pause until revalidation.
- A graph edge is broken or exceeds its declared route evidence. The edge becomes inactive, and dependent journey skipping or shipments stop safely.
- Two players attempt incompatible charter claims. The server keeps the first committed claim and presents a conflict requiring explicit consent or a merge proposal.
- A charter split or migration failure leaves the original graph active and records the pending operation for retry.
- A server restart during registration must commit either the old charter or the complete new revision, never a half-registered building.

## Performance constraints
Registration and validation are explicit operations. The server must not continuously scan all blocks around every charter. A validation request may inspect a bounded volume, with a configurable default of 32,768 blocks and a hard maximum of 262,144 blocks per operation.

Graph membership and route results are cached by node and validation revision. A settlement summary sent to a client should contain at most 512 nodes and 1,024 edges per page. Unloaded settlements use records and capacity arithmetic, not permanent chunk loading.

## Accessibility and documentation requirements
Charter screens must provide text labels for every role and a reason for each validation result. Graph membership must be visible through lines, icons, and a list, not colour alone. The UI must support keyboard navigation and screen-reader-readable role names.

The Field Guide must state that blueprints and aesthetic scoring are not required. It must explain registration, proximity attachment, explicit connections, residence and workplace requirements, outposts, and how to fix a failed validation.

## Non-goals
- This is not a city-builder that controls block placement.
- It does not require mandatory blueprints, architectural themes, beauty ratings, or a medieval visual style.
- It is not a circular claim system that treats every nearby block as one settlement.
- It does not permanently load settlement chunks or simulate every villager physically off-screen.
- It does not convert every natural village without a visible import and charter decision.

## Open balance questions
- What default proximity distances feel helpful without merging neighbouring player settlements?
- Which functional checks are reliable across modded blocks and which need capability adapters?
- How much capacity should an outpost provide before it becomes a hidden full settlement?
- Should a failed service remain usable for current occupants during a grace period?
- What charter merge and split workflow is understandable in multiplayer?

## Planned tests
- Register valid and invalid residences, workplaces, warehouses, workshops, and outposts with no aesthetic checks.
- Verify normal block placement works before, during, and after charter registration.
- Test proximity attachment, explicit long-distance edges, barriers, broken links, and cached revalidation.
- Import a natural village as a proposal and assert that no silent conversion occurs.
- Crash during registration, split, and merge operations and verify atomic revision recovery.
- Cap validation volumes, node and edge pages, and unloaded settlement work without chunk loading.
- Verify two players receive deterministic charter conflict results and that dependent villager and shipment references remain safe.

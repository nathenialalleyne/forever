# Many Roads Home roadmap

This roadmap records the modpack-first pivot. Many Roads Home is a curated modpack of third-party mods, configuration, datapacks, resource packs, optional blueprints, and a small companion integration mod where a documented gap remains. The existing Forever code and prototype remain preserved evidence under [ADR 0034](adr/0034-preserve-existing-custom-code.md). They are not proof that the corresponding pack milestones are complete.

The pinned technical baseline remains Minecraft 26.2, Java 25, Fabric Loader 0.19.3, Fabric API 0.158.0+26.2, Loom 1.17.20, and Matcha Flavoured 1.12. The pack composition will be owned by Packwiz under [ADR 0025](adr/0025-packwiz-source-of-truth.md). No gameplay implementation is claimed by this planning change.

## Current position

### M0: Modpack foundation

**Status: Complete as a decision and preservation gate.** The working identity, product ownership, Packwiz source-of-truth rule, existing-mod escalation order, Matcha loading boundary, information policy, danger and travel constraints, blueprint policy, companion-code gate, and source-preservation rule are recorded in ADRs 0023 through 0034. The old prototype backlog is archived rather than discarded.

The root Fabric and audit foundation from the pre-pivot work remains in the repository. It is useful infrastructure and evidence, but it does not make a Many Roads Home pack release or a gameplay milestone complete.

### M1: Baseline compatibility

**Status: Next.** Run `LAB-01` through `LAB-12` before implementing custom systems. The labs test Matcha onboarding, the dual-role pack loader, building and excavation, blueprints, food, seasons, storage, local transport, rail and vehicles, performance and worldgen, adventure rewards, and the companion boundary. `PACK-01` then assembles the first Packwiz-managed profile from evidence.

M1 exits only when the selected inputs have authoritative compatibility and licence records, the exact Matcha archive loads in both roles, ordinary play remains available, and unresolved gaps are explicit. Global Packs is the current named 26.2 loader candidate, but its All-Rights-Reserved status remains a release risk.

## Planned milestones

### M2: Information and onboarding

**Status: Planned.** Deliver the unified Field Journal and normalise starter books, recipe viewers, contextual information, and advancement signals. A process expected of the player must remain inspectable. Source books may be suppressed only after their instructions and provenance are retained. See `INFO-01` and `INFO-02`.

### M3: Building and gathering

**Status: Planned.** Configure ordinary building, placement, excavation, tree work, regional gathering, cultivation, and project-ready resource paths from existing tools and data. No blueprint, Builder specialisation, quest, or registration may become a toll gate in front of normal construction. See `BUILD-01` and `BUILD-02`.

### M4: Food and seasons

**Status: Planned.** Integrate cooking, readable ingredient traits, alchemy, preservation, and a modest climate layer. Serene Seasons is the first climate target, behind a safe abstraction and fallback. Seasonal inconvenience must be reducible through cultivation, preservation, trade, import, shelter, or greenhouse function. See `FOOD-01` and `FOOD-02`.

### M5: Storage and logistics

**Status: Planned.** Deliver local physical storage search, personal cache rules, local item transport, reservations, shipments, and receiving buffers. Physical inventories remain authoritative. Remote visibility does not create remote withdrawal, and cross-dimensional movement needs an explicit infrastructure boundary. See `STORAGE-01` and `LOGISTICS-01`.

### M6: Transportation

**Status: Planned.** Establish explicit route records and physical services for roads, rail, horses, boats, and selected vehicles. Fast travel is earned only after discovery, physical travel, suitable infrastructure, registration, bounded validation, and a current service. See `TRANS-01` and `TRANS-02`.

### M7: Masteries and equipment

**Status: Planned.** Add evidence-based capability and mastery progression plus long-lived equipment condition, repair, and item specialisation only after existing candidates and the companion gap gate are reviewed. Learning remains permanent, repetition is not mastery, and broken items remain recoverable. See `PROG-01` and `PROG-02`.

### M8: Settlements and villagers

**Status: Planned.** Deliver free-form functional settlement registration, graph connections, outposts, villager careers, migration, mentorship, and bounded unloaded simulation. Buildings are judged by function, not style. Unloaded simulation cannot invent arbitrary villager death. See `SETTLE-01` and `SETTLE-02`.

### M9: Exploration and danger

**Status: Planned.** Add region identity, discovery, meaningful structure outcomes, local hazards, and contextual encounters. Exploration should change knowledge and options rather than become a repeatable loot treadmill. Enemy difficulty must remain local and inspectable, with no global scaling. See `EXP-01` and `EXP-02`.

### M10: Blueprints and construction contracts

**Status: Planned.** Make optional blueprint workflows and style-independent construction contracts useful for large projects. A contract may request a safe residence, bridge, greenhouse, station, warehouse, or similar function, but it must accept multiple layouts and palettes and preserve hand-built participation. See `BLUE-01` and `BLUE-02`.

### M11: Economy and regional networks

**Status: Planned.** Connect physical Obols, Coin Purse balances, player shops, NPC willingness, contracts, regional demand, and route-backed exchange. Prices and demand are data-driven. Goods remain physical, demand is bounded, and solo fulfilment remains viable. See `ECON-01` and `ECON-02`.

### M12: World chronicle and long-term play

**Status: Planned.** Record significant places, people, routes, discoveries, projects, careers, migrations, and notable items without logging every routine action. Then validate backups, migrations, dependency removal, history compaction, and release procedures against disposable worlds. See `CHRON-01` and `CHRON-02`.

## Release gates that apply across the roadmap

1. The compatibility labs come before custom gameplay implementation. A custom companion feature needs the documented alternative and gap analysis in ADR 0033.
2. Packwiz metadata, not a launcher export or local instance, is the composition authority. Every input needs an exact identity, source, licence record, and removal note.
3. Official Matcha remains one pinned archive that supplies both datapack and resource-pack roles. A loader change, archive change, or load-order change requires disposable-world validation.
4. Normal Minecraft building, survival, and solo play remain available. Adventure rewards, blueprints, mastery, seasonal effects, storage, and travel services cannot silently become essential toll gates.
5. Every persistent record needs a schema version, serializer, validation, migration, failure behaviour, recovery path, and tests before a persistent release.
6. The current `forever` mod ID, `dev.forever` package, and persisted identifiers are not renamed by the pivot. Before the first persistent release, the recommended path is a separate identifier migration review.
7. Final art is not part of this roadmap until an approved asset manifest and an accepted gameplay need exist.
8. Every dependency and removal test uses a disposable development world. No real survival world is a test fixture.

## Prototype relationship

The old `FVR-*` tickets remain available in [`backlog-prototype-archive.md`](backlog-prototype-archive.md). They document earlier intent and implementation assumptions. The active [`backlog.md`](backlog.md) is the only queue for new work after this pivot. A future ticket may reuse an old idea only after checking the current ADRs, the compatibility evidence, and the save and removal risks.

The next ticket is `LAB-01`. No later milestone is started in this documentation change.

# LAB-09: Rail, horse, boat, and vehicle compatibility spike

- Date: 2026-08-28
- Run by: lab worker, Modrinth API review and automated dedicated-server/client runs
- Manifest: `labs/manifests/LAB-09-transport-and-vehicles.toml`
- Status: complete for the focused 26.2 compatibility and save-safety review; route-service, multiplayer, full unloaded-transit, and sustained performance tests remain outstanding

## Question

Can maintained transport mods provide physical local travel and freight options while keeping technical play first-class, without becoming instant travel, a global route scan, or a mandatory entrance fee for ordinary play?

## Finding

**Adopt the vanilla movement baseline and add no transport dependency from this spike.** Walking, horses, boats, rails, chest minecarts, and Elytra preserve distinct physical contexts without requiring registration, a network, or a global service. Vanilla rails physically move a chest minecart and preserve its inventory across the controlled save and restart run. This is enough to keep technical play first-class while leaving route registration as an explicit Forever boundary rather than an automatic scan.

No tested mod supplied the required combination of a bounded station or depot endpoint, explicit registration, physical service behavior, durable freight ownership, and safe removal. Minecart Trains Fork is the closest rail-adjacent candidate because it links carts physically, but it is an alpha and exposes pairwise chaining rather than a bounded station or route contract. High-Speed Rail changes physics only and permits extreme speeds. Move Boats only changes boat handling. Horseman has global-looking summon defaults and lost its custom item on provider removal. Automobility adds a broad beta vehicle and assembler surface, increases the recipe count by 75, and leaves custom entities and blocks invalid when removed.

The result is not a rejection of automation or engineered transport. A future route adapter may bind named, explicitly registered vanilla endpoints and preserve physical travel in a loaded route. This lab found no maintained 26.2 candidate that owns that contract safely enough to add now. No custom rail physics, vehicle entity, live pathfinding graph, or global logistics code is justified.

## Exact versions

| Component | Project ID | Version ID | Version | File | Licence | Side |
|---|---|---|---|---|---|---|
| Minecraft | | | 26.2 | | | |
| Fabric Loader | | | 0.19.3 | | | |
| Fabric API | `P7dR8mSH` | `NqwNSxwA` | 0.158.0+26.2 | `fabric-api-0.158.0+26.2.jar` | Apache-2.0 | both |
| Global Packs | `NRLPy2mk` | `DqrPrUMp` | 26.2.0 | `globalpacks-fabric-26.2-26.2.0.jar` | LicenseRef-All-Rights-Reserved | both |
| Matcha Flavoured | `QI0EmgZ1` | `E9rngRfK` | 1.12 | `Matcha_Flavoured_1_12.zip` | CC-BY-NC-SA-4.0 | both |
| Roughly Enough Items | `nfn13YXA` | `4o0NSIMj` | 26.2.820 | `RoughlyEnoughItems-26.2.820.jar` | MIT | both |
| Jade | `nvQzSEkH` | `ue8CO97w` | 26.2.11 | `Jade-mc26.2-Fabric-26.2.11.jar` | CC-BY-NC-SA-4.0 | both |
| Architectury API | `lhGA9TYQ` | `1yQC4VvP` | 21.0.7 | `architectury-fabric-21.0.7.jar` | LGPL-3.0-only | both |
| Cloth Config API | `9s6osm5g` | `Nv3xnWXd` | 26.2.155 | `cloth-config-26.2.155.jar` | LGPL-3.0-only | both |
| High-Speed Rail | `d7PCSOkD` | `UnL9JxtC` | 0.24.0+26.2 | `highspeed-rail-fabric-0.24.0+26.2.jar` | MIT | server |
| Minecart Trains Fork | `PtALnG3G` | `qaPiTACF` | 2.5.0-alpha.3+26.2 | `minecart-trains-fork-2.5.0-alpha.3+26.2.jar` | GPL-3.0-only | both |
| Horseman | `qIv5FhAA` | `xU6ysEWF` | 1.7.5 | `horseman-fabric-26.2-1.7.5.jar` | MIT | both |
| Forge Config API Port | `ohNO6lps` | `rSd3GiG8` | 26.2.1 | `ForgeConfigAPIPort-v26.2.1-mc26.2.x-Fabric.jar` | MPL-2.0 | both |
| Move Boats | `7qPEjpyt` | `P4qWMK4p` | 26.2.0-3.7-fabric+forge+neo | `moveboats-26.2.0-3.7.jar` | LicenseRef-All-Rights-Reserved | server |
| Collective | `e0M1UDsY` | `M75JwjyS` | 26.2.0-8.39-fabric+forge+neo | `collective-26.2.0-8.39.jar` | LicenseRef-All-Rights-Reserved | both |
| Automobility: Unofficial Port | `B6d5S1wQ` | `LRK6PwC8` | 0.5.0-unofficial.29+26.2 | `automobility-0.5.0-unofficial.29+26.2-fabric.jar` | MIT | both |

The candidate files were downloaded from the Modrinth CDN URLs in the manifest. The API SHA-1 and SHA-512 values matched the downloaded files. Local SHA-256 values are also recorded in the manifest. The candidate files were used only below `/tmp/lab09/` and were not copied into `pack/`.

Authoritative API endpoints used:

- Project metadata: `https://api.modrinth.com/v2/project/<project_id>`
- Exact compatibility query: `https://api.modrinth.com/v2/project/<project_id>/version?game_versions=%5B%2226.2%22%5D&loaders=%5B%22fabric%22%5D`
- Search queries with exact `26.2`, Fabric, and mod facets for rail, train, horse, boat, vehicle, freight, and transportation terms

## Candidate discovery

Modrinth metadata, exact version records, release types, environment, licence, source links, and feature descriptions were checked on 2026-08-28. Search results were treated as discovery only. A candidate was treated as an exact compatibility input only when its version record returned Minecraft 26.2 and Fabric.

| Candidate | Exact API result | Initial fit |
|---|---|---|
| High-Speed Rail | `d7PCSOkD` / `UnL9JxtC`, `0.24.0+26.2`, release | Server-side rail physics and speed configuration, with no station or freight data. |
| Minecart Trains Fork | `PtALnG3G` / `qaPiTACF`, `2.5.0-alpha.3+26.2`, alpha | Physical pairwise minecart chaining with client chain rendering. |
| Horseman | `qIv5FhAA` / `xU6ysEWF`, `1.7.5`, release | Horse summon and management convenience, including a custom copper horn. |
| Move Boats | `7qPEjpyt` / `P4qWMK4p`, `26.2.0-3.7-fabric+forge+neo`, release | Physical boat pickup and release convenience, with no new transport endpoint. |
| Automobility: Unofficial Port | `B6d5S1wQ` / `LRK6PwC8`, `0.5.0-unofficial.29+26.2`, beta | Road vehicles and an automobile assembler, with a broad custom content surface. |
| Icy's Better Horses | `XlUm5I57` / `41Q9OmbL`, `1.2.0`, release | Exact 26.2 horse progression, but advertised roster and beyond-range whistle behavior are not local physical service. |
| Copper Rails | `vdIUVEqI` / `XKqqDTlD`, `1.1.1+mc26.2`, release | Rail variants and physics, with no station or freight endpoint in the inspected project. |
| Roads 'n' Vehicles | `XoVPWcRe` / `LpyVQi3a`, `0.1.1+26.2.X`, release | Small decor and ride surface, with no station or freight endpoint in the inspected project. |
| Steam 'n' Rails Continued | `f20AygJv` / `K4oQfESB`, `SNR.FLY-STABLE-1.0.1`, release | Exact 26.2 addon release, but its required Create Fly record is named `26.2-rc-2`. |

### Candidates without a clean exact 26.2 Fabric input

- **Create and Create Fabric:** no usable exact Fabric 26.2 release was returned by the version review. Create is Forge/NeoForge-oriented in the observed records. No substitute was tested.
- **Extended Rails:** no exact verified Fabric 26.2 release was returned by the Modrinth API search/version review. No substitute was tested.
- **Immersive Aircraft:** no exact verified Fabric 26.2 release was returned by the Modrinth API search/version review. No substitute was tested.
- **Better Boat Movement:** the observed candidate was labelled for 26.1 rather than an exact verified 26.2 Fabric release. No substitute was tested.
- **Create Fly plus Steam 'n' Rails Continued:** Steam 'n' Rails has an exact 26.2 Fabric release, but the required Create Fly record is `create-fly-26.2-rc-2-6.0.9-1` and is also listed for `26.2-rc-2`. There was no clean stable 26.2 dependency stack, so no substitute was invented.

### Candidates rejected from LAB-08 and carried forward

LAB-08 already rejected Tesseract and Hopper Plus because their advertised any-distance, cross-dimensional, or teleporting item transfer contradicted ADR 0012. They remain rejected here for the same reason. This vehicle lab did not retest item transport.

## Test world

- Seed: `80808`
- World type and difficulty: normal world, peaceful difficulty
- Game mode: creative
- View distance and simulation distance: 8 each
- Game rules changed: no persistent gameplay-rule changes. Controlled commands created rails, minecarts, horses, boats, and candidate entities or blocks, inspected NBT, saved, stopped, restarted, and copied worlds for provider-removal checks.
- Disposable world paths: `/tmp/lab09/cases/` with separate baseline, candidate, restart, and removal-copy instances
- Confirmed not a real survival world: yes. No Minecraft `saves` directory or user survival world was opened.

Forced chunks were used in the controlled fixture to keep command observations repeatable. This is not evidence that any candidate safely simulates moving entities or freight through unloaded chunks. No candidate was granted permanent chunk loading.

## Client and server configuration

- Dedicated server tested: yes for the clean baseline and all five tested candidates. Every tested candidate reached a normal `Done` line.
- Client tested: yes for the baseline, Automobility, and Minecart Trains Fork. Both candidate clients reached the OpenGL and texture-atlas checks without a candidate-specific fatal error. Screenshots were black on this host, so no human visual inspection or interactive control test is claimed.
- Multiplayer tested: no real player connection. Server-side state was inspected with commands and saved NBT. No permission, ownership, or multiplayer desync claim is made.
- Client environment: OpenGL 4.5 through Mesa 25.2.8. Audio and narrator checks remain unavailable on this host.
- Optional dependencies: Cloth Config was retained in the baseline. Minecart Trains Fork's optional Mod Menu was not added. Automobility's optional Controlify and Momentum were not added.

## Startup outcome

### Clean baseline

The clean baseline reached the LAB-08 reference counts and ready state:

```text
[17:42:59] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[17:43:06] [main/INFO]: Loaded 2346 recipes
[17:43:06] [main/INFO]: Loaded 1805 advancements
[17:43:08] [Server thread/INFO]: Done (1.532s)! For help, type "help"
```

The baseline fixture also demonstrated physical movement and cargo persistence:

```text
[17:43:13] [Server thread/INFO]: Summoned new Minecart with Chest
[17:43:13] [Server thread/INFO]: Summoned new Horse
[17:43:14] [Server thread/INFO]: Summoned new Oak Boat
[17:43:14] [Server thread/INFO]: Minecart with Chest has the following entity data: [4.509999990463257d, 63.0625d, 0.5d]
[17:43:15] [Server thread/INFO]: Minecart with Chest has the following entity data: [{count: 3, Slot: 0b, id: "minecraft:diamond"}]
[17:43:15] [Server thread/INFO]: Horse has the following entity data: [20.5d, 64.0d, 0.5d]
[17:43:16] [Server thread/INFO]: Oak Boat has the following entity data: [40.5d, 64.0d, 0.5d]
[17:43:20] [Server thread/INFO]: Minecart with Chest has the following entity data: [4.509999990463257d, 63.0625d, 0.5d]
[17:43:21] [Server thread/INFO]: Minecart with Chest has the following entity data: [{count: 3, Slot: 0b, id: "minecraft:diamond"}]
[17:43:22] [Server thread/INFO]: Saved the game
```

### Candidate startup counts

| Instance | Recipes | Advancements | Ready line | Candidate-specific issue |
|---|---:|---:|---|---|
| High-Speed Rail | 2346 | 1805 | `Done (1.606s)` | None in startup. |
| Minecart Trains Fork | 2346 | 1805 | `Done (1.821s)` | None in startup. |
| Horseman plus Forge Config API Port | 2347 | 1806 | `Done (1.557s)` | `Couldn't load advancements: [horseman:husbandry/summon_horse]`. |
| Move Boats plus Collective | 2346 | 1805 | `Done (1.526s)` | None in startup. |
| Automobility: Unofficial Port | 2421 | 1805 | `Done (1.594s)` | No startup fatal; recipe count increased by 75. |

Representative candidate lines:

```text
[Server thread/INFO]: Loaded 2347 recipes
[Server thread/INFO]: Loaded 1806 advancements
[Server thread/ERROR]: Couldn't load advancements: [horseman:husbandry/summon_horse]

[Server thread/INFO]: Loaded 2421 recipes
[Server thread/INFO]: Loaded 1805 advancements
```

Every run also repeated the known baseline Matcha noise, including duplicate datapack extraction and invalid generated advancement data:

```text
[main/ERROR]: java.nio.file.FileAlreadyExistsException: ./datapacks/Matcha_Flavoured_1_12.zip
[Worker-Main-2/ERROR]: Couldn't parse data file 'minecraft:advancement/custom/root...'
```

The `frozenlib` warning belongs to the Simple Copper Pipes run from LAB-08 and is not part of the LAB-09 candidate evidence. The Matcha messages above were present in the shared baseline and are not attributed to the vehicle candidates.

## Context and limitations of each travel mode

| Mode | Physical context | Capacity and service limitation | Dimension and authority limitation |
|---|---|---|---|
| Walking | Local movement, scouting, and access to any ordinary block path | No transport service or named endpoint; player inventory is the only ordinary carried inventory | No cross-dimensional transfer; server-authoritative player movement |
| Vanilla horse | Physical overland travel through terrain; useful for local and regional routes | Mount capacity follows vanilla animal rules; no station, schedule, or freight contract | Dimension-local entity; no automatic route registration; server-authoritative entity state |
| Vanilla boat | Physical water travel and shoreline connection | Ordinary boat has no freight endpoint; chest boats remain ordinary vanilla inventory if used | Water-bound and dimension-local; no automatic route registration; server-authoritative entity state |
| Vanilla rail and chest minecart | Fixed physical track and visible cart movement; suitable for engineered technical routes | Vanilla chest minecart provides a 27-slot inventory, but no station, service, permission, or exactly-once shipment contract | Track is local to its dimension; no global scan or cross-dimensional transfer; server-authoritative cart and cargo state |
| Elytra | Gliding, vertical access, scouting, and route discovery | Not a station or freight service; carried inventory remains player inventory | It remains a distinct scouting option and is not removed or made the only fast-travel path |

This keeps technical play first-class without making it mandatory. A player can walk, use a horse or boat, build a rail line, or scout by Elytra. No route, profession, quest, station, or companion screen becomes a toll gate in front of ordinary travel.

## Station, route, and freight endpoint assessment

The ticket requires bounded endpoints suitable for explicit registration rather than automatic discovery. No tested candidate met that requirement.

| Candidate | Observed transport surface | Bounded endpoint or route hook | Result |
|---|---|---|---|
| Vanilla rail and chest minecart | Physical rail movement and chest-minecart inventory | None supplied by vanilla; a future adapter would need explicitly registered named markers | Retain as baseline, do not auto-discover |
| High-Speed Rail | Physics and speed configuration only | No station, depot, route, or freight resource found | No route owner |
| Minecart Trains Fork | Right-click iron-chain linking and axe unlinking; generated server config has `cartSpacing` and `brakingAfterTrainSeparation` | Pairwise cart linkage only; no bounded station or route-registration endpoint | Defer pending a narrow public hook and maturity |
| Horseman | Summon and horse management, with `max_summoning_distance = -1` and `dimension_handling = "ANY"` defaults | No local stable or registered service endpoint | Reject global behavior |
| Move Boats | Crouch/right-click pickup and release of a boat | No station, route, cargo, or service endpoint | No route owner |
| Automobility | Custom road vehicles and an automobile assembler | No bounded station or freight contract found | No route owner |

There was no tested capacity, throughput, service, or permission contract for freight. The only positive freight observation was vanilla chest-minecart inventory persistence in the controlled baseline. No candidate demonstrated a durable source, cargo, buffer, destination, and exactly-once shipment path.

Cross-dimensional travel was not enabled by any tested candidate. Horseman's `ANY` dimension handling is an explicit reason for rejection, not evidence that a cross-dimensional service is acceptable. ADR 0012 requires physical portal depot endpoints and bounded service behavior before such a feature could be considered.

## Horse and boat baseline

The vanilla fixture summoned a horse and an oak boat, moved each in the loaded test world, inspected positions, and saved successfully. Their positions were still present after the controlled wait and save. No new dependency is required to keep these modes available.

Move Boats also started cleanly and preserved an oak boat at `[0.5d,70.0d,0.5d]` after save. Its behavior is physical and does not teleport the boat, but the mod adds handling convenience rather than a route or freight service. Its Modrinth metadata is `LicenseRef-All-Rights-Reserved`, as is its required Collective library, which is a release and redistribution concern for the private pack review. Ordinary vanilla boats remain available.

## Rail and vehicle behavior

### High-Speed Rail

The exact release loaded without changing the 2346/1805 baseline counts. Its generated configuration defaults included `oldMaxSpeed = 8`, `maxSpeed = 8`, gravel values of `oldMaxSpeed = 40` and `maxSpeed = 200`, `isSpeedometerEnabled = true`, and `isIceBoatsEnabled = false`. The packaged data contained physics/mixin configuration, not stations, route records, freight blocks, or endpoints. A long rail fixture did not produce a reliable movement observation, so the vanilla fixture is the movement evidence rather than this mod. The mod also documents that new physics can exceed 1000 at the user's risk. That is too open-ended for a fixed-route baseline without a separate bounded physics review.

### Minecart Trains Fork

The exact alpha release loaded at the baseline counts. Its intended operation is physical: an iron chain links adjacent carts and an axe unlinks them. The server generated `minecart-trains-fork-server.json` with `{"brakingAfterTrainSeparation":true,"cartSpacing":5}`. Client classes include minecart rendering and chain/particle behavior. The client smoke run reached OpenGL and 13 texture atlases without a candidate-specific fatal error.

The disposable server fixture did not prove safe freight movement. The cart left the deliberately small or invalid track fixture and the resulting entity inspection did not provide reliable cargo evidence. There is no station, route registration, endpoint, or freight contract in the inspected release. Keep this as a possible future physical linkage experiment, not as an adopted dependency.

### Automobility: Unofficial Port

The beta port started at 2421 recipes and 1805 advancements. It summoned an `automobility:automobile` entity and persisted an `automobility:automobile_assembler` block entity through restart. It adds a broad custom vehicle and assembler surface rather than a bounded route endpoint. The candidate client reached the OpenGL and texture-atlas checks without a candidate-specific fatal error, but screenshots were black and no controls were interactively reviewed.

## Guidebook, starter item, and recipe-viewer check

No tested transport candidate supplied a verified integrated guidebook or starter item for its route behavior. Horseman supplies a copper-horn recipe item, but not a route explanation or registered service. Automobility supplies vehicle and assembler recipes, but not a guidebook or starter route. High-Speed Rail, Minecart Trains Fork, and Move Boats did not add a guidebook or starter transport item in the inspected test.

REI and Jade remained in the baseline. No candidate-specific recipe-viewer integration was accepted as evidence of a transport service. The absence of a guidebook is another reason not to add a new player-facing vehicle system during this compatibility phase.

## Matcha interaction

The clean reference remained 2346 recipes and 1805 advancements. Candidate counts were:

- High-Speed Rail: 2346 recipes, 1805 advancements, no content-count delta.
- Minecart Trains Fork: 2346 recipes, 1805 advancements, no content-count delta.
- Horseman: 2347 recipes and 1806 advancements. Its `horseman:husbandry/summon_horse` advancement failed to load, so the extra advancement is not clean baseline evidence.
- Move Boats plus Collective: 2346 recipes and 1805 advancements, with no custom recipe or advancement delta observed.
- Automobility: 2421 recipes and 1805 advancements, a delta of 75 recipes and no advancement increase.

No duplicate item or recipe identifier with Matcha was observed in the tested startup logs. Automobility's 75-recipe expansion and Horseman's failed advancement are still compatibility noise that would need a separate content review before adoption. The repeated Matcha `FileAlreadyExistsException`, invalid generated advancement parsing, and failed `main:mechanics/wither_test` requirement were baseline pack errors and were not introduced by these candidates.

## Multiplayer interaction

No real client connected to a dedicated server. Server commands established candidate loading, entity creation, position inspection, and save behavior, but did not establish per-player permissions, ownership, route visibility, or multiplayer desynchronization behavior. Those checks remain open for any future candidate that reaches a pilot.

## Persistence and chunk unloading

| Case | Evidence | Conclusion |
|---|---|---|
| Vanilla chest minecart | Position and three-diamond cargo remained after the controlled wait and save | Positive baseline evidence for physical cargo persistence while loaded |
| Vanilla horse and boat | Entity positions remained available after the controlled save | Positive baseline evidence for ordinary entity persistence |
| High-Speed Rail | Configuration only; no custom freight or endpoint state | No provider persistence contract established |
| Minecart Trains Fork | Server config persisted; fixture movement/cargo inspection was inconclusive | No durable freight claim |
| Horseman | Horse and custom copper horn existed while provider was installed | Provider removal test below shows custom item loss |
| Move Boats | Vanilla boat remained after save; no custom cargo state | Convenience behavior only, not a freight contract |
| Automobility | Automobile entity and assembler block persisted through a restart | Provider removal test below shows custom state becomes invalid or is skipped |

A full in-transit chunk-unload test was not completed. The controlled cases used force-loaded chunks for repeatability, and no candidate demonstrated safe durable transit when a carrier or endpoint chunk unloaded. The report therefore makes no positive unloaded-transit claim. Any future route or freight implementation must keep work bounded, avoid permanent chunk loading, and define what happens to cargo when a loaded entity or endpoint becomes unavailable.

## Removal behavior

Removal was tested on the candidate copies with meaningful custom persistent state.

### Horseman removal

After removing Horseman and Forge Config API Port from a copy, the server reached the baseline counts and saved, but the custom copper horn could not be decoded:

```text
[Server thread/WARN]: [net.minecraft.world.level.block.entity.BlockEntity] Serialization errors:
minecraft:chest // ... BlockPos{x=2,y=64,z=0}: Failed to decode value '{Slot:0b,count:1,id:"horseman:copper_horn"}' from field 'Items' at index 0': Unknown registry key ... horseman:copper_horn
```

The chest then contained no items:

```text
2, 64, 0 has the following block data: {components: {}, x: 2, y: 64, Items: [], z: 0, id: "minecraft:chest"}
```

No recovered item entity was observed. The exact command used for a multi-entity item query was syntactically invalid, so the absence of an item entity is not treated as a complete recovery proof. The chest data loss alone is sufficient to reject provider removal safety.

### Automobility removal

After removing Automobility from a saved copy, the server reached the baseline counts and saved, but custom block and entity state could not be restored:

```text
[Server thread/WARN]: Recoverable errors when loading section [0, 4, 0]: Unknown registry key ... automobility:automobile_assembler -> using default
[Server thread/ERROR]: Failed to read field ... block_entity_type automobility:automobile_assembler
[Server thread/WARN]: Skipping block entity with invalid type: "automobility:automobile_assembler"
[Server thread/WARN]: Skipping Entity with id automobility:automobile
[Server thread/ERROR]: entity decode error unknown registry key
```

The server remained up, but the custom vehicle and assembler were not a safe migration outcome. This is a direct save-removal risk for any future profile change.

High-Speed Rail, Minecart Trains Fork, and Move Boats did not create a provider-specific persistent cargo block in the controlled fixture. Their removal behavior was not credited as safe freight removal because the relevant custom endpoint or cargo state was absent. Vanilla entities remain the baseline fallback, but no provider removal plan is established.

## Performance observations

- Hardware: AMD Ryzen 7 9700X, 8 cores, 19.5 GiB RAM, Mesa OpenGL 4.5.
- Dedicated-server startup observations ranged from 0.738 seconds on the second Horseman run to 1.821 seconds for Minecart Trains Fork. Automobility loaded in 1.594 seconds, High-Speed Rail in 1.606 seconds, Horseman in 1.557 seconds, and Move Boats in 1.526 seconds.
- No tick profiler, memory profile, sustained route test, large-network test, or unloaded-world simulation benchmark was run. Startup time is not a performance bound.
- No candidate was permitted to force-load chunks as a design behavior. The test fixtures used controlled force-loading only to make observations repeatable.

## Conflicts observed

- Horseman defaults include `max_summoning_distance = -1` and `dimension_handling = "ANY"`. That is incompatible with the local, explicit, dimension-scoped route model in ADRs 0010, 0012, 0018, and 0031. Its custom copper horn was also lost from a chest after provider removal.
- Icy's Better Horses advertises beyond-range whistle and roster behavior in its project materials. That would make horse access global or teleport-like rather than an earned local service, so it was rejected without runtime installation.
- Tesseract and Hopper Plus were rejected in LAB-08 for any-distance or cross-dimensional item transport. The same behavior remains incompatible with ADR 0012.
- Automobility adds 75 recipes and custom entity/block IDs. Removing it leaves those IDs invalid or skipped. Its beta unofficial status and broad surface are not suitable for the primary profile.
- Horseman's additional advancement failed to load. This is candidate-specific advancement noise.
- Move Boats and Collective report `LicenseRef-All-Rights-Reserved` in Modrinth metadata. Ordinary vanilla boats do not carry this dependency or licensing uncertainty.
- The repeated Matcha extraction and advancement errors were present in the baseline and are not attributed to the transport candidates.

## Does it fit the pack?

The vanilla modes fit. They preserve physical travel, technical construction, ordinary survival, and clear context without adding a global registry or a new save format. Rails and chest minecarts provide a sufficient technical foundation for future explicit route registration. A route overlay can name endpoints without taking ownership of vanilla movement or discovering every rail in the world.

The tested mods do not fit the primary profile:

- High-Speed Rail is a physics modifier without bounded station or freight semantics, and its documented extreme-speed mode needs a separate safety and balance decision.
- Minecart Trains Fork is promising physical linkage but is alpha, lacks station and freight hooks, and did not prove cargo behavior in the fixture.
- Horseman and Icy's Better Horses undermine local physical service through global-looking access. Horseman also fails removal safety.
- Move Boats is a convenience interaction, not a transport service, and its metadata licence is not suitable for an unreviewed primary dependency.
- Automobility is an unofficial beta port with a broad custom entity/block and recipe surface and unsafe provider removal.
- Create-based rail and aircraft candidates were not given invented compatibility substitutes when the exact 26.2 Fabric dependency stack was absent.

These decisions do not contradict the approved ADRs. They reinforce them. Horseman's defaults and the LAB-08 Tesseract/Hopper Plus candidates are examples of behavior that would contradict ADR 0010 or ADR 0012 if adopted. No ADR was silently changed.

## Follow-up required

1. Retain vanilla walking, horses, boats, rails, chest minecarts, and Elytra as the transport baseline.
2. Do not add a transport or vehicle dependency to `pack/` from LAB-09.
3. If a later route adapter is proposed, require named endpoint markers, explicit registration, bounded validation, dimension-scoped service records, visible failure, and a recovery plan for cargo and entities before implementation.
4. Test any future rail or freight pilot for disconnected track, station connectivity, route damage, endpoint unload, restart during transit, permissions, duplicate shipment requests, and provider removal. The pilot must not force-load chunks or scan global routes.
5. Preserve Elytra as scouting and vertical movement. Do not turn a route system into a universal fast-travel menu.
6. The next backlog ticket is **LAB-12: Companion boundary and gap-analysis compatibility spike**. It should consume this report and the other lab results before considering any companion boundary. It is not started by this change.

## Decision

| Candidate or mode | Verdict | Reason |
|---|---|---|
| Vanilla walking | `ADOPT_BASELINE` | Local, always available movement with no dependency, endpoint, route scan, or save migration. |
| Vanilla horses | `ADOPT_BASELINE` | Physical overland travel remains useful without global summon behavior or a new dependency. |
| Vanilla boats | `ADOPT_BASELINE` | Physical water travel remains available; ordinary boats do not require a transport service. |
| Vanilla rails and chest minecarts | `ADOPT_BASELINE` | Loaded rails physically move a chest minecart and preserve its 27-slot inventory in the controlled save, while leaving route registration explicit. |
| Vanilla Elytra | `ADOPT_BASELINE` | Retains a distinct gliding, scouting, and vertical role rather than becoming a fixed-route monopoly. |
| High-Speed Rail `d7PCSOkD` | `REJECT` | Physics-only candidate with no bounded station or freight endpoint and documented extreme-speed behavior. |
| Minecart Trains Fork `PtALnG3G` | `DEFER` | Physical cart linkage is promising, but the alpha release lacks route/freight hooks and did not prove cargo safety in the fixture. |
| Horseman `qIv5FhAA` | `REJECT` | Default unlimited and any-dimension summon behavior conflicts with local route ADRs, and custom item data was lost on removal. |
| Move Boats `7qPEjpyt` | `REJECT` | Physical boat handling is compatible but adds no route or freight endpoint and carries an All-Rights-Reserved dependency stack. |
| Automobility: Unofficial Port `B6d5S1wQ` | `REJECT` | Unofficial beta with 75 extra recipes, broad custom state, no bounded endpoint, and invalid custom entities/blocks after removal. |
| Icy's Better Horses `XlUm5I57` | `REJECT` | Advertised roster and beyond-range whistle behavior are global or teleport-like rather than local physical service. |
| Copper Rails `vdIUVEqI` | `DEFER` | Exact 26.2 release exists, but inspected scope is rail variants/physics with no bounded station or freight contract. |
| Roads 'n' Vehicles `XoVPWcRe` | `DEFER` | Exact 26.2 release exists, but inspected scope has no station, route, or freight endpoint. |
| Create Fly plus Steam 'n' Rails Continued | `DEFER` | Addon has an exact 26.2 release, but the required Create Fly input is `26.2-rc-2`; no clean stable stack was tested. |
| Create rail / Create Fabric | `DEFER` | No usable exact Fabric 26.2 release was verified, so no substitute was tested. |
| Extended Rails | `DEFER` | No exact verified Fabric 26.2 release was found through the Modrinth API; no substitute was tested. |
| Immersive Aircraft | `DEFER` | No exact verified Fabric 26.2 release was found through the Modrinth API; no substitute was tested. |
| Better Boat Movement | `DEFER` | Observed candidate was labelled for 26.1 rather than an exact verified 26.2 Fabric release. |
| Tesseract `OUhp5O2m` | `REJECT` | LAB-08 found any-distance and cross-dimensional item transport, contradicting ADR 0012 without a physical depot. |
| Hopper Plus `t4nmH7xE` | `REJECT` | LAB-08 found distance-teleporting item transport, contradicting ADR 0012. |

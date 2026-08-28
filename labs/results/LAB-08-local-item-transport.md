# LAB-08: Local item transport and automation compatibility spike

- Date: 2026-08-28
- Run by: lab worker, Modrinth API review and automated dedicated-server runs
- Manifest: `labs/manifests/LAB-08-local-item-transport.toml`
- Status: complete for the dedicated-server and save-safety question; client rendering, multiplayer permissions, and large-network profiling are blocked or outstanding

## Question

Can a maintained mod provide physical local item transport that keeps technical play first-class, without becoming teleporting logistics or losing player-owned cargo when a network or chunk is removed?

## Finding

**Yes, 26.2 Fabric mods can provide physical local transport, but no tested candidate is ready for unconditional baseline adoption.** Logistics: Automation is the best maintained, source-available candidate for a narrowly configured physical-pipe pilot. Its traveling items are stored in the pipe block entity, move over time, survive controlled chunk unload and restart, and are dropped when the pipe is removed. It also ships provider, requester, and autocrafting networks whose graph has no inspected maximum and whose permission and duplicate-request behavior were not verified. Those features must not be enabled as an implicit warehouse or route authority.

Copper Item Pipes is the closest mechanical match to the visible requirement. It moves an item display one copper grate at a time, has no recipe or advancement footprint, survives unload and restart, and drops cargo when a grate is removed. It is currently unlisted, has no source URL or issue tracker in its Modrinth metadata, and only supports hopper-to-copper-grate paths. It is therefore **not maintenance-ready evidence** for the primary pack.

Ductwork is the safest bounded automation candidate in this run. It preserves ordinary hoppers, supports a vanilla chest minecart, and safely retains cargo on unload, restart, and removal. Its client source contains no item-in-duct renderer, so it does not satisfy this lab's explicit visible-in-transit requirement. Classic Pipes visibly stores progress-bearing packets and interoperates with a vanilla hopper, but a server-side block removal silently lost the packets in transit and its current release logs four failed advancement loads. Simple Copper Pipes moved items and passed the sample unload and removal tests, but its required FrozenLib produced a persistent registry error, its source freshness does not match the current release, and the existing candidate matrix already records an upstream extreme-duplication issue that this small test did not reproduce.

Technical play was treated as a first-class route throughout. Ductwork's hopper and chest-minecart behavior was positive evidence, and Logistics was not rejected merely because it offers automation. The rejections are for visibility, boundedness, startup errors, provenance, or player-property safety. Vanilla hoppers remain available without any mod dependency.

| Candidate | Decision | Short reason |
|---|---|---|
| Logistics: Automation `JHnkVzMU` | `PILOT` | Physical pipe subset is promising, but provider/requester/autocrafting must be excluded until graph bounds, permissions, and duplicate requests are proven. |
| Copper Item Pipes `NNEAuaYK` | `NEEDS_MORE_EVIDENCE` | Best visible local behavior, but unlisted, source-unavailable, hopper-only, and not independently maintainable from the API evidence. |
| Ductwork `XCNsSwQj` | `REJECT` for this lab | Bounded and safe, but no visible item-in-duct rendering was found, which fails the stated physical-visible requirement. |
| Classic Pipes `5IRtOFZj` | `REJECT` | In-pipe cargo was lost on server-side block removal, and four candidate advancements failed to load. |
| Simple Copper Pipes `CEH3bXSV` | `REJECT` for the current baseline | Sample transfer and removal were safe, but FrozenLib has a persistent registry error and the existing duplication warning remains unresolved. |
| Vanilla hoppers and chest minecarts | Retain as baseline | Ordinary technical transport remains usable without registration, a network, or a new dependency. |

No custom pipes, shipment state machine, remote withdrawal, or companion Java code is justified by this lab.

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
| Simple Copper Pipes | `9r4ZkgSN` | `CEH3bXSV` | 2.1.7-mc26.2 | `SimpleCopperPipes-mc26.2-2.1.7.jar` | MIT | both |
| FrozenLib | `9KawNmQc` | `Ak4sHFuc` | 2.5.3-mc26.2 | `FrozenLib-2.5.3-mc26.2.jar` | GPL-3.0-only | both |
| Classic Pipes | `xX5VOqpH` | `5IRtOFZj` | 1.1.7 | `classicpipes-fabric-26.2-1.1.7.jar` | CC-BY-NC-SA-4.0 | both |
| Ductwork | `E5Qy8rmu` | `XCNsSwQj` | 0.13.0 | `ductwork-0.13.0.jar` | MIT | both |
| Cooldown Coordinator | `2lfPteTN` | `GMuuCYlJ` | 0.10.0 | `cooldown-coordinator-0.10.0.jar` | Apache-2.0 | both |
| Copper Item Pipes | `QItx9xE2` | `NNEAuaYK` | 1.1.0+mod | `copper-item-pipes-1.1.0.jar` | MIT | server |
| Logistics: Automation | `cyW2SS1x` | `JHnkVzMU` | 0.8.6+mc26.2.fabric | `logistics-0.8.6+mc26.2.fabric.jar` | MIT | both |

The exact candidate files were downloaded from the Modrinth CDN URLs in the manifest. The API SHA-1 and SHA-512 values matched the downloaded files. Local SHA-256 values are also recorded in the manifest.

Authoritative API endpoints used:

- Project metadata: `https://api.modrinth.com/v2/project/<project_id>`
- Exact compatibility query: `https://api.modrinth.com/v2/project/<project_id>/version?game_versions=%5B%2226.2%22%5D&loaders=%5B%22fabric%22%5D`
- Search queries with exact `26.2`, Fabric, and mod facets for `pipe`, `item_pipe`, `item_transport`, `duct`, `hopper`, `conveyor`, `automation`, and `logistics`

## Candidate discovery

Modrinth metadata, exact version records, release types, environment, licence, source links, and feature descriptions were checked on 2026-08-28. Search results were treated as discovery only. A candidate was not treated as compatible until its exact version endpoint returned a release for Minecraft 26.2 and Fabric, or the reason for deferral was recorded.

| Candidate | Exact API result | Initial fit |
|---|---|---|
| Simple Copper Pipes | `9r4ZkgSN` / `CEH3bXSV`, `2.1.7-mc26.2` | Physical copper pipe blocks, adjacent inventory transfer, required FrozenLib. |
| Classic Pipes | `xX5VOqpH` / `5IRtOFZj`, `1.1.7` | Physical item and fluid pipes, progress-bearing packets, optional networked request and autocrafting features. |
| Ductwork | `E5Qy8rmu` / `XCNsSwQj`, `0.13.0` | Local ducts and collectors using the Fabric Transfer API, intentionally compatible with vanilla hoppers. |
| Copper Item Pipes | `QItx9xE2` / `NNEAuaYK`, `1.1.0+mod` | Embedded datapack that moves item displays through copper grates. No source URL or issue tracker in the API metadata. |
| Logistics: Automation | `cyW2SS1x` / `JHnkVzMU`, `0.8.6+mc26.2.fabric` | Source-available physical traveling items plus provider, requester, and autocrafting networks. |

### Named candidates not usable for this exact Fabric baseline

- **Create**, project `LNytGWDc`, and **Create Fabric**, project `Xbc0uyRg`: the exact 26.2 Fabric queries returned no usable release. Create is Forge/NeoForge-oriented, and the official project uses `LicenseRef-Create-Mod-License`.
- **Pipez**, project `iRmWy6ga`: no exact Fabric 26.2 release. The observed loader targets are Forge and NeoForge, and the licence is `LicenseRef-All-Rights-Reserved`.
- **Integrated Dynamics**, project `yYzdQHJI`, and **Integrated Tunnels**, project `Etqy1Omb`: no usable exact Fabric 26.2 release. The observed projects target Forge and NeoForge.
- **Modern Industrialization**, project `Gov5Dboq`: no usable exact Fabric 26.2 release. Its project description says Fabric support stops at Minecraft 1.20.4.
- **Tesseract**, project `OUhp5O2m`, release `AETPj8j4`: exact 26.2 Fabric exists, but the project advertises item transport any distance and across dimensions. That is rejected under ADR 0012 without an explicit physical depot.
- **Hopper Plus**, project `t4nmH7xE`, release `qHMihi82`: exact 26.2 Fabric exists, but its project description explicitly advertises teleporting items across distances.

BuildCraft Refabricated, Hydrofarm, and Redstone Additions have exact 26.2 Fabric-family releases but were not included in this focused run. They have broad surfaces, and BuildCraft Refabricated declares a different Fabric API dependency. Their reasons are preserved in the manifest's `[not_tested]` section.

## Test world

- Seed: `80808`
- World type and difficulty: normal world, peaceful difficulty
- Game mode: creative
- View distance and simulation distance: 8 each
- Game rules changed: no persistent gameplay-rule changes. Test commands used controlled tick freezing, block placement, NBT inspection, force-load removal and re-addition, and server stop/restart.
- Disposable world paths: `/tmp/lab08/cases/` with separate baseline, candidate, transfer, unload, restart, minecart, and removal-copy instances
- Confirmed not a real survival world: yes. Every instance was created below `/tmp/lab08/` and no Minecraft `saves` directory or user survival world was opened.

The baseline and candidate runs used the same fixed baseline inputs. No candidate JAR was copied into `pack/`, and no third-party binary was added to the repository.

## Client and server configuration

- Dedicated server tested: yes for the clean baseline and all five tested candidates. Every tested candidate reached a normal `Done` line.
- Client tested: no. The host has no usable display. Rendered screenshots, recipe-viewer screens, tooltips, pipe configuration screens, and player break paths requiring a client could not be tested.
- Multiplayer tested: no real player connection. Server-side item state and endpoint state were inspected with commands and saved NBT. No permission or multi-player desync claim is made.
- Baseline optional dependencies: REI, Jade, Architectury, and Cloth Config were retained from LAB-01. JEI was not added. Classic Pipes contains a JEI plugin entry, but no JEI instance was present.

## Startup outcome

### Clean baseline

The clean baseline reached the expected reference counts and ready state:

```text
[16:34:10] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[16:34:15] [main/INFO]: Loaded 2346 recipes
[16:34:15] [main/INFO]: Loaded 1805 advancements
[16:34:16] [Server thread/INFO]: Done (0.280s)! For help, type "help"
```

### Candidate counts

| Instance | Candidate delta from baseline | Recipes | Advancements | Ready line | Candidate-specific startup issue |
|---|---:|---:|---:|---|---|
| Simple Copper Pipes | `+10 / +10` | 2356 | 1815 | yes, `Done (0.203s)` in the clean candidate boot | Persistent `Registry 'frozenlib:wind_manager_extension_type' was empty after loading`. First boot also logged 13 unread config entries before the config was generated. |
| Logistics: Automation | `+589 / +0` | 2935 | 1805 | yes, `Done (2.795s)` | No candidate-specific startup error in the clean boot. The large recipe increase is a significant pack surface. |
| Ductwork | `+3 / +3` | 2349 | 1808 | yes, `Done (2.341s)` | No candidate-specific startup error. |
| Classic Pipes | `+93 / +57` | 2439 | 1862 | yes, `Done (3.967s)` | `Couldn't load advancements: [classicpipes:request_item, classicpipes:autocraft_recipe_pipe, classicpipes:autocraft_eight_steps, classicpipes:obtain_pipe]`. |
| Copper Item Pipes | `+0 / +0` | 2346 | 1805 | yes, `Done (0.189s)` | No candidate-specific startup error. |

All candidate boots also reproduced diagnostics already present in the Matcha and Global Packs baseline. Representative lines were:

```text
[main/ERROR]: java.nio.file.FileAlreadyExistsException: ./datapacks/Matcha_Flavoured_1_12.zip
[Worker-Main-*/WARN]: Invalid path in datapack: minecraft:dimension_type/I_HATE_TRUE_DARKNESS.txt, ignoring
[Worker-Main-*/ERROR]: Couldn't parse data file 'minecraft:advancement/custom/root' from 'minecraft:loot_table/advancement/custom/root.json': DataResult.Error['Not a JSON object: null']
[Worker-Main-*/WARN]: Found loot table element validation problem in {minecraft:chests/ruined_portal@minecraft:loot_table}.pools[0].entries[11]: Missing element minecraft:chests/food/enchanted_golden_apple of type minecraft:loot_table
[Worker-Main-*/ERROR]: Couldn't parse data file 'main:mechanics/wither_test' from 'main:advancement/mechanics/wither_test.json': DataResult.Error['Advancement completion requirements did not exactly match specified criteria. Missing: [summoned_wither]. Unknown: [eat_glow_jam]']
```

The Java launcher also printed its restricted-native-access and `Unsafe` deprecation warnings. They appeared independently of the candidate and were not treated as candidate failures.

## Physical transport and automation behavior

Every transfer used a known 16-diamond quantity and checked the source, transport or buffer, and destination. Counts below are server-side observations. The host could not provide a client screenshot, so a visual claim is made only where a rendered or display-entity mechanism was confirmed by source or runtime data.

### Simple Copper Pipes

A chest at `(0,64,0)`, a copper pipe at `(1,64,0)`, and a destination chest at `(2,64,0)` were used. The source and destination remained ordinary chests. A frozen mid-run snapshot showed the candidate's physical pipe inventory:

```text
1, 64, 0 has the following block data: ... Items: [{count: 1, Slot: 0b, id: "minecraft:diamond"}] ... id: "simple_copper_pipes:copper_pipe"
```

The final snapshot showed the source empty, the pipe empty, and the destination holding all 16 diamonds. The source implementation stores a five-slot block-entity inventory and transfers over ticks. No dedicated item-in-pipe renderer was found in the inspected source, so the explicit visible-over-time requirement was not established for this candidate.

The candidate's README supports insertion into chests, hoppers, barrels, and other inventories. The sample transfer did not use a minecart. No cross-dimensional or wireless link was found in the inspected physical path.

### Classic Pipes

A copper pipe was placed between a source chest and a destination vanilla hopper. A redstone block was required by this pipe variant to enable transfer. After the test was frozen, the pipe contained four independent packet records with distinct progress values while the source held seven diamonds and the hopper held five:

```text
0, 64, 1 has the following block data: ... id: "classicpipes:copper_pipe", items: [{item: {count: 1, id: "minecraft:diamond"}, ... progress: 1792s, speed: 64s, ...}, {item: {count: 1, id: "minecraft:diamond"}, ... progress: 1216s, ...}, {item: {count: 1, id: "minecraft:diamond"}, ... progress: 640s, ...}, {item: {count: 1, id: "minecraft:diamond"}, ... progress: 64s, ...}]
0, 64, 0 has the following block data: ... Items: [{count: 7, Slot: 0b, id: "minecraft:diamond"}] ...
0, 64, 2 has the following block data: ... Items: [{count: 5, Slot: 0b, id: "minecraft:diamond"}] ... id: "minecraft:hopper"
```

This is strong server evidence of time-based physical packet movement and vanilla hopper interoperability. The source also contains networked sorting, fetching, and autocrafting features. Its network graph is self-discovered rather than explicitly registered by the pack, and no graph bound or permission test was established.

### Ductwork

Ductwork was tested with `vanilla=true`, `placement=false`, and `cheaper=false` in its config. A source chest fed a Ductwork collector, which fed a vanilla hopper. After one second the source held 13, the collector held one, and the hopper held two, preserving the total 16:

```text
0, 64, 0 has the following block data: ... Items: [{count: 13, Slot: 0b, id: "minecraft:diamond"}] ... id: "minecraft:chest"
0, 64, 1 has the following block data: ... Items: [{count: 1, Slot: 0b, id: "minecraft:diamond"}] ... id: "ductwork:collector"
0, 64, 2 has the following block data: ... Items: [{count: 2, Slot: 0b, id: "minecraft:diamond"}] ... id: "minecraft:hopper"
```

The candidate README describes one-slot ducts, five-slot collectors, dampers, and local adjacent transfer. The source uses the Fabric Transfer API. `DuctworkClient` registers screens and a built-in resource pack, but no item-in-duct or block-entity item renderer was found. It therefore provides physical server-side buffers and time-based transfer, but not the requested visible item movement.

Ductwork did support a vanilla chest minecart. After the minecart settled on the test track, the source minecart held 13 diamonds, the collector held one, and the destination hopper held two:

```text
Minecart with Chest has the following entity data: ... Items: [{count: 13, Slot: 0b, id: "minecraft:diamond"}] ...
0, 63, 1 has the following block data: ... Items: [{count: 1, Slot: 0b, id: "minecraft:diamond"}] ... id: "ductwork:collector"
0, 63, 2 has the following block data: ... Items: [{count: 2, Slot: 0b, id: "minecraft:diamond"}] ... id: "minecraft:hopper"
```

This is positive evidence that technical play and vanilla cargo vehicles remain first-class. It is not a reason to waive the current visual requirement.

### Copper Item Pipes

Copper Item Pipes is an embedded datapack in a mod JAR. It modifies vanilla copper grates rather than adding a large new block catalogue. The functions create an `item_display` carrying the actual item and schedule movement one grate at a time. A 20-grate path from a source hopper to a destination hopper produced this runtime state while frozen:

```text
Item Display has the following entity data: ... item: {count: 16, id: "minecraft:diamond"} ... Pos: [0.5d, 64.375d, 5.5d] ... Tags: ["cip_display"]
```

The display then reached the destination hopper with all 16 diamonds. Removing a grate while the display was in transit converted the display into a normal dropped item entity containing all 16 diamonds. This is physically inspectable and visible block by block, although the implementation uses a server teleport command between adjacent grates. It does not provide remote inventory access, an any-distance link, or a cross-dimensional bridge.

Only hopper insertion and extraction were exercised. The project documentation does not provide a minecart endpoint. No new recipe, advancement, item, or block was registered by this candidate.

### Logistics: Automation

The candidate documentation advertises visible traveling items, and the source stores a `TravelingItem` in `LogisticsData.ItemsInTransit`. A controlled server-side injection was used to isolate transit because no client or real player was available to construct the full UI flow. The item was stored in the candidate's own pipe state and reached a vanilla inventory endpoint in the transfer run. A mid-transit unload run captured:

```text
0, 64, 16 has the following block data: {components: {}, LogisticsData: {ConnectionTypes: {south: "inventory"}, ItemsInTransit: [{item: {count: 16, id: "minecraft:diamond"}, progress: 0.08f, direction: 3}]}, x: 0, y: 64, z: 16, id: "logistics:pipe/pipe"}
```

This is actual persisted cargo in the pipe, not a remote inventory count. The source also shows a bounded per-pipe virtual capacity and a `preRemoveSideEffects` path that drops traveling items. The weakness is the network boundary. `NetworkRegistry` recursively flood-fills connected pipes without an inspected maximum node count or route registration boundary. The mod also includes provider, requester, and autocrafting modules. Those features can turn a physical pipe network into an implicit same-dimension warehouse unless the pack excludes them and adds a permission and request contract.

The tested network was dimension-local. The registry is owned by a `Level`, and no cross-dimensional bridge was found in the inspected release. This is compatible with ADR 0012 only if the pack does not add a hidden bridge and does not treat a provider or requester as a cross-dimensional warehouse.

## Chunk unloading and restart

The chunk tests deliberately placed cargo in a chunk at `z=16` or spread a path across multiple chunks, removed the force-load ticket, waited, re-added the ticket, and then restarted the server. No candidate was allowed to force-load the test path permanently.

| Candidate | Before unload | After unload and reload | After restart | Result |
|---|---|---|---|---|
| Simple Copper Pipes | Pipe block entity held 16 diamonds | Same 16 remained in `Items` | Same 16 remained | No observed cargo loss |
| Classic Pipes | Basic pipe held one packet record containing 16 diamonds | Same packet remained in `items` | Same packet remained | No observed cargo loss while unloaded |
| Ductwork | Collector held two diamonds and source held 14 | Same split remained after reload | Same split remained | No observed cargo loss |
| Copper Item Pipes | Display held 16 diamonds at approximately `z=18.5` | Same display UUID and item reappeared and progressed to approximately `z=32.5` | Same display UUID and count survived a controlled restart | No observed cargo loss |
| Logistics: Automation | `ItemsInTransit` held 16 diamonds at progress `0.08f` | Same pipe state remained after reload | Same item remained in `ItemsInTransit` | No observed cargo loss |

The Copper Item Pipes display was not found while its chunk was unloaded, which is expected for a chunk-owned entity. It reappeared with the same UUID and item after the path was loaded again. No test used a permanent chunk loader.

## Vanilla hopper and minecart interaction

- **Vanilla hoppers:** Simple Copper Pipes, Classic Pipes, Ductwork, Copper Item Pipes, and the Logistics endpoint path all interacted with ordinary hopper blocks in the server tests. Classic Pipes required a powered copper pipe for the selected test. Ductwork's collector-to-hopper path was the cleanest full-count test.
- **Vanilla chest minecarts:** Ductwork passed a 16-diamond chest-minecart to collector to hopper flow with the final split `13 / 1 / 2`. This shows that an existing technical cargo vehicle remains useful. The other candidates were not separately tested with minecart endpoints.
- **Ordinary inventory use:** Every candidate was added only to an isolated test instance. Vanilla chests and hoppers remained usable, and the clean baseline started without any transport mod. No candidate was allowed to become a prerequisite for opening or using an ordinary inventory.

## Persistence

The candidates wrote different kinds of server-owned state:

| Candidate | Persistent state observed | Save-safety observation |
|---|---|---|
| Simple Copper Pipes | Pipe block-entity `Items`, transfer cooldowns, and pipe state | Cargo survived restart. Dropped block removal recovered the item as normal item entities. |
| Classic Pipes | Pipe block-entity `items` records containing item, direction, speed, progress, age, and ejection state | Cargo survived restart, but the removal path below lost it for server-side block removal. |
| Ductwork | Collector or duct block-entity inventory and transfer cooldown | Cargo survived restart and was dropped on removal. |
| Copper Item Pipes | Vanilla `item_display` entity with item, UUID, tag, and position, plus scheduled function state | The display and item survived unload and restart. Missing-grate handling dropped the item. |
| Logistics: Automation | Pipe block-entity `LogisticsData`, connection types, and `ItemsInTransit` records | Traveling item codec and saved NBT survived unload and restart. Removal code dropped the cargo in the tested path. |

No candidate was treated as a replacement for the physical source-of-truth rule from LAB-07. The item stack remains in a physical inventory, pipe buffer, entity, or destination, rather than being represented only by an index.

## Removal behaviour

### Removing a transport block while cargo is in transit

These tests used server-side `/setblock ... air destroy` on disposable worlds while 16 diamonds were in or associated with the transport path.

| Candidate | Observed result |
|---|---|
| Simple Copper Pipes | Safe in the sample. The pipe block item dropped, and the 16 diamonds appeared as two normal item entities with counts 11 and 5. |
| Classic Pipes | Unsafe for this removal path. The pipe block item dropped, but four diamonds in the pipe disappeared. The source held seven and the hopper held five, so the observed total was 12 rather than 16. Source inspection shows a player-specific `playerWillDestroy` drop path, but the server-side removal path was not safe. |
| Ductwork | Safe. Removing the collector dropped the collector block and a diamond item entity containing the buffered cargo. |
| Copper Item Pipes | Safe. Removing a grate converted the display into a normal diamond item entity containing all 16. |
| Logistics: Automation | Safe in the tested pipe path. Removing the pipe dropped the pipe block and a diamond item entity containing the traveling 16-stack. |

Classic Pipes is rejected even though its normal player-break path may differ. The pack cannot assume that commands, explosions, automation, chunk repair, or administrative removal will always use the one safe path.

### Removing the provider from a populated disposable save

A copy of each populated disposable world was booted after removing the candidate JAR and its required library where applicable. Every copy reached a `Done` line and returned to the baseline counts, but none was cleanly migration-safe:

- Simple Copper Pipes: `Missing data pack frozenlib` and `Missing data pack simple_copper_pipes`, followed by an unknown `simple_copper_pipes:copper_pipe` item entity decode.
- Classic Pipes: `Missing data pack classicpipes`, followed by an unknown `classicpipes:oak_pipe` item entity decode.
- Ductwork: `Missing data pack ductwork`, followed by an unknown `ductwork:collector` item entity decode.
- Logistics: multiple unknown Logistics ore blocks were replaced with defaults, and the saved `logistics:pipe/pipe` block entity was skipped as an invalid type.
- Copper Item Pipes: `Missing data pack mr_copper_itempipes`, but its path uses vanilla copper grates and the server reached `Done` at 2346 recipes and 1805 advancements.

This is not a recommendation to remove a selected provider from a real world. It demonstrates that mod removal can strand block items, replace generated blocks, or discard block entities even when the server continues booting. A future profile change needs a drain, recovery, or migration procedure before any provider is removed.

## Guidebook and starter-item check

No tested candidate shipped a second guidebook or a starter book in its installed resources.

- Simple Copper Pipes and FrozenLib contain configuration and block text, but no guidebook asset or first-join manual.
- Classic Pipes links to an external wiki and contains no in-pack guidebook.
- Ductwork contains screens and a built-in resource pack, but no guidebook.
- Logistics contains recipe and module text, but no starter manual.
- Copper Item Pipes is an embedded transport datapack with no book, starter item, or recipe-viewer content.

A client or real first-join player was not available, so a literal inventory observation was not possible. The resource and server-start inspection found no starter item grant. Matcha itself remains the onboarding authority from LAB-01.

## Matcha interaction

No duplicate recipe or advancement identifier was observed between Matcha and the tested candidates. The counts show the candidate footprint:

- Copper Item Pipes added zero recipes and zero advancements.
- Ductwork added three recipes and three advancements.
- Simple Copper Pipes added ten recipes and ten advancements.
- Classic Pipes added 93 recipes and 57 advancement count entries, while four of its named advancements failed to load.
- Logistics added 589 recipes and no additional advancement count.

No candidate added a second recipe viewer. REI remained the selected viewer, and JEI was not installed. Classic Pipes' `jei_mod_plugin` was present in the JAR but inactive without JEI. No candidate's guidebook competed with Matcha's instruction path.

The common Matcha parse and Global Packs duplicate-file diagnostics were present in the clean baseline as well as candidate runs. They were not attributed to the transport candidates.

## Network scope, route authority, and cross-dimensional behavior

ADR 0010 requires explicit route registration and bounded verification. ADR 0012 makes logistics dimension-scoped by default and allows cross-dimensional movement only through an explicit physical depot. Against those decisions:

- Copper Item Pipes, Ductwork, and Simple Copper Pipes operate through adjacent physical blocks or entities. They do not provide a universal warehouse or an inspected cross-dimensional bridge. Their paths can be considered as possible endpoint adapters later, but they do not themselves register Forever routes.
- Classic Pipes and Logistics: Automation self-discover connected networks. Classic Pipes adds sorting, fetching, and autocrafting, while Logistics adds provider, requester, and autocrafting modules. Neither was shown to impose a maximum network size or to expose the explicit route-registration contract required by ADR 0010.
- Logistics' registry is per `Level`, and no cross-dimensional bridge was found in the inspected release. Classic Pipes, Ductwork, Simple Copper Pipes, and Copper Item Pipes also had no cross-dimensional link in the tested path.
- Tesseract and Hopper Plus were rejected without a live transfer test because their official project descriptions explicitly advertise behavior that conflicts with the no-teleport and no-magical-cross-dimension requirements.
- No permanent chunk loading was observed or accepted. No candidate was allowed to make an unloaded settlement active merely to preserve item movement.

A later route or shipment contract may consume a selected transport adapter only after it can identify the physical source, cargo, buffer, and destination and can surface a described failure. It must not use a candidate's network graph as a substitute for route registration.

## Multiplayer interaction

No real player connection was available. Consequently, the following were not claimed:

- per-owner network permissions;
- multiplayer request isolation;
- duplicate-request resistance;
- client/server screen authority;
- client packet desynchronization behavior;
- player-break behavior for the client-required candidates.

The server was authoritative for every observation made here. Cargo counts came from server block NBT, entity NBT, or server logs. The next Logistics pilot must test two players, denied access, repeated requests, an endpoint disappearing between request and delivery, and an unloaded destination.

## Performance observations

The server ran on an AMD Ryzen 7 9700X with 19.5 GiB host memory and a 3 GiB Java heap. Observed ready-line timings were:

| Instance | Ready time |
|---|---:|
| Baseline | 0.280s |
| Simple Copper Pipes | 0.203s |
| Logistics: Automation | 2.795s |
| Ductwork | 2.341s |
| Classic Pipes | 3.967s |
| Copper Item Pipes | 0.189s |

These are startup observations from separate disposable instances, not a benchmark. No tick profiler, memory profile, sustained large network, route traversal bound, or network-size stress test was run. Logistics and Classic Pipes remain specifically unproven against the no-unbounded-traversal rule. The next pilot must measure a bounded network and reject any configuration that performs a global or unbounded scan.

## Conflicts observed

- **Simple Copper Pipes:** persistent FrozenLib registry error. Existing `docs/mod-research/candidate-matrix.csv` also records an upstream extreme item-duplication issue. This lab did not reproduce that issue in the 16-diamond path, so it remains an unresolved risk rather than a new reproduction.
- **Classic Pipes:** four named advancement loads failed. Its optional request and autocrafting features overlap the pack's future logistics authority. Server-side removal lost cargo.
- **Ductwork:** no candidate startup errors, no recipe or guidebook conflict, but no visible item-in-duct renderer.
- **Copper Item Pipes:** no recipe or advancement conflict and no new item or block registry footprint. Provenance and maintenance are insufficient for a baseline dependency.
- **Logistics: Automation:** 589 extra recipes and a broad provider/requester/autocrafting surface. Physical transport itself did not teleport or lose cargo in the tested paths, but the auto-discovered network is not yet a bounded route contract.
- **Global baseline:** Matcha duplicate-file and parse diagnostics were already present without candidates. They need their existing baseline follow-up, not a transport-specific attribution.

## Does it fit the pack?

No candidate fits the primary pack unchanged. One candidate can be a narrow follow-up pilot:

1. **Logistics physical-pipe subset:** promising for visible, server-owned cargo transport, but only if Provider, Requester, and Autocrafting blocks and recipes can be excluded or disabled through supported configuration or datapack controls. The pilot must impose an explicit network-size and route-registration policy, verify permissions, and prove duplicate-request resistance. If supported controls do not exist, stop at the gap and cite ADR 0033 before considering any companion adapter.
2. **Copper Item Pipes:** mechanically attractive for a small hopper-to-grate conveyor, but source availability, maintenance, and unlisted status fail the current dependency bar. Reassess only after a maintainership and provenance review.
3. **Ductwork:** retain as a possible technical fallback if the design explicitly relaxes visible item-in-duct motion. Its bounded adjacent behavior, safe removal, and chest-minecart interaction are otherwise strong.
4. **Vanilla hoppers and minecarts:** retain as first-class ordinary technical play regardless of the final mod choice.

The result respects LAB-07. No tested transport mod creates a wireless indexed warehouse as the default source of truth. Any future networked request feature must be evaluated as a withdrawal capability, not as harmless storage indexing.

## Follow-up required

1. Before a Logistics pilot, run a client and two-player test for provider/requester permissions, duplicate requests, endpoint removal, empty networks, and a destination that unloads during delivery.
2. Prove a supported way to exclude Logistics provider, requester, and autocrafting surfaces. Measure a deliberately bounded network and record the limit in data or configuration. Do not silently add custom Java.
3. Review Copper Item Pipes' source provenance, maintenance channel, license record, and exact embedded datapack behavior. Do not put it in `pack/` while the project is unlisted and source-unavailable.
4. If visible transport is relaxed, compare Ductwork against vanilla hopper and chest-minecart builds for ordinary technical play. Do not make Ductwork a prerequisite for vanilla inventories.
5. Keep a provider migration plan before any dependency removal. The populated-save removal copies reached `Done` but produced missing-data-pack and unknown-registry diagnostics.
6. Preserve route authority in Forever under ADR 0010. A later adapter may translate a physical endpoint or bounded transport result, but it must not copy private candidate identifiers into unrelated code. Any custom gap requires ADR 0033 evidence.
7. Next backlog ticket: **LAB-09, Rail, horse, boat, and vehicle compatibility spike.** It was not started in this change.

## Validation record

Commands and checks actually run:

- Modrinth project and exact-version API queries for all named candidates and the discovered pipe, duct, hopper, conveyor, and logistics searches.
- Exact CDN downloads for every candidate and required dependency in the manifest.
- `sha1sum`, `sha512sum`, and `sha256sum` verification against API and local hashes.
- Dedicated Fabric 26.2 server launches with Java 25 for the clean baseline, five candidates, transfer paths, endpoint removal, chunk unload, restart, and populated-save provider removal copies.
- Server command tests for source, transport, buffer, destination, hopper, chest-minecart, dropped-item, block-entity, item-display, force-load, and restart state.
- Source and JAR inspection for renderers, item codecs, saved NBT, graph construction, embedded datapack functions, guidebook assets, starter content, recipe viewers, and optional dependencies.
- TOML and Markdown deliverable review after writing the two files.

Commands intentionally not run:

- `./scripts/validate-pack.sh`, `./scripts/build-pack.sh`, and `./scripts/report-dependencies.sh`, because this lab did not modify `pack/` or dependency metadata and the deliverables are lab records only.
- `./gradlew build`, `./gradlew test`, `runClient`, `runGametest`, and `runDatagen`, because no companion source or gameplay code changed and the candidate binaries are not Gradle dependencies of this repository.
- Client launch, screenshots, multiplayer, permission checks, duplicate-request checks, and a sustained profiler run, due the unavailable display, unavailable real player connection, and lab scope. These are recorded as evidence gaps rather than passing results.

## Decision

| Candidate | Decision | Reason |
|---|---|---|
| Logistics: Automation `JHnkVzMU` | `PILOT` | Use only the physical transport subset in a follow-up. Cargo is server-owned, time-based, unload-safe, and removal-safe in the tested path. Provider, requester, autocrafting, permissions, duplicate requests, and graph bounds remain unproven. |
| Copper Item Pipes `NNEAuaYK` | `NEEDS_MORE_EVIDENCE` | Strongest visible local behavior with zero recipe or advancement footprint, but unlisted, source-unavailable, hopper-only, and not yet a defensible maintained dependency. |
| Ductwork `XCNsSwQj` | `REJECT` for the current requirement | Safe, bounded, and technically compatible, including chest minecarts, but no visible item-in-duct renderer was found. |
| Classic Pipes `5IRtOFZj` | `REJECT` | Visible progress and hopper support do not offset the observed cargo loss on server-side removal and failed candidate advancements. |
| Simple Copper Pipes `CEH3bXSV` | `REJECT` for the current baseline | Physical transfer and sample removal were safe, but the persistent FrozenLib registry error and unresolved duplication risk fail the baseline bar. |
| Create, Pipez, Integrated Dynamics, Integrated Tunnels, Modern Industrialization | `DEFER` | No usable exact Fabric 26.2 release was available for this lab. |
| Tesseract `AETPj8j4` | `REJECT` | Official description advertises any-distance and cross-dimensional item transport, conflicting with ADR 0012 and the physical local contract. |
| Hopper Plus `qHMihi82` | `REJECT` | Official description explicitly advertises teleporting items across distances. |
| Vanilla hoppers and chest minecarts | `ADOPT_BASELINE` as ordinary Minecraft capability | Preserve technical play without registration, remote access, or a new dependency. |

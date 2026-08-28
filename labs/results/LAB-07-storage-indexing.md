# LAB-07: Storage indexing and local search compatibility spike

- Date: 2026-08-28
- Run by: lab worker, automated dedicated-server runs
- Manifest: `labs/manifests/LAB-07-storage-indexing.toml`
- Status: complete for the server-side question; client interaction is blocked by the host's lack of a usable display

## Question

Can a maintained storage mod make physical local goods searchable without becoming universal wireless storage, and can it do so without conflicting with Many Roads Home's local, physical, bounded-storage design?

## Finding

**Yes, but only for a narrow read-only location guide.** StorageGuide 2.4.1 provides a server-owned map from assigned item identities to physical chest positions. It does not withdraw items, does not create a wireless warehouse, and leaves vanilla chests as the physical source of truth. It is a credible **PILOT** for local location search.

It is not a replacement for the planned warehouse index. StorageGuide maps one configured storage wall, not live quantities, stack revisions, arbitrary registered containers, or settlement and dimension-scoped warehouses. Its invalid-config recovery also silently replaces malformed data with a blank configuration. The client-only snapshot tools may provide useful local memory aids, but they are not server-authoritative and could not be opened here.

Tom's Simple Storage has the strongest generic terminal search, but its headline feature is a wireless terminal and its Fabric configuration has no documented switch to disable that feature. Storage Drawers is physical and bounded, but it is a drawer-storage provider rather than a generic index. Its filled drawer item can be nested inside a vanilla chest, and removing it from a save strands the drawer contents.

**No tested candidate genuinely replaces `InventoryIndex` and `WarehouseController`. Keep that existing-code REPLACE classification provisional and move it back toward KEEP or ADAPT until a full physical index and migration contract is proven.** Do not write a custom replacement in this lab.

## Exact versions

| Component | Project ID | Version ID | Version | File | Licence | Side |
|---|---|---|---|---|---|---|
| Minecraft | | | 26.2 | | | |
| Fabric Loader | | | 0.19.3 | | | |
| Fabric API | `P7dR8mSH` | `NqwNSxwA` | 0.158.0+26.2 | `fabric-api-0.158.0+26.2.jar` | Apache-2.0 | both |
| Global Packs | `NRLPy2mk` | `DqrPrUMp` | 26.2.0 | `globalpacks-fabric-26.2-26.2.0.jar` | LicenseRef-All-Rights-Reserved | both |
| Matcha Flavoured | `QI0EmgZ1` | `E9rngRfK` | 1.12 | `Matcha_Flavoured_1_12.zip` | CC-BY-NC-SA-4.0 | both |
| REI | `nfn13YXA` | `4o0NSIMj` | 26.2.820 | `RoughlyEnoughItems-26.2.820.jar` | MIT | both |
| Jade | `nvQzSEkH` | `ue8CO97w` | 26.2.11 | `Jade-mc26.2-Fabric-26.2.11.jar` | CC-BY-NC-SA-4.0 | both |
| Architectury API | `lhGA9TYQ` | `1yQC4VvP` | 21.0.7 | `architectury-fabric-21.0.7.jar` | LGPL-3.0-only | both |
| Cloth Config API | `9s6osm5g` | `Nv3xnWXd` | 26.2.155 | `cloth-config-26.2.155.jar` | LGPL-3.0-only | both |
| Tom's Simple Storage Mod | `XZNI4Cpy` | `9KkiCXs5` | 26.2-2.11.2-fabric | `toms_storage_fabric-26.2-2.11.2.jar` | MIT | both |
| Storage Drawers (Unofficial Port) | `3bqn07Ul` | `tsCab41T` | 26.2-19.1.7 | `StorageDrawers-fabric-26.2-19.1.7.jar` | MIT | both |
| StorageGuide | `4kCoqlbL` | `ZAXhGyEX` | 2.4.1 | `storageguide-2.4.1.jar` | MIT | both |

The exact candidate files were downloaded from the Modrinth CDN URLs in the manifest. The API SHA-1 and SHA-512 values matched the downloaded files. The local SHA-256 values are also recorded in the manifest.

Authoritative Modrinth endpoints used:

- Project metadata: `https://api.modrinth.com/v2/project/<project_id>`
- Exact baseline query: `https://api.modrinth.com/v2/project/<project_id>/version?game_versions=%5B%2226.2%22%5D&loaders=%5B%22fabric%22%5D`
- Storage category search: `https://api.modrinth.com/v2/search?query=storage&facets=%5B%5B%22categories%3Astorage%22%5D%2C%5B%22categories%3Afabric%22%5D%2C%5B%22versions%3A26.2%22%5D%2C%5B%22project_type%3Amod%22%5D%5D&limit=100&index=downloads`

## Candidate discovery

The following current Modrinth results were relevant to the question. Project metadata, version metadata, release type, loader, environment, licence, and feature claims were read from the official API on 2026-08-28. Search results are not treated as proof of compatibility by themselves.

| Candidate | Exact observed release | API result and initial fit |
|---|---|---|
| Tom's Simple Storage Mod | `XZNI4Cpy` / `9KkiCXs5`, `toms_storage_fabric-26.2-2.11.2.jar`, release | Server and client. Searchable terminal over physically connected inventories, but the project advertises a wireless terminal and its config has no explicit enable/disable flag. |
| Storage Drawers (Unofficial Port) | `3bqn07Ul` / `tsCab41T`, `StorageDrawers-fabric-26.2-19.1.7.jar`, release | Server and client. Physical drawers and a controller range of 50 blocks. It does not index ordinary chests as a generic warehouse, and its filled drawer item carries an inventory. |
| StorageGuide | `4kCoqlbL` / `ZAXhGyEX`, `storageguide-2.4.1.jar`, release | Server and client. One server-owned storage-wall map returns physical chest locations and has no withdrawal path. Best fit for a narrow pilot. |
| Storage Tracker | `csg2BIgk` / `EaRyTxXR`, `storage-logger-1.2.0.jar`, release | MIT client-only snapshot index. It records opened containers and can index nested shulkers. It cannot be server-authoritative. Client was not available. |
| Storage Finder | `qtRYFKf8` / `PReIxUWo`, `storage-finder-1.0.3.jar`, release | MIT client-only memory and highlight tool. It remembers previously opened nearby containers and performs no server-side installation. Client was not available. |
| Chest Tracker (Unofficial Port) | `VC2NohMN` / `cwtdCpdg`, `chesttracker-2.8.3+26.2.jar`, release | LGPL unofficial client-only port. Searches remembered storage on multiplayer servers, but the index is local to the client. Client was not available. |
| InvSearch | `s8jPicuI` / `5B8rNxhp`, `invsearch-storage-indexer-1.2.12-fabric.jar`, release | Client-only, LicenseRef-All-Rights-Reserved, with an embedded web dashboard. Not suitable for the default pack baseline. |
| Easy Storage | `I94M3KjL` / `TmGfMDmE`, `easy-storage-1.4.0+26.2.jar`, release | Server and client. The project explicitly says there is no fixed chest limit and that items can be inserted and extracted through one display. That fails the bounded local-source contract. |
| Upgraded Iron Chests | `gie37Jxu` / `vV2zgK02`, `upgraded-iron-chests-1.4.jar`, release | Server and client. Advertises global search across linked chests. LicenseRef-All-Rights-Reserved and no source URL in API metadata. |
| Linked Chests | `Mr2x2AUf` / `5oJWzvLd`, `LinkedChests-v26.2.1-mc26.2.x-Fabric.jar`, release | Advertises the same inventory from any chest, any place, any time. This is the rejected universal wireless/shared-inventory model. |
| Portable Storage | `fgNKEUno` / `bAwTVe47`, `portablestorage-2.3.3.jar`, release | Explicit portable warehouse with infinite stacking. Conflicts with ADR 0011 and the save-safety bar. The project warns that AI wrote 99.99 percent of its code and design. |
| InfiniteShulkerBox | `Yan8jT0m` / `ZfmgcHIb`, `InfiniteShulkerBox-1.5.jar`, release | Explicitly allows an infinite amount of Shulker Boxes inside one Shulker Box. Direct nesting rejection. |
| CompactStorage | `CNYyGWn5` / `mehMI8pF`, `compact_storage-fabric-26.2.15.jar`, release | Upgradable chests, barrels, and backpacks. No search/index contract. Its backpack surface needs a separate ADR 0011 review. |

### Named candidates with no usable Fabric 26.2 release

- **Simple Storage Network**, project `cVIr8Vz1`: exact 26.2 Fabric query returned `[]`. Project metadata lists Forge and NeoForge loaders. The newest observed release was `YznUL5Pm`, `26.1.2-1.13.2`, NeoForge, `storagenetwork-26.1.2-1.13.2.jar`, published 2026-08-23. Its project licence is LicenseRef-All-Rights-Reserved.
- **Sophisticated Storage**, project `hMlaZH8f`: exact 26.2 Fabric query returned `[]`. A 26.2 release exists as `EVA2q8Zy`, `26.2-1.5.113.2122`, NeoForge-only, `sophisticatedstorage-26.2-1.5.113.2122.jar`, with LicenseRef-All-Rights-Reserved.
- **Functional Storage**, project `cO40ZIg3`: exact 26.2 query returned no releases. The newest observed entry was `nSI7DQsa`, `26.1-1.6.1` for Minecraft 26.1.2 NeoForge, and it is a beta release.
- **Sophisticated Storage (Unofficial Fabric port)**, project `iHtpVwJL`: exact 26.2 Fabric query returned `[]`. The newest observed release was `xezSXA8y`, `1.20.1-1.3.5.11.142`.
- **Expanded Storage**: `/v2/project/expanded-storage` and `/v2/project/expandedstorage` both returned 404. The exact-title search returned unrelated addons or modpacks rather than a direct project. No reproducible Modrinth candidate was available.

The three tested candidate source and release pages were also inspected:

- Tom's Simple Storage: `https://api.modrinth.com/v2/project/XZNI4Cpy`, `https://github.com/tom5454/Toms-Storage`
- Storage Drawers (Unofficial Port): `https://api.modrinth.com/v2/project/3bqn07Ul`, `https://github.com/chaevsfe/StorageDrawers`
- StorageGuide: `https://api.modrinth.com/v2/project/4kCoqlbL`, `https://github.com/boyakhil978/StorageGuide`

## Test worlds

Every run used a disposable instance below `/tmp/lab07/`. No path was under a Minecraft `saves` directory and no real survival world was opened.

| Instance | Seed observed in log | Settings | Purpose |
|---|---:|---|---|
| `/tmp/lab07/instances/base/world` | `-2937657069787122635` | default world, easy, survival | Baseline recipe and advancement counts |
| `/tmp/lab07/instances/toms/world` | `8614617246574547153` | default world, easy, survival | Tom startup, physical blocks, restart, and removal copy |
| `/tmp/lab07/instances/drawers/world` | `3767946520948196268` | default world, easy, survival | Drawers startup, populated drawer, controller, nesting, restart, and removal copy |
| `/tmp/lab07/instances/storageguide/world` | `3346792497867639377` | default world, easy, survival | StorageGuide map/config, physical chest, restart, invalid config, and removal copy |

The Fabric server launcher was the exact loader endpoint for Minecraft 26.2, Fabric Loader 0.19.3, installer 1.1.0. Candidate SHA-512 values were checked against the API before testing.

## Client and server configuration

- Dedicated server tested: yes for the baseline and all three added candidates.
- Client tested: no. The host has no usable display. This prevents honest testing of terminal screens, keybinds, grid creation through the UI, search paging, highlights, and client-side snapshot tools.
- Multiplayer tested: no real player connection. StorageGuide's server permission and packet authority were inspected in source. No client decision was treated as server evidence.
- Optional dependencies were not added. REI and Jade were already in the baseline. Tom embeds Cloth Config. Storage Drawers optionally integrates with Jade. StorageGuide only suggests Mod Menu for client settings.

## Startup outcome

### Clean baseline

The baseline reached the ready state with the reference counts:

```text
[15:53:55] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[15:54:01] [main/INFO]: Loaded 2346 recipes
[15:54:01] [main/INFO]: Loaded 1805 advancements
[15:54:03] [Server thread/INFO]: Done (1.725s)! For help, type "help"
[15:54:03] [Server thread/INFO]: Seed: [-2937657069787122635]
[15:54:03] [Server thread/INFO]: Stopping server
```

The baseline also produced two `java.nio.file.FileAlreadyExistsException` lines for `./datapacks/Matcha_Flavoured_1_12.zip`, the same Global Packs behaviour seen in LAB-01 and LAB-02. Matcha emitted its existing invalid-path and advancement/loot parse diagnostics. These are baseline noise and were present in candidate runs as well.

### Tom's Simple Storage

```text
[15:55:24] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[15:55:30] [main/INFO]: Tom's Storage Setup starting
[15:55:32] [main/INFO]: Loaded 2371 recipes
[15:55:32] [main/INFO]: Loaded 1805 advancements
[15:55:33] [Server thread/INFO]: Done (1.555s)! For help, type "help"
[15:55:33] [Server thread/INFO]: There are 4 data pack(s) enabled: [vanilla (built-in)], [fabric-convention-tags-v2 (Fabric mod)], [toms_storage (Fabric mod)], [Matcha_Flavoured_1_12.zip (Global)]
```

No candidate crash occurred. The candidate adds 25 recipes and no net advancement count. A first-run `server.properties` missing-file error occurred before Minecraft generated that file. The restart run used the generated properties file and reached `Done (0.348s)`.

The project README advertises `Storage & Crafting Terminal`, `Inventory Connector`, and `Wireless terminal`. The downloaded Fabric config contains these relevant defaults:

```text
wirelessRange = 16
advWirelessRange = 64
wirelessTermBeaconLvl = 1
wirelessTermBeaconLvlCrossDim = 4
invConnectorScanRange = 16
invConnectorMaxCables = 2048
```

The source exposes numeric ranges and beacon thresholds, but no documented `enableWirelessTerminal` or equivalent switch. The source also uses the terminal's network access to pull and push physical stacks. Setting the beacon thresholds to `-1` disables the special beacon paths in the source, but it does not remove the ordinary wireless terminal item or its recipe. Setting a range to zero would be an inferred workaround, not an upstream-documented safety mode, and could not be client-verified here.

### Storage Drawers

```text
[16:01:13] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[16:01:19] [main/INFO]: Loaded 2478 recipes
[16:01:19] [main/INFO]: Loaded 1859 advancements
[16:01:20] [Server thread/INFO]: Done (0.739s)! For help, type "help"
[16:01:20] [Server thread/INFO]: Start loading plugin from Storage Drawers: com.jaquadro.minecraft.storagedrawers.integration.Waila
[16:01:23] [main/INFO]: New denied storage item storagedrawers:creative_vending_upgrade
```

No candidate crash occurred. The candidate adds 132 recipes and 54 advancements. It logs compacting rules and its Jade integration. Source configuration defines a physical controller range of 50 blocks on X, Y, or Z planes and warns that larger ranges can fail when chunks unload.

### StorageGuide

```text
[16:07:07] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[16:07:14] [main/INFO]: Loaded 2346 recipes
[16:07:14] [main/INFO]: Loaded 1805 advancements
[16:07:16] [Server thread/INFO]: Done (1.919s)! For help, type "help"
[16:07:16] [Server thread/INFO]: Seed: [3346792497867639377]
[16:07:19] [Server thread/INFO]: StorageGuide Big Brother is off.
[16:07:19] [Server thread/INFO]: StorageGuide Big Brother turned on.
[16:07:19] [Server thread/INFO]: StorageGuide Big Brother is on.
```

No candidate crash or candidate-specific startup warning occurred. StorageGuide adds no recipes or advancements.

## Matcha interaction and content noise

| Run | Recipes | Advancements | Difference from 2346/1805 | Observation |
|---|---:|---:|---:|---|
| Baseline | 2346 | 1805 | 0 / 0 | Reference |
| Tom's Simple Storage | 2371 | 1805 | +25 / 0 | Storage recipes changed the recipe manager. No net advancement increase. |
| Storage Drawers | 2478 | 1859 | +132 / +54 | Large recipe and recipe-unlock advancement surface. |
| StorageGuide | 2346 | 1805 | 0 / 0 | No packaged recipe or advancement data. |

Packaged-resource inspection found:

- Tom's: 26 recipe files and 5 unlock advancement files. The unlock files award storage recipes. No guidebook, manual, journal, or starter-item asset was found.
- Storage Drawers: 133 recipe files and 57 recipe advancement files. No guidebook or starter-item asset was found.
- StorageGuide: no recipe or advancement files and no guidebook or starter-item asset.

The baseline Matcha parse diagnostics were repeated in all candidate runs. They are not attributed to these candidates. The candidate count changes above are the useful comparison signal.

## Candidate observations and fit

### Tom's Simple Storage Mod

#### Observed

- A terminal and an inventory connector registered on the dedicated server.
- A physical terminal block, connector block, and vanilla chest were placed with commands.
- A diamond stack was written to the vanilla chest.
- The first run reported the physical records:

```text
[15:55:34] [Server thread/INFO]: 0, 80, 0 has the following block data: {components: {}, modes: 0, searchType: 0, sorting: 0, x: 0, y: 80, z: 0, id: "toms_storage:storage_terminal"}
[15:55:34] [Server thread/INFO]: 1, 80, 0 has the following block data: {components: {}, x: 1, y: 80, Items: [{count: 3, Slot: 0b, id: "minecraft:diamond"}], z: 0, id: "minecraft:chest"}
[15:55:34] [Server thread/INFO]: 2, 80, 0 has the following block data: {components: {}, x: 2, y: 80, z: 0, id: "toms_storage:inventory_connector"}
```

- Source review shows the terminal caches a connected `NetworkInventory`, exposes search in its screen, and has `pullStack` and `pushStack` operations. The network is physically connected by connectors and cables, but the wireless terminal can activate a terminal from a distance. Beacon settings include same-dimension and cross-dimension access paths in the source.
- The default Fabric config is persisted as `/tmp/lab07/instances/toms/config/toms_storage.json`. It contains no boolean that disables wireless terminal items.

#### Restart

The same seed and all physical records survived a clean restart:

```text
[15:56:32] [main/INFO]: Loaded 2371 recipes
[15:56:32] [main/INFO]: Loaded 1805 advancements
[15:56:33] [Server thread/INFO]: Done (0.348s)! For help, type "help"
[15:57:33] [Server thread/INFO]: 0, 80, 0 has the following block data: {components: {}, modes: 0, searchType: 0, sorting: 0, x: 0, y: 80, z: 0, id: "toms_storage:storage_terminal"}
[15:57:33] [Server thread/INFO]: 1, 80, 0 has the following block data: {components: {}, x: 1, y: 80, Items: [{count: 3, Slot: 0b, id: "minecraft:diamond"}], z: 0, id: "minecraft:chest"}
[15:57:33] [Server thread/INFO]: 2, 80, 0 has the following block data: {components: {}, x: 2, y: 80, z: 0, id: "toms_storage:inventory_connector"}
```

#### Removal

The removal copy omitted only `toms_storage_fabric-26.2-2.11.2.jar`. Minecraft reached the ready state and preserved the vanilla chest, but replaced the candidate blocks with defaults and skipped their block entities:

```text
[15:58:10] [main/WARN]: Missing data pack toms_storage
[15:58:13] [Worker-Main-9/ERROR]: Recoverable errors when loading section [0, 5, 0]: (Unknown registry key in ResourceKey[minecraft:root / minecraft:block]: toms_storage:inventory_connector -> using default); (Unknown registry key in ResourceKey[minecraft:root / minecraft:block]: toms_storage:storage_terminal -> using default)
[15:58:13] [Server thread/ERROR]: Failed to read field (id="toms_storage:storage_terminal"): Unknown registry key in ResourceKey[minecraft:root / minecraft:block_entity_type]: toms_storage:storage_terminal
[15:58:13] [Server thread/ERROR]: Skipping block entity with invalid type: "toms_storage:storage_terminal"
[15:58:13] [Server thread/ERROR]: Failed to read field (id="toms_storage:inventory_connector"): Unknown registry key in ResourceKey[minecraft:root / minecraft:block_entity_type]: toms_storage:inventory_connector
[15:58:13] [Server thread/ERROR]: Skipping block entity with invalid type: "toms_storage:inventory_connector"
[15:58:14] [Server thread/INFO]: 1, 80, 0 has the following block data: {components: {}, x: 1, y: 80, Items: [{count: 3, Slot: 0b, id: "minecraft:diamond"}], z: 0, id: "minecraft:chest"}
```

The candidate blocks and any contents in them are not recoverable by simply removing the JAR. Vanilla containers remain usable. This is a save-safety failure for an optional storage provider.

#### Interpretation

**Decision: REJECT for the default LAB-07 storage/index role.** It has a useful bounded physical cable network and a searchable terminal, but the wireless terminal is a headline feature, no explicit disable switch was observed, and removal invalidates candidate block and block-entity data. A pack datapack could hide recipes, but that would not be the same as a supported disable mode and would not protect pre-existing items.

### Storage Drawers (Unofficial Port)

#### Observed

- The candidate loaded as `storagedrawers 19.1.7` on the dedicated server.
- A drawer and controller were placed. The drawer was populated with three diamonds using its persisted `Drawers` representation. A vanilla chest was populated with four emeralds.
- The candidate's records were:

```text
[16:01:23] [Server thread/INFO]: 0, 80, 0 has the following block data: {Upgrades: [], components: {}, Validate: 1b, x: 0, y: 80, z: 0, DataVersion: 4903, id: "storagedrawers:standard_drawers_1", Drawers: [{Missing: 0b, Item: {count: 1, id: "minecraft:diamond"}, Count: 3}]}
[16:01:23] [Server thread/INFO]: 1, 80, 0 has the following block data: {components: {}, x: 1, y: 80, Items: [{count: 4, Slot: 0b, id: "minecraft:emerald"}], z: 0, id: "minecraft:chest"}
[16:01:23] [Server thread/INFO]: 2, 80, 0 has the following block data: {components: {}, RemoteNodes: [], x: 2, y: 80, z: 0, DataVersion: 4903, id: "storagedrawers:controller"}
```

- The same drawer count, diamond prototype, controller record, and emerald chest stack survived restart:

```text
[16:01:52] [Server thread/INFO]: 0, 80, 0 has the following block data: {Upgrades: [], components: {}, x: 0, y: 80, z: 0, DataVersion: 4903, id: "storagedrawers:standard_drawers_1", Drawers: [{Missing: 0b, Item: {count: 1, id: "minecraft:diamond"}, Count: 3}]}
[16:01:52] [Server thread/INFO]: 1, 80, 0 has the following block data: {components: {}, x: 1, y: 80, Items: [{count: 4, Slot: 0b, id: "minecraft:emerald"}], z: 0, id: "minecraft:chest"}
[16:01:52] [Server thread/INFO]: 2, 80, 0 has the following block data: {components: {}, RemoteNodes: [], x: 2, y: 80, z: 0, DataVersion: 4903, id: "storagedrawers:controller"}
```

- The upstream README warns that enchanted and NBT-bearing items may be deleted or reverted during updates and recommends emptying modded containers before updating. That is a direct compatibility risk for a long-lived world.

#### Container nesting

A populated drawer was mined into a vanilla chest. The resulting storage-block item carried the drawer inventory in `minecraft:block_entity_data`:

```text
[16:03:53] [Server thread/INFO]: Dropped 1 [Oak Drawers 1x1] from loot table storagedrawers:blocks/oak_full_drawers_1
[16:03:53] [Server thread/INFO]: 3, 80, 0 has the following block data: {components: {}, x: 3, y: 80, Items: [{components: {"minecraft:max_stack_size": 1, "minecraft:block_entity_data": {Upgrades: [], components: {}, DataVersion: 4903, id: "storagedrawers:standard_drawers_1", Drawers: [{Missing: 0b, Item: {count: 1, id: "minecraft:diamond"}, Count: 3}]}}, count: 1, Slot: 0b, id: "storagedrawers:oak_full_drawers_1"}], z: 0, id: "minecraft:chest"}
```

This is a real recursive inventory surface. The vanilla chest contains an item that carries a second inventory. It violates the no-nesting rule in the storage specification and ADR 0011's equivalent-container invariant.

#### Removal

The removal copy omitted only `StorageDrawers-fabric-26.2-19.1.7.jar`. Minecraft reached the ready state, preserved the vanilla emerald chest, and lost the drawer/controller block identities and block entities:

```text
[16:02:16] [main/WARN]: Missing data pack storagedrawers
[16:02:19] [Worker-Main-11/ERROR]: Recoverable errors when loading section [0, 5, 0]: (Unknown registry key in ResourceKey[minecraft:root / minecraft:block]: storagedrawers:controller -> using default); (Unknown registry key in ResourceKey[minecraft:root / minecraft:block]: storagedrawers:oak_full_drawers_1 -> using default)
[16:02:20] [Server thread/ERROR]: Failed to read field (id="storagedrawers:standard_drawers_1"): Unknown registry key in ResourceKey[minecraft:root / minecraft:block_entity_type]: storagedrawers:standard_drawers_1
[16:02:20] [Server thread/ERROR]: Skipping block entity with invalid type: "storagedrawers:standard_drawers_1"
[16:02:22] [Server thread/INFO]: 1, 80, 0 has the following block data: {components: {}, x: 1, y: 80, Items: [{count: 4, Slot: 0b, id: "minecraft:emerald"}], z: 0, id: "minecraft:chest"}
```

#### Interpretation

**Decision: REJECT for LAB-07's local indexing role.** It is a useful physical drawer provider for a separate storage-content lab, but it does not search arbitrary local chests, permits filled drawer items to be nested in containers, warns about item loss on updates, and cannot be removed without losing drawer storage. It must not be added to the primary pack from this lab.

### StorageGuide

#### Observed

- The candidate loaded on both the dedicated server and its common entrypoint as `storageguide 2.4.1`.
- It contributes no recipes, advancements, blocks, or items.
- Its source and README define one rectangular server-owned storage grid. Operators assign item IDs to cell positions. Search returns a cell location and the client renders a local highlight. The server never extracts an item from a chest.
- The server uses operator permission checks for grid and cell edits. Networking is versioned with separate payload IDs, and payload state is bounded to a collection codec limit of 8192 cells.
- The stored position record has only x, y, and z. It has no dimension field. The configuration supports one grid, not multiple settlement or dimension-scoped warehouses.
- The config declares `version = 5` and is written to `config/storageguide.json`, not to world-scoped SavedData. The map is therefore an instance/server configuration rather than a per-world, dimension-scoped record.
- For the server-only run, a one-cell map assigning `minecraft:diamond` to `(0,80,0)` was prepared from the documented JSON schema because the client UI was unavailable. A vanilla chest at that position held three diamonds:

```text
[16:07:19] [Server thread/INFO]: Changed the block at 0, 80, 0
[16:07:19] [Server thread/INFO]: 0, 80, 0 has the following block data: {components: {}, x: 0, y: 80, Items: [{count: 3, Slot: 0b, id: "minecraft:diamond"}], z: 0, id: "minecraft:chest"}
```

- The server command changed and persisted the optional Big Brother setting:

```text
[16:07:19] [Server thread/INFO]: StorageGuide Big Brother is off.
[16:07:19] [Server thread/INFO]: StorageGuide Big Brother turned on.
[16:07:19] [Server thread/INFO]: StorageGuide Big Brother is on.
```

#### Restart

The same server config, grid assignment, Big Brother setting, and physical chest survived restart:

```text
[16:07:46] [main/INFO]: Loaded 2346 recipes
[16:07:46] [main/INFO]: Loaded 1805 advancements
[16:07:46] [Server thread/INFO]: Done (0.240s)! For help, type "help"
[16:07:49] [Server thread/INFO]: 0, 80, 0 has the following block data: {components: {}, x: 0, y: 80, Items: [{count: 3, Slot: 0b, id: "minecraft:diamond"}], z: 0, id: "minecraft:chest"}
[16:07:49] [Server thread/INFO]: StorageGuide Big Brother is on.
```

The saved file retained `version: 5`, the one-cell `minecraft:diamond` assignment, and `sloppinessDetector: true`.

#### Invalid-config recovery

A separate disposable copy was given deliberately malformed JSON in `config/storageguide.json`. The server still started and StorageGuide silently rewrote the file as a valid blank version-5 configuration:

```text
[16:09:10] [main/INFO]: Loaded 2346 recipes
[16:09:10] [main/INFO]: Loaded 1805 advancements
[16:09:11] [Server thread/INFO]: Done (0.746s)! For help, type "help"
[16:09:11] [Server thread/INFO]: StorageGuide Big Brother is off.
```

The resulting file contained:

```json
{
  "version": 5,
  "sloppinessDetector": false,
  "forceClientsToUseMod": false,
  "sloppinessCooldownSeconds": 30,
  "bigBrotherMessages": ["Big Brother caught {playername} slacking off."],
  "cells": [],
  "sloppinessHistory": []
}
```

The source confirms that `StorageGuideConfig.load` catches `IOException | RuntimeException` and returns a new blank configuration. It does not preserve the malformed original, create a recovery copy, or emit an actionable diagnostic. This violates the project's fail-safe persistent-data rule even though the file has a schema version.

#### Removal

The removal copy omitted only `storageguide-2.4.1.jar`. The server reached the ready state with the baseline counts, and the vanilla chest retained its diamonds:

```text
[16:08:15] [main/INFO]: Loaded 2346 recipes
[16:08:15] [main/INFO]: Loaded 1805 advancements
[16:08:15] [Server thread/INFO]: Done (0.719s)! For help, type "help"
[16:08:19] [Server thread/INFO]: 0, 80, 0 has the following block data: {components: {}, x: 0, y: 80, Items: [{count: 3, Slot: 0b, id: "minecraft:diamond"}], z: 0, id: "minecraft:chest"}
```

The orphaned `config/storageguide.json` remained on disk, but it does not prevent the server from starting and contains only derived map data. It can be discarded as part of a documented config cleanup. Physical container contents were unaffected.

#### Interpretation

**Decision: PILOT, not ADOPT_BASELINE and not REPLACE_EXISTING_CODE.** StorageGuide is the only tested candidate that directly meets the local, physical, read-only location requirement without wireless withdrawal. It still needs a client-capable run, a hard grid-size bound, fail-safe config recovery, a dimension or world identity, and a decision on whether one storage wall is enough for the settlement model. It maps intended destinations rather than indexing live physical quantities, so it cannot replace the generic warehouse/index implementation.

## Acceptance and test matrix

| Requirement | Evidence | Result |
|---|---|---|
| Physical inventories remain source of truth | Vanilla chest ItemStacks survived every candidate restart and the StorageGuide removal test. Candidate block entities were inspected separately. | Pass for tested physical containers |
| Ordinary containers usable without registration | Vanilla chest with diamonds or emeralds remained readable in Tom's and StorageGuide removal copies. | Pass |
| Search results identify a physical location | StorageGuide source and config map item IDs to explicit cell positions. | Partial, client UI blocked |
| Search alone does not grant withdrawal | StorageGuide's `locateItem` sends a highlight only. No transfer operation exists in the server path. | Pass by source review |
| Candidate indexing is bounded | Tom has 16-block connector scan and 2048-cable defaults. Storage Drawers has a 50-block controller range. StorageGuide has an 8192-cell packet codec limit, but no explicit grid-area validation. | Partial |
| Index rebuild or stale data does not alter physical stacks | No live rebuild test was possible. StorageGuide stores assignments, not live stacks. Tom and Drawers own physical block entities rather than a separate generic index. | Not proven |
| Traveler's Cache remains 9 to 18 slots | No candidate was treated as a Traveler's Cache. Portable Storage and InfiniteShulkerBox were rejected rather than substituted. | Pass by scope control |
| No magical cross-dimensional warehouse | StorageGuide has no transfer path. Tom source exposes beacon same-dimension and cross-dimension settings, so it fails this boundary by default. | StorageGuide pass; Tom fail |
| No container nesting | Storage Drawers filled drawer item nested in a vanilla chest with `block_entity_data`. InfiniteShulkerBox also explicitly advertises recursive nesting. | Fail for those candidates |
| Permissions and server authority | StorageGuide operator checks and server-side map/config writes were verified by source and server commands. Client permission flow was not live-tested. | Partial |
| Item identity changes | No component, enchantment, or modded-item identity mutation was run. Storage Drawers README warns NBT-bearing items can be deleted or reverted during updates. | Not tested, known risk |
| Restart | All three added candidates restarted with the same seed. Physical test records survived while their provider was installed. | Pass |
| Removal | Tom and Storage Drawers removal copies booted but converted provider blocks and skipped provider block entities. StorageGuide removal preserved vanilla physical storage and left only an orphaned config. | StorageGuide pass; Tom/Drawers fail |
| No-mod fallback | Removal copies reached `Done` and vanilla chest ItemStacks remained. | Pass for ordinary containers |
| Recipe and advancement counts | Baseline 2346/1805, Tom 2371/1805, Drawers 2478/1859, StorageGuide 2346/1805. | Measured |
| Guidebook or starter item | Packaged resource inspection found no guidebook/starter asset in tested candidates. Tom and Drawers add recipe/unlock content. StorageGuide adds none. | Pass for no guidebook/starter duplication |
| Search paging and client UI | No display was available. | Blocked |
| Multiplayer player flow | No real player connection. | Blocked |

## Performance observations

No TPS or heap profile was collected. The hardware was an AMD Ryzen 7 9700X with approximately 19.5 GiB available memory. Dedicated-server startup completed for all tested candidates. No permanent chunk loading was introduced by the candidates during the test. Temporary `forceload` was used only to make test coordinates queryable and was removed before each test server stopped. StorageGuide's source needs a separate review for grid-area bounding because its one-grid representation has no explicit maximum before network serialization.

## Conflicts observed

- Tom's adds a searchable terminal and a wireless terminal. The wireless item is a direct conflict with the default no-universal-wireless rule unless a supported disable configuration is proven.
- Storage Drawers adds a large recipe and recipe-unlock advancement surface, plus compacting rules. Its controller is a physical local network, but it is not an arbitrary vanilla-container index.
- StorageGuide adds no recipes, advancements, items, or blocks. Its UI is separate from REI and Jade, and its server setting is optional.
- No duplicate guidebook or starter item was observed.
- Matcha's known parse diagnostics and Global Packs duplicate-archive errors were present in the baseline and candidate runs. They are not new candidate conflicts.

## Does it fit the pack?

### StorageGuide

It fits the hard constraints best when treated as a **read-only local storage map**:

- goods stay in physical chests;
- search returns a physical position rather than a remote item transfer;
- grid and cell editing are operator-authoritative;
- ordinary vanilla chests need no registration to remain usable;
- there is no wireless or cross-dimensional withdrawal feature;
- removing it leaves physical chests intact.

It does not fit as the complete storage system without further work. One grid per server is too narrow for settlements, its positions do not include a dimension, it does not report current quantities or stack revisions, and malformed config is silently blanked. These are pack-integration and save-safety gaps, not permission to write a custom warehouse now. If a later companion adapter is proposed, it must pass ADR 0033 gap analysis.

### Tom's Simple Storage

Its cable network keeps storage physical and has useful bounds, but its wireless terminal is a headline feature. The source shows ordinary wireless activation plus beacon-based same-dimension and cross-dimension paths. There is no documented feature switch that removes wireless access. The removal test also showed candidate block data becomes unknown registry data when the JAR is removed. It does not fit the default baseline.

### Storage Drawers

It is a reasonable physical bulk-storage provider in isolation, but it is not the missing generic index. Filled drawers can be stored inside chests, and the provider cannot be removed from a populated save without losing its block identities and contents. It does not fit the LAB-07 role.

## Follow-up required

1. Do not add Tom's Simple Storage or Storage Drawers to `pack/` from this lab.
2. Keep StorageGuide as a pilot candidate only. Run a client-capable spike with a disposable server to exercise grid creation, item assignment, search results, highlights, paging, non-operator editing denial, stale configuration, and protocol/version mismatch.
3. Before any pilot adoption, require StorageGuide to add or document an explicit maximum grid area, dimension/world identity, backup-preserving invalid-config recovery, and a migration procedure for its versioned configuration.
4. Decide whether StorageGuide's assignment map is sufficient for the player-facing Field Guide use case. It is not a live warehouse index and must not be represented as one.
5. Retain the existing `InventoryIndex` and `WarehouseController` safety contracts until a candidate proves live physical handles, quantities, revisions, permissions, stale-index behaviour, and migration. The pivot inventory's REPLACE classification should be revisited rather than silently deleting the code.
6. Do not test or adopt portable warehouses, global linked inventories, infinite shulker nesting, or other candidates that violate ADRs 0011 and 0012.
7. Next lab work remains LAB-08, local item transport and automation. The next functional backlog ticket is STORAGE-01, physical local storage and search. Do not start either ticket in this change.

## Validation commands

Commands actually run for this lab included:

- Python/urllib and `curl` requests to the Modrinth project, version, and search API endpoints named above.
- `git clone --depth=1` of the three public candidate source repositories into `/tmp/lab07/research/` for source/config inspection.
- Exact CDN downloads of the baseline, Matcha, Tom's, Storage Drawers, and StorageGuide files, followed by SHA-1, SHA-512, and SHA-256 checks.
- Dedicated-server launches with JDK 25 and the Fabric 26.2 loader in `/tmp/lab07/instances/base`, `toms`, `drawers`, and `storageguide`, including clean stop commands.
- Server-console `setblock`, `item replace`, `data get`, `data merge`, `loot insert`, `forceload`, `seed`, and StorageGuide Big Brother commands in disposable worlds.
- Restart runs for each tested candidate, removal-copy runs with only the candidate JAR moved out, and the Storage Drawers filled-container nesting probe.
- A malformed StorageGuide configuration probe in a separate disposable copy.
- `git status --short --branch` and direct review of both deliverables.

Intentionally not run:

- `./gradlew build`, `./gradlew test`, `./scripts/validate-pack.sh`, and `./scripts/build-pack.sh`, because this compatibility lab changes no Java, pack, dependency, or generated build input.
- `./gradlew runClient`, because the host has no usable display. Client UI and live player flows remain blocked rather than guessed.
- `./gradlew runGametest`, `./gradlew runServer`, `./gradlew runDatagen`, Matcha acquisition, and Matcha installation scripts, because the lab used isolated downloaded server instances and did not change the companion code or the primary pack.

## Decision

| Candidate | Decision | Reason |
|---|---|---|
| Tom's Simple Storage Mod | **REJECT** | Searchable physical cable network is useful, but wireless access is a headline feature with no documented disable switch, and removal loses provider block data. |
| Storage Drawers (Unofficial Port) | **REJECT** | Physical storage is bounded, but it is not a generic local index, permits filled-container nesting, warns about item loss, and removal loses drawer block data and contents. |
| StorageGuide | **PILOT** | Read-only server-backed local chest location map with physical source of truth and safe physical removal, subject to client, bounds, dimension, and fail-safe config follow-up. |
| Storage Tracker | **NEEDS_MORE_EVIDENCE** | Attractive client-side local snapshot search, but server unsupported and client UI was blocked. |
| Storage Finder | **DEFER** | Client-only remembered-container finder. It does not establish a server-authoritative storage contract. |
| Chest Tracker (Unofficial Port) | **DEFER** | Useful client-only memory aid, but unofficial and not a server-owned index. |
| InvSearch | **REJECT** | Client-only, LicenseRef-All-Rights-Reserved, and includes an embedded web dashboard outside the default storage boundary. |
| Easy Storage | **REJECT** | Official description advertises no fixed chest limit and direct insert/extract through one display. |
| Upgraded Iron Chests | **REJECT** | All-Rights-Reserved, no source URL in API metadata, and global linked-chest search. |
| Linked Chests | **REJECT** | Explicit any-place shared inventory is the universal wireless model rejected by ADR 0012. |
| Portable Storage | **REJECT** | Explicit portable warehouse and infinite stacking conflict with ADR 0011 and save safety. |
| InfiniteShulkerBox | **REJECT** | Explicit recursive Shulker Box nesting. |
| CompactStorage | **DEFER** | Storage and backpacks exist, but no indexing contract was observed and the backpack needs an ADR 0011 review. |
| Simple Storage Network | **DEFER** | No Fabric 26.2 release. Latest observed release is NeoForge 26.1.2 and All-Rights-Reserved. |
| Sophisticated Storage | **DEFER** | 26.2 exists only for NeoForge and is All-Rights-Reserved. |
| Functional Storage | **DEFER** | No 26.2 release. Newest observed entry is a 26.1.2 NeoForge beta. |
| Sophisticated Storage (Unofficial Fabric port) | **DEFER** | No 26.2 Fabric release. |
| Expanded Storage | **DEFER** | No direct Modrinth project or reproducible 26.2 candidate was found. |

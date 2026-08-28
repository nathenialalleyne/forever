# LAB-03: Building and excavation compatibility spike

- Date: 2026-08-28
- Run by: automated lab worker
- Manifest: `labs/manifests/LAB-03-building-and-excavation.toml`
- Status: complete for dedicated-server loading and removal; client gameplay acceptance is externally blocked

## Question

Can maintained 26.2 Fabric mods own assisted block placement and vein mining at real material and durability cost, with server authority intact, alongside Matcha, without changing ordinary Minecraft placement?

## Scope and method

This was a compatibility spike, not a gameplay implementation. Candidate versions were re-verified against the official Modrinth API on 2026-08-28. The exact project IDs, version IDs, files, release dates, licences, and runtime dependencies are in the manifest.

Each candidate was tested in an isolated disposable server under `/tmp/lab03/instances/<candidate>`. Third-party files were never copied into `pack/`, and no real save was used. The candidate set covered assisted placement, command-based building, ore vein mining, general grouped mining, tree felling, and server-only comparisons. Modrinth searches also found alternatives recorded under `[not_tested]` and `[not_added]` in the manifest.

## Test world

- Seed: `-481729384`
- World: default Overworld generation
- Difficulty: easy
- Game mode: survival
- Server: dedicated Fabric server, offline mode for the disposable fixture
- Spawn protection: `0`
- View distance: `6`
- Simulation distance: `4`
- Candidate ports: `26000` through `26204`, all isolated from one another
- Removal copies: `/tmp/lab03/removal/<candidate>`, ports `26301` through `26313`
- JDK: `/home/natea/toolchains/jdk-25.0.4.1+1`, Java 25
- Memory: `-Xms1G -Xmx3G`
- Confirmed not a real survival world: yes. All fixtures stayed under `/tmp/lab03/`.

## Client and server configuration

- Dedicated server tested: **yes**, with Matcha and each candidate installed separately.
- Client tested: **no, externally blocked**. This host has no usable display. No client launch, keybind, inventory action, placement operation, or mining operation was claimed as tested.
- Multiplayer tested: **no**. The server was offline and no second client could be launched.
- Claims or protected-area mod installed: **no**. Protected-area behavior therefore remains untested.
- Ordinary manual placement and breaking: **not operationally verified**. A clean server boot is not evidence that a client interaction remained unchanged.

This means the lab can establish release loading, server-side configuration surfaces, resource/advancement deltas, Matcha coexistence, and removal from an unused copied world. It cannot close the acceptance criteria for exact inventory consumption, tool condition, cancellation, claims, disconnect/retry, or multiplayer permissions.

## Exact versions

The fixed baseline was Minecraft 26.2, Fabric Loader 0.19.3, Fabric API `NqwNSxwA`, Global Packs `DqrPrUMp`, Matcha `E9rngRfK`, REI `4o0NSIMj`, Jade `ue8CO97w`, Architectury `1yQC4VvP`, and Cloth Config `Nv3xnWXd`. Matcha SHA-256 was verified as `6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248`.

The tested candidate artifacts were:

| Candidate | Project / version | File | Licence | Declared topology |
|---|---|---|---|---|
| Effortless Building | `DYtfQEYj` / `BkGu1Eid` | `effortlessbuilding-fabric-26.2-4.3.jar` | LGPL-3.0-only | client and server required |
| Building Wands | `XkisZUfp` / `madr0hP0` | `BuildingWands_3.2.2_release_fabric_26.2.jar` | Apache-2.0 | client and server required |
| AutoBuild | `wzBO46L7` / `Q7qeA1GB` | `autobuild-2.0.1+26.2.jar` | MIT | client and server required |
| WorldEdit | `1u6JkXh5` / `6YnCYPwc` | `worldedit-mod-7.4.5.jar` | GPL-3.0-only | server control comparison |
| Liteminer | `VTnHoofC` / `dK23TC0J` | `liteminer-fabric-4.1.2+26.2.jar` | MIT | client and server required |
| MinersAdvantage | `VcTmRh04` / `6KojE9jd` | `MinersAdvantage_2.22.0+26.2-fabric-2.22.0.jar` | MIT | client and server required |
| VeinMiner | `OhduvhIc` / `InpIvPQ1` | `veinminer-fabric-2.12.1.jar` | AGPL-3.0-only | server required, client optional |
| Server Side Vein Mine | `Fl69jDHD` / `9SzCUKVU` | `serversideveinmine-1.4.0.jar` | CC0-1.0 | server only |
| Simple Ore Vein Miner | `oU7CpxQo` / `frEL8M6q` | `simple-ore-vein-miner-1.0.4.jar` | MIT | client and server required |
| FallingTree | `Fb4jn8m6` / `sOoH5kkd` | `FallingTree-26.2-25.jar` | LGPL-3.0-only | server required, client optional |
| Tree Harvester | `abooMhox` / `3VcDfhZP` | `treeharvester-26.2.0-9.4.jar` | All Rights Reserved | server only |
| Ore Harvester | `Xiv4r347` / `A3tU6KDN` | `oreharvester-26.2.0-1.6.jar` | All Rights Reserved | server only |
| Chopt | `omDn2JEQ` / `V0nSnDKh` | `chopt-1.0.10+mc26.2.jar` | Modrinth: MIT | client and server required |

Runtime dependencies tested with candidates were Amber `g0SbOU8i`, Konfig `MqZmdbqT`, Fabric Language Kotlin `bdhiINYC`, and Collective `M75JwjyS`. WorldEdit bundled its `worldeditcui-protocol` dependency inside the JAR, so no separate protocol file was added.

## Startup outcome

The clean baseline reached the normal server-ready line:

```text
[15:54:21] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[15:54:21] [main/INFO]: Loading 49 mods:
[15:54:24] [main/INFO]: Found new data pack Matcha_Flavoured_1_12.zip, loading it automatically
[15:54:26] [main/INFO]: Loaded 2346 recipes
[15:54:26] [main/INFO]: Loaded 1805 advancements
[15:54:27] [Server thread/INFO]: Done (1.511s)! For help, type "help"
```

The actual candidate runs, including the corrected runs with the four building JARs present, produced:

| Case | Mods loaded | Recipes | Advancements | Server-ready line | Result |
|---|---:|---:|---:|---|---|
| Baseline | 49 | 2346 | 1805 | `Done (1.511s)` | pass |
| Effortless Building | 50 | 2347 | 1805 | `Done (1.476s)` | pass; +1 recipe |
| Building Wands | 50 | 2358 | 1805 | `Done (1.460s)` | pass; +12 recipes |
| AutoBuild | 50 | 2346 | 1805 | `Done (1.788s)` | pass |
| WorldEdit | 51 | 2346 | 1805 | `Done (2.382s)` | pass |
| Liteminer | 52 | 2346 | 1805 | `Done (1.538s)` | pass |
| MinersAdvantage | 50 | 2346 | 1805 | `Done (1.562s)` | pass |
| VeinMiner | 67 | 2346 | 1805 | `Done (1.447s)` | pass |
| Server Side Vein Mine | 51 | 2346 | 1805 | `Done (1.473s)` | pass |
| Simple Ore Vein Miner | 50 | 2346 | 1805 | `Done (1.486s)` | pass |
| FallingTree | 50 | 2346 | 1805 | `Done (1.472s)` | pass |
| Tree Harvester | 51 | 2346 | 1805 | `Done (1.440s)` | pass |
| Ore Harvester | 51 | 2346 | 1805 | `Done (1.579s)` | pass |
| Chopt | 50 | 2346 | 1805 | `Done (1.678s)` | pass |

All candidate logs showed Matcha's automatic-load line. No candidate prevented server startup. Building Wands produced an additional repeated warning:

```text
[main/WARN]: Error reading pack metadata, attempting fallback type
com.google.gson.JsonParseException: Pack declares support for version newer than 81, but is missing mandatory fields min_format and max_format
```

The warning comes from Building Wands' `pack.mcmeta` and did not prevent its recipes or server entrypoint from loading. It should be resolved or accepted explicitly before adoption.

### Baseline Matcha noise

Every fixture, including the baseline, produced the same lab-fixture and existing Matcha messages:

- two `FileAlreadyExistsException: ./datapacks/Matcha_Flavoured_1_12.zip` messages because the ZIP was copied into `datapacks/` while Global Packs also attempted its configured copy;
- invalid `HELP.txt` paths in the Matcha data;
- the existing `minecraft:advancement/custom/root` parse error;
- the existing `main:mechanics/wither_test` criteria mismatch; and
- the existing missing enchanted-golden-apple loot-table warning.

These were not introduced by the building or excavation candidates. The duplicate-copy fixture should be cleaned up in a future lab harness change, but it is not a candidate compatibility failure.

## Recipes, advancements, guidebooks, and starter items

Archive inspection counted files, not directory entries. Server counts are the authoritative loaded counts.

| Candidate | Archive content | Loaded count delta | Guidebook or automatic starter item |
|---|---|---|---|
| Effortless Building | 1 recipe for the randomizer tool; no advancement files | `+1 / +0` | No guidebook or automatic starter item. `buildingtechniquesbook.png` is only a texture in the archive; no book item definition was found. |
| Building Wands | 12 recipe files and 10 files under the nonstandard plural `data/wands/advancements/` path | `+12 / +0` | No guidebook or automatic starter item. It registers craftable wands, magic bags, and a palette, plus a creative wand that must not be made a normal survival progression path. |
| AutoBuild | No recipe or advancement data | `+0 / +0` | No guidebook or registered starter item found in the archive. |
| WorldEdit | No recipe or advancement data | `+0 / +0` | No guidebook. It uses a vanilla axe as its default selection wand. |
| Liteminer | No recipe or advancement data | `+0 / +0` | No guidebook or starter item. |
| MinersAdvantage | No recipe or advancement data | `+0 / +0` | No guidebook or starter item. |
| VeinMiner | No recipe or advancement data | `+0 / +0` | No guidebook or starter item. |
| Server Side Vein Mine | No recipe or advancement data | `+0 / +0` | No guidebook or starter item. |
| Simple Ore Vein Miner | No recipe or advancement data | `+0 / +0` | No guidebook or starter item. |
| FallingTree | No recipe or advancement data | `+0 / +0` | No guidebook or starter item. |
| Tree Harvester | No recipe or advancement data | `+0 / +0` | No guidebook or starter item. |
| Ore Harvester | No recipe or advancement data | `+0 / +0` | No guidebook or starter item. |
| Chopt | No recipe or advancement data; registers a shrinking-stump block and block entity | `+0 / +0` | No guidebook or starter item. The stump is an intermediate world block, not onboarding content. |

The no-guidebook result preserves LAB-01's finding that the current pack has no guidebook stack. None of these candidates should be adopted on the assumption that a guidebook already explains them. A future selected mechanic still needs the pack's in-game explanation path.

## Material, durability, limits, and authority assessment

The following is evidence-based but separates upstream claims and generated configuration from gameplay proof. The client-side proof is still outstanding.

### Assisted placement

- **Effortless Building** is the best fit by declared behavior. Its project documentation says survival actions consume the correct number of blocks, exposes maximum blocks per operation and reach controls, and allows break/replace tool-use and durability settings. It explicitly says the server part performs bulk placement and that functional server settings are admin-only. The JAR contains separate C2S packet handlers and server storage. It needs a client test before it can be called authoritative in practice.
- **Building Wands** also fits the material model on paper. Its documentation says survival consumes blocks, uses suitable tools for destruction, and can optionally consume XP. Generated `config/wands.json` exposes tier limits from 32 to 1024, a global limit of 8192, wand durability values, survival drop behavior, and per-tier break permissions. The creative wand and very large default upper limits are a progression risk. Its malformed built-in pack metadata warning is an additional compatibility risk.
- **AutoBuild** documents inventory material use, missing-material reporting, a configurable number of blocks per tick, optional nearby-container search, a replacement safety check, and separate multiplayer building states. It does not document a tool-durability cost for placement, and no server config file was emitted during headless startup. It also overlaps LAB-04's schematic scope. It remains a follow-up candidate, not the selected placement path.
- **WorldEdit** is rejected for the survival placement objective. The project describes itself as a map editor for creative or temporary survival use. Its generated configuration has `use-inventory=false`, `use-inventory-override=false`, `default-max-changed-blocks=-1`, and `max-changed-blocks=-1`. Permissions can limit commands, but that does not turn it into real-material construction.

### Excavation and tree felling

- **VeinMiner** is the strongest ore-vein candidate. It is declared server-required, and its generated server settings include `maxChain=100`, `needCorrectTool=true`, `permissionRestricted=false`, `decreaseDurability=true`, and `searchRadius=1`. For a pilot, reduce `maxChain` to 64 or lower, enable `permissionRestricted`, retain correct-tool and durability checks, require an intentional activation path such as sneak, and keep the group limited to approved ores. Its optional client support is useful for vanilla-client fallback, but gameplay still needs a two-client authority test.
- **Liteminer** exposes a 64-block limit, food exhaustion, additional mining time, and `prevent_tool_breaking=true`. Its generated `require_correct_tool_enabled=false` must be enabled for Matcha's tier model. The configuration provides a tool-break guard but does not expose as clear a per-block durability switch as VeinMiner, so actual condition accounting remains unverified.
- **FallingTree** is the best tree-felling candidate for a narrow pilot. It is server-required, supports server-only operation, and generated `fallingtree.json` contains `maxScanSize=500`, `maxSize=100`, `durabilityMode=NORMAL`, `forceToolUsage=false`, `damageMultiplicand=1.0`, `breakInCreative=false`, and a configurable tool list. A pilot should require the correct axe, retain normal durability, lower the tree-size cap if playtesting requires it, and keep creative behavior disabled. Its documentation warns that it must not share a Wood group with VeinMiner, so remove that group if both are used.
- **Tree Harvester** has an unusually clear cost surface: crouch-by-default activation, axe requirement, one durability loss per harvested log, exhaustion, time scaling, player-made-tree protection, and sapling replacement. It is technically promising, but its Modrinth licence is All Rights Reserved, so it cannot be selected for the pack without an explicit redistribution decision.
- **Ore Harvester** similarly has crouch-by-default activation, one durability loss per ore, exhaustion, time scaling, a pickaxe blacklist, and normal drops. Its All Rights Reserved licence blocks pack selection. Its default fuzzy ore search also needs a Matcha-specific check because it can classify blocks from names rather than an explicit allowlist.
- **Chopt** documents exact vanilla durability and enchantment behavior, stops at the affordable number of logs when an axe breaks, shares progress between players, and caps work at 32 swings and 256 logs. It is a compelling tree-felling design, but it requires both sides, introduces a persistent shrinking-stump block/entity during use, and has a licence discrepancy: Modrinth says MIT while the embedded `fabric.mod.json` says CC0-1.0. Resolve that discrepancy before any adoption decision.
- **MinersAdvantage** has strong server-oriented controls, including TPS guarding, tick delays, per-tick limits, 12-block vein distance, 6x6x6 excavation dimensions, and time scaling. It is also a broad bundle containing excavation, shafts, tree felling, crop handling, item gathering, tool substitution, and lighting. Its generated server configuration does not make ore durability accounting as explicit as VeinMiner, and its breadth creates more overlap with future systems. Defer it rather than adding a whole convenience suite.
- **Server Side Vein Mine** is attractive for authority because it is server-only and has a 64-block maximum with an explicit block allowlist. It does not expose a durability, correct-tool, or permission setting in its generated config. Do not select it until a client test proves vanilla block-break validation and Matcha tool accounting.
- **Simple Ore Vein Miner** is small and has a 64-block limit, pickaxe requirement, and client/server split. Its project documentation does not state a durability policy or a configurable permission boundary, so it remains a lower-priority comparison.

## Ordinary placement and server-authority acceptance matrix

| Requirement | Result | Evidence or blocker |
|---|---|---|
| Vanilla manual placement with all features disabled | **blocked** | No usable display or client launch. |
| Vanilla manual placement with a feature enabled but not activated | **blocked** | No usable display or client launch. Do not infer from startup. |
| Real materials for assisted placement | **documented for Effortless, Building Wands, AutoBuild** | Upstream project descriptions; no inventory operation was run. |
| Real tool durability for bulk breaking/felling | **documented/configured for VeinMiner, FallingTree, Tree Harvester, Ore Harvester, Chopt; partial for Liteminer** | Config and project documentation; no tool-condition operation was run. |
| Bounded selection or vein size | **static evidence pass** | Limits observed in candidate configs and project documentation. Operational cap test is blocked. |
| Permission boundary | **partial** | VeinMiner exposes `permissionRestricted`; Effortless restricts server setting changes to admins; other candidates need playtest/config review. No multiplayer permission test. |
| Server-authoritative mutation | **static evidence only** | Server-required topology, server configs, server packet/entrypoint classes, and server startup pass. No client request or rejection test. |
| Protected-area behavior | **not tested** | No claims/protection provider in the fixture. |
| Cancellation, undo, disconnect, or retry | **not tested** | No client or multiplayer run. |
| Item drops and no silent deletion | **not tested operationally** | Static documentation says normal/survival drops for several candidates. No break operation was run. |
| Matcha tool-tier interaction | **not tested operationally** | Require correct tool and durability settings are identified, but no Matcha tool was used. |
| Absent-mod fallback | **server/removal pass only** | Every removal copy booted with the baseline counts. Ordinary player actions after removal were not tested. |

## Removal behavior

For each candidate, a copy of its saved disposable world was created under `/tmp/lab03/removal/<candidate>`. The candidate JAR and its candidate-only runtime dependencies were absent from the copy. Only the baseline mods, Matcha ZIP, Global Packs configuration, and the copied world remained.

Every removal case reached `Done`, loaded exactly `2346` recipes and `1805` advancements, and produced zero candidate-specific error lines after the known Matcha and lab-fixture messages were excluded:

| Removed candidate | Removal result | Loaded counts |
|---|---|---|
| Effortless Building | `READY`, `Done (0.188s)` | 2346 / 1805 |
| Building Wands | `READY`, `Done (0.218s)` | 2346 / 1805 |
| AutoBuild | `READY`, `Done (0.221s)` | 2346 / 1805 |
| WorldEdit | `READY`, `Done (0.229s)` | 2346 / 1805 |
| Liteminer | `READY`, `Done (0.196s)` | 2346 / 1805 |
| MinersAdvantage | `READY`, `Done (0.207s)` | 2346 / 1805 |
| VeinMiner | `READY`, `Done (0.190s)` | 2346 / 1805 |
| Server Side Vein Mine | `READY`, `Done (0.205s)` | 2346 / 1805 |
| Simple Ore Vein Miner | `READY`, `Done (0.248s)` | 2346 / 1805 |
| FallingTree | `READY`, `Done (0.230s)` | 2346 / 1805 |
| Tree Harvester | `READY`, `Done (0.228s)` | 2346 / 1805 |
| Ore Harvester | `READY`, `Done (0.194s)` | 2346 / 1805 |
| Chopt | `READY`, `Done (0.191s)` | 2346 / 1805 |

This proves removal from an unused copied world, not removal after an unfinished operation. No client action occurred, so no candidate item, undo history, persistent stump, or in-flight operation existed. Chopt's custom block/entity and Building Wands' player/wand state remain migration risks after real use. Effortless undo history and WorldEdit schematics also require a used-world removal test before a release.

## Performance observations

The dedicated server started successfully for every candidate. The logged server initialization times were 1.440 to 2.382 seconds after the fixture's first-run library setup. This is not a TPS or bulk-operation benchmark. No client rendering, large-structure placement, vein size stress test, protected-area test, or long-running tick profile was possible. No permanent chunk-loading behavior was observed during the short startup-only run, but passive simulation was not exercised.

## Conflicts and risks

1. **Matcha tools and condition.** Candidate defaults must not be trusted to preserve Matcha tiers. Enable correct-tool checks, retain real durability, cap operation size, and test broken-tool behavior with each selected Matcha tier.
2. **VeinMiner and FallingTree overlap.** FallingTree's documentation calls out the Wood group conflict. If both are piloted, remove VeinMiner's Wood group and let FallingTree own trees.
3. **Building Wands progression.** The creative wand, 8192 global limit, magic bags, and high-tier durability settings can bypass intended progression if exposed to ordinary survival players. Restrict or remove the creative path and start with caps no higher than 64 blocks per operation.
4. **Building Wands resource metadata.** Its `pack.mcmeta` triggers the missing `min_format`/`max_format` fallback warning on 26.2. The server still loads, but this is not clean compatibility.
5. **Ore classification.** Ore Harvester's fuzzy search may classify blocks by names. Use an explicit allowlist when testing Matcha or modded ores.
6. **Licensing.** Tree Harvester and Ore Harvester are All Rights Reserved. Chopt's Modrinth licence and embedded metadata disagree. None should enter `pack/` until licensing is resolved.
7. **No pack adoption.** This lab did not change `pack/` and did not prove the player-facing acceptance criteria. No candidate is `ADOPT_BASELINE` from this run.

## Decision

| Candidate | Decision | Reason |
|---|---|---|
| Effortless Building | `PILOT` | Best documented survival material and server-authority path for assisted placement. Client verification remains required. |
| Building Wands | `NEEDS_MORE_EVIDENCE` | Strong survival material, tool, and limit controls, but the creative path, high defaults, and malformed 26.2 pack metadata need resolution. |
| AutoBuild | `DEFER` | Material-aware building is documented, but placement durability is not, and schematic behavior belongs with LAB-04. |
| WorldEdit | `REJECT` | Command map editor with inventory use disabled and unbounded changed-block defaults. Not a real-material survival building tool. |
| Liteminer | `NEEDS_MORE_EVIDENCE` | Bounded and server-synced, but correct-tool and exact durability behavior need a client test. |
| MinersAdvantage | `DEFER` | Technically bounded and server-aware, but its broad bundle and unclear ore durability create unnecessary overlap. |
| VeinMiner | `PILOT` | Clearest server-side durability, correct-tool, permission, group, and chain-limit controls. |
| Server Side Vein Mine | `NEEDS_MORE_EVIDENCE` | Strong server-only topology and 64-block cap, but no configured durability, correct-tool, or permission boundary was found. |
| Simple Ore Vein Miner | `NEEDS_MORE_EVIDENCE` | Small 64-block ore-only option, but its durability and permission behavior are undocumented. |
| FallingTree | `PILOT` | Server-side tree felling with explicit normal durability and size/tool controls. |
| Tree Harvester | `REJECT` | Technically promising cost controls, but All Rights Reserved licensing blocks pack selection. |
| Ore Harvester | `REJECT` | Technically promising cost controls, but All Rights Reserved licensing and fuzzy ore classification block selection. |
| Chopt | `NEEDS_MORE_EVIDENCE` | Excellent documented durability and hard caps, but it has a persistent stump state, requires both sides, and has a licence discrepancy. |

## Does it fit the pack?

**Yes in principle, but not yet as an accepted baseline.** Existing mods provide a credible no-custom-Java path, and the least invasive provisional stack is:

- **Assisted placement:** Effortless Building, with server-side limits, vanilla reach, inventory-only placement, and durability-enabled break/replace settings. Building Wands is the alternative if its metadata warning and creative/progression paths are resolved.
- **Ore vein mining:** VeinMiner, with `maxChain` reduced to 64 or lower, `needCorrectTool=true`, `permissionRestricted=true`, intentional activation, durability decrease retained, and only approved ore groups enabled.
- **Tree felling:** FallingTree, with a bounded tree size, required axe, `durabilityMode=NORMAL`, `forceToolUsage=true`, and creative mode disabled. Remove VeinMiner's Wood group when both are tested.

Do not combine all candidates. Do not add WorldEdit as a survival construction system. Do not add MinersAdvantage, Liteminer, Simple Ore Vein Miner, Server Side Vein Mine, Tree Harvester, Ore Harvester, or Chopt to the primary pack until the client and authority tests close their specific gaps. AutoBuild and Litematica remain primarily LAB-04 questions because of schematic overlap.

No custom placement, excavation, durability, or global block-break code is justified by this spike. The existing-mod escalation path has not reached the custom-code gate.

## Follow-up required

1. Run a client-capable lab with the provisional Effortless Building + VeinMiner + FallingTree stack. Verify ordinary manual placement with each feature disabled and enabled, exact material consumption, exact tool condition, broken-tool retention, operation caps, cancellation, undo, item drops, and no silent partial result.
2. Repeat with two clients on a dedicated server. Test permission denial, client/server disagreement, disconnect and retry, protected areas, and an unmodified vanilla client where the candidate claims to support one.
3. Exercise Matcha tools across their tiers, including a tool at one condition and a zero-condition/broken state. Confirm no candidate bypasses the Matcha progression.
4. Resolve Building Wands' 26.2 pack metadata warning before considering it as the placement alternative.
5. Resolve the Tree Harvester/Ore Harvester redistribution constraint and Chopt's licence discrepancy before any pack inclusion.
6. Continue with **LAB-04: Blueprint and schematic compatibility spike**. It owns Litematica, OmniBuild Wand, AutoBuild schematic behavior, and WorldEdit schematic interactions.

## Commands and evidence actually run

- Modrinth API version and project queries for every tested artifact and recorded alternative.
- Artifact downloads and checksum verification into `/tmp/lab03/`.
- Isolated Fabric server preparation with `/tmp/lab03/prepare_case.py`.
- Clean dedicated-server boots with `/tmp/lab03/run_case.sh` for the baseline and all 13 candidate cases, using unique ports.
- Archive inspection with `zipinfo`, `unzip -p ... fabric.mod.json`, and resource-path counting.
- Generated configuration inspection for Building Wands, Liteminer, MinersAdvantage, VeinMiner, FallingTree, Tree Harvester, Ore Harvester, Server Side Vein Mine, and WorldEdit.
- Copied-world removal boots for all 13 candidates under `/tmp/lab03/removal/`.
- Post-run process check confirming no LAB-03 server remained.
- Not run intentionally: `./gradlew build`, `./gradlew test`, `./gradlew runClient`, `./gradlew runGametest`, `./scripts/validate-pack.sh`, `./scripts/build-pack.sh`, and `./scripts/report-dependencies.sh`. This ticket changed no Java, no pack metadata, and no primary-pack dependency, while the client commands were blocked by the unavailable display.

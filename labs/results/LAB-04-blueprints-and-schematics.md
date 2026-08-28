# LAB-04: Blueprint and schematic compatibility spike

- Date: 2026-08-28
- Run by: automated lab worker
- Manifest: `labs/manifests/LAB-04-blueprints-and-schematics.toml`
- Status: complete for API, source, archive, dedicated-server loading, and AutoBuild removal; client gameplay acceptance is externally blocked

## Question

Can an existing maintained 26.2 Fabric mod provide the Ghost Plan level, meaning a survival schematic preview and bounded material list for manual placement, without making creative-mode printing the building path?

## Scope and method

This was a compatibility spike, not a gameplay implementation. Candidate versions, release dates, project IDs, version IDs, files, hashes, declared sides, and licences were re-verified against the official Modrinth API on 2026-08-28. The exact manifest records the artifacts and the alternatives that were not added.

The work used four kinds of evidence:

1. Modrinth project and version metadata.
2. Upstream source inspection for Litematica and Simple Blueprints.
3. Archive and bytecode inspection for candidates without usable source, including AutoBuild and HoloBuilder.
4. Fresh dedicated-server fixtures under `/tmp/lab04/` using the pinned Many Roads Home baseline.

No candidate was copied into `pack/`. No third-party JAR or community schematic was committed. No real save was opened or modified.

## Exact versions

The fixed baseline was Minecraft 26.2, Fabric Loader 0.19.3, Fabric API `NqwNSxwA`, Global Packs `DqrPrUMp`, Matcha Flavoured `E9rngRfK`, REI `4o0NSIMj`, Jade `ue8CO97w`, Architectury `1yQC4VvP`, and Cloth Config `Nv3xnWXd`. The pinned Matcha SHA-256 was `6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248`.

| Candidate | Project / version | File | Modrinth licence | Declared topology | Lab treatment |
|---|---|---|---|---|---|
| Litematica | `bEpr0Arc` / `Fhq3KCI8`, 0.28.5 | `litematica-fabric-26.2-0.28.5.jar` | LGPL-3.0-only | client only | source and archive inspection, client launch blocked |
| MaLiLib | `GcWjdA9I` / `ZCq1iE1J`, 0.29.4 | `malilib-fabric-26.2-0.29.4.jar` | LGPL-3.0-only | client only | Litematica dependency, client launch blocked |
| AutoBuild | `wzBO46L7` / `Q7qeA1GB`, 2.0.1+26.2 | `autobuild-2.0.1+26.2.jar` | MIT | client and server required | dedicated-server load, removal, archive and bytecode inspection |
| HoloBuilder - Schematic Loader | `k9maSaFI` / `2dPJ8wxq`, 0.1.5 | `holobuilder-fabric-26.2-0.1.5.jar` | All Rights Reserved on Modrinth | client only | archive inspection, client launch blocked |
| Simple Blueprints | `fYjWAMwz` / `V9DvhYUD`, 26.2-1.2.1 | `simple_blueprints-26.2-1.2.1.jar` | MIT | client only | source and archive inspection, client launch blocked |
| SchematicPreview | `OC1Ud2T4` / `ZtNv1tgJ`, 0.0.17+26.2 | `schematicpreview-0.0.17+26.2.jar` | All Rights Reserved | client only | addon archive inspection, client launch blocked |

The downloaded candidate hashes were:

- Litematica: `e711f69b0899ab3c4afafd49e7b825aa7d5051ee5358c88fef36023c612ae91a`
- MaLiLib: `a691f78959e19ac0e1224e5f3aec63c5fc705ba2b9b7c3a86367e569b95bf2c8`
- AutoBuild: `dd851e1274e8ce3e879658d9c75c542557abeaf691572de5a9f8121d0e7c85e3`
- HoloBuilder: `04c4571cd609c0aba822404637669e25fc75111cd1f8f1b1f57110614f43d774`
- Simple Blueprints: `9d87ab7336b06fd7bc3f2a3880a0629d74868a64c3e0b1093e0e40b7da656959`
- SchematicPreview: `a030767498e92c1f0031386bd22aaf40dd0045151495c8e36335b383678074ca`

## Test world

- Seed: `-481729384`
- World: default Overworld generation
- Difficulty: easy
- Game mode: survival
- Server: dedicated Fabric server in offline mode for the disposable fixture
- Spawn protection: `0`
- View distance: `6`
- Simulation distance: `4`
- Candidate ports: baseline `25565`, AutoBuild `25565`, command probe `25567`, removal `25566`
- JDK: `/home/natea/toolchains/jdk-25.0.4.1+1`, Java 25
- Memory: `-Xms1G -Xmx3G`
- Disposable paths: `/tmp/lab04/instances/baseline`, `/tmp/lab04/instances/autobuild`, `/tmp/lab04/instances/autobuild-command-probe`, and `/tmp/lab04/instances/removal-autobuild`
- Confirmed not a real survival world: yes

The first automated fixture attempt failed before launch because its copy operation used the same source and destination path:

```text
cp: '/tmp/lab04/instances/baseline/config-global_packs.toml' and '/tmp/lab04/instances/baseline/config-global_packs.toml' are the same file
```

The fixture setup was corrected and rerun. The corrected runs used a fresh disposable world and reached the expected server-ready lines.

## Client and server configuration

- Client tested: **no, externally blocked**. This host has no usable display. No client launch, keybind, inventory action, schematic load screen, preview render, material-list interaction, transform, or placement action is claimed as tested.
- Dedicated server tested: **yes** for the baseline, AutoBuild, the malformed-file command probe, and an AutoBuild removal copy.
- Multiplayer tested: **no**. No second client could be launched.
- Claims or protected-area mod installed: **no**. Claim, permission, reach, disconnect, retry, and non-op behavior remain untested.
- Ordinary manual placement and breaking: **not operationally verified**. A clean server boot is not evidence that client interactions remained unchanged.

The server fixtures establish exact release loading, Matcha coexistence, server bootstrap, candidate removal from an unused copy, and the absence of candidate recipe or advancement deltas. They do not close client-only or multiplayer acceptance criteria.

## Format and capability findings

### Litematica

Litematica is the strongest Ghost Plan candidate. The 26.2 source checkout was the `26.2` branch at commit `c00d53fcbcc1b7fd34c68a7cde32c51c0cfd1768`. Source inspection found:

- Native `.litematic` handling through `LitematicaSchematic`.
- `.schem` support through Sponge schematic conversion in the load GUI. The reader validates Sponge format versions 2 and 3, palette and block data, and rejects malformed data.
- Legacy `.schematic` and vanilla `.nbt` file type recognition.
- Schematic metadata, dimensions, block data, entity data, and block-entity data in the native format.
- Material-list classes that calculate total, available, missing, and mismatch counts from the client inventory and optional storage sources. The list can be exported as text or JSON.
- Client-local schematic files and client-local placement or selection state. This is not a server-authoritative project or contract record.

Litematica is explicitly client-side and its project description emphasizes creative-mode work. That does not disqualify its preview layer. A translucent client preview does not mutate the server world, while ordinary manual placement remains a normal server-authoritative Minecraft interaction. It does disqualify Litematica itself as the authority for automated construction.

Litematica does not include a printer. Printer functionality is supplied by separate addons, which are out of scope. Its Easy Place feature is an interaction aid that can be blocked or rejected by a server. It must not be treated as a server construction API or as proof that a placement request is permitted.

### AutoBuild

AutoBuild 2.0.1+26.2 is a broader candidate than the requested Ghost Plan. Its project description advertises `.schem` and `.litematic` loading, ghost preview, transforms, layer or block stepping, material required and missing displays, container inventory, pause and resume, and automatic placement. Static inspection of `BuildEngine`, `BuildStateManager`, `SchematicLoader`, and command classes found evidence of:

- Server-side block mutation through `ServerLevel.setBlock`.
- Inventory and nearby-container material checks, with pausing when materials are missing.
- Survival and adventure handling, collision or protected-block checks, undo entries, and per-player runtime state.
- `.schem` and `.litematic` loader paths.
- Bounds and volume validation, palette and block-data validation, and varint-array validation.

This is enough to call AutoBuild a plausible Ghost Plan plus Assisted Construction comparison. It is not enough to accept it as the pack's construction contract. Static inspection did not establish integration with a claims or permissions provider, a bounded reach policy, tool durability behavior for every operation, or a persistent and versioned project record. The project also advertises instant construction and a community sharing platform. Both require explicit server policy before consideration.

A significant safety concern is that the statically inspected AutoBuild loader falls back to air for unknown block IDs. That may result in omitted blocks rather than an explicit actionable mapping failure. A client test with missing mappings is required before any use.

### HoloBuilder

HoloBuilder claims a translucent hologram, world comparison, transforms, layers, and material preview for `.schem`, `.schematic`, and `.litematic` files. Its archive contains readers for Litematic and Sponge formats and material requirement classes. It is client-only, so it cannot directly mutate server state.

It remains `NEEDS_MORE_EVIDENCE` rather than a recommendation. The Modrinth project is marked All Rights Reserved, while the embedded `LICENSE_holobuilder` in the downloaded JAR states MIT. There is no public source URL in the project metadata. The host could not launch its client UI, so the claimed format and material behavior were not operationally verified.

### Simple Blueprints

Simple Blueprints is useful as a negative control. Its source checkout was the `26.2` branch at commit `9e06529138f95fa754e0a9686353fb96c487b1d5`. It provides transparent previews, layers, rotation, mirroring, and item counts, but its stored local files are `*.dat` and `*.nbt`. It does not implement the target `.litematic` or `.schem` interchange path.

More importantly, source inspection found that `BlueprintBlockData.canPlace(LocalPlayer)` requires `player.isCreative()`, and placement sends client `/setblock` commands. It is therefore creative-only printing rather than a survival-authoritative construction solution. It is rejected for the pack despite having a preview and item-count surface.

### Other candidates

- SchematicPreview is a client-only Litematica addon. It can improve preview utilities but does not establish an independent format, material, or authority layer. It is not needed for the baseline candidate.
- Axiom 5.5.0 has an exact 26.2 release, but the project is an All Rights Reserved creative/editor tool with no source URL. Its newer 6.0.0 26.2 release was alpha and was not considered as a core dependency.
- WorldEdit 7.4.5 supports schematic import and paste, but LAB-03 already rejected it as a privileged map editor rather than a survival planning layer. Its normal use is command-based editing or paste and does not satisfy the no-printing requirement.
- Structurize has an official GPL-3.0 project, but no verified 26.2 Fabric artifact was returned by the Modrinth lookups. No substitute was invented.
- Schematic Placer is a WorldEdit addon whose stated purpose is placing schematics over time. Litematica Printer explicitly adds printing. Both are rejected as printing paths.
- Litematica RawMaterials is an optional MIT addon with an exact 26.2 release. It expands raw crafting requirements but is not necessary to prove that base Litematica supplies a block-level material list.
- Portable Blueprints, Building Blueprint, and Schematica Reloaded had no verified 26.2 Fabric release in the queries used for this lab. The observed descriptions also focus on instant rebuilding or structure placement rather than preview-only planning.

## Three-level comparison

| Candidate | Ghost Plan: preview plus material list, manual placement | Assisted Construction: real materials and durability | Construction Contract: gradual, server-authoritative, not creative printing | Assessment |
|---|---|---|---|---|
| Litematica + MaLiLib | **Yes, strongest fit**. Native `.litematic`, `.schem` conversion, preview, and material counts. | **No as a supported service**. Easy Place may assist normal interactions, but server acceptance and durability were not verified. | **No**. No Forever project, provenance, contract, or worker semantics. | Pilot as an optional client-only Ghost Plan after client and multiplayer tests. |
| AutoBuild | **Yes by declared behavior and static loader/build paths**. | **Plausibly yes**, with server-side material checks and automatic placement. Reach, claims, full durability behavior, and client interaction are unverified. | **No**. It offers automatic and instant modes rather than the specified contract model. | Compatibility spike only. Do not add to the primary pack yet. |
| HoloBuilder | **Claimed yes**, including both target format families and materials. | **No**, client-only. | **No**. | Needs evidence and a licence clarification. |
| Simple Blueprints | **Partial**. Preview and item counts, but only local `.dat` or `.nbt` behavior was found. | **No**. Placement is creative-only through `/setblock`. | **No**. | Reject. |
| SchematicPreview | **Addon only**. It improves Litematica previews but is not an independent provider. | No. | No. | Defer as optional polish. |
| WorldEdit | **No for this design**. It can load schematics, but its normal operation is privileged map editing and paste. | No bounded survival contract established. | No. | Reject, consistent with LAB-03. |
| Axiom | No. Creative/editor focus and no source evidence. | No. | No. | Reject as a survival baseline. |
| Structurize | No verified 26.2 Fabric artifact. | Unknown. | Unknown. | Defer. |

The direct answer is **yes**. Litematica with MaLiLib can provide the Ghost Plan level without itself printing blocks. It should be treated as an optional client planning aid, not as a Forever authority, contract engine, or required capability. No candidate tested here closes all three levels in the intended design.

## Startup outcome

The clean baseline reached the normal server-ready line:

```text
[16:28:31] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[16:28:31] [main/INFO]: Loading 49 mods:
[16:28:35] [main/INFO]: Found new data pack Matcha_Flavoured_1_12.zip, loading it automatically
[16:28:37] [main/INFO]: Loaded 2346 recipes
[16:28:37] [main/INFO]: Loaded 1805 advancements
[16:28:38] [Server thread/INFO]: Done (1.462s)! For help, type "help"
```

AutoBuild loaded in a separate fresh fixture:

```text
[16:33:41] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[16:33:41] [main/INFO]: Loading 50 mods:
	- autobuild 2.0.1+26.2
[16:33:45] [main/INFO]: [AutoBuild] Schematics folder: autobuild/schematics
[16:33:45] [main/INFO]: [AutoBuild] Exports folder: autobuild/schematics/exports
[16:33:47] [main/INFO]: Loaded 2346 recipes
[16:33:47] [main/INFO]: Loaded 1805 advancements
[16:33:49] [Server thread/INFO]: Done (1.816s)! For help, type "help"
```

The server command process was intentionally allowed to run until `timeout` returned 124. That is normal for a healthy server that remains running. The wrapper recorded the expected exit and the ready line was present.

The AutoBuild removal copy booted without the candidate:

```text
[16:37:00] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[16:37:00] [main/INFO]: Loading 49 mods:
[16:37:06] [main/INFO]: Loaded 2346 recipes
[16:37:06] [main/INFO]: Loaded 1805 advancements
[16:37:06] [Server thread/INFO]: Starting Minecraft server on *:25566
[16:37:06] [Server thread/INFO]: Done (0.186s)! For help, type "help"
```

The baseline, AutoBuild, and command-probe fixtures all repeated known baseline Matcha noise, including the duplicate datapack copy error and existing Matcha path and advancement validation warnings. The candidate did not add a new recipe, advancement, or startup failure.

## Matcha interaction

AutoBuild loaded alongside the pinned Matcha pack with the same loaded counts as the baseline: `2346` recipes and `1805` advancements. AutoBuild contains no recipe or advancement data in the inspected archive. Its schematics folder is separate from the Matcha datapack namespace.

Litematica, MaLiLib, HoloBuilder, Simple Blueprints, and SchematicPreview are client-only and were not placed in a dedicated server fixture. No client-side Matcha interaction was claimed.

No candidate archive inspection found bundled community schematic content. AutoBuild advertises a community sharing platform, but the downloaded JAR did not contain community schematic files. Any community or player-authored file remains subject to the strategy document's provenance and redistribution rules.

Known baseline messages included:

```text
java.nio.file.FileAlreadyExistsException: ./datapacks/Matcha_Flavoured_1_12.zip
Invalid path in datapack: minecraft:dimension_type/I_HATE_TRUE_DARKNESS.txt, ignoring
Couldn't parse data file 'minecraft:advancement/custom/root'
```

These were also present without AutoBuild and are not candidate compatibility failures.

## Multiplayer interaction

Multiplayer was not run because the host cannot launch a client. No desynchronization, permission bypass, claim behavior, disconnect behavior, retry behavior, or per-player material race was observed.

The authority assessment is therefore split:

- Litematica's preview is client-only and does not have authority to mutate blocks. Manual placement remains a normal server interaction. A server can reject an interaction, and Litematica's Easy Place documentation warns that servers may block or ban such behavior. This is compatible with the server-authority rule only when used as a preview and manual-placement reference.
- AutoBuild has a server component and static evidence of server-side mutation and material checking. Static inspection did not establish a claims API or a complete permission and reach policy. Its C2S and build paths require a real non-op multiplayer test before acceptance.
- HoloBuilder and Simple Blueprints cannot be server authority because their Modrinth metadata declares them client-only. Simple Blueprints additionally emits `/setblock` commands only for creative players.

No custom networking, parser, renderer, placement engine, or authority adapter was written.

## Persistence

The candidates do not provide a safe Forever project persistence contract as tested:

- Litematica stores schematic files under a client-local schematics directory and keeps selections, placements, schematic projects, and related block-entity data in client-local state. It is not a server save record.
- HoloBuilder and Simple Blueprints use client-local blueprint or schematic files and configuration. Their state is not a server-authoritative settlement or project record.
- AutoBuild created `autobuild/schematics` and `autobuild/schematics/exports` in its server fixture. Static inspection showed per-player build state in runtime structures. The boot-only run did not create a verified persistent active-plan record in the world save.

The removal test only covered a fresh, unused copied world. It did not cover removing a candidate while a client-local plan is active or while an AutoBuild operation is in progress. Physical blocks remain the authoritative world state. A future Forever record must preserve a readable unavailable state rather than silently deleting a plan when a client mod is removed.

## Missing mappings, malformed files, and physical-world safety

The acceptance test could not be fully exercised without a display, but static evidence gives useful bounds:

| Case | Evidence | Result of this lab |
|---|---|---|
| Litematica `.schem` mapping | Source reader validates format, palette, and block data, warns on unknown palette blocks, and returns failure on read errors. | Promising, but the warning and UI path need a client test. |
| AutoBuild malformed or unsupported file | Static loader dispatches `.schem` and `.litematic`, validates dimensions, volume, palette, and data, and throws on invalid input. | The server command probe with malformed files produced no player feedback because commands were issued from the console. Runtime handling remains inconclusive. |
| AutoBuild unknown block ID | Static inspection found fallback to air. | Safety concern. Must be changed, rejected, or proven to produce an explicit actionable limitation before use. |
| HoloBuilder mappings and malformed files | Readers were present in the archive. | Client UI unavailable, so untested. |
| Simple Blueprints mappings | Local `.dat` and `.nbt` paths only. | Does not establish target `.litematic` or `.schem` handling. |
| Unavailable client or removed mod | Physical world is independent of client preview in the fresh fixtures. | Fresh-world removal passed. Active-plan migration remains untested. |

No candidate operation was run against a populated survival world, and no test changed a physical block. This demonstrates fixture safety, not successful gameplay placement.

## Blueprint metadata and provenance contract

Blueprints must remain optional. The player must be able to hand-build and register a functional structure using ordinary Minecraft building, with no blueprint, style, palette, or exact-template requirement. The pack must not commit or redistribute community schematic content without explicit permission.

If a future Forever project or contract record refers to an imported plan, it should store metadata and a bounded reference rather than copy the whole schematic into a world record. At minimum, the design should reserve fields for:

- Explicit record schema version and codec or serializer version.
- Stable plan or project ID and owner UUID.
- Origin such as player-created, pack-authored, or imported.
- Source file reference, source format, and SHA-256 content hash.
- Creator or author attribution, creation and modification timestamps, and licence or permission statement.
- Minecraft/data version, parser or provider name and version, and any required addon or mod version.
- Dimensions, bounded block count, bounded entity and block-entity counts, and bounded NBT size.
- Transform, layer, and placement orientation.
- Whether block entities, entities, commands, inventories, or other executable payloads are present. Commands and inventories should be rejected or explicitly stripped from a planning reference unless separately approved.
- Availability state such as available, missing file, unsupported format, missing mapping, unavailable client, or invalid content.

Imported files should remain with the player or approved distribution source. The server may retain the reference and provenance state, but it must not silently claim redistribution rights. Invalid records need visible failure and preservation of the original reference where possible. Any persistent record requires the migration and save-safety treatment required by the architecture and world-save documents.

## Functional validation independent of visual similarity

The project and settlement specifications support hand-built registration and functional validation without requiring a schematic. A colour-independent review should inspect the actual contract, not palette similarity or block count. Examples of functional predicates include:

- Required entrance or access path exists.
- Enclosure, roof, and weather protection meet the relevant functional threshold.
- Required room, workstation, storage, or production nodes exist and are reachable.
- Structural connections and required adjacency relationships exist.
- Safety, clearance, and interaction reach are valid.
- Required block tags or capabilities are present, while substitute blocks remain possible where the contract permits them.

A preview may help a player plan those outcomes, but it must not turn exact visual reproduction into settlement registration or mastery progression.

## Acceptance criteria result

| Ticket criterion | Result | Evidence and limitation |
|---|---|---|
| A player can build and register a functional structure without a blueprint. | **Design pass, runtime not tested** | The project and settlement specifications explicitly allow hand-built, style-independent registration. No client was available to operationally exercise it. |
| At least one candidate previews or describes a player-authored plan without changing server block authority. | **Partial pass** | Litematica is client-only and its preview does not mutate the world. Its source and format paths are strong evidence. AutoBuild also describes a preview, but its server placement path requires multiplayer authority testing. |
| Missing mappings, unsupported files, and unavailable client tooling preserve the physical world and show an actionable limitation. | **Partial, client blocked** | Litematica has source-level validation and warning or failure paths. AutoBuild has bounds and malformed-input checks, but also an unknown-ID-to-air fallback. The GUI and physical-world cases were not run. |
| Blueprint metadata and provenance are defined. | **Pass for this spike** | The metadata and provenance contract above defines the minimum reference, hash, attribution, licence, bounds, payload, and unavailable-state fields. No persistent schema was implemented. |
| Functional criteria are independent from visual similarity or palette. | **Pass for this review** | The functional validation section uses access, enclosure, reachability, capability, and safety predicates rather than exact block count, colour, or style. |

The required client and multiplayer test set remains open. This report does not convert source or static evidence into a claim that placement, material deduction, or permission behavior passed.

## Removal behaviour

AutoBuild was removed from a copy of its fresh test world. The copy loaded 49 mods, reached the normal ready line, and loaded the same recipe and advancement counts. No AutoBuild registry or leftover state prevented startup.

This is a narrow removal pass. It proves that an unused world does not depend on AutoBuild for startup. It does not prove that active build state, client-local schematic projects, contract references, or unavailable files migrate cleanly. The safe future behavior is to retain physical blocks and expose an unavailable or legacy plan status with provenance, not to delete the physical build or silently discard the record.

Litematica, MaLiLib, HoloBuilder, Simple Blueprints, and SchematicPreview were not removed from a launched client because the client could not be started.

## Performance observations

Hardware was an AMD Ryzen 7 9700X with 19.5 GiB RAM. No profiler was run.

- Baseline server ready time was `Done (1.462s)` after the fixture was initialized.
- AutoBuild server ready time was `Done (1.816s)`.
- The removal copy ready time was `Done (0.186s)`.
- No permanent chunk loading was observed in the boot-only server logs.
- AutoBuild static defaults included a preview budget of 700 operations per tick, a paste budget of 80 per tick, and an undo maximum of 50,000 entries. These are configuration observations, not performance measurements.
- Client render cost, material-list cost, preview bounds, and large schematic behavior were not measured.

Any future adoption must impose bounded dimensions, volume, material entries, entities, block entities, NBT, and per-tick work. A plan must never trigger an unbounded world scan or force-load chunks for a passive preview.

## Conflicts observed

- AutoBuild did not change the server recipe or advancement counts from the baseline in its server fixture.
- No candidate supplied a guidebook or automatic starter item. Litematica and the other client-only candidates therefore cannot be adopted on the assumption that in-game onboarding already exists.
- AutoBuild created its own schematic and export directories. No world-save contract was observed in the boot-only run.
- Client UI overlap, keybind conflict, preview rendering, and material-list usability were not operationally tested because there was no display.
- AutoBuild's project and `fabric.mod.json` declare MIT, while the bundled `LICENSE_autobuild` says CC0. HoloBuilder's project is All Rights Reserved while its embedded license says MIT. These are release and redistribution gates, not harmless metadata differences.
- No downloaded candidate bundled community schematic files. The absence of bundled files does not grant permission to redistribute user or community files from a sharing service.

## Does it fit the pack?

The pack should adopt the smallest existing boundary that answers the player problem. That is an optional Litematica plus MaLiLib client pilot for Ghost Plan only, subject to a real client and multiplayer test. It should not include Litematica Printer, a server paster, WorldEdit paste commands, or any other printing addon as part of that pilot.

Litematica fits because its preview and material list are useful without changing the server's block authority, and because ordinary manual placement remains available. Its client-only persistence must not be mistaken for a Forever project record. Its Easy Place behavior must be disabled or explicitly treated as untrusted normal interaction until server testing proves otherwise.

AutoBuild is technically capable of more than Ghost Plan and is therefore a poor default for the current modpack boundary. It could be a future isolated compatibility experiment only after its license conflict, unknown-block fallback, claims and permissions behavior, reach, durability, cancellation, disconnect, and instant-mode policy are resolved. It is not the Construction Contract specified by the project.

HoloBuilder is a plausible alternative preview, but its licence contradiction and unavailable source make it a release risk. Simple Blueprints, Axiom, WorldEdit, Schematic Placer, and Litematica Printer do not fit the survival planning boundary. No custom Java is justified by this lab. ADR 0033 would be required only if a later, documented cross-mod gap remains after the existing candidates and data/configuration options are exhausted.

The plan remains optional at every level. A player can build and register a functional structure without using any blueprint.

## Follow-up required

1. Run a disposable client with Litematica and MaLiLib on a host with a usable display. Load one valid `.litematic` and one valid Sponge `.schem`, then record preview, transforms, layers, total/available/missing/mismatch material counts, unload, and manual placement behavior.
2. Repeat with malformed files, an unknown block mapping, unsupported format, missing dependency, and an unavailable client. Each case must leave the physical world intact and show an actionable limitation.
3. Run a two-client dedicated-server test with a non-op player, a claims or permission boundary, mismatched client installs, cancellation, disconnect and retry, and server restart. Confirm that every block mutation, material deduction, and permission decision is server-authoritative.
4. Bound material-list and parse inputs before considering any provider for a shared pack. Include dimensions, volume, entities, block entities, NBT, and per-tick work.
5. Obtain written licence clarification for AutoBuild and HoloBuilder. For AutoBuild, also establish whether instant mode can be disabled and whether a supported claims or permission integration exists.
6. Decide whether the next implementation ticket needs only documentation and pack configuration for an optional client Ghost Plan or whether a separately scoped integration gap remains. Do not write a custom renderer, parser, or placement engine as a response to this lab.

Next backlog ticket: `LAB-06: Seasons and climate compatibility spike`. It was not started in this change.

## Decision

| Candidate | Decision | Reason |
|---|---|---|
| Litematica | `PILOT` | Strongest existing Ghost Plan implementation with `.litematic`, `.schem` conversion, previews, and material lists. Client and multiplayer evidence is still required. |
| MaLiLib | `PILOT` | Required Litematica dependency. No independent player-facing feature was evaluated. |
| AutoBuild | `NEEDS_MORE_EVIDENCE` | Provides Ghost Plan and server-side assisted construction on paper, but it can overreach into printing and has unresolved licence, mapping, claim, permission, reach, durability, and client-runtime risks. |
| HoloBuilder | `NEEDS_MORE_EVIDENCE` | Plausible preview and material alternative, but client-only behavior is untested and the project/JAR licence declarations conflict. |
| Simple Blueprints | `REJECT` | Does not establish `.litematic` or `.schem` support and gates placement behind creative-only `/setblock` behavior. |
| SchematicPreview | `DEFER` | Litematica addon rather than an independent provider. Revisit only if the base Litematica pilot demonstrates a concrete preview gap. |
| Axiom | `REJECT` | Creative/editor focus, All Rights Reserved project, and no source URL. |
| WorldEdit | `REJECT` | LAB-03 already established it as a privileged map editor and paste tool, not the survival planning layer. |
| Structurize | `DEFER` | No verified 26.2 Fabric artifact. |
| Litematica Printer | `REJECT` | Explicit printing implementation conflicts with the no-creative-printing scope. |
| Schematic Placer | `REJECT` | WorldEdit placement addon whose purpose is schematic printing over ticks. |
| Litematica RawMaterials | `DEFER` | Optional material expansion. Base Litematica already supplies the required block-level list for Ghost Plan. |
| Blueprints image projection | `DEFER` | Exact 26.2 client release exists, but the evidence reviewed did not establish a schematic material-list provider. |
| Portable Blueprints | `DEFER` | No verified 26.2 Fabric release and its stated instant rebuilding needs a separate authority review. |
| Building Blueprint | `DEFER` | No verified 26.2 Fabric release, All Rights Reserved, and placement-oriented description. |
| Schematica Reloaded | `DEFER` | No verified 26.2 Fabric release. |

## Commands and evidence actually run

- Read `labs/README.md`, the LAB-01 and LAB-02 examples, the LAB-04 backlog ticket, the project and settlement specifications, `docs/blueprints/strategy.md`, `docs/design/building-assistance.md`, the relevant ADRs, `docs/world-save-safety.md`, and the LAB-03 result before changing artifacts.
- Queried the official Modrinth project and version API for each candidate, using the exact 26.2 Fabric filter recorded in the manifest.
- Downloaded only disposable fixtures under `/tmp/lab04/` and verified candidate hashes with `sha256sum`.
- Inspected archives with `jar tf`, Fabric metadata, embedded licence files, and candidate resource paths.
- Cloned and inspected the 26.2 source branches for Litematica and Simple Blueprints. Recorded source revisions in the format findings above.
- Inspected AutoBuild bytecode with `javap` for loader, command, build, material, and server mutation behavior.
- Ran the clean baseline server with `timeout 300 .../java -Xms1G -Xmx3G -jar fabric-server-launch.jar nogui`. It reached `Done` and returned timeout 124 because the server remained alive.
- Ran the AutoBuild server fixture with the same disposable-world method. It reached `Done` and returned timeout 124 for the same normal reason.
- Ran the AutoBuild removal copy on port 25566. It reached `Done` and returned timeout 124 for the same normal reason.
- Ran a server command probe with malformed `.schem` and `.litematic` files and `autobuild list`, `autobuild load`, `autobuild materials`, and `autobuild missing`. The process exited cleanly, but console execution did not expose player-scoped command feedback, so malformed handling is not marked as passed.
- Intentionally did not run `./gradlew build`, `./gradlew test`, `./scripts/validate-pack.sh`, or `./scripts/build-pack.sh`. This ticket changes only lab documentation and does not alter the pack or companion code. No client command was run because the host has no display.

## Evidence sources

- Modrinth project API: `https://api.modrinth.com/v2/project/<project_id>`
- Modrinth version API: `https://api.modrinth.com/v2/project/<project_id>/version?game_versions=%5B%2226.2%22%5D&loaders=%5B%22fabric%22%5D`
- Litematica project: `https://modrinth.com/mod/litematica`
- Litematica source: `https://github.com/sakura-ryoko/litematica`
- Litematica documentation: `https://github.com/maruohon/litematica/wiki`
- MaLiLib project: `https://modrinth.com/mod/malilib`
- AutoBuild project: `https://modrinth.com/mod/autobuild`
- AutoBuild project site: `https://autobuildmc.com`
- HoloBuilder project: `https://modrinth.com/mod/holobuilder-schematic-loader`
- Simple Blueprints source: `https://github.com/Maxboxx/SimpleBlueprints`
- SchematicPreview source: `https://github.com/DimasKama/SchematicPreview`
- WorldEdit source and documentation: `https://github.com/EngineHub/WorldEdit/` and `https://worldedit.enginehub.org/en/latest/`
- Structurize source: `https://github.com/ldtteam/Structurize`

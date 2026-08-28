# LAB-10: Performance, ambience, structures, and worldgen compatibility spike

- Date: 2026-08-28
- Run by: coordinating agent, automated
- Manifest: `labs/manifests/LAB-10-performance-structures-ambience.toml`
- Status: complete for the dedicated-server, archive, and world-save questions; client rendering and client ambience remain blocked by the headless test environment

## Question

Which performance mods are safe for the 26.2 baseline, and which structure or ambience candidates can be used without violating the conservative worldgen posture or adding avoidable pack noise?

## Headline result

- **Adopt as the shipped server baseline:** Lithium `f7vZ0VWU` and FerriteCore `d5ddUdiB`. Both are release builds, started cleanly on the dedicated server, preserved the 2346 recipe and 1805 advancement baseline, and added no guidebook or starter-item surface in archive inspection.
- **Adopt only in a development profile:** spark `iYFOl6lQ`. It is useful for evidence, not player-facing baseline content. Its idle sample reported 20 TPS and a 0.9 ms median tick.
- **Do not adopt an alpha or beta as core:** Sodium `a9YZH3ip` is alpha and Sound Physics Remastered `d8iioMMp` is beta. Both are deferred even though the beta audio candidate passed a server smoke boot.
- **Do not add a structure-content provider to `pack/` from this lab.** No pair was stacked. Structory is the only content provider retained as a possible future **single-provider pilot**, and Sparse Structures is only a possible future spacing-control pilot. Neither is a baseline decision.
- **Keep ambience optional.** Client-only ambience candidates require a display-backed client test. No candidate found in archive inspection introduced a second guidebook or explicit starter item.

## Exact versions

The manifest records the exact project ID, version ID, file name, release channel, publication timestamp, licence, and Modrinth and local hashes for all 22 candidate and required-dependency artifacts. The 18 primary candidates were all re-verified against the live Modrinth API on 2026-08-28. The four required libraries used by the isolated structure/content fixtures were also re-verified.

| Component | Project ID | Version ID | File | API release type and environment |
|---|---|---|---|---|
| Minecraft | | | `26.2` | fixed baseline |
| Fabric Loader | | | `0.19.3` | fixed baseline |
| Fabric API | `P7dR8mSH` | `NqwNSxwA` | `fabric-api-0.158.0+26.2.jar` | fixed baseline |
| Global Packs | `NRLPy2mk` | `DqrPrUMp` | `globalpacks-fabric-26.2-26.2.0.jar` | fixed baseline |
| Matcha Flavoured | `QI0EmgZ1` | `E9rngRfK` | `Matcha_Flavoured_1_12.zip` | fixed baseline |
| Lithium | `gvQqBUqZ` | `f7vZ0VWU` | `lithium-fabric-0.25.3+mc26.2.jar` | release, client or server |
| FerriteCore | `uXXizFIs` | `d5ddUdiB` | `ferritecore-9.0.0-fabric.jar` | release, client or server |
| ImmediatelyFast | `5ZwdcRci` | `vkW9vHhp` | `ImmediatelyFast-Fabric-1.16.4+26.2.jar` | release, client only |
| Entity Culling | `NNAgCjsB` | `iiF6U3Ne` | `entityculling-fabric-1.10.5-mc26.2.jar` | release, client only |
| Sodium | `AANobbMI` | `a9YZH3ip` | `sodium-fabric-0.9.2-alpha.4+mc26.2.jar` | **alpha**, client only |
| spark | `l6YH9Als` | `iYFOl6lQ` | `spark-1.10.173-fabric.jar` | release, client or server |
| Mod Menu | `mOgUt4GM` | `njXb639R` | `modmenu-20.0.1.jar` | release, client only |
| Sparse Structures | `qwvI41y9` | `iPGkJT7H` | `sparsestructures-fabric-26.2-3.1.4.jar` | release, server only |
| Structory | `aKCwCJlY` | `TUbwu7eG` | `Structory_26.2_v1.3.7.jar` | release, server only |
| Dungeons and Taverns | `tpehi7ww` | `TaODUzQu` | `dungeons-and-taverns-5.3.1.jar` | release, server only |
| MVS, Moog's Voyager Structures | `OQAgZMH1` | `cvepxaBC` | `MoogsVoyagerStructures-1.21-5.0.11.jar` | release, server only |
| Towns and Towers | `DjLobEOy` | `eN3WLQ3P` | `t_and_t-fabric-neoforge-1.13.11.jar` | release, server only/client optional |
| Repurposed Structures | `muf0XoRe` | `CrmgMIJp` | `repurposed_structures-7.7.6+26.2-fabric.jar` | release, server only |
| Sound Physics Remastered | `qyVF9oeo` | `d8iioMMp` | `sound-physics-remastered-fabric-1.5.1+26.2.jar` | **beta**, client only/server optional |
| Particular Reforged | `pYFUU6cq` | `1VSvTbTm` | `particular-26.2-Fabric-1.5.7.jar` | release, client only |
| Presence Footsteps | `rcTfTZr3` | `8TkGmrgl` | `PresenceFootsteps-1.13.3+26.2.jar` | release, client only |
| AmbientSounds | `fM515JnW` | `jGlT0HRa` | `AmbientSounds_FABRIC_v6.3.6_mc26.2.jar` | release, client only |
| Friends&Foes | `POQ2i9zu` | `rJBCX3gG` | `friendsandfoes-fabric-4.0.27+mc26.2.jar` | release, client and server |

The Structory API reports version `1.3.17`, while the exact file name returned by Modrinth is `Structory_26.2_v1.3.7.jar`. Both values are recorded deliberately rather than normalised.

API verification used the official version endpoint `https://api.modrinth.com/v2/version/<version_id>` and the 26.2 Fabric project-version filter. The downloaded files were checked against the returned SHA-1 and SHA-512 values and against local SHA-256 values in the manifest.

## Test world

All worlds were disposable instances below `/tmp/lab10/`. No real survival save was opened, modified, or used as a source.

- World type: normal overworld, `generate-structures=true`.
- Difficulty: easy.
- Server mode: offline mode only because the test server was local and isolated.
- JVM: Temurin `25.0.4.1+1`, `-Xmx3G`.
- Performance startup seed: `LAB10-PERF-SAME` for the original clean/Lithium/FerriteCore/both comparison, plus `LAB10-RSS-PERF` for fresh wall-clock and RSS samples.
- Structure startup seed: `LAB10-STRUCT-SAME`.
- Locate seed: `LAB10-LOCATE-SAME`.
- Bounded generation seed: `LAB10-WORLDGEN-SAME`.
- Spark sample seed: `LAB10-SPARK`.
- Each candidate provider was run in its own instance. No structure-content pair was co-installed.
- For the bounded generation sample, `forceload add -120 -120 120 120` marked 256 chunks. The saved region scan contained 4 region files and 400 chunks at `minecraft:full`, plus earlier generation-stage records. This is a deliberately bounded sample, not a global density benchmark.

## Client and server configuration

- Client tested: **blocked**. The current lab environment has no usable display for a client launch. Sodium, ImmediatelyFast, Entity Culling, Mod Menu, Particular Reforged, Presence Footsteps, and AmbientSounds therefore have no runtime rendering or client-audio result here. Sodium is also alpha and is deferred independently of the display blocker.
- Dedicated server tested: yes. Lithium, FerriteCore, their combination, all six structure candidates, Friends&Foes, spark, and the server-optional Sound Physics build reached a normal ready line and exited cleanly after `stop`.
- Multiplayer tested: no real player connection. Server-side startup and command authority were exercised, but no two-client flow was available.
- Client-only jars were intentionally not placed in server instances. This avoids treating a side-mismatch failure as a valid compatibility result.

## Startup outcome

### Clean baseline and performance variants

The fixed baseline reference is 2346 recipes and 1805 advancements. The original server log also recorded the expected baseline noise from the existing pack:

```text
[17:14:40] [main/INFO]: Loaded 2346 recipes
[17:14:40] [main/INFO]: Loaded 1805 advancements
[17:14:42] [Server thread/INFO]: Done (1.679s)! For help, type "help"
```

The comparable candidate runs reported:

| Instance | Added server-side candidate | Recipes | Advancements | Server log `Done` |
|---|---|---:|---:|---:|
| `perf-clean` | none | 2346 | 1805 | 1.488 s |
| `perf-lithium` | Lithium | 2346 | 1805 | 1.729 s |
| `perf-ferrite` | FerriteCore | 2346 | 1805 | 1.709 s |
| `perf-both` | Lithium + FerriteCore | 2346 | 1805 | 1.512 s |

Representative lines from the combined run:

```text
[16:52:11] [main/INFO]: Loaded 2346 recipes
[16:52:11] [main/INFO]: Loaded 1805 advancements
[16:52:13] [Server thread/INFO]: Done (1.512s)! For help, type "help"
```

A fresh runner sample with RSS collection reported:

```text
logs/rss-clean.console:READY_SECONDS 10.609
logs/rss-clean.console:PEAK_RSS_KB 1554408
logs/rss-clean.console:EXIT_CODE 0
logs/rss-both.console:READY_SECONDS 10.349
logs/rss-both.console:PEAK_RSS_KB 1165936
logs/rss-both.console:EXIT_CODE 0
```

The RSS and wall-clock values are observations from one run each and are not evidence that the combined mods reduce memory or startup time. They varied with library extraction, JIT warm-up, filesystem cache, and the concurrent lab workload. The internal `Done` time is not the same measurement as the runner's end-to-end `READY_SECONDS` value.

### Structure and content variants

| Instance | Candidate | Recipes | Advancements | Server log `Done` | Candidate-specific issue |
|---|---|---:|---:|---:|---|
| `struct-sparse` | Sparse Structures | 2346 | 1805 | 1.520 s | none |
| `struct-structory` | Structory | 2346 | 1805 | 2.117 s | none |
| `struct-dnt` | Dungeons and Taverns | 2346 | 1810 | 2.689 s | advancement load error list |
| `struct-mvs` | MVS | 2346 | 1805 | 2.901 s | none |
| `struct-towns` | Towns and Towers | 2346 | 1805 | 2.930 s | none |
| `struct-repurposed` | Repurposed Structures | 2346 | 1824 | 1.833 s | none |
| `content-friends-foes` | Friends&Foes | 2364 | 1807 | 1.817 s | advancement load error list and server-side client-class warning |

Dungeons and Taverns emitted this candidate-specific line before the count:

```text
[16:53:54] [main/ERROR]: Couldn't load advancements: [nova_structures:nether/find_hamlet, nova_structures:adventure/find_shrine, ...]
[16:53:54] [main/INFO]: Loaded 1810 advancements
```

Friends&Foes emitted:

```text
[16:54:47] [main/WARN]: Error loading class: net/minecraft/client/renderer/state/level/BlockOutlineRenderState (java.lang.ClassNotFoundException: net/minecraft/client/renderer/state/level/BlockOutlineRenderState)
[16:54:52] [main/INFO]: Loaded 2364 recipes
[16:54:52] [main/ERROR]: Couldn't load advancements: [friendsandfoes:husbandry/tame_a_glare, ...]
[16:54:52] [main/INFO]: Loaded 1807 advancements
```

The class warning was not a startup failure, but it is a server-side compatibility smell in a candidate that is being considered here only as adjacent ambience content. Its extra mobs, structures, recipes, loot, and advancements also make it unsuitable as a pure ambience baseline.

### Spark development profile

The server smoke test reached ready and accepted `spark tps`:

```text
[17:15:08] [Server thread/INFO]: Done (1.986s)! For help, type "help"
[17:15:10] [spark-worker-pool-1-thread-1/INFO]: [⚡] TPS from last 5s, 10s, 1m, 5m, 15m:
[17:15:10] [spark-worker-pool-1-thread-1/INFO]: [⚡]  *20.0, *20.0, *20.0, *20.0, *20.0
[17:15:10] [spark-worker-pool-1-thread-1/INFO]: [⚡] Tick durations (min/med/95%ile/max ms) from last 10s, 1m:
[17:15:10] [spark-worker-pool-1-thread-1/INFO]: [⚡]  0.5/0.9/9.0/31.7;  0.5/0.9/9.0/31.7
[17:15:10] [spark-worker-pool-1-thread-1/INFO]: [⚡] CPU usage from last 10s, 1m, 15m:
[17:15:10] [spark-worker-pool-1-thread-1/INFO]: [⚡]  36%, 36%, 36%  (system)
[17:15:10] [spark-worker-pool-1-thread-1/INFO]: [⚡]  21%, 21%, 21%  (process)
```

This is an idle, approximately 10-second observation, not a gameplay or chunk-generation benchmark. It validates that spark is useful in a development profile and does not justify shipping it to players.

### Common baseline diagnostics

The following appeared in the clean baseline and in candidate instances, so they are not attributed to the candidates:

```text
[17:14:38] [main/ERROR]: java.nio.file.FileAlreadyExistsException: ./datapacks/Matcha_Flavoured_1_12.zip
[17:14:38] [main/ERROR]: java.nio.file.FileAlreadyExistsException: ./datapacks/Matcha_Flavoured_1_12.zip
[17:14:39] [main/ERROR]: Couldn't parse data file 'minecraft:advancement/custom/root' from 'minecraft:loot_table/advancement/custom/root.json': DataResult.Error['Not a JSON object: null']
[17:14:39] [main/ERROR]: Couldn't parse data file 'main:mechanics/wither_test' from 'main:advancement/mechanics/wither_test.json': DataResult.Error['Advancement completion requirements did not exactly match specified criteria. Missing: [summoned_wither]. Unknown: [eat_glow_jam]']
```

The global-packs refmap warning, Java native-access warnings, offline-mode warning, and invalid `HELP.txt` datapack paths were also baseline noise. Candidate-specific errors are called out separately above.

## Matcha interaction

The clean baseline and both server-side performance variants all reported the exact reference counts:

- Baseline: **2346 recipes, 1805 advancements**.
- Lithium: **2346 recipes, 1805 advancements**.
- FerriteCore: **2346 recipes, 1805 advancements**.
- Lithium plus FerriteCore: **2346 recipes, 1805 advancements**.

Sparse Structures, Structory, MVS, and Towns and Towers also preserved 2346/1805. Repurposed Structures preserved the recipe count and loaded 19 additional advancement files, producing 1824 loaded advancements. Dungeons and Taverns preserved the recipe count but loaded 1810 advancements and emitted its candidate-specific advancement error list. These are not acceptable as silent noise in the shipped baseline.

Friends&Foes changed the counts to **2364 recipes and 1807 advancements**. Its archive contains 19 recipe files, 14 advancement files, loot, structures, and entity content. It is not an ambience-only addition and needs a separate content and advancement review.

No performance or pure ambience archive contained recipe or advancement paths. Zip archive inspection found no `guidebook`, Patchouli, Field Guide, questbook, or explicit starter-item path in the candidates. This preserves LAB-01's finding that the current pack has no guidebook problem. Client UI and actual first-join item flows remain unverified without a display-backed client test.

No candidate was found to override Matcha identifiers directly. The pre-existing Matcha diagnostics above remain visible in all server runs, so a future pack-integrity check should not rely on those errors alone to detect a candidate regression.

## Multiplayer interaction

No player connection was available. The server remained authoritative for commands, world generation, and save loading. No candidate introduced a custom player state or a client-decided server outcome in these tests. Client/server mismatch testing for client-only rendering and ambience mods is blocked, not passed.

## Persistence

- Lithium, FerriteCore, spark, and Sound Physics server smoke tests did not introduce candidate-specific persistent world data in archive or save inspection. Their ordinary configuration/cache files were confined to disposable instances.
- Sparse Structures changes structure-placement calculations and therefore affects generation decisions, even though its jar contains no content structures. It is not persistence-neutral for a world that generates new chunks.
- Structory, Dungeons and Taverns, MVS, Towns and Towers, Repurposed Structures, and Friends&Foes write generated structure starts and blocks into chunk data when their content is placed. Those blocks and chunk records become part of the world.
- The bounded world scans produced no NBT parser errors. They are not a substitute for a migration test across multiple seeds and versions.

## Structure placement and overlap evidence

The following archive counts are direct counts of JSON records in the downloaded jars. Placement ranges are parsed from structure-set JSON files. They are useful for relative scope, not a guarantee of global density.

| Candidate | Structure definitions | Structure sets | Parsed placement spacing | Parsed separation | Bounded custom starts |
|---|---:|---:|---:|---:|---:|
| Structory | 16 | 5 | 23 to 44, median 30 | 10 to 16 | 0 in 400 full chunks |
| Dungeons and Taverns | 202 `nova_structures` definitions | 32 | 16 to 200, median 70 | 6 to 190 | 6 in 400 full chunks |
| MVS | 129 | 114 | 12 to 112, median 34 | 7 to 98 | 17 in 400 full chunks |
| Towns and Towers | 64 | 3 | 32 to 51, median 48 | 12 to 16 | 0 in 400 full chunks |
| Repurposed Structures | 167 | 52, with 37 parsed placement records | 1 to 400, median 53 | 0 to 250 | 3 in 400 full chunks |
| Friends&Foes | 4 | 4 | 16 to 40, median 40 | 8 to 10 | 0 in 400 full chunks |
| Sparse Structures | 0 | 0 | global multiplier, not content | global multiplier | not applicable |

Dungeons and Taverns also contains 72 JSON files below `data/minecraft/worldgen/`, consisting of configured features, placed features, template pools, and processor lists, plus six vanilla structure tags. This is broader than simply adding isolated landmarks. Its project page recommends deleting structure-set files to remove individual structures, but that is a pre-generation configuration operation, not a safe rollback for chunks already generated.

Sparse Structures' shipped default configuration was inspected directly:

```text
"spreadFactor": 2,
"idBasedSalt": true
```

The project description says the default doubles structure spacing and that the global factor applies to all structures. It also warns that very low factors make generation considerably slower. The id-based salt may reduce some placement overlap, but it does not make stacking structure providers safe or undo generated structures. It should only be considered alongside one already-approved provider, never as a reason to add several providers.

### Locate samples

`locate structure` was run from the origin in fresh instances with one provider at a time. These results demonstrate that the candidate registrations are active. They do not measure global density or prove that two providers would not compete.

| Candidate | Locate samples |
|---|---|
| Structory | `structory:northern_ruin` at 546 blocks; `structory:old_manor` at 4481 blocks |
| Dungeons and Taverns | `nova_structures:badlands_miner_outpost` at 4032 blocks; `nova_structures:tavern_acacia` at 4457 blocks |
| MVS | `mvs:small_ruin` at 4982 blocks; `mvs:cathedral` at 3915 blocks |
| Towns and Towers | `towns_and_towers:village_forest` at 1664 blocks; `towns_and_towers:pillager_outpost_forest` at 4103 blocks |
| Repurposed Structures | `repurposed_structures:mansion_desert` at 10238 blocks; `repurposed_structures:fortress_jungle` at 8850 blocks |
| Friends&Foes | `friendsandfoes:iceologer_cabin` at 2979 blocks; `friendsandfoes:illusioner_shack` at 1114 blocks |

### Bounded generation sample

The generated structure starts below were read directly from the four region files in each disposable world. All scans completed without `NBT_ERROR` output.

- Dungeons and Taverns: six custom starts, including `firewatch_tower_forest`, `trial_dungeon`, `cave_chamber_dungeon_colony`, `deepslate_camp`, `illager_camp`, and `tavern_jungle`.
- MVS: 17 custom starts, including four haystacks, three piles, and smaller wells, carts, pens, barns, a windmill, and towers.
- Repurposed Structures: three custom starts, including a savanna mineshaft, grassy igloo, and jungle pyramid.
- Structory, Towns and Towers, and Friends&Foes: no custom start occurred in this particular bounded 400-full-chunk sample. Their `locate` probes still returned registered structures, so this is not evidence that they never generate.
- Every generation case also contained normal vanilla starts such as mineshafts, ancient cities, and jungle pyramids. Vanilla generation remained functional in all cases.

The bulk generation command produced expected lag warnings because it intentionally forced 256 chunks at once:

```text
[17:03:35] [Server thread/WARN]: Can't keep up! Is the server overloaded? Running 4831ms or 96 ticks behind
```

This is evidence that forced bulk generation is expensive, not a claim about ordinary exploration performance.

## Removal behaviour

### Performance removal

A copy of the fresh `rss-both` world was made as `remove-perf`. Lithium and FerriteCore were removed from that copy. It booted with the baseline counts and stopped cleanly:

```text
[17:15:47] [main/INFO]: Loaded 2346 recipes
[17:15:47] [main/INFO]: Loaded 1805 advancements
[17:15:47] [Server thread/INFO]: Done (0.187s)! For help, type "help"
READY_SECONDS 7.266
PEAK_RSS_KB 1291224
EXIT_CODE 0
```

This supports treating the two performance mods as stateless and removable from a copy. It does not establish that every future version will have identical behaviour.

### Structure removal

A copy of the already-generated `gen-dnt` world was made as `remove-dnt`, and the Dungeons and Taverns jar was removed. The server reached ready and exited 0, but the log exposed the save risk:

```text
[17:15:55] [main/WARN]: Missing data pack mr_dungeons_andtaverns
[17:15:58] [Server thread/ERROR]: Unknown structure start: nova_structures:deepslate_camp
[17:15:58] [Server thread/ERROR]: Unknown structure start: nova_structures:illager_camp
[17:15:59] [Server thread/INFO]: Done (1.393s)! For help, type "help"
READY_SECONDS 8.502
PEAK_RSS_KB 1361500
EXIT_CODE 0
```

The pre-removal region scan contained six Dungeons and Taverns custom starts. The removed-copy scan still contained four custom starts, including `firewatch_tower_forest`, `trial_dungeon`, `cave_chamber_dungeon_colony`, and `tavern_jungle`. The two starts reported as unknown were not restored or cleanly migrated. No automatic rollback of generated blocks occurred. This is a concrete demonstration that a server booting after removal does not mean removal is safe.

The official Structory project page states that adding it to an existing world affects only new chunks. This agrees with the world-save rule: an existing world remains authoritative, and a future pilot needs a worldgen lock, a backup, a seed/chunk fixture, a compatibility notice, and a removal plan before persistent release.

## Performance observations

Hardware and environment:

- AMD Ryzen 7 9700X 8-Core Processor, 12 logical processors visible to the lab.
- Linux x86_64 under WSL2.
- 19.5 GiB host memory reported, approximately 16 GiB swap configured.
- Temurin JDK `25.0.4.1+1`.
- Server process launched with `-Xmx3G`.

The measurements are intentionally modest. Each startup was a single observation on one machine, with cached libraries and concurrent lab activity. The values establish that the candidates boot and preserve content counts. They do not establish a statistically reliable performance improvement. A real performance acceptance pass needs repeated cold and warm starts, representative entity and chunk workloads, and longer spark samples under the actual pack.

Operational lab rejection gates used here, which are evidence gates rather than new gameplay balance values:

1. Alpha, beta, or release-candidate builds cannot be core dependencies.
2. A performance-only candidate must not add recipes or advancements, and a startup error attributable to the candidate blocks baseline adoption.
3. A server candidate must reach `Done (...)`, exit cleanly, and preserve the known baseline counts unless its content delta is the explicit subject of another lab.
4. No structure-content pair is allowed in the conservative baseline. A future pilot may use at most one provider and must pass fresh-world, seed, chunk-boundary, existing-world, backup, and removal review.
5. An unknown structure start after removal is a failed removal-safety result, even if the server subsequently reaches ready.
6. One startup or one idle tick sample is insufficient to claim a performance gain.

## Conflicts observed

- No duplicate recipe or item conflict was observed for Lithium, FerriteCore, Sparse Structures, Structory, MVS, Towns and Towers, or the pure client ambience archives.
- Dungeons and Taverns added advancement files and emitted a large candidate-specific advancement load error list. Its broad `data/minecraft/worldgen` footprint and All Rights Reserved licence also make it a poor foundation candidate.
- Repurposed Structures added 19 advancement files and raised the loaded advancement count by 19. Its structure variations cover many existing structure families and include extremely broad spacing ranges.
- Friends&Foes added 19 recipes, 14 advancement files, mobs, structures, and loot. It emitted advancement errors and one server-side client-class warning. It is not a pure ambience mod.
- No candidate archive exposed a second guidebook or explicit starter item. A client first-join flow was not tested.
- No client keybind or UI overlap was tested because the client could not launch.
- No structure providers were stacked, by design. Therefore this lab does not claim pairwise non-overlap. The conservative conclusion is to avoid stacking rather than infer safety from isolated runs.
- Existing Matcha/global-packs errors remained present in every server case. They are baseline diagnostics, not candidate regressions.

## Does it fit the pack?

### Performance

Lithium and FerriteCore fit the pack's server-authoritative, existing-mod-first posture. They do not add player-facing mechanics, do not alter recipe or advancement counts, and passed clean dedicated-server startup. They should be added to the primary pack only after the normal dependency review records their exact hashes. The runtime evidence is a compatibility result, not a measured claim that they improve every workload.

spark fits only as a development tool. It should not ship in the player baseline because it is a diagnostic surface rather than gameplay content.

ImmediatelyFast, Entity Culling, and Mod Menu are plausible client additions but remain `NEEDS_MORE_EVIDENCE` until a display-backed client smoke test covers startup, settings, keybinds, and multiplayer connection. Sodium is deferred because the verified 26.2 artifact is alpha and because client rendering was not testable here.

### Structures and worldgen

Vanilla worldgen is the only current baseline. Structory has the smallest measured content footprint and no recipe or advancement noise, so it is the only structure-content candidate worth a future **single-provider pilot**. That is not permission to add it to existing worlds or to combine it with Sparse Structures without a separate decision. Sparse Structures may be useful as a controlled spacing experiment, but its default global multiplier changes vanilla, datapack, and modded structures together and therefore is not a neutral baseline dependency.

Dungeons and Taverns, MVS, Towns and Towers, and Repurposed Structures are not suitable for the current restrained posture. Their scope, structure-set footprints, family overlap, content noise, licence constraints, or removal behavior make them unsuitable for default inclusion. Friends&Foes belongs in a separate mob/content lab, not in this ambience baseline.

### Ambience

Ambience remains optional and must not carry gameplay state by itself. Sound Physics passed a server-optional smoke boot but is beta, so it is deferred. Particular Reforged, Presence Footsteps, and AmbientSounds are release client candidates but need a display-backed audio and performance playtest. No final ambience assets were authored by this repository.

## Follow-up required

1. Keep `pack/` on vanilla world generation for the current foundation milestone. Do not add a structure provider from this lab.
2. If exploration later needs one structure provider, open a separate worldgen pilot for Structory alone. Lock the worldgen choice, publish a fresh-world-only notice, test multiple seeds and chunk boundaries, back up before testing, and document the existing-world and removal policy before changing `pack/`.
3. If spacing is still a problem after that pilot, evaluate Sparse Structures as a separate controlled configuration experiment. Do not combine it with an unapproved provider or use it to conceal overlap.
4. Run a display-backed client lab for Sodium only after a non-alpha 26.2 release exists, and for ImmediatelyFast, Entity Culling, Mod Menu, Particular Reforged, Presence Footsteps, and AmbientSounds. The test should include a real client connection and first-join/UI/audio checks.
5. Keep spark in the development profile and repeat profiling on representative entity, chunk, and exploration workloads. Use more than one startup and record longer TPS and memory windows.
6. Review the pre-existing Matcha diagnostics separately. Candidate clean startup does not make those baseline errors harmless.
7. No custom Java or worldgen hook is justified by this lab. ADR 0033 gap analysis was not invoked.

## Decision

| Candidate | Decision | Reason |
|---|---|---|
| Lithium `f7vZ0VWU` | `ADOPT_BASELINE` | Release build, dedicated-server startup passed, 2346/1805 preserved, no candidate content noise. |
| FerriteCore `d5ddUdiB` | `ADOPT_BASELINE` | Release build, dedicated-server startup passed, 2346/1805 preserved, no candidate content noise. |
| ImmediatelyFast `vkW9vHhp` | `NEEDS_MORE_EVIDENCE` | Client-only release; no display-backed client test. |
| Entity Culling `iiF6U3Ne` | `NEEDS_MORE_EVIDENCE` | Client-only release; no display-backed client test. |
| Sodium `a9YZH3ip` | `DEFER` | Verified 26.2 artifact is alpha and client rendering was not testable. |
| spark `iYFOl6lQ` | `ADOPT_BASELINE` for development profile only | Useful profiling command and healthy idle sample; not player-facing content. |
| Mod Menu `njXb639R` | `NEEDS_MORE_EVIDENCE` | Client-only UI; no display-backed test. |
| Sparse Structures `iPGkJT7H` | `PILOT` only | Global spacing controller, not a content provider. Future use requires a separate configuration and worldgen review, never a stacked default. |
| Structory `TUbwu7eG` | `PILOT` only | Smallest measured structure footprint and no recipe/advancement noise, but generated chunks are permanent and no baseline worldgen dependency is approved. |
| Dungeons and Taverns `TaODUzQu` | `REJECT` for the current baseline | Broad content and vanilla-worldgen footprint, advancement errors, All Rights Reserved metadata, and unsafe post-generation removal behavior. |
| MVS `cvepxaBC` | `DEFER` | 129 structures and 114 sets are too broad for the current restrained posture; requires a separate single-provider decision. |
| Towns and Towers `eN3WLQ3P` | `DEFER` | Village/outpost overhaul overlaps existing worldgen and has a non-commercial share-alike licence; no default adoption. |
| Repurposed Structures `CrmgMIJp` | `REJECT` for the current baseline | 167 structures, 52 sets, broad structure-family variation, 1824 loaded advancements, and wide spacing ranges conflict with the locked conservative posture. |
| YUNG's Better Structures | `NEEDS_MORE_EVIDENCE` | No exact verified 26.2 Fabric Modrinth project/version was found; no similarly named substitute was invented. |
| Sound Physics Remastered `d8iioMMp` | `DEFER` | Server smoke boot passed, but the verified build is beta and client audio was not testable. |
| Particular Reforged `1VSvTbTm` | `NEEDS_MORE_EVIDENCE` | Client-only release; no display-backed particle/audio test. |
| Presence Footsteps `8TkGmrgl` | `NEEDS_MORE_EVIDENCE` | Client-only release; no display-backed audio test. |
| AmbientSounds `jGlT0HRa` | `NEEDS_MORE_EVIDENCE` | Client-only release; no display-backed audio test. |
| Friends&Foes `rJBCX3gG` | `DEFER` | Not ambience-only; adds mobs, structures, recipes, loot, and advancements, with candidate-specific advancement errors. Requires a separate content lab. |
| Vanilla Minecraft world generation | `ADOPT_BASELINE` | Preserves ordinary world creation and is the only generation baseline approved by ADR 0015. |

## Sources and evidence paths

- ADR 0015: `docs/adr/0015-conservative-worldgen-posture.md`
- LAB-10 ticket: `docs/backlog.md`
- Modrinth API records: `/tmp/lab10/api/` during the run, with exact IDs and hashes retained in the manifest
- Disposable server logs: `/tmp/lab10/logs/`
- Bounded region summaries: `/tmp/lab10/results/gen-*-structures.json`
- Direct archive inspection: `/tmp/lab10/artifacts/`
- Structory project page: `https://modrinth.com/mod/structory`
- Dungeons and Taverns project page: `https://modrinth.com/mod/dungeons-and-taverns`
- Sparse Structures project page: `https://modrinth.com/mod/sparsestructures`

The next backlog ticket is **LAB-11: Adventure rewards and capability-gate compatibility spike**. It is not started by this change.

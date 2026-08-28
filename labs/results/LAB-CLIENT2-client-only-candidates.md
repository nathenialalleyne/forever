# LAB-CLIENT2: Client-only candidates from LAB-10

- Date: 2026-08-28
- Run by: llama, automated
- Parent lab: `LAB-10: Performance, ambience, structures, and worldgen compatibility spike`
- Status: complete for client loading, OpenGL initialisation, resource reload, and archive content checks. Visual quality, in-world behaviour, first-join behaviour, multiplayer, and audio remain unverified where noted.

## Question

Do the LAB-10 client/display-blocked candidates load on a Minecraft 26.2 client without candidate-specific startup failure, and do they add recipe, advancement, guidebook, or starter-item noise?

This follow-up corrects LAB-10's false display blocker. It does not change LAB-10's server-side performance or conservative worldgen conclusions.

## Scope and exact versions

The primary artifacts are the exact versions from the LAB-10 manifest. LAB-10 re-verified these releases against the Modrinth API on 2026-08-28, and the local SHA-256 values were checked before these runs. No substitute version was used.

| Candidate | Project ID | Version ID | File | Modrinth channel and environment |
|---|---|---|---|---|
| ImmediatelyFast | `5ZwdcRci` | `vkW9vHhp` | `ImmediatelyFast-Fabric-1.16.4+26.2.jar` | release, client only |
| Entity Culling | `NNAgCjsB` | `iiF6U3Ne` | `entityculling-fabric-1.10.5-mc26.2.jar` | release, client only |
| Sodium | `AANobbMI` | `a9YZH3ip` | `sodium-fabric-0.9.2-alpha.4+mc26.2.jar` | **alpha**, client only |
| Mod Menu | `mOgUt4GM` | `njXb639R` | `modmenu-20.0.1.jar` | release, client only |
| Sound Physics Remastered | `qyVF9oeo` | `d8iioMMp` | `sound-physics-remastered-fabric-1.5.1+26.2.jar` | **beta**, client only/server optional |
| Particular Reforged | `pYFUU6cq` | `1VSvTbTm` | `particular-26.2-Fabric-1.5.7.jar` | release, client only |
| Presence Footsteps | `rcTfTZr3` | `8TkGmrgl` | `PresenceFootsteps-1.13.3+26.2.jar` | release, client only |
| AmbientSounds | `fM515JnW` | `jGlT0HRa` | `AmbientSounds_FABRIC_v6.3.6_mc26.2.jar` | release, client only |

The temporary fixtures also used the required libraries below. These were downloaded under `/tmp` only and were not added to `pack/`.

| Library | Project ID | Version ID | File |
|---|---|---|---|
| BaguetteLib, required by Particular | `OfKzpbRU` | `dqvmJFYA` | `baguettelib-26.2-Fabric-2.0.5.jar` |
| Forge Config API Port, required by Particular | `ohNO6lps` | `rSd3GiG8` | `ForgeConfigAPIPort-v26.2.1-mc26.2.x-Fabric.jar` |
| Kirin, required by Presence Footsteps | `9aNz8Zqn` | `fmosRz2v` | `kirin-1.22.0+26.2.jar` |
| CreativeCore, required by AmbientSounds | `OsZiaDHq` | `SI6rsPDj` | `CreativeCore_FABRIC_v2.14.16_mc26.2.jar` |

All cases used the fixed baseline of Minecraft `26.2`, Fabric Loader `0.19.3`, Fabric API `0.158.0+26.2`, Global Packs `26.2.0`, Matcha Flavoured `1.12`, REI `26.2.820`, Jade `26.2.11`, Architectury API `21.0.7`, Cloth Config `26.2.155`, and Forever `0.0.0-dev`.

## Client environment and method

Hardware and runtime:

- AMD Ryzen 7 9700X 8-Core Processor
- Linux x86_64 under WSL2
- 19.5 GiB host memory reported
- Temurin JDK `25.0.4.1+1`
- `DISPLAY=:0`
- OpenGL 4.5 through Mesa `25.2.8`, renderer `llvmpipe`

Each case was a fresh copy of a disposable client run directory under `/tmp/lab10/client2/`. The fixed baseline client was run once as a control. Each candidate was then added alone, with only its required libraries. No candidate was added to `pack/`.

The exact repository harness `scripts/client-smoke.sh` was reused from an isolated copy of the project. Every client launch was bounded by `CLIENT_SMOKE_TIMEOUT=45` seconds. The control used 60 seconds. The client was expected to remain open, so timeout was the normal stop path. The per-case wrapper also checked and terminated any detached Java client belonging to `/tmp/lab10/client2/project` after the timeout. A final process check found no client survivor in that fixture.

Screenshots were not used. Window capture is black on this host. The evidence below is from the raw console logs, Fabric's loaded-mod list, candidate loggers, OpenGL initialisation, and stitched texture atlases.

### Harness status interpretation

The harness reported the following successful signals for every candidate case:

```text
client-smoke: ok: Using graphics backend OpenGL, using drivers: 4.5 (Core Profile) Mesa 25.2.8-0ubuntu0.24.04.2
client-smoke: ok: stitched 13 texture atlases
client-smoke: ok: the companion mod initialised on the client
```

The harness itself returned non-zero in these fixtures because the existing baseline emits diagnostics that the current script does not exclude, including Global Packs' duplicate Matcha copy attempt and Matcha resource/model warnings. Some cases also produced status `141` when the script's `pipefail` warning preview encountered the deliberately truncated warning stream. Those statuses are not treated as a client crash. The raw logs reached the OpenGL and atlas markers in every case. Candidate-specific warnings and errors are classified separately below.

The clean control demonstrates the environmental audio failure:

```text
[17:34:12] [Render thread/ERROR] (Minecraft) Error starting SoundSystem. Turning off sounds & music
java.lang.IllegalStateException: Failed to open OpenAL device
```

The narrator also fails because `libflite.so` is absent. These are host limitations, not candidate regressions.

## Startup observations

The timestamps have one-second resolution. They measure time from the first `Loading Minecraft` line to the first OpenGL line and the blocks atlas line. They are not FPS, frame-time, or workload measurements.

| Case | Fabric loaded mods | Load to OpenGL | Load to blocks atlas | Rendered 13 atlases |
|---|---:|---:|---:|---:|
| Clean control | 59 | about 5 s | about 6 s | yes |
| ImmediatelyFast | 60 | about 5 s | about 6 s | yes |
| Entity Culling | 62 | about 4 s | about 5 s | yes |
| Sodium | 60 | about 4 s | about 5 s | yes |
| Mod Menu | 60 | about 5 s | about 5 s | yes |
| Sound Physics Remastered | 60 | about 5 s | about 7 s | yes |
| Particular Reforged | 62 | about 4 s | about 6 s | yes |
| Presence Footsteps | 61 | about 5 s | about 6 s | yes |
| AmbientSounds | 62 | about 4 s | about 6 s | yes |

A single startup per case on one machine is weak evidence. It does not justify a performance claim. In particular, it does not establish that ImmediatelyFast, Entity Culling, or Sodium improves frame time in an in-world workload.

## Recipe, advancement, guidebook, and starter-item check

The standalone client did not connect to a server, so it emitted no `Loaded N recipes` or `Loaded N advancements` lines. Those counts are server/data-pack startup observations, not standalone menu-client observations.

LAB-10's server reference remains **2346 recipes and 1805 advancements** for the clean baseline, Lithium, FerriteCore, and Lithium plus FerriteCore. This follow-up did not repeat those server runs because its scope was client-only. The eight primary candidate archives each contained zero recipe paths and zero advancement paths in direct archive inspection:

| Candidate | Recipe paths | Advancement paths | Guidebook/starter path-name matches |
|---|---:|---:|---:|
| ImmediatelyFast | 0 | 0 | 0 |
| Entity Culling | 0 | 0 | 0 |
| Sodium | 0 | 0 | 0 |
| Mod Menu | 0 | 0 | 0 |
| Sound Physics Remastered | 0 | 0 | 0 |
| Particular Reforged | 0 | 0 | 0 |
| Presence Footsteps | 0 | 0 | 0 |
| AmbientSounds | 0 | 0 | 0 |

The path scan covered `data/*/recipes`, `data/*/recipe`, `data/*/advancements`, and path names containing `guidebook`, `patchouli`, `field guide`, `questbook`, `starter`, or `on_join`. It does not prove that a mod cannot grant an item through code. No server connection or first-join flow was available in this spike, so that behaviour remains a human or dedicated-server follow-up.

AmbientSounds reports 60 sound assets in its loaded engine, and Presence Footsteps and Sound Physics carry sound resources. Those are expected ambience assets, not recipe or advancement noise.

## Candidate results

### ImmediatelyFast `vkW9vHhp`

**New verdict: `PILOT`. LAB-10 verdict superseded: yes, for the display-backed load question.**

The candidate loaded and initialised its rendering integration:

```text
/tmp/lab10/client2/logs/immediatelyfast.console
[17:35:19] [main/INFO] (FabricLoader) Loading 60 mods:
	- immediatelyfast 1.16.4+26.2
[17:35:24] [Render thread/INFO] (ImmediatelyFast) Initializing ImmediatelyFast 1.16.4+26.2 on llvmpipe (LLVM 20.1.2, 256 bits) (Mesa) with OpenGL 4.5 (Core Profile) Mesa 25.2.8-0ubuntu0.24.04.2
[17:35:24] [Render thread/INFO] (Minecraft) Using graphics backend OpenGL, using drivers: 4.5 (Core Profile) Mesa 25.2.8-0ubuntu0.24.04.2
[17:35:25] [Render thread/INFO] (Minecraft) Created: 2048x2048x4 minecraft:textures/atlas/blocks.png-atlas
```

No ImmediatelyFast-specific warning or error was found after comparison with the clean control. The archive contained no recipe, advancement, guidebook, or starter-item path. This proves client loading and a real OpenGL resource reload. It does not prove that the optimisation is beneficial or visually correct in a world. A human pass still needs in-world rendering, settings, and frame-time observation.

### Entity Culling `iiF6U3Ne`

**New verdict: `PILOT`. LAB-10 verdict superseded: yes, for the display-backed load question.**

Fabric loaded Entity Culling and its nested rendering libraries, then reached the OpenGL and atlas markers:

```text
/tmp/lab10/client2/logs/entityculling.console
[17:36:14] [main/INFO] (FabricLoader) Loading 62 mods:
	- entityculling 1.10.5
	   |-- transition 1.0.21
	   \-- trender 1.0.15
[17:36:14] [main/WARN] (FabricLoader/Mixin) Reference map 'entityculling.refmap.mixins.json' for entityculling.mixins.json could not be read. If this is a development environment you can ignore this message
[17:36:18] [Render thread/INFO] (Minecraft) [STDOUT]: [TRender] Initializing Client...
[17:36:18] [Render thread/INFO] (Minecraft) Using graphics backend OpenGL, using drivers: 4.5 (Core Profile) Mesa 25.2.8-0ubuntu0.24.04.2
[17:36:19] [Render thread/INFO] (Minecraft) Created: 2048x2048x4 minecraft:textures/atlas/blocks.png-atlas
```

The reference-map message is a non-fatal development-style warning. The client did not crash and rendered 13 atlases. No Entity Culling-specific recipe, advancement, guidebook, or starter-item path was found. The culling behaviour itself was not exercised in a loaded world, so this is a compatibility pilot rather than proof that occlusion decisions are correct or that frame time improves.

### Mod Menu `njXb639R`

**New verdict: `PILOT`. LAB-10 verdict superseded: yes, for the display-backed load question.**

The client loaded Mod Menu and reached its update-checker entrypoint:

```text
/tmp/lab10/client2/logs/modmenu.console
[17:37:04] [main/INFO] (FabricLoader) Loading 60 mods:
	- modmenu 20.0.1
[17:37:08] [Render thread/INFO] (Mod Menu/Update Checker) Checking mod updates...
[17:37:09] [Render thread/INFO] (Minecraft) Using graphics backend OpenGL, using drivers: 4.5 (Core Profile) Mesa 25.2.8-0ubuntu0.24.04.2
[17:37:09] [Render thread/INFO] (Minecraft) Created: 2048x2048x4 minecraft:textures/atlas/blocks.png-atlas
```

No Mod Menu-specific warning or error was found beyond the clean-control diagnostics. Its archive contained no recipe, advancement, guidebook, or starter-item path. The Mod Menu screen was not opened or visually inspected. The black screenshot limitation and lack of interactive UI automation mean this result proves loading only, not that every configuration screen or button works.

### Sodium `a9YZH3ip`

**New verdict: `DEFER`. LAB-10 verdict superseded: no.**

The alpha artifact did load and rendered through the Mesa OpenGL path:

```text
/tmp/lab10/client2/logs/sodium.console
[17:38:30] [main/INFO] (FabricLoader) Loading 60 mods:
	- sodium 0.9.2-alpha.4+mc26.2
[17:38:30] [main/INFO] (Sodium) Loaded configuration file for Sodium: 37 options available, 0 override(s) found
[17:38:30] [main/WARN] (FabricLoader/Mixin) @Mixin target net.minecraft.client.renderer.StagedVertexBuffer.GpuBufferPool was not found sodium-common.mixins.json:core.StagedVertexBufferMixin from mod sodium
[17:38:30] [main/WARN] (Sodium-GraphicsAdapterProbe) Failed to query PCI device name for 0x1414:0x008e
[17:38:30] [main/WARN] (Sodium-Workarounds) Sodium has applied one or more workarounds to prevent crashes or other issues on your system: [NO_ERROR_CONTEXT_UNSUPPORTED]
[17:38:35] [Render thread/INFO] (Sodium-GlSurface) OpenGL Vendor: Mesa
[17:38:35] [Render thread/INFO] (Sodium-GlSurface) OpenGL Renderer: llvmpipe (LLVM 20.1.2, 256 bits)
[17:38:35] [Render thread/INFO] (Sodium-GlSurface) OpenGL Version: 4.5 (Core Profile) Mesa 25.2.8-0ubuntu0.24.04.2
[17:38:35] [Render thread/INFO] (Minecraft) Created: 2048x2048x4 minecraft:textures/atlas/blocks.png-atlas
```

This is useful evidence that the alpha build can initialise and render on this host. It also exposes a missing mixin target, PCI probe failure, and an applied workaround. The alpha release channel remains a hard reason not to make Sodium a core dependency. The archive contained no recipe, advancement, guidebook, or starter-item path. A non-alpha 26.2 release is required before reconsideration.

### Sound Physics Remastered `d8iioMMp`

**New verdict: `DEFER`. LAB-10 verdict superseded: no.**

The candidate loaded and its reverb setup ran before the host audio failure:

```text
/tmp/lab10/client2/logs/sound-physics.console
[17:39:28] [main/INFO] (FabricLoader) Loading 60 mods:
	- sound_physics_remastered 1.5.1+26.2
[17:39:33] [Render thread/INFO] (Sound Physics - General) Reloading reverb parameters
[17:39:33] [Render thread/INFO] (Minecraft) Using graphics backend OpenGL, using drivers: 4.5 (Core Profile) Mesa 25.2.8-0ubuntu0.24.04.2
[17:39:35] [Render thread/ERROR] (Minecraft) Error starting SoundSystem. Turning off sounds & music
[17:39:35] [Render thread/INFO] (Minecraft) Created: 2048x2048x4 minecraft:textures/atlas/blocks.png-atlas
```

No Sound Physics-specific error was found. The `SoundSystem` error is identical to the clean control and reports `Failed to open OpenAL device`. Therefore this run verifies Fabric loading and candidate reverb initialisation only. It cannot verify whether the effect sounds good, whether reverb is applied correctly, or whether the candidate behaves correctly after entering a world. The Modrinth artifact is beta, so the LAB-10 deferral stands independently of the audio blocker. Its archive contained no recipe, advancement, guidebook, or starter-item path.

### Particular Reforged `1VSvTbTm`

**New verdict: `PILOT`. LAB-10 verdict superseded: yes, for the display-backed load question.**

The required libraries and candidate initialised, and the client rendered its resource atlases:

```text
/tmp/lab10/client2/logs/particular.console
[17:40:22] [main/WARN] (FabricLoader/Resolution) Warnings were found!
 - Mod 'Forge Config API Port' (forgeconfigapiport) 26.2.1 recommends any version of modmenu, which is missing!
 - Mod 'Particular Reforged' (particular) 1.5.7 recommends any version of modmenu, which is missing!
[17:40:22] [main/INFO] (FabricLoader) Loading 62 mods:
	- baguettelib 2.0.5
	- forgeconfigapiport 26.2.1
	- particular 1.5.7
[17:40:26] [Render thread/INFO] (BaguetteLib) BaguetteLib loaded on Fabric!
[17:40:26] [Render thread/INFO] (Particular Reforged) I am quite particular about the effects I choose to add :3
[17:40:26] [Render thread/INFO] (Minecraft) Using graphics backend OpenGL, using drivers: 4.5 (Core Profile) Mesa 25.2.8-0ubuntu0.24.04.2
[17:40:28] [Render thread/INFO] (Minecraft) Created: 2048x2048x4 minecraft:textures/atlas/blocks.png-atlas
```

The missing Mod Menu messages are recommendations, not missing required dependencies. Mod Menu was intentionally omitted from this isolated Particular fixture so the candidate was tested with only required libraries. The candidate and BaguetteLib also emit non-fatal missing-reference-map warnings. No Particular-specific exception or crash occurred. Its archive contained no recipe, advancement, guidebook, or starter-item path.

This proves client loading and resource reload, not that the particles look correct or that Particular's effects are desirable. A combined Particular plus Mod Menu profile and an in-world visual pass remain appropriate before any player-facing adoption.

### Presence Footsteps `8TkGmrgl`

**New verdict: `PILOT`, conditional on preparing its configuration directory. LAB-10 verdict superseded: yes, but the new evidence replaces the old display blocker with a configuration issue.**

The first fresh-directory run loaded the candidate and rendered, but it emitted a candidate-specific error:

```text
/tmp/lab10/client2/logs/presence-footsteps.console
[17:41:16] [main/INFO] (FabricLoader) Loading 61 mods:
	- kirin 1.22.0+26.2
	- presencefootsteps 1.13.3+26.2
[17:41:20] [Render thread/ERROR] (PathMonitor) java.nio.file.NoSuchFileException: /tmp/lab10/client2/project/run/config/presencefootsteps
[17:41:21] [Render thread/INFO] (Minecraft) Using graphics backend OpenGL, using drivers: 4.5 (Core Profile) Mesa 25.2.8-0ubuntu0.24.04.2
[17:41:22] [Render thread/INFO] (Minecraft) Created: 2048x2048x4 minecraft:textures/atlas/blocks.png-atlas
```

A second run created the expected empty `run/config/presencefootsteps` directory before launch. The PathMonitor error did not recur, while the same loader and rendering markers appeared:

```text
/tmp/lab10/client2/logs/presence-footsteps-configdir.console
[17:46:14] [main/INFO] (FabricLoader) Loading 61 mods:
	- kirin 1.22.0+26.2
	- presencefootsteps 1.13.3+26.2
[17:46:20] [Render thread/INFO] (Minecraft) Using graphics backend OpenGL, using drivers: 4.5 (Core Profile) Mesa 25.2.8-0ubuntu0.24.04.2
[17:46:22] [Render thread/INFO] (Minecraft) Created: 2048x2048x4 minecraft:textures/atlas/blocks.png-atlas
```

The first result means a truly empty fresh instance does not produce a clean candidate log. The second means the release can load without that error when its configuration directory exists. The result is therefore a pilot with an explicit packaging or setup condition, not a clean baseline approval. The archive contained no recipe, advancement, guidebook, or starter-item path.

The client reached rendering, but no working audio device was available. Actual footstep playback, sound selection, and interaction with a server-loaded world remain unverified.

### AmbientSounds `jGlT0HRa`

**New verdict: `PILOT`. LAB-10 verdict superseded: yes, for the display-backed load question.**

AmbientSounds and CreativeCore loaded, its engine registered, and the client rendered:

```text
/tmp/lab10/client2/logs/ambient-sounds.console
[17:42:10] [main/INFO] (FabricLoader) Loading 62 mods:
	- ambientsounds 6.3.6
	- creativecore 2.14.16
[17:42:10] [main/WARN] (FabricLoader/Mixin) Reference map 'creativecore.mixins.refmap.json' for creativecore.mixins.json could not be read. If this is a development environment you can ignore this message
[17:42:10] [main/WARN] (FabricLoader/Mixin) Reference map 'ambientsounds.mixins.refmap.json' for ambientsounds.mixins.json could not be read. If this is a development environment you can ignore this message
[17:42:14] [Render thread/INFO] (Minecraft) Using graphics backend OpenGL, using drivers: 4.5 (Core Profile) Mesa 25.2.8-0ubuntu0.24.04.2
[17:42:15] [Render thread/INFO] (ambientsounds) Loaded AmbientEngine 'basic' v3.2.5. 11 dimension(s), 11 features, 11 blockgroups, 2 sound collections, 38 regions, 60 sounds, 12 sound categories, 6 solids and 2 biome types
[17:42:16] [Render thread/INFO] (Minecraft) Created: 2048x2048x4 minecraft:textures/atlas/blocks.png-atlas
```

The two CreativeCore reference-map messages and the AmbientSounds reference-map message are non-fatal development-style warnings. No candidate-specific exception or crash occurred. The archive contained no recipe, advancement, guidebook, or starter-item path.

The engine registration is verified, but the host cannot play audio. No claim is made about sound quality, mixing, performance, or whether the 60 registered sounds are appropriate for the pack. Those need a working-audio human pass.

## Audio and visual limits

The following claims are deliberately not made:

- Sound Physics reverb sounds correct.
- Presence Footsteps produces correct footsteps.
- AmbientSounds produces suitable ambience.
- Particular's particle effects look correct.
- ImmediatelyFast, Entity Culling, or Sodium improves in-world frame time.
- Mod Menu's screens are usable or free of visual layout issues.
- Any candidate behaves correctly after a real player connects to a server.

The host has no working OpenAL device and logs `Error starting SoundSystem. Turning off sounds & music` in the clean control and all candidate cases. Screenshots capture black. These limitations do not prevent verifying that a real OpenGL context was created and that resource atlases were stitched, but they prevent sound and visual-quality acceptance.

## Persistence, worldgen, and server authority

This client-only spike did not open a survival save, generate chunks, or test structure placement. It makes no change to ADR 0015. Vanilla world generation remains the only approved baseline, and no structure candidate was added or stacked.

The client-only candidates wrote only disposable client configuration or cache state during these runs. No server world was used, and no persistent survival save was opened. A real multiplayer authority test was not performed.

## LAB-10 verdict changes

| Candidate | LAB-10 verdict | LAB-CLIENT2 verdict | Did the old display blocker stand? | Result |
|---|---|---|---|---|
| ImmediatelyFast `vkW9vHhp` | `NEEDS_MORE_EVIDENCE` | `PILOT` | no | Changed. It loads and initialises on OpenGL 4.5, but performance and in-world behaviour remain unproven. |
| Entity Culling `iiF6U3Ne` | `NEEDS_MORE_EVIDENCE` | `PILOT` | no | Changed. It loads and renders, with a non-fatal reference-map warning. Culling behaviour remains untested. |
| Mod Menu `njXb639R` | `NEEDS_MORE_EVIDENCE` | `PILOT` | no | Changed. It loads and reaches its update checker. The UI was not manually inspected. |
| Sodium `a9YZH3ip` | `DEFER` | `DEFER` | no, but alpha reason survives | Stands. It renders, but it is alpha and emits mixin and workaround warnings. |
| Sound Physics Remastered `d8iioMMp` | `DEFER` | `DEFER` | no, but beta and audio limits survive | Stands. It initialises reverb parameters, but it is beta and audio cannot be judged. |
| Particular Reforged `1VSvTbTm` | `NEEDS_MORE_EVIDENCE` | `PILOT` | no | Changed. It loads with required libraries, with non-fatal reference-map and missing-optional-Mod-Menu warnings. Visual effects remain untested. |
| Presence Footsteps `8TkGmrgl` | `NEEDS_MORE_EVIDENCE` | `PILOT`, conditional on its config directory | no, but a new config issue was found | Changed with a condition. Empty config directory removes the PathMonitor error. Audio remains untested. |
| AmbientSounds `jGlT0HRa` | `NEEDS_MORE_EVIDENCE` | `PILOT` | no | Changed. Its AmbientEngine registers and the client renders, with non-fatal reference-map warnings. Audio remains untested. |

No server-side performance or structure verdict changed. LAB-10's `ADOPT_BASELINE` decisions for Lithium and FerriteCore, development-only placement of spark, and ADR 0015's no-new-worldgen-dependency posture remain in force. The server reference counts remain 2346 recipes and 1805 advancements.

## Follow-up required

1. Keep all eight candidates out of `pack/` until the pack-level decision records a client profile and the required hashes.
2. Run a human client pass with a real audio device for Sound Physics, Presence Footsteps, and AmbientSounds. Include entering a fresh world and walking across representative materials for Presence Footsteps.
3. Run an in-world visual and performance pass for ImmediatelyFast, Entity Culling, Sodium, and Particular. Use repeated samples rather than one startup, and inspect settings and ordinary play.
4. Open Mod Menu and check its settings screens, keybinds, and interaction with the selected recipe viewer and Jade.
5. Resolve how a fresh pack creates `config/presencefootsteps`, or document a supported setup step, before treating Presence Footsteps as a clean pilot.
6. Keep Sodium deferred until a non-alpha 26.2 release is available. Keep Sound Physics deferred until a non-beta release and working-audio evidence exist.
7. Do not add custom Java or a worldgen hook for any result in this spike.

The next backlog ticket is **LAB-12: Companion boundary and gap-analysis compatibility spike**, after integration of the already-completed LAB-11 reward review. It is not started by this change.

## Decision

- **Client-load decisions changed:** ImmediatelyFast, Entity Culling, Mod Menu, Particular Reforged, Presence Footsteps, and AmbientSounds move from display-blocked `NEEDS_MORE_EVIDENCE` to `PILOT`, with Presence Footsteps carrying the explicit configuration-directory condition.
- **Deferrals stand:** Sodium remains deferred because its exact 26.2 artifact is alpha. Sound Physics Remastered remains deferred because its exact artifact is beta and host audio is unavailable.
- **No core client baseline is approved by this spike.** A rendered main menu is evidence of loading, not evidence that a candidate works well enough for default inclusion.

## Evidence paths

- Client environment correction: `docs/testing/client-environment.md`
- Reusable smoke harness: `scripts/client-smoke.sh`
- Prior decisions: `labs/results/LAB-10-performance-structures-ambience.md`
- Exact candidate pins: `labs/manifests/LAB-10-performance-structures-ambience.toml`
- Disposable client console logs: `/tmp/lab10/client2/logs/*.console`
- Per-case smoke output: `/tmp/lab10/client2/logs/*.smoke` and `/tmp/lab10/client2/logs/*.run.out`
- Disposable client fixture: `/tmp/lab10/client2/project/`
- Required library API records: `/tmp/lab10/api/deps/`

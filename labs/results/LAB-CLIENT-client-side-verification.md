# LAB-CLIENT: client-side verification for LAB-01, LAB-02, LAB-03, and LAB-04

- Date: 2026-08-28
- Run by: Jcode, automated client follow-up
- Manifest: none. This follow-up consumes the fixtures in `labs/manifests/LAB-01-information-and-onboarding.toml`, `labs/manifests/LAB-02-pack-loader-dual-role.toml`, `labs/manifests/LAB-03-building-and-excavation.toml`, and `labs/manifests/LAB-04-blueprints-and-schematics.toml`.
- Status: complete for client startup and resource-role verification. World interaction, visual, audio, narration, and multiplayer checks remain human-required.

## Question

Do the adopted and pilot client-side candidates load on the pinned Minecraft 26.2 baseline with Matcha, and do Global Packs' missing and one-sided Matcha configurations fail open without blocking client startup?

## Exact versions

| Component | Project ID | Version ID | File |
|---|---|---|---|
| Minecraft | | | 26.2 |
| Fabric Loader | | | 0.19.3 |
| Fabric API | `P7dR8mSH` | `NqwNSxwA` | `fabric-api-0.158.0+26.2.jar` |
| Global Packs | `NRLPy2mk` | `DqrPrUMp` | `globalpacks-fabric-26.2-26.2.0.jar` |
| Matcha Flavoured | `QI0EmgZ1` | `E9rngRfK` | `Matcha_Flavoured_1_12.zip` |
| Roughly Enough Items | `nfn13YXA` | `4o0NSIMj` | `RoughlyEnoughItems-26.2.820.jar` |
| Jade | `nvQzSEkH` | `ue8CO97w` | `Jade-mc26.2-Fabric-26.2.11.jar` |
| Architectury API | `lhGA9TYQ` | `1yQC4VvP` | `architectury-fabric-21.0.7.jar` |
| Cloth Config API | `9s6osm5g` | `Nv3xnWXd` | `cloth-config-26.2.155.jar` |
| Effortless Building | `DYtfQEYj` | `BkGu1Eid` | `effortlessbuilding-fabric-26.2-4.3.jar` |
| Litematica | `bEpr0Arc` | `Fhq3KCI8` | `litematica-fabric-26.2-0.28.5.jar` |
| MaLiLib | `GcWjdA9I` | `ZCq1iE1J` | `malilib-fabric-26.2-0.29.4.jar` |

The pinned Matcha archive used in every valid baseline profile had SHA-256
`6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248`.
The copied altered fixture had SHA-256
`5d785bc2c452e43b8489291c563b1bbf8da7740124bbbb6b337427decf73585e`.
The candidate JARs were hashed in this run. Litematica and MaLiLib matched the hashes recorded in the LAB-04 manifest:

- Effortless Building: `5cf0700bf001553fb8e5e74fc23a8f4131569d608c40a82d426c2fe11c180d18`
- Litematica: `e711f69b0899ab3c4afafd49e7b825aa7d5051ee5358c88fef36023c612ae91a`
- MaLiLib: `a691f78959e19ac0e1224e5f3aec63c5fc705ba2b9b7c3a86367e569b95bf2c8`

## Test world

- Seed: not applicable. No client profile joined a world.
- World type and difficulty: not applicable.
- Game rules changed: none.
- Disposable client path: `/tmp/lab-client-20260828/project`, with one copied `run/` directory per case under `/tmp/lab-client-20260828/cases/`.
- Confirmed not a real survival world: yes.
- No save directory was created by the client-only runs.

## Client and server configuration

- Client tested: yes. The valid full-pack baseline, Matcha MISSING, altered-archive, data-only, Effortless Building, and Litematica profiles were each launched with Java 25, `DISPLAY=:0`, and a bounded `timeout`.
- Dedicated server tested: yes in the preceding LAB-01, LAB-02, LAB-03, and LAB-04 runs. It was not rerun in this client-only follow-up.
- Multiplayer tested: no players. No second client or network session was used.

The client launcher remains open by design. The corrected candidate commands used
`timeout --signal=TERM --kill-after=10s 35s ./gradlew --no-daemon runClient`. Exit 124
means the timeout stopped the client after startup evidence was collected. Each run used
a cleanup trap to terminate the matching disposable client process and restore the
baseline fixture. No client process for this project remained afterward.

An early set of attempts omitted `cd /tmp/lab-client-20260828/project` and therefore
loaded the repository root with 51 mods. Those logs are explicitly excluded. Every
result below comes from the corrected runs, which report 59 mods for the baseline and
include the adopted JARs in the Fabric mod list.

## Startup outcome

### Healthy full-pack baseline

The client loaded the adopted information stack and Matcha's resource role:

```text
/tmp/lab-client-20260828/project/baseline-client.log:23 [17:30:44] [main/INFO] (FabricLoader/GameProvider) Loading Minecraft 26.2 with Fabric Loader 0.19.3
/tmp/lab-client-20260828/project/baseline-client.log:25 [17:30:44] [main/INFO] (FabricLoader) Loading 59 mods:
/tmp/lab-client-20260828/project/baseline-client.log:113 [17:30:49] [Render thread/INFO] (REI) [REI] Registered plugin provider DefaultClientPlugin [roughlyenoughitems] for REIClientPlugin
/tmp/lab-client-20260828/project/baseline-client.log:120 [17:30:49] [Render thread/INFO] (Minecraft) Backend library: LWJGL version 3.4.1-snapshot
/tmp/lab-client-20260828/project/baseline-client.log:121 [17:30:49] [Render thread/INFO] (Minecraft) Using graphics backend OpenGL, using drivers: 4.5 (Core Profile) Mesa 25.2.8-0ubuntu0.24.04.2
/tmp/lab-client-20260828/project/baseline-client.log:158 [17:30:49] [Render thread/INFO] (Minecraft) Reloading ResourceManager: ... globalpacks, jade, roughlyenoughitems, Matcha_Flavoured_1_12.zip
/tmp/lab-client-20260828/project/baseline-client.log:442 [17:30:51] [Render thread/INFO] (Minecraft) Created: 2048x2048x4 minecraft:textures/atlas/blocks.png-atlas
```

Jade loaded its client plugins at lines 160, 162, 164, 166, and 168. No crash report,
Mixin application failure, or incompatible-mod-set marker occurred in the baseline log.
This closes the previously deferred client loading question for REI and Jade and proves
that Global Packs applies the single Matcha archive as a client resource pack.

### LAB-02 failure paths

| Case | Client evidence | Result |
|---|---|---|
| MISSING archive | `missing-client-correct.log:22` loaded 59 mods, `:153` reloaded the resource manager without `Matcha_Flavoured_1_12.zip`, and `:213` created the blocks atlas. No crash marker or Global Packs missing-pack diagnostic occurred. | Client startup was not blocked. The resource role was absent silently, matching LAB-02's server-side fail-open result. |
| Altered archive | `altered-client-correct.log:22` loaded 59 mods, `:155` reloaded the resource manager with `Matcha_Flavoured_1_12.zip`, and `:439` created the blocks atlas. The changed archive produced no checksum diagnostic and no crash marker. | Client startup was not blocked. The client accepted the altered resource archive, so integrity remains a server-side mitigation responsibility. |
| Data role only | `data-only-client-correct.log:22` loaded 59 mods, `:154` reloaded the resource manager without `Matcha_Flavoured_1_12.zip`, and `:214` created the blocks atlas. No crash marker occurred. | Client startup was not blocked. This is the quiet half-install predicted by LAB-02: the server can retain Matcha data while the client has vanilla resources. |

The data-only case still logged one `FileAlreadyExistsException` while Global Packs
handled the archive in the disposable fixture. The healthy and altered profiles logged
two of the same exceptions because the archive was deliberately placed at the configured
`datapacks/` path while also listed as a required source. This is a known fixture
artifact already present in the baseline setup, not a client crash. The MISSING profile
had no such exception because the archive was absent.

The client did not emit the MRH-010 `MISSING` or `DEGRADED` banner. That is expected from
the current design. `ForeverMatchaCompat.initialize()` registers detection on
`ServerLifecycleEvents.SERVER_STARTED` and `END_DATA_PACK_RELOAD`, while the client log
states that Matcha mappings remain inactive until server datapack signals prove identity.
The client evidence therefore closes the resource-role question, but not a connected
client presentation of the server's diagnostic. The existing server-side mitigation is
still required.

### LAB-01 adopted information tools

The healthy baseline loaded REI's common and client plugins and all observed Jade client
plugins without a candidate-specific crash. The same REI and Jade plugin lines occurred
in the altered, data-only, Effortless, and Litematica profiles. The client resource
reload completed in each profile.

The test host has two unrelated capabilities that remain unavailable:

- Narration logs `Error while loading the narrator` because `libflite.so` is absent.
- Audio logs `Error starting SoundSystem. Turning off sounds & music` because no OpenAL
device is available.

Screenshots of the window are black on this host. The pinned Matcha archive also produced
13 pre-existing missing-texture warnings in every profile, including examples for
`minecraft:item/vermeil_axe`, `minecraft:item/buttered_apple`, and
`minecraft:item/raw_curry`. These warnings originate in Matcha models whose referenced
textures are absent from the pinned archive. They are not REI or Jade failures, but they
mean the overall client resource reload is not warning-free.

### LAB-03 Effortless Building

```text
/tmp/lab-client-20260828/project/effortless-client-correct.log:22 [17:46:25] [main/INFO] (FabricLoader) Loading 60 mods:
/tmp/lab-client-20260828/project/effortless-client-correct.log:26     - effortlessbuilding 4.3
/tmp/lab-client-20260828/project/effortless-client-correct.log:100 [17:46:31] [Render thread/INFO] (Effortless Building) Hello Fabric world!
/tmp/lab-client-20260828/project/effortless-client-correct.log:159 [17:46:32] [Render thread/INFO] (Minecraft) Reloading ResourceManager: ... effortlessbuilding ... Matcha_Flavoured_1_12.zip
/tmp/lab-client-20260828/project/effortless-client-correct.log:443 [17:46:33] [Render thread/INFO] (Minecraft) Created: 2048x2048x4 minecraft:textures/atlas/blocks.png-atlas
```

Effortless Building loaded on the client and reached its own client initializer. Its
only candidate-specific startup messages were the normal development-environment
reference-map warnings. No candidate-specific crash or resource error was observed.
This closes the deferred client startup check, but not the assisted-placement behavior
check.

### LAB-04 Litematica and MaLiLib

```text
/tmp/lab-client-20260828/project/litematica-client-correct.log:22 [17:47:14] [main/INFO] (FabricLoader) Loading 62 mods:
/tmp/lab-client-20260828/project/litematica-client-correct.log:79     - litematica 0.28.5
/tmp/lab-client-20260828/project/litematica-client-correct.log:81     - malilib 0.29.4
/tmp/lab-client-20260828/project/litematica-client-correct.log:160 [17:47:19] [Render thread/INFO] (Minecraft) Reloading ResourceManager: ... litematica, malilib ... Matcha_Flavoured_1_12.zip
/tmp/lab-client-20260828/project/litematica-client-correct.log:164 [17:47:20] [Render thread/INFO] (litematica) Placement Manager calculated thread limit: [02/03]
/tmp/lab-client-20260828/project/litematica-client-correct.log:167 [17:47:20] [Render thread/INFO] (litematica) [DEBUG] FallbackBlockModels: initialized.
/tmp/lab-client-20260828/project/litematica-client-correct.log:169 [17:47:20] [Render thread/INFO] (malilib) i18nLang#load: File: '/assets/malilib/lang/en_us.json' has been loaded successfully
/tmp/lab-client-20260828/project/litematica-client-correct.log:361 [17:47:20] [Render thread/WARN] (litematica) getSchematicsBaseDirectory(): Created schematic directory '/tmp/lab-client-20260828/project/run/schematics'
/tmp/lab-client-20260828/project/litematica-client-correct.log:362 [17:47:20] [Render thread/ERROR] (malilib) loadFromFile(): Failed to load config file '/tmp/lab-client-20260828/project/run/config/malilib.json'
/tmp/lab-client-20260828/project/litematica-client-correct.log:453 [17:47:20] [Render thread/INFO] (Minecraft) Created: 2048x2048x4 minecraft:textures/atlas/blocks.png-atlas
```

Litematica and MaLiLib loaded and initialized. The missing `malilib.json` message is a
nonfatal first-run configuration error in the empty disposable instance, not a crash.
The run also created only client-local `config/litematica` and `schematics` directories.
Because this warning is candidate-specific, Litematica is not classified as a clean
warning-free startup. It remains a pilot pending a human first-run configuration and
preview check.

## Matcha interaction

The healthy client resource manager explicitly included `Matcha_Flavoured_1_12.zip`.
The MISSING and data-only profiles explicitly excluded it. The altered profile included
it even though its checksum differed from the lock. This confirms the Global Packs dual
role at the client resource boundary and confirms that the loader does not verify the
archive's identity on the client.

No client profile joined a server, so client-side recipe data, advancement data, and
Matcha gameplay behavior were not re-counted here. The earlier server evidence remains:
2346 recipes and 1805 advancements for the healthy Matcha baseline, and 1585 recipes for
the server MISSING case. LAB-05's `ADOPT NOTHING` food decision was not reopened. These
client startup runs did not exercise crops, cooking, hunger, healing, or food effects.

No startup log evidence showed REI, Jade, Effortless Building, Litematica, or MaLiLib replacing
or altering a Matcha recipe, item, tag, advancement, or pack filter. This is only a
load-boundary result, not a gameplay compatibility result.

## Multiplayer interaction

Not tested. There was no second client and no client-to-server session. Server authority,
REI synchronization, Jade server overrides, assisted placement authority, Litematica
preview locality, and desynchronization remain open functional checks.

## Persistence

No Minecraft world was opened, so no world, chunk, entity, player, route, or Matcha save
data was written. Global Packs used an instance configuration file. Litematica created
client-local configuration and schematic directories in the disposable run directory.
No candidate data crossed into a persistent world save in this follow-up.

## Removal behaviour

Removal from a world copy was not applicable because this was a title-screen startup
follow-up with no world. Each candidate was removed by restoring the disposable baseline
run directory after its timed launch. Litematica's client-local directories remained
only in the disposable case directory and were not part of the repository.

The preceding LAB-02 same-save and LAB-03/LAB-04 server checks remain the evidence for
server-side removal and save compatibility. A human client playtest should still check
whether deliberately retained client configuration is desirable between launches.

## Performance observations

The host is an AMD Ryzen 7 9700X with 19.5 GiB RAM. The client used Mesa 25.2.8
`llvmpipe` through OpenGL 4.5. The baseline and every candidate reached resource reload
and the 2048x2048x4 block atlas within roughly 5 to 7 seconds of the Minecraft load line.
No tick-time, memory, chunk-loading, or seasonal rendering profile was possible because
no world was entered. Litematica reported a placement-manager thread limit of `[02/03]`,
but this startup observation is not a performance acceptance test.

## Conflicts observed

- Global Packs emits no client diagnostic for a missing required Matcha archive or a
  missing resource-pack role. The client simply starts without the archive in its
  resource manager.
- The altered archive also starts without an integrity diagnostic.
- The healthy, altered, and candidate profiles emit the known same-path
  `FileAlreadyExistsException` fixture error from Global Packs.
- The pinned Matcha archive emits 13 missing-texture model warnings. These are baseline
  resource defects, not viewer or building-mod conflicts.
- Litematica and MaLiLib emit the nonfatal missing-first-run-config error quoted above.
- No client log evidence can establish advancement toast volume or advancement-screen
  noise. No world was joined, and screenshot capture is unusable on this host.
- Keybind overlap, REI recipe-panel behavior, Jade block/entity tooltip behavior,
  Litematica preview rendering, Effortless placement behavior, and audio/narration were
  not proven by logs alone.

## Does it fit the pack?

The client evidence does not change any existing decision.

- Global Packs remains `ADOPT_BASELINE` with the MRH-010 server-side integrity and
  baseline-health mitigation. The client confirms the happy-path resource role and also
  confirms the loader's fail-open MISSING and data-only behavior.
- REI and Jade remain `ADOPT`. Both load their client integrations alongside Matcha with
  no candidate-specific startup failure. Their actual panels and tooltips still need a
  human interaction pass.
- Effortless Building remains `PILOT`. It is a clean 26.2 client load and fits assisted
  placement, but ordinary placement, bounded operation limits, survival behavior, and
  server authority were not exercised here.
- Litematica and MaLiLib remain `PILOT`. Both load on 26.2 and initialize the preview
  path, but a fresh instance logs a nonfatal missing configuration file. Ghost Plan
  preview, material lists, configuration persistence, and the no-printer boundary need
  a human check.

The client tests do not justify adding, removing, or re-pinning anything in `pack/`.

## Follow-up required

1. Run a human client session against a disposable Matcha server. Open REI, search for a
   Matcha recipe, inspect a block and entity with Jade, and check the advancement screen
   and first-join toast behavior. This is the remaining LAB-01 client interaction and
   advancement-noise check.
2. In that session, confirm whether an operator can see the server-side MRH-010 baseline
   warning when the archive is missing or one role is omitted. The title-screen-only
   client cannot invoke the server lifecycle detector.
3. Exercise Effortless Building in survival and creative modes with ordinary vanilla
   placement available. Check arrays, mirrors, operation bounds, keybind overlap, and a
   server-authoritative multiplayer case.
4. Exercise Litematica with a disposable `.litematic` file. Verify Ghost Plan preview,
   block-level material lists, config creation and reload, no printer behavior, and
   client-only state. Do not add the printer addon.
5. Record the pinned Matcha missing-texture warnings as a separate asset or upstream
   compatibility follow-up. Do not mask them by treating the baseline as warning-free.
6. Do not reopen LAB-05 food ownership based on these startup-only observations.

## Decision

| Candidate or path | Decision after this follow-up | Reason |
|---|---|---|
| Global Packs `DqrPrUMp` | `ADOPT_BASELINE` unchanged | Client resource role is active in the healthy profile. MISSING, altered, and data-only cases fail open without blocking startup, so MRH-010 remains necessary. |
| Matcha `E9rngRfK` | Baseline unchanged | Client resource role loads, with 13 pre-existing missing-texture warnings in the pinned archive. |
| Roughly Enough Items `4o0NSIMj` | `ADOPT` unchanged | Client plugin registration succeeds alongside Matcha. UI and recipe interaction remain human checks. |
| Jade `ue8CO97w` | `ADOPT` unchanged | Client plugins load alongside Matcha. Tooltip and advancement-noise interaction remain human checks. |
| Effortless Building `BkGu1Eid` | `PILOT` unchanged | 26.2 client initializer and resource reload succeed with no candidate-specific error. Functional building and authority remain untested. |
| Litematica `Fhq3KCI8` plus MaLiLib `ZCq1iE1J` | `PILOT` unchanged | Both initialize the 26.2 client preview path. A nonfatal missing first-run `malilib.json` warning and all functional preview checks remain open. |

## Deferred-item closure

Closed by this result:

- The assumed no-client environment blocker is disproven for this host.
- LAB-02's healthy Global Packs resource role is verified on the client.
- LAB-02 MISSING and data-only fail-open behavior is verified on the client. Neither
  blocks startup, and neither produces a client-side baseline diagnostic.
- LAB-01 REI and Jade client loading alongside Matcha is verified.
- LAB-03 Effortless Building client startup on 26.2 is verified.
- LAB-04 Litematica and MaLiLib client startup on 26.2 is verified, with the documented
  first-run configuration warning.

Still human-required:

- REI panel and Matcha recipe interaction
- Jade block/entity tooltip interaction
- Advancement screen, toast volume, and advancement-noise judgment
- Effortless placement operations and multiplayer authority
- Litematica schematic preview, material list, config persistence, and no-printer check
- Visual appearance, screenshots, audio, narration, and two-player multiplayer

No decision changed, no pack metadata changed, and no new dependency was adopted by this
follow-up.

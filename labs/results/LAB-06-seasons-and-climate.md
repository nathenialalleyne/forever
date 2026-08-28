# LAB-06: Seasons and climate compatibility spike

- Date: 2026-08-28
- Run by: Jcode, automated
- Manifest: `labs/manifests/LAB-06-seasons-and-climate.toml`
- Status: complete for Modrinth verification, dedicated-server startup, provider queries, persistence inspection, and same-save removal checks. Client, multiplayer, crop-growth, and greenhouse interaction remain unverified because this host has no usable display or connected client.

## Question

Can a maintained 26.2 seasons or climate provider coexist with the clean Matcha Flavoured 1.12 environment, and should the pack depend on one directly or consume it through the climate abstraction required by ADR 0014?

## Conclusion

**ADR 0014 still holds. The pack should consume a provider-neutral climate abstraction and must not depend directly on a seasons mod.** A maintained provider can boot beside Matcha, but the provider's crop, weather, saved-calendar, client-sync, and removal behavior is not a stable pack contract.

Homeostatic Seasons is the best follow-up pilot target behind the abstraction. Its 26.2 Fabric build is a release under MIT, it boots with Matcha without changing the recipe or advancement counts, it exposes a calendar and transition query, and it does not require the separate Homeostatic body-temperature or thirst mod. It does not expose a crop or greenhouse contract in the inspected artifact, so it is not ready for direct pack adoption.

Serene Seasons has a real 26.2 Fabric artifact despite the `26.1.2.0.4` version label in the research matrix and artifact name. It is beta, All Rights Reserved, requires beta GlitchCore, and its custom saved game rule produces an unknown-registry error when the provider is removed from a save. Defer it.

Ecliptic Seasons: Fabricated has a 26.2 release and a rich public climate and crop API, but its own project page calls 26.2 “In Development.” It enables seasonal crop and humidity restrictions, has a large advancement and tag surface, writes calendar data in every dimension, and has a Modrinth All Rights Reserved record with additional mixed license text in the jar. Defer it.

No candidate was added to `pack/`.

## Exact versions

| Component | Project ID | Version ID | File | API release type |
|---|---|---|---|---|
| Minecraft | | | 26.2 | |
| Fabric Loader | | | 0.19.3 | |
| Fabric API | `P7dR8mSH` | `NqwNSxwA` | `fabric-api-0.158.0+26.2.jar` | |
| Global Packs | `NRLPy2mk` | `DqrPrUMp` | `globalpacks-fabric-26.2-26.2.0.jar` | |
| Matcha Flavoured | `QI0EmgZ1` | `E9rngRfK` | `Matcha_Flavoured_1_12.zip` | |
| Roughly Enough Items | `nfn13YXA` | `4o0NSIMj` | `RoughlyEnoughItems-26.2.820.jar` | |
| Jade | `nvQzSEkH` | `ue8CO97w` | `Jade-mc26.2-Fabric-26.2.11.jar` | |
| Architectury API | `lhGA9TYQ` | `1yQC4VvP` | `architectury-fabric-21.0.7.jar` | |
| Cloth Config API | `9s6osm5g` | `Nv3xnWXd` | `cloth-config-26.2.155.jar` | |
| Serene Seasons | `e0bNACJD` | `13sXhUkI` | `SereneSeasons-fabric-26.2-26.1.2.0.4.jar` | beta |
| GlitchCore | `s3dmwKy5` | `SDUCBYRU` | `GlitchCore-fabric-26.2-26.2.0.0.0.jar` | beta |
| Homeostatic Seasons | `j3xSC4PA` | `1bSif4Rz` | `homeostaticseasons-26.2-fabric-1.2.0.3.jar` | release |
| Ecliptic Seasons: Fabricated | `WXA03JUt` | `NTGuEhvF` | `EclipticSeasons-Fabricated-26.2-fabric-0.14.5.jar` | release |
| Forge Config API Port | `ohNO6lps` | `rSd3GiG8` | `ForgeConfigAPIPort-v26.2.1-mc26.2.x-Fabric.jar` | release |

The Matcha archive SHA-256 was checked locally as `6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248`, matching the shared baseline. Candidate SHA-512 values and the exact CDN identities are recorded in the manifest.

Modrinth API verification used the project and version endpoints, including the exact 26.2 Fabric filter:

```text
https://api.modrinth.com/v2/project/<project-id>/version?game_versions=["26.2"]&loaders=["fabric"]
```

Serene Seasons returned one matching version, `13sXhUkI`. Its Modrinth `game_versions` contains `26.2`, its file is named with `26.2`, and its Fabric metadata requires Minecraft `26.2`. The `26.1.2.0.4` string is the provider version, not evidence that the file targets only Minecraft 26.1.2. The release is nevertheless beta.

The search terms `season`, `seasons`, `climate`, `weather`, `temperature`, and `crop` were also queried with Modrinth facets `project_type=mod`, `categories=fabric`, and `versions=26.2`. The full seasonal providers found were Serene Seasons, Homeostatic Seasons, and Ecliptic Seasons: Fabricated. Other relevant results were utility or partial providers: Seasonal Visuals (`xhy3SoOP`) is visual-only, SeasonHud-Fabric (`iDiIfZLX`) is an overlay, and Natural Temperature (`QZt2fevq`) changes world-generation climate zones rather than providing a calendar. Fabric Seasons (`KJe6y9Eu`) returned no 26.2 Fabric version and was not tested.

The same search surfaced partial candidates that were intentionally not booted. Crops Love Rain (`cRci7UZp`, version `YzNJMQVQ`) only accelerates crops during rain and has no calendar or local climate contract. Tough As Nails (`ge1sOdFH`, version `bZ9oibj4`) provides body temperature and thirst, overlaps Matcha's food and healing model, and is beta. Homeostatic (`oDobngty`, version `IQrivzqu`) is the optional body-temperature and thirst companion for Homeostatic Seasons and was not installed for the same Matcha-ownership reason.

## Test world

- Seeds:
  - baseline: `LAB06-20260828-baseline`
  - Serene Seasons: `LAB06-20260828-serene`
  - Homeostatic Seasons: `LAB06-20260828-homeostatic`
  - Ecliptic Seasons: `LAB06-20260828-ecliptic`
- World type: default `minecraft:normal` overworld with Nether and End
- Difficulty: easy
- Game mode: survival
- View distance: 8
- Simulation distance: 6
- Game rules changed: no manual changes. Matcha's global setup applied its configured rules, including `natural_health_regeneration false`, `advance_time false`, `spawn_phantoms false`, and `keep_inventory true`.
- Disposable world paths:
  - `/tmp/lab06/instances/baseline/world-retry`
  - `/tmp/lab06/instances/serene/world-serene-retry`
  - `/tmp/lab06/instances/homeostatic/world-homeostatic`
  - `/tmp/lab06/instances/ecliptic/world-ecliptic`
- Removal copies:
  - `/tmp/lab06/instances/serene-removal/world-serene-retry`
  - `/tmp/lab06/instances/homeostatic-removal/world-homeostatic`
  - `/tmp/lab06/instances/ecliptic-removal/world-ecliptic`
- Confirmed not real survival worlds: yes. Every world and removal copy was under `/tmp/lab06/`.

An initial harness attempt used a port shared with another lab and was discarded. The evidence below comes from corrected isolated ports 25565, 25706, 25707, and 25708, with removal checks on 25716, 25717, and 25718.

## Client and server configuration

- Client tested: no. The host has no usable display. Client tooltips, crop icons, calendar screens, foliage colors, seasonal snow overlays, and stale-client projections were not claimed.
- Dedicated server tested: yes. The clean baseline, all three provider suites, all three same-save removal copies, provider restarts, and read-only provider command queries reached the server ready line.
- Multiplayer tested: no. No client connected to these disposable servers.
- Recipe viewer: REI remained the only installed viewer. No optional second viewer or guidebook dependency was added.
- Java: 25, using `~/toolchains/jdk-25.0.4.1+1`.
- Hardware: AMD Ryzen 7 9700X 8-core CPU and 19.5 GiB memory.

## Startup outcome

The clean Matcha baseline reached the ready line:

```text
[16:36:51] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[16:36:51] [main/INFO]: Loading 49 mods:
[16:36:54] [main/INFO]: Found new data pack Matcha_Flavoured_1_12.zip, loading it automatically
[16:36:56] [main/INFO]: Loaded 2346 recipes
[16:36:56] [main/INFO]: Loaded 1805 advancements
[16:36:58] [Server thread/INFO]: Done (1.568s)! For help, type "help"
```

Serene Seasons plus GlitchCore reached the ready line on Minecraft 26.2:

```text
[16:41:50] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[16:41:50] [main/INFO]: Loading 53 mods:
        - glitchcore 26.2.0.0.0
        - sereneseasons 26.1.2.0.4
[16:41:54] [main/INFO]: Registered synced config with path sereneseasons/fertility.toml
[16:41:54] [main/INFO]: Registered synced config with path sereneseasons/seasons.toml
[16:41:58] [Server thread/INFO]: Done (1.709s)! For help, type "help"
```

The corrected Serene run emitted missing development reference-map warnings for GlitchCore and Serene Seasons. It did not crash. The provider also registered both synced configuration paths.

Homeostatic Seasons reached the ready line with its bundled dependencies:

```text
[16:46:08] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[16:46:08] [main/INFO]: Loading 52 mods:
        - homeostaticseasons 1.2.0.3
           |-- climatesettings 1.2.1
           \-- whitenoise 2.2.1
[16:46:14] [main/INFO]: Loaded 2346 recipes
[16:46:14] [main/INFO]: Loaded 1805 advancements
[16:46:15] [Server thread/INFO]: Done (1.418s)! For help, type "help"
```

The only provider-specific startup warning was the missing `climatesettings.refmap.json` development reference map. The provider did not crash or require the separate Homeostatic temperature and thirst mod.

Ecliptic Seasons: Fabricated reached the ready line with Forge Config API Port:

```text
[16:46:38] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[16:46:38] [main/INFO]: Loading 51 mods:
        - eclipticseasons 26.2-fabric-0.14.5
        - forgeconfigapiport 26.2.1
[16:46:42] [main/WARN]: Configuration file ./config/eclipticseasons-common.toml is not correct. Correcting
[16:46:45] [main/INFO]: Loaded 2350 recipes
[16:46:45] [main/INFO]: Loaded 1838 advancements
[16:46:47] [Server thread/INFO]: Done (2.317s)! For help, type "help"
```

Ecliptic emitted a missing `eclipticseasons.refmap.json` warning and Forge Config API Port recommended Mod Menu, which was intentionally absent from the server lab. It also logged its enabled resource integrations:

```text
[16:46:42] [main/INFO]: eclipticseasons:rain_together
[16:46:42] [main/INFO]: eclipticseasons:vanilla_biome_climate_settings
[16:46:42] [main/INFO]: eclipticseasons:regional_snow_time
```

The following errors occurred in the baseline and candidate runs alike, so they are treated as pre-existing Matcha or shared Global Packs lab noise rather than provider failures:

```text
[16:36:54] [main/ERROR]: java.nio.file.FileAlreadyExistsException: ./datapacks/Matcha_Flavoured_1_12.zip
[16:36:56] [Worker-Main-5/ERROR]: Couldn't parse data file 'main:mechanics/wither_test' from 'main:advancement/mechanics/wither_test.json': Advancement completion requirements did not exactly match specified criteria. Missing: [summoned_wither]. Unknown: [eat_glow_jam]
```

All corrected provider and removal runs reached `Done (...)`. No candidate produced a dedicated-server client-class crash.

## Matcha interaction

Matcha owns the food and cooking space described by LAB-05. It controls hunger, healing, food effects, cooking recipes, and a subset of preservation. Its archive contains 128 food recipes and 15 preservation recipes, and it disables natural health regeneration. LAB-05 found no dedicated Matcha crop-climate tag or crop suitability model.

The recipe and advancement counts were compared with the clean Matcha baseline:

| Suite | Recipes | Advancements | Delta from baseline |
|---|---:|---:|---:|
| Clean Matcha baseline | 2346 | 1805 | 0 / 0 |
| Serene Seasons | 2348 | 1807 | +2 / +2 |
| Homeostatic Seasons | 2346 | 1805 | 0 / 0 |
| Ecliptic Seasons: Fabricated | 2350 | 1838 | +4 / +33 |

No candidate added a second cooking system or changed Matcha's food recipe count in a way that caused a load failure. Homeostatic Seasons is the cleanest Matcha fit because it adds no recipe or advancement delta in this setup and does not require a player thirst or temperature module.

Serene Seasons contains calendar and season-sensor recipes, crop season tags, a greenhouse-glass tag, a calendar item, and a season sensor. It defaults to `seasonal_crops = true`, `out_of_season_crop_behavior = 0` where out-of-season crops grow slowly, and `crop_tooltips = true`. This is a direct interaction with the ingredients Matcha expects players to cultivate, even though no food recipe conflict appeared during idle startup.

Ecliptic Seasons contains calendar, growth-detector, hygrometer, and season-sensor recipes, 43 advancement resources, and a large crop-tag surface. Its default configuration enables seasonal crop restriction, humidity control, bone-meal restriction, and `ForceCompatMode = true`. Its advancement delta is a potential source of advancement noise. It also has a `summer_eat_glistering_melon_slice` advancement resource, but no Matcha recipe collision or parse error beyond the baseline Matcha errors was observed.

Homeostatic Seasons' inspected jar contains no crop tag or crop-suitability API and its embedded documentation says it adds no new blocks or items. Crop behavior still requires gameplay testing before it can be used as the pack's crop provider or assumed to be neutral for every modded plant.

No second guidebook or starter item was observed in the server artifact inventories. Serene and Ecliptic provide their own calendar or diagnostic content, so the eventual Field Guide should explain the selected climate provider without exposing a second competing guide system. Client tooltips and icons were not tested.

## Dedicated-server state queries

The provider APIs and command surfaces were inspected without importing third-party types into Forever.

Serene's read-only `season get` command produced:

```text
[16:51:43] [Server thread/INFO]: Current season is Early Spring, day 1/8, tick 71/24000
```

Homeostatic's read-only `season query` command produced both current state and a transition estimate:

```text
[16:51:53] [Server thread/INFO]: The current season is Early Spring.
[16:51:53] [Server thread/INFO]: There are 2.7 days (65929 ticks) left until Mid Spring.
```

Ecliptic's read-only calendar commands produced:

```text
[16:52:45] [Server thread/INFO]: Spring Equinox
[16:52:46] [Server thread/INFO]: Spring
[16:52:47] [Server thread/INFO]: Mid Spring
```

This demonstrates server-side current-season inspection and, for Homeostatic, a forecast-like next-transition estimate. It does not demonstrate a player-facing forecast icon, a local temperature display, crop suitability text, or mitigation UI.

The public APIs have different shapes:

- Serene exposes an `ISeasonState` with season, sub-season, day, cycle ticks, and durations. Its crop behavior is primarily expressed through tags and its configured seasonal growth handler.
- Homeostatic exposes current season, time until a season, time until the next season, precipitation, freeze and melt checks, and a biome temperature calculation. No crop suitability or greenhouse API was found in the inspected release.
- Ecliptic exposes season, sub-season, solar term, day-in-term, local rain or snow, humidity, temperature-related climate data, and crop growth and greenhouse data. These APIs use Ecliptic classes and must not become the pack contract.

## Multiplayer interaction

No multiplayer test was possible because no client could connect to the headless lab servers. The artifacts and logs indicate that all three providers are both-side mods and that they have server-to-client state paths, but synchronization, stale-client handling, per-player display, and permission behavior remain unverified.

The server must remain authoritative. A future adapter must query and normalize state on the server, then send a bounded, versioned projection to clients. A client must never decide crop viability, greenhouse eligibility, season changes, or weather outcomes.

## Persistence

All three candidate suites were booted on fresh disposable worlds and then restarted on the same saved worlds.

### Serene Seasons

Serene created synced configuration files:

```text
config/sereneseasons/fertility.toml
config/sereneseasons/seasons.toml
```

It wrote a calendar saved-data file in every tested dimension:

```text
dimensions/minecraft/overworld/data/sereneseasons/seasons.dat
dimensions/minecraft/the_nether/data/sereneseasons/seasons.dat
dimensions/minecraft/the_end/data/sereneseasons/seasons.dat
```

The jar exposes `SeasonSavedData` and a codec/version field. The provider also registers the custom game rule `sereneseasons:do_season_cycle`. A restart with the provider installed loaded the saved world and returned the same current season query. This is real persistent state, not only a client display.

No direct terrain-generation, structure, or dimension resource was found in the inspected jar; Serene does contain biome-tag resources. A full terrain comparison was not performed.

### Homeostatic Seasons

The idle boot created `config/homeostaticseasons-common.toml`. No provider-specific world data file appeared in the fresh world after the short boot and restart. The jar does contain `PlacedMeltablesSavedData`, `ReplacedMeltablesSavedData`, and weather-frequency state classes. Because the test did not create seasonal snow replacements or meltable-block changes, those save paths were not exercised.

The embedded documentation describes chunk-based updates limited to loaded, visible chunks and per-block temperature calculation. That is promising for bounded work, but it is not a substitute for a real loaded-chunk performance test.

### Ecliptic Seasons: Fabricated

Ecliptic created several configuration files, including `eclipticseasons-common.toml`, `eclipticseasons-client.toml`, `eclipticseasons-mixins.toml`, and `eclipticseasons-startup.toml`. It wrote a solar calendar saved-data file in all three dimensions:

```text
dimensions/minecraft/overworld/data/eclipticseasons/solar_manager.dat
dimensions/minecraft/the_nether/data/eclipticseasons/solar_manager.dat
dimensions/minecraft/the_end/data/eclipticseasons/solar_manager.dat
```

The default configuration also contains `SaveChunkEnvironmentalHumidity = true`. The project documentation says some chunk data storage was removed in this rewrite, but the artifact exposes chunk and climate API types and the save path above. A migration must therefore treat both calendar data and possible chunk attachments as provider-owned state.

No direct terrain-generation, structure, or dimension resource was found in the inspected jar; Ecliptic does contain biome-tag and resource-pack climate data, although the project is categorized as worldgen and the provider changes biome climate behavior. A terrain and new-chunk comparison was not performed.

## Removal behaviour

Each provider was removed from a copy of the provider-created world. The remaining jars were the clean Matcha baseline and the already selected REI, Jade, Architectury, and Cloth Config support set.

### Serene Seasons removal

The server reached the ready line and returned baseline content counts:

```text
[16:47:33] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[16:47:33] [main/INFO]: Loading 49 mods:
[16:47:38] [main/INFO]: Loaded 2346 recipes
[16:47:38] [main/INFO]: Loaded 1805 advancements
[16:47:38] [Server thread/INFO]: Done (0.142s)! For help, type "help"
```

It also logged a provider-state failure:

```text
[16:47:38] [main/ERROR]: Failed to parse saved data for 'SavedDataType[minecraft:game_rules]': Unknown registry key in ResourceKey[minecraft:root / minecraft:game_rule]: sereneseasons:do_season_cycle; Unknown registry key in ResourceKey[minecraft:root / minecraft:game_rule]: sereneseasons:do_season_cycle missed input: {"sereneseasons:do_season_cycle":1b}
```

The stale `config/sereneseasons/` files and `seasons.dat` files remained on disk. Serene removal is therefore boot-tolerant for this disposable world but not migration-safe. Removing or replacing it requires handling the custom game rule and calendar data explicitly.

### Homeostatic Seasons removal

The server reached the ready line with baseline content counts and no provider-specific saved-data parse error:

```text
[16:47:44] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[16:47:44] [main/INFO]: Loading 49 mods:
[16:47:51] [main/INFO]: Loaded 2346 recipes
[16:47:51] [main/INFO]: Loaded 1805 advancements
[16:47:51] [Server thread/INFO]: Done (0.151s)! For help, type "help"
```

The idle save had not created snow-replacement records, so this only establishes removal behavior for an otherwise untouched provider save. A save containing modified meltable blocks or weather records still needs a migration test.

### Ecliptic Seasons: Fabricated removal

The server reached the ready line with baseline content counts and no immediate saved-data parse error:

```text
[16:47:58] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[16:47:58] [main/INFO]: Loading 49 mods:
[16:48:03] [main/INFO]: Loaded 2346 recipes
[16:48:03] [main/INFO]: Loaded 1805 advancements
[16:48:03] [Server thread/INFO]: Done (0.158s)! For help, type "help"
```

The copied world still contains `eclipticseasons/solar_manager.dat` in the overworld, Nether, and End, along with the provider configuration files. This is not data loss during the boot, but it is orphaned provider state with no demonstrated migration or cleanup. Removal is boot-tolerant for this fresh world, not proven migration-safe.

## Performance observations

These are startup observations only, not a benchmark. The server ran on the AMD Ryzen 7 9700X host with Java 25:

| Suite | First observed `Done` time | Restart observation |
|---|---:|---:|
| Clean Matcha baseline | 1.568 s | 0.142 to 0.158 s in removal copies |
| Serene Seasons | 1.709 s | 0.163 to 0.268 s |
| Homeostatic Seasons | 1.418 s | 0.159 to 0.190 s |
| Ecliptic Seasons: Fabricated | 2.317 s | 0.178 to 0.220 s |

The times include world preparation and cache state, so they must not be used as a provider ranking. Ecliptic logged one useful server-side observation:

```text
[16:52:04] [Server thread/INFO]: Reload crop data cost 8 ms in server side.
```

Relevant bounded-work risks remain:

- Serene's generated config documents random melt rolls per chunk each tick and warns that high roll counts are not recommended for servers. Its default season entries include `melt_rolls = 1` for several seasons.
- Homeostatic's documentation claims visible-loaded-chunk snow updates and per-block temperature. No tick profiler or multi-chunk measurement was run.
- Ecliptic defaults to crop humidity and seasonal checks, saves chunk environmental humidity, and enables client `ForceChunkRenderUpdate`. Its server crop reload was small in this empty-world observation, but farm-scale and client render costs were not measured.
- No client rendering benchmark, memory profile, tick-time profile, loaded-chunk sweep, or farm scan was run.

The abstraction must require on-demand or cached local queries and must not make a provider's per-chunk or per-crop work part of a permanent global tick loop.

## Conflicts observed

- No candidate caused a Matcha food recipe or hunger-controller load failure.
- Homeostatic Seasons preserved the baseline recipe and advancement counts and added no observed new blocks or items.
- Serene added two recipes and two advancements, plus calendar and season-sensor content. Its seasonal crop tags overlap the pack's regional crop cultivation capability and can indirectly affect Matcha ingredient acquisition.
- Ecliptic added four recipes and 33 advancements, plus calendar, crop diagnostics, humidity, and seasonal crop-tag content. This is a larger UI, recipe-viewer, and advancement surface than the climate abstraction needs.
- REI remained the only installed recipe viewer. No duplicate viewer was introduced.
- No client keybind, tooltip, calendar screen, icon, or stale projection was tested.
- All candidate suites produced some missing reference-map warnings. They did not prevent dedicated-server startup.
- Serene and GlitchCore are All Rights Reserved in Modrinth metadata. Ecliptic's Modrinth project record is All Rights Reserved, while its jar contains additional code/resource license text. Any future pack adoption requires a separate licensing review.

## Does it fit the pack?

The provider-neutral abstraction decision fits the pack better than any direct dependency.

### Required abstraction surface

The pack-facing port should use Forever records and enums, never Serene, Homeostatic, Ecliptic, GlitchCore, ClimateSettings, WhiteNoise, or Forge Config API objects. The minimum server-side query surface is:

| Stable concept | Required result |
|---|---|
| Current season | A normalized season and optional phase such as early, middle, or late, with `Unknown` when unavailable. |
| Day of season | A bounded day index, total duration, and time to next phase or season when the provider supplies it. |
| Forecast band | A short, explainable next-transition or weather band with availability and reason. It must not pretend to know provider-specific random weather when the provider cannot expose that information. |
| Local profile | Normalized temperature band, humidity or moisture band, precipitation kind, and current weather at a bounded position query. |
| Crop suitability | A described result such as preferred, possible, unsuitable, neutral, or unknown, including a reason, confidence, and the crop identity used for the query. It must not be a bare boolean or an invented rule for an unknown crop. |
| Mitigation | Described capabilities and requirements such as greenhouse, shelter, irrigation, storage, import, or trade. Do not return a provider greenhouse object. |
| Availability | Available, unavailable, or incompatible, with a stable reason and provider/version diagnostics kept out of player-facing logic. |

The data and configuration layer should own crop mappings, thresholds, phase lengths, effect strengths, and mitigation rules. The adapter may translate a provider's season tags, temperature values, humidity values, or greenhouse checks into the normalized records. The core must preserve the ordinary vanilla path when no mapping is present.

### Safe fallback

When a provider is absent, incompatible, or fails a query:

- current season and forecast are unavailable rather than guessed;
- the local profile is neutral or vanilla-derived with an explicit confidence state;
- unknown crops remain neutral and ordinary crop growth is not blocked or silently slowed;
- no provider crop, weather, temperature, or food effect is applied;
- ordinary vanilla food, travel, building, and farming remain functional;
- the Field Guide can explain that climate information is unavailable and list ordinary alternatives.

This directly preserves the Matcha findings from LAB-05. Matcha food, hunger, healing, and preservation remain authoritative. Seasons may influence the availability of ingredients only through an accepted crop-climate contract, not by adding a second food system or by silently changing Matcha food effects.

### Authority, persistence, and removal

The server owns the calendar, local profile, crop result, greenhouse or mitigation result, and seasonal effects. Client packets must be versioned and bounded. Client screens and tooltips render synchronized projections and show text plus icons, not color alone.

The Forever calendar record is a new persistent-data structure and must begin with a schema version, codec or serializer, validation, migration, and visible failure behavior. Provider saved data must not be copied into that record. A provider replacement should translate only the normalized calendar state, preserve original data where recovery is possible, and fall back to neutral state when translation is unavailable.

The Serene removal result proves why this matters. A custom provider game rule can survive as an unknown registry key after the jar is removed. Ecliptic leaves multi-dimension calendar records behind. Homeostatic may create saved meltable state after actual weather interaction. Provider selection cannot be treated as a cosmetic mod toggle.

### Escalation result

No custom calendar, crop-kill system, or provider-specific Java was written. The first implementation step remains configuration or datapack mapping where possible, followed by a narrow optional adapter under `dev.forever.compat.*`. If a capability cannot be expressed through existing configuration, datapack data, or a supported public API, document the gap under ADR 0033 before writing code.

## Follow-up required

1. Keep ADR 0014 as the governing decision. Do not add a direct Serene Seasons, Homeostatic Seasons, or Ecliptic Seasons dependency to the primary pack in this lab.
2. Use Homeostatic Seasons as the next adapter pilot target only after a focused compatibility ticket covers crop suitability, unknown crops, greenhouse or mitigation behavior, and the client projection. Do not install the separate Homeostatic body-temperature or thirst mod because LAB-05 established Matcha ownership of food, hunger, healing, and food effects.
3. Build disposable-world tests that set and restart the calendar, cross a season boundary, query forecast state, inspect an unknown crop, validate a bounded greenhouse or mitigation, and remove the provider after saved seasonal snow or crop state exists.
4. Test a real client with a server and two players when a display-capable environment is available. Verify text, icons, recipe-viewer explanations, server authority, and stale-client handling.
5. Recheck the provider's license and Modrinth status before any redistribution. Serene and GlitchCore are All Rights Reserved, and Ecliptic's metadata and embedded license text need reconciliation.
6. The next backlog ticket is **LAB-08: Local item transport and automation compatibility spike**. The climate follow-up above is required before climate adoption but was not started in this change.

## Decision

- **Homeostatic Seasons 1bSif4Rz: PILOT.** Best 26.2 release candidate for a provider-neutral adapter because it boots cleanly beside Matcha, leaves the recipe and advancement baseline unchanged, and supplies calendar, transition, weather, and temperature APIs. Crop and greenhouse behavior still needs evidence.
- **Serene Seasons 13sXhUkI: DEFER.** Real 26.2 artifact, but beta, All Rights Reserved, beta GlitchCore dependency, seasonal crop overlap, and an unknown custom game rule on removal.
- **Ecliptic Seasons: Fabricated NTGuEhvF: DEFER.** Boots on 26.2 and has a rich API, but 26.2 is documented as in development, defaults to broad crop and humidity changes, adds advancement noise, persists multi-dimension state, and has unresolved license metadata.
- **Fabric Seasons KJe6y9Eu: DEFER.** No exact 26.2 Fabric version exists in the Modrinth API response, so no incompatible build was tested.
- **Crops Love Rain cRci7UZp: REJECT for the climate-provider role.** Its 26.2 search result only accelerates crop growth during rain and supplies no calendar, local profile, forecast, or mitigation contract.
- **Tough As Nails bZ9oibj4: DEFER.** It is a beta body-temperature and thirst system that overlaps Matcha's food, hunger, healing, and food-effect ownership rather than providing a seasons contract.
- **Primary pack decision: PILOT behind the climate abstraction only.** No candidate was added directly or indirectly to `pack/` by LAB-06.


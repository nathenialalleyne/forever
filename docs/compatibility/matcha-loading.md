# Loading official Matcha in the modpack

Many Roads Home uses the official Matcha Flavoured release as its gameplay starting
point. This document records how that archive reaches every newly created world, which
utility was selected, what was actually verified, and how to undo the choice.

## Selected approach

**Global Packs**, Fabric build `26.2.0`, loading the official Matcha archive from the
pack's `datapacks/` folder as both a required datapack and a required resource pack.

The choice was close to forced rather than preferred. Of the three named candidates,
only Global Packs has a Minecraft 26.2 release at all.

| Candidate | Newest game version observed | Licence | Verdict |
|---|---|---|---|
| Global Packs (`NRLPy2mk`) | `26.2` | LicenseRef-All-Rights-Reserved | Selected |
| Paxi (`CU0PAyzb`) | `26.1.2` | LGPL-3.0-only | Unusable on 26.2 today |
| Open Loader (`KwWsINvD`) | `1.21.1` | LGPL-2.1-only | Far behind, unusable |

Sources: `https://api.modrinth.com/v2/project/NRLPy2mk/version?game_versions=%5B%2226.2%22%5D&loaders=%5B%22fabric%22%5D`,
`https://api.modrinth.com/v2/project/paxi`, `https://api.modrinth.com/v2/project/open-loader`.

The licence is a genuine drawback and is recorded rather than hidden. Global Packs is
All Rights Reserved, so it can be referenced for download but not repackaged or
patched, and there is no source to fork if it stops being maintained. Paxi is LGPL and
would be the preferable long-term dependency on licence grounds alone. If Paxi
publishes a 26.2 build, reassess this decision rather than treating it as settled.

## Exact versions

| Component | Value |
|---|---|
| Minecraft | `26.2` |
| Fabric Loader | `0.19.3` (latest `stable: true` in `meta.fabricmc.net/v2/versions/loader`) |
| Fabric API | `0.158.0+26.2`, version `NqwNSxwA` |
| Global Packs | `26.2.0`, version `DqrPrUMp`, file `globalpacks-fabric-26.2-26.2.0.jar` |
| Matcha Flavoured | `1.12`, version `E9rngRfK`, file `Matcha_Flavoured_1_12.zip` |
| Matcha SHA-256 | `6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248` |

The Matcha SHA-256 was recomputed from a fresh download and matches `matcha.lock.json`,
so the pinned identity in this repository and the file the pack installs are the same
artifact.

Global Packs declares `minecraft >=26.1` and `java >=25` in its `fabric.mod.json`,
which is consistent with the project's Java 25 baseline.

## Why one archive is listed twice

The official Matcha archive is a single zip containing `assets/`, `data/`, and one
`pack.mcmeta`. It is simultaneously a resource pack and a datapack. Its `pack.mcmeta`
declares `min_format 88.0` and `max_format 107.1` and carries Matcha's own `filter`
block.

`pack/defaultconfigs/global_packs.toml` therefore lists the identical path under both
`[resourcepacks].required` and `[datapacks].required`. Listing it in only one place
would silently deliver half of Matcha: either its mechanics with vanilla textures, or
its textures with vanilla mechanics. Both failure modes are quiet, which is why the
duplication is deliberate and commented in the config.

## File placement

```text
<instance>/
├── mods/
│   ├── fabric-api-0.158.0+26.2.jar
│   └── globalpacks-fabric-26.2-26.2.0.jar
├── datapacks/
│   └── Matcha_Flavoured_1_12.zip      <- resource pack AND datapack
└── config/
    └── global_packs.toml
```

Packwiz writes the archive to `datapacks/` because `.config-packwiz.toml` sets
`datapack-folder = "datapacks"`. Packwiz refuses to add a Modrinth project whose loader
field is `datapack` until that option exists, and the official Matcha project is exactly
such a project. The Packwiz option and the Global Packs config must name the same
folder. `scripts/validate-pack.sh` asserts that agreement, because if the two drift
apart Matcha stops loading without any error message.

## Load order, measured

LAB-02 tested this rather than trusting the changelog wording, and the result is the
opposite of the obvious reading. A probe datapack listed **before** Matcha in
`global_packs.toml` won: the resulting `level.dat` order was Matcha then probe, and the
recipe count changed from 2346 to 2345 because the probe replaced a Matcha recipe.

Minecraft applies later packs over earlier ones, so **the first entry in the config has
the lowest applied priority**. Matcha is listed first deliberately, because it is the
foundation and should be overridden by later entries rather than overriding them. Any
future Many Roads Home pack intended to change part of Matcha must be added **after** it.

## Failure behaviour: fail-open

LAB-02 also measured what happens when the install is wrong, and every path fails open.
A missing archive starts a normal server with 1585 vanilla recipes and **no diagnostic**.
Removing the resource-pack role starts normally with no diagnostic. An altered archive
produces one extra parse error indistinguishable from Matcha's own two.

Global Packs offers no integrity or required-pack verification, so the pack must supply
its own. That gap is ticket MRH-010 and is the reason this dependency is described as
adopted *with mitigation* rather than simply adopted. Full detail in
`labs/results/LAB-02-pack-loader-dual-role.md`.

## Installed-pack verification

The first server test copied the Global Packs config into place by hand, which proved
Matcha loads but skipped the path a real user takes. The `.mrpack` was therefore
re-tested the way a launcher installs it: unpack `overrides/` into the instance root,
then download each declared file to its declared path.

That second test found two things the first one could not.

**The `overrides/defaultconfigs/` route works.** Global Packs read the shipped file and
wrote `config/global_packs.toml` containing the pinned
`datapacks/Matcha_Flavoured_1_12.zip` path rather than its own default of `datapacks/`,
`resourcepacks/`, and `global_packs/required_data/`. The generated config naming our
value and not the mod's default is what proves the override was honoured rather than
coincidentally equivalent.

**The shipped config declared the wrong schema version.** It said `config_version = 3`,
and the 26.2.0 build rewrote it to `4` on first launch, discarding the explanatory
comments. Functionally harmless, but it meant every fresh instance performed a migration
write and lost the documentation. The shipped file now pins `config_version = 4` and
declares the `enable_builtin` key the mod actually writes. After that correction a clean
instance boots with the config byte-identical to the shipped file, with no migration
write.

Independently confirmed on the installed instance: `Found new data pack
Matcha_Flavoured_1_12.zip, loading it automatically`, `Loaded 2346 recipes`,
`Done (1.419s)`, and `world/level.dat` listing the pack under `DataPacks`.

The exported archive was also validated against the published Modrinth `.mrpack` format:
`formatVersion` 1, required keys present, every file carrying both the required `sha1`
and `sha512`, no unsafe paths, and every download over HTTPS. Each declared URL was
fetched and its hash and byte length recompared, and all three matched.

## Client behaviour

Not yet verified. The environment used for this run had no usable display, so a real
client launch could not be performed honestly. The client path is
therefore recorded as an outstanding acceptance criterion rather than claimed. The procedure is written up as a short manual pass in
`docs/testing/client-verification.md`, which needs a launcher and about ten minutes, not
a development environment.

What is known without a client: the archive carries 2845 `assets/` entries and 2671
`data/` entries under a single `pack.mcmeta`, so both halves are present in the file the
pack installs, and the datapack half is confirmed applied on a server. What remains
unproven is only whether Global Packs activates the resource-pack half in a real client
session. A dedicated server never loads resource packs, so no server test can settle
this, and it must not be inferred from the datapack result.

## Server behaviour: verified

A dedicated server was materialised from the exported `.mrpack` and booted.

```sh
./scripts/build-pack.sh
# resolve the manifest into an instance, then:
curl -s "https://meta.fabricmc.net/v2/versions/loader/26.2/0.19.3/1.1.0/server/jar" -o fabric-server-launch.jar
echo "eula=true" > eula.txt
java -Xmx2G -jar fabric-server-launch.jar nogui
```

Observed:

- `Loading Minecraft 26.2 with Fabric Loader 0.19.3`;
- `globalpacks 26.2.0` present in the mod list;
- `Found new data pack Matcha_Flavoured_1_12.zip, loading it automatically`;
- `Loaded 2346 recipes` and `Loaded 1805 advancements`, far above vanilla, confirming
  Matcha's content is genuinely applied rather than merely present;
- `Done (1.519s)! For help, type "help"`;
- `world/level.dat` records `Matcha_Flavoured_1_12.zip` under its enabled `DataPacks`,
  which is the decisive evidence that the new world adopted the pack automatically.

## Known risks

**A non-fatal `FileAlreadyExistsException` appears twice during startup.** It is logged
at ERROR level and names `./datapacks/Matcha_Flavoured_1_12.zip`. It occurs immediately
before the pack loads successfully, so the current reading is that Global Packs
attempts to copy the archive into a location where it already exists. The server
reaches a ready state and the pack applies, so this is cosmetic today. It is recorded
because an ERROR line that is actually harmless trains readers to ignore the log, and
because the behaviour could change. LAB-01 should confirm the cause.

**Matcha's own content produces parse warnings.** `minecraft:advancement/custom/root`
fails to parse, `main:mechanics/wither_test` has mismatched criteria, and two stray
`.txt` files are ignored. These originate in the third-party archive, not in this pack,
and should be reported upstream rather than patched locally.

**Global Packs is All Rights Reserved.** It cannot be forked or patched if it lapses.
The mitigation is that the dependency is shallow: it loads packs and does nothing else,
so replacing it with Paxi or an equivalent would be a config change plus a lab run, not
a redesign.

**Matcha is CC-BY-NC-SA-4.0.** The archive is referenced by URL and hash and is never
committed or repackaged here. Any public distribution requires the licence review in
ADR 0020.

## Rollback plan

1. Remove the Global Packs entry: `packwiz remove globalpacks`, then rebuild.
2. Delete `pack/defaultconfigs/global_packs.toml`.
3. Matcha then loads only where a player enables it manually, which is the pre-pack
   behaviour and is safe: no world data is converted by adding or removing the loader.
4. To move to a different utility, add it, point its own config at the same
   `datapacks/Matcha_Flavoured_1_12.zip`, and re-run the server verification above.

Removing the loader does not remove Matcha's effects from an existing world. A world
created with Matcha enabled keeps the pack listed in `level.dat`, so removing the
archive from a world that already uses it is a save-affecting operation and must follow
`docs/world-save-safety.md`.

## Verification commands

```sh
./scripts/validate-pack.sh          # metadata consistency, pins, folder agreement
./scripts/build-pack.sh             # reproducible .mrpack export
sha256sum Matcha_Flavoured_1_12.zip # must equal the value pinned above
```

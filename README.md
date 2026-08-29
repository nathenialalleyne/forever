# Many Roads Home

**Many ways to play. One world worth keeping.**

Many Roads Home is a Minecraft Fabric **modpack** for long-lived survival worlds. It aims
to make building, exploration, professions, production, trade, and technical engineering
different but interacting routes to a capable world, rather than a single optimal
checklist of farms.

The defining rule:

> Adventure discovers possibilities. Building gives them a home. Professions make them
> repeatable. Engineering scales them. Trade connects them.

## The product is a modpack

This matters more than anything else in this README, because the repository began life as
a custom mod and that history is still visible in `src/`.

The product is:

1. a curated set of maintained third-party mods;
2. carefully designed configuration;
3. datapacks for recipes, loot, tags, trades, progression, and balance;
4. a resource pack for cohesion and compatibility;
5. optional blueprint libraries;
6. a small companion integration mod, used only where existing tools cannot express the
   design.

The companion mod is not the product. Mature existing mods should own block placement,
vein mining, schematic previews, recipe viewing, contextual information, cooking,
seasons, storage indexing, local item transport, rail physics, horses, structures,
performance, and ambience. Custom Java is reserved for connective systems that no
existing mod can provide, such as the capability web, the unified knowledge system,
Matcha normalization, settlement registration, villager careers, cross-mod economy,
discovery consequences, earned passenger services, and world history.

See `docs/modpack-architecture.md` and `docs/architecture/ownership-matrix.csv`.

## Current status

The repository is mid-pivot from a custom-mod-first prototype to a modpack-first product.
The prototype is preserved at tag `prototype-pre-modpack-pivot`.

**The modpack foundation exists and is verified:**

| Component | Pinned |
|---|---|
| Minecraft | `26.2` |
| Fabric Loader | `0.19.3` |
| Fabric API | `0.158.0+26.2` (`NqwNSxwA`) |
| Global Packs | `26.2.0` (`DqrPrUMp`) |
| Matcha Flavoured | `1.12` (`E9rngRfK`) |

A dedicated server built from the exported `.mrpack` boots with Matcha loaded
automatically, applying 2346 recipes and 1805 advancements, with the pack recorded in
`level.dat`. See `docs/compatibility/matcha-loading.md` for the evidence.

**A prototype companion mod also exists**, implementing seven gameplay systems with 143
unit tests and 27 dedicated-server GameTests. None of it has been playtested, and much of
it may be replaced by third-party mods. Its per-file classification is in
`docs/pivot/existing-code-inventory.md`.

## Working with the pack

```sh
./scripts/validate-pack.sh        # metadata consistency, exact pins, no committed binaries
./scripts/build-pack.sh           # reproducible .mrpack export under dist/
./scripts/report-dependencies.sh  # regenerate the dependency lock report
python3 scripts/check-repo-map.py # AGENTS.md's repository map still matches the tree
./scripts/client-smoke.sh         # the client launches, renders, and stops cleanly
```

Client checks run automatically too. An earlier belief that this host had no display was
never tested and turned out to be false, so `./scripts/client-smoke.sh` launches the
client, confirms it renders, and stops it. See `docs/testing/client-environment.md`.

Three things still need a person, because the host has no audio, no narrator, and window
screenshots capture black: visual and colour judgement, sound, and two-player multiplayer.
`docs/testing/client-verification.md` is the short manual pass for those.

Packwiz is the source of truth for the mod list. It has no tagged releases, so install it
with `go install github.com/packwiz/packwiz@latest` per the official guidance, then put
`$(go env GOPATH)/bin` on your PATH. Without it the scripts still run and still validate
the pack; they skip only the checks that need the tool itself, and say so rather than
passing silently. Third-party
JARs are never committed: every dependency is referenced by URL and hash.

## Technical baseline

All versions are pinned. See [`docs/dependency-baseline.md`](docs/dependency-baseline.md)
for verification sources and the update policy.

| Component | Version |
|---|---|
| Minecraft | `26.2` |
| Java/JDK | `25` |
| Fabric Loader | `0.19.3` |
| Fabric API | `0.158.0+26.2` |
| Fabric Loom | `1.17.20` (`net.fabricmc.fabric-loom`) |
| Gradle wrapper | `9.5.1` |
| JUnit | `5.14.2` |
| Matcha Flavoured | `1.12` |

Minecraft 26.2 is non-obfuscated. No Yarn mappings are used. Matcha Flavoured is a
separately acquired datapack/resource-pack archive, not a Gradle dependency. Its pinned
identity is Modrinth project `QI0EmgZ1`, version ID `E9rngRfK`, file
`Matcha_Flavoured_1_12.zip`, release type `release`, Minecraft `26.2`, license
`CC-BY-NC-SA-4.0`.

## Prerequisites and setup

- Git and a POSIX shell for the repository scripts.
- A JDK 25 installation. The locally documented example is:

  ```sh
  export JAVA_HOME=~/toolchains/jdk-25.0.4.1+1
  java -version
  ```

- A disposable development world for any client, server, or Matcha work.

From the repository root, verify Java and use the Gradle wrapper. The wrapper downloads
build dependencies as needed. It does not fetch Matcha.

```sh
cd /path/to/forever
export JAVA_HOME=~/toolchains/jdk-25.0.4.1+1  # adjust for the local JDK 25 install
java -version
```

Read [`AGENTS.md`](AGENTS.md) before making changes. It is the primary AI-agent
instruction file and the single source of truth.

> **WARNING: NEVER use a real Forever survival world for development.** Create or clone
> a disposable world before launching the client, starting a server, installing Matcha,
> changing dependencies, or testing migrations. A save with years of play is not a test
> fixture.

## Build and tests

Build the root mod with:

```sh
./gradlew build
```

Run the plain JVM unit tests with:

```sh
./gradlew test
```

Validate the art assets with:

```sh
./gradlew validateAssets
```

This is a developer-only check. It runs a pure-JDK tool from the separate `assetTools`
source set, so no image-handling code and no image-generation dependency ever reaches
the Minecraft runtime or the mod JAR. It checks filenames, image formats, dimensions,
fully transparent images, missing texture references, duplicate asset IDs, and
agreement between the production tree and `docs/assets/asset-manifest.csv`. Manifest
rows with status `planned` are deliberately never rejected for having no file yet.

Editable art sources live in `art/` and are never loaded by the game. Exported
production assets live in `src/client/resources/assets/forever/`, which is the only
tree Minecraft reads. See `art/README.md` for the quickstart and
`docs/assets/pipeline.md` for the authoritative reference.

Run the dedicated-server GameTest harness with:

```sh
./gradlew runGametest
```

The current GameTest is an initialisation smoke test. It verifies that a dedicated
server can load Forever and reach a live world. It does not verify gameplay because no
gameplay exists yet.

Datagen is currently only a scaffold. The canonical command is:

```sh
./gradlew runDatagen
```

No gameplay data should be claimed as generated until a ticket adds real datagen inputs
and tests. If the scaffold task is not available in the current checkout, record that
as a foundation limitation rather than adding unrelated content.

## Client and dedicated server

Launch the client in a disposable development environment with:

```sh
./gradlew runClient
```

Launch a dedicated server with:

```sh
./gradlew runServer
```

For manual verification, use a disposable world, confirm the server reaches its normal
ready state, confirm the log contains the common Forever initialisation message without
client-class errors, then stop it cleanly. For automated dedicated-server verification,
use `./gradlew runGametest`.

## Matcha acquisition and disposable installation

Matcha is pinned separately from the Fabric mod build. Fetch the exact pinned archive
and its lock metadata from the repository root with:

```sh
./scripts/fetch-matcha.sh
```

Only install the fetched archive into a **disposable dev world**. Stop the world first,
confirm that the target is disposable, and use the script with its explicit world path.
The command name is:

```sh
./scripts/install-matcha-dev.sh
```

Because the script never guesses a world path, the actual guarded invocation is:

```sh
./scripts/install-matcha-dev.sh --world /path/to/disposable-dev-world --ack-dev-world
```

The scripts must not auto-update Matcha or target a live save. Keep the original Matcha
material separate from Forever code and follow [`docs/world-save-safety.md`](docs/world-save-safety.md)
and [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md).

## Matcha audit tool

The audit CLI is a separate Gradle build. Run its tests from its own directory with its
own wrapper:

```sh
cd tools/matcha-audit && ./gradlew test
```

For the report-producing invocation, read
[`docs/matcha-audit/README.md`](docs/matcha-audit/README.md) first and use the CLI's
checked-in `--help` output. The current intended shape is:

```sh
cd tools/matcha-audit
./gradlew run --args="--archive ../../vendor/matcha/Matcha_Flavoured_1_12.zip --lock ../../matcha.lock.json --output ../../generated/matcha/1.12"
```

The audit workflow must use pinned input and deterministic output. Generated official
reports appear under:

```text
generated/matcha/<version>/
```

CI deliberately does not fetch Matcha. Matcha acquisition is a network, licensing, and
determinism boundary, so it is a deliberate developer operation rather than a hidden CI
side effect.

## Design and workflow documents

- [`AGENTS.md`](AGENTS.md), primary AI-agent instructions
- [`docs/vision.md`](docs/vision.md), project premise and intended long arc
- [`docs/design-principles.md`](docs/design-principles.md), non-negotiable principles
- [`docs/architecture.md`](docs/architecture.md), module boundaries and authority
- [`docs/code-guidelines.md`](docs/code-guidelines.md), human code navigation and layer placement
- [`docs/dependency-baseline.md`](docs/dependency-baseline.md), pinned versions
- [`docs/development-workflow.md`](docs/development-workflow.md), ten-step AI workflow
- [`docs/world-save-safety.md`](docs/world-save-safety.md), save and migration safety
- [`docs/roadmap.md`](docs/roadmap.md), milestone status
- [`docs/backlog.md`](docs/backlog.md), one-ticket work queue

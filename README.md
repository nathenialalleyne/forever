# Forever

Forever is a Minecraft Fabric gameplay framework and curated modpack foundation for
long-lived survival worlds. Its goal is to make specialisation, civilisation,
exploration, and infrastructure competitive routes to abundance without removing the
freedom and improvisation that make ordinary Minecraft work.

## Current status

This repository is currently an **architecture and audit foundation only**. The current
scope is M0 project foundation, M1 Matcha acquisition/pinning/audit tooling, and M2
AI-readable architecture and design documentation.

**Gameplay systems are not implemented yet.** There is no implemented mastery,
equipment condition or repair, reforge, settlement, villager career, Mason, Obol,
warehouse, logistics, transportation, season, food-trait, shop, worker, or Field Guide
gameplay system. Future systems are documented and queued in `docs/backlog.md`, not
silently present in the foundation.

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
- [`docs/dependency-baseline.md`](docs/dependency-baseline.md), pinned versions
- [`docs/development-workflow.md`](docs/development-workflow.md), ten-step AI workflow
- [`docs/world-save-safety.md`](docs/world-save-safety.md), save and migration safety
- [`docs/roadmap.md`](docs/roadmap.md), milestone status
- [`docs/backlog.md`](docs/backlog.md), one-ticket work queue

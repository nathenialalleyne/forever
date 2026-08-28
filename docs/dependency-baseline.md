# Dependency Baseline

Every version in this document is **pinned deliberately**. Nothing here floats.
Do not upgrade any of these without opening a backlog ticket and re-running the
full build, tests, and a dedicated-server smoke test against a disposable world.

Resolved on: **2026-08-27**

## Pinned versions

| Component | Version | Authoritative source |
|---|---|---|
| Minecraft Java Edition | `26.2` | `https://meta.fabricmc.net/v2/versions/game` (official Fabric meta) |
| Java / JDK | `25` (Temurin 25.0.4.1+1 used locally) | `https://api.adoptium.net/v3/info/available_releases` (25 is current LTS); required by the official Fabric 26.2 template |
| Fabric Loader | `0.19.3` | `https://meta.fabricmc.net/v2/versions/loader` (latest entry marked `stable: true`) |
| Fabric API | `0.158.0+26.2` | `https://api.modrinth.com/v2/project/fabric-api/version?game_versions=["26.2"]` (official Modrinth listing) |
| Fabric Loom | `1.17.20` | `https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml` |
| Loom plugin ID | `net.fabricmc.fabric-loom` | official Fabric template `build.gradle` |
| Gradle | `9.5.1` (wrapper) | official Fabric template `gradle/wrapper/gradle-wrapper.properties` |
| JUnit | `5.14.2` (BOM) | Maven Central |
| Matcha Flavoured | `1.12` | `https://api.modrinth.com/v2/project/QI0EmgZ1` |

## How these were verified

All values were read from official Fabric meta endpoints, official Fabric Maven
metadata, the official `FabricMC/fabric-example-mod` repository branch `26.2`, and
the official Matcha Flavoured Modrinth project. No tutorials, unofficial wikis, or
search-result snippets were used.

The official template branch for Minecraft 26.2 exists at
`https://github.com/FabricMC/fabric-example-mod/tree/26.2` and its
`gradle.properties` states:

```
minecraft_version=26.2
loader_version=0.19.3
loom_version=1.17-SNAPSHOT
fabric_api_version=0.158.0+26.2
```

## Deliberate deviations from the official template

### 1. Loom is pinned to `1.17.20`, not `1.17-SNAPSHOT`

The official template ships a floating snapshot Loom version. Forever forbids
floating dependency versions (see design principle 18 and `AGENTS.md`), because a
snapshot can change under a world save without warning. `1.17.20` is the newest
released (non-alpha) `1.17.x` Loom in official Fabric Maven metadata and is the
released counterpart of the snapshot line the template targets.

Note that Fabric Maven's `<release>` marker currently points at `1.18.0-alpha.19`.
Alpha plugin versions are not appropriate for a project whose primary risk is world
save corruption, so the stable `1.17.x` line was chosen.

### 2. Non-obfuscated Minecraft, no Yarn mappings

`https://meta.fabricmc.net/v2/versions/yarn/26.2` returns an empty array. Minecraft
26.2 is distributed non-obfuscated, so no mappings dependency is declared. This is
why modern Loom is required and why older tutorials referencing
`mappings loom.officialMojangMappings()` or Yarn do not apply.

### 3. No mixins

The official template includes example mixins. Forever removed them: this milestone
has no concrete requirement that cannot be met with the public Fabric API, and every
mixin is a permanent maintenance and compatibility cost. Mixins may be added later
only when a specific ticket justifies one.

## Matcha

Matcha Flavoured is a datapack/resource-pack archive, **not** a Fabric mod, so it is
not a Gradle dependency. It is acquired and pinned by `scripts/fetch-matcha.sh` /
`.ps1`, and its exact identity is recorded in `matcha.lock.json` including SHA-256.

| Field | Value |
|---|---|
| Modrinth project ID | `QI0EmgZ1` |
| Slug | `matcha-flavoured` |
| Selected version | `1.12` |
| Version ID | `E9rngRfK` |
| Release type | `release` |
| Minecraft compatibility | `26.2` |
| License | `CC-BY-NC-SA-4.0` |

See `THIRD_PARTY_NOTICES.md` and `docs/compatibility/matcha.md`.

## Update policy

1. Dependency updates are their own backlog ticket. They are never a side effect of
   feature work.
2. Clone any development world before changing a pinned version.
3. Matcha and Fabric versions are never auto-updated by scripts or CI.
4. If an authoritative source contradicts this file, update this file first, in its
   own commit, with the source named.

# Modpack architecture

Many Roads Home is a modpack. The product is a curated set of maintained third-party
mods, the configuration that makes them agree with each other, datapacks that shape
recipes and progression, a resource pack for cohesion, optional blueprint libraries,
and a small companion mod used only where nothing existing can do the job.

The companion mod is not the product. That distinction is the whole point of this
document, because the repository began as a custom mod and the habits of that phase do
not transfer.

## What owns what

Ownership is assigned per system in `docs/architecture/ownership-matrix.csv`. The
categories are:

| Owner | Meaning |
|---|---|
| `THIRD_PARTY_MOD` | An existing maintained mod provides the mechanic |
| `PACK_CONFIG` | Configuration of a third-party mod expresses the decision |
| `DATAPACK` | Recipes, loot, tags, trades, advancements, or balance data |
| `RESOURCE_PACK` | Textures, models, sounds, translations, UI cohesion |
| `BLUEPRINT_LIBRARY` | Optional build content, never a gameplay requirement |
| `COMPANION_INTEGRATION` | Small Java that connects other mods but owns no mechanic |
| `CUSTOM_CORE` | Java that implements a mechanic no existing mod provides |
| `UNDECIDED` | Not yet researched or not yet decided |

Every `CUSTOM_CORE` and `COMPANION_INTEGRATION` entry must record why no maintained
option is sufficient. That justification is the gate, and it is deliberately annoying
to write, because the failure mode of a project like this is a slow drift back into
building everything from scratch.

## The escalation order

Before writing Java, work down this list and stop at the first rung that can express
the requirement:

1. existing configuration
2. datapack
3. resource pack
4. a supported scripting layer, if stable and compatible
5. a public API or event integration
6. a narrow compatibility adapter
7. a narrow, version-guarded Mixin
8. a private fork
9. a new custom implementation

Rungs 7 and 8 are hazardous and require their own justification. Never modify a
third-party JAR in place. ADR 0026 records the full policy and the conditions under
which a custom implementation is approved.

## Repository layout

```text
pack/                  Packwiz source of truth. Metadata only, never binaries.
  pack.toml            Pinned Minecraft, loader, and pack identity
  index.toml           Generated index; refreshed by scripts
  mods/*.pw.toml       One pinned dependency per file
  datapacks/*.pw.toml  Datapack dependencies, including official Matcha
  config/              Config shipped to every instance
  defaultconfigs/      Per-world default config, including Global Packs
companion/             Future home of the integration mod
src/                   Current companion code, pending the pivot classification
tools/                 Developer tooling: Matcha audit CLI, mod research
labs/                  Compatibility spikes: manifests and written results
scripts/               build-pack, validate-pack, report-dependencies
dist/                  Exported .mrpack artifacts. Git-ignored build output.
docs/                  Design, ADRs, research matrices, compatibility notes
```

## Why Packwiz

Packwiz is the source of truth because it stores a pinned project ID, version ID, and
file hash per dependency in plain TOML that reviews well in a diff. A launcher-exported
mods directory is not an acceptable source of truth: it loses the distinction between a
deliberate pin and whatever happened to be installed, and it invites committing JARs.

The tradeoffs are real. Packwiz has no tagged releases, so installation is a Go build
from source or a CI artifact, which is friction for a new contributor and a supply-chain
consideration for CI. Its CurseForge and Modrinth exports are good but the tool is small
and maintained by few people. Those costs were accepted because the alternative,
managing a mod list by hand, fails silently and expensively. ADR 0025 records this.

Third-party binaries are never committed. Every dependency is referenced by URL and
hash, which keeps licensing clean and the repository small. `scripts/validate-pack.sh`
enforces this, and it deliberately excludes the Gradle wrapper JAR, which is a
legitimate part of a Gradle build.

## The companion mod boundary

The companion mod exists for connective systems that no third-party mod can reasonably
own, because they are specific to how this pack combines other mods:

- the capability web that guarantees multiple routes to essential capabilities;
- the unified contextual knowledge system, in place of many starter books;
- Matcha normalization behind an adapter, so its internals never leak;
- free-form settlement registration that evaluates function rather than style;
- villager career and institutional progression;
- cross-mod economic behaviour;
- discovery consequences;
- earned passenger services;
- world history.

Everything else should be a mod, a config, a datapack, or a resource pack. If the
companion mod grows a feature that a maintained mod already provides, that is a defect
in the ownership decision, not a feature.

## Client and server sides

Every dependency is marked with the correct side in its `.pw.toml`. A client-only mod
must not be required on a dedicated server, and a mod that carries server-authoritative
behaviour must not be optional on the client. Getting this wrong produces failures that
look like corruption but are only a packaging mistake, so it is checked per dependency
rather than assumed.

## Verification

The pack is validated and exported by script, and both run in CI without private
credentials:

```sh
./scripts/validate-pack.sh        # metadata consistency, exact pins, no committed binaries
./scripts/build-pack.sh           # reproducible .mrpack under dist/
./scripts/report-dependencies.sh  # regenerate the dependency lock report
```

A claim that the pack works is only credible with the command output that produced it.
The dedicated-server verification for the current baseline is recorded in
`docs/compatibility/matcha-loading.md`, including the exact log lines observed.

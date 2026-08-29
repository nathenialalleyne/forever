# AGENTS.md

This is the primary instruction file for AI agents working on **Many Roads Home**. It is
the single source of truth for agent behaviour, scope control, design constraints, and
completion reporting.

## The product is a modpack, not a mod

Read this before anything else, because it inverts the assumption the repository was
built on. Many Roads Home is a **modpack**. The product is a curated set of maintained
third-party mods, their configuration, datapacks, a resource pack, optional blueprint
libraries, and a **small** companion integration mod used only where existing tools
cannot express the design.

The companion mod in `src/` is not the product. It began as a custom-mod-first prototype
and much of it may be replaced by third-party mods. Its classification is recorded in
`docs/pivot/existing-code-inventory.md`, and the prototype is preserved at the tag
`prototype-pre-modpack-pivot`.

Prefer mature existing mods for block placement and excavation, vein mining, schematic
previews, recipe viewing, contextual information, cooking content, seasons, storage
indexing, local item transport, rail physics, horse mechanics, structure generation,
performance, and visual or sound ambience.

Reserve custom Java for connective systems that no maintained mod can provide: the
capability web, the unified contextual knowledge system, Matcha normalization, free-form
settlement registration, villager career and institutional progression, cross-mod
economic behaviour, discovery consequences, earned passenger services, world history, and
integrations that cannot be expressed through configuration, datapacks, resource packs,
or supported APIs.

**Before writing Java, work down the escalation order in
[ADR 0026](docs/adr/0026-existing-mod-first-escalation-policy.md) and stop at the first
rung that works:** existing configuration, datapack, resource pack, supported scripting
layer, public API or event integration, narrow compatibility adapter, narrow
version-guarded Mixin, private fork, then new custom implementation. A custom
implementation requires a documented gap analysis under
[ADR 0033](docs/adr/0033-documented-gap-analysis-for-companion-code.md). Never modify a
third-party JAR.

The modpack source of truth is `pack/`, managed with Packwiz. Third-party JARs are never
committed; dependencies are referenced by URL and hash. `CLAUDE.md` and `.github/copilot-instructions.md` are deliberately
short pointers to this file. Do not treat a shorter pointer, a code comment, or an
implementation convenience as permission to weaken these instructions.

## Before changing anything

Read `docs/_agent-brief.md` for the current run scope, then read all of the following
before changing a system:

1. `docs/vision.md`, to understand the player problem and the intended long arc.
2. `docs/design-principles.md`, to identify non-negotiable constraints.
3. `docs/architecture.md`, to place code at the correct boundary and preserve server
   authority.
4. Every relevant record in `docs/adr/`, including the ADR named by the system
   specification or backlog ticket.
5. The relevant system specification in `docs/systems/`, if one exists.
6. `docs/dependency-baseline.md` when the change touches a version, external project,
   build tool, or generated output.
7. `docs/world-save-safety.md` before touching saves, persistent data, migrations, or
   development-world tooling.

If the relevant ADR or system specification does not exist, stop at the design gap.
Create or refine a backlog ticket for the missing decision and report the gap instead
of inventing a silent design. The specification is authoritative. When code and a specification conflict, stop, report the conflict, and do not rewrite the specification to match an implementation shortcut.

## Ticket and scope discipline

Work from **one backlog ticket at a time**. A ticket must have an objective,
dependencies, explicit non-goals, acceptance criteria, and expected tests before
implementation starts. If the request is broad, split it into tickets first. The phrase
“Build the system” means implement only the selected ticket and its necessary follow-through,
not every neighbouring system. Never use a broad phrase as permission to change
unrelated systems, APIs, save formats, assets, or balance values.

Keep the change minimal. Do not create empty Java classes or future package placeholders
just to make the architecture diagram look implemented. Every TODO comment must name a
backlog ticket such as `FVR-123`. Do not change an approved design decision silently.
If a decision must change, record a new or superseding ADR, update the ticket, and make
the change visible in the completion report.

## Current status and hard scope

The project is **mid-pivot** from a custom-mod-first prototype to a modpack-first
product. Two things are true at once and must not be confused.

**The modpack foundation exists and is verified.** `pack/` pins Minecraft 26.2, Fabric
loader 0.19.3, Fabric API `NqwNSxwA`, Global Packs `DqrPrUMp`, and official Matcha
`E9rngRfK`. A dedicated server built from the exported `.mrpack` boots with Matcha loaded
automatically. Evidence is in `docs/compatibility/matcha-loading.md`.

**A prototype companion mod also exists** in `src/`, implementing seven gameplay systems
with unit tests and dedicated-server GameTests. It has never been playtested, and several
of its systems duplicate mechanics that maintained mods already own. Do not treat its
existence as approval to extend it.

The next work is **compatibility spikes**, not gameplay. The lab queue is in
`labs/README.md` and the tickets are in `docs/backlog.md`. LAB-01, information and
onboarding, is next.

Do not implement a gameplay system during this phase. Do not add every candidate mod to
the primary pack at once. Do not write custom Java for a mechanic a maintained mod
provides. Establish what the pack should own and what existing mods should own before
writing more gameplay code.

## Non-negotiable engineering and design rules

These rules explain the constraints that future implementations must preserve.

### Design authority and integration boundaries

- **Read the design before coding.** Vision, principles, architecture, relevant ADRs,
  and the relevant system specification must be understood before a system changes.
  This prevents an agent from solving a local problem by violating a project-wide
  decision.
- **Isolate Matcha.** No code outside `dev.forever.compat.matcha` may hardcode Matcha
  scoreboard names, function paths, advancement IDs, disguised item identities, custom
  model data, recipe tricks, or other private implementation details. Matcha is a
  third-party datapack with no API stability guarantee. Translate it behind the adapter
  boundary instead of spreading brittle identifiers through Forever.
- **Keep ordinary Minecraft available.** Normal block placement and ordinary Minecraft
  play must remain possible. Registration, a specialisation, a quest, a workshop, or a
  Forever screen must never become a toll gate in front of vanilla building or basic
  survival play.
  Normal block placement and ordinary Minecraft play must remain possible at all times.
- **Do not make visual state colour-only.** A wear state, warning, mastery state, or
  status must also have text, a tooltip, an icon shape, or another non-colour cue.
- **Do not ship final textures early.** Final visual assets require both an approved
  asset manifest and an approved gameplay need. A placeholder or documented absence is
  safer than committing art for a design that has not been accepted.

### Persistent data and authority

- **Every persistent-data structure is a migration problem from its first line.** It
  must define an explicit schema version, a codec or serializer, validation rules, a
  migration strategy, failure behaviour, and tests. This applies to player, item,
  villager, settlement, warehouse, shipment, route, and chronicle data. Do not hide
  unrelated records in one unversionable save blob.
- **The server is authoritative.** Every server-authoritative system must be tested for
  persistence across save and reload, duplication resistance, restart behaviour, and
  the client/server authority boundary. Clients render synchronised projections and do
  not decide outcomes. Network payloads must be versioned and bounded.
- **Fail safely.** Never silently discard invalid data, swallow an exception without
  actionable context, or accept a partial migration as success. Preserve the original
  data where recovery is possible and make the failure visible.

### Performance and progression

- **No unbounded world scans.** Use explicit registration, bounded queries, and cached
  results. A feature must not search every chunk, block, entity, or container on an
  unbounded schedule.
- **No permanent chunk loading for passive settlement simulation.** Unloaded
  settlements use bounded abstract simulation with a fixed budget. Do not force-load
  chunks to make off-screen NPCs pathfind while nobody is present.
- **Repetition is not mastery.** Do not award mastery XP for repetitive actions unless
  an explicitly approved ADR permits that exact source. Raw counters such as “place
  20,000 blocks” recreate the grind Forever is intended to replace.
- **Items are not permanently deleted by ordinary durability exhaustion.** A zero-
  condition item becomes broken and unusable until repaired. It retains its identity
  and data rather than disappearing.
- **Put balance in data.** Thresholds, prices, rates, slot counts, distances, and
  multipliers belong in data or configuration wherever practical. Mechanics belong in
  code, but tuning should not require rewriting logic.
- **Build every system as an extension surface.** Forever is a framework, so new content
  must be cheap to add later without editing a switch statement and recompiling. See
  [ADR 0022](docs/adr/0022-extensible-by-default.md). Three seams are expected by default:
  catalogue content loads from datapack data and its loader accepts **any namespace**, not
  only `forever`; behaviour that varies by environment goes behind a small port named in
  Forever's own vocabulary with a safe no-op fallback, as `SettlementWorldView`,
  `GuideRecipeViewerCapability`, and `MatchaAdapter` already do; and a fallible operation
  returns a described result carrying the reason rather than a bare boolean.
  This does **not** override the ban on premature abstraction below. Do not add an
  interface for a single implementation with no second caller, an abstract base class where
  a record and a function suffice, or a plugin registry for a system with one entry. Ship
  the concrete implementation and extract the seam when the second caller genuinely
  arrives. Prefer composition and data over inheritance.

- **Explain every player-facing mechanic in game.** A complete mechanic needs a
  translation, a tooltip or UI explanation, a Field Guide entry, and recipe-viewer
  integration when recipes are relevant. Discovery may hide a possibility, but it may
  not hide instructions for a process the player is expected to perform.

### Validation and reporting

- **Run all applicable builds and tests before claiming completion.** Distinguish
  commands that passed, failed, or were not run. Do not turn a local inspection into a
  claim that a build passed.
- **Complete with an evidence report.** State the files changed, commands run, test
  results, assumptions, known risks, and the next ticket. Review the final diff against
  the selected ticket before stopping.
- **The repository is PRIVATE, and making it public is a licensing decision, not a
  settings change.** `origin` is `github.com/nathenialalleyne/forever` and is private on
  purpose. `generated/matcha/<version>/` contains roughly 1,570 verbatim excerpts of
  Matcha command text, which is CC-BY-NC-SA-4.0 share-alike third-party material, and
  `LICENSE` is All-rights-reserved. [ADR 0020](docs/adr/0020-private-project-license-separation.md)
  requires a licence recheck before any public repository, release, or redistribution.
  Do not run `gh repo edit --visibility public` or publish a mirror. Making it public
  first requires untracking the Matcha-derived reports, choosing a licence for the
  original code, and recording a superseding ADR.

- **Commit at every green checkpoint, and push if a remote exists.** Do not accumulate a
  large uncommitted working tree. Commit as soon as the build and the applicable tests
  pass and the change is coherent on its own, then `git push` when a remote is configured.
  A single commit at the end of a long session is a failure mode: uncommitted work is
  invisible to everyone else, it cannot be reviewed incrementally, and it is lost outright
  if the tree is reset or another agent cleans the workspace. This repository has already
  lost untracked work that way.
  Practical rules. Prefer several small commits that each pass `./gradlew build` over one
  large one. Never commit a red build; fix it or stash the broken part. Never commit a
  merge conflict marker, a debugging probe, or a temporary file. When several agents share
  a checkout, commit only the paths you own, using explicit `git add <path>` rather than
  `git add -A`, so you do not sweep up another agent's half-finished edit. Never run
  `git checkout`, `git reset`, `git stash`, or `git clean` against a shared tree, because
  those discard work you do not own.

- **Protect real worlds.** Automated development must never open, modify, or point a
  tool at a real user survival world. Use a disposable development world, and follow
  `docs/world-save-safety.md` for cloning, backups, dependency changes, and migration
  tests.

## Pinned technical baseline

These versions are authoritative. Do not float or upgrade them as a side effect of
feature work. A version change requires its own ticket, an updated baseline document,
and the disposable-world validation described in `docs/dependency-baseline.md` and
`docs/world-save-safety.md`.

| Component | Version |
|---|---|
| Minecraft | `26.2` |
| Java/JDK | `25` |
| Fabric Loader | `0.19.3` |
| Fabric API | `0.158.0+26.2` |
| Fabric Loom | `1.17.20` (plugin ID `net.fabricmc.fabric-loom`) |
| Gradle wrapper | `9.5.1` |
| JUnit | `5.14.2` |
| Matcha Flavoured | `1.12` (Modrinth version `E9rngRfK`) |
| Global Packs | `26.2.0` (Modrinth version `DqrPrUMp`) |
| Packwiz | no tagged releases; `go install github.com/packwiz/packwiz@latest` |

The pack dependencies above are pinned in `pack/` as Packwiz metadata and are the
authoritative list. `scripts/validate-pack.sh` fails the build if Minecraft or the Fabric
loader drifts from this table, which is the check that stops a silent version change.

Minecraft 26.2 is distributed non-obfuscated. There are no Yarn mappings and none are
declared. The local JDK 25 example is:

```sh
export JAVA_HOME=~/toolchains/jdk-25.0.4.1+1
```

Matcha is not a Gradle dependency. The pinned release is Modrinth project `QI0EmgZ1`,
slug `matcha-flavoured`, version ID `E9rngRfK`, file
`Matcha_Flavoured_1_12.zip`, release type `release`, published
`2026-08-12T01:54:25.512843Z`, compatible with Minecraft `26.2`, and licensed
`CC-BY-NC-SA-4.0`. Its URL is
`https://cdn.modrinth.com/data/QI0EmgZ1/versions/E9rngRfK/Matcha_Flavoured_1_12.zip`.
Acquire it only through the pinned tooling. Keep Matcha-derived material separate from
Forever's original code and observe `THIRD_PARTY_NOTICES.md`.

## Repository map

The map distinguishes the small foundation that exists from boundaries reserved for
future work. A directory being shown here does not mean its gameplay is implemented.

| Path | Responsibility and current state |
|---|---|
| `README.md` | Human and contributor entry point, including the safety warning and canonical commands. |
| `AGENTS.md` | This primary AI-agent rulebook. |
| `CLAUDE.md` | Short pointer to this file and the design documents. |
| `.github/copilot-instructions.md` | Short pointer to this file and the design documents. |
| `.github/workflows/ci.yml` | Java 25 root build, unit-test, and standalone audit-tool CI. It deliberately does not fetch Matcha. |
| `build.gradle`, `settings.gradle`, `gradle.properties` | Root Fabric/Loom build, split source sets, test configuration, and pinned properties. Do not edit them for a documentation-only ticket. |
| `gradle/`, `gradlew`, `gradlew.bat` | Root Gradle wrapper at version 9.5.1. |
| `src/main/` | Common/server-safe source and resources. Holds the companion mod, which is layered `domain`/`application`/`adapter` per ADR 0021 and enforced by ArchUnit. It is still at the repository root rather than under `companion/`; see that directory's README. |
| `src/client/` | Client-only source and resources, including the client entrypoint and asset placeholders. Loom keeps these classes off the common classpath. |
| `src/test/` | Plain JVM JUnit tests for logic that does not need Minecraft. |
| `src/gametest/` | Dedicated-server GameTest source and resources. The current test is an initialisation smoke test. |
| `pack/` | **Packwiz modpack source of truth.** Metadata only, never third-party binaries. `pack.toml` pins Minecraft and the loader; `mods/` and `datapacks/` hold one pinned dependency per file. |
| `companion/` | Reserved home for the companion mod. Deliberately a documented boundary marker, not a placeholder: the source still lives in `src/` and its README explains why the move is a separate ticket. Do not add code here without that ticket. |
| `art/` | Source art, palettes, and previews for the asset pipeline, with `assetTools` and `./gradlew validateAssets`. Working files only; shipped textures live under `src/client/resources/`. |
| `config/checkstyle/` | Checkstyle rules and suppressions enforcing the layered-package boundaries. |
| `CHANGELOG.md` | Human-readable record of notable changes. |
| `.editorconfig` | Shared whitespace and encoding settings. |
| `LICENSE`, `THIRD_PARTY_NOTICES.md` | Rights model and third-party attribution. The current licence is All-Rights-Reserved and is **not** an open-source grant. Read `labs/results/LAB-LICENCE-publication-readiness.md` and ADR 0020 before any visibility or distribution change. |
| `.gitignore` | Ignore rules. Keeps `vendor/` archives, `dist/` exports, and build output out of version control, which is what stops third-party binaries being committed. |
| `.config-packwiz.toml` | Packwiz configuration required by `build-pack.sh` and `validate-pack.sh`. Its `datapack-folder` setting is what lets packwiz accept the official Matcha archive, whose Modrinth loader field is `datapack`. |
| `labs/` | Compatibility spikes: manifests and written results. Candidates are tested here in small groups, never added straight to `pack/`. |
| `dist/` | Exported `.mrpack` build artifacts. Git-ignored; produced by `scripts/build-pack.sh`. |
| `scripts/` | Pack build, validation, and dependency reporting, plus pinned Matcha acquisition and disposable development-world installation. These scripts must never target a live world. |
| `vendor/matcha/` | Local, pinned Matcha material when deliberately acquired. It is not a Gradle dependency and must remain separate from original code. |
| `matcha.lock.json` | Exact Matcha release identity and SHA-256 lock metadata, when acquired by the pinned script. |
| `tools/matcha-audit/` | Standalone audit CLI with its own Gradle wrapper and tests. It must not be included in the mod JAR. |
| `generated/matcha/<version>/` | Generated Matcha audit reports. Generated output must be deterministic and must not be treated as hand-authored source. |
| `docs/_agent-brief.md` | Run-specific scope and baseline brief. |
| `docs/vision.md` | Player problem, intended long arc, and current status. |
| `docs/design-principles.md` | Eighteen non-negotiable design principles. |
| `docs/architecture.md` | Intended module boundaries, data placement, authority, and performance constraints. |
| `docs/dependency-baseline.md` | Pinned versions, verification sources, and update policy. |
| `docs/adr/` | Approved architectural decisions. Read relevant records before changing a decision. |
| `docs/systems/` | System specifications. Read the relevant specification before implementing that system. |
| `docs/compatibility/` | Third-party compatibility contracts, including Matcha adapter guidance. |
| `docs/matcha-audit/` | Audit inputs, report conventions, and classification evidence. |
| `docs/assets/` | Approved asset direction and manifest. Final textures require both this approval and an accepted gameplay need. |
| `docs/playtests/` | Future playtest plans and findings. |
| `docs/development-workflow.md` | The ten-step AI implementation workflow. |
| `docs/world-save-safety.md` | Disposable-world, backup, dependency, and migration safety rules. |
| `docs/roadmap.md` | Milestones and scope status. |
| `docs/backlog.md` | Ticket queue and acceptance contracts. |
| `docs/session-report-*.md` | Completion reports. The most recent one states where the project actually stands and what to do next. |
| `docs/decisions/` | Decisions that require the project owner rather than an experiment. A file prefixed `OPEN-` is unresolved and names exactly what is being asked. |

The architecture document is intentionally broader than the current source tree. Do
not create empty packages to fill the map. The first implementation of a system should
create its boundary, tests, and documentation together under a selected ticket.

## Canonical build, test, and development commands

Run from the repository root unless a command changes directory. These are the exact
commands expected by the project. They are not evidence by themselves. Only report a
command as passing when it was actually run and its result observed.

Modpack commands, which are the primary product:

```sh
./scripts/validate-pack.sh        # metadata consistency, exact pins, no committed binaries
./scripts/build-pack.sh           # reproducible .mrpack export under dist/
./scripts/report-dependencies.sh  # regenerate the dependency lock report
```

Companion-mod commands:

```sh
./gradlew build
./gradlew test
./gradlew runClient
./gradlew runServer
./gradlew runGametest
./gradlew runDatagen
./scripts/fetch-matcha.sh
./scripts/install-matcha-dev.sh
cd tools/matcha-audit && ./gradlew test
```

`runGametest` is the automated dedicated-server smoke path. `runServer` is the manual
dedicated-server launch path. Use a disposable world, confirm that the server reaches
its normal ready state and logs the common Forever initialiser without client-class
errors, then stop it cleanly. `runDatagen` is currently a scaffold command. It must not
be described as generating gameplay content until a ticket adds and verifies real data
generation. If a scaffold task is not available in a checkout, report that limitation
instead of adding unrelated data.

The fetch and install scripts are developer-only operations. Fetch the exact pinned
Matcha release before installing it into a disposable dev world. Never fetch or install
Matcha from CI, and never use a real Forever survival world. Audit reports belong under
`generated/matcha/<version>/`.

`./scripts/install-matcha-dev.sh` requires an explicit `--world <path>` and either the
`--ack-dev-world` flag or a `.forever-dev-world` marker. The script must never infer a
world from `run/`, a Minecraft installation, or a `saves/` directory.

## Routing table: read this before doing that

| If you are about to... | Read first |
|---|---|
| Change a gameplay system or its public behaviour | `docs/vision.md`, `docs/design-principles.md`, `docs/architecture.md`, the relevant ADR, the relevant `docs/systems/` specification, and its `docs/backlog.md` ticket |
| Add or change persistent player, item, entity, world, route, warehouse, or history data | `docs/architecture.md` data placement, `docs/world-save-safety.md`, the relevant system specification, and the relevant migration ADR |
| Touch Matcha identifiers, recipes, functions, advancements, or disguised items | `docs/dependency-baseline.md`, the relevant Matcha audit/classification docs, and the Matcha adapter ADR. Keep all internals inside `dev.forever.compat.matcha`. |
| Change common/client boundaries, networking, or a screen | `docs/architecture.md` authority section, the relevant system specification, and the source-set rules in the build configuration |
| Change a dependency, Minecraft, Fabric, Loom, Gradle, or JUnit version | `docs/dependency-baseline.md` and `docs/world-save-safety.md`, then create a version-update ticket |
| Add ANY new mechanic, or reach for custom Java | [ADR 0026](docs/adr/0026-existing-mod-first-escalation-policy.md) escalation order, `docs/mod-research/candidate-matrix.csv`, and `docs/architecture/ownership-matrix.csv`. Ask first whether a maintained mod already owns this. |
| Add, update, pin, or remove a mod | `docs/modpack-architecture.md`, `pack/` metadata, then run `scripts/validate-pack.sh`. Never commit a JAR. |
| Test a candidate mod | `docs/testing/compatibility-labs.md` and `labs/README.md`. Use an isolated lab, never the primary pack. |
| Add a new content type, interface, base class, registry, or optional integration | [ADR 0022](docs/adr/0022-extensible-by-default.md) and `docs/code-guidelines.md`. Ask whether it should be datapack data, a port, or a described result, and whether a second caller actually exists yet |
| Add balance values, prices, thresholds, rates, slots, or distances | `docs/design-principles.md` principle 12, the relevant system specification, and the data/configuration plan |
| Add a player-facing mechanic, tooltip, translation, or recipe | `docs/design-principles.md` principles 2, 15, and 16, then the Field Guide and recipe-viewer requirements |
| Add textures, models, sounds, or other final art | `docs/design-principles.md` principles 16 and 17 plus the approved asset manifest in `docs/assets/` |
| Change settlement simulation, routes, or world queries | `docs/architecture.md` performance constraints and the relevant system specification. Verify bounded work, cached results, and no permanent chunk loading. |
| Run anything against a world save | `docs/world-save-safety.md`. If the world is real or irreplaceable, stop and do not run it. |

## Completion report

At the end of a ticket, report the ticket ID and stop. Include:

- files changed, with a sentence explaining why each was in scope;
- commands actually run, including commands intentionally not run;
- test and build results, with failure output or an external blocker when relevant;
- assumptions and any specification or dependency uncertainty;
- known risks, especially save, migration, duplication, authority, and compatibility risks;
- the next backlog ticket, without starting it in the same change; and
- the commit range for the work, confirming it is committed and pushed when a remote
  exists. Work that is only described in a report and left uncommitted is not delivered.

The next agent should be able to resume from this report without guessing what happened.

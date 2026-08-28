# Forever backlog

This is the ordered ticket queue. Work on one ticket at a time. A ticket is not ready
for implementation unless its objective, dependencies, explicit non-goals, acceptance
criteria, and expected tests are clear. Status is part of the contract:

- **Done** means the ticket is part of the completed foundation scope recorded for this
  run. It does not mean future gameplay has been implemented.
- **Next** is the next intended ticket after the foundation.
- **Planned** is not started and must not be implemented opportunistically.

The sections are intentionally self-contained. If the queue later grows beyond one
file, a ticket may move to `docs/backlog/FVR-xxx.md` without changing its ID or fields.
This file remains the index and dependency map.

## Foundation

### FVR-001: Bootstrap Fabric 26.2 project [Done]

- **Objective:** Establish the `forever` Fabric project with the pinned Minecraft 26.2,
  Java 25, Fabric Loader, Fabric API, Loom, and `dev.forever` project identity.
- **Dependencies:** None.
- **Explicit non-goals:** No gameplay registrations, custom items, custom blocks,
  screens, assets, or Matcha integration.
- **Acceptance criteria:**
  1. The root wrapper and project properties use the approved pinned baseline.
  2. The common and client entrypoints load with mod ID `forever` and version
     `0.0.0-dev`.
  3. Minecraft 26.2 is treated as non-obfuscated with no Yarn mappings declared.
- **Expected tests:** `./gradlew build`; load the minimal mod in `./gradlew runClient`
  and confirm a disposable dedicated-server startup path is available.

### FVR-002: Add common/client source separation [Done]

- **Objective:** Make common/server-safe and client-only code separate source sets so
  illegal references fail during compilation rather than on a dedicated server.
- **Dependencies:** FVR-001.
- **Explicit non-goals:** No client screens, HUDs, networking payloads, or gameplay
  systems.
- **Acceptance criteria:**
  1. The build has distinct common and client source sets and entrypoints.
  2. Common code contains no reference to `dev.forever.client`.
  3. The client is documented as a projection of server state, never the gameplay
     authority.
- **Expected tests:** Root compilation and unit tests; `./gradlew runGametest` or an
  equivalent dedicated-server smoke test proving common loading does not require client
  classes.

### FVR-003: Add automated build/test baseline [Done]

- **Objective:** Make root compilation, plain JVM tests, and dedicated-server GameTest
  startup repeatable for local development and CI.
- **Dependencies:** FVR-001 and FVR-002.
- **Explicit non-goals:** No gameplay assertions beyond foundation initialisation and
  no network fetch of Matcha.
- **Acceptance criteria:**
  1. `./gradlew build` and `./gradlew test` are canonical root commands.
  2. A separate GameTest source set and `runGametest` server run are documented.
  3. CI uses Temurin Java 25, Gradle's action-managed cache, and failure report upload.
- **Expected tests:** Root build, root unit tests, and initialisation GameTest on a
  disposable environment. CI workflow review must confirm no secrets or Matcha fetch.

### FVR-004: Add AI architecture documents [Done]

- **Objective:** Record the vision, principles, architecture, workflow, save-safety
  rules, roadmap, and ticket structure so future agents can work from explicit design.
- **Dependencies:** None, although the documents must remain mutually consistent.
- **Explicit non-goals:** No gameplay implementation, speculative Java packages, final
  textures, or design decisions hidden in code.
- **Acceptance criteria:**
  1. Future agents are told to read the relevant design docs, ADRs, and system specs
     before changing a system.
  2. The architecture documents server authority, source boundaries, persistence
     placement, bounded work, and Matcha isolation.
  3. The roadmap and backlog distinguish complete foundation work from unstarted
     gameplay.
- **Expected tests:** Markdown link and heading review; manual consistency review
  against `docs/vision.md`, `docs/design-principles.md`, and `docs/architecture.md`.

### FVR-005: Add dependency/version lock documentation [Done]

- **Objective:** Make the approved versions and update policy visible and reproducible.
- **Dependencies:** FVR-001.
- **Explicit non-goals:** No dependency upgrade, floating version, automatic update,
  world conversion, or Matcha download during CI.
- **Acceptance criteria:**
  1. Minecraft, JDK, Loader, API, Loom, Gradle, JUnit, and Matcha versions match the
     verified baseline.
  2. Matcha's project, release, file, licence, and acquisition boundary are recorded.
  3. Version changes require a ticket, a baseline update, a full build/test cycle, and
     disposable-world dedicated-server validation.
- **Expected tests:** Review all version declarations against
  `docs/dependency-baseline.md`; run `./gradlew build` and the audit-tool tests when a
  real version change is later proposed.

### FVR-A001: Establish the art and texture pipeline [Done]

- **Objective:** Establish the editable art source tree, the production asset
  directories, a developer-only asset validation tool exposed as `./gradlew
  validateAssets`, and the documentation an artist needs to take one asset from source
  to a verified in-game texture.
- **Dependencies:** FVR-001, FVR-003, and the approved asset manifest and art direction
  in `docs/assets/`.
- **Explicit non-goals:** No gameplay item, block, screen, recipe, model, blockstate,
  translation, or system. No final artwork. No image-generation dependency in the
  Minecraft runtime. No vanilla Minecraft texture is downloaded or copied. The
  validation tool must never ship inside the mod JAR.
- **Acceptance criteria:**
  1. `art/` holds editable sources and `src/client/resources/assets/forever/` holds the
     exported production tree, with the split and both workflows documented.
  2. `./gradlew validateAssets` runs a pure-JDK tool from a separate `assetTools` source
     set and reports snake_case naming, supported image formats, dimensions, the 16x16
     default for item and ordinary block textures with manifest whitelisting,
     fully transparent images, missing texture references, duplicate asset IDs,
     unmanifested production PNGs, and manifest rows whose assets are missing.
  3. Rows with status `planned` are never rejected for having no file, so an unfinished
     asset cannot block the build.
  4. Exactly one intentionally obvious original placeholder texture exists and does not
     imitate Minecraft's built-in missing-texture graphic.
- **Expected tests:** Unit tests for a valid 16x16 PNG, incorrect dimensions, an
  uppercase filename, a fully transparent image, a production PNG missing from the
  manifest, and a missing texture reference, plus exit-code, duplicate-ID, planned-row,
  and tooling-marker cases; and a real `./gradlew validateAssets` run against the
  repository reporting CLEAN.

## Matcha acquisition and compatibility

### FVR-010: Fetch and pin Matcha scripts [Done]

- **Objective:** Provide deterministic developer tooling that fetches exactly Matcha
  Flavoured 1.12 and records or verifies its lock identity and checksum.
- **Dependencies:** FVR-005.
- **Explicit non-goals:** No automatic update, CI download, gameplay adapter, Matcha
  fork, or installation into a real world.
- **Acceptance criteria:**
  1. `./scripts/fetch-matcha.sh` selects Modrinth version ID `E9rngRfK` rather than
     `latest` and places the archive in the documented vendor location.
  2. The lock metadata records the release identity and SHA-256, and a mismatch fails
     with an actionable error.
  3. A repeat fetch is idempotent and cannot silently replace a changed pinned archive.
- **Expected tests:** Shell tests with network responses and archive fixtures, checksum
  mismatch tests, repeat-run tests, and manual review that CI never invokes the script.

### FVR-011: Install Matcha into a disposable development world [Done]

- **Objective:** Provide a guarded developer script for installing the pinned Matcha
  archive into a disposable dev world without touching a live save.
- **Dependencies:** FVR-010 and the world-save rules in `docs/world-save-safety.md`.
- **Explicit non-goals:** No live-world support, auto-update, world backup deletion,
  registry migration, or production modpack installer.
- **Acceptance criteria:**
  1. `./scripts/install-matcha-dev.sh` uses the pinned local archive and clearly
     identifies the disposable target.
  2. Missing, stale, or checksum-invalid input fails before changing the target.
  3. Re-running the installation does not duplicate files or leave an ambiguous pack
     version, and the script documents how to remove the disposable world.
- **Expected tests:** Installation into a temporary disposable world, missing-archive
  and invalid-lock cases, interrupted-copy recovery, and an idempotent second run.

### FVR-012: Build the Matcha audit CLI [Done]

- **Objective:** Create a standalone, deterministic CLI that inventories the pinned
  Matcha archive without making it a Gradle dependency of the mod.
- **Dependencies:** FVR-010 and FVR-005.
- **Explicit non-goals:** No gameplay interpretation, adapter implementation, Matcha
  mutation, network download, or inclusion in the Forever mod JAR.
- **Acceptance criteria:**
  1. The audit tool is a separate Gradle build with its own wrapper and test command.
  2. It reports archive paths, relevant metadata, and validation findings with stable
     ordering and explicit exit status.
  3. Versioned generated output is written under `generated/matcha/<version>/` without
     overwriting a different pinned release.
- **Expected tests:** CLI success and failure exit codes, deterministic repeated output,
  malformed archive handling, path traversal checks, and standalone `./gradlew test`
  from `tools/matcha-audit`.

### FVR-013: Add Matcha audit fixtures and tests [Done]

- **Objective:** Make audit behaviour testable without network access or licensed
  production material by using small synthetic archive fixtures.
- **Dependencies:** FVR-012.
- **Explicit non-goals:** No claim that fixtures represent every Matcha system, no
  official audit result, and no production adapter behaviour.
- **Acceptance criteria:**
  1. Fixtures cover valid entries, metadata, malformed paths, duplicate records, and
     unsupported or incomplete input.
  2. Tests prove stable ordering, bounded resource handling, useful diagnostics, and
     deterministic report serialisation.
  3. The fixture suite contains no unapproved third-party or licensed Matcha content.
- **Expected tests:** The complete audit-tool unit suite, repeated-output comparison,
  malformed-input tests, and a review of fixture licensing and provenance.

### FVR-014: Generate the first official Matcha audit [Next]

- **Objective:** Fetch the exact pinned Matcha release once as a deliberate developer
  operation and publish the first official deterministic inventory and audit report.
- **Dependencies:** FVR-010, FVR-011, FVR-012, and FVR-013.
- **Explicit non-goals:** No gameplay integration, system override, adapter code,
  Matcha fork, or classification decision hidden inside the report.
- **Acceptance criteria:**
  1. The input archive matches version ID `E9rngRfK` and its recorded SHA-256.
  2. The audit is reproducible from the pinned input and produces reports under
     `generated/matcha/1.12/` with a documented generation command and timestamp policy.
  3. The report identifies unknowns, licensing boundaries, and findings that require
     human classification rather than guessing at semantics.
- **Expected tests:** Fetch checksum verification, complete audit-tool tests, two audit
  runs compared for deterministic content, and a manual review of the generated report.

### FVR-015: Manually classify Matcha systems [Planned]

- **Objective:** Classify each audited Matcha system as keep, extend, override, replace
  eventually, or undecided, with evidence and an owner for the next decision.
- **Dependencies:** FVR-014 and the relevant vision, principles, ADRs, and system specs.
- **Explicit non-goals:** No implementation of classified gameplay, no silent change to
  approved design decisions, and no claim that classification is compatibility code.
- **Acceptance criteria:**
  1. Every relevant audited system has exactly one current classification and rationale.
  2. Evidence points to audited paths or observed behaviour without leaking private
     Matcha identifiers into unrelated Forever code.
  3. Conflicts with Forever principles are recorded as decisions or blockers, not
     hidden by changing the specification.
- **Expected tests:** Completeness check for the audit inventory, link/path review,
  classification consistency review, and an ADR review for each changed decision.

### FVR-016: Define stable Matcha adapter interfaces [Planned]

- **Objective:** Define the smallest stable compatibility interfaces that translate
  Matcha capabilities into Forever concepts behind `dev.forever.compat.matcha`.
- **Dependencies:** FVR-015, FVR-005, and the adapter architecture ADR.
- **Explicit non-goals:** No hardcoded Matcha internals outside the adapter package, no
  gameplay system implementation, no fork, and no required Matcha runtime for vanilla
  Forever startup.
- **Acceptance criteria:**
  1. Interfaces expose stable Forever concepts and capability checks rather than raw
     scoreboard, function, advancement, or disguised-item identifiers.
  2. Absent, malformed, and unsupported Matcha input has a safe no-op or explicit
     diagnostic path that does not crash ordinary Minecraft play.
  3. Adapter boundaries and ownership are documented with versioned contracts.
- **Expected tests:** Adapter unit tests against fixtures, absent-Matcha startup tests,
  unsupported-version tests, and a source scan proving private identifiers stay in the
  permitted package.

### FVR-017: Add Matcha version compatibility detection [Planned]

- **Objective:** Detect whether the installed Matcha release matches the supported lock
  identity and expose actionable capability and incompatibility diagnostics.
- **Dependencies:** FVR-010, FVR-014, and FVR-016.
- **Explicit non-goals:** No automatic download, update, replacement of a live pack,
  migration of arbitrary Matcha versions, or gameplay balancing.
- **Acceptance criteria:**
  1. Exact supported version and checksum are accepted, while mismatches are reported
     before dependent features run.
  2. A missing or unsupported version degrades safely and explains the disabled
     capability to a developer or server operator.
  3. Detection is bounded, deterministic, and isolated behind the compatibility layer.
- **Expected tests:** Supported, missing, stale, corrupt, and future-version fixtures;
  dedicated-server startup with and without Matcha; and deterministic diagnostics.

## Equipment and condition vertical slice

### FVR-100: Define item identity and condition schema [Planned]

- **Objective:** Define the versioned per-`ItemStack` identity, condition,
  craftsmanship, and specialisation data required for durable equipment.
- **Dependencies:** FVR-002, FVR-004, FVR-005, and the equipment system specification.
- **Explicit non-goals:** No repair UI, workshop block, reforge process, balance tuning,
  or permanent durability behaviour change until the schema is accepted.
- **Acceptance criteria:**
  1. The schema has an explicit version, codec or serializer, field validation,
     migration strategy, and documented failure behaviour.
  2. Identity survives dropping, trading, container storage, and ordinary item-stack
     operations without becoming a global mutable record.
  3. Condition and balance fields are separated so tuning can live in data/configuration.
- **Expected tests:** Codec round trips, invalid-field rejection, prior-version
  migration fixtures, unknown-field handling, and item-stack copy/drop/container tests.

### FVR-101: Prevent permanent durability destruction [Planned]

- **Objective:** Replace ordinary zero-durability deletion with a safe transition to a
  retained broken item while preserving vanilla use where no Forever condition applies.
- **Dependencies:** FVR-100 and the approved equipment ADR.
- **Explicit non-goals:** No field repair, workshop repair, reforge, custom visual art,
  or degradation of ordinary non-equipment Minecraft play.
- **Acceptance criteria:**
  1. A supported item reaching zero condition remains in the owning inventory or world
     and cannot be silently deleted by ordinary exhaustion.
  2. Unaffected vanilla items retain their normal behaviour and normal block placement
     remains available.
  3. The server owns the transition and records an actionable diagnostic on invalid
     condition data.
- **Expected tests:** Server-side break transition, dropped-item transition,
  container/trade transition, invalid-state handling, and a regression test for normal
  vanilla durability behaviour.

### FVR-102: Add the broken-item state [Planned]

- **Objective:** Represent a broken, unusable item state that retains identity and is
  understandable through non-colour cues until repair.
- **Dependencies:** FVR-100 and FVR-101.
- **Explicit non-goals:** No repair recipe, workshop registration, reforge path, or
  final texture before the asset manifest and gameplay need are approved.
- **Acceptance criteria:**
  1. Broken state is encoded in validated persistent data and rejects use paths that
     would bypass the repair requirement.
  2. Tooltip, text, icon shape, or another non-colour cue explains the state and how to
     recover it.
  3. Identity, enchantments, prior path progress, and other allowed data remain intact.
- **Expected tests:** State persistence, use denial, tooltip/translation checks,
  colour-independent presentation review, and server-authority tests for client packets.

### FVR-103: Add field repair [Planned]

- **Objective:** Provide a bounded, server-authoritative field repair action that
  restores partial condition without deleting item identity.
- **Dependencies:** FVR-102, the equipment system specification, and the balance data
  contract.
- **Explicit non-goals:** No full restoration, workshop, reforge, max-durability decay
  spiral, repetitive mastery XP, or client-only repair outcome.
- **Acceptance criteria:**
  1. A valid field process restores a data-driven partial amount and consumes only the
     approved costs on the server.
  2. Invalid item, insufficient resource, distance, or permission cases fail without
     changing the item or consuming resources.
  3. Player-facing instructions include translations, tooltip/UI explanation, and a
     Field Guide entry.
- **Expected tests:** Valid and invalid server actions, resource atomicity, disconnect
  and retry handling, save/reload, duplicate-request resistance, and UI/guide content.

### FVR-104: Add workshop repair [Planned]

- **Objective:** Add a workshop repair process that restores full condition through an
  explicit server-side interaction and data-driven cost.
- **Dependencies:** FVR-103 and the approved workshop system specification.
- **Explicit non-goals:** No settlement graph, Mason career, reforge specialisation,
  permanent chunk loading, or removal of ordinary anvil and vanilla play.
- **Acceptance criteria:**
  1. A valid workshop interaction restores full condition without changing identity or
     deleting the item.
  2. Cost, eligibility, and timing are data-driven and validated on the server.
  3. The process is understandable through translations, tooltip/UI explanation,
     Field Guide text, and recipe-viewer integration when a recipe is involved.
- **Expected tests:** GameTest for interaction and full repair, invalid/duplicate request
  tests, restart persistence, client/server authority checks, and recipe/guide checks.

### FVR-105: Add the reforge path [Planned]

- **Objective:** Add the approved reforge operation that restores full condition and
  permits an item to switch its active specialisation path without losing identity or
  prior path progress.
- **Dependencies:** FVR-100, FVR-104, the item-specialisation ADR, and the reforge
  system specification.
- **Explicit non-goals:** No mastery registry implementation, recurring tax, XP loss,
  re-levelling, permanent destruction, or unapproved path balance.
- **Acceptance criteria:**
  1. An item may know several paths but has only one active path at a time.
  2. Reforging preserves item identity and inactive path progress, restores full
     condition, and either commits atomically or changes nothing.
  3. The operation has versioned persistent data, validated costs, failure behaviour,
     and in-game explanation.
- **Expected tests:** Multi-path persistence, successful and rejected reforges,
  duplicate/disconnect attempts, interrupted save handling, restart behaviour, and
  client/server authority tests.

### FVR-106: Add equipment persistence and duplication GameTests [Planned]

- **Objective:** Prove the complete equipment vertical slice survives world lifecycle
  events and cannot create or destroy items through ordinary interaction races.
- **Dependencies:** FVR-100 through FVR-105.
- **Explicit non-goals:** No new equipment feature or balance change beyond the tested
  slice.
- **Acceptance criteria:**
  1. Identity, condition, broken state, path progress, repair, and reforge survive save,
     reload, drop, pickup, container movement, trade, and server restart.
  2. Repeated packets, disconnects, simultaneous interactions, and reloads cannot
     duplicate an item or permanently delete it.
  3. GameTests prove the server decides outcomes and the client is only a projection.
- **Expected tests:** Dedicated-server GameTests for persistence, duplication, restart,
  authority boundaries, and vanilla regression, plus unit tests for codecs and migration.

## Knowledge and Field Guide

### FVR-200: Build the Field Guide framework [Planned]

- **Objective:** Create the in-game documentation framework that can explain every
  player-facing Forever mechanic without requiring an external wiki.
- **Dependencies:** FVR-002, FVR-004, and the client architecture specification.
- **Explicit non-goals:** No mastery, equipment, settlement, or Matcha gameplay; no
  final art before the asset manifest; no client authority over gameplay.
- **Acceptance criteria:**
  1. Entries have stable IDs, versioned content metadata, translations, categories, and
     a bounded rendering/search contract.
  2. The client renders server-synchronised summaries and cannot grant outcomes.
  3. Missing or outdated entries fail visibly and safely rather than showing incorrect
     instructions.
- **Expected tests:** Entry schema validation, translation fallback, bounded payload and
  rendering tests, dedicated-server absence of client references, and UI smoke tests.

### FVR-201: Add searchable Field Guide entries [Planned]

- **Objective:** Make approved Field Guide content searchable and navigable by topic,
  prerequisite, and recipe or system reference.
- **Dependencies:** FVR-200.
- **Explicit non-goals:** No hidden quest gating, external web links as required
  instructions, unbounded text or result payloads, or speculative future entries.
- **Acceptance criteria:**
  1. Search is deterministic, bounded, and handles no-result and malformed-entry cases.
  2. Results explain known processes without hiding required instructions behind
     discovery state.
  3. Entries have translations and a non-colour-only status presentation.
- **Expected tests:** Search ranking and limit tests, malformed-content tests, client
  rendering tests, translation coverage, and payload-size checks.

### FVR-202: Add Matcha early-progression entries [Planned]

- **Objective:** Document the approved early Matcha progression in the Field Guide
  using stable Forever adapter concepts and audited evidence.
- **Dependencies:** FVR-014, FVR-015, FVR-016, and FVR-200.
- **Explicit non-goals:** No Matcha internals outside the adapter, no gameplay override,
  no unsupported claims about unaudited behaviour, and no external-wiki dependency.
- **Acceptance criteria:**
  1. Each entry maps to an audited and classified capability or is explicitly marked
     unknown.
  2. The instructions, prerequisites, failure states, translations, and relevant
     recipe links are inspectable in game.
  3. Removing or disabling Matcha does not crash the base client or corrupt entries.
- **Expected tests:** Fixture-backed entry validation, translation and link checks,
  absent/unsupported Matcha rendering, and a manual audit-to-entry trace review.

### FVR-203: Integrate the recipe viewer [Planned]

- **Objective:** Expose relevant Forever and approved Matcha-related recipes through a
  recipe-viewer integration without making an optional viewer a hard dependency.
- **Dependencies:** FVR-200, FVR-201, FVR-202, and the optional-adapter contract.
- **Explicit non-goals:** No recipe invention, recipe balance changes, hard dependency
  on one viewer, or server-side client UI logic.
- **Acceptance criteria:**
  1. Present recipes and usages have stable links from the Field Guide where applicable.
  2. The integration is optional and has a safe no-op when the viewer is absent.
  3. Recipe data, translations, and displayed prerequisites agree with server truth.
- **Expected tests:** Viewer-present and viewer-absent client tests, recipe link
  completeness, translation checks, and a dedicated-server classpath check.

### FVR-204: Document campfire processes [Planned]

- **Objective:** Add clear Field Guide documentation for approved campfire processes,
  including inputs, timing, outputs, safety, and failure states.
- **Dependencies:** FVR-200 and the relevant process specification.
- **Explicit non-goals:** No recipe changes, new campfire mechanics, final textures, or
  hidden instructions that require reading an external guide.
- **Acceptance criteria:**
  1. Every documented process matches an approved recipe or server mechanic.
  2. The entry shows inputs, outputs, conditions, and recovery from interruption in
     translated, inspectable text.
  3. Recipe-viewer integration is present when the process has a recipe.
- **Expected tests:** Content/schema validation, recipe cross-check, translation
  coverage, interruption wording review, and in-game navigation smoke test.

## Mastery

### FVR-300: Add the mastery registry [Planned]

- **Objective:** Define a stable registry for mastery paths, capabilities, prerequisites,
  and data-driven balance values.
- **Dependencies:** FVR-004, FVR-005, FVR-015, and the mastery system specification.
- **Explicit non-goals:** No repetitive-action XP, player persistence, loadout UI,
  Prospector implementation, or final mastery art.
- **Acceptance criteria:**
  1. Paths have stable IDs, validated definitions, explicit dependencies, and bounded
     capability descriptions.
  2. Progression thresholds and modifiers live in data/configuration where practical.
  3. Invalid or duplicate definitions fail with actionable diagnostics before gameplay
     outcomes are evaluated.
- **Expected tests:** Registry load and validation, duplicate/missing dependency tests,
  malformed data tests, deterministic ordering, and data reload checks.

### FVR-301: Add persistent player mastery state [Planned]

- **Objective:** Store player mastery progress as a versioned persistent attachment or
  component that survives dimensions, save/reload, and server restart.
- **Dependencies:** FVR-300, FVR-004, and the persistent-data requirements in
  `docs/world-save-safety.md`.
- **Explicit non-goals:** No loadout switching, XP source implementation, recurring
  tax, XP loss, or client-owned progress.
- **Acceptance criteria:**
  1. The format defines schema version, codec/serializer, validation, migration, and
     explicit failure and recovery behaviour.
  2. Progress is permanent and inactive paths retain their levels.
  3. Server state remains authoritative and synchronisation is versioned and bounded.
- **Expected tests:** Codec round trips, prior-schema migration, invalid-data handling,
  save/reload, dimension transfer, restart, duplicate-write, and authority tests.

### FVR-302: Add one Focus and two Supporting slots [Planned]

- **Objective:** Represent the mastery loadout contract with exactly one Focus slot and
  two Supporting slots while retaining progress for inactive paths.
- **Dependencies:** FVR-301 and the mastery loadout ADR.
- **Explicit non-goals:** No switching interaction, rest requirement implementation,
  re-levelling, XP loss, or arbitrary additional slots.
- **Acceptance criteria:**
  1. A valid loadout contains one Focus and two Supporting assignments, with explicit
     validation for duplicates and incompatible paths.
  2. Inactive progress remains stored and has no hidden reset or recurring tax.
  3. The loadout summary is a bounded server projection and is explainable in the
     Field Guide/UI when exposed.
- **Expected tests:** Valid/invalid slot combinations, persistence, duplicate request
  handling, bounded synchronisation, and client/server authority tests.

### FVR-303: Add rest-based mastery switching [Planned]

- **Objective:** Allow players to change active mastery assignments only through the
  approved rest, home, or workplace interaction.
- **Dependencies:** FVR-302, the rest/switching ADR, and the relevant interaction spec.
- **Explicit non-goals:** No instant hotbar switching, XP loss, re-levelling, recurring
  tax, or settlement implementation beyond the required interaction contract.
- **Acceptance criteria:**
  1. The server validates rest/home/workplace conditions and commits a switch
     atomically.
  2. Invalid, repeated, disconnected, or client-forged requests do not alter state or
     consume a cost.
  3. The process and its reason are translated, explained in UI/tooltips, and recorded
     in the Field Guide.
- **Expected tests:** GameTests for valid rest and invalid locations, reconnect/retry and
  duplicate requests, save/reload and restart, and forged client packet rejection.

### FVR-304: Add the Prospector prototype [Planned]

- **Objective:** Prototype a Prospector path whose progression rewards discovery,
  breadth, technique, and completed projects rather than repetitive-action counters.
- **Dependencies:** FVR-300 through FVR-303, the mastery XP ADR, and the Prospector
  system specification.
- **Explicit non-goals:** No generic mastery catalogue, raw “mine N blocks” XP, final
  balance, permanent chunk loading, or unrelated specialist paths.
- **Acceptance criteria:**
  1. The prototype has a bounded, data-driven set of approved accomplishment sources.
  2. No repetitive-action mastery XP is introduced without an explicitly approved ADR.
  3. The active path, feedback, translations, tooltip/UI explanation, and Field Guide
     entry agree with server-authoritative outcomes.
- **Expected tests:** XP-source allowlist tests, duplicate-event and replay tests,
  persistence/restart, client-forgery rejection, and content/guide checks.

### FVR-305: Add mastery persistence and duplication tests [Planned]

- **Objective:** Prove registry-backed mastery state, loadout slots, switching, and
  Prospector progress remain correct across lifecycle and packet races.
- **Dependencies:** FVR-301 through FVR-304.
- **Explicit non-goals:** No new mastery path or progression balance changes.
- **Acceptance criteria:**
  1. Progress, active slots, and inactive paths survive save/reload, dimension change,
     disconnect, and server restart.
  2. Replayed accomplishments, duplicate packets, and concurrent switches cannot grant
     duplicate progress or lose valid progress.
  3. GameTests prove the server decides all outcomes and bounded projections are all the
     client receives.
- **Expected tests:** Dedicated-server GameTests for persistence, duplication, restart,
  and authority boundaries, plus codec and migration unit tests.

## Settlement

### FVR-400: Add the settlement Charter [Planned]

- **Objective:** Define the versioned settlement Charter that establishes ownership,
  name, membership, and governance without imposing an architectural style.
- **Dependencies:** FVR-004, the settlement ADRs, and the settlement system specification.
- **Explicit non-goals:** No building scan, residence validation, villager career,
  automatic village conversion, or aesthetic scoring.
- **Acceptance criteria:**
  1. Charter data has schema version, codec/serializer, validation, migration, and
     explicit invalid-data behaviour.
  2. A Charter identifies a settlement without a permanent chunk-loading requirement
     and does not judge theme or appearance.
  3. Server authority, ownership changes, and failure recovery are documented.
- **Expected tests:** Codec and migration tests, invalid Charter tests, restart/save
  tests, permission boundary tests, and a no-style-enforcement review.

### FVR-401: Add building selection and registration [Planned]

- **Objective:** Let a player explicitly select and register functional buildings with a
  settlement using bounded, cached registration rather than world-wide discovery.
- **Dependencies:** FVR-400 and the registration system specification.
- **Explicit non-goals:** No unbounded scans, forced style, permanent chunk loading,
  automatic registration of every nearby structure, or worker simulation.
- **Acceptance criteria:**
  1. Registration is explicit, server-validated, bounded, and removable through a safe
     versioned operation.
  2. Registered references survive save/reload and stale references fail safely without
     deleting unrelated world content.
  3. Normal block placement and ordinary Minecraft building remain possible without
     registration.
- **Expected tests:** Explicit registration GameTests, bounded-query tests, stale
  reference handling, persistence/restart, and vanilla-building regression tests.

### FVR-402: Add the residence validator [Planned]

- **Objective:** Validate residence function using enclosure, beds, workstations,
  storage, safety, and space without enforcing a theme or blueprint.
- **Dependencies:** FVR-401 and the residence validation specification.
- **Explicit non-goals:** No aesthetic score, required architectural style, unbounded
  block scan, arbitrary off-screen villager death, or chunk force-loading.
- **Acceptance criteria:**
  1. Validation uses bounded registered areas and cached results with clear invalidation.
  2. Results explain which functional checks passed or failed and use non-colour cues.
  3. Validation is server-authoritative and does not prevent ordinary vanilla building.
- **Expected tests:** Functional pass/fail fixtures, bounds and cache invalidation,
  malformed registration handling, client projection checks, and dedicated-server tests.

### FVR-403: Add the settlement building graph [Planned]

- **Objective:** Represent settlements as a versioned graph of registered buildings,
  workplaces, residences, infrastructure, and outposts.
- **Dependencies:** FVR-400 through FVR-402 and the settlement graph ADR.
- **Explicit non-goals:** No circle-radius settlement model, continuous world scan,
  permanent chunk loading, logistics shipments, or career simulation.
- **Acceptance criteria:**
  1. Graph records define schema version, codec/serializer, validation, migration, and
     failure behaviour for missing or stale nodes and edges.
  2. Attachment and distance rules are data-driven, bounded, and cached.
  3. The graph survives save/reload and server restart without silently dropping nodes.
- **Expected tests:** Graph codec/migration, node and edge validation, bounded update
  budget, stale reference recovery, persistence, restart, and authority tests.

### FVR-404: Add existing-village import [Planned]

- **Objective:** Provide an explicit Charter/import workflow for incorporating an
  existing natural Minecraft village without erasing its vanilla behaviour.
- **Dependencies:** FVR-400, FVR-401, FVR-402, FVR-403, and the village import ADR.
- **Explicit non-goals:** No automatic world-wide village scan, forced conversion,
  removal of vanilla buildings, arbitrary NPC deletion, or style requirements.
- **Acceptance criteria:**
  1. Import is explicit, previewable, bounded, and cancellable before committing graph
     records.
  2. Existing blocks, entities, and identifiers are preserved unless a migration plan
     explicitly covers a change.
  3. Partial or invalid import fails safely and leaves the original world and Charter
     recoverable.
- **Expected tests:** Disposable-world import GameTests, preview/cancel, partial failure
  rollback, persistence/restart, duplicate-import prevention, and vanilla regression.

### FVR-405: Add a settlement migration prototype [Planned]

- **Objective:** Prove one versioned settlement schema migration and its operational
  backup, verification, and recovery procedure before broader settlement persistence.
- **Dependencies:** FVR-400 through FVR-404 and the world-save migration procedure.
- **Explicit non-goals:** No broad schema rewrite, live-world migration, unbounded
  repair tool, or release of persistent settlement gameplay without backup procedures.
- **Acceptance criteria:**
  1. A prior fixture migrates to the new schema through validated, deterministic steps.
  2. Failed, interrupted, and unknown-version migrations preserve recoverable input and
     produce actionable diagnostics.
  3. The procedure documents backup, detection, migration, verification, rollback, and
     dedicated-server startup checks on disposable copies.
- **Expected tests:** Prior-version fixtures, invalid and interrupted migrations,
  rollback/recovery tests, save/reload, restart, and dedicated-server smoke tests.

## Mason vertical slice

### FVR-500: Add the masonry workshop [Planned]

- **Objective:** Add the first approved masonry workshop interaction as the physical
  anchor for the Mason vertical slice.
- **Dependencies:** FVR-104, FVR-200, FVR-203, and the workshop/Mason specifications.
- **Explicit non-goals:** No Mason career ranks, Obol economy, settlement-wide worker
  simulation, final textures before approval, or forced replacement of vanilla crafting.
- **Acceptance criteria:**
  1. Workshop registration and state use the approved source boundary, server authority,
     versioned persistent format, codec, validation, migration, and failure behaviour.
  2. The workshop provides optional convenience while ordinary Minecraft crafting and
     block placement remain possible.
  3. Interaction instructions include translation, tooltip/UI explanation, Field Guide
     text, and recipe-viewer links when applicable.
- **Expected tests:** Workshop GameTests, invalid and duplicate interactions, save/reload
  and restart, absent optional integration, authority checks, and content tests.

### FVR-501: Add Mason persistent career state [Planned]

- **Objective:** Store Mason career rank, techniques, mentors, apprenticeships, and
  related knowledge in a versioned persistent entity or player structure.
- **Dependencies:** FVR-500, FVR-301, and the career persistence specification.
- **Explicit non-goals:** No rank progression, bulk catalogue, Obol purse, arbitrary
  off-screen death, or unversioned career blob.
- **Acceptance criteria:**
  1. State defines schema version, codec/serializer, validation, migration, and failure
     behaviour for missing or invalid career records.
  2. Institutional knowledge and personal knowledge follow the approved mortality and
     teaching rules rather than a hidden global singleton.
  3. Server state survives save/reload and restart and has bounded client summaries.
- **Expected tests:** Codec and migration fixtures, entity/player lifecycle, restart,
  invalid state, death/knowledge boundary, duplicate-write, and authority tests.

### FVR-502: Add Mason Apprentice rank [Planned]

- **Objective:** Implement the first Mason rank with approved learning and workshop
  capabilities.
- **Dependencies:** FVR-501 and the Apprentice system specification.
- **Explicit non-goals:** No Journeyman or Master capability, repetitive-action mastery
  XP, final rank balance, or unrelated career systems.
- **Acceptance criteria:**
  1. Apprentice capabilities and thresholds are data-driven and server-validated.
  2. Learning rewards approved breadth, technique, mentorship, or projects rather than
     raw repetition counters.
  3. Player-facing rank rules are translated, explained in UI/tooltips, and in the
     Field Guide.
- **Expected tests:** Rank validation, approved-source tests, duplicate/replay handling,
  persistence/restart, forged-client rejection, and content checks.

### FVR-503: Add Mason Journeyman rank [Planned]

- **Objective:** Add the Journeyman rank and its approved additional masonry capability
  without breaking Apprentice data or normal play.
- **Dependencies:** FVR-502 and the Journeyman system specification.
- **Explicit non-goals:** No Master rank, economy, worker network, repetitive XP, or
  migration shortcut that discards Apprentice knowledge.
- **Acceptance criteria:**
  1. Promotion is an explicit server-authoritative transition with data-driven criteria.
  2. Previous rank progress and identity remain valid across save/reload and restart.
  3. The capability is optional convenience and includes all required player-facing
     explanation and recipe links.
- **Expected tests:** Promotion success/failure, replay and duplicate promotion,
  migration, persistence/restart, client authority, and content tests.

### FVR-504: Add Mason Master rank [Planned]

- **Objective:** Add the Master rank as the top of this vertical slice with bounded,
  approved masonry capabilities.
- **Dependencies:** FVR-503 and the Master system specification.
- **Explicit non-goals:** No new profession catalogue, settlement worker simulation,
  unlimited automation, final economy rebalance, or degradation of vanilla building.
- **Acceptance criteria:**
  1. Master promotion and capabilities are validated on the server and configured in
     data where practical.
  2. The rank does not create unbounded work, permanent chunk loading, or a mandatory
     replacement for ordinary Minecraft play.
  3. Rank state, instructions, failure cases, translation, Field Guide, and recipe
     viewer output are consistent.
- **Expected tests:** Promotion and capability GameTests, performance/budget checks,
  persistence/restart, duplicate requests, forged-client tests, and documentation checks.

### FVR-505: Add the bulk block catalogue [Planned]

- **Objective:** Define a data-driven catalogue of approved bulk masonry blocks and
  operations for the Mason workshop.
- **Dependencies:** FVR-500, FVR-504, and the catalogue data specification.
- **Explicit non-goals:** No unbounded batch size, free resources, removal of vanilla
  recipes, final textures, or catalogue entries without migration aliases.
- **Acceptance criteria:**
  1. Entries have stable IDs, validated inputs/outputs, bounded batch sizes, and
     data-driven costs and rates.
  2. Catalogue changes preserve registered content or include a tested migration plan.
  3. The interface explains previews, costs, errors, and completion without relying on
     colour alone.
- **Expected tests:** Data validation, batch-bound tests, atomic output/input handling,
  interruption/retry, persistence/restart, duplication tests, and recipe/guide checks.

### FVR-506: Add the Obol purse [Planned]

- **Objective:** Add the approved hybrid Obol economy with physical transferable items
  and a withdrawable Coin Purse balance that does not consume many inventory slots.
- **Dependencies:** FVR-500, FVR-501, the economy ADR, and the purse persistence spec.
- **Explicit non-goals:** No mandatory shop pricing, arbitrary item deletion, unbounded
  balance, cross-dimensional universal warehouse, or client-owned currency.
- **Acceptance criteria:**
  1. Purse data has schema version, codec/serializer, validation, migration, failure
     behaviour, and atomic deposit/withdraw operations.
  2. Physical Obols remain transferable and the balance is withdrawable according to the
     approved rules.
  3. The server prevents duplicate deposits, withdrawals, replayed requests, and
     overflow while UI/tooltips and the Field Guide explain the state.
- **Expected tests:** Currency codec/migration, atomic transaction GameTests, restart,
  disconnect/retry, duplication and overflow tests, cross-dimension persistence, and
  client/server authority checks.

### FVR-507: Add Mason persistence and duplication GameTests [Planned]

- **Objective:** Prove the workshop, career ranks, catalogue operations, and Obol purse
  survive lifecycle events and cannot duplicate blocks, items, or currency.
- **Dependencies:** FVR-500 through FVR-506.
- **Explicit non-goals:** No new Mason capability or economy balance change.
- **Acceptance criteria:**
  1. Workshop, career, catalogue, and purse state survive save/reload and dedicated
     server restart with schema and migration evidence.
  2. Replayed, concurrent, interrupted, and disconnected operations are atomic and do
     not duplicate or permanently delete resources.
  3. GameTests cover client/server authority and ordinary vanilla crafting/building.
- **Expected tests:** Dedicated-server persistence, restart, duplication, rollback,
  overflow, and authority GameTests plus codec and migration unit tests.

## Storage and logistics

### FVR-600: Add the warehouse controller [Planned]

- **Objective:** Define a server-authoritative warehouse controller for registered
  storage infrastructure with bounded access and versioned world-scoped state.
- **Dependencies:** FVR-403, FVR-500, the logistics ADR, and the warehouse specification.
- **Explicit non-goals:** No cross-dimensional universal warehouse, unbounded inventory
  scan, container nesting, or client-side item movement authority.
- **Acceptance criteria:**
  1. Controller state has schema version, codec/serializer, validation, migration, and
     explicit failure behaviour.
  2. Access is through registered infrastructure and bounded operations, not a global
     scan or permanent chunk loading.
  3. Inventory mutations are atomic, server-authoritative, and explainable in UI and
     the Field Guide.
- **Expected tests:** Controller codec/migration, bounded access, atomic move, save/
  reload, restart, duplicate request, and client-forgery GameTests.

### FVR-601: Add inventory indexing [Planned]

- **Objective:** Index registered warehouse contents for bounded search and retrieval
  without scanning every container or chunk on every request.
- **Dependencies:** FVR-600 and the indexing specification.
- **Explicit non-goals:** No stale index treated as authoritative, unbounded rebuild on
  the server tick, container nesting, or cross-dimensional magic storage.
- **Acceptance criteria:**
  1. Index entries have a versioned codec, validation, rebuild/invalidation strategy,
     and safe failure behaviour.
  2. Queries have explicit limits and use cached or incrementally maintained results.
  3. The server revalidates authoritative inventory before committing a transfer.
- **Expected tests:** Index round trip/migration, stale and corrupt index rebuild,
  query bounds, concurrent mutation, restart, duplication, and authority tests.

### FVR-602: Add the search terminal [Planned]

- **Objective:** Provide a client projection for searching indexed warehouse contents
  and requesting bounded, server-validated transfers.
- **Dependencies:** FVR-600, FVR-601, FVR-200, and FVR-201.
- **Explicit non-goals:** No client-owned inventory, unbounded result lists, hidden
  server scans, forced warehouse use, or degraded ordinary inventory interaction.
- **Acceptance criteria:**
  1. Search requests and responses are versioned, bounded, and safe when the terminal,
     index, or optional client integration is absent.
  2. The server validates permissions, current inventory, destination capacity, and
     atomicity before moving anything.
  3. Empty, stale, denied, and partial results have translated non-colour explanations
     and Field Guide documentation.
- **Expected tests:** Payload bounds, stale-result rejection, atomic transfer, duplicate
  request, disconnect/retry, restart, and client/server authority tests.

### FVR-603: Add the Traveler's Cache [Planned]

- **Objective:** Add the personal Traveler's Cache with nine slots initially and an
  approved expansion to eighteen slots, without allowing container nesting.
- **Dependencies:** FVR-600, the Traveler's Cache ADR, and the item persistence spec.
- **Explicit non-goals:** No universal warehouse, cross-dimensional shared inventory,
  nested containers, forced use, or removal of ordinary inventory slots.
- **Acceptance criteria:**
  1. Cache contents and expansion state have versioned persistent data, codec,
     validation, migration, and recovery behaviour.
  2. Capacity is exactly nine slots initially and eighteen after the approved expansion,
     with server-side enforcement and atomic movement.
  3. The cache is personal, its limits are explained in UI/tooltips and the Field Guide,
     and normal inventory remains usable.
- **Expected tests:** Capacity boundary, save/reload, dimension transfer, restart,
  duplicate movement, disconnect/retry, migration, and authority GameTests.

### FVR-604: Prevent container nesting [Planned]

- **Objective:** Enforce the invariant that a container cannot be placed inside another
  container, including warehouse, cache, trade, drop, and automation paths.
- **Dependencies:** FVR-600, FVR-601, FVR-602, FVR-603, and the container safety ADR.
- **Explicit non-goals:** No arbitrary item deletion, inventory nerf, new container type,
  or exception that creates an unbounded recursive inventory.
- **Acceptance criteria:**
  1. Every insertion path rejects nested containers before mutation and reports an
     actionable reason.
  2. Existing valid items remain recoverable, and any legacy nested state has a tested
     migration or quarantine path rather than silent deletion.
  3. The rule is server-authoritative, bounded, translated, and documented in the Field
     Guide where player-facing.
- **Expected tests:** Direct, drop, trade, warehouse, cache, automation, reload, and
  migration cases; duplicate and rollback tests; and a dedicated-server authority test.

## Later themes

The following are intentionally summarised rather than expanded into implementation
tickets. Each must receive the same five fields before work starts: generic food traits,
player shops, apprenticeships, workers, logistics expansion, roads, rail, journey
skipping, seasons, regional economies, animal bonding, and the world chronicle.

They are all **Planned and not started**. Their future tickets must preserve the current
principles: solo viability, data-driven balance, bounded world work, no permanent
passive chunk loading, versioned persistent data, no arbitrary off-screen villager
death, and no degradation of ordinary Minecraft play.

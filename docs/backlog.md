# Many Roads Home backlog

This is the active ticket queue for the modpack-first product. The working product is the Many Roads Home pack, not a large custom mod. The repository's existing prototype backlog is preserved verbatim in [`backlog-prototype-archive.md`](backlog-prototype-archive.md) under [ADR 0034](adr/0034-preserve-existing-custom-code.md). Its old implementation status is historical evidence and is not completion of a current pack milestone.

Work on one ticket at a time. `Done` in this file means that the documentation or decision gate is complete. It does not claim that gameplay is implemented. `Next` is the next unblocked ticket. `Planned` is not permission to implement it opportunistically. The first implementation tickets are the compatibility spikes `LAB-01` through `LAB-12`. No custom gameplay system may begin before those spikes have produced evidence.

Every ticket has the same contract:

- objective,
- dependencies,
- authoritative sources to inspect,
- existing-mod candidates,
- custom-code gate,
- explicit non-goals,
- acceptance criteria,
- test requirements, and
- removal or migration risks.

Candidate names are leads for a compatibility investigation, not claims that they support Minecraft 26.2. The spike must inspect an official project page, source repository, release metadata, licence, and supported loader evidence. If evidence is unavailable, record `NEEDS_MORE_EVIDENCE` rather than inferring compatibility.

## M0: Modpack foundation

M0 records the product and preservation contract. It contains no gameplay implementation. The first executable work is the M1 compatibility laboratory queue.

### MRH-000: Record the modpack-first pivot contract [Done]

- **Objective:** Establish Many Roads Home as the working product identity, define modpack ownership, select Packwiz as the composition authority, preserve the existing custom source, and make the compatibility-first queue explicit.
- **Dependencies:** The existing foundation documents and the shared pivot brief.
- **Authoritative sources to inspect:** `.pivot-context.md`, `docs/vision.md`, `docs/design-principles.md`, `docs/architecture.md`, `docs/dependency-baseline.md`, `docs/world-save-safety.md`, ADRs 0001 through 0022, and ADRs 0023 through 0034.
- **Existing-mod candidates:** None. This is a documentation gate. Packwiz and Global Packs are recorded as later investigation subjects rather than implemented dependencies.
- **Custom-code gate:** No code. The ticket is complete only when the companion remains subject to ADRs 0026 and 0033.
- **Explicit non-goals:** No gameplay implementation, pack export, dependency upgrade, Java rename, third-party JAR, final asset, or world launch.
- **Acceptance criteria:**
  1. The twelve new ADRs, name and identity document, active backlog, archived prototype backlog, and pivot roadmap exist.
  2. The active queue identifies `LAB-01` through `LAB-12` as the first implementation work.
  3. Existing source and third-party material are preserved and ownership boundaries are stated.
  4. The current `forever` identifiers remain unchanged.
- **Test requirements:** Markdown link review, required-section review, ADR numbering review, archive comparison, `git diff --check`, and manual consistency review against the design documents.
- **Removal/migration risks:** Replacing the active backlog can hide old assumptions if the archive is incomplete. The archive must remain committed and the working identity must not be treated as a persistent identifier migration.

## M1: Baseline compatibility

These are investigation and integration spikes. They establish whether the selected pack can work before the project owns custom systems.

### LAB-01: Matcha information and onboarding compatibility spike [Done]

- **Objective:** Test the official Matcha baseline with a recipe viewer, Jade-style contextual information, guidebook suppression, advancement-noise handling, and the requirements for one unified Field Journal.
- **Dependencies:** MRH-000, the pinned Matcha lock, and the Matcha loading decision in ADR 0027.
- **Authoritative sources to inspect:** `.pivot-context.md`, `docs/compatibility/matcha.md`, `docs/systems/matcha.md`, `docs/systems/exploration-and-regions.md`, ADRs 0001, 0002, 0003, 0027, 0028, and 0029, the pinned Matcha archive at `https://cdn.modrinth.com/data/QI0EmgZ1/versions/E9rngRfK/Matcha_Flavoured_1_12.zip`, and the official source or release pages for each selected viewer and guidebook.
- **Existing-mod candidates:** EMI, REI, JEI, Jade, Patchouli or the Matcha guidebook, Better Advancements, Advancement Plaques, and FTB Quests. Compatibility is `NEEDS_MORE_EVIDENCE` until each candidate is checked for the pinned baseline.
- **Custom-code gate:** No custom screen, journal, or adapter implementation in this spike. Record a gap for M2 only if existing configuration and supported integration cannot preserve instructions and context. Any later companion code needs ADR 0033 evidence.
- **Explicit non-goals:** No guidebook deletion, no advancement rewrite, no custom Field Journal, no Matcha identifier leakage, no final translations, and no gameplay progression change.
- **Acceptance criteria:**
  1. A disposable instance loads the exact Matcha archive and records whether both its data and resource roles are active.
  2. A player can inspect representative Matcha items and recipes through the selected recipe viewer and contextual information tool, or the missing capability is documented.
  3. Guidebook duplication is inventoried. No proposed suppression loses a process, condition, warning, or provenance record.
  4. Advancement noise is classified into useful discovery, redundant notification, or required instruction, with a proposed configuration for each.
  5. The Field Journal requirements name contextual entry points, recipe links, Matcha normalisation, source metadata, discovery state, failure reasons, and optional-provider fallbacks.
  6. Missing viewers or guidebooks leave ordinary play and an inspectable instruction path intact.
- **Test requirements:** Disposable client and dedicated-server startup, recipe inspection, item context inspection, guidebook and advancement comparison before and after proposed settings, missing-viewer fallback, translation review, and a manual instruction-loss audit.
- **Removal/migration risks:** Suppressing a book or advancement can remove information that a player has already learned. Any persistent discovery state, source link, or translation key needs a versioned migration and a reversible configuration path.

### LAB-02: Matcha pack-loader and dual-role compatibility spike [Done]

- **Objective:** Verify that the exact Matcha archive can be loaded as both datapack and resource pack through the compatibility-controlled utility selected by ADR 0027.
- **Dependencies:** MRH-000, LAB-01, the Matcha lock record, and the pinned Minecraft 26.2 baseline.
- **Authoritative sources to inspect:** `docs/dependency-baseline.md`, `docs/world-save-safety.md`, `docs/compatibility/matcha.md`, ADRs 0001, 0020, 0025, and 0027, Matcha project evidence at `https://api.modrinth.com/v2/project/QI0EmgZ1`, Global Packs project evidence at `https://api.modrinth.com/v2/project/NRLPy2mk`, Paxi project evidence at `https://api.modrinth.com/v2/project/CU0PAyzb`, and OpenLoader project evidence at `https://api.modrinth.com/v2/project/KwWsINvD`.
- **Existing-mod candidates:** Global Packs `26.2.0` with ID `DqrPrUMp`, Paxi, and OpenLoader. Global Packs is the current named 26.2 candidate. Paxi and OpenLoader require new release evidence before being considered.
- **Custom-code gate:** No custom loader. Use the selected utility and pack configuration. A narrow diagnostic or adapter is considered only after the utility's supported configuration has been tested and documented.
- **Explicit non-goals:** No Matcha extraction, archive rewrite, private loader fork, third-party JAR modification, public redistribution, or Minecraft version change.
- **Acceptance criteria:**
  1. The single pinned Matcha archive is loaded without editing or splitting it.
  2. Both the datapack and resource-pack roles are active in a disposable instance, with load order recorded.
  3. A changed checksum, missing archive, one-sided load, and conflicting load order produce visible diagnostics rather than a valid partial baseline.
  4. The Global Packs All-Rights-Reserved status is recorded as a release risk and not treated as permission to redistribute.
  5. The result states whether Global Packs can remain the technical candidate or whether another utility investigation is required.
- **Test requirements:** Lock checksum verification, clean installation, repeat installation, altered archive, missing archive, data-only load, resource-only load, both-role load, order conflict, dedicated-server startup, and disposable-world save/reload.
- **Removal/migration risks:** Changing the loader or its configuration can alter data-pack order, resource presentation, recipes, and saved references. Retain the old profile, record the utility version, and test removal before any world is opened with a changed loader.

### MRH-010: Fail loudly when the Matcha baseline is absent or altered [Done]

- **Objective:** Detect at startup that the pinned Matcha archive is missing, altered, or loaded in only one of its two roles, and report it loudly, rather than presenting a working server that silently lacks the entire gameplay foundation.
- **Dependencies:** LAB-02, `matcha.lock.json`, ADR 0027, and ADR 0033.
- **Why this exists:** LAB-02 measured four failure paths and all four fail open. Deleting the archive produced a normal startup with 1585 vanilla recipes and **no diagnostic whatsoever**. Removing the resource-pack role produced a normal startup with no diagnostic. An altered archive produced one extra parse error indistinguishable from Matcha's own. A player or server operator can therefore run a Many Roads Home world that contains none of Many Roads Home.
- **Authoritative sources to inspect:** `labs/results/LAB-02-pack-loader-dual-role.md`, `matcha.lock.json`, `docs/compatibility/matcha-loading.md`, and the existing Matcha detection code in `dev.forever.compat.matcha`.
- **Existing-mod candidates:** None. Global Packs offers no integrity or required-pack verification, which LAB-02 established by direct test, and no other 26.2 pack loader exists. This is the gap analysis ADR 0033 requires.
- **Custom-code gate:** **Met.** No configuration of the selected utility can express this check. The existing `MatchaAdapter` already performs detection and already distinguishes `PRESENT_UNVERIFIED` from `SUPPORTED`, so the work is to make that status visible and actionable at startup rather than to build something new.
- **Explicit non-goals:** No crash on mismatch, because the world save outranks the feature and a refusal to start would strand an existing world. No automatic download or repair. No archive modification. No new gameplay.
- **Acceptance criteria:**
  1. A missing archive produces an unmistakable startup error naming the expected file and its locked SHA-256.
  2. An archive whose checksum differs from `matcha.lock.json` is reported distinctly from Matcha's own parse errors.
  3. A one-sided load, data without resource or the reverse, is reported where detectable on that side.
  4. The check never prevents the server from starting, and never modifies world data.
  5. The message names the remedy, not just the symptom.
- **Test requirements:** Unit tests for present, absent, altered, and unreadable archives; a dedicated-server GameTest asserting the diagnostic appears; and confirmation that a correct install produces no new noise.
- **Removal/migration risks:** Low. The check is read-only and touches no persistent state. The main risk is a false positive after a legitimate Matcha version bump, so the message must name the lock file as the thing to update.

### LAB-03: Building and excavation compatibility spike [Done]

- **Objective:** Identify an existing configuration or mature mod stack for block placement, bulk excavation, tree felling, and related gathering convenience without gating ordinary building.
- **Dependencies:** MRH-000, LAB-02, ADRs 0024 and 0026, and the pinned loader baseline.
- **Authoritative sources to inspect:** `docs/vision.md`, `docs/design-principles.md`, `docs/systems/mastery.md`, `docs/systems/projects.md`, `docs/systems/equipment.md`, ADRs 0005, 0006, 0024, 0026, and the official release and configuration documentation for each candidate.
- **Existing-mod candidates:** Effortless Building, Building Wands, WorldEdit, FTB Ultimine, Veinminer, FallingTree, and equivalent 26.2 Fabric candidates discovered through authoritative sources.
- **Custom-code gate:** No custom placement, excavation, or durability implementation. Test configuration, datapack tags, public APIs, and safe fallbacks before opening a companion gap.
- **Explicit non-goals:** No custom block, item, tool, texture, mastery XP, style validator, or global block-break rule.
- **Acceptance criteria:**
  1. Ordinary vanilla block placement and breaking work with all candidate features disabled and enabled.
  2. Bulk operations have bounded selection, permission, tool, and resource behaviour and cannot silently delete unrelated blocks or items.
  3. The selected stack does not make a Builder mastery or blueprint a prerequisite for basic construction.
  4. Candidate conflicts with Matcha tools, equipment condition, claims, and server authority are recorded.
  5. The recommendation names the least invasive configuration and the capabilities that remain unsupported.
- **Test requirements:** Client and dedicated-server placement, bulk selection limits, cancellation, tool condition, protected-area behaviour, item drops, disconnect or retry, multiplayer permission, and absent-mod fallback.
- **Removal/migration risks:** Disabling a placement or excavation mod can change recipes, key bindings, block interaction, and unfinished operations. Do not remove a selected tool from a persistent profile without a release note and disposable-world test.

### LAB-04: Blueprint and schematic compatibility spike [Done]

- **Objective:** Evaluate an existing planning tool for optional previews, material lists, and player-authored blueprints while preserving style-independent functional building.
- **Dependencies:** MRH-000, LAB-03, ADRs 0032 and 0026, and the projects and settlements specifications.
- **Authoritative sources to inspect:** `docs/systems/projects.md`, `docs/systems/settlements.md`, `docs/design-principles.md`, ADRs 0009, 0026, and 0032, and official documentation, source, release, format, and licence information for each candidate.
- **Existing-mod candidates:** Litematica, Axiom, WorldEdit, Structurize, and any maintained 26.2 Fabric schematic provider. Compatibility and redistribution status are `NEEDS_MORE_EVIDENCE` until verified.
- **Custom-code gate:** No custom blueprint renderer, parser, or placement engine. A companion integration is considered only for a documented cross-mod gap after the candidate and format tests fail.
- **Explicit non-goals:** No required blueprint, canonical house set, aesthetic scoring, automated building, final asset, or style-specific settlement validation.
- **Acceptance criteria:**
  1. A player can build and register a functional structure without using a blueprint.
  2. At least one candidate can preview or describe a player-authored plan without changing server authority over blocks.
  3. Missing block mappings, unsupported files, and unavailable client tooling leave the physical world intact and produce an actionable limitation.
  4. The report defines which blueprint metadata may be stored and how provenance is retained.
  5. Functional contract criteria are expressed independently from visual similarity or palette.
- **Test requirements:** Load and unload a blueprint, missing-block handling, client/server mismatch, permission checks, material-list bounds, hand-built registration, malformed file handling, and a colour-independent functional validation review.
- **Removal/migration risks:** Removing a schematic tool can strand client-only plans or persistent contract references. Keep physical builds authoritative and migrate unavailable plans to a readable legacy or unavailable state.

### LAB-05: Food, cooking, and preservation compatibility spike [Done]

- **Objective:** Find an existing cooking and food-processing stack that supports readable recipes, regional ingredients, preservation, and optional trait integration without making vanilla food obsolete.
- **Dependencies:** MRH-000, LAB-01, LAB-02, ADRs 0024 and 0026, and the food specification.
- **Authoritative sources to inspect:** `docs/systems/food-and-alchemy.md`, `docs/systems/exploration-and-regions.md`, `docs/systems/storage.md`, `docs/design-principles.md`, ADRs 0014, 0024, and 0026, and official candidate documentation and release metadata.
- **Existing-mod candidates:** Farmer's Delight, Croptopia, Cooking for Blockheads, Brewin' and Chewin', Create processing, and a maintained alchemy provider where one is needed. Each candidate's 26.2 support is `NEEDS_MORE_EVIDENCE`.
- **Custom-code gate:** No custom food effect engine or recipe replacement. Use configuration, datapack recipes, tags, and public integration first. A companion food adapter needs a separate gap analysis.
- **Explicit non-goals:** No hard seasonal food lockout, hidden recipe puzzle, global hunger overhaul, silent spoilage deletion, or replacement of ordinary vanilla cooking.
- **Acceptance criteria:**
  1. Vanilla food remains edible and useful without trait knowledge.
  2. Candidate recipes and processing steps are visible through the selected viewer or Field Journal plan.
  3. Unknown modded foods remain ordinary food until a safe mapping exists.
  4. Preservation and alchemy candidates have bounded inputs, outputs, durations, and failure behaviour.
  5. Regional acquisition, cultivation, import, and trade remain possible alternatives where the pack expects them.
- **Test requirements:** Recipe discovery, unknown-food fallback, processing interruption, duplicate output prevention, preservation recovery, effect-cap review, server-side consumption, and client explanation checks.
- **Removal/migration risks:** Removing a cooking mod can orphan recipes, prepared items, traits, or preserved stock. Record item identities, retain physical inputs, and define a readable migration before changing a selected food provider.

### LAB-06: Seasons and climate compatibility spike [Done]

- **Objective:** Verify a climate provider and safe fallback for visible seasons, modest crop situations, weather context, and greenhouse or mitigation choices.
- **Dependencies:** MRH-000, LAB-05, LAB-02, ADR 0014, and the seasons and weather specification.
- **Authoritative sources to inspect:** `docs/systems/seasons-and-weather.md`, `docs/systems/food-and-alchemy.md`, `docs/systems/transportation.md`, `docs/design-principles.md`, ADRs 0014, 0024, and 0026, and the official Serene Seasons project and release sources.
- **Existing-mod candidates:** **Revised by LAB-06.** Homeostatic Seasons `1bSif4Rz` is the pilot target. Serene Seasons `13sXhUkI` is DEFERRED: beta 26.2 build, All Rights Reserved, beta GlitchCore dependency, and an unknown custom game rule left behind on removal. Also vanilla weather and greenhouse or crop-support mods with public compatibility.
- **Custom-code gate:** No custom calendar or crop-kill system. A climate abstraction is considered only after the provider and fallback behaviour are tested and the adapter gap is documented.
- **Explicit non-goals:** No months-long crop lockout, weather disaster simulator, mandatory greenhouse, guessed mapping for unknown crops, or permanent chunk loading.
- **Acceptance criteria:**
  1. Current season, forecast band, local profile, crop suitability, and mitigation are inspectable as text and icons.
  2. Missing or incompatible Serene Seasons leaves ordinary crops, food, and travel functional.
  3. Unknown crops receive a neutral profile rather than an invented destructive rule.
  4. Effects remain modest and data-driven, with cultivation, preservation, import, and trade alternatives.
  5. The candidate's server and client responsibilities and version support are recorded.
- **Test requirements:** Provider present, provider absent, unknown crop, calendar restart, forecast change, greenhouse or mitigation validation, client stale view, and dedicated-server startup.
- **Removal/migration risks:** A climate provider can alter crop growth, saved calendar state, and prepared food expectations. Remove or replace it only with a calendar migration and a safe neutral mapping for existing records.

### LAB-07: Storage indexing and local search compatibility spike [Done]

- **Objective:** Select an existing storage and indexing solution that makes physical local goods searchable without turning a remote index into magical withdrawal.
- **Dependencies:** MRH-000, LAB-02, LAB-01, ADRs 0011, 0012, 0024, and 0026, and the storage specification.
- **Authoritative sources to inspect:** `docs/systems/storage.md`, `docs/systems/logistics.md`, `docs/design-principles.md`, ADRs 0011, 0012, 0024, and 0026, and official candidate documentation, source, release, and licence pages.
- **Existing-mod candidates:** Tom's Simple Storage, Simple Storage Network, Sophisticated Storage, Functional Storage, Storage Drawers, and vanilla containers with a bounded search layer.
- **Custom-code gate:** No custom warehouse controller or search terminal. First test configuration and public integration. A companion layer is allowed only after physical-source-of-truth, nesting, and removal gaps are written.
- **Explicit non-goals:** No universal cross-dimensional warehouse, remote item creation, infinite container nesting, forced registration for vanilla chests, or unbounded inventory scans.
- **Acceptance criteria:**
  1. Physical inventories remain the source of truth and ordinary containers remain usable without registration.
  2. Search results identify a physical location and do not grant withdrawal rights by themselves.
  3. Candidate indexing is bounded and can rebuild or mark stale results without altering physical stacks.
  4. Traveler's Cache or equivalent personal storage cannot recursively contain another inventory.
  5. Permissions, server authority, and missing-container behaviour are documented.
- **Test requirements:** Index creation, stale index, physical mutation, search paging, permission denial, item identity changes, container nesting rejection, restart, and no-mod fallback.
- **Removal/migration risks:** Removing a storage provider can invalidate container references, network links, and search metadata. Keep physical containers untouched and migrate derived indexes as discardable data only when safe.

### LAB-08: Local item transport and automation compatibility spike [Done]

- **Objective:** Identify a local transport and automation stack that moves physical goods through bounded networks without replacing the logistics and route authority planned for later milestones.
- **Dependencies:** MRH-000, LAB-07, LAB-03, LAB-02, ADRs 0010, 0012, 0024, and 0026, and the logistics specification.
- **Authoritative sources to inspect:** `docs/systems/logistics.md`, `docs/systems/storage.md`, `docs/systems/transportation.md`, `docs/world-save-safety.md`, ADRs 0010, 0012, 0024, and 0026, and official candidate source and release records.
- **Existing-mod candidates:** Create, Pipez, Integrated Dynamics with Integrated Tunnels, Modern Industrialization transport, vanilla hoppers, and another maintained local transport provider if evidence exists.
- **Custom-code gate:** No custom pipes, shipment state machine, or remote withdrawal. Configuration and public APIs must be exhausted before a connective companion gap is opened.
- **Explicit non-goals:** No magical remote bulk withdrawal, cross-dimensional transfer without an explicit depot, unbounded network traversal, or permanent chunk loading.
- **Acceptance criteria:**
  1. Goods remain physical in a source, transport, cargo, buffer, or destination inventory.
  2. Network limits, routing scope, permissions, and failure behaviour are bounded and inspectable.
  3. A broken or unloaded endpoint does not duplicate or silently delete cargo.
  4. Local automation does not become a hidden dependency for ordinary inventory use.
  5. The report identifies which later shipment and route contracts can consume the selected stack.
- **Test requirements:** Full and empty transfers, endpoint removal, restart during transfer, duplicate request, unloaded endpoint, cross-dimension rejection, permission checks, and server performance bounds.
- **Removal/migration risks:** Transport networks often encode links and buffers outside the item stack. Removing one can strand or duplicate cargo, so preserve the old network long enough to drain or migrate it in a disposable-world test.

### LAB-09: Rail, horse, boat, and vehicle compatibility spike [Done]

- **Objective:** Evaluate existing transport mods and vanilla modes for fixed routes, freight, horses, boats, and physical service behaviour.
- **Dependencies:** MRH-000, LAB-03, LAB-08, LAB-02, ADRs 0010, 0013, 0018, 0024, and 0026, and the transportation specification.
- **Authoritative sources to inspect:** `docs/systems/transportation.md`, `docs/systems/animals.md`, `docs/systems/logistics.md`, ADRs 0010, 0013, 0018, 0024, and 0026, and official candidate release and API information.
- **Existing-mod candidates:** Create rail, Extended Rails, vanilla minecarts, horses, boats, Immersive Aircraft or another scoped vehicle provider, and any maintained route or freight mod with 26.2 evidence.
- **Custom-code gate:** No custom rail physics, vehicle entity, or live pathfinding graph. Test existing capabilities and public route hooks first. A route adapter must be narrow and version-aware.
- **Explicit non-goals:** No instant travel, global route scan, universal vehicle, Elytra deletion, permanent chunk loading, or automatic route registration.
- **Acceptance criteria:**
  1. Walking, horses, boats, rail, and any selected vehicle each have a clear context and limitation.
  2. Rail and freight candidates expose bounded station or endpoint behaviour suitable for explicit registration.
  3. Elytra or gliding remains a distinct scouting and vertical option rather than an accidental fixed-route monopoly.
  4. Broken infrastructure fails visibly and leaves another ordinary travel option.
  5. The compatibility report records capacity, service, dimension, and authority limitations.
- **Test requirements:** Station connectivity, disconnected track, freight transfer, horse and boat baseline, vehicle absence, chunk unload, restart, route damage, and dedicated-server movement.
- **Removal/migration risks:** Removing a vehicle mod can strand entities, cargo, or route references. A future profile change needs an entity and cargo recovery plan before the dependency is removed.

### LAB-10: Performance, ambience, structures, and worldgen compatibility spike [Done]

- **Objective:** Establish a conservative baseline for performance, ambience, and restrained structure content without adding a world-generation dependency by assumption.
- **Dependencies:** MRH-000, LAB-02, LAB-03, and ADR 0015 plus the performance rules in the architecture document.
- **Authoritative sources to inspect:** `docs/architecture.md`, `docs/vision.md`, `docs/design-principles.md`, `docs/systems/exploration-and-regions.md`, ADR 0015, the dependency baseline, and official sources for every candidate.
- **Existing-mod candidates:** Sodium, Lithium, FerriteCore, Entity Culling, AmbientSounds, Presence Footsteps, YUNG's Better Structures, Structory, Towns and Towers, and vanilla world generation. Candidate support remains `NEEDS_MORE_EVIDENCE` until checked.
- **Custom-code gate:** No custom worldgen, structure generator, global scan, or performance manager. Prefer configuration and existing profiling tools. Any compatibility hook needs a separate gap analysis.
- **Explicit non-goals:** No dramatic worldgen rewrite, mandatory structure pack, enemy scaling, permanent chunk loading, or final ambience assets authored by this repository.
- **Acceptance criteria:**
  1. The selected baseline keeps vanilla terrain and ordinary world creation functional.
  2. Structure candidates are measured for density, overlap, spawn safety, and removal implications before selection.
  3. Performance candidates do not hide server errors or change authority to the client.
  4. Ambience remains optional and does not carry gameplay state alone.
  5. The report identifies a disposable-world profiling method and rejection thresholds in data or configuration.
- **Test requirements:** Clean world creation, structure placement sample, dedicated-server startup, tick and memory observation, optional ambience absence, client/server mismatch, and a no-worldgen-dependency fallback.
- **Removal/migration risks:** Worldgen and structures are difficult to remove after chunks are generated. A selected generator or structure mod requires a worldgen lock, backup, compatibility notice, and explicit removal analysis before persistent release.

### LAB-11: Adventure rewards and capability-gate compatibility spike [Done]

- **Objective:** Audit quest, structure, and advancement rewards so that they provide discovery and optional opportunity without becoming the only route to essential capabilities.
- **Dependencies:** MRH-000, LAB-01, LAB-02, LAB-10, ADRs 0028 and 0030, and the exploration and projects specifications.
- **Authoritative sources to inspect:** `docs/systems/exploration-and-regions.md`, `docs/systems/projects.md`, `docs/systems/mastery.md`, `docs/design-principles.md`, ADRs 0015, 0028, 0029, and 0030, and official candidate documentation and configuration.
- **Existing-mod candidates:** FTB Quests, Better Advancements, Advancement Plaques, Patchouli, structure providers, and the selected Matcha advancement and reward configuration.
- **Custom-code gate:** No custom quest engine or global enemy scaling. Use configuration, datapacks, reward tables, and existing events first. A companion consequence system needs ADR 0033 evidence.
- **Explicit non-goals:** No mandatory questbook opening, essential capability locked behind a rare structure, global enemy multiplier, repeatable loot treadmill, or multiplayer-only reward.
- **Acceptance criteria:**
  1. Every proposed reward is classified as essential, optional, convenience, knowledge, cosmetic, or historical.
  2. Every essential capability has a documented non-adventure route that is viable in solo play.
  3. Expected processes remain inspectable in the Field Journal and recipe viewer.
  4. Local danger is bounded and contextual rather than globally scaled.
  5. Missing structures, unavailable teammates, and declined opportunities have safe outcomes.
- **Test requirements:** Fresh-player route, no-structure route, solo route, declined quest, repeated structure visit, reward duplication, advancement-noise review, and server authority checks.
- **Removal/migration risks:** Removing a quest or structure can strand persistent reward claims and discovery state. Preserve the knowledge record and provide an alternate route before disabling a source.

### LAB-12: Companion boundary and gap-analysis compatibility spike [Done]

- **Objective:** Produce the first evidence-led inventory of connective problems that existing mods, configuration, datapacks, resource packs, scripting, and public APIs cannot safely solve.
- **Dependencies:** MRH-000 and LAB-01 through LAB-11.
- **Authoritative sources to inspect:** ADRs 0022, 0024, 0026, 0033, and 0034, `docs/architecture.md`, `docs/world-save-safety.md`, every LAB report, and the official source, release, API, and licence records for each candidate.
- **Existing-mod candidates:** All candidates retained by LAB-01 through LAB-11, plus Patchouli or Modonomicon for knowledge, FTB Quests for projects, route and logistics providers, and any maintained cross-mod capability provider found during the spikes.
- **Custom-code gate:** This ticket may recommend a custom companion feature but may not implement one. Each proposed gap must evaluate the exact nine-step escalation order and at least three plausible alternatives where available. A private fork requires its own licence and rebuild evidence.
- **Explicit non-goals:** No Java implementation, new registry, custom screen, Mixins, private fork, third-party JAR modification, or silent reuse of prototype systems.
- **Acceptance criteria:**
  1. Each candidate gap names the player problem, pack-identity importance, owner, fallback, and affected systems.
  2. The report records why configuration and datapacks are insufficient and which higher escalation steps failed.
  3. Persistent data, network, save migration, removal, licensing, and maintenance costs are named before any approval.
  4. Each gap has proposed tests covering absence, malformed input, restart, duplication, migration, and authority.
  5. The output distinguishes “custom code justified”, “use existing tool”, and “defer because evidence is incomplete”.
- **Test requirements:** Completeness check against all LAB reports, candidate-source review, licence review, save-safety review, and a manual gate review by the pack owner.
- **Removal/migration risks:** A gap report that omits a dependency or persisted identifier can authorise an unsafe companion. No code ticket may proceed from an incomplete report, and the report must be revisited when candidate versions change.

### MRH-011: Ship the Matcha baseline diagnostic with the pack [Next]

- **Objective:** Make the MRH-010 baseline diagnostic actually reach a player, so a missing or degraded Matcha archive is reported instead of silently changing the world.
- **Dependencies:** MRH-010 (complete), PACK-01, ADR 0027, and `docs/decisions/OPEN-rights-model.md`.
- **Evidence for this ticket:** Two failure paths were tested against a server assembled from `pack/`, and both are silent.
  - *Matcha missing:* starts at 1585 recipes and 1688 advancements instead of 2346/1805, with no diagnostic.
  - *Matcha substituted:* a structurally valid decoy with the correct filename gives the same 1585/1688 and, again, no diagnostic. This is the more dangerous case, because the file exists and is named correctly, so an operator has nothing to notice.

  The code is present in `dev.forever.compat.matcha` with unit and GameTest coverage, and `MatchaBaselineReport` already maps the substituted case to DEGRADED and the missing case to MISSING. It is simply not shipped.
- **Custom-code gate:** No new code is needed. The work is packaging an existing, tested feature.
- **Explicit non-goals:** No new gameplay, no blocking startup, no change to the diagnostic's behaviour or wording.
- **Acceptance criteria:**
  1. A pack install with Matcha missing shows the framed ERROR banner, and a degraded archive shows the WARN form.
  2. Startup is never blocked, matching MRH-010's tested behaviour.
  3. The companion build is pinned by exact version and hash like every other input, and appears in `docs/compatibility/pack-input-provenance.md`.
- **Blocked on:** the rights-model decision. Shipping the companion mod means the pack distributes original code, so its licence must be settled first.

### PACK-01: Assemble the pinned baseline profile [Planned]
- **Evidence recorded 2026-08-29 (partial, not a completion claim).** Several acceptance criteria are now demonstrated and should not be re-derived:
  - Reproducible export (criterion 1): two `./scripts/build-pack.sh` runs two seconds apart produced **byte-identical** `.mrpack` files.
  - No committed third-party JARs (criterion 5): the export contains only `modrinth.index.json` and the Global Packs override; every dependency is a URL and hash reference. Verified by listing archive entries.
  - Inputs resolve as pinned (criterion 2, in part): all 9 pinned files download and match their sha512, checked by `scripts/verify-pack-downloads.py`.
  - Dedicated-server startup: a disposable instance assembled from `pack/` alone booted and stopped cleanly, preserving the 2346-recipe and 1805-advancement Matcha baseline.
  - Index integrity: `scripts/validate-pack.sh` now verifies the index against its files and `pack.toml` against the index, and was checked against a real `packwiz refresh`.
  - Per-input identity, source, licence, compatibility and removal notes (criterion 2, complete): `docs/compatibility/pack-input-provenance.md`, generated from the Modrinth API with every source URL requested to confirm it resolves. `validate-pack.sh` now fails if a pinned input is missing from it.
  - Packwiz tool identity and acquisition policy (criterion 4): `docs/compatibility/packwiz-acquisition.md`. Verified that upstream publishes 0 releases and 0 tags, so the Go pseudo-version `v0.0.0-20260218225342-dfd8b68a4796` is recorded as the exact-revision substitute for a version pin.
  - Missing-input failure case (criterion 5, tested): a disposable server with the Matcha datapack removed but every mod present **starts normally** at 1585 recipes and 1688 advancements, versus 2346/1805 with Matcha. It fails open exactly as LAB-02 measured.
  - **Gap found by that test:** the MRH-010 diagnostic did **not** fire, because the companion mod is not in `pack/`. The safety net exists in `dev.forever.compat.matcha` and is unit- and GameTest-covered, but a player installing the pack today would get the silent degradation MRH-010 was written to prevent. Shipping it needs a companion build plus the rights-model decision, since the pack would then distribute original code. Tracked as **MRH-011**.
  - Client startup (tested): a clean clone with exactly the 9 pinned inputs reached a rendering main menu. OpenGL 4.5, 61 mods loaded including Lithium, FerriteCore, Jade, REI and Global Packs, and 26 texture atlases stitched. Matcha's client resource role is active.
  - **Known benign client noise:** 374 `Codepoint 'e000' declared multiple times` warnings from Matcha's own `custom_emojis.png`. Pre-existing (187 appear in an earlier single-role baseline) and doubled here because Matcha is deliberately loaded in both the resource-pack and datapack roles. Not caused by any Many Roads Home change.
  - Altered-input failure case (criterion 5, tested): a structurally valid decoy archive with the correct filename `Matcha_Flavoured_1_12.zip` produced 1585 recipes and 1688 advancements and **no diagnostic whatsoever**. This is worse than the missing case, because the file is present and correctly named, so nothing looks wrong. It strengthens MRH-011 rather than adding new work: `MatchaBaselineReport` already classifies an unverifiable archive as DEGRADED, so shipping the companion is the fix.
  - **Still open:** a disposable-world smoke test with real play, which needs a human.

- **Objective:** Build the first Packwiz-managed Many Roads Home profile from the compatibility results, with pinned inputs, configuration, provenance, and a reproducible Matcha loading path.
- **Dependencies:** LAB-01 through LAB-12, ADRs 0024, 0025, and 0027, and the licence review for every selected input.
- **Authoritative sources to inspect:** `docs/dependency-baseline.md`, `docs/world-save-safety.md`, ADRs 0020, 0024, 0025, and 0027, the Packwiz repository at `https://github.com/packwiz/packwiz`, the Packwiz releases page at `https://github.com/packwiz/packwiz/releases`, and the official source page for every selected mod and pack input.
- **Existing-mod candidates:** Packwiz for composition, Global Packs for the current Matcha utility candidate, and the selected candidates that passed LAB compatibility gates.
- **Custom-code gate:** No companion feature is added. The pack profile may reference an existing companion build only if its separate gap analysis is accepted. Do not modify third-party JARs.
- **Explicit non-goals:** No public release, automatic updates, unpinned `latest` inputs, persistent-world migration, final art, or gameplay balance claim beyond the tested profile.
- **Acceptance criteria:**
  1. Packwiz metadata is the only composition authority and generated exports are reproducible.
  2. Every input has exact identity, source, licence, compatibility status, and removal note.
  3. The Matcha archive is one input with both roles configured through the selected utility.
  4. The Packwiz tool identity and acquisition policy are recorded despite the absence of tagged upstream releases.
  5. A clean profile can be assembled without committed third-party JARs and produces an actionable failure for missing or altered inputs.
- **Test requirements:** Clean checkout assembly, repeated export comparison, altered metadata, missing input, Matcha checksum, dedicated-server startup, client startup, and disposable-world smoke test.
- **Removal/migration risks:** The profile becomes the first dependency contract. Removing an input, changing load order, or changing a config can alter recipes, worldgen, registries, or saved state. Require a backup, migration note, and review for every subsequent profile change.

## M2: Information and onboarding

### INFO-01: Deliver the unified Field Journal and contextual knowledge surface [Planned]

- **Objective:** Implement the single contextual Field Journal that explains pack processes, connects source content, and exposes instructions without requiring external wiki research.
- **Dependencies:** PACK-01, LAB-01, ADRs 0028, 0029, and 0033, plus accepted candidate reports for recipe viewing and contextual information.
- **Authoritative sources to inspect:** `docs/systems/matcha.md`, `docs/systems/exploration-and-regions.md`, `docs/systems/projects.md`, `docs/systems/mastery.md`, `docs/design-principles.md`, ADRs 0002, 0022, 0028, 0029, and 0033, and the LAB-01 report.
- **Existing-mod candidates:** Patchouli, Modonomicon, EMI, REI, JEI, Jade, FTB Quests, and the selected guidebook provider. Existing tools remain the default surface where they can expose the required context.
- **LAB-01 evidence (read before starting):** The two problems this ticket was written to solve were measured and **did not materialise**. There is no guidebook duplication, because Matcha ships no guidebook and the adopted Jade and REI do not either. There is no advancement-noise problem: of 243 Matcha advancements, 141 are recipe plumbing that Matcha *already silences*, only 69 toast, and just 3 are tabs. LAB-01 classified none as redundant notification and proposed **changing nothing**. The remaining 59 `tutorial` and 20 `mechanics` entries carry required instruction and must not be suppressed. Scope this ticket to the genuine remaining gap, not the presumed one.
- **Custom-code gate:** A companion journal is allowed only if LAB-12 proves that existing surfaces cannot provide one coherent, server-authoritative view. Any code must pass ADR 0033 and use data-driven entries with a safe absent-provider fallback. **On LAB-01 evidence this gate is currently NOT met**, and Jade's `IBlockComponentProvider` is the identified seam for surfacing journal state without a journal owning the HUD.
- **Explicit non-goals:** No replacement of every upstream screen, hidden instructions, client-owned discovery, custom recipe system, or requirement to use a journal for ordinary vanilla play.
- **Acceptance criteria:**
  1. A player can inspect a known process, its prerequisites, outcome, warning, and alternatives in game.
  2. Entries expose source, version, discovery state, and optional-provider limitations without leaking private Matcha identifiers.
  3. Contextual entry points link from items, recipes, blocks, routes, structures, and failed actions where relevant.
  4. Server state controls discovery and the client renders bounded pages and projections.
  5. Missing journal providers do not remove ordinary building, food, shelter, or inventory use.
- **Test requirements:** Unit tests for entry validation and bounded pages, server/client authority tests, translation and accessibility review, recipe-viewer links, optional-provider absence, save/reload of knowledge, migration from one entry schema version, and dedicated-server startup.
- **Removal/migration risks:** Knowledge is persistent player state. Removing an entry must preserve a readable legacy or unavailable record. A broken source link must not erase what the player learned.

### INFO-02: Normalise onboarding, guidebooks, and advancement noise [Planned]

- **Objective:** Apply the LAB-01 findings so that starter books, advancements, recipe viewers, and contextual overlays form one understandable onboarding flow.
- **LAB-01 evidence (read before starting):** LAB-01 measured the onboarding surfaces and proposed **changing nothing**. Matcha ships no starter book to reconcile, and it already silences the 141 recipe-unlock advancements, leaving 69 toasts and 3 tabs across 243. No entry was classified as redundant notification, and the 59 `tutorial` plus 20 `mechanics` entries carry required instruction. REI and Jade are the adopted surfaces; JEI was rejected as beta-only and as a second viewer. Treat suppression work as unjustified unless new evidence contradicts this.
- **Dependencies:** INFO-01, PACK-01, LAB-01, and ADRs 0028 and 0029.
- **Authoritative sources to inspect:** LAB-01, `docs/systems/matcha.md`, `docs/systems/projects.md`, `docs/design-principles.md`, ADRs 0002, 0028, and 0029, and each selected upstream configuration guide.
- **Existing-mod candidates:** Patchouli or Modonomicon, FTB Quests, Better Advancements, Advancement Plaques, EMI, REI, JEI, and Jade.
- **Custom-code gate:** Prefer configuration and datapacks. A custom suppression or notification bridge requires the LAB-12 gap record and must not delete source instructions.
- **Explicit non-goals:** No quest-gated first hour, no removal of all source books, no advancement reward rebalance, no global notification mute, and no new gameplay unlock.
- **Acceptance criteria:**
  1. Duplicate starter material is suppressed only when its complete instruction is available in the Field Journal or a stable linked provider.
  2. Useful discovery signals remain distinguishable from redundant noise.
  3. The player can find instructions from a fresh profile without a prescribed quest order.
  4. Every disabled source has a versioned configuration and a rollback note.
  5. Text, icons, and non-colour cues explain warnings and unavailable integrations.
- **Test requirements:** Fresh profile walkthrough, experienced profile walkthrough, source-book comparison, advancement trigger review, missing-provider fallback, localisation review, and client restart.
- **Removal/migration risks:** Suppression settings and learned-state identifiers may differ between versions. Restore a source book or migrate its entries before changing the onboarding profile for an existing world.

## M3: Building and gathering

### BUILD-01: Configure the ordinary building and gathering stack [Planned]

- **Objective:** Ship the selected placement, excavation, tree, and gathering conveniences with normal Minecraft building always available.
- **LAB-03/LAB-04 evidence (read before starting):** The placement stack is already selected. LAB-03 validated **Effortless Building** and LAB-04 validated **Litematica + MaLiLib**, both PILOT on server-side evidence with client and multiplayer verification still outstanding. LAB-04 **rejected Simple Blueprints** because it gates placement behind a creative-only `setblock` path unusable in survival, and held **AutoBuild** at NEEDS_MORE_EVIDENCE because it can overreach into printing and has unresolved licence, claim, permission, reach, and durability questions. Neither pilot is in `pack/` yet.
- **Dependencies:** PACK-01, LAB-03, INFO-01, and ADRs 0024, 0026, and 0032.
- **Authoritative sources to inspect:** LAB-03, `docs/design-principles.md`, `docs/systems/mastery.md`, `docs/systems/equipment.md`, `docs/systems/projects.md`, ADRs 0005, 0006, 0024, 0026, and 0032.
- **Existing-mod candidates:** The LAB-03 recommendation, including Effortless Building, Building Wands, WorldEdit, FTB Ultimine, Veinminer, and FallingTree where they pass the baseline.
- **Custom-code gate:** No custom block or excavation mechanics. Configuration or datapack work is preferred. A companion integration requires a new gap analysis if the selected tools cannot share permissions or item conditions.
- **Explicit non-goals:** No style scoring, blueprint requirement, Builder mastery requirement, custom block, custom tool, or final texture.
- **Acceptance criteria:**
  1. Ordinary block placement and gathering work before registration, mastery, quest, or blueprint use.
  2. Bulk operations have bounded selection and preserve item and condition rules.
  3. The Field Journal explains controls, limits, failure reasons, and safe fallback.
  4. The selected stack does not break Matcha item identity or vanilla tools.
  5. Pack configuration is documented as source metadata rather than a local manual tweak.
- **Test requirements:** Dedicated-server placement, bulk operation bounds, protection, tool condition, drops, cancellation, client absence, accessibility cues, and regression tests for vanilla building.
- **Removal/migration risks:** Configuration keys and unfinished operations may change between mod versions. Drain or cancel active operations before removal and retain normal vanilla behaviour.

### BUILD-02: Add gathering breadth and project-ready resource paths [Planned]

- **Objective:** Make regional gathering, cultivation, import, and meaningful project inputs usable without turning raw repetition into progression.
- **Dependencies:** BUILD-01, INFO-01, LAB-05, LAB-06, LAB-11, and the exploration, food, mastery, and projects specifications.
- **Authoritative sources to inspect:** `docs/systems/exploration-and-regions.md`, `docs/systems/food-and-alchemy.md`, `docs/systems/mastery.md`, `docs/systems/projects.md`, `docs/design-principles.md`, ADRs 0003, 0014, 0028, and 0030.
- **Existing-mod candidates:** Farmer's Delight, Croptopia, Create farming and processing, FallingTree, FTB Ultimine, productive resource mods, and the selected regional or structure provider.
- **Custom-code gate:** Do not add a repetition counter or custom resource generator. A connective resource or discovery record needs the LAB-12 evidence and ADR 0033 approval.
- **Explicit non-goals:** No AFK replacement farm, raw action-count XP, permanent resource lockout, global enemy scaling, or essential rare-loot gate.
- **Acceptance criteria:**
  1. A first regional acquisition can be meaningful without making later access permanently tedious.
  2. Cultivation, preservation, import, trade, or infrastructure provide documented alternatives where expected.
  3. Project completion is based on a validated outcome, not a block-count counter.
  4. The Field Journal explains unknown resources, safe use, and alternative acquisition.
  5. Unknown modded inputs remain physical and recoverable.
- **Test requirements:** First discovery, repeat acquisition, alternative route, project substitution, unknown item, solo route, and no-repetition-progression review.
- **Removal/migration risks:** Resource tags and recipe identities can change the value of existing stock. Preserve physical items, version category mappings, and migrate only with explicit evidence.

## M4: Food and seasons

### FOOD-01: Integrate cooking, traits, alchemy, and preservation [Planned]

- **Objective:** Deliver readable food and preparation choices using existing mods and data before considering a custom trait bridge.
- **LAB-05 evidence (read before starting):** LAB-05 concluded **adopt nothing**. Matcha already owns food, hunger, healing, and food effects, so adding a food mod would contest an owner the pack already has. This ticket is therefore data and presentation work over Matcha's existing vocabulary, not a mod-selection exercise. A candidate that overlaps Matcha's ownership should be rejected on that basis alone.
- **Dependencies:** BUILD-02, INFO-01, LAB-05, LAB-06, storage and economy plans, and the food specification.
- **Authoritative sources to inspect:** `docs/systems/food-and-alchemy.md`, `docs/systems/storage.md`, `docs/systems/economy.md`, `docs/systems/mastery.md`, ADRs 0014, 0024, 0026, and 0029.
- **Existing-mod candidates:** The LAB-05 recommendation, including Farmer's Delight, Croptopia, Cooking for Blockheads, Brewin' and Chewin', Create processing, and a stable alchemy provider.
- **Custom-code gate:** No custom effect engine unless existing recipes, tags, and APIs cannot express the approved vocabulary and LAB-12 records the gap. Unknown foods must remain ordinary food.
- **Explicit non-goals:** No forced food trait, opaque recipe puzzle, unbounded effect stacking, silent spoilage deletion, or removal of vanilla food.
- **Acceptance criteria:**
  1. Trait names, effects, interactions, duration, intensity, and contraindications are inspectable.
  2. Cooking and alchemy remain distinct choices with bounded outputs.
  3. Preservation keeps physical food recoverable through failure and restart.
  4. Modded ingredients can use generic mappings or remain neutral when ambiguous.
  5. The Field Journal and recipe viewer explain every expected process.
- **Test requirements:** Recipe and trait inspection, effect caps, unknown ingredient, interrupted preparation, duplicate output prevention, restart recovery, vanilla regression, and accessibility review.
- **Removal/migration risks:** Prepared food and trait records may outlive their provider. Preserve source identity and migrate removed effects to a readable legacy state instead of applying an arbitrary replacement.

### FOOD-02: Integrate seasons and climate mitigation [Planned]

- **Objective:** Add a modest, predictable climate layer through the selected provider and fallback, with greenhouse, preservation, trade, and import as alternatives.
- **LAB-06 evidence (read before starting):** No climate provider is adopted and nothing was added to `pack/`. ADR 0014 stands: depend on the **abstraction**, never a provider. **Homeostatic Seasons** `1bSif4Rz` is the pilot target because it boots beside Matcha and leaves the recipe and advancement baseline unchanged; **Serene Seasons** is deferred (beta 26.2, All Rights Reserved, beta GlitchCore dependency). Critically, provider removal is **not** a cosmetic toggle: a custom game rule can persist as an unknown registry key after the jar is gone, and some providers leave calendar or meltable state in the save. Crop suitability, unknown crops, and greenhouse mitigation remain unanswered and interact with Matcha's food ownership from LAB-05.
- **Dependencies:** FOOD-01, LAB-06, INFO-01, and the transportation and exploration specifications.
- **Authoritative sources to inspect:** `docs/systems/seasons-and-weather.md`, `docs/systems/food-and-alchemy.md`, `docs/systems/transportation.md`, `docs/systems/settlements.md`, ADR 0014, and the LAB-06 report.
- **Existing-mod candidates:** Serene Seasons, vanilla weather, greenhouse or crop-support mods, and the climate provider selected by LAB-06.
- **Custom-code gate:** No custom calendar or destructive crop rule. A Many Roads Integration climate adapter requires an accepted gap analysis and a safe no-op provider.
- **Explicit non-goals:** No hard seasonal ban, punitive waiting period, random settlement disaster, mandatory greenhouse, or guessed unknown-crop mapping.
- **Acceptance criteria:**
  1. Season, local climate, forecast band, suitability, and mitigation are visible with text and icons.
  2. Missing climate provider keeps ordinary food and travel playable.
  3. Greenhouse function is validated by bounded function, not style or blueprint.
  4. Climate values and effects are data-driven and modest.
  5. A player can recover from missed timing through multiple documented routes.
- **Test requirements:** Calendar save/reload, provider absence, unknown crop, greenhouse invalidation, forecast change, crop regression, client/server authority, and dedicated-server startup.
- **Removal/migration risks:** Season state and crop mappings can alter a persistent world. Keep a versioned calendar and neutral fallback before replacing or removing a climate provider.

## M5: Storage and logistics

### STORAGE-01: Deliver physical local storage and search [Planned]

- **Objective:** Provide a bounded local search and personal cache experience using the selected storage stack while keeping physical inventories authoritative.
- **LAB-07 evidence (read before starting):** LAB-07 **overturned the code inventory's only REPLACE classification**. No surveyed storage mod indexes containers server-authoritatively without either granting wireless access or losing data, so the assumption that an existing stack could simply be adopted here did not survive testing. Re-read the LAB-07 report before selecting any candidate, and treat 'the existing stack' in the gate below as unproven rather than chosen.
- **Dependencies:** FOOD-01, PACK-01, LAB-07, INFO-01, and the storage specification.
- **Authoritative sources to inspect:** `docs/systems/storage.md`, `docs/systems/logistics.md`, `docs/world-save-safety.md`, ADRs 0011, 0012, 0024, and 0026, and the LAB-07 report.
- **Existing-mod candidates:** The LAB-07 recommendation, including Tom's Simple Storage, Simple Storage Network, Sophisticated Storage, Functional Storage, Storage Drawers, and vanilla containers.
- **Custom-code gate:** No custom warehouse or terminal until the existing stack's indexing, permissions, and route boundaries fail a documented test. A companion implementation needs ADR 0033 approval.
- **Explicit non-goals:** No remote magical withdrawal, dimension-spanning warehouse, recursive cache, forced container registration, or unbounded scan.
- **Acceptance criteria:**
  1. Search results identify physical locations and access rights.
  2. Indexes are derived, bounded, rebuildable, and never the only copy of an item.
  3. Personal cache capacity, nesting rules, and movement behaviour are documented.
  4. Failed transfers leave source and destination unchanged.
  5. Normal chests, barrels, shulkers, and inventories remain valid without the feature.
- **Test requirements:** Index rebuild, stale result, physical mutation, transfer atomicity, cache nesting, death or drop, restart, permissions, paging, and absent-provider fallback.
- **Removal/migration risks:** Derived indexes can be discarded only when physical references remain safe. Cache components and container links require schema and item recovery tests before removal.

### LOGISTICS-01: Deliver local physical shipments [Planned]

- **Objective:** Connect storage, local transport, and receiving buffers so that bulk movement is physical, restart-safe, and bounded.
- **Dependencies:** STORAGE-01, LAB-08, LAB-09, INFO-01, ADRs 0010, 0012, 0024, 0026, and the logistics specification.
- **Authoritative sources to inspect:** `docs/systems/logistics.md`, `docs/systems/storage.md`, `docs/systems/transportation.md`, `docs/world-save-safety.md`, ADRs 0010, 0012, and 0026, and the LAB-08 and LAB-09 reports.
- **Existing-mod candidates:** Create, Pipez, Integrated Dynamics with Integrated Tunnels, the selected storage provider, and a selected rail or vehicle freight provider.
- **Custom-code gate:** A custom shipment ledger is allowed only if existing physical transfer and route APIs cannot provide idempotent reservation, cargo, and receiving states. Require ADR 0033 before code.
- **Explicit non-goals:** No magical remote withdrawal, arbitrary cross-dimensional shipment, invisible cargo loss, infinite reservation, or permanent chunk loading.
- **Acceptance criteria:**
  1. A shipment has source, destination, manifest, route, reservation, status, and receiving state.
  2. Physical goods occupy a source, cargo, buffer, or destination at every stage.
  3. Replaying a request or restart does not duplicate or consume cargo twice.
  4. Failed routes release or preserve cargo with an actionable reason.
  5. Cross-dimensional movement is rejected unless an approved depot exists.
- **Test requirements:** Request lifecycle, reservation, loading, transit, receiving, cancellation, restart at every transition, duplicate request, route damage, malformed manifest, and server-only outcome checks.
- **Removal/migration risks:** Shipment records and reserved stacks cannot be deleted with a mod update. Reconcile or cancel all active shipments and preserve physical cargo before changing the provider.

## M6: Transportation

### TRANS-01: Deliver explicit routes and physical transport services [Planned]

- **Objective:** Create route registration and service validation around the selected rail, road, horse, boat, and vehicle tools without continuous world scanning.
- **Dependencies:** LOGISTICS-01, LAB-09, INFO-01, LAB-03, ADRs 0010, 0013, 0018, 0024, and the transportation specification.
- **Authoritative sources to inspect:** `docs/systems/transportation.md`, `docs/systems/logistics.md`, `docs/systems/settlements.md`, `docs/systems/animals.md`, ADRs 0010, 0013, 0018, and the LAB-09 report.
- **Existing-mod candidates:** Create rail, Extended Rails, vanilla minecarts, horses, boats, and the selected vehicle provider from LAB-09.
- **Custom-code gate:** No global route graph or custom rail physics. A route adapter may be proposed only after supported endpoint events and bounded validation are exhausted and ADR 0033 is complete.
- **Explicit non-goals:** No automatic route discovery, distance-only service, instant travel, permanent chunk loading, or requirement to register ordinary walking.
- **Acceptance criteria:**
  1. Routes use explicit stable markers, bounded validation, cached results, and visible stale reasons.
  2. Rail can serve repeated fixed routes without making all other modes invalid.
  3. Road, boat, horse, and walking contexts remain meaningful.
  4. Infrastructure damage produces a safe false negative and another travel option.
  5. Route records are versioned and contain endpoint, mode, service, and validation references.
- **Test requirements:** Road survey, rail graph bound, disconnected route, station removal, invalidation, cache rebuild, restart, multiplayer permissions, cross-dimension rejection, and no-scan performance review.
- **Removal/migration risks:** Route records point to physical markers and services. Missing endpoints must produce an unavailable state, not a coordinate teleport or deleted route history.

### TRANS-02: Deliver earned passenger services and fast travel [Planned]

- **Objective:** Configure or implement a route-gated convenience that lets a player skip established journeys without making geography irrelevant.
- **Dependencies:** TRANS-01, INFO-01, LAB-11, ADRs 0018, 0031, and the transportation specification.
- **Authoritative sources to inspect:** `docs/systems/transportation.md`, `docs/systems/exploration-and-regions.md`, `docs/world-save-safety.md`, ADRs 0010, 0013, 0018, 0028, and 0031, and the selected travel provider documentation.
- **Existing-mod candidates:** Waystones, Travel Anchors, Create passenger services, rail station providers, and any existing route-aware fast-travel tool that passes the LAB reports.
- **Custom-code gate:** Prefer configuration and a public integration. A companion passenger service requires ADR 0033 evidence and must not create a coordinate-only teleport.
- **Explicit non-goals:** No unrestricted waypoint menu, admin command replacement, free global teleport, route registration by distance alone, or cross-dimensional bypass.
- **Acceptance criteria:**
  1. Fast travel requires discovered endpoints, physical travel, suitable infrastructure, explicit registration, bounded validation, and a current service.
  2. A stale or damaged route denies the skip with an actionable reason.
  3. The service points to route and endpoint IDs, not only coordinates.
  4. Walking and ordinary transport remain usable without registration.
  5. Fares, capacity, time, and cooldown are data-driven and cannot replace the route proof.
- **Test requirements:** Unmet prerequisite matrix, valid skip, route damage, endpoint removal, restart, duplicate request, permission denial, solo route, multiplayer shared route, and client/server authority.
- **Removal/migration risks:** Fast-travel grants and route references can outlive the provider. Migrate them to unavailable or revalidation states and never silently preserve a stale teleport permission.

## M7: Masteries and equipment

### PROG-01: Deliver evidence-based capability and mastery progression [Planned]

- **Objective:** Build the capability web and configurable mastery loadout around breadth, discovery, projects, and technique rather than repetitive action counts.
- **Dependencies:** INFO-01, BUILD-02, TRANS-01, LAB-12, ADRs 0003, 0004, 0022, 0026, and the mastery specification.
- **Authoritative sources to inspect:** `docs/systems/mastery.md`, `docs/systems/projects.md`, `docs/systems/exploration-and-regions.md`, `docs/design-principles.md`, ADRs 0003, 0004, 0022, 0026, and the LAB-12 gap report.
- **Existing-mod candidates:** Project MMO, LevelZ, FTB Quests, Origins-style capability providers, and the selected pack tools that already expose evidence or skill state.
- **Custom-code gate:** Custom mastery or capability code requires a complete ADR 0033 gap analysis. No custom code is justified merely because a level or quest system is familiar.
- **Explicit non-goals:** No raw block-count XP, irreversible class choice, mandatory mastery for ordinary building, recurring respec tax, or client-owned outcome.
- **Acceptance criteria:**
  1. Learning is permanent and inactive mastery progress is retained.
  2. One Focus and two Supporting slots are configurable without XP loss.
  3. Evidence is idempotent and attributable to meaningful breadth, discovery, projects, or technique.
  4. Each capability has a plain-language explanation and Field Journal entry.
  5. Vanilla actions remain usable without a mastery.
- **Test requirements:** Evidence duplication, inactive progress, loadout switch, restart, death or copy behaviour, client packet authority, solo path, multiplayer contribution, and rejection of repetition counters.
- **Removal/migration risks:** Mastery IDs, progress, and loadout state are persistent. Removing a mastery needs an alias, preserved history, or explicit unavailable state without deleting unrelated learning.

### PROG-02: Deliver long-lived equipment condition and specialisation [Planned]

- **Objective:** Provide recoverable equipment identity, condition, repair, and item-level specialisation using an existing provider where possible and a narrow companion only for proven gaps.
- **Dependencies:** PROG-01, BUILD-01, FOOD-01, LAB-03, LAB-12, ADRs 0005, 0006, 0026, and the equipment specification.
- **Authoritative sources to inspect:** `docs/systems/equipment.md`, `docs/systems/mastery.md`, `docs/world-save-safety.md`, ADRs 0005, 0006, 0026, 0033, and the LAB-03 and LAB-12 reports.
- **Existing-mod candidates:** Tetra, Silent Gear, tool condition and repair providers, Create or vanilla repair interactions, and the selected Matcha equipment rules.
- **Custom-code gate:** No custom item component or durability interception until candidate APIs, configuration, datapack recipes, and adapter options are documented. Any new persistent component requires schema, migration, and duplication tests.
- **Explicit non-goals:** No item deletion at zero condition, max-condition decay spiral, universal perfect tool, irreversible path switch, or final texture before asset approval.
- **Acceptance criteria:**
  1. A broken item remains present, identifiable, and unusable until repair.
  2. Field repair, workshop repair, and reforge rules are visible and data-driven.
  3. Item identity and prior path progress survive movement, storage, trade, drop, restart, and repair.
  4. Vanilla tools remain usable where no selected condition system applies.
  5. The server owns all condition and reforge outcomes.
- **Test requirements:** Codec round trip, invalid data, prior-version migration, zero-condition transition, repair atomicity, reforge preservation, container and trade movement, duplicate requests, restart, and vanilla regression.
- **Removal/migration risks:** Item components and registry IDs are expensive to change after persistent release. Retain aliases and recovery tooling before replacing a provider or renaming any equipment identifier.

## M8: Settlements and villagers

### SETTLE-01: Deliver free-form settlement registration and functional building graphs [Planned]

- **Objective:** Let players register useful residences, workplaces, warehouses, workshops, stations, and outposts without requiring a style or blueprint.
- **Dependencies:** BUILD-01, BUILD-02, TRANS-01, STORAGE-01, INFO-01, LAB-04, LAB-12, ADRs 0009, 0032, and the settlements specification.
- **Authoritative sources to inspect:** `docs/systems/settlements.md`, `docs/systems/projects.md`, `docs/systems/transportation.md`, `docs/design-principles.md`, ADRs 0009, 0010, 0016, 0026, 0032, and the LAB-04 and LAB-12 reports.
- **Existing-mod candidates:** MineColonies, MCA Reborn, Villager Recruits, Structurize, and existing claim or building registration providers with authoritative 26.2 evidence.
- **Custom-code gate:** A custom settlement graph requires ADR 0033 and must prove that existing claim, colony, and building tools cannot express function without imposing style or a large simulation.
- **Explicit non-goals:** No aesthetic score, mandatory blueprint, circular settlement scan, automatic natural-village conversion, or permanent chunk loading.
- **Acceptance criteria:**
  1. Ordinary building remains valid outside a charter.
  2. Registration names function, capacity, access, and connections rather than architectural theme.
  3. Graph membership uses explicit nodes and bounded edges with cached validation.
  4. Existing natural villages remain unchanged until a player-confirmed import workflow exists.
  5. Invalid function removes only the service and preserves the physical build.
- **Test requirements:** Registration, invalid function, split or rename, outpost edge, route invalidation, natural village non-import, bounded volume, restart, migration, and dedicated-server performance.
- **Removal/migration risks:** Settlement IDs, building references, and graph edges are world data. Do not remove a provider without preserving charters, marking services unavailable, and testing a migration on a disposable copy.

### SETTLE-02: Deliver villager careers, migration, and institutional knowledge [Planned]

- **Objective:** Make villagers persistent people with careers, mentorship, migration, safe unloaded simulation, and knowledge that can survive teaching.
- **Dependencies:** SETTLE-01, PROG-01, FOOD-02, TRANS-01, INFO-01, LAB-12, ADR 0007, and the villagers specification.
- **Authoritative sources to inspect:** `docs/systems/villagers.md`, `docs/systems/settlements.md`, `docs/systems/animals.md`, `docs/world-save-safety.md`, ADRs 0007, 0009, 0016, 0026, and the LAB-12 report.
- **Existing-mod candidates:** MineColonies, MCA Reborn, Villager Recruits, Easy Villagers, and career or migration providers with public data and safe lifecycle contracts.
- **Custom-code gate:** No custom villager career state until existing profession, colony, and migration APIs are evaluated. A companion system requires versioned entity data, bounded simulation, and ADR 0033 approval.
- **Explicit non-goals:** No arbitrary off-screen death, ordinary breeding as the only population route, infinite villager simulation, disposable vending-machine careers, or multiplayer-only mentorship.
- **Acceptance criteria:**
  1. Career, home, workplace, rank, mentor, apprentice, techniques, and current state are persistent and inspectable.
  2. Loaded hazards have visible causes and a recoverable downed state where practical.
  3. Unloaded simulation uses a fixed budget and cannot invent random death.
  4. Taught institutional knowledge can survive an individual, while untaught knowledge has an explicit loss rule.
  5. Solo play can host a complete career and migration path.
- **Test requirements:** Entity schema round trip, migration, loaded cause, unloaded safe simulation, death and rescue, mentorship transfer, reassignment, restart, duplication resistance, and no-force-load performance.
- **Removal/migration risks:** Villager attachments and relationships can reference settlement and technique IDs. A provider change must preserve people as physical entities and move unresolved links to a visible reassignment state.

## M9: Exploration and danger

### EXP-01: Deliver regions, discovery, and meaningful structure outcomes [Planned]

- **Objective:** Make regions and structures teach new possibilities, connect to people and trade, and reduce repeated travel through knowledge and infrastructure rather than loot farming.
- **Dependencies:** SETTLE-01, FOOD-01, TRANS-01, INFO-01, LAB-10, LAB-11, ADRs 0015, 0028, and the exploration specification.
- **Authoritative sources to inspect:** `docs/systems/exploration-and-regions.md`, `docs/systems/projects.md`, `docs/systems/food-and-alchemy.md`, `docs/systems/transportation.md`, ADRs 0015, 0018, 0028, 0030, and the LAB-10 and LAB-11 reports.
- **Existing-mod candidates:** YUNG's Better Structures, Structory, Towns and Towers, Terralith where compatible, structure or map providers, and the selected adventure or quest provider.
- **Custom-code gate:** Prefer existing structures, datapack tags, loot, advancements, and Field Journal data. A region or discovery ledger requires ADR 0033 evidence if no existing provider can supply stable claims.
- **Explicit non-goals:** No worldgen overhaul by default, rare structure farming as the main economy, essential capability hidden behind one structure, or automatic chunk-wide discovery scans.
- **Acceptance criteria:**
  1. Region identity records ecological, cultural, economic, and knowledge dimensions with inspectable descriptions.
  2. Discovery is a meaningful observation, not merely entering a chunk.
  3. Unique outcomes are bounded and not endlessly duplicated by repeat visits.
  4. A missing structure or declined opportunity leaves a safe alternate route.
  5. The Field Journal explains discovered processes and remaining uncertainty.
- **Test requirements:** First discovery, repeat visit, unique reward claim, missing structure, alternate route, multiplayer sharing, save/reload, and structure density review.
- **Removal/migration risks:** Region claims and structure outcomes become persistent world knowledge. Removing a source must preserve the claim and provide a readable unavailable or migrated result.

### EXP-02: Deliver local danger without global enemy scaling [Planned]

- **Objective:** Use bounded regional, structural, environmental, and objective-based danger while preserving source mob balance elsewhere.
- **Dependencies:** EXP-01, FOOD-02, TRANS-01, INFO-01, LAB-10, LAB-11, ADRs 0028 and 0030.
- **Authoritative sources to inspect:** `docs/systems/exploration-and-regions.md`, `docs/systems/transportation.md`, `docs/systems/seasons-and-weather.md`, `docs/design-principles.md`, ADR 0030, and candidate encounter-mod documentation.
- **Existing-mod candidates:** Better Combat, Combatify, Simply Swords, selected adventure encounter settings, vanilla difficulty, and regional hazard providers that expose bounded configuration.
- **Custom-code gate:** No global enemy modifier. A local encounter adapter requires a documented scope, source, version guard, fallback, and ADR 0033 review.
- **Explicit non-goals:** No world-age multiplier, player-level multiplier, dimension-wide global scaling, hidden combat level, or essential survival gate.
- **Acceptance criteria:**
  1. Vanilla and ordinary modded entities retain source values outside documented local situations.
  2. Local danger names scope, duration, affected entities, reason, and mitigation.
  3. Warnings use text, icons, and non-colour cues.
  4. Missing adventure rewards do not make ordinary building, shelter, food, or travel impossible.
  5. Local modifiers are data-driven and bounded.
- **Test requirements:** Same mob inside and outside scope, world-age regression, multiplayer player-level regression, missing provider, local hazard expiry, client warning, and dedicated-server authority.
- **Removal/migration risks:** Persisted encounter records can retain modifiers after a provider is removed. Expire or migrate them to neutral state and never leave a global attribute change behind.

## M10: Blueprints and construction contracts

### BLUE-01: Deliver optional blueprint workflows [Planned]

- **Objective:** Make blueprint preview, material planning, and player-authored sharing useful without making a blueprint part of ordinary building permission.
- **LAB-04 evidence (read before starting):** LAB-04 selected **Litematica + MaLiLib** as PILOT for the Ghost Plan level: it supports `.litematic` and `.schem`, previews, and material lists, so the custom-code gate below is firmly closed on rendering and file format. **Simple Blueprints was rejected** for gating placement behind a creative-only `setblock` path, and **AutoBuild** is NEEDS_MORE_EVIDENCE because it can overreach into printing, which is this ticket's explicit non-goal. PILOT means server-side evidence passed; client and multiplayer verification is outstanding and nothing is in `pack/` yet.
- **Dependencies:** SETTLE-01, BUILD-01, INFO-01, LAB-04, ADR 0032, and the projects specification.
- **Authoritative sources to inspect:** `docs/systems/projects.md`, `docs/systems/settlements.md`, `docs/design-principles.md`, ADRs 0009, 0026, and 0032, and the LAB-04 report.
- **Existing-mod candidates:** Litematica, Axiom, WorldEdit, Structurize, and the candidate selected by LAB-04.
- **Custom-code gate:** No custom renderer or file format. A companion bridge requires evidence that existing formats and public APIs cannot provide the required preview or functional metadata.
- **Explicit non-goals:** No canonical style, automated construction, mandatory schematic, exact palette requirement, or redistribution of unlicensed plans.
- **Acceptance criteria:**
  1. Blueprint use is optional and can be disabled without affecting ordinary building.
  2. Player-authored plans preserve provenance and do not grant server-side block authority.
  3. Missing mappings and unsupported formats are visible and non-destructive.
  4. Material estimates are bounded and cannot become a second physical inventory.
  5. The Field Journal explains manual and blueprint-assisted paths equally.
- **Test requirements:** Preview, import, missing block, malformed file, permissions, client-only absence, material estimate bounds, hand-built equivalent, and provenance review.
- **Removal/migration risks:** Plan files may reference removed blocks or providers. Preserve physical builds, mark plans unavailable, and avoid storing a plan as the only evidence of a completed contract.

### BLUE-02: Deliver style-independent construction contracts [Planned]

- **Objective:** Let projects request functional outcomes such as a safe residence, bridge, greenhouse, station, or warehouse without prescribing visual style.
- **Dependencies:** BLUE-01, SETTLE-01, BUILD-02, TRANS-01, FOOD-02, INFO-01, ADRs 0028, 0032, and the projects specification.
- **Authoritative sources to inspect:** `docs/systems/projects.md`, `docs/systems/settlements.md`, `docs/systems/seasons-and-weather.md`, `docs/design-principles.md`, ADRs 0009, 0015, 0028, and 0032.
- **Existing-mod candidates:** FTB Quests, Create contracts or schematics, Structurize, MineColonies building rules, and the selected project or blueprint provider.
- **Custom-code gate:** Prefer data-defined objectives, existing task predicates, and functional block tags. A custom contract validator needs ADR 0033 and bounded server-side checks.
- **Explicit non-goals:** No aesthetic score, prescribed theme, block-count reward, blueprint-only completion, or quest chain that blocks ordinary survival.
- **Acceptance criteria:**
  1. Contract criteria name function, access, capacity, safety, and relevant connections.
  2. Multiple palettes and layouts can satisfy the same declared purpose.
  3. A declined or abandoned contract leaves physical work and ordinary play intact.
  4. Contributions are attributed to material work or knowledge, not proximity.
  5. Completion, failure, reward, and migration states are versioned and explainable.
- **Test requirements:** Two visually different valid builds, invalid function, substitution, abandonment, multiplayer contribution, solo completion, restart, and server-authoritative validation.
- **Removal/migration risks:** Contract IDs and validation references can survive a provider update. Migrate active contracts to paused or legacy state rather than declaring an unrelated build complete.

## M11: Economy and regional networks

### ECON-01: Deliver physical currency, shops, and contracts [Planned]

- **Objective:** Provide an exchange layer with physical and compact currency, free player pricing, bounded NPC willingness, and contracts that represent real goods.
- **LAB-05/LAB-07 evidence (read before starting):** LAB-05 established that Matcha owns food, hunger, healing, and food effects, which bounds what an exchange layer may re-price or re-effect. LAB-07 overturned the only REPLACE classification: no surveyed storage mod indexes containers server-authoritatively without wireless access or data loss, so contracts and shops cannot assume an existing indexed-warehouse substrate. Physical conservation must be designed against real containers.
- **Dependencies:** STORAGE-01, LOGISTICS-01, SETTLE-01, FOOD-01, PROG-01, INFO-01, LAB-05, LAB-07, and the economy specification.
- **Authoritative sources to inspect:** `docs/systems/economy.md`, `docs/systems/storage.md`, `docs/systems/logistics.md`, `docs/systems/settlements.md`, ADR 0008, ADR 0017, and the LAB reports for storage, food, and transport.
- **Existing-mod candidates:** Numismatic Overhaul, Lightman's Currency, player-shop providers, villager market providers, Create trading features, and vanilla trading where compatible.
- **Custom-code gate:** No custom economy or second hidden currency until existing currency and shop APIs fail to express physical conservation, free pricing, willingness, and contract state. ADR 0033 is required for a companion economy.
- **Explicit non-goals:** No forced player price, infinite demand sink, remote item creation, pay-to-unlock essential capability, or unbounded banking simulator.
- **Acceptance criteria:**
  1. Physical Obols remain transferable and Coin Purse balances remain compact, atomic, and withdrawable.
  2. Player prices are free within safe data-defined bounds and NPC willingness can decline a sale.
  3. Contracts reserve physical goods or payment and expose expiry, substitutes, and consequences.
  4. Solo players can fulfil useful contracts through gathering, craft, preservation, import, or direct delivery.
  5. The Field Journal explains prices, refusal, reservation, and failure.
- **Test requirements:** Deposit and withdrawal conservation, stale shop view, declined sale, contract reservation, restart, duplicate transaction, malformed currency, multiplayer permissions, and client/server authority.
- **Removal/migration risks:** Currency and contract records affect inventories and world economy. Never remove a currency provider without preserving balances, recovering physical tokens, and migrating active contracts.

### ECON-02: Deliver regional demand and network exchange [Planned]

- **Objective:** Connect settlements, routes, food, storage, villagers, and logistics into bounded regional supply and demand without simulating a global market.
- **Dependencies:** ECON-01, LOGISTICS-01, TRANS-01, SETTLE-02, EXP-01, FOOD-01, and the economy, logistics, and villagers specifications.
- **Authoritative sources to inspect:** `docs/systems/economy.md`, `docs/systems/logistics.md`, `docs/systems/settlements.md`, `docs/systems/villagers.md`, `docs/systems/exploration-and-regions.md`, ADRs 0009, 0010, 0012, and the relevant LAB reports.
- **Existing-mod candidates:** Create logistics and trading, the selected warehouse and transport providers, Lightman's Currency or Numismatic Overhaul, villager market providers, and regional trade or contract mods with authoritative baseline evidence.
- **Custom-code gate:** A cross-mod market or category adapter requires ADR 0033. Use data-defined categories and existing shipment or shop contracts before creating a new network authority.
- **Explicit non-goals:** No universal warehouse, infinite demand, global auction house, household-by-household simulation, or multiplayer requirement.
- **Acceptance criteria:**
  1. Demand is category-based, bounded, visible, and tied to a settlement or region.
  2. Supply and delivery use physical goods, route service, reservation, and receiving state.
  3. Geography matters for first access while cultivation, preservation, trade, and transport reduce repeated inconvenience.
  4. Surplus changes urgency without silently deleting goods.
  5. Network records are dimension-scoped unless an explicit portal depot is accepted.
- **Test requirements:** Category demand, substitute, surplus, route failure, shipment restart, cross-dimension rejection, contract expiry, solo fulfilment, and bounded simulation performance.
- **Removal/migration risks:** Regional demand and route references are persistent. Removing a settlement, route, or provider must pause or cancel commitments and preserve physical goods and readable history.

## M12: World chronicle and long-term play

### CHRON-01: Deliver the world chronicle and significant history [Planned]

- **Objective:** Record significant settlements, routes, discoveries, careers, projects, migrations, and notable item histories without logging every routine action.
- **Dependencies:** INFO-01, SETTLE-02, TRANS-01, EXP-01, BLUE-02, ECON-02, PROG-02, and the world chronicle specification.
- **Authoritative sources to inspect:** `docs/systems/world-chronicle.md`, `docs/systems/projects.md`, `docs/systems/settlements.md`, `docs/systems/villagers.md`, `docs/systems/equipment.md`, `docs/world-save-safety.md`, ADRs 0007, 0010, 0020, 0022, and the relevant milestone reports.
- **Existing-mod candidates:** FTB Quests, Better Advancements, Advancement Plaques, Patchouli or Modonomicon for presentation, and read-only export or history tools where an official source exists.
- **Custom-code gate:** A custom chronicle is allowed only if existing advancement, quest, and history surfaces cannot provide significant, deduplicated, permission-aware records. It requires ADR 0033 and a bounded persistent schema.
- **Explicit non-goals:** No surveillance feed, block-by-block log, power reward for every entry, invisible history rewrite, or client-created fact.
- **Acceptance criteria:**
  1. Significant events have stable IDs, schema versions, actors, place, time, fact, provenance, and references.
  2. Duplicate retries do not create duplicate entries.
  3. Missing entities leave a readable last-known reference.
  4. Pages, filters, pins, privacy, and summaries are bounded and accessible.
  5. The Field Journal explains what is and is not recorded.
- **Test requirements:** Event significance, duplicate event, malformed candidate, missing reference, permission filtering, page bounds, restart, compaction protection, schema migration, and client projection checks.
- **Removal/migration risks:** Chronicle history should outlive many feature providers. Preserve immutable facts and replace missing links with legacy labels rather than deleting the world story.

### CHRON-02: Deliver long-term save, removal, and release hardening [Planned]

- **Objective:** Validate that a long-lived Many Roads Home world can survive pack updates, optional-mod removal, migration, and years of bounded history.
- **Dependencies:** CHRON-01, every persistent milestone, PACK-01, ADRs 0020, 0023, 0025, 0027, 0033, and 0034, plus `docs/world-save-safety.md`.
- **Authoritative sources to inspect:** `docs/world-save-safety.md`, `docs/dependency-baseline.md`, `docs/architecture.md`, all system specifications with persistent data, ADRs 0020, 0023, 0025, 0027, 0033, and 0034, and the Packwiz profile and provenance records.
- **Existing-mod candidates:** Packwiz export and validation tooling, Global Packs or its replacement utility, vanilla backup and recovery procedures, WorldEdit or another read-only inspection tool, and the selected providers' official migration guidance.
- **Custom-code gate:** No new gameplay code. Migration tooling may be added only for a named schema or removal case with a reviewed gap analysis, preserved source data, and disposable-world evidence.
- **Explicit non-goals:** No live-world test, automatic update, silent data discard, public release, broad dependency upgrade, or cleanup of preserved prototype code.
- **Acceptance criteria:**
  1. Each persistent record has a schema version, serializer, validation, migration, failure, and recovery behaviour.
  2. A cloned disposable world survives selected dependency update, removal, and rollback scenarios.
  3. Invalid or partial migration preserves recoverable source data and reports an actionable failure.
  4. Packwiz metadata, third-party notices, Matcha provenance, and utility licence status are release-auditable.
  5. A release checklist distinguishes pack, companion, upstream, and historical prototype ownership.
- **Test requirements:** Full backup and restore, save/reload, migration from prior fixtures, interrupted migration, duplicate prevention, provider absence, registry and resource-path review, dedicated-server startup, and long-run bounded-history test.
- **Removal/migration risks:** This ticket exists because every later dependency and identifier can become a save obligation. A failed migration is a release blocker. Do not open a real survival world or claim release readiness until the disposable-world evidence is complete.

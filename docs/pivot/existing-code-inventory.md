# Existing-code inventory

**Pivot:** Many Roads Home modpack-first pivot  
**Branch:** `modpack-first`  
**Review date:** 2026-08-28  
**Status:** inventory only. No source, resource, script, pack, or document outside this file was changed by this worker.

## Scope and reading method

The run brief in `.pivot-context.md` was read first. I then ran the required recursive source inventory and inspected the implementation, test, tooling, pack, and design records rather than classifying from filenames alone:

```sh
find src tools scripts docs -type f | sort
```

The observed tree contains **294 Java source files**, not the approximately 261 stated in the run prompt:

| Area | Observed files | Meaningful contents |
|---|---:|---|
| `src` | 302 | 182 common Java classes, one client class, 32 unit-test classes, 9 GameTest classes, 37 asset-tool classes, resources, and the placeholder asset |
| `tools` | 166 | the standalone Matcha audit project, 32 production classes, one test class, and 32 deterministic fixture files plus build support |
| `scripts` | 11 | acquisition, disposable-world installation, audit, Packwiz export, validation, and dependency reporting |
| `docs` | 88 before this output | design, ADR, system specifications, research, audit evidence, playtest, asset, and pivot records |
| `pack` | 7 | Packwiz metadata and pinned Matcha, Fabric API, and Global Packs inputs |
| `generated` | 21 | deterministic Matcha 1.12 audit reports |
| `art` | 10 | palette source plus documented empty placeholder directories |

Empty directories and `.gitkeep` markers are called out where they communicate a source boundary. Gradle caches, `build/`, `run/`, ordinary runtime logs, and editor state are operational output rather than meaningful implementation entries. The ignored third-party Matcha archive under `vendor/matcha/` is called out separately because it is a licensing and provenance boundary.

## Classification vocabulary

- **KEEP:** retain as a sound foundation in the companion, pack, tools, or documentation boundary named below.
- **ADAPT:** retain the useful concept or safety contract, but change ownership, wiring, namespace, data location, or implementation to fit the pack-first design.
- **REPLACE:** the current implementation is a generic mechanic that should be supplied by a maintained third-party mod. Keep migration tests or an adapter only until the replacement is selected and proven.
- **DEFER:** intentional design, test, or placeholder work for a later ticket. It is not evidence that the mechanic is currently implemented.
- **RETIRE:** generated, stale, or duplicate output that should not be part of the future source of truth. Removal requires its own safe cleanup change.
- **UNKNOWN_PENDING_REVIEW:** evidence is insufficient to make a safe ownership decision. Do not delete or rewrite the entry until the named review is complete.

The detailed records use grouped paths when every listed file has the same responsibility, build status, test coverage, destination, and decision. Brace groups expand to one exact file per member. This keeps all meaningful classes visible without pretending that a value object has a different migration plan from its sibling value objects.

## Classification summary

Counts below are **decision records**, not raw file counts. A decision record may expand to several exact paths, which are all listed in the sections that follow.

| Classification | Decision records | Meaning in this review |
|---|---:|---|
| KEEP | 35 | Architecture, safety contracts, connective domain concepts, Matcha isolation, tests, audit tooling, and authoritative design records |
| ADAPT | 16 | Existing custom code or metadata that can survive only after ownership and namespace changes |
| REPLACE | 1 | Generic searchable warehouse/index implementation that duplicates the intended third-party storage role |
| DEFER | 1 | Future gameplay designs, not-yet-built integrations, and empty asset or lab placeholders |
| RETIRE | 1 | Ignored exported distribution output |
| UNKNOWN_PENDING_REVIEW | 0 | No existing artifact is left without an interim disposition. Candidate ownership remains provisional where noted. |
| **Total** | **54** | Every meaningful inventory decision record is represented below |

These totals count records, not classes. For example, the 27 Matcha adapter classes are one KEEP record because they form one deliberately isolated compatibility boundary. The raw Java counts and exact paths are still listed below. The provisional-owner and negative-inventory tables use classifications as explanatory state labels, but are not additional detailed decision records.

## Provisional third-party ownership map

`docs/mod-research/candidate-matrix.csv` is present in this checkout and was read in full. It contains 58 candidate rows with exact project IDs, version IDs, Minecraft/loader claims, licensing, persistence, conflict, and decision fields. The following mappings are therefore research dispositions, not completed adoption decisions. The current authoritative ownership notes are `docs/architecture/ownership-matrix.csv`, `docs/modpack-architecture.md`, ADR 0024, ADR 0026, and the laboratory queue in `labs/README.md`.

| Existing responsibility or design | Provisional owner | Existing-code decision | Evidence or next review |
|---|---|---|---|
| Recipe viewing | EMI or REI, exactly one | KEEP the narrow `GuideRecipeViewerCapability`; do not build a viewer | LAB-01 and 26.2 client/server test |
| Contextual block/entity information | Jade or equivalent | KEEP the Field Journal contract; ADAPT only a bridge | LAB-01 and exact 26.2 release verification |
| Mass placement and assisted construction | Effortless Building, AutoBuild, or equivalent | No current implementation found. DEFER replacement selection | LAB-02 and LAB-12. Server authority and real material cost are acceptance gates |
| Vein mining | Liteminer, MinersAdvantage, or equivalent | No current implementation found. DEFER | LAB-02. Check Matcha tool tiers and condition |
| Schematic preview | Litematica or equivalent | No current implementation found. DEFER | LAB-02 and LAB-12. `.schem` and `.litematic` need separate evidence |
| Cooking content | Farmer's Delight Refabricated or equivalent | No current implementation found. DEFER | LAB-03 against Matcha food content |
| Seasons and climate | Serene Seasons or equivalent | No current implementation found. DEFER | LAB-04. Keep only a future climate port |
| Searchable local storage | Tom's Simple Storage or equivalent | REPLACE the generic warehouse/index code after a safe migration plan | LAB-05. Vanilla containers remain physical truth |
| Physical item movement | Simple Copper Pipes or equivalent | No current implementation found. DEFER | LAB-06 |
| Rail physics | High-Speed Rail or equivalent | No current implementation found. DEFER | LAB-07 |
| Horse handling | Icy's Better Horses or equivalent | No current implementation found. DEFER | LAB-07 |
| Structures | One restrained structure pack | No current implementation found. DEFER | LAB-11 and ADR 0015 |
| Generic skill-tree presentation | Pufferfish's Skills or equivalent | KEEP pack-specific slot rules and progression concepts. No current UI exists | LAB-08. The generic UI, if selected, owns presentation |
| Equipment condition and repair | No complete generic owner established | KEEP the identity and nonbreaking model, ADAPT UI or hooks later | LAB-09. Matcha equipment/durability is overlap evidence, not an implementation owner |
| Mason career and institutional knowledge | No generic owner established | KEEP as companion connective progression | LAB-10. A villager API may provide hooks but not the pack model |
| Obol purse and cross-mod economy | No generic owner established | KEEP as companion connective economy | ADR 0008 and ADR 0017. Matcha `obols_currency` is an interaction to reconcile, not proof of equivalence |
| Matcha normalization | Companion compatibility boundary | KEEP strongly | ADR 0002, Matcha audit, and successful Matcha GameTest |
| Fast travel and freight | Companion integration over settlements and routes | DEFER | ADR 0012, ADR 0018, ADR 0031. Not a waystone or magical warehouse replacement |
| Blueprints | Optional blueprint library | DEFER | ADR 0032. Never a survival requirement and never redistributed without permission |

| Existing responsibility or design | Matrix candidate and current disposition | Existing-code decision | Required follow-up |
|---|---|---|---|
| Recipe viewing | REI `nfn13YXA`, `26.2.820+fabric [4o0NSIMj]`, `ADOPT_BASELINE`; EMI has no observed 26.2 Fabric release and is `DEFER` | KEEP the narrow guide port, no custom viewer | LAB-01 must verify one viewer and suppress duplicate guide noise |
| Contextual block/entity information | Jade `nvQzSEkH`, `26.2.11+fabric [ue8CO97w]`, `PILOT` | KEEP the Field Journal contract, adapt only a bridge | LAB-01 on client/server and Matcha item presentation |
| Mass building | AutoBuild `wzBO46L7`, `2.0.1+26.2 [Q7qeA1GB]`, `COMPATIBILITY_SPIKE`; Building Wands `XkisZUfp`, `PILOT`; Effortless Building `DYtfQEYj`, `PILOT`; Axiom is `REJECT` | No current implementation found. DEFER replacement selection | LAB-02 and LAB-12. Server authority, material cost, and ordinary placement are gates |
| Vein mining | Liteminer `VTnHoofC`, `4.1.2+26.2 [dK23TC0J]`, `PILOT`; MinersAdvantage `VcTmRh04`, `COMPATIBILITY_SPIKE` | No current implementation found. DEFER | LAB-02 against equipment condition, permissions, and Matcha tool tiers |
| Schematic preview | Litematica `bEpr0Arc`, `0.28.5 [Fhq3KCI8]`, `PILOT` | No current implementation found. DEFER | LAB-02/LAB-12. Keep Easy Place disabled until server policy is tested |
| Cooking content | Farmer's Delight Refabricated `7vxePowz`, `26.2-3.6.17 [7v050iYz]`, `ADOPT_BASELINE`; Cooking for Blockheads `vJnhuDde`, `COMPATIBILITY_SPIKE` | No current implementation found. DEFER | LAB-03 against Matcha recipes, ingredients, hunger, and healing |
| Seasons and climate | Serene Seasons `e0bNACJD`, `26.1.2.0.4 [13sXhUkI]`, `PILOT` | No current implementation found. DEFER | LAB-04. Keep climate abstraction rather than hardcoding the candidate |
| Searchable local storage | Tom's Simple Storage `XZNI4Cpy`, `26.2-2.11.2-fabric [9KkiCXs5]`, `COMPATIBILITY_SPIKE`; Storage Drawers `3bqn07Ul`, `PILOT` | REPLACE generic index after migration evidence | LAB-05 and explicit saved-data/removal plan |
| Physical item movement | Simple Copper Pipes `9r4ZkgSN`, `2.1.7-mc26.2 [CEH3bXSV]`, `COMPATIBILITY_SPIKE` | No current implementation found. DEFER | LAB-06 for local range, chunk unloading, and Matcha overlap |
| Rail physics | High-Speed Rail `d7PCSOkD`, `0.24.0+26.2 [UnL9JxtC]`, `COMPATIBILITY_SPIKE` | No current implementation found. DEFER | LAB-07 |
| Horse handling | Icy's Better Horses `XlUm5I57`, `1.2.0 [41Q9OmbL]`, `COMPATIBILITY_SPIKE`; Horseman `qIv5FhAA`, `1.7.5 [xU6ysEWF]`, `PILOT` | No current implementation found. DEFER | LAB-07 and mount progression/removal checks |
| Structures and spacing | Sparse Structures `qwvI41y9`, `3.1.4 [iPGkJT7H]`, `PILOT`; Structory `aKCwCJlY`, `1.3.17 [TUbwu7eG]`, `PILOT`; broad packs remain `DEFER` or `COMPATIBILITY_SPIKE` | No current implementation found. DEFER | LAB-11 on fresh worlds, density, loot, and removal |
| Generic skill UI | Pufferfish's Skills `hqQqvaa4`, `0.18.3 [GxnNQdFo]`, `COMPATIBILITY_SPIKE` with direct mastery overlap | KEEP pack-specific rules. No custom UI exists | LAB-08 before any presentation integration |
| Equipment repair UI | Simple Smithing Overhaul `U5TJjZc3`, `2.9.10+26.2-fabric [GG4f5wOP]`, `COMPATIBILITY_SPIKE` with direct equipment overlap | KEEP identity/nonbreaking model, ADAPT UI/hooks | LAB-09 |
| Villager hooks | Villager API `9Tp2Becg`, `1.26.7.1 [QmtHsEEy]`, `COMPATIBILITY_SPIKE` | KEEP Mason/career model, adapt hooks | LAB-10 |
| Performance | FerriteCore `uXXizFIs`, Lithium `gvQqBUqZ`, ImmediatelyFast `5ZwdcRci`, Mod Menu `mOgUt4GM`, and spark `l6YH9Als` are `ADOPT_BASELINE`; Entity Culling and Sodium are `PILOT` | No custom implementation found. DEFER pack adoption until assembled test | Performance lab and side-specific verification |
| Matcha loading | Global Packs `NRLPy2mk`, `26.2.0 [DqrPrUMp]`, `ADOPT_BASELINE`; Simple Resource Loader is `PILOT`; Paxi/Open Loader are `DEFER` | KEEP adapter and lock | Recheck license, client behavior, and world rollback |

The matrix's `ADOPT_BASELINE` values are not silently applied by this inventory. They are candidate research dispositions. The pack currently contains only the pinned baseline entries described below, and each future addition still needs the lab and license gates.

All common Java sources compiled under the pinned Java 25 root build. The current source uses mod id `forever` and package `dev.forever`. That identifier is not renamed by this inventory.

### [ADAPT] Common entrypoint and identity helpers

**Paths:**

- `src/main/java/dev/forever/ForeverMod.java`
- `src/main/java/dev/forever/core/ForeverIdentifiers.java`
- `src/main/java/dev/forever/core/data/SchemaVersioned.java`

**Current responsibility:** `ForeverMod` is the common initializer and invokes the seven existing systems plus Matcha compatibility. `ForeverIdentifiers` centralizes `forever:<path>` identifier construction. `SchemaVersioned` is the generic versioned-codec migration helper. The entrypoint is intentionally not a god object, while the identifiers and schema helper sit below every persistent feature.

**Build and tests:** Compiles in `compileJava`. `ForeverModTest` and `SchemaVersionedTest` exist. The root `build test` command passed with tasks up to date. ArchitectureRulesTest reported zero violations in the existing run.

**Possible duplicated mechanic:** None for the schema helper or initializer. Identifier construction can become a source of accidental duplication if the companion is split. The seven invoked systems include generic mechanics that may later leave the companion.

**Recommended destination:** Companion mod entrypoint and unchanged core helper. Pack composition should eventually invoke only the connective systems that survive the ownership matrix.

**Migration risk:** High for `SchemaVersioned` because changing codec semantics can invalidate every persistent record. Medium for `ForeverMod`. High for identifiers once shipped.

**Rationale:** Keep the migration helper and boundary discipline. Adapt the initializer only after ownership decisions are made. Do not create a second entrypoint or rename identifiers as part of the pivot.

### [KEEP] Matcha compatibility boundary

**Paths:** all 27 classes under `src/main/java/dev/forever/compat/matcha/`:

- `ForeverMatchaCompat.java`, `MatchaAdapter.java`, `MatchaAdapterStatus.java`, `MatchaAdapters.java`
- `MatchaBehaviorCapability.java`, `MatchaBehaviorObservation.java`, `MatchaBehaviorTranslation.java`, `MatchaCapability.java`
- `MatchaConfidence.java`, `MatchaDetectionEvidence.java`, `MatchaDetectionResult.java`, `MatchaDetectionStatus.java`
- `MatchaItemIdentityCapability.java`, `MatchaItemObservation.java`, `MatchaItemTranslation.java`, `MatchaPackMetadata.java`
- `MatchaPresence.java`, `MatchaProfile.java`, `MatchaRuntimeMarkers.java`, `MatchaServerEvidence.java`
- `MatchaSignalKind.java`, `MatchaSupportLevel.java`, `MatchaTranslationStatus.java`, `MatchaVersionDetector.java`
- `NoOpMatchaAdapter.java`, `VanillaItemFallback.java`, `VerifiedMatchaAdapter.java`

**Current responsibility:** Detect the official Matcha archive and runtime markers, normalize private Matcha identifiers into Forever concepts, publish a verified adapter or safe no-op, and provide neutral vanilla fallbacks. All private scoreboard names, function paths, advancement identities, item identities, custom model data, and hashes are isolated here as required by ADR 0002 and the compatibility contract.

**Build and tests:** Compiles in `compileJava`. `MatchaServerEvidenceTest`, `MatchaVersionDetectorTest`, and `MatchaDetectionGameTest` cover the boundary. The root GameTest run loaded Matcha signals and passed all runtime tests. It reported the adapter as `PRESENT_UNVERIFIED` in that runtime because locked archive hash and `pack.mcmeta` evidence were not available to the runtime detector, so this is not a claim of complete adapter verification.

**Possible duplicated mechanic:** Matcha itself supplies many mechanics classified in `docs/matcha-audit/system-classification.csv`, including tool tiers, substitutions, hunger, healing, food effects, equipment, durability, trades, Obols-related currency signals, recipes, structures, weather, and mod compatibility. The adapter must normalize these signals, not recreate them.

**Recommended destination:** Companion compatibility package, unchanged boundary. Matcha archive remains a Packwiz datapack/resource-pack dependency and is never a Gradle dependency.

**Migration risk:** Very high compatibility and licensing risk. `MatchaProfile.java` contains private identifiers and release fingerprints. `MatchaServerEvidence.java` contains bounded knowledge of server signals. `VerifiedMatchaAdapter.java` contains exact mapping behavior and model identities. These are the three most sensitive code files inside an otherwise stable boundary.

**Rationale:** This is precisely the kind of connective code the companion mod is allowed to own. Keep the isolation, no-op behavior, bounded evidence, and fail-safe publication. Recheck exact mappings after every Matcha pin change.

### [ADAPT] Client boundary

**Paths:** `src/client/java/dev/forever/client/ForeverClient.java`

**Current responsibility:** Minimal client initializer and logger boundary. There is no custom screen, recipe viewer, schematic renderer, or generic skill UI.

**Build and tests:** Compiles in `compileClientJava`. No dedicated client test exists. `runClient` was intentionally not run because this WSL environment has no usable display, and client verification is recorded as pending in `docs/compatibility/matcha-loading.md`.

**Possible duplicated mechanic:** A future custom guide, recipe viewer, schematic renderer, and skill UI would duplicate selected third-party client mods. None is present in this class.

**Recommended destination:** Unchanged companion client source set, later adapted to render only the Field Journal or narrow integrations that survive LAB-01 and later labs.

**Migration risk:** Medium. Client-only imports and entrypoint mistakes can break dedicated servers even when common code compiles.

**Rationale:** Retain the explicit source-set boundary. Do not add client UI until an ownership gap is documented.

## 2. Equipment system

The equipment code implements an identity-preserving condition, broken-state, repair, specialization, and reforging model. It is not a generic mass-placement or generic durability library. The closest third-party overlap is Matcha's audited equipment/durability behavior and a possible future smithing overhaul, but no candidate currently proves equivalence.

### [KEEP] Equipment domain and calculation

**Paths:** `src/main/java/dev/forever/core/equipment/EquipmentMath.java`

**Current responsibility:** Pure calculation for condition and repair/reforge values. It uses adapter records at the current package boundary but does not own Minecraft world mutation.

**Build and tests:** Compiles in `compileJava`. Covered by `EquipmentMathTest` and exercised indirectly by `EquipmentStateTest`.

**Possible duplicated mechanic:** Generic tool durability mods and Matcha durability/equipment classifications. No selected third-party mod guarantees Forever's identity-preserving broken state.

**Recommended destination:** Companion domain/application calculation, possibly moved behind a small service boundary when an actual third-party tool mod needs integration.

**Migration risk:** Medium for logic, high for balance values if the data contract changes.

**Rationale:** This is a connective identity rule, not a generic block mechanic. Keep it unless LAB-09 proves a complete compatible owner.

### [ADAPT] Equipment data, registration, and item integration

**Paths:**

- `src/main/java/dev/forever/core/equipment/adapter/EquipmentBalance.java`
- `src/main/java/dev/forever/core/equipment/adapter/EquipmentBalanceLoader.java`
- `src/main/java/dev/forever/core/equipment/adapter/EquipmentBalanceRegistry.java`
- `src/main/java/dev/forever/core/equipment/adapter/EquipmentComponents.java`
- `src/main/java/dev/forever/core/equipment/adapter/EquipmentDamageHandler.java`
- `src/main/java/dev/forever/core/equipment/adapter/EquipmentItem.java`
- `src/main/java/dev/forever/core/equipment/adapter/EquipmentOperationResult.java`
- `src/main/java/dev/forever/core/equipment/adapter/EquipmentService.java`
- `src/main/java/dev/forever/core/equipment/adapter/EquipmentState.java`
- `src/main/java/dev/forever/core/equipment/adapter/ForeverEquipment.java`

**Current responsibility:** Loads balance data, registers the persistent `forever:equipment_state` component and `forever:field_tool`, intercepts damage so zero condition does not delete an item, blocks broken-item use, displays condition, and performs field/workshop repair and reforging with described results.

**Build and tests:** Compiles in `compileJava`. `EquipmentBalanceTest`, `EquipmentStateTest`, `EquipmentMathTest`, and `EquipmentGameTest` cover it. The GameTest run passed its equipment tests. The `EquipmentState` codec is schema 2 with migration from schema 1.

**Possible duplicated mechanic:** Matcha `equipment` and `durability` classifications, vanilla durability/Mending, and a possible smithing-overhaul repair interface. No evidence establishes a third-party owner for the cross-mod nonbreaking identity rule.

**Recommended destination:** Companion integration for condition state, break prevention, and repair/reforge semantics. Move balance JSON to pack/datapack ownership when the companion no longer needs bundled defaults. `EquipmentItem` and the custom field tool should be retained only if LAB-09 finds no compatible external item/tool path.

**Migration risk:** Very high. `forever:equipment_state` is persistent and network-visible. `EquipmentState` carries UUID identity, condition, craftsmanship, active path, and retained path progress. `EquipmentDamageHandler` changes vanilla item destruction semantics. `EquipmentItem` and `forever:field_tool` are registry identities. Changing or removing them after a persistent release requires a migration and a broken-item recovery plan.

**Rationale:** Classify as ADAPT rather than REPLACE. The mechanic is a pack identity and repair policy, not just another durability bar. Keep the safety contract while testing whether a smithing or tool mod can own only presentation and interaction.

## 3. Mastery system

### [KEEP] Mastery domain concepts

**Paths:**

- `src/main/java/dev/forever/core/mastery/domain/EvidenceKind.java`
- `src/main/java/dev/forever/core/mastery/domain/LoadoutChange.java`
- `src/main/java/dev/forever/core/mastery/domain/LoadoutRules.java`
- `src/main/java/dev/forever/core/mastery/domain/MasteryDataException.java`
- `src/main/java/dev/forever/core/mastery/domain/MasteryProgress.java`
- `src/main/java/dev/forever/core/mastery/domain/ProgressionEvidence.java`
- `src/main/java/dev/forever/core/mastery/domain/ProgressionMilestone.java`
- `src/main/java/dev/forever/core/mastery/domain/SwitchContext.java`

**Current responsibility:** Pure records and rules for evidence-based progression, a one-Focus-plus-two-Supporting loadout, permanent progress, and free switching at approved contexts. The code intentionally does not award mastery merely for repeating raw actions.

**Build and tests:** Compiles in `compileJava`. Covered by `MasteryCodecTest`, `MasteryLoadoutTest`, `MasteryRegistryTest`, `ProspectorProgressionTest`, and the mastery GameTest.

**Possible duplicated mechanic:** Generic RPG or skill-tree mods may duplicate the presentation and generic action-XP layer. They do not automatically implement this pack's evidence and slot rules.

**Recommended destination:** Companion domain concepts and pack/datapack-defined progression content.

**Migration risk:** Medium for pure rules, high for progression identifiers and saved state once players have progress.

**Rationale:** The rules are pack-specific connective design. Keep them while allowing a selected third-party UI or progression framework to render or host them only after LAB-08.

### [ADAPT] Mastery runtime, persistence, and content loading

**Paths:**

- `src/main/java/dev/forever/core/mastery/adapter/ForeverMastery.java`
- `src/main/java/dev/forever/core/mastery/adapter/LoadoutSwitchResult.java`
- `src/main/java/dev/forever/core/mastery/adapter/MasteryAttachment.java`
- `src/main/java/dev/forever/core/mastery/adapter/MasteryDefinition.java`
- `src/main/java/dev/forever/core/mastery/adapter/MasteryIds.java`
- `src/main/java/dev/forever/core/mastery/adapter/MasteryLoadout.java`
- `src/main/java/dev/forever/core/mastery/adapter/MasteryLoadoutService.java`
- `src/main/java/dev/forever/core/mastery/adapter/MasteryProgression.java`
- `src/main/java/dev/forever/core/mastery/adapter/MasteryRegistry.java`
- `src/main/java/dev/forever/core/mastery/adapter/MasteryRegistryAccess.java`
- `src/main/java/dev/forever/core/mastery/adapter/MasteryRegistryLoader.java`
- `src/main/java/dev/forever/core/mastery/adapter/MasteryResourceReloadListener.java`
- `src/main/java/dev/forever/core/mastery/adapter/MasteryState.java`
- `src/main/java/dev/forever/core/mastery/adapter/ProgressionResult.java`
- `src/main/java/dev/forever/core/mastery/adapter/ProspectorEvidence.java`

**Current responsibility:** Registers the player attachment `forever:mastery_state`, loads mastery definitions, applies evidence and milestones idempotently, gates loadout switching to rest/home/workplace contexts, and exposes described progression outcomes. `MasteryState` is schema 2.

**Build and tests:** Compiles in `compileJava`. Unit and GameTest coverage passed. The architecture rules support the ongoing domain/application/adapter split and currently report zero violations.

**Possible duplicated mechanic:** Pufferfish's Skills or another generic skill UI/progression mod may own presentation. There is no selected 26.2 candidate in the repository, and no custom UI exists to replace.

**Recommended destination:** Companion integration for the pack-specific rules and state. Mastery definition JSON can become pack/datapack content. If a third-party progression framework is selected, adapt its hooks rather than moving the slot rules into a generic action-XP system.

**Migration risk:** High for the persistent attachment, `MasteryIds` values, translation keys, and definition IDs. The loader currently accepts only the `forever` namespace despite ADR 0022 requiring any namespace. This is a known adaptation defect tracked by FVR-A002.

**Rationale:** Keep the custom progression model, adapt ownership and loader extensibility. Do not classify it as a generic skill-tree replacement merely because a third-party UI may be selected.

## 4. Settlement system

### [KEEP] Settlement domain graph and validation concepts

**Paths:**

- `src/main/java/dev/forever/core/settlement/domain/FunctionalRequirement.java`
- `src/main/java/dev/forever/core/settlement/domain/MigrationRecord.java`
- `src/main/java/dev/forever/core/settlement/domain/MigrationStatus.java`
- `src/main/java/dev/forever/core/settlement/domain/SettlementBalance.java`
- `src/main/java/dev/forever/core/settlement/domain/SettlementCodecs.java`
- `src/main/java/dev/forever/core/settlement/domain/SettlementDataException.java`
- `src/main/java/dev/forever/core/settlement/domain/SettlementEdge.java`
- `src/main/java/dev/forever/core/settlement/domain/SettlementEdgeKind.java`
- `src/main/java/dev/forever/core/settlement/domain/SettlementMigrationProcedure.java`
- `src/main/java/dev/forever/core/settlement/domain/SettlementRole.java`
- `src/main/java/dev/forever/core/settlement/domain/ValidationIssue.java`
- `src/main/java/dev/forever/core/settlement/domain/ValidationResult.java`
- `src/main/java/dev/forever/core/settlement/domain/ValidationStatus.java`

**Current responsibility:** Pure settlement graph, functional requirement, migration, validation, and role concepts. They evaluate function and declared purpose rather than style, a fixed radius, or a mandatory blueprint.

**Build and tests:** Compiles in `compileJava`. Covered by settlement codec, graph, import, balance, and migration tests. Settlement persistence GameTests passed.

**Possible duplicated mechanic:** Claim, village, structure, or colony mods may overlap with settlement ownership or generation. No candidate provides the style-free functional graph and existing-village import model documented here.

**Recommended destination:** Companion domain model. Settlement balance and functional requirement content may move to datapack data.

**Migration risk:** High once graph edges, roles, and migration records are saved. Medium before release.

**Rationale:** This is a central pack identity concept and is not a generic structure-generation feature. Keep the domain even if external mods provide claims, villages, or optional structure content.

### [KEEP] Settlement adapter, saved state, and bounded world port

**Paths:**

- `src/main/java/dev/forever/core/settlement/adapter/BuildingBounds.java`
- `src/main/java/dev/forever/core/settlement/adapter/BuildingRegistrationService.java`
- `src/main/java/dev/forever/core/settlement/adapter/ForeverSettlement.java`
- `src/main/java/dev/forever/core/settlement/adapter/MigrationService.java`
- `src/main/java/dev/forever/core/settlement/adapter/MigrationSimulationResult.java`
- `src/main/java/dev/forever/core/settlement/adapter/RegisteredBuilding.java`
- `src/main/java/dev/forever/core/settlement/adapter/ResidenceValidator.java`
- `src/main/java/dev/forever/core/settlement/adapter/Settlement.java`
- `src/main/java/dev/forever/core/settlement/adapter/SettlementBalanceLoader.java`
- `src/main/java/dev/forever/core/settlement/adapter/SettlementCharterService.java`
- `src/main/java/dev/forever/core/settlement/adapter/SettlementGraph.java`
- `src/main/java/dev/forever/core/settlement/adapter/SettlementSavedData.java`
- `src/main/java/dev/forever/core/settlement/adapter/SettlementState.java`
- `src/main/java/dev/forever/core/settlement/adapter/SettlementWorldView.java`
- `src/main/java/dev/forever/core/settlement/adapter/VillageImportProposal.java`
- `src/main/java/dev/forever/core/settlement/adapter/VillageImportRecord.java`

**Current responsibility:** Server-authoritative explicit building registration, bounded residence validation, settlement graph persistence, village import proposals, and a `SettlementWorldView` port that avoids hardcoding one world implementation. `SettlementState` is schema 2 and `SettlementSavedData` owns `forever:settlements`.

**Build and tests:** Compiles in `compileJava`. `ResidenceValidatorTest`, `SettlementBalanceTest`, `SettlementCodecTest`, `SettlementGraphTest`, `SettlementImportTest`, `SettlementMigrationTest`, and settlement GameTests passed. The tests cover bounded queries and persistence/reload behavior.

**Possible duplicated mechanic:** Generic claims, colony management, mandatory blueprint construction, and structure generation. The current implementation does not generate structures and does not require a visual style.

**Recommended destination:** Companion mod. A future external claims or village mod may be an adapter dependency, but it must not replace the domain graph without an explicit decision.

**Migration risk:** Very high for `forever:settlements`, `SettlementState`, graph edges, imported-village records, and any route or economy data that later references settlement identities. Medium for the world-view port.

**Rationale:** The current code is more connective than generic. Keep the bounded server authority and data migration behavior. Adapt only after a candidate proves it can preserve free-form functional validation and safe removal.

## 5. Storage system

The storage system contains both a custom traveler cache and a generic searchable warehouse/index. These must not receive the same classification.

### [KEEP] Storage domain and application safety contracts

**Paths:**

- `src/main/java/dev/forever/core/storage/domain/CacheOperation.java`
- `src/main/java/dev/forever/core/storage/domain/CacheValidation.java`
- `src/main/java/dev/forever/core/storage/domain/IndexMutationResult.java`
- `src/main/java/dev/forever/core/storage/domain/PhysicalTransferResult.java`
- `src/main/java/dev/forever/core/storage/domain/StorageBalance.java`
- `src/main/java/dev/forever/core/storage/domain/StorageSafetyLimits.java`
- `src/main/java/dev/forever/core/storage/domain/WarehouseSearchRequest.java`
- `src/main/java/dev/forever/core/storage/application/StorageBalanceAccess.java`

**Current responsibility:** Pure described results, bounded cache and transfer rules, search request semantics, safety limits, and balance access. These contracts distinguish physical inventory from an index and reject unsafe recursive or unbounded behavior.

**Build and tests:** Compiles in `compileJava`. Storage behavior and codec tests passed. GameTests cover physical truth, no duplication, restart/decommission safety, and bounded behavior.

**Possible duplicated mechanic:** Generic storage indexing and searchable storage mods may duplicate the warehouse search contract. The physical transfer and safety contracts remain useful even if the index is replaced.

**Recommended destination:** Companion safety/domain boundary. Adapt the warehouse-specific records to the selected storage mod, or keep them as an integration contract.

**Migration risk:** High because result semantics and safety limits control duplication resistance. Medium for pure requests and balance access.

**Rationale:** Preserve the invariants even if the generic index goes away. A third-party storage mod must not become permission to make remote stock physically available everywhere.

### [REPLACE] Generic warehouse indexing and search

**Paths:**

- `src/main/java/dev/forever/core/storage/adapter/InventoryIndex.java`
- `src/main/java/dev/forever/core/storage/adapter/InventoryIndexEntry.java`
- `src/main/java/dev/forever/core/storage/adapter/InventoryIndexState.java`
- `src/main/java/dev/forever/core/storage/adapter/WarehouseController.java`
- `src/main/java/dev/forever/core/storage/adapter/WarehouseMutationResult.java`
- `src/main/java/dev/forever/core/storage/adapter/WarehouseRecord.java`
- `src/main/java/dev/forever/core/storage/adapter/WarehouseSavedData.java`
- `src/main/java/dev/forever/core/storage/adapter/WarehouseSearchPage.java`
- `src/main/java/dev/forever/core/storage/adapter/WarehouseSearchResult.java`
- `src/main/java/dev/forever/core/storage/adapter/WarehouseState.java`

**Current responsibility:** Maintains a searchable local warehouse/index over physical containers, saves warehouse state under the Forever data boundary, and exposes search and mutation operations. ADR 0021 identifies `WarehouseController` as an intentionally large adapter of roughly 430 lines.

**Build and tests:** Compiles in `compileJava`. `StorageBehaviorTest`, `StorageCodecTest`, and `StorageGameTest` passed. Passing tests establish the current safety behavior, not that this custom index is the right release owner.

**Possible duplicated mechanic:** Tom's Simple Storage or another mature local storage index. The candidate matrix now records Tom's Simple Storage as a 26.2 `COMPATIBILITY_SPIKE` and Storage Drawers as a `PILOT`; neither is adopted until LAB-05 proves physical truth, removal, and Matcha compatibility. Vanilla containers remain the source of physical truth.

**Recommended destination:** Replace with one tested third-party local index after LAB-05. Retain `StorageSafetyLimits`, physical transfer, reconciliation tests, and a narrow compatibility adapter. Do not silently delete saved warehouse data. The removal or migration plan must define what happens to index-only records and any physical containers.

**Migration risk:** **Highest storage risk.** `forever:storage/warehouses`, `WarehouseState`, index revisions, container references, and any remote-looking result can cause duplication or item loss if migrated incorrectly. `WarehouseController` is a large authority boundary and should not be rewritten during an unrelated pack pin change.

**Rationale:** This is the clearest existing implementation of a generic mechanic that the pivot expects a maintained mod to own. The safety and physical-truth contracts are valuable, but the custom searchable implementation should not remain merely because its tests pass.

### [ADAPT] Traveler cache and physical container access

**Paths:**

- `src/main/java/dev/forever/core/storage/adapter/ContainerItemPredicate.java`
- `src/main/java/dev/forever/core/storage/adapter/ContainerReference.java`
- `src/main/java/dev/forever/core/storage/adapter/ContainerSnapshot.java`
- `src/main/java/dev/forever/core/storage/adapter/ForeverStorage.java`
- `src/main/java/dev/forever/core/storage/adapter/PhysicalInventoryTransfer.java`
- `src/main/java/dev/forever/core/storage/adapter/StorageBalanceLoader.java`
- `src/main/java/dev/forever/core/storage/adapter/StorageCodecs.java`
- `src/main/java/dev/forever/core/storage/adapter/StorageContainerAccess.java`
- `src/main/java/dev/forever/core/storage/adapter/TravelerCache.java`
- `src/main/java/dev/forever/core/storage/adapter/TravelerCacheComponent.java`
- `src/main/java/dev/forever/core/storage/adapter/TravelerCacheContents.java`
- `src/main/java/dev/forever/core/storage/adapter/TravelerCacheItem.java`

**Current responsibility:** Bounded, non-nesting traveler cache, physical container snapshots and transfers, loaded-only access, storage balance loading, and the persistent `forever:travelers_cache` item/component path. It deliberately separates an item cache from a magical remote warehouse.

**Build and tests:** Compiles in `compileJava`. Storage unit and GameTest coverage passed.

**Possible duplicated mechanic:** Backpacks, portable storage, shulker-like caches, and inventory transport mods. No selected candidate is shown in the repository, and a generic backpack may not carry the route and traveler semantics.

**Recommended destination:** Companion integration or a pack-configured portable-storage mod. Preserve the no-nesting, bounded capacity, loaded-only, and physical transfer rules. Move balance data to pack/datapack ownership where possible.

**Migration risk:** High for `forever:travelers_cache` and `forever:traveler_cache_contents`, medium for physical transfer records. Portable contents are direct item-loss and duplication risk.

**Rationale:** Adapt rather than replace outright. The cache is a connective travel/economy object, while its inventory mechanics may be delegated if a candidate can preserve the safety boundary.

## 6. Career and Mason system

### [KEEP] Career and Mason domain concepts

**Paths:**

- `src/main/java/dev/forever/core/career/domain/CareerCodecs.java`
- `src/main/java/dev/forever/core/career/domain/CareerDataException.java`
- `src/main/java/dev/forever/core/career/domain/CareerLearningSource.java`
- `src/main/java/dev/forever/core/career/domain/CareerStatus.java`
- `src/main/java/dev/forever/core/career/domain/MasonRank.java`

**Current responsibility:** Pure career status, learning-source, rank, codec, and error concepts for villager and institutional progression.

**Build and tests:** Compiles in `compileJava`. Covered by career codec and Mason service/data/workshop tests, plus the vertical-slice GameTest.

**Possible duplicated mechanic:** Generic villager-career or profession mods may expose hooks, but no evidence shows they implement institutional memory, teaching, rank, or pack-specific Mason progression.

**Recommended destination:** Companion domain and pack/datapack content.

**Migration risk:** High once villager career state is saved or players teach one another. Low for pure rank values before release.

**Rationale:** This is explicitly a connective identity system. Do not replace it with a generic profession overhaul without preserving the learning model.

### [KEEP] Career runtime and Mason progression

**Paths:**

- `src/main/java/dev/forever/core/career/adapter/CareerAttachment.java`
- `src/main/java/dev/forever/core/career/adapter/CareerDeathResult.java`
- `src/main/java/dev/forever/core/career/adapter/CareerMortalityService.java`
- `src/main/java/dev/forever/core/career/adapter/CareerProgressionResult.java`
- `src/main/java/dev/forever/core/career/adapter/CareerSimulationResult.java`
- `src/main/java/dev/forever/core/career/adapter/CareerState.java`
- `src/main/java/dev/forever/core/career/adapter/CareerTeachingResult.java`
- `src/main/java/dev/forever/core/career/adapter/CareerTeachingService.java`
- `src/main/java/dev/forever/core/career/adapter/ForeverCareer.java`
- `src/main/java/dev/forever/core/career/adapter/MasonCareerAttachment.java`
- `src/main/java/dev/forever/core/career/adapter/MasonCareerDefinition.java`
- `src/main/java/dev/forever/core/career/adapter/MasonCareerService.java`
- `src/main/java/dev/forever/core/career/adapter/MasonCatalog.java`
- `src/main/java/dev/forever/core/career/adapter/MasonCatalogEntry.java`
- `src/main/java/dev/forever/core/career/adapter/MasonData.java`
- `src/main/java/dev/forever/core/career/adapter/MasonDataLoader.java`
- `src/main/java/dev/forever/core/career/adapter/MasonDataRegistry.java`
- `src/main/java/dev/forever/core/career/adapter/MasonIds.java`
- `src/main/java/dev/forever/core/career/adapter/MasonRankRule.java`
- `src/main/java/dev/forever/core/career/adapter/MasonResourceReloadListener.java`
- `src/main/java/dev/forever/core/career/adapter/MasonSupplyResult.java`
- `src/main/java/dev/forever/core/career/adapter/MasonSupplyService.java`
- `src/main/java/dev/forever/core/career/adapter/MasonWorkshopService.java`
- `src/main/java/dev/forever/core/career/adapter/MasonWorkshopValidator.java`
- `src/main/java/dev/forever/core/career/adapter/PhysicalDeathCause.java`

**Current responsibility:** Stores player and Mason career state, handles death and bounded unloaded simulation, teaching, catalog progression, supply, workshop validation, and Mason identifiers. `CareerState` is schema 2. `MasonCareerDefinition` currently narrows the implementation to Mason-specific data.

**Build and tests:** Compiles in `compileJava`. `CareerCodecTest`, `MasonCareerServiceTest`, `MasonDataTest`, `MasonWorkshopTest`, and the five-method `MasonVerticalSliceGameTest` passed.

**Possible duplicated mechanic:** Villager API or career mods may provide events, professions, or UI. Generic processing/crafting mods may overlap with catalog conversion, but not the rank, teaching, and institutional-memory model. Matcha's villagers/trades classifications need reconciliation.

**Recommended destination:** Companion mod for career identity, teaching, mortality, and cross-system progression. Catalog and supply values should be pack/datapack data. Use a third-party villager API only as an adapter seam.

**Migration risk:** Very high for the `forever:career_state` attachment, Mason identity and rank records, learned data, and death/simulation semantics. High for `MasonIds` values such as `forever:mason` and `forever:stone_cutting`, and for data paths loaded from the `forever` namespace. The loader is namespace-locked despite ADR 0022.

**Rationale:** The user brief specifically warns that Mason career is not a generic feature. Keep and adapt rather than replace. A future migration may move catalog execution to recipes or an external content mod, but the institutional progression remains companion-owned unless evidence says otherwise.

## 7. Economy and Obol system

### [KEEP] Economy domain and application

**Paths:**

- `src/main/java/dev/forever/core/economy/domain/CoinPurseState.java`
- `src/main/java/dev/forever/core/economy/domain/EconomyBalance.java`
- `src/main/java/dev/forever/core/economy/domain/EconomyCodecs.java`
- `src/main/java/dev/forever/core/economy/domain/EconomyDataException.java`
- `src/main/java/dev/forever/core/economy/domain/PurseTransactionResult.java`
- `src/main/java/dev/forever/core/economy/domain/PurseTransactionStatus.java`
- `src/main/java/dev/forever/core/economy/application/EconomyBalanceAccess.java`

**Current responsibility:** Versioned coin-purse state, described transaction outcomes, economy balances, and application-level balance access. `CoinPurseState` is schema 2 and uses a sequence for transaction identity.

**Build and tests:** Compiles in `compileJava`. `EconomyCodecTest`, `PurseServiceTest`, and the Mason vertical slice passed.

**Possible duplicated mechanic:** Matcha has an `obols_currency` audit classification and may affect trades. Generic shop or economy mods may provide UI and price tables. The evidence does not show an equivalent persistent Obol purse with the documented physical/logical conversion rules.

**Recommended destination:** Companion domain/application for the hybrid Obol identity. Balance and price data belong in pack/datapack configuration.

**Migration risk:** Very high for purse sequence, saved attachment, and physical/logical conversion. Duplication or loss is possible if a third-party shop consumes physical Obols without consulting the purse.

**Rationale:** The user brief correctly identifies Obol economy as a connective system, not an automatic REPLACE candidate. Keep it and reconcile Matcha trade behavior through the adapter boundary.

### [ADAPT] Economy adapter and physical Obol item

**Paths:**

- `src/main/java/dev/forever/core/economy/adapter/CoinPurseAttachment.java`
- `src/main/java/dev/forever/core/economy/adapter/EconomyBalanceLoader.java`
- `src/main/java/dev/forever/core/economy/adapter/EconomyResourceReloadListener.java`
- `src/main/java/dev/forever/core/economy/adapter/ForeverEconomy.java`
- `src/main/java/dev/forever/core/economy/adapter/ObolItem.java`
- `src/main/java/dev/forever/core/economy/adapter/PurseService.java`

**Current responsibility:** Registers `forever:coin_purse` and `forever:obol`, loads balance data, and performs atomic physical-to-logical and logical-to-physical purse operations.

**Build and tests:** Compiles in `compileJava`. Economy unit tests and the vertical slice GameTest passed.

**Possible duplicated mechanic:** Matcha Obol signals, villager trades, generic shop systems, and vanilla item stacks. No external candidate has been selected as the owner of the hybrid purse.

**Recommended destination:** Companion integration for the purse and identity. `ObolItem` and balance JSON may be adapted to pack content or a selected economy UI. Retain explicit transaction results.

**Migration risk:** Very high for registry ID `forever:obol`, attachment ID `forever:coin_purse`, and any already-converted physical items. High for trade integration and balance data.

**Rationale:** Adaptation is safer than replacement. The pack needs one economy contract across systems, and generic shop mods are not automatically a substitute.

## 8. Unified Field Journal and guide system

### [KEEP] Guide domain and recipe-viewer port

**Paths:**

- `src/main/java/dev/forever/core/guide/domain/GuideDataException.java`
- `src/main/java/dev/forever/core/guide/domain/GuideRecipeViewerStatus.java`
- `src/main/java/dev/forever/core/guide/adapter/GuideRecipeViewerCapability.java`
- `src/main/java/dev/forever/core/guide/adapter/GuideRecipeViewerResult.java`
- `src/main/java/dev/forever/core/guide/adapter/NoOpGuideRecipeViewerCapability.java`

**Current responsibility:** Describes guide data failures and a narrow optional capability for opening recipe viewers without making the guide depend on one viewer. The no-op implementation is safe when no viewer is installed.

**Build and tests:** Compiles in `compileJava`. Guide registry, content coverage, Matcha isolation, guide GameTest, and translation GameTest passed.

**Possible duplicated mechanic:** EMI or REI should own recipe viewing. This code is a port, not a recipe viewer implementation.

**Recommended destination:** Companion integration port plus an external recipe viewer selected by LAB-01. Keep the no-op fallback.

**Migration risk:** Medium for the port result contract. Low for the no-op class. High if the guide silently becomes coupled to a viewer-specific API.

**Rationale:** This is the expected extension surface under ADR 0022. Do not classify it as a custom viewer.

### [ADAPT] Guide registry, search, gates, and access

**Paths:**

- `src/main/java/dev/forever/core/guide/adapter/ForeverGuide.java`
- `src/main/java/dev/forever/core/guide/adapter/GuideAccess.java`
- `src/main/java/dev/forever/core/guide/adapter/GuideEntry.java`
- `src/main/java/dev/forever/core/guide/adapter/GuideGate.java`
- `src/main/java/dev/forever/core/guide/adapter/GuideRegistry.java`
- `src/main/java/dev/forever/core/guide/adapter/GuideRegistryAccess.java`
- `src/main/java/dev/forever/core/guide/adapter/GuideRegistryLoader.java`
- `src/main/java/dev/forever/core/guide/adapter/GuideResourceReloadListener.java`
- `src/main/java/dev/forever/core/guide/adapter/GuideSearchHit.java`
- `src/main/java/dev/forever/core/guide/adapter/GuideSearchQuery.java`
- `src/main/java/dev/forever/core/guide/adapter/GuideSearchResponse.java`

**Current responsibility:** Registers, loads, gates, and searches guide entries. `GuideEntry` currently expects IDs such as `forever:guide/...` and process IDs such as `forever:process/...`. There is no custom screen in the current client source.

**Build and tests:** Compiles in `compileJava`. Guide unit and GameTest coverage passed.

**Possible duplicated mechanic:** Generic guidebook mods and recipe viewers may supply a search UI. Jade and a selected recipe viewer supply low-level information. They do not provide one unified contextual knowledge state across Matcha and the rest of the pack.

**Recommended destination:** Companion Field Journal model and registry. Guide entry content and translations should be pack/resource-pack data. Adapt the presentation to an existing book or UI framework if LAB-01 proves one suitable.

**Migration risk:** High for guide IDs, process IDs, discovery gates, and translation/resource paths. Medium for search result shape.

**Rationale:** Keep semantic knowledge and discovery consequences. Adapt the UI and loader. The loader's `forever` namespace restriction is a known FVR-A002 issue.

## 9. Data resources for the companion systems

### [ADAPT] Existing balance and catalogue data

**Paths:**

- `src/main/resources/data/forever/career/mason.json`
- `src/main/resources/data/forever/career/mason_catalog.json`
- `src/main/resources/data/forever/economy/balance.json`
- `src/main/resources/data/forever/equipment/balance.json`
- `src/main/resources/data/forever/settlement/settlement.json`
- `src/main/resources/data/forever/storage/balance.json`

**Current responsibility:** Bundled default data for Mason, economy, equipment, settlement, and storage loaders.

**Build and tests:** Processed by `processResources`; root build passed. Feature tests load representative data through the corresponding loaders. No separate data-only test covers every balance field.

**Possible duplicated mechanic:** Pack configs and datapacks should own tuning. Matcha supplies overlapping food, tool, trade, and equipment data. A third-party storage or smithing mod may make some rows obsolete.

**Recommended destination:** Pack/datapack source of truth, with a temporary companion fallback only while migration is staged. Accept any namespace in loaders per ADR 0022.

**Migration risk:** High because changing paths or schema can change live balance and invalidate persistent interpretation. Medium before release.

**Rationale:** Balance belongs in data, but the current location is embedded in the companion mod. Adapt ownership without changing values silently.

### [ADAPT] Existing guide and mastery data

**Paths:**

- `src/main/resources/data/forever/guide/campfire_process.json`
- `src/main/resources/data/forever/guide/early_processing.json`
- `src/main/resources/data/forever/guide/early_tools.json`
- `src/main/resources/data/forever/guide/equipment_condition.json`
- `src/main/resources/data/forever/guide/equipment_reforging.json`
- `src/main/resources/data/forever/guide/fire_starting.json`
- `src/main/resources/data/forever/guide/mason_career.json`
- `src/main/resources/data/forever/guide/mastery_loadout.json`
- `src/main/resources/data/forever/guide/mastery_progression.json`
- `src/main/resources/data/forever/guide/settlement_charter.json`
- `src/main/resources/data/forever/guide/storage_travelers_cache.json`
- `src/main/resources/data/forever/mastery/alchemist_herbalist.json`
- `src/main/resources/data/forever/mastery/builder.json`
- `src/main/resources/data/forever/mastery/cook.json`
- `src/main/resources/data/forever/mastery/explorer_cartographer.json`
- `src/main/resources/data/forever/mastery/farmer_naturalist.json`
- `src/main/resources/data/forever/mastery/fisher_mariner.json`
- `src/main/resources/data/forever/mastery/loadout.json`
- `src/main/resources/data/forever/mastery/merchant_steward.json`
- `src/main/resources/data/forever/mastery/prospector.json`
- `src/main/resources/data/forever/mastery/rider_animal_handler.json`
- `src/main/resources/data/forever/mastery/smith.json`

**Current responsibility:** Guide explanations, process documentation, and mastery definitions for the implemented prototype.

**Build and tests:** Processed by `processResources`; guide and mastery coverage tests plus GameTests passed. Some translation/data coverage is representative rather than exhaustive.

**Possible duplicated mechanic:** Recipe viewers, starter books, generic skill-tree content, and Matcha's own recipes/tutorialization. The unified Field Journal meaning remains custom.

**Recommended destination:** Datapack and resource pack content loaded by a narrow companion registry. External mods own low-level recipe and block information.

**Migration risk:** Medium for content, high for IDs and discovery state. Removing an entry can strand saved knowledge or translations.

**Rationale:** Content is data and should move toward pack ownership, while the capability and knowledge model remains companion-owned.

### [ADAPT] Player-facing translations

**Paths:** `src/main/resources/assets/forever/lang/en_us.json`

**Current responsibility:** Translations for equipment, guide, mastery, settlement, storage, and other prototype UI/text. The code and translation coverage are not perfectly aligned. Mason/economy code paths have less direct translation coverage than the guide tests imply.

**Build and tests:** Processed by `processResources`. `GuideTranslationGameTest` passed its representative checks. No exhaustive translation linter was run in this inventory.

**Possible duplicated mechanic:** Resource packs and third-party mods own their own text. A unified Field Journal needs a pack-level translation layer, not duplicated starter-book prose.

**Recommended destination:** Resource pack for pack-facing text, with the companion retaining only keys required for its runtime API. Preserve namespace aliases during any future rename.

**Migration risk:** High for translation keys and resource paths after public release. Medium before release.

**Rationale:** Adapt ownership and coverage. Do not rename `forever` keys casually.

### [ADAPT] Mod metadata and client placeholder

**Paths:**

- `src/main/resources/fabric.mod.json`
- `src/client/resources/assets/forever/items/.gitkeep`
- `src/client/resources/assets/forever/blockstates/.gitkeep`
- `src/client/resources/assets/forever/lang/.gitkeep`
- `src/client/resources/assets/forever/models/block/.gitkeep`
- `src/client/resources/assets/forever/models/item/.gitkeep`
- `src/client/resources/assets/forever/textures/block/.gitkeep`
- `src/client/resources/assets/forever/textures/entity/.gitkeep`
- `src/client/resources/assets/forever/textures/gui/.gitkeep`
- `src/client/resources/assets/forever/textures/item/.gitkeep`

**Current responsibility:** Fabric metadata declares mod id `forever`, common/client entrypoints, and dependencies. The `.gitkeep` files preserve empty client resource boundaries without claiming that gameplay art exists.

**Build and tests:** Metadata and resources processed successfully by the root build. Dedicated-server GameTests passed without client-class errors. No client launch was run.

**Possible duplicated mechanic:** None in the placeholders. A future custom screen or item art would overlap third-party UI/content and needs an approved asset manifest plus gameplay need.

**Recommended destination:** Companion metadata and resource-pack boundary. Keep placeholders until a real approved asset is needed.

**Migration risk:** High for mod id, entrypoint names, dependency declarations, and resource paths. Low for `.gitkeep` markers.

**Rationale:** Adapt metadata only through a separate identity decision. Preserve ordinary Minecraft access and do not add empty gameplay classes or final textures.

### [KEEP] Approved placeholder asset

**Paths:** `src/client/resources/assets/forever/textures/item/forever_missing_asset.png`

**Current responsibility:** Explicit missing-asset placeholder used by the asset pipeline rather than an untracked visual state.

**Build and tests:** Packaged by the client resource process. The asset tool compiles and its unit test passed. `validateAssets` was intentionally not run in this inventory.

**Possible duplicated mechanic:** None. It is not final gameplay art.

**Recommended destination:** Resource-pack/tooling placeholder, unchanged until an approved asset replaces it.

**Migration risk:** Low, unless a resource namespace migration removes the fallback path.

**Rationale:** The design rules prefer a documented placeholder over premature final art.

## 10. Asset validation tool

### [KEEP] Asset validator implementation

**Paths:** all 37 classes under `src/assetTools/java/dev/forever/tools/assets/`:

- `AssetFile.java`, `AssetInventory.java`, `AssetInventoryScanner.java`, `AssetManifest.java`, `AssetRule.java`
- `AssetValidationContext.java`, `AssetValidationOptions.java`, `AssetValidationOptionsParser.java`, `AssetValidationReport.java`, `AssetValidator.java`, `AssetValidatorApplication.java`, `AssetViolation.java`
- `ColourModelKind.java`, `DuplicateAssetIdCheck.java`, `FullyTransparentCheck.java`, `HumanReportRenderer.java`, `ImageFormatCheck.java`, `ImageInspector.java`, `ImageSummary.java`
- `JsonReportRenderer.java`, `JsonResourceReferenceScanner.java`, `ManifestEntryMissingAssetCheck.java`, `ManifestReader.java`, `ManifestRow.java`, `ManifestStatus.java`
- `MissingTextureReferenceCheck.java`, `NameNotSnakeCaseCheck.java`, `ProductionAsset.java`, `ReferenceKind.java`, `Resolution.java`, `ResourceReference.java`, `ResourceReferenceResolver.java`
- `Rfc4180Parser.java`, `UnexpectedDimensionsCheck.java`, `UnmanifestedAssetCheck.java`, `UsageException.java`, `ViolationCode.java`

**Current responsibility:** Separate pure-JVM asset inventory and validation source set. It checks manifest coverage, resource references, naming, image format, dimensions, transparency, duplicate IDs, and missing files. It is not included as runtime gameplay.

**Build and tests:** Compiles in `compileAssetToolsJava`, with checkstyle passing in the root build. `AssetValidatorApplicationTest` exists and passed as part of the up-to-date root test task. The explicit `./gradlew validateAssets` command was not run.

**Possible duplicated mechanic:** None. It is developer tooling and complements, rather than duplicates, resource-pack validation.

**Recommended destination:** `tools` or unchanged asset-tools source set. Keep separate from the companion JAR.

**Migration risk:** Low to medium. A resource namespace change or moving final assets can create false positives. The tool itself is not save-affecting.

**Rationale:** This is useful infrastructure for the pack/resource-pack boundary and prevents shipping undocumented assets. Keep it.

## 11. Test infrastructure

### [KEEP] Unit and architecture regression tests

**Paths:** all 32 files under `src/test/java/`:

- `src/test/java/dev/forever/ForeverModTest.java`
- `src/test/java/dev/forever/architecture/ArchitectureRulesTest.java`
- `src/test/java/dev/forever/compat/matcha/MatchaServerEvidenceTest.java`
- `src/test/java/dev/forever/compat/matcha/MatchaVersionDetectorTest.java`
- `src/test/java/dev/forever/core/career/adapter/CareerCodecTest.java`
- `src/test/java/dev/forever/core/career/adapter/MasonCareerServiceTest.java`
- `src/test/java/dev/forever/core/career/adapter/MasonDataTest.java`
- `src/test/java/dev/forever/core/career/adapter/MasonWorkshopTest.java`
- `src/test/java/dev/forever/core/data/SchemaVersionedTest.java`
- `src/test/java/dev/forever/core/economy/adapter/EconomyCodecTest.java`
- `src/test/java/dev/forever/core/economy/adapter/PurseServiceTest.java`
- `src/test/java/dev/forever/core/equipment/adapter/EquipmentBalanceTest.java`
- `src/test/java/dev/forever/core/equipment/adapter/EquipmentMathTest.java`
- `src/test/java/dev/forever/core/equipment/adapter/EquipmentStateTest.java`
- `src/test/java/dev/forever/core/guide/adapter/GuideContentCoverageTest.java`
- `src/test/java/dev/forever/core/guide/adapter/GuideMatchaIsolationTest.java`
- `src/test/java/dev/forever/core/guide/adapter/GuideRegistryTest.java`
- `src/test/java/dev/forever/core/mastery/adapter/MasteryCodecTest.java`
- `src/test/java/dev/forever/core/mastery/adapter/MasteryLoadoutTest.java`
- `src/test/java/dev/forever/core/mastery/adapter/MasteryRegistryTest.java`
- `src/test/java/dev/forever/core/mastery/adapter/MasteryTestFixtures.java`
- `src/test/java/dev/forever/core/mastery/adapter/ProspectorProgressionTest.java`
- `src/test/java/dev/forever/core/settlement/adapter/ResidenceValidatorTest.java`
- `src/test/java/dev/forever/core/settlement/adapter/SettlementBalanceTest.java`
- `src/test/java/dev/forever/core/settlement/adapter/SettlementCodecTest.java`
- `src/test/java/dev/forever/core/settlement/adapter/SettlementGraphTest.java`
- `src/test/java/dev/forever/core/settlement/adapter/SettlementImportTest.java`
- `src/test/java/dev/forever/core/settlement/adapter/SettlementMigrationTest.java`
- `src/test/java/dev/forever/core/settlement/adapter/SettlementTestFixtures.java`
- `src/test/java/dev/forever/core/storage/adapter/StorageBehaviorTest.java`
- `src/test/java/dev/forever/core/storage/adapter/StorageCodecTest.java`
- `src/test/java/dev/forever/tools/assets/AssetValidatorApplicationTest.java`

**Current responsibility:** Plain-JVM feature, codec, architecture, Matcha isolation, asset, and data tests. ArchitectureRulesTest enforces seven unfrozen rules and currently reports zero violations. The tests capture valuable persistence, authority, bounded-work, and no-duplication contracts.

**Build and tests:** All compile in `compileTestJava`. The required root `build test` command passed, but Gradle reported the tasks as `UP-TO-DATE`, so this run is evidence that the configured test task is green, not a fresh execution of every test method.

**Possible duplicated mechanic:** Tests for generic warehouse search or custom UI may need to move with a replacement. Safety, migration, Matcha isolation, and architecture tests should remain.

**Recommended destination:** Companion regression suite plus pack/lab test suites. Adapt fixtures when code ownership changes. Never delete a safety test just because a third-party mod is selected.

**Migration risk:** High for migration coverage if saved-data classes are replaced. Medium for architecture rules during package shrinkage. Low for pure helper tests.

**Rationale:** Tests are evidence and should outlive an implementation where they describe an invariant. Mark individual tests for replacement only after equivalent external behavior is tested.

### [KEEP] Dedicated-server GameTests

**Paths:**

- `src/gametest/java/dev/forever/gametest/ForeverInitializationGameTest.java`
- `src/gametest/java/dev/forever/gametest/equipment/EquipmentGameTest.java`
- `src/gametest/java/dev/forever/gametest/guide/GuideGameTest.java`
- `src/gametest/java/dev/forever/gametest/guide/GuideTranslationGameTest.java`
- `src/gametest/java/dev/forever/gametest/mason/MasonVerticalSliceGameTest.java`
- `src/gametest/java/dev/forever/gametest/mastery/MasteryPersistenceGameTest.java`
- `src/gametest/java/dev/forever/gametest/matcha/MatchaDetectionGameTest.java`
- `src/gametest/java/dev/forever/gametest/settlement/SettlementPersistenceGameTest.java`
- `src/gametest/java/dev/forever/gametest/storage/StorageGameTest.java`
- `src/gametest/resources/fabric.mod.json`

**Current responsibility:** Dedicated-server initialization, persistence, Matcha detection, and vertical-slice checks across the current prototype. The source contains 26 annotated methods across nine classes, while the runtime runner reported 27 tests. That discrepancy is recorded rather than silently explained.

**Build and tests:** `compileGametestJava` passed. `./gradlew runGametest` passed with exit 0. The runtime loaded Minecraft 26.2, Fabric Loader 0.19.3, Fabric API, Forever, and Forever GameTest. All 27 runtime tests passed. Matcha emitted its own non-fatal parse warnings for malformed or stray content. No client classes loaded on the server.

**Possible duplicated mechanic:** GameTests currently exercise custom systems that may later be delegated. The server-authority, save/reload, physical truth, and compatibility checks remain needed for the selected modpack.

**Recommended destination:** Companion and pack integration acceptance suite. Add isolated lab GameTests for each candidate mod rather than treating the current vertical slice as pack acceptance.

**Migration risk:** High if a system is removed without replacing its acceptance behavior. Low for the harness itself.

**Rationale:** Keep the dedicated-server feedback loop. Reclassify individual assertions only after the ownership matrix and labs establish an external equivalent.

## 12. Standalone Matcha audit project

### [KEEP] Audit CLI production classes

**Paths:** all 32 files under `tools/matcha-audit/src/main/java/dev/forever/matcha/audit/`:

- `AuditAnalyzer.java`, `AuditException.java`, `AuditReport.java`, `AuditRunner.java`, `ClassifiedFile.java`
- `CliOptions.java`, `CliParser.java`, `CsvWriter.java`, `ExitCodes.java`, `FunctionAnalyzer.java`, `Hashing.java`
- `InputFile.java`, `InputLoader.java`, `InputSnapshot.java`, `JsonAnalyzer.java`, `JsonParseResult.java`, `JsonReference.java`, `JsonSupport.java`
- `MatchaAuditApplication.java`, `PackAnalyzer.java`, `PackLayout.java`, `ParseErrorObservation.java`, `PathClassifier.java`, `ReferenceObservation.java`
- `ReferenceResolver.java`, `ReportWriter.java`, `ResolvedReference.java`, `SummaryWriter.java`, `UnknownPathAnalyzer.java`, `UnknownPathObservation.java`
- `VanillaOverrideObservation.java`, `ZipSafety.java`

**Current responsibility:** Standalone deterministic archive audit CLI. It inventories Matcha files, parses JSON and function references, records unknown paths, resolves references, hashes input, detects vanilla namespace overrides, and writes reproducible reports. It is explicitly not part of the mod JAR.

**Build and tests:** The independent command `cd tools/matcha-audit && ./gradlew test` passed with exit 0. Gradle reported four actionable tasks up to date. The tool uses the pinned Java/JUnit baseline and does not fetch Matcha during CI.

**Possible duplicated mechanic:** None. It audits a third-party datapack/resource pack rather than implementing gameplay.

**Recommended destination:** `tools/matcha-audit`, unchanged.

**Migration risk:** Medium for report format and input hash compatibility. High licensing/provenance risk if generated Matcha excerpts are redistributed carelessly.

**Rationale:** Keep as a separate audit boundary. It is required to understand Matcha before exposing any of its private behavior through the companion.

### [KEEP] Audit CLI test and deterministic fixtures

**Paths:**

- `tools/matcha-audit/src/test/java/dev/forever/matcha/audit/MatchaAuditApplicationTest.java`
- `tools/matcha-audit/src/test/resources/fixture-pack/README.txt`
- `tools/matcha-audit/src/test/resources/fixture-pack/pack.mcmeta`
- `tools/matcha-audit/src/test/resources/fixture-pack/assets/matcha/atlases/blocks.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/assets/matcha/custom/weird.txt`
- `tools/matcha-audit/src/test/resources/fixture-pack/assets/matcha/equipment/worker.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/assets/matcha/font/default.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/assets/matcha/items/tool.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/assets/matcha/lang/en_us.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/assets/matcha/models/item/tool.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/assets/matcha/sounds.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/assets/matcha/textures/item/tool.png`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/advancements/adv.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/damage_type/hit.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/dimension/garden.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/dimension_type/garden.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/equipment/worker.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/functions/later.mcfunction`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/functions/main.mcfunction`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/functions/nested/helper.mcfunction`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/functions/reward.mcfunction`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/item_modifiers/tool.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/loot_tables/loot.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/loot_tables/nested_loot.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/mystery/unknown.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/predicates/check.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/recipes/broken.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/recipes/custom.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/structures/garden.nbt`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/tags/items/tools.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/villager_trades/farmer.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/matcha/worldgen/biome/garden.json`
- `tools/matcha-audit/src/test/resources/fixture-pack/data/minecraft/recipes/vanilla.json`

**Current responsibility:** Small synthetic fixture archive and its test. The fixture intentionally covers parse errors, references, unknown paths, vanilla overrides, and resource/data namespaces. It is not the Matcha release itself.

**Build and tests:** Covered by the independent audit-tool test command, which passed with tasks up to date.

**Possible duplicated mechanic:** None. Fixture content is test input.

**Recommended destination:** Tools test resources, unchanged.

**Migration risk:** Low. Changing fixtures can weaken audit guarantees without failing compilation.

**Rationale:** Keep as an offline, license-safe regression corpus separate from generated third-party excerpts.

### [KEEP] Audit project build support

**Paths:**

- `tools/matcha-audit/build.gradle`
- `tools/matcha-audit/settings.gradle`
- `tools/matcha-audit/gradle/wrapper/gradle-wrapper.jar`
- `tools/matcha-audit/gradle/wrapper/gradle-wrapper.properties`
- `tools/matcha-audit/gradlew`
- `tools/matcha-audit/gradlew.bat`
- `tools/matcha-audit/.gitignore`
- `tools/matcha-audit/README.md`

**Current responsibility:** Independent Gradle project and user documentation for the audit CLI. It keeps the audit out of the mod JAR.

**Build and tests:** Independent `./gradlew test` passed. No root build task packages this project into the mod.

**Possible duplicated mechanic:** None.

**Recommended destination:** Tools, unchanged.

**Migration risk:** Medium for wrapper and Java version drift. Low for gameplay data.

**Rationale:** Keep the separate reproducible audit project and its pinned build boundary.

## 13. Scripts and developer workflows

### [KEEP] Matcha acquisition and disposable-world safety scripts

**Paths:**

- `scripts/README.md`
- `scripts/fetch-matcha.sh`
- `scripts/fetch-matcha.ps1`
- `scripts/install-matcha-dev.sh`
- `scripts/install-matcha-dev.ps1`

**Current responsibility:** Acquire the exact official Matcha release from Modrinth, verify the lock and SHA-256, store the ignored archive under `vendor/matcha/`, and install it only into an explicitly named disposable development world as both resource pack and datapack. The scripts refuse inferred or obviously real-world paths and require an acknowledgement or `.forever-dev-world` marker.

**Build and tests:** Shell and PowerShell scripts are not compiled by Gradle. They were inspected but not executed in this inventory. No fetch or install was run because no dependency or world mutation was required, and the user explicitly forbade touching real worlds.

**Possible duplicated mechanic:** None. They manage Matcha material and safety, not gameplay.

**Recommended destination:** Developer tools, unchanged. Keep the pinned source and fail-closed behavior.

**Migration risk:** High licensing and world-save safety risk. Medium for shell/PowerShell parity. A future pack loader must not weaken the explicit world boundary.

**Rationale:** Keep. These scripts are the safe acquisition boundary required by the project instructions and ADR 0020.

### [ADAPT] Pack export, validation, and dependency reporting scripts

**Paths:**

- `scripts/build-pack.sh`
- `scripts/build-pack.ps1`
- `scripts/validate-pack.sh`
- `scripts/validate-pack.ps1`
- `scripts/report-dependencies.sh`

**Current responsibility:** Refresh Packwiz metadata, export `dist/*.mrpack`, validate exact pins and absence of committed third-party binaries, and regenerate `docs/mod-research/dependency-lock.md`.

**Build and tests:** Scripts were inspected but not run in this inventory. `build-pack.sh` and `validate-pack.sh` are called by pivot documentation and require Packwiz or resolved artifacts not needed for the Java build. No claim is made that a fresh export or validation passed.

**Possible duplicated mechanic:** None. They own pack reproducibility and dependency review.

**Recommended destination:** Pack tooling, adapted as candidate mods and the ownership matrix mature. Keep the shell/PowerShell parity and fail-closed checks.

**Migration risk:** High supply-chain and packaging risk. A stale index or wrong side marker can produce a pack that appears to load but has missing or client-only dependencies.

**Rationale:** Adapt rather than retire. The pivot makes these scripts more central than the old mod-only Gradle path.

### [KEEP] Audit launcher

**Paths:** `scripts/run-matcha-audit.sh`

**Current responsibility:** Checks Java 25 and invokes the installed standalone Matcha audit CLI.

**Build and tests:** Not run. The underlying audit project test passed independently.

**Possible duplicated mechanic:** None.

**Recommended destination:** Tools, unchanged.

**Migration risk:** Low. Medium if tool installation layout changes.

**Rationale:** Keep the one-command audit entrypoint.

## 14. Pack source and third-party material

### [KEEP] Packwiz source of truth and pinned dependencies

**Paths:**

- `.config-packwiz.toml`
- `pack/pack.toml`
- `pack/index.toml`
- `pack/.packwizignore`
- `pack/datapacks/matcha-flavoured.pw.toml`
- `pack/mods/fabric-api.pw.toml`
- `pack/mods/globalpacks.pw.toml`
- `pack/defaultconfigs/global_packs.toml`
- `matcha.lock.json`

**Current responsibility:** Packwiz metadata pins Minecraft 26.2, Fabric Loader 0.19.3, Packwiz pack identity, Fabric API, Global Packs, and official Matcha. The Global Packs config intentionally lists the same Matcha archive as both a required resource pack and required datapack. `matcha.lock.json` records Matcha project `QI0EmgZ1`, version `E9rngRfK`, file name, SHA-256, publication metadata, and CC-BY-NC-SA-4.0 license.

**Build and tests:** Pack metadata is not compiled by Gradle. The root Java build and GameTest passed, but `validate-pack.sh` and `build-pack.sh` were not run in this inventory. `docs/compatibility/matcha-loading.md` records a prior dedicated-server pack verification and its client verification gap.

**Possible duplicated mechanic:** Pack inputs deliberately include Matcha gameplay. Future candidate mods may overlap it. Global Packs is the selected loader because it has a 26.2 release, while Paxi and Open Loader evidence in the compatibility record do not.

**Recommended destination:** Pack config and dependency lock, unchanged as source of truth. Adapt only through explicit dependency tickets and lab evidence.

**Migration risk:** Very high. Global Packs is All Rights Reserved and currently the only named 26.2 loader. Matcha is CC-BY-NC-SA-4.0. Changing the archive, folder, load order, or loader can silently deliver only half of Matcha or alter existing world data.

**Rationale:** Keep the pin and licensing boundary. Do not commit binaries or make the repository public without the required license recheck.

### [KEEP] Local Matcha archive boundary

**Paths:** `vendor/matcha/Matcha_Flavoured_1_12.zip`

**Current responsibility:** Ignored local copy of the exact third-party Matcha archive used for deliberate development installation and audit. It is not a Gradle dependency and is not part of original Forever source.

**Build and tests:** Not compiled. No fetch or install was run in this inventory.

**Possible duplicated mechanic:** It is the source of Matcha's mechanics, not an implementation owned by the companion.

**Recommended destination:** Local-only developer vendor cache, unchanged and ignored. Keep generated reports separate and preserve the lock.

**Migration risk:** Very high licensing/provenance risk. Never stage or redistribute it as original code.

**Rationale:** Keep the boundary, not the binary in version control. The archive is called out because it is meaningful even though ignored.

### [RETIRE] Exported distribution artifact

**Paths:** `dist/many-roads-home-0.1.0-dev.mrpack`

**Current responsibility:** Ignored generated Packwiz export.

**Build and tests:** Not a source or test input. No fresh export was run.

**Possible duplicated mechanic:** None, but it may embed dependency choices that differ from current `pack/` metadata.

**Recommended destination:** Delete-later generated output. Recreate from `pack/` only through the pack build script.

**Migration risk:** Medium provenance risk. A stale artifact can be mistaken for the current pack and can contain third-party material under a different review state.

**Rationale:** It must not be treated as a source of truth. Retire it through normal ignored-output cleanup, not by silently staging or deleting it during this inventory.

## 15. Generated Matcha audit reports

### [KEEP] Deterministic generated audit evidence

**Paths:** all 21 files under `generated/matcha/1.12/`:

- `advancements.json`, `assets.json`, `cross-references.json`, `file-inventory.csv`, `functions.json`
- `input-sha256.txt`, `item-modifiers.json`, `loot-tables.json`, `metadata.json`, `namespaces.json`
- `pack-filters.json`, `predicates.json`, `recipes.json`, `registry-data.json`, `scoreboards.json`
- `summary.md`, `tags.json`, `unknown-paths.csv`, `unresolved-references.csv`, `vanilla-overrides.csv`, `worldgen.json`

**Current responsibility:** Generated, deterministic reports from the pinned Matcha archive. The summary records 5,190 files, 2,433 data files, 2,754 resource files, zero parse errors, 95 unknown paths, 24 scoreboards, 3,649 vanilla namespace overrides, 28,981 references, and 60 unresolved internal references.

**Build and tests:** Generated output is not compiled by the mod. The standalone audit test passed. The files were inspected as generated evidence and must not be hand-edited.

**Possible duplicated mechanic:** Reports document exactly where Matcha overlaps custom equipment, food, trades, structures, weather, recipes, and other systems. They are evidence, not gameplay.

**Recommended destination:** `generated/matcha/<version>/`, unchanged and separate from original source. Regenerate only with the standalone audit tool and lock.

**Migration risk:** High licensing and provenance risk because reports include excerpts and identifiers derived from CC-BY-NC-SA-4.0 material. Medium determinism risk if the audit format changes.

**Rationale:** Keep for auditability and classification. Do not treat generated reports as hand-authored APIs or copy Matcha internals outside the adapter package.

## 16. Documentation inventory

The documentation is not uniform. Some records are authoritative pivot decisions, some describe the old prototype, and some are future designs. The classification below preserves history while making conflicts visible. No document is rewritten by this inventory.

### [KEEP] Project governance and invariant documents

**Paths:**

- `AGENTS.md`
- `docs/code-guidelines.md`
- `docs/design-principles.md`
- `docs/development-workflow.md`
- `docs/world-save-safety.md`
- `docs/dependency-baseline.md`
- `docs/verified-api-reference.md`
- `docs/vision.md`
- `docs/modpack-architecture.md`
- `docs/branding/name-and-identity.md`

**Current responsibility:** Agent constraints, code and workflow rules, design principles, pinned dependency policy, world safety, verified API notes, product vision, modpack ownership, and the working Many Roads Home identity.

**Build and tests:** Markdown and CSV are not compiled. Their claims are partially exercised by the build, GameTest, scripts, and prior verification records. The docs themselves require human review.

**Possible duplicated mechanic:** These documents govern ownership rather than implement mechanics.

**Recommended destination:** Authoritative design and workflow docs, unchanged as this inventory's scope. Future tickets may reconcile stale statements explicitly.

**Migration risk:** High if implementation follows an obsolete rule or if a pivot decision is silently contradicted. Low file-format risk.

**Rationale:** Keep as the decision record. In particular, `docs/modpack-architecture.md` is the current ownership authority for the pivot.

### [ADAPT] Run brief, roadmap, active backlog, and old architecture

**Paths:**

- `docs/_agent-brief.md`
- `docs/architecture.md`
- `docs/backlog.md`
- `docs/roadmap.md`

**Current responsibility:** Run scope, original companion architecture, implementation ticket queue, and milestone status. The old records describe a foundation-only state or a custom-mod implementation path that conflicts with the current tree and the pivot records.

**Build and tests:** Not compiled. Their contradiction is visible from source counts, tests, and the GameTest run.

**Possible duplicated mechanic:** The documents do not implement mechanics, but they can authorize custom implementations that the modpack pivot now expects to delegate.

**Recommended destination:** Adapt in a dedicated documentation/backlog reconciliation ticket. Preserve old ticket history, update ownership and release status, and link every retained class to a current gap analysis.

**Migration risk:** High process risk. A stale backlog can cause an agent to implement a future custom system or rename identifiers without a current decision.

**Rationale:** Do not silently choose between conflicting documents. The pivot ADRs and ownership matrix need to supersede stale status statements visibly.

### [KEEP] Pivot ADRs and ADR index

**Paths:**

- `docs/adr/README.md`
- `docs/adr/0023-many-roads-home-working-title.md`
- `docs/adr/0024-modpack-first-product-ownership.md`
- `docs/adr/0025-packwiz-source-of-truth.md`
- `docs/adr/0026-existing-mod-first-escalation-policy.md`
- `docs/adr/0027-compatibility-controlled-matcha-loading.md`
- `docs/adr/0028-adventure-rewards-not-essential-gates.md`
- `docs/adr/0029-unified-contextual-knowledge.md`
- `docs/adr/0030-no-global-enemy-scaling.md`
- `docs/adr/0031-fast-travel-requires-established-routes.md`
- `docs/adr/0032-optional-style-independent-blueprints.md`
- `docs/adr/0033-documented-gap-analysis-for-companion-code.md`
- `docs/adr/0034-preserve-existing-custom-code.md`

**Current responsibility:** Establish Many Roads Home as the working product title, modpack-first ownership, Packwiz source of truth, existing-mod-first escalation, controlled Matcha loading, non-essential adventure rewards, unified knowledge, no global enemy scaling, route-gated travel, optional blueprints, documented gap analysis, and preservation of historical custom code.

**Build and tests:** Not compiled. Decisions are reflected in current pack metadata, scripts, ownership matrix, and the inventory evidence.

**Possible duplicated mechanic:** These records explicitly classify generic building, storage, recipe, season, transport, horse, and skill UI work as third-party or deferred while retaining connective systems.

**Recommended destination:** Authoritative docs, unchanged.

**Migration risk:** High if ignored. Low if followed and superseded visibly.

**Rationale:** These are the current pivot authority and must remain reviewable.

### [KEEP] Earlier design ADRs

**Paths:**

- `docs/adr/0001-use-official-matcha-as-starting-pack.md`
- `docs/adr/0002-isolate-matcha-behind-adapter.md`
- `docs/adr/0003-classify-matcha-mechanics-individually.md`
- `docs/adr/0004-player-mastery-loadout.md`
- `docs/adr/0005-item-specialization-paths.md`
- `docs/adr/0006-repair-and-reforge-model.md`
- `docs/adr/0007-villager-mortality.md`
- `docs/adr/0008-hybrid-obol-currency.md`
- `docs/adr/0009-settlements-as-building-graphs.md`
- `docs/adr/0010-explicit-route-registration.md`
- `docs/adr/0011-travelers-cache.md`
- `docs/adr/0012-cross-dimensional-logistics.md`
- `docs/adr/0013-elytra-as-glider.md`
- `docs/adr/0014-climate-abstraction-for-seasons.md`
- `docs/adr/0015-conservative-worldgen-posture.md`
- `docs/adr/0016-existing-village-import.md`
- `docs/adr/0017-player-shop-pricing.md`
- `docs/adr/0018-infrastructure-gated-journey-skipping.md`
- `docs/adr/0019-use-java-not-kotlin.md`
- `docs/adr/0020-private-project-license-separation.md`
- `docs/adr/0021-layered-feature-packages.md`
- `docs/adr/0022-extensible-by-default.md`

**Current responsibility:** Original approved decisions for Matcha isolation, progression, equipment, villagers, economy, settlement graphs, routes, traveler cache, transport, climate, worldgen, village import, shops, journey skipping, Java, licensing, layering, and extensibility.

**Build and tests:** Not compiled. Relevant decisions are reflected in current source and tests. ADR 0021 directly inventories the current storage layers.

**Possible duplicated mechanic:** The ADRs identify generic candidates but do not install them.

**Recommended destination:** Authoritative historical/design docs, unchanged until explicitly superseded.

**Migration risk:** High if persistent-data or authority decisions are changed without a superseding ADR.

**Rationale:** Keep all records. The pivot changes ownership, not permission to erase design rationale.

### [ADAPT] Architecture ownership data

**Paths:** `docs/architecture/ownership-matrix.csv`

**Current responsibility:** Current per-system ownership table. It explicitly assigns third-party ownership for recipe viewing, information, building operations, vein mining, schematic previews, cooking, seasons, storage indexing, minecart speed, horses, structures, performance, and generic mastery UI, while retaining connective roles for Matcha, equipment, repairs, settlements, villagers, economy, routes, knowledge, and capability guarantees.

**Build and tests:** Not compiled. The matrix was read and cross-referenced with source and pack files.

**Possible duplicated mechanic:** It is the canonical duplicate map for this inventory.

**Recommended destination:** Authoritative pack architecture data, unchanged until research updates it.

**Migration risk:** High process risk if source ownership changes without updating this CSV. Low runtime risk.

**Rationale:** Keep. It is more current than the older architecture status statements.

### [ADAPT] Compatibility records

**Paths:**

- `docs/compatibility/adapter-contracts.md`
- `docs/compatibility/matcha.md`
- `docs/compatibility/matcha-loading.md`

**Current responsibility:** Adapter port rules, Matcha isolation, and the selected Global Packs loading approach. `matcha-loading.md` records exact versions, the selected loader, server verification, the client verification gap, Global Packs licensing risk, and rollback behavior.

**Build and tests:** Not compiled. The root GameTest independently passed. Client launch and pack export were not run in this inventory.

**Possible duplicated mechanic:** Global Packs and Matcha are third-party dependencies. The adapter contract prevents their internals from leaking into unrelated code.

**Recommended destination:** Compatibility docs, unchanged for now. Adapt after each candidate lab and dependency pin change.

**Migration risk:** High for pack load order, license, save adoption, and adapter status. Removing Matcha from an existing world is save-affecting.

**Rationale:** Keep the evidence and explicit uncertainty. Do not treat server verification as client acceptance.

### [KEEP] Matcha audit methodology and classification

**Paths:**

- `docs/matcha-audit/README.md`
- `docs/matcha-audit/findings.md`
- `docs/matcha-audit/manual-review-checklist.md`
- `docs/matcha-audit/system-classification-template.csv`
- `docs/matcha-audit/system-classification.csv`

**Current responsibility:** Audit conventions, findings, manual review gates, and per-system Matcha classifications. The current classification has 30 rows covering progression, fire/campfire, kiln, tools, substitutions, hunger, healing, food effects, death, equipment, durability, enchanting, XP, spawners, drops, villagers, trades, Obols, shulkers, loot, structures, Nether, End, weather, darkness, multiplayer, recipes, tutorialization, resource-pack identity, and mod compatibility.

**Build and tests:** Not compiled. Generated reports and the independent audit test support the evidence.

**Possible duplicated mechanic:** Matcha overlaps multiple old custom systems. The classification separates them rather than assuming the entire archive is one indivisible mechanic.

**Recommended destination:** Audit/design docs, unchanged.

**Migration risk:** High licensing and interpretation risk if audit text is copied into runtime code or if a new Matcha version is compared without regenerating reports.

**Rationale:** Keep as the required evidence trail.

### [KEEP] Asset direction and manifest

**Paths:**

- `docs/assets/art-direction.md`
- `docs/assets/asset-manifest.csv`
- `docs/assets/briefs/item-coin-purse.md`
- `docs/assets/pipeline.md`

**Current responsibility:** Approved visual direction, asset manifest, coin-purse brief, and asset validation workflow. The documents deliberately prevent final textures before an accepted gameplay need.

**Build and tests:** Not compiled. Asset validator compiled and its test passed. Explicit `validateAssets` was not run.

**Possible duplicated mechanic:** Resource packs and third-party mod art may overlap. No final gameplay art is implemented.

**Recommended destination:** Resource-pack and tools docs, unchanged. Adapt the manifest to actual pack-owned assets later.

**Migration risk:** Medium for stale references and false validation. Low save risk.

**Rationale:** Keep the approval gate and placeholder posture.

### [KEEP] Mod research and dependency lock

**Paths:**

- `docs/mod-research/README.md`
- `docs/mod-research/candidate-matrix.csv`
- `docs/mod-research/dependency-lock.md`

**Current responsibility:** The candidate matrix records the 58 current role candidates and exact research dispositions. The README defines the authoritative Modrinth/source-review method and re-verification commands. The dependency lock reports the exact current `pack/` inputs. These are complementary records, not substitutes for compatibility labs.

**Build and tests:** Not compiled. The candidate matrix and README were read and cross-checked with the Packwiz files. The dependency lock was read and cross-checked with the pack metadata.

**Possible duplicated mechanic:** The candidate matrix maps current generic responsibilities to third-party candidates and records their overlap, while the dependency lock records only current dependencies. Neither substitutes for a lab result.

**Recommended destination:** Generated pack research records, unchanged and regenerated where applicable by `scripts/report-dependencies.sh`. Update the candidate matrix through an explicit research ticket, not during implementation.

**Migration risk:** Medium if it becomes stale and is mistaken for candidate research.

**Rationale:** Keep the research records, but treat the candidate matrix as a screening record rather than a completed adoption decision. Candidate ownership remains provisional until the exact candidate passes its lab.

### [DEFER] Future system specifications with no implementation

**Paths:**

- `docs/systems/animals.md`
- `docs/systems/exploration-and-regions.md`
- `docs/systems/food-and-alchemy.md`
- `docs/systems/logistics.md`
- `docs/systems/projects.md`
- `docs/systems/seasons-and-weather.md`
- `docs/systems/transportation.md`
- `docs/systems/world-chronicle.md`
- `docs/blueprints/strategy.md`

**Current responsibility:** Design-only specifications for future animals, exploration, food, logistics, projects, seasons, transportation, and world history. No corresponding gameplay Java system exists in the current source tree.

**Build and tests:** Not compiled. No implementation or GameTest claims are made.

**Possible duplicated mechanic:** These records explicitly point toward Farmer's Delight, Serene Seasons, pipes, rail/horse mods, restrained structures, and companion connective systems. They must not authorize early custom implementations.

**Recommended destination:** Keep as design docs, with eventual ownership split between pack config, datapack, resource pack, third-party mods, and companion integration. Do not create placeholder classes.

**Migration risk:** Medium design drift, low current save risk because nothing is implemented.

**Rationale:** Defer. The current project scope is foundation plus pivot classification, not future gameplay implementation.

### [ADAPT] Implemented system specifications

**Paths:**

- `docs/systems/equipment.md`
- `docs/systems/mastery.md`
- `docs/systems/settlements.md`
- `docs/systems/storage.md`
- `docs/systems/economy.md`
- `docs/systems/matcha.md`
- `docs/systems/villagers.md`

**Current responsibility:** System specifications for the seven implemented feature areas and Matcha. They describe custom implementation status, acceptance rules, and backlog links.

**Build and tests:** Not compiled. Source and tests demonstrate that the seven areas have prototype implementations despite older foundation-only status text elsewhere.

**Possible duplicated mechanic:** Equipment, mastery UI, storage search, Matcha overlap, and some career/supply behavior may be partially duplicated by third-party candidates. Settlement, Obol economy, and Mason career remain connective.

**Recommended destination:** Adapt each specification to an ownership matrix and a release-status document. Preserve domain invariants, migration requirements, and tests. Split generic mechanics into pack/third-party sections.

**Migration risk:** High process risk because these specs can conflict with `docs/roadmap.md`, `CHANGELOG.md`, and the agent brief.

**Rationale:** Adapt, not delete. The specification is evidence of intended behavior, but the pivot requires explicit owner and gap analysis before more code.

### [KEEP] Design data for capabilities, farming, rewards, and building assistance

**Paths:**

- `docs/design/adventure-reward-policy.md`
- `docs/design/building-assistance.md`
- `docs/design/capability-web.csv`
- `docs/design/farm-pressure-matrix.csv`

**Current responsibility:** Cross-system design for non-essential adventure rewards, optional building assistance, multiple capability routes, and farm/resource pressure. `building-assistance.md` explicitly says it is design only and that no schematic parser, placement service, worker, custom block, or custom item exists.

**Build and tests:** Not compiled. No building-assistance code is present to test. The capability and farming rows are design data only.

**Possible duplicated mechanic:** Building operations, schematic previews, and automation should be supplied by existing mods. Capability guarantees, contextual consequences, and reward policy remain pack-specific.

**Recommended destination:** Pack/datapack design and future companion gap analysis. Optional blueprint content belongs in a separate library.

**Migration risk:** Medium design risk, low current save risk. Building-assistance requirements become high-risk if implementation begins without LAB-02/LAB-12.

**Rationale:** Keep the design and defer implementation. It is important negative evidence that several likely replacement systems do not exist in Java.

### [KEEP] Playtest records

**Paths:**

- `docs/playtests/README.md`
- `docs/playtests/fvr700-session-001.md`

**Current responsibility:** Playtest methodology and the first FVR-700 session record. The session identifies the central blocker: systems are implemented and tested but have no player-reachable entry points. It records 27 GameTests passing and a disposable `run/fvr700-playtest-world` marker.

**Build and tests:** Not compiled. The documented GameTest result is independently reproduced by `./gradlew runGametest` in this run.

**Possible duplicated mechanic:** The blocker is integration and player access, not a missing generic mechanic.

**Recommended destination:** Playtest evidence, unchanged with an explicit pivot follow-up. The session should not be read as release acceptance.

**Migration risk:** High process risk if prototype tests are mistaken for a playable pack. Low save risk because the world is marked disposable.

**Rationale:** Keep the honest negative result and use it to gate future work.

### [KEEP] Backlog archive

**Paths:** `docs/backlog-prototype-archive.md`

**Current responsibility:** Historical archive of prototype implementation tickets moved out of the active pivot queue.

**Build and tests:** Not compiled. It was read as historical context.

**Possible duplicated mechanic:** It contains old custom-system plans that may now be third-party or deferred.

**Recommended destination:** Historical docs, delete-later only after every ticket has a replacement owner or explicit closure.

**Migration risk:** Medium. Deleting it can lose rationale and migration references.

**Rationale:** Keep while the pivot maps old work to new ownership.

## 17. Root project and repository metadata

### [KEEP] Build configuration and wrappers

**Paths:**

- `build.gradle`
- `settings.gradle`
- `gradle.properties`
- `gradlew`
- `gradlew.bat`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradle/wrapper/gradle-wrapper.properties`
- `.editorconfig`
- `.github/workflows/ci.yml`

**Current responsibility:** Fabric Loom 1.17.20 build, split common/client/assetTools/GameTest source sets, Java 25 configuration, pinned dependencies, checkstyle, asset validation task, datagen scaffold, and CI. CI runs root build/test/architecture/checkstyle/asset validation plus independent audit tests without fetching Matcha.

**Build and tests:** The required root command `export JAVA_HOME=~/toolchains/jdk-25.0.4.1+1 && ./gradlew build test` passed with exit 0 and `BUILD SUCCESSFUL`. Tasks were mostly `UP-TO-DATE`, including compilation and test tasks. `runGametest` passed separately. No dependency was upgraded.

**Possible duplicated mechanic:** None directly. The build currently packages the custom companion implementation, including systems the pivot may later shrink.

**Recommended destination:** Companion and tools build infrastructure. Adapt source sets and CI only in a dedicated ownership/migration ticket. Keep the root Gradle baseline pinned.

**Migration risk:** High for source-set, loader, and Java changes. Medium for CI if it starts fetching third-party content, which it must not do.

**Rationale:** Keep. Do not edit build configuration for this inventory or for a documentation-only pivot classification.

### [KEEP] Repository safety, licensing, and contributor entrypoints

**Paths:**

- `.gitignore`
- `README.md`
- `CLAUDE.md`
- `.github/copilot-instructions.md`
- `LICENSE`
- `THIRD_PARTY_NOTICES.md`
- `CHANGELOG.md`

**Current responsibility:** Ignore rules, contributor and user entrypoints, agent pointers, proprietary license, Matcha third-party notice, and change history. `README.md` states the first implementation pass has seven systems, 126 unit tests, and 20 GameTests, which conflicts with the current observed 32 unit-test classes and 27 runtime GameTests. `CHANGELOG.md` still says foundation only.

**Build and tests:** Not compiled. Their status claims were compared with source and runtime evidence.

**Possible duplicated mechanic:** None directly. Incorrect status can cause someone to treat custom prototype mechanics as a released product or overlook the pack-first owner.

**Recommended destination:** Keep repository entrypoints and legal records. Adapt README/changelog/status in a dedicated documentation reconciliation ticket. Preserve `THIRD_PARTY_NOTICES.md` and license separation.

**Migration risk:** High legal/provenance risk for LICENSE and Matcha notices. High communication risk for stale README/CHANGELOG. Low build risk.

**Rationale:** Keep the files, adapt inaccurate status visibly. Never make the private repository public during this work.

### [KEEP] Art source and palette

**Paths:**

- `art/README.md`
- `art/palettes/forever-core.gpl`
- `art/palettes/.gitkeep`
- `art/previews/.gitkeep`
- `art/source/blocks/.gitkeep`
- `art/source/entities/.gitkeep`
- `art/source/gui/.gitkeep`
- `art/source/items/.gitkeep`
- `art/source/models/.gitkeep`
- `art/templates/.gitkeep`

**Current responsibility:** Asset source instructions, an original palette, and intentionally empty art staging directories.

**Build and tests:** Not compiled. Asset validator can inspect production resources, but no art validation command was run.

**Possible duplicated mechanic:** Third-party resource packs may provide art, but the project still needs a cohesive pack layer and licensing review.

**Recommended destination:** Resource pack/art tools, unchanged. Keep empty directories only while the pipeline uses them.

**Migration risk:** Medium licensing and provenance risk for future art. Low current save risk.

**Rationale:** No final textures are present, which is correct for an unapproved gameplay need.

### [KEEP] Compatibility laboratory scaffold

- `labs/README.md`
- `docs/testing/compatibility-labs.md`

**Current responsibility:** Defines isolated compatibility spikes, exact-version manifests, disposable-world rules, removal testing, and the 12-lab queue for information, building, food, seasons, storage, logistics, transport, skills, durability, villagers, exploration, and automation.

**Build and tests:** Not compiled. No lab was run by this inventory.

**Possible duplicated mechanic:** The queue is specifically for deciding which existing mods can replace or complement custom code.

**Recommended destination:** Developer research workflow, unchanged.

**Migration risk:** Medium process risk if labs are skipped and candidate claims are treated as facts.

**Rationale:** Keep as the evidence gate for all provisional third-party mappings.

## Identifier migration finding

The source currently uses mod id `forever` and package `dev.forever`. The working product title is Many Roads Home, and ADR 0023 discusses a future companion identity, but this inventory does **not** rename anything.

The costly identifiers are the ones that can be serialized into worlds, item stacks, components, registries, resource paths, or player-visible knowledge:

| Identifier class | Existing examples | Persistence or compatibility consequence |
|---|---|---|
| Registry IDs | `forever:field_tool`, `forever:obol` | Item/block identity in saved stacks and recipes. Renaming requires aliases or data migration. |
| Component and attachment types | `forever:equipment_state`, `forever:mastery_state`, `forever:career_state`, `forever:coin_purse`, `forever:traveler_cache_contents` | Player/item data survives reload and may be copied on death. Removing the type can strand or erase state. |
| SavedData keys | `forever:settlements`, `forever:storage/warehouses` | World-level state and migration. A key rename needs explicit read-old/write-new behavior and recovery tests. |
| Translation keys | `forever.*` keys in `src/main/resources/assets/forever/lang/en_us.json` | Player-facing text and resource-pack compatibility. Renaming can produce silent missing text. |
| Resource and data paths | `data/forever/...`, `assets/forever/...`, guide/process IDs | Datapack reload and saved discovery references can break. Loader namespace locks also make future external content harder. |
| Matcha private identifiers | Matcha scoreboards, functions, advancements, item identities, and model data in `dev.forever.compat.matcha` | Must remain isolated. Moving them outside the adapter creates compatibility and licensing risk. |
| Internal Java package/class names | `dev.forever.core...`, service and record names | Costly for source/API compatibility and tests, but generally not world-persistent unless serialized by name. These can be refactored with less save risk than IDs. |
| Log messages and local method names | Internal strings and helper names | Mostly non-persistent. Still useful for support, but not a save migration by themselves. |

The future companion id question is therefore a separate migration ticket. Before any public or persistent release, decide whether to retain `forever` for compatibility, add aliases, or write a tested migration. Do not infer that a product-title change permits an id change.

## Negative inventory: likely duplicated systems not found in Java

The following likely replacement candidates were explicitly searched for by responsibility and are **not implemented** in the current source tree. Their absence is a finding, not permission to add placeholders:

| Mechanic | Source found | Recommended owner | Classification of current state |
|---|---|---|---|
| Mass block placement, mirroring, palette replacement | None | Existing builder mod plus pack policy | DEFER |
| Vein mining | None | Existing vein-mining mod | DEFER |
| Schematic rendering and material preview | None | Litematica-class client tool | DEFER |
| Generic recipe viewer UI | Only a no-op port | EMI or REI, exactly one | DEFER |
| Generic contextual block/entity inspector | None | Jade or equivalent | DEFER |
| Seasons simulation | None | Serene Seasons or equivalent | DEFER |
| Generic cooking content | None | Farmer's Delight Refabricated or equivalent | DEFER |
| Minecart acceleration/rail physics | None | High-Speed Rail or equivalent | DEFER |
| Generic horse ownership/handling | None | Existing horse mod | DEFER |
| Structure generation | None | One restrained structure pack | DEFER |
| Global enemy scaling | None and prohibited by ADR 0030 | No global scaler | KEEP decision to not implement |

This negative inventory explains why there are no custom classes to classify as REPLACE for mass placement, vein mining, schematics, seasons, cooking, rails, horses, or structures. The existing generic storage index is the exception and is classified REPLACE above.

## Validation evidence

Commands actually run for this inventory:

1. `export JAVA_HOME=~/toolchains/jdk-25.0.4.1+1 && ./gradlew build test`
   - **Passed**, exit 0, `BUILD SUCCESSFUL`.
   - The output showed common, client, assetTools, GameTest, and test compilation tasks and `test` as `UP-TO-DATE`. This is configured-task green evidence, not a claim that Gradle freshly executed every test method during this run.
2. `export JAVA_HOME=~/toolchains/jdk-25.0.4.1+1 && ./gradlew runGametest`
   - **Passed**, exit 0, `BUILD SUCCESSFUL`.
   - Dedicated server loaded Minecraft 26.2, Fabric Loader 0.19.3, Fabric API, Forever, and the GameTest mod. The runner reported 27 tests passed. The source count discrepancy of 26 annotated methods is recorded above.
   - Matcha's own archive emitted parse warnings for malformed or stray content. Those warnings were not attributed to Forever.
3. `export JAVA_HOME=~/toolchains/jdk-25.0.4.1+1 && cd tools/matcha-audit && ./gradlew test`
   - **Passed**, exit 0, `BUILD SUCCESSFUL`.
   - Four actionable tasks were up to date.

Applicable commands intentionally not run:

- `./gradlew runClient`, because the current WSL environment has no usable display. Client verification remains pending in `docs/compatibility/matcha-loading.md`.
- `./gradlew runServer`, because the required dedicated-server acceptance was covered by `runGametest` and a separate pack-server verification is already documented. No real world was used.
- `./gradlew validateAssets`, because this inventory did not change assets and the requested compile/test status was already established. The asset validator itself compiled and its unit test task was green.
- `./scripts/fetch-matcha.sh`, `./scripts/install-matcha-dev.sh`, `./scripts/build-pack.sh`, `./scripts/validate-pack.sh`, and the PowerShell equivalents, because no dependency fetch, pack export, or world mutation was necessary. `install-matcha-dev.sh` was not pointed at any world.

## Riskiest migration items

1. **Generic storage index and warehouse persistence:** `WarehouseController`, `WarehouseState`, `WarehouseSavedData`, index revisions, and physical container references are the highest duplication, save, and item-loss risk. Replacing them with a third-party local index requires an explicit old-data migration, reconciliation, and no-duplication test plan.
2. **Persistent identity and schema IDs:** `forever:equipment_state`, `forever:mastery_state`, `forever:career_state`, `forever:coin_purse`, `forever:traveler_cache_contents`, `forever:settlements`, `forever:storage/warehouses`, `forever:obol`, and `forever:field_tool` are costly to rename or remove after persistent release. The current `forever` namespace must not be changed casually.
3. **Matcha and loader licensing/compatibility boundary:** Matcha is CC-BY-NC-SA-4.0 and Global Packs is All Rights Reserved. `MatchaProfile`, `MatchaServerEvidence`, `VerifiedMatchaAdapter`, `pack/defaultconfigs/global_packs.toml`, and the lock/archive workflow jointly determine whether a world receives the intended mechanics and whether private identifiers remain isolated.

## Next ticket and handoff

**Next backlog ticket:** FVR-014, the first official Matcha audit, remains the next foundation-era ticket in the existing backlog. In the pivot workstream, the immediate prerequisite is a documented candidate matrix and the LAB-01 through LAB-12 compatibility sequence before replacing or shrinking any custom system.

Do not start that ticket in this change. This file is an inventory and classification artifact only.

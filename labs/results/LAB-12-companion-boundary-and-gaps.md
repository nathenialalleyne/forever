# LAB-12: Companion boundary and gap analysis

- Date: 2026-08-28
- Run by: lab worker, automated evidence synthesis
- Manifest: `labs/manifests/LAB-12-companion-boundary-and-gaps.toml`
- Status: complete

## Question

After LAB-01 through LAB-11 and the client/licence follow-ups, what player-facing problems remain important to Many Roads Home and sufficiently unmet by existing tools to justify companion ownership?

## Exact versions

| Component | Project ID | Version ID | File |
|---|---|---|---|
| Minecraft | | | 26.2 |
| Fabric Loader | | | 0.19.3 |
| Fabric API | `P7dR8mSH` | `NqwNSxwA` | fabric-api-0.158.0+26.2.jar |
| Global Packs | `NRLPy2mk` | `DqrPrUMp` | globalpacks-fabric-26.2-26.2.0.jar |
| Matcha Flavoured | `QI0EmgZ1` | `E9rngRfK` | Matcha_Flavoured_1_12.zip |

This is an inventory lab. No new candidate dependency was installed and no primary-pack entry was changed. Exact candidate versions and authoritative Modrinth/source/licence records are retained in the preceding lab manifests and reports.

## Test world

- Seed: not applicable. No new world was opened for this synthesis.
- World type and difficulty: not applicable.
- Game rules changed: none.
- Disposable world path: none. Prior lab paths were under `/tmp` and are cited in their reports.
- Confirmed not a real survival world: yes. No real world was accessed.

## Client and server configuration

- Client tested: no new client run. LAB-CLIENT and LAB-CLIENT2 supply the available client evidence. Human UI and audio checks remain explicitly open where those reports say so.
- Dedicated server tested: no new server run. LAB-01 through LAB-11 provide the dedicated-server evidence used here.
- Multiplayer tested: no new multiplayer run. Authority gaps remain open where the source labs identify them.

## Startup outcome

No new startup was performed. The baseline evidence used for this decision is:

```text
[main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[main/INFO]: Found new data pack Matcha_Flavoured_1_12.zip, loading it automatically
[main/INFO]: Loaded 2346 recipes
[main/INFO]: Loaded 1805 advancements
[Server thread/INFO]: Done (1.478s)! For help, type "help"
```

LAB-02 is the important exception: Global Packs starts successfully even when Matcha is missing, altered, data-only, or overridden. The missing-archive case reported 1585 recipes and no diagnostic. That is the source of the pack-integrity gap below.

## Evidence inventory and boundary conclusion

The labs establish a deliberately small ownership boundary:

- Matcha owns food, cooking, food effects, hunger and healing. LAB-05 adopted nothing else.
- Vanilla owns ordinary placement, physical local transport, walking, horses, boats, rails, chest minecarts and Elytra. LAB-03, LAB-08 and LAB-09 found no reason to replace these foundations.
- REI and Jade own recipe inspection and contextual information. LAB-01 found no guidebook problem and no advancement-noise problem. Of 243 inspected advancements, 141 are recipe plumbing already silenced by Matcha, and only 69 show a toast. A Field Journal is therefore not approved as a solution to a disproved problem.
- Litematica, Effortless Building, VeinMiner and FallingTree remain pilots, not reasons to recreate renderers or placement/mining engines.
- Lithium and FerriteCore are the performance baseline. LAB-10 approved no structure provider because generated chunks are permanent.
- No teleport-like transport or universal wireless inventory belongs in the pack. This is required by ADR 0012 and was independently observed in LAB-07 through LAB-09.
- LAB-07 overturned the inventory code inventory's only `REPLACE` classification. No tested storage mod indexes arbitrary registered containers server-authoritatively without wireless access or data loss on removal. StorageGuide is only a read-only location-map pilot and is not a live warehouse index.
- LAB-02 found Global Packs fail-open behaviour. Its licence is All-Rights-Reserved with a dead source URL, so any feature depending on it inherits release and replacement risk.

The companion, if retained, is a connective layer only. It must not become a second cooking system, storage-item system, transport system, guidebook, worldgen provider, or generic automation mod.

## Candidate gap analyses

### G-01: Server-authoritative local storage index

**Player problem.** A player needs to find goods in nearby physical containers without walking every room, moving items into a wireless warehouse, or losing the meaning of local settlement logistics. StorageGuide maps configured locations but does not index live arbitrary containers, quantities, revisions, dimensions, or multiple settlement grids.

**Pack-identity importance.** High. Physical proximity, bounded local knowledge and visible stores are central to Many Roads Home's rejection of magical universal storage. This is the one candidate gap LAB-07 found to remain genuine after testing seven storage options.

**Owner and fallback.** A future `core.storage` application/adapter owned by the companion may own a bounded read/index projection over explicitly registered physical containers. The container and item remain Minecraft/vanilla-owned; the companion must never become the item authority. Fallback is ordinary chests, hoppers, chest minecarts and manual search. StorageGuide may be used as a read-only pilot while this question remains open.

**Affected systems.** Storage, settlements, warehouses, local transport, Field Guide/Jade presentation, permissions and future economy. It must not grant remote withdrawal or cross-dimensional access.

**Escalation record.** (1) Configuration cannot make vanilla containers searchable. (2) Datapacks can add recipes/tags/functions but cannot safely maintain a live arbitrary-container index. (3) Resource packs only change presentation. (4) No stable scripting layer was demonstrated for container lifecycle, revisions and bounded queries. (5) Existing public APIs were insufficient in the tested candidates for the exact server-authoritative contract. (6) A narrow adapter over StorageGuide or a supported storage API remains the preferred first implementation and must be tested before custom ownership. (7) A Mixin is not justified while container events/public hooks remain to be investigated. (8) A private fork is not justified, especially with StorageGuide's unresolved format/bounds concerns. (9) New custom code is **conditionally justified only after** the adapter/API route fails and the missing contract is documented under ADR 0033.

**Why config/datapacks are insufficient.** They cannot observe arbitrary container contents, atomically maintain stack revisions, identify dimensions, handle load/unload, or provide bounded server-authoritative query results while preserving physical source of truth.

**Costs before approval.** Persistent index records require an explicit schema version, codec, validation, migration, invalid-data preservation/recovery, and a rebuild path. Prefer a derived cache that can be discarded and rebuilt rather than authoritative item data. Network packets must be versioned, bounded, server-requested, permission-filtered and stale-result safe. Restart must rebuild or validate without duplication. Removal must leave all physical containers and contents intact and either remove only the derived cache or provide a documented cache purge. The companion adds ongoing container API compatibility and performance monitoring. StorageGuide's malformed-config blanking, one-grid scope and absent dimension identity must not be inherited silently. Licensing and notices for any provider must be rechecked; Global Packs' All-Rights-Reserved status is an independent release risk.

**Required tests before code approval.** With the companion absent, ordinary containers remain usable. Test malformed index/config input without destructive repair; restart and chunk unload/reload; duplicate insertion and concurrent scans; migration from every proposed schema; permission denial and client/server authority; stale revisions; container removal and mod removal; bounded query time and packet size; dimensions and multiple settlements; crash/interrupted rebuild recovery. Use disposable copies and retain the original save.

**Classification: CUSTOM CODE JUSTIFIED, CONDITIONAL.** This is the only current candidate that plausibly reaches rung 9, but it is not an implementation approval. First open a focused STORAGE-01/API investigation ticket and repeat the escalation at the actual selected provider boundary.

### G-02: Matcha/Global Packs integrity and startup diagnostics

**Player problem.** The server can boot with Matcha absent or only partially loaded while reporting no warning. Players may unknowingly receive vanilla food and progression instead of the pack foundation.

**Pack-identity importance.** High for correctness, but low as a companion gameplay system. It protects the baseline rather than adding a mechanic.

**Owner and fallback.** Pack/build validation and a narrow startup diagnostic owner. Fallback is vanilla Minecraft with an explicit loud warning and documented unsupported state, never silent partial success. Global Packs remains the loader candidate, not a custom replacement.

**Affected systems.** Matcha loading, Global Packs, startup diagnostics, release/build tooling and support.

**Escalation record.** (1) Global Packs configuration expresses loading but not integrity validation. (2) Datapack functions cannot reliably detect absence before the relevant data is missing. (3) Resource packs cannot validate server data. (4) No stable scripting layer was found. (5) A public loader API was not demonstrated. (6) A narrow adapter/check around the loader or server datapack state is plausible. (7) Mixin is unnecessary. (8) Fork is unjustified. (9) A new pack loader is rejected because Global Packs works. The correct classification is **use existing tool plus diagnostic**, not custom gameplay code.

**Costs and tests.** No gameplay save schema should be introduced. If a world marker is used, it must be versioned and migration-safe. Test missing, altered, data-only, wrong-order and correct archives; restart; malformed marker; duplicate detection; server authority; client resource-role visibility; removal and recovery. Global Packs' All-Rights-Reserved licence and dead source URL must be resolved before distribution.

**Classification: USE EXISTING TOOL.** Raise MRH-010/pack-integrity follow-through. Do not grow the companion around Global Packs.

### G-03: Cross-mod capability unlocks, equipment costs and project validation

**Player problem.** Future pack concepts may need a shared capability state that gates optional projects, validates materials/equipment and explains failure without making ordinary vanilla play unavailable.

**Pack-identity importance.** Potentially high, but unproven. LAB-11 found no essential capability that needed adventure-only rewards or quests. LAB-01 found no Field Journal emergency. No concrete player failure case has been measured.

**Owner and fallback.** Until a concrete use case exists, existing adopted tools own their operations and datapacks/advancements own discovery. Fallback is ordinary building, mining and crafting without a gate. A future companion may own only a concrete connective rule, not a universal capability framework.

**Affected systems.** Building, excavation, Ghost Plan, settlements, projects, equipment, discovery, REI/Jade and Matcha adapter.

**Escalation record.** (1) Existing configuration can set many tool limits and permissions. (2) Datapacks/advancements can express discovery and recipes. (3) Resource packs can explain states. (4) Stable scripting was not shown necessary. (5) Public APIs/events of the adopted tools may be enough. (6) Narrow adapters should be attempted per tool. (7) Mixin is premature. (8) Fork is unjustified. (9) No concrete requirement reaches custom implementation.

**Costs and tests.** Any player capability or project state would be persistent and require versioned schema, codec, validation, migration, backup/recovery and visible invalid-state handling. Network state must be bounded and server-authoritative. Removal must preserve ordinary blocks/items and clear only derived records. Test absent integrations, malformed definitions, restart, duplication, migration and permission/client authority for each concrete rule.

**Classification: DEFER BECAUSE EVIDENCE IS INCOMPLETE.** Do not implement a capability web, reservation system or project registry now. Open a focused ticket only when one measured cross-mod workflow cannot be expressed by data/configuration or a narrow adapter.

### G-04: Climate provider normalization

**Player problem.** If seasonal gameplay is adopted, pack systems need a provider-neutral calendar, weather and temperature view rather than hardcoding Homeostatic Seasons.

**Pack-identity importance.** Medium to high, but the gameplay contract is not yet approved. LAB-06 selected Homeostatic as a pilot behind an abstraction and explicitly deferred crop/greenhouse behaviour.

**Owner and fallback.** Existing Homeostatic pilot/provider owns simulation. A narrow optional adapter owns translation only, with a neutral climate fallback when absent. Ordinary crops and vanilla weather remain available.

**Affected systems.** Climate, crops, settlements, Matcha environment, client projection and future economy.

**Escalation record.** (1) Provider configuration and (2) datapack crop mappings should be tried first. (3) Resource pack only explains state. (4) Stable scripting was not required. (5) Provider API exists for calendar/transition/weather/temperature. (6) A narrow adapter is the selected route. (7) Mixin is unnecessary. (8) Fork is unjustified. (9) New calendar simulation is not justified.

**Costs and tests.** A normalized calendar record would require schema/version/codec/validation/migration, but provider saved data must not be copied into it. Packets need versioning and bounded stale-client handling. Provider removal after seasonal snow/crop state can leave unknown rules or records, as LAB-06 observed for alternatives. Test absent provider, malformed provider state, restart and season boundary, duplication, migration, unknown crop, authority, and removal from a copied save. Licence status must be rechecked before adoption.

**Classification: USE EXISTING TOOL.** Continue the Homeostatic pilot behind the abstraction. This is not approval for a custom calendar or crop system.

### G-05: Unified knowledge / Field Journal

**Player problem.** A unified explanation surface could eventually combine provider knowledge and discovery state. However, the measured premise that the current pack has too many guidebooks or advancement notifications is false.

**Pack-identity importance.** Potentially useful, not currently demonstrated. Matcha's readable advancements, REI and Jade already provide instruction and context.

**Owner and fallback.** Matcha/REI/Jade remain owners today. Fallback is the existing advancement, recipe viewer and tooltip path. No second guidebook is granted.

**Affected systems.** Information, Matcha adapter, REI, Jade, client projection and future content.

**Escalation record.** (1) Existing viewer configuration and (2) Matcha datapack already solve current instruction. (3) Resource pack can add translations/icons. (4) Stable scripting is unnecessary. (5) REI/Jade public seams exist. (6) A narrow integration could add provenance later. (7) Mixin is not justified. (8) Fork is unjustified. (9) A custom journal is not justified by current evidence.

**Costs and tests.** A discovery/provenance record would need schema, codec, validation, migration, stale-source handling and recovery. Client packets must be bounded and server-authoritative. Removal of REI/Jade must degrade to text. Tests must cover absent viewers, malformed entries, restart, duplicate discovery, migration, stale Matcha IDs, server authority and removal. Patchouli has no verified 26.2 release; LAB-11 also found FTB Quests unverified and a second book inappropriate.

**Classification: DEFER BECAUSE EVIDENCE IS INCOMPLETE.** Do not start Field Journal implementation until a new adopted content source creates a concrete information gap.

## Persistence, network, removal and licensing gate

No companion persistent structure is approved by this lab. If G-01 or any later gap proceeds, its ticket must specify schema version, codec, validation, migration, backup, failed-migration recovery, save/reload and restart behaviour before code. Derived indexes should be rebuildable and never own items. All client data is a projection; packets require explicit versions, bounds, permission checks and stale handling. Removal must preserve vanilla blocks/items and avoid orphaned registry IDs. Third-party JARs must never be modified.

The licence lab blocks casual expansion: Global Packs is All-Rights-Reserved with a dead source URL; Matcha and Jade are CC-BY-NC-SA-4.0; the project has incomplete third-party notices and generated Matcha-derived reports with provenance concerns. Every future provider or companion distribution needs a fresh licence and notice review.

## Validation commands

Commands actually run for this lab:

- `cat`/repository inspection of all 14 prior result files in `labs/results/`, including LAB-01 through LAB-11, LAB-CLIENT, LAB-CLIENT2 and LAB-LICENCE.
- Inspection of `docs/backlog.md`, `docs/architecture.md`, `docs/architecture/ownership-matrix.csv`, `docs/world-save-safety.md`, ADRs 0022, 0024, 0026 and 0033, `labs/README.md`, `docs/testing/compatibility-labs.md`, and the candidate matrix.
- Direct review of the prior labs' quoted logs, manifests, Modrinth/source/licence evidence, removal probes and follow-up requirements.
- `export JAVA_HOME=~/toolchains/jdk-25.0.4.1+1` was set for the lab environment; no new game process was required.

Intentionally not run:

- No client, server, world, multiplayer or Modrinth download run. This ticket is a synthesis and must not invent new gameplay evidence.
- `./gradlew build`, `./gradlew test`, `./gradlew runGametest`, `./scripts/validate-pack.sh`, and `./scripts/build-pack.sh`, because no Java, pack, dependency or generated build input changed.
- No Matcha acquisition or installation and no real-world access.

## Does it fit the pack?

Yes, only as a restraint boundary and a queue of narrowly evidenced follow-ups. The companion must not be retained as a justification for prototype systems that duplicate adopted tools. Of the plausible gaps, only server-authoritative local storage indexing currently has a credible path toward custom ownership, and even that is conditional on failing a narrow adapter/API investigation. The pack-integrity issue belongs to validation/diagnostics, and climate belongs behind an existing provider abstraction. Capability web and Field Journal proposals remain unproven.

## Follow-up required

1. Open or continue STORAGE-01 to test the smallest server-authoritative physical-container index, beginning with public APIs or a narrow StorageGuide adapter. Do not write code from this report alone.
2. Complete MRH-010 pack-integrity diagnostics without adding a custom loader or relying on silent Global Packs behaviour.
3. Continue the Homeostatic Seasons pilot only behind the provider-neutral abstraction, including save/removal tests.
4. Do not implement a universal capability web, reservation framework, project registry or Field Journal until a concrete cross-mod failure survives the escalation order.
5. Revisit all decisions when the pinned Minecraft version, candidate versions, provider licences, or persistent schemas change. The next product ticket is PACK-01, baseline assembly, subject to the LAB-12 gate.

## Decision

| Gap | Player problem and owner | Classification | Decision |
|---|---|---|---|
| G-01 server-authoritative local storage index | Find goods in bounded physical containers; future companion storage adapter/application, vanilla remains item owner | **custom code justified, conditional** | `NEEDS_MORE_EVIDENCE`: first exhaust public API and narrow adapter route; then a separate ADR-0033 implementation ticket may be proposed |
| G-02 Matcha/Global Packs integrity diagnostics | Prevent silent partial baseline; pack tooling/diagnostic owner, Global Packs remains loader | **use existing tool** | `COMPATIBILITY_SPIKE`: continue MRH-010; do not build a replacement loader |
| G-03 capability unlocks, reservations and project validation | Connect adopted tools for a future concrete project workflow; no measured current failure | **defer because evidence is incomplete** | `DEFER`: no universal capability web or reservation framework |
| G-04 climate normalization | Keep seasonal systems provider-neutral; Homeostatic pilot plus narrow optional adapter | **use existing tool** | `PILOT`: continue provider abstraction; no custom calendar/crop simulation |
| G-05 unified knowledge / Field Journal | Combine future sources; current Matcha/REI/Jade path already works | **defer because evidence is incomplete** | `DEFER`: no journal implementation or second guidebook |

**Capstone conclusion:** custom code is conditionally justified for only G-01, and no custom code is authorized by LAB-12. G-02 and G-04 should use existing tools and narrow configuration/adapter work. G-03 and G-05 do not yet clear the evidence gate.

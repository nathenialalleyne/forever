# MRH-012: Proposed diagnostic-only artifact boundary

**Status:** Design ready, explicitly pending owner approval. This document is evidence and a
proposal, not an implementation approval, an artifact approval, or a distribution grant.

**Scope:** Define the smallest *behavioural* boundary that a later ticket may implement for
the read-only Matcha baseline diagnostic from MRH-010. No source-set, module, project, fork,
shading, or packaging architecture is selected here.

## Assessment

The existing root entrypoint is not a diagnostic-only boundary. `fabric.mod.json` registers
`dev.forever.ForeverMod` as its common `main` entrypoint and
`dev.forever.client.ForeverClient` as its client entrypoint. `ForeverMod.onInitialize()`
then unconditionally calls seven prototype initialisers before it registers Matcha
compatibility (`src/main/java/dev/forever/ForeverMod.java:41-63`). Reusing the current root
entrypoint would therefore publish a full prototype runtime, not MRH-010 alone. The full root
JAR is rejected as the MRH-011 artifact for this reason.

The proposed boundary is a common/server-safe Fabric entrypoint whose only owned behaviour is:

1. register the Matcha server lifecycle observations;
2. gather bounded server-visible evidence after startup and successful datapack reload;
3. classify that evidence without guessing; and
4. emit the existing actionable baseline report without blocking startup or writing world
   state.

The required entrypoint is therefore a **diagnostic-only common `main` entrypoint**. The
current `ForeverMod` is not that entrypoint. This names the lifecycle contract, not the
implementation architecture. Whether a future implementation reuses, splits, or replaces
any current class is deliberately left to MRH-013 after this boundary is approved.

This proposal is not implementable by merely rewiring the existing report. The current
`MatchaServerEvidence.gather()` passes `null` for the observed digest and metadata and an
empty fingerprint map, `MatchaBaselineReport.fromStatus()` does not put the locked SHA-256
in its missing report, and no live healthy result has been demonstrated. Server-side
resource observation also cannot verify a remote client's resource-pack role, and the
current lifecycle registration has no proof of repeated-initialization idempotence. These
are source-vs-spec and evidence gaps for MRH-013, not reasons to weaken MRH-010. This is a
review correction, not a superseding ADR or a build-architecture decision.

## Source-backed runtime trace

| Stage | Source evidence | What actually happens today | Boundary consequence |
| --- | --- | --- | --- |
| Fabric metadata | `src/main/resources/fabric.mod.json:12-24` | Common `main` is `dev.forever.ForeverMod`; `client` is `dev.forever.client.ForeverClient`; the mod requires Fabric Loader `>=0.19.3`, Minecraft `~26.2`, Java `>=25`, and Fabric API. | A candidate artifact needs a server-safe diagnostic `main` registration and must not register the client initializer. The current metadata is baseline context, not a candidate manifest. |
| Common initialization | `ForeverMod.java:41-63` | Equipment, mastery, settlement, storage, guide, career, economy, and Matcha compatibility are all called in order. | All seven prototype registrations are outside the proposed boundary. |
| Matcha registration | `ForeverMatchaCompat.initialize():20-37` | Installs an absent no-op adapter, registers `SERVER_STARTED`, and registers successful `END_DATA_PACK_RELOAD`. | Lifecycle hooks are in scope; adapter gameplay translation is not. |
| Live evidence | `MatchaServerEvidence.gather():76-109` | Reads `ResourceManager.getNamespaces()` and a scoreboard objective. It probes ten fixed non-vanilla namespaces and records at most 64 paths. It never walks the archive and never mutates the server. | The bounded evidence observation is in scope. The diagnostic remains read-only. |
| Runtime marker observation | `MatchaServerEvidence.java:138-166` | Looks for the exact `version_number` objective. It records objective presence but no score-holder value or advancement IDs. | Marker absence is unverified evidence, not proof of absence. The implementation must retain this conservative rule. |
| Evidence value | `MatchaDetectionEvidence` and `MatchaRuntimeMarkers` | Validates bounded hashes, paths, identifiers, maps, sets, and diagnostics. `gather()` returns `ABSENT`, `PRESENT`, or `MALFORMED` evidence. | These validation contracts are diagnostic concerns. They do not own gameplay or save data. |
| Detection | `MatchaVersionDetector.detect()` | Requires the locked digest, metadata, complete fingerprints, and complete runtime markers for `SUPPORTED`; otherwise it returns an explicit non-supported state. | No nearest-version or fuzzy-match mode is allowed. |
| Adapter/report publication | `ForeverMatchaCompat.initialize(MatchaDetectionEvidence):41-54` and `MatchaBaselineReport.fromStatus()` | Publishes a safe adapter, logs the detection decision, then maps the status to `HEALTHY`, `DEGRADED`, or `MISSING`. | The baseline report and its actionable remedies are in scope. Matcha item/behaviour mapping is not. |
| Startup behaviour | `ForeverMatchaCompat.java:75-100` | `MISSING` logs a framed ERROR banner; `DEGRADED` logs WARN; healthy logs INFO. No branch throws or stops startup. | Startup must remain unblocked and the report must remain visible. |
| Datapack configuration | `pack/defaultconfigs/global_packs.toml` and `pack/datapacks/matcha-flavoured.pw.toml` | Global Packs is configured to load one pinned archive from `datapacks/Matcha_Flavoured_1_12.zip` in both required roles. Packwiz metadata supplies the URL and hash. | The loader and archive remain pack-owned inputs. The diagnostic does not replace, extract, split, or rewrite them. |

### Current status mapping, including its limits

The live gatherer deliberately supplies no archive digest, no pack metadata, and no file
fingerprints because a running server sees unpacked resources rather than the original zip.
It supplies namespace paths and, when available, only the objective name. Consequently:

A dedicated server cannot inspect whether a remote client has activated the archive's
resource-pack role. `getNamespaces()` and the scoreboard observe data-side signals only.
Even a healthy server-side result is therefore not full dual-role validation. MRH-010's
one-sided role requirement remains "where detectable on that side"; MRH-013 must add a
separate assembled-pack/client verification of the resource role. No client feature is
auto-approved by this boundary.

- no known Matcha namespace produces `ABSENT`, then `MISSING`, and the framed ERROR report;
- one or more known namespaces normally produces `PRESENT` evidence, then
  `PRESENT_UNVERIFIED`, then `DEGRADED` WARN because the full identity evidence is absent;
- a gather failure produces malformed evidence and a safe degraded report; and
- synthetic complete evidence in `MatchaVersionDetectorTest` can produce `SUPPORTED` and a
  healthy report, but that is not evidence that a built artifact or a live server currently
  supplies complete identity evidence; and
- the current source does not demonstrate the healthy-install no-new-noise requirement,
  distinct live checksum-mismatch reporting, or an idempotent lifecycle/report sequence.

LAB-02's missing archive and structurally valid decoy both supplied no Matcha namespaces and
therefore remain recorded as `MISSING` evidence. A partially altered *real* archive producing
the `DEGRADED`/WARN form is still unverified and belongs to MRH-013. No diagnostic artifact
hash or gameplay-absence claim is made here.

## Exact source closure and current prototype reachability

The closure below is a source-inspection result, not a claim about a candidate JAR. A bounded
lexical class-reference check found 183 common Java classes, 28 classes under
`dev.forever.compat.matcha`, and 143 common classes reachable from `ForeverMod` (the root
entrypoint, 28 Matcha classes, 112 seven-system classes, and two shared helper classes).
The check found no duplicate Java basenames. Java compilation still places the complete
production source set in the current root build; source reachability must not be confused
with a packaged-artifact isolation test.

### The seven initialisers in the current baseline

| Call in `ForeverMod` | Registration or side effect confirmed by source | State or gameplay surface | Candidate boundary |
| --- | --- | --- | --- |
| `ForeverEquipment.initialize()` (`:46`) | Installs balance reload handling, registers the `forever:equipment_state` data component and `forever:field_tool`, and installs a tooltip provider. | Item registry, item component, condition/repair prototype and bundled equipment data. | Exclude. |
| `ForeverMastery.initialize()` (`:48`) | Registers the persistent `forever:mastery_state` player attachment and mastery server-data reload listener. | Persistent player progression and mastery data. | Exclude. |
| `ForeverSettlement.initialize()` (`:49`) | Forces `SettlementSavedData.TYPE` registration for `forever:settlements`. | Persistent world SavedData and settlement prototype. | Exclude. |
| `ForeverStorage.initialize()` (`:50`) | Installs storage balance data, `forever:traveler_cache_contents`, the Traveler's Cache item, and `forever:storage/warehouses` SavedData. | Persistent item/world state, container index concepts, and storage prototype data. | Exclude. |
| `ForeverGuide.initialize()` (`:51`) | Registers the server-data Field Guide reload listener. | Prototype guide catalogue and gameplay-facing guide resources. | Exclude; no client surface is needed for a baseline log. |
| `ForeverCareer.initialize()` (`:55`) | Registers `forever:career_state`, Mason data reload handling, and a living-entity `AFTER_DEATH` hook. | Persistent villager state, career/mortality behaviour, and Mason data. | Exclude. |
| `ForeverEconomy.initialize()` (`:56`) | Registers `forever:coin_purse`, the `forever:obol` item, and economy balance reload handling. | Persistent player/item economy state and economy data. | Exclude. |

These are baseline artifact behaviour, not seven capabilities that the diagnostic boundary
may carry. The existing root entrypoint also loads the transitive feature classes behind
those registrations, including codecs, saved-data records, attachment records, data loaders,
services, and result types. `ForeverMod` cannot be used as a harmless wrapper around the
Matcha diagnostic.

### Current Matcha lifecycle closure

Starting at `ForeverMatchaCompat` and following its actual source references reaches all 28
classes in the Matcha package because `MatchaAdapters.fromEvidence()` constructs either the
no-op or verified adapter, and the adapter contract includes item and behaviour translation.
This is the conservative closure of reusing the current lifecycle as written:

```text
ForeverMatchaCompat
MatchaAdapter, MatchaAdapterStatus, MatchaAdapters, MatchaBaselineReport
MatchaBehaviorCapability, MatchaBehaviorObservation, MatchaBehaviorTranslation
MatchaCapability, MatchaConfidence
MatchaDetectionEvidence, MatchaDetectionResult, MatchaDetectionStatus
MatchaItemIdentityCapability, MatchaItemObservation, MatchaItemTranslation
MatchaPackMetadata, MatchaPresence, MatchaProfile, MatchaRuntimeMarkers
MatchaServerEvidence, MatchaSignalKind, MatchaSupportLevel, MatchaTranslationStatus
MatchaVersionDetector, NoOpMatchaAdapter, VanillaItemFallback, VerifiedMatchaAdapter
```

This list is an inventory of existing source dependencies, not an approval to ship all of
it. In particular, `VerifiedMatchaAdapter` contains exact item model and behaviour mapping
records, while the observation and translation types support gameplay-facing compatibility.
Those are not required to print a baseline-health report and are excluded from the proposed
artifact boundary. `MatchaProfile` also currently combines archive/profile evidence with
identity components and runtime mapping markers. MRH-013 must prove that unrelated
Matcha-derived mapping material is not pulled into the diagnostic artifact, rather than
assuming that package membership alone is sufficient isolation.

### Conceptual diagnostic closure (proposed, pending approval)

The smallest behavioural closure is the following set of responsibilities, plus one
common/server-safe entrypoint and lifecycle owner:

| Diagnostic responsibility | Existing source that provides evidence | Proposed treatment |
| --- | --- | --- |
| Bounded live observation | `MatchaServerEvidence` | Reuse the ten-namespace and marker observation semantics. |
| Validated evidence records | `MatchaDetectionEvidence`, `MatchaRuntimeMarkers`, `MatchaPackMetadata` | Reuse only the validation needed for detection. |
| Pinned profile facts | The evidence subset of `MatchaProfile` | Keep archive/profile provenance separate from item/behaviour mapping facts. |
| Detection outcome | `MatchaVersionDetector`, `MatchaDetectionResult`, `MatchaDetectionStatus`, `MatchaPresence`, `MatchaConfidence`, `MatchaSupportLevel` | Reuse exact/refuse-to-guess classification. |
| Operator report | `MatchaBaselineReport` and, if its current signature is retained, `MatchaAdapterStatus` | Reuse wording/severity/remedy semantics without enabling mappings. |
| Lifecycle/logging | Current mixed `ForeverMatchaCompat` shows the required hooks but also publishes a gameplay adapter | Implement only the lifecycle/report path after approval. The current root entrypoint is not eligible. |

This is a conceptual boundary, not a new class list or build decision. The future ticket must
show the final closure from its actual artifact, because the current `ForeverMatchaCompat`
API couples reporting to the full adapter graph. No implementation may silently retain that
coupling and call the result diagnostic-only.

## Explicit exclusions

The owner approval requested by MRH-012 covers the diagnostic behaviour only. It does **not**
approve any of the following:

- `ForeverMod` or any of the seven prototype initialisers;
- equipment, mastery, settlement, storage, guide, career, or economy registries, data
  reload listeners, items, components, attachments, SavedData, or gameplay resources;
- any `forever:*` persistent identifier, schema, migration, network payload, client
  projection, or server-to-client state;
- `ForeverClient`, a client entrypoint, screens, HUDs, tooltips, REI/Jade registration, or
  `src/client/resources/assets/forever/**` assets;
- `src/main/resources/data/forever/**` gameplay data and unrelated Forever translations;
- Matcha item identity and behaviour translation mappings, `VerifiedMatchaAdapter`, or
  unrelated Matcha-derived content;
- `tools/matcha-audit`, `generated/matcha/1.12/`, or the ignored `vendor/matcha/` archive as
  runtime code or packaged content. They remain separate audit/provenance material;
- a replacement loader, archive extraction, archive splitting, archive rewrite, third-party
  JAR modification, or a new gameplay system; or
- a source-set, module, project, fork, shading, publication, licence, or rights-model
  decision.

`global_packs.toml`, the Packwiz Matcha metadata, `matcha.lock.json`, and the generated
namespace report remain configuration/provenance inputs. The current runtime does not read
`matcha.lock.json` or the zip directly. `MatchaProfile` contains a copied expected digest,
so the implementation must not describe that constant as proof that a running server has
re-hashed the archive.

## Why reuse is minimal and justified

LAB-02 established that Global Packs configuration cannot detect a missing, one-sided, or
substituted archive. `log_pack_ids = true` produced the same relevant listing for a healthy
archive and a decoy. The diagnostic therefore addresses a measured pack-integrity gap, not a
preference for custom Java or a second gameplay owner.

The proposed reuse is limited to the already-tested bounded evidence, refusal-to-guess
classification, and actionable report. It does not reuse the root initializer, generic
prototype systems, or Matcha gameplay mappings. A custom pack loader is rejected because
Global Packs already loads the one archive in both roles. The escalation result is
**use the existing loader plus a narrow read-only diagnostic**. MRH-013 must still document
why its selected implementation remains the smallest safe owner under ADRs 0026 and 0033.

## Persistence, authority, removal, and failure behaviour

The diagnostic reads resource namespaces and scoreboard metadata and holds only process-local
status. It must introduce no persistent schema, SavedData key, attachment, item component,
network packet, migration, or world write. The server remains authoritative for the evidence
it can observe, but it cannot assert that a remote client activated the resource-pack role.
A separate assembled-pack/client verification is therefore required for full dual-role
validation; no client feature is approved by this document.

On absent, malformed, unsupported, or unverified evidence, startup remains unblocked and the
report is visible. The diagnostic must not repair, download, modify, or remove Matcha. Removing
the diagnostic can restore the existing silent loader failure, but it must not strand or
migrate gameplay state because the proposed boundary owns none. Removing or changing Matcha
or Global Packs remains a pack/world compatibility operation governed by ADR 0027 and
`docs/world-save-safety.md`.

## Provenance and rights boundary

- Original Forever source and original assets remain under the private-project,
  All-Rights-Reserved model in `LICENSE` and ADR 0020.
- Matcha Flavoured `1.12` remains a separately identified CC-BY-NC-SA-4.0 input, pinned by
  `matcha.lock.json` and `pack/datapacks/matcha-flavoured.pw.toml`. Its private identifiers,
  generated excerpts, and audit reports remain third-party-derived material.
- Global Packs `26.2.0` remains a separately identified All-Rights-Reserved dependency with
  the release risk recorded in ADR 0027 and `docs/compatibility/pack-input-provenance.md`.
- The standalone audit tool and generated reports are evidence, not a runtime library or a
  redistribution grant. The fixture pack under `tools/matcha-audit/src/test/resources/`
  remains synthetic test input and must not be confused with the official archive.
- Private preparation, a successful local build, or owner approval of this boundary does not
  authorise publication, pack inclusion, or distribution. The open rights decision remains a
  separate owner gate.

No source, generated report, archive, licence, or notice was changed by this design.

## Validation matrix

| Concern | Existing evidence inspected | Future implementation evidence required | MRH-012 status |
| --- | --- | --- | --- |
| Entrypoint registration | `fabric.mod.json` and `ForeverMod.java:41-63` | Artifact metadata names only the approved diagnostic common entrypoint and no client entrypoint. | Source confirmed; candidate unbuilt. |
| Seven prototype initialisers | All seven `Forever*.initialize()` implementations and their registrations | Dedicated artifact scan/startup proves no seven-system initializer, registry, attachment, item, SavedData, or reload listener runs. | Baseline behaviour distinguished; absence unverified. |
| Matcha lifecycle | `SERVER_STARTED` and successful `END_DATA_PACK_RELOAD` hooks | A repeated-initialization fixture proves exactly one registration set; one observation/report per `SERVER_STARTED` or successful reload; no report on failed reload; and no stale cross-server result. | Hooks are present, but lifecycle idempotence and cross-server isolation are unverified. |
| Bounded evidence | `MatchaServerEvidence`, fixed ten namespaces, `version_number`, 64-path cap | Live dedicated-server tests cover absent, partial namespace, marker unavailable, malformed read, and successful reload without treating server evidence as proof of a remote client resource role. | Existing source and GameTests reviewed; complete identity and remote-client role remain unverified. |
| Detection/report mapping | `MatchaVersionDetectorTest`, `MatchaBaselineReportTest`, `MatchaDetectionGameTest` | Before shipping, the artifact reports a missing archive with the expected locked SHA-256, distinguishes a checksum mismatch from Matcha parse errors, reports one-sided roles where detectable, preserves the remedy text, keeps startup unblocked, produces no new noise for a healthy install, and covers healthy, missing, decoy/MISSING, partially altered real archive/DEGRADED-WARN, unsupported, and malformed cases. | Synthetic mapping and missing/decoy evidence exist; the source-vs-spec acceptance gaps and partial-real case remain open. |
| Pack inputs and roles | `global_packs.toml`, Packwiz metadata, Matcha lock and LAB-02 | Exact pinned archive and Global Packs roles are present without extraction or rewrite; altered input remains visible, with a separate assembled-pack/client check for the remote resource role because server evidence is insufficient. | Pack baseline evidence exists; no candidate artifact or full dual-role validation. |
| Startup and world safety | Read-only source path and report tests | Dedicated server reaches ready state for every failure status, a healthy install adds no diagnostic noise, and world files are unchanged by the diagnostic. | No world run in MRH-012; healthy no-new-noise remains unverified. |
| Persistent state/network | Seven initializer source inspections and architecture rules | Artifact contains no gameplay persistence, migration, packet, or client-authority path. | Proposed exclusion; not verified from candidate artifact. |
| Client boundary | Current `client` metadata and `ForeverClient` source | Dedicated server loads with no client class; candidate metadata has no client registration or client asset dependency; a separate pack/client check verifies the resource role. A healthy server result is not dual-role proof. | Current client surface explicitly excluded; server-only observation cannot verify the remote resource role. |
| Provenance and rights | `LICENSE`, ADR 0020, ADR 0027, `THIRD_PARTY_NOTICES.md`, lock/provenance records | Candidate identity, source, notices, removal notes, and rights decision are reviewed before MRH-011 packaging. | Documentation boundary only; rights remain open. |
| Removal | ADR 0034 and world-save safety rules | Removing diagnostic code does not delete source or alter existing save identifiers; changing Matcha/loader follows a separate migration review. | No removal operation performed. |

## Existing tests and limits

The following existing tests are preserved and were inspected as evidence, not reclassified as
artifact approval:

- `MatchaBaselineReportTest` checks severity mapping, remedies, immutability, and the
  no-startup-blocking report contract.
- `MatchaVersionDetectorTest` checks exact synthetic support, absence, digest and fingerprint
  conflicts, runtime-marker conflict, ambiguity, malformed input, determinism, and safe adapter
  fallback.
- `MatchaServerEvidenceTest` cross-checks the ten probe namespaces against
  `generated/matcha/1.12/namespaces.json`, excludes `minecraft`, and checks `version_number`.
- `MatchaDetectionGameTest` exercises live-server evidence, absent/no-op behaviour, malformed
  evidence, and report/status consistency. It currently runs through the full root prototype
  mod, so it is not proof of diagnostic-only artifact isolation.
- `ForeverInitializationGameTest` proves the current full common initializer can start a
  dedicated GameTest server. It is not a test that the seven prototype systems are absent.
- `ForeverModTest` checks the current root package boundary, not candidate artifact metadata.
- No current test proves that the missing message includes the locked SHA-256, that a live
  checksum mismatch is distinct from Matcha parse errors, that a healthy live install adds
  no new noise, or that a one-sided remote client role is observable from the server.
- No current test is an explicit repeated-initialization fixture. Repeated calls must prove
  exactly one registration set, one observation/report per `SERVER_STARTED` or successful
  reload, no report on failed reload, and no stale result across servers. These are future
  implementation tests, not coverage claimed by MRH-012.

Not verified by MRH-012 are a diagnostic-only artifact, a partially altered real archive,
artifact hashes, exact packaged provenance, absence of gameplay registrations from a built
artifact, client-entrypoint removal, full dual-role/client resource activation, the locked
hash and distinct-mismatch report requirements, healthy no-new-noise, lifecycle idempotence,
or any world/save test. The requested Gradle build tests the unchanged baseline source tree;
it cannot make those candidate-artifact claims.

## Owner gates and next ticket

The owner must approve all of the following separately:

1. the conceptual boundary in this document: common/server-only Matcha evidence and baseline
   reporting, with no prototype gameplay or client surface;
2. the explicit rejection of the full root prototype JAR as MRH-011's artifact; and
3. opening **MRH-013**, the blocked implementation/packaging ticket that may select the
   minimal implementation architecture and build the approved diagnostic only.

Approval of this document does **not** approve Java changes, an artifact, pack inclusion,
publication, distribution, or a rights model. PACK-01's final diagnostic criterion remains
open and closes only with MRH-011's assembled-artifact proof; historical lab/profile evidence
is not completion. MRH-011 remains blocked on MRH-013's completed artifact work, PACK-01's
recorded non-diagnostic pinned-input/profile-assembly evidence and human smoke gate before
inclusion, and the open rights/distribution gate.

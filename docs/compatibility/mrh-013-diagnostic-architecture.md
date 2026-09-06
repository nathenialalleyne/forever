# MRH-013: Approved diagnostic implementation architecture

**Status:** Implemented and technically verified as a private dedicated-server candidate.
[Canonical runtime evidence](mrh-013-runtime-evidence.md) records the exact source/artifact,
review, tests, and remaining deployment limits. No pack inclusion or distribution is approved.

**Owner approval record (2026-09-06):** The project owner approved the MRH-012
common/server-only, read-only Matcha baseline diagnostic boundary and explicitly opened
MRH-013 for private implementation and disposable-instance tests. The approval excludes
pack inclusion, publication, distribution, licence or rights changes, gameplay, and the
full root prototype artifact. It also accepts the rejection of `ForeverMod` as a candidate
entrypoint because that entrypoint registers seven prototype systems.

This record is deliberately written before implementation. It selects the minimum build
and entrypoint shape for MRH-013; it does not supersede an ADR or authorise MRH-011.

## Problem and constraints

LAB-02 demonstrated that Global Packs can fail open when the exact Matcha archive is
missing, substituted, or loaded on only one side. The existing root implementation cannot
be shipped for this safety check: `ForeverMod` unconditionally invokes equipment,
mastery, settlement, storage, guide, career, and economy initialisers before Matcha. The
candidate must therefore carry only a bounded, actionable diagnostic and must remain safe
when the archive is absent or damaged.

The pinned baseline is Minecraft `26.2`, Fabric Loader `0.19.3`, Fabric API
`0.158.0+26.2`, Loom `1.17.20`, and Java `25`. The candidate must not add a dependency,
loader, Mixin, persistent identifier, network protocol, client entrypoint, gameplay
registry, world write, download, repair path, or Matcha extraction/repackaging.

## Selected minimum architecture

### Build boundary

Use a **diagnostic source set in the existing root Gradle/Loom project**, at
`src/diagnostic/java` and `src/diagnostic/resources`, plus one explicit
`diagnosticJar` task. The source set compiles against the already-pinned Minecraft and
Fabric API classpaths but is not attached to the existing `forever` mod mapping and is
not included in the normal root `jar`. The task writes a separately reviewable candidate
under `build/` and has no pack or publication task. A separate `diagnosticGameTest` source
set and fixture JAR is used only by disposable dedicated-server evidence; it is not part
of the candidate artifact.

The build generates one small `matcha-diagnostic.properties` resource from the committed
`matcha.lock.json` during diagnostic resource processing. This makes the archive filename,
version, SHA-256, and bounded size identity build inputs rather than hand-maintained
runtime constants. The generated resource is deterministic and contains no absolute path,
timestamp, archive bytes, or Matcha content.

The candidate includes only its own `fabric.mod.json`, diagnostic classes/resources, the
original Forever licence notice, and a diagnostic provenance notice. Its metadata has one
common `main` entrypoint and no `client` entrypoint. The normal root `fabric.mod.json`,
`ForeverMod`, `ForeverClient`, prototype source, tests, resources, audit tool, generated
reports, and pack metadata remain unchanged and outside this artifact.

### Runtime boundary

The entrypoint is a concrete `ModInitializer` under
`dev.forever.compat.matcha.diagnostic`. It registers exactly one `SERVER_STARTED`
callback and one successful `END_DATA_PACK_RELOAD` callback through the public Fabric
lifecycle API, guarded by a process-local registration gate. The callbacks receive the
server and pass it to one diagnostic observer. No result is retained globally: each
observation is scoped to the callback's server, so sequential or concurrent servers
cannot inherit a stale verdict.

The observer performs two bounded, read-only operations:

1. Resolve only `server.getServerDirectory()/datapacks/Matcha_Flavoured_1_12.zip`, after
   validating that the path is the expected relative child and is a regular non-symlink
   file. It requires a `SecureDirectoryStream` from the archive parent, takes one fixed
   maximum-size byte snapshot, hashes those bytes once with JDK `MessageDigest`, and parses
   that same snapshot with `ZipInputStream`. It reads a bounded `pack.mcmeta`, bounds entry
   count and entry-name length, rejects unsafe archive names, and checks known Matcha
   data/resource prefixes without extracting or walking any other filesystem path.
2. Ask the server's `ResourceManager` for the fixed audited Matcha data namespaces. This is
   bounded server-side data evidence. A dedicated server cannot prove that a remote client
   has activated the resource-pack role, so healthy server evidence is not dual-role proof.

### Filesystem provider limitation

The safe archive snapshot requires the filesystem provider to support directory-relative
opening through `SecureDirectoryStream`. If it does not, this private candidate reports
`UNREADABLE` with the limitation and does not reopen the archive through a raceable parent
path. The candidate makes no claim of Windows or macOS compatibility, and this limitation
does not authorise rollout. Linux runtime validation and the unsupported-provider fixture
are recorded in the evidence report; other platform validation remains outside this result.

The report is a value produced from this observation. It has explicit statuses for
healthy, missing, checksum mismatch, one-sided role, unsupported, malformed, and
unreadable outcomes. Missing reports include the exact expected filename and locked
SHA-256. A valid archive containing Matcha data with a non-matching digest reports an
explicit checksum mismatch, distinct from ZIP or JSON parse errors. A structurally valid
decoy with no audited Matcha data remains `MISSING`, preserving the LAB-02 evidence.
Failure reports name a remedy and never throw into server startup. A successful reload
produces one fresh report; a failed reload produces none. Healthy output is INFO-only and
adds no warning or error noise.

The diagnostic does not instantiate `MatchaAdapter`, `VerifiedMatchaAdapter`,
`NoOpMatchaAdapter`, or any existing report path that reaches item/behaviour mappings.
Their package is a valid Matcha isolation boundary, but the current API closure reaches
unrelated mappings and therefore cannot be called diagnostic-only. The new classes keep
all Matcha-specific namespace, archive, and profile facts below `dev.forever.compat.matcha`.

## ADR 0026 escalation and ADR 0033 gap reasoning

The selection is the narrowest response to the measured gap, not a new gameplay owner.

| Escalation step | Finding | Decision |
| --- | --- | --- |
| Existing configuration | Global Packs `log_pack_ids = true` produced the same relevant listing for healthy and decoy archives. It has no integrity or required-pack check. | Insufficient. Keep the existing loader/configuration. |
| Datapack | Matcha data can define gameplay but cannot hash the archive or require its own presence before a server starts. | Insufficient. Do not add a diagnostic datapack. |
| Resource pack | Resource content cannot validate the server's archive identity and cannot inspect a remote client's activated role. | Insufficient. Do not add a client resource diagnostic. |
| Stable scripting layer | No pinned, supported scripting layer is present in the baseline, and a script would still need bounded filesystem/hash and lifecycle access. | No candidate. Do not add a dependency. |
| Public API/event integration | Fabric's public server lifecycle events and Minecraft's public server/resource APIs expose the required startup, successful-reload, and namespace hooks. | Use directly at the boundary. |
| Narrow compatibility adapter | The selected implementation is a narrow read-only Matcha diagnostic under the existing isolation vocabulary. It does not publish gameplay translations. | **Selected.** |
| Version-guarded Mixin | No private Minecraft hook is needed. | Reject. |
| Private fork | Global Packs already loads the archive correctly; changing it would add provenance, licence, and maintenance cost. | Reject. |
| New custom implementation | A new loader or gameplay system would widen ownership. | Reject. The only custom code is the smallest diagnostic needed to close the measured integrity gap. |

Under ADR 0033, the player/operator problem is pack identity safety: a declared Many
Roads Home profile can otherwise run as vanilla or with a substituted foundation without
an actionable signal. The problem is important to pack identity, has an ordinary-play
fallback (the server remains running and vanilla-safe), and is not safely expressible by
Global Packs or content data. Three loader candidates were already evaluated in ADR 0027;
only Global Packs is compatible with 26.2, and none provides archive integrity verification.
The proposed owner is the companion's Matcha compatibility boundary, but only for a
read-only diagnostic. It owns no gameplay, persistent data, network state, or client
projection. There is no schema or migration because the candidate writes no world data;
removal restores the pre-existing silent-loader risk but does not strand saved gameplay
state. Runtime failures are visible, bounded, and fail open.

The maintenance owner is the Many Roads Home companion boundary. Its supported-version
policy follows the pinned Minecraft/loader baseline and the exact Matcha lock. A Matcha
pin change requires lock regeneration, candidate rebuild, archive and disposable-server
validation, and a fresh provenance review. No third-party JAR is modified. Matcha's
archive, licence, audit output, and private identifiers remain separate third-party
material under ADRs 0002, 0020, and 0027.

## Exact candidate closure

The candidate is intended to contain only these original diagnostic classes:

- `dev.forever.compat.matcha.diagnostic.MatchaDiagnosticArchive`
- `dev.forever.compat.matcha.diagnostic.MatchaDiagnosticEntrypoint`
- `dev.forever.compat.matcha.diagnostic.MatchaDiagnosticLifecycle`
- `dev.forever.compat.matcha.diagnostic.MatchaDiagnosticObserver`
- `dev.forever.compat.matcha.diagnostic.MatchaDiagnosticProfile`
- `dev.forever.compat.matcha.diagnostic.MatchaDiagnosticRegistrationGate`
- `dev.forever.compat.matcha.diagnostic.MatchaDiagnosticReport`
- `dev.forever.compat.matcha.diagnostic.MatchaDiagnosticStatus`

The separate GameTest fixture contains only
`dev.forever.compat.matcha.diagnostic.MatchaDiagnosticArtifactGameTest` and is never
copied into the candidate.

The final JAR scan must prove there is one common entrypoint, no client metadata, no
`dev.forever.ForeverMod`, `dev.forever.client`, `dev.forever.core`, gameplay registry or
attachment references, `SavedData`, network registration, existing Matcha mapping class,
or unrelated Matcha-derived evidence. Runtime class references are limited to JDK,
Minecraft server/resource types, Fabric lifecycle/initializer APIs, Gson already
supplied by the Minecraft runtime, and SLF4J already supplied by Fabric. No dependency is
shaded into the candidate.

## Required evidence before MRH-013 can be called complete

- Unit tests prove the generated profile matches `matcha.lock.json`; direct path
  validation, maximum archive size/entry count, unsafe paths, missing, unreadable,
  malformed, unsupported, decoy/MISSING, exact healthy, and partially altered real
  archive/checksum-mismatch cases.
- Report tests prove the locked hash and filename are in the ERROR/MISSING remedy,
  checksum mismatch is not a parse error, remedies name the lock/reinstall action, and
  healthy reports have no warning/error severity.
- A lifecycle fixture proves repeated initialization registers one callback set, one
  observation/report per startup or successful reload, no report for failed reload, and
  no stale result across multiple servers.
- A candidate JAR closure/metadata/notices test proves the root prototype and client are
  absent and the candidate has exactly one diagnostic main entrypoint.
- A dedicated server started from an isolated candidate artifact, not the full root
  prototype JAR, reaches ready state for healthy, missing, decoy, altered-real,
  unreadable, malformed, and unsupported fixtures. Logs show startup remains unblocked,
  healthy adds no warning/error, and failure diagnostics are actionable.
- A candidate-specific GameTest or equivalent dedicated-server fixture proves the live
  server callback path and no client-class loading. A separate assembled-pack/client test
  is required for the resource role; server evidence cannot establish a remote client's
  resource role.
- Before/after comparisons of fresh marked disposable instances and archive copies show
  no world writes, persistence, downloads, repairs, or network behaviour. All owned
  servers stop cleanly and logs remain outside the repository.

The evidence above is now recorded in the linked canonical report. This record does not
authorise pack inclusion. MRH-011 remains blocked on its own assembled-artifact proof,
the PACK-01 human smoke gate, and the unresolved rights and distribution decision.
The candidate has `environment: server`, so it does not execute in client or integrated
single-player environments. Its presence in a client instance is not diagnostic coverage.

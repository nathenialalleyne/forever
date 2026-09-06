# Session report: MRH-012 documentation review corrections

Written 2026-09-06 on `modpack-first`, from the MRH-012 slice base
`34ee50a3a8218337f8abce327d41efcd958739b7`.

## Scope and accepted finding disposition

This correction is documentation-only. It changes only the MRH-012 design boundary, the
MRH-010/MRH-012/MRH-013/MRH-011/PACK-01 backlog records, this dated report, and the source
inventory. No Java, build script, pack metadata, licence, dependency, world, network, or
publication path was changed.

- **P1, MRH-010 source-vs-spec gap:** MRH-010 is no longer labelled `Done`. Its acceptance
  contract remains intact, including the missing message's expected locked SHA-256, a
  distinct checksum-mismatch diagnostic, one-sided-role reporting where detectable, an
  unblocked/no-world-write failure path, and no new noise for a healthy install. The source
  gap is explicit: `MatchaServerEvidence.gather()` currently supplies `null` digest and
  metadata plus an empty fingerprint map; the missing `MatchaBaselineReport` remedy omits
  the locked hash; and a live healthy result is not demonstrated. MRH-013 now carries every
  MRH-010 acceptance and test requirement and must resolve them before MRH-011 shipping.
  MRH-012 explicitly says that merely rewiring the existing report is insufficient.
- **P1, dependency order:** MRH-013 depends on PACK-01's recorded pinned-input and
  profile-assembly evidence only, not PACK-01 completion or its final diagnostic criterion.
  PACK-01 criterion 5 remains open and closes only through MRH-011's assembled-artifact
  proof. MRH-011 names the non-diagnostic PACK-01 prerequisites and the human real-play
  smoke gate before inclusion. MRH-013 internal implementation does not wait for that human
  smoke. No PACK-01 acceptance criterion was removed.
- **P2, server/client observation:** MRH-012 and MRH-013 now state that server namespaces
  and scoreboard signals cannot verify a remote client's resource-pack role. Healthy server
  evidence is not full dual-role validation. The MRH-010 where-detectable role requirement
  remains, with separate pack/client verification required and no client feature approved
  by server evidence.
- **P2, lifecycle:** The future validation contract now requires an explicit repeated-
  initialization fixture with exactly one registration set, one observation/report per
  `SERVER_STARTED` or successful reload, no report on failed reload, and no stale result
  across servers. Existing source and tests do not provide this coverage, and the report
  does not claim that they do.
- **P2, inventory:** The original 2026-08-28 filesystem snapshot, including its historical
  294-Java and one-audit-test quantities, remains labelled historical. A bounded
  2026-09-06 reconciliation against `git ls-files '*.java'` records 297 tracked Java files:
  263 under `src` and 34 under `tools/matcha-audit`, including two independently confirmed
  tracked audit-test classes. The current detailed test paths and quantities are consistent
  with that reconciliation; no broad inventory rewrite was attempted.

These corrections record evidence limits and ordering. They do not supersede an ADR, select
an implementation architecture, approve the MRH-012 boundary, or authorise artifact or pack
inclusion.

## Evidence limits and remaining owner gates

- No diagnostic-only artifact was built, hashed, packaged, or added to `pack/` by this
  correction.
- The existing root-build experiment remains behaviour evidence only. It does not prove the
  locked-hash message, distinct checksum-mismatch reporting, one-sided client-role coverage,
  healthy no-new-noise, lifecycle idempotence, or diagnostic-only artifact isolation.
- The missing and structurally valid decoy cases remain historical `MISSING` evidence. A
  partially altered real archive producing `DEGRADED`/WARN remains open.
- A server-only run cannot verify remote client resources. Separate assembled-pack/client
  verification remains required before any full dual-role claim.
- The owner must approve the proposed MRH-012 boundary and open MRH-013. MRH-013 must resolve
  the carried MRH-010 contract before MRH-011 can ship. PACK-01's human smoke is required
  before MRH-011 pack inclusion, not before internal MRH-013 implementation. The final
  PACK-01 diagnostic criterion remains open until MRH-011 supplies assembled-artifact proof.
  The rights/distribution decision remains open.

## Checks

The correction worker ran the prose checks below. After the fresh review and inventory
fix, the parent also ran `git diff --check`, `python3 scripts/check-repo-map.py`,
`./scripts/validate-pack.sh`, and
`JAVA_HOME=$HOME/toolchains/jdk-25.0.4.1+1 ./gradlew build --no-daemon`: all passed.
Gradle reported 17 tasks up-to-date, not a fresh test execution. Pack validation skipped
Packwiz index refresh because Packwiz was not on PATH; the independent index consistency
check passed. The final log is `/tmp/mrh-012-final-validation-b35a72973/b35a72973.output`.
The parent additionally verified unchanged MRH-010/PACK-01 acceptance text, exact coverage
of all 28 Matcha class paths, relative links, and the four-document diff scope. Session
lens diagnostics reported no issues.

- `git diff --check` — passed.
- `python3 scripts/check-repo-map.py` — passed (`27 tracked top-level paths documented`).
- Bounded relative Markdown-link check over the changed documents — passed.
- Bounded backlog ticket-heading and cross-reference ID check — passed (`40` unique
  headings; required MRH-010/MRH-011/MRH-012/MRH-013/PACK-01 IDs present).
- `git ls-files '*.java'` reconciliation — passed: 297 tracked Java files, including 2
  tracked audit-test classes.
- Inventory path reconciliation — passed: all 33 tracked `src/test/java` paths and both
  tracked audit-test paths match the detailed inventory lists.
- Total-diff inspection from `34ee50a` through `HEAD`, plus the working-tree diff including
  the pre-existing inventory table-separator change — completed; no non-documentation path
  was included in this correction.

Historical evidence retained from the prior reviewed run, not rerun here:

- `./scripts/validate-pack.sh` — passed all pack metadata, exact-pin, provenance, and
  binary checks.
- `JAVA_HOME=$HOME/toolchains/jdk-25.0.4.1+1 ./gradlew clean build --no-daemon` — passed;
  the build emitted six existing GameTest deprecation warnings and ordinary deprecated-API
  notes. This validates the unchanged baseline source tree, not a diagnostic-only artifact.

No world access, Matcha acquisition/install, pack export, publication, or distribution was
run.

## Worktree and handoff

The earlier pre-review commits `0bc7a71` and `d0e8ae8` were committed and pushed before
review, contrary to the parent instruction. They are historical and were not rewritten.
The fresh follow-up review found no P0/P1 blockers and judged the design evidence ready
for owner review, not ticket completion. The parent added the review's missing
`MatchaBaselineReport.java` path to the 28-class inventory. The parent owns the final
validation and correction commit; its hash and push outcome will be recorded in the
completion response after those operations occur.

Remaining owner approval is the MRH-012 boundary and MRH-013 opening, followed by MRH-013's
artifact evidence, PACK-01's human smoke before inclusion, MRH-011's assembled-artifact proof,
and the rights/distribution decision. No true ADR conflict was introduced; the source/spec
conflict is recorded as an open evidence gap rather than silently resolved.

# Session report: MRH-012 diagnostic-only artifact preparation

Written 2026-09-05 for the documentation-only preparation slice on `modpack-first`.

## Scope completed

- Added `MRH-012`, a prerequisite to define and obtain approval for a diagnostic-only artifact boundary without selecting a new build architecture or authorising gameplay.
- Updated `MRH-011` to depend on `MRH-012`, reject the full root prototype JAR as its artifact, and preserve the earlier missing/decoy end-to-end evidence as behaviour evidence rather than artifact-scope approval.
- Recorded that the partially altered real-archive `DEGRADED`/`WARN` validation remains outstanding.
- Left `PACK-01` unchanged, including its human real-play smoke test. The rights model and all distribution gates remain open; private-only preparation does not authorise inclusion or distribution.

The source and prototype remain preserved under ADR 0034. No Java, build, pack, script, licence, dependency, artifact, or world-save path was changed.

## Validation

- `git diff --check` passed.
- `python3 scripts/check-repo-map.py` passed: 27 tracked top-level paths documented.
- `JAVA_HOME=$HOME/toolchains/jdk-25.0.4.1+1 ./gradlew build` passed (`BUILD SUCCESSFUL`; output captured outside the repository at `/tmp/mrh-012-gradle-build.log`).
- No Matcha acquisition/install tooling, world access, pack export, or distribution command was run.

## Handoff

Independent review found no P0/P1 blockers. The parent clarified the reviewed backlog sentence distinguishing decoy/MISSING evidence from present-but-unverified/DEGRADED evidence. The pre-existing contradictory summary in `docs/decisions/OPEN-rights-model.md` remains deferred; the backlog correctly retains the rights gate.

This checkpoint defines MRH-012; it does not complete its design inventory or owner approval. Next ticket: MRH-012, to document and review the conceptual diagnostic-only boundary. Any implementation requires a separate reviewed ticket. MRH-011 packaging remains blocked on boundary approval, PACK-01's human smoke test, and rights/distribution decisions; the WARN case must be validated against the future approved artifact before packaging acceptance.

The documentation checkpoint starts from `8744c1868f89bbbf6444bc91ef12e70b13522585`. The parent will record the resulting commit and push outcome in the completion response; this document does not claim a push before it occurs.

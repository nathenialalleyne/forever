# Session report: MRH-012 diagnostic-only boundary design

Written 2026-09-06 on `modpack-first`, from clean base `34ee50a3a8218337f8abce327d41efcd958739b7`.

## Scope completed

- Added [`docs/compatibility/mrh-012-diagnostic-artifact-boundary.md`](compatibility/mrh-012-diagnostic-artifact-boundary.md), a source-backed proposed boundary and validation matrix.
- Traced the current path from `fabric.mod.json` through `ForeverMod.java:41-63`, all seven unconditional prototype initialisers, `ForeverMatchaCompat`, server lifecycle hooks, bounded evidence gathering, `MatchaVersionDetector`, `MatchaBaselineReport`, and the Packwiz/Global Packs provenance/config inputs.
- Recorded the source-level closure: 183 common Java classes, 28 Matcha package classes, and 143 common classes reachable from the current root entrypoint. The document distinguishes this baseline reachability from the proposed diagnostic-only behaviour and rejects the full root prototype JAR as an MRH-011 artifact.
- Inventoried the seven initialisers, prototype registries/persistent identifiers, client entrypoint/assets, diagnostic classes, Matcha mapping classes, tests, generated audit reports, and licence/provenance boundaries.
- Updated the relevant source inventory counts from the inspected tree: 304 tracked `src` paths, 183 common classes, 33 unit-test classes, and 28 Matcha classes. No source or generated third-party material was changed.
- Marked MRH-012 **Design ready; owner approval required**, linked its evidence document, and kept it explicitly not `Done`.
- Added MRH-013 as the single separately scoped, blocked implementation/packaging ticket. It may select the minimal architecture and implement it only after MRH-012 approval. MRH-011 now depends on MRH-013 as well as the owner-approved boundary, so conceptual approval cannot imply a built artifact.

## Evidence limits and remaining owner gates

- No diagnostic-only artifact was built, hashed, packaged, or added to `pack/`.
- The requested `./gradlew build` builds the unchanged current baseline, not the proposed artifact, so it cannot prove gameplay absence from an artifact.
- Existing Matcha unit/GameTest evidence was preserved as behaviour evidence. The missing and decoy cases remain `MISSING`; a partially altered real archive producing `DEGRADED`/WARN remains open.
- The owner must approve the proposed common/server-only diagnostic boundary and separately open MRH-013. That approval does not choose a build architecture, approve gameplay, grant distribution rights, or resolve `OPEN-rights-model.md`.
- PACK-01's human real-play smoke test and the rights/distribution gate remain open.

## Checks

- `git diff --check` — passed.
- `python3 scripts/check-repo-map.py` — passed (`27 tracked top-level paths all documented`).
- Relative Markdown-link check over the changed documents — passed.
- Backlog heading-identity check — passed (`28` unique ticket headings).
- Source closure check — passed: `183` common Java files, `28` Matcha classes, and `143` lexical classes reachable from `ForeverMod` (`1 + 28 + 112 + 2`). No duplicate Java basenames were found.
- `./scripts/validate-pack.sh` — passed all pack metadata, exact-pin, provenance, and binary checks.
- `JAVA_HOME=$HOME/toolchains/jdk-25.0.4.1+1 ./gradlew clean build --no-daemon` — passed. The build emitted six existing GameTest deprecation warnings and ordinary deprecated-API notes; all tests and checks passed.

No world access, Matcha acquisition/install, pack export, publication, or distribution was run. The documentation change was committed as `0bc7a71` and pushed to `origin/modpack-first`; the follow-up report correction is the next commit in that range. The build validates the unchanged current baseline and not a diagnostic-only candidate artifact.

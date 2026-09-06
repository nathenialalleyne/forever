# Session report: MRH-013 private diagnostic candidate

## Delivered scope

The owner approved MRH-012's diagnostic-only boundary and explicitly authorised MRH-013
private implementation and disposable-instance tests. Independent code and final evidence
reviews now accept the private dedicated-server candidate. MRH-011 pack inclusion and
PACK-01 completion remain separate; no licence or public-distribution decision was made.

The implementation uses isolated diagnostic and test-fixture source sets in the existing
pinned build. It reads one bounded archive snapshot, verifies the locked identity, reports
missing/altered/one-sided/malformed/unreadable inputs without blocking startup, and owns no
gameplay or save state. The full prototype source and default artifact remain intact.

## Files and why they changed

- `build.gradle`: isolated source sets, deterministic lock-derived profile, candidate and
  separate GameTest JAR tasks, and test classpaths. No dependency versions changed.
- `src/diagnostic/`: eight small-scope diagnostic classes, one server initializer, generated
  profile input, metadata, and original licence/provenance notices. No prototype mappings,
  gameplay registrations, client entrypoint, or persistence is packaged.
- `src/diagnosticGameTest/`: separately packaged runtime fixtures, not shipped in the
  candidate. They cover observation and registered lifecycle callback behaviour.
- `src/test/java/dev/forever/compat/matcha/diagnostic/`: synthetic-archive, boundedness,
  status/remedy, lifecycle, portability, and deterministic-fixture regression tests.
  Mandatory CI does not require or acquire the ignored official Matcha archive.
- `docs/backlog.md`: recorded owner approval, MRH-013 completion, the separate candidate's
  MRH-010 evidence, and the still-open MRH-011/PACK-01 gates.
- `docs/compatibility/mrh-012-diagnostic-artifact-boundary.md` and
  `docs/compatibility/mrh-013-diagnostic-architecture.md`: owner approval, selected
  implementation architecture before code, closure, and deployment limits.
- `docs/compatibility/mrh-013-runtime-evidence.md`: canonical exact-artifact acceptance
  results and local raw-evidence locations. This report is the short handoff.

No `pack/`, original gameplay source, production art, third-party binary, dependency pin,
root licence, or repository visibility was changed.

## Evidence and corrections

[Canonical evidence](compatibility/mrh-013-runtime-evidence.md) identifies source
`729680b77acf328948c9c48da9f357b627fbf5fc` and the 41023-byte candidate SHA-256
`27fc15762d02583973ce7040ebd8dc6699cc528198c321f9cf7a7e7cb10569a4`.
The separate 4191-byte fixture hash is
`03cee2b0b51239e9a9a92ee3e317e23631fa0482e5343b2f0e79953c9e4e61e2`.

- All nine current-artifact dedicated-server scenarios pass, with ready state, passing
  GameTests, actionable expected statuses, exit 0, clean stop, and unchanged installed hashes.
- Real client ResourceManager lookup proves the exact Matcha source pack and bytes for
  `minecraft:items/amber.json`; the client shuts down normally after its assertion.
- Paired no-diagnostic/candidate/removal runs distinguish vanilla save churn. A stopped-world
  direct diagnostic probe leaves all 54 world manifest entries and archive bytes unchanged.
- Closure and reproducible-build checks exclude gameplay/client/fixture contamination.
- Final independent review: no issues found; private MRH-013 evidence closure accepted.

Two worker attempts timed out at the default 30-minute limit; partial changes were captured
under `/tmp/mrh013-timeout-recovery-*` and recovered through the same subagent protocol.
An early launch used PATH Java 21 and failed before Minecraft startup; all authoritative
runs used the absolute pinned JDK 25 executable. Reviews caught and corrected the ignored
binary test dependency, reverse-role classification, unsafe filesystem fallback, fixture
timestamps, and overbroad evidence wording. Fresh evidence replaced mixed copied hashes;
old raw evidence remains explicitly superseded rather than rewritten as successful proof.

## Validation and known limits

Workers ran clean root builds/tests, diagnostic/fixture JAR builds and closure checks,
Checkstyle, pack validation, repository-map checks, and the disposable runtime controls.
The canonical report distinguishes source tests, candidate GameTests, and client proof.
The parent final checkpoint passed on JDK 25:

- `./gradlew clean build diagnosticJar diagnosticGameTestJar --no-daemon`: 27 tasks
  executed, 184 JUnit tests across 35 suites, zero failures/errors/skips. Both rebuilt
  JAR hashes match the exact runtime-tested bytes above.
- `./scripts/validate-pack.sh`, `python3 scripts/check-repo-map.py`, and
  `git diff --check`: passed. Packwiz was not on PATH, so its index refresh was skipped;
  independent pinned-index consistency passed.
- Parent JAR hash/19-class package/one-entrypoint/server-metadata assertions passed.
  Diff checks confirm `src/main`, `src/client`, `pack`, original licences, dependency
  pins, generated reports, and art are unchanged. No owned test Java process remains.
- Session lens diagnostics: no issues across 15 diagnosed files.

Final build log: `/tmp/mrh013-final-checkpoint-b94141253/b94141253.output`.
Existing deprecation/Unsafe warnings remain; they did not fail the build.

The parent attempted primary LSP diagnostics on eight diagnostic Java files: five were
clean and three timed out. This is partial LSP coverage, not a zero-error full LSP claim;
the actual Gradle compile/test checks are the authoritative build evidence.

Not run: public export/release, diagnostic Packwiz inclusion, human real-play acceptance,
visual/audio judgement, real survival worlds, or wider-platform rollout. The client proof
has substantial pre-existing environment/content log noise and proves only its explicit
resource assertion. Server metadata excludes integrated single-player execution; server
namespaces do not prove loaded-pack provenance or remote-client activation. Unsupported
filesystem providers return `UNREADABLE`; Windows/macOS support is not claimed.

## Next ticket and delivery

Next: **MRH-011**, blocked on authorised artifact transport/provenance and rights decisions,
PACK-01's human disposable-world real-play smoke test, and its own assembled-pack fault
proof. No further gameplay or pack-inclusion work was started. A diagnostic-only JAR is
not a replacement for a prototype gameplay mod in a world that has used that prototype.

Work starts from `f5476372dd79653c0d98e9e8f0f8e83363f35b2b`; green source checkpoints are
`513d75c`, `16ad200`, `564e646`, `8414525`, and `729680b`. The parent will record the final
report commit and private-origin push outcome in the completion response after they occur.

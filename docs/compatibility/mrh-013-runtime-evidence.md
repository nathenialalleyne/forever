# MRH-013: Private diagnostic candidate evidence

**Result:** Technical acceptance verified on Linux/JDK 25, 2026-09-06. Independent final
review found no issues. This is not pack inclusion, a public release, distribution
permission, or completion of MRH-011/PACK-01. The owner approved private implementation
and disposable-instance tests after reviewing MRH-012.

## Exact candidate

Source: `729680b77acf328948c9c48da9f357b627fbf5fc`, following the architecture checkpoint
`513d75c` and implementation/review corrections `16ad200`, `564e646`, `8414525`.

| Artifact | SHA-256 | Bytes |
| --- | --- | ---: |
| `build/diagnostic-libs/forever-matcha-diagnostic-0.0.0-dev.jar` | `27fc15762d02583973ce7040ebd8dc6699cc528198c321f9cf7a7e7cb10569a4` | 41023 |
| `build/diagnostic-libs/forever-matcha-diagnostic-gametest-0.0.0-dev.jar` | `03cee2b0b51239e9a9a92ee3e317e23631fa0482e5343b2f0e79953c9e4e61e2` | 4191 |

The candidate contains eight top-level diagnostic classes, 19 class files, and 30 ZIP
entries. Metadata has one common initializer, no client entrypoint, and mod ID
`forever-matcha-diagnostic`. The fixture is a separate artifact. Closure inspection
excludes the root prototype, gameplay registries, persistence/network APIs, client
classes, unrelated Matcha mappings, and third-party binaries. The generated profile
matches `matcha.lock.json`; original proprietary licence and provenance notices are
included. Neither JAR is tracked or added to `pack/`.

Build commands use the pinned toolchain:

```sh
export JAVA_HOME="$HOME/toolchains/jdk-25.0.4.1+1"
./gradlew clean build diagnosticJar diagnosticGameTestJar --no-daemon
./scripts/validate-pack.sh
python3 scripts/check-repo-map.py
git diff --check
```

The mandatory tests use synthetic archives, not `vendor/matcha`. The earlier clean-CI
check with that ignored directory absent passed 182 tests with no failures, errors, or
skips (`/tmp/mrh013-final-clean-build.log`). Subsequent regression tests cover marker-free
wording, deterministic ZIP timestamps, and unsupported filesystem providers. The final
parent clean build executed 27 tasks and passed 184 tests with zero failures/errors/skips;
its log is `/tmp/mrh013-final-checkpoint-b94141253/b94141253.output`. Both final JAR
rebuilds reproduced the hashes above. Root unit/build checks are not substituted for the
candidate-only runtime evidence below.

## Canonical dedicated-server matrix

All nine cases passed with exit 0, one ready marker, passing GameTests, clean shutdown,
and unchanged installed candidate/fixture hashes. Failure cases remained usable servers.
Each used a fresh marked disposable instance, the absolute JDK 25 executable, pinned
Fabric dependencies, and the diagnostic plus separate fixture, never the root prototype.

| Case | Observed result |
| --- | --- |
| Healthy original archive | `HEALTHY`, INFO only; 2346 recipes and 1805 advancements |
| Missing archive | Framed `MISSING` ERROR with filename, locked SHA-256, and remedy |
| Altered real archive | Distinct `CHECKSUM_MISMATCH` WARN, not a parse error |
| Structurally valid decoy | `MISSING`, not a trusted baseline |
| Resource-only archive | `ONE_SIDED_ROLE` |
| Data-only archive | `ONE_SIDED_ROLE` |
| Unreadable input | `UNREADABLE` |
| Malformed input | `MALFORMED` |
| Unsupported input | `UNSUPPORTED` |

Canonical evidence is `/tmp/mrh013-lineage-20260906/canonical-evidence-manifest.tsv`.
Every directory in `cases/` contains `run-command.txt`, `run-pre.txt`, `run-post.txt`,
`observations.txt`, `logs/server.log`, and `logs/gametest.xml`. These capture installed
JAR and archive hashes immediately before and after each run. The healthy server fixture
uses identical original copies in root and world `datapacks/`; this is candidate-only
server validation, not a new Packwiz export or full Global Packs fault matrix.

The original Matcha archive remains 12248989 bytes, SHA-256
`6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248`.
Alterations were made only to disposable test copies. Earlier evidence under
`/tmp/mrh013-runtime-evidence-20260906/` copied stale hash records from seed instances.
Those records are preserved as superseded, not corrected in place or counted as current
proof. The canonical runs above contain fresh observations, not inherited evidence files.

## Lifecycle, read-only behaviour, and removal

Unit and separate candidate GameTest fixtures exercise the registrar/callback path,
repeated initialization/start, successful and failed/pre-start reload, fresh observation,
and independent server identities. They do not rely solely on direct observer calls.

Canonical controls are under `/tmp/mrh013-readonly-proof-20260906/`:

- `paired-candidate-only/`: identical stopped seed worlds run without the diagnostic,
  with only the diagnostic added, then after its removal. All starts reach `Done` and
  stop cleanly. Removing the candidate restores baseline loading without missing
  registry/class errors; 2346 recipes and 1805 advancements remain.
- Both paired runs add only `level.dat_old` and change the same seven vanilla paths.
  This establishes equivalent path-level save churn, not byte-identical live worlds.
  Minecraft itself still saves world state.
- `direct-probe/`: the exact candidate inspection path operates on a separate stopped
  disposable world. All 54 manifest entries and archive bytes remain unchanged. Combined
  with the inspected API/class closure, this supports diagnostic-only read behaviour,
  not a claim that a running Minecraft process makes no writes or network calls.
- `namespace-removal-v2/`: supplemental source-retained fixture checks namespace
  preservation through removal. It is outside the product candidate.

`reproduction-commands.txt`, `run_clean_server.py`, scoped manifests, probe sources,
`world-comparison.txt`, and `lifecycle-removal-assertions.txt` retain the controls.
The earlier exploratory pair containing the GameTest fixture is explicitly noncanonical.
All owned server processes were stopped. No real world or prototype gameplay world was
opened, migrated, or used for removal tests.

## Exact client resource proof

`/tmp/mrh013-client-proof-20260906/` holds a fresh private assembled client instance,
verified pinned mod hashes, commands, and a temporary original assertion fixture. The
actual client ResourceManager resolves `minecraft:items/amber.json` from a single source
pack `Matcha_Flavoured_1_12.zip`. Loaded bytes have SHA-256
`cb4f7f1873a99f366c89efe2452fc79df10c62d4393a0d237c260436cbf3418f`, matching the original
archive's 81-byte `assets/minecraft/items/amber.json`. The full installed archive has the
locked hash before and after the run. Both Global Packs roles are configured.

The assertion reports `MRH013_CLIENT_RESOURCE_ASSERTION PASS`; the client exits 0 after
requesting shutdown, with `MRH013_CLIENT_STOPPING` logged. Its timeout was a safety limit,
not the success signal. The candidate JAR is physically installed and hashed but correctly
excluded by Fabric on the client because its environment is `server`. No world was opened.
The temporary client probe is outside the repository and outside the candidate.

## Limits and next ticket

- The candidate requires `SecureDirectoryStream`; unsupported providers report
  `UNREADABLE` instead of unsafe fallback reads. No Windows/macOS support is claimed.
- Its `environment: server` metadata excludes client execution, including integrated
  single-player servers. This candidate protects dedicated servers only. MRH-011 must
  not promise single-player diagnostic coverage or silently change that boundary.
- Matching archive bytes and visible namespaces do not prove loaded-pack origin,
  precedence, or remote-client activation. The independent client proof covers one
  concrete resource source and bytes, not remote-client attestation.
- The client log has 823 environment/content warning/error lines. The focused resource
  assertion passes; generic warning-free client operation, visual quality, audio, and
  human real play are not claimed.
- `/tmp` evidence and probe sources are local and may expire. Preserve these directories
  before cleanup if later review needs raw logs; this document records the durable
  conclusions, exact identities, and evidence locations, not third-party log contents.

Next ticket: **MRH-011**, still blocked on PACK-01's human real-play smoke, authorised
artifact transport/provenance and rights/distribution decisions, and its own assembled-pack
missing/altered-input proof. The original prototype and its unresolved old diagnostic
path remain preserved; MRH-013 resolves the carried contract in the separate candidate,
not by upgrading or replacing an existing prototype world.

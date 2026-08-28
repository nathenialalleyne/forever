# Matcha audit workflow

This directory records how Forever inspects Matcha Flavoured before making a
compatibility decision. The audit is evidence gathering, not gameplay
implementation. It exists so that a later system owner can preserve useful
behaviour without copying unverified assumptions into Forever.

This workflow is the evidence trail for **FVR-014**, the first official Matcha audit,
and **FVR-015**, manual system classification. Keep the pinned input, generated
reports, and reviewed classification together. Do not close either ticket with a
prose summary that cannot be reproduced from the locked archive.

## What is being audited

Matcha is a datapack and resource-pack archive, not a Fabric mod. The current
pinned baseline is:

| Field | Value |
|---|---|
| Project | Matcha Flavoured |
| Modrinth project ID | `QI0EmgZ1` |
| Selected version | `1.12` |
| Version ID | `E9rngRfK` |
| Archive | `Matcha_Flavoured_1_12.zip` |
| Minecraft | `26.2` |
| Release type | `release` |
| License | `CC-BY-NC-SA-4.0` |
| SHA-256 | `6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248` |
| Locked source | `https://cdn.modrinth.com/data/QI0EmgZ1/versions/E9rngRfK/Matcha_Flavoured_1_12.zip` |

The archive, its SHA-256, and the source metadata are authoritative. The audit must
not use an unofficial wiki as a substitute for the archive. A wiki can suggest a
question, but it is not evidence for a classification.

## 1. Fetch and pin the archive

Use the repository acquisition script, not a manually downloaded replacement.
From the repository root on a Unix-like system:

```sh
bash scripts/fetch-matcha.sh
```

On Windows PowerShell:

```powershell
pwsh -File .\scripts\fetch-matcha.ps1
```

The scripts acquire the pinned release into `vendor/matcha/` and record or verify
its exact identity in `matcha.lock.json`. The lock must include the project ID,
version ID, archive name, source URL, Minecraft version, and SHA-256. A script must
refuse to silently select a newer release.

Before auditing, verify the lock and archive independently:

```sh
cat matcha.lock.json
sha256sum vendor/matcha/Matcha_Flavoured_1_12.zip
```

PowerShell equivalent:

```powershell
Get-Content matcha.lock.json
Get-FileHash vendor/matcha/Matcha_Flavoured_1_12.zip -Algorithm SHA256
```

The computed digest must equal the locked digest. If it does not, stop. Do not
rename the archive, edit the lock to make it pass, or run a partial audit against an
unknown input. A changed archive is a new acquisition and requires its own review.

The fetch and verification step also protects third-party material. Keep Matcha's
archive and derived audit evidence separate from Forever's original source and
content, and retain the licence notice required by the repository.

## 2. Run the standalone audit CLI

The CLI is a developer tool under `tools/matcha-audit`. It is intentionally separate
from the mod build and must never be bundled into the Forever JAR. Run its help
first so that the checked-in CLI option spelling is visible. The CLI does not fetch
Matcha or read the lock file itself. The acquisition step above verifies the lock,
then the CLI audits the verified ZIP or an already-unpacked directory:

```sh
cd tools/matcha-audit
./gradlew test
./gradlew installDist
build/install/matcha-audit/bin/matcha-audit --help
```

Run the installed CLI with the locked archive and a version-derived output
directory. The current pinned invocation is:

```sh
build/install/matcha-audit/bin/matcha-audit \
  --input ../../vendor/matcha/Matcha_Flavoured_1_12.zip \
  --output ../../generated/matcha/1.12 \
  --version-label 1.12
```

On Windows, use `gradlew.bat`, then
`build\\install\\matcha-audit\\bin\\matcha-audit.bat` with the same
`--input`, `--output`, and `--version-label` options. If a future CLI revision
changes an option, use the names shown by `--help`. Do not omit the locked archive,
do not substitute an arbitrary output directory, and do not treat a CLI success as
proof of pinning unless the acquisition lock and the CLI input digest agree.

The CLI accepts a ZIP or a directory, validates ZIP safety, keeps ZIP contents in
memory, and writes only inside the selected output directory. It has no network
access. A successful run produces deterministic reports for identical input bytes
and options, except for the generated timestamp isolated in `metadata.json`.

The audit should inspect, at minimum:

- the archive manifest and `pack.mcmeta`;
- all data and resource paths, with stable file hashes;
- functions and command references, including scheduled and called functions;
- scoreboards, tags, predicates, advancements, recipes, loot, and structures;
- disguised item signatures, including vanilla base item and custom model data;
- cross-file references and reachable entry points; and
- runtime marker observations or bounded disposable-world traces where the source
  alone cannot establish behaviour.

The CLI is not a replacement for manual review. Static output tells a reviewer what
exists and how it is connected. A runtime trace tells a reviewer what a player or
server actually observes in a controlled test. Both are needed for high-risk
behaviour.

## 3. Where reports land

Reports belong under:

```text
generated/matcha/<locked-version>/
```

For the current pin, the expected directory is:

```text
generated/matcha/1.12/
```

The generated directory is keyed by the version recorded in the lock. If a future
lock selects a different version, it gets a new sibling directory. Never overwrite
`1.12` with an unpinned archive and never blend reports from two versions.

There is no hidden intent classifier in the CLI. Use `summary.md` as the human
entry point, then read the exact generated files in this order:

1. **`metadata.json` and `input-sha256.txt`**: tool and schema identity, input
   identity, archive or canonical directory digest, attribution, parsed
   `pack.mcmeta`, and the generated timestamp. Confirm the digest against the
   locked archive before continuing.
2. **`file-inventory.csv`**: every observed logical pack path, side, namespace,
   classification, size, SHA-256, and JSON parse status. This is the source of
   truth for actual paths.
3. **`namespaces.json`, `pack-filters.json`, and `cross-references.json`**:
   observed namespaces, pack filters, reference edges, locations, and resolution
   status.
4. **`functions.json`, `scoreboards.json`, and `tags.json`**: line-level command
   observations, objective definitions and references, tag identities, replacement
   flags, and membership values. Follow calls and schedules from their entry points.
5. **`recipes.json`, `advancements.json`, `loot-tables.json`, `predicates.json`,
   `item-modifiers.json`, and `registry-data.json`**: observed content and JSON
   references for recipes, tutorialization, loot, predicates, item operations,
   trades, dimensions, and other recognised data.
6. **`worldgen.json` and `assets.json`**: structures, dimensions, world-generation
   data, models, textures, languages, sounds, atlases, item definitions, and other
   resource-pack observations.
7. **`vanilla-overrides.csv`, `unresolved-references.csv`, and
   `unknown-paths.csv`**: direct vanilla replacements, unresolved internal-looking
   references, and paths that need a manual classification.
8. **`summary.md`**: counts, warnings, attribution, and review notes. It makes no
   gameplay-intent claims and cannot replace the source files or a runtime test.

Treat generated output as evidence. Do not hand-edit it to make a classification
look complete. If the CLI output is wrong or incomplete, fix the tool under its own
ticket and regenerate the directory from the same input bytes. The generated
timestamp in `metadata.json` may change on regeneration. All other reports should
remain byte-stable for the same input and options.

## 4. How to read a report

Read reports in this order:

1. Confirm that the input manifest's SHA-256 equals `matcha.lock.json`.
2. Read the source inventory and choose a system entry point. An entry point may be
   a load or tick function, an advancement, a recipe, a tag, a marker, or a runtime
   interaction.
3. Follow every reference from that entry point. Record called functions,
   scoreboard reads and writes, tags, predicates, item signatures, recipe outputs,
   loot tables, and advancement effects.
4. Read the behaviour trace in execution order. Note conditions, resets, loops,
   schedules, and whether a state is per-player, per-entity, per-team, or global.
5. Compare the trace with a vanilla control test in a disposable world. State what
   vanilla behaviour was replaced, added, or left untouched.
6. Record unresolved references and ambiguous branches as unresolved. Do not fill
   gaps with a wiki description or a plausible design story.

An evidence reference must point to an actual archive-relative source path and a
precise locator where possible. For runtime evidence, include the disposable world
configuration, command or player action, initial state, observed output, and the
trace identifier. A file name alone is not a behaviour trace.

## Observed fact versus inferred intent

Keep these two kinds of statement separate in every report and classification row.

**Observed fact** is directly supported by a source path or controlled observation.
Examples include:

- a function executes `set ...` on an identified objective;
- an advancement criterion grants a named reward;
- a recipe accepts a particular ingredient and emits a particular output;
- an item uses a vanilla base item with an exact custom-model-data signature; or
- a runtime test shows that a player can or cannot perform an action under stated
  conditions.

**Inferred intent** is a reviewer's interpretation of why the behaviour exists or
what design problem it appears to address. It must be labelled as an inference and
must include a rationale and confidence. A display name, a function name, or a wiki
paragraph is not enough to turn an inference into a fact.

The separation matters because the classification decisions are reversible design
choices. Forever may keep an observed mechanic while changing its purpose, or it may
replace a mechanic whose purpose is useful but whose implementation conflicts with
world-save safety, accessibility, solo play, or adapter isolation.

## 5. Fill the system classification CSV

Start with `system-classification-template.csv`. The three rows beginning with
`EXAMPLE_ONLY.` are demonstrations, not findings. Delete them before adding real
rows. Keep one row per coherent system or behaviour cluster, not one row per source
file. A single system can cite many files in `source_files`.

For each real row:

1. assign a stable `system_id` such as `matcha.fire_campfire`;
2. fill `source_files` from the generated source inventory and include evidence
   locators in `notes` or the relevant report reference;
3. write `observed_behavior` using facts only;
4. describe the replaced vanilla behaviour separately from the observed Matcha
   behaviour;
5. write `inferred_purpose` as an explicitly marked inference, or use `unknown`;
6. assess player UX and the three risks using the allowed values below;
7. mark exactly one decision column with `x`: `keep`, `extend`, `override`,
   `replace_eventually`, or `undecided`;
8. make `rationale` explain the decision and its tradeoffs;
9. make `required_tests` concrete enough for another agent to execute; and
10. put unresolved references, provenance, and follow-up questions in `notes`.

Do not classify a system as `keep` merely because it is familiar. Do not classify it
as `replace_eventually` merely because a native Forever implementation sounds
cleaner. The evidence, risks, player experience, and migration cost belong in the
rationale.

### CSV value rules

The CSV is deliberately plain and clean. It has one exact header row, no comment
lines, and UTF-8 fields. Quote any field containing a comma. Within a real row,
`source_files` and `required_tests` use a semicolon-separated list of values. A
semicolon here separates data values, not a prose em dash.

| Column | Allowed values or format |
|---|---|
| `system_id` | Lowercase stable ID in the form `matcha.<slug>` for a finding. The reserved `EXAMPLE_ONLY.<slug>` prefix is used only by the template examples. |
| `category` | One of `progression`, `survival`, `equipment`, `items`, `food`, `health`, `economy`, `mobs`, `villagers`, `trades`, `structures`, `worldgen`, `environment`, `multiplayer`, `recipes`, `tutorialization`, `transport`, `storage`, `compatibility`, or `unknown`. |
| `source_files` | One or more actual archive-relative paths from the source inventory, separated by `;`. Use `unknown` only during triage, never as the final evidence for a decided row. |
| `observed_behavior` | Free text containing directly observed source or runtime facts, with evidence IDs or locators. Use `unknown` when it has not been established. Do not put inferred purpose here. |
| `vanilla_behavior_replaced` | `none`, `partial`, `full`, `unknown`, or `not_applicable`. |
| `inferred_purpose` | Free text beginning with `Inference:` plus a rationale and confidence, or `unknown`. |
| `player_ux` | `positive`, `neutral`, `mixed`, `negative`, or `unknown`, based on observed interaction and a stated reason. |
| `mod_compatibility_risk` | `none`, `low`, `medium`, `high`, or `unknown`. |
| `persistence_risk` | `none`, `low`, `medium`, `high`, or `unknown`. |
| `multiplayer_risk` | `none`, `low`, `medium`, `high`, or `unknown`. |
| `keep`, `extend`, `override`, `replace_eventually`, `undecided` | Either `x` or blank. Exactly one of these five decision columns must contain `x` for every real row. |
| `rationale` | Free text explaining the selected decision, evidence, tradeoffs, and any principle or ADR concern. |
| `required_tests` | One or more concrete test names or scenarios separated by `;`, including present and absent or vanilla controls where relevant. |
| `notes` | Free text for evidence IDs, ambiguities, migration notes, review ownership, or follow-up tickets. |

The initial CSV is a review instrument, not a permission to implement. A decided
row still requires tests and an implementation ticket before behaviour changes.

## Completion gate

The audit is ready for review only when:

- the lock and archive digest match;
- the generated report directory is version-specific and reproducible;
- every classification row cites actual source paths or is explicitly unresolved;
- observed facts and inferred intent are visibly separate;
- ambiguous behaviour is recorded as `unknown` or `undecided` rather than guessed;
- risks cover persistence and multiplayer as well as immediate gameplay;
- required tests include Matcha present and Matcha absent or vanilla-control cases;
- the manual system review in `manual-review-checklist.md` has been walked; and
- the reviewed CSV has no `EXAMPLE_ONLY.` rows remaining.

A later adapter implementation may consume the reviewed mapping, but it must still
refuse an unexpected Matcha version and retain the locked evidence reference.

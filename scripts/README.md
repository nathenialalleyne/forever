# Matcha acquisition and development installation

These scripts acquire the official **Matcha Flavoured** datapack/resource-pack
archive and install it only into an explicitly identified development setup. Matcha
is not a Gradle dependency and is not copied into Forever source control.

## Pinned source

The fetch scripts use only the official Modrinth API:

- Project metadata: `https://api.modrinth.com/v2/project/QI0EmgZ1`
- Version listing: `https://api.modrinth.com/v2/project/QI0EmgZ1/version`
- Artifact host: `https://cdn.modrinth.com/`

They do not use mirrors, wikis, search results, or third-party metadata. The
selected project, version, filename, source URL, publication timestamp, licence,
and downloaded SHA-256 are recorded in `matcha.lock.json`.

With no lock, the scripts select the latest listed `release` compatible with
Minecraft `26.2`. The explicit `--version <n>` option selects a listed release by
its displayed version number. Once the lock exists, a normal run reads it locally
and downloads only the locked URL. It does not query for a newer version.

Use `--refresh-lock` when changing the pin is intentional. Use `--force` to
re-download the archive named by the existing lock without refreshing that lock.
Both operations are explicit so a normal fetch cannot silently change a world’s
content.

## Fetch

From the repository root:

```text
bash scripts/fetch-matcha.sh
bash scripts/fetch-matcha.sh --dry-run
bash scripts/fetch-matcha.sh --version 1.12
bash scripts/fetch-matcha.sh --refresh-lock
bash scripts/fetch-matcha.sh --force
```

The PowerShell equivalent is:

```powershell
pwsh -File .\scripts\fetch-matcha.ps1
pwsh -File .\scripts\fetch-matcha.ps1 --dry-run
```

The verified archive is stored at `vendor/matcha/`. The directory and archive are
ignored because the binary is third-party material. The lock is deliberately
committed because it makes acquisition deterministic and auditable. A failed
SHA-256 check exits non-zero. A bad existing locked archive is removed and the
script stops rather than using it.

The shell script requires `curl`, `sha256sum`, `python3`, and `mktemp`. PowerShell
uses `Invoke-RestMethod`, `Invoke-WebRequest`, and `Get-FileHash`.

## Development installation

The installer never guesses a world path. It requires an existing development
world directory and either:

1. `--ack-dev-world`, which is an explicit one-time acknowledgement on the
   command line, or
2. a `.forever-dev-world` marker file inside the development world.

It refuses paths under common `.minecraft` save locations and paths whose final
component is an obviously real name such as `world`, `survival`, `production`, or
`server`, even with the acknowledgement flag. This is a safety boundary because a
mistake here would mutate a valuable world save. Use a disposable development copy
with a neutral name such as `matcha-dev-26-2`.

The default resource-pack directory is `run/resourcepacks`. The same locked archive
is copied to that directory and to `<world>/datapacks/`:

```text
bash scripts/install-matcha-dev.sh \
  --world /path/to/matcha-dev-26-2 \
  --ack-dev-world

bash scripts/install-matcha-dev.sh \
  --world /path/to/matcha-dev-26-2 \
  --resource-pack-dir /path/to/dev-resourcepacks \
  --dry-run \
  --ack-dev-world
```

The PowerShell equivalent is:

```powershell
pwsh -File .\scripts\install-matcha-dev.ps1 `
  --world 'C:\path\to\matcha-dev-26-2' `
  --ack-dev-world
```

The installer verifies the locked archive’s SHA-256 before making either copy and
prints each source-to-destination path. It creates only the requested resource-pack
and `datapacks` directories. It never touches a real survival world path, and it
never installs to a world unless the path is explicitly supplied.

## Matcha audit

```sh
./scripts/run-matcha-audit.sh \
  --input vendor/matcha/Matcha_Flavoured_1_12.zip \
  --output generated/matcha/1.12
```

Regenerates the audit reports from a locally acquired, pinned Matcha archive. Fetch the
archive with `./scripts/fetch-matcha.sh` first.

**Reproducible output.** Set `SOURCE_DATE_EPOCH` to make two runs byte-identical:

```sh
SOURCE_DATE_EPOCH=1787885453 ./scripts/run-matcha-audit.sh \
  --input vendor/matcha/Matcha_Flavoured_1_12.zip \
  --output generated/matcha/1.12
```

Every field in a report is a pure function of the input archive except the generation
timestamp, which was previously the only reason two audits of the same archive differed.
That defeated verifying the committed reports by regenerating and comparing them.
`SOURCE_DATE_EPOCH` is the cross-ecosystem convention for this, so no project-specific
flag is needed. When it is unset the real generation time is recorded, because a one-off
report is more useful with a true timestamp than a fixed placeholder.

This matters for publication: if the generated reports can be reproduced from a
legitimately obtained archive, they need not be committed at all. See
`labs/results/LAB-LICENCE-publication-readiness.md`.

## Pack download verification

```sh
python3 scripts/verify-pack-downloads.py                     # from pack/, the source of truth
python3 scripts/verify-pack-downloads.py --mrpack dist/*.mrpack   # from an exported pack
```

Downloads every pinned dependency and checks its bytes against the pinned hash and size.

This answers a different question from `validate-pack.sh`. That script checks the metadata
is internally consistent; a pin can be perfectly well-formed and still point at a URL that
404s or at a file whose contents have changed. This is the closest thing to an end-to-end
test the pack has, and the only check standing between "the metadata looks right" and "a
player can install this".

Requires network access, so it is a local and release-time check rather than part of the
offline CI run. It downloads to memory and writes nothing, because third-party binaries
must never land in the working tree.

Run it before any release, and after changing or re-pinning a dependency.

## Repository-map check

```sh
python3 scripts/check-repo-map.py
```

Fails when the AGENTS.md repository map and the tracked tree disagree, in either
direction. An undocumented tracked path means work landed somewhere unexplained; a map
row that matches nothing tracked means the map describes a layout that no longer exists.

The map is how a new agent decides where code belongs, so a stale map is worse than no
map: it teaches the wrong layout confidently. This check was written after four tracked
directories were found missing from it, including `companion/`, which exists purely to
mark a boundary.

Deliberately git-ignored directories such as `dist/` and `vendor/` are listed explicitly
in the script, so granting an exemption is a visible edit rather than a silent match.
Never create an empty directory to satisfy this check: AGENTS.md forbids placeholder
directories, and the correct fix is to remove the stale row.

Runs in CI. Pure Python with no dependencies.

## Client smoke check

```sh
./scripts/client-smoke.sh
```

The PowerShell equivalent is:

```powershell
pwsh -File .\scripts\client-smoke.ps1
```

Launches the development client, confirms it reaches a rendering main menu, and stops
it. The client never exits on its own, so the script always runs it under a timeout and
always kills any surviving process: a stray client would hold the display and corrupt a
later run's evidence.

It checks four things: a graphics backend was initialised, at least five texture atlases
were stitched, the companion mod logged client initialisation, and nothing was logged
beyond the known environment gaps. Atlas stitching happens late in startup and only when
rendering genuinely works, which makes it a stronger signal than any single log line.

Override the defaults with `CLIENT_SMOKE_TIMEOUT` and `CLIENT_SMOKE_LOG`.

This script exists because the client half of several decisions was previously deferred
on the untested belief that no client could run on a build host. That belief was false;
see `docs/testing/client-environment.md`. Making the check runnable stops the false
constraint returning quietly.

When no display is available the script **skips** and exits successfully, because a
missing display is a property of the machine rather than a defect in the pack. It
verifies that the client renders, not that it looks correct: visual judgement, sound,
narration, and multiplayer still need a human, per `docs/testing/client-verification.md`.

## Licensing boundary

Matcha is a separate third-party project under `CC-BY-NC-SA-4.0`. The original
Forever code remains private and is covered by the boundary described in the root
`LICENSE`. See `THIRD_PARTY_NOTICES.md` before considering any public distribution.

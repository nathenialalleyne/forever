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

## Licensing boundary

Matcha is a separate third-party project under `CC-BY-NC-SA-4.0`. The original
Forever code remains private and is covered by the boundary described in the root
`LICENSE`. See `THIRD_PARTY_NOTICES.md` before considering any public distribution.

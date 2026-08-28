# Export the Many Roads Home modpack to a Modrinth .mrpack under dist/.
# Windows counterpart of build-pack.sh. Behaviour and exit codes match.
$ErrorActionPreference = 'Stop'

$RootDir = Split-Path -Parent $PSScriptRoot
$PackDir = Join-Path $RootDir 'pack'
$DistDir = Join-Path $RootDir 'dist'
$Config  = Join-Path $RootDir '.config-packwiz.toml'

if (-not (Get-Command packwiz -ErrorAction SilentlyContinue)) {
    Write-Error "build-pack: packwiz is not on PATH. Install with: go install github.com/packwiz/packwiz@latest"
    exit 3
}

$version = (Select-String -Path (Join-Path $PackDir 'pack.toml') -Pattern '^version *= *"(.*)"').Matches[0].Groups[1].Value
if (-not $version) { Write-Error 'build-pack: could not read version from pack/pack.toml'; exit 4 }

New-Item -ItemType Directory -Force -Path $DistDir | Out-Null
$Out = Join-Path $DistDir "many-roads-home-$version.mrpack"

Push-Location $PackDir
try {
    # Refresh before export so the index hash matches the metadata on disk.
    packwiz --config $Config refresh
    packwiz --config $Config modrinth export -o $Out
} finally { Pop-Location }

Write-Host "build-pack: wrote $Out"

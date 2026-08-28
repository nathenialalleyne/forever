# Validate Many Roads Home pack metadata. Windows counterpart of validate-pack.sh.
# Does not download mods and needs no private credentials.
$ErrorActionPreference = 'Stop'

$RootDir = Split-Path -Parent $PSScriptRoot
$PackDir = Join-Path $RootDir 'pack'
$Config  = Join-Path $RootDir '.config-packwiz.toml'
$failures = 0

function Fail($m) { Write-Host "validate-pack: FAIL: $m"; $script:failures++ }
function Pass($m) { Write-Host "validate-pack: ok: $m" }

if (-not (Test-Path (Join-Path $PackDir 'pack.toml'))) { Fail 'pack/pack.toml is missing'; exit 1 }

# Gradle wrapper JARs are a legitimate part of the build toolchain and are excluded.
$binaries = git -C $RootDir ls-files -- '*.jar' '*.mrpack' 'pack/**/*.zip' |
    Where-Object { $_ -notmatch 'gradle/wrapper/gradle-wrapper\.jar' }
if ($binaries) { Fail "third-party binaries are committed: $($binaries -join ', ')" }
else { Pass 'no third-party binaries committed' }

$missingPin = $false
Get-ChildItem -Path $PackDir -Recurse -Filter '*.pw.toml' | ForEach-Object {
    $c = Get-Content $_.FullName -Raw
    if ($c -notmatch '(?m)^version = ') { Fail "no pinned version in $($_.Name)"; $missingPin = $true }
    if ($c -notmatch '(?m)^hash = ')    { Fail "no download hash in $($_.Name)"; $missingPin = $true }
}
if (-not $missingPin) { Pass 'every dependency pins an exact version and hash' }

$packToml = Get-Content (Join-Path $PackDir 'pack.toml') -Raw
if ($packToml -notmatch 'minecraft = "26\.2"')  { Fail 'pack.toml is not pinned to Minecraft 26.2' }
if ($packToml -notmatch 'fabric = "0\.19\.3"') { Fail 'pack.toml is not pinned to Fabric loader 0.19.3' }
Pass 'baseline versions checked'

if (Get-Command packwiz -ErrorAction SilentlyContinue) {
    Push-Location $PackDir
    try { packwiz --config $Config refresh | Out-Null; Pass 'packwiz refresh succeeded' }
    catch { Fail 'packwiz refresh failed' }
    finally { Pop-Location }
} else { Write-Host 'validate-pack: note: packwiz not on PATH, skipped index refresh' }

if ($failures -gt 0) { Write-Host "validate-pack: $failures check(s) failed"; exit 1 }
Write-Host 'validate-pack: all checks passed'

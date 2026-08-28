# Run the standalone Matcha audit CLI against the locally acquired archive.
# Windows counterpart of run-matcha-audit.sh.
#
# The audit tool is a separate Gradle build with its own wrapper. It is deliberately not
# part of the mod build, so it must never be invoked through the root Gradle project.
$ErrorActionPreference = 'Stop'

$RootDir  = Split-Path -Parent $PSScriptRoot
$ToolDir  = Join-Path $RootDir 'tools/matcha-audit'
$Archive  = Join-Path $RootDir 'vendor/matcha/Matcha_Flavoured_1_12.zip'
$Lock     = Join-Path $RootDir 'matcha.lock.json'
$OutDir   = Join-Path $RootDir 'generated/matcha/1.12'

if (-not (Test-Path $Archive)) {
    Write-Error "run-matcha-audit: $Archive is missing. Fetch it first with scripts/fetch-matcha.ps1."
    exit 3
}

Push-Location $ToolDir
try {
    & ./gradlew.bat run --args="--archive `"$Archive`" --lock `"$Lock`" --output `"$OutDir`""
} finally { Pop-Location }

Write-Host "run-matcha-audit: reports written to $OutDir"

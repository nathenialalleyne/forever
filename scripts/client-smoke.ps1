#Requires -Version 7.0
<#
.SYNOPSIS
    Launch the development client, confirm it reaches a rendering main menu, and stop it.

.DESCRIPTION
    This exists because the client half of several decisions was deferred on the belief
    that no client could run on a build host. That belief was untested and false. See
    docs/testing/client-environment.md. This script makes the check repeatable so the
    false constraint cannot quietly return.

    It verifies that the client renders, not that it looks correct. Visual judgement,
    sound, narration, and multiplayer still need a human: see
    docs/testing/client-verification.md.

    Behaviour matches scripts/client-smoke.sh.
#>
[CmdletBinding()]
param(
    # The client never exits on its own, so it is always run under a timeout.
    [int] $TimeoutSeconds = $(if ($env:CLIENT_SMOKE_TIMEOUT) { [int] $env:CLIENT_SMOKE_TIMEOUT } else { 420 }),
    [string] $LogFile = $(if ($env:CLIENT_SMOKE_LOG) { $env:CLIENT_SMOKE_LOG } else { Join-Path ([System.IO.Path]::GetTempPath()) 'mrh-client-smoke.log' })
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$rootDir = Split-Path -Parent $PSScriptRoot
$script:Failures = 0

function Write-Fail { param([string] $Message) Write-Host "client-smoke: FAIL: $Message"; $script:Failures++ }
function Write-Pass { param([string] $Message) Write-Host "client-smoke: ok: $Message" }

Push-Location $rootDir
try {
    Write-Host "client-smoke: launching the client for up to ${TimeoutSeconds}s"
    Write-Host "client-smoke: log: $LogFile"

    $gradlew = Join-Path $rootDir 'gradlew.bat'

    # The client is expected to be killed by the timeout, so a non-zero exit is normal
    # and must not fail the script. The log contents are the actual evidence.
    $process = Start-Process -FilePath $gradlew -ArgumentList 'runClient' `
        -RedirectStandardOutput $LogFile -RedirectStandardError "$LogFile.err" `
        -NoNewWindow -PassThru

    if (-not $process.WaitForExit($TimeoutSeconds * 1000)) {
        # Stop the whole tree: killing Gradle alone would orphan the client process,
        # which would hold the display and corrupt a later run's evidence.
        Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
    }
    Get-Process -Name 'java' -ErrorAction SilentlyContinue |
        Where-Object { $_.CommandLine -and $_.CommandLine -match 'devlaunchinjector' } |
        Stop-Process -Force -ErrorAction SilentlyContinue

    if (Test-Path "$LogFile.err") {
        Get-Content "$LogFile.err" | Add-Content -Path $LogFile
        Remove-Item "$LogFile.err" -ErrorAction SilentlyContinue
    }

    if (-not (Test-Path $LogFile) -or (Get-Item $LogFile).Length -eq 0) {
        Write-Fail 'the client produced no output at all'
        exit 1
    }

    $log = Get-Content $LogFile

    # A rendering backend proves more than "the process started": it proves a real GL
    # context was created, which is exactly what the false blocker denied was possible.
    $backend = $log | Select-String -Pattern 'Using graphics backend.*' | Select-Object -First 1
    if ($backend) { Write-Pass $backend.Matches[0].Value }
    else { Write-Fail 'no graphics backend was initialised' }

    # Texture atlases are stitched late in startup and only when rendering genuinely
    # works, so they are a stronger signal of reaching the menu than any single line.
    $atlasCount = ($log | Select-String -Pattern 'Created: .*-atlas').Count
    if ($atlasCount -ge 5) { Write-Pass "stitched $atlasCount texture atlases" }
    else { Write-Fail "only $atlasCount texture atlases were stitched; the client did not reach the menu" }

    if ($log | Select-String -Pattern '\(forever' -Quiet) {
        Write-Pass 'the companion mod initialised on the client'
    } else {
        Write-Fail 'the companion mod did not log any client initialisation'
    }

    # Known environment gaps are excluded deliberately and named, so a real new warning
    # is not lost in noise. Classpath lines are excluded because a single missing-native
    # stack trace prints the entire classpath.
    $unexpected = $log |
        Select-String -Pattern 'WARN|ERROR' |
        Where-Object { $_ -notmatch '(?i)narrator|flite|soundsystem|openal|text2speech' } |
        Where-Object { $_ -notmatch '(?i)\.jar|classpath|[/\\]home[/\\]' }
    if ($unexpected) {
        Write-Host 'client-smoke: note: unexpected warnings or errors were logged:'
        $unexpected | Select-Object -First 20 | ForEach-Object { Write-Host "  $_" }
        Write-Fail 'the client logged warnings beyond the known environment gaps'
    } else {
        Write-Pass 'no warnings beyond the known audio and narrator gaps'
    }

    if ($script:Failures -gt 0) {
        Write-Host "client-smoke: $($script:Failures) check(s) failed; see $LogFile"
        exit 1
    }

    Write-Host 'client-smoke: all checks passed'
}
finally {
    Pop-Location
}

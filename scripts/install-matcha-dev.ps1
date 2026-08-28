Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# Installing a datapack changes a world save. This script therefore accepts only
# an explicitly supplied development-world path, rejects common real-world paths,
# and requires either an acknowledgement flag or a marker file in that world.
$ExitUsage = 2
$ExitDependency = 3
$ExitLock = 7
$ExitHash = 6
$ExitFilesystem = 5
$ProjectId = 'QI0EmgZ1'
$TargetMinecraft = '26.2'
$ExpectedLicense = 'CC-BY-NC-SA-4.0'
$ScriptDirectory = Split-Path -Parent $MyInvocation.MyCommand.Definition
$RootDirectory = [System.IO.Path]::GetFullPath((Join-Path -Path $ScriptDirectory -ChildPath '..'))
$LockPath = Join-Path -Path $RootDirectory -ChildPath 'matcha.lock.json'
$VendorDirectory = Join-Path -Path $RootDirectory -ChildPath 'vendor/matcha'
$DefaultResourcePackDirectory = Join-Path -Path $RootDirectory -ChildPath 'run/resourcepacks'
$TemporaryPaths = [System.Collections.Generic.List[string]]::new()
$MatchaExitCode = 1

function Show-Usage {
    @'
Usage: scripts/install-matcha-dev.ps1 --world <path> [options]

Copy the locked Matcha archive to the development resource-pack directory and
to the supplied development world's datapacks directory.

Required:
  --world <path>             Existing development world directory.

Options:
  --resource-pack-dir <path> Override the resource-pack directory. Relative paths
                             are resolved from the current directory.
  --ack-dev-world            Explicitly acknowledge that --world is disposable
                             development data. A .forever-dev-world marker file
                             is an alternative.
  --dry-run                  Validate the lock, archive, and safety checks, then
                             print the two copies without changing the filesystem.
  -h, --help                 Show this help text.

The world path is never inferred from run/, saves/, or a Minecraft installation.
Paths under common .minecraft saves directories and obviously named production
worlds are refused even with --ack-dev-world.
'@
}

function Fail {
    param(
        [int]$Code,
        [string]$Message
    )
    $script:MatchaExitCode = $Code
    throw $Message
}

function Get-Sha256 {
    param([string]$Path)
    try {
        return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
    }
    catch {
        Fail $ExitHash ("could not calculate SHA-256 for {0}: {1}" -f $Path, $_.Exception.Message)
    }
}

function Read-Lock {
    try {
        $lock = Get-Content -LiteralPath $LockPath -Raw -Encoding UTF8 | ConvertFrom-Json
    }
    catch {
        Fail $ExitLock ("cannot read JSON lock {0}: {1}" -f $LockPath, $_.Exception.Message)
    }
    $requiredFields = @(
        'modrinth_project_id', 'version_id', 'version_number',
        'minecraft_compatibility', 'release_type', 'published_timestamp',
        'filename', 'download_source_url', 'sha256', 'license_identifier'
    )
    foreach ($field in $requiredFields) {
        if ($null -eq $lock.PSObject.Properties[$field]) {
            Fail $ExitLock ("lock is missing required field: {0}" -f $field)
        }
    }
    if ([string]$lock.modrinth_project_id -ne $ProjectId) {
        Fail $ExitLock 'lock belongs to a different Modrinth project'
    }
    if (-not (@($lock.minecraft_compatibility) -contains $TargetMinecraft)) {
        Fail $ExitLock ("lock is not compatible with Minecraft {0}" -f $TargetMinecraft)
    }
    if ([string]$lock.release_type -ne 'release' -or [string]$lock.license_identifier -ne $ExpectedLicense) {
        Fail $ExitLock 'lock is not the expected Matcha release or licence'
    }
    $filename = [string]$lock.filename
    if ([string]::IsNullOrWhiteSpace($filename) -or $filename -in @('.', '..') -or $filename.Contains('/') -or $filename.Contains('\')) {
        Fail $ExitLock 'lock contains an unsafe archive filename'
    }
    $sha256 = [string]$lock.sha256
    if ($sha256 -notmatch '^[0-9a-fA-F]{64}$') {
        Fail $ExitLock 'lock SHA-256 must be 64 hexadecimal characters'
    }
    try {
        $url = [System.Uri][string]$lock.download_source_url
    }
    catch {
        Fail $ExitLock 'lock download URL is not a valid URL'
    }
    if ($url.Scheme -ne 'https' -or $url.Host -ne 'cdn.modrinth.com') {
        Fail $ExitLock 'lock download URL is not the official Modrinth CDN'
    }
    return [pscustomobject]@{
        Filename = $filename
        Sha256 = $sha256.ToLowerInvariant()
    }
}

function Get-ExistingDirectoryPath {
    param([string]$Path)
    try {
        $item = Get-Item -LiteralPath $Path -Force
    }
    catch {
        Fail $ExitUsage ("development world directory does not exist: {0}" -f $Path)
    }
    if (-not $item.PSIsContainer) {
        Fail $ExitUsage ("development world path is not a directory: {0}" -f $Path)
    }
    return $item.FullName
}

function Refuse-ObviouslyRealWorld {
    param([string]$Path)
    $normalized = $Path.Replace('\', '/')
    $home = [Environment]::GetFolderPath('UserProfile').Replace('\', '/').TrimEnd('/')
    if ($normalized -eq '/' -or $normalized -eq $home -or $normalized -eq $RootDirectory.Replace('\', '/')) {
        Fail $ExitUsage ("refusing an obviously unsafe world path: {0}" -f $Path)
    }
    $realSavePatterns = @(
        "$home/.minecraft/saves/*",
        "$home/.minecraft/instances/*/saves/*",
        "$home/.var/app/*/.minecraft/saves/*",
        "$home/multimc/instances/*/.minecraft/saves/*",
        "$home/PrismLauncher/instances/*/.minecraft/saves/*"
    )
    foreach ($pattern in $realSavePatterns) {
        if ($normalized -like $pattern) {
            Fail $ExitUsage ("refusing a Minecraft installation or real save path: {0}" -f $Path)
        }
    }
    $leaf = (Split-Path -Leaf $normalized).ToLowerInvariant()
    if ($leaf -in @('world', 'world_nether', 'world_the_end', 'survival', 'survival_world', 'survival-world', 'main', 'live', 'prod', 'production', 'server', 'server-world')) {
        Fail $ExitUsage ("refusing an obviously real world name: {0}" -f $Path)
    }
}

$exitCode = 0
try {
    $worldPath = ''
    $resourcePackDirectory = $DefaultResourcePackDirectory
    $acknowledgeDevelopmentWorld = $false
    $dryRun = $false
    $showHelp = $false
    $argumentList = @($args)
    $index = 0
    while ($index -lt $argumentList.Count) {
        $token = [string]$argumentList[$index]
        if ($token -in @('-h', '--help')) {
            Show-Usage
            $showHelp = $true
            break
        }
        if ($token -eq '--world') {
            if ($index + 1 -ge $argumentList.Count -or [string]::IsNullOrWhiteSpace([string]$argumentList[$index + 1]) -or [string]$argumentList[$index + 1].StartsWith('-')) {
                Fail $ExitUsage '--world requires an existing development world path'
            }
            $worldPath = [string]$argumentList[$index + 1]
            $index += 2
            continue
        }
        if ($token.StartsWith('--world=', [StringComparison]::Ordinal)) {
            $worldPath = $token.Substring('--world='.Length)
            if ([string]::IsNullOrWhiteSpace($worldPath)) {
                Fail $ExitUsage '--world requires an existing development world path'
            }
            $index++
            continue
        }
        if ($token -eq '--resource-pack-dir') {
            if ($index + 1 -ge $argumentList.Count -or [string]::IsNullOrWhiteSpace([string]$argumentList[$index + 1])) {
                Fail $ExitUsage '--resource-pack-dir requires a path'
            }
            $resourcePackDirectory = [string]$argumentList[$index + 1]
            $index += 2
            continue
        }
        if ($token.StartsWith('--resource-pack-dir=', [StringComparison]::Ordinal)) {
            $resourcePackDirectory = $token.Substring('--resource-pack-dir='.Length)
            if ([string]::IsNullOrWhiteSpace($resourcePackDirectory)) {
                Fail $ExitUsage '--resource-pack-dir requires a path'
            }
            $index++
            continue
        }
        if ($token -eq '--ack-dev-world') {
            $acknowledgeDevelopmentWorld = $true
            $index++
            continue
        }
        if ($token -eq '--dry-run') {
            $dryRun = $true
            $index++
            continue
        }
        if ($token -eq '--') {
            if ($index + 1 -lt $argumentList.Count) {
                Fail $ExitUsage 'unexpected arguments after --'
            }
            break
        }
        Fail $ExitUsage ("unknown option: {0}" -f $token)
    }

    if (-not $showHelp) {
        if ([string]::IsNullOrWhiteSpace($worldPath)) {
            Fail $ExitUsage '--world is required; the script never guesses a world path'
        }
        if ((Test-Path -LiteralPath $LockPath) -and -not (Test-Path -LiteralPath $LockPath -PathType Leaf)) {
            Fail $ExitLock ("lock path exists but is not a regular file: {0}" -f $LockPath)
        }
        if (-not (Test-Path -LiteralPath $LockPath -PathType Leaf)) {
            Fail $ExitLock ("Matcha lock not found: {0}; run fetch-matcha.ps1 first" -f $LockPath)
        }
        $worldAbsolute = Get-ExistingDirectoryPath $worldPath
        Refuse-ObviouslyRealWorld $worldAbsolute
        $markerPath = Join-Path -Path $worldAbsolute -ChildPath '.forever-dev-world'
        if (-not $acknowledgeDevelopmentWorld -and -not (Test-Path -LiteralPath $markerPath -PathType Leaf)) {
            Fail $ExitUsage ("world is not acknowledged as disposable development data; pass --ack-dev-world or create {0}" -f $markerPath)
        }

        $lock = Read-Lock
        $archivePath = Join-Path -Path $VendorDirectory -ChildPath $lock.Filename
        if (-not (Test-Path -LiteralPath $archivePath -PathType Leaf)) {
            Fail $ExitFilesystem ("locked Matcha archive not found: {0}; run fetch-matcha.ps1 first" -f $archivePath)
        }
        $actualSha256 = Get-Sha256 $archivePath
        if ($actualSha256 -ne $lock.Sha256) {
            Fail $ExitHash ("locked Matcha archive SHA-256 mismatch (expected {0}, got {1})" -f $lock.Sha256, $actualSha256)
        }

        if ([System.IO.Path]::IsPathRooted($resourcePackDirectory)) {
            $resourcePackAbsolute = [System.IO.Path]::GetFullPath($resourcePackDirectory)
        }
        else {
            $resourcePackAbsolute = [System.IO.Path]::GetFullPath((Join-Path -Path (Get-Location).Path -ChildPath $resourcePackDirectory))
        }
        $resourceDestination = Join-Path -Path $resourcePackAbsolute -ChildPath $lock.Filename
        $datapackDirectory = Join-Path -Path $worldAbsolute -ChildPath 'datapacks'
        $datapackDestination = Join-Path -Path $datapackDirectory -ChildPath $lock.Filename

        if ($dryRun) {
            Write-Output ('DRY-RUN: would copy {0} -> {1}' -f $archivePath, $resourceDestination)
            Write-Output ('DRY-RUN: would copy {0} -> {1}' -f $archivePath, $datapackDestination)
        }
        else {
            try {
                New-Item -ItemType Directory -Path $resourcePackAbsolute -Force | Out-Null
                New-Item -ItemType Directory -Path $datapackDirectory -Force | Out-Null
            }
            catch {
                Fail $ExitFilesystem 'could not create one or more development destination directories'
            }
            $resourceTemp = Join-Path -Path $resourcePackAbsolute -ChildPath ('.matcha-install-{0}.tmp' -f [Guid]::NewGuid().ToString('N'))
            $datapackTemp = Join-Path -Path $datapackDirectory -ChildPath ('.matcha-install-{0}.tmp' -f [Guid]::NewGuid().ToString('N'))
            $TemporaryPaths.Add($resourceTemp)
            $TemporaryPaths.Add($datapackTemp)
            try {
                Copy-Item -LiteralPath $archivePath -Destination $resourceTemp -Force
                Copy-Item -LiteralPath $archivePath -Destination $datapackTemp -Force
                Move-Item -LiteralPath $resourceTemp -Destination $resourceDestination -Force
                $TemporaryPaths.Remove($resourceTemp) | Out-Null
                Move-Item -LiteralPath $datapackTemp -Destination $datapackDestination -Force
                $TemporaryPaths.Remove($datapackTemp) | Out-Null
            }
            catch {
                Fail $ExitFilesystem ("could not install Matcha in both development destinations: {0}" -f $_.Exception.Message)
            }
            Write-Output ('Copied {0} -> {1}' -f $archivePath, $resourceDestination)
            Write-Output ('Copied {0} -> {1}' -f $archivePath, $datapackDestination)
        }
    }
}
catch {
    [Console]::Error.WriteLine(('install-matcha-dev: error: {0}' -f $_.Exception.Message))
    $exitCode = $MatchaExitCode
}
finally {
    foreach ($temporaryPath in @($TemporaryPaths)) {
        if ($temporaryPath -and (Test-Path -LiteralPath $temporaryPath -PathType Leaf)) {
            Remove-Item -LiteralPath $temporaryPath -Force -ErrorAction SilentlyContinue
        }
    }
}
exit $exitCode

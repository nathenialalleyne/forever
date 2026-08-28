Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# This script mirrors fetch-matcha.sh. Resolution is restricted to Modrinth's
# official API, and the artifact host is restricted to Modrinth's official CDN.
$ExitUsage = 2
$ExitDependency = 3
$ExitMetadata = 4
$ExitDownload = 5
$ExitHash = 6
$ExitLock = 7
$ProjectId = 'QI0EmgZ1'
$TargetMinecraft = '26.2'
$ExpectedLicense = 'CC-BY-NC-SA-4.0'
$ApiBase = 'https://api.modrinth.com/v2'
$UserAgent = 'Forever-MatchaFetcher/1.0 (private project)'

$ScriptDirectory = Split-Path -Parent $MyInvocation.MyCommand.Definition
$RootDirectory = [System.IO.Path]::GetFullPath((Join-Path -Path $ScriptDirectory -ChildPath '..'))
$LockPath = Join-Path -Path $RootDirectory -ChildPath 'matcha.lock.json'
$VendorDirectory = Join-Path -Path $RootDirectory -ChildPath 'vendor/matcha'
$TemporaryPaths = [System.Collections.Generic.List[string]]::new()
$MatchaExitCode = 1

function Show-Usage {
    @'
Usage: scripts/fetch-matcha.ps1 [options]

Acquire the pinned Matcha Flavoured archive for Minecraft 26.2.

Options:
  --version <n>       Intentionally select the listed release number, for example 1.12.
  --refresh-lock      Resolve the latest stable 26.2 release and replace the lock.
  --force             Re-download the archive named by the existing lock. This does
                      not refresh the lock or resolve a newer version.
  --dry-run           Resolve or read the selection and print the planned action
                      without downloading or changing the lock.
  -h, --help          Show this help text.

Without --version or --refresh-lock, an existing lock is authoritative and no
newer version is queried. With no lock, the latest listed release compatible with
Minecraft 26.2 is resolved from api.modrinth.com.

Exit codes:
  0 success
  2 invalid command-line usage
  3 missing local dependency
  4 official Modrinth metadata failure
  5 download or filesystem failure
  6 SHA-256 mismatch
  7 invalid or unsafe lock metadata
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

function Get-ModrinthJson {
    param([string]$Uri)
    try {
        return Invoke-RestMethod -Uri $Uri -Method Get -Headers @{ 'User-Agent' = $UserAgent }
    }
    catch {
        Fail $ExitMetadata ("official Modrinth API request failed for {0}: {1}" -f $Uri, $_.Exception.Message)
    }
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

function Read-LockSelection {
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
    if ([string]$lock.release_type -ne 'release') {
        Fail $ExitLock 'lock does not identify a stable release'
    }
    if ([string]$lock.license_identifier -ne $ExpectedLicense) {
        Fail $ExitLock 'lock has an unexpected Matcha licence identifier'
    }

    $filename = [string]$lock.filename
    if ([string]::IsNullOrWhiteSpace($filename) -or $filename -in @('.', '..') -or $filename.Contains('/') -or $filename.Contains('\')) {
        Fail $ExitLock 'lock contains an unsafe archive filename'
    }
    $downloadUrl = [string]$lock.download_source_url
    try {
        $url = [System.Uri]$downloadUrl
    }
    catch {
        Fail $ExitLock 'lock download URL is not a valid URL'
    }
    if ($url.Scheme -ne 'https' -or $url.Host -ne 'cdn.modrinth.com') {
        Fail $ExitLock 'lock download URL is not the official Modrinth CDN'
    }
    $sha256 = [string]$lock.sha256
    if ($sha256 -notmatch '^[0-9a-fA-F]{64}$') {
        Fail $ExitLock 'lock SHA-256 must be 64 hexadecimal characters'
    }

    return [pscustomobject]@{
        ProjectId = [string]$lock.modrinth_project_id
        ProjectSlug = if ($null -eq $lock.PSObject.Properties['project_slug']) { 'matcha-flavoured' } else { [string]$lock.project_slug }
        ProjectName = if ($null -eq $lock.PSObject.Properties['project_name']) { 'Matcha Flavoured' } else { [string]$lock.project_name }
        LicenseIdentifier = [string]$lock.license_identifier
        VersionId = [string]$lock.version_id
        VersionNumber = [string]$lock.version_number
        Minecraft = $TargetMinecraft
        ReleaseType = [string]$lock.release_type
        Published = [string]$lock.published_timestamp
        Filename = $filename
        DownloadUrl = $downloadUrl
        ExpectedSha256 = $sha256.ToLowerInvariant()
        SourceApiUrl = if ($null -eq $lock.PSObject.Properties['source']) { "$ApiBase/version/$($lock.version_id)" } else { [string]$lock.source.version_api_url }
        ProjectApiUrl = if ($null -eq $lock.PSObject.Properties['source']) { "$ApiBase/project/$ProjectId" } else { [string]$lock.source.project_api_url }
        FileSize = if ($null -eq $lock.PSObject.Properties['artifact']) { $null } else { $lock.artifact.size }
        ApiHashes = if ($null -eq $lock.PSObject.Properties['artifact']) { @{} } else { $lock.artifact.api_hashes }
    }
}

function Resolve-Selection {
    param([string]$RequestedVersion)
    $project = Get-ModrinthJson "$ApiBase/project/$ProjectId"
    $versions = Get-ModrinthJson "$ApiBase/project/$ProjectId/version"
    if ([string]$project.id -ne $ProjectId) {
        Fail $ExitMetadata 'official API returned an unexpected project ID'
    }
    if ([string]$project.license.id -ne $ExpectedLicense) {
        Fail $ExitMetadata 'official API reports an unexpected Matcha licence'
    }

    $candidates = @($versions | Where-Object {
        ([string]$_.project_id -eq $ProjectId) -and
        (@($_.game_versions) -contains $TargetMinecraft) -and
        ([string]$_.version_type -eq 'release') -and
        ([string]$_.status -eq 'listed') -and
        ([string]::IsNullOrEmpty($RequestedVersion) -or [string]$_.version_number -eq $RequestedVersion)
    })
    if ($candidates.Count -eq 0) {
        $requestedText = if ([string]::IsNullOrEmpty($RequestedVersion)) { '' } else { " version '$RequestedVersion'" }
        Fail $ExitMetadata ("no listed release compatible with Minecraft {0}{1} was found" -f $TargetMinecraft, $requestedText)
    }

    $sorted = @($candidates | Sort-Object `
        @{ Expression = { [DateTimeOffset]::Parse([string]$_.date_published) }; Descending = $true }, `
        @{ Expression = { [string]$_.id }; Descending = $true })
    $selected = $sorted | Select-Object -First 1
    $primaryFiles = @($selected.files | Where-Object { $_.primary -eq $true })
    if ($primaryFiles.Count -ne 1) {
        Fail $ExitMetadata ("selected Modrinth version {0} does not have exactly one primary file" -f $selected.id)
    }
    $primary = $primaryFiles[0]
    $filename = [string]$primary.filename
    if ([string]::IsNullOrWhiteSpace($filename) -or $filename -in @('.', '..') -or $filename.Contains('/') -or $filename.Contains('\')) {
        Fail $ExitMetadata 'official metadata contains an unsafe archive filename'
    }
    try {
        $url = [System.Uri][string]$primary.url
    }
    catch {
        Fail $ExitMetadata 'official metadata contains an invalid artifact URL'
    }
    if ($url.Scheme -ne 'https' -or $url.Host -ne 'cdn.modrinth.com') {
        Fail $ExitMetadata 'selected download URL is not the official Modrinth CDN'
    }

    return [pscustomobject]@{
        ProjectId = $ProjectId
        ProjectSlug = [string]$project.slug
        ProjectName = [string]$project.title
        LicenseIdentifier = [string]$project.license.id
        VersionId = [string]$selected.id
        VersionNumber = [string]$selected.version_number
        Minecraft = $TargetMinecraft
        ReleaseType = [string]$selected.version_type
        Published = [string]$selected.date_published
        Filename = $filename
        DownloadUrl = [string]$primary.url
        ExpectedSha256 = ''
        SourceApiUrl = "$ApiBase/version/$($selected.id)"
        ProjectApiUrl = "$ApiBase/project/$ProjectId"
        FileSize = $primary.size
        ApiHashes = $primary.hashes
    }
}

function Write-LockFile {
    param(
        [pscustomobject]$Selection,
        [string]$Sha256
    )
    $resolvedAt = [DateTime]::UtcNow.ToString("yyyy-MM-ddTHH:mm:ss'Z'", [Globalization.CultureInfo]::InvariantCulture)
    $resolvedDate = [DateTime]::UtcNow.ToString('yyyy-MM-dd', [Globalization.CultureInfo]::InvariantCulture)
    $lock = [ordered]@{
        schema_version = 1
        modrinth_project_id = $Selection.ProjectId
        project_slug = $Selection.ProjectSlug
        project_name = $Selection.ProjectName
        version_id = $Selection.VersionId
        version_number = $Selection.VersionNumber
        minecraft_compatibility = @($Selection.Minecraft)
        release_type = $Selection.ReleaseType
        published_timestamp = $Selection.Published
        filename = $Selection.Filename
        download_source_url = $Selection.DownloadUrl
        sha256 = $Sha256.ToLowerInvariant()
        resolved_date = $resolvedDate
        resolved_at = $resolvedAt
        license_identifier = $Selection.LicenseIdentifier
        source = [ordered]@{
            project_api_url = $Selection.ProjectApiUrl
            version_api_url = $Selection.SourceApiUrl
        }
        artifact = [ordered]@{
            filename = $Selection.Filename
            url = $Selection.DownloadUrl
            sha256 = $Sha256.ToLowerInvariant()
            size = $Selection.FileSize
            api_hashes = $Selection.ApiHashes
        }
    }
    $lockTemp = Join-Path -Path $RootDirectory -ChildPath ('.matcha-lock-{0}.tmp' -f [Guid]::NewGuid().ToString('N'))
    $utf8 = New-Object -TypeName System.Text.UTF8Encoding -ArgumentList $false
    try {
        [System.IO.File]::WriteAllText($lockTemp, (($lock | ConvertTo-Json -Depth 8) + [Environment]::NewLine), $utf8)
    }
    catch {
        Fail $ExitLock ("could not write temporary lock metadata: {0}" -f $_.Exception.Message)
    }
    return $lockTemp
}

$exitCode = 0
try {
    $requestedVersion = ''
    $refreshLock = $false
    $forceDownload = $false
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
        if ($token -eq '--version') {
            if ($index + 1 -ge $argumentList.Count -or [string]::IsNullOrWhiteSpace([string]$argumentList[$index + 1]) -or [string]$argumentList[$index + 1].StartsWith('-')) {
                Fail $ExitUsage '--version requires a displayed version number'
            }
            $requestedVersion = [string]$argumentList[$index + 1]
            $index += 2
            continue
        }
        if ($token.StartsWith('--version=', [StringComparison]::Ordinal)) {
            $requestedVersion = $token.Substring('--version='.Length)
            if ([string]::IsNullOrWhiteSpace($requestedVersion)) {
                Fail $ExitUsage '--version requires a displayed version number'
            }
            $index++
            continue
        }
        if ($token -eq '--refresh-lock') {
            $refreshLock = $true
            $index++
            continue
        }
        if ($token -eq '--force') {
            $forceDownload = $true
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
        if ((Test-Path -LiteralPath $LockPath) -and -not (Test-Path -LiteralPath $LockPath -PathType Leaf)) {
            Fail $ExitLock ("lock path exists but is not a regular file: {0}" -f $LockPath)
        }
        $mode = 'resolve'
        if ((Test-Path -LiteralPath $LockPath -PathType Leaf) -and [string]::IsNullOrEmpty($requestedVersion) -and -not $refreshLock) {
            $mode = 'locked'
            $selection = Read-LockSelection
        }
        else {
            $selection = Resolve-Selection $requestedVersion
        }
        $archivePath = Join-Path -Path $VendorDirectory -ChildPath $selection.Filename
        Write-Output ('Matcha Flavoured {0} ({1}) for Minecraft {2}' -f $selection.VersionNumber, $selection.VersionId, $selection.Minecraft)
        Write-Output ('Source: {0}' -f $selection.DownloadUrl)
        Write-Output ('Archive: {0}' -f $archivePath)

        if ((Test-Path -LiteralPath $archivePath) -and -not (Test-Path -LiteralPath $archivePath -PathType Leaf)) {
            Fail $ExitDownload ("archive path exists but is not a regular file: {0}" -f $archivePath)
        }
        if ($mode -eq 'locked' -and (Test-Path -LiteralPath $archivePath -PathType Leaf)) {
            $existingHash = Get-Sha256 $archivePath
            if ($existingHash -ne $selection.ExpectedSha256) {
                if ($dryRun) {
                    Fail $ExitHash ("existing archive SHA-256 mismatch; dry-run would refuse to use it (expected {0}, got {1})" -f $selection.ExpectedSha256, $existingHash)
                }
                try {
                    Remove-Item -LiteralPath $archivePath -Force
                }
                catch {
                    Fail $ExitHash ("existing archive has the wrong SHA-256 and could not be removed: {0}" -f $archivePath)
                }
                Fail $ExitHash ("existing archive SHA-256 mismatch; removed the bad file (expected {0}, got {1})" -f $selection.ExpectedSha256, $existingHash)
            }
        }

        if ($dryRun) {
            if ($mode -eq 'locked' -and (Test-Path -LiteralPath $archivePath -PathType Leaf) -and -not $forceDownload) {
                Write-Output 'DRY-RUN: existing archive matches the lock; no download or lock change.'
            }
            elseif ($mode -eq 'locked') {
                Write-Output 'DRY-RUN: would download the locked URL, verify SHA-256, and replace the archive.'
            }
            else {
                Write-Output 'DRY-RUN: would download this official selection, calculate SHA-256, write the lock, and install the archive.'
            }
        }
        else {
            try {
                New-Item -ItemType Directory -Path $VendorDirectory -Force | Out-Null
            }
            catch {
                Fail $ExitDownload ("could not create archive directory: {0}" -f $VendorDirectory)
            }
            $downloadTemp = Join-Path -Path $VendorDirectory -ChildPath ('.matcha-download-{0}.tmp' -f [Guid]::NewGuid().ToString('N'))
            $TemporaryPaths.Add($downloadTemp)
            try {
                Invoke-WebRequest -Uri $selection.DownloadUrl -OutFile $downloadTemp -Headers @{ 'User-Agent' = $UserAgent } | Out-Null
            }
            catch {
                Fail $ExitDownload ("official Modrinth artifact download failed: {0}: {1}" -f $selection.DownloadUrl, $_.Exception.Message)
            }
            $actualHash = Get-Sha256 $downloadTemp
            if ($mode -eq 'locked' -and $actualHash -ne $selection.ExpectedSha256) {
                Fail $ExitHash ("downloaded archive SHA-256 mismatch (expected {0}, got {1})" -f $selection.ExpectedSha256, $actualHash)
            }
            $lockTemp = $null
            if ($mode -eq 'resolve') {
                $lockTemp = Write-LockFile $selection $actualHash
                $TemporaryPaths.Add($lockTemp)
            }
            try {
                Move-Item -LiteralPath $downloadTemp -Destination $archivePath -Force
            }
            catch {
                Fail $ExitDownload ("could not install the verified archive at {0}: {1}" -f $archivePath, $_.Exception.Message)
            }
            $TemporaryPaths.Remove($downloadTemp) | Out-Null
            if ($mode -eq 'resolve') {
                try {
                    Move-Item -LiteralPath $lockTemp -Destination $LockPath -Force
                }
                catch {
                    Fail $ExitLock ("could not install the new lock at {0}: {1}" -f $LockPath, $_.Exception.Message)
                }
                $TemporaryPaths.Remove($lockTemp) | Out-Null
                Write-Output ('Fetched and locked SHA-256: {0}' -f $actualHash)
            }
            else {
                Write-Output ('Fetched locked SHA-256: {0}' -f $actualHash)
            }
            Write-Output ('Installed: {0}' -f $archivePath)
        }
    }
}
catch {
    [Console]::Error.WriteLine(('fetch-matcha: error: {0}' -f $_.Exception.Message))
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

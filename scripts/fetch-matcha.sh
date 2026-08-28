#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

# Matcha acquisition is deliberately a small, standalone tool. It only resolves
# metadata from Modrinth's official API and downloads the URL returned by that API.
readonly EXIT_USAGE=2
readonly EXIT_DEPENDENCY=3
readonly EXIT_METADATA=4
readonly EXIT_DOWNLOAD=5
readonly EXIT_HASH=6
readonly EXIT_LOCK=7
readonly PROJECT_ID='QI0EmgZ1'
readonly TARGET_MINECRAFT='26.2'
readonly EXPECTED_LICENSE='CC-BY-NC-SA-4.0'
readonly API_BASE='https://api.modrinth.com/v2'
readonly USER_AGENT='Forever-MatchaFetcher/1.0 (private project)'

readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
readonly ROOT_DIR="$(cd -- "${SCRIPT_DIR}/.." && pwd -P)"
readonly LOCK_PATH="${ROOT_DIR}/matcha.lock.json"
readonly VENDOR_DIR="${ROOT_DIR}/vendor/matcha"

project_tmp=''
versions_tmp=''
selection_tmp=''
download_tmp=''
lock_tmp=''

usage() {
    cat <<'USAGE'
Usage: scripts/fetch-matcha.sh [options]

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
USAGE
}

fail() {
    local code="$1"
    shift
    printf 'fetch-matcha: error: %s\n' "$*" >&2
    exit "$code"
}

cleanup() {
    local status="$?"
    local temporary_path=''
    set +e
    for temporary_path in "$project_tmp" "$versions_tmp" "$selection_tmp" "$download_tmp" "$lock_tmp"; do
        if [[ -n "$temporary_path" && -e "$temporary_path" ]]; then
            rm -f -- "$temporary_path"
        fi
    done
    exit "$status"
}
trap cleanup EXIT

require_commands() {
    local command_name=''
    for command_name in curl sha256sum python3 mktemp; do
        if ! command -v "$command_name" >/dev/null 2>&1; then
            fail "$EXIT_DEPENDENCY" "required command not found: $command_name"
        fi
    done
}

fetch_api() {
    local url="$1"
    local output_path="$2"
    if ! curl --fail --location --silent --show-error \
        --retry 3 --retry-delay 1 --connect-timeout 15 --max-time 120 \
        --header "User-Agent: ${USER_AGENT}" \
        --output "$output_path" "$url"; then
        fail "$EXIT_METADATA" "official Modrinth API request failed: $url"
    fi
}

sha256_for() {
    local path="$1"
    local checksum_line=''
    if ! checksum_line="$(sha256sum -- "$path")"; then
        fail "$EXIT_HASH" "could not calculate SHA-256 for $path"
    fi
    printf '%s\n' "${checksum_line%% *}"
}

read_lock() {
    local fields=''
    if ! fields="$(python3 - "$LOCK_PATH" "$PROJECT_ID" "$TARGET_MINECRAFT" "$EXPECTED_LICENSE" <<'PY'
import json
import re
import sys
from urllib.parse import urlparse

path, project_id, target_minecraft, expected_license = sys.argv[1:]
try:
    with open(path, encoding="utf-8") as handle:
        lock = json.load(handle)
except (OSError, json.JSONDecodeError) as error:
    print(f"cannot read JSON lock: {error}", file=sys.stderr)
    raise SystemExit(1)

required = (
    "modrinth_project_id", "version_id", "version_number",
    "minecraft_compatibility", "release_type", "published_timestamp",
    "filename", "download_source_url", "sha256", "license_identifier",
)
missing = [key for key in required if key not in lock]
if missing:
    print("lock is missing required fields: " + ", ".join(missing), file=sys.stderr)
    raise SystemExit(1)

if lock["modrinth_project_id"] != project_id:
    print("lock belongs to a different Modrinth project", file=sys.stderr)
    raise SystemExit(1)
compatibility = lock["minecraft_compatibility"]
if not isinstance(compatibility, list) or target_minecraft not in compatibility:
    print(f"lock is not compatible with Minecraft {target_minecraft}", file=sys.stderr)
    raise SystemExit(1)
if lock["release_type"] != "release":
    print("lock does not identify a stable release", file=sys.stderr)
    raise SystemExit(1)
if lock["license_identifier"] != expected_license:
    print("lock has an unexpected Matcha licence identifier", file=sys.stderr)
    raise SystemExit(1)

filename = lock["filename"]
if not isinstance(filename, str) or not filename or filename in {".", ".."} or "/" in filename or "\\" in filename:
    print("lock contains an unsafe archive filename", file=sys.stderr)
    raise SystemExit(1)
url = lock["download_source_url"]
parsed = urlparse(url) if isinstance(url, str) else None
if parsed is None or parsed.scheme != "https" or parsed.hostname != "cdn.modrinth.com":
    print("lock download URL is not the official Modrinth CDN", file=sys.stderr)
    raise SystemExit(1)
sha256 = lock["sha256"]
if not isinstance(sha256, str) or re.fullmatch(r"[0-9a-fA-F]{64}", sha256) is None:
    print("lock SHA-256 must be 64 hexadecimal characters", file=sys.stderr)
    raise SystemExit(1)

fields = (
    lock["version_id"], str(lock["version_number"]), filename, url,
    sha256.lower(), lock["published_timestamp"], lock["release_type"],
    lock["license_identifier"], lock.get("project_slug", "matcha-flavoured"),
    lock.get("project_name", "Matcha Flavoured"),
)
print("\t".join(fields))
PY
)"; then
        fail "$EXIT_LOCK" "invalid lock metadata in $LOCK_PATH"
    fi
    IFS=$'\t' read -r LOCK_VERSION_ID LOCK_VERSION_NUMBER LOCK_FILENAME \
        LOCK_DOWNLOAD_URL LOCK_SHA256 LOCK_PUBLISHED LOCK_RELEASE_TYPE \
        LOCK_LICENSE LOCK_PROJECT_SLUG LOCK_PROJECT_NAME <<< "$fields"
}

resolve_selection() {
    if ! project_tmp="$(mktemp)"; then
        fail "$EXIT_METADATA" "could not create a temporary project metadata file"
    fi
    if ! versions_tmp="$(mktemp)"; then
        fail "$EXIT_METADATA" "could not create a temporary version metadata file"
    fi
    if ! selection_tmp="$(mktemp)"; then
        fail "$EXIT_METADATA" "could not create a temporary selection file"
    fi

    fetch_api "${API_BASE}/project/${PROJECT_ID}" "$project_tmp"
    fetch_api "${API_BASE}/project/${PROJECT_ID}/version" "$versions_tmp"

    if ! python3 - "$project_tmp" "$versions_tmp" "$PROJECT_ID" "$TARGET_MINECRAFT" "$EXPECTED_LICENSE" "${REQUESTED_VERSION}" > "$selection_tmp" <<'PY'
import json
import sys
from datetime import datetime
from urllib.parse import urlparse

project_path, versions_path, project_id, target_minecraft, expected_license, requested = sys.argv[1:]
try:
    with open(project_path, encoding="utf-8") as handle:
        project = json.load(handle)
    with open(versions_path, encoding="utf-8") as handle:
        versions = json.load(handle)
except (OSError, json.JSONDecodeError) as error:
    print(f"invalid official Modrinth metadata: {error}", file=sys.stderr)
    raise SystemExit(1)

if project.get("id") != project_id:
    print("official API returned an unexpected project ID", file=sys.stderr)
    raise SystemExit(1)
license_data = project.get("license") or {}
if license_data.get("id") != expected_license:
    print("official API reports an unexpected Matcha licence", file=sys.stderr)
    raise SystemExit(1)
if not isinstance(versions, list):
    print("official API returned a non-list version response", file=sys.stderr)
    raise SystemExit(1)

candidates = []
for version in versions:
    if not isinstance(version, dict):
        continue
    if version.get("project_id") != project_id:
        continue
    if target_minecraft not in (version.get("game_versions") or []):
        continue
    if version.get("version_type") != "release" or version.get("status") != "listed":
        continue
    if requested and version.get("version_number") != requested:
        continue
    published = version.get("date_published")
    try:
        published_date = datetime.fromisoformat(published.replace("Z", "+00:00"))
    except (AttributeError, TypeError, ValueError):
        continue
    primary_files = [item for item in (version.get("files") or []) if item.get("primary") is True]
    if len(primary_files) != 1:
        continue
    primary = primary_files[0]
    filename = primary.get("filename")
    url = primary.get("url")
    parsed = urlparse(url) if isinstance(url, str) else None
    if (not isinstance(filename, str) or not filename or filename in {".", ".."}
            or "/" in filename or "\\" in filename):
        continue
    if (parsed is None or parsed.scheme != "https" or parsed.hostname != "cdn.modrinth.com"):
        continue
    candidates.append((published_date, version.get("id", ""), version, primary))

if not candidates:
    requested_text = f" version {requested!r}" if requested else ""
    print(f"no listed release compatible with Minecraft {target_minecraft}{requested_text} was found", file=sys.stderr)
    raise SystemExit(1)

candidates.sort(key=lambda item: (item[0], item[1]), reverse=True)
_, _, selected, primary = candidates[0]
selection = {
    "project_id": project_id,
    "project_slug": project.get("slug", "matcha-flavoured"),
    "project_name": project.get("title", "Matcha Flavoured"),
    "license_identifier": license_data["id"],
    "version_id": selected["id"],
    "version_number": selected["version_number"],
    "minecraft_compatibility": [target_minecraft],
    "release_type": selected["version_type"],
    "published_timestamp": selected["date_published"],
    "filename": primary["filename"],
    "download_source_url": primary["url"],
    "source_api_url": f"https://api.modrinth.com/v2/version/{selected['id']}",
    "project_api_url": f"https://api.modrinth.com/v2/project/{project_id}",
    "file_size": primary.get("size"),
    "api_hashes": primary.get("hashes") or {},
}
print(json.dumps(selection, ensure_ascii=False, separators=(",", ":")))
PY
    then
        fail "$EXIT_METADATA" "could not parse the official Modrinth selection"
    fi
}

read_selection() {
    local fields=''
    if ! fields="$(python3 - "$selection_tmp" <<'PY'
import json
import sys
with open(sys.argv[1], encoding="utf-8") as handle:
    selection = json.load(handle)
print("\t".join((
    selection["project_id"], selection["project_slug"], selection["project_name"],
    selection["license_identifier"], selection["version_id"], selection["version_number"],
    selection["minecraft_compatibility"][0], selection["release_type"],
    selection["published_timestamp"], selection["filename"],
    selection["download_source_url"], selection["source_api_url"],
    selection["project_api_url"], json.dumps(selection.get("file_size")),
    json.dumps(selection.get("api_hashes") or {}, sort_keys=True),
)))
PY
)"; then
        fail "$EXIT_METADATA" "could not read the selected Modrinth version"
    fi
    IFS=$'\t' read -r SELECTED_PROJECT_ID SELECTED_PROJECT_SLUG SELECTED_PROJECT_NAME \
        SELECTED_LICENSE SELECTED_VERSION_ID SELECTED_VERSION_NUMBER \
        SELECTED_MINECRAFT SELECTED_RELEASE_TYPE SELECTED_PUBLISHED \
        SELECTED_FILENAME SELECTED_DOWNLOAD_URL SELECTED_SOURCE_API_URL \
        SELECTED_PROJECT_API_URL SELECTED_FILE_SIZE SELECTED_API_HASHES <<< "$fields"
}

write_lock() {
    local actual_hash="$1"
    local resolved_date=''
    local resolved_at=''
    resolved_date="$(date -u +%F)"
    resolved_at="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
    if ! lock_tmp="$(mktemp "${ROOT_DIR}/.matcha-lock.XXXXXX")"; then
        fail "$EXIT_LOCK" "could not create a temporary lock file"
    fi
    if ! python3 - "$selection_tmp" "$lock_tmp" "$actual_hash" "$resolved_date" "$resolved_at" <<'PY'
import json
import sys

selection_path, output_path, sha256, resolved_date, resolved_at = sys.argv[1:]
with open(selection_path, encoding="utf-8") as handle:
    selection = json.load(handle)
lock = {
    "schema_version": 1,
    "modrinth_project_id": selection["project_id"],
    "project_slug": selection["project_slug"],
    "project_name": selection["project_name"],
    "version_id": selection["version_id"],
    "version_number": selection["version_number"],
    "minecraft_compatibility": selection["minecraft_compatibility"],
    "release_type": selection["release_type"],
    "published_timestamp": selection["published_timestamp"],
    "filename": selection["filename"],
    "download_source_url": selection["download_source_url"],
    "sha256": sha256.lower(),
    "resolved_date": resolved_date,
    "resolved_at": resolved_at,
    "license_identifier": selection["license_identifier"],
    "source": {
        "project_api_url": selection["project_api_url"],
        "version_api_url": selection["source_api_url"],
    },
    "artifact": {
        "filename": selection["filename"],
        "url": selection["download_source_url"],
        "sha256": sha256.lower(),
        "size": selection.get("file_size"),
        "api_hashes": selection.get("api_hashes") or {},
    },
}
with open(output_path, "w", encoding="utf-8", newline="\n") as handle:
    json.dump(lock, handle, indent=2, ensure_ascii=False)
    handle.write("\n")
PY
    then
        fail "$EXIT_LOCK" "could not write temporary lock metadata"
    fi
}

require_commands

REQUESTED_VERSION=''
REFRESH_LOCK=0
FORCE_DOWNLOAD=0
DRY_RUN=0

while (($# > 0)); do
    case "$1" in
        -h|--help)
            usage
            exit 0
            ;;
        --version)
            if (($# < 2)) || [[ -z "$2" ]] || [[ "$2" == -* ]]; then
                usage >&2
                fail "$EXIT_USAGE" '--version requires a displayed version number'
            fi
            REQUESTED_VERSION="$2"
            shift 2
            ;;
        --version=*)
            REQUESTED_VERSION="${1#*=}"
            if [[ -z "$REQUESTED_VERSION" ]]; then
                usage >&2
                fail "$EXIT_USAGE" '--version requires a displayed version number'
            fi
            shift
            ;;
        --refresh-lock)
            REFRESH_LOCK=1
            shift
            ;;
        --force)
            FORCE_DOWNLOAD=1
            shift
            ;;
        --dry-run)
            DRY_RUN=1
            shift
            ;;
        --)
            shift
            if (($# > 0)); then
                usage >&2
                fail "$EXIT_USAGE" 'unexpected arguments after --'
            fi
            ;;
        *)
            usage >&2
            fail "$EXIT_USAGE" "unknown option: $1"
            ;;
    esac
done

if [[ -e "$LOCK_PATH" && ! -f "$LOCK_PATH" ]]; then
    fail "$EXIT_LOCK" "lock path exists but is not a regular file: $LOCK_PATH"
fi

MODE='resolve'
if [[ -f "$LOCK_PATH" && -z "$REQUESTED_VERSION" && "$REFRESH_LOCK" -eq 0 ]]; then
    MODE='locked'
    read_lock
    SELECTED_PROJECT_ID="$PROJECT_ID"
    SELECTED_PROJECT_SLUG="$LOCK_PROJECT_SLUG"
    SELECTED_PROJECT_NAME="$LOCK_PROJECT_NAME"
    SELECTED_LICENSE="$LOCK_LICENSE"
    SELECTED_VERSION_ID="$LOCK_VERSION_ID"
    SELECTED_VERSION_NUMBER="$LOCK_VERSION_NUMBER"
    SELECTED_MINECRAFT="$TARGET_MINECRAFT"
    SELECTED_RELEASE_TYPE="$LOCK_RELEASE_TYPE"
    SELECTED_PUBLISHED="$LOCK_PUBLISHED"
    SELECTED_FILENAME="$LOCK_FILENAME"
    SELECTED_DOWNLOAD_URL="$LOCK_DOWNLOAD_URL"
    EXPECTED_SHA256="$LOCK_SHA256"
else
    resolve_selection
    read_selection
    EXPECTED_SHA256=''
fi

if [[ "$SELECTED_PROJECT_ID" != "$PROJECT_ID" || "$SELECTED_MINECRAFT" != "$TARGET_MINECRAFT" ]]; then
    fail "$EXIT_METADATA" 'selected metadata does not match the pinned project or Minecraft version'
fi
if [[ "$SELECTED_RELEASE_TYPE" != 'release' || "$SELECTED_LICENSE" != "$EXPECTED_LICENSE" ]]; then
    fail "$EXIT_METADATA" 'selected metadata is not the expected release or licence'
fi
if [[ -z "$SELECTED_FILENAME" || "$SELECTED_FILENAME" == '.' || "$SELECTED_FILENAME" == '..' || "$SELECTED_FILENAME" == */* || "$SELECTED_FILENAME" == *'\\'* ]]; then
    fail "$EXIT_METADATA" 'selected metadata contains an unsafe archive filename'
fi
if [[ "$SELECTED_DOWNLOAD_URL" != https://cdn.modrinth.com/* ]]; then
    fail "$EXIT_METADATA" 'selected download URL is not the official Modrinth CDN'
fi

ARCHIVE_PATH="${VENDOR_DIR}/${SELECTED_FILENAME}"
printf 'Matcha Flavoured %s (%s) for Minecraft %s\n' \
    "$SELECTED_VERSION_NUMBER" "$SELECTED_VERSION_ID" "$SELECTED_MINECRAFT"
printf 'Source: %s\n' "$SELECTED_DOWNLOAD_URL"
printf 'Archive: %s\n' "$ARCHIVE_PATH"

if [[ -e "$ARCHIVE_PATH" && ! -f "$ARCHIVE_PATH" ]]; then
    fail "$EXIT_DOWNLOAD" "archive path exists but is not a regular file: $ARCHIVE_PATH"
fi

if [[ -f "$ARCHIVE_PATH" && -n "$EXPECTED_SHA256" ]]; then
    existing_hash="$(sha256_for "$ARCHIVE_PATH")"
    if [[ "$existing_hash" != "$EXPECTED_SHA256" ]]; then
        if [[ "$DRY_RUN" -eq 1 ]]; then
            fail "$EXIT_HASH" "existing archive SHA-256 mismatch; dry-run would refuse to use it (expected $EXPECTED_SHA256, got $existing_hash)"
        fi
        if ! rm -f -- "$ARCHIVE_PATH"; then
            fail "$EXIT_HASH" "existing archive has the wrong SHA-256 and could not be removed: $ARCHIVE_PATH"
        fi
        fail "$EXIT_HASH" "existing archive SHA-256 mismatch; removed the bad file (expected $EXPECTED_SHA256, got $existing_hash)"
    fi
fi

if [[ "$DRY_RUN" -eq 1 ]]; then
    if [[ "$MODE" == 'locked' && -f "$ARCHIVE_PATH" && "$FORCE_DOWNLOAD" -eq 0 ]]; then
        printf '%s\n' 'DRY-RUN: existing archive matches the lock; no download or lock change.'
    elif [[ "$MODE" == 'locked' ]]; then
        printf '%s\n' 'DRY-RUN: would download the locked URL, verify SHA-256, and replace the archive.'
    else
        printf '%s\n' 'DRY-RUN: would download this official selection, calculate SHA-256, write the lock, and install the archive.'
    fi
    exit 0
fi

if ! mkdir -p -- "$VENDOR_DIR"; then
    fail "$EXIT_DOWNLOAD" "could not create archive directory: $VENDOR_DIR"
fi
if ! download_tmp="$(mktemp "${VENDOR_DIR}/.matcha-download.XXXXXX")"; then
    fail "$EXIT_DOWNLOAD" 'could not create a temporary archive file'
fi
if ! curl --fail --location --silent --show-error \
    --retry 3 --retry-delay 1 --connect-timeout 15 --max-time 600 \
    --header "User-Agent: ${USER_AGENT}" \
    --output "$download_tmp" "$SELECTED_DOWNLOAD_URL"; then
    fail "$EXIT_DOWNLOAD" "official Modrinth artifact download failed: $SELECTED_DOWNLOAD_URL"
fi

actual_hash="$(sha256_for "$download_tmp")"
if [[ -n "$EXPECTED_SHA256" && "$actual_hash" != "$EXPECTED_SHA256" ]]; then
    fail "$EXIT_HASH" "downloaded archive SHA-256 mismatch (expected $EXPECTED_SHA256, got $actual_hash)"
fi

if [[ "$MODE" == 'resolve' ]]; then
    write_lock "$actual_hash"
fi
if ! mv -f -- "$download_tmp" "$ARCHIVE_PATH"; then
    fail "$EXIT_DOWNLOAD" "could not install the verified archive at $ARCHIVE_PATH"
fi
download_tmp=''
if [[ "$MODE" == 'resolve' ]]; then
    if ! mv -f -- "$lock_tmp" "$LOCK_PATH"; then
        fail "$EXIT_LOCK" "could not install the new lock at $LOCK_PATH"
    fi
    lock_tmp=''
    printf 'Fetched and locked SHA-256: %s\n' "$actual_hash"
else
    printf 'Fetched locked SHA-256: %s\n' "$actual_hash"
fi
printf 'Installed: %s\n' "$ARCHIVE_PATH"
exit 0

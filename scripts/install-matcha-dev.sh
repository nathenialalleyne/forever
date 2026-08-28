#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

# Installing a datapack changes a world save. This script therefore accepts only
# an explicitly supplied development-world path, rejects common real-world paths,
# and requires either an acknowledgement flag or a marker file in that world.
readonly EXIT_USAGE=2
readonly EXIT_DEPENDENCY=3
readonly EXIT_LOCK=7
readonly EXIT_HASH=6
readonly EXIT_FILESYSTEM=5
readonly PROJECT_ID='QI0EmgZ1'
readonly TARGET_MINECRAFT='26.2'
readonly EXPECTED_LICENSE='CC-BY-NC-SA-4.0'
readonly SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)"
readonly ROOT_DIR="$(cd -- "${SCRIPT_DIR}/.." && pwd -P)"
readonly LOCK_PATH="${ROOT_DIR}/matcha.lock.json"
readonly VENDOR_DIR="${ROOT_DIR}/vendor/matcha"
readonly DEFAULT_RESOURCE_PACK_DIR="${ROOT_DIR}/run/resourcepacks"

temporary_resource=''
temporary_datapack=''

usage() {
    cat <<'USAGE'
Usage: scripts/install-matcha-dev.sh --world <path> [options]

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
USAGE
}

fail() {
    local code="$1"
    shift
    printf 'install-matcha-dev: error: %s\n' "$*" >&2
    exit "$code"
}

cleanup() {
    local status="$?"
    set +e
    if [[ -n "$temporary_resource" && -e "$temporary_resource" ]]; then
        rm -f -- "$temporary_resource"
    fi
    if [[ -n "$temporary_datapack" && -e "$temporary_datapack" ]]; then
        rm -f -- "$temporary_datapack"
    fi
    exit "$status"
}
trap cleanup EXIT

require_commands() {
    local command_name=''
    for command_name in python3 sha256sum mktemp; do
        if ! command -v "$command_name" >/dev/null 2>&1; then
            fail "$EXIT_DEPENDENCY" "required command not found: $command_name"
        fi
    done
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
if not isinstance(lock["minecraft_compatibility"], list) or target_minecraft not in lock["minecraft_compatibility"]:
    print(f"lock is not compatible with Minecraft {target_minecraft}", file=sys.stderr)
    raise SystemExit(1)
if lock["release_type"] != "release" or lock["license_identifier"] != expected_license:
    print("lock is not the expected Matcha release or licence", file=sys.stderr)
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
print("\t".join((filename, sha256.lower())))
PY
)"; then
        fail "$EXIT_LOCK" "invalid lock metadata in $LOCK_PATH"
    fi
    IFS=$'\t' read -r LOCK_FILENAME LOCK_SHA256 <<< "$fields"
}

absolute_existing_directory() {
    local path="$1"
    if [[ ! -d "$path" ]]; then
        fail "$EXIT_USAGE" "development world directory does not exist: $path"
    fi
    if ! cd -- "$path" 2>/dev/null; then
        fail "$EXIT_USAGE" "cannot enter development world directory: $path"
    fi
    pwd -P
}

refuse_obviously_real_world() {
    local path="$1"
    local normalized="${path//\\//}"
    local home_normalized="${HOME:-}/"
    home_normalized="${home_normalized//\\//}"
    local base="${normalized##*/}"
    local lower_base="${base,,}"

    if [[ "$normalized" == '/' || "$normalized" == "$ROOT_DIR" || "$normalized" == "${home_normalized%/}" ]]; then
        fail "$EXIT_USAGE" "refusing an obviously unsafe world path: $path"
    fi
    case "$normalized" in
        */.minecraft/saves/*|*/.minecraft/instances/*/saves/*|*/.var/app/*/.minecraft/saves/*|*/multimc/instances/*/.minecraft/saves/*|*/PrismLauncher/instances/*/.minecraft/saves/*)
            fail "$EXIT_USAGE" "refusing a Minecraft installation or real save path: $path"
            ;;
    esac
    case "$lower_base" in
        world|world_nether|world_the_end|survival|survival_world|survival-world|main|live|prod|production|server|server-world)
            fail "$EXIT_USAGE" "refusing an obviously real world name: $path"
            ;;
    esac
}

hash_file() {
    local path="$1"
    local checksum_line=''
    if ! checksum_line="$(sha256sum -- "$path")"; then
        fail "$EXIT_HASH" "could not calculate SHA-256 for $path"
    fi
    printf '%s\n' "${checksum_line%% *}"
}

require_commands

WORLD_PATH=''
RESOURCE_PACK_DIR="$DEFAULT_RESOURCE_PACK_DIR"
ACK_DEV_WORLD=0
DRY_RUN=0

while (($# > 0)); do
    case "$1" in
        -h|--help)
            usage
            exit 0
            ;;
        --world)
            if (($# < 2)) || [[ -z "$2" ]] || [[ "$2" == -* ]]; then
                usage >&2
                fail "$EXIT_USAGE" '--world requires an existing development world path'
            fi
            WORLD_PATH="$2"
            shift 2
            ;;
        --world=*)
            WORLD_PATH="${1#*=}"
            if [[ -z "$WORLD_PATH" ]]; then
                usage >&2
                fail "$EXIT_USAGE" '--world requires an existing development world path'
            fi
            shift
            ;;
        --resource-pack-dir)
            if (($# < 2)) || [[ -z "$2" ]]; then
                usage >&2
                fail "$EXIT_USAGE" '--resource-pack-dir requires a path'
            fi
            RESOURCE_PACK_DIR="$2"
            shift 2
            ;;
        --resource-pack-dir=*)
            RESOURCE_PACK_DIR="${1#*=}"
            if [[ -z "$RESOURCE_PACK_DIR" ]]; then
                usage >&2
                fail "$EXIT_USAGE" '--resource-pack-dir requires a path'
            fi
            shift
            ;;
        --ack-dev-world)
            ACK_DEV_WORLD=1
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

if [[ -z "$WORLD_PATH" ]]; then
    usage >&2
    fail "$EXIT_USAGE" '--world is required; the script never guesses a world path'
fi
if [[ -e "$LOCK_PATH" && ! -f "$LOCK_PATH" ]]; then
    fail "$EXIT_LOCK" "lock path exists but is not a regular file: $LOCK_PATH"
fi
if [[ ! -f "$LOCK_PATH" ]]; then
    fail "$EXIT_LOCK" "Matcha lock not found: $LOCK_PATH; run fetch-matcha.sh first"
fi
if [[ ! -d "$WORLD_PATH" ]]; then
    fail "$EXIT_USAGE" "development world directory does not exist: $WORLD_PATH"
fi

WORLD_ABS="$(absolute_existing_directory "$WORLD_PATH")"
refuse_obviously_real_world "$WORLD_ABS"
MARKER_PATH="${WORLD_ABS}/.forever-dev-world"
if [[ "$ACK_DEV_WORLD" -ne 1 && ! -f "$MARKER_PATH" ]]; then
    fail "$EXIT_USAGE" "world is not acknowledged as disposable development data; pass --ack-dev-world or create $MARKER_PATH"
fi

read_lock
ARCHIVE_PATH="${VENDOR_DIR}/${LOCK_FILENAME}"
if [[ ! -f "$ARCHIVE_PATH" ]]; then
    fail "$EXIT_FILESYSTEM" "locked Matcha archive not found: $ARCHIVE_PATH; run fetch-matcha.sh first"
fi
ACTUAL_SHA256="$(hash_file "$ARCHIVE_PATH")"
if [[ "$ACTUAL_SHA256" != "$LOCK_SHA256" ]]; then
    fail "$EXIT_HASH" "locked Matcha archive SHA-256 mismatch (expected $LOCK_SHA256, got $ACTUAL_SHA256)"
fi

case "$RESOURCE_PACK_DIR" in
    /*) RESOURCE_PACK_ABS="$RESOURCE_PACK_DIR" ;;
    *) RESOURCE_PACK_ABS="$(pwd -P)/$RESOURCE_PACK_DIR" ;;
esac
RESOURCE_DEST="${RESOURCE_PACK_ABS}/${LOCK_FILENAME}"
DATAPACK_DIR="${WORLD_ABS}/datapacks"
DATAPACK_DEST="${DATAPACK_DIR}/${LOCK_FILENAME}"

if [[ "$DRY_RUN" -eq 1 ]]; then
    printf 'DRY-RUN: would copy %s -> %s\n' "$ARCHIVE_PATH" "$RESOURCE_DEST"
    printf 'DRY-RUN: would copy %s -> %s\n' "$ARCHIVE_PATH" "$DATAPACK_DEST"
    exit 0
fi

if ! mkdir -p -- "$RESOURCE_PACK_ABS" "$DATAPACK_DIR"; then
    fail "$EXIT_FILESYSTEM" 'could not create one or more development destination directories'
fi
if ! temporary_resource="$(mktemp "${RESOURCE_PACK_ABS}/.matcha-install.XXXXXX")"; then
    fail "$EXIT_FILESYSTEM" "could not create a temporary resource-pack destination in $RESOURCE_PACK_ABS"
fi
if ! temporary_datapack="$(mktemp "${DATAPACK_DIR}/.matcha-install.XXXXXX")"; then
    fail "$EXIT_FILESYSTEM" "could not create a temporary datapack destination in $DATAPACK_DIR"
fi
if ! cp -- "$ARCHIVE_PATH" "$temporary_resource"; then
    fail "$EXIT_FILESYSTEM" "could not copy Matcha to $RESOURCE_DEST"
fi
if ! cp -- "$ARCHIVE_PATH" "$temporary_datapack"; then
    fail "$EXIT_FILESYSTEM" "could not copy Matcha to $DATAPACK_DEST"
fi
if ! mv -f -- "$temporary_resource" "$RESOURCE_DEST"; then
    fail "$EXIT_FILESYSTEM" "could not install Matcha at $RESOURCE_DEST"
fi
temporary_resource=''
if ! mv -f -- "$temporary_datapack" "$DATAPACK_DEST"; then
    fail "$EXIT_FILESYSTEM" "could not install Matcha at $DATAPACK_DEST"
fi
temporary_datapack=''
printf 'Copied %s -> %s\n' "$ARCHIVE_PATH" "$RESOURCE_DEST"
printf 'Copied %s -> %s\n' "$ARCHIVE_PATH" "$DATAPACK_DEST"
exit 0

#!/usr/bin/env bash
# Export the Many Roads Home modpack to a Modrinth .mrpack under dist/.
#
# The export references every third-party file by URL and hash rather than bundling
# it. That keeps redistributable third-party binaries out of this repository, which
# matters because at least one selected dependency is All Rights Reserved.
set -euo pipefail
IFS=$'\n\t'

readonly ROOT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd -P)"
readonly PACK_DIR="${ROOT_DIR}/pack"
readonly DIST_DIR="${ROOT_DIR}/dist"
readonly CONFIG="${ROOT_DIR}/.config-packwiz.toml"

if ! command -v packwiz >/dev/null 2>&1; then
    echo "build-pack: packwiz is not on PATH." >&2
    echo "  Install it with: go install github.com/packwiz/packwiz@latest" >&2
    echo "  Then add \$(go env GOPATH)/bin to PATH. See docs/modpack-architecture.md." >&2
    exit 3
fi

version="$(grep -E '^version *= *' "${PACK_DIR}/pack.toml" | head -1 | sed 's/.*"\(.*\)"/\1/')"
if [[ -z "${version}" ]]; then
    echo "build-pack: could not read version from pack/pack.toml" >&2
    exit 4
fi

mkdir -p "${DIST_DIR}"
readonly OUT="${DIST_DIR}/many-roads-home-${version}.mrpack"

cd "${PACK_DIR}"
# Refresh first so the index hash matches the metadata actually on disk. Exporting a
# stale index produces a pack that installs the wrong files.
packwiz --config "${CONFIG}" refresh
packwiz --config "${CONFIG}" modrinth export -o "${OUT}"

echo "build-pack: wrote ${OUT}"

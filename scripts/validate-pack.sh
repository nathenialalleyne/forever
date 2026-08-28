#!/usr/bin/env bash
# Validate the Many Roads Home pack metadata without needing private credentials.
#
# This is the check CI runs. It deliberately does not download mods: it verifies that
# the metadata is internally consistent, that pinned versions are present, and that no
# third-party binary has been committed by accident.
set -euo pipefail
IFS=$'\n\t'

readonly ROOT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd -P)"
readonly PACK_DIR="${ROOT_DIR}/pack"
readonly CONFIG="${ROOT_DIR}/.config-packwiz.toml"
failures=0

fail() { echo "validate-pack: FAIL: $*" >&2; failures=$((failures + 1)); }
pass() { echo "validate-pack: ok: $*"; }

[[ -f "${PACK_DIR}/pack.toml" ]] || { fail "pack/pack.toml is missing"; exit 1; }

# 1. No third-party binaries may be committed. Licensing and repository size both
#    depend on this staying true.
# Gradle wrapper JARs are a legitimate, checked-in part of the build toolchain in
# every Gradle project, so they are excluded by path rather than by extension.
binaries="$(git -C "${ROOT_DIR}" ls-files -- '*.jar' '*.mrpack' 'pack/**/*.zip' | grep -v 'gradle/wrapper/gradle-wrapper.jar' || true)"
if [[ -n "${binaries}" ]]; then
    fail "third-party binaries are committed:"
    printf '  %s\n' ${binaries} >&2
else
    pass "no third-party binaries committed"
fi

# 2. Every pinned dependency must name an exact Modrinth version, never a range.
missing_pin=0
while IFS= read -r meta; do
    grep -q '^version = ' "${meta}" || { fail "no pinned version in ${meta#${ROOT_DIR}/}"; missing_pin=1; }
    grep -q '^hash = ' "${meta}" || { fail "no download hash in ${meta#${ROOT_DIR}/}"; missing_pin=1; }
done < <(find "${PACK_DIR}" -name '*.pw.toml')
[[ ${missing_pin} -eq 0 ]] && pass "every dependency pins an exact version and hash"

# 3. The Minecraft and loader versions must be the approved baseline. A silent
#    version drift is the failure mode this project most wants to prevent.
grep -q 'minecraft = "26.2"' "${PACK_DIR}/pack.toml" || fail "pack.toml is not pinned to Minecraft 26.2"
grep -q 'fabric = "0.19.3"' "${PACK_DIR}/pack.toml" || fail "pack.toml is not pinned to Fabric loader 0.19.3"
pass "baseline versions checked"

# 4. Global Packs and packwiz must agree on the datapack folder, or Matcha silently
#    fails to load.
if [[ -f "${PACK_DIR}/defaultconfigs/global_packs.toml" ]]; then
    folder="$(grep -E '^datapack-folder' "${CONFIG}" | sed 's/.*"\(.*\)"/\1/')"
    grep -q "${folder}/Matcha_Flavoured" "${PACK_DIR}/defaultconfigs/global_packs.toml" \
        || fail "global_packs.toml does not reference the packwiz datapack folder '${folder}'"
    pass "Global Packs and packwiz agree on the datapack folder"
fi

# 5. packwiz's own consistency check, when the tool is available.
if command -v packwiz >/dev/null 2>&1; then
    (cd "${PACK_DIR}" && packwiz --config "${CONFIG}" refresh >/dev/null) \
        && pass "packwiz refresh succeeded" || fail "packwiz refresh failed"
else
    echo "validate-pack: note: packwiz not on PATH, skipped index refresh"
fi

if [[ ${failures} -gt 0 ]]; then
    echo "validate-pack: ${failures} check(s) failed" >&2
    exit 1
fi
echo "validate-pack: all checks passed"

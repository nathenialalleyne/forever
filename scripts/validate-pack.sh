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

# 5. The exported .mrpack must satisfy the published Modrinth format: every file needs
#    BOTH sha1 and sha512, an https download, and a safe relative path. A launcher
#    rejects or mis-installs a pack that violates this, and the failure looks like
#    corruption rather than a metadata bug.
mrpack="$(ls -1 "${ROOT_DIR}"/dist/*.mrpack 2>/dev/null | head -1 || true)"
if [[ -n "${mrpack}" ]]; then
    python3 - "${mrpack}" <<'PY' && pass "exported .mrpack matches the Modrinth format" || fail "exported .mrpack violates the Modrinth format"
import json,sys,zipfile
idx=json.loads(zipfile.ZipFile(sys.argv[1]).read('modrinth.index.json'))
bad=[]
if idx.get('formatVersion')!=1: bad.append('formatVersion')
if 'minecraft' not in idx.get('dependencies',{}): bad.append('dependencies.minecraft')
for f in idx.get('files',[]):
    h=f.get('hashes',{})
    if 'sha1' not in h or 'sha512' not in h: bad.append(f"{f.get('path')}: needs sha1+sha512")
    p=f.get('path','')
    if p.startswith('/') or '..' in p: bad.append(f'unsafe path {p}')
    if any(not u.startswith('https://') for u in f.get('downloads',[])): bad.append(f'{p}: non-https')
sys.exit(1 if bad else 0)
PY
else
    echo "validate-pack: note: no .mrpack in dist/, skipped format check (run build-pack.sh)"
fi

# 6. The shipped Global Packs config must declare the schema version the mod expects.
#    A stale config_version is silently migrated on first launch, which rewrites the
#    file and discards the comments explaining why it is configured this way.
if [[ -f "${PACK_DIR}/defaultconfigs/global_packs.toml" ]]; then
    grep -qE '^config_version = 4$' "${PACK_DIR}/defaultconfigs/global_packs.toml" \
        && pass "Global Packs config_version matches the pinned build" \
        || fail "global_packs.toml config_version does not match the pinned Global Packs build (expected 4)"
fi

# 7. The index must match the files it describes, and pack.toml must match the index.
#    packwiz refresh normally maintains this, but packwiz is frequently absent (it has
#    no tagged releases, so it is not installed by default and CI never has it). Without
#    this check, editing any pinned file leaves a stale index and every other check still
#    passes, which is exactly what happened when a config comment was added.
if python3 - "${PACK_DIR}" <<'PYCHECK'
import hashlib, re, sys, tomllib
from pathlib import Path

pack = Path(sys.argv[1])
index_text = (pack / "index.toml").read_text(encoding="utf-8")
problems = []

listed = set()
for entry in re.finditer(r'file = "([^"]+)"\nhash = "([0-9a-f]+)"', index_text):
    name, expected = entry.group(1), entry.group(2)
    listed.add(name)
    target = pack / name
    if not target.is_file():
        problems.append(f"index lists a missing file: {name}")
    elif hashlib.sha256(target.read_bytes()).hexdigest() != expected:
        problems.append(f"index hash is stale for: {name}")

# A file present but unlisted is just as broken: it would not be exported. The
# exclusions mirror what packwiz itself omits, confirmed by diffing this check
# against a real `packwiz refresh`: the index never lists itself, pack.toml, or
# .packwizignore, and never lists a path .packwizignore excludes. Guessing these
# would make the check reject packwiz's own correct output, which is worse than
# having no check at all.
ignore_patterns = []
ignore_file = pack / ".packwizignore"
if ignore_file.is_file():
    for line in ignore_file.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#"):
            ignore_patterns.append(line)


def is_ignored(name: str) -> bool:
    from fnmatch import fnmatch

    for pattern in ignore_patterns:
        if pattern.endswith("/"):
            if name.startswith(pattern) or f"/{pattern}" in f"/{name}":
                return True
        elif fnmatch(name, pattern) or fnmatch(Path(name).name, pattern):
            return True
    return False


for candidate in sorted(pack.rglob("*")):
    if not candidate.is_file():
        continue
    name = candidate.relative_to(pack).as_posix()
    if name in ("index.toml", "pack.toml", ".packwizignore") or name in listed:
        continue
    if is_ignored(name):
        continue
    problems.append(f"file is not listed in the index: {name}")

declared = tomllib.loads((pack / "pack.toml").read_text(encoding="utf-8"))["index"]["hash"]
actual = hashlib.sha256((pack / "index.toml").read_bytes()).hexdigest()
if declared != actual:
    problems.append("pack.toml index hash does not match index.toml")

for problem in problems:
    print(f"  {problem}", file=sys.stderr)
sys.exit(1 if problems else 0)
PYCHECK
then
    pass "index matches the pinned files and pack.toml"
else
    fail "the pack index is out of date; run packwiz refresh or regenerate it"
fi

# 7. packwiz's own consistency check, when the tool is available.
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

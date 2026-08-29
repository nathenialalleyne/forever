#!/usr/bin/env python3
"""Verify that every dependency pinned in the pack really resolves to the pinned bytes.

`validate-pack.sh` checks that the metadata is internally consistent. That is a different
question from whether the pack can actually be installed: a pin can be perfectly
well-formed and still point at a URL that 404s, or at a file whose contents have changed.
Only downloading the bytes and hashing them answers that.

This is the closest thing to an end-to-end test the pack has. It is what stands between
"the metadata looks right" and "a player can install this".

Network access is required, so this is a local and release-time check rather than part of
the offline CI run. It downloads to memory and writes nothing: third-party binaries must
never land in the working tree.

Usage:
    python3 scripts/verify-pack-downloads.py [--mrpack dist/<file>.mrpack]

With no argument it reads the pinned metadata under `pack/` directly, so it works before
any export exists.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import sys
import urllib.error
import urllib.request
import zipfile
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
PACK_DIR = REPO_ROOT / "pack"
TIMEOUT_SECONDS = 120

try:  # Python 3.11+
    import tomllib
except ModuleNotFoundError:  # pragma: no cover
    tomllib = None


def pins_from_mrpack(path: Path) -> list[dict]:
    """Read pinned files from an exported .mrpack index."""
    with zipfile.ZipFile(path) as archive:
        index = json.loads(archive.read("modrinth.index.json"))
    return [
        {
            "name": entry["path"],
            "url": entry["downloads"][0],
            "size": entry.get("fileSize"),
            "hashes": entry.get("hashes", {}),
        }
        for entry in index.get("files", [])
    ]


def pins_from_pack_dir() -> list[dict]:
    """Read pinned files from the packwiz metadata that is the source of truth."""
    if tomllib is None:
        raise SystemExit("verify-pack-downloads: Python 3.11+ is required to read pack metadata")

    pins: list[dict] = []
    for toml_path in sorted(PACK_DIR.rglob("*.pw.toml")):
        data = tomllib.loads(toml_path.read_text(encoding="utf-8"))
        download = data.get("download", {})
        url = download.get("url")
        if not url:
            # A pin with no URL is a metadata error, not something to skip silently.
            print(f"verify-pack-downloads: FAIL: {toml_path.name} has no download URL", file=sys.stderr)
            pins.append({"name": toml_path.name, "url": None, "size": None, "hashes": {}})
            continue
        hash_format = download.get("hash-format", "")
        pins.append(
            {
                "name": data.get("filename", toml_path.name),
                "url": url,
                "size": None,
                "hashes": {hash_format: download["hash"]} if download.get("hash") else {},
            }
        )
    return pins


def verify(pin: dict) -> tuple[bool, str]:
    """Download one pinned file and compare its bytes against every declared hash."""
    if not pin["url"]:
        return False, "no download URL"

    try:
        with urllib.request.urlopen(pin["url"], timeout=TIMEOUT_SECONDS) as response:
            payload = response.read()
    except urllib.error.HTTPError as error:
        return False, f"HTTP {error.code}"
    except Exception as error:  # noqa: BLE001 - report any transport failure verbatim
        return False, f"download failed: {error}"

    if pin["size"] is not None and len(payload) != pin["size"]:
        return False, f"size {len(payload)} != declared {pin['size']}"

    if not pin["hashes"]:
        # Worth failing on: an unpinned hash means the pack cannot detect a changed file.
        return False, "no hash declared"

    for algorithm, expected in pin["hashes"].items():
        if not algorithm:
            return False, "hash declared with no algorithm"
        try:
            actual = hashlib.new(algorithm, payload).hexdigest()
        except ValueError:
            return False, f"unsupported hash algorithm: {algorithm}"
        if actual != expected:
            return False, f"{algorithm} mismatch"

    return True, f"{len(payload)} bytes, {', '.join(sorted(pin['hashes']))} verified"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--mrpack", type=Path, help="verify an exported .mrpack instead of pack/")
    args = parser.parse_args()

    if args.mrpack:
        if not args.mrpack.is_file():
            print(f"verify-pack-downloads: no such file: {args.mrpack}", file=sys.stderr)
            return 1
        pins = pins_from_mrpack(args.mrpack)
        source = str(args.mrpack)
    else:
        pins = pins_from_pack_dir()
        source = "pack/"

    if not pins:
        print(f"verify-pack-downloads: FAIL: no pinned files found in {source}", file=sys.stderr)
        return 1

    print(f"verify-pack-downloads: checking {len(pins)} pinned files from {source}")
    failures = 0
    for pin in pins:
        passed, detail = verify(pin)
        if passed:
            print(f"  ok   {pin['name']}: {detail}")
        else:
            failures += 1
            print(f"  FAIL {pin['name']}: {detail}", file=sys.stderr)

    if failures:
        print(
            f"verify-pack-downloads: {failures} of {len(pins)} pinned files failed. "
            "A pin that does not resolve to the pinned bytes means the pack cannot be "
            "installed as published.",
            file=sys.stderr,
        )
        return 1

    print(f"verify-pack-downloads: all {len(pins)} pinned files resolve to the pinned bytes")
    return 0


if __name__ == "__main__":
    sys.exit(main())

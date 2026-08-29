#!/usr/bin/env python3
"""Fail when the AGENTS.md repository map and the tracked tree disagree.

AGENTS.md is the primary instruction file for agents working on this project, and its
repository map is how a new agent decides where code belongs. A stale map is worse than
no map: it quietly teaches the wrong layout. This check found four tracked directories
missing from the map, including `companion/`, which exists precisely to mark a boundary.

The check runs in both directions, because each failure mode is real:

* A tracked path with no map entry means work landed somewhere undocumented.
* A map entry that matches nothing tracked means the map describes a layout that no
  longer exists, which is how "create empty packages to fill the map" starts.

Deliberately ignored paths are listed explicitly rather than pattern-matched, so adding
an exemption is a visible decision rather than a silent side effect.
"""

from __future__ import annotations

import re
import subprocess
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
AGENTS_FILE = REPO_ROOT / "AGENTS.md"

# Entries the map may declare without a matching tracked path. These are real
# directories that are deliberately git-ignored, so requiring a tracked file would force
# us to commit exactly the artefacts the ignore rules exist to keep out.
UNTRACKED_BY_DESIGN = {
    "dist",  # exported .mrpack artefacts, produced by scripts/build-pack.sh
    "vendor",  # locally acquired third-party archives, never committed
}

# Map rows written as a glob or placeholder rather than a literal path.
PLACEHOLDER_PATTERN = re.compile(r"[<>*]")


def tracked_top_level() -> set[str]:
    """The first path segment of every file Git tracks."""
    result = subprocess.run(
        ["git", "ls-files"],
        cwd=REPO_ROOT,
        capture_output=True,
        text=True,
        check=True,
    )
    return {line.split("/")[0] for line in result.stdout.splitlines() if line.strip()}


def declared_entries() -> set[str]:
    """Top-level paths named in the AGENTS.md repository-map table.

    A single row may name several related files, such as the Gradle build files, so every
    backticked path in the row's first column counts.
    """
    declared: set[str] = set()
    for line in AGENTS_FILE.read_text(encoding="utf-8").splitlines():
        row = re.match(r"^\| (`[^|]+`) \|", line)
        if not row:
            continue
        for entry in re.findall(r"`([^`]+)`", row.group(1)):
            entry = entry.strip()
            if PLACEHOLDER_PATTERN.search(entry):
                # e.g. `generated/matcha/<version>/`: check the concrete prefix instead.
                entry = entry.split("<")[0].split("*")[0]
            top = entry.strip("/").split("/")[0]
            if top:
                declared.add(top)
    return declared


def main() -> int:
    tracked = tracked_top_level()
    declared = declared_entries()

    undocumented = sorted(tracked - declared)
    phantom = sorted(entry for entry in declared - tracked if entry not in UNTRACKED_BY_DESIGN)

    failures = 0

    if undocumented:
        failures += 1
        print("check-repo-map: FAIL: tracked paths missing from the AGENTS.md map:", file=sys.stderr)
        for entry in undocumented:
            print(f"  {entry}", file=sys.stderr)
        print(
            "  Add a row describing what the path is for, or remove the files if they do "
            "not belong.",
            file=sys.stderr,
        )

    if phantom:
        failures += 1
        print("check-repo-map: FAIL: map entries that match nothing tracked:", file=sys.stderr)
        for entry in phantom:
            print(f"  {entry}", file=sys.stderr)
        print(
            "  Remove the row, or add it to UNTRACKED_BY_DESIGN if it is deliberately "
            "git-ignored. Do not create empty directories to satisfy this check.",
            file=sys.stderr,
        )

    if failures:
        return 1

    print(f"check-repo-map: ok: {len(tracked)} tracked top-level paths all documented")
    return 0


if __name__ == "__main__":
    sys.exit(main())

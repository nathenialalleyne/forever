#!/usr/bin/env python3
"""Re-verify the mod candidate matrix against the Modrinth API.

Research goes stale. A project that had no 26.2 build last month may have one now, and a
row that claimed 26.2 support may have been recorded from a snapshot or a misread page.
This tool re-queries the authoritative source for every row that carries a Modrinth
project ID and reports three things: rows whose 26.2 support disagrees with the API, rows
whose licence has changed, and rows that could not be checked.

It deliberately does not rewrite the CSV. A disagreement needs a human to decide whether
the row was wrong or the project moved, and silently editing research would destroy the
audit trail that makes the matrix trustworthy.

Usage:
    python3 tools/mod-research/verify-candidates.py [--csv PATH] [--quiet]

Exit codes:
    0  every checkable row agrees with the API
    1  at least one row disagrees
    2  usage or input error
"""
from __future__ import annotations

import argparse
import csv
import json
import re
import sys
import time
import urllib.error
import urllib.request

API = "https://api.modrinth.com/v2/project/{}"
TARGET_MC = "26.2"
# Modrinth asks API consumers to identify themselves so abuse can be traced to a project
# rather than to an anonymous script.
USER_AGENT = "many-roads-home/mod-research (+https://github.com/nathenialalleyne/forever)"
PROJECT_ID = re.compile(r"^[A-Za-z0-9]{8}$")


def fetch(project_id: str, retries: int = 3) -> dict:
    request = urllib.request.Request(API.format(project_id), headers={"User-Agent": USER_AGENT})
    for attempt in range(retries):
        try:
            with urllib.request.urlopen(request, timeout=30) as response:
                return json.load(response)
        except urllib.error.HTTPError as error:
            if error.code == 429 and attempt < retries - 1:
                # Respect rate limiting rather than hammering a public API.
                time.sleep(2 ** attempt)
                continue
            raise
    raise RuntimeError(f"could not fetch {project_id}")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--csv", default="docs/mod-research/candidate-matrix.csv")
    parser.add_argument("--quiet", action="store_true", help="only report disagreements")
    args = parser.parse_args()

    try:
        with open(args.csv, encoding="utf-8") as handle:
            rows = list(csv.DictReader(handle))
    except OSError as error:
        print(f"verify-candidates: cannot read {args.csv}: {error}", file=sys.stderr)
        return 2

    if not rows:
        print(f"verify-candidates: {args.csv} has no rows", file=sys.stderr)
        return 2

    disagreements: list[str] = []
    skipped = 0
    checked = 0

    for row in rows:
        name = row.get("project_name", "?")
        project_id = (row.get("project_id") or "").strip()
        if not PROJECT_ID.match(project_id):
            skipped += 1
            if not args.quiet:
                print(f"  SKIP     {name[:34]:36} no Modrinth project ID recorded")
            continue

        try:
            project = fetch(project_id)
        except Exception as error:  # noqa: BLE001 - reported, not swallowed
            skipped += 1
            print(f"  ERROR    {name[:34]:36} {error}")
            continue

        checked += 1
        api_has_target = TARGET_MC in project.get("game_versions", [])
        csv_claims_target = TARGET_MC in (row.get("minecraft_version") or "")
        api_licence = (project.get("license") or {}).get("id", "")
        csv_licence = row.get("license", "")

        if api_has_target != csv_claims_target:
            disagreements.append(
                f"{name}: API says {TARGET_MC} support is {api_has_target}, "
                f"matrix records {row.get('minecraft_version')!r}"
            )
            print(f"  DISAGREE {name[:34]:36} api_{TARGET_MC}={api_has_target} csv={row.get('minecraft_version')!r}")
        elif api_licence and csv_licence and api_licence not in csv_licence:
            disagreements.append(f"{name}: licence changed to {api_licence}, matrix records {csv_licence!r}")
            print(f"  LICENCE  {name[:34]:36} api={api_licence} csv={csv_licence!r}")
        elif not args.quiet:
            print(f"  ok       {name[:34]:36} {TARGET_MC}={api_has_target} {api_licence}")

    print(f"\nchecked {checked}, skipped {skipped}, disagreements {len(disagreements)}")
    for line in disagreements:
        print(f"  - {line}")
    return 1 if disagreements else 0


if __name__ == "__main__":
    sys.exit(main())

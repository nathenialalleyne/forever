# Mod candidate research

Research snapshot: **2026-08-28 UTC**.

This matrix records current candidates for Minecraft 26.2 on Fabric. It is a screening record for the Many Roads Home modpack-first pivot, not proof that a complete assembled instance is compatible.

## Method

- Modrinth's v2 project endpoint supplied project identity, environment, licence, description and links.
- The exact Modrinth version query for `game_versions=["26.2"]` and `loaders=["fabric"]` supplied the release, version ID, release type, date, dependencies and environment where a 26.2 Fabric record existed.
- When no 26.2 Fabric record existed, the matrix records the newest Fabric version actually observed. It does not substitute a similarly named fork or infer support from downloads.
- Where `source_url` existed, the linked source repository and its commit and issue endpoints were checked for maintenance signals. A missing, inaccessible or stale source signal is recorded rather than treated as proof of maintenance.
- Licences are copied from the authoritative Modrinth project record or its linked licence text. All-Rights-Reserved, Polyform, custom and Creative Commons restrictions are intentionally visible.
- Alternatives were searched by role. The matrix includes more than one plausible option for each role, including Simple Resource Loader as a pack-loader alternative and Sparse Structures as a structure-spacing control.
- Matcha overlap is a compatibility-screening judgement. Exact Matcha mechanic classification remains pending the official FVR-014 audit and is marked as potential or unverified where appropriate.

The `decision` field is a research disposition only. `ADOPT_BASELINE` follows the current foundation direction where evidence and scope are sufficient. `PILOT` means the candidate needs assembled-instance validation. `COMPATIBILITY_SPIKE` marks a known authority, save, balance, licence or interaction risk. `DEFER` records a viable future option without current 26.2 evidence or with an explicit release gate. `REJECT` records a poor fit or a clearly non-matching loader/version boundary.

Global Packs remains the named 26.2 loader selected by ADR 0027. The matrix records its source and licence risks and does not silently replace that decision with an alternative.

## Re-verification

Run from the repository root with network access. Use the same snapshot date and record any changed release or source evidence:

```sh
curl -fsSL 'https://api.modrinth.com/v2/project/<slug-or-id>'
curl -fsSL 'https://api.modrinth.com/v2/project/<id>/version?game_versions=%5B%2226.2%22%5D&loaders=%5B%22fabric%22%5D'
# For a no-26.2 row, inspect all Fabric releases instead:
curl -fsSL 'https://api.modrinth.com/v2/project/<id>/version?loaders=%5B%22fabric%22%5D'
# When source_url is a GitHub repository:
curl -fsSL 'https://api.github.com/repos/<owner>/<repo>/commits?per_page=10'
curl -fsSL 'https://api.github.com/repos/<owner>/<repo>/issues?state=all&per_page=5'
```

Then verify the matrix still parses as RFC4180 CSV and that its header remains unchanged:

```sh
python3 -c "import csv;rs=list(csv.DictReader(open('docs/mod-research/candidate-matrix.csv')));print(len(rs),'rows');assert all(len(r)==33 for r in rs)"
```

Do not treat a newer upstream version as an automatic update. Recheck the Packwiz lock, licence terms, removal consequences and disposable-world validation requirements before changing the pack composition.

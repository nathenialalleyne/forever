# Mod research tooling

Developer-only tooling for keeping `docs/mod-research/candidate-matrix.csv` honest.

## Why this exists

Mod research goes stale faster than almost any other document in the project. Minecraft
26.2 is new, so today's "no 26.2 release" becomes tomorrow's adoption candidate, and a
row recorded from a misread page can sit unchallenged for months while decisions are
built on it.

The matrix is only useful if its claims can be rechecked cheaply against the
authoritative source rather than trusted because someone wrote them down.

## verify-candidates.py

Re-queries the Modrinth API for every row carrying a project ID and reports rows whose
26.2 support or licence disagrees with the recorded research.

```sh
python3 tools/mod-research/verify-candidates.py           # full report
python3 tools/mod-research/verify-candidates.py --quiet   # disagreements only
```

Exit codes: `0` everything agrees, `1` at least one disagreement, `2` usage error.

It deliberately **does not rewrite the CSV**. A disagreement needs a human to decide
whether the row was wrong or the project genuinely moved, and silently editing research
would destroy the audit trail that makes the matrix worth consulting.

Last full run: 57 rows checked, 1 skipped (Packwiz, which is not a Modrinth project),
0 disagreements.

## Dependencies

Standard library only. No third-party packages, so it runs anywhere Python 3 is
available and adds nothing to the mod runtime.

The script identifies itself with a User-Agent naming the project, which is what Modrinth
asks API consumers to do, and it backs off on HTTP 429 rather than hammering a public
service.

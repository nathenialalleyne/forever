# Session report: compatibility-evidence phase complete

Written 2026-08-29. AGENTS.md requires a completion report that lets the next agent resume
without guessing. This covers the run that finished the compatibility labs and validated
the pack end to end.

## What state the project is in

The **compatibility-evidence phase is complete**. All twelve scheduled labs ran, plus three
that evidence demanded were added. `PACK-01` is evidence-complete apart from one check that
needs a human.

Nothing is blocked on further investigation. The two open items both need the project owner.

## Tickets

| Ticket | State |
|---|---|
| `LAB-01` … `LAB-12` | **Done.** Results in `labs/results/`. |
| `LAB-CLIENT`, `LAB-CLIENT2`, `LAB-LICENCE` | **Done.** Added mid-run; see below. |
| `PACK-01` | Evidence recorded in the ticket. Only a real-play world smoke test remains, and it needs a human. |
| `MRH-011` | **New, and the most important open ticket.** Packaging only, no code change, fix already verified. Blocked on the rights-model decision. |

## The finding that matters most

A pack assembled exactly as a player would install it has **two silent failure paths**:

- Matcha missing: the server starts normally at 1585 recipes instead of 2346, saying nothing.
- Matcha **substituted**: a structurally valid decoy with the correct filename produces the
  same degraded world, again silently. This is worse, because the file is present and
  correctly named, so nothing looks wrong.

MRH-010 exists to prevent exactly this and works, with unit and GameTest coverage. But it
lives in the companion mod and **the companion mod is not in `pack/`**, so it never runs for
a player. Unit tests, GameTests and metadata validation were all green throughout; only
assembling the real artefact and breaking it exposed the gap.

Both the configuration-only mitigation and the fix were then tested rather than assumed:

- `log_pack_ids = true` does **not** help. Healthy and decoy runs produce an identical
  listing block. Configuration cannot detect this.
- Adding the companion build **does** fix both paths, producing a framed actionable error
  banner without blocking startup.

## Decisions the labs reached

Restraint dominated. Five labs concluded *adopt nothing*, each correctly:

- Matcha already owns food, hunger, healing and food effects (LAB-05).
- Vanilla already handles local item transport (LAB-08) and travel (LAB-09).
- No structure provider is safe: generated chunks are permanent (LAB-10).
- No guidebook or advancement-noise problem exists, so no journal or reward system is
  needed (LAB-01, LAB-11).

`LAB-12`, the capstone, **authorised no custom code at all** and found exactly one
conditionally justified gap: a server-authoritative local storage index, following from
LAB-07 overturning the assumption that an existing storage mod could fill that role.

Six mods are adopted from more than thirty surveyed. Twenty-four of thirty-four systems in
the ownership matrix now carry a lab-backed decision.

## Corrections made to existing work

Several documents described a superseded project or an untested constraint:

- **A supposed "no display" blocker was false.** It had never been tested. A client runs
  here with OpenGL 4.5, which unblocked `LAB-CLIENT` and `LAB-CLIENT2` and resolved eight
  LAB-10 verdicts that had been deferred for that reason alone.
- `docs/_agent-brief.md`, the first file every agent is told to read, still described the
  pre-pivot custom-mod project and its M0–M2 scope.
- `docs/architecture.md` claimed only one feature package had been migrated to the layered
  layout; all seven had been.
- The README repeated the disproved display constraint; roadmap M1 read "Next" after eleven
  labs had finished, and M2 still promised a Field Journal whose premises LAB-01 disproved.

## Defects found and fixed

- **Audit reports were not deterministic**, despite AGENTS.md requiring it. An embedded
  timestamp and a leaked absolute path meant two audits of one archive differed. The path
  also leaked the operator's username into the files most likely to be published.
- **`validate-pack.sh` passed with a stale index.** packwiz maintains the index and is
  usually absent, since it has no tagged releases. Then the new check turned out to reject
  packwiz's own correct output, so it was aligned against the real tool.
- **`./gradlew` failed opaquely without `JAVA_HOME`**, with a message that never mentioned it.
- **Two adopted mods were never installed.** LAB-10 adopted Lithium and FerriteCore; neither
  was in `pack/`.

## New checks, each proven to fail before being trusted

| Check | Proven against |
|---|---|
| `scripts/check-repo-map.py` | an undocumented path, and a map row matching nothing |
| `scripts/verify-pack-downloads.py` | a corrupted hash, and a dead URL |
| `scripts/client-smoke.sh` | a client killed early, and a host with no display |
| index integrity in `validate-pack.sh` | a stale hash, an unlisted file, a mismatched `pack.toml` |
| provenance coverage in `validate-pack.sh` | a pin with no provenance entry |

## Commands run

All passing at the end of the run, with counts checked rather than recalled:

- `./gradlew build`
- `./gradlew test` — **152** root unit tests
- `cd tools/matcha-audit && ./gradlew test` — **27** audit-tool tests
- `./gradlew runGametest` — **28** GameTests (`@GameTest` annotations counted in `src/gametest`)
- ArchUnit and Checkstyle tasks, and `./gradlew validateAssets`
- `scripts/validate-pack.sh`, `scripts/check-repo-map.py`
- `scripts/verify-pack-downloads.py` — 9 of 9 pins verified against real bytes
- `scripts/build-pack.sh` and `scripts/client-smoke.sh`

Not run: `runDatagen`, which remains a scaffold.

## Assumptions and risks

- The substituted-archive case reports `MISSING` rather than `DEGRADED`. Defensible, since a
  decoy carrying no Matcha content has no baseline present, but it should be re-checked
  against a *partially* altered real archive when MRH-011 is implemented.
- Global Packs is All-Rights-Reserved and its declared source URL 404s. It is the only
  verified 26.2 pack loader, so this is a release risk with no current alternative.
- Matcha and Jade are NonCommercial, which is incompatible with any monetised distribution.
- The three ownership rows still at `DEFER` are genuinely unaddressed future systems.

## What to do next

1. **Decide the rights model**: `docs/decisions/OPEN-rights-model.md`. Recommendation is to
   stay private for now, but it now gates MRH-011.
2. **Then MRH-011**, which is packaging only and already verified.
3. Human-only checks: visual and colour judgement, sound, two-player multiplayer, and a
   real-play world smoke test.

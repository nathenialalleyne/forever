# LAB-02: Matcha pack-loader and dual role

> **Later correction.** This report states or assumes that no client could be launched
> from the build host. That was untested and is false: a graphical client runs here with
> OpenGL 4.5. See `docs/testing/client-environment.md`. The findings below stand, but the
> client-side items deferred as environmentally impossible should be retried, not left
> for a human.

- Date: 2026-08-28
- Run by: coordinating agent, automated
- Manifest: `labs/manifests/LAB-02-pack-loader-dual-role.toml`
- Status: complete for the server-side question, blocked for the client-side resource role

## Question

Can the single pinned Matcha archive be loaded as both datapack and resource pack through
Global Packs without editing or splitting it, and do the failure modes produce visible
diagnostics rather than a silent partial baseline?

## Exact versions

| Component | Project ID | Version ID | File |
|---|---|---|---|
| Minecraft | | | 26.2 |
| Fabric Loader | | | 0.19.3 |
| Fabric API | `P7dR8mSH` | `NqwNSxwA` | fabric-api-0.158.0+26.2.jar |
| Global Packs | `NRLPy2mk` | `DqrPrUMp` | globalpacks-fabric-26.2-26.2.0.jar |
| Matcha Flavoured | `QI0EmgZ1` | `E9rngRfK` | Matcha_Flavoured_1_12.zip |
| REI, Jade, Architectury, Cloth Config | | | as adopted by LAB-01 |

## Test world

Disposable instances under `/tmp/lab02/`, one per case, deleted after the run. Never a
real save.

## Checksum verification

The installed archive hashes to
`6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248`, which matches
`matcha.lock.json` exactly. The archive is loaded whole: it is never extracted, split, or
rewritten, satisfying acceptance criterion 1.

## Results by case

| Case | Setup | Server started | Recipes | Diagnostic |
|---|---|---|---|---|
| Base | Correct pack | yes, `Done (1.5s)` | 2346 | 2 pre-existing Matcha parse errors |
| A | Archive **missing** | **yes** | **1585** | **none** |
| B | Archive **altered** | **yes** | 2346 | 1 extra parse error among Matcha's own |
| C | **Data role only** | **yes** | 2346 | **none** |
| D | **Order conflict** | yes | 2345 | none |

## The headline finding: failures are silent

Acceptance criterion 3 asked whether a changed checksum, missing archive, one-sided load,
or conflicting order produce visible diagnostics rather than a valid partial baseline.

**They do not. Criterion 3 fails.**

- **Missing archive (case A).** The server started normally and reported `Loaded 1585
  recipes`, the vanilla count. Global Packs emitted no warning at all despite the file
  being listed under `required`. Grepping for any missing-pack diagnostic returned zero
  hits. A player would get a working server with none of the pack's gameplay and nothing
  in the log to say so. This is the most serious result in the lab.
- **Altered archive (case B).** The corrupted advancement produced exactly one additional
  `Couldn't parse` line, raising the count from 2 to 3. The server still started and still
  reported 2346 recipes. The signal exists but is buried among Matcha's own pre-existing
  parse errors, so it is invisible in practice. Nothing verifies the checksum at runtime.
- **Data role only (case C).** Removing the resource-pack entry produced a fully working
  server with 2346 recipes and no warning. On a client this would mean Matcha's mechanics
  with vanilla textures, which is exactly the quiet half-install
  `docs/compatibility/matcha-loading.md` warns about, and nothing detects it.
- **Order conflict (case D).** A probe pack listed *before* Matcha in `global_packs.toml`
  changed the recipe count from 2346 to 2345, proving it overrode a Matcha recipe. No
  conflict was reported.

## Load order: config order is inverted relative to applied order

This matters and the current comment in `global_packs.toml` is misleading.

The probe was listed **first** in the Global Packs `required` list. In `level.dat` the
resulting order is `Matcha_Flavoured_1_12.zip` then `zz_probe.zip`. Minecraft applies
later packs over earlier ones, so the probe won, which the recipe count confirms.

So "first in the config" means **lowest** applied priority, not highest. The shipped
config comment currently says the opposite, based on the Global Packs changelog wording
rather than observed behaviour. Any future pack intended to override part of Matcha must
be listed **after** it, not before.

## Global Packs licence risk

Global Packs is `LicenseRef-All-Rights-Reserved`. It is referenced by URL and hash and is
never committed or repackaged. This lab does not treat its use as permission to
redistribute, and acceptance criterion 4 is satisfied by that recorded constraint. The
dependency remains shallow: it loads packs and does nothing else, so replacing it is a
config change plus a lab run.

## Client behaviour

The resource-pack half remains unverified for the same external reason as LAB-01: the
host used for this run had no display, and a dedicated server never loads resource packs.
Case C shows the data role works alone, which means a client test is the only way to prove
the resource role is actually applied.

## Can Global Packs remain the technical candidate?

**Yes, with a required mitigation.** Acceptance criterion 5.

The utility does the job it claims: one archive, both roles declared, loaded whole,
reproducible, no world corruption, and clean removal. Paxi (26.1.2) and OpenLoader
(1.21.1) still have no 26.2 release, so there is no alternative to move to.

But its failure behaviour is fail-open, and the pack cannot rely on it to notice that the
foundation of the entire gameplay design is absent. That is a gap the pack must close
itself, and it is cheap to close.

## Follow-up required

1. **Add a pack-integrity check.** The recipe count is a usable signal: 1585 means Matcha
   is absent, 2346 means it loaded. A startup check that compares the installed archive's
   SHA-256 against `matcha.lock.json` and logs loudly on mismatch or absence would have
   caught cases A and B immediately. This is a legitimate companion-integration gap under
   ADR 0033: no configuration of Global Packs can express it.
2. **Correct the load-order comment** in `pack/defaultconfigs/global_packs.toml`, which
   currently states the opposite of observed behaviour.
3. **Client verification** of the resource role, following `docs/testing/client-verification.md`. This is the highest-value remaining check, because LAB-02 case C proved a data-only load produces no diagnostic at all.

## Decision

| Candidate | Decision | Reason |
|---|---|---|
| Global Packs `DqrPrUMp` | `ADOPT_BASELINE` with mitigation | Only 26.2 option, does the job, but fails open on every error path. The pack must add its own integrity check. |
| Paxi | `DEFER` | No 26.2 release. Preferable on licence grounds if one appears. |
| Open Loader | `DEFER` | Newest listed game version 1.21.1. |
| Custom pack loader | `REJECT` | The utility works. The gap is diagnostics, not loading. |
| Pack-integrity check | `COMPATIBILITY_SPIKE` becomes a real gap | Recorded for M1, justified by cases A and C. |

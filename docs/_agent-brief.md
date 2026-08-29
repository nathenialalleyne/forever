# Agent Brief: Many Roads Home, compatibility-evidence phase

You are a worker agent on **Many Roads Home**, a curated Minecraft Fabric **modpack**.
Repo root: `/home/natea/repos/forever` (the directory name predates the rename).

The project was previously a custom-mod-first prototype called Forever. It pivoted to a
modpack: the deliverable is maintained third-party mods plus configuration, datapacks and
a resource pack, with a *small* companion mod only where nothing existing can do the job.
The prototype code is preserved as evidence under ADR 0034, not as a claim of progress.

## Read these first (they already exist, do not rewrite them)
- `docs/vision.md`
- `docs/design-principles.md`
- `docs/architecture.md`
- `docs/dependency-baseline.md`
- `docs/roadmap.md` for where the project actually stands

## Current phase

`LAB-01` through `LAB-11` are complete; `LAB-12` is the last one running. Results are in
`labs/results/` and every one is authoritative over older planning text. Read the labs
relevant to your task **before** proposing anything: several documents predate them.

**The dominant finding is restraint.** Five labs concluded *adopt nothing*, and that was
the correct answer each time:
- Matcha already owns food, hunger, healing and food effects (LAB-05).
- Vanilla already handles local item transport (LAB-08) and travel (LAB-09).
- No structure provider is safe to adopt, because generated chunks are permanent (LAB-10).
- No guidebook or advancement-noise problem exists, so no reward or journal system is
  needed (LAB-01, LAB-11).

Two rules follow from that pattern. Any candidate advertising **any-distance, wireless or
cross-dimensional** behaviour is rejected under ADR 0012, and any candidate that **loses or
corrupts saved data when removed** is rejected on save safety. Both have already
disqualified real candidates.

## Hard scope rule

**Do NOT implement a gameplay system.** No masteries, equipment condition, repair,
reforging, settlements, villager careers, Obols, warehouses, food effects, seasons,
transportation, workers, shops, custom screens, custom blocks or custom items. Document
and backlog them instead.

Custom code requires a documented gap that `LAB-12` and ADR 0033 support. "No existing mod
does exactly what I imagined" is not a gap; "no existing mod can do this safely, and here
is the evidence" is.

Do not create empty Java classes or directories to populate a future package or to satisfy
the repository map. `scripts/check-repo-map.py` enforces the map, and inventing a directory
to please it is explicitly the wrong fix.

## Verified environment notes

- `export JAVA_HOME=~/toolchains/jdk-25.0.4.1+1` before any Gradle command.
- A graphical client **does** run here. An earlier belief that it could not was untested
  and false: see `docs/testing/client-environment.md` and `scripts/client-smoke.sh`.
  There is no audio, no narrator, and window screenshots capture black, so read logs.
- `runClient` never exits by itself. Use a timeout and kill any survivor.
- Use disposable instances under `/tmp`. Never point anything at a real world.

## Verified pinned baseline (authoritative, do not change)
| Component | Version |
|---|---|
| Minecraft | 26.2 |
| Java/JDK | 25 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.158.0+26.2 |
| Fabric Loom | 1.17.20 (plugin id `net.fabricmc.fabric-loom`) |
| Gradle wrapper | 9.5.1 |
| JUnit | 5.14.2 |
| Matcha Flavoured | 1.12 |

Matcha Modrinth: project ID `QI0EmgZ1`, slug `matcha-flavoured`, version ID
`E9rngRfK`, file `Matcha_Flavoured_1_12.zip`, release type `release`, published
`2026-08-12T01:54:25.512843Z`, MC `26.2`, license `CC-BY-NC-SA-4.0`,
URL `https://cdn.modrinth.com/data/QI0EmgZ1/versions/E9rngRfK/Matcha_Flavoured_1_12.zip`.

Minecraft 26.2 is NON-OBFUSCATED: there are no Yarn mappings and none are declared.

Local JDK 25 for running builds: `export JAVA_HOME=~/toolchains/jdk-25.0.4.1+1`

## Project identity
- product name **Many Roads Home**; the companion mod still uses mod ID `forever`,
  base package `dev.forever`, and version `0.0.0-dev`. Renaming those is its own ticket.
- private repository; original code is "All rights reserved" (LicenseRef-Forever-Proprietary).
  The rights model is an open decision: `docs/decisions/OPEN-rights-model.md`
- Matcha material is CC-BY-NC-SA-4.0 and must be kept separate from original code

## The 18 design principles (summarised; full text in docs/design-principles.md)
1. Normal Minecraft building always available. 2. Discovery may hide possibilities,
not instructions. 3. Learning permanent, specialisation configurable. 4. Repetition is
not mastery. 5. Items never deleted by durability. 6. Infrastructure turns repeated
effort into convenience. 7. Settlements evaluate function not style. 8. Geography
matters but infrastructure reduces it. 9. Solo play viable. 10. No permanent chunk
loading. 11. No arbitrary off-screen villager death. 12. Mechanics in code, balance in
data. 13. Matcha internals isolated to `dev.forever.compat.matcha`. 14. Every
persistent format versioned. 15. Every mechanic documentable in the Field Guide.
16. No colour-only accessibility. 17. Never degrade normal play to sell an upgrade.
18. The world save outranks any feature.

## Locked design decisions (A-S)
A. Official Matcha is the starting gameplay pack; do not fork initially; pin exact release.
B. Matcha isolated behind a compatibility adapter.
C. Matcha mechanics classified individually: keep / extend / override / replace eventually / undecided.
D. Mastery loadout: 1 Focus + 2 Supporting; progress permanent; inactive retain progress;
   switching requires rest/home/workplace interaction; no XP loss, no re-levelling, no recurring tax.
E. Item specialisation: an item may know several paths, only one active; switching requires
   reforging; reforging preserves identity and prior path progress.
F. Repair: no disappearance at zero condition; field repair restores partial; workshop repair
   restores full; reforging restores full and permits advanced changes; no max-durability decay spiral.
G. Villager mortality: may die; physically simulated cause matters; short downed/rescue state where
   practical; unloaded abstract sim cannot randomly kill; deliberate murder has settlement/reputation
   consequences; institutional knowledge may survive; untaught personal knowledge may be lost.
H. Obols hybrid: physical treasure/transferable items + Coin Purse balance; withdrawable; must not
   consume many inventory slots.
I. Settlements are graphs of registered buildings, not circles; proximity attachment; outposts;
   infrastructure connection; distances configurable.
J. Routes: roads use explicit survey/registration between markers; rail uses bounded
   station-to-station graph verification; results cached; never continuously scan the world.
K. Traveler's Cache: 9 slots, expandable to 18; personal essentials; no container nesting ever.
L. Cross-dimensional logistics: no magical universal warehouse across dimensions; later Portal
   Depots may connect networks.
M. Elytra becomes glider/scouting/vertical traversal; unlimited rocket flight is not dominant travel;
   established rail must beat Elytra for repeated fixed routes.
N. Seasons: depend on a climate abstraction, never a provider. LAB-06 pilots Homeostatic Seasons and defers Serene Seasons (beta, All Rights Reserved, beta GlitchCore, unknown game rule left on removal).
O. Worldgen posture conservative: dramatic terrain + restrained structure set; add no worldgen deps now.
P. Existing natural villages may later be incorporated via a charter/import workflow.
Q. Player shops: free pricing; NPCs have willingness-to-pay so bad prices simply do not sell.
R. Journey skipping only after: both destinations discovered, route physically travelled,
   suitable infrastructure built and registered, service validated.
S. Private for now; keep original code separate from Matcha-derived material and third-party assets.

## Quality bar
- Small named classes, explicit types, immutable records where appropriate.
- No god objects, no global mutable singletons, no premature abstraction.
- Validate external data. Log actionable errors. Never swallow exceptions. No broad
  `catch (Exception)` without rethrowing or adding context.
- No TODO comments without a corresponding backlog ticket ID (FVR-xxx).
- UTF-8 everywhere. Deterministic generated output.
- Document every added dependency and why it is necessary.
- Write genuine, substantive prose. No filler, no marketing voice, no restating the
  heading. Explain the *why*, including tradeoffs and rejected options.

## Style
- Use British-neutral clear English. Avoid em dashes. Avoid semicolons used as em dashes.
- Markdown headings, tables, and short paragraphs. Concrete over abstract.

## Owned by the human maintainer: do not delete

- `art/` (editable art sources), `src/assetTools/` (asset validator),
  `src/client/resources/assets/forever/` (exported production assets), and the
  `validateAssets` Gradle task are **deliberate, committed project infrastructure**
  added by the maintainer under FVR-A001.
- Run `./gradlew validateAssets` before committing any texture change.
- Do not remove them, and do not treat them as stray scope creep.

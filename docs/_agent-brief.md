# Agent Brief: M0-M2 Foundation Build

You are a worker agent on the **Forever** Minecraft Fabric project.
Repo root: `/home/natea/repos/forever`

## Read these first (they already exist, do not rewrite them)
- `docs/vision.md`
- `docs/design-principles.md`
- `docs/architecture.md`
- `docs/dependency-baseline.md`

## Hard scope rule for this run
Milestones M0 (project foundation), M1 (Matcha acquisition/pinning/audit tooling),
and M2 (AI-readable architecture and design docs) ONLY.

**Do NOT implement any gameplay system.** No masteries, item mastery, nonbreaking
equipment, repair, reforging, settlements, villager careers, migration, Obols, coin
purse, warehouses, food effects, seasons, transportation, workers, shops, custom
screens, custom blocks, or custom items. These get documented and backlogged, never
coded, during this run.

Do not create empty Java classes just to populate future packages.

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
- mod ID `forever`, display name `Forever`, base package `dev.forever`
- version `0.0.0-dev`
- private project; original Forever code is "All rights reserved" (LicenseRef-Forever-Proprietary)
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
N. Seasons: first climate adapter target is Serene Seasons, but depend on a climate abstraction.
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

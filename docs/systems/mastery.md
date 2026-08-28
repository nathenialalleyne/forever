# Mastery

## Status
**Concept. NOT implemented.** No mastery records, loadout switching, progression evaluator, or mastery UI exists yet. The primary backlog is FVR-300..305, with player explanations and Field Guide projection covered by FVR-200..204.

## Purpose
Mastery gives a player durable learning without turning ordinary survival into a counter-based grind. It makes specialisation a current working stance rather than an irreversible character class. A Builder can later work as a Prospector without losing what was learned as a Builder.

The system must make breadth, discovery, project ownership, and technique valuable enough to compete with repetitive farms. Its output is capability and convenience, not a second hotbar of combat abilities or a requirement to register ordinary Minecraft actions.

## Player experience
The planned masteries are Builder, Prospector, Cook, Smith, Explorer/Cartographer, Merchant/Steward, Farmer/Naturalist, Alchemist/Herbalist, Rider/Animal Handler, and Fisher/Mariner. The names describe domains, not rigid professions. A player can use any vanilla tool and place any ordinary block without a mastery.

A loadout has one Focus slot and two Supporting slots under ADR 0004. Focus gives the full current benefit, while Supporting masteries give narrower or weaker benefits. Progress in inactive masteries remains intact. Changing the loadout costs no XP and requires a rest, home, or workplace interaction so that the choice feels situated without becoming a recurring tax.

A player learns by trying different meaningful situations, recording discoveries, completing projects, and demonstrating technique. The UI should explain why a milestone was earned in plain language, such as “surveyed a river crossing and registered a safe route”, rather than “+3 experience”.

## Core rules
- Progress is permanent and stored per mastery. Switching Focus or Supporting slots never removes levels, discoveries, or prior path evidence.
- A progression milestone must be attributable to at least one of VARIETY, DISCOVERIES, PROJECTS, or TECHNIQUE. Raw action volume is not a primary source.
- VARIETY is measured by coverage of canonical domains, not by a lifetime action counter. For a Builder this can mean distinct material families, structural purposes, scales, and environmental contexts. One evidence key such as `stone_wall_riverbank` is recorded once, while ten thousand copies do not add ten thousand progress.
- Breadth thresholds use a coverage vector. A milestone may require, for example, three material families across two structural purposes and two terrain contexts. The exact thresholds are balance data, not code constants.
- DISCOVERIES are first observations or Field Guide facts that change what the player can do or understand. Finding a regional plant, identifying an ore behaviour, or learning a route marker can satisfy a discovery node once.
- PROJECTS are completed outcomes with a declared scope and validated result. A registered workshop, restored bridge, mapped region, or fulfilled bulk order can grant a project milestone. Abandoning a project does not grant completion credit.
- TECHNIQUE is assessed from a bounded rubric at a meaningful completion event. Examples include meeting a tolerance with fewer material substitutions, combining two known methods safely, choosing a route with lower risk, or producing a reliable result under a changed condition.
- Technique evidence is not a hidden repetition counter. The server records the input constraints, chosen method, outcome quality, and whether the player adapted to a new situation. Duplicate evidence with the same normalized context is idempotent.
- Mastery ranks are derived from named milestone nodes. A rank should unlock a capability, a recipe family, a service quality band, or a convenience modifier that is inspectable in the Field Guide.
- A Focus effect must not make its domain universally optimal. Supporting effects should encourage collaboration and temporary role coverage without making a solo player non-viable.
- Failed attempts can teach when they reveal a new condition or corrected technique. Repeating an already understood failure gives no progression.
- Team work may credit each participant only for the evidence they materially contributed. Standing nearby must not grant mastery.

Planned evidence examples are intentionally different by domain:

| Mastery | Breadth evidence | Technique or project evidence |
|---|---|---|
| Builder | materials, functions, terrain contexts | stable structure validated for function |
| Prospector | ore families, host environments, survey methods | correctly interpreted a sample and planned extraction |
| Cook | ingredient traits, preservation methods, meal contexts | balanced a meal for a stated situation |
| Smith | tool families, alloys, repair states | forged or reforged to a chosen trade-off |
| Explorer/Cartographer | regions, landmarks, route types | linked reliable discoveries into a map |
| Merchant/Steward | categories, suppliers, customer contexts | fulfilled a contract without stranded stock |
| Farmer/Naturalist | crops, habitats, seasons, propagation | improved yield through a changed condition |
| Alchemist/Herbalist | traits, preparations, reagents | produced a controlled concentration |
| Rider/Animal Handler | terrain, mounts, handling situations | completed a safe route with a bonded animal |
| Fisher/Mariner | waters, species groups, boat contexts | planned and completed a viable voyage |

## Data ownership
Compact mastery state belongs on a versioned persistent player attachment or component. It contains per-mastery rank, unlocked milestone IDs, normalized evidence keys, active Focus and Supporting IDs, and the last valid loadout-change context.

Large explanatory text and milestone definitions belong in data resources. Evidence records should be compact and pruned or aggregated after they have contributed to a milestone. The server must be able to recompute a derived summary from versioned definitions without treating the summary as the source of truth.

## Server/client responsibilities
The server validates every evidence event, applies milestone rules, and owns loadout changes. It sends a bounded summary of ranks, active slots, available switching context, and recent explanations. The client renders previews and Field Guide pages but cannot award progression or predict a final rank.

A progress explanation should include the named requirement still missing. A client request to switch loadout is a proposal containing the desired IDs and context, not an instruction to mutate state.

## Dependencies
- FVR-300..305 cover mastery data, evidence, loadout switching, progression rules, and presentation.
- FVR-200..204 cover the Field Guide and knowledge explanations required by principles 2 and 15.
- Equipment consumes active mastery capabilities, while projects, settlements, economy, transport, and food systems can produce evidence.
- All thresholds, rank rewards, and caps must live in versioned data resources under principle 12.

## Extension points
New masteries may register a stable ID, evidence schemas, milestone definitions, and capability outputs without changing existing player records. An evidence provider can submit a normalized accomplishment from a project, route, recipe, or discovery.

A future server may offer seasonal or server-specific milestone packs, but the base progression must remain valid in solo play. A second client can render the same summaries without becoming a progression authority.

## Failure cases
- An unknown mastery ID in saved loadout data is deactivated while its progress is preserved for migration.
- A malformed milestone definition is rejected at data load with the file and node named. It must not grant partial rewards.
- Duplicate events, reconnects, and replayed packets are idempotent by event identity and normalized evidence key.
- A failed loadout switch leaves the prior loadout active and explains whether rest, home, or workplace context is missing.
- A definition update that would reinterpret old evidence uses a schema migration or freezes that node. It must not silently lower earned ranks.
- A client with stale definitions displays a server-provided summary and asks for a content refresh rather than guessing.

## Performance constraints
Evidence validation is event-driven. It must not scan every block placed or every item in a player's inventory on each tick. A player action can create at most one bounded evidence event, with duplicate keys collapsed before persistence.

A player summary should stay below 32 KiB and contain at most 128 recent explanation records. Milestone evaluation should inspect only the affected mastery and dependency tags. A world with 100 players must not run a full mastery recomputation for every player each tick.

## Accessibility and documentation requirements
Every mastery, slot, milestone, and effect needs a text label, icon, and plain-language description. Focus and Supporting states must not be conveyed by colour alone. The UI must distinguish “known but inactive” from “not discovered”.

The Field Guide must show the evidence categories, the next available milestone, the reason an action qualifies, and the switching rule. It must document that ordinary Minecraft actions remain available without mastery and that repetition alone is not progression.

## Non-goals
- This is not an XP grind, skill tree with irreversible choices, or combat class system.
- It does not gate block placement, ordinary crafting, or access to basic survival.
- It does not grant infinite yield, automatic building, or perfect knowledge of undiscovered content.
- It does not require another player, a permanent chunk loader, or a quest-book sequence.
- It does not delete inactive progress or impose a recurring respec payment.

## Open balance questions
- How many breadth dimensions should each mastery expose before the evidence becomes bookkeeping?
- What is the right ratio between Focus and Supporting benefits for solo players?
- Which technique rubrics can be explained and verified consistently across vanilla and modded inputs?
- How many normalized evidence keys can a long-lived player retain before aggregation becomes necessary?
- Should a project award one mastery milestone or offer a player a choice among relevant domains?

## Planned tests
- Create a player with progress in all ten masteries, switch Focus and Supporting slots, and verify no progress changes.
- Assert that switching is accepted only through a valid rest, home, or workplace interaction.
- Feed duplicate variety, discovery, project, and technique events and verify idempotent results.
- Verify breadth thresholds require distinct tagged contexts rather than raw action counts.
- Verify a repeated identical action cannot advance a milestone without new evidence.
- Test malformed milestone data, unknown IDs, stale client definitions, and version migration fixtures.
- Bound summary size and measure that unrelated players and masteries are not recomputed on each event.

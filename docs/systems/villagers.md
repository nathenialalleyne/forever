# Villagers

## Status
**Concept. NOT implemented.** No migration service, villager career attachment, mentor relationship, home or workplace assignment, or loaded/unloaded simulation exists. This document depends on the settlement backlog FVR-400..405 and Field Guide work FVR-200..204. A dedicated villager implementation ticket must be assigned before code begins.

## Purpose
Villagers should be persistent people in the world rather than disposable breeding inputs or fixed vending machines. Migration supplies population and skills through geography and relationships. Career, home, workplace, rank, and known techniques make a villager worth remembering.

The simulation must reward investment without requiring permanent chunk loading. When a player is present, physical causes and visible behaviour matter. When nobody is present, bounded abstraction preserves continuity but must not invent arbitrary deaths.

## Player experience
A settlement can attract or receive migrants from another place, an expedition, a specialist network, or a documented opportunity. The player learns who a villager is, where they live, what they know, and who taught them. Ordinary breeding is not the normal population progression loop.

A villager has a persistent career and can advance through demonstrated work, mentoring, and successful projects. A mason might know a particular stone technique because an older villager taught it. A departure can move that expertise to another settlement, while an untaught personal technique may be lost if its owner dies.

While loaded, villagers walk, work, rest, eat where appropriate, use their workplaces, and can become downed or endangered by visible hazards. While unloaded, the settlement advances through bounded records for supplies, schedules, contracts, and safe movement. The player must not return to find that an important villager died to an invisible random roll.

## Core rules
- Population enters the system primarily through migration, invitations, contracts, and settlement opportunities. Ordinary breeding is not the required route to a larger or more skilled community.
- Each villager stores a stable identity, career, home, workplace, rank, known techniques, mentors, apprentices, settlement membership, and relevant history.
- Career is persistent. Changing a workplace or taking a temporary assignment does not erase the villager's learned role or techniques.
- Rank reflects recognised capability and responsibility. It must be based on validated work, mentorship, and project outcomes rather than a lifetime interaction counter.
- Mentorship transfers named techniques through a visible relationship. An apprentice needs access to a valid mentor, suitable workplace, time in the programme, and observed practice or project evidence.
- Institutional knowledge can survive an individual through teaching, written records, or a recognised workplace. Untaught personal knowledge may be lost on death.
- A loaded villager is subject to physical simulation and ordinary hazards. A short downed or rescue state should be used where the underlying cause supports it, so a player can respond before death.
- An unloaded abstract simulation may move supplies, advance a schedule, complete bounded safe work, or mark a known risk. It may not randomly kill a villager or claim an unobserved physical accident.
- Villagers may still die from a physically simulated cause, deliberate murder, or a documented world event. Such deaths create settlement and reputation consequences and preserve a clear cause when possible.
- A home and workplace are references to validated settlement services. If one becomes invalid, the villager enters a recoverable reassignment state rather than vanishing.
- A villager can be unavailable, travelling, downed, dead, or migrated. Each state has an explicit transition and save representation.
- Solo players can host, mentor, and benefit from a complete career network. Multiplayer adds exchange and specialisation but cannot be required.

A career record should explain observable state:

| Field | Example meaning |
|---|---|
| career and rank | current profession and recognised level |
| home and workplace | assigned settlement services |
| known techniques | stable capability IDs with provenance |
| mentor and apprentices | knowledge relationships |
| migration history | origin, arrival, departure, and reason |
| current simulation state | working, travelling, downed, safe, or dead |

## Data ownership
Compact villager state belongs on a versioned persistent entity attachment or component. It stores stable IDs and bounded technique, relationship, and history references. World-scoped settlement data owns service capacities, migration records, and institutional knowledge summaries.

Physical inventory remains on the entity or relevant container. A villager summary index may support settlement screens, but the index is derived and rebuildable. Dead or migrated entities retain the history needed by the chronicle without keeping an unloaded entity alive.

## Server/client responsibilities
The server owns migration offers, career transitions, assignments, mentorship, physical outcomes, abstract simulation, and death causes. The client renders a read-only villager card and requests actions such as invite, assign, mentor, or rescue.

Loaded movement and interactions happen in the common/server simulation. Client animations are projections and may be delayed or omitted. A client cannot promote a villager, complete a contract, or declare a safe abstract outcome.

## Dependencies
- FVR-400..405 provide settlement graph membership, residences, workplaces, capacities, and outposts.
- FVR-200..204 provide Field Guide explanations for careers, migration, mentorship, and safety states.
- Projects and economy create meaningful work and contracts. The Mason slice FVR-500..507 supplies the first concrete career example. Logistics and transportation can carry migrants or supplies.
- World chronicle records founding, migration, career milestones, mentorship, and notable deaths without logging every tick.
- All techniques, ranks, migration rules, and safe abstract rates belong in versioned data.

## Extension points
A career definition can declare required workplace capabilities, rank milestones, technique tags, mentor rules, and safe off-screen activities. A migration provider can offer a specialist from a discovered region or completed project.

A future animal handler, merchant, or Mason slice may add domain-specific techniques through stable IDs. External villager types may be adapted only if identity, inventory, lifecycle, and physical cause semantics can be preserved.

## Failure cases
- A corrupt attachment is quarantined and replaced by a safe unassigned state while the original data is retained for migration diagnostics.
- A missing home or workplace causes reassignment or a visible homeless/workless state, not entity deletion.
- A broken mentorship reference stops new knowledge transfer but preserves already learned techniques.
- A migration interrupted by restart resumes from a durable offer or arrival state, with no duplicate villager.
- A loaded physical death records its cause. An unloaded simulation cannot create an arbitrary death record.
- If a settlement loses capacity, existing assignments resolve deterministically and the player receives a list of affected villagers.

## Performance constraints
Loaded physical simulation must be limited to entities in ordinary active tracking ranges. Unloaded settlements use a fixed abstract work budget, for example no more than 64 villager records per settlement update and no more than 512 records per server cycle.

The server must not force-load chunks for careers or schedules. Villager summaries sent to a client should contain at most 128 villagers per page and no full inventory histories. Relationship graphs are bounded per villager, with old routine work aggregated into rank evidence.

## Accessibility and documentation requirements
Villager cards must state career, rank, home, workplace, techniques, current state, and actionable reason for an unavailable service in text. Downed, travelling, and dead states need icons and labels that do not depend on colour.

The Field Guide must explain migration instead of ordinary breeding, mentorship, loaded versus unloaded behaviour, rescue opportunities, and why off-screen simulation cannot create arbitrary death. A player's action history should identify the villager and outcome clearly.

## Non-goals
- This is not a full life simulator, dating system, or complex RPG companion system.
- It does not require permanent chunk loading or physical pathfinding for every off-screen villager.
- It does not guarantee immortality or prevent physically witnessed death.
- It does not make breeding the mandatory economic progression loop.
- It does not make villagers omniscient, infinitely productive, or interchangeable with a shop menu.

## Open balance questions
- What migration sources feel like population growth without making existing villages irrelevant?
- How quickly should a mentor transfer a technique, and how many apprentices can one mentor support?
- Which unloaded activities are safe enough to abstract without hiding meaningful risk?
- What rank benefits are useful without turning villagers into automation machines?
- How much relationship history should a player see before the card becomes a spreadsheet?

## Planned tests
- Create migrants with origins, careers, homes, workplaces, ranks, techniques, mentors, and apprentices and round-trip their data through restart.
- Verify breeding is not required for migration or career progression, while vanilla villagers remain valid.
- Simulate loaded hazards, downed rescue, physical death, and unloaded updates and assert no arbitrary off-screen death.
- Remove a residence or workplace and verify deterministic reassignment without entity loss.
- Interrupt migration and mentorship transactions and verify idempotent recovery.
- Enforce unloaded simulation budgets and confirm no chunk loading is requested.
- Verify technique inheritance, lost untaught knowledge, and chronicle entries across mentor death and apprentice continuity.

# Projects

## Status
**Concept. NOT implemented.** No opportunity generator, project record, planning workflow, contribution ledger, or completion validator exists. Mason work is tracked by FVR-500..507, and Field Guide explanations are FVR-200..204. A project implementation must remain backlogged until its own FVR ticket is assigned.

## Purpose
Projects give the player a way to state an intention and turn infrastructure, discovery, and collaboration into durable world change. They are not a traditional quest book filled with “punch a tree, claim three coins” tasks.

An opportunity is a meaningful situation the world presents. A project is the player's chosen response, which may accept, reshape, combine, or decline that opportunity. The system should reward an outcome that improves how the world is inhabited, not compliance with a checklist.

## Player experience
The player sees an opportunity because a settlement lacks a bridge, a region reveals a rare technique, a route needs a safe station, a villager wants to migrate, or a warehouse has a real bulk demand. The description states the situation, possible outcomes, constraints, and people or places affected.

The player can turn it into a project by choosing scope, site, materials, participants, and acceptable alternatives. They can also start a project without an opportunity by declaring a goal such as “connect the hill outpost to the river settlement” or “build a greenhouse for two regional herbs”.

Work proceeds through visible preparation, physical construction or gathering, validation, and completion. A project can be paused or abandoned without punishing the player for exploring another path. Its partial physical work remains in the world unless normal Minecraft actions remove it.

## Core rules
- An opportunity is an observed world condition or possibility. It is not a mandatory task, a timed notification spammer, or a reward claim button.
- A quest usually gives a prescribed sequence and a generic reward. A project instead defines a desired world outcome, functional constraints, evidence of completion, and optional reward or consequence.
- Opportunities may come from settlements, villagers, regions, economy categories, logistics failures, discoveries, seasons, animals, or existing physical structures.
- Player-initiated projects begin with an explicit goal and scope. The system validates the declared result, not whether the player followed one hidden build order.
- Projects can accept substitutions and multiple methods when the declared functional result remains valid. A bridge may use wood, stone, or another approved material family.
- Completion requires a server-validated outcome such as a registered building, working route, supplied order, mapped region, trained apprentice, or stable greenhouse. Merely placing a number of blocks is not enough.
- A project can award mastery evidence, knowledge, reputation, specialist access, infrastructure convenience, or a bounded material or Obol reward. Currency is optional and not the only feedback.
- Projects should surface trade-offs. A cheap temporary route may finish quickly but provide less capacity, while a durable rail station may take more preparation and unlock freight.
- An opportunity can be declined, deferred, or reshaped. Declining one does not block ordinary building, travel, farming, or survival.
- Multiplayer contributions are attributed to material work or knowledge, not proximity. Solo projects must remain complete and useful.
- Projects should create chronicle entries only for significant outcomes, not every intermediate action.
- A project must be inspectable in the Field Guide once its process is expected. Discovery can reveal that a project is possible, but not hide required instructions.
- Project definitions, validation thresholds, rewards, and expiry rules are data-driven.

A project record should expose:

| Field | Meaning |
|---|---|
| goal and scope | intended world change |
| origin | opportunity, player, discovery, or contract |
| site and participants | where and who |
| constraints | function, material, time, or quality rules |
| physical progress | derived from real world state |
| validation | current result and missing conditions |
| outcome | convenience, knowledge, reputation, or reward |

## Data ownership
World-scoped saved data owns opportunities, projects, scopes, participants, validation revisions, outcomes, and contribution summaries. Physical blocks, items, villagers, routes, and shipments remain owned by their systems and are referenced by stable IDs.

A project record stores declarations and evidence, not a second copy of a building or inventory. All records are schema-versioned. Abandoned or completed projects can be compacted into a chronicle summary once references are durable.

## Server/client responsibilities
The server creates opportunities, validates project declarations, checks physical progress, resolves completion, and grants outcomes. The client displays choices, requirements, alternatives, progress evidence, and consequences. It cannot mark a project complete from a local block scan.

The client may submit a proposed scope or material choice. The server returns a bounded validation result naming the missing functional condition and the next useful action.

## Dependencies
- FVR-500..507 cover the Mason slice and provide an early example of functional building projects.
- FVR-200..204 cover Field Guide explanations for project types, opportunities, and expected processes.
- Settlements provide registered buildings and services. Logistics, transportation, economy, mastery, villagers, regions, food, and seasons can generate or complete projects.
- World chronicle records significant project outcomes. Storage and equipment supply physical prerequisites.
- All thresholds, alternative tags, rewards, and expiry values live in versioned data.

## Extension points
A project provider can register an opportunity source, goal schema, validator, contribution rule, outcome, and documentation entry. A validator may use a settlement role, route capability, shipment state, ecological condition, or knowledge fact.

A future community project can accept contributions from multiple players and villagers with explicit attribution. A project can expose a preview to another client without granting that client authority.

## Failure cases
- A project references a removed building, route, villager, or definition. It pauses with the reference preserved and a migration or replacement choice.
- A player submits an invalid or oversized scope. The server rejects it before claiming resources.
- A crash occurs during completion and reward application. The project uses an idempotent completion ID and commits either the pre-completion state or the full outcome.
- An opportunity expires or its world condition changes. It becomes stale with a reason, not a hidden failure or forced debt.
- A multiplayer participant disconnects. The project retains credited work and remains completable by the remaining valid participants.
- A player abandons a project. Physical world changes and owned items remain subject to normal rules, while unspent project reservations are released.

## Performance constraints
Opportunity generation is event-driven or runs on bounded settlement and region updates. A server cycle should evaluate no more than 128 candidate opportunities and 64 active projects.

Project validation inspects only declared sites, referenced entities, and bounded route or inventory summaries. A building check should default to no more than 32,768 blocks. Client pages contain at most 64 projects and 128 evidence rows.

## Accessibility and documentation requirements
Project screens must state the goal, origin, scope, site, participants, constraints, current validation, alternatives, expiry, and next action in text. Opportunity, active, paused, stale, and complete states require icons and labels rather than colour alone.

The Field Guide must explain the difference between an opportunity and a quest, show a player-initiated example, explain functional validation, and state that no blueprint or prescribed aesthetic is required. It must show how to decline or reshape an opportunity.

## Non-goals
- This is not a quest-book filler system or a stream of chores for generic coins.
- It does not gate ordinary building, crafting, travel, or survival behind project acceptance.
- It does not demand a prescribed blueprint or aesthetic score.
- It does not duplicate the entire world, inventory, or villager state in every project record.
- It does not require multiplayer participation for completion.

## Open balance questions
- How often should opportunities appear before they feel like useful context rather than notification spam?
- Which player-initiated goals can be validated without constraining creative building?
- How many alternatives should a project expose before its scope becomes a design tool rather than a game system?
- What rewards best communicate that the world changed, rather than merely paying for a task?
- When should a stale opportunity be replaced, archived, or become a different opportunity?

## Planned tests
- Create opportunity, player-initiated, discovery, contract, and settlement projects with valid and invalid scopes.
- Verify a project can accept alternative materials and methods while enforcing functional constraints.
- Test pause, abandon, expiry, reshape, multiplayer contribution, and stale-world-condition paths.
- Crash during project completion and assert exactly-once validation, reward, and chronicle outcomes.
- Verify no generic “punch a tree” project is generated and no ordinary block placement is gated.
- Bound candidate generation, active validation, site scans, and client evidence payloads.
- Review Field Guide text with a solo flow from opportunity to completion and a no-blueprint building example.

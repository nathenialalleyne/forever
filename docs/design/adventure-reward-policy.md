# Adventure reward policy

## Status and purpose

This is design documentation for the Many Roads Home modpack-first pivot. It defines how exploration should create possibilities without making every other playstyle wait behind an artifact checklist. It is not an implementation specification, and it does not authorise a new custom item, structure, reward table, or persistent record.

The policy follows two related progressions:

```text
Discover -> Understand -> Institutionalize -> Scale
Experiment -> Develop -> Institutionalize -> Scale
```

**Discover** means that a place, person, object, or situation reveals a meaningful possibility. **Understand** means that the player can inspect the expected process in the Field Guide or an equivalent in-game explanation. **Institutionalize** means that the player builds, learns, commissions, cultivates, or trades for a dependable local route. **Scale** means that infrastructure, professional practice, engineering, or trade handles repeated demand. The second progression applies when discovery reveals a technique rather than a single service: the player experiments safely, develops a reliable method, and then gives it a home.

The reward for exploration is therefore not simply a larger chest. It is a change in what the world makes possible, followed by a choice about how that possibility becomes ordinary.

## What may be adventure-exclusive

Adventure may be the only first source of the following kinds of reward:

- **First knowledge.** A region family, route fact, material relationship, technique, preparation method, or Field Guide entry may first be learned by travelling, observing, sampling, or speaking with a relevant specialist.
- **Provenance and story.** A named object, chronicle entry, local history, or structure-specific decision may belong to one place. Its value is meaning and history, not an infinitely repeatable stat increase.
- **A first introduction.** A structure may introduce a mentor, profession, recipe family, regional crop, or service. After the player understands it, the capability must have a supported institutional route unless it is explicitly Prestige and not needed for efficient ordinary play.
- **A bounded unique outcome.** A structure may contain one world-changing key, choice, map fact, or artifact whose identity is tracked. A unique outcome may remain exclusive when it is optional, does not gate basic survival or construction, and does not produce a permanent stacking advantage.
- **A route lead.** A cartographer, rumour, or landmark may point towards a broad region family or a new opportunity. The lead may be incomplete, but it must not falsely imply that the player has no other way to learn the process.

Adventure-exclusive does not mean that the player is forever locked out of all related utility. A structure can be the first place where a player learns a possibility while a workshop, training path, cultivation method, trade route, or technical process later makes it repeatable.

## What must have alternate routes

The following must not be adventure-exclusive because they are part of efficient ordinary survival or because making them exclusive would recreate an Elytra or dungeon rush:

- transparent early progression and in-game instructions;
- ordinary block placement, ordinary Minecraft building, and basic palette freedom;
- item repair and the nonbreaking equipment rule;
- ordinary food, basic crops, and readable preparation;
- early portable essentials storage and local physical stock search;
- common project materials such as stone, wood, sand, glass, clay, gravel, and food;
- basic travel, rest, mount use, and safe local transport;
- access to workshops, projects, and professional learning;
- the ability to use a schematic as a preview and manual plan;
- the ability to participate in multiplayer or complete the route alone.

At least two genuinely different routes should exist for an Essential capability. Convenience capabilities should have at least two plausible routes. A Specialization may make one route excellent, but it must not invalidate all others. A Prestige reward may remain exclusive because ordinary play does not depend on it. These are capability-web rules, not promises that every route has identical cost or throughput.

The alternate route may arrive after first discovery. Geography is allowed to matter for first access. After that, the player may cultivate, preserve, train, process, buy, import, or transport the known capability. The first trip should be an adventure. The fiftieth trip should be a candidate for infrastructure.

## First-discovery rewards

A first discovery should answer four questions in sequence:

1. **What changed?** The player receives a clear, non-colour-only notice that a new region fact, technique, service, or object has been recorded.
2. **What can it do?** The Field Guide describes the player-facing benefit, the limits, and any risks in plain language.
3. **How can it be used?** Once the player is expected to perform a process, the exact process, inputs, substitutions, and failure conditions are inspectable in-game. Discovery may hide that a possibility exists. It may not hide instructions for a known process.
4. **How can it become dependable?** The game identifies one or more routes to institutionalise it: a project, workplace, workshop, cultivation site, route, trade service, or engineered process.

A first reward may contain a physical sample, a small amount of a regional material, a map fact, a specialist introduction, or a bounded recipe unlock. It should not simply hand out enough of a rare item to remove the reason the place matters. The reward should open a decision rather than close the whole system.

The `Experiment -> Develop` path uses the same discipline. An experiment should be safe enough to try, and a failed attempt may teach a new condition. Development should reward a meaningful adaptation, project result, or demonstrated technique. Repeating the same action in the same context must not create an unbounded mastery counter.

## Repeat rewards

Returning to a structure remains worthwhile, but repeat value is deliberately different from first-discovery value.

Good repeat rewards include:

- ordinary bounded materials appropriate to the structure and region;
- route confirmation, map annotations, or updated local circumstances;
- a changed opportunity, rescue, trade demand, or environmental condition;
- a chance to practise a known technique under a meaningfully different condition;
- contribution credit for a new participant who materially helped a shared project;
- a finite choice among useful outcomes where the choice is recorded and cannot be multiplied by reloads.

Bad repeat rewards include:

- a second copy of a world-unique artifact;
- a permanent percentage bonus that grows each time the structure is visited;
- generic rarity tiers whose only purpose is to make the player repeat a dungeon;
- gear-score escalation that makes ordinary tools obsolete;
- a structure loot table whose expected value exceeds every non-adventure route for common materials.

Repeat rewards should have a visible reason to stop. A warehouse can fill, a contract can be fulfilled, a map can become understood, or a structure can have no new unique outcome. Passive or repeatable production should saturate at bounded storage or demand. Remaining nearby for longer must have sharply diminishing value.

## Structure-instance tracking

A structure-specific unique outcome needs an identity that survives save and reload. The design contract is:

- identify the structure instance by stable world, dimension, placement, and structure identity data rather than by a transient entity or screen state;
- record whether a unique outcome has been claimed, by whom, and what world state it changed;
- separate **world claims** for finite physical outcomes from **player knowledge claims** for discoveries that can be understood by more than one player;
- make the claim idempotent, so a repeated interaction, reconnect, or server restart cannot produce a second physical outcome;
- preserve the original evidence when a record is invalid or cannot be migrated, and surface the failure instead of silently resetting the claim;
- keep ordinary loot and later local circumstances separate from the unique claim so that revisiting remains possible without cloning the unique result.

A world claim may prevent a second copy of a one-of-a-kind artifact while a player claim may allow several players to learn the same technique. The distinction must be visible in the Field Guide and in multiplayer feedback. Structure tracking is not permission to scan every chunk. It should be created by an observed, bounded interaction and maintained through explicit records.

## Multiplayer participation

Exploration must be cooperative without making another human a progression dependency.

- A solo player can discover, understand, and institutionalise every Essential and Convenience capability.
- A shared world claim prevents duplicate physical rewards, but knowledge may be shared through a map, mentor, Field Guide record, or institutional archive.
- Participation credit belongs to material contribution, meaningful observation, rescue, mapping, or project work. Standing near the discoverer does not grant the reward.
- The player who first discovers a place may receive provenance credit. A second player who materially maps a route or completes a different understanding step may receive their own knowledge credit without duplicating the unique object.
- A project can allow several contributors to benefit from the resulting infrastructure. It must retain enough attribution to explain why each participant received credit.
- A server restart, disconnect, or late join must not cause the same interaction to settle twice.

A multiplayer group should gain coordination advantages, not exclusive access to ordinary survival. The cartographer, builder, producer, engineer, and merchant can each contribute a different part of the path.

## Cartographers and rumours

Cartographers and rumours are support systems for discovery, not vending machines for exact artifact coordinates.

A cartographer may provide:

- a broad region-family tag;
- a direction, travel context, or approximate environmental clue;
- a confidence value and the evidence on which the lead is based;
- a known route or a warning that the route has not yet been physically travelled;
- a map exchange that shares institutional knowledge without granting a remote warehouse or journey skip.

A rumour may be incomplete or stale, but it must be honest about uncertainty. It should not silently reveal a complete checklist, and it should not make a required capability depend on finding one exact NPC with one exact offer. A lead may be earned through trade, a project, a prior discovery, or a regional relationship, so an explorer is not forced to search every village in a prescribed order.

The cartographic vocabulary should use broad region families across the ecological, cultural, economic, and knowledge dimensions. Examples include `coastal_maritime`, `river_trade`, `cold_forest_stewardship`, `volcanic_masonry`, and `old_rail_corridor`. A family tag is not a biome colour and is not a promise that every location in the family contains the same reward. Exact locations become appropriate only when the player has earned a route fact or when the map is recording a place they personally surveyed.

## Safeguards against mandatory artifact rushing

Every adventure reward review should answer these questions before approval:

1. **Does this gate an Essential capability?** If yes, add at least two non-adventure routes or reject the reward.
2. **Does this create an Elytra rush?** If the reward mainly exists to make long-distance travel tolerable, provide bedrolls, roads, rail, ports, mounts, or freight first. Flight can remain useful for scouting and vertical traversal without being the required entrance fee.
3. **Does this require a gear-score checklist?** Replace numeric power gates with understandable hazards, preparation, technique, or multiple valid methods.
4. **Does repeating the structure create permanent accumulation?** Use an idempotent claim, a bounded stock, or a non-stacking outcome. Never add a structure bonus that grows infinitely from visits or copies.
5. **Does the reward replace all active routes?** Keep mining, building, farming, trading, and engineering useful in their own domains. Adventure should discover possibilities, not become the only way to obtain basic materials.
6. **Can a player understand the process afterwards?** Add the exact Field Guide and recipe-viewer explanation before treating the reward as complete.
7. **Can a player decline it?** Declining, deferring, or missing an optional discovery must not block ordinary building, food, repair, travel, or survival.
8. **Can the result survive a restart?** Test duplicate interaction, death, reconnect, unload, missing resource-pack presentation, and an unsupported Matcha profile before allowing a unique outcome to alter the world.

The named `Last Light` capability is currently a design debt item. No existing project specification in this checkout defines its meaning, so it must remain Prestige and non-gating until a separate decision defines its owner, reward, and instance policy.

## Review standard

A reward is ready for a future implementation ticket only when its first-discovery event, in-game explanation, alternate routes, repeat behaviour, multiplayer attribution, structure-instance policy, and failure behaviour are all written down. A chest with a rare item is not a complete adventure system. The complete loop is discovery, understanding, institutional memory, and an earned reduction in repeated inconvenience.

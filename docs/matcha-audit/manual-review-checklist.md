# Matcha manual review checklist

This checklist is for the future review agent who must inspect Matcha **by system**.
It supplements the static audit CLI. It is not a licence to infer a system from a
function name or to design a replacement before the existing behaviour is known.

## Non-negotiable review rules

- **Inspect actual source paths.** Open the files named by the generated source
  inventory and record the archive-relative path and a precise locator.
- **Trace references.** Follow load and tick entry points, function calls, tags,
  predicates, scoreboards, advancements, recipes, loot, structures, and item
  signatures until the reachable behaviour is understood.
- **Separate observed facts from inferred intent.** A source command or controlled
  observation is a fact. A statement about why the author did it is an inference and
  must be labelled with confidence.
- **Do not rely on the unofficial wiki.** It is not evidence and cannot replace the
  locked archive or a runtime trace.
- **Do not propose a rewrite until behaviour is traced.** First document what the
  current system does, including surprising or undesirable cases.
- **Record ambiguous behaviour rather than guessing.** Use `unknown` and
  `undecided`, cite the missing evidence, and add a test that could resolve it.

Start with the input and pack manifest report, confirm the locked SHA-256, then use
the generated index to find the report families named below. The family labels are
semantic. The CLI index supplies the exact generated filenames under
`generated/matcha/<locked-version>/`.

## Report families

| Family | Use it for |
|---|---|
| Pack manifest | Lock version, archive digest, `pack.mcmeta`, pack format, resource-pack presence |
| Source inventory | Actual paths, file hashes, file kinds, and evidence IDs |
| Function and command trace | Command order, conditions, schedules, scoreboard reads and writes, state transitions |
| Item identity report | Vanilla base items, custom model data, components, model references, and identity collisions |
| Recipe and tag report | Ingredients, outputs, unlocks, tags, substitutions, and recipe conflicts |
| Advancement and tutorial report | Criteria, rewards, messages, ordering, and tutorial instructions |
| Loot and structure report | Loot pools, conditions, structure references, generation hooks, and chest contents |
| Runtime trace | Controlled player actions, server observations, timing, and vanilla-control results |
| Persistence and multiplayer report | Save locations, scope, join or leave behaviour, shared state, and duplication or loss risks |
| Compatibility report | Pack or mod detection, event ordering, optional dependencies, resource-pack assumptions, and conflicts |

## Review procedure

1. Create or select a disposable world with the exact Matcha archive and lock noted
   in the session record. Keep a vanilla-control world with the same relevant seed or
   a reproducible equivalent.
2. Choose one system row and list all of its source entry points from the inventory.
3. Trace source references before testing. Mark every branch, reset, schedule,
   condition, and scope as player, entity, team, dimension, world, or global.
4. Run the smallest controlled observation that distinguishes the behaviour from
   vanilla. Record setup, action, initial state, output, and evidence ID.
5. Compare the observed result with vanilla. Record replacement, addition, or no
   replacement without assigning purpose yet.
6. Only after the trace, write an inference about purpose and a classification
   decision. If the trace is incomplete, leave the decision `undecided`.
7. Add required tests for normal play, edge cases, Matcha absent, and an unexpected
   or alternate resource-pack case when identity or compatibility is involved.

## System review matrix

| Check | What to inspect in the source and runtime | Audit report to consult |
|---|---|---|
| [ ] Early progression | Identify the first load and tick entry points, starting inventory or items, first unlocks, recipe gates, tutorial messages, command prerequisites, and what a new player can still do in vanilla. Test a fresh player from spawn through the first meaningful milestone. | Source inventory, function and command trace, advancement and tutorial report, runtime trace |
| [ ] Fire and campfire | Trace ignition, extinguishing, fire spread, campfire interaction, damage, fuel, smoke, cooking, block tags, scheduled cleanup, and whether a player or global state changes the result. Test ordinary fire, soul fire if referenced, campfire cooking, rain, water, and multiplayer interaction. | Function and command trace, recipe and tag report, runtime trace |
| [ ] Kiln and smelting | Find every kiln or smelting entry point, input and output, fuel source, timing, XP award, container interaction, automation path, recipe unlock, and failure case. Compare furnace, blast furnace, smoker, campfire, and any disguised workstation behaviour. | Recipe and tag report, function and command trace, runtime trace |
| [ ] Tool tiers | Record how tools are identified, tier checks, mining speed, block restrictions, attack attributes, enchantment compatibility, durability changes, and zero-condition behaviour. Test each tier against representative blocks, entities, enchantments, off-hand use, and an unknown signature. | Item identity report, function and command trace, runtime trace |
| [ ] Item substitutions | List every vanilla item used as a disguised Matcha item, exact custom model-data or component signature, recipes, commands, tags, display changes, stack rules, and conversion paths. Test with a replacement or absent resource pack and with an ordinary vanilla item sharing the same base item. | Item identity report, recipe and tag report, pack manifest, runtime trace |
| [ ] Hunger | Trace food values, saturation, exhaustion, sprint or action changes, tick frequency, difficulty checks, scoreboards, and whether hunger is global or per-player. Test starvation, saturation, food taken from different sources, and two players with different states. | Function and command trace, persistence and multiplayer report, runtime trace |
| [ ] Healing | Identify every healing source, damage threshold, regeneration effect, cooldown, condition, item use, difficulty dependency, and interaction with vanilla regeneration. Test direct healing, regeneration, full health, absorption, damage in different dimensions, and simultaneous players. | Function and command trace, item identity report, runtime trace |
| [ ] Food effects | Trace effects, durations, amplifiers, stacking or replacement, hidden scores, food preparation, unsafe ingredients, and how effects survive relog or death. Record whether the resource pack only changes presentation. | Recipe and tag report, function and command trace, runtime trace, persistence and multiplayer report |
| [ ] Death | Follow damage to death, inventory handling, drops, respawn, spawn protection, score resets, advancement grants, penalties, item conversion, and dimension changes. Test ordinary death, named or enchanted items, keep-inventory settings, disconnect around death, and a second player viewing the result. | Function and command trace, item identity report, persistence and multiplayer report, runtime trace |
| [ ] Equipment | Inspect slot checks, armour or attribute changes, off-hand rules, equipment replacement, ownership, item naming, and whether an item works when its model is missing. Test all relevant slots, swapping, death, containers, and vanilla equipment. | Item identity report, function and command trace, runtime trace |
| [ ] Durability | Trace every decrement, repair path, break condition, zero-condition state, enchantment interaction, item preservation, and whether the item disappears or remains. Test use at one condition, at zero, repair through every route, and moving the item through a container or trade. | Item identity report, function and command trace, persistence and multiplayer report, runtime trace |
| [ ] Enchanting | Record allowed items, enchantment restrictions, costs, levels, anvil or table hooks, discovery requirements, compatibility with custom identity, and preservation through repair or reforge. Test ordinary and disguised items, incompatible combinations, and absent Matcha. | Item identity report, function and command trace, recipe and tag report, runtime trace |
| [ ] XP | Trace each XP source, score or level representation, awards, thresholds, consumption, death, reset, and coexistence with vanilla XP. Test small and large awards, level boundaries, relog, death, two players, and a vanilla XP source. | Function and command trace, persistence and multiplayer report, runtime trace |
| [ ] Spawners | Inspect activation distance, owner or player scope, mob type, limits, cooldowns, light checks, drops, commands, and unloaded-chunk behaviour. Test multiple players, a moved spawner, a chunk unload, peaceful or difficulty changes, and vanilla spawners. | Function and command trace, loot and structure report, persistence and multiplayer report, runtime trace |
| [ ] Mob drops | Trace loot tables, predicates, killed-by-player conditions, looting or luck, quantities, rare branches, automation, and whether disguised items can be produced or duplicated. Test direct kills, indirect kills, different tools, and a vanilla loot control. | Loot and structure report, item identity report, function and command trace, runtime trace |
| [ ] Villagers | Follow entity tags, profession and career state, schedules, interactions, workstations, rank or knowledge storage, death, unload, and rejoin behaviour. Determine which state is per villager and which is settlement or world state. Test a named villager, a newly spawned villager, a transported villager, and a player leaving the area. | Function and command trace, persistence and multiplayer report, runtime trace |
| [ ] Trades | Record offer creation, ingredients, outputs, price changes, refresh and restock conditions, discounts, demand, profession or rank checks, currency handling, and ownership. Test repeated trades, failed trades, restock while unloaded, two players trading, and ordinary vanilla offers. | Function and command trace, item identity report, recipe and tag report, persistence and multiplayer report, runtime trace |
| [ ] Obols and currency | Trace physical currency, balance or scoreboard storage, Coin Purse identity, deposits, withdrawals, transfers, death, containers, trades, duplication guards, and per-player versus shared scope. Test a full purse, dropped currency, a container, death, relog, and two players transferring value. | Item identity report, function and command trace, persistence and multiplayer report, runtime trace |
| [ ] Shulkers | Inspect nesting restrictions, contents, disguised item handling, storage hooks, copying or duplication paths, break and place behaviour, and whether state survives dimensions. Test a shulker inside every relevant container, a shulker containing a translated item, and a missing resource pack. | Item identity report, function and command trace, persistence and multiplayer report, runtime trace |
| [ ] Loot | Inventory every loot table and caller, pools, rolls, conditions, functions, luck, random seeds, chest or entity scope, and fallbacks for missing references. Test generated chest loot and entity loot against vanilla controls and trace any rare branch to its caller. | Loot and structure report, function and command trace, runtime trace |
| [ ] Structures | Trace structure files, templates, pools, generation hooks, placement conditions, loot references, markers, and whether existing villages or structures are modified. Test generation in a fresh disposable world, locate the structure from its entry point, and inspect what happens when a reference is missing. | Loot and structure report, source inventory, function and command trace, runtime trace |
| [ ] Nether | Inspect portal entry and exit, dimension-specific progression, resources, mobs, fire, travel restrictions, advancement gates, and cross-dimension state. Test first entry, return, death, a portal with multiple players, and the same action in the Overworld. | Function and command trace, advancement and tutorial report, runtime trace, persistence and multiplayer report |
| [ ] End | Inspect entry gates, dragon or boss progression, portal and gateway behaviour, End resources, Elytra or flight interactions, death, and advancement rewards. Test first entry, return, death, multiple players, and vanilla End behaviour with Matcha absent. | Function and command trace, advancement and tutorial report, loot and structure report, runtime trace |
| [ ] Weather and environment | Trace weather, time, biome, climate, season or temperature signals, crop and animal effects, shelter checks, and any scheduled environmental state. Test clear, rain, thunder, biome boundaries, unloaded areas, and a world without an optional climate provider. | Function and command trace, compatibility report, persistence and multiplayer report, runtime trace |
| [ ] Darkness | Identify light thresholds, mob spawn or visibility rules, damage or fear effects, sounds, client overlays, and resource-pack assumptions. Test boundary light levels, different dimensions, blindness or darkness effects, alternate resource packs, and two players with different client settings. | Function and command trace, pack manifest, compatibility report, runtime trace |
| [ ] Multiplayer behaviour | For every score, tag, storage record, and event, determine player, entity, team, settlement, dimension, or world scope. Trace join, leave, death, ownership, simultaneous interaction, command authority, and client synchronisation. Run at least two players through the same action in a disposable server. | Persistence and multiplayer report, function and command trace, runtime trace |
| [ ] Recipes | Inventory recipe types, ingredients, tags, unlock criteria, outputs, custom identity, conflicts with vanilla, and behaviour when the resource pack or datapack is absent. Test recipe discovery, shift-click, automation, substitutions, and an ordinary recipe using the same vanilla item. | Recipe and tag report, item identity report, advancement and tutorial report, runtime trace |
| [ ] Advancement tutorialization | Trace criteria, requirements, rewards, hidden and visible entries, messages, ordering, reset behaviour, and whether the advancement gives instructions the player can inspect in game. Test a fresh player, a completed criterion, a failed or reset condition, and the absent-pack fallback. | Advancement and tutorial report, function and command trace, runtime trace |
| [ ] Resource-pack-dependent item identity | Compare source item signatures with model overrides, textures, translations, and custom model data. Test with Matcha's resource pack, a vanilla or alternate resource pack, a missing model, renamed items, and an ordinary vanilla stack with no signature. Identity must not depend on colour, texture, or display name alone. | Pack manifest, item identity report, compatibility report, runtime trace |
| [ ] Mod compatibility | Inspect loader checks, optional class references, event ordering, third-party assumptions, namespace collisions, recipe and loot conflicts, dedicated-server loading, and the no-op path when each optional integration is absent. Test Matcha present, Matcha absent, an unexpected version, and each relevant optional mod present and absent. | Compatibility report, pack manifest, source inventory, runtime trace |

## Recording a completed row

For each checked system, add or update one classification row only after the trace.
Include:

- the actual source paths and evidence IDs;
- a fact-only behaviour description;
- the vanilla behaviour replaced, if any;
- an `Inference:` purpose statement with confidence, or `unknown`;
- UX, persistence, multiplayer, and compatibility risks;
- exactly one of `keep`, `extend`, `override`, `replace_eventually`, or `undecided`;
- required tests that include a vanilla control and the relevant absent or
  unsupported case; and
- unresolved references or ambiguous branches in `notes`.

A row may remain `undecided` after the first pass. That is a valid result when the
source is incomplete or runtime evidence is not yet available. It is not valid to
replace uncertainty with a plausible story.

## Review sign-off

Before signing off, confirm:

- [ ] Every system row points to actual source paths.
- [ ] Every cross-file reference was traced or recorded as unresolved.
- [ ] Static facts and runtime observations are distinguished.
- [ ] Inferred intent is labelled and does not masquerade as source behaviour.
- [ ] No conclusion relies on the unofficial wiki.
- [ ] No rewrite was proposed before behaviour was traced.
- [ ] Ambiguities are recorded rather than guessed.
- [ ] Present, absent, and unexpected-version cases are covered where relevant.
- [ ] The reviewed CSV no longer contains `EXAMPLE_ONLY.` rows.

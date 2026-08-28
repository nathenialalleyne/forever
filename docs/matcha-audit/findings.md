# Matcha manual classification findings

## Evidence boundary

This review uses only the pinned archive and its committed audit output:

- archive: `vendor/matcha/Matcha_Flavoured_1_12.zip`;
- lock version: `1.12`, version ID `E9rngRfK`;
- SHA-256: `6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248`;
- generated evidence: `generated/matcha/1.12/`;
- archive inventory: 5,190 files, including 1,076 recipes, 244 advancements, 145 functions, 293 loot tables, 108 tags, 332 registry-data files, 94 worldgen files, 309 item definitions, and 1,675 textures.

The filled row-by-row decisions are in `system-classification.csv`. The generated audit is static source and reference evidence. It does not include the disposable-world runtime traces required by the manual checklist. Statements below therefore distinguish literal source observations from inference and identify where playtesting is still a release blocker.

## Highest-risk interactions with planned Forever systems

### 1. Global rules, health, hunger, and death

`data/main/function/setup/gamerules.mcfunction` changes `natural_health_regeneration`, `advance_time`, `keep_inventory`, phantom spawning, explosion drop decay, and ender-pearl death behaviour. The tick dispatcher then invokes `main:mechanic/manage_hunger`, which applies effects from the `Hunger` food objective. The heart-container path observes the `deaths` objective, subtracts two `Hearts`, and reapplies a max-health attribute.

This is not a cosmetic compatibility layer. It changes the baseline survival contract and touches global gamerules, per-player scoreboards, health attributes, inventory retention, and death timing. The likely Forever response is an explicit **override**, not a passive extension. The adapter must not enable these mappings merely because a function or objective exists. Two-player, reconnect, death-screen, and save/reload tests are needed before any gameplay bridge is allowed.

The same setup disables natural regeneration while Estus, cake, heart containers, food effects, enchantments, and warding stones provide other health paths. A change to one of these paths can alter the balance and safety assumptions of the others. Forever food, equipment, and mastery work must treat Matcha health state as an external, profile-scoped input rather than silently recreating it.

### 2. `endless_repairs` and FVR-101

`data/endless_repairs/advancement/inventory_changed.json` rewards `data/endless_repairs/function/reset_breakable_repair_cost.mcfunction`. The function writes a zero `minecraft:repair_cost` component across numbered inventory and hotbar slots, then revokes its advancement. The source does **not** intercept a zero-durability break, retain a destroyed stack, or define a Forever broken-item state.

This can be compatible with FVR-101 only in a carefully defined precedence model:

- **Possible synergy:** if FVR-101 retains an item as a broken, unusable stack, removing the escalating anvil repair cost may support a bounded repair workflow.
- **Direct conflict:** resetting `repair_cost` can bypass Forever's repair-cost policy and does not solve item destruction. Treating it as an “endless durability” implementation would be an unsupported inference.
- **Required decision:** FVR-101 must own the break transition and define whether Matcha's repair-cost mutation is retained, translated, or disabled. The original item and all unrecognised components must remain recoverable if translation fails.

Until that decision and its container, trade, death, and restart tests pass, the Matcha repair mapping should remain disabled even when the pack itself is verified.

### 3. Disguised vanilla identities and resource-pack collisions

The archive places 309 item-definition files and 713 models in the `minecraft` resource namespace. Data recipes and loot tables assign `minecraft:item_model`, names, lore, attributes, entity data, and effects to vanilla base IDs. Examples include a poisonous potato used for the crystal heart, a chicken spawn egg carrying marker or armour-stand entity data, and a map carrying an abbey model.

The server must use exact component signatures. A resource pack, texture, colour, translated name, or approximate model match is not identity evidence. An ordinary vanilla item with the same base ID must remain ordinary. A missing or replaced resource pack may change presentation but must not change server outcomes. This is a high-risk **override** boundary for FVR-100, food, economy, storage, and every future item migration.

The adapter therefore maps only audited exact signatures to stable Forever concepts and returns the original observation unchanged when the signature is unknown. No raw model value or private Matcha identifier is exposed in the adapter's stable result.

### 4. Enchantments, effects, and XP

The `minecraft:enchantment/in_enchanting_table` tag has `replace: true` and contains `#minecraft:curse`. The 23 blessing recipes produce stored enchantments on enchanted books, while registry definitions invoke effect functions for traversal, movement, armour cleansing, regeneration, warding, and other effects. Separately, the anvil functions set a player to 100 levels and later set levels and points to zero for players outside a temporary exclusion tag.

These are likely **overrides**, not harmless additions. They can change enchanting-table availability, item compatibility, movement, combat, health, darkness, and the XP economy. The XP functions are particularly dangerous for future mastery because they operate on player XP rather than a bounded, stable compatibility event. Unknown enchantments and ordinary vanilla XP must use the neutral path.

### 5. World generation and saved-world safety

The archive contains 43 structure NBT files and overrides or defines structure and structure-set data for abbeys, villages, pillager outposts, igloos, and village template pools. `data/minecraft/worldgen/structure_set/abbey.json` adds a random-spread abbey with an exclusion zone around villages. `data/minecraft/worldgen/structure_set/villages.json` places five village variants, and those structures use `village_beta` template pools.

This is a save-level **override**. A fresh-world result cannot establish safety for already generated chunks. Forever's conservative worldgen posture should win for existing worlds, and any future bridge needs a fixed-seed disposable-world comparison, missing-pool test, chunk-boundary test, and an explicit rule for worlds created with Matcha and later loaded without it. The adapter must never mutate existing structure data as a side effect of detection.

### 6. Recipes, filters, and tutorial state

The pack contains 1,076 recipes and uses a large recipe-unlock advancement tree. `setup/revoke_all_recipe_unlock_advancements.mcfunction` explicitly revokes many recipe unlocks. `pack.mcmeta` contains a large block filter for vanilla advancements and recipes. The tutorial tree contains 244 advancements and relies on item, location, dimension, and villager-trade criteria.

This is an **override** boundary with a high multiplayer and migration risk. Recipe visibility is player state, while recipe definitions and filters are pack-order and namespace state. A new player joining a running world is handled by an advancement that calls `setup/update_players`, but many functions use `@a` followed by `@p`. The adapter must not reset recipe progress or emit tutorial events until the target player, pack version, and event ordering are proven.

The instructional material itself is a candidate **extension**. It aligns with the Field Guide direction only if the instructions remain inspectable, do not hide required vanilla processes, and degrade to a clear unavailable status when Matcha is absent or unsupported.

## Matcha systems Forever will likely need to override

The classification marks these as `override` because their source changes a Forever design constraint or changes a vanilla baseline that future systems must own:

- early progression and recipe-reset behaviour;
- beacon/campfire event ordering;
- hunger, healing, death, and health attributes;
- item substitutions and resource-pack-dependent identity;
- durability and the `endless_repairs` repair-cost mutation;
- enchantment acquisition and effect execution;
- XP and anvil handling;
- hostile drops and difficulty-specific entity changes;
- structure and village generation;
- Nether water handling;
- End completion rewards and elytra variants;
- time, sleep, weather, and freezing-water environment rules;
- multiplayer player-initialisation and selector scope;
- recipe precedence and vanilla pack filters.

The likely **extension** candidates are kiln/smelting routes, equipment sets, darkness-cleansing effects, and tutorial presentation. These still need runtime comparison because an extension can become an override through recipe conflicts, item identity collisions, or global gamerules.

The likely **replace eventually** candidate is the emerald-based Obol/currency substrate. The evidence shows emerald triggers and trade inputs, but no dedicated purse, balance, deposit, withdrawal, transfer, overflow, or duplication guard. Forever ADR 0008 requires a hybrid physical and purse model, so every ordinary emerald must not be reinterpreted as an Obol without a signature and migration decision.

## Persistence and multiplayer risks

- Scoreboards such as `Hearts`, `Hunger`, `deaths`, `version_number`, `wandering_trader_timer_score`, and effect timers are created and changed by datapack functions. Their save scope, reload behaviour, and migration safety need a disposable-world trace.
- Player tags such as the death and trader tags can survive an interrupted command sequence. The source includes join/update and server-load repair attempts, but no atomicity proof.
- Marker entities carry beacon, trader, village, and structure-related state. Their relationship to a player, dimension, or world is not consistently established by static source alone.
- Many functions use broad `@a` iteration and then `@p` in nested commands. This is a concrete selector-scope risk, not a claim that every command is broken. Two players in different locations and with different hunger, health, XP, recipe, or trader state must be tested together.
- `keep_inventory true` and the heart penalty cross death, respawn, save, and disconnect boundaries. A disconnect around death must not duplicate a penalty or lose a valid item.
- Villager trades and the `trade_set` format are retained as unknown-path evidence by the audit. The format, restock, per-villager ownership, and unloaded behaviour require a dedicated-server trace.
- Removing Matcha must disable only Matcha capability. It must not delete stable Forever records, ordinary vanilla items, or unknown source records.

## Open questions requiring playtesting

| Question | Minimum observation | Why it matters |
|---|---|---|
| Does a new player receive the intended recipe and scoreboard state without affecting another player? | Join two players at different times, inspect both scoreboards, recipes, tags, and advancements before and after a reload. | Resolves the `@a`/`@p` and update-player risk. |
| Does Matcha's food/hunger loop target the correct player? | Give two players different hunger values and consume ordinary and prepared food in parallel. | Determines whether Forever can safely override hunger and food effects. |
| What exactly happens at zero durability? | Use a Matcha tool at one condition, zero condition, container transfer, trade, drop, and restart. | Required before combining `endless_repairs` with FVR-101. |
| Does an ordinary same-base item remain vanilla? | Compare an unmodified poisonous potato, marker spawn egg, map, potion, and matching signed stack with and without the resource pack. | Proves identity translation is exact and resource-pack independent. |
| Which enchantments remain in the table and which are recipe-only? | Inspect table, anvil, blessing, and unknown-enchantment paths in a Matcha world and a vanilla control. | The replace tag may change the entire enchanting surface. |
| Does anvil XP handling erase unrelated player XP? | Run anvil interaction for one player while another gains XP, then relog and wait past the timer. | Prevents global XP loss and mastery interference. |
| Are custom trade sets loaded and scoped per villager? | Spawn named and new villagers, trade repeatedly, unload/reload, and trade with two players. | The audit cannot resolve the `trade_set` format statically. |
| What does a generated abbey or village change in a fresh fixed-seed world? | Compare the same seed with Matcha, without Matcha, and with a missing template reference. | Protects world generation and existing-save assumptions. |
| Does a dragon kill reward run once and for the correct player? | Two-player End fight, repeat kill or reload, inspect reward position and gamerules. | Prevents duplicate rewards and global difficulty/spawn changes. |
| Does Nether water cleanup remove only intended water? | Use water buckets in and near the Nether with multiple players and different dimensions loaded. | Validates bounded destructive commands and event ordering. |
| What does the client show with no Matcha resource pack? | Run server with exact data but no resource pack, an alternate pack, and a missing model. | Confirms server authority and non-colour fallback requirements. |
| Which pack wins namespace collisions? | Vary datapack and resource-pack order on a dedicated server and record load diagnostics. | Determines whether the adapter can safely coexist with other packs. |

## Adapter and detector follow-up

`ForeverMatchaCompat.initialize()` now starts in a vanilla-safe no-op state, and the evidence-taking overload selects the verified 1.12 implementation only when the locked digest, exact metadata, complete fingerprints, and complete runtime-marker set agree. Missing, malformed, ambiguous, and unexpected-version evidence remains disabled with an actionable status.

The static entry point is intentionally not wired into `ForeverMod.java` in this change. A server lifecycle caller still needs to collect the datapack archive/metadata, fingerprints, and exact runtime markers, then call the evidence-taking entry point. Wiring that collector and lifecycle call is a follow-up and must not introduce a Matcha dependency into core or the common entrypoint.

The adapter implementation is deliberately small. It translates a few audited exact item signatures and behaviour signals, returns neutral results for unknown values, and keeps all raw Matcha identifiers inside `dev.forever.compat.matcha`. It is not a gameplay implementation and does not replace the audit or the required runtime traces.

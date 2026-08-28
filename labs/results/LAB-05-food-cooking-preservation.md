# LAB-05: Food, cooking, and preservation compatibility spike

- Date: 2026-08-28
- Run by: lion, automated
- Manifest: `labs/manifests/LAB-05-food-cooking-preservation.toml`
- Status: complete for the dedicated-server and archive questions; client interaction blocked by the headless host

## Question

Can a maintained 26.2 cooking mod coexist with Matcha Flavoured 1.12, or does Matcha already own food, cooking, food effects, and preservation closely enough that adding another provider would create competing systems?

## Conclusion

**Matcha already owns the core food and cooking space. No cooking or food-content candidate is adopted.** Farmer's Delight Refabricated loads, but duplicates Matcha's process and food identities and introduces a conflicting hunger and healing model. Cooking for Blockheads loads cleanly, but adds another oven, a kitchen aggregation system, recipe books, and storage-like blocks without solving the preservation contract. Both are rejected for the default pack. Matcha's preservation content is partial, but no tested candidate supplies the missing bounded preservation model without adding another competing stack.

## Exact versions

| Component | Project ID | Version ID | File |
|---|---|---|---|
| Minecraft | | | 26.2 |
| Fabric Loader | | | 0.19.3 |
| Fabric API | `P7dR8mSH` | `NqwNSxwA` | `fabric-api-0.158.0+26.2.jar` |
| Global Packs | `NRLPy2mk` | `DqrPrUMp` | `globalpacks-fabric-26.2-26.2.0.jar` |
| Matcha Flavoured | `QI0EmgZ1` | `E9rngRfK` | `Matcha_Flavoured_1_12.zip` |
| Roughly Enough Items | `nfn13YXA` | `4o0NSIMj` | `RoughlyEnoughItems-26.2.820.jar` |
| Jade | `nvQzSEkH` | `ue8CO97w` | `Jade-mc26.2-Fabric-26.2.11.jar` |
| Architectury API | `lhGA9TYQ` | `1yQC4VvP` | `architectury-fabric-21.0.7.jar` |
| Cloth Config API | `9s6osm5g` | `Nv3xnWXd` | `cloth-config-26.2.155.jar` |
| Farmer's Delight Refabricated | `7vxePowz` | `7v050iYz` | `FarmersDelight-26.2-3.6.17+refabricated.jar` |
| Cooking for Blockheads | `vJnhuDde` | `4ajPEtRY` | `cookingforblockheads-fabric-26.2-26.2.0.3.jar` |
| Balm | `MBAkmtvl` | `hT8Zt2uD` | `balm-fabric-26.2-26.2.0.6.jar` |

The Matcha archive SHA-256 was recomputed as `6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248`, matching the pinned lock value. All candidate versions above were read from the Modrinth API on the lab date and are release builds.

## Test worlds

- Baseline seed: `LAB05-20260828-Food`
- Farmer's Delight seed: `LAB05-20260828-Food-FD`
- Cooking for Blockheads seed: `LAB05-20260828-Food-CFB`
- World type: default generated overworld, Nether, and End
- Difficulty: easy
- Manual gamerule changes: none. Matcha's own load function applied its configured rules, including `natural_health_regeneration false`, `advance_time false`, `spawn_phantoms false`, and `keep_inventory true`.
- Disposable paths: `/tmp/lab05/instances/{baseline,farmers-delight,cooking-for-blockheads}/world`
- Removal copies: `/tmp/lab05/instances/{farmers-delight-removal,cooking-for-blockheads-removal}/world`
- Confirmed not real survival worlds: yes. Every world was created under `/tmp/lab05/`.

## Client and server configuration

- Dedicated server tested: yes, for the baseline, both candidates, and both same-world removal copies.
- Client tested: no. The host has no usable display, so client recipe screens, item use, tooltips, and resource-pack rendering were not claimed.
- Multiplayer tested: no. No client connected to these disposable servers.
- Recipe viewer: REI remained the only installed viewer. Farmer's Delight's optional Reliable Recipe Viewer and JEI integrations were not added.

## Matcha archive inventory before candidate evaluation

The archive was downloaded directly from the pinned Modrinth CDN URL and extracted only under `/tmp/lab05/matcha-extracted/`. The following counts come from the archive itself, not from a project description.

| Matcha content | Observed evidence |
|---|---:|
| `data/food/recipe/*.json` | 128 |
| Matcha food recipe types | 54 shapeless crafting, 8 shaped crafting, 33 smelting, 29 campfire cooking, 4 smoking |
| Unique explicit food item models in those results | 73 |
| Unique explicit item-name components in those results | 63 |
| Food-result stacks with explicit `minecraft:food` | 115 |
| Of those, nutrition 0 and saturation 0 | 114 |
| Of those, nutrition 2 and saturation 1.2 | 1 |
| Consume-effect entries | 195 |
| Distinct consume-effect IDs | 15 |
| Preservation recipes | 15: 8 pickles, 4 jams, 2 canned foods, and mead |
| Cooking-recipe advancement files | 4 |
| Cooking and preservation tutorial advancement files | 11 |

The 15 preservation recipe IDs are `food:pickled_tomatoes`, `food:golden_pickled_carrots`, `food:pickled_carrots`, `food:canned_golden_apples`, `food:canned_apples`, `food:glow_jam`, `food:rind_jam`, `food:pickled_potatoes`, `food:sweet_berry_jam`, `food:mead`, `food:pumpkin_jam`, `food:pickled_mushrooms`, `food:pickled_red_mushrooms`, `food:pickled_warped_fungus`, and `food:pickled_crimson_fungus`.

Matcha also supplies both named cooking blocks before any candidate is installed:

- `data/crafting/recipe/oven.json` crafts the Matcha Oven from iron, bricks, and a campfire. It produces the vanilla furnace identity.
- `data/crafting/recipe/mudkiln.json` crafts the Matcha Kiln from mud bricks, packed mud, and a campfire. It produces the vanilla smoker identity.

The preservation recipes are immediate shapeless transformations using bottles and ingredients. They produce physical, recoverable stacks, but the archive does not define a preservation container, a shelf-life duration, a quality state, scheduled spoilage, or a recovery transition. Matcha therefore covers the vocabulary and a usable subset of preservation, but not the complete `food_preservation` capability contract.

Matcha's food model is not ordinary vanilla hunger. Its `data/main/function/setup/gamerules.mcfunction` sets `natural_health_regeneration false`. Its `data/main/function/mechanic/manage_hunger.mcfunction` then runs every tick:

```text
execute at @a if score @p Hunger matches 10.. run effect give @p minecraft:hunger 1 255 true
execute at @a if score @p Hunger matches ..6 run effect give @p minecraft:saturation 1 1 true
```

The archive's food results commonly use zero nutrition and zero saturation with `can_always_eat: true`, then apply regeneration, absorption, resistance, strength, night vision, speed, water breathing, or other effects through `minecraft:consumable`. The same archive contains an Estus Flask recipe with instant-health potion contents and an Estus function that applies regeneration and resistance. This is an effect-led healing model layered over Matcha's hunger controller.

The archive defines no dedicated food item tag under `data/food/` and no dedicated food tag under `data/minecraft/tags/item/`. Recipes use existing tags such as `#minecraft:eggs`. Food acquisition is also wired into Matcha's custom farmer and fisherman trade tags.

Matcha provides its own readable instruction text. For example:

```text
adv.kleispack.cook_food = Powerful Flavours
adv.kleispack.cook_food.desc = Reveal the hidden aspect of a Food by cooking it on a Campfire or an Oven
adv.kleispack.preserve_food = Pickles & Jam
adv.kleispack.preserve_food.desc = Preserve a food to boost its flavour profile
```

This establishes that a second cooking book is not needed to explain the existing Matcha process.

## Startup outcome

The baseline was booted first:

```text
[15:53:04] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[15:53:04] [main/INFO]: Loading 49 mods:
[15:53:08] [main/INFO]: Found new data pack Matcha_Flavoured_1_12.zip, loading it automatically
[15:53:10] [main/INFO]: Loaded 2346 recipes
[15:53:10] [main/INFO]: Loaded 1805 advancements
[15:53:12] [Server thread/INFO]: Done (1.995s)! For help, type "help"
```

The expected pre-existing Matcha diagnostics were present: two `FileAlreadyExistsException` lines from Global Packs, two invalid `.txt` paths, and Matcha's known advancement and loot-table parse diagnostics. The server still reached ready state and saved all dimensions.

Farmer's Delight reached ready state and changed both counts:

```text
[15:55:10] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[15:55:11] [main/INFO]: Loading 50 mods:
	- farmersdelight 26.2-3.6.17+refabricated
[15:55:15] [main/INFO]: Found new data pack Matcha_Flavoured_1_12.zip, loading it automatically
[15:55:17] [main/INFO]: Loaded 2656 recipes
[15:55:17] [main/INFO]: Loaded 2027 advancements
[15:55:19] [Server thread/INFO]: Done (1.888s)! For help, type "help"
```

Cooking for Blockheads plus Balm reached ready state and changed both counts:

```text
[15:55:14] [main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[15:55:14] [main/INFO]: Loading 53 mods:
	- balm 26.2.0.6
	- cookingforblockheads 26.2.0.3
[15:55:20] [main/INFO]: Found new data pack Matcha_Flavoured_1_12.zip, loading it automatically
[15:55:22] [main/INFO]: Loaded 2541 recipes
[15:55:22] [main/INFO]: Loaded 1998 advancements
[15:55:24] [Server thread/INFO]: Done (1.918s)! For help, type "help"
```

The live candidate commands used the documented `timeout 300` wrapper. They returned 124 after reaching ready state because the server remains alive when empty. This is normal for the lab procedure, not a startup crash. The logs show the candidate servers saving their dimensions before the wrapper ended.

## Recipe and advancement count deltas

| Case | Recipes | Delta from 2346 | Advancements | Delta from 1805 |
|---|---:|---:|---:|---:|
| Matcha baseline | 2346 | 0 | 1805 | 0 |
| Farmer's Delight Refabricated | 2656 | +310 | 2027 | +222 |
| Cooking for Blockheads plus Balm | 2541 | +195 | 1998 | +193 |
| Farmer's Delight removed from its copy | 2346 | 0 | 1805 | 0 |
| Cooking for Blockheads and Balm removed from their copy | 2346 | 0 | 1805 | 0 |

This confirms that both candidates materially expand the recipe and advancement registries. Neither is a transparent viewer-only addition.

## Farmer's Delight overlap and Matcha interaction

The exact jar contains:

- 339 recipe resource files, including 28 dedicated `data/farmersdelight/recipe/cooking/` recipes;
- 74 food-category recipe advancement files and 28 cooking-category advancement files;
- 222 total advancement files, with one root and 201 recipe unlock entries plus 21 main entries;
- 60 tag files;
- four direct cooking or processing blocks: `cooking_pot`, `stove`, `skillet`, and `cutting_board`;
- food families including soups, noodle soup, fried rice, mushroom rice, salads, pies, sandwiches, rolls, stews, cooked cuts, and milk or juice items.

The 28 dedicated cooking recipes are a second cooking system rather than a passive integration. Eleven of those 28 recipes share at least one concrete vanilla input with a Matcha food recipe. Examples include apple and sugar in apple cider, cocoa and sugar in hot cocoa, dried kelp in noodle soup, mushrooms in mushroom stew, and brown mushroom in stuffed pumpkin block.

Recipe IDs are namespaced differently, so there is no same-string recipe ID override. The functional overlap is still concrete. Candidate resources and Matcha food recipes share these exact vanilla output IDs:

| Shared output ID | Matcha food recipes | Farmer's Delight recipes |
|---|---|---|
| `minecraft:beetroot_soup` | `food:milk_bottle` uses this base identity with a Matcha item model | `farmersdelight:recipe/cooking/beetroot_soup` |
| `minecraft:bread` | `food:bread`, `food:bread_campfire`, `food:naan` | `bread_from_smelting`, `bread_from_smoking` |
| `minecraft:cake` | `food:cake` | `cake_from_milk_bottle`, `cake_from_slices` |
| `minecraft:mushroom_stew` | `food:chocolate`, `food:chocolate_campfire` use this base identity | `farmersdelight:recipe/cooking/mushroom_stew` |

This is the exact failure mode the pack is intended to avoid: the same ingredient families, similar prepared foods, and shared vanilla output identities would coexist with different process paths and different food components.

Farmer's Delight also registers a separate hunger and healing model. Direct jar inspection found:

- `Nourishment`, whose English description is `Prevents the food meter from decreasing, except when healing with saturation`;
- `Comfort`, whose English description is `Provides natural regeneration, regardless of how hungry you are`;
- a `HUNGER_INDUCING_FOOD` consumable with a 600-tick Hunger effect at 0.3 probability;
- four Nourishment duration bands of 600, 1200, 3600, and 6000 ticks;
- a `HealConsumeEffect` used by food such as melon juice;
- `enableVanillaSoupExtraEffects = true`, `enableRabbitStewBuff = true`, and stackable vanilla soup overrides enabled by default in `farmersdelight-common.json`.

`Comfort` directly contradicts Matcha's disabled natural regeneration rule. `Nourishment`, the hunger-inducing foods, the vanilla soup overrides, and the healing consume effect all add behavior to the same hunger and recovery boundary that Matcha already controls. No configuration-only reconciliation was identified.

Farmer's Delight's advancement JSON does not set `show_toast` or `announce_to_chat` true in this version. It therefore does not reproduce the worst advancement-toast spam, but it still adds 222 advancement records and its own recipe-book category. It has no guidebook, manual, or starter-book asset path in the jar. That is a positive relative to LAB-01, but it does not offset the cooking and effect conflicts.

The server did expose a recipe-viewer compatibility defect:

```text
[15:55:19] [Server thread/ERROR]: [REI] Failed to fill display for recipe: vectorwing.farmersdelight.common.crafting.CookingPotRecipe@e841f333 [ResourceKey[minecraft:recipe / farmersdelight:cooking/dumplings]]
[15:55:19] [Server thread/ERROR]: [REI] Failed to fill display for recipe: vectorwing.farmersdelight.common.crafting.CookingPotRecipe@8b62ecf4 [ResourceKey[minecraft:recipe / farmersdelight:cooking/cabbage_rolls]]
```

Its client mixin configuration also emitted two missing client-class warnings on the dedicated server for `GhostSlots` and `GuiGraphicsExtractor`. The server remained usable, but the selected viewer path is not clean for all candidate recipes.

Farmer's Delight applied 83 biome modifications to 45 of 66 new biomes during startup. It therefore also adds worldgen and regional crop behavior that would need a separate balance and removal review.

## Cooking for Blockheads overlap and Matcha interaction

The exact jar contains 197 recipe resources, 193 advancement resources, and 31 tag resources. The server loaded 195 of those recipes and all 193 advancements. The two uncounted resources are the condition-guarded optional integration recipes `data/cookingforblockheads/recipe/croptopia/toast.json` and `data/cookingforblockheads/recipe/pamhc2foodcore/toast.json`, each requiring an absent mod. No Matcha recipe parse error was introduced.

Cooking for Blockheads adds a separate kitchen aggregation layer and an independently named cooking block family:

- `Cooking Table` and `Cooking for Blockheads` recipe-book items;
- a `Cooking Oven`, with 16 dye variants;
- `Toaster`;
- `Cutting Board`;
- cabinets, counters, fridge, sinks, cow and milk jars, fruit basket, spice rack, and tool rack;
- `Preservation Chamber`.

The package has two craftable books, `cookingforblockheads:recipe_book` and `cookingforblockheads:crafting_book`, with these English names:

```text
item.cookingforblockheads.recipe_book = Cooking for Blockheads I
item.cookingforblockheads.crafting_book = Cooking for Blockheads II
```

No automatic starter-grant path was found in the jar resources, but craftable books are still a second player-facing knowledge interface. The project description explicitly calls the mod a cooking book and multiblock kitchen. This does not preserve LAB-01's single coherent instruction surface.

Cooking for Blockheads does not add a second large food catalogue in the tested jar. Its conflict is structural: its `Cooking Oven` competes with Matcha's Oven and its kitchen blocks aggregate and automate the same preparation path. Its default config also gives the oven a fuel-time multiplier of `0.33`, making it a separate tuning point for cooking throughput.

The `Preservation Chamber` does not implement the named food-preservation capability. Its own tooltip is:

```text
tooltip.cookingforblockheads.preservation_chamber.description = Prevents the last item of a slot from being used up.
```

That is inventory protection, not bounded shelf-life extension with a process, duration, quality result, or recoverable failure state.

No global hunger or natural-healing override was observed in the CFB resource or config inspection. It therefore has a lower effect-model conflict than Farmer's Delight, but it still fails the pack's no-competing-kitchen and no-second-book constraints. Its startup itself was clean apart from the pre-existing Global Packs and Matcha diagnostics.

## Advancement and guidebook noise

Matcha has already established the baseline knowledge path through recipe viewing, Jade context, and translated advancement instructions. The candidate observations are:

- Matcha: 244 advancement JSON files in the archive, with the baseline server loading 1805 total advancements. Matcha supplies the cooking and preservation tutorial text itself and no starter guidebook.
- Farmer's Delight: 222 advancement files, one root, no `show_toast=true` entries observed, and no guidebook or starter-book asset path.
- Cooking for Blockheads: 193 advancement files, all under recipe unlock paths, no root, no toast flags, but two craftable recipe books.
- Crop & Kettle, not booted: its official description explicitly advertises a cookbook and an advancement tab, and its FAQ says custom items are not detected by REI. It would directly recreate both the book and viewer-discoverability problems.

The no-guidebook problem from LAB-01 remains solved only if CFB and other cookbook providers are not adopted.

## Food preservation capability

The capability row `food_preservation` in `docs/design/capability-web.csv` defines preservation as a convenience that keeps harvest or hunt useful across seasons and journeys without constant reaping or invisible deletion. The food specification adds these requirements:

- preservation is an infrastructure choice with a cost in time, container, or quality;
- spoilage, if present, is visible, bounded, and recoverable;
- a destroyed or inaccessible preservation container leaves stored food physical and recoverable;
- preservation inputs, containers, duration bands, and quality outcomes are bounded and data-defined.

Observed coverage:

| Provider | Preservation evidence | Result against capability |
|---|---|---|
| Matcha | 15 physical pickled, jammed, canned, or mead transformations, plus translated tutorial advancements | Partial. It provides stable prepared outputs and instructions, but no timed container, quality state, scheduled shelf-life model, or recovery transition. |
| Farmer's Delight | Food processing, crates, and meals, but no shelf-life or preservation-duration contract | Does not fill the named gap. |
| Cooking for Blockheads | `Preservation Chamber` prevents the last item in a slot from being used up | Does not fill the named gap. It is inventory protection, not food preservation. |
| Pantry | 26.2 alpha only | Not eligible for the baseline and not tested. |
| Pantry for Blockheads | 26.2 release with 48 meals and an Artisan Press | Not tested. It expands content and is LicenseRef-All-Rights-Reserved, so it cannot be accepted as a preservation solution without a separately selected provider strategy. |

No candidate justifies adding a second preservation system. If the missing timed or quality-based preservation behavior becomes a product requirement, it needs a separate food implementation ticket and gap analysis. No custom code was written in this lab.

## Multiplayer interaction

No multiplayer client was available. The dedicated server showed that both tested candidates register common/server code and reach ready state. REI's common plugin loaded in all cases. Server-authoritative food consumption, processing interruption, per-player recipe state, and client/server desynchronization were not exercised.

## Persistence

Matcha is a datapack/resource pack with functions, scoreboards, recipes, advancements, and resource-pack assets. Its archive is recorded in the new world's datapack state by Global Packs.

The candidate removal tests used copies of worlds that had already booted with the candidate. No modded item stack, cooking block, crop, or container was placed by a client, so these tests do not prove migration of populated candidate content. They do prove that the empty candidate-created worlds remain readable after the candidate jars are removed.

Both candidates leave missing datapack records visible on removal. This is important evidence rather than a clean migration:

- Farmer's Delight removal:

  ```text
  [16:00:31] [main/WARN]: Missing data pack farmersdelight
  [16:00:34] [main/INFO]: Loaded 2346 recipes
  [16:00:34] [main/INFO]: Loaded 1805 advancements
  [16:00:35] [Server thread/INFO]: Done (0.304s)! For help, type "help"
  ```

- Cooking for Blockheads and Balm removal:

  ```text
  [16:00:31] [main/WARN]: Missing data pack balm
  [16:00:31] [main/WARN]: Missing data pack cookingforblockheads
  [16:00:33] [main/INFO]: Loaded 2346 recipes
  [16:00:33] [main/INFO]: Loaded 1805 advancements
  [16:00:34] [Server thread/INFO]: Done (0.273s)! For help, type "help"
  ```

Removal returns the registry counts to the Matcha baseline and the server reaches ready state, but any real world containing candidate items, blocks, block entities, crops, or recipe-specific state would require a migration or retained compatibility registration before removal. This is consistent with `docs/world-save-safety.md` and is not a reason to adopt either candidate.

## Removal behaviour

| Candidate | Removal setup | Outcome |
|---|---|---|
| Farmer's Delight | Copied the candidate-created world, removed only `FarmersDelight-26.2-3.6.17.jar`, booted on port 25568 | Server reached ready state with 2346 recipes and 1805 advancements. Logged `Missing data pack farmersdelight`. No populated candidate content was present. |
| Cooking for Blockheads | Copied the candidate-created world, removed CFB and Balm, booted on port 25569 | Server reached ready state with 2346 recipes and 1805 advancements. Logged missing `balm` and `cookingforblockheads` datapacks. No populated candidate content was present. |

## Performance observations

- Hardware: AMD Ryzen 7 9700X, 8 cores, 19.5 GiB RAM, JDK 25.
- Baseline ready time: `Done (1.995s)` after the first world preparation.
- Farmer's Delight ready time: `Done (1.888s)`. Its log reported `Applied 83 biome modifications to 45 of 66 new biomes in 4.265 ms`.
- Cooking for Blockheads ready time: `Done (1.918s)`. Its log reported `Applied 0 biome modifications to 0 of 66 new biomes in 1.041 ms`.
- Removal copies reached ready in under one second because their worlds and libraries were already materialized.
- No profiling run was performed. No client render or recipe-screen performance claim is made.

## Conflicts observed

1. **Cooking block duplication.** Matcha supplies Oven and Kiln. Farmer's Delight adds Cooking Pot, Stove, Skillet, and Cutting Board. Cooking for Blockheads adds Cooking Oven, Cooking Table, Toaster, and Cutting Board.
2. **Food recipe duplication.** Matcha supplies 128 food recipes with pizza, pad thai, ramen, curries, stews, soups, jams, pickles, canned foods, teas, and effect-bearing meals. Farmer's Delight adds 28 custom cooking recipes and 74 food-category recipe advancements covering the same ingredient families and meal categories.
3. **Functional output overlap.** Four exact vanilla output IDs are shared by Matcha food recipes and Farmer's Delight recipes: `minecraft:beetroot_soup`, `minecraft:bread`, `minecraft:cake`, and `minecraft:mushroom_stew`.
4. **Input overlap.** Eleven of Farmer's Delight's 28 custom cooking recipes share concrete vanilla inputs with Matcha food recipes.
5. **Hunger and healing conflict.** Matcha disables natural health regeneration, actively manages hunger and saturation, and encodes healing in consume effects. Farmer's Delight adds Comfort, Nourishment, hunger-inducing food, healing consume effects, soup overrides, and stackable soup behavior.
6. **Recipe viewer defect.** REI failed to fill the displays for `farmersdelight:cooking/dumplings` and `farmersdelight:cooking/cabbage_rolls` in the dedicated-server reload log.
7. **Knowledge-interface duplication.** Cooking for Blockheads adds two craftable recipe books. Crop & Kettle's official description adds a cookbook and advancement tab and says its custom items are not detected by REI.
8. **Storage-boundary overlap.** Cooking for Blockheads adds cabinets, counters, fridge, sinks, jars, baskets, racks, and a preservation chamber, overlapping the pack's physical storage and preservation boundaries.
9. **Advancement expansion.** Farmer's Delight adds 222 advancements and Cooking for Blockheads adds 193. Their JSON did not produce visible toast flags in this version, but the registry and recipe-book surface still expand substantially.
10. **Removal diagnostics.** Removing either candidate from its candidate-created world is readable but leaves explicit missing datapack warnings. A populated-world migration was not demonstrated.

## Does it fit the pack?

No tested food-content candidate fits the default pack.

- Matcha already provides the cooking process, food-effect vocabulary, hunger and healing model, regional food acquisition hooks, readable cooking instructions, and a partial preservation path.
- Farmer's Delight is a mature and well-maintained MIT release, but maturity does not resolve the measured duplication or the conflicting hunger and recovery semantics. It is **REJECT** for the default baseline.
- Cooking for Blockheads is a mature 26.2 release, but its All Rights Reserved license, second oven, storage-like kitchen network, and two recipe books make it a poor fit. It is **REJECT** for the default baseline.
- Crop & Kettle is a stable 26.2 release with source and documentation, but its official scope is an entire competing cooking progression with over 100 recipes, seven crops, cookbook, interactive cooking blocks, wine aging, trader, and advancement tab. It is **REJECT** without a boot because the direct design conflict is already documented and it explicitly fails REI discoverability for custom items.
- Farming for Blockheads can provide a market and acquisition utility, but it is All Rights Reserved and does not own cooking. It is **DEFER** to a separate regional acquisition spike.
- Pantry for Blockheads can add 14 crops and 48 meals, but it would expand the rejected food stack and is All Rights Reserved. It is **DEFER**.
- Pantry is an alpha build with no source or issues URL in the observed record. It is **DEFER**.
- Croptopia, Brewin' and Chewin', Create processing, and Farmer's Respite do not provide a verified 26.2 Fabric release for the named project. They are **DEFER** rather than substituted with similarly named forks.
- Balanced Alchemy and AlelChemist are not suitable replacements for Matcha's already-present food effects. They are **DEFER** to a separate alchemy question, not added here.

The lab therefore adopts nothing from the cooking and food-content candidates. Ordinary vanilla food remains the fallback, and Matcha remains the single food/cooking foundation while the missing timed preservation contract remains an explicit design gap rather than an excuse to add a second system.

## Acceptance criteria and test coverage

| Ticket criterion or test requirement | Evidence and status |
|---|---|
| Vanilla food remains edible and useful without trait knowledge | Matcha preserves ordinary vanilla item identities and does not remove vanilla recipes. Server registry and recipe load succeeded. Actual client consumption was not run. **Partially verified.** |
| Candidate recipes and processing steps visible through viewer or Field Journal plan | REI common plugins loaded. Farmer's Delight registered an REI integration but emitted two display-fill errors. CFB uses its own books. Client screen inspection was blocked. **Partially verified.** |
| Unknown modded foods remain ordinary food until safe mapping | No Forever adapter was added and no unsafe mapping was introduced. Dynamic unknown-food consumption was not run. **Design preserved, runtime unverified.** |
| Preservation and alchemy inputs, outputs, durations, and failure behavior bounded | Matcha's 15 immediate physical transforms were inspected. No candidate supplied the complete timed preservation contract. Processing interruption and recovery were not dynamically exercised. **Gap recorded.** |
| Regional acquisition, cultivation, import, and trade remain possible | Matcha trade tags and Farmer's Delight wild-crop resources were inspected. No candidate was adopted. Full regional gameplay was not playtested. **Partially verified.** |
| Recipe discovery | Archive inspection, server recipe counts, REI common reload, and candidate resource inventories completed. Client discovery blocked. **Partially verified.** |
| Unknown-food fallback | No client or player inventory test available. **Not run.** |
| Processing interruption | No client interaction available. **Not run.** |
| Duplicate output prevention | Static comparison found no same-string recipe IDs, four shared vanilla output IDs, and 11 of 28 FD cooking recipes sharing concrete inputs. Dynamic transaction interruption was not run. **Static conflict verified.** |
| Preservation recovery | Same-world removal was tested on empty candidate worlds. Populated preservation stock was not created. **Not run.** |
| Effect-cap review | Matcha's 195 consume-effect entries and 15 effect IDs were inventoried. No unbounded cap integration was added. Cross-mod stacking was not dynamically exercised. **Static review only.** |
| Server-side consumption | No client connected. **Not run.** |
| Client explanation checks | No usable display. **Blocked.** |

## Follow-up required

1. Reconcile the existing candidate-matrix `ADOPT_BASELINE` entry for Farmer's Delight with this LAB-05 result. The measured overlap and effect conflict warrant a superseding decision record rather than a silent matrix edit.
2. Keep Matcha as the single cooking and food foundation. Do not add Farmer's Delight, Cooking for Blockheads, Crop & Kettle, Pantry, or Pantry for Blockheads to `pack/` as part of this lab.
3. If timed preservation or quality states become required, open a separate food implementation ticket. Follow ADR 0026 first and add an ADR 0033 gap analysis before any companion code. Preserve physical inputs and define migration and recovery before persistent data is introduced.
4. A future client-enabled lab may verify REI display coverage, Matcha resource-pack rendering, actual consumption, unknown-food fallback, and the non-colour explanation path. It must use a new disposable world.

## Decision

| Candidate | Decision | Reason |
|---|---|---|
| Farmer's Delight Refabricated `7v050iYz` | `REJECT` | Loads on 26.2, but adds a second cooking stack, shares four vanilla output identities and 11/28 cooking inputs with Matcha, conflicts with Matcha's hunger and healing model, and emits two REI display errors. |
| Cooking for Blockheads `4ajPEtRY` | `REJECT` | Loads cleanly, but adds another oven and kitchen network plus two recipe books. Its All Rights Reserved license and non-preservation Preservation Chamber do not fit the pack. |
| Crop & Kettle `ZsKxsUNy` | `REJECT` | Official scope is a competing cookbook, advancement, crop, cooking-block, wine, and food system, and its custom items are not detected by REI. |
| Pantry for Blockheads `FXzqhdCv` | `DEFER` | Stable 26.2 release observed, but it adds 48 meals and 14 crops on top of a rejected cooking stack and is All Rights Reserved. |
| Farming for Blockheads `pYMVKfIF` | `DEFER` | Potential regional market utility, not a cooking provider. All Rights Reserved and not tested in this food spike. |
| Pantry `L9XcU0rv` | `DEFER` | 26.2 alpha build with no source or issues URL. Not eligible as a core dependency. |
| Croptopia | `DEFER` | No core project resolved at the exact slug and no verified 26.2 artifact found. |
| Brewin' and Chewin' | `DEFER` | Newest official Fabric release observed is 1.21.1, with no 26.2 Fabric version. |
| Create processing | `DEFER` | Newest official project release observed is 1.21.1 NeoForge, with no 26.2 Fabric version. |
| Farmer's Respite | `DEFER` | Newest official release observed is 1.20.1 Forge, with no 26.2 Fabric version. |
| Balanced Alchemy | `DEFER` | All Rights Reserved, no source or issues URL, and unnecessary while Matcha already owns food effects. |
| Herbcraft Alchemy | `DEFER` | Separate alchemy candidate requiring its own compatibility and effect-cap question. |
| AlelChemist | `DEFER` | No source or issues URL in the observed project record and not needed for this cooking question. |
| Food Effect Tooltips | `DEFER` | Client-only utility. Client testing was blocked and it does not provide cooking or preservation. |
| Reliable Recipe Viewer | `REJECT` for this baseline | Farmer's Delight's optional integration is not added because REI is already the single selected viewer. |
| JEI | `REJECT` for this baseline | Optional candidate has 26.2 beta builds and would create a second recipe viewer beside REI. |

## Evidence paths

- Matcha archive: `/tmp/lab05/matcha.zip`
- Extracted archive: `/tmp/lab05/matcha-extracted/`
- Baseline startup log: `/tmp/lab05/instances/baseline/boot.log`
- Farmer's Delight startup log: `/tmp/lab05/instances/farmers-delight/boot.log`
- Cooking for Blockheads startup log: `/tmp/lab05/instances/cooking-for-blockheads/boot.log`
- Farmer's Delight removal log: `/tmp/lab05/instances/farmers-delight-removal/removal.log`
- Cooking for Blockheads removal log: `/tmp/lab05/instances/cooking-for-blockheads-removal/removal.log`

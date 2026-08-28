# Food and alchemy

## Status
**Concept. NOT implemented.** No ingredient-trait registry, cooking effect system, alchemy preparation, preservation model, or generic modded-food adapter exists. Field Guide documentation is covered by FVR-200..204. A dedicated implementation ticket must be created before any gameplay code is added.

## Purpose
Food and alchemy should make regional ingredients, cultivation, discovery, and preparation meaningful without turning every recipe into an opaque puzzle. The system uses a small readable vocabulary of ingredient traits, then gives cooking and alchemy different time and intensity profiles.

Players should have several ways to obtain useful inputs. A discovery can reveal a new ingredient, cultivation can make it reliable, preservation can extend a harvest, import can overcome geography, and trade can reward a specialist network.

## Player experience
A player can inspect an ingredient and see a handful of traits such as nourishing, warming, refreshing, calming, stimulating, bitter, restorative, or preserving. The vocabulary is small enough to remember and appears on the item, recipe, and Field Guide page. A modded food that exposes nutrition or tags can participate through a generic adapter instead of needing a one-off Forever recipe.

Cooking combines traits into a meal with longer-lasting, milder effects. It is the dependable option for a journey, workday, or settlement meal. Alchemy concentrates a selected property into a shorter, stronger preparation with stricter inputs and clearer risks. Neither is simply the upgraded version of the other.

The player can discover a wild herb, cultivate it near home, preserve a seasonal harvest, import it from a distant region, or buy it from a merchant. No single acquisition route is mandatory, though local discovery should remain valuable for knowledge and first access.

## Core rules
- Ingredient traits come from a small, versioned vocabulary. Each trait has a name, icon, plain-language description, and bounded mechanical tags.
- Trait combinations are data-driven and readable. A recipe should show its main resulting traits before consumption or brewing when the player has the relevant knowledge.
- A generic food adapter maps compatible modded items through stable properties such as food value, tags, effects, or explicit integration data. Unknown items remain ordinary food rather than being assigned invented traits.
- Cooking produces longer-duration, lower-intensity effects. It favours reliable preparation, sharing, preservation, and sustained travel.
- Alchemy produces shorter-duration, concentrated effects. It favours deliberate timing, special reagents, and a risk or preparation trade-off rather than unlimited stacking.
- Effect intensity and duration have data-defined caps. Eating multiple items cannot create an unbounded amplifier or bypass all survival hazards.
- Ingredient discovery is a knowledge event. The player may use a known recipe only when the required process is documented in the Field Guide, even if the ingredient itself remains rare.
- Cultivation makes a discovered ingredient renewable. Requirements may include soil, climate, pollination, habitat, or a bounded greenhouse capability, but they should not impose arbitrary long lockouts.
- Preservation turns a surplus into a stable stock with a cost in time, container, or quality. It is an infrastructure choice, not a hidden item deletion rule.
- Import and trade can provide a known ingredient from another region. The first local discovery should still matter for knowledge, quality, or route access.
- Spoilage, if used, must be visible, bounded, and recoverable. It must never silently delete a named or irreplaceable discovery item.
- Effects must describe interactions and contraindications. A player should know whether warming helps a cold situation, whether stimulant and calming traits conflict, and why an alchemical concentration is shorter.
- The same ingredient can have different value in a meal, a preserved stock, and an alchemical preparation. The system rewards choosing a process rather than always selecting the highest number.
- Cooking and alchemy remain optional. Ordinary vanilla food remains edible and useful without trait knowledge.

A trait record should be compact and inspectable:

| Trait | Typical use | Readable warning |
|---|---|---|
| nourishing | sustained food value | can be filling without a special effect |
| warming | cold-weather comfort | does not make extreme exposure harmless |
| refreshing | recovery and travel | short recovery window |
| calming | steadier preparation | may conflict with stimulating traits |
| stimulating | focused short activity | concentrated effects end sooner |
| bitter | reagent or balancing input | often signals an alchemy trade-off |
| restorative | recovery preparation | bounded and not a replacement for safety |
| preserving | shelf-life support | requires an appropriate process |

## Data ownership
Item stacks retain source identity, trait IDs, preparation state, and any preservation metadata in versioned components where required. Recipe, trait, effect, duration, compatibility, and acquisition definitions are data resources.

The server owns active effects and consumption outcomes. A discovered-recipe or Field Guide knowledge record belongs on the player attachment or relevant world knowledge record. Derived trait indexes may be rebuilt from item tags and definitions.

## Server/client responsibilities
The server validates ingredient identity, recipe conditions, preparation time, effect caps, and consumption. The client displays trait labels, known or unknown recipe status, duration and intensity estimates, and preservation state.

A client may preview a recipe from server-sent definitions, but it cannot grant discovery knowledge or apply an effect. Unknown modded food must be handled by a server adapter decision, not a client guess based on an item name.

## Dependencies
- FVR-200..204 cover Field Guide pages for traits, recipes, discovery, and safe preparation.
- Economy and storage provide trade, preservation stock, and physical inventory. Seasons and regions provide climate and acquisition context.
- Mastery provides Cook, Farmer/Naturalist, and Alchemist/Herbalist evidence and optional capabilities.
- Matcha-provided ingredients or recipes are reached through the isolated compatibility adapter, never through private identifiers in this system.
- All effect magnitudes, caps, trait combinations, and spoilage windows belong in versioned data.

## Extension points
A food integration can register a stable nutrition and trait mapping, while an alchemy integration can provide a reagent capability and output contract. New traits require a name, icon, explanation, interaction rules, and a bounded effect before they can be used in content.

A preservation method can register input tags, container requirements, duration band, and quality outcome. A regional provider can add an acquisition path without making its ingredient exclusive to one route.

## Failure cases
- An unknown trait or malformed recipe disables that recipe and preserves the input items.
- An external food with ambiguous tags remains ordinary food and does not receive an unsafe effect.
- A crash between preparation completion and item output commits either the original inputs or the complete output, never both.
- A removed effect definition leaves existing prepared items readable and migratable. It must not apply an arbitrary replacement effect.
- An effect cap or interaction conflict is resolved server-side and explained to the player.
- A preservation container is destroyed or inaccessible without a valid transition. The stored food remains physical and recoverable.

## Performance constraints
Trait resolution is cached by item definition and preparation state. Consumption validates one item or one bounded meal, not the entire inventory. Effect recalculation should inspect at most 16 active trait sources per player.

A recipe screen should return no more than 128 relevant recipes and 64 trait explanations at once. Spoilage or preservation updates run on scheduled buckets, not one tick per item in every warehouse.

## Accessibility and documentation requirements
Traits require text, shape or icon cues, and a short description. Duration, intensity, conflicts, and preservation state must not be conveyed by colour alone. Unknown recipes should say what kind of discovery is missing without exposing an unreadable identifier.

The Field Guide must explain the trait vocabulary, cooking versus alchemy, generic modded-food handling, all five acquisition paths, effect caps, and safe preparation. Recipes must show inputs and process steps once discovered, following principles 2 and 15.

## Non-goals
- This is not a giant hidden-combination recipe puzzle.
- It is not a mandatory hunger overhaul or a replacement for vanilla food.
- It does not make alchemy a universally stronger form of cooking.
- It does not assign traits to every modded item by guessing from its display name.
- It does not create an unbounded buff-stacking or spoilage punishment system.

## Open balance questions
- Which trait vocabulary is smallest while still supporting regional cooking and useful alchemy?
- How much stronger should concentration feel when its duration is shorter?
- Which preservation costs encourage planning without making fresh food obsolete?
- How should unknown traits be discovered without requiring external wiki research?
- How much compatibility can be generic before an explicit mod adapter becomes necessary?

## Planned tests
- Map vanilla and representative modded food inputs and verify readable traits or safe ordinary-food fallback.
- Test cooking and alchemy duration, intensity, caps, conflicts, and stacking rules.
- Discover, cultivate, preserve, import, and trade an ingredient and verify each path produces valid state.
- Crash during preparation, preservation, and consumption and assert no input duplication or deletion.
- Remove a recipe or effect definition and verify migration and clear player messaging.
- Bound effect-source evaluation, recipe payloads, and scheduled preservation work.
- Verify Field Guide pages expose exact steps for known recipes without relying on colour or external documentation.

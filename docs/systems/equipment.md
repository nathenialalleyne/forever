# Equipment

## Status
**Concept. NOT implemented.** No item condition component, craftsmanship record, mastery-path state, repair flow, or reforging service exists. The implementation backlog is FVR-100..106, with explanations and Field Guide pages in FVR-200..204.

## Purpose
Equipment should become a long-lived expression of how a player works. It can gain identity and specialised capability without turning a single perfect tool into the answer to every task. The design has three conceptual layers: craftsmanship, mastery path, and rare enchantment.

The system replaces silent item deletion with recoverable condition. A named tool can become inconvenient or unusable, but investment in it is not erased by an unnoticed durability event.

## Player experience
A new pickaxe is useful immediately. Over time, its material and build quality establish a craftsmanship profile, the player teaches it one or more mastery paths, and an uncommon enchantment may add a rare twist. The player can carry several tools whose strengths are visibly different.

A pickaxe might know Prospector, Excavator, and Delver. Prospector favours careful ore identification and selective extraction. Excavator favours broad earthmoving with reduced setup friction. Delver favours deep, dangerous, or hard material. None is best at all three jobs, and each carries opportunity costs such as speed, yield, condition use, or handling in tight spaces.

At zero condition the item becomes broken and unusable until repaired. It stays in the inventory, keeps its name and history, and can be recovered in the field or at a workshop. Reforging changes its active path or advanced properties while preserving identity and prior path progress.

## Core rules
- Craftsmanship describes the physical item: material family, maker or origin where known, construction quality, and durable identity. It affects a bounded set of trade-offs rather than adding an unrestricted power multiplier.
- A mastery path describes learned use of that item. An item may know several paths, but exactly one path is active at a time, following ADR 0005.
- Rare enchantment is a separate, scarce layer. It can add an unusual capability or interaction, but it must not erase the trade-offs of craftsmanship and path selection.
- The active path can be changed only through a valid reforging process. Reforging preserves the item identity, prior path progress, and recorded history. It restores full condition under ADR 0005.
- A path is not a second copy of player mastery. It is an item-level expression that modifies how this particular tool handles work.
- Condition is a continuous or bucketed state with named thresholds. At zero, the item is `broken`, not deleted. Condition loss never destroys the ItemStack or its component data.
- Field repair restores a partial amount and consumes an appropriate repair input. Workshop repair restores full condition when the workplace is valid. Neither repair creates a max-condition decay spiral, following ADR 0006.
- Reforging requires a workshop or equivalent registered service, a declared target path, and materials defined in data. It must show the consequences before commitment.
- No tool may be universally perfect. A path that improves one context must expose a cost, limitation, or weaker result elsewhere.
- Ordinary vanilla tools remain usable without a path. Equipment progression adds alternatives and convenience rather than gating basic mining, farming, building, or travel.
- Enchantments and path effects cannot duplicate, delete, or silently transform unrelated inventory items. Any extra output is explicit and bounded.
- Item history records notable maker, path, repair, and reforge events, not every use. A named item can qualify for a chronicle entry when its history becomes significant.

A pickaxe path comparison is intentionally directional:

| Path | Best fit | Trade-off that remains |
|---|---|---|
| Prospector | identifying and extracting selected resource veins | slower or less efficient for indiscriminate bulk clearing |
| Excavator | planned broad removal of common terrain | weaker selectivity and more condition exposure on rare blocks |
| Delver | deep shafts, hard layers, and risky confined work | higher preparation cost and less convenient surface clearing |

## Data ownership
Per-ItemStack components own item identity, schema version, craftsmanship fields, known paths, active path, path progress references, rare enchantment metadata, condition, and a compact history summary. The component must travel through drops, trades, containers, logistics, and dimension changes.

Path definitions, repair inputs, reforge costs, condition thresholds, and balance multipliers are data resources. The server stores only stable IDs and values validated against the current definition set. History uses bounded entries or aggregates to avoid making an old tool too large to serialise.

## Server/client responsibilities
The server validates item components, applies condition changes, chooses path outcomes, and performs repair or reforging atomically. The client displays a read-only projection with condition state, path trade-offs, required inputs, and history.

A client preview may calculate an estimate from server-supplied definitions, but the server remains authoritative for whether a block interaction, repair, or reforge succeeds. Tooltips need to identify broken state with text and an icon, not only a changed durability colour.

## Dependencies
- FVR-100..106 cover item data, craftsmanship, path selection, condition, repair, and reforging.
- FVR-300..305 provide player mastery context that can unlock or support item paths.
- FVR-200..204 document tool states, repairs, and path trade-offs in the Field Guide.
- Smith, Builder, Prospector, Excavator, and Delver definitions belong in versioned content data.
- Workshops and settlement workplaces are optional services, not prerequisites for ordinary tool use.

## Extension points
A new item family can register craftsmanship fields, supported paths, condition policy, repair inputs, and history events. A path provider may inspect stable work context such as block tags, depth bands, or route status without importing client code.

Rare enchantments can register mutually compatible or incompatible tags. A future repair adapter may map an external mod's item state into the same stable condition contract, provided it can preserve identity and fail safely when the source state is unknown.

## Failure cases
- An item with a missing or invalid component falls back to a safe vanilla-like state and is marked for migration. It is not deleted.
- A path definition removed from content leaves the item path as `legacy unavailable`, preserving the item and history while disabling only the missing effect.
- A reforge with missing materials, invalid workplace, or stale preview fails without consuming inputs or changing the item.
- A crash during repair or reforge must commit either the old item and inputs or the new item and consumed inputs, never a half-applied state.
- A broken item remains transferable and visible. Containers and logistics must not discard it because it cannot currently be used.
- An external enchantment that cannot be interpreted is preserved as opaque data where the platform allows, with the Forever layer disabled rather than guessed.

## Performance constraints
Condition changes are event-driven and must not trigger a full inventory scan. Item component data should target less than 2 KiB for ordinary tools, with history aggregation applied before larger records are saved.

Reforging and repair operate on one ItemStack or a bounded recipe input set. Tooltips use a cached projection. A warehouse query must never deserialize every item history merely to display a search result.

## Accessibility and documentation requirements
Tooltips must state condition as words such as `sound`, `worn`, or `broken`, include a numeric or symbolic range where useful, and provide repair instructions. Active and inactive paths need distinct labels and icons. Rare enchantments need a text description and incompatibility explanation.

The Field Guide must show the three layers, the pickaxe path comparison, the no-deletion rule, field versus workshop repair, and the identity-preserving reforge rule. Previews must list what will be lost, retained, or changed before confirmation.

## Non-goals
- This is not a durability deletion or max-durability decay system.
- It is not a universal tool that removes the need for planning, infrastructure, or multiple item choices.
- It does not require a player to specialise before using ordinary tools.
- It does not make rare enchantments common or allow unlimited stacking of path effects.
- It does not turn every item into a detailed role-playing character with a long event log.

## Open balance questions
- How much craftsmanship variation is readable before players perceive random unfairness?
- Should the three pickaxe paths be available from one item family or introduced through separate discovery routes?
- What repair input creates meaningful preparation without making field repair a mandatory chore?
- How rare should enchantments be, and which trade-offs must they never bypass?
- Which history events deserve to be shown on an item and which belong only in the world chronicle?

## Planned tests
- Round-trip an item through crafting, drop, trade, container, shipment, and dimension transfer while preserving all three layers.
- Reduce condition to zero and assert the item remains present, named, transferable, and repairable.
- Test field repair, workshop repair, and reforging with success, missing-input, stale-preview, and crash-interruption fixtures.
- Verify a pickaxe can know all three paths but has only one active path at a time.
- Verify reforge preserves identity, prior path progress, and history while restoring full condition.
- Load an unknown path and malformed component and assert safe migration without item deletion.
- Bound component and history sizes and profile tooltip rendering for large inventories.

# ADR 0030: Do not apply global enemy scaling

- Date: 2026-08-28
- Related principles/ADRs: [Principles 1, 8, 9, 12, 15, 16, and 17](../design-principles.md), [ADR 0024](0024-modpack-first-product-ownership.md), [ADR 0028](0028-adventure-rewards-not-essential-gates.md)

## Status

Accepted. This is the pack-wide danger and difficulty rule.

## Context

Global enemy scaling is a familiar way to make a world feel harder over time or to keep late-game players challenged. A multiplier on health, damage, speed, or spawn pressure is simple to describe and easy to apply, but it makes every encounter less predictable. It can turn a peaceful home biome into a late-game tax, invalidate the balance of third-party mobs and equipment, make multiplayer players with different progress states disagree about the same entity, and make ordinary exploration feel like a hidden level system.

The vision wants geography and infrastructure to matter. Danger should be legible through place, preparation, weather, structure, objective, and consequence rather than a global number that rises because the calendar advanced. The pack also includes existing mods whose own encounter or dimension rules may be valid in their intended scope. A global multiplier would compete with those rules without understanding their contracts.

No global scaling does not mean no danger. Regions, structures, local encounters, environmental hazards, enemy composition, objectives, and data-defined situations can make a trip demanding. The trade-off is that challenge must be designed in context and tested across the assembled pack rather than obtained from one universal formula.

## Decision

Many Roads Home will not apply a global multiplier to hostile entity health, damage, speed, knockback, spawn rate, or similar combat attributes based on world age, total play time, distance from spawn, player level, or overall progression. Existing mod or vanilla values remain the default unless a specific local encounter or source mod owns a documented change.

Difficulty may vary through bounded, local, and inspectable context such as a structure encounter, a regional hazard, a weather situation, a declared project, an enemy composition, or a data-defined objective. Any local modifier must identify its scope, source, duration, affected entities, reason, and safe fallback. It must not silently become a global rule through a shared tag or an unbounded event listener.

A danger system must communicate relevant conditions with text, icons, sound where useful, and non-colour cues. It must not make combat or travel a required progression gate for ordinary building, shelter, food, or basic survival. Adventure rewards may provide preparation, information, or optional advantages under [ADR 0028](0028-adventure-rewards-not-essential-gates.md), but a missing reward cannot be the only way to remain viable.

Balance values belong in data and must be tested against vanilla and selected third-party entities. If a candidate mod requires global scaling for its own content, the compatibility spike must evaluate a scoped configuration or a different candidate rather than applying the multiplier to the whole pack.

## Consequences

- Encounters remain closer to the source mod and vanilla expectations, which reduces hidden progression coupling and improves multiplayer predictability.
- The pack can make a region or structure dangerous without making every home, mine, and road scale against the player.
- Late-game challenge requires more authored context. Encounter composition, hazards, objectives, and rewards need data, testing, and clear explanations.
- Players who expect a universal late-game increase may find the world less threatening outside designed situations. The pack must use discovery, geography, and meaningful consequences to create tension instead.
- Mod interactions become easier to reason about, but a local modifier can still conflict with an upstream mob or dimension rule. Source ownership and scope must be recorded.
- Existing worlds avoid a sudden global attribute change when progression or configuration changes. Local encounter records can still require migration if they persist.
- Accessibility reviews are more involved because danger is communicated through several contextual signals rather than one obvious scaling rule.

## Rejected alternatives

### Scale every enemy by world age

This is easy to implement, but it makes time itself a universal difficulty tax and punishes ordinary long-term settlement play. It also offers no explanation for why a familiar home creature suddenly has different properties.

### Scale enemies to the strongest nearby player

This can keep multiplayer encounters challenging, but it makes one player's progress alter another player's local world and creates unstable results when players join or leave. It also turns a hidden power score into a server-wide authority.

### Scale each dimension globally

Dimension-wide scaling is more contextual than world-age scaling, but it still flattens regional variety and can conflict with the rules of an existing dimension or adventure mod. A specific encounter can be scoped more honestly.

### Avoid all authored danger

Removing modifiers entirely would preserve source balance but would waste the pack's exploration and regional design space. The decision rejects global scaling, not local and legible challenge.

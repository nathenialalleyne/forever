# ADR 0028: Do not make adventure rewards the sole route to essential capabilities

- Date: 2026-08-28
- Related principles/ADRs: [Principles 1, 2, 8, 9, 15, and 17](../design-principles.md), [ADR 0015](0015-conservative-worldgen-posture.md), [ADR 0024](0024-modpack-first-product-ownership.md), [ADR 0029](0029-unified-contextual-knowledge.md), [ADR 0031](0031-fast-travel-requires-established-routes.md)

## Status

Accepted. This is a progression and accessibility rule for pack content.

## Context

Adventure content is a useful way to give places, structures, and discoveries meaning. A rare structure can introduce a technique, a specialist, a route, an ingredient, an artifact, or a project. The reward is strongest when it changes what a player notices or chooses rather than merely filling a chest. The danger is that a reward can become the only key to a capability that the player needs for ordinary survival or for the rest of the pack.

A single adventure gate tends to turn exploration into a mandatory checklist. It can also make a bad spawn, a missing structure, a multiplayer absence, or an inaccessible route block a solo world. That conflicts with the vision's parity goal, the protection of ordinary Minecraft building, and the rule that discovery may hide a possibility but not the instructions for a process the player is expected to perform. It also makes third-party adventure content an authority over basic pack operation.

There is a real trade-off. If every important reward has an equally direct alternative, the adventure may feel optional in a way that weakens its meaning. The design therefore needs to separate essential capability from distinctive opportunity. Exploration can be the first or best way to discover a possibility, provide a variant, or gain a convenience without being the exclusive route to basic participation.

## Decision

Adventure rewards must not be the exclusive gate for an essential capability. An essential capability is one needed for ordinary shelter, food, building, inventory use, baseline repair and recovery, readable instructions, basic travel, or participation in the pack's core loops. Each such capability must be available through vanilla behaviour, configuration, a predictable local route, or another documented non-adventure path.

Adventure rewards may instead provide distinctive knowledge, optional specialisation, a better service, a rare variant, a regional opportunity, a shortcut, a cosmetic or historical item, or the first evidence that a capability exists. A reward may be the most efficient route without being the only route. If an adventure introduces a required process, the process must be explained in the unified Field Journal and any relevant recipe viewer under [ADR 0029](0029-unified-contextual-knowledge.md).

Every proposed adventure reward must record its capability class, alternate acquisition route, failure behaviour when the structure is unavailable, and solo-play path. A structure or quest may gate a possibility until discovery, but it may not withhold the instructions for a process that the player is already expected to perform. Rewards must not require a particular architectural style, a multiplayer party, or repeated structure farming to remain viable.

An alternative route may carry different cost, risk, time, or regional context. It must still be real, inspectable, and attainable without the adventure reward. Balance values and reward rates belong in data. A later design that wants to make a capability adventure-exclusive requires a superseding ADR and an explicit review of ordinary-play and solo consequences.

## Consequences

- Players can explore for meaningful opportunities without being trapped by an absent structure, unlucky world generation, or an unavailable teammate.
- Adventure content has to offer quality, context, and distinctive consequences rather than relying on a single mandatory unlock.
- Pack balance becomes more difficult because alternate routes must remain useful without making the adventure reward irrelevant.
- The Field Journal must explain both the discovered route and any alternate route. This creates content and localisation work and makes silent quest configuration insufficient.
- A player may reach a capability without seeing the intended adventure, so the design cannot assume a fixed narrative order.
- Structure availability, reward uniqueness, and alternate-route records need migration-aware identifiers if they affect persistent knowledge or world claims.
- Some popular quest or structure mods may be unsuitable if they hard-gate their own essential features and expose no configuration. The escalation policy may require disabling or replacing their gate.

## Rejected alternatives

### Put every useful capability behind adventure rewards

This creates strong short-term motivation, but it turns the pack into a quest gate and makes world generation or party composition decide whether ordinary play is possible. It conflicts with the vision's open routes and solo requirement.

### Remove all rewards from exploration

This avoids gating, but it makes regions and structures less meaningful and wastes the opportunity to reward discovery with knowledge, people, history, and convenience. The decision preserves optional rewards while limiting their authority over essentials.

### Make the alternate route a hidden technical workaround

A route that exists only in code or an external wiki is not accessible design. The player needs an inspectable process and a visible reason when a route differs in cost or risk.

### Allow repeatable structure farming to supply the alternative

A farmable loot loop would recreate the repetition problem in a new location and could make world generation the dominant source of progress. Essential alternatives must be reliable enough not to depend on repeated rare loot.

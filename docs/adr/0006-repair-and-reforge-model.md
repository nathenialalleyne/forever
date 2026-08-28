# ADR 0006: Use repair and reforging instead of equipment deletion

- Date: 2026-08-28
- Related principles/ADRs: [Principles 5, 6, 12, 14, 15, 17, and 18](../design-principles.md), [ADR 0004](0004-player-mastery-loadout.md), [ADR 0005](0005-item-specialization-paths.md), [ADR 0017](0017-player-shop-pricing.md)

## Status

Accepted. This is a locked equipment durability decision.

## Context

Forever is intended for worlds that players inhabit for years. A named tool with a long
history should be an object the player maintains, not a disposable input to a production
loop. Vanilla durability reaching zero currently creates a sharp failure state, and a
player can lose an important item simply by missing a small UI warning. Principle 5
therefore requires that zero condition make an item unusable rather than delete it.

The wider vision also matters. If the only dependable route to replacement durability is
an XP grinder, a trading hall, or a repair farm, the system reinforces the anti-AFK-farm
checklist. Conversely, if repair is free and instant everywhere, workshops and skilled
craftsmanship have no value. The desired progression is repeated effort replaced by
infrastructure, not repeated equipment replacement disguised as a sink.

Player and item specialisation are separate decisions under [ADR 0004](0004-player-mastery-loadout.md)
and [ADR 0005](0005-item-specialization-paths.md). A path change must not erase an item's
identity, and a repair must not unexpectedly change the path. Persistence across dropped
stacks, containers, trades, and migrations is part of the problem, not a later detail.

There are competing goals: retain attachment, preserve a meaningful material economy,
keep field expeditions viable, and avoid a max-durability spiral that makes old equipment
slowly worthless. Balance values will change during playtesting, so Principle 12 requires
that costs, rates, and thresholds live in data rather than being buried in code.

## Decision

An item whose condition reaches zero becomes **broken** and unusable until repaired. The
ItemStack remains present with its identity, valid enchantments, path history, and other
validated metadata. Breaking does not silently delete, reset, or replace the item.

Three service levels are defined:

- **Field repair** is available away from a full workshop and restores only a configured
  partial amount of condition. It is a way to finish an expedition, not the best service.
- **Workshop repair** restores an item to full condition through an appropriate service
  and its configured materials, time, or specialist requirements.
- **Reforging** restores full condition and permits advanced changes such as selecting a
  different item specialisation path. Reforging follows [ADR 0005](0005-item-specialization-paths.md)
  and preserves the item's identity and retained path progress.

No repair path causes maximum durability to decay over successive uses. A well-maintained
item may remain useful indefinitely, subject to the costs and decisions of using it.
Repair, reforge, and condition rules are server-authoritative. Costs, field-repair caps,
service requirements, and balance multipliers are data-driven and validated on load.

A repair transaction is atomic. If the service cannot complete, the item and materials
remain recoverable rather than producing a half-written stack. Item condition and reforge
metadata use explicit component schema versions and safe migration paths.

## Consequences

- A player can recover a treasured tool instead of losing it to an unnoticed durability
  tick. This directly supports long-lived worlds and Principle 18.
- Field repair keeps exploration viable without making a workshop irrelevant. Workshops,
  settlements, and specialists can turn a recurring trip into earned convenience.
- Reforging gives equipment history a mechanical value and creates a natural place to
  change an active path without making the player craft a replacement.
- The model can reduce dependence on XP grinders and replacement-tool production, helping
  alternative routes compete with the anti-AFK-farm checklist.
- Broken items remain in inventories and may feel like clutter until the player reaches a
  service. UI must make the broken state explicit with text, icons, and tooltips, not only
  a colour change.
- Repair introduces logistics and material costs. A player who prefers constant field
  exploration may experience more interruption than with unrestricted Mending.
- Infinite maximum durability can weaken resource sinks and reduce demand for new tools.
  The accepted answer is to balance service inputs, craftsmanship, and performance in data,
  not to make old items decay invisibly.
- A second representation of condition alongside vanilla durability creates conversion
  and compatibility work, especially for Matcha or other items that already alter damage.
- Failed or interrupted service transactions are an exploit surface. Tests must cover
  disconnects, container movement, concurrent use, invalid components, and server restart.
- Future work includes repair stations, recipes, field-service UX, balance data, migration
  tests, integration with item paths, and Field Guide explanations of each service level.

## Rejected alternatives

### Vanilla deletion at zero durability

Deletion is familiar and simple, but it destroys named equipment without adding an
interesting decision. It contradicts Principle 5 and undermines the emotional continuity
that a world intended to last for years should create.

### Mending-only repair

Relying on Mending would preserve items, but it concentrates repair in enchantments and
usually makes XP generation the dominant support system. That would reward the same XP
grinder path Forever is trying to make optional. It also gives field repair no deliberate
relationship to workshops or settlement infrastructure.

### Max-durability decay from repeated repairs

Many RPG-style systems lower an item's maximum condition after each repair. That produces
a visible resource sink, but it creates a decay spiral in which older, meaningful items
become objectively worse and eventual replacement is mandatory. It conflicts with the
long-lived-world goal and turns maintenance into attrition rather than earned convenience.

### Free repair anywhere

This would remove the feel-bad of broken equipment, but it would also remove the reason to
build a workshop, employ a specialist, or plan a route. It would make repair a UI command
rather than part of the civilisation arc.

### Automatic replacement with a fresh equivalent

A replacement avoids the broken state, but it discards names, path history, enchantments,
and material identity. It treats equipment as a disposable commodity and makes player
attachment a liability.

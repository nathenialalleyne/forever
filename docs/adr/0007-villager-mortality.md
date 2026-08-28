# ADR 0007: Make villager mortality causal and bounded

- Date: 2026-08-28
- Related principles/ADRs: [Principles 6, 7, 9, 10, 11, 14, 15, and 18](../design-principles.md), [ADR 0009](0009-settlements-as-building-graphs.md), [ADR 0016](0016-existing-village-import.md), [ADR 0018](0018-infrastructure-gated-journey-skipping.md)

## Status

Accepted. This is a locked NPC mortality decision.

## Context

Forever wants villagers to be people in a settlement rather than vending machines behind
a trading hall. Careers, mentors, apprentices, techniques, and institutional knowledge
make a village part of the long arc in which the world becomes easier to inhabit. Mortality
can give those relationships stakes, and a villager may die through a real physical cause.
That makes the settlement route a meaningful alternative to a static trading hall and the
anti-AFK-farm checklist. Principles 6, 9, and 11 define the relevant value and risk.

There is an important limit. A player who was not present should not receive an arbitrary
message that an invested-in villager fell into an unseen quarry. That outcome is random,
unfalsifiable, and contrary to Principle 11. At the same time, physically simulating every
unloaded villager would require permanent chunk loading or unbounded work, violating
Principle 10 and risking server performance.

The design therefore has to separate loaded physical causality from bounded unloaded
abstraction. It also has to distinguish a person's experience from knowledge the
settlement has deliberately taught and recorded. A death should matter without erasing
all of a settlement's accumulated progress or making one missing NPC destroy solo play.

Natural villages and imported settlements add edge cases. Existing buildings may be
unsafe, villagers may be renamed or moved, and a player may deliberately kill someone.
Those events need consequences without requiring a style score or a permanent chunk
loaded around every settlement.

## Decision

Villagers may die. A death must have a physically simulated or otherwise attributable
cause, such as a hostile attack, fire, fall, suffocation, drowning, or deliberate player
violence. The event records enough causal information for the settlement and player-facing
history to explain what happened when that is possible.

When practical, a vulnerable villager enters a short **downed** or rescue state before
final death. Rescue is an opportunity, not an immortality guarantee. The exact duration,
causes, and intervention requirements are balance data and must not make normal play
unintelligible.

Unloaded settlements use bounded abstract simulation. That simulation may advance needs,
production, travel, apprenticeship, and other explicitly modelled state, but it may not
randomly kill a villager because a tick occurred while the settlement was unloaded. A
physical hazard is resolved through actual loaded simulation, or through a deterministic
and attributable event when a later architecture decision explicitly models that hazard.
It must never be an invisible dice roll.

Deliberate murder is allowed as an in-world action, but the responsible player incurs
settlement, witness, and reputation consequences. The consequence system is not a blanket
restriction on combat. It makes intentional violence legible and prevents an exploit in
which replacing a skilled villager is consequence-free.

Institutional knowledge survives when it has been taught, mentored, or recorded in the
settlement. Untaught personal knowledge belongs to the individual and may be lost on
that individual's death. Compact personal state is stored with the entity, while shared
knowledge and history belong to versioned world or settlement records.

## Consequences

- Villagers can become valued characters with meaningful histories rather than infinite
  interfaces. A death can be sad, understandable, and recoverable when rescue is possible.
- Players are protected from the most frustrating form of loss: an off-screen random death
  they could neither observe nor prevent.
- Bounded abstract simulation preserves the no-permanent-chunk-loading rule and keeps a
  world with many settlements within a predictable tick budget.
- Taught knowledge gives settlements continuity. A mentor's death is a loss, but it does
  not necessarily erase a technique that the community deliberately preserved.
- Deliberate murder has social consequences, which creates room for law, reputation, and
  story without making villagers mechanically immortal.
- Causal attribution is technically difficult. Minecraft hazards can interact across
  ticks, dimensions, and third-party mechanics, and a misleading cause report is worse
  than a less detailed one.
- Players may perceive unloaded villagers as unusually safe compared with loaded ones.
  This asymmetry is accepted because arbitrary off-screen death is worse, but the Field
  Guide must explain the boundary and loaded hazards must remain meaningful.
- The downed state adds entity, networking, AI, and recovery complexity. It can also be
  exploited as a pause before combat or used to retain a villager indefinitely if rescue
  rules are too generous.
- Knowledge inheritance requires careful distinction between personal components and
  settlement records, plus explicit schema migrations. Duplication and accidental loss
  are save-integrity risks under Principle 18.
- Future work includes the causal event model, rescue interactions, apprenticeships,
  reputation consequences, bounded simulation rules, chronicle entries, and tests around
  unload/reload, server restart, and imported villages.

## Rejected alternatives

### Immortal villagers

Making every villager invulnerable would eliminate arbitrary loss, but it would also make
villagers feel like protected interfaces rather than inhabitants. It removes meaningful
stakes and makes deliberate violence, rescue, and succession impossible to model.

### Random death during abstract simulation

A random unloaded mortality roll would be cheap to implement, but it violates Principle
11 exactly. The player could not identify a cause or have prevented it, and a large
settlement would become a lottery whose cost is paid by the save's history.

### Freeze all unloaded villagers

Freezing every NPC would avoid both random death and simulation cost, but it would make
settlement infrastructure stop existing whenever the player travelled away. It would also
make trade, apprenticeship, and the world's long arc depend on permanent proximity.
Bounded abstract progress is the better compromise.

### Force-load every settlement for full physical simulation

This would make hazards and careers consistent with loaded play, but it would violate
Principle 10 and impose a predictable performance cost that scales with settlement count.
It is not acceptable merely because it is conceptually simple.

### Permanent death with no downed or succession support

Unconditional final death would create stakes, but it would turn a single bad encounter
into a disproportionate loss and make the system hostile to solo players. Rescue where
practical and institutional knowledge reduce that cost without removing mortality.

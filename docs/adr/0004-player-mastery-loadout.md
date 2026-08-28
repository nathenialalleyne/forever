# ADR 0004: Use a configurable player mastery loadout

- Date: 2026-08-28
- Related principles/ADRs: [Principles 1, 3, 4, 6, 9, 12, 14, 15, and 17](../design-principles.md), [ADR 0003](0003-classify-matcha-mechanics-individually.md), [ADR 0005](0005-item-specialization-paths.md), [ADR 0018](0018-infrastructure-gated-journey-skipping.md)

## Status

Accepted. This is a locked progression decision.

## Context

Forever's premise is that a player should be able to reach abundance through a chosen
specialty, a settlement, trade, or infrastructure instead of being compelled to complete
the anti-AFK-farm checklist. Mastery is meant to reward breadth, discovery, technique,
and completed projects. A raw action counter would simply put the old repetition treadmill
inside a new menu, which conflicts with Principle 4.

The design also has to work for a solo player and for a multiplayer group. A server can
benefit from a smith, cartographer, and merchant, but a player must not lose a decade of
progress because a group changed roles. The interesting decision is what the player is
prepared to do now, not an irreversible choice made before the system was understood.

An unrestricted set of active specialties would remove meaningful focus and make every
player a universal best-in-slot character. A single permanent class would create the
opposite problem: experimentation would be punished, and players would feel obliged to
keep one early choice forever. The loadout must create a manageable active context while
preserving learning.

Mastery is player state, so it travels with the player and must be versioned separately
from large world records. It must be server-authoritative and remain independent from
ordinary block placement. No player should need a mastery level to build normally, as
required by Principle 1.

## Decision

A player has three active mastery slots:

- **One Focus** specialty, which receives the strongest active emphasis.
- **Two Supporting** specialties, which provide narrower complementary benefits.

The loadout limits active emphasis, not what the player is allowed to learn. All mastery
progress is permanent for that player. Inactive specialties retain their levels and do
not require re-levelling when selected again.

Changing the loadout requires an in-world rest, home, or appropriate workplace
interaction. The interaction represents a deliberate change of working context and gives
settlements and infrastructure a useful role. It does not consume mastery XP, erase
progress, impose a recurring tax, or create a full-respec penalty.

Mastery acquisition follows the breadth, discovery, technique, and project principles.
The exact thresholds, rewards, and multipliers are balance data under Principle 12. They
must not become disguised repetition counters without a new decision. Active benefits
must also remain legible through the Field Guide and other non-colour cues.

The loadout is stored in compact, versioned player state. The server validates changes,
calculates outcomes, and synchronises a bounded summary to the client. A client screen
may propose a loadout but never authorise one.

## Consequences

- Players can develop a durable identity without being trapped by it. A builder can learn
  cartography and later make cartography active without losing building progress.
- Three slots create a meaningful planning question. A Focus and two Supports can express
  a current project while leaving room for complementary skills.
- Multiplayer specialisation remains valuable because active emphasis is limited, while
  solo players can eventually learn every path needed for their own world.
- The rest, home, or workplace interaction makes switching part of the world rather than
  a purely out-of-character menu operation. It also gives infrastructure a reason to
  matter without charging an ongoing convenience tax.
- Active-slot balance becomes a major tuning risk. If Supporting effects are too strong,
  every player will activate the same universal trio. If they are too weak, the slots are
  decorative. This must be tested with real projects, not solved by permanent lock-in.
- Switching is not instantaneous. A player who arrives at an unexpected task without a
  suitable context may need to travel or wait, which is intentional friction but can feel
  inconvenient. The interaction must remain achievable in solo play.
- Permanent progress increases save size and migration responsibility. The player record
  needs explicit schema versions, safe defaults, and duplicate or invalid-entry handling.
- Rapid switching at a workplace could become an exploit if active bonuses affect a
  single transaction. Server-side action boundaries and data-driven switching rules are
  required before implementation.
- Future work includes the mastery vocabulary, progression evidence model, loadout UI,
  Field Guide explanations, balancing data, network payloads, and tests for save migration
  and switching around restarts or dimension changes.
- We accept that the system will be less immediately legible than a single class bar.
  The Field Guide and clear active/inactive indicators are required, not optional polish.

## Rejected alternatives

### One permanently active class

A single class would be easy to explain and balance, but it would make early mistakes
expensive and turn multiplayer role choice into a permanent partition. It conflicts with
Principle 3 and would discourage the experimentation that makes a long-lived world worth
returning to.

### Full respec with a substantial cost

Charging XP, materials, or a recurring fee for a complete respec could create an economy
sink, but it would turn learning into a risk calculation. Players would keep a known
specialty even when another would make the current project more enjoyable. The deliberate
interaction is enough friction for context without deleting learning.

### Skill trees with permanent commitment

Permanent branches can create strong identity, but they are hostile to discovery and
require players to understand the entire tree before choosing. They also favour external
wikis, contrary to Principles 2 and 15. The loadout supplies temporary commitment while
retaining the complete learning history.

### All learned specialties active at once

This would maximise convenience for a veteran, but it would erase specialisation as a
meaningful choice and make every alternative route compete against a universal character.
It would also create a large balance surface and reduce multiplayer complementarity.

### Unrestricted hot-swapping from the inventory screen

Instant changes would be convenient, but would let players switch to the optimal bonus for
every individual action. Requiring a rest, home, or workplace context preserves planning
without a permanent commitment or XP loss.

# ADR 0003: Classify Matcha mechanics individually

- Date: 2026-08-28
- Related principles/ADRs: [Principles 2, 4, 12, 13, 15, and 18](../design-principles.md), [ADR 0001](0001-use-official-matcha-as-starting-pack.md), [ADR 0002](0002-isolate-matcha-behind-adapter.md), [ADR 0013](0013-elytra-as-glider.md), [ADR 0015](0015-conservative-worldgen-posture.md)

## Status

Accepted. This is a locked audit and migration policy.

## Context

The vision identifies a specific failure mode, not a single guilty mod. Late-game
Minecraft converges on the same anti-AFK-farm checklist of farms, trading halls, XP
grinders, and Elytra because those
solutions overwhelm alternatives. Forever therefore needs to compare actual mechanics
against a parity goal. It must not assume that every mechanic in the starting pack is
helpful or that every change that feels restrictive is automatically justified.

Matcha provides a broad baseline under [ADR 0001](0001-use-official-matcha-as-starting-pack.md),
but its mechanics will have different relationships to Forever's principles. One may
support breadth-based mastery. Another may recreate a repetition counter. A third may
be harmless infrastructure, while a fourth may undermine normal Minecraft building or
world-save safety.

A single keep-or-remove decision would hide those differences. A wholesale fork would
also force the project to maintain and balance mechanics before their interactions are
understood. The architecture needs a review unit small enough to change one behaviour
without making an unrelated system inherit the same decision.

The review must be useful to future agents. Principle 2 requires known processes to be
explainable in-game, Principle 12 puts tuning in data, and Principle 18 puts migrations
and safe failure above feature completeness. A label without evidence would become a
new kind of undocumented assumption.

## Decision

Every significant Matcha mechanic is recorded and classified independently as one of:

- **Keep:** the observed behaviour aligns with the vision and can remain the baseline.
- **Extend:** the baseline is sound, but Forever adds capability around it without
  changing its core contract.
- **Override:** the mechanic remains conceptually useful, but Forever changes its
  behaviour or balance to satisfy an explicit principle.
- **Replace eventually:** the current implementation is only a temporary baseline and
  has a named Forever replacement or migration direction.
- **Undecided:** evidence is insufficient, or the interaction with another system needs
  an experiment before a safe decision can be made.

Each classification records the mechanic's player-facing purpose, observed inputs and
outputs, private Matcha identifiers, save impact, relevant principles, known conflicts,
compatibility tests, and the reason for the label. The private identifiers live behind
[ADR 0002](0002-isolate-matcha-behind-adapter.md), not in the classification's callers.

Classification is a design and audit record, not a promise that implementation begins in
the same milestone. An `undecided` item is visible decision debt. It cannot be silently
treated as `keep` merely because that is the easiest integration path.

When a mechanic crosses a boundary such as a new persistent format, a new economy rule,
or a direct conflict with a principle, the classification points to a separate ADR or
superseding decision. Balance values remain data-driven under Principle 12.

## Consequences

- The project can preserve useful parts of Matcha while targeting specific sources of
  checklist dominance instead of applying a blanket ideological verdict.
- Audit effort produces a map of what Forever owns, what it delegates, and what must be
  migrated. That makes later implementation and agent handoffs more predictable.
- The approach supports incremental delivery. A safe `keep` can be used while an
  `eventually replace` mechanic is designed and tested separately.
- Players get a better chance of seeing a coherent experience because each override must
  explain its purpose in terms of the vision and the Field Guide requirements.
- The audit is expensive. It requires reproducing behaviour, tracing interactions, and
  documenting data and save implications rather than checking filenames alone.
- Classification boundaries may be difficult when one Matcha function implements several
  mechanics. The record may need to split a code path conceptually while the adapter
  temporarily handles it as one integration unit.
- The pack may temporarily contain inconsistent mechanics while replacements are staged.
  This is an accepted transition risk, and compatibility notes must make it explicit.
- Too many `undecided` labels can become a way to avoid hard design work. Reviews need
  evidence, a next experiment, or a reason that the item is safely deferred.
- Future work includes the audit report, representative worlds, interaction tests,
  per-mechanic Field Guide entries, and migration plans before replacing persistent state.
- A classification can be superseded when evidence changes. The old record remains useful
  history under the append-only ADR process described in `README.md`.

## Rejected alternatives

### Keep all Matcha mechanics unchanged

This would minimise immediate integration work, but it would assume that the starting
pack already solves Forever's design problem. It could preserve a new set of mandatory
grinds or make Elytra and generic farms dominant again. The vision requires evidence of
parity, not loyalty to the first implementation.

### Remove or replace Matcha wholesale

A clean slate would simplify conceptual ownership, but it would discard useful tested
behaviour and create a large unvalidated implementation commitment. It also makes it
harder to identify which specific mechanic caused a conflict. Individual classification
keeps the scope proportional to the evidence.

### Fork first and audit later

A fork can make changes feel easy, but it turns unknown assumptions into maintained code
and obscures which upstream material was retained. The pinned archive and adapter provide
a controlled baseline while the audit determines whether a fork is justified.

### Wait until every mechanic is understood

Perfect knowledge before any foundation work is impractical and would delay useful audit
tooling. The `undecided` category allows safe, explicit uncertainty without pretending
that unresolved behaviour has been approved.

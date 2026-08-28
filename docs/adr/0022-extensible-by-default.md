# ADR 0022: Build every system as an extension surface

- Date: 2026-08-28
- Related principles/ADRs: [Principles 1, 12, 13, 15, and 18](../design-principles.md), [ADR 0002](0002-isolate-matcha-behind-adapter.md), [ADR 0003](0003-classify-matcha-mechanics-individually.md), [ADR 0021](0021-layered-feature-packages.md)

## Status

Accepted.

## Context

Forever is a framework and a curated pack foundation, not a single finished game mode. Its
value depends on new gameplay being cheap to add later: more masteries, more professions,
more building types, more transport modes, more food traits. If each addition requires
editing a switch statement, adding an enum constant, and recompiling, the project becomes
the bottleneck for its own content, and a third party cannot extend it at all.

The codebase already contains good instincts that were never written down as a rule.
`SettlementWorldView` is a genuine port: a minimal interface over the server world that
deliberately exposes no scan or chunk-loading operation, which keeps bounded validators
testable and stops a caller reaching for the whole `ServerLevel`.
`GuideRecipeViewerCapability` is a genuine optional seam: it names only Forever-owned
types so that REI or JEI can be adapted later without either becoming a dependency.
`MatchaAdapter` is the same idea applied to a third-party datapack, with a no-op fallback
when the pack is absent or unverified. Balance values, mastery definitions, Mason data,
and Field Guide entries already load from data rather than code.

Those are four different expressions of one principle that no document states, so each
new system re-decides it. A survey during the ADR 0021 migration found the gap made
concrete: six resource loaders filter their input with
`id.getNamespace().equals("forever")`, in guide, mastery, career, economy, settlement, and
storage. Every one of those systems is data-driven in shape but closed in practice,
because a datapack in any other namespace is silently ignored. The extension point exists
and is then locked to a single author.

The counter-pressure is real and must be respected. `AGENTS.md` forbids premature
abstraction, empty placeholder classes, and future package scaffolding. An interface with
one implementation, invented for a caller that does not exist, is a cost with no benefit:
it adds indirection, obscures the call path, and usually guesses the wrong shape. This ADR
must not become a licence to wrap everything in a factory.

## Decision

A system is extensible by default along three specific seams, and abstraction beyond those
seams still requires a real second caller.

### 1. Content is data, and data is open to any namespace

Anything that is a catalogue entry rather than a mechanic belongs in a datapack-loadable
resource: masteries, guide entries, professions, ranks, building types, balance values,
catalogue rows. A loader must accept **any namespace**, not only `forever`. Validation
stays strict, unknown fields are still rejected, and an invalid entry still fails loudly
with an actionable message naming the offending resource.

Namespace-locking a loader is a defect unless a specific, recorded reason requires it. The
existing six loaders are recorded as known exceptions below rather than silently accepted.

### 2. Behaviour that varies by environment goes behind a port

When a system needs something the server or another mod owns, define a small interface in
Forever's own vocabulary and adapt to it, rather than depending on the foreign type
directly. `SettlementWorldView` and `GuideRecipeViewerCapability` are the reference
implementations. A port must:

- name only Forever-owned or plain Java types in its signature;
- expose the narrowest operation the caller actually needs, never a general escape hatch;
- have a neutral, safe fallback when no implementation is present, following the
  `NoOpMatchaAdapter` precedent, so an absent integration degrades rather than crashes; and
- live at the layer that owns the concept, per ADR 0021.

### 3. Results are values, not booleans

An operation that can fail returns a described result carrying the reason, as
`ProgressionResult`, `WarehouseMutationResult`, and `LoadoutSwitchResult` already do. This
is what lets a new caller, a new UI, a Field Guide entry, or a future automation surface
explain an outcome without re-deriving it. A bare `boolean` throws away the only
information an extension needs, and it makes principle 15 harder to satisfy later.

### What this does not authorise

Do not create an interface for a single implementation with no second caller in sight. Do
not add an abstract base class where a record and a function will do. Do not build a
plugin registry for a system with one entry. Prefer composition and data over inheritance;
an abstract base class is the last option considered, not the first. When in doubt, ship
the concrete implementation and extract the seam when the second caller genuinely arrives.
Extracting a seam later is cheap because the layering in ADR 0021 keeps the call sites
visible; guessing the wrong seam early is expensive because every caller inherits the
mistake.

## Known exceptions

Six loaders currently filter resources to the `forever` namespace:
`GuideRegistryLoader`, `GuideEntry` identifier validation, `MasteryRegistryLoader`,
`MasonDataLoader`, `EconomyBalanceLoader`, and `SettlementBalanceLoader`. This is recorded
as a defect rather than a design. Opening them changes load behaviour and merge semantics,
including what happens when two datapacks define the same identifier, so it needs its own
ticket with tests for override order, duplicate identifiers, and invalid third-party input.
Each is therefore left in place and named here, following the ADR 0021 precedent of
recording an honest exception instead of forcing a change during unrelated work.

## Consequences

- New content arrives as data, so adding a mastery or guide entry needs no recompile and,
  once the namespace exceptions are resolved, no access to this repository.
- Systems stay testable, because a port can be implemented by a plain test double without
  a Minecraft runtime, which reinforces the domain purity rule from ADR 0021.
- Optional integrations stay optional. A missing viewer, pack, or mod produces a neutral
  fallback rather than a crash, consistent with principle 18.
- There is a real cost. A port is indirection, and indirection makes a call path harder to
  follow. That cost is accepted only where the seam is justified by data-driven content,
  environment variation, or an optional integration.
- The judgement of "is this seam earned yet" cannot be automated. ArchUnit can enforce
  dependency direction but not whether an abstraction deserves to exist, so this remains a
  review question for humans.

# ADR 0002: Isolate Matcha behind a compatibility adapter

- Date: 2026-08-28
- Related principles/ADRs: [Principles 12, 13, 14, 15, and 18](../design-principles.md), [ADR 0001](0001-use-official-matcha-as-starting-pack.md), [ADR 0003](0003-classify-matcha-mechanics-individually.md), [ADR 0020](0020-private-project-license-separation.md)

## Status

Accepted. This is a locked architecture boundary.

## Context

Forever's vision is not to replace Minecraft with a closed progression game. It is to
make routes such as mastery, settlement building, trade, and transport competitive with
the anti-AFK-farm checklist that otherwise dominates late survival. Matcha is the
starting gameplay pack under [ADR 0001](0001-use-official-matcha-as-starting-pack.md),
but it is a third-party datapack and resource-pack archive, not a stable library API.

Matcha's observable behaviour may be implemented through scoreboards, function paths,
advancement IDs, disguised item identities, custom model data, recipe tricks, and other
private conventions. Those identifiers are implementation details, not Forever concepts.
An upstream release can rename them or change their meaning without providing a source
compatibility promise.

If those details are imported directly by mastery, equipment, settlement, or client code,
a Matcha update becomes a project-wide breakage. It also becomes difficult to replace one
mechanic without rewriting every caller. That is the opposite of the architecture's
small API surface and the world-save priority in Principle 18.

The boundary must also accommodate uncertainty. [ADR 0003](0003-classify-matcha-mechanics-individually.md)
will classify mechanics as keep, extend, override, replace eventually, or undecided.
The integration shape therefore needs to support partial adoption rather than assume
that every Matcha mechanic is permanent.

## Decision

All Matcha-specific knowledge is confined to `dev.forever.compat.matcha` and its
associated, clearly identified compatibility resources. No other package may hardcode
Matcha scoreboard names, function paths, advancement IDs, disguised item identities,
custom model data, recipe tricks, or private file paths.

The adapter translates Matcha observations into stable Forever concepts. Its boundary
may expose capabilities, normalized events, item identity mappings, version information,
audit metadata, and compatibility overrides. Core systems depend on those concepts, not
on the third-party spelling that currently implements them.

The adapter owns version detection and validation. It must fail with an actionable error
when a pinned Matcha release is missing or structurally incompatible, rather than quietly
interpreting an unknown identifier as valid state. Supported Matcha versions and known
limitations are recorded alongside the audit results.

Forever Core remains authoritative for Forever-owned state and outcomes. The adapter is
not a second gameplay authority, a hidden global manager, or a place to accumulate every
unrelated compatibility rule. Client code receives server projections and does not read
Matcha internals directly.

A no-op or explicit unavailable capability is preferred to a crash when a future optional
integration is absent. The official starting pack is present for this milestone, but the
contract still keeps Matcha replacement and future optional adapters practical.

## Consequences

- A single upstream change is more likely to require an adapter update and a focused
  audit rather than a project-wide edit.
- Core domain terms can remain stable while Matcha is kept, extended, overridden, or
  replaced independently.
- Tests can exercise the Forever contract with fixtures that represent Matcha behaviour
  without making every core test depend on private datapack paths.
- The boundary makes provenance clearer. Matcha-derived knowledge is visibly compatibility
  work, which supports the separation in [ADR 0020](0020-private-project-license-separation.md).
- The adapter adds translation code, mapping tables, version checks, and an additional
  failure mode. A small integration can cost more lines than a direct call.
- Debugging can be less immediate because a failure may occur after translation. Logs must
  include the stable concept, the Matcha version, and the relevant external identifier
  without leaking ambiguity into callers.
- Some Matcha behaviours may not map cleanly to a Forever concept. The accepted response
  is an explicit capability gap or classification review, not a leaky escape hatch.
- The adapter can become a god object if every feature adds bespoke state to it. Reviews
  must keep it a narrow boundary with named translators and real callers.
- Future work includes contract tests, a version support matrix, audit fixtures, bounded
  mapping validation, and migrations for any persistent identity that Forever adopts.
- Every adapter change must be checked against save compatibility and the world-save rule,
  not just against a successful launch on a fresh world.

## Rejected alternatives

### Direct dependency from core systems

Having core code call Matcha functions and inspect its scoreboards would be shorter at
first. It would make upstream identifiers part of the de facto public API, however, and
would make replacing a single mechanic unsafe. The saved-state and testing costs are too
high for a third-party pack without API stability.

### Mixins into Matcha functions

Mixins could intercept behaviour at precise points and might avoid some polling or
translation. They would couple Forever to implementation details even more tightly,
create ordering and compatibility risks, and add a maintenance mechanism already
excluded from the current baseline. A mixin can only be considered later for a specific,
well-justified boundary, not as the default integration strategy.

### Vendor Matcha into Forever

Vendoring would make the files locally available and appear to simplify reproducibility.
It would blur provenance, complicate the CC-BY-NC-SA-4.0 obligations, and turn every
upstream update into a manual merge. Pinning the official archive and isolating its
adapter knowledge achieves reproducibility without pretending ownership of the pack.

### Reflection or unrestricted string lookups everywhere

Reflection could postpone the design of a contract, but it would move unchecked private
identifiers into runtime paths and make failures harder to diagnose. Compatibility code
may parse external data where necessary, but it must still present a typed, validated
boundary to the rest of Forever.

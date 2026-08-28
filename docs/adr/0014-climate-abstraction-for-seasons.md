# ADR 0014: Put seasons behind a climate abstraction

- Date: 2026-08-28
- Related principles/ADRs: [Principles 2, 8, 12, 13, 14, 15, and 18](../design-principles.md), [ADR 0002](0002-isolate-matcha-behind-adapter.md), [ADR 0003](0003-classify-matcha-mechanics-individually.md), [ADR 0015](0015-conservative-worldgen-posture.md)

## Status

Accepted. This is a locked optional-integration decision.

## Context

Seasonal and climate variation can make regions feel distinct and give farming, food,
travel, and settlement planning a reason to change over time. It can support Forever's
vision by making knowledge and infrastructure valuable rather than making every resource
an interchangeable output of the same farm. This supports alternatives to the
anti-AFK-farm checklist, while Principles 6 and 8 make infrastructure and regional
knowledge relevant to the climate choice.

The first planned climate integration target is Serene Seasons. It is not, however, safe
to let core gameplay depend directly on one third-party mod's API, names, calendar, or
save format. Optional integrations belong behind capability boundaries, and the absence
of an optional mod must not crash a dedicated server.

A direct seasonal implementation would also make later weather, biome, temperature, or
climate providers expensive to add. Conversely, an abstraction designed around every
possible mod before a second real caller exists would be premature architecture. The
boundary needs to be small enough to support the first adapter and expressive enough not
to leak Serene Seasons assumptions into food, agriculture, or settlement code.

Climate state is persistent-adjacent. If a season or phase affects production, a restart,
version change, or missing adapter must not silently change a world in a way players cannot
understand. Principle 12 favours data-driven thresholds, and Principle 15 requires an
in-game explanation of player-facing effects.

## Decision

Forever Core consumes a climate abstraction rather than importing Serene Seasons directly.
The abstraction exposes stable, bounded concepts needed by real gameplay, such as a
climate snapshot, seasonal or phase state, relevant transitions, and capability or
availability information. It must not expose third-party objects as core domain state.

Serene Seasons is the first adapter target. Its adapter translates the provider's state
into the Forever climate contract and remains in the optional compatibility area. A
missing or incompatible provider yields an explicit stable-climate fallback or unavailable
capability, according to the consuming mechanic. It must not cause a crash or silently
invent a partial season.

Climate-sensitive thresholds, durations, multipliers, and effects are data-driven under
Principle 12. Core systems query the abstraction or consume normalized events. They do
not hardcode a Serene Seasons calendar, item ID, tag, or save key. Provider version and
translation assumptions are recorded for audit and migration.

The abstraction is intentionally narrow and evolutionary. A second real provider or a
new required climate capability can justify extending it. A hypothetical method with no
consumer does not belong in the API. Player-facing effects must be documented in the
Field Guide and communicated with non-colour cues.

No world-generation dependency is added as part of this decision. Climate integration
must remain compatible with the conservative posture in [ADR 0015](0015-conservative-worldgen-posture.md).

## Consequences

- Core mechanics can use seasonal information without becoming coupled to one mod's API
  or calendar, preserving the option to support other providers later.
- A stable fallback allows a world to load and remain playable when Serene Seasons is
  absent, disabled, or temporarily incompatible.
- The adapter boundary makes provider-specific bugs diagnosable and keeps third-party
  identifiers out of the rest of the codebase, consistent with [ADR 0002](0002-isolate-matcha-behind-adapter.md).
- Data-driven climate effects can be tuned without recompiling mechanics and can be
  inspected as part of the balance audit.
- An abstraction can only represent the common contract unless it is deliberately
  extended. A provider-specific feature may be unavailable or require a new ADR rather
  than leaking an emergency escape hatch into core.
- Two calendars can disagree during upgrade, server restart, or a partial provider load.
  The adapter needs deterministic conversion and safe fallback rules or a climate change
  could alter production unexpectedly.
- Supporting an optional integration costs code, tests, documentation, and compatibility
  tracking even when most servers use the fallback. The first adapter must stay small.
- Climate effects can create hidden progression gates or external-wiki requirements if
  their timing and countermeasures are not explained. The Field Guide is a release
  requirement, not a later content task.
- Future work includes the climate contract, Serene Seasons adapter, provider capability
  tests, save and restart semantics, data schemas, fallback messaging, and a second-provider
  review before broadening the abstraction.
- We accept that a stable-climate world will not expose every seasonal feature. A reliable
  no-op is preferable to a hard dependency that makes world loading fragile.

## Rejected alternatives

### Direct dependency on Serene Seasons

Direct calls would be fast for the first feature, but they would make every consumer track
Serene Seasons' API and turn an optional mod into a core requirement. It would also make a
future provider migration a project-wide change.

### Hardcode a four-season calendar in core

A built-in calendar would avoid an external dependency, but it would treat one particular
model of climate as universal and make weather, biome, and future providers awkward. It
would also encode balance values in code contrary to Principle 12.

### No abstraction and no seasonal integration

Avoiding the feature would reduce maintenance, but it would discard a useful way to give
regions and agricultural knowledge more identity. The project can keep the boundary small
without committing to every climate feature now.

### Support every climate mod up front

A large universal interface would be premature and likely become a lowest-common-denominator
API with no clear real caller. Serene Seasons is a concrete first target, and later
providers can extend the contract when their requirements are known.

### Use biome temperature alone

Biome values are already available, but they represent static world generation rather than
calendar-driven climate. They cannot express seasonal change, provider capability, or a
stable fallback for a world that intentionally uses a seasonal mod.

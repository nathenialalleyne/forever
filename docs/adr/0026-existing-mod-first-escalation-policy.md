# ADR 0026: Escalate from existing tools before writing custom code

- Date: 2026-08-28
- Related principles/ADRs: [Principles 1, 6, 9, 12, 13, 14, 15, 17, and 18](../design-principles.md), [ADR 0022](0022-extensible-by-default.md), [ADR 0024](0024-modpack-first-product-ownership.md), [ADR 0033](0033-documented-gap-analysis-for-companion-code.md)

## Status

Accepted. This is the default implementation and maintenance policy for the modpack.

## Context

A modpack can usually solve a player problem by selecting or configuring an existing tool. Building placement, excavation, recipe inspection, contextual information, cooking, storage indexing, local transport, rail physics, horses, performance, structures, and ambience are all domains with established mod ecosystems. A custom implementation may initially look faster because it fits the project's vocabulary, but it creates code, compatibility, save, documentation, and removal work that the pack would then own forever.

The opposite danger is also real. A pack can become a pile of incompatible dependencies if it assumes that any existing mod is good enough. Some needs are connective rather than generic. A unified knowledge surface may need to join several providers. Matcha may need normalisation. A route service may need a stable cross-mod contract. Refusing all custom code would force the design into accidental behaviour or unmaintained workarounds. The project therefore needs an explicit escalation order that starts with the least-owned mechanism and records why each earlier layer failed.

A private fork is especially easy to misuse. It can turn a temporary compatibility problem into an untracked derivative, and it can create a distribution and licence obligation that cannot be removed cleanly. A Mixin has a similar maintenance cost even when it is technically small. Third-party JARs must not be edited in place under any circumstances.

## Decision

Every new pack capability follows this exact escalation order:

1. existing configuration.
2. datapack.
3. resource pack.
4. supported scripting layer if stable.
5. public API or event integration.
6. narrow compatibility adapter.
7. narrow version-guarded Mixin.
8. private fork.
9. new custom implementation.

The order is evaluated from the top down. A lower step may be considered only when the higher steps cannot express the required behaviour or cannot meet the documented safety and compatibility requirements. A skipped step requires a written reason in the ticket. “It was quicker to code” is not sufficient evidence.

A custom implementation is approved only when all of the following conditions are met:

- the feature is important to pack identity,
- no maintained compatible mod reasonably provides it,
- at least three plausible alternatives were evaluated where available,
- configuration and datapacks cannot express it,
- the maintenance burden is documented,
- save migration and removal behaviour are understood, and
- tests are planned.

A private fork is a last resort before a new custom implementation. It requires licence permission, the exact upstream commit, a documented patch set, an upstream-monitoring plan, the ability to rebuild it, and migration and removal analysis. The fork must not be represented as an unchanged upstream dependency. Never modify third-party JARs.

A compatibility adapter must be narrow, version-aware, and isolated behind a Forever or Many Roads-owned contract. A Mixin must be narrow and version-guarded, and its failure must be visible rather than silently applying to an unknown target. The selected mechanism must retain ordinary Minecraft behaviour when the integration is absent unless the pack explicitly documents a non-optional dependency and its removal plan.

## Consequences

- Most features can be delivered through configuration or data, which lowers Java surface area and makes pack updates easier to review.
- The policy creates an evidence burden. A ticket may spend time testing three plausible mods and several integration layers before any code is written.
- Existing-mod limitations can shape the player experience. The pack may need to accept a less perfect generic feature rather than own a bespoke implementation.
- Adapters and Mixins concentrate compatibility risk. Version guards, diagnostics, fixtures, and upstream monitoring become necessary ongoing work.
- Private forks can preserve a critical identity feature when no other route exists, but they create a second release pipeline and possible licence restrictions.
- A custom implementation becomes a deliberate product decision instead of the default response to an inconvenient API. This slows experimentation but protects save longevity and maintenance capacity.
- The same player-facing feature may require several layers, such as configuration plus a small adapter. The ticket must identify which layer owns each behaviour so that the stack does not become an opaque workaround.

## Rejected alternatives

### Write custom Java first

Custom code gives immediate control, but it makes the companion the bottleneck and duplicates capabilities that mature mods already maintain. It also hides whether the actual gap is a missing configuration or a cross-mod connection.

### Always choose the most popular existing mod

Popularity is not proof of 26.2 compatibility, a suitable licence, a public API, or fit with the world-save rules. The policy evaluates candidates against the actual baseline rather than assuming a name is enough.

### Use Mixins before public integration points

A Mixin can bypass an API gap, but it couples the pack to private implementation details and can fail on a minor upstream change. It belongs near the bottom of the sequence, with a version guard and a removal plan.

### Fork every dependency that matters

Forking would give control but would make the project responsible for upstream fixes, distribution rights, rebuilds, and merges across the whole pack. A fork is reserved for a documented identity-critical gap, not general preference.

### Ban all custom code

A complete ban would leave cross-mod concepts such as unified knowledge, Matcha normalisation, and route services without a safe owner. The final step remains available, but only after the evidence gate is satisfied.

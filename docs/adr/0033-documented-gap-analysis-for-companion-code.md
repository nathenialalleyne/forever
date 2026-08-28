# ADR 0033: Require a documented gap analysis before companion code

- Date: 2026-08-28
- Related principles/ADRs: [Principles 1, 2, 9, 12, 13, 14, 15, 17, and 18](../design-principles.md), [ADR 0022](0022-extensible-by-default.md), [ADR 0024](0024-modpack-first-product-ownership.md), [ADR 0026](0026-existing-mod-first-escalation-policy.md)

## Status

Accepted. This is the entry gate for Many Roads Integration code.

## Context

The companion mod is intentionally small. Its purpose is to connect systems that existing mods and pack configuration cannot connect safely, not to become a replacement for the modpack. Candidate connective systems include a capability web, unified knowledge, Matcha normalisation, free-form settlement registration, villager career progression, cross-mod economy, discovery consequences, earned passenger services, and world history. Each could be useful to the pack identity, but usefulness alone does not justify owning a new Java subsystem.

A custom feature can look isolated while creating persistent identifiers, network payloads, server authority, client projections, compatibility adapters, and a removal obligation. Without a written gap analysis, the project will tend to code the first plausible idea, compare it against no maintained alternative, and discover its save or licence cost after release. [ADR 0026](0026-existing-mod-first-escalation-policy.md) already puts custom implementation last. This ADR makes the evidence required before reaching that step explicit and repeatable.

There is a genuine cost to this gate. It slows a small experiment and can produce paperwork for a feature that later proves unnecessary. The alternative is faster local progress at the cost of a companion that gradually owns the whole pack and becomes difficult to replace. The companion's value depends on keeping that boundary narrow enough to remain maintainable.

## Decision

No new companion-code feature may enter implementation until its ticket contains a reviewed gap analysis with the following evidence:

1. the player problem and why it is important to Many Roads Home identity,
2. the intended behaviour, ownership boundary, and ordinary-play fallback,
3. at least three plausible maintained mod alternatives where available, with authoritative compatibility, licence, API, and limitation evidence,
4. attempts using the exact escalation order in [ADR 0026](0026-existing-mod-first-escalation-policy.md), including configuration, datapack, resource pack, stable scripting, public API, adapter, and Mixin options where relevant,
5. the reason configuration and datapacks cannot express the requirement,
6. the proposed smallest companion boundary and why it does not belong in a generic existing tool,
7. the maintenance owner, supported version policy, diagnostics, and upstream monitoring plan,
8. the persistent data, network, authority, save migration, backup, and removal behaviour,
9. the player-facing translation, tooltip or UI, Field Journal, and recipe-viewer implications, and
10. the tests required for normal play, absent integrations, malformed input, restart, duplication resistance, migration, and client/server authority.

A gap analysis may conclude that no companion code is justified. That is a valid outcome and should close the ticket with the selected existing-mod configuration or a documented limitation. If code is approved, it must use the smallest concrete implementation that satisfies the evidence. New interfaces, registries, or plugin surfaces still require a real second caller under [ADR 0022](0022-extensible-by-default.md).

The analysis is part of the product record. It must name the exact upstream sources and versions inspected and must be revisited when the pinned Minecraft baseline, candidate mod, licence, or persistent schema changes. A gap analysis does not authorise a private fork or a third-party JAR modification.

## Consequences

- The companion remains a deliberate connective layer rather than a default home for every missing feature.
- Alternative evaluation and save analysis happen before code creates a migration burden.
- A small experiment can take longer to start and may require maintaining a detailed compatibility record even when the final answer is configuration.
- Candidate mod availability can change, so an approved gap may need to be reopened when a maintained compatible alternative appears.
- The evidence record improves future removal and handoff, but it adds documentation that must be kept accurate alongside code and pack metadata.
- Tests become part of the approval decision, which can expose that a desirable feature has no safe failure or persistence model yet.
- The gate does not guarantee that a custom implementation will be small. A connected system may still require server storage, client projection, adapters, and migration tooling, and the ticket must show that cost before approval.

## Rejected alternatives

### Let any contributor add a small helper class

A helper can establish a persistent or compatibility boundary accidentally. Requiring the gap analysis prevents the amount of code from hiding the amount of ownership.

### Require only one existing-mod comparison

One candidate can be unavailable, poorly maintained, or unsuitable for the exact baseline. Three plausible alternatives provide a more credible check where the ecosystem has enough options, while the “where available” clause avoids inventing strawman candidates.

### Treat a public API as proof that custom code is justified

An API can make integration possible, but it does not prove that the feature belongs in the companion or that the resulting save and support burden is acceptable. Ownership and removal still need analysis.

### Build the entire connective framework up front

A universal framework would guess future seams and create the very monolith this pivot rejects. Concrete, evidenced features should arrive first, with abstractions extracted when a second caller exists.

### Modify a third-party JAR instead

Binary modification obscures provenance, breaks update and licence boundaries, and is forbidden by the escalation policy. A gap must be solved through a supported boundary, a documented fork, or a separate companion implementation.

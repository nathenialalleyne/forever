# ADR 0024: Make the modpack the primary product

- Date: 2026-08-28
- Related principles/ADRs: [Vision](../vision.md), [Principles 1, 6, 8, 9, 12, 15, 17, and 18](../design-principles.md), [ADR 0020](0020-private-project-license-separation.md), [ADR 0023](0023-many-roads-home-working-title.md), [ADR 0025](0025-packwiz-source-of-truth.md), [ADR 0026](0026-existing-mod-first-escalation-policy.md)

## Status

Accepted. This is the product ownership decision for the Many Roads Home pivot.

## Context

The original implementation plan treated a custom mod as the centre of the experience and third-party content as material to support around it. That puts too much responsibility in Java. Mature mods already solve many of the practical problems that make a long-lived world pleasant: placement and excavation, recipe inspection, contextual information, cooking, storage search, transport, performance, ambience, and schematic previews. Reimplementing those systems would spend the project's maintenance budget on infrastructure that is not distinctive to the pack.

The revised product is a curated combination of third-party mods, their configuration, datapacks, resource packs, optional blueprints, and a small companion integration mod. Its value is the interaction between those pieces and the choices made for a particular long-lived world. Product ownership therefore needs to distinguish composition from implementation. An upstream author owns an upstream mod. The pack maintainer owns the selected versions, configuration, compatibility claims, onboarding, provenance, and release testing. The companion owns only its accepted integration contracts.

This model introduces forces that a custom-mod model hides. The pack becomes sensitive to upstream release schedules, incompatible configuration defaults, licensing terms, launcher formats, and the quality of public APIs. A pack release can fail even when the companion code compiles. Conversely, a modpack can deliver useful play without a large custom codebase, which lowers save and maintenance risk and makes the design testable against real player-facing tools.

## Decision

Many Roads Home is a modpack-first product. A release is a tested composition of:

- pinned third-party mods and their configuration,
- the official Matcha archive and other approved datapacks,
- resource packs and translations whose provenance is recorded,
- optional, provenance-approved blueprints or schematic material,
- the small Many Roads Integration companion mod only where the escalation and gap-analysis gates approve it, and
- release documentation that explains the composition, compatibility limits, and removal consequences.

The pack configuration and release manifest own the composition contract. Third-party mods remain external dependencies and are not treated as Forever or Many Roads Home source. The companion mod does not become the authority for generic building, cooking, transport, storage, performance, or ambience when a maintained compatible mod can provide those capabilities.

Pack-level decisions include selected versions, enabled or disabled features, interoperability settings, load order, defaults, onboarding treatment, and known limitations. They must be reviewable without reading every upstream implementation. A pack release must carry a dependency and licence record, a compatibility matrix for the supported Minecraft and loader baseline, and a disposable-world validation result.

The pack must preserve ordinary Minecraft play. It may add convenience and connective systems, but it must not make basic building, shelter, food, inventory use, or survival depend on a particular quest, registration, blueprint, or companion feature. Removing an optional component must have a documented neutral fallback or a migration and removal plan before the component is declared optional.

## Consequences

- The project can reach a coherent playable baseline by integrating mature tools instead of rebuilding their general-purpose mechanics.
- Pack-level configuration becomes a first-class design surface. A compatibility regression can be fixed without changing Java when the underlying mod exposes the needed control.
- Ownership and provenance are clearer. The pack owns selection and composition, upstream authors own their code, and the companion owns only its narrow contracts.
- Testing must cover the full assembled instance, not only the repository's Java unit tests. A passing companion build is not a passing pack release.
- The release process inherits upstream version churn, incompatible defaults, and licence review work. A mature mod can still be a poor fit for the exact 26.2 baseline.
- The pack maintainer may have less control than a total conversion author. When an upstream behaviour is important but not configurable, the project must accept it, find another tool, or document a carefully gated compatibility response.
- Players may need to understand which part of the experience is pack configuration and which part is the companion mod. The unified Field Journal and release documentation must make that boundary legible.

## Rejected alternatives

### Make a custom mod the primary product

This gives one codebase control over every rule, but it recreates the maintenance burden that the pivot is intended to avoid. It also delays useful play while common systems are rebuilt and makes third-party compatibility a secondary concern instead of the central product task.

### Ship an uncurated list of compatible mods

A list would be easier to assemble, but it would not define load order, configuration, onboarding, provenance, or the interactions that make a world coherent. Players would be left to discover conflicts and hidden gates themselves.

### Fork or reimplement every important dependency

Owning every implementation would reduce upstream uncertainty in theory, but it would make the project a large total conversion and create many save, licence, and update obligations. The pack has not earned that maintenance scope.

### Treat the companion as a required universal framework

A universal framework would pull generic features back into custom code and make the companion a single point of failure. The companion is permitted only for connective gaps that survive the escalation policy.

# ADR 0034: Preserve existing custom code during the modpack-first pivot

- Date: 2026-08-28
- Related principles/ADRs: [Principles 12, 13, 14, 15, 17, and 18](../design-principles.md), [ADR 0020](0020-private-project-license-separation.md), [ADR 0021](0021-layered-feature-packages.md), [ADR 0024](0024-modpack-first-product-ownership.md), [ADR 0026](0026-existing-mod-first-escalation-policy.md), [ADR 0033](0033-documented-gap-analysis-for-companion-code.md)

## Status

Accepted. This is the source-preservation and pivot-scope decision.

## Context

The repository contains a prototype and foundation built around the Forever custom-mod direction. The pivot changes product ownership and implementation priority, but it does not make the earlier work worthless. The old code, audit records, specifications, and ticket descriptions show what the project intended to solve, what boundaries were considered, and where persistent or compatibility assumptions may already exist. The tag `prototype-pre-modpack-pivot` also preserves a historical point before this change.

Deleting or rewriting the old code during the pivot would make the new pack look cleaner while removing evidence that is needed for migration, gap analysis, and future reuse. It could also delete registered identifiers or generated material that an existing disposable world or later branch still references. Keeping everything without labelling it would create the opposite problem: agents might mistake an implemented prototype for an approved, tested, or pack-shipped feature.

The project therefore has to preserve history without treating history as product scope. The old custom systems remain subject to the new escalation policy. Reuse may be sensible when a genuine gap is proven, but it must be an explicit decision after ownership, persistence, compatibility, and removal behaviour are checked.

## Decision

Existing custom source, tests, documentation, audit reports, generated outputs, and prototype ticket content are preserved during the modpack-first pivot. This change does not delete, reset, rename, or silently repurpose those paths. The old backlog is moved to `docs/backlog-prototype-archive.md` as evidence of intent, and the active `docs/backlog.md` records the new queue and its relationship to that archive.

Preservation does not confer release status. The roadmap and active backlog must identify which material is historical, which is foundation, which is a compatibility experiment, and which is an accepted Many Roads Home feature. A future ticket that reuses existing custom code must perform the gap analysis in [ADR 0033](0033-documented-gap-analysis-for-companion-code.md), apply the escalation order in [ADR 0026](0026-existing-mod-first-escalation-policy.md), and review every persisted or external identifier it touches.

Matcha-derived material remains separate from original source under [ADR 0020](0020-private-project-license-separation.md). Prototype code must not be copied into the pack or companion boundary merely because it already exists in the repository. If a preserved path becomes unsafe to build, distribute, or migrate, a separate ticket must document the reason, a recovery copy, and the least destructive retirement or quarantine action before changing it.

The active product plan may reference preserved prototypes as evidence, but it must not count their old implementation status as completion of the new modpack milestones. The pivot's first implementation work is the LAB compatibility queue, not a wholesale activation of the old custom systems.

## Consequences

- The repository retains design evidence and possible reusable work, so future agents can compare a proposed companion feature with the original intent instead of guessing.
- No existing save-facing identifier is changed by the pivot itself, reducing immediate risk to development worlds and historical branches.
- The tree contains dormant or superseded code that can confuse contributors and may continue to impose build or maintenance cost. Labels, the archive, and the roadmap must make its status clear.
- Preserving generated Matcha material keeps provenance and audit reproducibility, but it also preserves third-party licence obligations and storage volume.
- Reusing old code is slower than copying it because the new gap analysis, compatibility boundary, and migration review are mandatory.
- A future cleanup may still be necessary. It must be a separate decision with backup, identifier inventory, removal tests, and a clear statement of what evidence is retained.
- The pivot cannot claim that the pack is playable merely because prototype code exists. Assembled-instance compatibility and player-facing validation remain the release evidence.

## Rejected alternatives

### Delete all custom source and start from an empty companion

This would reduce apparent scope, but it would destroy useful evidence and could remove save or audit context that is difficult to reconstruct. The pivot is a change in priority, not a demand to erase history.

### Move the old code to an untracked branch and remove it here

A branch is useful historical support, but relying on an unreachable or forgotten branch makes the evidence unavailable to normal reviews and does not address generated reports or documented intent. The repository keeps the material visible and labelled.

### Treat every existing prototype feature as accepted companion scope

This would reverse the pivot by allowing prior implementation effort to decide current product ownership. Existing code still has to pass the new evidence, compatibility, persistence, and removal gates.

### Rewrite the old backlog in place

Replacing the old ticket text would lose the original acceptance assumptions and make it impossible to tell whether a later failure came from the prototype or the new plan. The archive preserves the text while the active queue can be purpose-built for the modpack.

### Preserve code by modifying third-party JARs

A binary patch would not preserve an honest ownership boundary. It is forbidden by [ADR 0026](0026-existing-mod-first-escalation-policy.md), and any upstream change would make the result difficult to reproduce or remove.

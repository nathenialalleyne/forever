# Companion integration mod

This directory is the future home of the Many Roads Home companion mod. It is currently
a boundary marker with no code in it, and that is deliberate.

## Why the directory is empty

The companion mod source still lives at the repository root in `src/`, where the
custom-mod-first prototype put it. Moving roughly 300 Java files, five Gradle source
sets, and a Loom build into this directory is a mechanical but wide change that would
touch every path in the build, the CI workflows, and the architecture tests. Doing that
in the same run as the modpack pivot would mix a large structural move into a change
whose value is the decisions, and it would make the diff unreviewable.

The move is therefore a ticket of its own, gated on the compatibility spikes. That
ordering is intentional: the labs will decide which of the seven prototype systems are
replaced by third-party mods, and moving code that is about to be deleted would be
wasted work. See `docs/pivot/existing-code-inventory.md` for the per-file
classification and `docs/backlog.md` for the ticket.

## What belongs here eventually

Only connective systems that no maintained mod can provide:

- the capability web that guarantees multiple routes to essential capabilities;
- the unified contextual knowledge system, in place of many starter books;
- Matcha normalization behind an adapter, so its internals never leak;
- free-form settlement registration that evaluates function rather than style;
- villager career and institutional progression;
- cross-mod economic behaviour;
- discovery consequences;
- earned passenger services;
- world history.

## What does not belong here

Anything a maintained mod already does: block placement, vein mining, schematic
previews, recipe viewing, contextual information, cooking, seasons, storage indexing,
item transport, rail physics, horses, structure generation, performance, or ambience.

Before adding a system here, walk the escalation order in
[ADR 0026](../docs/adr/0026-existing-mod-first-escalation-policy.md) and record the gap
analysis required by
[ADR 0033](../docs/adr/0033-documented-gap-analysis-for-companion-code.md). The companion
mod is not the product, and a feature that appears here without that justification is a
defect in the ownership decision rather than a contribution.

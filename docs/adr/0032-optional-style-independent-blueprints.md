# ADR 0032: Keep blueprints optional and independent of architectural style

- Date: 2026-08-28
- Related principles/ADRs: [Principles 1, 2, 7, 12, 15, 16, 17, and 18](../design-principles.md), [ADR 0009](0009-settlements-as-building-graphs.md), [ADR 0024](0024-modpack-first-product-ownership.md), [ADR 0026](0026-existing-mod-first-escalation-policy.md)

## Status

Accepted. This is the construction planning and validation rule for the pack.

## Context

Blueprint and schematic tools can make large construction projects easier to plan, preview, measure, and share. They are also a natural existing-mod solution for a pack that values meaningful building projects. The same tools can become a hidden style authority if a settlement requires a prescribed template, awards an aesthetic score, or withholds a service until a player reproduces someone else's design.

The vision explicitly allows medieval villages, modern cities, industrial regions, scattered homesteads, fantasy kingdoms, and fishing hamlets. Settlements evaluate function, not style. Ordinary block placement must remain available without a construction site, specialisation, quest, or blueprint. A blueprint may be a helpful plan, but it cannot become the definition of a valid home or the only way to contribute to a contract.

Blueprints also carry provenance and compatibility questions. A schematic can contain third-party or player-created material, reference blocks that are absent, or rely on an external renderer. A custom blueprint engine would add a large client and file-format surface before the pack has proved that existing tools cannot meet the need.

## Decision

Blueprints are optional planning aids. A player may create, import where permitted, preview, material-check, or follow a blueprint, but ordinary building and settlement registration remain available without one. The pack will prefer a mature existing blueprint or schematic tool through the escalation policy and will not require a custom renderer as a foundation feature.

A blueprint used by a construction contract or functional settlement validator expresses intended function and constraints, not an aesthetic template. Validation may check declared roles, safe access, enclosure, capacity, workstations, storage, route connections, or other approved functional requirements. It must not score style, demand a specific palette, compare a build to a canonical image, or require an exact block arrangement when several functional designs are valid.

Player-created and third-party blueprints need a provenance and compatibility record. Missing block mappings, unsupported formats, or unavailable preview tools produce an explicit limitation and never delete the physical build. An imported blueprint is not permission to redistribute its contents. Blueprint metadata and contract definitions are data-driven, and any persistent project reference receives a version, migration path, and safe removal state.

A contract may offer a blueprint as one way to satisfy a declared outcome. It must also describe functional criteria in the Field Journal so a player can build by hand or use another planning method. A player who never uses a blueprint must retain access to ordinary building, functional registration, and meaningful project participation.

## Consequences

- Builders can use planning tools without being told what their settlement should look like.
- Functional contracts become more inclusive because hand-built, improvised, and blueprint-assisted results can satisfy the same purpose.
- Validation is harder than exact template matching. The project must define bounded functional checks and explain why a build succeeds or fails.
- Optional tools create format, provenance, and support work. A blueprint feature may be unavailable on a client or may reference content removed from the pack.
- Without a canonical style, players may receive less visual consistency in shared projects. The pack accepts that variation as the cost of preserving creative ownership.
- A blueprint can still accelerate large builds and make material planning legible, but it does not by itself prove that a structure is safe, connected, or useful.
- Existing schematic tools may have their own server, licence, or version constraints. The pack must validate the chosen tool rather than treating a familiar name as a guarantee.

## Rejected alternatives

### Require a blueprint for every registered building

This would turn a convenience into a toll gate in front of normal building and would exclude players who prefer improvisation or cannot use the selected client tool. It conflicts with Principle 1.

### Award settlement quality by visual similarity or style

Aesthetic scoring would make the system judge the player's taste and would privilege one theme over another. Function is the stable contract that different styles can share.

### Provide one official canonical blueprint set

Canonical plans could make validation simple, but they would quietly define the intended architecture and reduce the settlement system to template completion. They may be optional examples, never the only valid designs.

### Build a custom blueprint engine first

A custom engine would add file parsing, rendering, synchronisation, permissions, and migration work before a real gap has been shown. Existing tools must be tested and rejected with evidence first.

### Ban blueprint tools entirely

A ban would remove useful planning and material-estimation capabilities from builders who want them. Optional use provides convenience without making it a requirement.

# ADR 0015: Take a conservative posture toward world generation

- Date: 2026-08-28
- Related principles/ADRs: [Principles 1, 7, 8, 12, 14, 15, and 18](../design-principles.md), [ADR 0001](0001-use-official-matcha-as-starting-pack.md), [ADR 0003](0003-classify-matcha-mechanics-individually.md), [ADR 0014](0014-climate-abstraction-for-seasons.md), [ADR 0016](0016-existing-village-import.md)

## Status

Accepted. This is a locked world-generation posture.

## Context

Forever promises worlds that survive updates, migrations, and years of play. World
terrain and structure placement are among the hardest things to change safely after a
world has been explored. A new generator can create dramatic screenshots while making
existing chunks, seeds, portals, villages, and player travel assumptions difficult to
preserve. It also keeps exploration from becoming another mandatory stop on the
anti-AFK-farm checklist. Principle 18 makes this save risk decisive.

The vision also says that Forever is not a total conversion and does not dictate an
architectural style. World generation should provide a compelling Minecraft landscape,
not turn progression into a hunt for a mandatory structure set or make one aesthetic the
correct way to inhabit a world.

The desired posture is therefore asymmetric: preserve dramatic terrain and exploration,
while keeping custom structures restrained and purposeful. The foundation milestone has
no new world-generation dependencies. Every additional dependency is another compatibility
surface, another version to pin, and another source of save or chunk-generation risk.

Existing natural villages are valuable world assets. Requiring players to abandon them or
regenerating them to fit a new system would waste established worlds. [ADR 0016](0016-existing-village-import.md)
provides an explicit import path instead of hiding a migration inside world generation.

There is still a content pressure. Too little new generation can make Forever feel like a
thin overlay and may leave exploration without enough regional opportunities. The answer
must be measured additions after the foundation, not an early commitment to a large
terrain overhaul.

## Decision

During the current foundation posture, Forever adds no new world-generation dependency.
The project uses the verified Minecraft 26.2 and existing pack baseline, with any later
world-generation integration requiring its own dependency review and decision.

The content direction is dramatic terrain with a restrained structure set. Custom
structures, features, and placement rules may be added later when they have a clear
player-facing purpose, low enough density to preserve exploration, and safe configuration
and testing. They must not enforce an architectural theme or make one structure a required
checkpoint for normal building.

World-generation changes must be evaluated separately from gameplay balance. They require
seed and chunk-generation tests, compatibility review with existing worlds, explicit
versioning, and a plan for what happens to already generated terrain. No automatic
regeneration of explored chunks is implied by this ADR.

New structures should compose with settlements through registration or import rather than
assuming that generated blocks are automatically owned by a settlement. Climate work
follows [ADR 0014](0014-climate-abstraction-for-seasons.md) and must not smuggle in a
world-generation dependency.

The world save outranks spectacle. If a generation experiment cannot fail safely or can
only be rolled back by restoring an entire world, it is not ready for default inclusion.

## Consequences

- New worlds remain recognisably Minecraft, with room for the player's own style and for
  exploration to discover rather than simply follow a prescribed structure chain.
- The project avoids adding world-generation dependencies and their update burden during
  the highest-risk foundation work.
- Existing worlds are not silently rewritten to accommodate a new generator. Players can
  use the explicit village import path when they want settlement functionality.
- Restrained structures can give later systems meaningful landmarks without covering the
  landscape or turning every resource into a generated checklist stop.
- The conservative posture limits early spectacle and may make the project appear less
  ambitious than a terrain overhaul. That is an accepted trade for world longevity.
- Modded world-generation compatibility remains a future cost. Vanilla assumptions can
  still fail on unusual terrain, and each optional provider needs explicit testing rather
  than an optimistic claim of support.
- Sparse structures may not provide enough opportunities for settlement, trade, or
  exploration at first. The project must measure this in representative worlds instead
  of compensating with indiscriminate generation density.
- Any new feature placement increases seed reproducibility, chunk-boundary, migration,
  and performance testing. Data-driven placement does not remove those obligations.
- Future work includes a worldgen compatibility matrix, seed fixtures, structure density
  experiments, existing-chunk policy, import integration, and a separate ADR for each
  dependency or major terrain change.
- We accept that some desirable content will be delayed. A missing landmark is recoverable;
  corrupted or irreversibly reshaped worlds are not.

## Rejected alternatives

### A large terrain overhaul immediately

A terrain overhaul could make Forever visually distinctive, but it would add broad seed,
chunk, and compatibility risk before the core systems were tested. It would also make the
project feel more like a total conversion than a framework layered on Minecraft.

### Add several world-generation dependencies now

Stacking biome, structure, and terrain mods might provide more content quickly, but it
multiplies version tracking and interaction failures. It would be difficult to know which
provider caused a broken seed or an unsafe generated feature.

### Regenerate existing chunks to match Forever

Rewriting explored terrain would produce a consistent new map, but it would destroy player
builds, alter travel routes, and make the world-save promise meaningless. Existing chunks
must remain authoritative unless a future, explicitly accepted migration offers a safe
player-controlled alternative.

### Make structures the main progression gate

A structure-heavy path could direct players through content, but it would turn exploration
into a checklist and make ordinary building or settlement growth depend on generated
locations. Forever's systems should validate function without requiring a prescribed map.

### Ban all custom world generation forever

Never adding landmarks would minimise risk, but it would unnecessarily restrict a useful
way to support exploration and regional identity. A conservative posture is a gate for
evidence and safety, not a permanent refusal of every addition.

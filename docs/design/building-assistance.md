# Building assistance

## Status and design intent

This document records the building-assistance design for the Many Roads Home modpack-first pivot. It is design documentation only. It does not implement a schematic parser, a placement service, a worker, a custom block, or a custom item.

The goal is to retire repetitive placement without taking authorship away from the player. Ordinary Minecraft placement must remain available at all times. Assistance is an optional layer over survival building, not a toll gate in front of it. A plan may help a player see, count, and repeat a design, but it must not print an unrestricted creative copy into the world.

`docs/mod-research/candidate-matrix.csv` does not exist in this checkout as of this review. The candidate notes below are therefore provisional. A Modrinth page listing Minecraft 26.2 is evidence for a release claim, not a substitute for testing the exact file in this pack on both a dedicated server and a client.

## Non-negotiable boundaries

- The player can place ordinary blocks manually without a schematic, project, Builder specialisation, settlement registration, or quest.
- The server owns every world mutation, material deduction, permission check, block-entity check, tool-cost calculation, and failure result.
- The client may preview and request a bounded operation. It cannot decide that a block was placed, that a material was consumed, or that a contract completed.
- Real materials remain the source of truth. A plan, material list, reservation, or preview is not an inventory.
- A failed or interrupted operation leaves source stacks and destination blocks recoverable. A retry with the same operation identity must not duplicate blocks or consume materials twice.
- Limits are data-driven and visible. At minimum they cover selection volume, blocks per operation, reach, queue size, worker count, tool cost, nearby-storage radius, and contract throughput.
- No operation may force-load chunks indefinitely. A target outside the allowed loaded or explicitly serviced area is rejected or paused with a described reason.
- No architecture style is required. Functional validation can check safety, access, structure, and declared project purpose, but it cannot score a building as medieval, modern, industrial, or otherwise aesthetically correct.
- Wear, missing materials, blocked targets, and permission failures need text and a non-colour cue such as an icon, outline shape, count, or named state.

## Three levels

| Level | What it does | What it must not do | Natural place in progression |
|---|---|---|---|
| **Ghost Plan** | Shows a schematic preview, material list, transforms, and manual placement targets | Mutate the world, consume materials, withdraw from storage, or employ workers | Early and always optional |
| **Assisted Construction** | Performs bounded lines, walls, floors, mirrors, rotations, repeated details, and palette substitutions using real materials | Bypass server checks, print from an empty inventory, or turn one click into an unbounded paste | Mid-game convenience and Builder/Mason technique |
| **Construction Contract** | Runs a declared project gradually with real materials and optional workers | Become unrestricted creative printing, silently reserve an entire warehouse, or complete instantly | Late-game infrastructure or accessibility route |

The levels are capability boundaries, not three mandatory quest stages. A player can remain a manual builder, use only Ghost Plans, or use an assisted operation for one difficult wall without adopting a full contract system.

## Level 1: Ghost Plan

A Ghost Plan is a read-only projection of a proposed build.

### Required behaviour

- Import or open a bounded plan after validating its file format, dimensions, block states, block-entity data, and any entity or command payload. Unknown or unsafe content is rejected with an actionable explanation.
- Render a non-solid preview that clearly distinguishes planned blocks, already-correct blocks, missing blocks, conflicts, and air. The distinction cannot rely on colour alone.
- Provide a material list grouped by exact block identity and by an optional functional material family. The list must show required, available, missing, substituted, and reserved quantities separately.
- Allow origin movement, rotation, mirroring, layer selection, and bounded visibility without changing the world.
- Let the player mark a plan as a personal draft, a project proposal, or a shared reference. A plan is not a project completion claim.
- Support manual placement one block at a time using ordinary Minecraft rules. Manual placement does not require the plan to be accepted by a project registry.
- Show a plain-language warning when a planned block has a block entity, orientation, attachment, fluid interaction, or support requirement that manual placement may change.

### Schematic formats

`.litematic` and `.schem` should be treated as separate format targets. They must not be accepted by extension alone.

- A `.schem` import target has a clear authoritative description in the WorldEdit documentation. WorldEdit identifies the Sponge schematic format as `.schem`, supports loading and saving schematic files, and documents rotation, flipping, masks, and storage. See [WorldEdit clipboard documentation](https://worldedit.enginehub.org/en/latest/usage/clipboard/) and its [source text](https://worldedit.enginehub.org/en/latest/_sources/usage/clipboard.rst.txt).
- Litematica's official project and wiki establish schematic loading, placement movement and rotation, layer rendering, and a material-list workflow. The official pages checked for this document do not spell out the `.litematic` extension in the fetched text. Treat `.litematic` support as **NEEDS_MORE_EVIDENCE** until the exact 26.2 build is tested. See [Litematica project metadata](https://api.modrinth.com/v2/project/litematica), [official repository](https://github.com/maruohon/litematica), and [official schematic-management wiki](https://github.com/maruohon/litematica/wiki/Schematic-Management).
- Format conversion must be explicit. A conversion result records its source format, parser version, unsupported fields, and any dropped data. It must never silently turn entities, commands, inventories, or unsafe block-entity payloads into placement instructions.

A Ghost Plan therefore solves visibility and planning. It does not solve material access. This distinction is important because a client-side preview can be useful even when the server does not trust or install the same client mod.

## Level 2: Assisted Construction

Assisted Construction is a server-authorised convenience for a player who is actively building. It should feel like a better tool for deliberate work, not a remote command console.

### Operations

The minimum useful operation set is:

- line;
- wall;
- floor;
- bounded filled region where the target is explicitly selected;
- mirroring across a declared plane;
- rotation in quarter turns where block-state rules are known;
- repeated details such as an array of windows, pillars, or lights;
- palette replacement by declared functional category;
- optional replacement of only air, only matching blocks, or only explicitly replaceable blocks.

The operation preview must state the target volume, block count, material requirements, selected substitutions, expected tool cost, and any blocks it will skip. A missing block, invalid orientation, protected block, unloaded target, or insufficient tool condition produces a described result rather than a partial silent success.

### Material and storage rules

- The operation consumes real ItemStacks from the player's inventory or from a nearby physical inventory that the player is authorised to use.
- Nearby storage is a convenience, not a magical network. The server resolves a bounded set of containers, checks permissions, records container revisions, and reserves exact stacks before mutating target blocks.
- A warehouse search result is not an automatic withdrawal. A remote warehouse can make stock visible, but material must reach a local physical buffer, a player inventory, or an explicit shipment before Assisted Construction can use it.
- Substitutions are selected before execution. The interface shows the exact mapping, for example `primary wall: stone -> andesite`, and the resulting count for every category.
- A failed operation releases reservations or reports a durable reconciliation state. It never consumes an item because the client predicted that the block would fit.
- Container nesting rules remain in force. A portable cache, shulker, or warehouse reference cannot be used to create an unbounded recursive source of building materials.

### Tool and durability rules

- If the normal placement or interaction requires a tool, the server applies the same tool identity, condition, enchantment, and permission rules as manual play.
- Bulk placement may charge a configured tool cost, but the cost must be explained before execution and must not silently make a broken item disappear.
- A zero-condition tool becomes broken and remains recoverable under the equipment rules. It cannot be used for further assisted work until repaired.
- Undo is a bounded recovery operation, not a guarantee that every later world change can be reversed. An undo request must identify the operation revision and refuse to overwrite blocks that another player has changed unless the player explicitly chooses a safe alternative.
- Placement of block entities, fluids, redstone components, doors, rails, and orientation-sensitive blocks should use small validated batches. A shape helper must not assume that all blocks are inert cubes.

### Authority and limits

The client sends a versioned, bounded request containing the operation shape, origin, transform, palette mapping, desired block count, source handles, and client preview revision. The server rechecks every meaningful fact. It may reject, split, or pause the operation.

The server should apply small atomic batches rather than one unbounded tick. A batch result reports placed, skipped, missing, protected, and deferred counts. The request and each settled batch have stable identities so a reconnect or retry cannot duplicate output. A target in an unloaded chunk is not a reason to force-load the world indefinitely. The operation waits for an explicit service policy or fails with a reason.

Assistance must be configurable by world or server policy. Useful controls include:

- maximum blocks per request and per batch;
- maximum target volume and dimension;
- maximum reach and vertical distance;
- maximum mirror and repeat extents;
- whether block entities, fluids, rails, redstone, or fragile blocks are allowed;
- whether nearby inventories may be used and at what radius;
- whether tool condition is charged per block, per batch, or by a data-defined curve;
- maximum outstanding requests per player;
- permissions for shared sites and protected blocks;
- maximum work performed by an unloaded contract rather than a live player operation.

The default should favour a small, understandable operation. Larger limits can be earned through projects or specialisation, but no limit may become an infinite passive bonus from repeatedly placing the same structure.

## Level 3: Construction Contract

A Construction Contract is a late-game or accessibility-oriented project service. It is optional and must be more deliberate than Assisted Construction.

A contract declares:

- the site and bounded scope;
- the plan revision and palette mapping;
- the functional result or project purpose;
- the real materials and where they are staged;
- permitted substitutions and rejected blocks;
- whether optional workers may participate;
- construction order and a visible pace;
- tool, fuel, labour, or service costs;
- pause, cancellation, abandonment, and recovery rules;
- the validation evidence required for completion.

### Contract behaviour

- Materials are physically staged or reserved in a local receiving buffer. A contract cannot draw arbitrary items from every warehouse in the world.
- Construction proceeds gradually in visible batches. The player can watch, interrupt, inspect, and change the scope before later batches are settled.
- Workers are optional. A solo player can complete the same functional project, and another human player is never required to keep a contract moving.
- Unloaded settlements use bounded abstract progress only where a future worker system explicitly supports it. The system must not permanently force-load chunks so that NPCs can pathfind.
- A contract can leave partial physical work when abandoned. It cannot delete or refund blocks by guessing what happened after a restart.
- The server records the plan revision, material reservations, settled batches, worker contributions, and validation revision. A crash leaves a reconciling state if the physical result and ledger do not agree.
- Completion validates the declared function, not adherence to one hidden build order or one visual style.

A contract may be a valuable accessibility option for a player who cannot perform thousands of repetitive clicks. It must still require real materials, visible time, bounded labour, and a declared result. It is not a substitute for creative mode and must not quietly provide creative mode in survival.

## Blueprint palette categories

Palette categories describe function and substitution boundaries, not a required architectural style.

| Category | Meaning | Example questions |
|---|---|---|
| **primary wall** | Main mass or enclosure material | Does it provide the declared wall or enclosure function? |
| **secondary wall** | Supporting mass, infill, or contrasting wall material | Is the substitution structurally and visually acceptable to the player? |
| **trim** | Edges, bands, frames, posts, and repeated details | Does it preserve the selected detail pattern? |
| **roof** | Roof surface, cap, or overhead weather protection | Does the replacement preserve coverage and orientation? |
| **floor** | Walkable, work, or decorative interior surface | Does it preserve access and declared floor use? |
| **window** | Transparent or light-permitting opening treatment | Does it preserve the opening, pane, and attachment rules? |
| **foundation** | Base, footing, retaining, or terrain interface | Does it provide the declared support and site boundary? |

A plan can leave a category empty, use more than one material in a category, or declare no substitution. The player chooses the mapping. The validator should prefer tags such as `transparent`, `walkable`, `weather_resistant`, `trim`, or `load_bearing` where those tags are meaningful, but it must show the final block identities before execution.

A medieval palette, industrial palette, modern palette, fantasy palette, or entirely personal palette is equally valid. The system validates the declared functional result and the player's selected substitutions. It does not award a style score.

## Existing mod research

The following review uses official project metadata, official repositories, and official documentation where available. It is a snapshot, not a compatibility guarantee. Every candidate remains provisional until it passes a real 26.2 pack test, licence review, and server-authority checks. No third-party JAR is added by this document.

| Candidate | Evidence for preview and placement | Material consumption and nearby storage | Formats | Server authority and API | Licence and maintenance signal | Provisional decision |
|---|---|---|---|---|---|---|
| **Litematica** | The official project describes a client-side schematic mod with material lists, schematic placements, layer rendering, and Easy Place. Its official wiki documents loading schematics and moving or rotating placements. Sources: [Modrinth metadata](https://api.modrinth.com/v2/project/litematica), [repository](https://github.com/maruohon/litematica), [README](https://raw.githubusercontent.com/maruohon/litematica/master/README.md), [schematic-management wiki](https://github.com/maruohon/litematica/wiki/Schematic-Management). | Material-list support is established. Server-side consumption from nearby inventories is not established. Easy Place is client-side and the project warns that it may be rejected or treated as cheating by servers. It must not be treated as a server-authoritative printer. | `.litematic` is expected by common usage, but the official pages fetched for this review do not state the extension explicitly. Mark **NEEDS_MORE_EVIDENCE** and test the exact 26.2 build. `.schem` conversion is also not assumed. | Modrinth metadata lists `client_side: required`, `server_side: unsupported`, and a client-only environment. No stable public server integration API was verified. | GitHub metadata reports LGPL-3.0 and an active, non-archived repository. Maintenance signals are current repository metadata, not a promise of future support. Source: [GitHub API metadata](https://api.github.com/repos/maruohon/litematica), [licence](https://github.com/maruohon/litematica/blob/ornithe/1.12.2/LICENSE.txt). | Strong Ghost Plan candidate if format and rendering tests pass. Do not use Easy Place as the authority boundary. A custom integration would still own material, project, and contract rules. |
| **Effortless Building** | Official Modrinth documentation describes line, wall, floor, cube, diagonal, circle, cylinder, sphere, mirror, array, radial mirror, replace modes, undo, and survival support. Sources: [Modrinth project metadata](https://api.modrinth.com/v2/project/effortless-building), [project source](https://bitbucket.org/Requios/effortless-building/src/master/), [source README mirror](https://github.com/Requios/effortless-building-multi/blob/master/README.md). | The official description says survival actions consume the right number of blocks and exposes configurable distance, maximum blocks, mirror/array size, tool requirements, and durability use. It does not establish arbitrary nearby-warehouse consumption, so that boundary needs testing or a wrapper. | No schematic format support is established in the sources checked. Treat it as a shape and assisted-placement candidate, not a `.litematic` or `.schem` loader. | Modrinth metadata lists client and server support. The official description says the server part performs bulk placement and that functional settings are admin-controlled. No stable public API for the Forever project was verified. | Modrinth metadata lists LGPL-3.0-only and a Bitbucket source URL. The project metadata reports a recent update, but pack validation remains necessary. Sources: [Modrinth metadata](https://api.modrinth.com/v2/project/effortless-building), [licence in source mirror](https://raw.githubusercontent.com/Requios/effortless-building-multi/master/LICENSE). | Best provisional Assisted Construction candidate because survival material use, configurable limits, and a server component are explicitly described, and Modrinth lists 26.2. Test duplication, block entities, permissions, tool costs, and nearby inventory boundaries before selection. |
| **WorldEdit** | Official documentation establishes selections, clipboard preview through `//paste -n`, masks, paste, rotation, flipping, and schematic load/save. Source: [clipboard documentation](https://worldedit.enginehub.org/en/latest/usage/clipboard/), [clipboard source text](https://worldedit.enginehub.org/en/latest/_sources/usage/clipboard.rst.txt). | The fetched official documentation describes clipboard and paste operations, not survival material consumption from player or nearby inventories. It is therefore not a default survival assistance layer. | `.schem` is verified by the official documentation as the Sponge schematic extension. WorldEdit can also register third-party formats through its API. | Modrinth metadata lists server-only support for 26.2. Official permissions include limits and schematic permissions, and the documentation exposes a developer API. Sources: [Modrinth metadata](https://api.modrinth.com/v2/project/worldedit), [permissions](https://worldedit.enginehub.org/en/latest/permissions/), [documentation](https://worldedit.enginehub.org/en/latest/). | The official Modrinth project description states GPLv3 and the GitHub project is active. Confirm the exact licence obligations for any API linkage or code reuse before packaging. Sources: [Modrinth metadata](https://api.modrinth.com/v2/project/worldedit), [repository metadata](https://api.github.com/repos/EngineHub/WorldEdit). | Useful as an administrator-only tool, import fixture, or `.schem` reference. Do not expose unrestricted paste to survival players. A custom wrapper would need to add real material transactions and the project's bounded limits. |
| **Construction Wand** | Official README describes construction, angel, destruction, restrictions, direction, matching, replacement, random mode, undo, and configurable wand tiers. Sources: [Modrinth metadata](https://api.modrinth.com/v2/project/construction-wand), [repository](https://github.com/Theta-Dev/ConstructionWand), [README](https://raw.githubusercontent.com/Theta-Dev/ConstructionWand/master/README.md). | The README documents inventory sources including shulker boxes, bundles, and other containers, plus configurable durability and maximum block counts. Exact server transaction behaviour and compatibility with Forever's physical-storage rules were not verified. | No `.litematic` or `.schem` support is established in the sources checked. | Modrinth metadata lists client and server support for its published range, but its game versions stop at 1.20.2. No 26.2 release or stable integration API was verified. | GitHub metadata lists MIT, but the repository's last pushed signal is older than the other candidates. Sources: [Modrinth metadata](https://api.modrinth.com/v2/project/construction-wand), [GitHub API metadata](https://api.github.com/repos/Theta-Dev/ConstructionWand), [licence](https://raw.githubusercontent.com/Theta-Dev/ConstructionWand/master/LICENSE). | Do not pin for 26.2 on current evidence. Reconsider only after a 26.2 release and server-authority, material, and block-entity tests. |

> **Validation status.** The two conditional recommendations below have since been
> tested. LAB-03 validated Effortless Building and LAB-04 validated Litematica, both
> as PILOT. LAB-04 additionally rejected Simple Blueprints, which is not in the list
> below, because it gates placement behind a creative-only `setblock` path that is
> unavailable to survival players. Neither mod is in `pack/` yet: PILOT means the
> server-side evidence passed and client and multiplayer verification is outstanding.
> See `labs/results/LAB-03-*.md` and `labs/results/LAB-04-*.md`.

The candidate review does not establish a reason to replace the existing tools with a custom all-in-one builder. The provisional composition is:

1. use Litematica for Ghost Plan rendering and material inspection. **LAB-04 confirmed** the 26.2 build and `.litematic`/`.schem` handling, so the condition is met pending client verification;
2. use Effortless Building for bounded Assisted Construction shapes. **LAB-03 confirmed** its 26.2 server/client build, so the condition is met pending client verification;
3. keep WorldEdit restricted to administrative or controlled `.schem` import workflows unless a survival transaction wrapper is deliberately designed;
4. do not select Construction Wand until it has a supported 26.2 release;
5. add only a small connective layer for capability-web unlocks, palette semantics, physical inventory reservations, equipment costs, project validation, accessibility text, and contracts.

These are provisional roles, not implementation commitments. The companion layer must be able to run with a safe no-op when an optional candidate is absent. It must not make the modpack fail because a client-only preview tool is unavailable.

## Validation checklist for a future implementation ticket

A future ticket should not close on screenshots alone. It should test:

- a valid and malformed `.litematic` input, if supported by the pinned build;
- a valid and malformed `.schem` input;
- oversized dimensions, excessive block counts, unknown block states, fluids, entities, commands, and block entities;
- Ghost Plan preview, material list, rotation, mirroring, layers, and non-colour status cues;
- manual placement with no assistance installed;
- Assisted Construction line, wall, floor, mirror, rotation, repeated detail, and palette replacement;
- exact material consumption from player inventory;
- bounded nearby-container access with permissions and changing container revisions;
- insufficient materials, broken tools, protected blocks, changed targets, unloaded chunks, and interrupted batches;
- server rejection of client-predicted outcomes and retry idempotence after reconnect or restart;
- no duplication through undo, crash recovery, shulker contents, cache contents, or warehouse reservations;
- Construction Contract pause, cancellation, partial physical work, optional workers, and bounded unloaded simulation;
- solo completion and multiplayer contribution attribution;
- palette substitutions across all seven categories without enforcing an architecture style;
- licence and attribution obligations for every selected third-party mod.

Until those checks pass, the correct state is provisional research, not a promise that any candidate satisfies the Many Roads Home design.

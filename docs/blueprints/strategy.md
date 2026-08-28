# Blueprint strategy

Blueprints are optional content. A player who never opens a blueprint must be able to
reach every capability, found every settlement, and complete every project. This
document defines how blueprint content is organised, licensed, and distributed, and it
deliberately stops short of making any of it a requirement.

The reason for that limit is a specific failure mode. Colony-style mods that require a
blueprint for every building convert Minecraft from a building game into a construction
queue: the player stops designing and starts fetching materials for someone else's
design. Many Roads Home is aimed at people who want a world worth keeping, including
people who enjoy building badly and improving. Blueprints exist to help players who are
not confident builders, not to replace the act of building.

## Core neutral starter library

A small library shipped with the pack, covering the shapes a new settlement genuinely
needs: a modest house, a workshop, a store room, a shelter, and a road or bridge
segment. It exists so a player who registers a settlement is not staring at an empty
field with no idea what a valid building looks like.

Requirements:

- every entry is original work commissioned or built for this project;
- every entry is deliberately plain, so it reads as a starting point rather than a
  finished style;
- every entry uses the palette categories below, so it can be re-skinned;
- no entry is required by any validation rule. If a settlement check ever says "place
  blueprint X", that is a bug in the check.

## Optional style libraries

Distributed separately from the core pack. A style library is a coherent architectural
voice: a timber vernacular, a stone highland style, a desert adobe style. Players opt in.

No single architecture style may be required, and the pack must never validate a
settlement against a style. `docs/design-principles.md` principle 7 already forbids
evaluating architectural theme, and blueprints must not smuggle that rule back in
through the side door.

## Palette categories

Every blueprint declares its blocks against these roles rather than hardcoding a block:

- primary wall
- secondary wall
- trim
- roof
- floor
- window
- foundation

Palette substitution is what makes one blueprint usable in a jungle, a desert, and a
snowfield without shipping three copies. It also lets a player keep their own material
language while using a shape they like.

## Licensing and provenance

This is the section most likely to cause real trouble, so it is explicit.

- **Do not download or redistribute community schematics without permission and a
  compatible licence.** Popular schematic sites are full of work whose licence is either
  unstated or non-commercial, and "it was free to download" is not a licence.
- Every blueprint shipped by this project must have a recorded origin: who built it,
  when, under what terms, and whether the author agreed to redistribution.
- Original builds commissioned for the project are the safest source and should be the
  default.
- User imports are fine and are the player's own business. The pack must never upload,
  redistribute, or claim a player's imported schematic.

## AI-assisted builds

An AI-assisted build may be used as a starting point, but it must be reviewed by a human
who can judge whether it is actually buildable, survivable, and stylistically coherent
before it enters a library. Generated builds tend to produce floating blocks, unreachable
interiors, and material choices that are absurd in survival. Reviewing that is real work
and must not be skipped because the output looked plausible in a screenshot.

## File formats

The pack should read the formats players already have rather than inventing one:

- `.litematic`, the Litematica format, which is the de facto standard for survival
  schematic previews;
- `.schem`, the WorldEdit sponge format, widely produced by build tooling.

Format support is a third-party concern. `docs/architecture/ownership-matrix.csv`
assigns schematic previews to a third-party mod, and this document does not change that.

## Multiplayer distribution and permissions

- A server should be able to offer a shared library without every player installing it
  by hand.
- Placing a blueprint is a server-authoritative action subject to permissions and claim
  rules. A blueprint must never be a way to place blocks a player could not place
  manually.
- Material cost is real. See `docs/design/building-assistance.md`: Ghost Plan previews
  cost nothing, Assisted Construction consumes real materials and tool durability, and
  Construction Contracts require full materials and time.

## Attribution

Every library ships an attribution file naming each build, its author, and its licence.
Attribution is not optional even for original work, because provenance is what makes it
possible to answer a licensing question two years later without archaeology.

## What this document does not do

It does not select a schematic mod, define a blueprint file schema, or authorise
building any of this. Those are later tickets under M10, gated on the building and
gathering lab (LAB-02) and the building automation lab (LAB-12).

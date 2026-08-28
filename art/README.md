# Forever art sources

This file is the practical quickstart for the Forever art tree. The authoritative
production reference is [`docs/assets/pipeline.md`](../docs/assets/pipeline.md). Read that
document before exporting an asset. The governing visual constraints and approval gate
are in [`docs/assets/art-direction.md`](../docs/assets/art-direction.md).

## Current status

Forever is foundation-only. No gameplay system or gameplay asset is implemented. The
asset manifest records planned future needs, but a `planned` row is not permission to
produce art. The only existing production PNG is the deliberately obvious original
placeholder at
`src/client/resources/assets/forever/textures/item/forever_missing_asset.png`. It does
not copy or imitate Minecraft's built-in missing-texture graphic.

## Hard rules

- **Final art requires both an approved manifest row and an approved gameplay need.**
  A brief or a planned row alone is not approval. This is the AI rule 17 and
  art-direction gate.
- **Never download, extract, copy, or trace vanilla Minecraft textures.** New artwork
  must be original or separately licensed with its provenance recorded.
- **Never make a state colour-only.** Pair colour with shape, a mark, an overlay, text,
  a tooltip, or another non-colour cue. This is design principle 16.
- **Art is presentation, never gameplay logic.** Code and data must not inspect pixels,
  filenames, model appearance, palette values, or the presence of an overlay to decide
  an outcome. The server remains authoritative.
- **AI concepts are references for discussion only.** Reconstruct any approved result
  manually, pixel by pixel. Never trace, filter, export, or commit the generated concept.
  This protects licensing, provenance, and style coherence.

## Directory roles

```text
art/                                           # editable sources, never loaded by Minecraft
├── palettes/                                  # approved palettes
├── templates/                                 # authoring templates
├── source/
│   ├── items/                                 # Aseprite item sources
│   ├── blocks/                                # block texture sources
│   ├── gui/                                   # GUI and icon sources
│   ├── entities/                              # entity bases and overlays
│   └── models/                                # Blockbench sources
└── previews/                                  # optional derived review images

src/client/resources/assets/forever/           # exported resources Minecraft reads
├── textures/{item,block,gui,entity}/
├── models/{item,block}/
├── items/
├── blockstates/
└── lang/
```

Keep layered `.aseprite` and `.bbmodel` sources, palettes, and optional previews under
`art/`. Keep only exported PNGs and the JSON and language resources that Minecraft loads
under `src/client/resources/assets/forever/`. This split preserves editable provenance,
keeps authoring tools out of the mod, and makes the runtime tree independently
validatable. The game never loads `art/`.

## Quickstart

1. **Confirm the need and read the brief.** Check the relevant system specification,
   accepted decisions, and asset brief. Confirm required states and accessibility
   treatment. If the gameplay need is not approved, stop without creating final art.
2. **Add or review one manifest row.** Use a stable lower `snake_case` asset ID, preserve
   category and ID ordering in [`asset-manifest.csv`](../docs/assets/asset-manifest.csv),
   and begin with status `planned`. Do not duplicate rows for state combinations.
3. **Author in the source tree.** For a 16x16 item, use Aseprite in
   `art/source/items/<asset_id>.aseprite`. Keep the full 16x16 canvas and the editable
   layers. For a model, keep the Blockbench `.bbmodel` in `art/source/models/`. For
   blocks, GUI assets, and entity overlays, use the matching source directory.
4. **Review at Minecraft scale.** Check the silhouette at 1x, then check greyscale and
   silhouette-only views. State must remain legible without hue. Use composable overlays
   rather than hand-drawing every wear, rank, profession, or regional combination.
5. **Export only after the gate.** For item and block sprites, export an RGBA PNG at 1x
   with the full canvas preserved, no crop, trim, anti-aliasing, smoothing, or lossy
   conversion. Use lower `snake_case`. Blockbench model JSON goes to `models/item` or
   `models/block`, with any blockstate, item definition, and texture references in the
   corresponding production directories.
6. **Check repeating blocks.** Repeat a tile at least 3x3 with nearest-neighbour
   sampling. Compare opposite edge columns and rows, including repeated corners. Fix
   accidental value jumps, broken lines, and isolated highlights. The full seam workflow
   is in the [pipeline reference](../docs/assets/pipeline.md#block-texture-tiling-and-seam-checks).
7. **Validate the exported tree.** From the repository root, run:

   ```sh
   ./gradlew validateAssets
   ```

   This developer-only task uses the pure-JDK `assetTools` source set. It is not shipped
   in the mod JAR and does not add an image-generation dependency to Minecraft. Exit `0`
   means clean input, exit `1` means asset or manifest violations, and exit `2` means a
   usage or input/output error. Fix every reported stable violation code.
8. **Test in a disposable world.** Only after validation succeeds, run:

   ```sh
   ./gradlew runClient
   ```

   Use a disposable development world, never a real survival save. Inspect inventory and
   hotbar scale, model orientation, transparency, placed blocks, repeated block faces,
   entity overlays, and text or tooltip cues as applicable. If the gameplay system does
   not exist yet, record the in-game check as blocked by foundation scope rather than
   inventing a custom item or block.

For the complete Aseprite settings, Blockbench export path, entity composition rules,
nearest-neighbour preview commands, manifest example, validator CLI, and review checklist,
use [`docs/assets/pipeline.md`](../docs/assets/pipeline.md).

## Validator interface

The underlying CLI is documented for tool users who need a direct invocation:

```text
dev.forever.tools.assets.AssetValidatorApplication --assets <dir> --manifest <file> [--json <out>]
```

The normal developer entry point remains `./gradlew validateAssets`. The validator checks
exported production assets and their manifest relationship. It does not read Aseprite or
Blockbench sources, decide whether a gameplay need is approved, generate art, or replace
visual and in-game review. Planned manifest rows are not rejected merely because their
production file does not exist.

Do not add an image-processing library to the project for previews. An artist may use
FFmpeg or ImageMagick ad hoc with nearest-neighbour scaling, as described in the
[authoritative pipeline reference](../docs/assets/pipeline.md#optional-nearest-neighbour-previews).
Those tools are optional local tooling, not build dependencies.

## Before changing status to final

Confirm the gameplay need, manifest row, source provenance, export settings,
accessibility review, composable state treatment, validator result, and disposable-world
review. If any gate is missing, leave the row `planned` or `placeholder` and report what
is missing. Do not make the source tree look complete by adding speculative artwork.

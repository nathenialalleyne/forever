# Forever art and texture pipeline

This is the authoritative operational reference for Forever art production. The
[`art/README.md`](../../art/README.md) file is the short, practical entry point for
artists and contributors. It links here instead of repeating the detailed workflow.
The governing visual contract remains [`art-direction.md`](art-direction.md), which
sets the design constraints and approval gate. This document explains how to apply
that contract to editable sources, exported resources, validation, and in-game review.

## Current status and hard gates

Forever is still foundation-only. No gameplay system is implemented, and no gameplay
asset is currently approved for production. The only existing production PNG is
`src/client/resources/assets/forever/textures/item/forever_missing_asset.png`. It is a
deliberately obvious original placeholder for a missing-asset path. It does not imitate
Minecraft's built-in missing-texture graphic and is not a gameplay asset.

The following rules are hard gates, not suggestions:

1. **Do not create final artwork without an approved manifest row and an approved
   gameplay need.** A row with status `planned` records a possibility, not permission to
   draw. The system ticket or design brief must explain why the asset is needed, which
   states it must show, and how those states remain accessible. This is the AI rule 17
   and art-direction gate.
2. **Never download or copy vanilla Minecraft textures.** General observation of
   Minecraft's scale and visual language is not a licence to copy pixels, extract files,
   or trace a vanilla resource. New Forever artwork must be original or separately
   licensed with provenance recorded.
3. **Never communicate a state by colour alone.** Use silhouette, a mark, an overlay,
   an icon shape, a value change, text, or a tooltip as well. This is design principle
   16. A colour change may support a state, but it cannot be the only cue.
4. **Textures and models are presentation only.** Server-authoritative code and data
   identify items, blocks, entities, and states. Gameplay logic must never inspect a
   pixel, texture filename, model appearance, palette value, or missing texture result.
   Replacing or omitting a resource must not change the server outcome.
5. **Keep provenance explicit.** Do not commit an AI-generated image, a copied texture,
   an unreviewed third-party asset, or an untraceable derivative. Follow
   [ADR 0020](../adr/0020-private-project-license-separation.md) for ownership,
   attribution, and third-party boundaries.

A planned manifest row may exist while its source and production file do not. That is
expected in the current milestone. Do not turn a planned row into a placeholder or final
asset merely to make the tree look complete.

## Two trees, two purposes

Editable work and game-loaded resources are deliberately separate:

```text
art/                                           # editable sources, never loaded by Minecraft
├── palettes/                                  # approved project palettes and references
├── templates/                                 # blank or structural authoring templates
├── source/
│   ├── items/                                 # Aseprite item sources and layers
│   ├── blocks/                                # block texture sources
│   ├── gui/                                   # GUI and icon source files
│   ├── entities/                              # entity base and overlay sources
│   └── models/                                # Blockbench model sources
└── previews/                                  # optional, derived review images

src/client/resources/assets/forever/           # exported production resources only
├── textures/
│   ├── item/                                  # item PNGs
│   ├── block/                                 # block PNGs
│   ├── gui/                                   # GUI and icon PNGs
│   └── entity/                                # entity and overlay PNGs
├── models/
│   ├── item/                                  # item model JSON
│   └── block/                                 # block model JSON
├── items/                                     # item definition JSON
├── blockstates/                               # blockstate JSON
└── lang/                                      # client-only presentation strings
```

Translations belong in `src/main/resources/assets/forever/lang/en_us.json` whenever the
key names a server-authoritative concept, which today covers mastery, career, economy,
storage, and Field Guide text. Common code must be able to name those things without
reaching into a client-only source set, and `docs/architecture.md` forbids that
direction of dependency. Reserve the client `lang/` directory above for strings that
only a screen or other client-side presentation ever produces. When in doubt, add the
key to the common file next to the system that owns it.

The source tree exists for editing, review, and provenance. A `.aseprite`, `.bbmodel`,
palette, template, or preview under `art/` is not a Minecraft resource and must never
be used as an implicit runtime input. The production tree is deliberately shaped like
Minecraft's resource namespace. It is the only tree Minecraft reads for Forever assets.
Do not put editable sources in `src/client/resources`, and do not put a production export
in `art/source` and assume that the game will find it.

The split also protects the boundary between art and behaviour. Artists can preserve
layered sources, palette notes, and review previews without packaging authoring metadata
in the mod. Runtime resources can be validated independently, resource-pack authors can
replace them, and a missing export is visible instead of being silently supplied from an
untracked working file.

## Naming and manifest relationship

Use lower `snake_case` for asset IDs, resource basenames, and exported filenames. Names
are stable identifiers, not descriptions that may be changed casually after code or JSON
references exist. The production PNG basename must match the manifest `asset_id`, because
the validator identifies a production asset from its filename stem. For example, the item
texture for `item_coin_purse` would be
`textures/item/item_coin_purse.png`. If a brief specifies a different path, the brief and
manifest are the authority. Do not invent a second alias just to satisfy a tool or a model
export.

The manifest at [`asset-manifest.csv`](asset-manifest.csv) is sorted by category and then
`asset_id`. Preserve that ordering. Its columns record the stable ID, category, system,
milestone, required states, base resolution, vanilla reuse, accessibility requirement,
notes, and status. Statuses have these meanings:

- `planned` reserves a reviewed possibility. A planned row is **not rejected for having
  no production file**.
- `placeholder` identifies a temporary exported resource used for development or a
  visible fallback. It must not be presented as final art.
- `final` identifies an approved production export. It still needs the normal validator
  and in-game review.

A manifest entry does not make gameplay real. It records the art contract for a system
that must be approved separately. Conversely, a production file without a manifest row
is not an acceptable shortcut, even if it renders correctly on one machine.

## The 16x16 item workflow from need to review

The normal item workflow is intentionally small and repeatable:

1. **Confirm the need.** Read the relevant system specification, accepted design
   decisions, and asset brief. Confirm that the gameplay ticket has an approved need and
   that the required states and accessibility treatment are explicit. If the need is not
   approved, stop at documentation.
2. **Reserve the asset in the manifest.** Add or review one row with a stable snake-case
   ID, category `item`, required states, `16x16` resolution, reuse decision, and a note
   that explains the player-facing purpose. Preserve category and ID ordering. A row
   marked `planned` does not authorise production artwork.
3. **Create an editable source.** Work in `art/source/items/`, normally with an
   `.aseprite` file named after the manifest ID, such as
   `art/source/items/item_coin_purse.aseprite`. Keep layers and palette choices in the
   source so a later review can understand what was made and change one component without
   redrawing the whole item.
4. **Draw for Minecraft scale.** Work on a 16x16 canvas at integer zoom. Establish one
   readable silhouette first, then add only the marks that carry meaning. Use the
   approved palette and keep important features at or above one pixel where the brief
   requires them. Review the item at 1x in a hotbar-sized view, not only at a large zoom.
5. **Check accessibility and state composition.** Inspect the silhouette alone and in
   greyscale. Confirm that state differences do not depend on hue. If the item has wear,
   broken, profession, or specialisation states, use the shared overlay vocabulary rather
   than drawing every combination as a separate image. Exact values and instructions
   belong in text, tooltips, or the Field Guide when the system exists.
6. **Export a production PNG.** Export the full 16x16 canvas as an RGBA PNG at 1x. Do
   not trim transparent margins, crop the canvas, resample, anti-alias, smooth, dither,
   or use a lossy format. Pixel edges must remain hard and aligned to the canvas. Use the
   approved resource basename, for example
   `src/client/resources/assets/forever/textures/item/item_coin_purse.png`.
7. **Add the required resource references.** If the item needs an item definition or
   model, put JSON in the matching production directory. Use the Forever namespace and
   the exported resource path. Keep the editable `.aseprite` source in `art/` and the
   JSON that Minecraft reads in `src/client/resources/assets/forever/`.
8. **Validate before launching Minecraft.** Run `./gradlew validateAssets` from the
   repository root. Fix every violation rather than suppressing it. A clean result means
   the exported tree satisfies the mechanical checks. It does not replace visual review
   or gameplay approval.
9. **Test in a disposable world.** Run `./gradlew runClient` only with a disposable
   development world. Check the item in the inventory and hotbar, at normal UI scale, in
   first-person use if applicable, and in third-person or world placement where
   applicable. Check model orientation, transparency, resource references, and any
   text-visible state cue. Never point the client at a real or irreplaceable survival
   world.
10. **Record the review.** Confirm the manifest status and provenance, note the test
    result, and retain the editable source. A final status is appropriate only after the
    gameplay need, manifest row, accessibility review, validator, and in-game review all
    pass.

### Aseprite authoring and export settings

Aseprite is the preferred authoring tool for 16x16 item sprites because it keeps the
canvas, palette, layers, and pixel operations explicit. The exact application version is
not a repository dependency. Artists may use its GUI or its batch interface, provided
the resulting file obeys the same settings.

For a new item, use this convention:

```text
Editable source:   art/source/items/<asset_id>.aseprite
Production export: src/client/resources/assets/forever/textures/item/<asset_id>.png
```

For example:

```text
art/source/items/item_coin_purse.aseprite
src/client/resources/assets/forever/textures/item/item_coin_purse.png
```

The source should retain separate layers for the silhouette, restrained shading, and
state or overlay components where that helps review. Do not flatten the only editable
copy. Avoid layers that exist only to preserve an imported or generated image. The
source must be an original reconstruction from the approved brief, not a trace.

Use these export settings:

- canvas: exactly `16x16` pixels.
- format: PNG with RGBA transparency.
- scale: `1x`, with the full canvas preserved.
- crop or trim: disabled.
- resampling: disabled. If a preview is needed, use nearest-neighbour separately.
- anti-aliasing, interpolation, smoothing, and automatic colour reduction: disabled.
- pixel placement: integer coordinates with hard pixel edges.
- output name: lower `snake_case`, with no spaces, capitals, or category-specific alias.

An equivalent batch export may look like this, adjusted for the artist's local Aseprite
installation:

```sh
aseprite -b art/source/items/item_coin_purse.aseprite \
  --save-as src/client/resources/assets/forever/textures/item/item_coin_purse.png
```

The command is an authoring convenience, not a Gradle task and not a build dependency.
After exporting, inspect the PNG itself. Do not assume that a source preview or an
Aseprite layer is what Minecraft will load. A file that is entirely transparent, has an
unexpected dimension, or contains a format the validator cannot read is a failed export.

### State layers and reusable overlays

The texture family should be designed as a base plus composable state layers wherever
possible. For example, a tool can have one ordinary base, one worn overlay, one broken
overlay, and independent specialisation emblems. It should not have a unique file for
every pair of condition and specialisation states. The state is authoritative in server
data. The texture only helps the player read it.

Keep overlays in a fixed 16x16 canvas with a documented alignment point. A transparent
pixel is part of the alignment contract. Test each overlay on more than one base shape,
and check that it remains legible if a resource pack changes the base. If a future
renderer cannot show an overlay, the item must still be identifiable through its name,
tooltip, screen text, or Field Guide entry.

## Blockbench workflow for models

Blockbench sources belong in `art/source/models/`. A model is subject to the same
manifest and gameplay-need gate as a texture. The `.bbmodel` file is editable source and
never a runtime resource.

Use this sequence for an approved model:

1. Read the block or item brief and confirm the silhouette, interaction states, and
   accessibility treatment. Prefer ordinary Minecraft geometry and sounds where the
   design does not need something new.
2. Create a Blockbench project using the Minecraft Java block or item model format that
   matches the target resource. Keep the project on the intended grid and retain the
   `.bbmodel` source as `art/source/models/<asset_id>.bbmodel`.
3. Use only original Forever textures or explicitly approved resources. Do not import
   or export vanilla texture files. A model may reuse ordinary Minecraft geometry or
   model conventions without copying its pixels.
4. Assign texture references using the Forever namespace and the approved production
   path. Keep texture files in `textures/item`, `textures/block`, `textures/gui`, or
   `textures/entity` as appropriate.
5. Export the Minecraft Java model JSON without changing the approved resource name.
   Item models land in
   `src/client/resources/assets/forever/models/item/<asset_id>.json`. Block models land
   in `src/client/resources/assets/forever/models/block/<asset_id>.json`. Any blockstate
   JSON lands in `src/client/resources/assets/forever/blockstates/<asset_id>.json`, and
   item definition JSON lands in `src/client/resources/assets/forever/items/<asset_id>.json`.
6. Open the exported JSON as text and review its references. A Blockbench export can be
   visually correct in the editor while still pointing at a stale, missing, or wrongly
   namespaced texture. The validator's `MISSING_TEXTURE_REFERENCE` check is intended to
   catch this boundary.
7. Run `./gradlew validateAssets`, then inspect the model in Minecraft with
   `./gradlew runClient` and a disposable world. Check all relevant views, including
   inventory scale, world scale, block faces, rotations, and transparency.

Blockbench is an authoring tool, not a required runtime dependency. Do not put a
Blockbench executable, plugin, generated preview, or source model in the client resource
tree. A model JSON belongs in production only when its gameplay need and manifest row
have passed the gate.

## Block texture tiling and seam checks

A block texture that repeats across a wall or floor must not reveal an accidental seam.
The usual target is a 16x16 PNG unless the manifest says otherwise. If a brief calls for a
deliberate border or pattern, document that intent and make it consistent at every repeat.

Use this review sequence:

1. Verify the exported dimensions and format with `./gradlew validateAssets`.
2. Make a 3x3 or larger preview by repeating the same tile. Keep the preview at integer
   scale with nearest-neighbour sampling so interpolation cannot hide a one-pixel error.
3. Inspect every horizontal join. Compare the rightmost column of one tile with the
   leftmost column of the next. Inspect every vertical join by comparing the bottom row
   with the top row of the next. Look for a jump in colour value, a broken line, an
   isolated highlight, or a shadow that stops at the boundary.
4. Inspect corners where horizontal and vertical joins meet. A texture can look correct
   along one edge while producing a four-pixel defect at a repeated corner.
5. Review at both 1x and a large nearest-neighbour zoom. The 1x view checks Minecraft
   readability. The tiled zoom makes a repeated seam easier to locate, but it must not
   be used to justify detail that disappears at game scale.
6. If a seam is intended, document that intent in the asset brief or manifest notes. Do
   not silently fix a purposeful border by making the texture mirror or wrap.

A practical ad hoc preview command using ImageMagick is:

```sh
montage input.png input.png input.png input.png input.png input.png input.png input.png input.png \
  -tile 3x3 -geometry 16x16+0+0 -background none miff:- | \
  convert - -filter point -resize 768x768 art/previews/input-tile-3x.png
```

ImageMagick is optional developer tooling invoked by the artist. It is not a repository
or build dependency. If the local ImageMagick build does not accept the `miff:-` form,
use its equivalent montage or tile operation, then preserve the essential properties:
three repeated copies, no interpolation, and an integer scale. Do not commit a preview
as a production resource.

## Entity overlays and compositing

Entity presentation has more combinations than it is practical or safe to draw by hand.
A profession, rank, regional accent, work context, and downed state can combine in many
ways. The source workflow therefore treats an entity as a base plus independent,
aligned overlay layers:

```text
art/source/entities/
├── base/                 # original base or approved base reference
├── profession/          # role shapes or badges
├── rank/                # geometric rank indicators
├── regional/            # restrained accent layers
└── state/               # downed, work, or other state layers
```

The exact subdirectories may be introduced with the implementation ticket that first
needs them. Do not create empty gameplay content merely to fill this example tree.
When the entity system is approved, export overlays to
`src/client/resources/assets/forever/textures/entity/` with stable snake-case names and
fixed UV or canvas alignment. The runtime renderer may compose a base and zero or more
overlays. It must never need a separate illustration for every combination.

Composition is preferable for three reasons. It reduces the number of files that must
be reviewed and kept stylistically coherent. It means a new rank or regional accent can
be added without redrawing every existing profession. It also makes alternate resource
packs more viable because the server state remains independent of one particular merged
image. Overlay shape, value, and context must carry meaning. A regional tint can support
place identity, but it cannot be the sole profession or rank cue. Names, interaction
icons, tooltips, and other text remain available when an overlay is missing or disabled.

Align overlays against more than one base before approval. Check transparency around
edges, one-pixel marks at normal scale, and the result in a high-contrast or colour-
reduced view. Never encode server outcomes by testing whether an overlay exists.

## Optional nearest-neighbour previews

Previews help artists review a 16x16 source at a size where a seam or silhouette issue is
easy to see. They are derived review files, not production resources and not a build
step. Do not add an image-generation library or a Gradle dependency for preview creation.

For a concrete reproducible command with FFmpeg:

```sh
ffmpeg -y -i src/client/resources/assets/forever/textures/item/item_coin_purse.png \
  -vf "scale=256:256:flags=neighbor" \
  art/previews/item_coin_purse-16x.png
```

The command takes a 16x16 source to 256x256, a 16x integer enlargement, and uses
nearest-neighbour sampling. It does not alter the source. Substitute the approved input
and output names for another asset. `ffmpeg` is optional artist tooling invoked ad hoc on
the developer's machine. It is not a project dependency and must not be added to the
Minecraft runtime or the `assetTools` source set.

An ImageMagick equivalent is:

```sh
convert src/client/resources/assets/forever/textures/item/item_coin_purse.png \
  -filter point -resize 256x256 art/previews/item_coin_purse-16x.png
```

Do not use a browser screenshot, an anti-aliased editor zoom, or a smoothed thumbnail as
an acceptance preview. Those can conceal pixel gaps and make a failed export look clean.
If a preview is committed, it belongs under `art/previews/` and should be clearly derived.
The production PNG remains the reviewed input.

## AI-generated concepts and provenance

AI-generated concepts may be used privately to discuss a silhouette or a direction, but
they are not production inputs. Never trace one, run it through a pixel-art filter, export
it as a PNG, or commit it as a source image. Reconstruct the approved result manually,
pixel by pixel, from the accepted brief and project palette. The reconstructed work must
be independently reviewed rather than treated as a faithful copy of the concept.

This restriction protects more than visual quality. Generated images have uncertain
licensing and provenance. A model output may reproduce recognisable elements from a
training source, and a later contributor may not be able to identify which source was
followed. Direct export would make that uncertainty part of the mod's resource tree.
Manual reconstruction also preserves a coherent 16x16 language, keeps details legible at
Minecraft scale, and makes state and accessibility decisions deliberate rather than
accidental. The art-direction gate still applies: an appealing concept cannot create a
gameplay need, approve a manifest row, or bypass the AI rule 17 gate.

Do not download a reference texture from Minecraft, a resource pack, a mod, or an image
search and trace it either. If a third-party reference is necessary for a design review,
record its source and licence outside the production asset, obtain the required approval,
and do not copy its pixels into Forever. When provenance is uncertain, do not commit the
asset.

## Adding one asset to the manifest

The manifest is a CSV contract, not a list of files to generate automatically. Add one
asset only when the gameplay need and art brief have been approved.

Follow these steps:

1. **Choose one stable ID.** Use lower `snake_case`, keep the category prefix used by the
   existing manifest, and do not encode a transient state or colour in the ID. For an
   item family, `item_coin_purse` identifies the object while its states stay in
   `required_states`.
2. **Fill every column.** Record the category, system, milestone, required states,
   resolution, degree of vanilla reuse, accessibility requirement, notes, and status.
   State the player-facing reason in `notes`, including whether the row is only planned.
3. **Insert the row in deterministic order.** Sort by category and then `asset_id`. Do
   not reorder unrelated rows or make a duplicate entry to represent another state.
4. **Keep the initial status honest.** Use `planned` when the need is recorded but no
   export is approved. Use `placeholder` only for a deliberate temporary resource. Use
   `final` only after the production export, accessibility review, validator, and
   in-game review pass.
5. **Create the corresponding production resource only after the gate.** Match the
   approved basename and put textures, models, item definitions, blockstates, and
   translations in their respective production directories. Keep editable sources under
   `art/`.
6. **Validate the relationship.** Run `./gradlew validateAssets`. Resolve duplicate IDs,
   unmanifested files, missing files for non-planned rows, and broken JSON references.
7. **Review the player outcome.** Use `./gradlew runClient` with a disposable world and
   check the resource at Minecraft scale. Update the brief or ticket if the visual result
   exposes an accessibility or gameplay explanation gap.

Worked example using the existing Coin Purse brief. This row is already present in the
current manifest, so do not duplicate it. It demonstrates the CSV shape for one item:

```csv
item_coin_purse,item,economy,M2-design-approval,closed;open;empty;contains_balance;full;broken,16x16,partial,"Numeric balance and state text accompany the icon","Planned only. No art is to be produced until the gameplay need and production gate are approved.",planned
```

For this example, the `item_coin_purse` ID describes the family. The states are not six
separate final illustrations. The future item may use a small set of base shapes and
reusable closure or broken overlays, as the brief requires. Until the economy gameplay
need is approved for implementation, the `planned` status correctly has no corresponding
production PNG. If the row later becomes approved and an item texture is needed, the
normal basename would be `item_coin_purse.png` under `textures/item`, with any model or
item JSON added to its matching production directory.

CSV values containing commas must remain quoted. State names use the existing
semicolon-delimited convention inside `required_states`. Do not add comments to the CSV
row or change the column header to carry implementation details.

## Asset validation

The canonical validation command is:

```sh
./gradlew validateAssets
```

This is a developer-only tool in the separate `assetTools` source set. It is pure JDK,
is never shipped in the mod JAR, and has no image-generation dependency in the Minecraft
runtime. It validates exported production resources and their manifest relationship. It
does not load Aseprite or Blockbench sources, generate art, decide whether a gameplay need
is approved, or replace visual review.

The underlying CLI is:

```text
dev.forever.tools.assets.AssetValidatorApplication --assets <dir> --manifest <file> [--json <out>]
```

The standard inputs correspond to the production namespace and checked-in manifest:

```sh
java dev.forever.tools.assets.AssetValidatorApplication \
  --assets src/client/resources/assets/forever \
  --manifest docs/assets/asset-manifest.csv \
  --json build/asset-validation.json
```

The direct `java` form assumes the `assetTools` classes are already on the local classpath.
Use the Gradle task for ordinary development so that the project supplies that classpath.
The exit contract is stable:

- exit `0`: the inputs are clean.
- exit `1`: the inputs contain asset or manifest violations.
- exit `2`: usage or input/output error, such as a missing argument or unreadable path.

The validator reports these stable violation codes:

| Code | Meaning | Typical correction |
|---|---|---|
| `NAME_NOT_SNAKE_CASE` | An asset ID or exported resource name violates the lower snake-case convention. | Rename the ID or resource and update all approved references together. |
| `UNSUPPORTED_IMAGE_FORMAT` | A production image uses a format outside the supported texture set. | Export the approved texture as an RGBA PNG. |
| `UNEXPECTED_DIMENSIONS` | An image dimension does not match the manifest or asset contract. | Restore the approved canvas, normally 16x16 for item and block textures. |
| `FULLY_TRANSPARENT` | An image contains no visible pixels. | Fix the export or remove the unneeded file. Do not hide an empty file behind a manifest row. |
| `MISSING_TEXTURE_REFERENCE` | Production JSON names a texture that is not present at the resolved Forever path. | Correct the namespace or path, or export the approved texture. |
| `DUPLICATE_ASSET_ID` | The manifest declares the same asset ID more than once. | Keep one authoritative row and merge the approved information. |
| `UNMANIFESTED_ASSET` | A production asset exists without a corresponding manifest contract. | Add an approved row first, or remove the unapproved export. |
| `MANIFEST_ENTRY_MISSING_ASSET` | A manifest entry that requires a file has no matching production asset. | Export the approved file or keep the row `planned` until production is authorised. |

`planned` rows are deliberately exempt from the missing-file check. That is what lets
the manifest describe future needs without forcing speculative art into the repository.
A clean validator result therefore means the current exported tree is internally
consistent. It does not promote a planned row to final status.

## In-game verification

After validation succeeds, run the client against a disposable development world:

```sh
./gradlew validateAssets
./gradlew runClient
```

Do not use a real Forever survival save. For an item, check inventory and hotbar scale,
first-person use, third-person appearance where relevant, transparency, model orientation,
and every state that the current system exposes. For a block, place a small wall or floor
large enough to reveal tiling, inspect all faces, and verify that ordinary block placement
still works. For a GUI icon or entity overlay, check the actual screen or entity at normal
scale and in a high-contrast or colour-reduced view. Confirm that the state remains
understandable through text, a tooltip, a label, or another non-colour cue.

A resource-pack replacement or missing optional model must not make the server decide a
different outcome. If the feature has not been implemented yet, record that the in-game
check is blocked by foundation scope rather than inventing a test item or block. The
current project has no gameplay assets to exercise beyond the deliberate missing-asset
placeholder.

## Final review checklist

Before describing an asset as final, confirm all of the following:

- the gameplay need is approved by the relevant ticket or system decision.
- the manifest row exists, is correctly ordered, and is no longer merely speculative.
- the editable source and provenance are retained under `art/`.
- the exported production resource has the approved name, format, and dimensions.
- every visual state has a non-colour cue and an in-game text explanation where needed.
- overlays compose without a combinatorial set of hand-drawn files.
- no vanilla or unlicensed third-party pixels were downloaded, copied, or traced.
- `./gradlew validateAssets` exits `0`.
- `./gradlew runClient` was reviewed with a disposable world.
- the asset remains presentation-only and cannot become a gameplay logic dependency.

If any item is false, keep the asset planned or placeholder and report the missing gate.
Do not resolve a design or licensing gap by committing an attractive but unauthorised
texture.

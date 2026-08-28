# Asset art direction

## Role and production gate

Forever assets are a presentation layer for server-authoritative systems. They must
make a mechanic legible without making that mechanic depend on a particular texture,
model, sound, language, or colour palette.

The default target is **16x16, vanilla-compatible presentation**. Art should be
readable at Minecraft scale, remain recognisable in a busy inventory or world, and
use minimal visual clutter. Avoid highly detailed MMO-style art, tiny decorative
marks that disappear at normal scale, and UI chrome that turns Minecraft into a
spreadsheet.

**Do not create final art before the asset manifest and the gameplay need are
approved. This is AI rule 17.** The manifest is the planning gate. A planned row is
not evidence that an asset is needed, and a placeholder is not permission to add a
PNG, model, or sound file. Every final asset needs a named system need, required
states, accessibility treatment, and an approved manifest row first.

## Baseline visual rules

- Start with ordinary Minecraft silhouettes, proportions, contrast, and material
  language. A player should be able to identify a tool or block in the hotbar before
  seeing its detail.
- Prefer one clear silhouette and one meaningful mark over layers of ornamental
  detail. Use shape, notch, emblem, border, or icon position to communicate state.
- Reuse vanilla blocks, models, particles, and sounds where appropriate. New art
  should earn its maintenance cost by communicating a concept that vanilla reuse
  cannot express.
- Do not create a separate workstation block for every profession or mastery.
  Prefer multipurpose ledgers, depots, terminals, and ordinary blocks with data and
  UI projections. Professions and masteries can be distinguished by overlays, icon
  groups, tooltips, and interaction context.
- Systems must work with alternative resource packs. A replacement pack may change
  every texture and sound, omit an optional model, or use a high-contrast style.
  Server behaviour and item identity must continue to work.
- **Custom textures are a PRESENTATION layer, never gameplay logic.** Code and data
  identify an item through stable components or server state. They never inspect
  pixels, texture filenames, colour values, or a model's visual appearance to decide
  an outcome.
- Keep dimensions, contrast, and interaction states consistent with vanilla so that
  resource-pack authors can replace or restyle an asset without reverse-engineering
  a hidden gameplay contract.

## Tools and equipment

Tools use an ordinary base presentation plus reusable overlays. Do not draw every
combination of wear, broken state, and specialisation as a separate texture.

| State | Visual treatment | Non-visual support |
|---|---|---|
| Ordinary | The clean vanilla-compatible base tool silhouette | Tooltip and state text identify the tool and its current capability |
| Worn | A small reusable wear overlay such as a notch, edge mark, or restrained surface treatment | Exact condition or a readable condition band in the tooltip, plus an icon or text cue |
| Broken | A reusable broken overlay with a clear shape change, not only a red tint | The item remains identifiable, is described as broken, and explains the repair action |
| Specialisation indicator | A compact emblem, inset badge, or overlay layer that can be swapped independently of the base tool | The active path is written in the tooltip or UI and is available to screen-reading or text-based interfaces |

The rendering system should compose the base item and zero or more overlays at
runtime. The asset manifest tracks overlay states as a family so that a new
combination does not require a new hand-drawn asset. Overlays must remain legible
when a resource pack changes the base texture. If an overlay cannot render, the
server still knows the state and the tooltip or Field Guide remains sufficient.

## Villagers and people

Villager presentation should communicate role and state without turning characters
into a row of colour-coded icons.

- Use a profession overlay, badge, tool, apron detail, or interaction icon that can
  be recognised by shape and context.
- Use a rank indicator with a distinct symbol or geometric mark. Do not encode rank
  only as a darker or brighter version of the same colour.
- Support the initial **Mason** presentation as a coherent starting visual while
  leaving room for future professions and player-chosen regional styles.
- Add a restrained regional tint or accent to support place identity, but always pair
  it with a symbol, text label, model element, or interaction cue.
- Do not depend on clothing colour alone. This is required by design principle 16.
  Colour can disappear under a resource pack, display setting, or colour-vision
  difference.
- Keep overlays composable. A profession, rank, regional accent, downed state, or
  work context should not require an illustration for every possible combination.

## UI icon groups

Icons should form a consistent family: simple silhouettes, predictable stroke or
pixel weight, clear grouping, and a text label or tooltip where the meaning is not
obvious. The following groups are planned in the manifest:

| Icon group | What it communicates | Accessibility treatment |
|---|---|---|
| Masteries | Active focus, supporting paths, inactive progress | Shape or emblem differences plus text labels and progression values |
| Perks | A discrete unlocked advantage or modifier | Distinct symbol and tooltip description, never a colour swatch alone |
| Professional ranks | Villager or player career level | Ordered shapes or marks plus rank text |
| Housing, jobs, food, safety, connectivity, storage | Settlement function and infrastructure status | Separate icons, status text, and explicit unavailable or unknown states |
| Building types | Registered function such as home, workplace, depot, or public building | Silhouette plus label and interaction description |
| Professions | Mason and later professional roles | Role emblem or tool silhouette plus name |
| Techniques | Learned methods and teachable knowledge | Technique symbol plus full name and source |
| Obols and stock | Currency balance, physical stock, and available goods | Coin or stock silhouette plus numeric text and unit |
| Orders and shipments | Work requested, packed, in transit, delayed, or delivered | Directional or package shapes plus status text |
| Transport modes | Road, rail, portal depot, or other registered route | Mode-specific silhouette plus route label and availability text |
| Food traits | Traits such as nutrition, preservation, or effect category | Shape or glyph plus written trait and value |
| Regions | Discovered place, resource identity, and route context | Map shape or marker plus name and coordinates or description |
| Seasons and weather | Climate state and forecast context | Symbols plus written state and time, not colour alone |
| Item wear and repair | Condition, broken status, repair, and reforge availability | Notches, badges, explicit tooltip text, and repair action text |
| Projects and discoveries | Active work, completed project, and newly recorded discovery | Progress shape and written state, with a distinct discovery cue |

Avoid a large icon catalogue when one family glyph and a label will do. The Field
Guide and tooltips are part of the presentation system, so the icon never carries
all of the meaning by itself.

## Sound cues

Sound is a confirmation channel, not a hidden gameplay dependency. Reuse vanilla
sounds where they communicate the action well. New or layered cues should be short,
recognisable at normal game volume, and distinct from combat or danger sounds.

Planned cues are:

- journal page;
- contract stamp;
- mastery unlock;
- discovery;
- item breaking;
- repair or reforge;
- sale;
- migrant arrival;
- shipment arrival; and
- project completion.

**All sounds eventually require subtitles.** Subtitles must describe the event in
plain language and must not rely on an audio-only distinction. A missing sound file
or disabled sound channel must never remove the corresponding text, tooltip, or
server outcome.

## Alternative packs and accessibility checks

Before an asset is accepted, review it with the default presentation, a high-
contrast alternative, a pack with missing custom models, and a pack that changes
colours and fonts. Check hotbar scale, inventory scale, first-person use, third-
person use, and screen-reader or text-visible descriptions where applicable.

The following are rejection conditions:

- state is communicated only by hue or clothing colour;
- the mechanic stops working when the custom model is missing;
- a texture filename, model index, or sound event is required for server logic;
- an overlay is unreadable at Minecraft scale;
- every profession or mastery requires a new workstation block;
- a sound has no planned subtitle; or
- final art exists without an approved manifest row and gameplay need.

The manifest is sorted by category and then `asset_id`. Keep that ordering when
adding rows so reviews and generated comparisons remain deterministic. Every row
currently has status `planned`. No PNG, model, or audio file is created by this
planning document.

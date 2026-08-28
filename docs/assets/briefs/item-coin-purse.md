# Art Brief: Coin Purse (item, 16x16)

**Asset ID:** `item_coin_purse`
**Category:** item
**System:** economy
**Status:** Brief only. **No art is to be produced from this document yet.**
Production requires an approved manifest row and a demonstrated gameplay need
(AI rule 17, `docs/assets/art-direction.md`).

**Governing documents:** ADR 0008 (hybrid Obol currency), `docs/systems/economy.md`,
`docs/design-principles.md` principles 12, 15, 16, and 17,
`docs/assets/art-direction.md`.

---

## 1. Player-facing purpose

The Coin Purse is the container the player carries so that ordinary money does not
consume ordinary inventory space. It holds a **balance**, not a stack of items.

Under ADR 0008, Obols exist in two linked representations. Physical Obols are
tangible, lootable, giftable objects. The Coin Purse is the compact balance used for
routine exchange. The player withdraws physical Obols from the purse and deposits
valid ones back, each conversion being an atomic server-authoritative transaction.

The icon therefore has to communicate three things at a glance:

1. **This is money.** A player who has never opened it should guess its function.
2. **This is a store of money, not the money itself.** It must not be mistaken for
   a coin, a pile of coins, or a lootable treasure item.
3. **Roughly how full it is**, as a secondary reading, without the player being
   able to mistake the picture for the authoritative figure. The exact balance is
   always text (see section 7).

The purse is a **convenience**, never a gate. It does not make normal play worse in
order to justify itself (principle 17). Art should read as a practical everyday
possession, closer to a belt pouch than to a treasure hoard or a banker's strongbox.

## 2. Required silhouette

The silhouette is the primary carrier of meaning, because it survives resource-pack
replacement, colour-blindness, and small-scale rendering.

**Required:** a **soft-bodied drawstring pouch**. Wider at the base than at the neck,
with a visibly gathered or cinched throat and a closure element (tie, cord, or knot)
at the top. The bottom edge should be rounded and should suggest that the contents,
not a rigid frame, define the shape.

Requirements:

- Readable as a **single closed shape** at 16x16 with no zoom.
- Distinct in outline from vanilla and planned Forever items at a glance. It must not
  be confusable with a vanilla bundle, a pouch-like vanilla item, an ordinary sack, a
  bag of any generic kind, a chest, or a barrel.
- The **neck-and-cinch** is the identifying feature. If the cinch is removed, the
  silhouette should stop reading as a purse. Do not let the cinch become so small that
  it disappears at scale.
- Asymmetry is preferred over perfect bilateral symmetry so the item does not read as
  a UI glyph.
- The shape must remain recognisable when rendered in a single flat colour, because
  that is the worst realistic case (high-contrast packs, silhouette-only renders, and
  accessibility filters).

**Fill states change the body profile, not the object.** A fuller purse is rounder and
sits lower. An empty purse is slacker and flatter. The neck and closure stay in the
same place in all states so the item stays identifiable.

## 3. Material hierarchy

Reading order, from most to least visually dominant:

| Rank | Element | Treatment |
|---|---|---|
| 1 | Pouch body: soft worked leather or heavy cloth | Largest area. Carries the base tone and the fill-state profile. Matte, low texture detail, mostly flat with restrained shading. |
| 2 | Drawstring and cinch | Fine cord in a contrasting **value**, not merely a contrasting hue. Defines the neck. Should read at 1px width but must not vanish. |
| 3 | Suggested contents | Implied by body bulge, not drawn as individual coins. At most a **single** partially visible Obol edge in open states only. |
| 4 | Wear and use marks | Minimal. A crease, a seam, or a slight base darkening. Signals a used everyday object. Must never be confused with the tool wear overlay vocabulary. |
| 5 | Metal fittings | **Optional and discouraged.** If present, at most a small clasp or ring, and never large enough to compete with the cinch. |

Rationale: the purse should read as **worn, practical, and personal**. Forever's
economy is about exchange and stewardship, not wealth display. A jewelled or
gold-clasped purse would misrepresent the system's tone and would compete visually
with the Obol itself.

## 4. Maximum suggested colour count

**Maximum 6 colours, plus full transparency.** Target 4 to 5.

Suggested budget:

- 3 values for the pouch body (base, shadow, highlight)
- 1 to 2 values for the drawstring and cinch
- 1 accent for the visible Obol edge, used **only** in open states

Constraints:

- Stay within a vanilla-adjacent palette. No saturated fantasy hues.
- Neighbouring elements must differ in **value**, not only in hue, so the icon survives
  desaturation.
- No gradients, no dithering for its own sake, no anti-aliased edges against
  transparency.
- No outline colour that is pure black at full opacity unless local vanilla convention
  in the surrounding icon set already does so.

## 5. Visual relationship to the Obol

This relationship is the most important constraint in this brief, because ADR 0008
makes the purse and the Obol two representations of one concept. The art must express
"same currency, different form" without letting the two become interchangeable.

**Shared:**

- The Obol's accent colour appears in the purse **only** as the visible contents in
  open or full states. This is the single visual link between them.
- Both belong to the same restrained, non-ostentatious material language.

**Deliberately different:**

| | Obol | Coin Purse |
|---|---|---|
| Silhouette | Hard-edged, geometric, coin-like | Soft-bodied, cinched, irregular |
| Dominant material | Metal | Leather or cloth |
| Reading | The value itself | The container of value |
| Scale cue | Small object | Object that holds small objects |

**Hard rules:**

- The purse must **never** be mistaken for a large or stacked Obol. If a player could
  read the icon as "a big coin", it is wrong.
- The purse must not display a countable number of coins. Depicting three coins invites
  the reading that it contains three, which contradicts a balance-based container and
  breaks principle 15 (the mechanic must be understandable, and the picture must not
  lie about it).
- At most **one** partial Obol edge may be visible, and only in open or full states.
- Do not stamp the Obol's face motif onto the purse body. That would read as branding
  and would blur the two items.

The Obol's own brief is a separate, not-yet-written document. If the Obol design is
finalised first, the purse's accent colour must be taken from it rather than chosen
independently.

## 6. Accessibility requirements

Per principle 16, **no state may be encoded by colour alone.** Every requirement below
is mandatory, not advisory.

- **Shape carries state.** Fill level is expressed through body profile and cinch
  tension. A player who cannot distinguish the colours must still read the states from
  outline alone.
- **Value separation.** All adjacent elements must remain distinguishable when the
  texture is converted to greyscale. This is a checkable acceptance test.
- **Broken and unusable states** need a shape change, not a red tint.
- **Text is authoritative.** The exact balance, the unit name, and the state appear as
  text in the tooltip or screen. The icon is a supporting cue and is never the only
  source of the number. This matches the existing manifest row, which requires numeric
  balance and state text alongside the icon.
- **Resource-pack independence.** If a pack replaces or omits the texture, the item
  must remain identifiable and usable through its name, tooltip, and Field Guide entry.
- **No reliance on hover.** State must be available without requiring the player to
  hover, since that excludes some input methods.
- **Field Guide entry required** before the mechanic ships (principle 15).

## 7. Forbidden details

Do not include:

- **A currency symbol, numeral, or written amount baked into the texture.** The balance
  is dynamic; a painted number would be wrong the moment it changed.
- **A countable set of coins.** See section 5.
- **Spilling, overflowing, or scattered coins.** This implies loss, which is not the
  mechanic.
- **Sparkles, glints, shine bursts, glow, or any magical treatment.** The purse is
  mundane. Forever's currency is not magical, and glow would imply an enchantment layer
  the item does not have.
- **Gold-hoard signalling:** gemstones, jewels, ornate clasps, embossed crests, heraldry,
  or luxury ornament. Wrong tone for a stewardship economy.
- **Faction, kingdom, guild, or settlement insignia.** Forever does not enforce a theme
  or style (principle 7), and a crest would imply an affiliation system that does not
  exist.
- **Any depiction of a nested container** (a bag inside the bag, a pocket holding
  another pouch). Container nesting is explicitly prohibited by ADR 0011, and the art
  must not suggest a capability the game refuses to provide.
- **Straps, belts, or worn-on-body context.** The icon is the object, not the player
  wearing it.
- **Detail below 1px, high-frequency noise, or texture stippling** that turns to mush at
  16x16.
- **Perspective or lighting inconsistent with the surrounding vanilla item set.**
- **Colour-only state differences** of any kind.
- **A separate hand-drawn texture per state combination.** Use the composable approach
  in section 8.

## 8. Required texture states

The manifest row lists: `closed; open; empty; contains_balance; full; broken`.

These are **not six unrelated illustrations.** Following the overlay policy in
`docs/assets/art-direction.md`, they compose from a small base set plus reusable
overlays, so that a new combination does not require new hand-drawn art.

**Base shapes (drawn):**

| State | Requirement |
|---|---|
| `empty` | Slack, flatter body. Neck loose but still cinched. Clearly the same object, clearly holding nothing. No contents visible. |
| `contains_balance` | Default carrying state. Moderate rounded bulge. Neck cinched. No individual coins visible. This is the icon players see most often. |
| `full` | Maximum rounded profile, sitting lower and wider. Taut cinch. May show at most one partial Obol edge at the neck. Must not read as "about to burst" or as spilling. |

**Closure overlays (composable):**

| State | Requirement |
|---|---|
| `closed` | Drawstring drawn tight, neck gathered shut. Default presentation. |
| `open` | Neck relaxed and parted, drawstring visibly loosened. The only state permitted to reveal contents, and then only a single partial Obol edge. Shape change must be legible at 16x16 without colour. |

**Condition overlay:**

| State | Requirement |
|---|---|
| `broken` | Uses the shared reusable broken-item overlay vocabulary: a clear **shape change**, such as a torn seam or a parted base, not a red tint. The item stays identifiable as a Coin Purse and the tooltip explains the repair action. Consistent with principle 5, the purse is not deleted, and with ADR 0006's repair model. |

**Composition rules:**

- Closure overlays must render correctly over every base fill shape.
- Overlays must stay legible when a resource pack replaces the base texture.
- If an overlay fails to render, the server still knows the state and the tooltip and
  Field Guide remain sufficient.
- The neck and closure must occupy the same pixel region across all bases so overlays
  align without per-combination adjustment.

**Deliverable when production is eventually approved:** 3 base textures, 2 closure
overlays, and 1 shared broken overlay reused from the existing overlay family. That is
5 new files rather than 6 or more combinatorial variants.

---

## Acceptance checks

Before any produced asset is accepted:

1. Readable as a drawstring purse at 16x16 with no zoom.
2. Distinguishable from every vanilla pouch-like or bag-like item in outline alone.
3. Not confusable with a single or stacked Obol.
4. Six or fewer colours.
5. All states distinguishable in greyscale.
6. All states distinguishable in outline alone.
7. No baked numerals, symbols, glow, or ornament.
8. No implication of container nesting.
9. Overlay alignment holds across all three base shapes.
10. Manifest row updated, and a Field Guide entry plus tooltip text exist.

# Asset review after the modpack pivot

The asset manifest was written when Many Roads Home was going to be a custom mod, so it
assumed the project would draw art for every mechanic. The modpack pivot changes that.
The resource-pack policy is now to reuse third-party mod art wherever the mod supplies
the mechanic, and to reserve pack-owned art for branding, the Field Journal, compatibility
fixes, consistent icons, Matcha integration, status overlays, and pack-specific documents.

This review records which planned assets remain necessary. The per-asset result is in
`pivot-asset-review.csv`.

## Why this is a separate file

`asset-manifest.csv` has a strict ten-column header that `./gradlew validateAssets`
enforces, and the validator treats any other header as a usage error. Adding a review
column would have meant weakening that check, which exists to stop the manifest drifting
into an unvalidated free-form spreadsheet. Keeping the review beside the manifest costs a
join but preserves the gate.

## Categories

| Result | Count | Meaning |
|---|---:|---|
| `PACK_OWNED` | 24 | Connective or identity system with no maintained third-party owner in `docs/architecture/ownership-matrix.csv`. Art stays a pack responsibility. |
| `THIRD_PARTY_ART` | 10 | A selected mod supplies the mechanic and ships its own art. Reuse it. |
| `RESOLVED_BY_LAB` | 8 | Ownership depends on which candidate a compatibility spike selects. |

## What this means in practice

**Do not commission the ten `THIRD_PARTY_ART` assets.** Drawing a depot block, a route
marker, or a Traveler's Cache icon when the chosen mod already ships one is duplicated
work, and it fights the mod on every update because a resource-pack override has to track
the mod's model and texture paths.

**Do not commission the eight `RESOLVED_BY_LAB` assets yet.** Mastery icons, tool wear
overlays, and repair sounds all depend on whether LAB-08 and LAB-09 adopt an existing
skills or smithing framework. If they do, that framework supplies the presentation and
the pack supplies only the rules. Commissioning first would waste the work and create
pressure to keep art that no longer matches the design.

**The 24 `PACK_OWNED` entries are genuinely ours**, because the ownership matrix has no
third-party owner for settlements, careers, the economy, discoveries, world history, or
the unified knowledge interface. Even these remain gated: AI rule 17 and the art direction
still require an approved manifest row *and* an approved gameplay need before any final
asset is produced. Nothing here authorises drawing anything today.

## Standing constraints, unchanged by the pivot

- No final artwork without an approved manifest row and an approved gameplay need.
- Never download, extract, copy, or trace vanilla Minecraft textures.
- Never make a state colour-only. Pair colour with shape, text, or an icon cue.
- Art is presentation, never gameplay logic. Code must not inspect pixels or filenames.
- Do not create a custom workstation block where an existing block expresses the function.

## Next review

Re-run this review after LAB-01 through LAB-12 report. Each lab that adopts a mod should
move its related assets from `PACK_OWNED` or `RESOLVED_BY_LAB` to `THIRD_PARTY_ART`, and
a lab that rejects every candidate moves them the other way.

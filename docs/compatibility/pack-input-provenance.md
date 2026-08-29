# Pack input provenance

Every input pinned in `pack/`, with the exact identity, source, licence, compatibility
status and removal note that PACK-01 acceptance criterion 2 requires.

**Generated from verified data, not from memory.** Every field below was read from the
Modrinth API on 2026-08-29 and every source URL was requested to confirm it resolves. Do
not edit this by hand without re-checking; regenerate the facts instead.

These are licence *facts*. They do not decide the project's own rights model, which is an
open question in `docs/decisions/OPEN-rights-model.md`. They are what that decision needs.

## Summary of licensing risk

| Risk | Inputs |
|---|---|
| **Blocks redistribution without permission** | Global Packs (All Rights Reserved, and its declared source URL returns 404) |
| **NonCommercial: incompatible with paid or monetised distribution** | Matcha Flavoured, Jade |
| Copyleft, matters only if code is linked or modified | Architectury, Cloth Config, Lithium (LGPL-3.0) |
| Permissive | Fabric API (Apache-2.0), FerriteCore, REI (MIT) |

No input is redistributed as a binary: the pack references every file by URL and hash,
verified by `scripts/verify-pack-downloads.py`. That keeps the source repository clear of
third-party binaries, but it does **not** by itself grant permission to publish a pack
that downloads them. See `labs/results/LAB-LICENCE-publication-readiness.md`.

## Inputs

### Matcha Flavoured

| Field | Value |
|---|---|
| Modrinth project | `QI0EmgZ1` (`matcha-flavoured`) |
| Pinned version | `E9rngRfK` |
| File | `Matcha_Flavoured_1_12.zip` |
| Release type | `release` |
| Licence | `CC-BY-NC-SA-4.0` — NonCommercial + ShareAlike |
| Source | **none declared** |
| Sides | client `optional`, server `required` |
| Role | Gameplay foundation. Loaded in BOTH roles (resource pack and datapack) from one archive via Global Packs. |
| Removal | **Removing it changes the world.** It supplies the 2346-recipe / 1805-advancement baseline; without it a server starts with 1585 recipes and no diagnostic. MRH-010 exists to make that visible. Never remove from a played world. |

### Architectury API

| Field | Value |
|---|---|
| Modrinth project | `lhGA9TYQ` (`architectury-api`) |
| Pinned version | `1yQC4VvP` |
| File | `architectury-fabric-21.0.7.jar` |
| Release type | `release` |
| Licence | `LGPL-3.0-only` — copyleft (library) |
| Source | https://github.com/architectury/architectury |
| Sides | client `required`, server `required` |
| Role | Required library for REI. |
| Removal | Removing it breaks REI. No world data of its own. |

### Cloth Config API

| Field | Value |
|---|---|
| Modrinth project | `9s6osm5g` (`cloth-config`) |
| Pinned version | `Nv3xnWXd` |
| File | `cloth-config-26.2.155.jar` |
| Release type | `release` |
| Licence | `LGPL-3.0-only` — copyleft (library) |
| Source | https://github.com/shedaniel/ClothConfig/ |
| Sides | client `optional`, server `optional` |
| Role | Required library for REI. |
| Removal | Removing it breaks REI configuration screens. No world data of its own. |

### Fabric API

| Field | Value |
|---|---|
| Modrinth project | `P7dR8mSH` (`fabric-api`) |
| Pinned version | `NqwNSxwA` |
| File | `fabric-api-0.158.0+26.2.jar` |
| Release type | `release` |
| Licence | `Apache-2.0` — permissive |
| Source | https://github.com/FabricMC/fabric |
| Sides | client `optional`, server `optional` |
| Role | Required by the loader ecosystem and the companion mod. |
| Removal | Removing it breaks nearly every mod. No world data of its own. |

### FerriteCore

| Field | Value |
|---|---|
| Modrinth project | `uXXizFIs` (`ferrite-core`) |
| Pinned version | `d5ddUdiB` |
| File | `ferritecore-9.0.0-fabric.jar` |
| Release type | `release` |
| Licence | `MIT` — permissive |
| Source | https://github.com/malte0811/FerriteCore |
| Sides | client `optional`, server `optional` |
| Role | Memory-usage optimisation. Adopted by LAB-10. |
| Removal | Safe to remove: it changes memory layout, not saved data. |

### Global Packs

| Field | Value |
|---|---|
| Modrinth project | `NRLPy2mk` (`globalpacks`) |
| Pinned version | `DqrPrUMp` |
| File | `globalpacks-fabric-26.2-26.2.0.jar` |
| Release type | `release` |
| Licence | `LicenseRef-All-Rights-Reserved` — **All rights reserved** |
| Source | https://github.com/DarkRoleplay/Global-Data-and-Resourcepacks (**404 as of 2026-08-29**) |
| Sides | client `optional`, server `optional` |
| Role | Loads the Matcha archive in both roles. The only utility with a verified 26.2 Fabric release. |
| Removal | Removing it silently stops Matcha loading, which is the fail-open path LAB-02 measured and MRH-010 reports. **Release risk:** All-Rights-Reserved and its declared source URL 404s, so redistribution needs permission or a replacement. |

### Jade 🔍

| Field | Value |
|---|---|
| Modrinth project | `nvQzSEkH` (`jade`) |
| Pinned version | `ue8CO97w` |
| File | `Jade-mc26.2-Fabric-26.2.11.jar` |
| Release type | `release` |
| Licence | `CC-BY-NC-SA-4.0` — NonCommercial + ShareAlike |
| Source | https://github.com/Snownee/Jade |
| Sides | client `optional`, server `optional` |
| Role | Contextual block/entity information HUD. Adopted by LAB-01. |
| Removal | Safe to remove: it is a client HUD and owns no world data. **NonCommercial licence.** |

### Lithium

| Field | Value |
|---|---|
| Modrinth project | `gvQqBUqZ` (`lithium`) |
| Pinned version | `f7vZ0VWU` |
| File | `lithium-fabric-0.25.3+mc26.2.jar` |
| Release type | `release` |
| Licence | `LGPL-3.0-only` — copyleft (library) |
| Source | https://github.com/caffeinemc/lithium-fabric |
| Sides | client `optional`, server `optional` |
| Role | Server-side tick optimisation. Adopted by LAB-10. Verified to preserve the recipe/advancement baseline. |
| Removal | Safe to remove: it optimises existing behaviour and adds no content or saved data. |

### Roughly Enough Items (REI)

| Field | Value |
|---|---|
| Modrinth project | `nfn13YXA` (`rei`) |
| Pinned version | `4o0NSIMj` |
| File | `RoughlyEnoughItems-26.2.820.jar` |
| Release type | `release` |
| Licence | `MIT` — permissive |
| Source | https://github.com/shedaniel/RoughlyEnoughItems |
| Sides | client `optional`, server `optional` |
| Role | Recipe viewer. Adopted by LAB-01 over JEI, which was beta-only and a second viewer. |
| Removal | Safe to remove: a client-side viewer that owns no world data. Removing it hides recipe lookup, which principle 2 requires stay available. |

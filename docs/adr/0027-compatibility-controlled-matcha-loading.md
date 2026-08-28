# ADR 0027: Load official Matcha through a compatibility-controlled pack utility

- Date: 2026-08-28
- Related principles/ADRs: [Principles 1, 2, 13, 14, 15, 17, and 18](../design-principles.md), [ADR 0001](0001-use-official-matcha-as-starting-pack.md), [ADR 0002](0002-isolate-matcha-behind-adapter.md), [ADR 0020](0020-private-project-license-separation.md), [ADR 0024](0024-modpack-first-product-ownership.md), [ADR 0025](0025-packwiz-source-of-truth.md)

## Status

Accepted. This is the initial Matcha loading decision, subject to the stated licence and compatibility gates.

## Context

Matcha Flavoured 1.12 is the selected official starting substrate under [ADR 0001](0001-use-official-matcha-as-starting-pack.md). The verified release is Modrinth project `QI0EmgZ1`, version ID `E9rngRfK`, with archive URL [Matcha_Flavoured_1_12.zip](https://cdn.modrinth.com/data/QI0EmgZ1/versions/E9rngRfK/Matcha_Flavoured_1_12.zip), SHA-1 `77d080d2fe207a886c8c784ac239dec54a213065`, and licence CC-BY-NC-SA-4.0. It is not a Fabric mod. It ships as one archive that is both a datapack and a resource pack. Loading only one side would produce a partial or misleading baseline, while extracting or rewriting the archive could break the relationship between its data and presentation and complicate provenance.

A modpack also needs an explicit runtime loader. Among the three named candidates checked for this baseline, Global Packs has a real Fabric release for Minecraft 26.2: version `26.2.0`, ID `DqrPrUMp`, file `globalpacks-fabric-26.2-26.2.0.jar`, with project evidence at [its Modrinth project](https://api.modrinth.com/v2/project/NRLPy2mk). Its configuration file is `global_packs.toml`, it distinguishes builtin data packs from resource packs, and its documented load order makes the first configured entry win. Paxi's latest supported game version stops at 26.1.2 according to [its Modrinth project](https://api.modrinth.com/v2/project/CU0PAyzb). OpenLoader's latest supported game version stops at 1.21.1 according to [its Modrinth project](https://api.modrinth.com/v2/project/KwWsINvD). Global Packs is currently the only named candidate with a real Fabric 26.2 release.

Global Packs is marked LicenseRef-All-Rights-Reserved. That is a genuine consideration for a redistributed pack, not a detail to hide. Private development use does not automatically grant redistribution permission, and the loader's licence may become a release blocker even if its technical behaviour is suitable. The pack must not make a public distribution promise until that licence and the licences of the Matcha archive and every other input have been reviewed.

## Decision

Many Roads Home will load the exact official Matcha 1.12 archive through a compatibility-controlled pack utility. Global Packs `26.2.0` is the current named candidate for that utility because it is the only one of the three checked candidates with a real Fabric 26.2 release. Packwiz owns the selected loader input and its configuration. The Matcha compatibility work owns validation of the expected archive identity, the data/resource dual role, load order, and the diagnostic shown when the baseline is missing or structurally incompatible.

The single Matcha archive remains one pinned input. The loader configuration must make the same archive available in both the datapack and resource-pack roles without editing, extracting, or silently replacing it. If the selected utility cannot load the archive in both roles without repackaging it, the compatibility spike must record a blocker and evaluate another utility. It must not create a private split archive merely to make the configuration appear to work.

The compatibility-controlled utility must verify, before a supported world is used, that the archive identity matches the lock record and that both roles are active in the intended order. An absent, altered, or unsupported archive produces an actionable diagnostic and must not be interpreted as a valid partial Matcha state. Ordinary Minecraft remains usable in an instance that does not enable the Matcha profile, but a declared Many Roads Home profile must not silently claim to be complete when its required baseline is absent.

All Matcha identifiers and behaviour mappings remain inside `dev.forever.compat.matcha` and clearly identified compatibility resources under [ADR 0002](0002-isolate-matcha-behind-adapter.md). The loader is a pack boundary, not permission for core gameplay code to read private paths. The exact Global Packs licence and redistribution terms are a release gate. If permission is unavailable or the terms are incompatible with the intended distribution, a later decision must replace the utility before release rather than bypass the licence.

## Consequences

- The official starting substrate is reproduced as the same data and resource archive that was audited, reducing the risk of a presentation-only or data-only baseline.
- Packwiz, the loader configuration, the Matcha lock record, and the adapter diagnostics form a visible chain from source input to running instance.
- The current technical choice depends on Global Packs and its 26.2 compatibility. A future loader change may require configuration migration and a fresh disposable-world validation.
- Global Packs' All-Rights-Reserved status is a real distribution risk. It may prevent a public pack release or require replacing the utility even if no technical defect is found.
- A single archive serving two roles increases load-order and failure-mode complexity. A resource-only test is not enough, and a data-only test can produce false confidence.
- Failing closed on an altered archive protects the audit and save boundary but can make a developer instance refuse to start until the lock and configuration are corrected.
- Keeping the archive separate preserves attribution and share-alike analysis, but it makes packaging and release notices more involved than copying its contents into an ordinary Forever resource tree.

## Rejected alternatives

### Load Matcha only as a datapack

This would omit its resource-pack role and could change item presentation, recipe guidance, or identity assumptions. It would not reproduce the verified starting substrate.

### Load Matcha only as a resource pack

A resource pack cannot provide the gameplay data that the selected baseline requires. This would produce a visually convincing but mechanically incomplete instance.

### Extract and repackage the archive

Repackaging could make a loader configuration easier, but it would change the pinned input, risk breaking relative data and resource paths, and complicate CC-BY-NC-SA-4.0 provenance. The official archive remains the authoritative input.

### Use Paxi or OpenLoader for the first 26.2 profile

The checked Paxi release data stops at 26.1.2 and OpenLoader stops at 1.21.1. They are not viable for this pinned 26.2 baseline without new authoritative compatibility evidence.

### Write a custom global pack loader

A custom loader would create a large compatibility surface for a problem an existing 26.2 utility may solve. It would also make the companion responsible for pack lifecycle and increase the risk of partial loading.

### Hide the Global Packs licence risk

Omitting the risk would make a technically successful private test look like a distributable product. The licence is part of the decision and must remain visible to the release owner.

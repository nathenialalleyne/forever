# ADR 0023: Use Many Roads Home as the working product title

- Date: 2026-08-28
- Related principles/ADRs: [Vision](../vision.md), [Principles 1, 6, 8, 9, 12, 14, 15, 17, and 18](../design-principles.md), [ADR 0020](0020-private-project-license-separation.md), [ADR 0024](0024-modpack-first-product-ownership.md)

## Status

Accepted. This is a working identity decision and a migration gate for the first persistent release.

## Context

The product direction has changed. The project is now a modpack-first experience whose identity is carried by a curated world, compatible third-party content, and a small companion integration mod. The old name, Forever, communicates that a world should last. It does not communicate the two parts of the new promise: players can reach a good world by different routes, and the world itself is worth returning to. The title must describe the destination as well as the plurality of play styles.

The repository currently uses mod id `forever`, package `dev.forever`, and persisted identifiers created during the prototype and foundation work. These names are present in registry IDs, SavedData keys, component types, translation keys, resource paths, source packages, scripts, and documentation. A rename is not a cosmetic search and replace once a persistent world has been released. It can require aliases, datafixes, conversion of saved records, resource compatibility, and a long period in which both vocabularies must be understood.

At the same time, retaining Forever indefinitely has a cost. It would make the public pack name, the companion mod name, and the repository's internal vocabulary look like one product when they are not. A later migration after the first persistent release would be more expensive because old saves would contain the old identifiers. Internal package names are comparatively cheap to change because they are not themselves saved world identifiers, although a broad package move still has review and merge cost.

The likely future identity is therefore split deliberately. The pack display name is Many Roads Home, the pack slug is `many-roads-home`, the companion mod display name is Many Roads Integration, and the companion mod ID is `manyroads`. This record must not silently apply that rename to code or resources.

## Decision

Many Roads Home is the working product title for the modpack-first pivot. Its working tagline is:

> Many ways to play. One world worth keeping.

The working names are:

| Surface | Working name | Role |
|---|---|---|
| Pack display name | Many Roads Home | The curated modpack and player-facing product |
| Pack slug | `many-roads-home` | The distribution and catalogue identity |
| Companion mod display name | Many Roads Integration | The small optional or pack-selected integration layer |
| Companion mod ID | `manyroads` | The future loader identity of that companion mod |

The current internal identifiers remain unchanged during this pivot. No Java package, mod ID, registry ID, SavedData key, component type, translation key, or resource path is renamed by this ADR. Documentation may use Many Roads Home for the product and Forever when referring to existing internal identifiers or historical prototype material.

The recommendation is to retain the current `forever` internal identifiers temporarily during the pivot, then migrate persisted and player-facing identifiers before the first persistent release, or explicitly approve a compatibility plan that keeps them. Migrating before that release is safer while there is no released save population to protect. The migration requires its own ticket, an inventory of every persisted identifier, aliases or datafixes where required, a disposable-world conversion test, and a release review. Internal package names may be migrated in the same change or later because their save cost is low, but persisted identifiers must not be postponed casually.

The pack name and companion mod name are distinct. The pack owns composition and presentation. The companion owns only the narrow integration capabilities accepted through [ADR 0033](0033-documented-gap-analysis-for-companion-code.md). Neither name authorises copying third-party material into the other ownership boundary.

## Consequences

- The public product can describe both route diversity and a durable destination without claiming that every feature belongs in a custom mod.
- The temporary use of `forever` avoids an unreviewed mass rename while the pack composition and compatibility surface are still changing.
- A pre-release identifier migration remains an explicit work item rather than an assumption hidden in a future refactor.
- The identity split makes licensing, support, pack distribution, and companion-mod troubleshooting easier to describe.
- Keeping two vocabularies during the pivot creates documentation friction. Agents must distinguish the working product title from old source and save identifiers.
- Migrating before the first persistent release will touch resource paths, translations, registries, tests, scripts, and possibly generated data. A mistake could make a disposable world unreadable, so the migration cannot be treated as a branding-only task.
- Keeping `forever` permanently would avoid migration work but would leave a long-term public identity mismatch and make the eventual companion boundary less clear. This ADR does not choose that option.
- The working title is not clearance to publish it. A final availability and trademark review is required before public release.

## Rejected alternatives

### Rename every identifier immediately

An immediate rename would make the tree look coherent, but it would mix brand work with the pack pivot and obscure whether a failure came from composition, compatibility, or identifier conversion. It would also change identifiers before the project has completed its inventory of prototype persistence. The migration is safer as a separate release-gated ticket.

### Keep Forever as the permanent public pack name

Forever is a strong description of world longevity, but it names only the duration. It does not name the multiple routes through the experience or the home that those routes build. Keeping it would preserve short-term continuity at the cost of the product distinction this pivot needs.

### Rename only the display strings and keep all public IDs forever

This minimises immediate breakage, but it creates a permanent split between what players download, what support documents call it, and what loaders and saved data call it. It may be a compatibility bridge during development, not the recommended identity for a first persistent release.

### Call the companion mod Many Roads Home

The companion is not the whole product. Giving it the pack name would encourage future features to move back into a monolithic custom mod and would make it harder to explain which behaviour comes from the curated pack and which comes from integration code. Many Roads Integration keeps that boundary visible.

# Many Roads Home: name and identity

## Status

Working identity. This document records the name used during the modpack-first pivot. It does not rename the repository, Java packages, mod ID, or persisted identifiers. See [ADR 0023](../adr/0023-many-roads-home-working-title.md).

## Working name

**Many Roads Home** is the working display name for the modpack. Its tagline is:

> Many ways to play. One world worth keeping.

The name fits the product because the pack is designed around different valid routes through a long-lived world. A player may build, gather, cook, trade, explore, care for animals, develop a settlement, operate transport, or combine those activities. Those routes are not separate campaigns. They meet in the same world, where infrastructure and knowledge make repeated life easier. “Home” names the destination that gives the routes meaning. It is a place that accumulates function, memory, people, and history rather than a temporary staging area before the next progression tier.

The previous name, **Forever**, named longevity only. That remains an important promise, but it does not describe the multiple routes by which a player inhabits the world or the destination those routes create. **Many Roads Home** is broader because it names both the multiple routes and the destination. It does not require every player to use the same route, and it does not imply that the product is only a durability or save-preservation project.

## Intended tone

The intended tone is grounded, welcoming, practical, and quietly reflective. It should feel like an invitation to make a place useful and personal, not like a promise of a heroic power curve. Text should respect players who build slowly, prefer technical automation, play alone, join a server, or change specialisation. It should describe choices and consequences plainly, without pretending that a configuration option is a story or that a reward is mandatory when it is not.

The tone should avoid:

- grand claims that the pack will suit every player or solve every Minecraft frustration,
- language that presents one building style, profession, or travel method as the correct one,
- urgency, grind, or fear of missing out as a substitute for a meaningful choice, and
- unexplained fantasy terminology where a direct description would help a player act.

The tagline is concise, but it is not a marketing promise that overrides the design principles. In particular, the pack must still preserve ordinary Minecraft building, solo viability, readable instructions, and safe world saves.

## Names explicitly rejected

### Forever

Rejected as the public pack name for the pivot. It names the desired longevity of a world, but not the different ways of playing or the home that those ways build. It remains an internal repository and historical name until the identifier review in ADR 0023 is completed.

### Forever Renewed

Rejected because it still makes longevity the only named idea and suggests a sequel or content refresh rather than a world with several legitimate routes. It would also blur the boundary between the prototype project and the new pack.

### One True Path

Rejected because it contradicts the product's purpose. The pack should make alternatives competitive, not announce a preferred route.

### The Long Road

Rejected because it emphasises delay and endurance rather than the possibility of several routes. It risks framing convenience as something withheld instead of something built.

### Homestead

Rejected as too narrow. It suggests a rural or domestic style and does not cover transport, regional trade, exploration, industrial play, or a shared server world.

### Worldbound

Rejected because it describes attachment to a world but not the many forms of work, travel, and settlement that give the world identity. It also has a more restrictive tone than intended.

### Forever Integration

Rejected as the companion name. The companion is a supporting integration layer, not the pack itself. Calling it by the product name would encourage the custom mod to become the centre of ownership again.

## Product and companion distinction

The names refer to different things:

| Name | What it identifies | What it owns |
|---|---|---|
| Many Roads Home | The curated modpack | Selected mods, configuration, datapacks, resource packs, optional blueprints, onboarding, compatibility claims, and release composition |
| Many Roads Integration | The companion mod | Narrow connective capabilities that pass the existing-mod escalation and documented gap-analysis gates |
| `forever` and `dev.forever` | Current internal identifiers | Existing repository paths and source compatibility during the pivot |
| `many-roads-home` and `manyroads` | Likely future public identifiers | Pack slug and companion mod ID, subject to the migration decision and release review |

The pack may be playable with the companion absent when the selected profile has a safe fallback. The companion must not become a hidden dependency for ordinary Minecraft building, food, shelter, inventory use, or basic survival. If a future profile does require a companion capability, the dependency and its removal behaviour must be declared in the pack metadata.

## Availability and trademark review

The working name is not a clearance decision. Before a public repository, public pack listing, distribution, or release uses Many Roads Home, the project must complete a final availability and trademark review in the intended jurisdictions and platforms. The review should cover the pack name, slug, companion name, logos, and likely search or distribution conflicts. A conflict must result in a new naming decision before public release, not a silent change to a published identity.

Until that review is complete, documentation should call Many Roads Home the working title and should not imply that the name is reserved, registered, or available for exclusive use. The review is separate from the technical identifier migration. A technically safe rename is not evidence that the name is legally or commercially available.

# ADR 0020: Keep private Forever code separate from Matcha and third-party material

- Date: 2026-08-28
- Related principles/ADRs: [Principles 13, 14, 15, and 18](../design-principles.md), [ADR 0001](0001-use-official-matcha-as-starting-pack.md), [ADR 0002](0002-isolate-matcha-behind-adapter.md), [ADR 0003](0003-classify-matcha-mechanics-individually.md), [ADR 0014](0014-climate-abstraction-for-seasons.md)

## Status

Accepted. This is a locked project and licensing decision.

## Context

Forever is a private project for now. Original Forever code is All rights reserved under
`LicenseRef-Forever-Proprietary`. The starting gameplay pack is Matcha Flavoured 1.12,
whose verified archive is licensed CC-BY-NC-SA-4.0. Other optional integrations, resource
packs, libraries, textures, sounds, and generated assets may have their own terms. Keeping
those boundaries clear lets Forever pursue alternatives to the anti-AFK-farm checklist
without making third-party material part of an unreviewed proprietary release. Principles
13 and 18 make provenance and safe change project constraints.

Those terms are not interchangeable. CC-BY-NC-SA-4.0 carries attribution, non-commercial,
and share-alike obligations. Treating Matcha-derived material as if it were proprietary
Forever code could create an unlawful or impossible distribution boundary. Treating all
Forever work as if it inherited Matcha's licence would also make the private project's
ownership and future release choices unclear.

The technical architecture has the same boundary. Matcha is adopted as an official,
pinned starting point under [ADR 0001](0001-use-official-matcha-as-starting-pack.md), and
its private identifiers are isolated under [ADR 0002](0002-isolate-matcha-behind-adapter.md).
That separation should be reflected in source, resources, generated outputs, notices, and
agent workflow rather than existing only as a legal comment.

The project needs to be able to audit, replace, or eventually fork a mechanic. Provenance
must remain visible while [ADR 0003](0003-classify-matcha-mechanics-individually.md)
records what is retained or changed. A future public release, if one is chosen, will need
a deliberate licensing review rather than an assumption that private development makes
all obligations disappear.

## Decision

Forever-created source and assets remain private and proprietary unless a later decision
licenses them otherwise. Matcha material remains identifiable as third-party material
under CC-BY-NC-SA-4.0, and every other dependency or asset retains its own licence and
attribution requirements.

The official Matcha archive is acquired, pinned, audited, and packaged as a distinct
third-party input. Matcha-derived files, adaptations, resource content, and generated
outputs are not casually copied into the proprietary Forever namespace or presented as
original work. When an adaptation is necessary, its provenance, applicable licence, and
share-alike implications are recorded with the material.

Third-party notices and attribution are maintained as release inputs. A source file,
resource, script, data file, or generated artifact must have a known origin before it is
committed or distributed. The adapter boundary in [ADR 0002](0002-isolate-matcha-behind-adapter.md)
provides the default place for Matcha-specific implementation knowledge, while original
Forever mechanics remain separately authored.

Keeping the project private is not permission to ignore licence terms. Before a release,
redistribution, public repository, asset bundle, or derivative is made, the project must
recheck the applicable licences and decide whether the package layout and notices satisfy
them. A future change to the project licence or to Matcha use requires a new ADR or a
superseding licensing review.

## Consequences

- Ownership and provenance remain legible. Contributors and AI agents can tell which
  material is Forever-owned, Matcha-derived, or supplied by another third party.
- The project can keep the original Forever code private without accidentally claiming
  ownership of the selected gameplay pack or its derivatives.
- The adapter and classification boundaries make it easier to replace a Matcha mechanic
  with an original implementation without carrying over hidden files or obligations.
- Attribution and licence checks become a normal part of acquisition and release work,
  rather than a last-minute inspection after assets have been mixed.
- Separate directories, manifests, notices, and packaging rules add friction to ordinary
  development. Copying a useful Matcha example into a Forever class is no longer a
  harmless shortcut.
- Share-alike and non-commercial constraints may limit how a future public or commercial
  distribution can combine Matcha-derived material with the proprietary project. The
  project accepts that uncertainty rather than promising an incompatible release.
- Keeping Forever private reduces immediate collaboration and may make external review or
  contribution harder. That is a current project choice, not a claim that privacy is a
  permanent product goal.
- Provenance can be lost through generated resources, transformed archives, or copied
  snippets even when the original file is still present. Build and review tooling must
  preserve origin metadata where practical.
- Optional integrations such as climate providers need the same treatment. Their adapters
  may be private Forever code, but their APIs, assets, and redistributed material remain
  subject to their own terms.
- Future work includes a provenance manifest, contribution guidance, automated notice
  checks, package separation, derivative review, and a release checklist that tests both
  licensing and world-save compatibility.
- We accept a slower packaging workflow and occasional duplicated original work in exchange
  for a boundary that is understandable, auditable, and legally defensible.

## Rejected alternatives

### Re-license all material as proprietary

Forever cannot unilaterally re-license Matcha or other third-party material. Claiming a
single proprietary licence would discard attribution and share-alike obligations and could
make a distribution misleading or unlawful.

### Treat Matcha as an undifferentiated dependency

A dependency declaration without provenance separation would be convenient, but Matcha is
an archive with its own licence and implementation details, not a normal Fabric library.
It would make it too easy to copy or distribute derived material without reviewing its
terms.

### Fork Matcha and merge it into Forever's source tree

A fork might make modifications easier, but merging it into proprietary code would blur
ownership and derivative boundaries while increasing update and migration risk. If a fork
is ever necessary, it needs a distinct provenance and licensing decision.

### Open-source the whole project immediately

Public source could improve collaboration, but it would be a new licence and distribution
commitment for both original code and third-party inputs. Privacy is locked for now so the
project can establish ownership and compliance before choosing a public model.

### Choose a different pack solely for simpler licensing

A different pack might reduce legal complexity, but licensing alone does not establish
technical or design fit. The current pinned starting point remains subject to separation,
audit, and the option to change through an explicit decision.

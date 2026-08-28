# Third-party notices

Forever is a private project. This file records third-party material used during
local development and is not a grant of permission to redistribute any of it.

## Matcha Flavoured

**Project:** Matcha Flavoured  
**Creator / Modrinth team owner:** Klei, identified in the official Modrinth team
metadata by the username `klei_wright` and user ID `EENoO6ef`  
**Modrinth project:** `https://modrinth.com/datapack/matcha-flavoured`  
**Official project metadata:** `https://api.modrinth.com/v2/project/QI0EmgZ1`  
**Official version metadata:** `https://api.modrinth.com/v2/version/E9rngRfK`  
**Official team members:** `https://api.modrinth.com/v2/team/rOf1cMOd/members`  
**Pinned version:** `1.12`, version ID `E9rngRfK`  
**Minecraft compatibility:** `26.2`  
**Archive:** `Matcha_Flavoured_1_12.zip`  
**Official download URL:** `https://cdn.modrinth.com/data/QI0EmgZ1/versions/E9rngRfK/Matcha_Flavoured_1_12.zip`  
**Verified SHA-256:** `6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248`  
**Licence:** `CC-BY-NC-SA-4.0`  
**Licence text:** `https://creativecommons.org/licenses/by-nc-sa/4.0/`

The official team endpoint lists `klei_wright` as the accepted team owner. The
project page also credits the creator as Klei. That account-level attribution is
used here instead of guessing a legal name.

### What CC-BY-NC-SA-4.0 means

The Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
licence generally allows people to share and adapt the licensed material, subject
to these important conditions:

- **Attribution:** credit the creator, link to the licence, and indicate whether
  changes were made. Attribution must not imply endorsement.
- **NonCommercial:** the material and adaptations may not be used for a
  primarily commercial purpose.
- **ShareAlike:** adaptations must be distributed under the same licence, or a
  compatible licence permitted by its terms.
- **No additional restrictions:** downstream recipients cannot be given legal or
  technical restrictions that remove the freedoms granted by the licence.

The licence does not replace review of credits for components that may belong to
other contributors. The project’s own notices and credits remain relevant, and
any third-party content inside the archive must be checked separately.

The original Matcha binary is **not** in Forever source control. Fetching it places
a local copy at `vendor/matcha/`, which is ignored. Only the deterministic identity
and audit metadata in `matcha.lock.json` is intended to be committed.

Forever is private at this stage. Any public distribution of the archive, a bundle
containing it, a derivative, or a release that combines it with Forever code must
be reviewed separately for attribution, non-commercial use, share-alike
obligations, third-party credits, and the proposed distribution terms. This notice
is not that legal review and does not pre-approve a public release.

## Fabric and build dependencies

The following development dependencies are recorded in the project’s pinned
baseline. Their upstream licences remain separate from the Forever licence:

| Dependency | Pinned version in this project | Licence | Upstream source |
|---|---:|---|---|
| Fabric Loader | `0.19.3` | Apache-2.0 | [Fabric Loader](https://github.com/FabricMC/fabric-loader) |
| Fabric API | `0.158.0+26.2` | Apache-2.0 | [Fabric API](https://github.com/FabricMC/fabric) |
| Fabric Loom | `1.17.20` | Apache-2.0 | [Fabric Loom](https://github.com/FabricMC/fabric-loom) |
| Gson | project/toolchain dependency | Apache-2.0 | [Google Gson](https://github.com/google/gson) |
| JUnit Jupiter / JUnit Platform | `5.14.2` | EPL-2.0 | [JUnit 5](https://github.com/junit-team/junit5) |

The authoritative licence text for each dependency is the licence file shipped by
its upstream project or artifact. Dependency licences do not change the status of
original Forever code. `LICENSE` applies only to that original Forever code and
original Forever assets, not to Matcha or to these dependencies.

Minecraft itself is not redistributed by these scripts. The Fabric toolchain and
Minecraft runtime remain subject to their own terms and installation channels.

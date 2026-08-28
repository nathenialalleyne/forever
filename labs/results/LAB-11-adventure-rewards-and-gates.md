# LAB-11: Adventure rewards and capability gates

- Date: 2026-08-28
- Run by: lab worker, automated evidence review
- Manifest: `labs/manifests/LAB-11-adventure-rewards-and-gates.toml`
- Status: complete

## Question

Does the current Matcha and vanilla reward surface require a quest or structure reward to unlock any essential capability, and does any named 26.2 Fabric candidate provide a safe, non-duplicative solution?

## Exact versions

| Component | Project ID | Version ID | File |
|---|---|---|---|
| Minecraft | | | 26.2 |
| Fabric Loader | | | 0.19.3 |
| Fabric API | `P7dR8mSH` | `NqwNSxwA` | fabric-api-0.158.0+26.2.jar |
| Global Packs | `NRLPy2mk` | `DqrPrUMp` | globalpacks-fabric-26.2-26.2.0.jar |
| Matcha Flavoured | `QI0EmgZ1` | `E9rngRfK` | Matcha_Flavoured_1_12.zip |

Candidate release evidence was checked against the live Modrinth API. Better Advancements has project ID `Q2OqKxDG`, Fabric 26.2 beta version `8xsHNtMk`, file `BetterAdvancements-Fabric-26.2-0.6.0.72.jar`. Advancement Plaques has project ID `9NM0dXub`, Fabric 26.2 release `EULg1tpY`, file `AdvancementPlaques-26.2-fabric-1.7.2.jar`. Patchouli project `nU0bVIaL` lists no 26.2 version; its newest observed Fabric version is the 26.1.2 beta `AveV4Tjn`. The canonical FTB Quests project was not identified as a verifiable 26.2 Fabric release, so no version was invented.

## Test world

- Seed: not created; no candidate qualified for an install
- World type and difficulty: not applicable
- Game rules changed: none
- Disposable world path: not applicable
- Confirmed not a real survival world: yes

## Client and server configuration

- Client tested: no candidate installed. Client-only questions were answered from official Modrinth environment metadata and prior LAB-01/LAB-10 evidence; no visual claim is made.
- Dedicated server tested: no new candidate run. The unchanged baseline was already verified in LAB-01 and LAB-10.
- Multiplayer tested: no. The acceptance question is satisfied by documented vanilla and local solo routes.

## Startup outcome

No candidate was installed, so there is no candidate startup log. The applicable baseline evidence from LAB-01 is:

```text
[main/INFO]: Loading Minecraft 26.2 with Fabric Loader 0.19.3
[main/INFO]: Found new data pack Matcha_Flavoured_1_12.zip, loading it automatically
[main/INFO]: Loaded 2346 recipes
[main/INFO]: Loaded 1805 advancements
[Server thread/INFO]: Done (1.478s)! For help, type "help"
```

LAB-10 also verified that no structure provider is adopted. This lab therefore does not manufacture a candidate fixture when the compatibility gate has no target.

## Matcha interaction

LAB-01 measured Matcha's 243 advancement files. Of these, 141 are recipe plumbing already silenced by Matcha, 59 are tutorial entries, 20 are mechanics entries, 8 are Nether progression, 5 are End progression, and 13 are other content. Only 69 show toasts, and Matcha has three root tabs. LAB-01 found no redundant-notification category and recommended changing nothing. The tutorial and mechanics entries are required instruction and must remain inspectable.

### Reward classification

| Existing reward or surface | Classification | Route and gate result |
|---|---|---|
| Vanilla crafting, smelting, shelter, ordinary block placement, inventory, food, and basic repair/recovery | Essential | Direct vanilla routes. No adventure, quest, structure, or teammate is required. |
| Matcha recipe-plumbing advancements | Convenience | Vanilla recipe book and the underlying accessible recipes remain the route. Matcha already suppresses their notifications. |
| Matcha tutorial and mechanics advancements | Knowledge | The advancement text is the current inspectable route. Future Field Journal entries must preserve the instructions and provenance. Discovery may expose them progressively, but it must not remove expected process instructions. |
| Matcha Nether and End progression and other discovery advancements | Optional opportunity / knowledge | Exploration is the intended route to discovery, but ordinary survival and the core baseline do not depend on one rare generated structure. Vanilla dimension routes remain available. |
| Vanilla advancement toast, icon, and tab presentation | Cosmetic / knowledge | It communicates milestones and discovery. It does not grant an essential capability. |
| Historical completion state and already-earned advancement records | Historical | Preserve as player history. Removing a presentation mod must not be treated as deleting the underlying record. |

No essential capability lacked a documented non-adventure, solo route. In particular, LAB-10's no-structure decision means there is currently no adopted generated structure that can strand an essential reward. This satisfies ADR 0028 and the ticket's no-structure acceptance condition without adding a reward system.

## Multiplayer interaction

No new server-authoritative reward system was installed. Existing vanilla and Matcha advancement completion remains server-owned. Solo play is viable because the listed essential capabilities are vanilla/local, and no reward requires another player. No multiplayer-only reward or declined-opportunity failure path was introduced.

## Persistence

Vanilla advancement progress is persistent player history. Matcha's advancement records are likewise persistent advancement state, not a new lab-owned reward database. No quest, plaque, Patchouli book, or structure state was added. Any future journal or reward state must be versioned and migration-aware under the project's persistent-data rules.

## Removal behaviour

There is no candidate removal test because no candidate was installed. This is intentional, not omitted evidence. The existing LAB-10 structure-removal test demonstrated that removing generated structure content can leave unknown structure starts and missing datapack warnings. That is additional evidence against adopting an adventure structure provider merely to create reward gates.

## Performance observations

No candidate process was run. No new dependency, chunk generation, persistent structure, or recurring reward scan was introduced.

## Conflicts observed

- Advancement noise: no new problem identified. LAB-01 measured no redundant notification problem and proposed no suppression.
- Guidebook duplication: no need identified. LAB-01 found no Matcha guidebook and no adopted guidebook from REI/Jade.
- Structure overlap: not applicable to the adopted baseline. LAB-10 adopted no structure provider.
- Advancement Plaques would replace or decorate the notification surface, but its purpose is cosmetic and it is client-only. It does not address a measured issue.
- Better Advancements is client-only and its 26.2 Fabric build is beta. It is not suitable as a core dependency.

## Does it fit the pack?

The honest fit is to adopt nothing and change nothing. FTB Quests would add a quest authority for a problem not demonstrated by the current reward surface, while no exact 26.2 Fabric release was verified. Patchouli would add a second knowledge-book mechanism despite no current guidebook gap, and has no 26.2 release. Better Advancements and Advancement Plaques are client-only presentation changes, not essential capability providers. Better Advancements is beta on the target version; Advancement Plaques is a release but duplicates a surface that LAB-01 found adequate.

This preserves ordinary Minecraft, solo viability, the conservative world-generation posture in ADR 0015, the no-essential-adventure-gate rule in ADR 0028, the single contextual knowledge direction in ADR 0029, and the no-global-scaling rule in ADR 0030. No ADR or specification conflict was found.

## Follow-up required

- Keep the baseline unchanged. Do not add a quest engine, structure provider, advancement suppression, plaque layer, or Patchouli book as a reaction to this lab.
- When the Field Journal ticket is eventually executed, map Matcha tutorial/mechanics instruction and alternate routes without leaking Matcha internals.
- Revisit FTB Quests or Patchouli only when a concrete project/reward workflow exists and an exact 26.2 Fabric release is available.
- LAB-12 should treat “no reward system needed” as evidence against custom quest code, not as a missing implementation.

## Decision

| Candidate | Verdict | Reason |
|---|---|---|
| FTB Quests | DEFER | No canonical 26.2 Fabric release was verified, and no current essential-gate problem justifies adding a quest authority. |
| Better Advancements | DEFER | Exact 26.2 Fabric build exists but is beta and client-only; it addresses presentation, not a measured noise or capability gap. |
| Advancement Plaques | REJECT | Exact 26.2 Fabric release exists, but it is client-only cosmetic duplication of an already-acceptable advancement surface. |
| Patchouli | DEFER | No exact 26.2 release exists; adding another book would also conflict with the current single-knowledge-surface direction. |
| Structure providers | REJECT | LAB-10 adopted no structure provider, and ADR 0015 plus removal evidence make an adventure gate unjustified and save-risky. |
| Matcha and vanilla rewards | ADOPT_BASELINE | Keep the existing reward and advancement surface unchanged. All essential capabilities have documented non-adventure solo routes; knowledge and discovery rewards remain available without becoming mandatory gates. |

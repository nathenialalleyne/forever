# Compatibility laboratory

A lab is an isolated compatibility spike. It answers one question about a small set of
mods, and it produces a written result with a decision.

Labs exist because adding every candidate mod to one pack and launching it produces an
unreadable result. When twenty mods load together and something breaks, the failure
tells you almost nothing about which mod caused it. Testing in small groups costs more
runs but produces evidence you can act on.

## Rules

- One lab tests one question. If a lab needs two questions answered, split it.
- Never add all candidates to the primary baseline pack. The baseline in `pack/` stays
  minimal; labs use their own manifests.
- Every lab records exact versions. "Latest" is not a version.
- Every lab uses a disposable world. Never open a real survival world. Follow
  `docs/world-save-safety.md`.
- A lab that cannot be completed records the blocker. An unfinished lab with an honest
  blocker is more useful than a guess.
- Removal behaviour matters as much as installation. A mod that cannot be removed from
  a world without corrupting it is a much larger commitment than one that can.

## Layout

```text
labs/
├── README.md          this file
├── manifests/         one directory or file per lab, listing exact pinned versions
└── results/           one written result per lab run, from template.md
```

A manifest must be reproducible. Record Modrinth project IDs and version IDs, not just
names, so the same lab can be re-run months later against the same artifacts.

## The lab queue

| Lab | Question | Status |
|---|---|---|
| LAB-01 | Information and onboarding: recipe viewer, Jade, guidebook noise, advancement noise, Field Journal requirements | Done: REI + Jade adopted, JEI excluded, no guidebook problem found |
| LAB-02 | Building and gathering: assisted placement, vein mining, schematics, durability and inventory consumption, multiplayer authority | Next |
| LAB-03 | Food: Farmer's Delight Refabricated against Matcha hunger and healing behaviour | Planned |
| LAB-04 | Seasons: Serene Seasons against Matcha environment, crops, weather, performance | Planned |
| LAB-05 | Storage: local indexed storage, early shulkers, wireless restrictions, world removal | Planned |
| LAB-06 | Logistics: pipes, minecarts, storage integration, chunk unloading | Planned |
| LAB-07 | Transportation: rail, Elytra behaviour, horses, roads, dedicated server | Planned |
| LAB-08 | Skills: one Focus and two Supporting slots, external progression awards, suspension, UI noise | Planned |
| LAB-09 | Durability and repair: smithing overhaul against Matcha equipment, broken state, Mending conflicts | Planned |
| LAB-10 | Villagers: villager API against Matcha trades, profession overlays, stock limits | Planned |
| LAB-11 | Exploration: one restrained structure pack, spacing, loot control, save permanence | Planned |
| LAB-12 | Building automation: real material consumption, nearby storage, server permissions, limits | Planned |

## Running a lab

1. Copy `results/template.md` to `results/LAB-NN-<subject>.md`.
2. Write the manifest under `manifests/` with exact project and version IDs.
3. Build an isolated instance. Do not reuse the baseline instance.
4. Record what you observed, not what you expected. Keep observations separate from
   explanations.
5. End with one decision per candidate, using the vocabulary from the candidate matrix:
   ADOPT_BASELINE, PILOT, COMPATIBILITY_SPIKE, DEFER, REJECT, REPLACE_EXISTING_CODE, or
   NEEDS_MORE_EVIDENCE.

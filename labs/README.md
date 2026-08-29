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
| LAB-01 | Information and onboarding: recipe viewer, contextual info, guidebook noise, advancement noise, Field Journal requirements | Done: REI + Jade adopted, JEI excluded, no guidebook problem found |
| LAB-02 | Matcha pack-loader and dual role: one archive as both datapack and resource pack, load order, failure diagnostics | Done: Global Packs adopted, but ALL failure paths fail open. Raised MRH-010. |
| LAB-03 | Building and excavation: assisted placement, vein mining, durability and inventory consumption, multiplayer authority | Done: Effortless Building + VeinMiner PILOT; WorldEdit rejected |
| LAB-04 | Blueprints and schematics: preview, material lists, .litematic and .schem support, server authority | Done: Litematica PILOT for Ghost Plan; Simple Blueprints rejected |
| LAB-05 | Food, cooking, and preservation against Matcha hunger and healing behaviour | Done: ADOPT NOTHING. Matcha already owns food; all 7 candidates rejected or deferred |
| LAB-06 | Seasons and climate against Matcha environment, crops, weather, performance | Done: ADR 0014 upheld; Homeostatic PILOT behind the abstraction; nothing added to pack/ |
| LAB-07 | Storage indexing and local search: early shulkers, wireless restrictions, world removal | Done: no candidate qualifies; StorageGuide PILOT for read-only search only |
| LAB-08 | Local item transport and automation: pipes, minecarts, storage integration, chunk unloading | Done: vanilla hoppers baseline; Logistics: Automation PILOT; teleporting mods rejected |
| LAB-09 | Rail, horse, boat, and vehicle behaviour on a dedicated server | Done: all vanilla modes adopted as baseline; every teleport-like candidate rejected |
| LAB-10 | Performance, ambience, structures, and worldgen: spacing, loot control, save permanence | Done: Lithium+FerriteCore adopted; no structure provider; client verdicts to LAB-CLIENT2 |
| LAB-11 | Adventure rewards and capability gates: no essential capability is adventure-only | Done: adopt nothing; Matcha/vanilla rewards kept; structure gates rejected |
| LAB-12 | Companion boundary and gap analysis: what genuinely needs custom code | Done: only G-01 storage index conditionally justifies custom code; no code authorised |

## Running labs in parallel

Labs are not a strict sequence. Most of the dependencies in the backlog are real but
shallow, so once the two foundation labs are done the queue opens up considerably.

Deriving the waves from the recorded `Dependencies` field of each ticket:

| Wave | Labs that can run concurrently | Unblocked by |
|---|---|---|
| Foundation | LAB-01, then LAB-02 | nothing; LAB-02 depends on the information stack |
| 1 | **LAB-03, LAB-05, LAB-07** | LAB-01 and LAB-02 |
| 2 | LAB-04, LAB-06, LAB-08, LAB-10 | wave 1 |
| 3 | LAB-09, LAB-11 | wave 2 |
| 4 | LAB-12 | everything else, by design |

Ten sequential labs become four waves. LAB-12 is deliberately last because its whole
purpose is to decide what genuinely needs custom code, which is only answerable once
every other lab has reported.

### Rules for concurrent labs

- Each lab gets its own instance directory under `/tmp`. Instances must never be shared,
  because a lab's value comes from knowing exactly which mods were present.
- A worker commits only its own manifest and result, by name. Never `git add -A` while
  other labs are running.
- Only the coordinator updates `docs/backlog.md` and this file. Workers report a status
  line instead, so two agents never fight over the same table.
- If two labs both want to adopt a mod, the second one to report defers to the first and
  records the overlap rather than adding a duplicate pack entry.

## Running a lab

1. Copy `results/template.md` to `results/LAB-NN-<subject>.md`.
2. Write the manifest under `manifests/` with exact project and version IDs.
3. Build an isolated instance. Do not reuse the baseline instance.
4. Record what you observed, not what you expected. Keep observations separate from
   explanations.
5. End with one decision per candidate, using the vocabulary from the candidate matrix:
   ADOPT_BASELINE, PILOT, COMPATIBILITY_SPIKE, DEFER, REJECT, REPLACE_EXISTING_CODE, or
   NEEDS_MORE_EVIDENCE.

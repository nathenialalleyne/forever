# FVR-700 playtest session 001

Status: **blocked.** The disposable world and Matcha install are ready, but the
implemented systems have no player-reachable entry point yet. See the blocker below.

## Blocker: the systems are not reachable in normal play

**Read this before starting.** I checked the source before writing the checklists below
and the honest position is that FVR-700 cannot be run as written yet. The seven systems
are implemented and tested, but almost none of them is currently connected to anything
a player can do.

What is missing, verified by inspection:

| Missing | Evidence |
|---|---|
| Recipes | No recipe JSON anywhere under `src/main/resources/data` |
| Commands | No command class, and no `CommandRegistrationCallback` |
| Creative tab | No `CreativeModeTab` reference in any source set |
| Item textures and models | `items/` and `models/item/` contain only `.gitkeep` |
| Gameplay event hooks | The only non-lifecycle hook is `ServerLivingEntityEvents.AFTER_DEATH` in `ForeverCareer` |
| Cross-system callers | Every system is reached only by `initialize()` from `ForeverMod` |

The practical consequences are:

- The three registered items, `forever:field_tool`, `forever:travelers_cache`, and
  `forever:obol`, exist in the registry and can be summoned with `/give`, but they have
  no model, so they render as the missing-model placeholder.
- `EquipmentService.damage(...)` has **no caller outside its own package**. Nothing
  applies wear during play, so a tool will not wear down, reach zero condition, or
  become broken by being used. The broken-state behaviour is real, but only a test
  currently reaches it.
- Mastery, settlements, storage, guide, career, and economy are likewise only reachable
  from their own packages and their tests.

So a session now would mostly measure the absence of wiring, not the balance values
FVR-700 exists to interrogate. Balance observations recorded against `/give`-summoned
items with no wear loop would be misleading.

**Recommendation:** treat this as the finding, and raise an integration ticket to give
each implemented system a player-reachable entry point before playtesting. That ticket
is a prerequisite of FVR-700, not part of it. I have not raised or started it, since one
ticket at a time is the rule and this is your call.

## What you can genuinely do today

Three things are real and worth doing.

**1. Watch all 27 GameTests run in a live world.** This is the closest thing to
observing the systems behave, and it passes today:

```sh
export JAVA_HOME=~/toolchains/jdk-25.0.4.1+1
./gradlew runGametest
```

It ends with `All 27 required tests passed`. Each test builds a real server world,
drives one system, and asserts the outcome. Reading `src/gametest/java/dev/forever/`
alongside the run shows exactly what each system does and what it does not yet do.

**2. Confirm the modded world boots and Matcha is live.** Set `level-name` to
`fvr700-playtest-world` in `run/server.properties`, run `./gradlew runServer`, then set
it back. Expect `Found new data pack file/Matcha_Flavoured_1_12.zip` and signals in 10
namespaces.

**3. Play the world as a Matcha player.** Run `./gradlew runClient`, create a disposable
world with the Matcha datapack added, and play it. Matcha's own content is fully active,
so this is a genuine test of the pack Forever intends to build on, and of whether
Forever's absence is noticeable. It is not yet a test of Forever's own systems.

## Session facts to record when the session becomes possible

| Field | Value |
|---|---|
| Forever commit | `e308b0a` |
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.158.0+26.2 |
| Java | 25 |
| Matcha | Flavoured 1.12, Modrinth version `E9rngRfK` |
| Matcha archive SHA-256 | `6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248`, matches `matcha.lock.json` |
| Disposable world | `run/fvr700-playtest-world/`, marker present, git-ignored |
| World seed | *record before playing* |
| Difficulty and game rules | *record before playing* |
| Resource packs | *record, including whether Matcha's pack is enabled* |

## Known startup observations

These came from the smoke boot and are context for the session, not defects in Forever.

- All seven systems registered: equipment, mastery, settlement, storage, guide, career,
  economy, plus Matcha compatibility.
- Matcha compatibility reported `PRESENT_UNVERIFIED`. The Matcha datapack **was loaded
  and its content is live**: the server logged `Found new data pack
  file/Matcha_Flavoured_1_12.zip` and observed signals in 10 known namespaces. Matcha's
  own recipes, advancements, and functions are running, so the world is modded and will
  play as modded.

  What switched off is only **Forever's adapter**, the layer that translates a Matcha
  item or behaviour into a Forever concept. "Vanilla-safe" is the code's term for the
  no-op fallback in `NoOpMatchaAdapter`: it reports `supports(...) == false`, returns
  `unavailable` for every translation, and explicitly preserves the original item and
  vanilla handling rather than guessing. It does not mean the world is vanilla.

  This is deliberate. The adapter refuses to map Matcha internals unless it can prove
  the pack's identity from both the locked archive SHA-256 and parsed `pack.mcmeta`. My
  smoke boot supplied a copied datapack directory rather than either, so it could not
  prove identity and declined to act. Guessing here would risk misreading a disguised
  item, which is exactly what ADR 0002 and principle 13 exist to prevent.

  **The open question for the session** is what a player actually sees. Matcha content
  works either way, so decide whether any Forever feature that depends on the adapter
  is silently missing, and whether that is discoverable. If a normal install of the
  official pack also lands in `PRESENT_UNVERIFIED`, that is a defect worth a ticket
  against the adapter row of `docs/matcha-audit/system-classification.csv`.
- Matcha's own content produced parse warnings from its files, not Forever's:
  `minecraft:advancement/custom/root`, `main:mechanics/wither_test`, and two stray
  `.txt` paths. Record them against the audit if they affect play.

## The vanilla control

Run the same actions in a second world with **no** Matcha datapack and, where the
question allows, with the Forever feature unused. The control is part of the test. A
balance reading without it is an anecdote.

## Systems to exercise

Play normally first and only then push boundaries. For each system, record the action,
the expected result, the observed result, and whether it is confirmed or ambiguous.

Known player-facing entry points are the registered items `forever:field_tool`,
`forever:travelers_cache`, and `forever:obol`. Obtain them the intended way if a
recipe or reward exists, and fall back to `/give` only when it does not, recording
which you used.

### 1. Equipment condition and repair

Questions: does wear read clearly without relying on colour, is a broken tool obviously
broken and still identifiable, and does field repair versus workshop repair feel worth
the cost?

- [ ] Use `forever:field_tool` until it wears, then to zero condition.
- [ ] Confirm it becomes broken and unusable but is **not deleted** (principle 5).
- [ ] Field-repair it, then workshop-repair it, and compare restored amounts.
- [ ] Check the tooltip states condition in text, not colour alone (principle 16).
- [ ] Relog and confirm condition persists.

### 2. Mastery

Questions: does progress come from meaningful accomplishment rather than repetition,
and is one Focus plus two Supporting the right shape?

- [ ] Earn progress and note what actually granted it.
- [ ] Confirm no raw repetition counter is the primary path (principle 4).
- [ ] Switch the active loadout and confirm inactive masteries keep their levels and
      cost no XP (principle 3).
- [ ] Record whether progression rate feels too fast or slow, with the timing.

### 3. Settlements

- [ ] Register buildings and confirm the graph attaches by proximity as intended.
- [ ] Confirm validation checks function only, never architectural style (principle 7).
- [ ] Leave the area, return, and confirm no permanent chunk loading is implied
      (principle 10).

### 4. Storage and the Traveler's Cache

- [ ] Confirm `forever:travelers_cache` has exactly 9 slots, and 18 after the approved
      expansion.
- [ ] Attempt to nest a container inside it and confirm a clear refusal.
- [ ] Relog and confirm contents survive.
- [ ] Confirm ordinary inventory use is not degraded (principle 17).

### 5. Field Guide

- [ ] Confirm every mechanic you used is explained in game (principles 2 and 15).
- [ ] Record any mechanic you had to infer from code, a wiki, or guesswork. That is a
      defect, not a preference.

### 6. Mason career

- [ ] Progress the career and record what advanced it and how long it took.
- [ ] Confirm rank is shown by symbol or text, never by colour alone.

### 7. Obol economy

- [ ] Acquire `forever:obol` and confirm physical coins and Coin Purse balance both work.
- [ ] Confirm the purse does not consume many inventory slots.
- [ ] Withdraw a balance back to physical coins.
- [ ] Record whether prices and rewards feel meaningful, with the numbers observed.

## Balance observations

One row per value that felt wrong. An observation is required; an opinion alone is not
enough to justify a change.

| System | Value | Observed behaviour | Why it felt wrong | Suggested direction |
|---|---|---|---|---|
| | | | | |

## Matcha conflicts

Write each against the relevant row of `docs/matcha-audit/system-classification.csv`.

| Matcha behaviour | Forever system | Conflict observed | Classification row |
|---|---|---|---|
| | | | |

## Defects found

Each confirmed defect becomes its own ticket and gains a regression test in the ticket
that fixes it. Do not fix defects ad hoc inside this session.

| Summary | Reproduction | Confirmed or intermittent | Ticket |
|---|---|---|---|
| | | | |

## After the session

- [ ] Record how long the session lasted and which systems went untested.
- [ ] Delete or reset the disposable world, or note that it is being kept for a
      follow-up session.
- [ ] Raise a ticket per confirmed defect and per balance change worth making.

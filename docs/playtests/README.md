# Playtest recording

Playtests answer questions about player experience, balance, and failure modes after
the relevant behaviour has been implemented or a controlled prototype exists. They
do not replace source tracing, compatibility audits, or automated tests.

## Disposable-world rule

**Every playtest uses a disposable world. Never use a real Forever world.**

A playtest world may contain useful temporary progress, but it must be safe to
delete, regenerate, or reset. Do not open a long-lived personal survival world, a
shared production server, or any world that contains irreplaceable builds or save
data for a playtest. This protects the world save, avoids accidental migration, and
makes a result reproducible.

If a test needs a long setup, create a reproducible fixture or archive the disposable
world separately from real saves. Do not copy a real Forever world into a test
process unless the owner has explicitly made a disposable clone and the note says
that it is a clone. The clone is still disposable and must never be written back.

## Before the session

1. State one or more questions the session is meant to answer. A useful question
   names the player action, comparison, and observable outcome.
2. Record the exact build or commit, Minecraft and loader context, and any relevant
   configuration. Do not write only "latest".
3. Record whether Matcha is present, absent, or an unexpected version. When it is
   present, record the locked Matcha version and archive SHA-256. Do not test an
   unverified archive and call it the pinned release.
4. Create a new disposable world. Record its seed, world type, difficulty, game
   rules, dimensions used, and any setup commands or fixture files.
5. List the resource packs, optional mods, server mode, player count, and client
   settings. Resource-pack-dependent identity needs a test with the expected pack
   and at least one alternate or missing-pack case.
6. Define the vanilla control. Use the same relevant setup where possible, with
   Matcha and the optional Forever feature absent. A control is part of the test,
   not an optional anecdote.

Do not change several systems at once if the session is intended to measure one
mechanic. If a setup change is necessary, record it before continuing and mark the
reading as a new condition.

## During the session

Record what the player actually did and what the server actually returned. Keep
observations separate from explanations. A useful entry includes:

- the initial inventory, health, hunger, equipment, scores, and nearby blocks or
  entities when they affect the result;
- the exact action or command sequence;
- the expected vanilla-control result;
- the observed result, including no-op, delay, duplicate, loss, or confusing UI;
- timing, counts, and resource costs when balance is being read;
- player count and which player owned or triggered the state;
- screenshots, logs, trace IDs, or reproduction steps for a bug; and
- whether the result is confirmed, intermittent, or still ambiguous.

Test the normal path first, then the boundary and recovery paths. For persistent or
multiplayer mechanics, include relog, restart, chunk unload, death, ownership
changes, simultaneous actions, and Matcha absent or unsupported where relevant.
Stop and preserve the disposable world state when a data-loss or duplication bug
is found. Do not keep playing through it and overwrite the evidence.

## After the session

1. Fill the note before starting another session. Do not rely on memory for friction
   or prices.
2. Separate observed friction from a proposed fix. "The status was not visible
   without opening the screen" is an observation. "Add a red icon" is a decision
   that still needs the accessibility rules.
3. Record balance readings with their conditions, units, and comparison to vanilla
   or the target. A single subjective impression can be useful, but it is not a
   complete balance reading.
4. Record every bug with reproduction steps, actual result, expected result, build,
   world seed, player count, and severity or data-loss risk.
5. Record decisions explicitly. A decision may be to keep the behaviour, change a
   data value, defer a question, reject an implementation, or run another test. It
   is not permission to edit unrelated systems.
6. Link follow-up tickets or audit evidence where available. If the result conflicts
   with the source trace, preserve both and mark the discrepancy for investigation.

Playtest notes are evidence for iteration. They do not change the Matcha lock, the
adapter boundary, or a persistent schema by themselves.

## Playtest note template

Copy this template for one session. Keep one session per note so that a later agent
can compare conditions without guessing which observations happened together.

```markdown
# Playtest: YYYY-MM-DD, short scenario name

## Session identity

- Date: YYYY-MM-DD
- Session ID: `YYYY-MM-DD-short-slug`
- Build/version: `commit-or-build-version`
- Minecraft / loader context: `version and relevant loader context`
- Matcha version: `absent`, `1.12 / E9rngRfK`, or `unexpected and explain why testing stopped`
- Matcha archive SHA-256: `hash or N/A`
- Resource packs: `names and versions, or none`
- Optional mods and adapters: `list and versions, or none`
- Server mode: `single-player, integrated server, or dedicated disposable server`
- Players: `count and test roles`
- World seed: `seed`
- World status: `DISPOSABLE ONLY. Describe how it was created and where it will be deleted.`
- World settings: `difficulty, game rules, dimensions, simulation distance`
- Fixture or setup commands: `commands, structures, items, or scripts`
- Vanilla control: `world or condition used for comparison`

## What was tested

- Question or hypothesis:
- System and feature:
- Preconditions:
- Steps performed:
  1.
  2.
  3.
- Expected result:
- Vanilla-control result:

## Observed friction

Record facts first. Include confusing wording, excess clicks, waiting, travel,
resource handling, failure recovery, accessibility problems, multiplayer confusion,
and any place where normal vanilla play became worse.

- Observation:
- Frequency or reproduction rate:
- Who experienced it:
- Evidence, screenshot, log, or trace ID:
- Interpretation, explicitly labelled:

## Balance readings

Use numbers where practical. Include conditions and units, not just a verdict.

| Reading | Value | Conditions | Vanilla or target comparison | Confidence |
|---|---:|---|---|---|
| Time to complete |  |  |  |  |
| Items or resources spent |  |  |  |  |
| Items or resources gained |  |  |  |  |
| Failure, death, or repair cost |  |  |  |  |
| Repetition or waiting required |  |  |  |  |
| Other relevant reading |  |  |  |  |

Player comments or qualitative reading:

## Bugs

For each bug, include reproduction steps and whether it risks data loss, duplication,
crash, multiplayer desynchronisation, or only presentation.

- Bug ID or ticket:
- Severity and risk:
- Reproduction steps:
  1.
  2.
  3.
- Expected result:
- Actual result:
- Affected players or save scope:
- Evidence and logs:
- Workaround, if any:

## Decisions

List decisions made from this session and keep them distinct from observations.

- Decision:
- Rationale:
- Scope:
- Follow-up ticket or next experiment:
- Owner:

## Session close-out

- World deleted or disposal scheduled: `yes/no, with explanation`
- Uncommitted changes or temporary files removed: `yes/no`
- Reproducibility notes:
- Open questions:
```

## Minimum acceptance for a useful note

A note is not complete until another person can identify the build, Matcha state,
world seed, player count, setup, tested action, observed result, and comparison
condition. It must include all of the following headings or equivalent fields:

- date;
- build/version;
- Matcha version;
- world seed;
- what was tested;
- observed friction;
- balance readings;
- bugs; and
- decisions.

If any value is unknown, write `unknown` and explain how it could be resolved. Never
fill a missing seed, version, or Matcha digest with a guess. A missing value is safer
than a false claim about a world or build.

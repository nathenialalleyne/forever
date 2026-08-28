# LAB-NN: <subject>

- Date:
- Run by:
- Manifest: `labs/manifests/LAB-NN-<subject>.toml`
- Status: planned | in progress | complete | blocked

## Question

State the single question this lab answers. If you cannot state it in one sentence,
the lab is too broad.

## Exact versions

| Component | Project ID | Version ID | File |
|---|---|---|---|
| Minecraft | | | 26.2 |
| Fabric Loader | | | 0.19.3 |
| Fabric API | `P7dR8mSH` | `NqwNSxwA` | fabric-api-0.158.0+26.2.jar |
| Matcha Flavoured | `QI0EmgZ1` | `E9rngRfK` | Matcha_Flavoured_1_12.zip |
| | | | |

## Test world

- Seed:
- World type and difficulty:
- Game rules changed:
- Disposable world path:
- Confirmed not a real survival world: yes / no

## Client and server configuration

- Client tested: yes / no / blocked, with reason
- Dedicated server tested: yes / no / blocked, with reason
- Multiplayer tested: yes / no, and how many players

## Startup outcome

Quote the decisive log lines rather than summarising them. Include the ready line and
any error or warning that appeared, even if it seemed harmless.

## Matcha interaction

Did the candidate conflict with Matcha recipes, items, tags, advancements, or pack
filters? Record recipe and advancement counts where they are informative.

## Multiplayer interaction

Server authority, desync, per-player state, and anything that behaved differently from
single-player.

## Persistence

What did the candidate write into the save? Component data, saved data, entity data,
chunk data, or nothing?

## Removal behaviour

Remove the candidate from a copy of the test world and record what happens. If removal
is unsafe to test, say so and explain why rather than skipping the section.

## Performance observations

Tick time, memory, chunk loading, and any profiling output. Note the hardware.

## Conflicts observed

Duplicate recipes, duplicate items, overlapping UI, competing keybinds, competing
guidebooks, or advancement noise.

## Does it fit the pack?

Judge against the pack's design principles, not against the mod's own goals. A good mod
that duplicates a system another candidate already owns is still a poor fit.

## Follow-up required

## Decision

One decision per candidate, with a one-line reason each.

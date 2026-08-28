# Compatibility labs: testing strategy

This document explains why Many Roads Home tests candidate mods in small isolated
groups, what a lab must produce to count as evidence, and how a lab result becomes a
decision. The operational detail lives in `labs/README.md`; this is the reasoning.

## Why not one big pack

The obvious approach is to install every candidate at once and see whether the game
starts. It is fast, and it is nearly worthless.

When twenty unfamiliar mods load together and something breaks, the failure tells you
almost nothing about which mod caused it. Worse, the common outcome is not a crash but a
quiet conflict: two mods both add a copper pipe, two guidebooks both grant a starter
item, two structure packs both claim the same biome, and the pack seems fine until a
player forty hours in finds a recipe that cannot be crafted. Bisecting that afterwards
costs far more than testing in groups would have.

Small labs cost more runs. They buy evidence that names a cause.

## What a lab is

A lab answers exactly one question about a small set of mods, against the fixed baseline
in `pack/`: Minecraft 26.2, Fabric loader 0.19.3, Fabric API `NqwNSxwA`, Global Packs
`DqrPrUMp`, and official Matcha `E9rngRfK`.

The baseline is deliberately minimal and stays that way. Candidates are added in a lab
instance, never to the primary pack, until a lab says they belong there.

## What counts as evidence

A lab result is evidence only if someone else could reproduce it. That requires:

- **exact versions**, as Modrinth project IDs and version IDs, not display names.
  Names are ambiguous and versions move;
- **the actual log lines**, quoted rather than summarised. "It started fine" is not a
  result; `Done (1.519s)` with the surrounding context is;
- **a disposable world**, with its seed and settings recorded, and never a real survival
  world;
- **observations separated from explanations**. Record what happened, then say what you
  think it means. Merging the two hides the moment where a guess entered the record.

A lab that cannot be completed records the blocker and stops. An honest blocker is more
useful than a guess, and much more useful than a result quietly produced by changing the
question.

## The five things every lab must check

Beyond its own question, every lab checks the properties that decide whether a mod is
safe to depend on for a long-lived world:

1. **Matcha interaction.** Matcha is the gameplay foundation and it is large. A
   candidate that fights it is a poor fit regardless of its own quality. Recipe and
   advancement counts are a cheap signal that something has been overridden.
2. **Dedicated-server behaviour.** A client-only assumption is a common defect and it
   surfaces late. Anything with server-authoritative behaviour must be tested on a real
   dedicated server, not just in single-player.
3. **Persistence.** What does the mod write into the save? Component data, saved data,
   entity data, or nothing? This determines the cost of ever removing it.
4. **Removal behaviour.** Remove the candidate from a copy of the test world and see
   what happens. A mod that cannot be removed without corrupting a world is a much
   larger commitment than one that can, and principle 18 says the save outranks the
   feature. If removal is unsafe to test, say so rather than skipping it.
5. **Interface noise.** Does it add another guidebook, another starter item, another
   advancement tree, another keybind? The pack is trying to deliver one coherent
   knowledge interface, and every mod that spawns its own manual works against that.

## How a lab becomes a decision

Each lab ends with one decision per candidate, using the vocabulary from
`docs/mod-research/candidate-matrix.csv`:

`ADOPT_BASELINE`, `PILOT`, `COMPATIBILITY_SPIKE`, `DEFER`, `REJECT`,
`REPLACE_EXISTING_CODE`, `NEEDS_MORE_EVIDENCE`.

A decision updates the candidate matrix and, where it changes who owns a system, the
ownership matrix. A lab that produces no decision has not finished.

## Relationship to the existing Java tests

The companion mod keeps its own unit tests and dedicated-server GameTests. Those verify
that our code behaves as specified. They cannot tell us whether a third-party mod
conflicts with Matcha, whether two mods duplicate a mechanic, or whether the combination
is any good to play. Labs answer the questions tests cannot.

The two are complementary and neither substitutes for the other. A green test suite
alongside an untested mod list is exactly the position this project was in before the
pivot.

## Ordering

LAB-01, information and onboarding, comes first because it decides the recipe viewer and
the contextual information mod. Almost every later lab is easier to run once the player
can inspect a recipe, and the guidebook-noise question shapes the Field Journal
requirements that several milestones depend on.

The full queue is in `labs/README.md`.

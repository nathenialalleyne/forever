# Design Principles

These are non-negotiable. They are not style preferences; they are the constraints
that keep Forever coherent as many different agents work on it over a long period.

If a ticket appears to require violating one of these, **stop and report the
conflict**. Do not quietly reinterpret the rule. Changing a principle requires a new
ADR that supersedes the relevant decision.

---

## 1. Normal Minecraft building always remains available

A player must never be required to register a construction site, obtain a Builder
specialisation, or complete a quest before placing an ordinary block.

*Why:* Building is the reason most people play Minecraft. Gating it would break the
game to serve the framework. Forever's systems are additive conveniences layered on
top of vanilla freedom, never a toll gate in front of it.

## 2. Discovery may hide possibilities, but not instructions

Mystery before discovery is enjoyable: not knowing that a technique exists is fine.
Once the game expects the player to perform a known process, that exact process must
be inspectable in-game.

*Why:* "You must watch a YouTube video or read a wiki" is a design failure, not
difficulty. Forever must never require external archaeology for ordinary progression.
This principle is what makes the Field Guide mandatory rather than a nice-to-have.

## 3. Learning is permanent; specialisation is configurable

Players keep all mastery progress when changing active specialties. Inactive masteries
retain their levels. Switching costs no XP and requires no re-levelling.

*Why:* Punishing experimentation makes players pick one path early and never explore
the rest of the content. The interesting decision is "what am I doing right now", not
"what did I irreversibly commit to twelve hours ago".

## 4. Repetition is not mastery

Raw counters such as "mine 10,000 stone" or "place 20,000 blocks" must not be the
primary progression path. Mastery rewards breadth, discoveries, projects, technique,
and meaningful accomplishments.

*Why:* A repetition counter is exactly the AFK-grind treadmill Forever exists to
replace. Shipping one would recreate the original problem wearing a new hat. Any
repetition-based XP source requires an explicitly approved ADR.

## 5. Items do not permanently disappear because durability reaches zero

A zero-condition item becomes **broken and unusable until repaired**. It is not
deleted.

*Why:* Losing a named, enchanted, long-used tool to an unnoticed durability bar is
pure feel-bad with no design upside. Forever wants players to form attachments to
equipment over years; that is incompatible with silent deletion.

## 6. Infrastructure turns repeated effort into convenience

Roads, rail, warehouses, workshops, trade routes, and specialists should progressively
remove friction.

*Why:* This is the core reward currency of the whole design. It is the mechanism by
which "the world becomes easier to inhabit" actually happens.

## 7. Settlements evaluate function, not style

Validation may check enclosure, beds, workstations, storage, safety, and space. It must
never enforce an architectural theme, a required blueprint, or an aesthetic score.

*Why:* Style is the player's. A system that awards points for "looks medieval" would
destroy the creative freedom that makes long-lived worlds worth living in.

## 8. Geography matters, but infrastructure eventually reduces geographic inconvenience

The first acquisition of a regional resource may require real travel. Later, the player
may cultivate, preserve, buy, import, or transport it.

*Why:* Regional identity makes the world feel real and makes exploration meaningful.
Permanent geographic tax, however, just means flying back and forth forever. The first
trip is an adventure; the fiftieth is a chore, and Forever should let the player build
something that ends the chore.

## 9. Solo play remains viable

Multiplayer may reward different player niches, but no required progression path may
depend on another human player.

*Why:* Most Minecraft worlds are single-player or two-person. A design that requires a
populated server excludes the majority of its own audience.

## 10. Simulation must not require permanent chunk loading

Unloaded settlements use bounded abstract simulation. NPCs must not physically
pathfind in force-loaded chunks while nobody is nearby.

*Why:* Force-loading is how ambitious mods destroy server TPS. A world with thirty
settlements must not cost thirty times the tick budget. This constrains the settlement
and villager systems from day one, which is why it is a principle and not an
optimisation note.

## 11. No arbitrary off-screen villager death

A villager may die through a physically simulated cause. Abstract simulation of an
unloaded settlement must never randomly announce that a major NPC fell into a quarry.

*Why:* Losing an invested-in character to an invisible dice roll is infuriating and
unfalsifiable. If the player did not see it happen and could not have prevented it,
it should not have happened.

## 12. Mechanics belong in code; balance belongs in data

Thresholds, prices, progression milestones, production rates, slot counts, distances,
and multipliers must be data-driven wherever reasonably possible.

*Why:* Balance changes constantly during playtesting. If every tweak requires a
recompile and a code review, tuning will not happen and the numbers will stay wrong.

## 13. Matcha internals are isolated

No code outside `dev.forever.compat.matcha` may hardcode Matcha scoreboard names,
function paths, advancement IDs, disguised item identities, custom model data, recipe
tricks, or other private implementation details.

*Why:* Matcha is a third-party datapack with no API stability guarantee. Its internals
will change. If those details leak across the codebase, a single Matcha update becomes
a project-wide breakage instead of a one-file fix.

## 14. Every persistent format is versioned

Player, item, villager, settlement, warehouse, shipment, and world-history data must
have explicit schema versions and migration paths.

*Why:* This project's name is a promise about world longevity. Unversioned save data
makes the first schema change a choice between breaking worlds and freezing the design.

## 15. Every player-facing mechanic must be documentable in the Field Guide

A feature is not complete if the player cannot understand it from inside the game.

*Why:* This is principle 2 turned into a shipping requirement. "Documentation later"
reliably means never.

## 16. Accessibility cannot depend on texture or colour alone

Wear states, mastery states, warnings, and statuses need text, tooltip, icon-shape, or
other non-colour cues.

*Why:* Colour-blindness is common, and resource packs legitimately change colours.
Any state encoded only as a hue is invisible to a real portion of players.

## 17. Normal play must not be made worse to justify an upgrade

Warehouses, logistics, construction tools, and masteries provide optional convenience.
They must not intentionally degrade ordinary inventory use or block placement.

*Why:* Manufacturing a problem in order to sell its solution is the cheapest and most
resented form of progression design. Forever earns its upgrades against vanilla
friction that already exists.

## 18. The world save is more important than a feature

Fail safely. Avoid silent data loss, duplication, invalid migrations, and unstable
automatic updates.

*Why:* A missing feature is a disappointment. A corrupted five-year world is the end of
the project for that player. When these two trade off, the save wins every time.

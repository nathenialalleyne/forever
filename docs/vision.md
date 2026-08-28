# Many Roads Home: Vision

**Many ways to play. One world worth keeping.**

## The premise

> Building, exploration, professions, production, trade, and technical engineering
> should be different but interacting routes to a capable long-lived world, so players
> can inhabit that world without feeling compelled to speedrun the same AFK farms,
> trading halls, XP grinders, and Elytra progression.

Many Roads Home is a **modpack** for long-lived Minecraft survival worlds: the kind of
world you return to for years rather than abandon after the ender dragon dies.

The defining rule is:

> Adventure discovers possibilities. Building gives them a home. Professions make them
> repeatable. Engineering scales them. Trade connects them.

Important capabilities need multiple routes. An adventurer may discover a masonry
technique in a ruin; a master mason may develop the same technique through civic
projects; a merchant may import the knowledge; a technical player may design a
production route around it. The routes need not cost the same or take the same time, but
**no broad playstyle may own every essential convenience**.

The pack must not communicate "you are playing incorrectly unless you rush the optimal
farms, maximum enchantments, and Elytra". It must equally not communicate "you are
playing incorrectly unless you explore every dungeon". Replacing a farm treadmill with a
dungeon treadmill would be the same failure wearing different clothes.

## The product is a modpack

The product is a curated set of maintained third-party mods, their configuration,
datapacks, a resource pack, optional blueprint libraries, and a small companion
integration mod used only where existing tools cannot express the design. The companion
mod is not the product. See `docs/modpack-architecture.md`.

## The problem being solved

Late-game Minecraft converges. Regardless of what a player finds fun, the efficient
path runs through the same checklist: an iron farm, a raid farm, a gold farm, an XP
grinder, a villager trading hall, then Elytra and rockets. The checklist is so much
more productive than any alternative that players who dislike it still feel obliged
to complete it before starting the part of the game they actually enjoy.

The result is a strange inversion: players spend their first hundred hours building
things they do not like, to earn the right to build things they do.

## What Forever is not

**Forever is not anti-technical.** Redstone and farm engineering are legitimate,
skilful, enjoyable play. A player who loves designing a super-smelter should keep
doing it. The problem is not that farms exist; it is that farms are so overwhelmingly
dominant that they are effectively mandatory.

The goal is parity, not prohibition. A player who invests a comparable amount of
effort into being a master mason, running a trade route, developing a settlement, or
charting a region should reach a comparable level of abundance.

**Forever is not a total conversion.** It must still feel like Minecraft. It is not
an MMORPG with hotbars of abilities, not a city-management simulator with spreadsheet
screens, and not a quest-book mod that gates the first hour behind fetch tasks.

**Forever does not dictate style.** The player's chosen aesthetic is not a
requirement. A medieval village, a modern city, scattered homesteads, a fantasy
kingdom, an industrial region, and a fishing hamlet must all be equally valid. Systems
validate *function*, never *theme*.

## The intended long arc

The primary long-term progression is not a tech tree. It is the world becoming easier
to inhabit:

1. **The player develops specialties.** Masteries deepen through breadth, discovery,
   technique, and completed projects, not through repetition counters.
2. **Villagers develop careers and institutional knowledge.** They are not vending
   machines. They acquire professions, ranks, techniques, mentors, and apprentices,
   and their knowledge can outlive them if it was taught.
3. **Settlements develop infrastructure.** Registered buildings, workplaces,
   warehouses, roads, rail, and outposts accumulate into a functioning place.
4. **The world becomes easier to inhabit.** Trips that were once expeditions become
   routine, then eventually routine enough to skip.
5. **Repeated inconvenience is replaced by earned convenience.** This is the core
   reward currency of the entire design.

That last point is the design's spine. Forever does not make early Minecraft harder to
make later Minecraft feel good. It takes friction that already exists and lets the
player permanently retire it by building something.

## Why alternatives must be genuinely competitive

If the mason path yields 80% of what a stone generator yields, players will build the
generator and the mason path is decoration. Alternative routes have to actually win in
their own domain: the specialist should out-produce the generic solution for the
things they specialise in, and infrastructure should beat improvisation for anything
done repeatedly.

This is why several locked decisions exist that look like nerfs (Elytra becoming a
glider, rail outperforming flight on fixed routes). They are not nerfs for their own
sake; they are the minimum needed to make an alternative meaningful. Where a
restriction is not needed for that purpose, it does not belong in Forever.

## Solo and multiplayer

Solo play is fully viable. Multiplayer rewards distinct niches (a server's smith,
cartographer, and merchant genuinely benefit each other), but no required progression
path may depend on another human being present.

## What the name means

"Many Roads" is the multiple-routes promise: no single playstyle owns the essential
capabilities, and the way you reach a capability is a choice rather than a checklist.
"Home" is the destination the routes serve, and it carries the longevity commitment the
earlier working title "Forever" named directly. A world here should survive updates,
migrations, and years of play, which is why world-save safety outranks any individual
feature, why every persistent format is versioned, and why this project built an audit
tool before it built gameplay.

The full naming rationale, including rejected alternatives and the outstanding
availability and trademark review, is in `docs/branding/name-and-identity.md`.

## Current status

Mid-pivot from a custom-mod-first prototype to a modpack-first product.

The modpack foundation exists and is verified: Minecraft 26.2, Fabric loader 0.19.3,
Fabric API `NqwNSxwA`, Global Packs `DqrPrUMp`, and official Matcha `E9rngRfK`, with a
dedicated server booting from the exported `.mrpack` and Matcha loading automatically.

A prototype companion mod implementing seven gameplay systems also exists. It has never
been playtested, and its per-file classification is in
`docs/pivot/existing-code-inventory.md`. The next work is compatibility spikes, not
gameplay. See `docs/roadmap.md` and `docs/backlog.md`.

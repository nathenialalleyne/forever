# Animals

## Status
**Concept. NOT implemented.** No bonding record, mount progression, pet behaviour service, animal adapter, or animal history implementation exists. Field Guide work is FVR-200..204. Transportation integration is described here, but a dedicated animals FVR ticket must be assigned before code.

## Purpose
Animals should become dependable parts of a long-lived world through persistent bonding and useful, bounded behaviour. Mounts improve through use, while pets can help with practical roles. The system must avoid turning every animal into a complex RPG companion with a talent tree, equipment screen, and constant maintenance.

## Player experience
A player forms a relationship with an individual animal through care, safe handling, shared travel, and meaningful successful situations. A bonded horse becomes more trustworthy on known terrain and more useful for overland travel. Improvement is visible in handling and reliability rather than a long list of arbitrary attributes.

Pets can have useful behavioural roles such as warning of nearby threats, staying near a protected area, finding a familiar owner, or retrieving a limited tagged object when the environment makes that plausible. The role is simple, legible, and optional. A pet remains an animal, not an all-purpose combat or inventory assistant.

Animals are persistent world entities when loaded. Their identity, bond, home range, and relevant history can survive relocation and restart. Their care remains compatible with solo play and ordinary vanilla animal behaviour.

## Core rules
- Bonding is persistent per animal and owner or household. It records a stable identity, bond state, preferred home, trust context, and bounded history.
- Meaningful care and varied successful use can improve a bond. A raw feed-one-thousand-times counter is not the primary progression measure.
- Mount improvement focuses on handling, confidence, stamina band, terrain familiarity, and route reliability. It must not become unbounded stat inflation.
- A mount's advantages remain contextual. A horse is not a boat, a rail service, or a vertical flight system.
- Bonded mounts can assist transportation when their home, condition, path, and handling state are valid. They may need rest, food, or recovery, but routine care should not be a punitive chore.
- Pet roles are selected or discovered through a small set of behavioural capabilities. Each role has a range, cooldown, target rule, and failure response.
- Useful pet behaviour must be physical or clearly bounded. A warning pet cannot know every danger in an unloaded dimension, and a retrieval pet cannot conjure an item from a remote warehouse.
- Animals may die through visible physical causes. The system does not create arbitrary off-screen deaths from abstract simulation.
- Unloaded animal records may preserve safe hunger, home, bond, and schedule state, but they do not require permanent chunk loading or run complex pathfinding.
- Vanilla breeding and animal interactions remain valid. Forever's bonding and mount services add optional continuity rather than replacing basic survival.
- A moved or transferred animal keeps its identity and bond unless an explicit separation, death, or ownership action occurs.
- The Field Guide explains the practical role and limits of every added behaviour.

Possible bounded roles include:

| Role | Useful behaviour | Hard limit |
|---|---|---|
| sentinel | signals a nearby threat or hazard | local range and cooldown |
| homing companion | returns to a registered home or owner | needs a valid path or safe fallback |
| pack helper | carries a small defined load | no container nesting or warehouse access |
| trail companion | improves confidence on a known route | does not unlock an undiscovered destination |
| fisher or herder aid | helps a declared nearby activity | bounded area and no automatic abundance |

## Data ownership
Animal identity, bond state, owner references, mount progression, pet role, and compact history belong on a versioned persistent entity attachment or component. Physical inventory or carried load remains on the entity or item container.

Role definitions, handling modifiers, route familiarity rules, and care inputs are data resources. World-scoped settlement data may own animal home and service references. Derived lists of bonded animals are rebuildable from entity records.

## Server/client responsibilities
The server validates bonding actions, ownership, mount outcomes, pet behaviour, physical hazards, and unloaded safe state. The client renders bond summaries, role choices, handling feedback, and warnings. It cannot teleport a pet, apply a mount bonus, or claim a retrieval succeeded.

Loaded movement and behaviour remain server-authoritative. Client animations can show a warning or command request, but the server confirms range, path, cooldown, and physical result.

## Dependencies
- FVR-200..204 document bonding, mount improvement, pet roles, and safety limits.
- Transportation consumes mount and route capabilities. Seasons and weather can supply bounded handling situations.
- Settlements provide animal homes, stables, pasture, and outpost references. Villagers may support animal-handler services.
- Mastery provides Rider/Animal Handler and Farmer/Naturalist evidence.
- Any external animal integration must use a capability adapter and preserve identity and safe lifecycle semantics.

## Extension points
An animal provider can expose identity, movement medium, carrying capacity, bond hooks, and safe unloaded state. A pet role can register a bounded behaviour, target rules, cooldown, text description, and accessibility icon.

A future stable or caravan provider can add infrastructure services without turning an animal into a remote storage or teleport system. New mount types must state their medium and trade-offs.

## Failure cases
- A corrupt bond reference becomes unassigned while the animal remains physical and recoverable.
- A player loses ownership or a multiplayer permission changes. The animal enters a visible reassignment state rather than disappearing.
- A pet behaviour has no valid path, target, or cooldown. It fails safely and reports the reason.
- An animal dies physically while loaded. Its history and any institutional route knowledge remain subject to the documented chronicle rules.
- An unloaded record cannot be resolved to a live entity. The system does not invent a death. It marks the record for reconciliation.
- A mount or pet crosses a dimension without a supported transfer. The action is rejected or uses the normal physical game rule.

## Performance constraints
Loaded animal behaviour runs only for tracked entities and uses bounded local searches. A pet action may inspect at most 32 nearby targets and must have a cooldown. Unloaded updates use a fixed record budget, such as 128 animals per server cycle.

The server must not force-load chunks to maintain a bond or pet schedule. Bonded-animal screens return at most 64 entries per page and compact routine history before saving.

## Accessibility and documentation requirements
Bond state, mount condition, role, cooldown, ownership, and failure reasons need text and icons. Warning and command feedback cannot rely on a sound or colour alone. Pet role descriptions must state range, limits, and what happens when the role fails.

The Field Guide must explain persistent bonding, use-based mount improvement, useful but bounded pet roles, physical versus unloaded behaviour, and the fact that animals are not full RPG companions. Controls need keyboard and controller mappings where a command interface exists.

## Non-goals
- This is not a universal animal RPG with deep talent trees, levels, or equipment slots.
- It does not make every animal a fighter, courier, scout, or storage system.
- It does not force complex care routines or replace vanilla breeding.
- It does not teleport animals or grant remote warehouse access.
- It does not create arbitrary off-screen animal death.

## Open balance questions
- Which bond stages are meaningful without looking like a generic level bar?
- How much improvement should a mount gain from varied use before returns flatten?
- Which pet role is useful enough to matter but limited enough to preserve player agency?
- How should animal loss and recovery work in multiplayer without removing physical risk?
- Which external animal systems can provide reliable identity and unloaded safety semantics?

## Planned tests
- Bond, move, rename, transfer, save, reload, and recover animals while preserving identity and ownership.
- Verify mount improvement comes from varied meaningful use and remains bounded by medium and route.
- Exercise every pet role with valid, invalid, cooldown, no-path, and too-far cases.
- Simulate loaded physical hazards and unloaded updates and assert no arbitrary off-screen death.
- Test dimension transfer, permission changes, corrupt references, and missing entities.
- Enforce local search, cooldown, page, and unloaded-update budgets without chunk loading.
- Verify role descriptions, warning cues, and controller or keyboard actions are accessible.

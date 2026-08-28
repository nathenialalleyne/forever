# Exploration and regions

## Status
**Concept. NOT implemented.** No region identity model, discovery ledger, route knowledge, rare-structure outcome system, or exploration projection exists. Field Guide knowledge is covered by FVR-200..204. The supplied backlog does not provide a dedicated exploration ticket, so implementation must be assigned an FVR ID before code.

## Purpose
Exploration should reveal a world with distinct identities rather than turn every biome into a colour swap and every rare structure into a loot box to farm. A region is meaningful when its ecology, culture, economy, and knowledge differ in ways the player can understand and use.

The reward for travel is discovery and possibility. A first encounter may unlock a specialist, technique, route, ingredient, artifact, or project. Repeating the same structure should not be the dominant path to abundance.

## Player experience
A player enters a region and learns more than its biome name. The ecology may reveal a plant, animal habitat, water pattern, or climate. The culture may be represented by settlement practices, migration, architecture as history rather than an aesthetic score, or a specialist community. The economy may expose demand and trade opportunities. The knowledge identity may unlock a technique, map fact, or Field Guide entry.

Rare structures are memorable because they change what is possible. One may reveal a cartographic method, introduce a specialist, provide an artifact with a history, or start a project. It may contain useful physical goods, but repeatable loot is not the main reason to return.

The player can record a discovery, mark a route, bring back a sample, learn a regional practice, and build a local alternative. Geography matters for first access. Cultivation, preservation, import, trade, and infrastructure can later reduce the chore of making the same trip repeatedly.

## Core rules
- A region has four identity dimensions: ecological, cultural, economic, and knowledge. Each dimension has stable tags, a player-facing description, and evidence sources.
- Ecological identity may include biome families, habitats, resources, climate, water, and animal behaviour. It must not be reduced to a single colour or one block list.
- Cultural identity describes people, practices, settlement history, crafts, and migration. It does not award aesthetic points or require the player to copy a visual theme.
- Economic identity describes categories of demand, local supply, specialist services, and useful trade. It remains bounded and does not simulate every household.
- Knowledge identity describes techniques, map facts, discoveries, recipes, region relationships, and what has not yet been understood.
- Discovery is a first meaningful observation, not merely entering a chunk. A player must interact with a landmark, sample, route, specialist, structure, or documented condition that supplies new knowledge.
- Rare structures may unlock possibilities, knowledge, specialists, and artifacts. Their rewards should change the player's options, not simply produce a chest that can be farmed.
- A structure with a unique discovery outcome records a world or player claim so that repeated visits do not produce unlimited copies of the same unique unlock.
- Repeat visits can still provide ordinary materials, scenery, route confirmation, or changed local circumstances. Their marginal value should not exceed the value of making a new discovery everywhere.
- Region knowledge can be shared through maps, mentors, trade, institutional records, and Field Guide entries. The source and confidence remain visible.
- A player may discover something without immediately understanding its use. Discovery can hide possibilities, but once a process is expected, its instructions must be inspectable.
- World generation remains conservative. Dramatic terrain and a restrained structure set are preferable to many interchangeable points of interest.
- The first acquisition of a regional resource should have a real travel cost. Later infrastructure may cultivate, preserve, import, trade, or transport it.

A region summary should make identity concrete:

| Dimension | Example question answered |
|---|---|
| ecological | what lives and grows here? |
| cultural | who uses this place and what do they know? |
| economic | what is scarce, wanted, or worth moving? |
| knowledge | what can be learned here and what remains unknown? |

## Data ownership
World-scoped saved data owns region records, discovery claims, landmark identities, structure outcomes, route facts, and known specialist or artifact references. Player attachments own personal discovery knowledge and map notes where knowledge is not yet institutional.

A region record stores stable coordinates or generated identity, tags, schema version, and bounded summaries. It must not copy every block or entity in the region. Unique structure outcomes use durable claim IDs and migration-safe references.

## Server/client responsibilities
The server determines region identity, validates discovery interactions, grants knowledge, claims unique outcomes, and records routes. The client presents maps, summaries, Field Guide pages, and discovery previews. It cannot infer a discovery from a local biome colour or open a reward chest by prediction.

Map and discovery packets are bounded and can be paged. The client may store a local display cache, but the server decides whether a fact is known, shared, uncertain, or unavailable.

## Dependencies
- FVR-200..204 cover Field Guide pages, map facts, discovery instructions, and knowledge sharing.
- Transportation uses discovered destinations and route facts. Economy uses regional categories, while food and alchemy use ecological resources.
- Villagers and projects can provide cultural knowledge and specialist outcomes. Settlements can import or connect regional services.
- World generation follows the conservative posture in the agent brief. Matcha world content is observed through the isolated adapter.
- Mastery provides Explorer/Cartographer and Farmer/Naturalist evidence.

## Extension points
A region provider can contribute stable ecological, cultural, economic, or knowledge tags with evidence and a display description. A structure outcome provider can register a unique unlock, specialist, artifact history event, or project opportunity with a claim policy.

A map adapter can translate external map or biome data into read-only context. Future content may add regional variants without changing the four-dimensional identity contract.

## Failure cases
- A region definition is missing or malformed. The server retains coordinates and a safe unknown identity rather than inventing tags.
- A unique structure claim conflicts after restart or multiplayer discovery. The first durable claim wins, and the other player receives the remaining documented outcome.
- A structure is removed or regenerated by an external world change. Existing knowledge and artifacts remain, while new claims are disabled until reviewed.
- A stale client requests an undiscovered reward. The server rejects it without revealing hidden contents beyond the documented discovery clue.
- A failed knowledge share preserves the source discovery and retries through an explicit record.
- A region crosses a dimension or generated-data boundary. It receives separate stable identities rather than a guessed merge.

## Performance constraints
Region identity is computed from generated metadata and bounded samples at discovery or registration time. It must not scan every chunk in a biome. A discovery interaction may inspect at most 128 nearby blocks or entities and one registered structure record.

A map page should contain at most 256 region summaries and 512 discovery markers. Unique-claim lookup is keyed by stable structure identity. Background maintenance processes at most 128 stale records per server slice.

## Accessibility and documentation requirements
Region summaries need text for all four identities, a map icon, coordinates or named landmark, discovery status, and the next known action. Unknown, partial, and complete knowledge states require text and icon cues rather than colour alone.

The Field Guide must explain why a discovery matters, how rare structures differ from loot farms, what is known about a region, and how to obtain a known resource through cultivation, preservation, import, trade, or transport. It must not require external wiki research.

## Non-goals
- This is not a biome completion checklist or a map filled with arbitrary markers.
- It does not turn every rare structure into an endlessly farmable loot source.
- It does not require the player to copy regional architecture or award aesthetic scores.
- It does not remove all geographic differences once a player owns a route.
- It does not add a massive structure catalogue or scan the entire world continuously.

## Open balance questions
- What evidence makes a region feel culturally and economically distinct without adding a simulation spreadsheet?
- Which structure rewards should be unique world claims, player claims, or repeatable services?
- How much discovery sharing should be automatic versus earned through maps, mentors, or trade?
- What is the right amount of ordinary loot for a rare structure after its unique knowledge is known?
- How can a conservative structure set still offer enough variety over years of play?

## Planned tests
- Generate regions with distinct ecological, cultural, economic, and knowledge tags and verify all four appear in summaries.
- Assert entering a chunk alone does not grant a meaningful discovery.
- Discover a rare structure, claim a specialist or artifact, revisit it, and verify the unique outcome is not infinitely duplicated.
- Test discovery sharing through maps, mentors, trade, and failed transfers.
- Mutate or remove generated structures and verify known history and artifacts remain safe.
- Bound discovery sampling, map payloads, and stale-record maintenance.
- Verify Field Guide instructions are complete once a process becomes expected and do not depend on colour or external references.

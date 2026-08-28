# Transportation

## Status
**Concept. NOT implemented.** No route registry, journey service, vehicle capability layer, freight integration, or journey-skipping implementation exists. Field Guide documentation is FVR-200..204, while settlement links are part of FVR-400..405. A dedicated transportation ticket must be assigned before code.

## Purpose
Transportation makes geography meaningful at first and manageable later. Different modes should win in different contexts instead of allowing one late-game answer to dominate every trip. Infrastructure turns a repeated expedition into a reliable route and eventually into a journey that can be skipped.

The system treats travel as a physical activity with visible routes, hazards, capacity, and service quality. It does not make the world smaller by erasing distance from the beginning.

## Player experience
Walking is the default for local work, scouting, and short trips. Horses are the practical road and overland option, especially when a player has built or surveyed a safe route. Boats serve rivers, coasts, and oceans where water is the geography rather than an obstacle.

Rail is for repeated fixed routes and freight. A registered line between validated stations can move a player or shipment consistently, with capacity and service limits that make construction worthwhile. Elytra is a glider for scouting, vertical traversal, and emergency crossings. Unlimited rocket flight must not become the dominant answer to every long journey.

Later, a player can skip a journey only after both destinations are discovered, the route has been physically travelled, suitable infrastructure has been built and registered, and the service has been validated. The skipped result represents a route the world knows how to operate, not teleportation to an unknown location.

## Core rules
- Walking remains valid everywhere ordinary Minecraft permits it. Local routes should not require registration.
- Horses improve road and overland travel when bonding, terrain, road quality, and handling support them. A horse is not a universal mount for water or vertical flight.
- Boats are favoured on rivers, coasts, and oceans. Inland shortcuts still require the player to find or build a practical water connection.
- Rail represents repeated fixed service. It requires explicit station-to-station registration, bounded graph verification, and a physical line that remains serviceable.
- Rail may carry freight when station capacity, cargo handling, and route registration support it. It must beat Elytra for a repeated fixed route through reliability, capacity, or service time.
- Elytra is a glider, scouting tool, and vertical traversal method. Rocket use and flight distance require data-defined limits so that established rail retains a meaningful niche.
- Route records name markers, mode, edges, hazards, service capability, discovery state, and validation revision. They do not rely on a continuous world scan.
- A journey can be skipped only when both endpoints are discovered, the player has physically travelled the route, infrastructure is built and registered, and a validated service exists, following ADR 0018.
- Skipping consumes a visible service cost or time and may require a destination receiving state. It cannot produce goods or discoveries that the player has not physically established.
- A broken bridge, removed rail, missing station, or changed dimension invalidates the affected service. Walking or another valid mode remains available when possible.
- Route registration is separate from ordinary building. A player can build an attractive path without registering it, and registration evaluates function, not style.
- Travel rewards new knowledge, safe shortcuts, and route confidence. A player should not be required to repeat the same physical trip forever once a good service exists.

Mode selection is contextual:

| Mode | Strong context | Limitation |
|---|---|---|
| walking | local errands and first scouting | slow over long distance |
| horse | roads and overland freight | needs care and suitable terrain |
| boat | rivers, coasts, oceans | depends on water continuity |
| rail | repeated fixed routes and freight | construction and station requirements |
| Elytra | scouting, vertical movement, emergency crossing | not the best fixed-route service |

## Data ownership
World-scoped saved data owns discovered destinations, route markers, registered edges, station capabilities, validation revisions, service costs, and journey history. Mount and boat state remains on entities or item records as appropriate.

A route graph and travel estimates are derived caches keyed by infrastructure revision. The source of truth is the explicit marker and registration record plus the physical infrastructure checks used by the validator. Every persistent record has a schema version.

## Server/client responsibilities
The server validates discoveries, route evidence, vehicle context, infrastructure, cargo, and journey skipping. The client renders maps, route previews, mode comparisons, station status, and failure reasons. It cannot claim a trip was travelled or a rail line is complete.

A client may suggest a route from server-supplied graph data. A skip request contains endpoints, route ID, mode, and revision. The server rechecks all prerequisites before moving the player or shipment.

## Dependencies
- FVR-200..204 document mode strengths, route discovery, registration, and journey-skipping prerequisites.
- FVR-400..405 provide settlement nodes, outposts, station buildings, and functional validation.
- Logistics depends on route capacity and station receiving buffers. Animals supplies mount bonding and handling capabilities.
- ADR 0013 defines the walking, horse, boat, rail, and Elytra roles. ADR 0018 defines later journey skipping.
- Mastery provides Explorer/Cartographer and Rider/Animal Handler evidence, while economy can fund infrastructure.

## Extension points
A vehicle adapter can declare a mode, terrain or medium, capacity, handling, and service constraints. A rail adapter can expose station and freight capabilities without changing route semantics. A future portal service can add a distinct dimension edge only after an explicit decision.

A route validator may consume stable block tags, station capabilities, or surveyed markers. New modes must state the repeated-use niche they serve and the trade-off that stops them becoming universally dominant.

## Failure cases
- A route references a missing marker, station, or dimension. It becomes unavailable without deleting discovery history.
- Physical infrastructure changes after validation. The service pauses and explains the segment requiring revalidation.
- A journey-skip request has stale discovery or route data. It is rejected before moving the player or cargo.
- A horse, boat, or rail vehicle is lost during a physical trip. The player retains route knowledge, while cargo and equipment follow their own physical failure rules.
- A client disconnects during a skip. The server commits either the pre-trip state or the complete validated arrival, never a duplicate player or shipment.
- An Elytra or rocket rule conflicts with a future adapter. The base capability falls back to gliding and reports the unsupported enhancement.

## Performance constraints
Route creation and validation are explicit operations. A validation may inspect at most 16,384 blocks per segment by default and 131,072 for a whole registered route, with the result cached by infrastructure revision.

The route graph should cap a client page at 256 markers and 512 edges. Journey evaluation reads the cached path and endpoint state. No route system may continuously scan every rail, road, or loaded chunk, and no off-screen travel may force-load chunks.

## Accessibility and documentation requirements
Maps and route lists need text names, mode labels, distances, service status, and an explanation of every unmet skip prerequisite. Mode distinctions require icons and text, not colour alone. A player must be able to inspect the exact route used by a skip.

The Field Guide must explain when each mode is useful, why Elytra is a glider and scouting tool, how rail handles fixed routes and freight, and all four prerequisites for journey skipping. It must include a walking fallback for every required travel task.

## Non-goals
- This is not a fast-travel menu available for undiscovered destinations.
- It does not make Elytra useless or remove gliding and scouting.
- It does not make one vehicle optimal for land, water, freight, and vertical movement.
- It does not continuously scan or force-load the world to maintain route knowledge.
- It does not require a player to register ordinary paths before walking on them.

## Open balance questions
- What service cost makes journey skipping feel earned without becoming a repeated tax?
- How much route damage or maintenance should invalidate rail before it becomes busywork?
- How should horse bonding and road quality compare with walking on mixed terrain?
- What rocket and glide limits preserve Elytra's joy while protecting fixed-route rail?
- Which route facts must be physically travelled before a player can skip a journey?

## Planned tests
- Travel and register walking, horse, boat, and rail routes with valid and invalid terrain and station states.
- Verify Elytra retains gliding, scouting, and vertical traversal while rail wins a repeated fixed-route comparison.
- Assert journey skipping fails for undiscovered, untravelled, unregistered, or unvalidated routes.
- Mutate a route after validation and verify cached results invalidate safely.
- Crash during physical travel and journey skipping and assert no duplicate player, cargo, or discovery state.
- Bound route validation volumes, graph payloads, and server work without chunk loading.
- Test route and mode descriptions with keyboard navigation and non-colour status cues.

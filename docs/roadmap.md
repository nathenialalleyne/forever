# Roadmap

The roadmap is deliberately conservative. It separates the completed foundation from
future gameplay so that planned systems are not mistaken for shipped behaviour.

## Current scope: complete

### M0: Project foundation

**Status: Complete.** The repository has the pinned Fabric 26.2 project identity,
common/client source separation, root build and test baseline, and initial dedicated
server GameTest scaffolding. See FVR-001 through FVR-003 in `docs/backlog.md`.

### M1: Matcha acquisition, pinning, and audit tooling

**Status: Complete as tooling foundation.** The project has the contract for fetching
and installing the pinned Matcha release and for testing a standalone audit tool with
fixtures. The first official audit and system classification are deliberately the next
step, not retroactively claimed as complete. See FVR-010 through FVR-017.

### M2: AI-readable architecture and design documents

**Status: Complete.** Vision, non-negotiable principles, architecture boundaries,
dependency policy, AI workflow, save-safety guidance, roadmap, and a structured backlog
are documented. See FVR-004 and FVR-005.

No gameplay was implemented in M0, M1, or M2. This is the current foundation status.

## Future milestones: not started

### M3: Matcha audit and classification

**Status: Not started. Next scope.** Produce the first official deterministic audit,
classify Matcha systems as keep, extend, override, replace eventually, or undecided,
and define stable adapter boundaries without leaking Matcha internals into Forever.

### M4: Equipment and condition vertical slice

**Status: Not started.** Establish item identity and condition data, broken-item
behaviour, field and workshop repair, and the reforge path. Durability exhaustion must
not permanently delete an item. Persistence and duplication GameTests are part of the
vertical slice.

### M5: Field Guide

**Status: Not started.** Build the in-game explanation framework, searchable entries,
early Matcha progression documentation, recipe-viewer integration, and process guidance
such as campfire work. This milestone makes known mechanics inspectable in game.

### M6: Mastery

**Status: Not started.** Add the mastery registry and versioned player state, one Focus
and two Supporting slots, rest-based switching, and the first Prospector prototype.
Progress is permanent, and repetitive-action XP requires an approved ADR.

### M7: Settlement

**Status: Not started.** Add charters, registered buildings, functional residence
validation, the settlement building graph, existing-village import, and a migration
prototype. Simulation must remain bounded and must not permanently load chunks.

### M8: Mason vertical slice

**Status: Not started.** Build the masonry workshop and Mason career path through
Apprentice, Journeyman, and Master, with a bulk block catalogue and the Obol purse.
Persistence and duplication tests are required before release.

### M9: Storage and logistics

**Status: Not started.** Add the warehouse controller, bounded inventory indexing,
search terminal, Traveler's Cache, and the rule that containers can never be nested.

### M10: Transportation and routes

**Status: Not started.** Add explicit route surveying and registration, bounded rail
verification, cached route results, and journey skipping only after its discovery,
travel, infrastructure, and service prerequisites are met.

## Later seasons

These themes are intentionally later and are not started: generic food traits, player
shops, apprenticeships, workers, logistics expansion, roads, rail expansion, journey
skipping, seasons, regional economies, animal bonding, and the world chronicle. They
will receive detailed tickets only after their dependencies and design decisions are
ready. No item in this section should be implemented as an assumption about the current
scope.

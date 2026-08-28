# Matcha substrate

## Status
**Concept. NOT implemented.** No Matcha adapter, runtime classifier, or gameplay bridge exists in the current codebase. The implementation work is tracked by FVR-010..017 and must remain documentation and audit work until those tickets are taken up.

## Purpose
Forever starts from the official Matcha Flavoured pack rather than forking or replacing it immediately. Matcha supplies the initial gameplay substrate that makes a Forever world feel like the selected baseline, while Forever adds durable progression, infrastructure, and social systems around it.

This document describes the system boundary. It does not describe the private datapack mechanics themselves. Adapter mechanics and identifier mappings belong in `docs/compatibility/matcha.md`. The evidence-gathering workflow belongs in `docs/matcha-audit/`.

## Player experience
A player installs the pinned Matcha release and can play its intended baseline without learning which implementation details are Matcha-owned. Forever presents stable concepts such as an ingredient, a region discovery, or a tool capability rather than asking the player to understand scoreboard names or function paths.

When a Forever system changes how a Matcha-provided thing behaves, the change should be legible in the Field Guide. A player should be able to tell whether Forever kept a behaviour, extended it, changed a boundary case, or plans to replace it later. A missing optional bridge should produce a clear unavailable status, not a silently broken recipe or an invented substitute.

## Core rules
- The pinned starting substrate is Matcha Flavoured 1.12, project `QI0EmgZ1`, version `E9rngRfK`, for Minecraft 26.2. The archive and checksum are authoritative through the repository lock file.
- ADR 0001 governs acquisition and pinning. Forever does not fork Matcha for the initial foundation milestone.
- Each observed Matcha behaviour receives one classification from `keep`, `extend`, `override`, `replace eventually`, or `undecided`, as defined by ADR 0003.
- `keep` means Forever relies on the behaviour as supplied. `extend` adds a Forever capability without changing the Matcha contract. `override` deliberately changes the outcome for a documented design reason. `replace eventually` records a migration direction while preserving the current substrate. `undecided` forbids assumptions until evidence or a design decision exists.
- ADR 0002 and design principle 13 impose isolation. Only `dev.forever.compat.matcha` may know Matcha scoreboard names, function paths, advancement IDs, disguised identities, custom model data, recipe tricks, or other private details.
- Core systems consume stable Forever concepts and capability results. They must not branch on a raw Matcha identifier, even when that is the quickest way to make a prototype work.
- The adapter must detect the installed archive identity before enabling mappings. An unknown or modified release is incompatible until an explicit compatibility record exists.
- Matcha-derived material remains separate from original Forever code and assets. License notices and attribution are part of the release process.
- The world save outranks compatibility convenience. An adapter update must preserve or migrate recorded Forever state, and must fail closed rather than reinterpret ambiguous old data.
- Audit evidence is versioned. A classification without a source observation, expected outcome, and confidence is not ready to drive gameplay code.

## Data ownership
The adapter owns Matcha version metadata, capability detection, private identifier maps, and audit evidence references. The Forever core owns all Forever player, item, settlement, logistics, and chronicle state. It stores stable concept identifiers, not Matcha implementation strings.

Compatibility records need explicit schema versions and a source release identity. Mappings should be immutable for a given adapter version. A migration record may add a new mapping or mark a capability unavailable, but must not silently rewrite an old world without a tested migration path.

## Server/client responsibilities
The server loads the pinned identity, validates adapter capabilities, and resolves all Matcha interactions. The client receives bounded, player-facing summaries such as a capability name, availability, and Field Guide explanation. It never decides whether a Matcha action succeeded.

No common or server class may import client classes. No core class may reach into Matcha resources. Client screens may render adapter-provided labels and warnings, but they must not embed private identifiers or infer compatibility from a resource-pack file.

## Dependencies
- The pinned Matcha archive and `matcha.lock.json` are acquisition inputs, not Gradle dependencies.
- `docs/compatibility/matcha.md` is the human-readable adapter contract and identifier boundary.
- `docs/matcha-audit/` records the repeatable audit workflow and evidence.
- FVR-010..017 cover acquisition, pinning, detection, mapping, isolation checks, and audit foundations.
- Field Guide work in FVR-200..204 must explain any player-visible compatibility state.
- Future core systems depend on stable capability interfaces, never on the Matcha archive directly.

## Extension points
A version detector can accept a new pinned Matcha release only after checksum, licence, and audit review. A capability registry can expose stable concepts such as `ingredient_traits`, `regional_knowledge`, or `provided_recipe` without exposing implementation identifiers.

An adapter mapping may offer keep, extension, or override hooks with an explicit precedence order. A future replacement can implement the same stable contract and coexist during migration. Optional adapters for other content must use the same boundary pattern and cannot turn Matcha internals into a general-purpose API.

## Failure cases
- A missing archive, wrong checksum, wrong Minecraft version, or unsupported release disables Matcha integration and reports an actionable error.
- A changed private identifier produces an audit mismatch. The adapter does not guess from a similarly named function or item.
- A classification is absent or marked `undecided` at a required decision point. The affected feature remains unavailable or uses the documented baseline, never a silent override.
- A partially loaded resource set must not leave half of a mapping active. Capability registration is atomic per adapter load.
- A failed migration preserves the old saved data and records the blocked version. It must not delete an item, settlement, shipment, or chronicle entry.
- If a client lacks a cosmetic resource, the server state still remains correct and text labels provide the fallback.

## Performance constraints
Version detection and audit checks run at startup or an explicit maintenance command, not every tick. Runtime mappings are immutable lookup tables cached by capability and release identity.

A normal gameplay event may make at most one bounded adapter query. No adapter call may scan all loaded entities, all chunks, or all Matcha files. Audit output must cap individual evidence payloads at 1 MiB and total report size at 20 MiB unless a maintenance command explicitly requests more.

## Accessibility and documentation requirements
The Field Guide must show the Matcha release identity, the status of each player-facing capability, and the practical consequence of keep, extend, override, replace eventually, or undecided. Status must use text and icons, not colour alone.

Every mapped mechanic needs an in-game explanation once Forever expects the player to use it. Error messages name the missing release, capability, or migration rather than exposing an opaque internal identifier. Documentation must distinguish Matcha-provided behaviour from Forever behaviour and include attribution where required.

## Non-goals
- This system is not a fork of Matcha.
- It is not a promise to preserve every private Matcha implementation detail forever.
- It is not a general script execution engine or a way for core systems to call arbitrary datapack functions.
- It does not decide the balance of mastery, economy, seasons, or equipment.
- It does not make Matcha mandatory for every future Forever installation once stable replacement contracts exist.

## Open balance questions
- Which Matcha capabilities must be `keep` for the first playable Forever slice, and which can remain `undecided`?
- How much of a Matcha change warrants an adapter release versus a world migration?
- Should a known compatible but unpinned patch be refused outright or offered as a read-only audit result?
- Which player-facing terminology best communicates an override without making the player learn mod boundaries?
- How much audit evidence is enough to move a capability from `undecided` to a committed classification?

## Planned tests
- Verify the pinned project ID, version ID, Minecraft version, licence record, and checksum against the lock file.
- Scan compiled source and data for Matcha-private identifiers outside `dev.forever.compat.matcha`.
- Load an exact archive, a missing archive, a wrong-checksum archive, and an unknown release.
- Test each classification with a fake stable capability and assert deterministic precedence.
- Interrupt adapter loading after each mapping group and verify no partial capability registration remains.
- Run a migration fixture containing old stable concept IDs and assert that state is preserved or a clear migration block is reported.
- Bound audit report size and verify repeated runtime queries use cached mappings.

# Matcha adapter boundary

This document specifies the compatibility boundary around Matcha. It is an adapter
contract, not a description of Matcha's player-facing systems. The system-by-system
behaviour review belongs in `docs/systems/matcha.md` and in the generated audit
reports.

## Why Matcha is isolated

Matcha Flavoured is a third-party datapack and resource-pack archive. It has no
stable Java API, no mod entrypoint, and no promise that its internal names or command
layout will remain unchanged. Matcha is also third-party material that must remain
separate from Forever's original code and content.

Design principle 13 therefore applies directly: no code outside
`dev.forever.compat.matcha` may hardcode Matcha scoreboard names, function paths,
advancement IDs, disguised item identities, custom model data, recipe tricks, or
other private implementation details. [ADR 0002](../adr/0002-isolate-matcha-behind-adapter.md)
records the accepted decision to isolate Matcha behind an adapter. The practical
reason is change containment. An update to Matcha should require a new adapter
profile and its tests, not a search-and-replace
across Forever Core, the client, and every persistent system.

The adapter is not a second source of gameplay truth. Forever Core owns Forever's
stable state and outcomes. The adapter recognises Matcha signals, translates them
into stable Forever concepts, and reports what it could or could not support. If a
signal cannot be identified with evidence, the adapter refuses to guess.

## Boundary: what may cross it

`dev.forever.compat.matcha` is the only package that knows Matcha's internal
identifiers. The boundary is crossed in two directions:

| Direction | Allowed data | Explicitly excluded |
|---|---|---|
| Matcha and the server into the adapter | Verified pack metadata, the locked archive digest, known file fingerprints, exact marker scoreboard and advancement observations, vanilla item signatures, recipe and loot observations, and runtime command or event observations | Texture pixels, display names, colours, fuzzy name matches, or an unverified interpretation of a command sequence |
| Adapter into Forever API and Core | A support snapshot, stable Forever concept IDs, normalised behaviour inputs and outcomes, capability flags, scoped compatibility decisions, migration plans, and opaque audit evidence references | Matcha scoreboard names, function paths, advancement IDs, raw recipe tricks, custom model-data values, Matcha file paths, third-party classes, or Matcha-specific mutable state |

The following rules make the table enforceable:

1. A core caller asks for a stable capability or concept, such as a translated
   `forever.item.technique_plan_scroll`, rather than asking for a Matcha objective
   by name.
2. A translated event contains stable fields such as actor, target, context,
   outcome, and evidence reference. It does not contain a raw Matcha identifier.
3. Audit tooling may preserve raw paths and identifiers in the Matcha audit output,
   but that output remains adapter/audit material. Core receives only a profile ID
   and an opaque evidence ID when provenance is needed.
4. Matcha-specific records are versioned inside the adapter. Core persistence uses
   Forever schema versions and stable concepts, never a Matcha-shaped save blob.
5. Client code consumes synchronised Forever projections. It must not inspect Matcha
   resource-pack names, custom model data, or scoreboards to decide what happened.

This is a package boundary as well as a conceptual boundary. A future implementation
may split the adapter into small internal classes for detection, identity mapping,
behaviour mapping, overrides, audit provenance, and migration. None of those
internal types become a dependency of an unrelated core system.

## Matcha is a datapack, not a mod

Matcha must not be detected with a mod-version API. There is no valid
`FabricLoader.isModLoaded("matcha")` check that can establish Matcha's version, and
Matcha must not be added as a Gradle or runtime Java dependency. Detection is a
layered decision made by the adapter. Each layer is exact enough to support or
reject a known profile.

### Detection signals

| Signal | What the adapter checks | What it proves, and what it does not prove |
|---|---|---|
| `pack.mcmeta` | Parse the pack metadata at the normalised pack root. Validate its shape, declared pack format, and a canonicalised description or declared marker where present. | A compatible pack format and plausible identity. Description text alone never proves that the archive is Matcha. |
| Known file fingerprints | Compare a profile's expected relative-path inventory and hashes for stable, discriminating files. Use exact paths and bytes, not a broad substring search. | That the content matches a known profile closely enough for the fingerprint set. A path existing by itself is insufficient. |
| Marker scoreboards and advancements | After the datapack is loaded in a disposable or target server, query exact marker objective names and advancement IDs defined by the profile. Record whether markers are present, absent, or not yet loaded. | Runtime confirmation for the locked profile. Marker absence can mean that the pack has not run yet, so it is a reason to remain unconfirmed, not permission to infer a version. |
| Locked SHA-256 | Hash the acquired archive and compare it byte-for-byte with the SHA-256 in `matcha.lock.json`. The current baseline pins Matcha Flavoured `1.12`, version ID `E9rngRfK`, archive `Matcha_Flavoured_1_12.zip`. | The authoritative identity of the acquired archive. A different digest is an unexpected version, even if the pack looks similar. |

For the current lock, the expected archive digest is
`6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248`. A future
lock change must update the profile and its audit evidence together.

The normal sequence is:

1. Locate the canonical acquired archive or an explicitly identified installed
   pack. Normalise extraction safely and reject path traversal.
2. Verify the archive SHA-256 against `matcha.lock.json` before treating any
   profile as supported. Record the lock version and digest.
3. Parse `pack.mcmeta` and compare the known file fingerprint set for that locked
   profile.
4. Confirm exact scoreboard and advancement markers at runtime when the server
   has loaded the pack.
5. Enable only the profile for which the required checks agree. Store the reason
   and evidence for the decision.

A directory copied from somewhere else may be fingerprinted, but without the locked
archive provenance it is `present but unverified`, not the supported pinned release.
The adapter must not use texture appearance, lore, translated display text, or a
partial path match as a version detector.

### Detection results

The adapter exposes a stable status, not a raw detector exception. The conceptual
states are:

- `ABSENT`: no Matcha pack was found. The adapter supplies no Matcha capability.
- `SUPPORTED`: the locked archive and the required metadata, fingerprints, and
  runtime markers agree.
- `PRESENT_UNVERIFIED`: Matcha-like content exists, but the lock or a required
  confirmation is missing. No version-specific mapping is enabled.
- `UNSUPPORTED_VERSION`: content is present but its digest or fingerprints do not
  match a known profile.
- `MALFORMED`: metadata or archive structure cannot be safely read.
- `FAILED_SAFE`: detection or mapping failed after startup. The adapter remains
  disabled and reports an actionable diagnostic.

`PRESENT_UNVERIFIED`, `UNSUPPORTED_VERSION`, and `MALFORMED` are distinct from
`ABSENT`. They should be visible to an operator so a missing pin is not mistaken for
an intentional installation without Matcha.

## Item identity translation

Matcha disguises gameplay items as vanilla items and distinguishes them with custom
model data and related item data. A texture or display name is not an identity. A
resource pack can be replaced, language can change, and another datapack can use the
same vanilla base item.

The adapter therefore maintains a versioned identity table for each supported
Matcha profile. A profile-local signature contains the exact vanilla base item, the
custom-model-data representation used by that profile, and only the additional
components required to disambiguate the item. The table maps that signature to a
stable Forever concept ID. The raw signature remains inside the adapter.

A stable concept has these properties:

- It is a namespaced Forever ID, for example `forever.item.field_guide` or
  `forever.item.coin_purse`, not a Matcha name.
- Its meaning survives a resource-pack replacement and a Matcha profile update.
- Its mapping is versioned and testable. A changed custom model-data value produces
  a profile change, not an unnoticed reinterpretation.
- It preserves ordinary ItemStack facts such as count and applicable vanilla
  components. Translation must not silently discard enchantments, custom names,
  or other data that the stable concept does not own.
- It rejects unknown signatures. An unknown vanilla item remains a vanilla item or
  an explicitly preserved opaque item record. It is never guessed from its texture,
  colour, lore, or approximate model-data value.

The planned concept vocabulary includes items such as the Field Guide, Settlement
Charter, Surveyor's Tool or Deed, Coin Purse, Traveler's Cache, Shipping Manifest,
technique or plan scroll, and the optional Builder's Toolkit. These are stable
Forever concepts to which an audited profile may map. Their presence, signature,
and behaviour must still be established by the audit. Names in this document are
not evidence that a particular Matcha release implements each concept.

A translation result should carry the stable concept, the original vanilla stack
for safe fallback, the profile ID, and an opaque audit evidence ID. It must not
expose the custom model-data value to a caller outside the adapter.

## Behaviour mappings

Matcha behaviour is encoded in datapack functions, commands, scoreboards,
advancements, recipes, loot, tags, and interactions. The adapter converts those
signals into stable inputs and outcomes. It does not copy an entire function into
core code and it does not infer intent from a file name.

A behaviour mapping is a data entry scoped to a locked profile and includes:

- the stable Forever capability or concept affected;
- the trigger kind, such as interaction, recipe result, advancement, objective
  transition, scheduled function, loot result, or item translation;
- the normalised input and outcome shape;
- ordering or precondition requirements when the source commands make them
  observable;
- an evidence reference to the exact source and, where available, a runtime trace;
- the fallback when the mapping is unavailable.

Typical mapping domains are:

| Source signal | Stable adapter result | Safe fallback |
|---|---|---|
| A known objective or advancement transition | A bounded progression or tutorial event with a stable concept ID | Do not emit the event; retain ordinary vanilla progression |
| A function sequence that changes an item or player state | A typed state transition with validated inputs | Preserve the original stack or leave the state unchanged |
| A recipe, tag, or loot rule | A normalised recipe, substitution, or loot observation | Do not remove the vanilla recipe or loot unless an explicit supported override says so |
| A food, healing, damage, or death signal | A typed outcome with actor and context | Let vanilla handling stand when the Matcha rule is not confirmed |
| A marker scoreboard used as a capability signal | A support or capability status update | Keep the capability unavailable rather than treating a missing marker as false gameplay state |

The adapter profile is allowed to know the raw sequence and identifiers needed to
make this translation. Forever Core sees only the normalised contract. All values
that affect balance remain data-driven and versioned, in line with design principle
12.

## Compatibility overrides

An override is an explicit, narrow rule for a known conflict between a supported
Matcha profile and a Forever capability. It is not a general-purpose patch system and
it is not a reason to bypass the audit.

Every override is keyed by the exact Matcha profile ID and the stable Forever concept
or capability it affects. It records its rationale, precedence, evidence reference,
owner, tests, and removal condition. Useful override classes include:

- identity overrides when a profile signature must be translated before core sees it;
- trigger ordering overrides when a datapack event and a Forever event would both
  apply to the same action;
- recipe or loot overrides that prevent an accidental duplicate or an unsafe removal;
- tutorial or advancement presentation overrides that suppress duplicate messaging
  while keeping the underlying progression observable;
- persistence overrides that read a known legacy representation into a versioned
  Forever record without deleting the original on failure.

Override precedence is deterministic. A supported profile's specific override wins
only for the declared concept and trigger. It cannot change unrelated vanilla
behaviour, disable normal building, or make an optional Forever feature a required
Matcha dependency. An override that depends on an unverified version is disabled.

The audit report is the source for deciding whether to keep, extend, override, or
replace a Matcha behaviour. No override should be written merely because a source
file looks inconvenient. Trace the behaviour first.

## Audit metadata and provenance

The adapter and its audit tool must make every translation explainable. At minimum,
metadata for a profile or mapping records:

| Metadata | Purpose |
|---|---|
| Lock version, Matcha version ID, archive filename, and SHA-256 | Proves which input was inspected |
| `pack.mcmeta` result and pack format | Records pack-level identity evidence |
| Fingerprint set and result | Shows which files matched or differed |
| Runtime marker observations | Distinguishes source inspection from server confirmation |
| Source kind, relative path, and exact locator | Lets a reviewer return to the command, recipe, advancement, or data entry |
| Observed value and runtime context | Preserves what happened without mixing interpretation into the fact |
| Inferred purpose, confidence, reviewer, and classification | Makes interpretation and uncertainty explicit |
| Adapter profile and mapping schema versions | Makes later migration and comparison possible |

Generated reports may contain raw Matcha paths and identifiers because they are audit
artifacts. Those values do not cross into core APIs, core saves, or client gameplay
logic. A core-facing provenance field is an opaque evidence ID plus the verified
profile ID. Reports should be deterministic for the same locked input. If a report
records wall-clock time for provenance, that field must not affect content hashes or
semantic comparisons.

## Future migration helpers

The adapter is also the home for migration helpers needed when Matcha changes or when
Forever eventually replaces an audited Matcha mechanic. These helpers should be
planned around stable concepts, not around string similarity.

A migration helper may:

1. read a known old profile using its profile-specific identity and behaviour tables;
2. translate recognised items and player or world state into a versioned Forever
   schema;
3. produce a dry-run report showing recognised, unchanged, conflicting, and unknown
   records;
4. apply only after validation, preserving the original input and writing a
   migration marker;
5. leave unknown or conflicting data intact in a safe opaque form rather than delete
   it; and
6. support a tested stepwise path from Matcha profile data to a later native Forever
   representation.

Migration must be restart-safe and bounded. A failed conversion must not leave a
half-translated inventory or world record. It must not silently merge two concepts
just because their display names are similar. Every persistent Forever destination
uses its own schema version and migration path, as required by design principle 14.

## Graceful degradation

The world save outranks the feature. The adapter must fail closed for unsupported
Matcha content and fail safe for an absent pack.

| Situation | Adapter action | Player or operator result |
|---|---|---|
| Matcha is absent | Return the no-op Matcha capability and `ABSENT`. Do not register Matcha mappings or require Matcha resources. | Vanilla play and unrelated Forever systems continue. |
| The locked Matcha `1.12` archive is confirmed | Enable only the audited profile and its tested mappings. | Supported compatibility is available and attributable to the lock. |
| The SHA-256 differs or a known fingerprint is unexpected | Return `UNSUPPORTED_VERSION`; do not guess or partially enable the old profile. | Vanilla-safe behaviour remains. The diagnostic names the expected profile and the observed digest without attempting a destructive fix. |
| Pack metadata is malformed or markers are unavailable | Return `MALFORMED` or `PRESENT_UNVERIFIED`; retry only through an explicit lifecycle check. | No crash and no version-specific conversion. An actionable warning points to the audit or pack-loading problem. |
| Matcha is removed after Forever data exists | Keep stable Forever data and preserve unrecognised source records. Disable only the Matcha capability. | The world remains loadable. A feature may be unavailable, but items and records are not silently deleted. |
| An adapter mapping throws or returns invalid data | Reject that mapping, log context, and keep the no-op or vanilla fallback. | One unsupported mapping cannot take down the server or corrupt the save. |

There is no automatic "closest version" mode. A new Matcha archive requires a new
fingerprint profile, audit evidence, compatibility decision, and tests before its
mappings can be enabled.

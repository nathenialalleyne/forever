# Optional adapter contracts

Optional integrations are compatibility boundaries, not dependencies of Forever
Core. The initial adapter families are cooking, season and weather, rail,
animal and horse, storage, and world generation. The same contract applies to any
later third-party integration.

## Capability-interface pattern

Each optional integration is represented by a small capability interface whose
inputs and outputs use Forever-owned types. The core system asks whether a
capability is available and then calls the abstraction. It never asks an adapter for
a third-party object.

| Capability family | Core-facing question | Required neutral fallback |
|---|---|---|
| Cooking | Can this cooking context provide a supported preparation, quality, or processing result? | Keep vanilla cooking behaviour. Return no external modifier or recipe. |
| Season and weather | What climate context is supported for this position and time? | Use the Forever climate default or vanilla weather semantics. Do not impose season gates. |
| Rail | Can this station or marker participate in a supported bounded route graph? | Return no external rail capability. Existing ordinary play remains usable. |
| Animal and horse | Which supported attributes or behaviours are available for this animal? | Use vanilla entity behaviour and attributes. |
| Storage | Can a supported external inventory or transfer endpoint be addressed? | Report no external storage and leave ordinary inventories unchanged. |
| World generation | Is a supported generator or feature source present for this world? | Generate no optional content. Do not make world creation depend on it. |

An interface must have a real caller and a working implementation or no-op
implementation. Do not add a speculative interface solely to reserve a package name.
The interface should be narrow enough that a missing integration has an obvious
neutral answer and no caller needs to catch an adapter-specific exception.

The core-facing lifecycle is:

1. Discover the optional provider at startup or at the provider's safe lifecycle
   boundary.
2. Validate its identity and supported version before registering it.
3. Publish an immutable capability snapshot with support level, confidence, and
   diagnostics.
4. Let server-authoritative core code call the interface using bounded, validated
   requests.
5. Return a normal result or a documented neutral result. Do not turn absence into
   an exceptional control path.

Adapters may have richer internal implementations, but the capability interface is
the only contract core relies on.

## Absence must be a working state

Every capability has a no-op or default implementation that is safe to call. It is
not acceptable for the absent case to return `null`, throw an unsupported-operation
exception, or leave a static field uninitialised.

The no-op must be semantically neutral, not merely quiet. Examples:

- a missing cooking adapter does not remove vanilla furnace or campfire recipes;
- a missing season adapter does not make crops or travel fail because no season was
  reported;
- a missing rail adapter does not crash route inspection and does not claim that an
  unverified route exists;
- a missing animal adapter leaves vanilla pathfinding and attributes in charge;
- a missing storage adapter does not intercept or corrupt a normal container; and
- a missing world-generation adapter leaves the normal generator as the complete
  source of terrain and features.

A no-op status is observable for diagnostics, but it is not a gameplay error. Core
code should be able to run its normal path without a special absence branch at every
call site.

## Optional dependencies and classloading

The Forever core and API must not import, mention, or expose a third-party type.
That includes method signatures, fields, generic parameters, static initialisers,
serialised records, and client payloads. A core type such as `RouteSnapshot` is a
Forever type even when an adapter fills it from a rail mod.

Third-party classes must be confined to the corresponding `dev.forever.compat.*`
implementation. In particular:

- do not hard-load an absent mod class from a common entrypoint, class field, static
  initializer, or shared registration path;
- do not reference an optional class merely to test whether it exists;
- discover presence using safe metadata or loader information, then load the
  provider implementation lazily only when the dependency is confirmed;
- keep optional implementations out of the signatures compiled for the core
  abstraction; and
- ensure a dedicated server that lacks the optional mod can load, construct, and
  call every no-op capability.

A loader presence check can say that a mod is installed. It cannot prove that its
version or behaviour is supported. Datapacks and resource packs need their own
content-based detection, as described for Matcha in `matcha.md`.

## Version detection and refusal to guess

An adapter must identify the provider before enabling behaviour. The detection
strategy depends on the integration:

- a mod adapter validates the loader-visible mod ID and the exact supported version
  or a deliberately bounded compatible range;
- a datapack or resource-pack adapter validates pack metadata, fingerprints, and
  locked content digests; and
- a world or saved-data adapter validates its schema version and migration path
  before reading records.

A display name, a partially matching API, a class that happens to exist, or a
similar-looking recipe is not sufficient evidence. When the provider is newer,
older, malformed, or ambiguous, the adapter reports unsupported or unverified and
uses its no-op fallback. It must not guess which field or event has the old meaning.

There is no automatic compatibility mode that silently maps an unknown version to
the nearest known version. Adding support for a new version is a new profile or
adapter change with evidence, tests, and a documented decision.

## Support and confidence reporting

Every adapter publishes a small immutable status snapshot. The exact Java type is a
future implementation detail, but the contract includes the following concepts:

| Field | Allowed meaning |
|---|---|
| Adapter ID | Stable Forever ID such as `cooking` or `rail`, never a third-party class name |
| Presence | `ABSENT`, `PRESENT`, or `MALFORMED` |
| Support level | `FULL`, `PARTIAL`, or `NONE` |
| Confidence | `EXACT`, `KNOWN_COMPATIBLE`, `UNVERIFIED`, or `NOT_APPLICABLE` |
| Detected version | An opaque, human-readable version only after validation, otherwise absent |
| Diagnostics | Bounded actionable messages and evidence IDs, never a swallowed exception or raw secret |

`ABSENT` with `NONE` and `NOT_APPLICABLE` is the normal no-op result. `PRESENT`
with `UNVERIFIED` cannot expose a partial gameplay mapping merely because some
signals look familiar. `PARTIAL` means that the adapter has explicitly documented
which capabilities work and which return neutral results. A provider must not
report `FULL` when it has skipped a required version or schema check.

Errors are handled at the adapter boundary with context. The implementation logs
which adapter, version check, capability, and input failed, then returns the
smallest safe neutral result. It must not use a broad catch that hides corruption or
continues with invalid data.

## Core depends on abstraction, never on third-party type

The dependency direction is one way:

```text
Forever Core -> Forever API capability interface <- optional adapter implementation
```

Core may depend on a stable abstraction such as `ClimateCapability` and
`CookingResult`. It may not depend on a vendor's `Season`, `RailNetwork`,
`HorseEntity`, `ExternalInventory`, or worldgen feature type. The client receives
bounded, versioned projections of core state, not provider objects.

This rule protects save data as well as classloading. If an optional integration is
removed, its provider-specific objects disappear, but the world still contains
versioned Forever records and can use the no-op capability. If a provider changes,
only its adapter and profile should need to change.

## Illustrative Java sketch

The following is a contract sketch only. It is illustrative and is **not committed
code**. Names, result types, and package locations may change when a second real
caller justifies the implementation.

```java
// Illustrative only. No third-party type appears in this contract.
public interface CookingCapability {
    AdapterStatus status();

    CookingResult prepare(CookingRequest request);
}

public final class NoOpCookingCapability implements CookingCapability {
    @Override
    public AdapterStatus status() {
        return AdapterStatus.absent("cooking");
    }

    @Override
    public CookingResult prepare(CookingRequest request) {
        return CookingResult.noExternalChange();
    }
}
```

`CookingRequest`, `CookingResult`, and `AdapterStatus` in this sketch are Forever
concepts. The real optional implementation may convert them to a vendor API inside
its own package, but that conversion must not leak through the interface.

## Testing requirements

An adapter is incomplete until both presence and absence have been tested. The
minimum test matrix is:

| Case | Required assertion |
|---|---|
| Supported provider version present | Detection reports the expected support and the capability returns the documented result. |
| Provider absent | A dedicated-server or unit test constructs the no-op capability, calls it, and observes a neutral result without a crash. |
| Newer or older unsupported version | Detection refuses to guess, reports `NONE` or an explicit degraded status, and leaves the neutral fallback active. |
| Malformed metadata or schema | The adapter fails closed with an actionable diagnostic and does not partially mutate state. |
| Optional class absent from runtime | Common/server startup and no-op calls complete without `NoClassDefFoundError`, linkage errors, or reflective loading of the absent class. |
| Partial support | Unsupported sub-capabilities return their own neutral result and are visible in the status snapshot. |
| Save and reload | Any adapter-derived Forever data uses a versioned schema and survives restart without requiring the provider. |
| Runtime boundary | Server-authoritative results are bounded and deterministic; the client cannot decide an outcome from an adapter signal. |

Where a provider can be installed in a test fixture, test the real integration at
least once. Where it cannot, test the adapter's parser, fingerprint, refusal, and
no-op paths with deterministic fixtures. A test that only exercises the provider
present path is not sufficient. A test that only asserts that startup does not crash
is also not sufficient, because it may hide an incorrect fallback or an accidental
loss of vanilla behaviour.

# Code guidelines: where Forever code belongs

This is a short navigation guide for human contributors. It answers where to look before
editing an existing system and where a new file belongs. The detailed authority remains in
[`docs/architecture.md`](architecture.md), the relevant system specification, its ADRs, and
[`docs/development-workflow.md`](development-workflow.md).

The guide is in `docs/code-guidelines.md` rather than `CONTRIBUTING.md` because this repository
currently has no contributor guide and its binding design and workflow guidance already lives
under `docs/`. Add a project-level `CONTRIBUTING.md` only when there is broader contributor
process to document, rather than creating a second copy of these package rules.

## I want to change X. Where do I look?

| Change | Start here | Keep in mind |
|---|---|---|
| A rule, invariant, value object, validation function, or explainable result | `src/main/java/dev/forever/core/<feature>/domain/` | Domain code is pure and must not import `net.minecraft`, Fabric, `application`, or `adapter`. |
| A server use case or orchestration flow | `src/main/java/dev/forever/core/<feature>/application/` | The application layer may use domain types, but must not depend on concrete adapters. |
| An item, component, registry entry, `ItemStack` operation, block/container lookup, event, resource reload, or `SavedData` record | `src/main/java/dev/forever/core/<feature>/adapter/` | Keep Minecraft and Fabric details at the boundary. The adapter may call application and domain code. |
| A persistent schema, codec, validation, migration, or recovery path | The relevant system specification, ADR, `docs/world-save-safety.md`, then the feature's persistence code | Never change a stored shape as a side effect of a package tidy-up. Test old, current, invalid, and future data. |
| A balance value, threshold, rate, price, slot count, distance, or multiplier | `src/main/resources/data/` and the relevant system specification | Put tuning in validated data. Do not hide it in mechanics code. |
| A client screen, HUD, tooltip, Field Guide page, or recipe-viewer integration | `src/client/java/dev/forever/client/` and the relevant client resources | Client code is a projection. It must not decide a server-authoritative outcome. |
| A dedicated-server integration test | `src/gametest/java/` | Use a disposable world. Test the common code path without requiring client classes. |
| Pure logic that does not need a Minecraft runtime | `src/test/java/` beside the feature and layer it exercises | Keep the fixture small and assert the failure and boundary cases, not only the happy path. |
| Matcha identifiers, datapack paths, disguised identities, or version detection | `src/main/java/dev/forever/compat/matcha/` | Do not leak Matcha private details into core or client code. |

For a storage example, the current pilot is organised as follows:

```text
src/main/java/dev/forever/core/storage/
├── domain/
│   ├── CacheOperation.java
│   ├── StorageBalance.java
│   └── WarehouseSearchRequest.java
├── application/
│   └── StorageBalanceAccess.java
└── adapter/
    ├── ForeverStorage.java
    ├── TravelerCache.java
    ├── StorageContainerAccess.java
    └── WarehouseController.java
```

The example is abbreviated. The complete classification is recorded in
[`ADR 0021`](adr/0021-layered-feature-packages.md).

## I am adding a new thing. Where does it go?

### 1. Choose the feature first

Use the existing feature package for the behaviour. Do not place a storage rule in a global
utility package or create a project-wide `domain` directory. If no feature owns the concept,
stop and record the design gap in a backlog ticket before inventing a shared home.

### 2. Classify by responsibility

Ask these questions in order:

1. Could this code be tested with plain Java and the shared neutral data helpers, without
   loading Minecraft or Fabric? If it owns a rule, value, validation, or result, put it in
   `<feature>.domain`.
2. Does it coordinate a use case while remaining independent of concrete Minecraft classes?
   Put it in `<feature>.application`.
3. Does it touch a registry, item, component, world, container, saved-data API, resource
   reload, event, or Minecraft-shaped codec? Put it in `<feature>.adapter`.
4. Does it answer more than one question? Split it only along a real seam, preserve the
   existing behaviour, and add tests for the boundary. Do not create a wrapper or interface
   merely to fill a directory.

Classify by inspection of imports and behaviour. A class with no direct `net.minecraft` import
is not automatically domain code. For example, a registration entrypoint or a codec helper
may still belong in `adapter` because it serves Minecraft-shaped records. Likewise, a pure
value codec may stay in `domain` when its encoded value has no Minecraft dependency.

### 3. Keep dependencies pointing inward

The only permitted feature-layer direction is:

```text
adapter -> application -> domain
```

An adapter may also use domain directly. Domain never imports application or adapter code.
Application never imports adapter code. Same-layer collaboration is acceptable when it does
not create a cycle. If a lower layer needs a Minecraft-shaped type, keep that type at the
adapter boundary or introduce a genuinely useful neutral value type as part of a separate,
behaviour-preserving change.

Use package-private classes and methods for implementation details that do not need to cross a
layer. Make a boundary type public only when a real caller needs it. A public type still needs a
small contract and tests.

### 4. Check the design before coding

Before changing a system, read the current run brief, vision, design principles, architecture,
relevant ADRs, system specification, and save-safety guidance where persistence is involved.
Confirm that the backlog ticket names the objective, non-goals, acceptance criteria, and tests.
If the specification and code disagree, stop and report the conflict. Do not make the code
appear compliant by weakening the specification.

### 5. Verify the boundary

For a feature change, run the applicable unit tests and dedicated-server GameTests. Review the
full diff for accidental schema, registry, dependency, client, save, or compatibility changes.
Never point development tooling at a real survival world. The completion report should identify
what passed, what was not run, remaining risks, and the next ticket.

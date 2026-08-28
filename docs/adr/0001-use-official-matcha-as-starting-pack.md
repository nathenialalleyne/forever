# ADR 0001: Use the official Matcha release as the starting pack

- Date: 2026-08-28
- Related principles/ADRs: [Principles 1, 2, 12, 13, 14, and 18](../design-principles.md), [ADR 0002](0002-isolate-matcha-behind-adapter.md), [ADR 0003](0003-classify-matcha-mechanics-individually.md), [ADR 0020](0020-private-project-license-separation.md)

## Status

Accepted. This is a locked foundation decision.

## Context

Forever exists to stop the late game converging on the same anti-AFK-farm checklist:
iron farm, raid farm, gold farm, XP grinder, trading hall, then Elytra and rockets.
The project needs to make specialisation, civilisation, exploration, and infrastructure
credible alternatives without becoming an MMORPG or requiring a total conversion. This
supports Principle 1's protection of ordinary building and Principle 18's priority for
safe, reproducible worlds.

The first milestone is an architecture and audit foundation, not a fresh gameplay
implementation. A real starting pack gives the audit work something concrete to inspect.
It also lets Forever measure whether its intended alternatives coexist with ordinary
Minecraft building and survival rather than merely agreeing with an abstract design.

Matcha Flavoured is the selected official gameplay pack. The verified release is version
1.12, Modrinth project `QI0EmgZ1`, version ID `E9rngRfK`, for Minecraft 26.2. It is a
release archive, not a Fabric mod dependency. Its exact file is acquired by the fetch
scripts and recorded in `matcha.lock.json`, including a SHA-256 digest.

That distinction matters. Forever is not adopting an unpinned name such as "latest
Matcha", and Gradle cannot provide dependency reproducibility for a datapack archive.
Minecraft 26.2, Fabric, Java, and Matcha versions are all deliberately pinned so an
audit result can be reproduced against the same inputs.

The choice has competing forces. Reusing an existing pack is faster and gives useful
behaviour to audit, but it imports assumptions Forever may eventually reject. Forking
immediately would make ownership and fixes clearer, but would create a permanent merge
burden before the project knows which mechanics need changing. World-save longevity and
license separation also matter more than short-term convenience.

## Decision

Forever starts from the official Matcha Flavoured 1.12 release and does not fork it
initially. The selected release is pinned by its exact Modrinth version identity and
content digest. Acquisition is deterministic, and a changed upstream archive is a
separate dependency decision rather than an incidental result of a build.

Matcha is treated as an auditable starting pack and a replaceable implementation detail,
not as Forever's long-term public API. The initial work is to acquire, inspect, record,
and test it. It is not permission to copy Matcha internals throughout Forever or to
silently alter the upstream archive.

Forever-owned mechanics are developed around stable concepts in the Forever architecture.
The only code permitted to know Matcha's private identifiers is the compatibility
boundary in [ADR 0002](0002-isolate-matcha-behind-adapter.md). Each observed mechanic is
classified independently under [ADR 0003](0003-classify-matcha-mechanics-individually.md).

The lock record, audit output, and third-party attribution remain part of the foundation.
The Matcha material stays legally and technically distinguishable from original Forever
material under [ADR 0020](0020-private-project-license-separation.md).

No decision to fork, replace, or redistribute Matcha is implied by this ADR. Such a
change requires an explicit review of compatibility, save migration, and licensing.

## Consequences

- The project can begin with real behaviour instead of spending the foundation milestone
  rebuilding a broad gameplay baseline from guesses.
- Audit findings and compatibility tests can be reproduced because the input archive is
  exact, even when a future Matcha release changes implementation details.
- Forever preserves the possibility of adopting useful Matcha mechanics while replacing
  mechanics that conflict with the vision's parity goal.
- Players can still encounter normal Minecraft building and exploration while the
  project evaluates additions, which supports Principle 1 and the non-total-conversion
  boundary in the vision.
- The project inherits Matcha's bugs, balance assumptions, private implementation choices,
  and any interaction costs until each mechanic has been reviewed.
- A release archive is another acquisition and packaging surface. Fetch scripts, lock
  validation, notices, and test fixtures must remain correct even though Gradle does not
  manage Matcha.
- We accept that refusing an immediate fork may delay fixes that would be easy to make in
  a private copy. That delay is preferable to committing to an unnecessary divergence.
- The pack may prove unsuitable in ways that require substantial replacement work. The
  audit must be willing to report that result rather than protecting the initial choice.
- Future work includes the acquisition audit, a supported-version policy, integration
  fixtures, a migration plan for any replaced persistent identifiers, and a clear release
  procedure for third-party material.
- Any upstream update needs a cloned development world, a new audit, and a dedicated
  decision or approved dependency ticket. It must never be auto-updated in CI.

## Rejected alternatives

### Fork Matcha immediately

An immediate fork would provide control over every file, but it would also create a
long-lived merge and provenance problem before the audit identifies what needs changing.
It would make upstream fixes expensive to compare and could accidentally mix Matcha
derivatives with proprietary Forever work. The adapter and classification boundaries
preserve an exit route without paying that cost on day one.

### Write all gameplay from scratch

A clean implementation would avoid inherited assumptions, but it would delay the project
and reproduce the very convergence problem Forever is meant to study. It would also make
it harder to distinguish a genuinely better mechanic from an untested design preference.
The current milestone explicitly documents and audits gameplay rather than implementing it.

### Use a different pack

Another pack could have a more permissive licence, a cleaner API, or a closer balance
profile. No alternative was verified to provide a better foundation for this pinned
Minecraft 26.2 baseline. Choosing one without an equivalent audit would trade a known
integration target for an unmeasured one. A future pack change remains possible through a
new ADR.

### Start with no gameplay pack

Doing nothing would reduce compatibility obligations, but it would make the early audit
abstract and postpone the discovery of conflicts until much more Forever code existed.
The official Matcha release is the least speculative concrete baseline available for the
foundation milestone.

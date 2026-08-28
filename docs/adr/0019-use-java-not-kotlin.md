# ADR 0019: Use Java rather than Kotlin for Forever

- Date: 2026-08-28
- Related principles/ADRs: [Principles 12, 14, 15, and 18](../design-principles.md), [ADR 0002](0002-isolate-matcha-behind-adapter.md), [ADR 0020](0020-private-project-license-separation.md)

## Status

Accepted. This is a locked language and toolchain decision.

## Context

Forever is built against Minecraft 26.2 and the official Fabric 26.2 template and
toolchain, which are Java-first. That baseline is Java-first in its source examples,
Gradle setup, Loom workflow, and the vanilla source that maintainers read. Java 25 is
already the pinned runtime for the project. Using the language that the official toolchain
demonstrates reduces the number of moving parts while the project builds alternatives to
the anti-AFK-farm checklist. Principles 12 and 18 make reproducible maintenance and safe
change especially important.

Kotlin is a capable JVM language, but it would add the Fabric Language Kotlin runtime mod
as a hard dependency. That runtime would need its own pinned version and compatibility
version tracking as Minecraft updates. It would therefore add a required runtime layer,
not merely a compiler preference. A project whose main risk is world-save safety should
not add that layer without a concrete benefit that outweighs it.

The maintenance context is unusual. Forever is intended to be maintained substantially by
AI agents as well as humans. Java has far better AI-agent training coverage and example coverage for Fabric mod
development, Minecraft integration, and the current official toolchain. That coverage
matters when agents need to read unfamiliar vanilla code, diagnose mappings or API changes,
and produce code that fits the established ecosystem.

Minecraft 26.2 is non-obfuscated and has no Yarn mappings in this baseline. Reading the
vanilla Java source is part of the normal workflow. Keeping Forever in Java means the
project's code, the official template, and the source being inspected use the same language
and conventions instead of introducing a translation boundary into every investigation.

Modern Java also covers most of the language features that would otherwise motivate
Kotlin data classes and sealed hierarchies. Records provide concise immutable value
carriers, and sealed types provide controlled hierarchies. Principle 12 puts thresholds,
prices, rates, and other balance in
JSON, so a Kotlin DSL advantage for expressing configuration largely evaporates.

There is a real cost to choosing Java. Codec and serialization definitions, validation,
command-line tooling, and some null-handling code are more verbose. That cost is visible
and accepted. The decision is about total project reliability and maintenance, not a claim
that Java is more pleasant for every individual class.

## Decision

Forever uses Java for production source code and the primary build/toolchain path. New
common, server, client, adapter, data, and CLI code is written in Java and follows the
official Fabric 26.2 Java-first conventions.

The project does not add Fabric Language Kotlin as a runtime dependency and does not create
a mixed Java/Kotlin production codebase. Java records, sealed interfaces or classes,
`Optional` where appropriate, small named types, and ordinary composition should be used
to keep the code explicit without recreating a god object.

Mechanics remain in code while thresholds, prices, milestones, production rates, distances,
and multipliers remain in validated data under Principle 12. A Kotlin-style configuration
DSL is not a reason to introduce Kotlin when the authoritative content belongs in JSON.

Java's verbosity is handled through project conventions, immutable records where suitable,
small codecs and serializers, focused helpers with real callers, and tests. It is not
handled by hiding state in global singletons or by weakening the architecture. The JDK,
Fabric, Loom, Gradle, and Minecraft versions remain pinned under the dependency baseline.

A future proposal to introduce another JVM language would require a new ADR that shows a
specific capability unavailable or materially unsafe in Java, accounts for runtime and
agent-training costs, and preserves the save and deployment guarantees.

## Consequences

- The code aligns with the official Fabric 26.2 template and the Java source that
  maintainers inspect, reducing toolchain and debugging translation costs.
- The project avoids a hard runtime dependency on Fabric Language Kotlin and one more
  version that must be pinned and tracked through Minecraft updates.
- AI agents have a larger body of relevant Java Fabric examples and are more likely to
  produce code that matches the current ecosystem. That is a practical maintenance benefit,
  not merely a stylistic preference.
- Java records and sealed types provide concise immutable data and closed domain variants
  for much of the architecture without adding a second language runtime.
- JSON remains the balance surface, so content designers and agents can tune numbers
  without compiling a Kotlin DSL or coupling balance to a programming language.
- Codec, serialization, CLI, command-line, and validation code will be more verbose than
  the equivalent Kotlin. Boilerplate can obscure mistakes if reviews are rushed.
- Java has weaker language-level null-safety and fewer concise collection operations than
  Kotlin. The project must compensate with validation, explicit contracts, tests, and
  disciplined use of types rather than pretending the risk does not exist.
- Contributors who prefer Kotlin may find the project less attractive, and some useful
  Kotlin libraries or examples cannot be adopted without adding the rejected runtime cost.
- The Java-first choice does not remove Fabric compatibility work. Official APIs, vanilla
  source changes, and non-obfuscated names still require continuous audit and tests.
- Future work includes Java style conventions, codec patterns, command parsing helpers,
  nullability guidelines, agent examples, and build checks that prevent accidental Kotlin
  source or runtime drift.
- We accept more local verbosity in exchange for a smaller dependency graph, stronger
  alignment with the target toolchain, and a more maintainable AI-assisted codebase.

## Rejected alternatives

### Kotlin for all production code

Kotlin would reduce boilerplate and improve null-safety, but it would require Fabric
Language Kotlin as a hard runtime dependency plus another pinned compatibility version. It
would also diverge from the official Fabric 26.2 Java-first workflow and the Java source
that the project reads when debugging Minecraft behaviour.

### A mixed Java and Kotlin codebase

A mixed codebase could let each contributor choose a preferred language, but it would add
build ordering, style, interop, debugging, and agent-context costs without removing the
Kotlin runtime dependency. It would make ownership and examples less consistent for a
small project.

### Kotlin only for data and configuration DSLs

A DSL could make balance declarations pleasant, but Principle 12 says the authoritative
balance belongs in JSON. Adding Kotlin for a layer that should be data would increase
runtime and version risk while making content less portable and inspectable.

### Groovy or Kotlin as the primary Gradle language

Changing the build language would not address the runtime and Fabric API considerations,
and a Kotlin Gradle DSL would add another place where agents and contributors must
translate examples. The official template and pinned baseline are already Java-oriented.

### Java now, Kotlin later without an ADR

A quiet future migration would scatter language assumptions and make dependency changes
look like ordinary feature work. Any new language must prove a concrete need and account
for save, packaging, toolchain, and maintenance costs through an explicit decision.

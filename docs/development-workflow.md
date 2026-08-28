# Development workflow

This is the required AI workflow for Forever. It is intentionally narrow. The project
uses one ticket at a time so that a seemingly helpful implementation does not quietly
expand into an unreviewed change to save formats, balance, compatibility, or unrelated
gameplay.

## 1. Choose one ticket

Start with exactly one ID from `docs/backlog.md`. Confirm that the ticket is the next
unblocked item, record its status as in progress, and state what is explicitly outside
scope. If the request contains several systems, split it into tickets before changing
code.

**Done looks like:** one ticket ID is named in the working notes or change description,
its dependencies are satisfied, and no second ticket is being implemented opportunistically.

**Common failure modes:** treating “build the system” as permission to build every
neighbouring feature, starting a ticket whose ADR is not approved, or carrying an old
ticket's assumptions into a new one without checking the current backlog.

## 2. Read the relevant documents and ADRs

Read `docs/_agent-brief.md`, `docs/vision.md`, `docs/design-principles.md`, and
`docs/architecture.md`. Then read every relevant ADR and the relevant system
specification in `docs/adr/` and `docs/systems/`. Read `docs/dependency-baseline.md`
for version or third-party changes and `docs/world-save-safety.md` for world or
persistent-data work.

The purpose is to discover constraints before an implementation shortcut creates a
conflict. If the code and specification disagree, stop and report the conflict. Do not
rewrite the specification to legitimise the code.

**Done looks like:** the ticket can name the design decisions it relies on, the intended
module and authority boundary is clear, and missing decisions have become a reported
blocker or a new documentation ticket.

**Common failure modes:** reading only a class comment, assuming an empty ADR or system
directory means no design applies, or copying a familiar Minecraft pattern that breaks
Forever's save-safety or server-authority rules.

## 3. Write or refine acceptance criteria

Before implementation, make the ticket observable. Acceptance criteria must describe
the externally meaningful result, failure behaviour, persistence expectations, and
boundaries that must not change. A persistent structure must name its schema version,
codec or serializer, validation, migration strategy, and invalid-data behaviour. A
server-authoritative system must name persistence, duplication, restart, and
client/server authority checks.

Also state the non-goals. A non-goal is a guard against scope creep, not a suggestion.
For player-facing work, include translations, tooltip or UI explanation, Field Guide
entry, and recipe-viewer integration when applicable.

**Done looks like:** another agent can decide pass or fail from the ticket without
guessing what “works” means, and each criterion maps to at least one expected test or
review check.

**Common failure modes:** criteria that only say “implement X”, missing failure cases,
accepting a client-side result without a server check, or leaving migration and
duplication behaviour for a later ticket that never arrives.

## 4. Implement the smallest change

Change only what the selected ticket requires. Keep mechanics in code and balance in
data or configuration where practical. Use the architecture's source-set boundaries,
keep Matcha internals inside `dev.forever.compat.matcha`, and do not add empty future
classes. Preserve normal block placement and ordinary Minecraft play.

If implementation reveals that the accepted design cannot work, stop and report the
conflict. Do not silently alter an approved ADR or the specification to make a partial
implementation appear complete.

**Done looks like:** the diff has a clear relationship to the ticket, includes no
unrelated refactor or dependency update, and leaves the repository in a coherent state
when the feature is disabled or its optional integration is absent.

**Common failure modes:** introducing a god object, scanning the whole world each tick,
force-loading chunks for passive simulation, hardcoding tuning values, leaking Matcha
identifiers into common code, or adding final assets before the asset manifest and
gameplay need are approved.

## 5. Add tests

Add tests at the same time as the implementation. Use plain JUnit for logic that does
not require Minecraft and GameTests or an equivalent integration harness for actual
world behaviour. Test valid and invalid inputs, bounded work, optional-mod absence, and
failure messages where relevant.

For persistent or server-authoritative work, tests must cover save and reload,
duplication attempts, server restart, and the client/server authority boundary. Test
schema migration from at least one prior version before declaring a persistent format
ready. For a player-facing mechanic, check translations, explanation text, and the
Field Guide or recipe-viewer contract as appropriate.

**Done looks like:** every acceptance criterion has a test or an explicitly documented
manual verification, and tests fail for the regression the ticket is intended to
prevent.

**Common failure modes:** testing only a happy-path helper, asserting client state
instead of server truth, omitting a restart or duplicate-item case, using a real world
as a fixture, or writing tests that pass while the feature is absent.

## 6. Run all applicable builds and tests

Run the root build and relevant unit, GameTest, datagen, audit, or compatibility
commands. Use JDK 25 and the pinned wrappers. A dedicated-server smoke test is required
when common/server loading, registration, networking, or persistent world behaviour is
affected. Run audit-tool tests in `tools/matcha-audit` with its own wrapper.

Do not fetch Matcha from CI or as an invisible build side effect. Matcha acquisition is
a deliberate developer operation using the pinned script and a disposable world.

**Done looks like:** the completion report lists each applicable command as passed,
failed, blocked, or not run, with enough output to explain failures. No command is
claimed as passed merely because it is documented.

**Common failure modes:** running only an IDE build, using a different JDK, ignoring a
failed dedicated-server start, skipping tests because a change “is only data”, or
silently accepting a missing datagen or audit task.

## 7. Review the diff against the ticket

Inspect the complete diff, including newly created files. Compare every changed line
with the ticket's objective, non-goals, and acceptance criteria. Check for accidental
version changes, generated files, Matcha-derived material, save-format changes,
client-only references in common code, unbounded scans, and unapproved art.

**Done looks like:** every changed file is justified by the selected ticket, every
acceptance criterion has evidence, and unrelated changes have been reverted or moved to
a new ticket.

**Common failure modes:** reviewing only the last edited file, forgetting untracked
files, leaving debug output or broad exception handling, or noticing scope expansion
only after the change has been merged.

## 8. Update only affected documentation

Update the relevant system specification, ADR index, Field Guide contract, dependency
baseline, migration notes, README, or changelog only when the ticket changes those
facts. Keep documentation and implementation consistent, but do not rewrite a design
document merely to match an implementation shortcut. Foundation work must keep the
current “no gameplay” status honest.

**Done looks like:** a future agent can find the new behaviour, constraints, and version
or migration facts without reading the implementation, while unrelated documents remain
untouched.

**Common failure modes:** copying rules into competing pointer files, changing every
roadmap item after one ticket, documenting planned behaviour as shipped, or updating a
specification after the fact to conceal a conflict.

## 9. Record known risks

Before stopping, write down assumptions and remaining risks. Include save compatibility,
migration failure, item or inventory duplication, restart recovery, authority boundary,
performance bounds, optional dependency behaviour, licensing, and test-environment
limitations when they apply. Name the next ticket and any blocker it inherits.

**Done looks like:** a maintainer can understand what was not proven and what must be
checked next, rather than inferring safety from a green unit-test count.

**Common failure modes:** calling a feature safe because it works in one session,
omitting a known untested migration, hiding a network dependency, or claiming that a
real-world check was performed when it was intentionally not run.

## 10. Stop

Stop after the selected ticket is complete, documented, and reported. Do not begin the
next ticket in the same change, even if its code appears nearby or its first step looks
small. The next agent should start by reading the next ticket and its documents from a
clean scope boundary.

**Done looks like:** the ticket status is updated, the diff is reviewable, the evidence
report names files, commands, results, assumptions, risks, and the next ticket, and no
unrequested follow-up implementation has started.

**Common failure modes:** “while I am here” refactors, pre-implementing a future
system, adding a speculative abstraction, or leaving the repository in a half-started
state for multiple tickets at once.

# Many Roads Home ADRs

This directory records design decisions that shape Many Roads Home's long-lived Minecraft
worlds. Existing records may still use Forever for the repository and internal code identity.
An ADR captures the problem, the choice made, the trade-offs accepted, and the alternatives
that were deliberately not chosen. It is a durable contract for humans and AI agents,
not a feature announcement or a substitute for implementation documentation.

[Jump to the index of all ADRs](#index).

## Purpose

Forever's vision is to make specialisation, civilisation, exploration, and infrastructure
credible alternatives to the late-game anti-AFK-farm checklist. That goal creates choices
whose consequences cross systems: save formats, transport, settlement identity, third-party
compatibility, balance, licensing, and the meaning of normal Minecraft play.

The ADR set keeps those choices visible. A future implementation may change the mechanism
while preserving the decision, but it must not quietly change the decision's constraints.
The [design principles](../design-principles.md) remain the non-negotiable guardrails.
The [architecture](../architecture.md) explains where a decision is expected to live.

## Numbering

- ADRs use a four-digit, zero-padded sequence beginning at `0001`.
- Numbers are assigned in order and are never reused, even if a decision is deprecated.
- Filenames use the form `NNNN-short-kebab-case.md`.
- The filename and number remain stable for the life of the record.
- A later ADR receives a new number even when it changes or reverses an earlier choice.
- The index below is the canonical directory map and gives each record a one-line summary.

Numbers describe decision history, not priority, implementation order, or system ownership.
Related ADRs should be linked by number so a reader can follow the chain of reasoning.

## Statuses

Every ADR has one of these statuses:

- **Proposed** means the decision is being discussed and is not yet a project contract.
  Code and content must not rely on it as settled policy.
- **Accepted** means the decision is current and locked. Implementation may proceed within
  its constraints, and a conflicting ticket must stop for an ADR review.
- **Superseded** means a later ADR replaces the decision for current work. The old record
  remains valuable historical context and links to the replacing ADR.
- **Deprecated** means the decision is no longer recommended, but it has no single direct
  replacement or its retirement is itself the important information.

Status is about authority, not quality. A Superseded ADR is not erased, and a Proposed ADR
may contain useful analysis that a later decision should preserve.

## When a new ADR is required

Open a new ADR when a choice has meaningful consequences beyond one small implementation
and at least one reasonable alternative exists. In particular, a new ADR is required for:

- A change to a non-negotiable design principle or a decision that constrains several
  systems.
- A new or removed dependency, language, runtime, compatibility boundary, or license.
- A persistent player, item, entity, settlement, route, warehouse, shipment, or world
  format, including a migration or recovery strategy.
- A change to world generation, dimension logistics, travel authority, or save safety.
- A replacement of an Accepted ADR, even when the new choice appears to be a small tweak.
- A cross-system rule whose balance, accessibility, multiplayer, or solo-play consequences
  cannot be understood from a local class or data file alone.

Do not open an ADR merely to record a private method name, a routine refactor, a balance
value that belongs in data, or an implementation detail with no durable design trade-off.
If uncertain, prefer a short Proposed ADR over an undocumented cross-system assumption.

## ADR writing and review process

1. Search the index and related principles before drafting. Identify whether an existing
   decision already covers the question.
2. Give the record the next unused number and an exact, descriptive filename.
3. Include the date, status, and a `Related principles/ADRs` line near the title.
4. Explain the actual problem and forces in **Context**. Refer to the vision and numbered
   principles rather than describing an abstract best practice.
5. State one coherent choice in **Decision**. Put tunable thresholds and rates in data
   where Principle 12 requires it.
6. Record positive and negative **Consequences**, including accepted risks and work that
   the decision now makes necessary.
7. Name real **Rejected alternatives** and explain why each lost. Do not use strawmen.
8. Link related ADRs by number and verify that links and filenames are stable.
9. Review save safety, solo play, multiplayer, accessibility, performance, provenance,
   and Field Guide implications when they are relevant.
10. Mark the record Accepted only after the decision is genuinely locked. A feature ticket
    may implement an Accepted ADR, but it may not silently reinterpret it.

## Append-only policy

Accepted ADRs are historical records. Do not rewrite their Context, Decision, Consequences,
or Rejected alternatives to make them agree with a later idea. Do not delete, rename, or
reuse an ADR number.

The append-only rule means that when a decision changes, write a new ADR with a new number.
The new record must identify
the old ADR, explain what changed, and describe migration or compatibility consequences.
The old record remains in the directory so a reader can understand why earlier work was
made.

The substantive text is immutable. A status line may be changed only as administrative
metadata when an ADR becomes Superseded or Deprecated, and it must link to the new record
or explain the retirement. No other edit should be made to an Accepted ADR. Corrections
that alter meaning require a new ADR rather than a silent fix.

A later implementation must follow the newest Accepted record in the chain. If two
Accepted ADRs appear to conflict, stop and open a review rather than choosing whichever
is more convenient. The world save outranks a feature, and an unsafe migration is a
reason to delay implementation, not to edit history.

## Index

The current index contains the locked decisions for the foundation and early design work.
All records below are **Accepted**.

| ADR | Summary |
|---|---|
| [0001](0001-use-official-matcha-as-starting-pack.md) | Use the exact official Matcha Flavoured 1.12 release as the auditable starting pack without forking initially. |
| [0002](0002-isolate-matcha-behind-adapter.md) | Keep Matcha identifiers and behaviour behind a narrow `dev.forever.compat.matcha` adapter boundary. |
| [0003](0003-classify-matcha-mechanics-individually.md) | Audit each Matcha mechanic as keep, extend, override, replace eventually, or undecided. |
| [0004](0004-player-mastery-loadout.md) | Give players one Focus and two Supporting mastery slots while keeping all learning permanent. |
| [0005](0005-item-specialization-paths.md) | Let each item retain several paths while only one is active and switching requires reforging. |
| [0006](0006-repair-and-reforge-model.md) | Preserve broken equipment and use field repair, workshop repair, and full-condition reforging. |
| [0007](0007-villager-mortality.md) | Allow causal villager death while forbidding arbitrary mortality in unloaded abstract simulation. |
| [0008](0008-hybrid-obol-currency.md) | Combine transferable physical Obols with a compact withdrawable Coin Purse balance. |
| [0009](0009-settlements-as-building-graphs.md) | Model settlements as graphs of registered functional buildings and infrastructure connections. |
| [0010](0010-explicit-route-registration.md) | Require explicit road surveys and bounded rail verification, then cache route results. |
| [0011](0011-travelers-cache.md) | Provide a nine-slot Traveler's Cache expandable to eighteen with no container nesting. |
| [0012](0012-cross-dimensional-logistics.md) | Keep logistics dimension-scoped and allow later connections only through explicit Portal Depots. |
| [0013](0013-elytra-as-glider.md) | Preserve Elytra for gliding, scouting, and vertical traversal while rail wins on fixed routes. |
| [0014](0014-climate-abstraction-for-seasons.md) | Target Serene Seasons through an optional climate abstraction rather than a core dependency. |
| [0015](0015-conservative-worldgen-posture.md) | Preserve dramatic terrain, restrain structures, and add no world-generation dependency now. |
| [0016](0016-existing-village-import.md) | Incorporate existing natural villages through a bounded, player-confirmed charter workflow. |
| [0017](0017-player-shop-pricing.md) | Let players choose prices while NPC willingness-to-pay determines whether a sale happens. |
| [0018](0018-infrastructure-gated-journey-skipping.md) | Permit journey skipping only after discovery, physical travel, built infrastructure, and validation. |
| [0019](0019-use-java-not-kotlin.md) | Use Java to match the Fabric 26.2 toolchain, source workflow, and AI-maintained codebase. |
| [0020](0020-private-project-license-separation.md) | Keep proprietary Forever work distinct from CC-BY-NC-SA-4.0 Matcha and other third-party material. |
| [0021](0021-layered-feature-packages.md) | Keep package-by-feature while expressing domain, application, and adapter boundaries inside each feature. |
| [0022](0022-extensible-by-default.md) | Make content data-open, environment-dependent behaviour port-based, and fallible operations described results. |
| [0023](0023-many-roads-home-working-title.md) | Use Many Roads Home as the working product title and migrate persisted public identifiers before the first persistent release. |
| [0024](0024-modpack-first-product-ownership.md) | Make the curated modpack the primary product and keep the companion integration layer narrow. |
| [0025](0025-packwiz-source-of-truth.md) | Use Packwiz metadata as the authoritative source for composition and treat exports as generated outputs. |
| [0026](0026-existing-mod-first-escalation-policy.md) | Escalate from configuration through supported integration layers before approving a private fork or custom implementation. |
| [0027](0027-compatibility-controlled-matcha-loading.md) | Load the exact official Matcha archive through a compatibility-controlled utility that provides both pack roles. |
| [0028](0028-adventure-rewards-not-essential-gates.md) | Do not make adventure rewards the sole route to essential capabilities. |
| [0029](0029-unified-contextual-knowledge.md) | Use one contextual Field Journal while preserving instructions and source provenance from starter books. |
| [0030](0030-no-global-enemy-scaling.md) | Avoid global enemy scaling and use bounded, local, inspectable danger instead. |
| [0031](0031-fast-travel-requires-established-routes.md) | Require discovery, physical travel, infrastructure, registration, and validation before fast travel. |
| [0032](0032-optional-style-independent-blueprints.md) | Keep blueprints optional and validate construction function rather than architectural style. |
| [0033](0033-documented-gap-analysis-for-companion-code.md) | Require a documented alternative and maintenance gap analysis before adding companion code. |
| [0034](0034-preserve-existing-custom-code.md) | Preserve prototype source and evidence during the modpack-first pivot without treating it as released scope. |

If an ADR is superseded or deprecated, update this index's status note and link to the
replacement while retaining the historical record. Do not remove the row or renumber the
remaining records.

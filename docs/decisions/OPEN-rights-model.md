# Decision needed: what rights model does Many Roads Home have?

**Status: open. Waiting on the project owner. Nothing is blocked by this except publication.**

The repository is private today, and ADR 0020 requires a licence review before that
changes. The review is done: `labs/results/LAB-LICENCE-publication-readiness.md`. This
page is the short version, so the decision does not require reading 333 lines.

## Why this needs a person

Everything else in this project has been settled by running an experiment. This cannot
be. It is a question about what you want the project to *be*, and parts of it are legal
rather than technical. An agent guessing here would be guessing about your intent and
your risk tolerance.

## The situation in four facts

1. `LICENSE` is a bespoke All-Rights-Reserved licence. It is **not** an open-source grant,
   so publishing the repository as-is would let people read the code without giving them
   permission to use it. That may or may not be what you want.
2. `generated/matcha/` holds 21 report files derived from Matcha, which is
   **CC-BY-NC-SA-4.0**. ShareAlike and NonCommercial interact awkwardly with an
   All-Rights-Reserved root licence.
3. Those reports are now **byte-for-byte reproducible** from the pinned archive. This was
   fixed after the audit ran, and it matters: reproducible evidence does not need to be
   committed, which removes the hardest part of the problem.
4. Global Packs, an adopted dependency, is All-Rights-Reserved and its declared source URL
   returns 404. That affects *distributing a pack*, not publishing source. Verified
   directly against the Modrinth API.

## The realistic choices

| Choice | What it means | Cost | Recommended? |
|---|---|---|---|
| **Stay private** | Change nothing. Decide later. | None. | **Yes, for now.** Nothing else is blocked, and this preserves every option. |
| **Public source, open licence** | Pick MIT, Apache-2.0 or GPL for original work. Drop the generated Matcha reports and regenerate them on demand. Clean history. | Licence choice, history work, legal review. | The best public path if you want contributors. |
| **Public source, still proprietary** | Keep All-Rights-Reserved but scope it explicitly to original material. Same cleanup. | Same cost as above, without granting reuse. | Only if read-only visibility is genuinely the goal. |
| **Publish a `.mrpack`** | A separate question from the source repo. Needs Global Packs permission or a replacement first. | Per-release review. | Later, after the source decision. |

## One thing now depends on this

Testing found that the pack ships **no Matcha safety net**. If the Matcha archive is
missing, a server starts normally with 1585 recipes instead of 2346 and says nothing. The
diagnostic that prevents this (MRH-010) is written and tested, but it lives in the
companion mod, and the companion mod is not in the pack.

There are actually **two** silent paths, and the second is worse: a decoy archive with the
correct filename also yields 1585 recipes and says nothing, so the file looks present and
correct. Configuration alone cannot detect this; I tested Global Packs' `log_pack_ids` and
it produces identical output for a healthy and a fake archive.

**The fix is proven and waiting.** Dropping the existing companion build into a pack
instance turns both silent failures into a loud, actionable error banner, without blocking
startup. No new code is needed. Tracked as **MRH-011**.

Shipping it means the pack distributes original code, which needs its licence settled. So
this decision is no longer only about publication: it gates a finished, tested safety
feature for anyone who installs the pack.

This does not change my recommendation, because the pack has no users yet. It does mean
the decision stops being free the moment someone installs it.

## What I would suggest

**Stay private for now.** It costs nothing, blocks nothing, and the only thing you lose is
public visibility of a project that is still deciding what it is.

When you do want to publish, the second row is the strongest option, and it is now cheaper
than it was: because the audit reports regenerate byte-identically from a legitimately
obtained archive, they can simply be left out of a public snapshot rather than needing a
legal review of 1,570 third-party excerpts. That was the single biggest obstacle.

## What I am deliberately not doing

I am not changing the licence, making anything public, deleting the generated reports, or
rewriting history. Those are all one-way doors, and this is a decision that should be made
by you, in some cases with a lawyer. The audit is explicit that it is not legal advice.

## If you want to proceed

Say which row you want and I will do the mechanical work: the licence file, per-path
provenance notices, the reproducible-reports arrangement, and a pre-publication checklist.
The legal review of Matcha-derived material is the one step I cannot do for you.

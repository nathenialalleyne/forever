# Packwiz acquisition policy

Packwiz is the composition authority for Many Roads Home under ADR 0025. It is a
developer tool, not a pack input: nothing it produces embeds the tool, and no player ever
needs it.

## Why this document exists

PACK-01 criterion 4 requires the tool identity and acquisition policy to be recorded
*despite the absence of tagged upstream releases*. That absence is the whole problem.
Every other dependency in this project is pinned to an exact published version. Packwiz
cannot be, because upstream publishes none.

Recording how it is obtained, and how to tell which build produced a given index, is the
substitute for a version pin.

## The verified position

Checked on 2026-08-29 against the upstream repository and the locally installed binary.

| Fact | Value |
|---|---|
| Module | `github.com/packwiz/packwiz` |
| Tagged releases | **none.** Verified 2026-08-29: the GitHub API reports 0 releases and 0 tags |
| Install command | `go install github.com/packwiz/packwiz@latest` |
| Locally installed build | `v0.0.0-20260218225342-dfd8b68a4796` |
| Module hash | `h1:e6WSGD9fo7V8sbxGNOZiBHX6HnlBezOcQxVBZD6R0fM=` |
| Binary sha256 | `4ffe7ba919d69d848c714b66...` (local build artefact; not reproducible across machines) |

The Go pseudo-version is the useful identity. It encodes the upstream commit
(`dfd8b68a4796`) and its timestamp, so `v0.0.0-20260218225342-dfd8b68a4796` names an exact
source revision even though no tag exists. Record it whenever a pack change is attributed
to a packwiz run.

## Policy

1. **Install from the official module path only.** `go install github.com/packwiz/packwiz@latest`,
   then put `$(go env GOPATH)/bin` on PATH. Never vendor the binary into this repository:
   `validate-pack.sh` forbids committed binaries, and a checked-in tool would be both a
   licensing and a reproducibility problem.
2. **Record the pseudo-version when it matters.** Read it with
   `go version -m $(command -v packwiz)`. A pack change that alters `index.toml` should be
   attributable to a specific tool build.
3. **Pin deliberately when reproducibility matters more than currency.** `@latest` is
   convenient but non-deterministic. For a release, install the exact pseudo-version above
   instead, so the index can be regenerated identically later.
4. **Never require it.** The scripts must work without packwiz installed. They do:
   `validate-pack.sh` verifies the index itself and skips only the packwiz refresh, saying
   so rather than passing silently. This matters because CI has no Go toolchain.
5. **Re-verify after upgrading.** A newer packwiz may write a different index format. After
   any upgrade, run `packwiz refresh`, confirm `validate-pack.sh` still passes, and confirm
   two consecutive `build-pack.sh` exports remain byte-identical.

## Known behaviour worth remembering

The index excludes `index.toml`, `pack.toml`, `.packwizignore` itself, and anything
`.packwizignore` matches. A hand-written index that included `.packwizignore` was rejected
by a real `packwiz refresh`, which is why `validate-pack.sh` now mirrors these exclusions
rather than guessing at them.

Exports are reproducible: two `build-pack.sh` runs seconds apart produced byte-identical
`.mrpack` files with this build.

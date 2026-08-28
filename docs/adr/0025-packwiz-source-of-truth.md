# ADR 0025: Use Packwiz metadata as the pack source of truth

- Date: 2026-08-28
- Related principles/ADRs: [Principles 12, 14, 15, and 18](../design-principles.md), [ADR 0020](0020-private-project-license-separation.md), [ADR 0024](0024-modpack-first-product-ownership.md), [ADR 0027](0027-compatibility-controlled-matcha-loading.md)

## Status

Accepted. This is the pack composition and reproducibility decision.

## Context

Many Roads Home will contain more than a Java project. It will contain mods, configuration, datapacks, resource packs, optional blueprints, and compatibility notes that must be assembled into a repeatable instance. A hand-maintained list or a launcher-specific export can describe what a developer happened to install, but it cannot reliably explain which input is authoritative, whether two exports differ, or whether a removed component is safe for an existing world.

Packwiz is designed for a git-friendly TOML metadata format and can export or install a modpack. Its official repository describes the metadata files as version-controlled pack inputs and documents export and installer workflows at [github.com/packwiz/packwiz](https://github.com/packwiz/packwiz). The official releases page currently has no tagged releases at [github.com/packwiz/packwiz/releases](https://github.com/packwiz/packwiz/releases). The upstream repository documents installation through GitHub Actions artifacts or `go install github.com/packwiz/packwiz@latest`. That makes Packwiz useful as the pack model while making the Packwiz executable itself a separate reproducibility concern.

There is a practical distinction between source metadata and generated distribution. CurseForge or Modrinth exports, installer manifests, downloaded JARs, and a local instance are outputs. Editing those outputs directly would cause a later Packwiz build to erase the change, and it would make review depend on a binary directory rather than a small text diff. The pack also needs a provenance record that is more explicit than a version string because a datapack archive such as Matcha is not a Gradle dependency.

## Decision

Packwiz metadata is the source of truth for the Many Roads Home composition. The repository will keep the Packwiz pack definition, per-input metadata, configuration templates, optionality, source URLs, hashes where available, licence notes, and load-order intent under version control. Generated launcher exports, installer manifests, downloaded third-party files, and local instance directories are derived outputs and are not hand-edited as the product definition.

Every selected input must have an exact version or immutable revision, an authoritative source, a compatibility note for the pinned Minecraft and loader baseline, and a removal or migration note when it can affect a world. A source that only offers a moving `latest` reference is not sufficient for a release without recording an immutable tool or input identity separately. Matcha remains pinned by its own lock record and is not made into a Gradle dependency.

The Packwiz command-line tool is itself a build input. Because the upstream project has no tagged GitHub releases, a future implementation ticket must choose and record a reproducible Packwiz tool revision, binary checksum, or trusted CI artifact policy for local and CI use. Release automation must fail when the tool identity is unknown or when generated output differs from the declared metadata. It must not silently upgrade Packwiz or any pack input.

Packwiz owns composition, not third-party source ownership. The repository must not commit third-party JARs merely because Packwiz can reference them, and it must not modify a third-party JAR in place. Notices, attribution, and any redistribution conditions remain part of the release inputs under [ADR 0020](0020-private-project-license-separation.md).

## Consequences

- A reviewer can inspect the pack change as text and reproduce the intended composition without trusting a developer's local instance.
- Launcher exports and generated manifests can be regenerated, so drift between distribution targets is detectable rather than treated as a second source of truth.
- Pack assembly gains a real toolchain dependency. The no-release-tags state of Packwiz means the project must pin its executable through a deliberate policy instead of pretending a semantic version exists.
- A Packwiz metadata update can still change a world through a removed or reconfigured input. Dependency, save-safety, and disposable-world review remain required for pack changes.
- Text metadata does not prove that two mods are compatible. The pack still needs an assembled-instance matrix and dedicated-server or client validation where relevant.
- Keeping downloaded files out of the repository reduces provenance confusion but means an offline rebuild needs access to the recorded sources or a controlled cache.
- Contributors must learn the difference between editing Packwiz inputs and inspecting generated exports. A local manual fix can be lost if it is not represented in the source metadata.

## Rejected alternatives

### Use a hand-written `mods.txt` or README list

A list can name dependencies, but it does not provide structured version metadata, per-input optionality, generated exports, or a reliable update boundary. It would recreate the drift the pack needs to prevent.

### Treat a launcher export as the source of truth

Launcher-specific exports are necessary distribution products, not neutral authoring formats. Using one as the source would make other launchers and server installations dependent on an accidental export and would encourage binary or generated-file edits.

### Use Gradle as the pack manager

Gradle is appropriate for the companion's build, not for owning every non-Java pack input. Making the mod build the authority would couple gameplay compilation to third-party pack composition and would not naturally model resource packs, blueprints, or launcher exports.

### Commit and edit every downloaded JAR

This would make local reproduction appear simple, but it would enlarge the repository, obscure upstream provenance, increase licence obligations, and encourage silent binary modifications. The lock and source metadata are the reviewable authority.

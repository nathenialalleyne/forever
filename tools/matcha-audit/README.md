# Matcha audit CLI

`matcha-audit` is a standalone developer tool for making a conservative, machine-readable inventory and reference graph from the official Matcha Flavoured datapack/resource-pack archive. It does not attempt to infer gameplay mechanics. Its reports separate observed file and reference facts from explicitly labelled heuristics so that a human or another AI can review the source material before an adapter is written.

The tool is deliberately outside the Forever mod build. It has its own Gradle wrapper and `settings.gradle`, and it is never packaged into the mod JAR or shipped to players.

## Build

Use the pinned JDK 25 toolchain:

```sh
export JAVA_HOME=~/toolchains/jdk-25.0.4.1+1
./gradlew test
./gradlew build
```

The application distribution is produced by:

```sh
./gradlew installDist
build/install/matcha-audit/bin/matcha-audit --input /path/to/Matcha_Flavoured_1_12.zip --output generated/matcha/1.12 --version-label 1.12
```

On Windows, use `gradlew.bat` and `build\\install\\matcha-audit\\bin\\matcha-audit.bat`. Paths containing spaces are accepted when quoted by the shell.

## Usage

```text
matcha-audit --input <zip-or-directory> --output <output-directory> [--version-label <label>]
```

`--input` may be a ZIP archive or an already-unpacked directory. `--output` is created if necessary. `--version-label` is an optional human-readable identity for the selected input. If omitted, the input file name or directory name is used. No network access or Matcha acquisition is performed by this CLI.

The ZIP reader is intentionally fail-closed. It rejects absolute paths, Windows drive and UNC paths, path traversal, duplicate logical paths, ZIP symlink entries, malformed archives, and entries that exceed the documented entry, size, or compression-ratio limits. Directory inputs are also rejected if any symlink is present. ZIP contents are held in memory and are never extracted to disk. The tool writes only the generated reports inside the selected output directory.

## Exit codes

| Code | Meaning |
|---:|---|
| 0 | Audit completed. Individual malformed JSON files remain in the inventory and are reported as warnings. |
| 2 | CLI usage error, such as a missing option or unknown option. |
| 3 | Input error, such as a missing path, unreadable path, unsupported input type, or malformed directory. |
| 4 | Unsafe or rejected ZIP archive, including traversal, absolute path, symlink, duplicate path, ZIP bomb, or malformed archive. |
| 5 | Analysis error. The input could be read, but a required analysis operation could not be completed. |
| 6 | Output error. The selected output directory could not be created or a report could not be written. |
| 10 | Unexpected internal error. |

## Generated reports

Every successful run writes the following files, with stable sorting and UTF-8 encoding. The generated timestamp is isolated to `metadata.json`; all other reports are byte-stable for identical input bytes and options.

| File | Contents |
|---|---|
| `metadata.json` | Tool/schema identity, input identity and SHA-256, attribution, parsed `pack.mcmeta`, and the sole generated timestamp. |
| `input-sha256.txt` | SHA-256 of the ZIP bytes, or of a canonical sorted directory manifest. |
| `file-inventory.csv` | Every observed file, its logical pack path, side, namespace, classification, size, SHA-256, and JSON parse status. |
| `namespaces.json` | Observed data/resource namespaces and observed namespaced references. |
| `pack-filters.json` | Observed `pack.mcmeta` filter data. |
| `recipes.json` | Recipe file identities, types, observed result/ingredient IDs, and JSON references. |
| `advancements.json` | Advancement parents, reward functions, reward recipes, reward loot, and other observed references. |
| `functions.json` | Function commands and line-level observations for calls, scheduling, scoreboards, advancements, recipes, loot, item/data operations, IDs, and file paths. |
| `scoreboards.json` | Observed objective definitions and objective references. |
| `tags.json` | Tag identities, replacement flags, and observed membership values. |
| `loot-tables.json` | Loot table identities, types, nested table references, and JSON references. |
| `predicates.json` | Predicate files and observed JSON references. |
| `item-modifiers.json` | Item modifier files and observed JSON references. |
| `registry-data.json` | Registry-like, villager-trade, dimension, dimension-type, and other recognised data files. |
| `worldgen.json` | World-generation and structure data, including dimensions and dimension types when present. |
| `assets.json` | Resource-pack models, textures, language files, sounds, atlases, item definitions, equipment assets, fonts, and other recognised assets. |
| `vanilla-overrides.csv` | Observed files in the `minecraft` data/resource namespace, including direct vanilla recipe overrides. |
| `cross-references.json` | Stable, line/path-located observed reference edges and their observed resolution status. |
| `unresolved-references.csv` | References that look internal to an observed non-vanilla namespace but have no matching observed file. |
| `unknown-paths.csv` | Files and directories whose path could not be assigned to a recognised datapack/resource-pack category. |
| `summary.md` | Counts, categories, warnings, attribution, and review notes. It makes no gameplay-intent claims. |

`metadata.json` contains the required attribution header and metadata: Matcha Flavoured, project `QI0EmgZ1`, licence `CC-BY-NC-SA-4.0`. Matcha-derived material is kept separate from Forever's proprietary source.

JSON records carry `observationType: observed`. CSV reports carry an `observation_type` column. Reference resolution is a separate derived field with a `resolutionBasis`, so an exact observed string is not confused with a claim about its meaning.

For a directory input, the recorded hash is a canonical manifest SHA-256. Files are sorted by normalised POSIX relative path, then each file contributes its UTF-8 path, a NUL byte, its eight-byte big-endian content length, its bytes, and a `0xff` separator. For a ZIP input, the recorded hash is the SHA-256 of the archive bytes themselves.

## Dependency justification

- **Gson `com.google.code.gson:gson:2.13.2`** is pinned exactly because the tool must parse and emit Minecraft's JSON data while preserving arbitrary object shapes, report malformed JSON without dropping files, and canonicalise object keys for deterministic reports. The JDK has no JSON parser, and adding a larger data-binding framework would provide more surface area than this read-only analyser needs.
- **JUnit 5.14.2** is pinned through the JUnit BOM for the synthetic, offline unit tests. Jupiter provides temporary-directory tests, assertions, and test discovery without bringing a Minecraft runtime or the mod into this standalone build.

The Gradle wrapper is pinned to the repository's Gradle 9.5.1 baseline. Gson and JUnit are the only application/test dependencies. The real Matcha archive is never committed to this project or its tests.

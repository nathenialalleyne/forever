package dev.forever.tools.assets;

import java.nio.file.Path;

/** Immutable command-line paths for one validation run. */
record AssetValidationOptions(Path assetsDirectory, Path manifestFile, Path jsonOutput) {

	static final Path DEFAULT_ASSETS_DIRECTORY = Path.of("src/client/resources/assets/forever");
	static final Path DEFAULT_MANIFEST_FILE = Path.of("docs/assets/asset-manifest.csv");
}

package dev.forever.tools.assets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Coordinates one bounded inventory pass and the independent named checks. */
final class AssetValidator {

	AssetValidationReport validate(Path assetsDirectory, Path manifestFile)
			throws IOException, UsageException {
		requireAssetsDirectory(assetsDirectory);
		requireManifestFile(manifestFile);

		AssetManifest manifest = ManifestReader.read(manifestFile);
		AssetInventory inventory = AssetInventoryScanner.scan(assetsDirectory);
		ImageInspector inspector = new ImageInspector();
		List<ProductionAsset> productionAssets = inventory.productionPngs().stream()
				.map(file -> new ProductionAsset(file, inspector.inspect(file.path())))
				.toList();
		AssetValidationContext context = new AssetValidationContext(
				assetsDirectory.toAbsolutePath().normalize(), inventory, productionAssets, manifest);

		List<AssetRule> rules = List.of(
				new NameNotSnakeCaseCheck(),
				new ImageFormatCheck(),
				new UnexpectedDimensionsCheck(),
				new FullyTransparentCheck(),
				new MissingTextureReferenceCheck(),
				new DuplicateAssetIdCheck(),
				new UnmanifestedAssetCheck(),
				new ManifestEntryMissingAssetCheck());
		List<AssetViolation> violations = new ArrayList<>();
		for (AssetRule rule : rules) {
			violations.addAll(rule.check(context));
		}
		return new AssetValidationReport(productionAssets, violations);
	}

	private static void requireAssetsDirectory(Path assetsDirectory) throws UsageException {
		if (assetsDirectory == null) {
			throw new UsageException("Assets directory path is missing. Provide --assets <dir>.");
		}
		if (!Files.exists(assetsDirectory)) {
			throw new UsageException("Assets directory does not exist: " + assetsDirectory
					+ ". Create it or provide --assets <dir>.");
		}
		if (!Files.isDirectory(assetsDirectory)) {
			throw new UsageException("Assets path is not a directory: " + assetsDirectory
					+ ". Provide --assets <dir> pointing to the asset namespace.");
		}
	}

	private static void requireManifestFile(Path manifestFile) throws UsageException {
		if (manifestFile == null) {
			throw new UsageException("Manifest path is missing. Provide --manifest <file>.");
		}
		if (!Files.exists(manifestFile)) {
			throw new UsageException("Manifest file does not exist: " + manifestFile
					+ ". Create it or provide --manifest <file>.");
		}
		if (!Files.isRegularFile(manifestFile)) {
			throw new UsageException("Manifest path is not a regular file: " + manifestFile
					+ ". Provide --manifest <file> pointing to the CSV manifest.");
		}
	}
}

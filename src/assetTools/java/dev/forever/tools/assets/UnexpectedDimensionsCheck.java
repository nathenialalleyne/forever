package dev.forever.tools.assets;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Applies the manifest whitelist and the 16x16 default to item and block textures. */
final class UnexpectedDimensionsCheck implements AssetRule {

	private static final Resolution DEFAULT_RESOLUTION = new Resolution(16, 16);

	@Override
	public ViolationCode code() {
		return ViolationCode.UNEXPECTED_DIMENSIONS;
	}

	@Override
	public List<AssetViolation> check(AssetValidationContext context) {
		List<AssetViolation> violations = new ArrayList<>();
		for (ProductionAsset asset : context.productionAssets()) {
			Path relativePath = asset.file().relativePath();
			if (!isItemOrBlockTexture(relativePath)) {
				continue;
			}
			ImageSummary image = asset.image();
			if (image.width() < 0 || image.height() < 0) {
				continue;
			}
			Resolution expected = context.manifest().firstRowFor(asset.assetId())
					.flatMap(row -> Resolution.parse(row.baseResolution()))
					.orElse(DEFAULT_RESOLUTION);
			if (image.width() != expected.width() || image.height() != expected.height()) {
				violations.add(new AssetViolation(code(), pathName(relativePath),
						"Image is " + image.dimensionsLabel() + ", but item and block textures require "
								+ expected.label() + " for this manifest entry or the default."));
			}
		}
		return violations;
	}

	private static boolean isItemOrBlockTexture(Path path) {
		String name = path.toString().replace(path.getFileSystem().getSeparator(), "/");
		return name.startsWith("textures/item/") || name.startsWith("textures/block/");
	}

	private static String pathName(Path path) {
		return path.toString().replace(path.getFileSystem().getSeparator(), "/");
	}
}

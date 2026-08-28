package dev.forever.tools.assets;

import java.util.ArrayList;
import java.util.List;

/** Rejects images whose every decoded pixel has zero alpha. */
final class FullyTransparentCheck implements AssetRule {

	@Override
	public ViolationCode code() {
		return ViolationCode.FULLY_TRANSPARENT;
	}

	@Override
	public List<AssetViolation> check(AssetValidationContext context) {
		List<AssetViolation> violations = new ArrayList<>();
		for (ProductionAsset asset : context.productionAssets()) {
			if (asset.image().fullyTransparent()) {
				violations.add(new AssetViolation(code(), pathName(asset),
						"Every pixel has alpha 0, so the PNG is fully transparent."));
			}
		}
		return violations;
	}

	private static String pathName(ProductionAsset asset) {
		return asset.file().relativePath().toString()
				.replace(asset.file().relativePath().getFileSystem().getSeparator(), "/");
	}
}

package dev.forever.tools.assets;

import java.util.ArrayList;
import java.util.List;

/** Requires every production PNG to have a corresponding manifest row. */
final class UnmanifestedAssetCheck implements AssetRule {

	@Override
	public ViolationCode code() {
		return ViolationCode.UNMANIFESTED_ASSET;
	}

	@Override
	public List<AssetViolation> check(AssetValidationContext context) {
		List<AssetViolation> violations = new ArrayList<>();
		for (ProductionAsset asset : context.productionAssets()) {
			if (context.manifest().firstRowFor(asset.assetId()).isEmpty()) {
				violations.add(new AssetViolation(code(), pathName(asset),
						"Production PNG asset_id '" + asset.assetId() + "' has no manifest row."));
			}
		}
		return violations;
	}

	private static String pathName(ProductionAsset asset) {
		return asset.file().relativePath().toString()
				.replace(asset.file().relativePath().getFileSystem().getSeparator(), "/");
	}
}

package dev.forever.tools.assets;

import java.util.ArrayList;
import java.util.List;

/** Requires placeholder and final manifest rows to have a production PNG. */
final class ManifestEntryMissingAssetCheck implements AssetRule {

	@Override
	public ViolationCode code() {
		return ViolationCode.MANIFEST_ENTRY_MISSING_ASSET;
	}

	@Override
	public List<AssetViolation> check(AssetValidationContext context) {
		List<AssetViolation> violations = new ArrayList<>();
		for (ManifestRow row : context.manifest().rows()) {
			if (row.status() == ManifestStatus.PLANNED || hasProductionAsset(context, row.assetId())) {
				continue;
			}
			violations.add(new AssetViolation(code(), row.assetId(),
					"Manifest row with status " + row.status() + " expects PNG asset_id '"
							+ row.assetId() + "', but no production PNG exists."));
		}
		return violations;
	}

	private static boolean hasProductionAsset(AssetValidationContext context, String assetId) {
		return context.productionAssets().stream().anyMatch(asset -> asset.assetId().equals(assetId));
	}
}

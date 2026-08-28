package dev.forever.tools.assets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Finds duplicate production stems and duplicate manifest asset_id rows. */
final class DuplicateAssetIdCheck implements AssetRule {

	@Override
	public ViolationCode code() {
		return ViolationCode.DUPLICATE_ASSET_ID;
	}

	@Override
	public List<AssetViolation> check(AssetValidationContext context) {
		List<AssetViolation> violations = new ArrayList<>();
		Map<String, List<ProductionAsset>> productionById = context.productionAssets().stream()
				.collect(Collectors.groupingBy(ProductionAsset::assetId, TreeMap::new, Collectors.toList()));
		for (Map.Entry<String, List<ProductionAsset>> entry : productionById.entrySet()) {
			if (entry.getValue().size() > 1) {
				String paths = entry.getValue().stream().map(DuplicateAssetIdCheck::pathName)
						.sorted().collect(Collectors.joining(", "));
				violations.add(new AssetViolation(code(), entry.getKey(),
						"Production asset_id '" + entry.getKey() + "' appears at multiple paths: " + paths + "."));
			}
		}
		for (Map.Entry<String, List<ManifestRow>> entry : context.manifest().rowsByAssetId().entrySet()) {
			if (entry.getValue().size() > 1) {
				violations.add(new AssetViolation(code(), entry.getKey(),
						"Manifest asset_id '" + entry.getKey() + "' appears in " + entry.getValue().size() + " rows."));
			}
		}
		return violations;
	}

	private static String pathName(ProductionAsset asset) {
		return asset.file().relativePath().toString()
				.replace(asset.file().relativePath().getFileSystem().getSeparator(), "/");
	}
}

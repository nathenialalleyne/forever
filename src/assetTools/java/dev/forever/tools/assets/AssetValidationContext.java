package dev.forever.tools.assets;

import java.nio.file.Path;
import java.util.List;

/** Read-only inputs shared by the independent checks. */
record AssetValidationContext(Path assetsDirectory, AssetInventory inventory,
		List<ProductionAsset> productionAssets, AssetManifest manifest) {

	AssetValidationContext {
		productionAssets = List.copyOf(productionAssets);
	}
}

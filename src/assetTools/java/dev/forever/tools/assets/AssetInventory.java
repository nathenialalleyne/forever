package dev.forever.tools.assets;

import java.nio.file.Path;
import java.util.List;

/** Deterministically ordered files and directories found below the asset root. */
record AssetInventory(List<AssetFile> files, List<AssetFile> productionPngs,
		List<AssetFile> referenceJsons, List<Path> directories) {

	AssetInventory {
		files = List.copyOf(files);
		productionPngs = List.copyOf(productionPngs);
		referenceJsons = List.copyOf(referenceJsons);
		directories = List.copyOf(directories);
	}
}

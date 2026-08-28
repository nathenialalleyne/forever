package dev.forever.tools.assets;

/** A production PNG and the result of its ImageIO inspection. */
record ProductionAsset(AssetFile file, ImageSummary image) {

	String assetId() {
		return file.stem();
	}
}

package dev.forever.tools.assets;

/** One RFC4180 manifest row with the exact columns defined by the asset contract. */
record ManifestRow(
		String assetId,
		String category,
		String system,
		String milestone,
		String requiredStates,
		String baseResolution,
		String reusesVanilla,
		String accessibilityRequirement,
		String notes,
		ManifestStatus status) {
}

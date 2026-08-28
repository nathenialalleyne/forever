package dev.forever.core.guide;

/**
 * Neutral recipe-viewer implementation used when no optional viewer is installed.
 *
 * <p>It reports absence as ordinary capability status and never changes guide data or
 * turns an absent client integration into a server error.
 */
public final class NoOpGuideRecipeViewerCapability implements GuideRecipeViewerCapability {

	public static final NoOpGuideRecipeViewerCapability INSTANCE = new NoOpGuideRecipeViewerCapability();

	private NoOpGuideRecipeViewerCapability() {
	}

	@Override
	public GuideRecipeViewerResult describe(GuideEntry entry) {
		if (entry == null) {
			return GuideRecipeViewerResult.invalid("No Field Guide entry was supplied to the recipe-viewer seam.");
		}
		return GuideRecipeViewerResult.unavailable(
				entry.id(), "No optional recipe viewer is installed; the Field Guide entry remains available.");
	}
}

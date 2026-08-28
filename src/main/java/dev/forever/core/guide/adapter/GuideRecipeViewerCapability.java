package dev.forever.core.guide.adapter;

/**
 * Optional capability used by a recipe viewer to expose recipes for a validated guide entry.
 *
 * <p>This contract contains only Forever-owned types. REI and JEI are deliberately not
 * dependencies of Forever because either viewer may be absent, replaced, or updated on a
 * different schedule. A viewer-specific adapter can implement this seam later without
 * making common/server loading depend on a client library.
 */
@FunctionalInterface
public interface GuideRecipeViewerCapability {

	/** Returns a versioned, bounded view or a neutral unavailable result. */
	GuideRecipeViewerResult describe(GuideEntry entry);
}

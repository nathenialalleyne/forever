package dev.forever.core.guide.adapter;

import java.util.Objects;
import net.minecraft.resources.Identifier;

/** Bounded server projection of one ranked Field Guide entry. */
public record GuideSearchHit(
		Identifier entryId,
		String titleKey,
		String category,
		int relevance,
		boolean accessible) {

	public GuideSearchHit {
		if (!GuideEntry.isGuideIdentifier(entryId)) {
			throw new IllegalArgumentException("Guide search hit must use a Forever guide ID.");
		}
		Objects.requireNonNull(titleKey, "guide search title key must not be null");
		Objects.requireNonNull(category, "guide search category must not be null");
		if (relevance < 0) {
			throw new IllegalArgumentException("Guide search relevance cannot be negative.");
		}
	}

	public Identifier id() {
		return entryId;
	}

	public boolean available() {
		return accessible;
	}
}

package dev.forever.core.guide.adapter;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable, bounded request for server-side Field Guide search.
 *
 * <p>The request protocol version is part of the common contract so a future client
 * cannot accidentally interpret a newer response as the current shape. Oversized
 * result limits are clamped to the server maximum rather than allowing an unbounded
 * response.
 */
public record GuideSearchQuery(
		int protocolVersion,
		String text,
		Optional<String> category,
		int limit,
		GuideAccess access) {

	public static final int CURRENT_PROTOCOL_VERSION = 1;
	public static final int MAX_TEXT_LENGTH = 64;
	public static final int MAX_TERMS = 8;
	public static final int MAX_LIMIT = 32;

	public GuideSearchQuery(String text, Optional<String> category, int limit) {
		this(CURRENT_PROTOCOL_VERSION, text, category, limit, GuideAccess.empty());
	}

	public GuideSearchQuery(String text, int limit) {
		this(text, Optional.empty(), limit);
	}

	public GuideSearchQuery {
		if (protocolVersion != CURRENT_PROTOCOL_VERSION) {
			throw new IllegalArgumentException("Unsupported guide search protocol version " + protocolVersion
					+ "; this server understands version " + CURRENT_PROTOCOL_VERSION + ".");
		}
		Objects.requireNonNull(text, "guide search text must not be null");
		text = text.trim().toLowerCase(Locale.ROOT);
		if (text.length() > MAX_TEXT_LENGTH) {
			throw new IllegalArgumentException("Guide search text exceeds the maximum of " + MAX_TEXT_LENGTH + " characters.");
		}
		if (!text.isBlank() && text.split("\\s+").length > MAX_TERMS) {
			throw new IllegalArgumentException("Guide search text contains more than " + MAX_TERMS + " terms.");
		}
		Objects.requireNonNull(category, "guide search category must not be null");
		category = category.map(value -> {
			String normalised = value.trim().toLowerCase(Locale.ROOT);
			if (normalised.isBlank() || normalised.length() > GuideEntry.MAX_CATEGORY_LENGTH
					|| !normalised.matches("[a-z0-9][a-z0-9_.-]*")) {
				throw new IllegalArgumentException("Guide search category must be a bounded lowercase identifier.");
			}
			return normalised;
		});
		if (limit < 1) {
			throw new IllegalArgumentException("Guide search limit must be at least 1.");
		}
		limit = Math.min(limit, MAX_LIMIT);
		Objects.requireNonNull(access, "guide search access must not be null");
	}

	public static GuideSearchQuery all(int limit) {
		return new GuideSearchQuery("", Optional.empty(), limit);
	}

	public static GuideSearchQuery text(String text, int limit) {
		return new GuideSearchQuery(text, Optional.empty(), limit);
	}

	public static GuideSearchQuery category(String category, int limit) {
		return new GuideSearchQuery("", Optional.of(category), limit);
	}

	public GuideSearchQuery withAccess(GuideAccess nextAccess) {
		return new GuideSearchQuery(protocolVersion, text, category, limit, nextAccess);
	}

	/** Alias that makes call sites using the search-as-keyword terminology explicit. */
	public String keyword() {
		return text;
	}
}

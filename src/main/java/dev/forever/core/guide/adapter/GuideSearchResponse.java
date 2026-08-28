package dev.forever.core.guide.adapter;

import java.util.List;
import java.util.Objects;

/** Versioned, bounded response for a server-side Field Guide search. */
public record GuideSearchResponse(
		int protocolVersion,
		List<GuideSearchHit> results,
		boolean truncated) {

	public static final int CURRENT_PROTOCOL_VERSION = GuideSearchQuery.CURRENT_PROTOCOL_VERSION;
	public static final int MAX_RESULTS = GuideSearchQuery.MAX_LIMIT;

	public GuideSearchResponse {
		if (protocolVersion != CURRENT_PROTOCOL_VERSION) {
			throw new IllegalArgumentException("Unsupported guide response protocol version " + protocolVersion
					+ "; this server understands version " + CURRENT_PROTOCOL_VERSION + ".");
		}
		Objects.requireNonNull(results, "guide search results must not be null");
		if (results.size() > MAX_RESULTS) {
			throw new IllegalArgumentException("Guide search response exceeds the maximum of " + MAX_RESULTS + " results.");
		}
		if (results.stream().anyMatch(Objects::isNull)) {
			throw new IllegalArgumentException("Guide search results cannot contain null hits.");
		}
		results = List.copyOf(results);
	}

	public List<GuideSearchHit> hits() {
		return results;
	}

	public boolean isTruncated() {
		return truncated;
	}
}

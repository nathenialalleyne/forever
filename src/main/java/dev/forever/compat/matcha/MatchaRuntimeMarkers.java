package dev.forever.compat.matcha;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * Exact runtime observations supplied by server-side datapack inspection.
 *
 * <p>Raw objective and advancement names are accepted only at this adapter
 * boundary. They are never returned by a core-facing translation result.
 */
public record MatchaRuntimeMarkers(
		boolean observationsAvailable,
		Set<String> scoreboardObjectives,
		Map<String, Integer> scoreboardValues,
		Set<String> advancementIds) {

	private static final int MAX_IDENTIFIER_LENGTH = 256;
	private static final int MAX_ENTRIES = 256;

	public MatchaRuntimeMarkers {
		scoreboardObjectives = immutableIdentifiers(scoreboardObjectives, "scoreboard objective");
		scoreboardValues = immutableScores(scoreboardValues);
		advancementIds = immutableIdentifiers(advancementIds, "advancement ID");
	}

	public static MatchaRuntimeMarkers unavailable() {
		return new MatchaRuntimeMarkers(false, Set.of(), Map.of(), Set.of());
	}

	public static MatchaRuntimeMarkers observed(
			Set<String> scoreboardObjectives,
			Map<String, Integer> scoreboardValues,
			Set<String> advancementIds) {
		return new MatchaRuntimeMarkers(true, scoreboardObjectives, scoreboardValues, advancementIds);
	}

	private static Set<String> immutableIdentifiers(Set<String> values, String field) {
		Objects.requireNonNull(values, field + " set must not be null.");
		if (values.size() > MAX_ENTRIES) {
			throw new IllegalArgumentException(field + " set exceeds the maximum of " + MAX_ENTRIES + " entries.");
		}
		Set<String> copy = new LinkedHashSet<>();
		for (String value : values) {
			if (value == null || value.isBlank() || value.length() > MAX_IDENTIFIER_LENGTH) {
				throw new IllegalArgumentException("Invalid " + field + " value.");
			}
			copy.add(value);
		}
		return Collections.unmodifiableSet(copy);
	}

	private static Map<String, Integer> immutableScores(Map<String, Integer> values) {
		Objects.requireNonNull(values, "scoreboard values must not be null.");
		if (values.size() > MAX_ENTRIES) {
			throw new IllegalArgumentException("Scoreboard values exceed the maximum of " + MAX_ENTRIES + " entries.");
		}
		Map<String, Integer> copy = new TreeMap<>();
		for (Map.Entry<String, Integer> entry : values.entrySet()) {
			String key = entry.getKey();
			if (key == null || key.isBlank() || key.length() > MAX_IDENTIFIER_LENGTH || entry.getValue() == null) {
				throw new IllegalArgumentException("Invalid scoreboard value entry.");
			}
			copy.put(key, entry.getValue());
		}
		return Collections.unmodifiableMap(copy);
	}
}

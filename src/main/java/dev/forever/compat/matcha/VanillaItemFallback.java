package dev.forever.compat.matcha;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Vanilla item facts retained when an identity translation is unavailable.
 *
 * <p>The adapter deliberately removes profile identity components before this
 * value crosses the boundary. The caller can therefore preserve ordinary item
 * data without receiving a Matcha model, entity marker, or custom-model value.
 */
public record VanillaItemFallback(String baseItemId, Map<String, String> components, int count) {

	public VanillaItemFallback {
		baseItemId = requireId(baseItemId);
		components = immutableComponents(components);
		if (count < 1 || count > 99) {
			throw new IllegalArgumentException("Item count must be between 1 and 99.");
		}
	}

	static VanillaItemFallback fromObservation(MatchaItemObservation observation) {
		Objects.requireNonNull(observation, "item observation must not be null.");
		TreeMap<String, String> preserved = new TreeMap<>();
		observation.components().forEach((key, value) -> {
			if (!MatchaProfile.IDENTITY_COMPONENTS.contains(key)) {
				preserved.put(key, value);
			}
		});
		return new VanillaItemFallback(observation.baseItemId(), preserved, observation.count());
	}

	private static String requireId(String value) {
		Objects.requireNonNull(value, "base item ID must not be null.");
		if (value.isBlank() || value.length() > 256 || !value.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) {
			throw new IllegalArgumentException("Invalid base item ID.");
		}
		return value;
	}

	private static Map<String, String> immutableComponents(Map<String, String> values) {
		Objects.requireNonNull(values, "components must not be null.");
		if (values.size() > 64) {
			throw new IllegalArgumentException("Item components exceed the maximum of 64 entries.");
		}
		TreeMap<String, String> copy = new TreeMap<>();
		for (Map.Entry<String, String> entry : values.entrySet()) {
			if (entry.getKey() == null || entry.getKey().isBlank() || entry.getKey().length() > 256
					|| entry.getValue() == null || entry.getValue().length() > 4096) {
				throw new IllegalArgumentException("Invalid item component.");
			}
			copy.put(entry.getKey(), entry.getValue());
		}
		return Collections.unmodifiableMap(copy);
	}
}

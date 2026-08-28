package dev.forever.compat.matcha;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Server-side item facts presented to the adapter for identity translation.
 * Display names, colours, and textures are deliberately not part of the type.
 */
public record MatchaItemObservation(
		String baseItemId,
		Optional<String> itemModelId,
		Map<String, String> components,
		int count) {

	public MatchaItemObservation {
		baseItemId = requireId(baseItemId, "base item ID");
		itemModelId = normaliseOptionalId(itemModelId, "item model ID");
		components = immutableComponents(components);
		if (count < 1 || count > 99) {
			throw new IllegalArgumentException("Item count must be between 1 and 99.");
		}
	}

	public MatchaItemObservation(String baseItemId, String itemModelId, int count) {
		this(baseItemId, optionalId(itemModelId), Map.of(), count);
	}

	public MatchaItemObservation(String baseItemId, Optional<String> itemModelId, int count) {
		this(baseItemId, itemModelId, Map.of(), count);
	}

	private static Optional<String> optionalId(String value) {
		return value == null || value.isBlank() ? Optional.empty() : Optional.of(value);
	}

	private static Optional<String> normaliseOptionalId(Optional<String> value, String field) {
		Objects.requireNonNull(value, field + " must not be null.");
		return value.map(item -> requireId(item, field));
	}

	private static String requireId(String value, String field) {
		Objects.requireNonNull(value, field + " must not be null.");
		if (value.isBlank() || value.length() > 256 || !value.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) {
			throw new IllegalArgumentException("Invalid " + field + ".");
		}
		return value;
	}

	private static Map<String, String> immutableComponents(Map<String, String> values) {
		Objects.requireNonNull(values, "components must not be null.");
		if (values.size() > 64) {
			throw new IllegalArgumentException("Item components exceed the maximum of 64 entries.");
		}
		Map<String, String> copy = new TreeMap<>();
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

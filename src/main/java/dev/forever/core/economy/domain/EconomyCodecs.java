package dev.forever.core.economy.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Codec helpers local to the economy records. */
final class EconomyCodecs {

	private EconomyCodecs() {
	}

	static <E extends Enum<E>> Codec<E> enumCodec(Class<E> enumType) {
		return Codec.STRING.comapFlatMap(
				value -> {
					try {
						return DataResult.success(Enum.valueOf(enumType, value.toUpperCase(Locale.ROOT)));
					} catch (IllegalArgumentException exception) {
						return DataResult.error(() -> "Unknown " + enumType.getSimpleName()
								+ " in economy data: " + value);
					}
				},
				value -> value.name().toLowerCase(Locale.ROOT));
	}

	static String requiredText(String value, String field, int maximum) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Economy " + field + " must not be blank.");
		}
		String trimmed = value.trim();
		if (trimmed.length() > maximum) {
			throw new IllegalArgumentException("Economy " + field + " exceeds " + maximum + " characters.");
		}
		return trimmed;
	}

	static <T> Codec<Set<T>> boundedSet(Codec<T> elementCodec, int maximum, String label) {
		return elementCodec.listOf().validate(values -> {
			if (values.size() > maximum) {
				return DataResult.error(() -> "Economy " + label + " contains too many entries.");
			}
			Set<T> distinct = new LinkedHashSet<>(values);
			if (distinct.size() != values.size()) {
				return DataResult.error(() -> "Economy " + label + " contains duplicate entries.");
			}
			return DataResult.success(List.copyOf(values));
		}).comapFlatMap(
				values -> DataResult.success(Set.copyOf(new LinkedHashSet<>(values))),
				values -> values.stream().sorted(Comparator.comparing(Object::toString)).toList());
	}

	static <T> Set<T> copySet(Set<T> values, String field, int maximum) {
		if (values == null || values.size() > maximum) {
			throw new IllegalArgumentException("Economy " + field + " is null or exceeds its bound.");
		}
		Set<T> copy = new LinkedHashSet<>();
		for (T value : values) {
			if (value == null) {
				throw new IllegalArgumentException("Economy " + field + " cannot contain null.");
			}
			copy.add(value);
		}
		return Set.copyOf(new ArrayList<>(copy));
	}
}

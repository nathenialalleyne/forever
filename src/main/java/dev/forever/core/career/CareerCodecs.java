package dev.forever.core.career;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Codec helpers local to the career records. Persistent career data is bounded at every edge. */
final class CareerCodecs {

	static final Codec<UUID> UUID_CODEC = Codec.STRING.comapFlatMap(
			value -> {
				try {
					return DataResult.success(UUID.fromString(value));
				} catch (IllegalArgumentException exception) {
					return DataResult.error(() -> "Invalid UUID in career data: " + value);
				}
			},
			UUID::toString);

	private CareerCodecs() {
	}

	static <E extends Enum<E>> Codec<E> enumCodec(Class<E> enumType) {
		return Codec.STRING.comapFlatMap(
				value -> {
					try {
						return DataResult.success(Enum.valueOf(enumType, value.toUpperCase(Locale.ROOT)));
					} catch (IllegalArgumentException exception) {
						return DataResult.error(() -> "Unknown " + enumType.getSimpleName()
								+ " in career data: " + value);
					}
				},
				value -> value.name().toLowerCase(Locale.ROOT));
	}

	static <T> Codec<List<T>> boundedList(Codec<T> elementCodec, int maximum, String label) {
		return elementCodec.listOf().validate(values -> {
			if (values.size() > maximum) {
				return DataResult.error(() -> "Career " + label + " contains " + values.size()
						+ " entries, exceeding the bounded limit of " + maximum + ".");
			}
			return DataResult.success(List.copyOf(values));
		});
	}

	static <T> Codec<Set<T>> boundedSet(Codec<T> elementCodec, int maximum, String label) {
		return boundedList(elementCodec, maximum, label).comapFlatMap(
				values -> {
					Set<T> distinct = new LinkedHashSet<>(values);
					if (distinct.size() != values.size()) {
						return DataResult.error(() -> "Career " + label + " contains duplicate entries.");
					}
					return DataResult.success(Set.copyOf(distinct));
				},
				values -> values.stream()
						.sorted(Comparator.comparing(Object::toString))
						.toList());
	}

	static String requiredText(String value, String field, int maximum) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Career " + field + " must not be blank.");
		}
		String trimmed = value.trim();
		if (trimmed.length() > maximum) {
			throw new IllegalArgumentException("Career " + field + " exceeds " + maximum + " characters.");
		}
		return trimmed;
	}

	static <T> Set<T> copySet(Set<T> values, String field, int maximum) {
		if (values == null) {
			throw new IllegalArgumentException("Career " + field + " cannot be null.");
		}
		if (values.size() > maximum) {
			throw new IllegalArgumentException("Career " + field + " contains too many entries.");
		}
		Set<T> copy = new LinkedHashSet<>();
		for (T value : values) {
			if (value == null) {
				throw new IllegalArgumentException("Career " + field + " cannot contain null.");
			}
			copy.add(value);
		}
		return Set.copyOf(new ArrayList<>(copy));
	}
}

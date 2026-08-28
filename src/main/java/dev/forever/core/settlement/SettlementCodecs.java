package dev.forever.core.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Small codec helpers shared by the immutable settlement records. */
final class SettlementCodecs {

	static final Codec<UUID> UUID_CODEC = Codec.STRING.comapFlatMap(
			value -> {
				try {
					return DataResult.success(UUID.fromString(value));
				} catch (IllegalArgumentException exception) {
					return DataResult.error(() -> "Invalid UUID in settlement data: " + value);
				}
			},
			UUID::toString);

	private SettlementCodecs() {
	}

	static <E extends Enum<E>> Codec<E> enumCodec(Class<E> enumType) {
		return Codec.STRING.comapFlatMap(
				value -> {
					try {
						return DataResult.success(Enum.valueOf(enumType, value.toUpperCase(Locale.ROOT)));
					} catch (IllegalArgumentException exception) {
						return DataResult.error(() -> "Unknown " + enumType.getSimpleName()
								+ " in settlement data: " + value);
					}
				},
				value -> value.name().toLowerCase(Locale.ROOT));
	}

	static <T> Codec<List<T>> boundedList(Codec<T> elementCodec, int maximum, String label) {
		return elementCodec.listOf().validate(values -> {
			if (values.size() > maximum) {
				return DataResult.error(() -> "Settlement " + label + " contains " + values.size()
						+ " entries, exceeding the bounded limit of " + maximum + ".");
			}
			return DataResult.success(List.copyOf(values));
		});
	}

	static <K, V> Codec<Map<K, V>> boundedMap(
			Codec<K> keyCodec, Codec<V> valueCodec, int maximum, String label) {
		return Codec.unboundedMap(keyCodec, valueCodec).validate(values -> {
			if (values.size() > maximum) {
				return DataResult.error(() -> "Settlement " + label + " contains " + values.size()
						+ " entries, exceeding the bounded limit of " + maximum + ".");
			}
			return DataResult.success(Map.copyOf(values));
		});
	}

	static <E extends Enum<E>> Codec<Set<E>> enumSetCodec(
			Class<E> enumType, int maximum, String label) {
		return setCodec(enumCodec(enumType), maximum, label);
	}

	static <T> Codec<Set<T>> setCodec(Codec<T> elementCodec, int maximum, String label) {
		return boundedList(elementCodec, maximum, label).comapFlatMap(
				values -> {
					Set<T> distinct = new LinkedHashSet<>(values);
					if (distinct.size() != values.size()) {
						return DataResult.error(() -> "Settlement " + label
								+ " contains duplicate entries.");
					}
					return DataResult.success(Set.copyOf(distinct));
				},
				values -> values.stream()
						.sorted(Comparator.comparing(Object::toString))
						.toList());
	}

	static String requiredText(String value, String field, int maximum) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Settlement " + field + " must not be blank.");
		}
		String trimmed = value.trim();
		if (trimmed.length() > maximum) {
			throw new IllegalArgumentException("Settlement " + field + " exceeds " + maximum
					+ " characters.");
		}
		return trimmed;
	}

	static <E extends Enum<E>> Set<E> copyEnumSet(Set<E> values, Class<E> enumType, String field) {
		if (values == null || values.isEmpty()) {
			throw new IllegalArgumentException("Settlement " + field + " must not be empty.");
		}
		EnumSet<E> copy = EnumSet.noneOf(enumType);
		for (E value : values) {
			if (value == null) {
				throw new IllegalArgumentException("Settlement " + field + " cannot contain null.");
			}
			copy.add(value);
		}
		return Set.copyOf(new ArrayList<>(copy));
	}
}

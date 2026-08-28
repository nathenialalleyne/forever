package dev.forever.compat.matcha;

import java.util.Objects;
import java.util.Optional;

/**
 * Validated, server-observed values from a datapack's pack metadata.
 *
 * <p>The adapter accepts a pack ID when the server exposes one, but does not
 * treat a display name alone as proof of Matcha identity.
 */
public record MatchaPackMetadata(
		Optional<String> packId,
		String description,
		double minFormat,
		double maxFormat,
		Optional<String> declaredVersion) {

	private static final int MAX_DESCRIPTION_LENGTH = 4096;
	private static final int MAX_VALUE_LENGTH = 512;

	public MatchaPackMetadata {
		packId = normaliseOptional(packId, "pack ID");
		description = requireLength(description, "description", MAX_DESCRIPTION_LENGTH);
		declaredVersion = normaliseOptional(declaredVersion, "declared version");
		if (!Double.isFinite(minFormat) || !Double.isFinite(maxFormat)) {
			throw new IllegalArgumentException("Pack format values must be finite numbers.");
		}
		if (minFormat > maxFormat) {
			throw new IllegalArgumentException("Pack minimum format cannot exceed maximum format.");
		}
	}

	public MatchaPackMetadata(String packId, String description, double minFormat, double maxFormat) {
		this(toOptional(packId, "pack ID"), description, minFormat, maxFormat, Optional.empty());
	}

	public MatchaPackMetadata(String description, double minFormat, double maxFormat) {
		this(Optional.empty(), description, minFormat, maxFormat, Optional.empty());
	}

	public MatchaPackMetadata withDeclaredVersion(String version) {
		return new MatchaPackMetadata(packId, description, minFormat, maxFormat,
				toOptional(version, "declared version"));
	}

	private static Optional<String> normaliseOptional(Optional<String> value, String field) {
		Objects.requireNonNull(value, field + " must not be null.");
		return value.map(item -> requireLength(item, field, MAX_VALUE_LENGTH));
	}

	private static Optional<String> toOptional(String value, String field) {
		return value == null || value.isBlank() ? Optional.empty() : Optional.of(requireLength(value, field, MAX_VALUE_LENGTH));
	}

	private static String requireLength(String value, String field, int maxLength) {
		Objects.requireNonNull(value, field + " must not be null.");
		if (value.length() > maxLength) {
			throw new IllegalArgumentException(field + " exceeds the maximum length of " + maxLength + ".");
		}
		return value;
	}
}

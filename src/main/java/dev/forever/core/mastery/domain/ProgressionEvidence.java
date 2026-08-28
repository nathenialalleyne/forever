package dev.forever.core.mastery.domain;

import java.util.Locale;
import java.util.Objects;

/**
 * A server-validated accomplishment candidate.
 *
 * <p>The event identity and normalized evidence key make retries idempotent. Callers must
 * submit a meaningful event identity, not a per-action increment.
 */
public record ProgressionEvidence(
		String eventId,
		EvidenceKind kind,
		String dimension,
		String key) {

	public ProgressionEvidence {
		eventId = normalise(eventId, "event ID", 128);
		kind = Objects.requireNonNull(kind, "evidence kind");
		dimension = normalise(dimension, "evidence dimension", 64);
		key = normalise(key, "evidence key", 192);
	}

	/** Stable key stored in the mastery evidence set. */
	public String normalizedEvidenceKey() {
		return kind.serializedName() + ":" + dimension + ":" + key;
	}

	private static String normalise(String value, String name, int maxLength) {
		Objects.requireNonNull(value, name);
		String normalised = value.trim().toLowerCase(Locale.ROOT);
		if (normalised.isEmpty() || normalised.length() > maxLength) {
			throw new IllegalArgumentException(
					"Mastery " + name + " must be non-blank and at most " + maxLength + " characters.");
		}
		if (!normalised.matches("[a-z0-9][a-z0-9_./:-]*")) {
			throw new IllegalArgumentException(
					"Mastery " + name + " contains unsupported characters: '" + value + "'.");
		}
		return normalised;
	}
}

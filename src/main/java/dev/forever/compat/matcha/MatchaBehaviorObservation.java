package dev.forever.compat.matcha;

import java.util.Objects;

/** A bounded, exact server observation sent to a behaviour translator. */
public record MatchaBehaviorObservation(MatchaSignalKind kind, String identifier, Integer value) {

	public MatchaBehaviorObservation {
		Objects.requireNonNull(kind, "signal kind must not be null.");
		if (identifier == null || identifier.isBlank() || identifier.length() > 256) {
			throw new IllegalArgumentException("Signal identifier must be non-blank and bounded.");
		}
	}

	public MatchaBehaviorObservation(MatchaSignalKind kind, String identifier) {
		this(kind, identifier, null);
	}
}

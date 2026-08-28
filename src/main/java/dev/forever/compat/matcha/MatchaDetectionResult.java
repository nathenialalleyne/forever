package dev.forever.compat.matcha;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Immutable result of the Matcha version detector. */
public record MatchaDetectionResult(
		MatchaDetectionStatus status,
		MatchaPresence presence,
		MatchaSupportLevel supportLevel,
		MatchaConfidence confidence,
		Optional<String> detectedVersion,
		List<String> diagnostics) {

	public MatchaDetectionResult {
		Objects.requireNonNull(status, "status must not be null.");
		Objects.requireNonNull(presence, "presence must not be null.");
		Objects.requireNonNull(supportLevel, "support level must not be null.");
		Objects.requireNonNull(confidence, "confidence must not be null.");
		Objects.requireNonNull(detectedVersion, "detected version must not be null.");
		Objects.requireNonNull(diagnostics, "diagnostics must not be null.");
		if (diagnostics.stream().anyMatch(item -> item == null || item.isBlank())) {
			throw new IllegalArgumentException("Diagnostics must be non-blank.");
		}
		diagnostics = List.copyOf(diagnostics);
	}

	public boolean isSupported() {
		return status == MatchaDetectionStatus.SUPPORTED;
	}

	public static MatchaDetectionResult absent() {
		return new MatchaDetectionResult(
				MatchaDetectionStatus.ABSENT,
				MatchaPresence.ABSENT,
				MatchaSupportLevel.NONE,
				MatchaConfidence.NOT_APPLICABLE,
				Optional.empty(),
				List.of("No Matcha datapack signal was observed; the vanilla-safe fallback is active."));
	}

	public static MatchaDetectionResult failedSafe(String diagnostic) {
		return new MatchaDetectionResult(
				MatchaDetectionStatus.FAILED_SAFE,
				MatchaPresence.MALFORMED,
				MatchaSupportLevel.NONE,
				MatchaConfidence.UNVERIFIED,
				Optional.empty(),
				List.of(diagnostic));
	}
}

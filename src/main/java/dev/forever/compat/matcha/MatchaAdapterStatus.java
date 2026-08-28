package dev.forever.compat.matcha;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Stable support snapshot exposed by a Matcha adapter. */
public record MatchaAdapterStatus(
		String adapterId,
		MatchaDetectionStatus detectionStatus,
		MatchaPresence presence,
		MatchaSupportLevel supportLevel,
		MatchaConfidence confidence,
		Optional<String> detectedVersion,
		List<String> diagnostics) {

	public MatchaAdapterStatus {
		if (adapterId == null || adapterId.isBlank() || adapterId.length() > 64) {
			throw new IllegalArgumentException("Adapter ID must be non-blank and bounded.");
		}
		Objects.requireNonNull(detectionStatus, "detection status must not be null.");
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

	public static MatchaAdapterStatus fromDetection(MatchaDetectionResult result) {
		Objects.requireNonNull(result, "detection result must not be null.");
		return new MatchaAdapterStatus(
				"matcha",
				result.status(),
				result.presence(),
				result.supportLevel(),
				result.confidence(),
				result.detectedVersion(),
				result.diagnostics());
	}
}

package dev.forever.compat.matcha;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Safe result of translating one Matcha behaviour observation. */
public record MatchaBehaviorTranslation(
		MatchaTranslationStatus status,
		Optional<String> stableConceptId,
		Optional<String> profileId,
		Optional<String> evidenceId,
		List<String> diagnostics) {

	public MatchaBehaviorTranslation {
		Objects.requireNonNull(status, "translation status must not be null.");
		Objects.requireNonNull(stableConceptId, "stable concept must not be null.");
		Objects.requireNonNull(profileId, "profile ID must not be null.");
		Objects.requireNonNull(evidenceId, "evidence ID must not be null.");
		Objects.requireNonNull(diagnostics, "diagnostics must not be null.");
		if (stableConceptId.isPresent() && !stableConceptId.get().matches("forever\\.[a-z0-9_.-]+")) {
			throw new IllegalArgumentException("Stable concept IDs must be Forever-owned IDs.");
		}
		if (diagnostics.stream().anyMatch(item -> item == null || item.isBlank())) {
			throw new IllegalArgumentException("Diagnostics must be non-blank.");
		}
		diagnostics = List.copyOf(diagnostics);
	}

	public static MatchaBehaviorTranslation mapped(String conceptId, String profileId, String evidenceId) {
		return new MatchaBehaviorTranslation(
				MatchaTranslationStatus.MAPPED,
				Optional.of(conceptId),
				Optional.of(profileId),
				Optional.of(evidenceId),
				List.of());
	}

	public static MatchaBehaviorTranslation unavailable(String diagnostic) {
		return new MatchaBehaviorTranslation(
				MatchaTranslationStatus.UNAVAILABLE,
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				List.of(diagnostic));
	}

	public static MatchaBehaviorTranslation unmapped(String diagnostic) {
		return new MatchaBehaviorTranslation(
				MatchaTranslationStatus.UNMAPPED,
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				List.of(diagnostic));
	}

	public static MatchaBehaviorTranslation invalid(String diagnostic) {
		return new MatchaBehaviorTranslation(
				MatchaTranslationStatus.INVALID,
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				List.of(diagnostic));
	}
}

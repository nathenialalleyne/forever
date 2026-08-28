package dev.forever.compat.matcha;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Safe result of translating a server-observed item signature. */
public record MatchaItemTranslation(
		MatchaTranslationStatus status,
		Optional<String> stableConceptId,
		Optional<VanillaItemFallback> original,
		Optional<String> profileId,
		Optional<String> evidenceId,
		List<String> diagnostics) {

	public MatchaItemTranslation {
		Objects.requireNonNull(status, "translation status must not be null.");
		Objects.requireNonNull(stableConceptId, "stable concept must not be null.");
		Objects.requireNonNull(original, "original fallback must not be null.");
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

	public static MatchaItemTranslation mapped(
			String conceptId, MatchaItemObservation original, String profileId, String evidenceId) {
		return new MatchaItemTranslation(
				MatchaTranslationStatus.MAPPED,
				Optional.of(conceptId),
				optionalFallback(original),
				Optional.of(profileId),
				Optional.of(evidenceId),
				List.of());
	}

	public static MatchaItemTranslation unavailable(MatchaItemObservation original, String diagnostic) {
		return new MatchaItemTranslation(
				MatchaTranslationStatus.UNAVAILABLE,
				Optional.empty(),
				optionalFallback(original),
				Optional.empty(),
				Optional.empty(),
				List.of(diagnostic));
	}

	public static MatchaItemTranslation unmapped(MatchaItemObservation original, String diagnostic) {
		return new MatchaItemTranslation(
				MatchaTranslationStatus.UNMAPPED,
				Optional.empty(),
				optionalFallback(original),
				Optional.empty(),
				Optional.empty(),
				List.of(diagnostic));
	}

	public static MatchaItemTranslation invalid(String diagnostic) {
		return new MatchaItemTranslation(
				MatchaTranslationStatus.INVALID,
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				List.of(diagnostic));
	}

	private static Optional<VanillaItemFallback> optionalFallback(MatchaItemObservation observation) {
		return observation == null
				? Optional.empty()
				: Optional.of(VanillaItemFallback.fromObservation(observation));
	}
}

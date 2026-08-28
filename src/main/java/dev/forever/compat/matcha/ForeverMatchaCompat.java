package dev.forever.compat.matcha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Entry point and lifecycle holder for the isolated Matcha capability. */
public final class ForeverMatchaCompat {
	private static final Logger LOGGER = LoggerFactory.getLogger("forever-matcha-compat");

	private static volatile MatchaAdapter active = NoOpMatchaAdapter.absent();

	private ForeverMatchaCompat() {
	}

	/**
	 * Installs the safe absent-pack state. A server lifecycle caller still needs to
	 * provide datapack evidence through {@link #initialize(MatchaDetectionEvidence)}.
	 */
	public static MatchaAdapter initialize() {
		MatchaAdapter next = NoOpMatchaAdapter.absent();
		active = next;
		LOGGER.info("Matcha compatibility initialized in vanilla-safe no-op mode; server datapack signal wiring is still required.");
		return next;
	}

	/** Detects and atomically publishes the verified adapter or its no-op fallback. */
	public static MatchaAdapter initialize(MatchaDetectionEvidence evidence) {
		MatchaAdapter next;
		try {
			next = MatchaAdapters.fromEvidence(evidence);
		} catch (RuntimeException exception) {
			LOGGER.error("Matcha detection failed safely; mappings remain disabled. Check the pinned archive and evidence source.",
					exception);
			next = NoOpMatchaAdapter.fromDetection(MatchaDetectionResult.failedSafe(
					"Matcha detection failed while validating external evidence. See the server log for the validation context."));
		}
		active = next;
		logDecision(next.status());
		return next;
	}

	public static MatchaAdapter active() {
		return active;
	}

	public static MatchaAdapterStatus status() {
		return active.status();
	}

	private static void logDecision(MatchaAdapterStatus status) {
		switch (status.detectionStatus()) {
			case SUPPORTED -> LOGGER.info("Matcha {} detected with exact profile {}.",
					status.detectedVersion().orElse("unknown"), MatchaProfile.PROFILE_ID);
			case ABSENT -> LOGGER.info("Matcha is absent; vanilla-safe no-op capability remains active.");
			case UNSUPPORTED_VERSION, PRESENT_UNVERIFIED, MALFORMED, FAILED_SAFE ->
					LOGGER.warn("Matcha capability disabled with status {}. {}", status.detectionStatus(),
							String.join(" ", status.diagnostics()));
		}
	}
}

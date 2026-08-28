package dev.forever.compat.matcha;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
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

		// Detection needs a running server, because a datapack exposes no mod-version
		// API and its namespaces only exist once resources are loaded. Re-detect after
		// a datapack reload too, so /reload cannot leave a stale decision in place.
		ServerLifecycleEvents.SERVER_STARTED.register(
				server -> initialize(MatchaServerEvidence.gather(server)));
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
				(server, resources, success) -> {
					if (success) {
						initialize(MatchaServerEvidence.gather(server));
					}
				});

		LOGGER.info("Matcha compatibility registered; Forever's mappings stay inactive until server datapack signals prove the pack's identity.");
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
		reportBaseline(next.status());
		return next;
	}

	public static MatchaAdapter active() {
		return active;
	}

	public static MatchaAdapterStatus status() {
		return active.status();
	}

	/**
	 * Reports the health of the gameplay baseline, loudly when it is not healthy.
	 *
	 * <p>LAB-02 established that the pack loader fails open: a missing Matcha archive
	 * produces a perfectly normal server start with no diagnostic. This is the pack's
	 * own safety net for that, and it is deliberately separate from
	 * {@link #logDecision} because the two answer different questions. logDecision says
	 * whether Forever's mappings are active; this says whether the player is getting the
	 * game they installed.
	 */
	private static void reportBaseline(MatchaAdapterStatus status) {
		MatchaBaselineReport report = MatchaBaselineReport.fromStatus(status, MatchaProfile.VERSION);
		if (!report.needsAttention()) {
			LOGGER.info("{}", report.summary());
			return;
		}
		// Severity picks the log level deliberately. MISSING means the player is not
		// playing this pack at all, which is worth a framed banner at ERROR. DEGRADED
		// means the gameplay content is running and only Forever's translation layer is
		// inactive, which is a warning: shouting ERROR at a working install is how a
		// project teaches its operators to ignore errors.
		if (report.severity() == MatchaBaselineReport.Severity.MISSING) {
			String banner = "=".repeat(78);
			LOGGER.error("{}", banner);
			LOGGER.error("MANY ROADS HOME BASELINE PROBLEM: {}", report.severity());
			LOGGER.error("{}", report.summary());
			for (String remedy : report.remedies()) {
				LOGGER.error("  -> {}", remedy);
			}
			LOGGER.error("{}", banner);
			return;
		}
		LOGGER.warn("{}", report.summary());
		for (String remedy : report.remedies()) {
			LOGGER.warn("  -> {}", remedy);
		}
	}

	private static void logDecision(MatchaAdapterStatus status) {
		switch (status.detectionStatus()) {
			case SUPPORTED -> LOGGER.info("Matcha {} detected with exact profile {}.",
					status.detectedVersion().orElse("unknown"), MatchaProfile.PROFILE_ID);
			case ABSENT -> LOGGER.info("Matcha is absent; Forever's Matcha mappings stay disabled and vanilla handling is preserved.");
			case UNSUPPORTED_VERSION, PRESENT_UNVERIFIED, MALFORMED, FAILED_SAFE ->
					LOGGER.warn("Forever's Matcha mappings are disabled with status {}. Any loaded Matcha "
									+ "datapack still runs its own content; only Forever's translation layer is "
									+ "inactive. {}",
							status.detectionStatus(), String.join(" ", status.diagnostics()));
			// A switch statement over an enum is not exhaustiveness-checked by the
			// compiler, so a newly added status would otherwise reach no branch and be
			// logged nowhere. Detection state decides whether Matcha mappings are
			// applied, so silently losing that signal is exactly the failure this
			// project refuses to accept. Log the gap rather than throwing: logging must
			// never take down a running server.
			default -> LOGGER.error("Unhandled Matcha detection status {}. This is a Forever bug: "
					+ "logDecision must describe every MatchaDetectionStatus. Treating it as disabled.",
					status.detectionStatus());
		}
	}
}

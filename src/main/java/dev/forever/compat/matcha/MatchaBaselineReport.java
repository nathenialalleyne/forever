package dev.forever.compat.matcha;

import java.util.List;
import java.util.Objects;

/**
 * Decides how loudly the server should complain about the state of the Matcha baseline.
 *
 * <p>LAB-02 measured what the selected pack loader does when the install is wrong, and
 * every path failed open. Deleting the pinned archive produced a normal server start
 * with the vanilla recipe count and no diagnostic whatsoever. Removing one of the two
 * pack roles was equally silent. A server operator could therefore run a Many Roads Home
 * world that contains none of Many Roads Home and never be told.
 *
 * <p>That is a gap the pack must close itself. No configuration of the loader can
 * express "this pack is required and must match a known checksum", which is the gap
 * analysis ADR 0033 requires before writing companion code.
 *
 * <p>This class is deliberately a pure decision over an already-computed detection
 * status. It performs no IO, reads no world, and touches no persistent state, so it can
 * be unit tested without a Minecraft runtime. The caller decides what to do with the
 * verdict.
 *
 * <p><strong>It never prevents startup.</strong> Principle 18 puts the world save above
 * any feature, and refusing to boot would strand an existing world for a problem the
 * operator may already know about. The correct behaviour is an unmissable log entry that
 * names the remedy, not a crash.
 */
public record MatchaBaselineReport(Severity severity, String summary, List<String> remedies) {

	/** How the operator should be told, ordered by increasing urgency. */
	public enum Severity {
		/** The baseline is present and verified. Nothing needs saying beyond the usual line. */
		HEALTHY,
		/**
		 * The baseline is present but could not be proven to be the pinned release.
		 * Gameplay content still runs; only Forever's translation layer stays inactive.
		 */
		DEGRADED,
		/**
		 * The baseline is absent. This is the case LAB-02 found produced no diagnostic
		 * at all, and it is the one that matters most: the pack's entire gameplay
		 * foundation is missing.
		 */
		MISSING
	}

	public MatchaBaselineReport {
		Objects.requireNonNull(severity, "severity must not be null.");
		Objects.requireNonNull(summary, "summary must not be null.");
		remedies = List.copyOf(Objects.requireNonNull(remedies, "remedies must not be null."));
		if (summary.isBlank()) {
			throw new IllegalArgumentException("A baseline report must explain itself; summary was blank.");
		}
		if (severity != Severity.HEALTHY && remedies.isEmpty()) {
			// A warning that does not say what to do trains operators to ignore warnings.
			throw new IllegalArgumentException(
					"A non-healthy baseline report must name at least one remedy, but none was supplied.");
		}
	}

	/**
	 * Builds the report for a detection status.
	 *
	 * @param status the adapter status already computed by detection
	 * @param expectedVersion the Matcha version this pack pins, for the operator message
	 * @return a report describing how loudly to complain and what to do about it
	 */
	public static MatchaBaselineReport fromStatus(MatchaAdapterStatus status, String expectedVersion) {
		Objects.requireNonNull(status, "status must not be null.");
		Objects.requireNonNull(expectedVersion, "expectedVersion must not be null.");

		return switch (status.detectionStatus()) {
			case SUPPORTED -> new MatchaBaselineReport(
					Severity.HEALTHY,
					"Matcha " + status.detectedVersion().orElse(expectedVersion)
							+ " is present and verified. The gameplay baseline is complete.",
					List.of());
			case ABSENT -> new MatchaBaselineReport(
					Severity.MISSING,
					"The Matcha gameplay baseline is NOT INSTALLED. This world has none of the pack's "
							+ "recipes, progression, or balance, and is effectively vanilla.",
					List.of(
							"Confirm datapacks/Matcha_Flavoured_1_12.zip exists in the instance.",
							"Confirm the pack loader lists that file as a required datapack AND resource pack.",
							"Reinstall the pack from the exported .mrpack, which pins Matcha " + expectedVersion + "."));
			case PRESENT_UNVERIFIED, MALFORMED, UNSUPPORTED_VERSION, FAILED_SAFE -> new MatchaBaselineReport(
					Severity.DEGRADED,
					"Matcha is present but could not be verified as the pinned release (" + expectedVersion
							+ "). Its own content still runs; Forever's translation layer stays inactive.",
					List.of(
							"Compare the installed archive's SHA-256 against matcha.lock.json.",
							"If Matcha was deliberately updated, update matcha.lock.json and the pack pin together."));
			// A switch over an enum is not exhaustiveness-checked in a statement position,
			// and a new status reaching no branch would silently lose the very signal this
			// class exists to surface. Treat the unknown case as degraded rather than
			// healthy, because assuming health is the dangerous direction.
			default -> new MatchaBaselineReport(
					Severity.DEGRADED,
					"Matcha detection returned an unrecognised status (" + status.detectionStatus()
							+ "). This is a Forever bug, not necessarily an install problem.",
					List.of("Report this status value; MatchaBaselineReport must describe every detection status."));
		};
	}

	/** Whether the operator needs to be told something beyond the ordinary startup line. */
	public boolean needsAttention() {
		return severity != Severity.HEALTHY;
	}
}

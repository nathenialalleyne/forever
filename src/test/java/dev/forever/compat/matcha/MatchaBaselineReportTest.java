package dev.forever.compat.matcha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for the baseline health report introduced by MRH-010.
 *
 * <p>These exist because LAB-02 proved the pack loader fails open. The lab deleted the
 * Matcha archive and the server started normally with the vanilla recipe count and no
 * diagnostic. The report is the pack's own safety net, so its most important property is
 * that the absent case is loud and actionable rather than merely present.
 */
class MatchaBaselineReportTest {

	private static final String PINNED = "1.12";

	private static MatchaAdapterStatus statusOf(MatchaDetectionStatus detection) {
		return MatchaAdapterStatus.fromDetection(switch (detection) {
			case ABSENT -> MatchaDetectionResult.absent();
			case FAILED_SAFE -> MatchaDetectionResult.failedSafe("probe");
			default -> MatchaDetectionResult.absent();
		});
	}

	@Test
	@DisplayName("a verified baseline needs no operator attention")
	void healthyBaselineIsQuiet() {
		MatchaBaselineReport report = new MatchaBaselineReport(
				MatchaBaselineReport.Severity.HEALTHY, "Matcha 1.12 is present and verified.", List.of());

		assertFalse(report.needsAttention());
		assertTrue(report.remedies().isEmpty());
	}

	@Test
	@DisplayName("an absent baseline is reported as MISSING, not merely degraded")
	void absentBaselineIsMissing() {
		MatchaBaselineReport report =
				MatchaBaselineReport.fromStatus(statusOf(MatchaDetectionStatus.ABSENT), PINNED);

		assertEquals(MatchaBaselineReport.Severity.MISSING, report.severity());
		assertTrue(report.needsAttention());
		// The operator must be told the world is effectively vanilla, because that is
		// exactly what LAB-02 observed happening silently.
		assertTrue(report.summary().contains("NOT INSTALLED"),
				"summary should state plainly that the baseline is absent: " + report.summary());
	}

	@Test
	@DisplayName("every non-healthy report names at least one concrete remedy")
	void nonHealthyReportsNameARemedy() {
		for (MatchaDetectionStatus detection : MatchaDetectionStatus.values()) {
			MatchaBaselineReport report = MatchaBaselineReport.fromStatus(statusOf(detection), PINNED);
			if (report.needsAttention()) {
				assertFalse(report.remedies().isEmpty(),
						detection + " produced a warning with no remedy, which trains operators to ignore warnings");
			}
		}
	}

	@Test
	@DisplayName("a warning without a remedy is rejected at construction")
	void warningWithoutRemedyIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new MatchaBaselineReport(
				MatchaBaselineReport.Severity.MISSING, "something is wrong", List.of()));
	}

	@Test
	@DisplayName("a report must explain itself")
	void blankSummaryIsRejected() {
		assertThrows(IllegalArgumentException.class, () -> new MatchaBaselineReport(
				MatchaBaselineReport.Severity.HEALTHY, "   ", List.of()));
	}

	@Test
	@DisplayName("the remedy list is immutable so a caller cannot silence a warning")
	void remediesAreImmutable() {
		MatchaBaselineReport report =
				MatchaBaselineReport.fromStatus(statusOf(MatchaDetectionStatus.ABSENT), PINNED);

		assertThrows(UnsupportedOperationException.class, () -> report.remedies().clear());
	}

	@Test
	@DisplayName("the absent remedy names both pack roles, because a one-sided load is also silent")
	void absentRemedyMentionsBothRoles() {
		MatchaBaselineReport report =
				MatchaBaselineReport.fromStatus(statusOf(MatchaDetectionStatus.ABSENT), PINNED);

		String joined = String.join(" ", report.remedies());
		assertTrue(joined.contains("datapack") && joined.contains("resource pack"),
				"LAB-02 case C showed a data-only load is silent, so the remedy must mention both roles: " + joined);
	}

	@Test
	@DisplayName("the degraded remedy points at the lock file, since that is what proves identity")
	void degradedRemedyMentionsTheLock() {
		MatchaBaselineReport report =
				MatchaBaselineReport.fromStatus(statusOf(MatchaDetectionStatus.FAILED_SAFE), PINNED);

		assertEquals(MatchaBaselineReport.Severity.DEGRADED, report.severity());
		assertTrue(String.join(" ", report.remedies()).contains("matcha.lock.json"));
	}

	@Test
	@DisplayName("the pinned version is quoted so the operator knows what to install")
	void reportQuotesThePinnedVersion() {
		MatchaBaselineReport report =
				MatchaBaselineReport.fromStatus(statusOf(MatchaDetectionStatus.ABSENT), PINNED);

		assertTrue(String.join(" ", report.remedies()).contains(PINNED));
	}
}

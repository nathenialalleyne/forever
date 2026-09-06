package dev.forever.compat.matcha.diagnostic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatchaDiagnosticReportTest {
	@Test
	@DisplayName("healthy output is informational and carries no warning or error")
	void healthyReportIsQuiet() {
		MatchaDiagnosticReport report = MatchaDiagnosticReport.healthy("healthy");

		assertEquals(MatchaDiagnosticReport.Severity.HEALTHY, report.severity());
		assertFalse(report.status() == MatchaDiagnosticStatus.MISSING);
		assertTrue(report.remedies().isEmpty());
	}

	@Test
	@DisplayName("every degraded status is a warning with an actionable remedy")
	void degradedReportsAreActionable() {
		for (MatchaDiagnosticStatus status : MatchaDiagnosticStatus.values()) {
			if (status == MatchaDiagnosticStatus.HEALTHY || status == MatchaDiagnosticStatus.MISSING) {
				continue;
			}
			MatchaDiagnosticReport report = MatchaDiagnosticReport.warning(status, status.name(), List.of("remedy"));
			assertEquals(MatchaDiagnosticReport.Severity.WARN, report.severity());
			assertFalse(report.remedies().isEmpty());
		}
	}

	@Test
	@DisplayName("a warning cannot be constructed without a remedy")
	void warningNeedsRemedy() {
		assertThrows(IllegalArgumentException.class,
				() -> MatchaDiagnosticReport.warning(MatchaDiagnosticStatus.MALFORMED, "bad", List.of()));
	}

	@Test
	@DisplayName("missing status is reserved for the error banner")
	void missingUsesErrorSeverity() {
		MatchaDiagnosticReport report = MatchaDiagnosticReport.missing("missing", List.of("restore"));
		assertEquals(MatchaDiagnosticStatus.MISSING, report.status());
		assertEquals(MatchaDiagnosticReport.Severity.ERROR, report.severity());
	}
}

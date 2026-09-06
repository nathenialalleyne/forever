package dev.forever.compat.matcha.diagnostic;

import java.util.List;
import java.util.Objects;

/** Immutable, actionable result of one bounded Matcha archive observation. */
record MatchaDiagnosticReport(
		MatchaDiagnosticStatus status,
		Severity severity,
		String summary,
		List<String> remedies) {

	enum Severity {
		HEALTHY,
		WARN,
		ERROR
	}

	MatchaDiagnosticReport {
		Objects.requireNonNull(status, "status must not be null.");
		Objects.requireNonNull(severity, "severity must not be null.");
		Objects.requireNonNull(summary, "summary must not be null.");
		if (summary.isBlank()) {
			throw new IllegalArgumentException("A diagnostic report must explain itself.");
		}
		remedies = List.copyOf(Objects.requireNonNull(remedies, "remedies must not be null."));
		if (severity == Severity.HEALTHY && !remedies.isEmpty()) {
			throw new IllegalArgumentException("A healthy diagnostic must not carry remedies.");
		}
		if (severity != Severity.HEALTHY && remedies.isEmpty()) {
			throw new IllegalArgumentException("An unhealthy diagnostic must name a remedy.");
		}
	}

	static MatchaDiagnosticReport healthy(String summary) {
		return new MatchaDiagnosticReport(
				MatchaDiagnosticStatus.HEALTHY,
				Severity.HEALTHY,
				summary,
				List.of());
	}

	static MatchaDiagnosticReport missing(String summary, List<String> remedies) {
		return new MatchaDiagnosticReport(
				MatchaDiagnosticStatus.MISSING,
				Severity.ERROR,
				summary,
				remedies);
	}

	static MatchaDiagnosticReport warning(MatchaDiagnosticStatus status, String summary, List<String> remedies) {
		if (status == MatchaDiagnosticStatus.HEALTHY || status == MatchaDiagnosticStatus.MISSING) {
			throw new IllegalArgumentException("Warning status must describe a degraded outcome.");
		}
		return new MatchaDiagnosticReport(status, Severity.WARN, summary, remedies);
	}
}

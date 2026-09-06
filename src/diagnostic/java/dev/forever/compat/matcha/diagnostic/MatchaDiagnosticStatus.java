package dev.forever.compat.matcha.diagnostic;

/** Explicit outcomes emitted by the read-only Matcha baseline diagnostic. */
enum MatchaDiagnosticStatus {
	HEALTHY,
	MISSING,
	CHECKSUM_MISMATCH,
	ONE_SIDED_ROLE,
	UNSUPPORTED,
	MALFORMED,
	UNREADABLE
}

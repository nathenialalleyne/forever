package dev.forever.compat.matcha;

/** Safe, stable outcome of Matcha detection. */
public enum MatchaDetectionStatus {
	ABSENT,
	SUPPORTED,
	PRESENT_UNVERIFIED,
	UNSUPPORTED_VERSION,
	MALFORMED,
	FAILED_SAFE
}

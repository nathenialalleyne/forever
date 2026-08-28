package dev.forever.compat.matcha;

/** Factory that keeps unsupported Matcha states on the neutral path. */
public final class MatchaAdapters {
	private static final MatchaVersionDetector DETECTOR = new MatchaVersionDetector();

	private MatchaAdapters() {
	}

	public static MatchaAdapter fromEvidence(MatchaDetectionEvidence evidence) {
		MatchaDetectionResult result = DETECTOR.detect(evidence);
		return result.isSupported()
				? new VerifiedMatchaAdapter(result)
				: NoOpMatchaAdapter.fromDetection(result);
	}

	public static MatchaVersionDetector detector() {
		return DETECTOR;
	}
}

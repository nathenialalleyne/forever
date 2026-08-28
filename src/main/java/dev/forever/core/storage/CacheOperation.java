package dev.forever.core.storage;

/** Bounded, explainable result of a server-authoritative cache operation. */
public record CacheOperation(boolean applied, int moved, String explanationKey) {

	public CacheOperation {
		if (moved < 0) {
			throw new IllegalArgumentException("cache operation moved count must not be negative");
		}
		if (explanationKey == null || explanationKey.length() > StorageSafetyLimits.MAX_QUERY_TEXT_LENGTH) {
			throw new IllegalArgumentException("cache operation explanation key is invalid");
		}
	}

	public static CacheOperation success(int moved) {
		return new CacheOperation(true, moved, "");
	}

	public static CacheOperation rejected(String explanationKey) {
		return new CacheOperation(false, 0, explanationKey);
	}
}

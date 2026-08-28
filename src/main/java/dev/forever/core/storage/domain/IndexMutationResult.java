package dev.forever.core.storage.domain;

/** Explainable result of a bounded index update. */
public record IndexMutationResult(boolean applied, int indexedSlots, String explanationKey) {

	public IndexMutationResult {
		if (indexedSlots < 0) {
			throw new IllegalArgumentException("indexed slot count must not be negative");
		}
		if (explanationKey == null || explanationKey.length() > StorageSafetyLimits.MAX_QUERY_TEXT_LENGTH) {
			throw new IllegalArgumentException("index explanation key is invalid");
		}
	}

	public static IndexMutationResult success(int indexedSlots) {
		return new IndexMutationResult(true, indexedSlots, "");
	}

	public static IndexMutationResult rejected(String explanationKey, int indexedSlots) {
		return new IndexMutationResult(false, indexedSlots, explanationKey);
	}
}

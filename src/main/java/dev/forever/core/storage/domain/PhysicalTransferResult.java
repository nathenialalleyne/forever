package dev.forever.core.storage.domain;

/** Explainable result of an atomic movement between two physical inventories. */
public record PhysicalTransferResult(boolean applied, int moved, String explanationKey) {

	public PhysicalTransferResult {
		if (moved < 0) {
			throw new IllegalArgumentException("physical transfer moved count must not be negative");
		}
		if (explanationKey == null || explanationKey.length() > StorageSafetyLimits.MAX_QUERY_TEXT_LENGTH) {
			throw new IllegalArgumentException("physical transfer explanation key is invalid");
		}
	}

	public static PhysicalTransferResult success(int moved) {
		return new PhysicalTransferResult(true, moved, "");
	}

	public static PhysicalTransferResult rejected(String explanationKey) {
		return new PhysicalTransferResult(false, 0, explanationKey);
	}
}

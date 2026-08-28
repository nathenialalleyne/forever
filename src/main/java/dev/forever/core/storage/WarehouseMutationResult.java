package dev.forever.core.storage;

import java.util.Optional;
import java.util.Objects;

/** Explainable result of a server-authoritative warehouse registration mutation. */
public record WarehouseMutationResult(
		boolean applied,
		String explanationKey,
		Optional<WarehouseRecord> warehouse) {

	public WarehouseMutationResult {
		if (explanationKey == null || explanationKey.length() > StorageSafetyLimits.MAX_QUERY_TEXT_LENGTH) {
			throw new IllegalArgumentException("warehouse explanation key is invalid");
		}
        warehouse = Objects.requireNonNull(warehouse, "warehouse");
	}

	public static WarehouseMutationResult success(WarehouseRecord warehouse) {
		return new WarehouseMutationResult(true, "", Optional.of(warehouse));
	}

	public static WarehouseMutationResult rejected(String explanationKey) {
		return new WarehouseMutationResult(false, explanationKey, Optional.empty());
	}
}

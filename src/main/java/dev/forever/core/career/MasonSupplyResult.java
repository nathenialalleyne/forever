package dev.forever.core.career;

import java.util.Objects;

/** Immutable result for one bounded, atomic Mason catalogue operation. */
public record MasonSupplyResult(
		boolean applied,
		MasonCatalogEntry entry,
		int batchCount,
		long inputConsumed,
		long outputProduced,
		String message) {

	public MasonSupplyResult {
		Objects.requireNonNull(entry, "entry");
		if (batchCount < 0 || inputConsumed < 0 || outputProduced < 0) {
			throw new IllegalArgumentException("Mason supply counts cannot be negative.");
		}
		message = CareerCodecs.requiredText(message, "Mason supply result", 512);
	}
}

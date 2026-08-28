package dev.forever.core.economy.domain;

import java.util.Objects;

/** Complete result of one purse mutation, including both sides of the value delta. */
public record PurseTransactionResult(
		boolean applied,
		PurseTransactionStatus status,
		CoinPurseState state,
		long balanceDelta,
		long physicalValueDelta,
		String message) {

	public PurseTransactionResult {
		status = Objects.requireNonNull(status, "status");
		state = Objects.requireNonNull(state, "state");
		if (balanceDelta + physicalValueDelta != 0L) {
			throw new IllegalArgumentException("Purse transaction deltas must conserve total value.");
		}
		if (applied != (status == PurseTransactionStatus.APPLIED)) {
			throw new IllegalArgumentException("Purse transaction applied flag disagrees with its status.");
		}
		message = EconomyCodecs.requiredText(message, "purse transaction result", 512);
	}
}

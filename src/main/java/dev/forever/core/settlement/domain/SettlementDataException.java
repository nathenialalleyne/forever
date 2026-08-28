package dev.forever.core.settlement.domain;

/** Explicit, actionable failure for malformed settlement input or an invalid mutation. */
public final class SettlementDataException extends RuntimeException {

	public SettlementDataException(String message) {
		super(message);
	}

	public SettlementDataException(String message, Throwable cause) {
		super(message, cause);
	}
}

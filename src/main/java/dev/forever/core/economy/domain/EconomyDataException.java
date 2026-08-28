package dev.forever.core.economy.domain;

/** Failure while loading or validating economy data. */
public final class EconomyDataException extends RuntimeException {

	public EconomyDataException(String message) {
		super(message);
	}

	public EconomyDataException(String message, Throwable cause) {
		super(message, cause);
	}
}

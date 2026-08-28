package dev.forever.core.career;

/** Failure while loading or validating server career data. */
public final class CareerDataException extends RuntimeException {

	public CareerDataException(String message) {
		super(message);
	}

	public CareerDataException(String message, Throwable cause) {
		super(message, cause);
	}
}

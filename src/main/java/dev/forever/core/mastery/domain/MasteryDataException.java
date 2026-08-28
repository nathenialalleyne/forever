package dev.forever.core.mastery.domain;

/**
 * Indicates that a mastery data resource is malformed or internally inconsistent.
 *
 * <p>The message is deliberately suitable for a server log. Resource loading must fail
 * before a malformed definition can participate in progression evaluation.
 */
public final class MasteryDataException extends IllegalArgumentException {

	public MasteryDataException(String message) {
		super(message);
	}

	public MasteryDataException(String message, Throwable cause) {
		super(message, cause);
	}
}

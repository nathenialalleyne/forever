package dev.forever.core.guide.domain;

/**
 * Raised when a Field Guide resource cannot be decoded or violates the guide schema.
 *
 * <p>The resource identifier is included by the loader so a data-pack author can fix
 * the offending file without guessing which entry failed.
 */
public final class GuideDataException extends IllegalArgumentException {

	public GuideDataException(String message) {
		super(message);
	}

	public GuideDataException(String message, Throwable cause) {
		super(message, cause);
	}
}

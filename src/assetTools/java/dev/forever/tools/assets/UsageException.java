package dev.forever.tools.assets;

/** A user-correctable command-line, manifest, or resource-input error. */
final class UsageException extends Exception {

	UsageException(String message) {
		super(message);
	}
}

package dev.forever.tools.assets;

/** One actionable finding emitted by a named validator check. */
record AssetViolation(ViolationCode code, String subject, String message) {

	AssetViolation {
		message = message.replaceAll("\\R", " ");
	}
}

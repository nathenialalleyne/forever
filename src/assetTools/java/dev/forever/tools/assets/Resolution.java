package dev.forever.tools.assets;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A whitelisted pixel size parsed from a manifest base_resolution field. */
record Resolution(int width, int height) {

	private static final Pattern FORMAT = Pattern.compile("^(\\d+)x(\\d+)(?:\\s+.*)?$");

	static Optional<Resolution> parse(String value) {
		Matcher matcher = FORMAT.matcher(value);
		if (!matcher.matches()) {
			return Optional.empty();
		}
		try {
			return Optional.of(new Resolution(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
		} catch (NumberFormatException exception) {
			return Optional.empty();
		}
	}

	String label() {
		return width + "x" + height;
	}
}

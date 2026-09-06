package dev.forever.compat.matcha.diagnostic;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Reads the diagnostic's immutable profile generated from the committed Matcha lock.
 *
 * <p>The profile is an artifact input, not a world file. A missing or invalid profile is
 * reported as malformed evidence by the observer rather than allowing a startup failure.
 */
final class MatchaDiagnosticProfile {
	private static final String RESOURCE = "/matcha-diagnostic.properties";
	private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");
	private static final Pattern FILENAME = Pattern.compile("[A-Za-z0-9._-]{1,128}");
	private static final int MAX_PROFILE_VALUE_LENGTH = 512;

	private MatchaDiagnosticProfile() {
	}

	static Profile read() {
		try (InputStream stream = MatchaDiagnosticProfile.class.getResourceAsStream(RESOURCE)) {
			if (stream == null) {
				return Profile.invalid("The embedded Matcha lock profile is missing from the diagnostic artifact.");
			}
			Properties properties = new Properties();
			try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				properties.load(reader);
			}
			String filename = required(properties, "archive_filename");
			String version = required(properties, "version");
			String sha256 = required(properties, "archive_sha256").toLowerCase(Locale.ROOT);
			long archiveSize = positiveLong(properties, "archive_size");
			long maxArchiveBytes = positiveLong(properties, "max_archive_bytes");
			double minFormat = finiteDouble(properties, "pack_min_format");
			double maxFormat = finiteDouble(properties, "pack_max_format");
			String description = required(properties, "pack_description").replaceAll("\\s+", " ").trim()
					.toLowerCase(Locale.ROOT);

			if (!FILENAME.matcher(filename).matches() || !filename.endsWith(".zip")) {
				return Profile.invalid("The embedded Matcha archive filename is not a safe direct filename.");
			}
			if (!sha256.matches(SHA256.pattern())) {
				return Profile.invalid("The embedded Matcha lock profile does not contain a lowercase SHA-256.");
			}
			if (version.length() > MAX_PROFILE_VALUE_LENGTH || description.length() > MAX_PROFILE_VALUE_LENGTH) {
				return Profile.invalid("The embedded Matcha lock profile contains an overlong value.");
			}
			if (maxArchiveBytes < archiveSize || maxArchiveBytes > 64L * 1024L * 1024L) {
				return Profile.invalid("The embedded Matcha archive bound is outside the safe bounded range.");
			}
			if (minFormat > maxFormat) {
				return Profile.invalid("The embedded Matcha pack-format range is invalid.");
			}
			return new Profile(filename, version, sha256, archiveSize, maxArchiveBytes,
					minFormat, maxFormat, description, null);
		} catch (IOException | IllegalArgumentException exception) {
			return Profile.invalid("The embedded Matcha lock profile could not be read: "
					+ exception.getClass().getSimpleName() + ".");
		}
	}

	private static String required(Properties properties, String key) {
		String value = properties.getProperty(key);
		if (value == null || value.isBlank() || value.length() > MAX_PROFILE_VALUE_LENGTH) {
			throw new IllegalArgumentException("Missing or overlong profile value: " + key);
		}
		return value.trim();
	}

	private static long positiveLong(Properties properties, String key) {
		long value = Long.parseLong(required(properties, key));
		if (value <= 0) {
			throw new IllegalArgumentException("Profile value must be positive: " + key);
		}
		return value;
	}

	private static double finiteDouble(Properties properties, String key) {
		double value = Double.parseDouble(required(properties, key));
		if (!Double.isFinite(value)) {
			throw new IllegalArgumentException("Profile value must be finite: " + key);
		}
		return value;
	}

	record Profile(
			String archiveFilename,
			String version,
			String archiveSha256,
			long archiveSize,
			long maxArchiveBytes,
			double minPackFormat,
			double maxPackFormat,
			String packDescription,
			String error) {
		static Profile invalid(String error) {
			return new Profile("", "", "", 0, 0, 0, 0, "", error);
		}

		boolean valid() {
			return error == null;
		}
	}
}

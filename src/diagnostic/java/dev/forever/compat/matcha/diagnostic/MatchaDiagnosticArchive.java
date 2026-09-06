package dev.forever.compat.matcha.diagnostic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/** Reads one bounded Matcha archive without extracting or modifying it. */
final class MatchaDiagnosticArchive {
	private static final String PACK_METADATA = "pack.mcmeta";
	private static final int HASH_BUFFER_BYTES = 16 * 1024;
	private static final int MAX_METADATA_BYTES = 64 * 1024;
	private static final int MAX_ARCHIVE_ENTRIES = 8192;
	private static final int MAX_ENTRY_NAME_LENGTH = 512;
	private static final List<String> KNOWN_DATA_NAMESPACES = List.of(
			"blasting", "blessings", "crafting", "endless_repairs", "food",
			"main", "smelting", "smithing_table", "smoking", "stonecutting");

	private MatchaDiagnosticArchive() {
	}

	static Inspection inspect(Path archive, MatchaDiagnosticProfile.Profile profile) {
		HashResult hash = hash(archive, profile.maxArchiveBytes());
		if (hash.failureStatus() != null) {
			return Inspection.failure(hash.failureStatus(), hash.failureMessage());
		}
		return inspectZip(archive, profile, hash.digest());
	}

	static boolean hasKnownData(Set<String> namespaces) {
		for (String namespace : KNOWN_DATA_NAMESPACES) {
			if (namespaces.contains(namespace)) {
				return true;
			}
		}
		return false;
	}

	private static HashResult hash(Path archive, long maxBytes) {
		try {
			long declaredSize = Files.size(archive);
			if (declaredSize > maxBytes) {
				return HashResult.failure(
						MatchaDiagnosticStatus.UNSUPPORTED,
						"The Matcha archive is " + declaredSize + " bytes, beyond the diagnostic bound of "
								+ maxBytes + "; refusing an unbounded hash.");
			}
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			long bytesRead = 0;
			byte[] buffer = new byte[HASH_BUFFER_BYTES];
			try (InputStream stream = Files.newInputStream(archive, StandardOpenOption.READ)) {
				int read;
				while ((read = stream.read(buffer)) != -1) {
					bytesRead += read;
					if (bytesRead > maxBytes) {
						return HashResult.failure(
								MatchaDiagnosticStatus.UNSUPPORTED,
								"The Matcha archive grew beyond the diagnostic bound of " + maxBytes
										+ " bytes while it was being read; refusing an unbounded hash.");
					}
					digest.update(buffer, 0, read);
				}
			}
			return HashResult.success(HexFormat.of().formatHex(digest.digest()));
		} catch (NoSuchAlgorithmException exception) {
			return HashResult.failure(
					MatchaDiagnosticStatus.MALFORMED,
					"The pinned JDK does not provide SHA-256; the Matcha archive cannot be verified safely.");
		} catch (IOException | SecurityException exception) {
			return HashResult.failure(
					MatchaDiagnosticStatus.UNREADABLE,
					"The Matcha archive could not be read for its bounded SHA-256: "
							+ exception.getClass().getSimpleName() + ".");
		}
	}

	private static Inspection inspectZip(
			Path archive, MatchaDiagnosticProfile.Profile profile, String digest) {
		try (ZipFile zip = new ZipFile(archive.toFile(), StandardCharsets.UTF_8)) {
			if (zip.size() > MAX_ARCHIVE_ENTRIES) {
				return Inspection.failure(
						MatchaDiagnosticStatus.UNSUPPORTED,
						"The Matcha archive contains " + zip.size() + " entries, beyond the diagnostic bound of "
								+ MAX_ARCHIVE_ENTRIES + ".");
			}
			boolean dataRole = false;
			boolean resourceRole = false;
			ZipEntry metadataEntry = null;
			int metadataEntries = 0;
			Enumeration<? extends ZipEntry> entries = zip.entries();
			int entryCount = 0;
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				entryCount++;
				if (entryCount > MAX_ARCHIVE_ENTRIES) {
					return Inspection.failure(
							MatchaDiagnosticStatus.UNSUPPORTED,
							"The Matcha archive exceeded the diagnostic entry bound of " + MAX_ARCHIVE_ENTRIES + ".");
				}
				String name = validateEntryName(entry);
				if (name.equals(PACK_METADATA)) {
					metadataEntry = entry;
					metadataEntries++;
				}
				if (isKnownDataPath(name)) {
					dataRole = true;
				}
				if (name.startsWith("assets/")) {
					resourceRole = true;
				}
			}
			if (metadataEntries == 0) {
				return Inspection.failure(
						MatchaDiagnosticStatus.MALFORMED,
						"The Matcha archive has no pack.mcmeta entry; it is not a readable pack archive.");
			}
			if (metadataEntries > 1) {
				return Inspection.failure(
						MatchaDiagnosticStatus.MALFORMED,
						"The Matcha archive contains duplicate pack.mcmeta entries; its identity is ambiguous.");
			}
			MetadataInspection metadata = inspectMetadata(zip, metadataEntry, profile);
			return new Inspection(dataRole, resourceRole, metadata, digest, null, null);
		} catch (ZipException exception) {
			return Inspection.failure(
					MatchaDiagnosticStatus.MALFORMED,
					"The Matcha archive is malformed and could not be opened as a ZIP: "
							+ exception.getMessage() + ".");
		} catch (IOException | SecurityException exception) {
			return Inspection.failure(
					MatchaDiagnosticStatus.UNREADABLE,
					"The Matcha archive could not be read as a ZIP: "
							+ exception.getClass().getSimpleName() + ".");
		} catch (IllegalArgumentException exception) {
			return Inspection.failure(MatchaDiagnosticStatus.MALFORMED, exception.getMessage());
		}
	}

	private static MetadataInspection inspectMetadata(
			ZipFile zip, ZipEntry metadataEntry, MatchaDiagnosticProfile.Profile profile) {
		try (InputStream stream = zip.getInputStream(metadataEntry)) {
			String text = readBounded(stream, MAX_METADATA_BYTES);
			JsonElement root = JsonParser.parseString(text);
			if (!root.isJsonObject()) {
				return MetadataInspection.malformed("pack.mcmeta is not a JSON object.");
			}
			JsonObject object = root.getAsJsonObject();
			JsonElement packElement = object.get("pack");
			if (packElement == null || !packElement.isJsonObject()) {
				return MetadataInspection.malformed("pack.mcmeta has no JSON pack object.");
			}
			JsonObject pack = packElement.getAsJsonObject();
			JsonElement minElement = pack.get("min_format");
			JsonElement maxElement = pack.get("max_format");
			if (minElement == null || maxElement == null || !minElement.isJsonPrimitive() || !maxElement.isJsonPrimitive()
					|| !minElement.getAsJsonPrimitive().isNumber() || !maxElement.getAsJsonPrimitive().isNumber()) {
				return MetadataInspection.unsupported("pack.mcmeta does not declare the supported min_format/max_format range.");
			}
			double minFormat = minElement.getAsDouble();
			double maxFormat = maxElement.getAsDouble();
			if (!Double.isFinite(minFormat) || !Double.isFinite(maxFormat)) {
				return MetadataInspection.malformed("pack.mcmeta declares a non-finite pack format.");
			}
			JsonElement descriptionElement = pack.get("description");
			if (descriptionElement == null) {
				return MetadataInspection.unsupported("pack.mcmeta has no description for the pinned Matcha profile.");
			}
			String description = normaliseDescription(descriptionElement);
			if (Double.compare(minFormat, profile.minPackFormat()) != 0
					|| Double.compare(maxFormat, profile.maxPackFormat()) != 0) {
				return MetadataInspection.unsupported(
						"pack.mcmeta format range " + minFormat + ".." + maxFormat
								+ " is not the pinned Matcha " + profile.version() + " range.");
			}
			if (!description.equals(profile.packDescription())) {
				return MetadataInspection.unsupported(
						"pack.mcmeta description is not the audited Matcha " + profile.version() + " description.");
			}
			return MetadataInspection.valid();
		} catch (IOException | SecurityException exception) {
			return MetadataInspection.failure(
					MatchaDiagnosticStatus.UNREADABLE,
					"pack.mcmeta could not be read: " + exception.getClass().getSimpleName() + ".");
		} catch (RuntimeException exception) {
			return MetadataInspection.failure(
					MatchaDiagnosticStatus.MALFORMED,
					"pack.mcmeta is malformed and could not be parsed: "
							+ exception.getClass().getSimpleName() + ".");
		}
	}

	private static String normaliseDescription(JsonElement element) {
		StringBuilder text = new StringBuilder();
		appendDescription(element, text);
		return text.toString().replaceAll("\\s+", " ").trim().toLowerCase(java.util.Locale.ROOT);
	}

	private static void appendDescription(JsonElement element, StringBuilder target) {
		if (element.isJsonPrimitive()) {
			target.append(element.getAsString());
			return;
		}
		if (!element.isJsonArray()) {
			if (element.isJsonObject() && element.getAsJsonObject().has("text")) {
				appendDescription(element.getAsJsonObject().get("text"), target);
				return;
			}
			throw new IllegalArgumentException("description contains an unsupported JSON shape");
		}
		if (element.getAsJsonArray().size() > 128) {
			throw new IllegalArgumentException("description contains too many components");
		}
		for (JsonElement child : element.getAsJsonArray()) {
			appendDescription(child, target);
		}
	}

	private static String readBounded(InputStream stream, int maxBytes) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		byte[] buffer = new byte[HASH_BUFFER_BYTES];
		int total = 0;
		int read;
		while ((read = stream.read(buffer)) != -1) {
			total += read;
			if (total > maxBytes) {
				throw new IOException("entry exceeds the bounded metadata size of " + maxBytes + " bytes");
			}
			output.write(buffer, 0, read);
		}
		return output.toString(StandardCharsets.UTF_8);
	}

	private static String validateEntryName(ZipEntry entry) {
		String name = entry.getName();
		if (name == null || name.isBlank() || name.length() > MAX_ENTRY_NAME_LENGTH || name.indexOf('\0') >= 0
				|| name.indexOf('\\') >= 0 || name.startsWith("/") || name.matches("[A-Za-z]:.*")) {
			throw new IllegalArgumentException("The ZIP contains an unsafe or overlong entry name.");
		}
		String[] segments = name.split("/", -1);
		for (int index = 0; index < segments.length; index++) {
			String segment = segments[index];
			if (segment.equals("..") || segment.equals(".") || (segment.isEmpty() && index != segments.length - 1)) {
				throw new IllegalArgumentException("The ZIP contains a non-normalised entry path: " + name);
			}
		}
		return name;
	}

	private static boolean isKnownDataPath(String path) {
		for (String namespace : KNOWN_DATA_NAMESPACES) {
			if (path.startsWith("data/" + namespace + "/")) {
				return true;
			}
		}
		return false;
	}

	private record HashResult(String digest, MatchaDiagnosticStatus failureStatus, String failureMessage) {
		static HashResult success(String digest) {
			return new HashResult(digest, null, null);
		}

		static HashResult failure(MatchaDiagnosticStatus status, String message) {
			return new HashResult(null, status, message);
		}
	}

	record Inspection(
			boolean dataRole,
			boolean resourceRole,
			MetadataInspection metadata,
			String digest,
			MatchaDiagnosticStatus failureStatus,
			String failureMessage) {
		static Inspection failure(MatchaDiagnosticStatus status, String message) {
			return new Inspection(false, false, MetadataInspection.valid(), null, status, message);
		}
	}

	record MetadataInspection(MatchaDiagnosticStatus failureStatus, String failureMessage) {
		static MetadataInspection valid() {
			return new MetadataInspection(null, null);
		}

		static MetadataInspection malformed(String message) {
			return new MetadataInspection(MatchaDiagnosticStatus.MALFORMED, message);
		}

		static MetadataInspection unsupported(String message) {
			return new MetadataInspection(MatchaDiagnosticStatus.UNSUPPORTED, message);
		}

		static MetadataInspection failure(MatchaDiagnosticStatus status, String message) {
			return new MetadataInspection(status, message);
		}
	}
}

package dev.forever.compat.matcha.diagnostic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

/** Reads one bounded Matcha archive without extracting or modifying it. */
final class MatchaDiagnosticArchive {
	private static final String PACK_METADATA = "pack.mcmeta";
	private static final int HASH_BUFFER_BYTES = 16 * 1024;
	private static final int MAX_METADATA_BYTES = 64 * 1024;
	private static final int MAX_ARCHIVE_ENTRIES = 8192;
	private static final int MAX_ENTRY_NAME_LENGTH = 512;
	private static final int MAX_ENTRY_NAME_BYTES = 512;
	private static final int MAX_ENTRY_EXTRA_BYTES = 8 * 1024;
	private static final long MAX_ENTRY_UNCOMPRESSED_BYTES = 4L * 1024L * 1024L;
	private static final long MAX_TOTAL_UNCOMPRESSED_BYTES = 32L * 1024L * 1024L;
	private static final int MAX_DESCRIPTION_DEPTH = 32;
	private static final int MAX_DESCRIPTION_COMPONENTS = 128;
	private static final long MAX_SNAPSHOT_BYTES = 64L * 1024L * 1024L;
	private static final List<String> KNOWN_DATA_NAMESPACES = List.of(
			"blasting", "blessings", "crafting", "endless_repairs", "food",
			"main", "smelting", "smithing_table", "smoking", "stonecutting");
	private MatchaDiagnosticArchive() {
	}
	static Inspection inspect(Path archive, MatchaDiagnosticProfile.Profile profile) {
		if (archive == null || profile == null || !profile.valid()) {
			return Inspection.failure(
					MatchaDiagnosticStatus.MALFORMED,
					"The diagnostic archive or its embedded profile was invalid; no archive was trusted.");
		}
		long maxBytes = profile.maxArchiveBytes();
		if (maxBytes <= 0 || maxBytes > MAX_SNAPSHOT_BYTES) {
			return Inspection.failure(
					MatchaDiagnosticStatus.UNSUPPORTED,
					"The diagnostic archive bound is outside the fixed safe snapshot limit of "
							+ MAX_SNAPSHOT_BYTES + " bytes.");
		}
		SnapshotResult snapshot = snapshot(archive, maxBytes);
		if (snapshot.failureStatus() != null) {
			return Inspection.failure(snapshot.failureStatus(), snapshot.failureMessage());
		}
		return inspectZip(snapshot.bytes(), profile, snapshot.digest());
	}
	static boolean hasKnownData(Set<String> namespaces) {
		if (namespaces == null) {
			return false;
		}
		for (String namespace : KNOWN_DATA_NAMESPACES) {
			if (namespaces.contains(namespace)) {
				return true;
			}
		}
		return false;
	}
	private static SnapshotResult snapshot(Path archive, long maxBytes) {
		try {
			Path parent = archive.getParent();
			if (parent == null || Files.isSymbolicLink(archive) || Files.isSymbolicLink(parent)) {
				return SnapshotResult.failure(
						MatchaDiagnosticStatus.UNREADABLE,
						"The diagnostic archive path or its parent is a symbolic link; refusing to read outside the instance.");
			}
			if (!Files.isDirectory(parent, LinkOption.NOFOLLOW_LINKS)) {
				return SnapshotResult.failure(
						MatchaDiagnosticStatus.UNREADABLE,
						"The diagnostic archive parent is not a regular directory; no archive was read.");
			}
			try (DirectoryStream<Path> directory = Files.newDirectoryStream(parent)) {
				if (Files.isSymbolicLink(parent)) {
					return SnapshotResult.failure(
							MatchaDiagnosticStatus.UNREADABLE,
							"The diagnostic archive parent became a symbolic link; refusing to read outside the instance.");
				}
				if (directory instanceof SecureDirectoryStream<?> secure) {
					return snapshotSecureDirectory(secure, archive.getFileName(), maxBytes);
				}
				return SnapshotResult.failure(
						MatchaDiagnosticStatus.UNREADABLE,
						"The filesystem provider does not expose SecureDirectoryStream for safe directory-relative archive "
								+ "opening; use a provider that does instead of a raceable fallback.");
			}
		} catch (IOException | SecurityException exception) {
			return SnapshotResult.failure(
					MatchaDiagnosticStatus.UNREADABLE,
					"The Matcha archive could not be opened for a bounded no-follow snapshot: "
							+ exception.getClass().getSimpleName() + ".");
		} catch (IllegalArgumentException | UnsupportedOperationException exception) {
			return SnapshotResult.failure(
					MatchaDiagnosticStatus.UNREADABLE,
					"The Matcha archive path could not be opened safely: "
							+ exception.getClass().getSimpleName() + ".");
		}
	}
	@SuppressWarnings("unchecked")
	private static SnapshotResult snapshotSecureDirectory(
			SecureDirectoryStream<?> directory, Path filename, long maxBytes) throws IOException {
		SecureDirectoryStream<Path> typedDirectory = (SecureDirectoryStream<Path>) directory;
		BasicFileAttributeView attributes = typedDirectory.getFileAttributeView(
				filename, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
		if (attributes == null || !attributes.readAttributes().isRegularFile()) {
			return SnapshotResult.failure(
					MatchaDiagnosticStatus.UNREADABLE,
					"The expected Matcha archive is not a regular readable file.");
		}
		try (SeekableByteChannel channel = typedDirectory.newByteChannel(
				filename, Set.of(StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS))) {
			return readSnapshot(channel, maxBytes);
		}
	}
	private static SnapshotResult readSnapshot(SeekableByteChannel channel, long maxBytes) {
		try {
			long declaredSize = channel.size();
			if (declaredSize < 0 || declaredSize > maxBytes) {
				return SnapshotResult.failure(
						MatchaDiagnosticStatus.UNSUPPORTED,
						"The Matcha archive is " + declaredSize + " bytes, beyond the diagnostic bound of "
								+ maxBytes + "; refusing an unbounded snapshot.");
			}
			if (declaredSize > Integer.MAX_VALUE) {
				return SnapshotResult.failure(
						MatchaDiagnosticStatus.UNSUPPORTED,
						"The Matcha archive is too large for the bounded diagnostic snapshot.");
			}
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			ByteBuffer destination = ByteBuffer.allocate((int) declaredSize);
			int emptyReads = 0;
			while (destination.hasRemaining()) {
				int position = destination.position();
				int read = channel.read(destination);
				if (read < 0) {
					return SnapshotResult.failure(
							MatchaDiagnosticStatus.UNREADABLE,
							"The Matcha archive changed or was truncated while its stable snapshot was read.");
				}
				if (read == 0) {
					if (++emptyReads >= 3) {
						return SnapshotResult.failure(
								MatchaDiagnosticStatus.UNREADABLE,
								"The Matcha archive did not provide a bounded snapshot for reading.");
					}
					continue;
				}
				emptyReads = 0;
				digest.update(destination.array(), position, read);
			}
			ByteBuffer extra = ByteBuffer.allocate(1);
			if (channel.read(extra) != -1) {
				return SnapshotResult.failure(
						MatchaDiagnosticStatus.UNSUPPORTED,
						"The Matcha archive grew beyond the diagnostic bound while its stable snapshot was read; "
								+ "refusing an unbounded snapshot.");
			}
			return SnapshotResult.success(destination.array(), HexFormat.of().formatHex(digest.digest()));
		} catch (NoSuchAlgorithmException exception) {
			return SnapshotResult.failure(
					MatchaDiagnosticStatus.MALFORMED,
					"The pinned JDK does not provide SHA-256; the Matcha archive cannot be verified safely.");
		} catch (IOException | SecurityException exception) {
			return SnapshotResult.failure(
					MatchaDiagnosticStatus.UNREADABLE,
					"The Matcha archive could not be read for its bounded SHA-256: "
							+ exception.getClass().getSimpleName() + ".");
		}
	}
	private static Inspection inspectZip(
			byte[] archiveBytes, MatchaDiagnosticProfile.Profile profile, String digest) {
		try (InputStream input = new ByteArrayInputStream(archiveBytes);
				ZipInputStream zip = new ZipInputStream(input, StandardCharsets.UTF_8)) {
			boolean dataRole = false;
			boolean resourceRole = false;
			String metadataText = null;
			int entryCount = 0;
			int metadataEntries = 0;
			long totalBytes = 0;
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				entryCount++;
				if (entryCount > MAX_ARCHIVE_ENTRIES) {
					return Inspection.failure(
							MatchaDiagnosticStatus.UNSUPPORTED,
							"The Matcha archive exceeded the diagnostic entry bound of "
									+ MAX_ARCHIVE_ENTRIES + ".");
				}
				String name = validateEntryName(entry);
				validateEntryMetadata(entry);
				if (name.equals(PACK_METADATA)) {
					metadataEntries++;
					if (metadataEntries > 1) {
						return Inspection.failure(
								MatchaDiagnosticStatus.MALFORMED,
								"The Matcha archive contains duplicate pack.mcmeta entries; its identity is ambiguous.");
					}
				}
				dataRole |= isKnownDataPath(name);
				resourceRole |= name.startsWith("assets/");
				EntryRead read = readEntry(zip, entry, name, totalBytes);
				totalBytes = read.totalBytes();
				if (name.equals(PACK_METADATA)) {
					metadataText = read.content();
				}
				zip.closeEntry();
			}
			if (metadataEntries == 0) {
				return Inspection.failure(
						MatchaDiagnosticStatus.MALFORMED,
						"The Matcha archive has no pack.mcmeta entry; it is not a readable pack archive.");
			}
			MetadataInspection metadata = inspectMetadata(metadataText, profile);
			return new Inspection(dataRole, resourceRole, metadata, digest, null, null);
		} catch (BoundExceededException exception) {
			return Inspection.failure(MatchaDiagnosticStatus.UNSUPPORTED, exception.getMessage());
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
	private static EntryRead readEntry(
			ZipInputStream zip, ZipEntry entry, String name, long totalBytes) throws IOException {
		long entryLimit = name.equals(PACK_METADATA) ? MAX_METADATA_BYTES : MAX_ENTRY_UNCOMPRESSED_BYTES;
		long declaredSize = entry.getSize();
		if (declaredSize > entryLimit) {
			throw new BoundExceededException(
					"The ZIP entry " + name + " declares " + declaredSize
							+ " uncompressed bytes, beyond its diagnostic bound of " + entryLimit + ".");
		}
		if (declaredSize >= 0 && declaredSize > MAX_TOTAL_UNCOMPRESSED_BYTES - totalBytes) {
			throw new BoundExceededException(
					"The ZIP entries exceed the diagnostic decompressed-byte bound of "
							+ MAX_TOTAL_UNCOMPRESSED_BYTES + ".");
		}
		ByteArrayOutputStream metadata = name.equals(PACK_METADATA)
				? new ByteArrayOutputStream(Math.min(MAX_METADATA_BYTES, HASH_BUFFER_BYTES)) : null;
		byte[] buffer = new byte[HASH_BUFFER_BYTES];
		long entryBytes = 0;
		int read;
		while ((read = zip.read(buffer)) != -1) {
			entryBytes += read;
			totalBytes += read;
			if (entryBytes > entryLimit) {
				throw new BoundExceededException(
						"The ZIP entry " + name + " exceeds its diagnostic decompressed-byte bound of "
								+ entryLimit + ".");
			}
			if (totalBytes > MAX_TOTAL_UNCOMPRESSED_BYTES) {
				throw new BoundExceededException(
						"The ZIP entries exceed the diagnostic decompressed-byte bound of "
							+ MAX_TOTAL_UNCOMPRESSED_BYTES + ".");
			}
			if (metadata != null) {
				metadata.write(buffer, 0, read);
			}
		}
		return new EntryRead(totalBytes, metadata == null ? null : metadata.toString(StandardCharsets.UTF_8));
	}
	private static MetadataInspection inspectMetadata(
			String text, MatchaDiagnosticProfile.Profile profile) {
		try {
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
			if (minElement == null || maxElement == null || !minElement.isJsonPrimitive()
					|| !maxElement.isJsonPrimitive() || !minElement.getAsJsonPrimitive().isNumber()
					|| !maxElement.getAsJsonPrimitive().isNumber()) {
				return MetadataInspection.unsupported(
						"pack.mcmeta does not declare the supported min_format/max_format range.");
			}
			double minFormat = minElement.getAsDouble();
			double maxFormat = maxElement.getAsDouble();
			if (!Double.isFinite(minFormat) || !Double.isFinite(maxFormat)) {
				return MetadataInspection.malformed("pack.mcmeta declares a non-finite pack format.");
			}
			JsonElement descriptionElement = pack.get("description");
			if (descriptionElement == null) {
				return MetadataInspection.unsupported(
						"pack.mcmeta has no description for the pinned Matcha profile.");
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
		} catch (RuntimeException exception) {
			return MetadataInspection.failure(
					MatchaDiagnosticStatus.MALFORMED,
					"pack.mcmeta is malformed and could not be parsed: "
							+ exception.getClass().getSimpleName() + ".");
		}
	}
	private static String normaliseDescription(JsonElement element) {
		StringBuilder text = new StringBuilder();
		appendDescription(element, text, 0);
		return text.toString().replaceAll("\\s+", " ").trim().toLowerCase(java.util.Locale.ROOT);
	}
	private static void appendDescription(JsonElement element, StringBuilder target, int depth) {
		if (depth > MAX_DESCRIPTION_DEPTH) {
			throw new IllegalArgumentException("description is too deeply nested");
		}
		if (element.isJsonPrimitive()) {
			target.append(element.getAsString());
			return;
		}
		if (!element.isJsonArray()) {
			if (element.isJsonObject() && element.getAsJsonObject().has("text")) {
				appendDescription(element.getAsJsonObject().get("text"), target, depth + 1);
				return;
			}
			throw new IllegalArgumentException("description contains an unsupported JSON shape");
		}
		if (element.getAsJsonArray().size() > MAX_DESCRIPTION_COMPONENTS) {
			throw new IllegalArgumentException("description contains too many components");
		}
		for (JsonElement child : element.getAsJsonArray()) {
			appendDescription(child, target, depth + 1);
		}
		if (target.length() > MAX_METADATA_BYTES) {
			throw new IllegalArgumentException("description is too long after normalisation");
		}
	}
	private static String validateEntryName(ZipEntry entry) {
		String name = entry.getName();
		byte[] encodedName = name == null ? new byte[0] : name.getBytes(StandardCharsets.UTF_8);
		if (name == null || name.isBlank() || name.length() > MAX_ENTRY_NAME_LENGTH
				|| encodedName.length > MAX_ENTRY_NAME_BYTES || name.indexOf('\0') >= 0
				|| name.indexOf('\\') >= 0 || name.startsWith("/") || name.matches("[A-Za-z]:.*")) {
			throw new IllegalArgumentException("The ZIP contains an unsafe or overlong entry name.");
		}
		String[] segments = name.split("/", -1);
		for (int index = 0; index < segments.length; index++) {
			String segment = segments[index];
			if (segment.equals("..") || segment.equals(".")
					|| (segment.isEmpty() && index != segments.length - 1)) {
				throw new IllegalArgumentException("The ZIP contains a non-normalised entry path: " + name);
			}
		}
		return name;
	}
	private static void validateEntryMetadata(ZipEntry entry) throws BoundExceededException {
		byte[] extra = entry.getExtra();
		if (extra != null && extra.length > MAX_ENTRY_EXTRA_BYTES) {
			throw new BoundExceededException(
					"The ZIP entry " + entry.getName() + " has overlong extra metadata beyond the diagnostic bound of "
							+ MAX_ENTRY_EXTRA_BYTES + " bytes.");
		}
	}
	private static boolean isKnownDataPath(String path) {
		for (String namespace : KNOWN_DATA_NAMESPACES) {
			if (path.startsWith("data/" + namespace + "/")) {
				return true;
			}
		}
		return false;
	}
	private record SnapshotResult(byte[] bytes, String digest, MatchaDiagnosticStatus failureStatus, String failureMessage) {
		static SnapshotResult success(byte[] bytes, String digest) { return new SnapshotResult(bytes, digest, null, null); }
		static SnapshotResult failure(MatchaDiagnosticStatus status, String message) { return new SnapshotResult(null, null, status, message); }
	}
	private record EntryRead(long totalBytes, String content) {
	}
	private static final class BoundExceededException extends IOException {
		BoundExceededException(String message) { super(message); }
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
		static MetadataInspection valid() { return new MetadataInspection(null, null); }
		static MetadataInspection malformed(String message) { return new MetadataInspection(MatchaDiagnosticStatus.MALFORMED, message); }
		static MetadataInspection unsupported(String message) { return new MetadataInspection(MatchaDiagnosticStatus.UNSUPPORTED, message); }
		static MetadataInspection failure(MatchaDiagnosticStatus status, String message) { return new MetadataInspection(status, message); }
	}
}

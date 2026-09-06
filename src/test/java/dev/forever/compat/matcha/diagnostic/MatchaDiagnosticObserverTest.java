package dev.forever.compat.matcha.diagnostic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MatchaDiagnosticObserverTest {
	private static final String LOCKED_SHA256 = "6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248";
	private static final long FIXED_ZIP_ENTRY_TIME_MILLIS = 315_532_800_000L;
	private static final String DATA_ENTRY = "data/main/function/setup/scoreboard.mcfunction";
	private static final String ASSET_ENTRY = "assets/minecraft/items/heart_container.json";
	private static final String VALID_METADATA = "{\"pack\":{\"min_format\":88.0,\"max_format\":107.1,"
			+ "\"description\":[{\"text\":\"Klei's Matcha Flavoured\"},{\"text\":\" 1.12 for 26.2\"}]}}";

	@Test
	@DisplayName("the generated profile is synchronised with matcha.lock.json")
	void generatedProfileMatchesLock() throws IOException {
		MatchaDiagnosticProfile.Profile profile = MatchaDiagnosticProfile.read();
		var lock = JsonParser.parseString(Files.readString(Path.of("matcha.lock.json"))).getAsJsonObject();
		assertTrue(profile.valid());
		assertEquals(lock.get("filename").getAsString(), profile.archiveFilename());
		assertEquals(lock.get("sha256").getAsString(), profile.archiveSha256());
		assertEquals(lock.get("version_number").getAsString(), profile.version());
		assertEquals(lock.getAsJsonObject("artifact").get("size").getAsLong(), profile.archiveSize());
		assertTrue(profile.maxArchiveBytes() >= profile.archiveSize());
		assertTrue(profile.maxArchiveBytes() <= 64L * 1024L * 1024L);
	}

	@Test
	@DisplayName("a deterministic synthetic archive is healthy when its server datapack role is observed")
	void syntheticArchiveIsHealthyWhenItsDataRoleIsObserved(@TempDir Path temp) throws IOException {
		Path archive = archivePath(temp);
		writeArchive(temp, VALID_METADATA, List.of(DATA_ENTRY), List.of(ASSET_ENTRY));

		MatchaDiagnosticReport report = inspectFixture(temp, Set.of("main"));

		assertEquals(MatchaDiagnosticStatus.HEALTHY, report.status());
		assertEquals(MatchaDiagnosticReport.Severity.HEALTHY, report.severity());
		assertTrue(report.summary().contains("archive bytes match the locked SHA-256"));
		assertTrue(report.summary().contains("does not prove that archive was the loaded source"));
		assertTrue(report.summary().contains("remote client's resource-pack role"));
		assertFalse(report.summary().contains("marker"), report.summary());
		assertTrue(report.remedies().isEmpty());
		assertTrue(Files.isRegularFile(archive));
	}

	@Test
	@DisplayName("the same synthetic archive input produces byte-identical ZIP bytes")
	void syntheticArchiveBytesAreReproducible(@TempDir Path temp) throws IOException {
		Path first = temp.resolve("first/datapacks/Matcha_Flavoured_1_12.zip");
		Path second = temp.resolve("second/datapacks/Matcha_Flavoured_1_12.zip");

		writeArchiveAt(first, VALID_METADATA, List.of(DATA_ENTRY), List.of(ASSET_ENTRY), "{}");
		writeArchiveAt(second, VALID_METADATA, List.of(DATA_ENTRY), List.of(ASSET_ENTRY), "{}");

		assertArrayEquals(Files.readAllBytes(first), Files.readAllBytes(second));
	}

	@Test
	@DisplayName("a missing archive is an ERROR naming both filename and locked SHA-256")
	void missingArchiveNamesLockedIdentity(@TempDir Path temp) {
		MatchaDiagnosticReport report = MatchaDiagnosticObserver.inspect(temp, Set.of());
		String remedies = String.join(" ", report.remedies());

		assertEquals(MatchaDiagnosticStatus.MISSING, report.status());
		assertEquals(MatchaDiagnosticReport.Severity.ERROR, report.severity());
		assertTrue(report.summary().contains("NOT INSTALLED"));
		assertTrue(remedies.contains("Matcha_Flavoured_1_12.zip"));
		assertTrue(remedies.contains(LOCKED_SHA256));
		assertTrue(remedies.contains("datapack") && remedies.contains("resource pack"));
	}

	@Test
	@DisplayName("a structurally valid decoy remains MISSING rather than pretending to be Matcha")
	void decoyArchiveIsMissing(@TempDir Path temp) throws IOException {
		writeArchive(temp, VALID_METADATA, List.of(), List.of());

		MatchaDiagnosticReport report = inspectFixture(temp, Set.of("main"));

		assertEquals(MatchaDiagnosticStatus.MISSING, report.status());
		assertEquals(MatchaDiagnosticReport.Severity.ERROR, report.severity());
		assertTrue(report.summary().contains("decoy archive"));
	}

	@Test
	@DisplayName("a valid altered synthetic archive reports checksum mismatch, not a parse error")
	void alteredSyntheticArchiveIsChecksumMismatch(@TempDir Path temp) throws IOException {
		Path archive = archivePath(temp);
		writeArchive(temp, VALID_METADATA, List.of(DATA_ENTRY), List.of(ASSET_ENTRY));
		MatchaDiagnosticProfile.Profile profile = fixtureProfile(archive);
		Files.write(archive, new byte[] {0}, StandardOpenOption.APPEND);

		MatchaDiagnosticReport report = MatchaDiagnosticObserver.inspect(temp, Set.of("main"), profile);

		assertEquals(MatchaDiagnosticStatus.CHECKSUM_MISMATCH, report.status());
		assertEquals(MatchaDiagnosticReport.Severity.WARN, report.severity());
		assertTrue(report.summary().contains("checksum"));
		assertTrue(report.summary().contains("not a ZIP or pack.mcmeta parse error"));
		assertFalse(report.summary().contains("malformed"));
	}

	@Test
	@DisplayName("malformed ZIP bytes are reported as malformed")
	void malformedArchiveIsDistinct(@TempDir Path temp) throws IOException {
		Path archive = archivePath(temp);
		Files.createDirectories(archive.getParent());
		Files.writeString(archive, "not a zip", StandardCharsets.UTF_8);

		MatchaDiagnosticReport report = MatchaDiagnosticObserver.inspect(temp, Set.of());

		assertEquals(MatchaDiagnosticStatus.MALFORMED, report.status());
		assertEquals(MatchaDiagnosticReport.Severity.WARN, report.severity());
		assertTrue(report.summary().contains("no pack.mcmeta"), report.summary());
	}

	@Test
	@DisplayName("malformed pack metadata is reported without trusting archive roles")
	void malformedMetadataIsDistinct(@TempDir Path temp) throws IOException {
		Path archive = archivePath(temp);
		writeArchive(temp, "{", List.of(DATA_ENTRY), List.of(ASSET_ENTRY));

		MatchaDiagnosticReport report = inspectFixture(temp, Set.of("main"));

		assertEquals(MatchaDiagnosticStatus.MALFORMED, report.status());
		assertEquals(MatchaDiagnosticReport.Severity.WARN, report.severity());
		assertTrue(report.summary().contains("pack.mcmeta"));
		assertTrue(Files.isRegularFile(archive));
	}

	@Test
	@DisplayName("valid pack metadata with the wrong format is unsupported")
	void unsupportedMetadataIsReported(@TempDir Path temp) throws IOException {
		String metadata = "{\"pack\":{\"min_format\":1,\"max_format\":2,"
				+ "\"description\":\"not Matcha\"}}";
		writeArchive(temp, metadata, List.of(DATA_ENTRY), List.of(ASSET_ENTRY));

		MatchaDiagnosticReport report = inspectFixture(temp, Set.of("main"));

		assertEquals(MatchaDiagnosticStatus.UNSUPPORTED, report.status());
		assertEquals(MatchaDiagnosticReport.Severity.WARN, report.severity());
		assertTrue(report.summary().contains("not the pinned Matcha"));
	}

	@Test
	@DisplayName("a non-regular or unreadable archive path is reported without opening it")
	void nonRegularArchiveIsUnreadable(@TempDir Path temp) throws IOException {
		Path archive = archivePath(temp);
		Files.createDirectories(archive);

		MatchaDiagnosticReport report = MatchaDiagnosticObserver.inspect(temp, Set.of());

		assertEquals(MatchaDiagnosticStatus.UNREADABLE, report.status());
		assertEquals(MatchaDiagnosticReport.Severity.WARN, report.severity());
		assertTrue(report.summary().contains("not a regular"));
	}

	@Test
	@DisplayName("an archive containing only data reports its one-sided role")
	void archiveWithoutResourcesIsOneSided(@TempDir Path temp) throws IOException {
		writeArchive(temp, VALID_METADATA, List.of(DATA_ENTRY), List.of());

		MatchaDiagnosticReport report = inspectFixture(temp, Set.of("main"));

		assertEquals(MatchaDiagnosticStatus.ONE_SIDED_ROLE, report.status());
		assertEquals(MatchaDiagnosticReport.Severity.WARN, report.severity());
		assertTrue(report.summary().contains("no resource-pack assets"));
	}

	@Test
	@DisplayName("an archive containing only resources reports the reverse one-sided role")
	void archiveWithoutDataIsOneSided(@TempDir Path temp) throws IOException {
		writeArchive(temp, VALID_METADATA, List.of(), List.of(ASSET_ENTRY));

		MatchaDiagnosticReport report = inspectFixture(temp, Set.of());

		assertEquals(MatchaDiagnosticStatus.ONE_SIDED_ROLE, report.status());
		assertEquals(MatchaDiagnosticReport.Severity.WARN, report.severity());
		assertTrue(report.summary().contains("resource-pack assets but no audited Matcha data namespace"));
	}

	@Test
	@DisplayName("an archive with neither audited role remains a missing decoy")
	void archiveWithoutEitherRoleIsMissing(@TempDir Path temp) throws IOException {
		writeArchive(temp, VALID_METADATA, List.of(), List.of());

		MatchaDiagnosticReport report = inspectFixture(temp, Set.of());

		assertEquals(MatchaDiagnosticStatus.MISSING, report.status());
		assertEquals(MatchaDiagnosticReport.Severity.ERROR, report.severity());
		assertTrue(report.summary().contains("decoy archive"));
	}

	@Test
	@DisplayName("a valid archive whose server data role is absent is reported as one-sided")
	void serverDataRoleCanBeMissing(@TempDir Path temp) throws IOException {
		writeArchive(temp, VALID_METADATA, List.of(DATA_ENTRY), List.of(ASSET_ENTRY));

		MatchaDiagnosticReport report = inspectFixture(temp, Set.of());

		assertEquals(MatchaDiagnosticStatus.ONE_SIDED_ROLE, report.status());
		assertTrue(report.summary().contains("datapack role was not observed"));
		assertTrue(report.summary().contains("cannot verify a remote client's resource-pack role"));
	}

	@Test
	@DisplayName("a direct archive symlink is rejected before the source is opened")
	void directArchiveSymlinkIsUnreadable(@TempDir Path temp) throws IOException {
		Path source = temp.resolve("source.zip");
		writeArchiveAt(source, VALID_METADATA, List.of(DATA_ENTRY), List.of(ASSET_ENTRY), "{}");
		Path archive = archivePath(temp);
		Files.createDirectories(archive.getParent());
		Files.createSymbolicLink(archive, source);

		MatchaDiagnosticReport report = MatchaDiagnosticObserver.inspect(temp, Set.of());

		assertEquals(MatchaDiagnosticStatus.UNREADABLE, report.status());
		assertTrue(report.summary().contains("symbolic link"), report.summary());
	}

	@Test
	@DisplayName("a replaced datapacks directory is rejected rather than followed")
	void datapacksDirectorySymlinkIsUnreadable(@TempDir Path temp) throws IOException {
		Path outside = temp.resolve("outside");
		writeArchiveAt(outside.resolve("Matcha_Flavoured_1_12.zip"),
				VALID_METADATA, List.of(DATA_ENTRY), List.of(ASSET_ENTRY), "{}");
		Path datapacks = temp.resolve("datapacks");
		Files.createSymbolicLink(datapacks, outside);

		MatchaDiagnosticReport report = MatchaDiagnosticObserver.inspect(temp, Set.of());

		assertEquals(MatchaDiagnosticStatus.UNREADABLE, report.status());
		assertTrue(report.summary().contains("datapacks directory is a symbolic link"));
	}

	@Test
	@DisplayName("unsafe archive entry paths are rejected without extraction")
	void unsafeArchiveEntryIsRejected(@TempDir Path temp) throws IOException {
		writeArchive(temp, VALID_METADATA, List.of("../outside"), List.of(ASSET_ENTRY));

		MatchaDiagnosticReport report = inspectFixture(temp, Set.of("main"));

		assertEquals(MatchaDiagnosticStatus.MALFORMED, report.status());
		assertTrue(report.summary().contains("unsafe") || report.summary().contains("non-normalised"));
		assertFalse(Files.exists(temp.resolve("outside")));
	}

	@Test
	@DisplayName("the archive size bound is checked before allocating a snapshot")
	void oversizedArchiveIsUnsupported(@TempDir Path temp) throws IOException {
		MatchaDiagnosticProfile.Profile profile = MatchaDiagnosticProfile.read();
		Path archive = archivePath(temp);
		Files.createDirectories(archive.getParent());
		try (RandomAccessFile file = new RandomAccessFile(archive.toFile(), "rw")) {
			file.setLength(profile.maxArchiveBytes() + 1);
		}

		MatchaDiagnosticReport report = MatchaDiagnosticObserver.inspect(temp, Set.of());

		assertEquals(MatchaDiagnosticStatus.UNSUPPORTED, report.status());
		assertTrue(report.summary().contains("diagnostic bound"));
	}

	@Test
	@DisplayName("an archive with too many entries is rejected before traversal can grow")
	void tooManyEntriesAreUnsupported(@TempDir Path temp) throws IOException {
		List<String> entries = new ArrayList<>();
		for (int index = 0; index < 8193; index++) {
			entries.add("misc/entry-" + index);
		}
		writeArchive(temp, VALID_METADATA, entries, List.of());

		MatchaDiagnosticReport report = inspectFixture(temp, Set.of());

		assertEquals(MatchaDiagnosticStatus.UNSUPPORTED, report.status());
		assertTrue(report.summary().contains("entry bound"));
	}

	@Test
	@DisplayName("an overlong entry name is rejected before content is read")
	void overlongEntryNameIsMalformed(@TempDir Path temp) throws IOException {
		String name = "data/main/" + "a".repeat(520);
		writeArchive(temp, VALID_METADATA, List.of(name), List.of());

		MatchaDiagnosticReport report = inspectFixture(temp, Set.of());

		assertEquals(MatchaDiagnosticStatus.MALFORMED, report.status());
		assertTrue(report.summary().contains("overlong entry name"));
	}

	@Test
	@DisplayName("overlong ZIP entry metadata is unsupported")
	void overlongEntryMetadataIsUnsupported(@TempDir Path temp) throws IOException {
		Path archive = archivePath(temp);
		Files.createDirectories(archive.getParent());
		try (OutputStream output = Files.newOutputStream(archive);
				ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
			writeEntry(zip, "pack.mcmeta", VALID_METADATA);
			ZipEntry entry = new ZipEntry(DATA_ENTRY);
			entry.setTime(FIXED_ZIP_ENTRY_TIME_MILLIS);
			entry.setExtra(new byte[8193]);
			zip.putNextEntry(entry);
			zip.write("{}".getBytes(StandardCharsets.UTF_8));
			zip.closeEntry();
		}

		MatchaDiagnosticReport report = inspectFixture(temp, Set.of());

		assertEquals(MatchaDiagnosticStatus.UNSUPPORTED, report.status());
		assertTrue(report.summary().contains("extra metadata"));
	}

	@Test
	@DisplayName("metadata exceeding its decompression bound is unsupported")
	void overlongPackMetadataIsUnsupported(@TempDir Path temp) throws IOException {
		String metadata = "{\"pack\":{\"min_format\":88.0,\"max_format\":107.1,"
				+ "\"description\":\"" + "x".repeat(65_536) + "\"}}";
		writeArchive(temp, metadata, List.of(DATA_ENTRY), List.of(ASSET_ENTRY));

		MatchaDiagnosticReport report = inspectFixture(temp, Set.of());

		assertEquals(MatchaDiagnosticStatus.UNSUPPORTED, report.status());
		assertTrue(report.summary().contains("diagnostic decompressed-byte bound")
				|| report.summary().contains("diagnostic bound"));
	}

	@Test
	@DisplayName("an entry whose decompressed bytes exceed the bound is unsupported")
	void decompressedEntryBombIsUnsupported(@TempDir Path temp) throws IOException {
		String content = "z".repeat(4 * 1024 * 1024 + 1);
		writeArchiveAt(archivePath(temp), VALID_METADATA, List.of(DATA_ENTRY), List.of(), content);

		MatchaDiagnosticReport report = inspectFixture(temp, Set.of());

		assertEquals(MatchaDiagnosticStatus.UNSUPPORTED, report.status());
		assertTrue(report.summary().contains("decompressed-byte bound")
				|| report.summary().contains("uncompressed bytes"));
	}

	@Test
	@DisplayName("diagnostic inspection does not modify a world fixture")
	void inspectionIsReadOnly(@TempDir Path temp) throws IOException {
		Path worldMarker = temp.resolve("world/level.dat");
		Files.createDirectories(worldMarker.getParent());
		Files.writeString(worldMarker, "disposable-world-marker", StandardCharsets.UTF_8);
		byte[] before = Files.readAllBytes(worldMarker);
		writeArchive(temp, VALID_METADATA, List.of(DATA_ENTRY), List.of(ASSET_ENTRY));

		MatchaDiagnosticObserver.inspect(temp, Set.of("main"), fixtureProfile(archivePath(temp)));

		assertTrue(Files.exists(worldMarker));
		assertEquals(java.util.Arrays.toString(before), java.util.Arrays.toString(Files.readAllBytes(worldMarker)));
	}

	private static MatchaDiagnosticReport inspectFixture(Path temp, Set<String> namespaces) throws IOException {
		return MatchaDiagnosticObserver.inspect(temp, namespaces, fixtureProfile(archivePath(temp)));
	}

	private static MatchaDiagnosticProfile.Profile fixtureProfile(Path archive) throws IOException {
		byte[] bytes = Files.readAllBytes(archive);
		return new MatchaDiagnosticProfile.Profile(
				"Matcha_Flavoured_1_12.zip",
				"synthetic-fixture",
				digest(bytes),
				bytes.length,
				bytes.length + 1L,
				88.0,
				107.1,
				"klei's matcha flavoured 1.12 for 26.2",
				null);
	}

	private static String digest(byte[] bytes) {
		try {
			return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
		} catch (NoSuchAlgorithmException exception) {
			throw new AssertionError(exception);
		}
	}

	private static Path archivePath(Path temp) {
		return temp.resolve("datapacks/Matcha_Flavoured_1_12.zip");
	}

	private static void writeArchive(
			Path temp, String metadata, List<String> dataEntries, List<String> assetEntries) throws IOException {
		writeArchiveAt(archivePath(temp), metadata, dataEntries, assetEntries, "{}");
	}

	private static void writeArchiveAt(
			Path archive,
			String metadata,
			List<String> dataEntries,
			List<String> assetEntries,
			String content) throws IOException {
		Files.createDirectories(archive.getParent());
		try (OutputStream output = Files.newOutputStream(archive);
				ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
			writeEntry(zip, "pack.mcmeta", metadata);
			for (String entry : dataEntries) {
				writeEntry(zip, entry, content);
			}
			for (String entry : assetEntries) {
				writeEntry(zip, entry, content);
			}
		}
	}

	private static void writeEntry(ZipOutputStream zip, String name, String content) throws IOException {
		ZipEntry entry = new ZipEntry(name);
		entry.setTime(FIXED_ZIP_ENTRY_TIME_MILLIS);
		zip.putNextEntry(entry);
		zip.write(content.getBytes(StandardCharsets.UTF_8));
		zip.closeEntry();
	}
}

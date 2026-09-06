package dev.forever.compat.matcha.diagnostic;

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
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MatchaDiagnosticObserverTest {
	private static final Path PINNED_ARCHIVE = Path.of("vendor/matcha/Matcha_Flavoured_1_12.zip");
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
	@DisplayName("the exact archive is healthy when its server datapack role is observed")
	void exactArchiveIsHealthy(@TempDir Path temp) throws IOException {
		Path archive = copyPinnedArchive(temp);

		MatchaDiagnosticReport report = MatchaDiagnosticObserver.inspect(temp, Set.of("main"));

		assertEquals(MatchaDiagnosticStatus.HEALTHY, report.status());
		assertEquals(MatchaDiagnosticReport.Severity.HEALTHY, report.severity());
		assertTrue(report.summary().contains("matches the locked SHA-256"));
		assertTrue(report.summary().contains("cannot verify a remote client's resource-pack role"));
		assertTrue(report.remedies().isEmpty());
		assertTrue(Files.isRegularFile(archive));
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
		assertTrue(remedies.contains("6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248"));
		assertTrue(remedies.contains("datapack") && remedies.contains("resource pack"));
	}

	@Test
	@DisplayName("a structurally valid decoy remains MISSING rather than pretending to be Matcha")
	void decoyArchiveIsMissing(@TempDir Path temp) throws IOException {
		writeArchive(temp, VALID_METADATA, List.of(), List.of());

		MatchaDiagnosticReport report = MatchaDiagnosticObserver.inspect(temp, Set.of("main"));

		assertEquals(MatchaDiagnosticStatus.MISSING, report.status());
		assertEquals(MatchaDiagnosticReport.Severity.ERROR, report.severity());
		assertTrue(report.summary().contains("decoy archive"));
	}

	@Test
	@DisplayName("a valid partially altered real archive reports checksum mismatch, not a parse error")
	void alteredRealArchiveIsChecksumMismatch(@TempDir Path temp) throws IOException {
		Path archive = copyPinnedArchive(temp);
		Files.write(archive, new byte[] {0}, StandardOpenOption.APPEND);

		MatchaDiagnosticReport report = MatchaDiagnosticObserver.inspect(temp, Set.of("main"));

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
		assertTrue(report.summary().contains("malformed"));
	}

	@Test
	@DisplayName("valid pack metadata with the wrong format is unsupported")
	void unsupportedMetadataIsReported(@TempDir Path temp) throws IOException {
		String metadata = "{\"pack\":{\"min_format\":1,\"max_format\":2,"
				+ "\"description\":\"not Matcha\"}}";
		writeArchive(temp, metadata, List.of(DATA_ENTRY), List.of(ASSET_ENTRY));

		MatchaDiagnosticReport report = MatchaDiagnosticObserver.inspect(temp, Set.of("main"));

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

		MatchaDiagnosticReport report = MatchaDiagnosticObserver.inspect(temp, Set.of("main"));

		assertEquals(MatchaDiagnosticStatus.ONE_SIDED_ROLE, report.status());
		assertEquals(MatchaDiagnosticReport.Severity.WARN, report.severity());
		assertTrue(report.summary().contains("no resource-pack assets"));
	}

	@Test
	@DisplayName("a valid archive whose server data role is absent is reported as one-sided")
	void serverDataRoleCanBeMissing(@TempDir Path temp) throws IOException {
		copyPinnedArchive(temp);

		MatchaDiagnosticReport report = MatchaDiagnosticObserver.inspect(temp, Set.of());

		assertEquals(MatchaDiagnosticStatus.ONE_SIDED_ROLE, report.status());
		assertTrue(report.summary().contains("datapack role was not observed"));
		assertTrue(report.summary().contains("cannot verify a remote client's resource-pack role"));
	}

	@Test
	@DisplayName("unsafe archive entry paths are rejected without extraction")
	void unsafeArchiveEntryIsRejected(@TempDir Path temp) throws IOException {
		writeArchive(temp, VALID_METADATA, List.of("../outside"), List.of(ASSET_ENTRY));

		MatchaDiagnosticReport report = MatchaDiagnosticObserver.inspect(temp, Set.of("main"));

		assertEquals(MatchaDiagnosticStatus.MALFORMED, report.status());
		assertTrue(report.summary().contains("unsafe") || report.summary().contains("non-normalised"));
		assertFalse(Files.exists(temp.resolve("outside")));
	}

	@Test
	@DisplayName("the archive size bound is checked before ZIP traversal")
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
	@DisplayName("diagnostic inspection does not modify a world fixture")
	void inspectionIsReadOnly(@TempDir Path temp) throws IOException {
		Path worldMarker = temp.resolve("world/level.dat");
		Files.createDirectories(worldMarker.getParent());
		Files.writeString(worldMarker, "disposable-world-marker", StandardCharsets.UTF_8);
		byte[] before = Files.readAllBytes(worldMarker);
		copyPinnedArchive(temp);

		MatchaDiagnosticObserver.inspect(temp, Set.of("main"));

		assertTrue(Files.exists(worldMarker));
		assertEquals(java.util.Arrays.toString(before), java.util.Arrays.toString(Files.readAllBytes(worldMarker)));
	}

	private static Path copyPinnedArchive(Path temp) throws IOException {
		Path archive = archivePath(temp);
		Files.createDirectories(archive.getParent());
		Files.copy(PINNED_ARCHIVE, archive);
		return archive;
	}

	private static Path archivePath(Path temp) {
		return temp.resolve("datapacks/Matcha_Flavoured_1_12.zip");
	}

	private static void writeArchive(Path temp, String metadata, List<String> dataEntries, List<String> assetEntries)
			throws IOException {
		Path archive = archivePath(temp);
		Files.createDirectories(archive.getParent());
		try (OutputStream output = Files.newOutputStream(archive);
				ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
			writeEntry(zip, "pack.mcmeta", metadata);
			for (String entry : dataEntries) {
				writeEntry(zip, entry, "{}");
			}
			for (String entry : assetEntries) {
				writeEntry(zip, entry, "{}");
			}
		}
	}

	private static void writeEntry(ZipOutputStream zip, String name, String content) throws IOException {
		zip.putNextEntry(new ZipEntry(name));
		zip.write(content.getBytes(StandardCharsets.UTF_8));
		zip.closeEntry();
	}
}

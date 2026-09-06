package dev.forever.compat.matcha.diagnostic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MatchaDiagnosticArchiveTest {
	private static final long FIXED_ZIP_ENTRY_TIME_MILLIS = 315_532_800_000L;

	@Test
	@DisplayName("a standard JDK ZIP filesystem without secure directory handles fails safely")
	void unsupportedFilesystemProviderIsUnreadable(@TempDir Path temp) throws IOException {
		Path zipfsArchive = temp.resolve("zipfs-fixture.zip");
		try (OutputStream output = Files.newOutputStream(zipfsArchive);
				ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
			ZipEntry entry = new ZipEntry("datapacks/Matcha_Flavoured_1_12.zip");
			entry.setTime(FIXED_ZIP_ENTRY_TIME_MILLIS);
			zip.putNextEntry(entry);
			zip.write(new byte[] {0});
			zip.closeEntry();
		}

		try (FileSystem filesystem = FileSystems.newFileSystem(zipfsArchive, Map.of())) {
			MatchaDiagnosticReport report = MatchaDiagnosticObserver.inspect(filesystem.getPath("/"), Set.of("main"));

			assertEquals(MatchaDiagnosticStatus.UNREADABLE, report.status());
			assertEquals(MatchaDiagnosticReport.Severity.WARN, report.severity());
			assertTrue(report.summary().contains("filesystem provider"), report.summary());
			assertTrue(report.summary().contains("SecureDirectoryStream"), report.summary());
			assertTrue(report.summary().contains("raceable fallback"), report.summary());
		}
	}
}

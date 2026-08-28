package dev.forever.tools.audit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchaAuditApplicationTest {
    private static final List<String> REPORT_NAMES = List.of(
            "metadata.json", "input-sha256.txt", "file-inventory.csv", "namespaces.json", "pack-filters.json",
            "recipes.json", "advancements.json", "functions.json", "scoreboards.json", "tags.json",
            "loot-tables.json", "predicates.json", "item-modifiers.json", "registry-data.json", "worldgen.json",
            "assets.json", "vanilla-overrides.csv", "cross-references.json", "unresolved-references.csv",
            "unknown-paths.csv", "summary.md");

    @TempDir
    Path temporaryDirectory;

    @Test
    void directoryAndEquivalentZipProduceEquivalentNormalisedResults() throws Exception {
        Path directory = copyFixture(temporaryDirectory.resolve("input with spaces"));
        Path zip = zipFixture(directory, temporaryDirectory.resolve("fixture with spaces.zip"));
        Path directoryOutput = temporaryDirectory.resolve("directory output");
        Path zipOutput = temporaryDirectory.resolve("zip output");
        assertEquals(ExitCodes.SUCCESS, invoke(directory, directoryOutput));
        assertEquals(ExitCodes.SUCCESS, invoke(zip, zipOutput));

        for (String report : REPORT_NAMES) {
            if (report.equals("metadata.json") || report.equals("input-sha256.txt") || report.equals("summary.md")) {
                continue;
            }
            assertEquals(Files.readString(directoryOutput.resolve(report)), Files.readString(zipOutput.resolve(report)), report);
        }
    }

    @Test
    void outputOrderingIsDeterministic() throws Exception {
        Path directory = copyFixture(temporaryDirectory.resolve("fixture"));
        Path first = temporaryDirectory.resolve("first");
        Path second = temporaryDirectory.resolve("second");
        assertEquals(ExitCodes.SUCCESS, invoke(directory, first));
        assertEquals(ExitCodes.SUCCESS, invoke(directory, second));
        for (String report : REPORT_NAMES) {
            if (!report.equals("metadata.json")) {
                assertEquals(Files.readString(first.resolve(report)), Files.readString(second.resolve(report)), report);
            }
        }
        List<String> inventoryLines = Files.readAllLines(first.resolve("file-inventory.csv"));
        List<String> body = inventoryLines.subList(1, inventoryLines.size());
        List<String> sorted = body.stream().sorted().toList();
        assertEquals(sorted, body);
    }

    @Test
    void sha256IsRecordedCorrectly() throws Exception {
        Path directory = copyFixture(temporaryDirectory.resolve("fixture"));
        Path zip = zipFixture(directory, temporaryDirectory.resolve("fixture.zip"));
        Path output = temporaryDirectory.resolve("output");
        assertEquals(ExitCodes.SUCCESS, invoke(zip, output));
        assertEquals(sha256(Files.readAllBytes(zip)), Files.readString(output.resolve("input-sha256.txt")).trim());
    }

    @Test
    void packMcmetaIsParsed() throws Exception {
        Path output = runFixture();
        JsonObject metadata = json(output.resolve("metadata.json"));
        assertEquals(101, metadata.getAsJsonObject("packMetadata").getAsJsonObject("pack").get("pack_format").getAsInt());
        assertEquals("parsed", metadata.get("packMetadataStatus").getAsString());
    }

    @Test
    void packFiltersAreIdentified() throws Exception {
        JsonObject filters = json(runFixture().resolve("pack-filters.json"));
        assertEquals("observed", filters.get("status").getAsString());
        assertTrue(filters.getAsJsonObject("filter").getAsJsonArray("block").get(0).getAsJsonObject().get("namespace").getAsString().equals("minecraft"));
    }

    @Test
    void vanillaRecipeOverrideIsIdentified() throws Exception {
        String overrides = Files.readString(runFixture().resolve("vanilla-overrides.csv"));
        assertTrue(overrides.contains("minecraft:vanilla"));
        assertTrue(overrides.contains("recipes"));
    }

    @Test
    void advancementRelationshipsAreIdentified() throws Exception {
        JsonArray entries = entries(runFixture().resolve("advancements.json"));
        JsonObject advancement = entries.get(0).getAsJsonObject();
        assertEquals("minecraft:story/root", advancement.get("parent").getAsString());
        assertTrue(containsString(advancement.getAsJsonArray("rewardFunctions"), "matcha:reward"));
        assertTrue(containsString(advancement.getAsJsonArray("rewardRecipes"), "matcha:custom"));
        assertTrue(containsString(advancement.getAsJsonArray("rewardLoot"), "matcha:loot"));
    }

    @Test
    void functionToFunctionReferencesAreIdentified() throws Exception {
        JsonObject main = findById(entries(runFixture().resolve("functions.json")), "matcha:main");
        assertTrue(main.getAsJsonArray("calls").toString().contains("matcha:nested/helper"));
        JsonArray crossReferences = entries(runFixture().resolve("cross-references.json"));
        assertTrue(crossReferences.toString().contains("function-call"));
        assertTrue(crossReferences.toString().contains("matcha:nested/helper"));
    }

    @Test
    void scoreboardObjectiveCreationAndReferenceAreIdentified() throws Exception {
        JsonObject scoreboard = findByObjective(entries(runFixture().resolve("scoreboards.json")), "matcha_points");
        assertTrue(scoreboard.getAsJsonArray("definitions").size() > 0);
        assertTrue(scoreboard.getAsJsonArray("references").size() > 0);
        assertTrue(Files.readString(lastOutput.resolve("summary.md")).contains("Scoreboard objectives detected: 1"));
    }

    @Test
    void tagMembershipIsIdentified() throws Exception {
        JsonObject tag = entries(runFixture().resolve("tags.json")).get(0).getAsJsonObject();
        assertTrue(tag.getAsJsonArray("memberships").toString().contains("minecraft:stick"));
        assertTrue(tag.getAsJsonArray("memberships").toString().contains("#minecraft:logs"));
    }

    @Test
    void resourceModelsTexturesAndLanguageAreInventoried() throws Exception {
        JsonArray assets = entries(runFixture().resolve("assets.json"));
        String text = assets.toString();
        assertTrue(text.contains("\"assetType\":\"models\"") || text.contains("\"assetType\": \"models\""));
        assertTrue(text.contains("\"assetType\":\"textures\"") || text.contains("\"assetType\": \"textures\""));
        assertTrue(text.contains("\"assetType\":\"language\"") || text.contains("\"assetType\": \"language\""));
        assertTrue(text.contains("translationKeyCount"));
    }

    @Test
    void unknownPathsAppearInUnknownPathReport() throws Exception {
        String unknown = Files.readString(runFixture().resolve("unknown-paths.csv"));
        assertTrue(unknown.contains("data/matcha/mystery/unknown.json"));
        assertTrue(unknown.contains("data/matcha/mystery"));
        assertTrue(unknown.contains("assets/matcha/custom/weird.txt"));
    }

    @Test
    void brokenJsonIsReportedWithoutDisappearing() throws Exception {
        Path output = runFixture();
        String inventory = Files.readString(output.resolve("file-inventory.csv"));
        assertTrue(inventory.contains("data/matcha/recipes/broken.json"));
        assertTrue(inventory.contains("invalid-json"));
        JsonObject cross = json(output.resolve("cross-references.json"));
        assertTrue(cross.getAsJsonArray("parseErrors").toString().contains("broken.json"));
        assertTrue(Files.readString(output.resolve("summary.md")).contains("JSON file(s) could not be parsed"));
    }

    @Test
    void unsafeZipPathsAreRejected() throws Exception {
        Path unsafe = temporaryDirectory.resolve("unsafe.zip");
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(unsafe))) {
            output.putNextEntry(new ZipEntry("../escaped.txt"));
            output.write("nope".getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        Path output = temporaryDirectory.resolve("unsafe output");
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        int code = MatchaAuditApplication.run(new String[]{"--input", unsafe.toString(), "--output", output.toString()}, null, new PrintStream(errors));
        assertEquals(ExitCodes.UNSAFE_ARCHIVE, code);
        assertFalse(Files.exists(output));
        assertTrue(errors.toString(StandardCharsets.UTF_8).contains("escapes the archive root"));
    }

    @Test
    void zipSymlinkEntriesAreRejected() throws Exception {
        Path unsafe = temporaryDirectory.resolve("symlink.zip");
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(unsafe))) {
            output.putNextEntry(new ZipEntry("link"));
            output.write("target".getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        byte[] archive = Files.readAllBytes(unsafe);
        int central = findSignature(archive, 0x02014b50);
        assertTrue(central >= 0);
        archive[central + 4] = 20;
        archive[central + 5] = 3;
        int unixSymlinkMode = (0120000 | 0777) << 16;
        putLittleEndianInt(archive, central + 38, unixSymlinkMode);
        Files.write(unsafe, archive, StandardOpenOption.TRUNCATE_EXISTING);

        Path output = temporaryDirectory.resolve("symlink output");
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        int code = MatchaAuditApplication.run(new String[]{"--input", unsafe.toString(), "--output", output.toString()}, null, new PrintStream(errors));
        assertEquals(ExitCodes.UNSAFE_ARCHIVE, code);
        assertFalse(Files.exists(output));
        assertTrue(errors.toString(StandardCharsets.UTF_8).contains("symlink"));
    }

    @Test
    void toolDoesNotWriteOutsideSelectedOutputDirectory() throws Exception {
        Path input = copyFixture(temporaryDirectory.resolve("input"));
        Path sentinel = temporaryDirectory.resolve("sentinel.txt");
        Files.writeString(sentinel, "unchanged", StandardCharsets.UTF_8);
        Path output = temporaryDirectory.resolve("selected output");
        assertEquals(ExitCodes.SUCCESS, invoke(input, output));
        assertEquals("unchanged", Files.readString(sentinel));
        assertEquals(new TreeSet<>(REPORT_NAMES), new TreeSet<>(Files.list(output).map(path -> path.getFileName().toString()).toList()));
        assertEquals(Set.of("input", "sentinel.txt", "selected output"), topLevelNames(temporaryDirectory));
    }

    @Test
    void identicalRunsAreByteStableExceptForIsolatedTimestamp() throws Exception {
        Path input = copyFixture(temporaryDirectory.resolve("fixture"));
        Path output = temporaryDirectory.resolve("output");
        assertEquals(ExitCodes.SUCCESS, invoke(input, output));
        Map<String, byte[]> first = snapshotReports(output);
        String firstMetadata = Files.readString(output.resolve("metadata.json"));
        assertEquals(ExitCodes.SUCCESS, invoke(input, output));
        Map<String, byte[]> second = snapshotReports(output);
        for (String report : REPORT_NAMES) {
            if (!report.equals("metadata.json")) {
                assertEquals(new String(first.get(report), StandardCharsets.UTF_8), new String(second.get(report), StandardCharsets.UTF_8), report);
            }
        }
        JsonObject firstJson = JsonParser.parseString(firstMetadata).getAsJsonObject();
        JsonObject secondJson = json(output.resolve("metadata.json"));
        firstJson.remove("generatedAt");
        secondJson.remove("generatedAt");
        assertEquals(firstJson, secondJson);
        assertNotNull(secondJson);
    }

    private Path lastOutput;

    private Path runFixture() throws Exception {
        Path input = copyFixture(temporaryDirectory.resolve("fixture " + System.nanoTime()));
        lastOutput = temporaryDirectory.resolve("output " + System.nanoTime());
        assertEquals(ExitCodes.SUCCESS, invoke(input, lastOutput));
        return lastOutput;
    }

    private int invoke(Path input, Path output) {
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        return MatchaAuditApplication.run(new String[]{
                "--input", input.toString(), "--output", output.toString(), "--version-label", "synthetic"
        }, null, new PrintStream(errors));
    }

    private static Path fixtureRoot() throws URISyntaxException {
        return Path.of(Objects.requireNonNull(MatchaAuditApplicationTest.class.getResource("/fixture-pack")).toURI());
    }

    private static Path copyFixture(Path destination) throws IOException, URISyntaxException {
        Path source = fixtureRoot();
        try (var paths = Files.walk(source)) {
            for (Path path : paths.sorted().toList()) {
                Path relative = source.relativize(path);
                Path target = destination.resolve(relative);
                if (Files.isDirectory(path)) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(path, target);
                }
            }
        }
        return destination;
    }

    private static Path zipFixture(Path directory, Path zip) throws IOException {
        List<Path> files;
        try (var paths = Files.walk(directory)) {
            files = paths.filter(Files::isRegularFile).sorted().toList();
        }
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(zip))) {
            for (Path file : files) {
                String name = directory.relativize(file).toString().replace(java.io.File.separatorChar, '/');
                ZipEntry entry = new ZipEntry(name);
                entry.setTime(0L);
                output.putNextEntry(entry);
                output.write(Files.readAllBytes(file));
                output.closeEntry();
            }
        }
        return zip;
    }

    private static JsonObject json(Path path) throws IOException {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }

    private static JsonArray entries(Path path) throws IOException {
        return json(path).getAsJsonArray("entries");
    }

    private static boolean containsString(JsonArray values, String expected) {
        for (var value : values) {
            if (value.getAsString().equals(expected)) {
                return true;
            }
        }
        return false;
    }

    private static JsonObject findById(JsonArray values, String id) {
        for (var value : values) {
            JsonObject object = value.getAsJsonObject();
            if (object.has("id") && object.get("id").getAsString().equals(id)) {
                return object;
            }
        }
        throw new AssertionError("No entry with id " + id);
    }

    private static JsonObject findByObjective(JsonArray values, String objective) {
        for (var value : values) {
            JsonObject object = value.getAsJsonObject();
            if (object.has("objective") && object.get("objective").getAsString().equals(objective)) {
                return object;
            }
        }
        throw new AssertionError("No scoreboard objective " + objective);
    }

    private static String sha256(byte[] bytes) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
        StringBuilder result = new StringBuilder();
        for (byte value : digest) {
            result.append(String.format("%02x", value));
        }
        return result.toString();
    }

    private static int findSignature(byte[] bytes, int signature) {
        for (int offset = 0; offset <= bytes.length - 4; offset++) {
            if ((bytes[offset] & 0xff) == (signature & 0xff)
                    && (bytes[offset + 1] & 0xff) == ((signature >>> 8) & 0xff)
                    && (bytes[offset + 2] & 0xff) == ((signature >>> 16) & 0xff)
                    && (bytes[offset + 3] & 0xff) == ((signature >>> 24) & 0xff)) {
                return offset;
            }
        }
        return -1;
    }

    private static void putLittleEndianInt(byte[] bytes, int offset, int value) {
        bytes[offset] = (byte) value;
        bytes[offset + 1] = (byte) (value >>> 8);
        bytes[offset + 2] = (byte) (value >>> 16);
        bytes[offset + 3] = (byte) (value >>> 24);
    }

    private static Map<String, byte[]> snapshotReports(Path directory) throws IOException {
        Map<String, byte[]> result = new HashMap<>();
        for (String report : REPORT_NAMES) {
            result.put(report, Files.readAllBytes(directory.resolve(report)));
        }
        return result;
    }

    private static Set<String> topLevelNames(Path directory) throws IOException {
        try (var paths = Files.list(directory)) {
            return paths.map(path -> path.getFileName().toString()).collect(java.util.stream.Collectors.toSet());
        }
    }
}

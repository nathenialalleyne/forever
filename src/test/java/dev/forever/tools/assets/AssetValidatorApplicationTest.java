package dev.forever.tools.assets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AssetValidatorApplicationTest {

	private static final String HEADER = "asset_id,category,system,milestone,required_states,base_resolution,"
				+ "reuses_vanilla,accessibility_requirement,notes,status\n";

	@TempDir
	Path temporaryDirectory;

	@Test
	void validSixteenBySixteenPngPasses() throws Exception {
		Path assets = createAssetsDirectory();
		writePng(assets.resolve("textures/item/valid_asset.png"), 16, 16, BufferedImage.TYPE_INT_ARGB, 0xff336699);
		Path manifest = writeManifest(row("valid_asset", "final", "16x16"));

		RunResult result = run(assets, manifest);

		assertEquals(0, result.exitCode(), result.output() + result.errors());
		assertTrue(result.output().contains("textures/item/valid_asset.png: 16x16, RGBA"));
		assertTrue(result.output().contains("Result: CLEAN"));
		assertTrue(result.errors().isEmpty());
	}

	@Test
	void incorrectDimensionsAreRejectedByDefault() throws Exception {
		Path assets = createAssetsDirectory();
		writePng(assets.resolve("textures/item/wide_asset.png"), 32, 16, BufferedImage.TYPE_INT_ARGB, 0xff336699);
		Path manifest = writeManifest(row("wide_asset", "final", "16x16"));

		RunResult result = run(assets, manifest);

		assertEquals(1, result.exitCode());
		assertTrue(result.output().contains("UNEXPECTED_DIMENSIONS"));
	}

	@Test
	void manifestResolutionCanWhitelistAnotherItemSize() throws Exception {
		Path assets = createAssetsDirectory();
		writePng(assets.resolve("textures/item/large_asset.png"), 32, 32, BufferedImage.TYPE_INT_ARGB, 0xff336699);
		Path manifest = writeManifest(row("large_asset", "final", "32x32"));

		RunResult result = run(assets, manifest);

		assertEquals(0, result.exitCode(), result.output() + result.errors());
		assertTrue(result.output().contains("Violations: 0"), result.output());
	}

	@Test
	void uppercaseFilenameStemIsRejected() throws Exception {
		Path assets = createAssetsDirectory();
		writePng(assets.resolve("textures/item/Uppercase.png"), 16, 16, BufferedImage.TYPE_INT_ARGB, 0xff336699);
		Path manifest = writeManifest(row("Uppercase", "final", "16x16"));

		RunResult result = run(assets, manifest);

		assertEquals(1, result.exitCode());
		assertTrue(result.output().contains("NAME_NOT_SNAKE_CASE"));
	}

	@Test
	void fullyTransparentImageIsRejected() throws Exception {
		Path assets = createAssetsDirectory();
		writePng(assets.resolve("textures/item/transparent_asset.png"), 16, 16, BufferedImage.TYPE_INT_ARGB, 0);
		Path manifest = writeManifest(row("transparent_asset", "final", "16x16"));

		RunResult result = run(assets, manifest);

		assertEquals(1, result.exitCode());
		assertTrue(result.output().contains("FULLY_TRANSPARENT"));
	}

	@Test
	void productionPngWithoutManifestRowIsRejected() throws Exception {
		Path assets = createAssetsDirectory();
		writePng(assets.resolve("textures/item/unlisted_asset.png"), 16, 16, BufferedImage.TYPE_INT_ARGB, 0xff336699);
		Path manifest = writeManifest();

		RunResult result = run(assets, manifest);

		assertEquals(1, result.exitCode());
		assertTrue(result.output().contains("UNMANIFESTED_ASSET"));
	}

	@Test
	void missingTextureReferenceIsRejected() throws Exception {
		Path assets = createAssetsDirectory();
		writePng(assets.resolve("textures/item/reference_owner.png"), 16, 16, BufferedImage.TYPE_INT_ARGB, 0xff336699);
		writeJson(assets.resolve("models/item/reference_owner.json"),
				"{\"textures\":{\"layer0\":\"forever:item/missing_texture\"}}\n");
		Path manifest = writeManifest(row("reference_owner", "final", "16x16"));

		RunResult result = run(assets, manifest);

		assertEquals(1, result.exitCode());
		assertTrue(result.output().contains("MISSING_TEXTURE_REFERENCE"));
		assertTrue(result.output().contains("forever:item/missing_texture"));
	}

	@Test
	void minecraftReferencesAreIgnoredAndExistingForeverReferencesPass() throws Exception {
		Path assets = createAssetsDirectory();
		writePng(assets.resolve("textures/item/reference_owner.png"), 16, 16, BufferedImage.TYPE_INT_ARGB, 0xff336699);
		writePng(assets.resolve("textures/item/reference_texture.png"), 16, 16, BufferedImage.TYPE_INT_ARGB, 0xff336699);
		writeJson(assets.resolve("models/item/reference_owner.json"),
				"{\"parent\":\"minecraft:item/generated\",\"textures\":{"
						+ "\"layer0\":\"forever:item/reference_texture\"}}\n");
		Path manifest = writeManifest(row("reference_owner", "final", "16x16"),
				row("reference_texture", "final", "16x16"));

		RunResult result = run(assets, manifest);

		assertEquals(0, result.exitCode(), result.output() + result.errors());
		assertTrue(result.output().contains("Violations: 0"), result.output());
	}

	@Test
	void plannedRowsDoNotFailWhenTheirPngDoesNotExist() throws Exception {
		Path assets = createAssetsDirectory();
		Path manifest = writeManifest(row("planned_asset", "planned", "16x16"));

		RunResult result = run(assets, manifest);

		assertEquals(0, result.exitCode(), result.output() + result.errors());
		assertTrue(result.output().contains("Violations: 0"), result.output());
	}

	@Test
	void duplicateProductionAndManifestAssetIdsAreReported() throws Exception {
		Path assets = createAssetsDirectory();
		writePng(assets.resolve("textures/item/duplicate_asset.png"), 16, 16, BufferedImage.TYPE_INT_ARGB, 0xff336699);
		writePng(assets.resolve("textures/block/duplicate_asset.png"), 16, 16, BufferedImage.TYPE_INT_ARGB, 0xff336699);
		Path manifest = writeManifest(row("duplicate_asset", "final", "16x16"),
				row("duplicate_asset", "final", "16x16"));

		RunResult result = run(assets, manifest);

		assertEquals(1, result.exitCode());
		assertTrue(result.output().contains("DUPLICATE_ASSET_ID"));
	}

	@Test
	void placeholderRowWithoutPngFailsButPlannedRowDoesNot() throws Exception {
		Path assets = createAssetsDirectory();
		Path manifest = writeManifest(row("planned_asset", "planned", "16x16"),
				row("placeholder_asset", "placeholder", "16x16"));

		RunResult result = run(assets, manifest);

		assertEquals(1, result.exitCode());
		assertTrue(result.output().contains("MANIFEST_ENTRY_MISSING_ASSET"));
		assertTrue(result.output().contains("placeholder_asset"));
		assertFalse(result.output().contains("planned_asset', but no production PNG"));
	}

	@Test
	void gitkeepMarkersAreNotTreatedAsAssets() throws Exception {
		// The production tree ships empty directories that git can only track via a
		// .gitkeep marker. Treating those markers as assets made the real repository
		// fail validation with nine spurious naming violations.
		Path assets = createAssetsDirectory();
		Files.createDirectories(assets.resolve("textures/item"));
		Files.writeString(assets.resolve("textures/item/.gitkeep"), "");
		Files.createDirectories(assets.resolve("lang"));
		Files.writeString(assets.resolve("lang/.gitkeep"), "");
		writePng(assets.resolve("textures/item/valid_asset.png"), 16, 16, BufferedImage.TYPE_INT_ARGB, 0xff336699);
		Path manifest = writeManifest(row("valid_asset", "final", "16x16"));

		RunResult result = run(assets, manifest);

		assertEquals(0, result.exitCode(), result.output() + result.errors());
		assertFalse(result.output().contains("gitkeep"));
		assertTrue(result.output().contains("PNG files: 1"));
	}

	@Test
	void indexedAndUndecodablePngsAreRejected() throws Exception {
		Path assets = createAssetsDirectory();
		writePng(assets.resolve("textures/item/indexed_asset.png"), 16, 16, BufferedImage.TYPE_BYTE_INDEXED, 0xff336699);
		Files.createDirectories(assets.resolve("textures/item"));
		Files.write(assets.resolve("textures/item/broken_asset.png"), new byte[] {1, 2, 3, 4});
		Path manifest = writeManifest(row("indexed_asset", "final", "16x16"),
				row("broken_asset", "final", "16x16"));

		RunResult result = run(assets, manifest);

		assertEquals(1, result.exitCode());
		assertTrue(result.output().contains("UNSUPPORTED_IMAGE_FORMAT"));
		assertTrue(result.output().contains("INDEXED"));
		assertTrue(result.output().contains("UNDECODABLE"));
	}

	@Test
	void rfc4180QuotedManifestFieldsAreAccepted() throws Exception {
		Path assets = createAssetsDirectory();
		writePng(assets.resolve("textures/item/quoted_asset.png"), 16, 16, BufferedImage.TYPE_INT_ARGB, 0xff336699);
		String quotedRow = csvRow(List.of("quoted_asset", "item", "testing", "M2", "ordinary", "16x16",
				"yes", "text", "A note, with a \"quote\"\nand a newline", "final"));
		Path manifest = writeManifestText(quotedRow + "\n");

		RunResult result = run(assets, manifest);

		assertEquals(0, result.exitCode(), result.output() + result.errors());
	}

	@Test
	void exitCodesDistinguishCleanViolationsAndUsageErrors() throws Exception {
		Path assets = createAssetsDirectory();
		writePng(assets.resolve("textures/item/valid_asset.png"), 16, 16, BufferedImage.TYPE_INT_ARGB, 0xff336699);
		Path manifest = writeManifest(row("valid_asset", "final", "16x16"));

		RunResult clean = run(assets, manifest);
		RunResult violation = run(assets, writeManifest(row("other_asset", "planned", "16x16")));
		RunResult usage = run(temporaryDirectory.resolve("missing-assets"), manifest);

		assertEquals(0, clean.exitCode(), clean.output() + clean.errors());
		assertEquals(1, violation.exitCode());
		assertEquals(2, usage.exitCode());
		assertTrue(usage.errors().contains("does not exist"));
		assertTrue(usage.errors().contains("--assets"));
		assertFalse(usage.errors().contains("Exception in thread"));
	}

	@Test
	void missingManifestIsAUsageError() throws Exception {
		Path assets = createAssetsDirectory();

		RunResult result = run(assets, temporaryDirectory.resolve("missing-manifest.csv"));

		assertEquals(2, result.exitCode());
		assertTrue(result.errors().contains("Manifest file does not exist"));
		assertTrue(result.errors().contains("--manifest"));
	}

	@Test
	void invalidPathArgumentIsAUsageErrorWithoutAStackTrace() throws Exception {
		Path manifest = writeManifest();

		RunResult result = runArguments("--assets", String.valueOf((char) 0), "--manifest", manifest.toString());

		assertEquals(2, result.exitCode());
		assertTrue(result.errors().contains("invalid path"));
		assertFalse(result.errors().contains("Exception in thread"));
	}

	@Test
	void jsonReportIsWrittenDeterministically() throws Exception {
		Path assets = createAssetsDirectory();
		writePng(assets.resolve("textures/item/valid_asset.png"), 16, 16, BufferedImage.TYPE_INT_ARGB, 0xff336699);
		Path manifest = writeManifest(row("valid_asset", "final", "16x16"));
		Path json = temporaryDirectory.resolve("reports/assets.json");
		Files.createDirectories(json.getParent());

		RunResult first = run(assets, manifest, json);
		String firstJson = Files.readString(json);
		RunResult second = run(assets, manifest, json);

		assertEquals(0, first.exitCode(), first.output() + first.errors());
		assertEquals(0, second.exitCode(), second.output() + second.errors());
		assertEquals(firstJson, Files.readString(json));
		assertTrue(firstJson.contains("\"colour_model\": \"RGBA\""));
	}

	private Path createAssetsDirectory() throws Exception {
		Path assets = temporaryDirectory.resolve("assets/forever");
		Files.createDirectories(assets);
		return assets;
	}

	private Path writeManifest(String... rows) throws Exception {
		return writeManifestText(String.join("\n", rows) + (rows.length == 0 ? "" : "\n"));
	}

	private Path writeManifestText(String rows) throws Exception {
		Path manifest = temporaryDirectory.resolve("asset-manifest.csv");
		Files.writeString(manifest, HEADER + rows, StandardCharsets.UTF_8);
		return manifest;
	}

	private static String row(String assetId, String status, String resolution) {
		return csvRow(List.of(assetId, "item", "testing", "M2", "ordinary", resolution, "yes", "text", "test asset", status));
	}

	private static String csvRow(List<String> fields) {
		return fields.stream().map(AssetValidatorApplicationTest::csvField)
				.collect(java.util.stream.Collectors.joining(","));
	}

	private static String csvField(String field) {
		if (field.indexOf(',') >= 0 || field.indexOf('"') >= 0 || field.indexOf('\n') >= 0 || field.indexOf('\r') >= 0) {
			return '"' + field.replace("\"", "\"\"") + '"';
		}
		return field;
	}

	private static void writePng(Path path, int width, int height, int imageType, int pixel) throws Exception {
		Files.createDirectories(path.getParent());
		BufferedImage image = new BufferedImage(width, height, imageType);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				image.setRGB(x, y, pixel);
			}
		}
		assertTrue(ImageIO.write(image, "png", path.toFile()), "The JDK PNG writer must be available for the fixture.");
	}

	private static void writeJson(Path path, String content) throws Exception {
		Files.createDirectories(path.getParent());
		Files.writeString(path, content, StandardCharsets.UTF_8);
	}

	private RunResult run(Path assets, Path manifest) {
		return run(assets, manifest, null);
	}

	private RunResult run(Path assets, Path manifest, Path json) {
		String[] arguments = json == null
				? new String[] {"--assets", assets.toString(), "--manifest", manifest.toString()}
				: new String[] {"--assets", assets.toString(), "--manifest", manifest.toString(), "--json", json.toString()};
		return runArguments(arguments);
	}

	private RunResult runArguments(String... arguments) {
		ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
		ByteArrayOutputStream errorBytes = new ByteArrayOutputStream();
		PrintStream output = new PrintStream(outputBytes, true, StandardCharsets.UTF_8);
		PrintStream errors = new PrintStream(errorBytes, true, StandardCharsets.UTF_8);
		int exitCode = AssetValidatorApplication.run(arguments, output, errors);
		return new RunResult(exitCode, outputBytes.toString(StandardCharsets.UTF_8), errorBytes.toString(StandardCharsets.UTF_8));
	}

	private record RunResult(int exitCode, String output, String errors) {
	}
}

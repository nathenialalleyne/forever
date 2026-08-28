package dev.forever.tools.assets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Reads and validates the exact asset manifest schema. */
final class ManifestReader {

	private static final List<String> HEADER = List.of(
			"asset_id", "category", "system", "milestone", "required_states", "base_resolution",
			"reuses_vanilla", "accessibility_requirement", "notes", "status");

	private ManifestReader() {
	}

	static AssetManifest read(Path path) throws IOException, UsageException {
		List<List<String>> records = Rfc4180Parser.parse(Files.readString(path, StandardCharsets.UTF_8));
		if (records.isEmpty()) {
			throw new UsageException("Manifest is empty. The first row must contain the exact required header.");
		}
		if (!HEADER.equals(records.getFirst())) {
			throw new UsageException("Manifest header must be exactly: " + String.join(",", HEADER) + ".");
		}
		List<ManifestRow> rows = new ArrayList<>();
		for (int index = 1; index < records.size(); index++) {
			List<String> fields = records.get(index);
			int rowNumber = index + 1;
			if (fields.size() != HEADER.size()) {
				throw new UsageException("Manifest row " + rowNumber + " has " + fields.size()
						+ " columns, but exactly " + HEADER.size() + " are required.");
			}
			if (fields.getFirst().isBlank()) {
				throw new UsageException("Manifest row " + rowNumber + " has an empty asset_id.");
			}
			ManifestStatus status = ManifestStatus.parse(fields.get(9), rowNumber);
			rows.add(new ManifestRow(fields.get(0), fields.get(1), fields.get(2), fields.get(3), fields.get(4),
					fields.get(5), fields.get(6), fields.get(7), fields.get(8), status));
		}
		return new AssetManifest(rows);
	}
}

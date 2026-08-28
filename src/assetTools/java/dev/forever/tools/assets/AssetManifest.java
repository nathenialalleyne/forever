package dev.forever.tools.assets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/** Parsed manifest rows and deterministic lookup helpers. */
record AssetManifest(List<ManifestRow> rows) {

	AssetManifest {
		rows = List.copyOf(rows);
	}

	Optional<ManifestRow> firstRowFor(String assetId) {
		return rows.stream().filter(row -> row.assetId().equals(assetId)).findFirst();
	}

	Map<String, List<ManifestRow>> rowsByAssetId() {
		TreeMap<String, List<ManifestRow>> grouped = new TreeMap<>();
		for (ManifestRow row : rows) {
			grouped.computeIfAbsent(row.assetId(), ignored -> new ArrayList<>()).add(row);
		}
		TreeMap<String, List<ManifestRow>> immutable = new TreeMap<>();
		for (Map.Entry<String, List<ManifestRow>> entry : grouped.entrySet()) {
			immutable.put(entry.getKey(), List.copyOf(entry.getValue()));
		}
		return Collections.unmodifiableMap(immutable);
	}
}

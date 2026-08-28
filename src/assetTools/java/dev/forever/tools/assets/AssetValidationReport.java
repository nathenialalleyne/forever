package dev.forever.tools.assets;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Immutable validation result shared by the human and JSON renderers. */
record AssetValidationReport(List<ProductionAsset> productionAssets, List<AssetViolation> violations) {

	AssetValidationReport {
		productionAssets = productionAssets.stream()
				.sorted(Comparator.comparing(asset -> pathName(asset.file().relativePath())))
				.toList();
		violations = violations.stream()
				.sorted(Comparator.comparing((AssetViolation violation) -> violation.code().name())
						.thenComparing(AssetViolation::subject)
						.thenComparing(AssetViolation::message))
				.toList();
	}

	boolean hasViolations() {
		return !violations.isEmpty();
	}

	Map<ViolationCode, List<AssetViolation>> violationsByCode() {
		TreeMap<ViolationCode, List<AssetViolation>> grouped = violations.stream()
				.collect(Collectors.groupingBy(AssetViolation::code, TreeMap::new, Collectors.toList()));
		return Collections.unmodifiableMap(grouped);
	}

	private static String pathName(Path path) {
		return path.toString().replace(path.getFileSystem().getSeparator(), "/");
	}
}

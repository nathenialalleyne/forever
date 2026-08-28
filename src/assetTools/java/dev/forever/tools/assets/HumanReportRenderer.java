package dev.forever.tools.assets;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** Renders the deterministic report intended for developers and CI logs. */
final class HumanReportRenderer {

	private HumanReportRenderer() {
	}

	static String render(AssetValidationReport report) {
		StringBuilder output = new StringBuilder();
		output.append("Asset validation report\n\n");
		output.append("PNG assets:\n");
		if (report.productionAssets().isEmpty()) {
			output.append("  (none)\n");
		} else {
			for (ProductionAsset asset : report.productionAssets()) {
				ImageSummary image = asset.image();
				output.append("  ").append(pathName(asset.file().relativePath()))
						.append(": ").append(image.dimensionsLabel())
						.append(", ").append(image.colourModel()).append('\n');
			}
		}

		output.append("\nViolations by code:\n");
		if (!report.hasViolations()) {
			output.append("  (none)\n");
		} else {
			for (Map.Entry<ViolationCode, List<AssetViolation>> entry : report.violationsByCode().entrySet()) {
				output.append("  ").append(entry.getKey()).append(" (").append(entry.getValue().size()).append("):\n");
				for (AssetViolation violation : entry.getValue()) {
					output.append("    - ").append(violation.subject()).append(": ")
							.append(violation.message()).append('\n');
				}
			}
		}

		output.append("\nCounts:\n");
		output.append("  PNG files: ").append(report.productionAssets().size()).append('\n');
		output.append("  Violations: ").append(report.violations().size()).append('\n');
		// Only non-zero codes are listed. Printing every code with a zero count would
		// mean the report always mentions every code name, which makes the output
		// impossible to search for a specific failure and defeats "this run did not
		// report X" checks.
		for (ViolationCode code : ViolationCode.values()) {
			long count = report.violations().stream().filter(violation -> violation.code() == code).count();
			if (count > 0) {
				output.append("  ").append(code).append(": ").append(count).append('\n');
			}
		}
		output.append("  Result: ").append(report.hasViolations() ? "FAILED" : "CLEAN").append('\n');
		return output.toString();
	}

	private static String pathName(Path path) {
		return path.toString().replace(path.getFileSystem().getSeparator(), "/");
	}
}

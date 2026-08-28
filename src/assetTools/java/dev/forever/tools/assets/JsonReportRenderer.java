package dev.forever.tools.assets;

import java.nio.file.Path;

/** Serialises validation output without adding a JSON library to the developer tool. */
final class JsonReportRenderer {

	private JsonReportRenderer() {
	}

	static String render(AssetValidationReport report) {
		StringBuilder json = new StringBuilder();
		json.append("{\n  \"assets\": [");
		for (int index = 0; index < report.productionAssets().size(); index++) {
			if (index > 0) {
				json.append(',');
			}
			ProductionAsset asset = report.productionAssets().get(index);
			ImageSummary image = asset.image();
			json.append("\n    {\n")
					.append("      \"asset_id\": ").append(quote(asset.assetId())).append(",\n")
					.append("      \"path\": ").append(quote(pathName(asset.file().relativePath()))).append(",\n")
					.append("      \"width\": ").append(image.width()).append(",\n")
					.append("      \"height\": ").append(image.height()).append(",\n")
					.append("      \"colour_model\": ").append(quote(image.colourModel().name())).append('\n')
					.append("    }");
		}
		if (!report.productionAssets().isEmpty()) {
			json.append('\n');
		}
		json.append("\n  ],\n  \"violations\": [");
		for (int index = 0; index < report.violations().size(); index++) {
			if (index > 0) {
				json.append(',');
			}
			AssetViolation violation = report.violations().get(index);
			json.append("\n    {\n")
					.append("      \"code\": ").append(quote(violation.code().name())).append(",\n")
					.append("      \"subject\": ").append(quote(violation.subject())).append(",\n")
					.append("      \"message\": ").append(quote(violation.message())).append('\n')
					.append("    }");
		}
		if (!report.violations().isEmpty()) {
			json.append('\n');
		}
		json.append("\n  ],\n  \"counts\": {\n")
				.append("    \"png_files\": ").append(report.productionAssets().size()).append(",\n")
				.append("    \"violations\": ").append(report.violations().size()).append(",\n")
				.append("    \"by_code\": {");
		for (int index = 0; index < ViolationCode.values().length; index++) {
			if (index > 0) {
				json.append(',');
			}
			ViolationCode code = ViolationCode.values()[index];
			long count = report.violations().stream().filter(violation -> violation.code() == code).count();
			json.append('\n').append("      ").append(quote(code.name())).append(": ").append(count);
		}
		json.append("\n    }\n  },\n  \"result\": ")
				.append(quote(report.hasViolations() ? "failed" : "clean"))
				.append("\n}\n");
		return json.toString();
	}

	private static String quote(String value) {
		StringBuilder quoted = new StringBuilder("\"");
		for (int index = 0; index < value.length(); index++) {
			char character = value.charAt(index);
			switch (character) {
				case '"' -> quoted.append("\\\"");
				case '\\' -> quoted.append("\\\\");
				case '\b' -> quoted.append("\\b");
				case '\f' -> quoted.append("\\f");
				case '\n' -> quoted.append("\\n");
				case '\r' -> quoted.append("\\r");
				case '\t' -> quoted.append("\\t");
				default -> {
					if (character < 0x20) {
						quoted.append(String.format("\\u%04x", (int) character));
					} else {
						quoted.append(character);
					}
				}
			}
		}
		return quoted.append('"').toString();
	}

	private static String pathName(Path path) {
		return path.toString().replace(path.getFileSystem().getSeparator(), "/");
	}
}

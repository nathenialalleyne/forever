package dev.forever.tools.assets;

import java.nio.file.Path;

/** Parses the deliberately small validator command line. */
final class AssetValidationOptionsParser {

	private AssetValidationOptionsParser() {
	}

	static AssetValidationOptions parse(String[] arguments) throws UsageException {
		if (arguments == null) {
			throw new UsageException("Arguments must not be null.");
		}
		Path assetsDirectory = AssetValidationOptions.DEFAULT_ASSETS_DIRECTORY;
		Path manifestFile = AssetValidationOptions.DEFAULT_MANIFEST_FILE;
		Path jsonOutput = null;
		boolean assetsSeen = false;
		boolean manifestSeen = false;
		boolean jsonSeen = false;

		for (int index = 0; index < arguments.length; index++) {
			String argument = arguments[index];
			switch (argument) {
				case "--assets" -> {
					if (assetsSeen) {
						throw new UsageException("Option --assets was supplied more than once.");
					}
					assetsSeen = true;
					assetsDirectory = Path.of(valueFor(arguments, index, argument));
					index++;
				}
				case "--manifest" -> {
					if (manifestSeen) {
						throw new UsageException("Option --manifest was supplied more than once.");
					}
					manifestSeen = true;
					manifestFile = Path.of(valueFor(arguments, index, argument));
					index++;
				}
				case "--json" -> {
					if (jsonSeen) {
						throw new UsageException("Option --json was supplied more than once.");
					}
					jsonSeen = true;
					jsonOutput = Path.of(valueFor(arguments, index, argument));
					index++;
				}
				default -> throw new UsageException("Unknown argument: " + argument + ".");
			}
		}
		return new AssetValidationOptions(assetsDirectory, manifestFile, jsonOutput);
	}

	private static String valueFor(String[] arguments, int optionIndex, String option)
			throws UsageException {
		if (optionIndex + 1 >= arguments.length || arguments[optionIndex + 1].isBlank()
				|| arguments[optionIndex + 1].startsWith("--")) {
			throw new UsageException("Option " + option + " requires a non-empty value.");
		}
		return arguments[optionIndex + 1];
	}
}

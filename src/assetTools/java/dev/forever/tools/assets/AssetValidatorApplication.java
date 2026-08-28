package dev.forever.tools.assets;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/** Command-line entry point for validating Forever presentation assets. */
public final class AssetValidatorApplication {

	static final String USAGE = "Usage: AssetValidatorApplication [--assets <dir>] [--manifest <file>]"
			+ " [--json <out>]";

	private AssetValidatorApplication() {
	}

	/**
	 * Runs the validator without terminating the current JVM.
	 *
	 * <p>Keeping the runner separate from {@link #main(String[])} makes exit-code behaviour
	 * testable without replacing the test process's lifecycle.
	 */
	public static int run(String[] arguments, PrintStream output, PrintStream errors) {
		Objects.requireNonNull(output, "output");
		Objects.requireNonNull(errors, "errors");
		if (arguments != null && arguments.length == 1 && "--help".equals(arguments[0])) {
			output.println(USAGE);
			return 0;
		}

		try {
			AssetValidationOptions options = AssetValidationOptionsParser.parse(arguments);
			AssetValidationReport report = new AssetValidator().validate(
					options.assetsDirectory(), options.manifestFile());
			if (options.jsonOutput() != null) {
				writeJsonReport(options.jsonOutput(), report);
			}
			output.print(HumanReportRenderer.render(report));
			return report.hasViolations() ? 1 : 0;
		} catch (UsageException exception) {
			errors.println("Asset validator usage error: " + exception.getMessage());
			errors.println(USAGE);
			return 2;
		} catch (IOException exception) {
			errors.println("Asset validator I/O error: " + exception.getMessage());
			return 2;
		}
	}

	public static void main(String[] arguments) {
		int exitCode = run(arguments, System.out, System.err);
		if (exitCode != 0) {
			System.exit(exitCode);
		}
	}

	private static void writeJsonReport(Path output, AssetValidationReport report) throws IOException {
		Path parent = output.toAbsolutePath().normalize().getParent();
		if (parent != null && !Files.exists(parent)) {
			throw new IOException("JSON report directory does not exist: " + parent
					+ ". Create it or choose an existing directory.");
		}
		Files.writeString(output, JsonReportRenderer.render(report), StandardCharsets.UTF_8,
				StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
	}
}

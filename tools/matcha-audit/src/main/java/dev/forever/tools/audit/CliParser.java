package dev.forever.tools.audit;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Parses the intentionally small, dependency-free command-line contract. */
public final class CliParser {
    private CliParser() {
    }

    public static CliOptions parse(String[] args) throws AuditException {
        if (args == null) {
            throw usage("Arguments cannot be null.");
        }

        Path input = null;
        Path output = null;
        String versionLabel = null;
        List<String> positional = new ArrayList<>();

        for (int index = 0; index < args.length; index++) {
            String argument = args[index];
            if (argument == null || argument.isBlank()) {
                throw usage("An argument cannot be blank.");
            }
            switch (argument) {
                case "--input" -> input = Path.of(requireValue(args, ++index, "--input"));
                case "--output" -> output = Path.of(requireValue(args, ++index, "--output"));
                case "--version-label" -> versionLabel = requireValue(args, ++index, "--version-label");
                case "--help", "-h" -> throw usage(usageText());
                default -> {
                    if (argument.startsWith("-")) {
                        throw usage("Unknown option '" + argument + "'.\n\n" + usageText());
                    }
                    positional.add(argument);
                }
            }
        }

        if (!positional.isEmpty()) {
            throw usage("Unexpected positional argument '" + positional.getFirst() + "'.\n\n" + usageText());
        }
        if (input == null) {
            throw usage("Missing required --input.\n\n" + usageText());
        }
        if (output == null) {
            throw usage("Missing required --output.\n\n" + usageText());
        }
        if (versionLabel != null && versionLabel.isBlank()) {
            throw usage("--version-label cannot be blank.");
        }

        String resolvedLabel = versionLabel;
        if (resolvedLabel == null) {
            Path fileName = input.getFileName();
            resolvedLabel = fileName == null ? "unknown-input" : fileName.toString();
        }
        return new CliOptions(input, output, resolvedLabel);
    }

    public static String usageText() {
        return "Usage: matcha-audit --input <zip-or-directory> --output <output-directory> [--version-label <label>]";
    }

    private static String requireValue(String[] args, int index, String option) throws AuditException {
        if (index >= args.length || args[index] == null || args[index].isBlank() || args[index].startsWith("--")) {
            throw usage("Option " + option + " requires a value.\n\n" + usageText());
        }
        return args[index];
    }

    private static AuditException usage(String message) {
        return new AuditException(ExitCodes.USAGE, message);
    }
}

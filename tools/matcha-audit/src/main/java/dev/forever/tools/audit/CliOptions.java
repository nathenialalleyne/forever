package dev.forever.tools.audit;

import java.nio.file.Path;

/** Parsed command-line options for one audit invocation. */
public record CliOptions(Path input, Path output, String versionLabel) {
}

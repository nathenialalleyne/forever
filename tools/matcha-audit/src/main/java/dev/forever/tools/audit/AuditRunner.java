package dev.forever.tools.audit;

import java.nio.file.Path;

/** Executes one complete load, analyse, and report-write cycle. */
public final class AuditRunner {
    public void run(CliOptions options) throws AuditException {
        InputSnapshot snapshot = InputLoader.load(options.input());
        AuditReport report;
        try {
            report = AuditAnalyzer.analyse(snapshot);
        } catch (RuntimeException exception) {
            throw new AuditException(ExitCodes.ANALYSIS, "Input was read but analysis failed: " + exception.getMessage(), exception);
        }
        ReportWriter.write(options, report);
    }

    public void run(Path input, Path output, String versionLabel) throws AuditException {
        run(new CliOptions(input, output, versionLabel));
    }
}

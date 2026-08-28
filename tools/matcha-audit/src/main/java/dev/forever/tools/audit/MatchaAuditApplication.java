package dev.forever.tools.audit;

import java.io.PrintStream;
import java.nio.file.InvalidPathException;

/** Application entrypoint and user-facing process error boundary. */
public final class MatchaAuditApplication {
    private MatchaAuditApplication() {
    }

    public static void main(String[] args) {
        int exitCode = run(args, System.out, System.err);
        if (exitCode != ExitCodes.SUCCESS) {
            System.exit(exitCode);
        }
    }

    public static int run(String[] args, PrintStream output, PrintStream errors) {
        try {
            CliOptions options = CliParser.parse(args);
            new AuditRunner().run(options);
            if (output != null) {
                output.println("Audit completed: " + options.output().toAbsolutePath().normalize());
            }
            return ExitCodes.SUCCESS;
        } catch (AuditException exception) {
            if (errors != null) {
                errors.println("matcha-audit: " + exception.getMessage());
            }
            return exception.exitCode();
        } catch (InvalidPathException exception) {
            if (errors != null) {
                errors.println("matcha-audit: invalid path: " + exception.getInput());
            }
            return ExitCodes.USAGE;
        } catch (RuntimeException exception) {
            if (errors != null) {
                errors.println("matcha-audit: unexpected internal error: " + exception.getMessage());
            }
            return ExitCodes.INTERNAL;
        }
    }
}

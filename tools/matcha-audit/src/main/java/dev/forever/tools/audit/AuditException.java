package dev.forever.tools.audit;

/** A user-facing failure with a stable process exit code. */
public final class AuditException extends Exception {
    private final int exitCode;

    public AuditException(int exitCode, String message) {
        super(message);
        this.exitCode = exitCode;
    }

    public AuditException(int exitCode, String message, Throwable cause) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public int exitCode() {
        return exitCode;
    }
}

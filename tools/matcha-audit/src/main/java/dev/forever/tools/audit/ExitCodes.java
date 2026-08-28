package dev.forever.tools.audit;

/** Stable process exit codes for the standalone audit CLI. */
public final class ExitCodes {
    public static final int SUCCESS = 0;
    public static final int USAGE = 2;
    public static final int INPUT = 3;
    public static final int UNSAFE_ARCHIVE = 4;
    public static final int ANALYSIS = 5;
    public static final int OUTPUT = 6;
    public static final int INTERNAL = 10;

    private ExitCodes() {
    }
}

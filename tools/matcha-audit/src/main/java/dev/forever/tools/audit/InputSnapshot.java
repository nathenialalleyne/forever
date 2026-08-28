package dev.forever.tools.audit;

import java.nio.file.Path;
import java.util.List;

/** Read-only in-memory view of either a directory or ZIP input. */
public record InputSnapshot(InputKind kind, Path source, List<InputFile> files, String sha256) {
    public InputSnapshot {
        files = List.copyOf(files);
    }

    public enum InputKind {
        DIRECTORY,
        ZIP
    }
}

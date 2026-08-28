package dev.forever.tools.audit;

/** One regular file observed at a normalised path inside an input snapshot. */
public record InputFile(String path, byte[] content, String sha256) {
    public InputFile {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Input file path cannot be blank.");
        }
        if (content == null) {
            throw new IllegalArgumentException("Input file content cannot be null.");
        }
        if (sha256 == null || sha256.isBlank()) {
            throw new IllegalArgumentException("Input file SHA-256 cannot be blank.");
        }
    }

    public long size() {
        return content.length;
    }
}

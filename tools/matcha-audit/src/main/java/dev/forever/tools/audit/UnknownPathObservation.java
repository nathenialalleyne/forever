package dev.forever.tools.audit;

/** A file or directory path that needs human classification. */
public record UnknownPathObservation(String path, String kind, String side, String reason) {
}

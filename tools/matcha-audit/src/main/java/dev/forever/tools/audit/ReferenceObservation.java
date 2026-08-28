package dev.forever.tools.audit;

/** An observed edge candidate before matching it against observed files. */
public record ReferenceObservation(
        String sourceFile,
        String sourceKind,
        String sourceLocation,
        String relation,
        String target,
        String targetKind) {
}

package dev.forever.tools.audit;

/** A reference edge with a conservative resolution result derived from the inventory. */
public record ResolvedReference(
        ReferenceObservation observation,
        String status,
        String resolvedFile,
        String basis) {
}

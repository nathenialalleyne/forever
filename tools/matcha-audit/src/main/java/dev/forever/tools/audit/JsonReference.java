package dev.forever.tools.audit;

/** One namespaced string observed at a JSON path. */
public record JsonReference(String id, String jsonPath, String key) {
}

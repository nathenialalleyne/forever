package dev.forever.tools.assets;

/** One Forever resource-location reference found in a scoped JSON file. */
record ResourceReference(String sourcePath, String property, String resourcePath, ReferenceKind kind) {
}

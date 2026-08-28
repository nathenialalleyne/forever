package dev.forever.tools.audit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

/** Extracts pack metadata, filters, namespaces, and observed vanilla namespace files. */
public final class PackAnalyzer {
    private PackAnalyzer() {
    }

    public static void analyse(AuditReport report) {
        ClassifiedFile metadataFile = report.files().stream()
                .filter(file -> file.logicalPath().equals("pack.mcmeta"))
                .findFirst()
                .orElse(null);
        if (metadataFile == null) {
            report.setPackFilters("not-found", null);
            report.addWarning("No pack.mcmeta was observed at the detected pack root.");
        } else {
            JsonParseResult parsed = report.parseJson(metadataFile);
            if (parsed.parsed() && parsed.element().isJsonObject()) {
                JsonElement canonical = JsonSupport.canonical(parsed.element());
                report.setPackMetadata(canonical.getAsJsonObject());
                JsonObject metadata = parsed.element().getAsJsonObject();
                if (metadata.has("filter")) {
                    report.setPackFilters("observed", JsonSupport.canonical(metadata.get("filter")));
                } else {
                    report.setPackFilters("absent", null);
                }
                addPackMetadataReferences(report, parsed.element());
            } else if (parsed.parsed()) {
                report.addWarning("pack.mcmeta is valid JSON but its root is not an object.");
                report.setPackFilters("invalid-shape", null);
            } else {
                report.setPackFilters("invalid-json", null);
                report.addWarning("pack.mcmeta could not be parsed as JSON; the file remains in the inventory.");
            }
        }

        for (ClassifiedFile file : report.files()) {
            if (file.namespace().equals("minecraft")) {
                report.vanillaOverrides().add(new VanillaOverrideObservation(
                        file.side(), file.category(), vanillaId(file), file.logicalPath(),
                        "observed file is placed in the minecraft namespace"));
            }
        }
    }

    private static void addPackMetadataReferences(AuditReport report, JsonElement root) {
        for (JsonReference reference : JsonSupport.collectIdReferences(root)) {
            report.addReference(new ReferenceObservation("pack.mcmeta", "pack-metadata", reference.jsonPath(), "pack-metadata-reference", reference.id(), reference.id().startsWith("#") ? "tag-id" : "namespaced-id"));
        }
    }

    private static String vanillaId(ClassifiedFile file) {
        if (file.side().equals("data")) {
            String[] parts = file.logicalPath().split("/");
            if (parts.length >= 4) {
                String id = JsonSupport.idFor(file, parts[2]);
                if (!id.isBlank()) {
                    return id;
                }
            }
        }
        if (file.side().equals("resource")) {
            String[] parts = file.logicalPath().split("/");
            if (parts.length >= 3) {
                StringBuilder path = new StringBuilder();
                for (int index = 2; index < parts.length; index++) {
                    if (index > 2) {
                        path.append('/');
                    }
                    path.append(parts[index]);
                }
                return "minecraft:" + path;
            }
        }
        return file.logicalPath();
    }
}

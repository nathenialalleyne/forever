package dev.forever.tools.audit;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Mutable analysis result owned by one invocation and consumed by the report writer. */
public final class AuditReport {
    private final InputSnapshot snapshot;
    private final PackLayout layout;
    private final List<ClassifiedFile> files;
    private final Map<String, JsonParseResult> parsedJson = new LinkedHashMap<>();
    private final List<JsonObject> recipes = new ArrayList<>();
    private final List<JsonObject> advancements = new ArrayList<>();
    private final List<JsonObject> functions = new ArrayList<>();
    private final Map<String, JsonObject> scoreboards = new LinkedHashMap<>();
    private final List<JsonObject> tags = new ArrayList<>();
    private final List<JsonObject> lootTables = new ArrayList<>();
    private final List<JsonObject> predicates = new ArrayList<>();
    private final List<JsonObject> itemModifiers = new ArrayList<>();
    private final List<JsonObject> registryData = new ArrayList<>();
    private final List<JsonObject> worldgen = new ArrayList<>();
    private final List<JsonObject> assets = new ArrayList<>();
    private final List<ReferenceObservation> references = new ArrayList<>();
    private final Set<String> referenceKeys = new LinkedHashSet<>();
    private final List<ParseErrorObservation> parseErrors = new ArrayList<>();
    private final List<UnknownPathObservation> unknownPaths = new ArrayList<>();
    private final List<VanillaOverrideObservation> vanillaOverrides = new ArrayList<>();
    private final Set<String> dataNamespaces = new TreeSet<>();
    private final Set<String> resourceNamespaces = new TreeSet<>();
    private final Set<String> warnings = new TreeSet<>();
    private JsonObject packMetadata;
    private String packFiltersStatus = "not-found";
    private com.google.gson.JsonElement packFilters;

    public AuditReport(InputSnapshot snapshot, PackLayout layout, List<ClassifiedFile> files) {
        this.snapshot = snapshot;
        this.layout = layout;
        this.files = List.copyOf(files);
        for (ClassifiedFile file : files) {
            if (file.side().equals("data") && !file.namespace().isBlank()) {
                dataNamespaces.add(file.namespace());
            }
            if (file.side().equals("resource") && !file.namespace().isBlank()) {
                resourceNamespaces.add(file.namespace());
            }
        }
    }

    public InputSnapshot snapshot() {
        return snapshot;
    }

    public PackLayout layout() {
        return layout;
    }

    public List<ClassifiedFile> files() {
        return files;
    }

    public JsonParseResult parseJson(ClassifiedFile file) {
        return parsedJson.computeIfAbsent(file.logicalPath(), ignored -> {
            JsonParseResult result = JsonSupport.parse(file.input());
            if (!result.parsed() && result.status().equals("invalid-json")) {
                parseErrors.add(new ParseErrorObservation(file.logicalPath(), result.error()));
            }
            return result;
        });
    }

    public JsonParseResult parsedJson(String logicalPath) {
        return parsedJson.get(logicalPath);
    }

    public List<JsonObject> recipes() {
        return recipes;
    }

    public List<JsonObject> advancements() {
        return advancements;
    }

    public List<JsonObject> functions() {
        return functions;
    }

    public Map<String, JsonObject> scoreboards() {
        return scoreboards;
    }

    public List<JsonObject> tags() {
        return tags;
    }

    public List<JsonObject> lootTables() {
        return lootTables;
    }

    public List<JsonObject> predicates() {
        return predicates;
    }

    public List<JsonObject> itemModifiers() {
        return itemModifiers;
    }

    public List<JsonObject> registryData() {
        return registryData;
    }

    public List<JsonObject> worldgen() {
        return worldgen;
    }

    public List<JsonObject> assets() {
        return assets;
    }

    public List<ReferenceObservation> references() {
        return references;
    }

    public List<ParseErrorObservation> parseErrors() {
        return parseErrors;
    }

    public List<UnknownPathObservation> unknownPaths() {
        return unknownPaths;
    }

    public List<VanillaOverrideObservation> vanillaOverrides() {
        return vanillaOverrides;
    }

    public Set<String> dataNamespaces() {
        return dataNamespaces;
    }

    public Set<String> resourceNamespaces() {
        return resourceNamespaces;
    }

    public Set<String> warnings() {
        return warnings;
    }

    public JsonObject packMetadata() {
        return packMetadata;
    }

    public void setPackMetadata(JsonObject packMetadata) {
        this.packMetadata = packMetadata;
    }

    public String packFiltersStatus() {
        return packFiltersStatus;
    }

    public com.google.gson.JsonElement packFilters() {
        return packFilters;
    }

    public void setPackFilters(String status, com.google.gson.JsonElement packFilters) {
        this.packFiltersStatus = status;
        this.packFilters = packFilters;
    }

    public void addReference(ReferenceObservation reference) {
        String key = String.join("\u0000", reference.sourceFile(), reference.sourceLocation(), reference.relation(), reference.target(), reference.targetKind());
        if (referenceKeys.add(key)) {
            references.add(reference);
        }
    }

    public void addUnknownPath(UnknownPathObservation path) {
        unknownPaths.add(path);
    }

    public void addWarning(String warning) {
        if (warning != null && !warning.isBlank()) {
            warnings.add(warning);
        }
    }

    public JsonObject scoreboard(String objective) {
        return scoreboards.computeIfAbsent(objective, name -> {
            JsonObject result = new JsonObject();
            result.addProperty("observationType", "observed");
            result.addProperty("objective", name);
            result.add("definitions", new com.google.gson.JsonArray());
            result.add("references", new com.google.gson.JsonArray());
            return result;
        });
    }

    public static void addObserved(JsonObject object) {
        object.addProperty("observationType", "observed");
    }
}

package dev.forever.tools.audit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Analyses JSON data without pretending to understand gameplay semantics. */
public final class JsonAnalyzer {
    private static final Set<String> GENERIC_REGISTRY_CATEGORIES = Set.of(
            "registry-data", "dimension", "dimension-type", "equipment", "structures");

    private JsonAnalyzer() {
    }

    public static void analyse(AuditReport report) {
        for (ClassifiedFile file : report.files()) {
            if (file.jsonCandidate()) {
                report.parseJson(file);
            }
        }
        for (ClassifiedFile file : report.files()) {
            switch (file.category()) {
                case "recipes" -> analyseRecipe(report, file);
                case "advancements" -> analyseAdvancement(report, file);
                case "tags" -> analyseTag(report, file);
                case "loot-tables" -> analyseLootTable(report, file);
                case "predicates" -> analyseSimpleData(report, file, report.predicates(), "predicate");
                case "item-modifiers" -> analyseSimpleData(report, file, report.itemModifiers(), "item-modifier");
                case "registry-data", "dimension", "dimension-type" ->
                        analyseRegistryData(report, file);
                case "structures" -> {
                    analyseRegistryData(report, file);
                    analyseStructureWorldgen(report, file);
                }
                case "equipment" -> {
                    if (file.side().equals("data")) {
                        analyseRegistryData(report, file);
                    } else {
                        analyseAsset(report, file);
                    }
                }
                case "worldgen" -> analyseWorldgen(report, file);
                case "models", "item-definitions", "language", "sounds", "atlases", "fonts", "textures", "blockstates", "particles", "shaders", "post_effect", "colormap", "gui" ->
                        analyseAsset(report, file);
                default -> {
                    // The file remains in file-inventory.csv and unknown-paths.csv where applicable.
                }
            }
        }
    }

    private static void analyseRecipe(AuditReport report, ClassifiedFile file) {
        JsonObject record = baseRecord(file, dataId(file, "recipes", "recipe"), "recipe");
        JsonParseResult parsed = report.parseJson(file);
        addParseStatus(record, parsed);
        if (parsed.parsed()) {
            JsonElement root = parsed.element();
            addPropertyIfString(record, root, "type");
            List<JsonReference> resultRefs = referencesUnder(root, "result");
            List<JsonReference> ingredientRefs = referencesUnder(root, "ingredients", "ingredient", "base", "addition", "template");
            addStringList(record, "resultIds", resultRefs.stream().map(JsonReference::id).toList());
            addStringList(record, "ingredientIds", ingredientRefs.stream().map(JsonReference::id).toList());
            addJsonReferences(record, report, file, "recipe", JsonSupport.collectIdReferences(root));
            addSpecificReferences(report, file, "recipe-result", resultRefs);
            addSpecificReferences(report, file, "recipe-ingredient", ingredientRefs);
        }
        report.recipes().add(record);
    }

    private static void analyseAdvancement(AuditReport report, ClassifiedFile file) {
        JsonObject record = baseRecord(file, dataId(file, "advancements", "advancement"), "advancement");
        JsonParseResult parsed = report.parseJson(file);
        addParseStatus(record, parsed);
        if (parsed.parsed()) {
            JsonElement root = parsed.element();
            String parent = stringMember(root, "parent");
            if (!parent.isBlank()) {
                record.addProperty("parent", parent);
                addSpecificReferences(report, file, "advancement-parent", List.of(new JsonReference(parent, "$.parent", "parent")));
            }
            JsonElement rewards = objectMember(root, "rewards");
            List<JsonReference> functions = referencesUnder(rewards, "function");
            List<JsonReference> recipes = referencesUnder(rewards, "recipes");
            List<JsonReference> loot = referencesUnder(rewards, "loot");
            addStringList(record, "rewardFunctions", functions.stream().map(JsonReference::id).toList());
            addStringList(record, "rewardRecipes", recipes.stream().map(JsonReference::id).toList());
            addStringList(record, "rewardLoot", loot.stream().map(JsonReference::id).toList());
            addJsonReferences(record, report, file, "advancement", JsonSupport.collectIdReferences(root));
            addSpecificReferences(report, file, "advancement-reward-function", functions);
            addSpecificReferences(report, file, "advancement-reward-recipe", recipes);
            addSpecificReferences(report, file, "advancement-reward-loot", loot);
        }
        report.advancements().add(record);
    }

    private static void analyseTag(AuditReport report, ClassifiedFile file) {
        JsonObject record = baseRecord(file, dataId(file, "tags"), "tag");
        JsonParseResult parsed = report.parseJson(file);
        addParseStatus(record, parsed);
        if (parsed.parsed()) {
            JsonElement root = parsed.element();
            addPropertyIfBoolean(record, root, "replace");
            List<JsonReference> values = referencesUnder(root, "values");
            JsonArray memberships = new JsonArray();
            for (JsonReference value : values) {
                JsonObject membership = new JsonObject();
                membership.addProperty("observationType", "observed");
                membership.addProperty("value", value.id());
                membership.addProperty("jsonPath", value.jsonPath());
                membership.addProperty("required", !value.id().startsWith("#"));
                memberships.add(membership);
                report.addReference(new ReferenceObservation(file.logicalPath(), "tags", value.jsonPath(), "tag-membership", value.id(), value.id().startsWith("#") ? "tag-id" : "namespaced-id"));
            }
            record.add("memberships", memberships);
            addJsonReferences(record, report, file, "tag", JsonSupport.collectIdReferences(root));
        }
        report.tags().add(record);
    }

    private static void analyseLootTable(AuditReport report, ClassifiedFile file) {
        JsonObject record = baseRecord(file, dataId(file, "loot_tables", "loot_table"), "loot-table");
        JsonParseResult parsed = report.parseJson(file);
        addParseStatus(record, parsed);
        if (parsed.parsed()) {
            JsonElement root = parsed.element();
            addPropertyIfString(record, root, "type");
            List<JsonReference> tables = new ArrayList<>(referencesUnder(root, "table", "random_sequence"));
            tables.addAll(collectTypedLootTableNames(root, "$"));
            tables = distinctReferences(tables);
            addStringList(record, "tableReferences", tables.stream().map(JsonReference::id).toList());
            addJsonReferences(record, report, file, "loot-table", JsonSupport.collectIdReferences(root));
            addSpecificReferences(report, file, "loot-table-reference", tables);
        }
        report.lootTables().add(record);
    }

    private static void analyseSimpleData(AuditReport report, ClassifiedFile file, List<JsonObject> destination, String kind) {
        String[] roots = file.category().equals("item-modifiers") ? new String[]{"item_modifiers", "item_modifier"} : new String[]{"predicates", "predicate"};
        JsonObject record = baseRecord(file, dataId(file, roots), kind);
        JsonParseResult parsed = report.parseJson(file);
        addParseStatus(record, parsed);
        if (parsed.parsed()) {
            addJsonReferences(record, report, file, kind, JsonSupport.collectIdReferences(parsed.element()));
        }
        destination.add(record);
    }

    private static void analyseRegistryData(AuditReport report, ClassifiedFile file) {
        JsonObject record = baseRecord(file, dataIdForRegistry(file), "registry-data");
        record.addProperty("registryType", file.category());
        JsonParseResult parsed = report.parseJson(file);
        addParseStatus(record, parsed);
        if (parsed.parsed()) {
            addJsonReferences(record, report, file, "registry-reference", JsonSupport.collectIdReferences(parsed.element()));
            addStringList(record, "observedTopLevelKeys", topLevelKeys(parsed.element()));
        }
        report.registryData().add(record);
    }

    private static void analyseStructureWorldgen(AuditReport report, ClassifiedFile file) {
        JsonObject record = baseRecord(file, dataId(file, "structures", "structure"), "structure-data");
        record.addProperty("worldgenType", "structures");
        JsonParseResult parsed = report.parseJson(file);
        addParseStatus(record, parsed);
        if (parsed.parsed()) {
            addJsonReferences(record, report, file, "structure-reference", JsonSupport.collectIdReferences(parsed.element()));
        }
        report.worldgen().add(record);
    }

    private static void analyseWorldgen(AuditReport report, ClassifiedFile file) {
        JsonObject record = baseRecord(file, dataIdForWorldgen(file), "worldgen");
        String[] parts = file.logicalPath().split("/");
        record.addProperty("worldgenType", parts.length > 3 ? parts[3] : "unknown");
        JsonParseResult parsed = report.parseJson(file);
        addParseStatus(record, parsed);
        if (parsed.parsed()) {
            addJsonReferences(record, report, file, "worldgen-reference", JsonSupport.collectIdReferences(parsed.element()));
        }
        report.worldgen().add(record);
    }

    private static void analyseAsset(AuditReport report, ClassifiedFile file) {
        JsonObject record = baseRecord(file, assetId(file), "asset");
        record.addProperty("assetType", file.category());
        record.addProperty("resourcePath", resourcePath(file));
        JsonParseResult parsed = report.parseJson(file);
        addParseStatus(record, parsed);
        if (parsed.parsed()) {
            addJsonReferences(record, report, file, "asset-reference", JsonSupport.collectIdReferences(parsed.element()));
            if (file.category().equals("language")) {
                record.addProperty("translationKeyCount", topLevelObjectSize(parsed.element()));
                addStringList(record, "translationKeys", topLevelKeys(parsed.element()));
            }
            if (file.category().equals("models")) {
                addPropertyIfString(record, parsed.element(), "parent");
                List<JsonReference> textures = referencesUnder(parsed.element(), "textures");
                addStringList(record, "textureReferences", textures.stream().map(JsonReference::id).toList());
            }
            if (file.category().equals("sounds")) {
                addStringList(record, "soundKeys", topLevelKeys(parsed.element()));
            }
            if (file.category().equals("atlases")) {
                addStringList(record, "atlasSources", JsonSupport.collectIdReferences(parsed.element()).stream().map(JsonReference::id).toList());
            }
            if (file.category().equals("fonts")) {
                addStringList(record, "fontProviders", topLevelKeys(parsed.element()));
            }
        }
        report.assets().add(record);
    }

    private static List<JsonReference> collectTypedLootTableNames(JsonElement element, String path) {
        List<JsonReference> result = new ArrayList<>();
        if (element == null || element.isJsonNull()) {
            return result;
        }
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            JsonElement type = object.get("type");
            JsonElement name = object.get("name");
            if (type != null && type.isJsonPrimitive() && type.getAsJsonPrimitive().isString()
                    && type.getAsString().equals("minecraft:loot_table")
                    && name != null && name.isJsonPrimitive() && name.getAsJsonPrimitive().isString()
                    && JsonSupport.isNamespacedId(name.getAsString())) {
                result.add(new JsonReference(name.getAsString(), path + ".name", "name"));
            }
            for (var entry : object.entrySet()) {
                result.addAll(collectTypedLootTableNames(entry.getValue(), path + "." + entry.getKey()));
            }
        } else if (element.isJsonArray()) {
            int index = 0;
            for (JsonElement child : element.getAsJsonArray()) {
                result.addAll(collectTypedLootTableNames(child, path + "[" + index + "]"));
                index++;
            }
        }
        return result;
    }

    private static List<JsonReference> distinctReferences(List<JsonReference> references) {
        return references.stream().collect(java.util.stream.Collectors.toMap(
                        reference -> reference.id() + "\\u0000" + reference.jsonPath(),
                        reference -> reference,
                        (first, ignored) -> first,
                        java.util.LinkedHashMap::new))
                .values().stream().toList();
    }

    private static JsonObject baseRecord(ClassifiedFile file, String id, String kind) {
        JsonObject record = new JsonObject();
        record.addProperty("observationType", "observed");
        record.addProperty("kind", kind);
        record.addProperty("id", id);
        record.addProperty("file", file.logicalPath());
        record.addProperty("namespace", file.namespace());
        return record;
    }

    private static void addParseStatus(JsonObject record, JsonParseResult parsed) {
        record.addProperty("parseStatus", parsed.status());
        if (!parsed.error().isBlank()) {
            record.addProperty("parseError", parsed.error());
        }
    }

    private static void addJsonReferences(JsonObject record, AuditReport report, ClassifiedFile file, String sourceKind, List<JsonReference> references) {
        JsonArray output = new JsonArray();
        for (JsonReference reference : references.stream().sorted(Comparator.comparing(JsonReference::jsonPath).thenComparing(JsonReference::id)).toList()) {
            JsonObject observed = new JsonObject();
            observed.addProperty("observationType", "observed");
            observed.addProperty("id", reference.id());
            observed.addProperty("jsonPath", reference.jsonPath());
            observed.addProperty("key", reference.key());
            output.add(observed);
            report.addReference(new ReferenceObservation(file.logicalPath(), sourceKind, reference.jsonPath(), relationForKey(reference.key()), reference.id(), reference.id().startsWith("#") ? "tag-id" : "namespaced-id"));
        }
        record.add("references", output);
    }

    private static void addSpecificReferences(AuditReport report, ClassifiedFile file, String relation, List<JsonReference> references) {
        for (JsonReference reference : references) {
            report.addReference(new ReferenceObservation(file.logicalPath(), file.category(), reference.jsonPath(), relation, reference.id(), reference.id().startsWith("#") ? "tag-id" : "namespaced-id"));
        }
    }

    private static List<JsonReference> referencesUnder(JsonElement root, String... keys) {
        if (root == null || root.isJsonNull()) {
            return List.of();
        }
        return JsonSupport.collectReferencesForKeys(root, Set.of(keys));
    }

    private static JsonElement objectMember(JsonElement root, String key) {
        return root != null && root.isJsonObject() ? root.getAsJsonObject().get(key) : null;
    }

    private static String stringMember(JsonElement root, String key) {
        JsonElement value = objectMember(root, key);
        return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString() ? value.getAsString() : "";
    }

    private static void addPropertyIfString(JsonObject destination, JsonElement root, String key) {
        String value = stringMember(root, key);
        if (!value.isBlank()) {
            destination.addProperty(key, value);
        }
    }

    private static void addPropertyIfBoolean(JsonObject destination, JsonElement root, String key) {
        JsonElement value = objectMember(root, key);
        if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean()) {
            destination.addProperty(key, value.getAsBoolean());
        }
    }

    private static void addStringList(JsonObject destination, String key, List<String> values) {
        destination.add(key, JsonSupport.strings(JsonSupport.uniqueSortedStrings(values)));
    }

    private static List<String> topLevelKeys(JsonElement root) {
        if (root == null || !root.isJsonObject()) {
            return List.of();
        }
        return root.getAsJsonObject().keySet().stream().sorted().toList();
    }

    private static int topLevelObjectSize(JsonElement root) {
        return root != null && root.isJsonObject() ? root.getAsJsonObject().size() : 0;
    }

    private static String dataIdForRegistry(ClassifiedFile file) {
        String[] parts = file.logicalPath().split("/");
        String root = parts.length > 2 ? parts[2] : file.category();
        String id = JsonSupport.idFor(file, root);
        return id.isBlank() ? fallbackId(file) : id;
    }

    private static String dataId(ClassifiedFile file, String... roots) {
        for (String root : roots) {
            String id = JsonSupport.idFor(file, root);
            if (!id.isBlank()) {
                return id;
            }
        }
        return fallbackId(file);
    }

    private static String dataIdForWorldgen(ClassifiedFile file) {
        String id = JsonSupport.idFor(file, "worldgen");
        return id.isBlank() ? fallbackId(file) : id;
    }

    private static String fallbackId(ClassifiedFile file) {
        return file.namespace().isBlank() ? "" : file.namespace() + ":" + file.logicalPath();
    }

    private static String assetId(ClassifiedFile file) {
        String path = resourcePath(file);
        int extension = path.lastIndexOf('.');
        String idPath = extension > path.lastIndexOf('/') ? path.substring(0, extension) : path;
        return file.namespace().isBlank() ? idPath : file.namespace() + ":" + idPath;
    }

    private static String resourcePath(ClassifiedFile file) {
        String[] parts = file.logicalPath().split("/");
        if (parts.length < 3) {
            return file.logicalPath();
        }
        StringBuilder result = new StringBuilder();
        for (int index = 2; index < parts.length; index++) {
            if (index > 2) {
                result.append('/');
            }
            result.append(parts[index]);
        }
        return result.toString();
    }

    private static String relationForKey(String key) {
        return switch (key) {
            case "parent" -> "advancement-parent-or-model-parent";
            case "function" -> "function-reference";
            case "recipes" -> "recipe-reference";
            case "loot" -> "loot-reference";
            case "table", "random_sequence" -> "loot-table-reference";
            case "ingredients", "ingredient", "base", "addition", "template" -> "ingredient-reference";
            case "model", "texture", "textures" -> "asset-reference";
            case "tag" -> "tag-reference";
            default -> "json-id-reference";
        };
    }
}

package dev.forever.tools.audit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/** Gson utilities for canonical reports and conservative Minecraft ID extraction. */
public final class JsonSupport {
    private static final Pattern NAMESPACED_ID = Pattern.compile("#?[a-z0-9_.-]+:[a-z0-9_./-]+", Pattern.CASE_INSENSITIVE);
    private static final Gson REPORT_GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private JsonSupport() {
    }

    public static JsonParseResult parse(InputFile file) {
        String lowerPath = file.path().toLowerCase(java.util.Locale.ROOT);
        if (!lowerPath.endsWith(".json") && !lowerPath.endsWith("pack.mcmeta")) {
            return new JsonParseResult(null, "not-applicable", "");
        }
        String text = new String(file.content(), StandardCharsets.UTF_8);
        if (!text.isEmpty() && text.charAt(0) == '\ufeff') {
            text = text.substring(1);
        }
        try {
            JsonElement element = JsonParser.parseString(text);
            return new JsonParseResult(element, "parsed", "");
        } catch (JsonParseException | IllegalStateException exception) {
            return new JsonParseResult(null, "invalid-json", stableError(exception));
        }
    }

    public static List<JsonReference> collectIdReferences(JsonElement element) {
        List<JsonReference> references = new ArrayList<>();
        collectIdReferences(element, "$", "", references);
        references.sort(Comparator.comparing(JsonReference::id)
                .thenComparing(JsonReference::jsonPath)
                .thenComparing(JsonReference::key));
        return references;
    }

    public static List<JsonReference> collectReferencesForKeys(JsonElement element, Set<String> keys) {
        List<JsonReference> references = new ArrayList<>();
        collectReferencesForKeys(element, "$", keys, references);
        references.sort(Comparator.comparing(JsonReference::id)
                .thenComparing(JsonReference::jsonPath)
                .thenComparing(JsonReference::key));
        return references;
    }

    public static JsonElement canonical(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return JsonNull.INSTANCE;
        }
        if (element.isJsonObject()) {
            JsonObject result = new JsonObject();
            element.getAsJsonObject().entrySet().stream()
                    .sorted(Comparator.comparing(java.util.Map.Entry::getKey))
                    .forEach(entry -> result.add(entry.getKey(), canonical(entry.getValue())));
            return result;
        }
        if (element.isJsonArray()) {
            JsonArray result = new JsonArray();
            for (JsonElement child : element.getAsJsonArray()) {
                result.add(canonical(child));
            }
            return result;
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isBoolean()) {
            return new JsonPrimitive(primitive.getAsBoolean());
        }
        if (primitive.isNumber()) {
            return new JsonPrimitive(primitive.getAsNumber());
        }
        return new JsonPrimitive(primitive.getAsString());
    }

    public static String pretty(JsonElement element) {
        return REPORT_GSON.toJson(canonical(element)) + "\n";
    }

    public static JsonObject object() {
        return new JsonObject();
    }

    public static JsonArray array(Iterable<? extends JsonElement> elements) {
        JsonArray result = new JsonArray();
        for (JsonElement element : elements) {
            result.add(element);
        }
        return result;
    }

    public static JsonArray strings(Iterable<String> values) {
        JsonArray result = new JsonArray();
        for (String value : values) {
            result.add(value);
        }
        return result;
    }

    public static boolean isNamespacedId(String value) {
        return value != null && NAMESPACED_ID.matcher(value).matches();
    }

    public static String withoutTagPrefix(String value) {
        return value != null && value.startsWith("#") ? value.substring(1) : value;
    }

    public static List<String> uniqueSortedStrings(Iterable<String> values) {
        Set<String> unique = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                unique.add(value);
            }
        }
        return unique.stream().sorted().toList();
    }

    public static String idFor(ClassifiedFile file, String rootDirectory) {
        String[] parts = file.logicalPath().split("/");
        if (parts.length < 4 || !parts[0].equals("data") || !parts[2].equals(rootDirectory)) {
            return "";
        }
        StringBuilder path = new StringBuilder();
        for (int index = 3; index < parts.length; index++) {
            if (index > 3) {
                path.append('/');
            }
            path.append(parts[index]);
        }
        String value = path.toString();
        if (value.endsWith(".json")) {
            value = value.substring(0, value.length() - 5);
        } else if (value.endsWith(".mcfunction")) {
            value = value.substring(0, value.length() - 11);
        } else if (value.endsWith(".nbt")) {
            value = value.substring(0, value.length() - 4);
        }
        return file.namespace().isBlank() || value.isBlank() ? "" : file.namespace() + ":" + value;
    }

    private static void collectIdReferences(JsonElement element, String path, String key, List<JsonReference> result) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isString()) {
                String value = element.getAsString();
                if (isNamespacedId(value)) {
                    result.add(new JsonReference(value, path, key));
                }
            }
            return;
        }
        if (element.isJsonObject()) {
            for (var entry : element.getAsJsonObject().entrySet()) {
                collectIdReferences(entry.getValue(), path + "." + entry.getKey(), entry.getKey(), result);
            }
            return;
        }
        int index = 0;
        for (JsonElement child : element.getAsJsonArray()) {
            collectIdReferences(child, path + "[" + index + "]", key, result);
            index++;
        }
    }

    private static void collectReferencesForKeys(JsonElement element, String path, Set<String> keys, List<JsonReference> result) {
        if (element == null || element.isJsonNull()) {
            return;
        }
        if (element.isJsonObject()) {
            for (var entry : element.getAsJsonObject().entrySet()) {
                String childPath = path + "." + entry.getKey();
                if (keys.contains(entry.getKey())) {
                    collectIdReferences(entry.getValue(), childPath, entry.getKey(), result);
                }
                collectReferencesForKeys(entry.getValue(), childPath, keys, result);
            }
        } else if (element.isJsonArray()) {
            int index = 0;
            for (JsonElement child : element.getAsJsonArray()) {
                collectReferencesForKeys(child, path + "[" + index + "]", keys, result);
                index++;
            }
        }
    }

    private static String stableError(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? exception.getClass().getSimpleName() : message.replaceAll("\\s+", " ").trim();
    }
}

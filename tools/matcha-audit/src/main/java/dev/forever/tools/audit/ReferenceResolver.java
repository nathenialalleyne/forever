package dev.forever.tools.audit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Resolves only against observed files and leaves vanilla/external references explicit. */
public final class ReferenceResolver {
    private ReferenceResolver() {
    }

    public static List<ResolvedReference> resolve(AuditReport report) {
        FileIndex index = FileIndex.build(report);
        List<ResolvedReference> resolved = new ArrayList<>();
        for (ReferenceObservation observation : report.references()) {
            String target = observation.target();
            if (observation.targetKind().equals("scoreboard-objective")) {
                var scoreboard = report.scoreboards().get(target);
                boolean hasDefinition = scoreboard != null && scoreboard.getAsJsonArray("definitions").size() > 0;
                resolved.add(new ResolvedReference(observation, hasDefinition ? "resolved" : "unresolved", "", hasDefinition ? "exact-observed-scoreboard-definition" : "no-observed-scoreboard-definition"));
                continue;
            }
            if (observation.targetKind().equals("file-path")) {
                String normalised = target.replace('\\', '/');
                String file = index.byPath.get(normalised);
                resolved.add(new ResolvedReference(observation, file == null ? "unresolved" : "resolved", file == null ? "" : file, file == null ? "no-observed-file-at-path" : "exact-observed-file-path"));
                continue;
            }

            String bare = JsonSupport.withoutTagPrefix(target);
            String namespace = namespaceOf(bare);
            String expectedCategory = expectedCategory(observation);
            String file = index.find(bare, expectedCategory);
            if (file != null) {
                resolved.add(new ResolvedReference(observation, "resolved", file, "exact-observed-file-id"));
            } else if (namespace.equals("minecraft")) {
                resolved.add(new ResolvedReference(observation, "external-vanilla", "", "minecraft-namespace-not-in-observed-pack"));
            } else if (report.dataNamespaces().contains(namespace) || report.resourceNamespaces().contains(namespace)) {
                resolved.add(new ResolvedReference(observation, "unresolved", "", "observed-namespace-without-matching-observed-file"));
            } else {
                resolved.add(new ResolvedReference(observation, "external-namespace", "", "namespace-not-observed-in-pack"));
            }
        }
        resolved.sort(Comparator.comparing((ResolvedReference value) -> value.observation().sourceFile())
                .thenComparing(value -> value.observation().sourceLocation())
                .thenComparing(value -> value.observation().relation())
                .thenComparing(value -> value.observation().target()));
        return resolved;
    }

    private static String expectedCategory(ReferenceObservation observation) {
        return switch (observation.targetKind()) {
            case "function-id" -> "functions";
            case "recipe-id" -> "recipes";
            case "advancement-id" -> "advancements";
            case "loot-table-id" -> "loot-tables";
            case "item-modifier-id" -> "item-modifiers";
            case "tag-id" -> "tags";
            default -> switch (observation.relation()) {
                case "advancement-parent" -> "advancements";
                case "function-reference", "scheduled-function" -> "functions";
                case "recipe-reference", "recipe-result", "recipe-ingredient" -> "recipes";
                case "loot-reference", "loot-table-reference" -> "loot-tables";
                case "tag-reference", "tag-membership" -> "tags";
                default -> "";
            };
        };
    }

    private static String namespaceOf(String id) {
        int separator = id.indexOf(':');
        return separator > 0 ? id.substring(0, separator) : "";
    }

    private static final class FileIndex {
        private final Map<String, String> byPath = new HashMap<>();
        private final Map<String, List<IndexedId>> byId = new HashMap<>();

        private static FileIndex build(AuditReport report) {
            FileIndex result = new FileIndex();
            for (ClassifiedFile file : report.files()) {
                result.byPath.put(file.logicalPath(), file.logicalPath());
                if (file.side().equals("data")) {
                    String physicalRoot = physicalRoot(file);
                    String id = JsonSupport.idFor(file, physicalRoot);
                    if (!id.isBlank()) {
                        result.add(id, file.category(), file.logicalPath());
                    }
                    if (file.category().equals("dimension-type")) {
                        String dimensionTypeId = JsonSupport.idFor(file, "dimension_type");
                        if (!dimensionTypeId.isBlank()) {
                            result.add(dimensionTypeId, file.category(), file.logicalPath());
                        }
                    }
                } else if (file.side().equals("resource")) {
                    String resourcePath = resourcePath(file);
                    String id = file.namespace().isBlank() ? resourcePath : file.namespace() + ":" + removeJsonExtension(resourcePath);
                    result.add(id, "assets", file.logicalPath());
                    if (file.category().equals("models") || file.category().equals("item-definitions")) {
                        String shortId = shortAssetId(file);
                        result.add(shortId, "assets", file.logicalPath());
                    }
                    if (file.category().equals("textures")) {
                        result.add(file.namespace() + ":" + removeLastExtension(resourcePath), "assets", file.logicalPath());
                    }
                }
            }
            return result;
        }

        private void add(String id, String category, String file) {
            byId.computeIfAbsent(id, ignored -> new ArrayList<>()).add(new IndexedId(category, file));
        }

        private String find(String id, String expectedCategory) {
            List<IndexedId> candidates = byId.get(id);
            if (candidates == null) {
                return null;
            }
            return candidates.stream()
                    .filter(candidate -> expectedCategory.isBlank() || candidate.category().equals(expectedCategory) || expectedCategory.equals("assets") && candidate.category().equals("resource"))
                    .map(IndexedId::file)
                    .sorted()
                    .findFirst()
                    .orElse(null);
        }

        private record IndexedId(String category, String file) {
        }
    }

    private static String physicalRoot(ClassifiedFile file) {
        String[] parts = file.logicalPath().split("/");
        return parts.length > 2 ? parts[2] : file.category();
    }

    private static String resourcePath(ClassifiedFile file) {
        String[] parts = file.logicalPath().split("/");
        if (parts.length < 3) {
            return file.logicalPath();
        }
        return String.join("/", java.util.Arrays.copyOfRange(parts, 2, parts.length));
    }

    private static String shortAssetId(ClassifiedFile file) {
        String resource = resourcePath(file);
        String prefix = file.category().equals("models") ? "models/" : "items/";
        if (resource.startsWith(prefix)) {
            return file.namespace() + ":" + removeJsonExtension(resource.substring(prefix.length()));
        }
        return file.namespace() + ":" + removeJsonExtension(resource);
    }

    private static String removeJsonExtension(String value) {
        return value.endsWith(".json") ? value.substring(0, value.length() - 5) : value;
    }

    private static String removeLastExtension(String value) {
        int dot = value.lastIndexOf('.');
        return dot > 0 ? value.substring(0, dot) : value;
    }
}

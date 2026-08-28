package dev.forever.tools.audit;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Maps paths to conservative, documented resource/data categories. */
public final class PathClassifier {
    private static final Set<String> REGISTRY_DIRECTORIES = Set.of(
            "banner_pattern", "banner_patterns", "cat_variant", "cat_variants", "chat_type",
            "chicken_variant", "chicken_variants", "cow_variant", "cow_variants", "damage_type",
            "dialog", "enchantment", "enchantments", "instrument", "jukebox_song", "painting_variant",
            "painting_variants", "point_of_interest_type", "test_environment", "test_instance",
            "trial_spawner", "trim_material", "trim_pattern", "villager_trade", "villager_trades",
            "wolf_variant");

    private static final Set<String> RESOURCE_DIRECTORIES = Set.of(
            "blockstates", "colormap", "equipment", "font", "fonts", "gui", "items", "lang", "models",
            "particles", "post_effect", "shaders", "sounds", "textures", "atlases", "item", "block");

    private PathClassifier() {
    }

    public static ClassifiedFile classify(InputFile input, PackLayout layout) {
        String logicalPath = layout.logicalPath(input.path());
        String[] parts = logicalPath.split("/");
        if (logicalPath.equals("pack.mcmeta")) {
            return new ClassifiedFile(input, logicalPath, "pack", "", "pack-metadata", isJson(input), "not-analysed", "");
        }
        if (logicalPath.equals("pack.png")) {
            return new ClassifiedFile(input, logicalPath, "pack", "", "pack-icon", false, "not-applicable", "");
        }
        if (parts.length >= 3 && parts[0].equals("data")) {
            String namespace = parts[1];
            String category = dataCategory(parts[2], parts, input.path());
            return new ClassifiedFile(input, logicalPath, "data", namespace, category, isJson(input), "not-analysed", "");
        }
        if (parts.length >= 3 && parts[0].equals("assets")) {
            String namespace = parts[1];
            String category = resourceCategory(parts[2], parts, input.path());
            return new ClassifiedFile(input, logicalPath, "resource", namespace, category, isJson(input), "not-analysed", "");
        }
        String side = parts.length > 0 && parts[0].equals("data") ? "data"
                : parts.length > 0 && parts[0].equals("assets") ? "resource" : "unknown";
        return new ClassifiedFile(input, logicalPath, side, "", "unknown", isJson(input), "not-analysed", "");
    }

    private static String dataCategory(String root, String[] parts, String path) {
        return switch (root) {
            case "advancements", "advancement" -> hasExtension(path, ".json") ? "advancements" : "unknown";
            case "functions", "function" -> hasExtension(path, ".mcfunction") ? "functions" : "unknown";
            case "recipes", "recipe" -> hasExtension(path, ".json") ? "recipes" : "unknown";
            case "predicates", "predicate" -> hasExtension(path, ".json") ? "predicates" : "unknown";
            case "loot_tables", "loot_table" -> hasExtension(path, ".json") ? "loot-tables" : "unknown";
            case "item_modifiers", "item_modifier" -> hasExtension(path, ".json") ? "item-modifiers" : "unknown";
            case "tags" -> parts.length >= 4 && hasExtension(path, ".json") ? "tags" : "unknown";
            case "structures", "structure" -> "structures";
            case "worldgen" -> "worldgen";
            case "dimension" -> "dimension";
            case "dimension_type" -> "dimension-type";
            case "equipment" -> "equipment";
            case "enchantments", "enchantment" -> "registry-data";
            default -> REGISTRY_DIRECTORIES.contains(root) && hasExtension(path, ".json") ? "registry-data" : "unknown";
        };
    }

    private static String resourceCategory(String root, String[] parts, String path) {
        if (root.equals("sounds.json") && parts.length == 3) {
            return "sounds";
        }
        if (root.equals("sounds") && parts.length == 3 && path.toLowerCase(Locale.ROOT).endsWith("sounds.json")) {
            return "sounds";
        }
        if (root.equals("lang") && hasExtension(path, ".json")) {
            return "language";
        }
        if (root.equals("models") && hasExtension(path, ".json")) {
            return "models";
        }
        if (root.equals("items") && hasExtension(path, ".json")) {
            return "item-definitions";
        }
        if (root.equals("atlases") && hasExtension(path, ".json")) {
            return "atlases";
        }
        if ((root.equals("font") || root.equals("fonts")) && hasExtension(path, ".json")) {
            return "fonts";
        }
        if (root.equals("equipment")) {
            return "equipment";
        }
        if (root.equals("textures")) {
            return "textures";
        }
        if (root.equals("sounds")) {
            return "sounds";
        }
        if (RESOURCE_DIRECTORIES.contains(root)) {
            return root.equals("item") ? "item-definitions" : root;
        }
        return "unknown";
    }

    public static boolean isJson(InputFile input) {
        String path = input.path().toLowerCase(Locale.ROOT);
        return path.endsWith(".json") || path.endsWith("pack.mcmeta");
    }

    private static boolean hasExtension(String path, String extension) {
        return path.toLowerCase(Locale.ROOT).endsWith(extension);
    }
}

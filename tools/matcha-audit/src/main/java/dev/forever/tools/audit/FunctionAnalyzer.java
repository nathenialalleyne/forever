package dev.forever.tools.audit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extracts literal command observations from functions without evaluating gameplay. */
public final class FunctionAnalyzer {
    private static final Pattern ID_PATTERN = Pattern.compile("#?[a-z0-9_.-]+:[a-z0-9_./-]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern FUNCTION_CALL = Pattern.compile("\\bfunction\\s+(#?[a-z0-9_.-]+:[a-z0-9_./-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCHEDULED_FUNCTION = Pattern.compile("\\bschedule\\s+function\\s+(#?[a-z0-9_.-]+:[a-z0-9_./-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ADVANCEMENT_OPERATION = Pattern.compile("\\badvancement\\s+(grant|revoke)\\s+\\S+\\s+(?:only|from|through|until)\\s+(#?[a-z0-9_.-]+:[a-z0-9_./-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern RECIPE_OPERATION = Pattern.compile("\\brecipe\\s+(give|take)\\s+\\S+\\s+(#?[a-z0-9_.-]+:[a-z0-9_./-]+|\\*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern LOOT_OPERATION = Pattern.compile("\\b(?:loot|fish)\\s+(#?[a-z0-9_.-]+:[a-z0-9_./-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ITEM_MODIFIER = Pattern.compile("\\bitem\\s+modify\\s+\\S+\\s+\\S+\\s+(#?[a-z0-9_.-]+:[a-z0-9_./-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern FILE_REFERENCE = Pattern.compile("\\b(?:data|assets)/[A-Za-z0-9_.\\-/]+", Pattern.CASE_INSENSITIVE);

    private FunctionAnalyzer() {
    }

    public static void analyse(AuditReport report) {
        for (ClassifiedFile file : report.files()) {
            if (file.category().equals("functions")) {
                analyseFunction(report, file);
            }
        }
    }

    private static void analyseFunction(AuditReport report, ClassifiedFile file) {
        String id = JsonSupport.idFor(file, file.logicalPath().split("/")[2]);
        JsonObject function = new JsonObject();
        function.addProperty("observationType", "observed");
        function.addProperty("id", id);
        function.addProperty("file", file.logicalPath());
        function.addProperty("namespace", file.namespace());

        JsonArray commands = new JsonArray();
        JsonArray calls = new JsonArray();
        JsonArray scheduled = new JsonArray();
        JsonArray scoreboardOperations = new JsonArray();
        JsonArray advancementOperations = new JsonArray();
        JsonArray recipeOperations = new JsonArray();
        JsonArray lootOperations = new JsonArray();
        JsonArray itemDataOperations = new JsonArray();
        Set<String> namespaceReferences = new LinkedHashSet<>();
        Set<String> vanillaIds = new LinkedHashSet<>();
        Set<String> matchaFileReferences = new LinkedHashSet<>();
        Set<String> seenObjectives = new LinkedHashSet<>();

        String text = new String(file.input().content(), StandardCharsets.UTF_8);
        if (!text.isEmpty() && text.charAt(0) == '\ufeff') {
            text = text.substring(1);
        }
        String[] lines = text.split("\\R", -1);
        function.addProperty("lineCount", lines.length);
        for (int index = 0; index < lines.length; index++) {
            String command = lines[index].trim();
            int lineNumber = index + 1;
            if (command.isBlank() || command.startsWith("#")) {
                continue;
            }
            JsonObject commandRecord = observedLine(lineNumber, command);
            commands.add(commandRecord);

            Matcher ids = ID_PATTERN.matcher(command);
            while (ids.find()) {
                String target = ids.group();
                if (looksLikeNbtKeyValue(command, ids.start(), target)) {
                    continue;
                }
                namespaceReferences.add(target);
                report.addReference(new ReferenceObservation(file.logicalPath(), "functions", "line:" + lineNumber, "namespace-reference", target, target.startsWith("#") ? "tag-id" : "namespaced-id"));
                if (target.toLowerCase(java.util.Locale.ROOT).startsWith("minecraft:")) {
                    vanillaIds.add(target);
                } else if (isObservedNonVanillaNamespace(report, target)) {
                    matchaFileReferences.add(target);
                }
            }
            Matcher paths = FILE_REFERENCE.matcher(command);
            while (paths.find()) {
                String target = paths.group();
                matchaFileReferences.add(target);
                report.addReference(new ReferenceObservation(file.logicalPath(), "functions", "line:" + lineNumber, "file-path-reference", target, "file-path"));
            }

            Matcher matcher = FUNCTION_CALL.matcher(command);
            while (matcher.find()) {
                String target = matcher.group(1);
                if (!isScheduledFunctionToken(command, matcher.start())) {
                    calls.add(observedTarget(lineNumber, command, target));
                    report.addReference(new ReferenceObservation(file.logicalPath(), "functions", "line:" + lineNumber, "function-call", target, "function-id"));
                }
            }
            matcher = SCHEDULED_FUNCTION.matcher(command);
            while (matcher.find()) {
                String target = matcher.group(1);
                scheduled.add(observedTarget(lineNumber, command, target));
                report.addReference(new ReferenceObservation(file.logicalPath(), "functions", "line:" + lineNumber, "scheduled-function", target, "function-id"));
            }
            matcher = ADVANCEMENT_OPERATION.matcher(command);
            while (matcher.find()) {
                String operation = matcher.group(1).toLowerCase(java.util.Locale.ROOT);
                String target = matcher.group(2);
                JsonObject observation = observedTarget(lineNumber, command, target);
                observation.addProperty("operation", operation);
                advancementOperations.add(observation);
                report.addReference(new ReferenceObservation(file.logicalPath(), "functions", "line:" + lineNumber, "advancement-" + operation, target, "advancement-id"));
            }
            matcher = RECIPE_OPERATION.matcher(command);
            while (matcher.find()) {
                String operation = matcher.group(1).toLowerCase(java.util.Locale.ROOT);
                String target = matcher.group(2);
                JsonObject observation = observedTarget(lineNumber, command, target);
                observation.addProperty("operation", operation);
                recipeOperations.add(observation);
                if (!target.equals("*")) {
                    report.addReference(new ReferenceObservation(file.logicalPath(), "functions", "line:" + lineNumber, "recipe-" + operation, target, "recipe-id"));
                }
            }
            matcher = LOOT_OPERATION.matcher(command);
            while (matcher.find()) {
                String target = matcher.group(1);
                lootOperations.add(observedTarget(lineNumber, command, target));
                report.addReference(new ReferenceObservation(file.logicalPath(), "functions", "line:" + lineNumber, "loot-operation", target, "loot-table-id"));
            }
            matcher = ITEM_MODIFIER.matcher(command);
            while (matcher.find()) {
                String target = matcher.group(1);
                itemDataOperations.add(observedTarget(lineNumber, command, target));
                report.addReference(new ReferenceObservation(file.logicalPath(), "functions", "line:" + lineNumber, "item-modifier-reference", target, "item-modifier-id"));
            }
            if (startsWithCommand(command, "item") || startsWithCommand(command, "data")) {
                JsonObject operation = observedLine(lineNumber, command);
                operation.addProperty("operationType", firstToken(command));
                itemDataOperations.add(operation);
            }

            analyseScoreboard(report, file, lineNumber, command, scoreboardOperations, seenObjectives);
        }

        function.add("commands", commands);
        function.add("calls", sortByLine(calls));
        function.add("scheduledFunctions", sortByLine(scheduled));
        function.add("scoreboardOperations", sortByLine(scoreboardOperations));
        function.add("advancementOperations", sortByLine(advancementOperations));
        function.add("recipeOperations", sortByLine(recipeOperations));
        function.add("lootOperations", sortByLine(lootOperations));
        function.add("itemDataOperations", sortByLine(itemDataOperations));
        function.add("namespaceReferences", JsonSupport.strings(JsonSupport.uniqueSortedStrings(namespaceReferences)));
        function.add("hardcodedVanillaIds", JsonSupport.strings(JsonSupport.uniqueSortedStrings(vanillaIds)));
        function.add("matchaFileReferences", JsonSupport.strings(JsonSupport.uniqueSortedStrings(matchaFileReferences)));
        report.functions().add(function);
    }

    private static void analyseScoreboard(AuditReport report, ClassifiedFile file, int line, String command, JsonArray output, Set<String> seenObjectives) {
        String[] tokens = command.split("\\s+");
        if (tokens.length < 3 || !tokens[0].equalsIgnoreCase("scoreboard")) {
            int scoreIndex = indexOfIgnoreCase(tokens, "score");
            if (scoreIndex >= 0 && tokens.length > scoreIndex + 2) {
                addObjectiveReference(report, file, line, command, tokens[scoreIndex + 2], "execute-score", output, seenObjectives);
            }
            return;
        }
        if (tokens.length >= 5 && tokens[1].equalsIgnoreCase("objectives") && tokens[2].equalsIgnoreCase("add")) {
            String objective = tokens[3];
            String criterion = tokens[4];
            JsonObject observation = observedLine(line, command);
            observation.addProperty("operation", "create");
            observation.addProperty("objective", objective);
            observation.addProperty("criterion", criterion);
            output.add(observation);
            addScoreboardDefinition(report, file, line, command, objective, criterion);
            seenObjectives.add(objective);
            return;
        }
        if (tokens.length >= 4 && tokens[1].equalsIgnoreCase("objectives")) {
            String operation = tokens[2].toLowerCase(java.util.Locale.ROOT);
            String objective = tokens[3];
            if (!objective.equals("*")) {
                addObjectiveReference(report, file, line, command, objective, "objectives-" + operation, output, seenObjectives);
            }
            return;
        }
        if (tokens.length >= 5 && tokens[1].equalsIgnoreCase("players")) {
            String operation = tokens[2].toLowerCase(java.util.Locale.ROOT);
            addObjectiveReference(report, file, line, command, tokens[4], "players-" + operation, output, seenObjectives);
            if (operation.equals("operation") && tokens.length >= 8) {
                addObjectiveReference(report, file, line, command, tokens[7], "players-operation-source", output, seenObjectives);
            }
        }
    }

    private static void addScoreboardDefinition(AuditReport report, ClassifiedFile file, int line, String command, String objective, String criterion) {
        JsonObject scoreboard = report.scoreboard(objective);
        JsonArray definitions = scoreboard.getAsJsonArray("definitions");
        JsonObject definition = observedLine(line, command);
        definition.addProperty("operation", "create");
        definition.addProperty("criterion", criterion);
        definition.addProperty("file", file.logicalPath());
        definitions.add(definition);
        report.addReference(new ReferenceObservation(file.logicalPath(), "functions", "line:" + line, "scoreboard-objective-definition", objective, "scoreboard-objective"));
    }

    private static void addObjectiveReference(AuditReport report, ClassifiedFile file, int line, String command, String objective, String operation, JsonArray output, Set<String> seenObjectives) {
        if (objective == null || objective.isBlank() || objective.equals("*")) {
            return;
        }
        JsonObject observation = observedLine(line, command);
        observation.addProperty("operation", "reference");
        observation.addProperty("referenceType", operation);
        observation.addProperty("objective", objective);
        output.add(observation);
        JsonObject scoreboard = report.scoreboard(objective);
        scoreboard.getAsJsonArray("references").add(observation.deepCopy());
        report.addReference(new ReferenceObservation(file.logicalPath(), "functions", "line:" + line, "scoreboard-objective-reference", objective, "scoreboard-objective"));
        seenObjectives.add(objective);
    }

    private static JsonObject observedLine(int line, String text) {
        JsonObject result = new JsonObject();
        result.addProperty("observationType", "observed");
        result.addProperty("line", line);
        result.addProperty("text", text);
        return result;
    }

    private static JsonObject observedTarget(int line, String text, String target) {
        JsonObject result = observedLine(line, text);
        result.addProperty("target", target);
        return result;
    }

    private static JsonArray sortByLine(JsonArray source) {
        List<JsonObject> objects = new ArrayList<>();
        for (var element : source) {
            objects.add(element.getAsJsonObject());
        }
        objects.sort(Comparator.<JsonObject>comparingInt(value -> value.get("line").getAsInt())
                .thenComparing(value -> value.has("target") ? value.get("target").getAsString() : ""));
        JsonArray result = new JsonArray();
        objects.forEach(result::add);
        return result;
    }

    private static boolean isScheduledFunctionToken(String command, int functionTokenStart) {
        String prefix = command.substring(0, functionTokenStart).toLowerCase(java.util.Locale.ROOT);
        return prefix.endsWith("schedule ");
    }

    private static boolean looksLikeNbtKeyValue(String command, int start, String target) {
        int colon = target.indexOf(':');
        if (colon < 1 || colon == target.length() - 1 || !Character.isDigit(target.charAt(colon + 1))) {
            return false;
        }
        if (start == 0) {
            return false;
        }
        char before = command.charAt(start - 1);
        return before == '{' || before == ',';
    }

    private static boolean startsWithCommand(String command, String token) {
        return command.equalsIgnoreCase(token) || command.regionMatches(true, 0, token + " ", 0, token.length() + 1);
    }

    private static String firstToken(String command) {
        int separator = command.indexOf(' ');
        return separator < 0 ? command : command.substring(0, separator);
    }

    private static int indexOfIgnoreCase(String[] values, String wanted) {
        for (int index = 0; index < values.length; index++) {
            if (values[index].equalsIgnoreCase(wanted)) {
                return index;
            }
        }
        return -1;
    }

    private static boolean isObservedNonVanillaNamespace(AuditReport report, String id) {
        String bare = JsonSupport.withoutTagPrefix(id);
        int separator = bare.indexOf(':');
        if (separator < 1) {
            return false;
        }
        String namespace = bare.substring(0, separator);
        return !namespace.equalsIgnoreCase("minecraft")
                && (report.dataNamespaces().contains(namespace) || report.resourceNamespaces().contains(namespace));
    }
}

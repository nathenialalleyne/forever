package dev.forever.tools.audit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Renders the stable human-readable summary from observed report data. */
public final class SummaryWriter {
    private SummaryWriter() {
    }

    public static String render(CliOptions options, AuditReport report, List<ResolvedReference> references) {
        StringBuilder summary = new StringBuilder();
        summary.append("# Matcha audit summary\n\n");
        summary.append("> Derived from the official Matcha Flavoured archive, licence CC-BY-NC-SA-4.0, project QI0EmgZ1.\n");
        summary.append("> This report records observed file/reference data and explicitly labelled analysis results. It does not claim gameplay intent.\n\n");

        summary.append("## Input identity\n\n");
        summary.append("- Version label: `").append(markdown(options.versionLabel())).append("`\n");
        summary.append("- Input kind: `").append(report.snapshot().kind().name().toLowerCase(java.util.Locale.ROOT)).append("`\n");
        summary.append("- Input path: `").append(markdown(report.snapshot().source().toString())).append("`\n");
        summary.append("- SHA-256: `").append(report.snapshot().sha256()).append("`\n");
        summary.append("- Pack-root detection: `").append(report.layout().detection()).append("`\n");
        if (report.layout().isHeuristic()) {
            summary.append("- Pack-root interpretation: **heuristic**, not an observed Minecraft file property.\n");
        }
        summary.append("\n");

        summary.append("## File counts\n\n");
        summary.append("- Regular files: ").append(report.files().size()).append("\n");
        summary.append("- Data-pack files: ").append(countSide(report, "data")).append("\n");
        summary.append("- Resource-pack files: ").append(countSide(report, "resource")).append("\n");
        summary.append("- JSON parse errors retained in inventory: ").append(report.parseErrors().size()).append("\n");
        summary.append("- Unknown-path rows: ").append(report.unknownPaths().size()).append("\n\n");

        summary.append("## Namespaces\n\n");
        summary.append("- Data: ").append(backtickList(report.dataNamespaces())).append("\n");
        summary.append("- Resources: ").append(backtickList(report.resourceNamespaces())).append("\n\n");

        summary.append("## Major data categories\n\n");
        appendCategoryTable(summary, report, "data");
        summary.append("\n## Major resource-pack categories\n\n");
        appendCategoryTable(summary, report, "resource");
        summary.append("\n");

        long functionReferences = references.stream()
                .filter(value -> value.observation().sourceKind().equals("functions"))
                .count();
        long unresolved = references.stream().filter(value -> value.status().equals("unresolved")).count();
        summary.append("## Reference and override counts\n\n");
        summary.append("- Vanilla namespace overrides observed: ").append(report.vanillaOverrides().size()).append("\n");
        summary.append("- Scoreboard objectives detected: ").append(report.scoreboards().size()).append("\n");
        summary.append("- Function reference edges detected: ").append(functionReferences).append("\n");
        summary.append("- Total reference edges detected: ").append(references.size()).append("\n");
        summary.append("- Unresolved internal-looking references: ").append(unresolved).append("\n\n");

        summary.append("## Prominent warnings\n\n");
        if (report.warnings().isEmpty()) {
            summary.append("- None recorded.\n\n");
        } else {
            for (String warning : report.warnings()) {
                summary.append("- ").append(warning).append("\n");
            }
            summary.append('\n');
        }

        summary.append("## Review boundary\n\n");
        summary.append("The analyser reports literal paths, JSON fields, command text, and reference relationships where recognised. Resolution status is based only on exact matches against observed files in this input. Vanilla and external namespaces are not treated as missing files. No gameplay mechanics, balance, or gameplay intent has been inferred.\n");
        return summary.toString();
    }

    private static void appendCategoryTable(StringBuilder summary, AuditReport report, String side) {
        Map<String, Long> counts = report.files().stream()
                .filter(file -> file.side().equals(side))
                .collect(Collectors.groupingBy(ClassifiedFile::category, TreeMap::new, Collectors.counting()));
        summary.append("| Category | Files |\n|---|---:|\n");
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            summary.append('|').append(entry.getKey()).append('|').append(entry.getValue()).append('|').append('\n');
        }
        if (counts.isEmpty()) {
            summary.append("| _none observed_ | 0 |\n");
        }
    }

    private static long countSide(AuditReport report, String side) {
        return report.files().stream().filter(file -> file.side().equals(side)).count();
    }

    private static String backtickList(Iterable<String> values) {
        List<String> list = new ArrayList<>();
        values.forEach(value -> list.add("`" + value + "`"));
        return list.isEmpty() ? "_none observed_" : String.join(", ", list);
    }

    private static String markdown(String value) {
        return value.replace("`", "'").replace("\n", " ").replace("\r", " ");
    }
}

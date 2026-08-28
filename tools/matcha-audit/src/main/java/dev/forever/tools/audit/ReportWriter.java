package dev.forever.tools.audit;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Writes the complete stable report set beneath exactly the selected output directory. */
public final class ReportWriter {
    private static final String TOOL_VERSION = "0.1.0";
    private static final String SCHEMA_VERSION = "1";
    private static final List<String> REPORT_NAMES = List.of(
            "metadata.json", "input-sha256.txt", "file-inventory.csv", "namespaces.json", "pack-filters.json",
            "recipes.json", "advancements.json", "functions.json", "scoreboards.json", "tags.json",
            "loot-tables.json", "predicates.json", "item-modifiers.json", "registry-data.json", "worldgen.json",
            "assets.json", "vanilla-overrides.csv", "cross-references.json", "unresolved-references.csv",
            "unknown-paths.csv", "summary.md");

    private ReportWriter() {
    }

    public static void write(CliOptions options, AuditReport report) throws AuditException {
        Path output = prepareOutputDirectory(options.output());
        List<ResolvedReference> references = ReferenceResolver.resolve(report);
        try {
            writeText(output, "metadata.json", JsonSupport.pretty(metadata(options, report)));
            writeText(output, "input-sha256.txt", report.snapshot().sha256() + "\n");
            writeText(output, "file-inventory.csv", inventoryCsv(report));
            writeText(output, "namespaces.json", namespacesJson(report));
            writeText(output, "pack-filters.json", packFiltersJson(report));
            writeText(output, "recipes.json", collection("recipes", report.recipes()));
            writeText(output, "advancements.json", collection("advancements", report.advancements()));
            writeText(output, "functions.json", collection("functions", report.functions()));
            writeText(output, "scoreboards.json", scoreboardsJson(report));
            writeText(output, "tags.json", collection("tags", report.tags()));
            writeText(output, "loot-tables.json", collection("lootTables", report.lootTables()));
            writeText(output, "predicates.json", collection("predicates", report.predicates()));
            writeText(output, "item-modifiers.json", collection("itemModifiers", report.itemModifiers()));
            writeText(output, "registry-data.json", collection("registryData", report.registryData()));
            writeText(output, "worldgen.json", collection("worldgen", report.worldgen()));
            writeText(output, "assets.json", collection("assets", report.assets()));
            writeText(output, "vanilla-overrides.csv", vanillaOverridesCsv(report));
            writeText(output, "cross-references.json", crossReferencesJson(report, references));
            writeText(output, "unresolved-references.csv", unresolvedReferencesCsv(references));
            writeText(output, "unknown-paths.csv", unknownPathsCsv(report));
            writeText(output, "summary.md", SummaryWriter.render(options, report, references));
        } catch (IOException | SecurityException exception) {
            throw new AuditException(ExitCodes.OUTPUT, "Could not write audit reports inside '" + output + "': " + exception.getMessage(), exception);
        }
    }

    private static Path prepareOutputDirectory(Path requested) throws AuditException {
        if (requested == null) {
            throw new AuditException(ExitCodes.OUTPUT, "Output directory cannot be null.");
        }
        Path output = requested.toAbsolutePath().normalize();
        try {
            rejectSymlinkAncestors(output);
            if (Files.exists(output, LinkOption.NOFOLLOW_LINKS) && !Files.isDirectory(output, LinkOption.NOFOLLOW_LINKS)) {
                throw new AuditException(ExitCodes.OUTPUT, "Output path is not a directory: " + requested);
            }
            Files.createDirectories(output);
            rejectSymlinkAncestors(output);
            for (String reportName : REPORT_NAMES) {
                Path report = output.resolve(reportName);
                if (Files.isSymbolicLink(report)) {
                    throw new AuditException(ExitCodes.OUTPUT, "Refusing to overwrite symbolic-link report path: " + reportName);
                }
            }
            return output;
        } catch (AuditException exception) {
            throw exception;
        } catch (IOException | SecurityException exception) {
            throw new AuditException(ExitCodes.OUTPUT, "Could not create output directory '" + requested + "': " + exception.getMessage(), exception);
        }
    }

    private static void rejectSymlinkAncestors(Path output) throws AuditException {
        Path current = output.getRoot();
        if (current == null) {
            current = output.toAbsolutePath().getRoot();
        }
        for (Path part : output) {
            current = current.resolve(part);
            if (Files.isSymbolicLink(current)) {
                throw new AuditException(ExitCodes.OUTPUT, "Output path contains a symbolic-link directory: " + current);
            }
        }
    }

    private static void writeText(Path output, String name, String contents) throws IOException, AuditException {
        Path path = output.resolve(name).normalize();
        if (!path.getParent().equals(output)) {
            throw new AuditException(ExitCodes.OUTPUT, "Report path escaped the selected output directory: " + name);
        }
        if (Files.isSymbolicLink(path)) {
            throw new AuditException(ExitCodes.OUTPUT, "Refusing to write through symbolic-link report path: " + name);
        }
        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS) && !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new AuditException(ExitCodes.OUTPUT, "Existing report path is not a regular file: " + name);
        }
        Path temporary = Files.createTempFile(output, "." + name + ".", ".tmp");
        try {
            Files.writeString(temporary, contents, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            try {
                Files.move(temporary, path, java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    /**
     * The report timestamp, honouring {@code SOURCE_DATE_EPOCH} when it is set.
     *
     * <p>Every other field in the report is a pure function of the input archive, so this
     * timestamp was the only reason two audits of the same pinned archive produced
     * different bytes. That defeated any attempt to verify generated output by
     * regenerating it and comparing, which matters because the reports are committed and
     * must be honest about their input.
     *
     * <p>{@code SOURCE_DATE_EPOCH} is the cross-ecosystem convention for exactly this
     * problem, so a caller that needs byte-identical output sets it rather than learning a
     * project-specific flag. When it is unset the behaviour is unchanged and the real
     * current time is recorded, because a human reading a one-off report is better served
     * by a true generation time than by a fixed placeholder.
     *
     * <p>An unparseable or negative value is a caller mistake worth surfacing: it is
     * reported rather than silently ignored, since silently falling back would produce
     * non-reproducible output for someone who explicitly asked for reproducibility.
     */
    private static Instant generatedAt() {
        return generatedAt(System.getenv("SOURCE_DATE_EPOCH"), Instant::now);
    }

    /**
     * Resolves the report timestamp from a raw {@code SOURCE_DATE_EPOCH} value.
     *
     * <p>Package-private and parameterised rather than reading the environment directly,
     * because an environment variable cannot be set inside a running JVM. Without this
     * seam the reproducibility guarantee could only be tested by running the CLI as a
     * subprocess, which is slow and easy to skip, so the guarantee would drift untested.
     */
    static Instant generatedAt(String sourceDateEpoch, java.util.function.Supplier<Instant> fallback) {
        if (sourceDateEpoch == null || sourceDateEpoch.isBlank()) {
            return fallback.get();
        }
        long epochSeconds;
        try {
            epochSeconds = Long.parseLong(sourceDateEpoch.trim());
        } catch (NumberFormatException cause) {
            throw new IllegalArgumentException(
                    "SOURCE_DATE_EPOCH must be an integer number of seconds since the Unix epoch, but was: "
                            + sourceDateEpoch,
                    cause);
        }
        if (epochSeconds < 0) {
            throw new IllegalArgumentException(
                    "SOURCE_DATE_EPOCH must not be negative, but was: " + sourceDateEpoch);
        }
        return Instant.ofEpochSecond(epochSeconds);
    }

    private static JsonObject metadata(CliOptions options, AuditReport report) {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", SCHEMA_VERSION);
        root.addProperty("tool", "matcha-audit");
        root.addProperty("toolVersion", TOOL_VERSION);
        root.addProperty("generatedAt", generatedAt().toString());
        JsonObject attribution = new JsonObject();
        attribution.addProperty("source", "Matcha Flavoured archive");
        attribution.addProperty("projectId", "QI0EmgZ1");
        attribution.addProperty("license", "CC-BY-NC-SA-4.0");
        attribution.addProperty("notice", "Reports are derived from the official Matcha Flavoured archive.");
        root.add("attribution", attribution);
        JsonObject input = new JsonObject();
        input.addProperty("kind", report.snapshot().kind().name().toLowerCase(Locale.ROOT));
        input.addProperty("path", report.snapshot().source().toString());
        input.addProperty("versionLabel", options.versionLabel());
        input.addProperty("sha256", report.snapshot().sha256());
        input.addProperty("fileCount", report.files().size());
        root.add("input", input);
        JsonObject layout = new JsonObject();
        layout.addProperty("prefix", report.layout().prefix());
        layout.addProperty("detection", report.layout().detection());
        layout.addProperty("observationType", report.layout().isHeuristic() ? "heuristic" : "observed");
        root.add("packRoot", layout);
        JsonObject pack = report.packMetadata();
        root.add("packMetadata", pack == null ? JsonNull.INSTANCE : pack);
        root.addProperty("packMetadataStatus", pack == null ? "not-observed-or-invalid" : "parsed");
        root.addProperty("packFiltersStatus", report.packFiltersStatus());
        root.add("warnings", JsonSupport.strings(report.warnings()));
        return root;
    }

    private static String inventoryCsv(AuditReport report) {
        List<List<String>> rows = new ArrayList<>();
        for (ClassifiedFile file : report.files()) {
            JsonParseResult parsed = file.jsonCandidate() ? report.parsedJson(file.logicalPath()) : null;
            rows.add(List.of(
                    file.input().path(), file.logicalPath(), file.side(), file.namespace(), file.category(),
                    Long.toString(file.size()), file.input().sha256(), Boolean.toString(file.jsonCandidate()),
                    parsed == null ? "not-applicable" : parsed.status(), parsed == null ? "" : parsed.error(),
                    "observed", "path-and-extension-classification"));
        }
        return CsvWriter.render(List.of("path", "logical_path", "side", "namespace", "category", "size", "sha256", "json_candidate", "parse_status", "parse_error", "observation_type", "classification_basis"), rows);
    }

    private static String namespacesJson(AuditReport report) {
        JsonObject root = observedRoot();
        root.add("data", namespaceEntries(report.dataNamespaces(), report, "data"));
        root.add("resource", namespaceEntries(report.resourceNamespaces(), report, "resource"));
        Set<String> all = new java.util.TreeSet<>();
        all.addAll(report.dataNamespaces());
        all.addAll(report.resourceNamespaces());
        root.add("all", JsonSupport.strings(all));
        List<String> references = report.references().stream()
                .map(ReferenceObservation::target)
                .filter(JsonSupport::isNamespacedId)
                .map(JsonSupport::withoutTagPrefix)
                .map(ReportWriter::namespaceOf)
                .filter(value -> !value.isBlank())
                .distinct().sorted().toList();
        root.add("referencedNamespaces", JsonSupport.strings(references));
        return JsonSupport.pretty(root);
    }

    private static JsonArray namespaceEntries(Set<String> namespaces, AuditReport report, String side) {
        JsonArray result = new JsonArray();
        for (String namespace : namespaces) {
            JsonObject entry = new JsonObject();
            entry.addProperty("observationType", "observed");
            entry.addProperty("namespace", namespace);
            entry.addProperty("side", side);
            entry.addProperty("fileCount", report.files().stream().filter(file -> file.side().equals(side) && file.namespace().equals(namespace)).count());
            entry.add("categories", JsonSupport.strings(report.files().stream()
                    .filter(file -> file.side().equals(side) && file.namespace().equals(namespace))
                    .map(ClassifiedFile::category).distinct().sorted().toList()));
            result.add(entry);
        }
        return result;
    }

    private static String packFiltersJson(AuditReport report) {
        JsonObject root = observedRoot();
        root.addProperty("status", report.packFiltersStatus());
        root.addProperty("source", "pack.mcmeta");
        root.add("filter", report.packFilters() == null ? JsonNull.INSTANCE : report.packFilters());
        return JsonSupport.pretty(root);
    }

    private static String collection(String name, List<JsonObject> entries) {
        JsonObject root = observedRoot();
        root.add("entries", sortedEntries(entries));
        root.addProperty("entryCount", entries.size());
        root.addProperty("collection", name);
        return JsonSupport.pretty(root);
    }

    private static String scoreboardsJson(AuditReport report) {
        JsonObject root = observedRoot();
        List<JsonObject> values = report.scoreboards().values().stream().sorted(Comparator.comparing(value -> value.get("objective").getAsString())).toList();
        root.add("entries", sortedEntries(values));
        root.addProperty("entryCount", values.size());
        root.addProperty("collection", "scoreboards");
        return JsonSupport.pretty(root);
    }

    private static JsonArray sortedEntries(List<JsonObject> entries) {
        List<JsonObject> sorted = entries.stream().sorted(Comparator
                .comparing((JsonObject value) -> stringValue(value, "file"))
                .thenComparing(value -> stringValue(value, "id"))
                .thenComparing(value -> stringValue(value, "objective"))
                .thenComparing(value -> stringValue(value, "assetType"))).toList();
        JsonArray result = new JsonArray();
        for (JsonObject entry : sorted) {
            result.add(JsonSupport.canonical(entry));
        }
        return result;
    }

    private static String crossReferencesJson(AuditReport report, List<ResolvedReference> references) {
        JsonObject root = observedRoot();
        JsonArray entries = new JsonArray();
        for (ResolvedReference resolved : references) {
            ReferenceObservation observation = resolved.observation();
            JsonObject entry = new JsonObject();
            entry.addProperty("observationType", "observed");
            entry.addProperty("sourceFile", observation.sourceFile());
            entry.addProperty("sourceKind", observation.sourceKind());
            entry.addProperty("sourceLocation", observation.sourceLocation());
            entry.addProperty("relation", observation.relation());
            entry.addProperty("target", observation.target());
            entry.addProperty("targetKind", observation.targetKind());
            entry.addProperty("resolutionStatus", resolved.status());
            entry.addProperty("resolutionBasis", resolved.basis());
            if (!resolved.resolvedFile().isBlank()) {
                entry.addProperty("resolvedFile", resolved.resolvedFile());
            }
            entries.add(entry);
        }
        root.add("entries", entries);
        root.addProperty("entryCount", entries.size());
        JsonArray errors = new JsonArray();
        for (ParseErrorObservation error : report.parseErrors().stream().sorted(Comparator.comparing(ParseErrorObservation::file)).toList()) {
            JsonObject item = new JsonObject();
            item.addProperty("observationType", "observed");
            item.addProperty("file", error.file());
            item.addProperty("error", error.error());
            errors.add(item);
        }
        root.add("parseErrors", errors);
        return JsonSupport.pretty(root);
    }

    private static String vanillaOverridesCsv(AuditReport report) {
        List<List<String>> rows = report.vanillaOverrides().stream()
                .sorted(Comparator.comparing(VanillaOverrideObservation::file).thenComparing(VanillaOverrideObservation::category))
                .map(value -> List.of(value.side(), value.category(), value.vanillaId(), value.file(), value.reason(), "observed"))
                .toList();
        return CsvWriter.render(List.of("side", "category", "vanilla_id", "file", "reason", "observation_type"), rows);
    }

    private static String unresolvedReferencesCsv(List<ResolvedReference> references) {
        List<List<String>> rows = references.stream()
                .filter(value -> value.status().equals("unresolved"))
                .map(value -> {
                    ReferenceObservation observation = value.observation();
                    return List.of(observation.sourceFile(), observation.sourceKind(), observation.sourceLocation(), observation.relation(), observation.target(), observation.targetKind(), value.status(), value.basis(), "observed-reference");
                })
                .toList();
        return CsvWriter.render(List.of("source_file", "source_kind", "source_location", "relation", "target", "target_kind", "resolution_status", "reason", "observation_type"), rows);
    }

    private static String unknownPathsCsv(AuditReport report) {
        List<List<String>> rows = report.unknownPaths().stream()
                .sorted(Comparator.comparing(UnknownPathObservation::path).thenComparing(UnknownPathObservation::kind))
                .map(value -> List.of(value.path(), value.kind(), value.side(), value.reason(), "observed"))
                .toList();
        return CsvWriter.render(List.of("path", "kind", "side", "reason", "observation_type"), rows);
    }

    private static JsonObject observedRoot() {
        JsonObject root = new JsonObject();
        root.addProperty("observationType", "observed");
        root.addProperty("schemaVersion", SCHEMA_VERSION);
        return root;
    }

    private static String stringValue(JsonObject object, String property) {
        return object.has(property) && object.get(property).isJsonPrimitive() ? object.get(property).getAsString() : "";
    }

    private static String namespaceOf(String id) {
        int separator = id.indexOf(':');
        return separator > 0 ? id.substring(0, separator) : "";
    }
}

package dev.forever.tools.audit;

import java.util.LinkedHashSet;
import java.util.Set;

/** Records unclassified files and their containing directories for manual review. */
public final class UnknownPathAnalyzer {
    private UnknownPathAnalyzer() {
    }

    public static void analyse(AuditReport report) {
        Set<String> directories = new LinkedHashSet<>();
        for (ClassifiedFile file : report.files()) {
            if (!file.category().equals("unknown")) {
                continue;
            }
            report.addUnknownPath(new UnknownPathObservation(file.logicalPath(), "file", file.side(), reason(file)));
            String[] parts = file.logicalPath().split("/");
            for (int length = 1; length < parts.length; length++) {
                String directory = String.join("/", java.util.Arrays.copyOf(parts, length));
                if (isUnknownDirectory(directory, file)) {
                    directories.add(directory);
                }
            }
        }
        for (String directory : directories) {
            String side = directory.startsWith("data/") ? "data" : directory.startsWith("assets/") ? "resource" : "unknown";
            report.addUnknownPath(new UnknownPathObservation(directory, "directory", side, "containing directory of an unclassified file"));
        }
    }

    private static boolean isUnknownDirectory(String directory, ClassifiedFile file) {
        if (directory.equals("data") || directory.equals("assets") || directory.equals("pack")) {
            return false;
        }
        String[] parts = directory.split("/");
        if (file.side().equals("data") && parts.length <= 2) {
            return false;
        }
        if (file.side().equals("resource") && parts.length <= 2) {
            return false;
        }
        return true;
    }

    private static String reason(ClassifiedFile file) {
        if (file.logicalPath().startsWith("data/") || file.logicalPath().startsWith("assets/")) {
            return "path is under a pack tree but does not match a recognised category or file extension";
        }
        return "path is outside the recognised pack metadata, data, and assets trees";
    }
}

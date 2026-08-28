package dev.forever.tools.audit;

import java.util.Comparator;

/** Coordinates the independent analyzers for one in-memory input snapshot. */
public final class AuditAnalyzer {
    private AuditAnalyzer() {
    }

    public static AuditReport analyse(InputSnapshot snapshot) {
        PackLayout layout = PackLayout.detect(snapshot.files());
        java.util.List<ClassifiedFile> classified = snapshot.files().stream()
                .map(file -> PathClassifier.classify(file, layout))
                .sorted(Comparator.comparing(ClassifiedFile::logicalPath).thenComparing(file -> file.input().path()))
                .toList();
        AuditReport report = new AuditReport(snapshot, layout, classified);
        PackAnalyzer.analyse(report);
        JsonAnalyzer.analyse(report);
        FunctionAnalyzer.analyse(report);
        UnknownPathAnalyzer.analyse(report);
        if (layout.isHeuristic()) {
            report.addWarning("Pack root used the labelled heuristic '" + layout.detection() + "'.");
        }
        if (report.files().isEmpty()) {
            report.addWarning("Input contained no regular files.");
        }
        if (!report.parseErrors().isEmpty()) {
            report.addWarning(report.parseErrors().size() + " JSON file(s) could not be parsed and remain visible in the inventory.");
        }
        return report;
    }
}

package dev.forever.tools.audit;

import java.util.List;
import java.util.StringJoiner;

/** RFC-4180-style UTF-8 CSV rendering with deterministic line endings. */
public final class CsvWriter {
    private CsvWriter() {
    }

    public static String render(List<String> headers, Iterable<List<String>> rows) {
        StringBuilder result = new StringBuilder();
        result.append(row(headers));
        result.append('\n');
        for (List<String> values : rows) {
            result.append(row(values));
            result.append('\n');
        }
        return result.toString();
    }

    private static String row(List<String> values) {
        StringJoiner joiner = new StringJoiner(",");
        for (String value : values) {
            String safe = value == null ? "" : value;
            joiner.add('"' + safe.replace("\"", "\"\"") + '"');
        }
        return joiner.toString();
    }
}

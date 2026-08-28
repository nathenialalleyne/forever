package dev.forever.tools.audit;

/** A visible JSON parse failure retained as an observed inventory warning. */
public record ParseErrorObservation(String file, String error) {
}

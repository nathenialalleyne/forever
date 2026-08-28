package dev.forever.tools.audit;

/** An observed file placed in the vanilla namespace. */
public record VanillaOverrideObservation(String side, String category, String vanillaId, String file, String reason) {
}

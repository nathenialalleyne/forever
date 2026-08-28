package dev.forever.core.mastery;

import net.minecraft.resources.Identifier;

/** Constructors for the bounded Prospector prototype evidence vocabulary. */
public final class ProspectorEvidence {

	private ProspectorEvidence() {
	}

	public static ProgressionEvidence oreType(String eventId, Identifier oreId) {
		return evidence(eventId, EvidenceKind.VARIETY, "ore_type", oreId.toString());
	}

	public static ProgressionEvidence depthBand(String eventId, String bandId) {
		return evidence(eventId, EvidenceKind.VARIETY, "depth_band", bandId);
	}

	public static ProgressionEvidence biome(String eventId, Identifier biomeId) {
		return evidence(eventId, EvidenceKind.VARIETY, "biome", biomeId.toString());
	}

	public static ProgressionEvidence surveyMethod(String eventId, String methodId) {
		return evidence(eventId, EvidenceKind.VARIETY, "survey_method", methodId);
	}

	public static ProgressionEvidence discovery(String eventId, String discoveryId) {
		return evidence(eventId, EvidenceKind.DISCOVERY, "ore_discovery", discoveryId);
	}

	public static ProgressionEvidence project(String eventId, String projectId) {
		return evidence(eventId, EvidenceKind.PROJECT, "project", projectId);
	}

	public static ProgressionEvidence technique(String eventId, String techniqueId) {
		return evidence(eventId, EvidenceKind.TECHNIQUE, "technique", techniqueId);
	}

	private static ProgressionEvidence evidence(
			String eventId, EvidenceKind kind, String dimension, String key) {
		return new ProgressionEvidence(eventId, kind, dimension, key);
	}
}

package dev.forever.core.mastery.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;

/**
 * A named progression node. Its threshold is the number of distinct matching evidence
 * keys, never the number of repeated actions.
 */
public record ProgressionMilestone(
		String id,
		EvidenceKind evidenceKind,
		String dimension,
		int threshold,
		int rank,
		String capabilityKey,
		String explanationKey) {

	private record Encoded(
			String id,
			EvidenceKind evidenceKind,
			String dimension,
			int threshold,
			int rank,
			String capabilityKey,
			String explanationKey) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.sizeLimitedString(128).fieldOf("id").forGetter(Encoded::id),
			EvidenceKind.CODEC.fieldOf("evidence_kind").forGetter(Encoded::evidenceKind),
			Codec.sizeLimitedString(64).fieldOf("dimension").forGetter(Encoded::dimension),
			Codec.INT.fieldOf("threshold").forGetter(Encoded::threshold),
			Codec.INT.fieldOf("rank").forGetter(Encoded::rank),
			Codec.sizeLimitedString(256).fieldOf("capability_key").forGetter(Encoded::capabilityKey),
			Codec.sizeLimitedString(256).fieldOf("explanation_key").forGetter(Encoded::explanationKey)
		).apply(instance, Encoded::new));

	/** Codec for the validated, data-driven milestone representation. */
	public static final Codec<ProgressionMilestone> CODEC = RAW_CODEC.flatXmap(
			encoded -> create(
					encoded.id(),
					encoded.evidenceKind(),
					encoded.dimension(),
					encoded.threshold(),
					encoded.rank(),
					encoded.capabilityKey(),
					encoded.explanationKey()),
				milestone -> DataResult.success(new Encoded(
					milestone.id(),
					milestone.evidenceKind(),
					milestone.dimension(),
					milestone.threshold(),
					milestone.rank(),
					milestone.capabilityKey(),
					milestone.explanationKey())));

	public ProgressionMilestone {
		id = Objects.requireNonNull(id, "milestone id");
		evidenceKind = Objects.requireNonNull(evidenceKind, "milestone evidence kind");
		dimension = Objects.requireNonNull(dimension, "milestone dimension");
		capabilityKey = Objects.requireNonNull(capabilityKey, "milestone capability key");
		explanationKey = Objects.requireNonNull(explanationKey, "milestone explanation key");
	}

	private static DataResult<ProgressionMilestone> create(
			String id,
			EvidenceKind evidenceKind,
			String dimension,
			int threshold,
			int rank,
			String capabilityKey,
			String explanationKey) {
		ProgressionMilestone milestone;
		try {
			milestone = new ProgressionMilestone(
					id, evidenceKind, dimension, threshold, rank, capabilityKey, explanationKey);
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
		return validate(milestone);
	}

	/** Validates this node independently of the definition that contains it. */
	public DataResult<ProgressionMilestone> validate() {
		return validate(this);
	}

	private static DataResult<ProgressionMilestone> validate(ProgressionMilestone milestone) {
		if (!isKey(milestone.id())) {
			return DataResult.error(() -> "Milestone id '" + milestone.id()
					+ "' must use lowercase letters, digits, '.', '_', or '-'.");
		}
		if (!isKey(milestone.dimension())) {
			return DataResult.error(() -> "Milestone '" + milestone.id()
					+ "' has invalid evidence dimension '" + milestone.dimension() + "'.");
		}
		if (milestone.threshold() < 1) {
			return DataResult.error(() -> "Milestone '" + milestone.id()
					+ "' must have a positive distinct-evidence threshold, found "
					+ milestone.threshold() + ".");
		}
		if (milestone.rank() < 1) {
			return DataResult.error(() -> "Milestone '" + milestone.id()
					+ "' must award a positive rank, found " + milestone.rank() + ".");
		}
		if (!isTranslationKey(milestone.capabilityKey())) {
			return DataResult.error(() -> "Milestone '" + milestone.id()
					+ "' has invalid capability_key '" + milestone.capabilityKey() + "'.");
		}
		if (!isTranslationKey(milestone.explanationKey())) {
			return DataResult.error(() -> "Milestone '" + milestone.id()
					+ "' has invalid explanation_key '" + milestone.explanationKey() + "'.");
		}
		return DataResult.success(milestone);
	}

	private static boolean isKey(String value) {
		return value != null && value.matches("[a-z0-9][a-z0-9_.-]*");
	}

	private static boolean isTranslationKey(String value) {
		return value != null && value.matches("[a-z0-9][a-z0-9_.-]*") && value.length() <= 256;
	}
}

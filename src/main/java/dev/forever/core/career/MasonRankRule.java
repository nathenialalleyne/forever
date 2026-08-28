package dev.forever.core.career;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Set;
import net.minecraft.resources.Identifier;

/** Data-defined evidence thresholds and capabilities for one Mason rank. */
public record MasonRankRule(
		MasonRank rank,
		int demonstratedTechniqueThreshold,
		int taughtTechniqueThreshold,
		int validatedProjectThreshold,
		Set<Identifier> capabilities) {

	private static final int MAX_CAPABILITIES = 32;
	private record Encoded(
			MasonRank rank,
			int demonstratedTechniqueThreshold,
			int taughtTechniqueThreshold,
			int validatedProjectThreshold,
			Set<Identifier> capabilities) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			MasonRank.CODEC.fieldOf("rank").forGetter(Encoded::rank),
			Codec.INT.fieldOf("demonstrated_technique_threshold")
					.forGetter(Encoded::demonstratedTechniqueThreshold),
			Codec.INT.fieldOf("taught_technique_threshold").forGetter(Encoded::taughtTechniqueThreshold),
			Codec.INT.fieldOf("validated_project_threshold").forGetter(Encoded::validatedProjectThreshold),
			CareerCodecs.boundedSet(Identifier.CODEC, MAX_CAPABILITIES, "rank capabilities")
					.fieldOf("capabilities").forGetter(Encoded::capabilities)
	).apply(instance, Encoded::new));

	public static final Codec<MasonRankRule> CODEC = RAW_CODEC.flatXmap(
			encoded -> create(encoded),
			rule -> DataResult.success(new Encoded(rule.rank(), rule.demonstratedTechniqueThreshold(),
					rule.taughtTechniqueThreshold(), rule.validatedProjectThreshold(), rule.capabilities())));

	public MasonRankRule {
		rank = Objects.requireNonNull(rank, "rank");
		if (rank == MasonRank.UNASSIGNED) {
			throw new IllegalArgumentException("Mason rank rules cannot target the unassigned rank.");
		}
		if (demonstratedTechniqueThreshold < 0 || taughtTechniqueThreshold < 0
				|| validatedProjectThreshold < 0) {
			throw new IllegalArgumentException("Mason rank evidence thresholds cannot be negative.");
		}
		capabilities = CareerCodecs.copySet(capabilities, "rank capabilities", MAX_CAPABILITIES);
		if (capabilities.isEmpty()) {
			throw new IllegalArgumentException("Every Mason rank must expose at least one capability.");
		}
	}

	private static DataResult<MasonRankRule> create(Encoded encoded) {
		try {
			return DataResult.success(new MasonRankRule(encoded.rank(), encoded.demonstratedTechniqueThreshold(),
					encoded.taughtTechniqueThreshold(), encoded.validatedProjectThreshold(), encoded.capabilities()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	public boolean satisfiedBy(CareerState state) {
		return state.demonstratedTechniques().size() >= demonstratedTechniqueThreshold
				&& state.taughtTechniques().size() >= taughtTechniqueThreshold
				&& state.validatedProjects().size() >= validatedProjectThreshold;
	}
}

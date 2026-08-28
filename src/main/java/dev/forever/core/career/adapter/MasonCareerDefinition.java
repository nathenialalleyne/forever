package dev.forever.core.career;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.data.SchemaVersioned;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;

/** Immutable server-data definition for the Mason profession and its rank rules. */
public record MasonCareerDefinition(
		int schemaVersion,
		Identifier profession,
		Set<Identifier> approvedTechniques,
		List<MasonRankRule> ranks) {

	public static final int CURRENT_SCHEMA = 1;
	private static final int MAX_TECHNIQUES = 128;
	private static final int MAX_RANKS = 8;

	private record Encoded(
			int schemaVersion,
			Identifier profession,
			Set<Identifier> approvedTechniques,
			List<MasonRankRule> ranks) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("schema_version").forGetter(Encoded::schemaVersion),
			Identifier.CODEC.fieldOf("profession").forGetter(Encoded::profession),
			CareerCodecs.boundedSet(Identifier.CODEC, MAX_TECHNIQUES, "approved techniques")
					.fieldOf("approved_techniques").forGetter(Encoded::approvedTechniques),
			CareerCodecs.boundedList(MasonRankRule.CODEC, MAX_RANKS, "Mason rank rules")
					.fieldOf("ranks").forGetter(Encoded::ranks)
	).apply(instance, Encoded::new));

	private static final Codec<MasonCareerDefinition> RAW_DEFINITION_CODEC = RAW_CODEC.flatXmap(
			encoded -> create(encoded),
			definition -> DataResult.success(new Encoded(definition.schemaVersion(), definition.profession(),
					definition.approvedTechniques(), definition.ranks())));

	public static final Codec<MasonCareerDefinition> CODEC = SchemaVersioned.migrating(
			RAW_DEFINITION_CODEC,
			MasonCareerDefinition::schemaVersion,
			CURRENT_SCHEMA,
			SchemaVersioned.noMigrationsYet("Mason career definition"));

	public MasonCareerDefinition {
		if (schemaVersion < 1) {
			throw new IllegalArgumentException("Mason career definition schema version must be at least 1.");
		}
		profession = Objects.requireNonNull(profession, "profession");
		if (!profession.equals(MasonIds.MASON)) {
			throw new IllegalArgumentException("The vertical slice definition must target profession "
					+ MasonIds.MASON + ".");
		}
		approvedTechniques = CareerCodecs.copySet(approvedTechniques, "approved techniques", MAX_TECHNIQUES);
		if (approvedTechniques.isEmpty()) {
			throw new IllegalArgumentException("Mason data must approve at least one technique.");
		}
		ranks = List.copyOf(Objects.requireNonNull(ranks, "ranks"));
		if (ranks.isEmpty() || ranks.size() > MAX_RANKS) {
			throw new IllegalArgumentException("Mason data must contain a bounded set of rank rules.");
		}
		Set<MasonRank> seen = new HashSet<>();
		int previousDemonstrated = 0;
		int previousTaught = 0;
		int previousProjects = 0;
		for (MasonRankRule rule : ranks) {
			Objects.requireNonNull(rule, "rank rule");
			if (!seen.add(rule.rank())) {
				throw new IllegalArgumentException("Mason data declares rank " + rule.rank() + " more than once.");
			}
			if (rule.demonstratedTechniqueThreshold() < previousDemonstrated
					|| rule.taughtTechniqueThreshold() < previousTaught
					|| rule.validatedProjectThreshold() < previousProjects) {
				throw new IllegalArgumentException("Mason rank thresholds must not decrease as rank rises.");
			}
			previousDemonstrated = rule.demonstratedTechniqueThreshold();
			previousTaught = rule.taughtTechniqueThreshold();
			previousProjects = rule.validatedProjectThreshold();
		}
		for (MasonRank rank : List.of(MasonRank.APPRENTICE, MasonRank.JOURNEYMAN, MasonRank.MASTER)) {
			if (!seen.contains(rank)) {
				throw new IllegalArgumentException("Mason data is missing the " + rank + " rank rule.");
			}
		}
	}

	private static DataResult<MasonCareerDefinition> create(Encoded encoded) {
		try {
			return DataResult.success(new MasonCareerDefinition(encoded.schemaVersion(), encoded.profession(),
					encoded.approvedTechniques(), encoded.ranks()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	public Optional<MasonRankRule> ruleFor(MasonRank rank) {
		return ranks.stream().filter(rule -> rule.rank() == rank).findFirst();
	}

	public boolean knowsDefinitionFor(Identifier technique) {
		return approvedTechniques.contains(technique);
	}

	public boolean exposesCapability(Identifier capability) {
		return ranks.stream().anyMatch(rule -> rule.capabilities().contains(capability));
	}

	/** Returns a stable copy suitable for a bounded server summary. */
	public List<MasonRankRule> orderedRanks() {
		return List.copyOf(new ArrayList<>(ranks));
	}
}

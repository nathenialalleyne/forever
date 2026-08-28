package dev.forever.core.mastery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;

/** Immutable, data-driven definition of one mastery path. */
public record MasteryDefinition(
		Identifier id,
		String displayKey,
		String descriptionKey,
		List<Identifier> dependencies,
		Set<String> evidenceDimensions,
		List<ProgressionMilestone> milestones) {

	private record Encoded(
			Identifier id,
			String displayKey,
			String descriptionKey,
			List<Identifier> dependencies,
			List<String> evidenceDimensions,
			List<ProgressionMilestone> milestones) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("id").forGetter(Encoded::id),
			Codec.sizeLimitedString(256).fieldOf("display_key").forGetter(Encoded::displayKey),
			Codec.sizeLimitedString(256).fieldOf("description_key").forGetter(Encoded::descriptionKey),
			Identifier.CODEC.listOf().fieldOf("dependencies").forGetter(Encoded::dependencies),
			Codec.sizeLimitedString(64).listOf().fieldOf("evidence_dimensions")
					.forGetter(Encoded::evidenceDimensions),
			ProgressionMilestone.CODEC.listOf().fieldOf("milestones").forGetter(Encoded::milestones)
		).apply(instance, Encoded::new));

	/** Codec for a fully validated mastery definition. */
	public static final Codec<MasteryDefinition> CODEC = RAW_CODEC.flatXmap(
			encoded -> create(
					encoded.id(),
					encoded.displayKey(),
					encoded.descriptionKey(),
					encoded.dependencies(),
					encoded.evidenceDimensions(),
					encoded.milestones()),
				definition -> DataResult.success(new Encoded(
					definition.id(),
					definition.displayKey(),
					definition.descriptionKey(),
					definition.dependencies(),
					definition.evidenceDimensions().stream().sorted().toList(),
					definition.milestones())));

	public MasteryDefinition {
		id = Objects.requireNonNull(id, "mastery id");
		displayKey = Objects.requireNonNull(displayKey, "mastery display key");
		descriptionKey = Objects.requireNonNull(descriptionKey, "mastery description key");
		dependencies = List.copyOf(Objects.requireNonNull(dependencies, "mastery dependencies"));
		evidenceDimensions = Set.copyOf(
				Objects.requireNonNull(evidenceDimensions, "mastery evidence dimensions"));
		milestones = List.copyOf(Objects.requireNonNull(milestones, "mastery milestones"));
	}

	private static DataResult<MasteryDefinition> create(
			Identifier id,
			String displayKey,
			String descriptionKey,
			List<Identifier> dependencies,
			List<String> evidenceDimensions,
			List<ProgressionMilestone> milestones) {
		if (!hasNoDuplicateStrings(evidenceDimensions)) {
			return DataResult.error(() -> "Mastery '" + id
					+ "' lists an evidence dimension more than once.");
		}
		MasteryDefinition definition;
		try {
			definition = new MasteryDefinition(
					id,
					displayKey,
					descriptionKey,
					dependencies,
					new HashSet<>(evidenceDimensions),
					milestones);
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
		return validate(definition);
	}

	/** Validates this definition without requiring the containing registry. */
	public DataResult<MasteryDefinition> validate() {
		return validate(this);
	}

	/** Returns the highest rank named by this definition. */
	public int highestRank() {
		return milestones.stream().mapToInt(ProgressionMilestone::rank).max().orElse(0);
	}

	/** Returns nodes that can evaluate an evidence event of the supplied kind and dimension. */
	public List<ProgressionMilestone> matching(EvidenceKind kind, String dimension) {
		return milestones.stream()
				.filter(node -> node.evidenceKind() == kind && node.dimension().equals(dimension))
				.toList();
	}

	public Optional<ProgressionMilestone> milestone(String milestoneId) {
		return milestones.stream().filter(node -> node.id().equals(milestoneId)).findFirst();
	}

	private static DataResult<MasteryDefinition> validate(MasteryDefinition definition) {
		if (definition.id() == null) {
			return DataResult.error(() -> "Mastery definition has no id.");
		}
		if (!isTranslationKey(definition.displayKey())) {
			return DataResult.error(() -> "Mastery '" + definition.id()
					+ "' has invalid display_key '" + definition.displayKey() + "'.");
		}
		if (!isTranslationKey(definition.descriptionKey())) {
			return DataResult.error(() -> "Mastery '" + definition.id()
					+ "' has invalid description_key '" + definition.descriptionKey() + "'.");
		}
		if (!hasNoDuplicates(definition.dependencies())) {
			return DataResult.error(() -> "Mastery '" + definition.id()
					+ "' lists a dependency more than once.");
		}
		if (definition.dependencies().contains(definition.id())) {
			return DataResult.error(() -> "Mastery '" + definition.id()
					+ "' cannot depend on itself.");
		}
		if (definition.evidenceDimensions().isEmpty()) {
			return DataResult.error(() -> "Mastery '" + definition.id()
					+ "' must declare at least one evidence dimension.");
		}
		for (String dimension : definition.evidenceDimensions()) {
			if (!isDimension(dimension)) {
				return DataResult.error(() -> "Mastery '" + definition.id()
						+ "' has invalid evidence dimension '" + dimension + "'.");
			}
		}
		if (definition.milestones().isEmpty()) {
			return DataResult.error(() -> "Mastery '" + definition.id()
					+ "' must declare at least one named milestone.");
		}

		Set<String> milestoneIds = new HashSet<>();
		for (ProgressionMilestone milestone : definition.milestones()) {
			DataResult<ProgressionMilestone> milestoneResult = milestone.validate();
			if (milestoneResult.error().isPresent()) {
				String message = milestoneResult.error().orElseThrow().message();
				return DataResult.error(() -> "Mastery '" + definition.id() + "': " + message);
			}
			if (!milestoneIds.add(milestone.id())) {
				return DataResult.error(() -> "Mastery '" + definition.id()
						+ "' declares duplicate milestone '" + milestone.id() + "'.");
			}
			if (!definition.evidenceDimensions().contains(milestone.dimension())) {
				return DataResult.error(() -> "Mastery '" + definition.id() + "' milestone '"
						+ milestone.id() + "' uses undeclared evidence dimension '"
						+ milestone.dimension() + "'.");
			}
		}
		return DataResult.success(definition);
	}

	private static boolean hasNoDuplicates(List<Identifier> values) {
		return values.size() == new HashSet<>(values).size();
	}

	private static boolean hasNoDuplicateStrings(List<String> values) {
		return values.size() == new HashSet<>(values).size();
	}

	private static boolean isDimension(String value) {
		return value != null && value.matches("[a-z0-9][a-z0-9_.-]*");
	}

	private static boolean isTranslationKey(String value) {
		return value != null && value.matches("[a-z0-9][a-z0-9_.-]*") && value.length() <= 256;
	}
}

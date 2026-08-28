package dev.forever.core.equipment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.data.SchemaVersioned;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.resources.Identifier;

/** Immutable, validated balance data loaded from server resources. */
public record EquipmentBalance(
		int schemaVersion,
		int maxCondition,
		double fieldRepairFraction,
		double soundThreshold,
		double wornThreshold,
		ItemCost fieldRepairCost,
		ItemCost workshopRepairCost,
		ItemCost reforgeCost,
		Map<Integer, Double> craftsmanshipMultipliers,
		Map<Identifier, PathDefinition> paths) {

	public static final int CURRENT_SCHEMA = 1;
	public static final int MAX_PATHS = 16;
	public static final int MAX_TIERS = 33;

	public record ItemCost(Identifier item, int count) {
		public ItemCost {
			Objects.requireNonNull(item, "item");
			if (count < 1 || count > 64) {
				throw new IllegalArgumentException("equipment cost count must be between 1 and 64, got " + count);
			}
		}
	}

	public record PathDefinition(
			String translationKey,
			int maxProgress,
			double conditionMultiplier) {
		public PathDefinition {
			if (translationKey == null || translationKey.isBlank()) {
				throw new IllegalArgumentException("equipment path translation_key must not be blank");
			}
			if (maxProgress < 1 || maxProgress > EquipmentState.MAX_PATH_PROGRESS) {
				throw new IllegalArgumentException("equipment path max_progress must be between 1 and "
						+ EquipmentState.MAX_PATH_PROGRESS + ", got " + maxProgress);
			}
			if (!Double.isFinite(conditionMultiplier) || conditionMultiplier <= 0.0 || conditionMultiplier > 100.0) {
				throw new IllegalArgumentException("equipment path condition_multiplier must be finite and in (0, 100]");
			}
		}
	}

	private static final Codec<Integer> TIER_KEY_CODEC = Codec.STRING.comapFlatMap(
			value -> {
				try {
					return DataResult.success(Integer.valueOf(value));
				} catch (NumberFormatException exception) {
					return DataResult.error(() -> "equipment craftsmanship tier key must be an integer, got '" + value + "'");
				}
			},
			value -> Integer.toString(value));

	private static final Codec<ItemCost> ITEM_COST_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("item").forGetter(ItemCost::item),
			Codec.intRange(1, 64).fieldOf("count").forGetter(ItemCost::count)
		).apply(instance, ItemCost::new));

	private static final Codec<PathDefinition> PATH_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.validate(value -> value.isBlank()
					? DataResult.error(() -> "equipment path translation_key must not be blank")
					: DataResult.success(value)).fieldOf("translation_key").forGetter(PathDefinition::translationKey),
			Codec.intRange(1, EquipmentState.MAX_PATH_PROGRESS).fieldOf("max_progress")
					.forGetter(PathDefinition::maxProgress),
			Codec.doubleRange(0.000001, 100.0).fieldOf("condition_multiplier")
					.forGetter(PathDefinition::conditionMultiplier)
		).apply(instance, PathDefinition::new));

	private static final Codec<Map<Integer, Double>> TIER_CODEC = Codec.unboundedMap(
			TIER_KEY_CODEC,
			Codec.doubleRange(0.000001, 100.0)).validate(values -> {
			if (values.isEmpty()) {
				return DataResult.error(() -> "equipment craftsmanship_multipliers must contain at least tier 0");
			}
			if (values.size() > MAX_TIERS) {
				return DataResult.error(() -> "equipment craftsmanship_multipliers contains too many tiers");
			}
			if (!values.containsKey(0)) {
				return DataResult.error(() -> "equipment craftsmanship_multipliers must define tier 0");
			}
			return DataResult.success(values);
		});

	private static final Codec<Map<Identifier, PathDefinition>> PATHS_CODEC = Codec.unboundedMap(
			Identifier.CODEC, PATH_CODEC).validate(values -> {
			if (values.isEmpty()) {
				return DataResult.error(() -> "equipment paths must contain at least one path definition");
			}
			if (values.size() > MAX_PATHS) {
				return DataResult.error(() -> "equipment paths contains " + values.size()
						+ " paths, but the maximum is " + MAX_PATHS);
			}
			return DataResult.success(values);
		});

	private record Raw(
			int schemaVersion,
			int maxCondition,
			double fieldRepairFraction,
			double soundThreshold,
			double wornThreshold,
			ItemCost fieldRepairCost,
			ItemCost workshopRepairCost,
			ItemCost reforgeCost,
			Map<Integer, Double> craftsmanshipMultipliers,
			Map<Identifier, PathDefinition> paths) {
	}

	private static final Codec<Raw> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("schema_version").forGetter(Raw::schemaVersion),
			Codec.intRange(1, EquipmentState.MAX_CONDITION).fieldOf("max_condition").forGetter(Raw::maxCondition),
			Codec.doubleRange(0.000001, 1.0).fieldOf("field_repair_fraction")
					.forGetter(Raw::fieldRepairFraction),
			Codec.doubleRange(0.000001, 1.0).fieldOf("sound_threshold").forGetter(Raw::soundThreshold),
			Codec.doubleRange(0.000001, 1.0).fieldOf("worn_threshold").forGetter(Raw::wornThreshold),
			ITEM_COST_CODEC.fieldOf("field_repair_cost").forGetter(Raw::fieldRepairCost),
			ITEM_COST_CODEC.fieldOf("workshop_repair_cost").forGetter(Raw::workshopRepairCost),
			ITEM_COST_CODEC.fieldOf("reforge_cost").forGetter(Raw::reforgeCost),
			TIER_CODEC.fieldOf("craftsmanship_multipliers").forGetter(Raw::craftsmanshipMultipliers),
			PATHS_CODEC.fieldOf("paths").forGetter(Raw::paths)
		).apply(instance, Raw::new));

	private static final Codec<EquipmentBalance> BASE_CODEC = RAW_CODEC.comapFlatMap(
			EquipmentBalance::fromRaw,
			balance -> new Raw(
					balance.schemaVersion(),
					balance.maxCondition(),
					balance.fieldRepairFraction(),
					balance.soundThreshold(),
					balance.wornThreshold(),
					balance.fieldRepairCost(),
					balance.workshopRepairCost(),
					balance.reforgeCost(),
					balance.craftsmanshipMultipliers(),
					balance.paths()));

	public static final Codec<EquipmentBalance> CODEC = SchemaVersioned.migrating(
			BASE_CODEC,
			EquipmentBalance::schemaVersion,
			CURRENT_SCHEMA,
			SchemaVersioned.noMigrationsYet("equipment balance"));

	public EquipmentBalance {
		if (schemaVersion < 1) {
			throw new IllegalArgumentException("equipment balance schema version must be at least 1");
		}
		if (maxCondition < 1 || maxCondition > EquipmentState.MAX_CONDITION) {
			throw new IllegalArgumentException("equipment balance max_condition is outside the supported range");
		}
		if (!Double.isFinite(fieldRepairFraction) || fieldRepairFraction <= 0.0 || fieldRepairFraction > 1.0) {
			throw new IllegalArgumentException("equipment balance field_repair_fraction must be finite and in (0, 1]");
		}
		if (!Double.isFinite(soundThreshold) || !Double.isFinite(wornThreshold)
				|| soundThreshold <= 0.0 || soundThreshold > 1.0
				|| wornThreshold <= 0.0 || wornThreshold >= soundThreshold) {
			throw new IllegalArgumentException("equipment balance thresholds must satisfy 0 < worn < sound <= 1");
		}
		fieldRepairCost = Objects.requireNonNull(fieldRepairCost, "fieldRepairCost");
		workshopRepairCost = Objects.requireNonNull(workshopRepairCost, "workshopRepairCost");
		reforgeCost = Objects.requireNonNull(reforgeCost, "reforgeCost");
		craftsmanshipMultipliers = immutableTierMap(craftsmanshipMultipliers);
		paths = immutablePathMap(paths);
	}

	public Optional<PathDefinition> path(Identifier id) {
		return Optional.ofNullable(paths.get(Objects.requireNonNull(id, "id")));
	}

	public Optional<Double> craftsmanshipMultiplier(int tier) {
		return Optional.ofNullable(craftsmanshipMultipliers.get(tier));
	}

	public String conditionStatusKey(double fraction) {
		if (fraction >= soundThreshold) {
			return "tooltip.forever.equipment.status.sound";
		}
		if (fraction >= wornThreshold) {
			return "tooltip.forever.equipment.status.worn";
		}
		return "tooltip.forever.equipment.status.critical";
	}

	private static DataResult<EquipmentBalance> fromRaw(Raw raw) {
		if (raw.schemaVersion() < 1) {
			return DataResult.error(() -> "equipment balance schema version must be at least 1");
		}
		if (raw.wornThreshold() >= raw.soundThreshold()) {
			return DataResult.error(() -> "equipment balance worn_threshold must be less than sound_threshold");
		}
		try {
			return DataResult.success(new EquipmentBalance(
					raw.schemaVersion(),
					raw.maxCondition(),
					raw.fieldRepairFraction(),
					raw.soundThreshold(),
					raw.wornThreshold(),
					raw.fieldRepairCost(),
					raw.workshopRepairCost(),
					raw.reforgeCost(),
					raw.craftsmanshipMultipliers(),
					raw.paths()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(() -> "invalid equipment balance: " + exception.getMessage());
		}
	}

	private static Map<Integer, Double> immutableTierMap(Map<Integer, Double> values) {
		Objects.requireNonNull(values, "craftsmanshipMultipliers");
		TreeMap<Integer, Double> sorted = new TreeMap<>();
		values.forEach((tier, multiplier) -> {
			if (tier == null || multiplier == null || tier < 0 || tier > EquipmentState.MAX_CRAFTSMANSHIP_TIER
					|| !Double.isFinite(multiplier) || multiplier <= 0.0 || multiplier > 100.0) {
				throw new IllegalArgumentException("invalid craftsmanship multiplier entry");
			}
			sorted.put(tier, multiplier);
		});
		if (sorted.isEmpty() || !sorted.containsKey(0)) {
			throw new IllegalArgumentException("craftsmanship multipliers must define tier 0");
		}
		return Collections.unmodifiableMap(sorted);
	}

	private static Map<Identifier, PathDefinition> immutablePathMap(Map<Identifier, PathDefinition> values) {
		Objects.requireNonNull(values, "paths");
		if (values.isEmpty() || values.size() > MAX_PATHS) {
			throw new IllegalArgumentException("equipment paths must contain between 1 and " + MAX_PATHS + " entries");
		}
		TreeMap<Identifier, PathDefinition> sorted = new TreeMap<>();
		values.forEach((id, definition) -> {
			if (id == null || definition == null) {
				throw new IllegalArgumentException("equipment paths cannot contain null entries");
			}
			sorted.put(id, definition);
		});
		return Collections.unmodifiableMap(sorted);
	}
}

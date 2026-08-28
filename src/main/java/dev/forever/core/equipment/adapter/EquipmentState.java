package dev.forever.core.equipment.adapter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.data.SchemaVersioned;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.core.component.DataComponentGetter;

/**
 * The immutable, per-stack equipment record.
 *
 * <p>The record deliberately contains no balance values. Balance definitions are loaded by
 * {@link EquipmentBalanceLoader}; this record only carries the durable identity and the values
 * that must travel with the stack. A zero current condition is the broken state. It is not a
 * separate boolean, so the two values cannot drift apart.
 */
public record EquipmentState(
		int schemaVersion,
		UUID identity,
		int currentCondition,
		int maxCondition,
		int craftsmanshipTier,
		Optional<Identifier> activePath,
		Map<Identifier, Integer> pathProgress) implements TooltipProvider {

	/** The current persisted state schema. Version one is migrated on load. */
	public static final int CURRENT_SCHEMA = 2;

	/** The largest supported condition value in a single stack record. */
	public static final int MAX_CONDITION = 1_000_000_000;

	/** The largest supported craftsmanship tier. */
	public static final int MAX_CRAFTSMANSHIP_TIER = 32;

	/** The largest progress value for an individual path. */
	public static final int MAX_PATH_PROGRESS = 1_000_000_000;

	/** The largest number of retained paths on one stack. */
	public static final int MAX_KNOWN_PATHS = 16;

	private static final UUID ZERO_IDENTITY = new UUID(0L, 0L);

	private static final Codec<UUID> UUID_CODEC = Codec.STRING.comapFlatMap(
			value -> {
				try {
					return DataResult.success(UUID.fromString(value));
				} catch (IllegalArgumentException exception) {
					return DataResult.error(() -> "equipment identity must be a UUID, got '" + value + "'");
				}
			},
			UUID::toString);

	private static final Codec<Map<Identifier, Integer>> PATH_PROGRESS_CODEC = Codec.unboundedMap(
			Identifier.CODEC,
			Codec.intRange(0, MAX_PATH_PROGRESS)).validate(progress -> {
				if (progress.size() > MAX_KNOWN_PATHS) {
					return DataResult.error(() -> "equipment path_progress contains " + progress.size()
							+ " paths, but the maximum is " + MAX_KNOWN_PATHS);
				}
				return DataResult.success(progress);
			});

	private record Raw(
			int schemaVersion,
			UUID identity,
			int currentCondition,
			int maxCondition,
			int craftsmanshipTier,
			Optional<Identifier> activePath,
			Map<Identifier, Integer> pathProgress) {
	}

	private static final Codec<Raw> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("schema_version").forGetter(Raw::schemaVersion),
			UUID_CODEC.fieldOf("identity").forGetter(Raw::identity),
			Codec.intRange(0, MAX_CONDITION).fieldOf("current_condition").forGetter(Raw::currentCondition),
			Codec.intRange(1, MAX_CONDITION).fieldOf("max_condition").forGetter(Raw::maxCondition),
			Codec.intRange(0, MAX_CRAFTSMANSHIP_TIER).fieldOf("craftsmanship_tier")
					.forGetter(Raw::craftsmanshipTier),
			Identifier.CODEC.optionalFieldOf("active_path").forGetter(Raw::activePath),
			PATH_PROGRESS_CODEC.optionalFieldOf("path_progress", Map.of()).forGetter(Raw::pathProgress)
		).apply(instance, Raw::new));

	private static final Codec<EquipmentState> BASE_CODEC = RAW_CODEC.comapFlatMap(
			EquipmentState::fromRaw,
			state -> new Raw(
					state.schemaVersion(),
					state.identity(),
					state.currentCondition(),
					state.maxCondition(),
					state.craftsmanshipTier(),
					state.activePath(),
					state.pathProgress()));

	/** Persistent codec with explicit older-schema migration and future-schema rejection. */
	public static final Codec<EquipmentState> CODEC = SchemaVersioned.migrating(
			BASE_CODEC,
			EquipmentState::schemaVersion,
			CURRENT_SCHEMA,
			EquipmentState::migrate);

	/**
	 * Creates a new current-schema state with a fresh stable identity.
	 *
	 * @param maxCondition the configured maximum condition
	 * @param craftsmanshipTier the initial craftsmanship tier
	 */
	public static EquipmentState newItem(int maxCondition, int craftsmanshipTier) {
		return create(UUID.randomUUID(), maxCondition, craftsmanshipTier, Optional.empty(), Map.of());
	}

	/** Creates a current-schema state with an explicitly supplied identity. */
	public static EquipmentState create(
			UUID identity,
			int maxCondition,
			int craftsmanshipTier,
			Optional<Identifier> activePath,
			Map<Identifier, Integer> pathProgress) {
		return new EquipmentState(
				CURRENT_SCHEMA,
				identity,
				maxCondition,
				maxCondition,
				craftsmanshipTier,
				activePath,
				pathProgress);
	}

	public EquipmentState {
		if (schemaVersion < 1) {
			throw new IllegalArgumentException("equipment schema version must be at least 1");
		}
		identity = Objects.requireNonNull(identity, "identity");
		if (identity.equals(ZERO_IDENTITY)) {
			throw new IllegalArgumentException("equipment identity must not be the zero UUID");
		}
		if (maxCondition < 1 || maxCondition > MAX_CONDITION) {
			throw new IllegalArgumentException("equipment max condition must be between 1 and "
					+ MAX_CONDITION + ", got " + maxCondition);
		}
		if (currentCondition < 0 || currentCondition > maxCondition) {
			throw new IllegalArgumentException("equipment current condition must be between 0 and max condition, got "
					+ currentCondition + "/" + maxCondition);
		}
		if (craftsmanshipTier < 0 || craftsmanshipTier > MAX_CRAFTSMANSHIP_TIER) {
			throw new IllegalArgumentException("equipment craftsmanship tier must be between 0 and "
					+ MAX_CRAFTSMANSHIP_TIER + ", got " + craftsmanshipTier);
		}
		activePath = Objects.requireNonNull(activePath, "activePath");
		pathProgress = immutableProgress(pathProgress);
		if (activePath.isPresent() && !pathProgress.containsKey(activePath.get())) {
			throw new IllegalArgumentException("active equipment path must be present in path_progress");
		}
		if (activePath.isEmpty() && !pathProgress.isEmpty()) {
			throw new IllegalArgumentException("equipment with known paths must have one active path");
		}
	}

	/** Returns whether this item has no usable condition remaining. */
	public boolean isBroken() {
		return currentCondition == 0;
	}

	/** Returns the condition as a fraction in the inclusive range 0..1. */
	public double conditionFraction() {
		return (double) currentCondition / (double) maxCondition;
	}

	/** Returns the retained progress for a path, or zero when it has not been learned. */
	public int progressFor(Identifier path) {
		return pathProgress.getOrDefault(Objects.requireNonNull(path, "path"), 0);
	}

	/** Returns whether this item has retained progress for the given path. */
	public boolean knowsPath(Identifier path) {
		return pathProgress.containsKey(Objects.requireNonNull(path, "path"));
	}

	/** Returns a condition-clamped copy without changing maximum condition. */
	public EquipmentState withCondition(int requestedCondition) {
		int clamped = Math.max(0, Math.min(maxCondition, requestedCondition));
		return new EquipmentState(
				schemaVersion,
				identity,
				clamped,
				maxCondition,
				craftsmanshipTier,
				activePath,
				pathProgress);
	}

	/** Returns a copy with the requested path progress. */
	public EquipmentState withPathProgress(Identifier path, int progress) {
		Objects.requireNonNull(path, "path");
		if (!knowsPath(path)) {
			throw new IllegalArgumentException("cannot update progress for an unknown equipment path " + path);
		}
		if (progress < 0 || progress > MAX_PATH_PROGRESS) {
			throw new IllegalArgumentException("equipment path progress must be between 0 and "
					+ MAX_PATH_PROGRESS + ", got " + progress);
		}
		Map<Identifier, Integer> updated = new TreeMap<>(pathProgress);
		updated.put(path, progress);
		return new EquipmentState(
				schemaVersion,
				identity,
				currentCondition,
				maxCondition,
				craftsmanshipTier,
				activePath,
				updated);
	}

	/** Returns a copy that learns a path with zero prior progress. */
	public EquipmentState learnPath(Identifier path) {
		Objects.requireNonNull(path, "path");
		if (knowsPath(path)) {
			return this;
		}
		if (pathProgress.size() >= MAX_KNOWN_PATHS) {
			throw new IllegalArgumentException("equipment cannot retain more than " + MAX_KNOWN_PATHS + " paths");
		}
		Map<Identifier, Integer> updated = new TreeMap<>(pathProgress);
		updated.put(path, 0);
		Optional<Identifier> nextActive = activePath.isPresent() ? activePath : Optional.of(path);
		return new EquipmentState(
				schemaVersion,
				identity,
				currentCondition,
				maxCondition,
				craftsmanshipTier,
				nextActive,
				updated);
	}

	/** Returns a copy with a retained path selected as active and full condition. */
	public EquipmentState reforgedTo(Identifier targetPath) {
		Objects.requireNonNull(targetPath, "targetPath");
		if (!knowsPath(targetPath)) {
			throw new IllegalArgumentException("cannot activate unknown equipment path " + targetPath);
		}
		return new EquipmentState(
				schemaVersion,
				identity,
				maxCondition,
				maxCondition,
				craftsmanshipTier,
				Optional.of(targetPath),
				pathProgress);
	}

	/**
	 * Maps the state to vanilla's bounded broken marker without changing maximum durability.
	 *
	 * <p>The registered equipment item deliberately uses a one-point vanilla durability sentinel.
	 * Its visible bar is rendered from this component, while vanilla damage is kept at zero until
	 * the Forever condition reaches zero. This keeps {@link net.minecraft.world.item.ItemStack#isBroken()}
	 * true exactly for the broken state rather than for every partially worn stack.
	 */
	public int vanillaDamage(int vanillaMaxDamage) {
		if (vanillaMaxDamage < 1) {
			return 0;
		}
		return isBroken() ? vanillaMaxDamage : 0;
	}

	@Override
	public void addToTooltip(
			Item.TooltipContext context,
			Consumer<Component> textConsumer,
			TooltipFlag type,
			DataComponentGetter components) {
		Objects.requireNonNull(textConsumer, "textConsumer");
		if (isBroken()) {
			textConsumer.accept(Component.translatable("tooltip.forever.equipment.status.broken"));
			textConsumer.accept(Component.translatable("tooltip.forever.equipment.repair_hint"));
		} else {
			String statusKey = EquipmentBalanceRegistry.current()
					.map(balance -> balance.conditionStatusKey(conditionFraction()))
					.orElse("tooltip.forever.equipment.status.worn");
			textConsumer.accept(Component.translatable(
					"tooltip.forever.equipment.condition", currentCondition, maxCondition));
			textConsumer.accept(Component.translatable(statusKey));
		}
		activePath.ifPresent(path -> {
			String pathName = EquipmentBalanceRegistry.current()
					.flatMap(balance -> balance.path(path))
					.map(EquipmentBalance.PathDefinition::translationKey)
					.orElse("tooltip.forever.equipment.path.legacy_unavailable");
			textConsumer.accept(Component.translatable(
					"tooltip.forever.equipment.active_path", Component.translatable(pathName)));
		});
	}

	private static Map<Identifier, Integer> immutableProgress(Map<Identifier, Integer> progress) {
		Objects.requireNonNull(progress, "pathProgress");
		if (progress.size() > MAX_KNOWN_PATHS) {
			throw new IllegalArgumentException("equipment cannot retain more than " + MAX_KNOWN_PATHS + " paths");
		}
		TreeMap<Identifier, Integer> sorted = new TreeMap<>();
		progress.forEach((path, value) -> {
			if (path == null || value == null) {
				throw new IllegalArgumentException("equipment path_progress cannot contain null keys or values");
			}
			if (value < 0 || value > MAX_PATH_PROGRESS) {
				throw new IllegalArgumentException("equipment path progress must be between 0 and "
						+ MAX_PATH_PROGRESS + ", got " + value);
			}
			sorted.put(path, value);
		});
		return Collections.unmodifiableMap(sorted);
	}

	private static DataResult<EquipmentState> fromRaw(Raw raw) {
		if (raw.schemaVersion() < 1) {
			return DataResult.error(() -> "equipment schema version must be at least 1");
		}
		if (raw.identity() == null || raw.identity().equals(ZERO_IDENTITY)) {
			return DataResult.error(() -> "equipment identity must be a non-zero UUID");
		}
		if (raw.currentCondition() < 0 || raw.currentCondition() > raw.maxCondition()) {
			return DataResult.error(() -> "equipment current_condition must be between 0 and max_condition, got "
					+ raw.currentCondition() + "/" + raw.maxCondition());
		}
		if (raw.activePath().isPresent() && !raw.pathProgress().containsKey(raw.activePath().get())) {
			return DataResult.error(() -> "equipment active_path must be present in path_progress");
		}
		if (raw.activePath().isEmpty() && !raw.pathProgress().isEmpty()) {
			return DataResult.error(() -> "equipment with known paths must have one active_path");
		}
		try {
			return DataResult.success(new EquipmentState(
					raw.schemaVersion(),
					raw.identity(),
					raw.currentCondition(),
					raw.maxCondition(),
					raw.craftsmanshipTier(),
					raw.activePath(),
					raw.pathProgress()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(() -> "invalid equipment state: " + exception.getMessage());
		}
	}

	private static DataResult<EquipmentState> migrate(
			EquipmentState value,
			int storedVersion,
			int targetVersion) {
		if (storedVersion == 1 && targetVersion == CURRENT_SCHEMA) {
			return DataResult.success(new EquipmentState(
					CURRENT_SCHEMA,
					value.identity(),
					value.currentCondition(),
					value.maxCondition(),
					value.craftsmanshipTier(),
					value.activePath(),
					value.pathProgress()));
		}
		return DataResult.error(() -> "no equipment migration from schema " + storedVersion
				+ " to schema " + targetVersion);
	}
}

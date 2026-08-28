package dev.forever.core.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.data.SchemaVersioned;

/**
 * Data-driven settlement tuning. The JSON resource is the source of gameplay numbers.
 *
 * <p>{@link #ABSOLUTE_MAX_VALIDATION_VOLUME} is a safety invariant rather than a balance
 * knob. The normal cap remains data-defined, while no resource may authorize an unbounded
 * validation scan.
 */
public record SettlementBalance(
		int schemaVersion,
		double proximityAttachmentDistance,
		double outpostAttachmentDistance,
		int validationVolumeCap,
		int hardValidationVolumeCap,
		int minimumInteriorSpace,
		int minimumBeds,
		int minimumWorkstations,
		int minimumStorageSlots,
		int minimumLightLevel,
		int abstractVillagerBudgetPerTick,
		long migrationIntervalTicks,
		int migrationArrivalsPerInterval,
		int maximumImportBuildings) {

	public static final int CURRENT_SCHEMA = 1;
	public static final long ABSOLUTE_MAX_VALIDATION_VOLUME = 262_144L;

	private record Encoded(
			int schemaVersion,
			double proximityAttachmentDistance,
			double outpostAttachmentDistance,
			int validationVolumeCap,
			int hardValidationVolumeCap,
			int minimumInteriorSpace,
			int minimumBeds,
			int minimumWorkstations,
			int minimumStorageSlots,
			int minimumLightLevel,
			int abstractVillagerBudgetPerTick,
			long migrationIntervalTicks,
			int migrationArrivalsPerInterval,
			int maximumImportBuildings) {
	}

	private static final Codec<Encoded> RAW_ENCODED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("schema_version").forGetter(Encoded::schemaVersion),
			Codec.DOUBLE.fieldOf("proximity_attachment_distance").forGetter(Encoded::proximityAttachmentDistance),
			Codec.DOUBLE.fieldOf("outpost_attachment_distance").forGetter(Encoded::outpostAttachmentDistance),
			Codec.INT.fieldOf("validation_volume_cap").forGetter(Encoded::validationVolumeCap),
			Codec.INT.fieldOf("hard_validation_volume_cap").forGetter(Encoded::hardValidationVolumeCap),
			Codec.INT.fieldOf("minimum_interior_space").forGetter(Encoded::minimumInteriorSpace),
			Codec.INT.fieldOf("minimum_beds").forGetter(Encoded::minimumBeds),
			Codec.INT.fieldOf("minimum_workstations").forGetter(Encoded::minimumWorkstations),
			Codec.INT.fieldOf("minimum_storage_slots").forGetter(Encoded::minimumStorageSlots),
			Codec.INT.fieldOf("minimum_light_level").forGetter(Encoded::minimumLightLevel),
			Codec.INT.fieldOf("abstract_villager_budget_per_tick").forGetter(Encoded::abstractVillagerBudgetPerTick),
			Codec.LONG.fieldOf("migration_interval_ticks").forGetter(Encoded::migrationIntervalTicks),
			Codec.INT.fieldOf("migration_arrivals_per_interval").forGetter(Encoded::migrationArrivalsPerInterval),
			Codec.INT.fieldOf("maximum_import_buildings").forGetter(Encoded::maximumImportBuildings)
	).apply(instance, Encoded::new));

	private static final Codec<SettlementBalance> RAW_CODEC = RAW_ENCODED_CODEC.flatXmap(
			encoded -> create(
					encoded.schemaVersion(),
					encoded.proximityAttachmentDistance(),
					encoded.outpostAttachmentDistance(),
					encoded.validationVolumeCap(),
					encoded.hardValidationVolumeCap(),
					encoded.minimumInteriorSpace(),
					encoded.minimumBeds(),
					encoded.minimumWorkstations(),
					encoded.minimumStorageSlots(),
					encoded.minimumLightLevel(),
					encoded.abstractVillagerBudgetPerTick(),
					encoded.migrationIntervalTicks(),
					encoded.migrationArrivalsPerInterval(),
					encoded.maximumImportBuildings()),
			balance -> DataResult.success(new Encoded(
					balance.schemaVersion(),
					balance.proximityAttachmentDistance(),
					balance.outpostAttachmentDistance(),
					balance.validationVolumeCap(),
					balance.hardValidationVolumeCap(),
					balance.minimumInteriorSpace(),
					balance.minimumBeds(),
					balance.minimumWorkstations(),
					balance.minimumStorageSlots(),
					balance.minimumLightLevel(),
					balance.abstractVillagerBudgetPerTick(),
					balance.migrationIntervalTicks(),
					balance.migrationArrivalsPerInterval(),
					balance.maximumImportBuildings())));

	public static final Codec<SettlementBalance> CODEC = SchemaVersioned.migrating(
			RAW_CODEC,
			SettlementBalance::schemaVersion,
			CURRENT_SCHEMA,
			SchemaVersioned.noMigrationsYet("settlement balance"));

	public SettlementBalance {
		if (schemaVersion < 1) {
			throw new IllegalArgumentException("Settlement balance schema version must be at least 1.");
		}
		if (!Double.isFinite(proximityAttachmentDistance) || proximityAttachmentDistance <= 0.0) {
			throw new IllegalArgumentException("Settlement proximity attachment distance must be finite and positive.");
		}
		if (!Double.isFinite(outpostAttachmentDistance)
				|| outpostAttachmentDistance < proximityAttachmentDistance) {
			throw new IllegalArgumentException("Settlement outpost distance must be finite and at least the proximity distance.");
		}
		if (validationVolumeCap <= 0 || hardValidationVolumeCap < validationVolumeCap) {
			throw new IllegalArgumentException("Settlement validation caps must be positive and ordered.");
		}
		if (hardValidationVolumeCap > ABSOLUTE_MAX_VALIDATION_VOLUME) {
			throw new IllegalArgumentException("Settlement hard validation cap cannot exceed "
					+ ABSOLUTE_MAX_VALIDATION_VOLUME + " blocks.");
		}
		if (minimumInteriorSpace < 1 || minimumBeds < 1 || minimumWorkstations < 1
				|| minimumStorageSlots < 1 || minimumLightLevel < 0) {
			throw new IllegalArgumentException("Settlement residence thresholds are outside their valid ranges.");
		}
		if (abstractVillagerBudgetPerTick < 1 || migrationIntervalTicks < 1
				|| migrationArrivalsPerInterval < 1 || maximumImportBuildings < 1) {
			throw new IllegalArgumentException("Settlement migration and import limits must be positive.");
		}
	}

	static DataResult<SettlementBalance> create(
			int schemaVersion,
			double proximityAttachmentDistance,
			double outpostAttachmentDistance,
			int validationVolumeCap,
			int hardValidationVolumeCap,
			int minimumInteriorSpace,
			int minimumBeds,
			int minimumWorkstations,
			int minimumStorageSlots,
			int minimumLightLevel,
			int abstractVillagerBudgetPerTick,
			long migrationIntervalTicks,
			int migrationArrivalsPerInterval,
			int maximumImportBuildings) {
		try {
			return DataResult.success(new SettlementBalance(
					schemaVersion,
					proximityAttachmentDistance,
					outpostAttachmentDistance,
					validationVolumeCap,
					hardValidationVolumeCap,
					minimumInteriorSpace,
					minimumBeds,
					minimumWorkstations,
					minimumStorageSlots,
					minimumLightLevel,
					abstractVillagerBudgetPerTick,
					migrationIntervalTicks,
					migrationArrivalsPerInterval,
					maximumImportBuildings));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	static Codec<SettlementBalance> rawCodec() {
		return RAW_CODEC;
	}
}

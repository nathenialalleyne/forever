package dev.forever.core.storage.adapter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.data.SchemaVersioned;
import dev.forever.core.storage.domain.StorageSafetyLimits;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

/** Versioned world-scoped storage state for one dimension. */
public record WarehouseState(int schemaVersion, Map<UUID, WarehouseRecord> warehouses) {

	public static final int CURRENT_SCHEMA = 1;
	private static final Codec<Map<UUID, WarehouseRecord>> WAREHOUSES_CODEC = Codec.unboundedMap(
			StorageCodecs.UUID_CODEC, WarehouseRecord.CODEC).validate(warehouses ->
				warehouses.size() > StorageSafetyLimits.MAX_PERSISTED_WAREHOUSES
						? DataResult.error(() -> "storage state contains too many warehouses")
						: DataResult.success(warehouses));

	private record Raw(int schemaVersion, Map<UUID, WarehouseRecord> warehouses) {
	}

	private static final Codec<Raw> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("schema_version").forGetter(Raw::schemaVersion),
			WAREHOUSES_CODEC.fieldOf("warehouses").forGetter(Raw::warehouses)
		).apply(instance, Raw::new));

	private static final Codec<WarehouseState> BASE_CODEC = RAW_CODEC.comapFlatMap(
			WarehouseState::fromRaw,
			state -> new Raw(state.schemaVersion(), state.warehouses()));

	public static final Codec<WarehouseState> CODEC = SchemaVersioned.migrating(
			BASE_CODEC,
			WarehouseState::schemaVersion,
			CURRENT_SCHEMA,
			SchemaVersioned.noMigrationsYet("storage warehouse"));

	public WarehouseState {
		if (schemaVersion < 1) {
			throw new IllegalArgumentException("storage warehouse schema version must be at least 1");
		}
		Objects.requireNonNull(warehouses, "warehouses");
		if (warehouses.size() > StorageSafetyLimits.MAX_PERSISTED_WAREHOUSES) {
			throw new IllegalArgumentException("storage state contains too many warehouses");
		}
		TreeMap<UUID, WarehouseRecord> sorted = new TreeMap<>((left, right) ->
				left.toString().compareTo(right.toString()));
		for (Map.Entry<UUID, WarehouseRecord> entry : warehouses.entrySet()) {
			if (entry.getKey() == null || entry.getValue() == null) {
				throw new IllegalArgumentException("storage state cannot contain null warehouse IDs or records");
			}
			if (!entry.getKey().equals(entry.getValue().id())) {
				throw new IllegalArgumentException("storage warehouse map key does not match its record ID");
			}
			sorted.put(entry.getKey(), entry.getValue());
		}
		warehouses = Map.copyOf(sorted);
	}

	public static WarehouseState empty() {
		return new WarehouseState(CURRENT_SCHEMA, Map.of());
	}

	public WarehouseState with(WarehouseRecord warehouse) {
		Objects.requireNonNull(warehouse, "warehouse");
		TreeMap<UUID, WarehouseRecord> next = new TreeMap<>((left, right) ->
				left.toString().compareTo(right.toString()));
		next.putAll(warehouses);
		next.put(warehouse.id(), warehouse);
		return new WarehouseState(schemaVersion, next);
	}

	public WarehouseState without(UUID id) {
		Objects.requireNonNull(id, "id");
		TreeMap<UUID, WarehouseRecord> next = new TreeMap<>((left, right) ->
				left.toString().compareTo(right.toString()));
		next.putAll(warehouses);
		next.remove(id);
		return new WarehouseState(schemaVersion, next);
	}

	private static DataResult<WarehouseState> fromRaw(Raw raw) {
		try {
			return DataResult.success(new WarehouseState(raw.schemaVersion(), raw.warehouses()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}
}

package dev.forever.core.storage.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.data.SchemaVersioned;

/** Immutable, validated balance values for the storage systems. */
public record StorageBalance(
		int schemaVersion,
		int initialCacheSlots,
		int expandedCacheSlots,
		int maxWarehouses,
		int maxRegisteredContainers,
		int maxIndexedSlots,
		int maxQueryResults,
		int maxQueryTextLength,
		int maxQueryFacets,
		int warehouseRegistrationRange,
		int maxRebuildContainers,
		int maxRebuildSlots,
		int maxNetworkPayloadBytes) {

	public static final int CURRENT_SCHEMA = 1;

	private record Raw(
			int schemaVersion,
			int initialCacheSlots,
			int expandedCacheSlots,
			int maxWarehouses,
			int maxRegisteredContainers,
			int maxIndexedSlots,
			int maxQueryResults,
			int maxQueryTextLength,
			int maxQueryFacets,
			int warehouseRegistrationRange,
			int maxRebuildContainers,
			int maxRebuildSlots,
			int maxNetworkPayloadBytes) {
	}

	private static final Codec<Raw> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("schema_version").forGetter(Raw::schemaVersion),
			Codec.INT.fieldOf("initial_cache_slots").forGetter(Raw::initialCacheSlots),
			Codec.INT.fieldOf("expanded_cache_slots").forGetter(Raw::expandedCacheSlots),
			Codec.INT.fieldOf("max_warehouses").forGetter(Raw::maxWarehouses),
			Codec.INT.fieldOf("max_registered_containers").forGetter(Raw::maxRegisteredContainers),
			Codec.INT.fieldOf("max_indexed_slots").forGetter(Raw::maxIndexedSlots),
			Codec.INT.fieldOf("max_query_results").forGetter(Raw::maxQueryResults),
			Codec.INT.fieldOf("max_query_text_length").forGetter(Raw::maxQueryTextLength),
			Codec.INT.fieldOf("max_query_facets").forGetter(Raw::maxQueryFacets),
			Codec.INT.fieldOf("warehouse_registration_range").forGetter(Raw::warehouseRegistrationRange),
			Codec.INT.fieldOf("max_rebuild_containers").forGetter(Raw::maxRebuildContainers),
			Codec.INT.fieldOf("max_rebuild_slots").forGetter(Raw::maxRebuildSlots),
			Codec.INT.fieldOf("max_network_payload_bytes").forGetter(Raw::maxNetworkPayloadBytes)
		).apply(instance, Raw::new));

	private static final Codec<StorageBalance> BASE_CODEC = RAW_CODEC.comapFlatMap(
			StorageBalance::fromRaw,
			balance -> new Raw(
					balance.schemaVersion(),
					balance.initialCacheSlots(),
					balance.expandedCacheSlots(),
					balance.maxWarehouses(),
					balance.maxRegisteredContainers(),
					balance.maxIndexedSlots(),
					balance.maxQueryResults(),
					balance.maxQueryTextLength(),
					balance.maxQueryFacets(),
					balance.warehouseRegistrationRange(),
					balance.maxRebuildContainers(),
					balance.maxRebuildSlots(),
					balance.maxNetworkPayloadBytes()));

	/** Codec for data-pack values. It never yields an unchecked balance. */
	public static final Codec<StorageBalance> CODEC = SchemaVersioned.migrating(
			BASE_CODEC,
			StorageBalance::schemaVersion,
			CURRENT_SCHEMA,
			SchemaVersioned.noMigrationsYet("storage balance"));

	public StorageBalance {
		if (schemaVersion < 1) {
			throw new IllegalArgumentException("storage balance schema version must be at least 1");
		}
		if (initialCacheSlots < 1 || initialCacheSlots > StorageSafetyLimits.MAX_PERSISTED_CACHE_SLOTS) {
			throw new IllegalArgumentException("storage initial_cache_slots is outside the supported range");
		}
		if (expandedCacheSlots < initialCacheSlots
				|| expandedCacheSlots > StorageSafetyLimits.MAX_PERSISTED_CACHE_SLOTS) {
			throw new IllegalArgumentException(
					"storage expanded_cache_slots must be at least the initial capacity and within the safety limit");
		}
		if (maxWarehouses < 1 || maxWarehouses > StorageSafetyLimits.MAX_PERSISTED_WAREHOUSES) {
			throw new IllegalArgumentException("storage max_warehouses is outside the supported range");
		}
		if (maxRegisteredContainers < 1 || maxRegisteredContainers > StorageSafetyLimits.MAX_PERSISTED_CONTAINERS) {
			throw new IllegalArgumentException("storage max_registered_containers is outside the supported range");
		}
		if (maxIndexedSlots < 1 || maxIndexedSlots > StorageSafetyLimits.MAX_PERSISTED_INDEX_ENTRIES) {
			throw new IllegalArgumentException("storage max_indexed_slots is outside the supported range");
		}
		if (maxQueryResults < 1 || maxQueryResults > StorageSafetyLimits.MAX_QUERY_RESULTS) {
			throw new IllegalArgumentException("storage max_query_results must be between 1 and 200");
		}
		if (maxQueryTextLength < 1 || maxQueryTextLength > StorageSafetyLimits.MAX_QUERY_TEXT_LENGTH) {
			throw new IllegalArgumentException("storage max_query_text_length must be between 1 and 128");
		}
		if (maxQueryFacets < 1 || maxQueryFacets > StorageSafetyLimits.MAX_QUERY_FACETS) {
			throw new IllegalArgumentException("storage max_query_facets must be between 1 and 16");
		}
		if (warehouseRegistrationRange < 1) {
			throw new IllegalArgumentException("storage warehouse_registration_range must be positive");
		}
		if (maxRebuildContainers < 1 || maxRebuildContainers > StorageSafetyLimits.MAX_REBUILD_CONTAINERS) {
			throw new IllegalArgumentException("storage max_rebuild_containers is outside the supported range");
		}
		if (maxRebuildSlots < 1 || maxRebuildSlots > StorageSafetyLimits.MAX_REBUILD_SLOTS) {
			throw new IllegalArgumentException("storage max_rebuild_slots is outside the supported range");
		}
		if (maxNetworkPayloadBytes < 1 || maxNetworkPayloadBytes > StorageSafetyLimits.MAX_NETWORK_PAYLOAD_BYTES) {
			throw new IllegalArgumentException("storage max_network_payload_bytes is outside the supported range");
		}
	}

	private static DataResult<StorageBalance> fromRaw(Raw raw) {
		try {
			return DataResult.success(new StorageBalance(
					raw.schemaVersion(),
					raw.initialCacheSlots(),
					raw.expandedCacheSlots(),
					raw.maxWarehouses(),
					raw.maxRegisteredContainers(),
					raw.maxIndexedSlots(),
					raw.maxQueryResults(),
					raw.maxQueryTextLength(),
					raw.maxQueryFacets(),
					raw.warehouseRegistrationRange(),
					raw.maxRebuildContainers(),
					raw.maxRebuildSlots(),
					raw.maxNetworkPayloadBytes()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}
}

package dev.forever.core.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StorageCodecTest {

	private static final ResourceKey<Level> TEST_DIMENSION = ResourceKey.create(
			Registries.DIMENSION, Identifier.parse("forever:test_dimension"));

	@Test
	@DisplayName("storage balance codec round-trips the configured limits")
	void balanceRoundTrip() {
		StorageBalance original = balance();

		JsonElement encoded = StorageBalance.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
		StorageBalance decoded = StorageBalance.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

		assertEquals(original, decoded);
	}

	@Test
	@DisplayName("Traveler's Cache schema one migrates to the current schema")
	void cacheSchemaMigrates() {
		TravelerCacheContents old = new TravelerCacheContents(
				1, false, emptySlots(9));
		JsonElement encoded = TravelerCacheContents.rawCodec().encodeStart(JsonOps.INSTANCE, old).getOrThrow();

		TravelerCacheContents migrated = TravelerCacheContents.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

		assertEquals(TravelerCacheContents.CURRENT_SCHEMA, migrated.schemaVersion());
		assertEquals(false, migrated.expanded());
		assertEquals(old.slots(), migrated.slots());
	}

	@Test
	@DisplayName("future storage schemas are rejected instead of coerced")
	void futureSchemaIsRejected() {
		TravelerCacheContents future = new TravelerCacheContents(
				TravelerCacheContents.CURRENT_SCHEMA + 1, false, emptySlots(9));
		JsonElement encoded = TravelerCacheContents.rawCodec().encodeStart(JsonOps.INSTANCE, future).getOrThrow();

		DataResult<TravelerCacheContents> result = TravelerCacheContents.CODEC.parse(JsonOps.INSTANCE, encoded);

		assertTrue(result.error().isPresent());
		assertTrue(result.error().orElseThrow().message().contains("restore a backup"));
	}

	@Test
	@DisplayName("warehouse SavedData state codec round-trips its dimension-scoped model")
	void warehouseStateRoundTrip() {
		UUID id = UUID.fromString("00000000-0000-0000-0000-000000000601");
		WarehouseRecord warehouse = WarehouseRecord.empty(id, TEST_DIMENSION, new BlockPos(4, 70, 8));
		WarehouseState original = new WarehouseState(
				WarehouseState.CURRENT_SCHEMA, Map.of(id, warehouse));

		JsonElement encoded = WarehouseState.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
		WarehouseState decoded = WarehouseState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

		assertEquals(original, decoded);
		assertEquals(TEST_DIMENSION, decoded.warehouses().get(id).dimension());
	}

	private static StorageBalance balance() {
		return new StorageBalance(1, 9, 18, 16, 32, 128, 5, 128, 4, 64, 4, 32, 65_536);
	}

	private static List<net.minecraft.world.item.ItemStack> emptySlots(int size) {
		return java.util.stream.IntStream.range(0, size)
				.mapToObj(ignored -> net.minecraft.world.item.ItemStack.EMPTY)
				.toList();
	}
}

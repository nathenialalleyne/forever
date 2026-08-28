package dev.forever.core.equipment.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.forever.core.equipment.EquipmentBalance;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class EquipmentBalanceTest {

	@Test
	void bundledBalanceRoundTripsThroughItsCodec() throws IOException {
		try (var stream = getClass().getClassLoader().getResourceAsStream(
				"data/forever/equipment/balance.json")) {
			assertTrue(stream != null, "bundled balance resource must be present");
			JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
			EquipmentBalance balance = EquipmentBalance.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
			JsonElement encoded = EquipmentBalance.CODEC.encodeStart(JsonOps.INSTANCE, balance).getOrThrow();
			EquipmentBalance decoded = EquipmentBalance.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

			assertEquals(balance, decoded);
			assertEquals(3, decoded.paths().size());
			assertEquals(100, decoded.maxCondition());
		}
	}

	@Test
	void malformedThresholdsAreRejected() {
		JsonElement malformed = JsonParser.parseString("""
				{
				  \"schema_version\": 1,
				  \"max_condition\": 100,
				  \"field_repair_fraction\": 0.25,
				  \"sound_threshold\": 0.25,
				  \"worn_threshold\": 0.75,
				  \"field_repair_cost\": {\"item\": \"minecraft:iron_ingot\", \"count\": 1},
				  \"workshop_repair_cost\": {\"item\": \"minecraft:iron_block\", \"count\": 1},
				  \"reforge_cost\": {\"item\": \"minecraft:diamond\", \"count\": 1},
				  \"craftsmanship_multipliers\": {\"0\": 1.0},
				  \"paths\": {
				    \"forever:prospector\": {
				      \"translation_key\": \"equipment.path.prospector\",
				      \"max_progress\": 1000,
				      \"condition_multiplier\": 1.0
				    }
				  }
				}
				""");

		var result = EquipmentBalance.CODEC.parse(JsonOps.INSTANCE, malformed);

		assertTrue(result.error().isPresent());
		assertTrue(result.error().get().message().contains("worn_threshold"));
	}

	@Test
	void futureBalanceSchemaIsRejected() throws IOException {
		try (var stream = getClass().getClassLoader().getResourceAsStream(
				"data/forever/equipment/balance.json")) {
			assertTrue(stream != null, "bundled balance resource must be present");
			JsonObject future = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
					.getAsJsonObject();
			future.addProperty("schema_version", EquipmentBalance.CURRENT_SCHEMA + 1);

			var result = EquipmentBalance.CODEC.parse(JsonOps.INSTANCE, future);

			assertTrue(result.error().isPresent());
			assertTrue(result.error().get().message().contains("schema version"));
		}
	}
}

package dev.forever.core.equipment.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class EquipmentStateTest {

	private static final Identifier PROSPECTOR = Identifier.parse("forever:prospector");
	private static final Identifier EXCAVATOR = Identifier.parse("forever:excavator");

	@Test
	void codecRoundTripPreservesIdentityConditionAndPaths() {
		UUID identity = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
		EquipmentState state = EquipmentState.create(
				identity,
				100,
				1,
				Optional.of(PROSPECTOR),
				Map.of(PROSPECTOR, 17, EXCAVATOR, 42));

		JsonElement encoded = EquipmentState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow();
		EquipmentState decoded = EquipmentState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

		assertEquals(state, decoded);
		assertTrue(decoded.knowsPath(EXCAVATOR));
	}

	@Test
	void schemaOneMigratesToCurrentSchema() {
		JsonObject old = new JsonObject();
		old.addProperty("schema_version", 1);
		old.addProperty("identity", "123e4567-e89b-12d3-a456-426614174000");
		old.addProperty("current_condition", 25);
		old.addProperty("max_condition", 100);
		old.addProperty("craftsmanship_tier", 0);
		old.addProperty("active_path", PROSPECTOR.toString());
		JsonObject progress = new JsonObject();
		progress.addProperty(PROSPECTOR.toString(), 9);
		old.add("path_progress", progress);

		EquipmentState migrated = EquipmentState.CODEC.parse(JsonOps.INSTANCE, old).getOrThrow();

		assertEquals(EquipmentState.CURRENT_SCHEMA, migrated.schemaVersion());
		assertEquals(25, migrated.currentCondition());
		assertEquals(9, migrated.progressFor(PROSPECTOR));
	}

	@Test
	void futureSchemaIsRejectedWithActionableMessage() {
		JsonObject future = JsonParser.parseString("""
				{
				  \"schema_version\": 3,
				  \"identity\": \"123e4567-e89b-12d3-a456-426614174000\",
				  \"current_condition\": 100,
				  \"max_condition\": 100,
				  \"craftsmanship_tier\": 0,
				  \"path_progress\": {}
				}
				""").getAsJsonObject();

		var result = EquipmentState.CODEC.parse(JsonOps.INSTANCE, future);

		assertTrue(result.error().isPresent());
		assertTrue(result.error().get().message().contains("schema version 3"));
	}

	@Test
	void malformedConditionIsRejected() {
		JsonObject malformed = JsonParser.parseString("""
				{
				  \"schema_version\": 2,
				  \"identity\": \"123e4567-e89b-12d3-a456-426614174000\",
				  \"current_condition\": 101,
				  \"max_condition\": 100,
				  \"craftsmanship_tier\": 0,
				  \"path_progress\": {}
				}
				""").getAsJsonObject();

		var result = EquipmentState.CODEC.parse(JsonOps.INSTANCE, malformed);

		assertTrue(result.error().isPresent());
		assertTrue(result.error().get().message().contains("current_condition"));
	}

	@Test
	void conditionUpdatesClampAtBothBoundaries() {
		EquipmentState state = EquipmentState.create(
				UUID.randomUUID(), 100, 0, Optional.empty(), Map.of());

		assertEquals(0, state.withCondition(-50).currentCondition());
		assertEquals(100, state.withCondition(500).currentCondition());
		assertFalse(state.isBroken());
		assertTrue(state.withCondition(0).isBroken());
	}

	@Test
	void vanillaDamageOnlyMarksTheBrokenState() {
		EquipmentState state = EquipmentState.create(
				UUID.randomUUID(), 100, 0, Optional.empty(), Map.of());

		assertEquals(0, state.withCondition(99).vanillaDamage(1));
		assertEquals(1, state.withCondition(0).vanillaDamage(1));
	}

	@Test
	void reforgePreservesIdentityAndInactivePathProgress() {
		UUID identity = UUID.randomUUID();
		EquipmentState state = EquipmentState.create(
				identity,
				100,
				0,
				Optional.of(PROSPECTOR),
				Map.of(PROSPECTOR, 13, EXCAVATOR, 71)).withCondition(0);

		EquipmentState reforged = state.reforgedTo(EXCAVATOR);

		assertEquals(identity, reforged.identity());
		assertEquals(100, reforged.currentCondition());
		assertEquals(Optional.of(EXCAVATOR), reforged.activePath());
		assertEquals(13, reforged.progressFor(PROSPECTOR));
		assertEquals(71, reforged.progressFor(EXCAVATOR));
	}
}

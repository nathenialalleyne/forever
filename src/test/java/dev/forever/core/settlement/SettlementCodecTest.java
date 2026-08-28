package dev.forever.core.settlement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SettlementCodecTest {

	@Test
	@DisplayName("world-scoped settlement state codec round-trips the graph and migration records")
	void stateCodecRoundTrip() {
		Settlement root = SettlementTestFixtures.charter();
		RegisteredBuilding first = SettlementTestFixtures.building(
				"00000000-0000-0000-0000-000000000101", 0, Set.of(SettlementRole.RESIDENCE));
		RegisteredBuilding second = SettlementTestFixtures.building(
				"00000000-0000-0000-0000-000000000102", 20, Set.of(SettlementRole.WORKPLACE));
		root = SettlementGraph.attach(root, first, SettlementTestFixtures.BALANCE).settlement();
		root = SettlementGraph.attach(root, second, SettlementTestFixtures.BALANCE).settlement();
		MigrationRecord migration = MigrationService.offer(
				UUID.fromString("00000000-0000-0000-0000-000000000201"),
				Optional.empty(), "river valley", "mason", 0L);
		root = root.withMigration(migration);
		SettlementState original = SettlementState.empty().withSettlement(root);

		JsonElement encoded = SettlementState.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
		SettlementState decoded = SettlementState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

		assertEquals(original, decoded);
	}

	@Test
	@DisplayName("schema one settlement state migrates deterministically to schema two")
	void priorSchemaMigrates() {
		SettlementState old = new SettlementState(1, Map.of(SettlementTestFixtures.charter().id(),
				SettlementTestFixtures.charter()));
		JsonElement encoded = SettlementState.rawCodec().encodeStart(JsonOps.INSTANCE, old).getOrThrow();

		SettlementState migrated = SettlementState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

		assertEquals(SettlementState.CURRENT_SCHEMA, migrated.schemaVersion());
		assertEquals(old.settlements(), migrated.settlements());
	}

	@Test
	@DisplayName("a newer settlement state schema is rejected with backup guidance")
	void futureSchemaIsRejected() {
		SettlementState future = new SettlementState(
				SettlementState.CURRENT_SCHEMA + 1, Map.of(SettlementTestFixtures.charter().id(),
						SettlementTestFixtures.charter()));
		JsonElement encoded = SettlementState.rawCodec().encodeStart(JsonOps.INSTANCE, future).getOrThrow();

		DataResult<SettlementState> result = SettlementState.CODEC.parse(JsonOps.INSTANCE, encoded);

		assertTrue(result.error().isPresent());
		assertTrue(result.error().orElseThrow().message().contains("restore a backup"));
	}

	@Test
	@DisplayName("invalid Charter names fail before they can enter world state")
	void invalidCharterIsRejected() {
		org.junit.jupiter.api.Assertions.assertThrows(
				IllegalArgumentException.class,
				() -> Settlement.found(UUID.randomUUID(), "  ", UUID.randomUUID(),
						SettlementTestFixtures.OVERWORLD, net.minecraft.core.BlockPos.ZERO));
	}
}

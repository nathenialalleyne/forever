package dev.forever.core.career;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CareerCodecTest {

	private static final Identifier TECHNIQUE = MasonIds.STONE_CUTTING;
	private static final Identifier SECOND_TECHNIQUE = MasonIds.STRUCTURAL_FINISH;
	private static final UUID HOME = UUID.fromString("00000000-0000-0000-0000-000000000101");
	private static final UUID WORKPLACE = UUID.fromString("00000000-0000-0000-0000-000000000102");

	@Test
	@DisplayName("career attachment codec round-trips all persistent fields")
	void roundTrip() {
		CareerState original = new CareerState(
				CareerState.CURRENT_SCHEMA,
				Optional.of(MasonIds.MASON),
				MasonRank.JOURNEYMAN,
				Set.of(TECHNIQUE, SECOND_TECHNIQUE),
				Set.of(TECHNIQUE),
				Set.of(TECHNIQUE),
				Set.of(MasonIds.VALIDATED_WORKSHOP),
				Optional.of(HOME),
				Optional.of(WORKPLACE),
				Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000103")),
				Set.of(UUID.fromString("00000000-0000-0000-0000-000000000104")),
				CareerStatus.ACTIVE,
				Optional.empty());

		JsonElement encoded = CareerState.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
		CareerState decoded = CareerState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

		assertEquals(original, decoded);
	}

	@Test
	@DisplayName("schema one career data migrates without inventing evidence")
	void schemaOneMigrates() {
		CareerState old = new CareerState(
				1,
				Optional.of(MasonIds.MASON),
				MasonRank.APPRENTICE,
				Set.of(TECHNIQUE),
				Optional.of(HOME),
				Optional.of(WORKPLACE),
				Optional.empty(),
				Set.of());
		JsonElement encoded = CareerState.rawCodec().encodeStart(JsonOps.INSTANCE, old).getOrThrow();

		CareerState migrated = CareerState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

		assertEquals(CareerState.CURRENT_SCHEMA, migrated.schemaVersion());
		assertEquals(old.knownTechniques(), migrated.knownTechniques());
		assertTrue(migrated.demonstratedTechniques().isEmpty());
		assertTrue(migrated.taughtTechniques().isEmpty());
		assertTrue(migrated.validatedProjects().isEmpty());
	}

	@Test
	@DisplayName("a future career schema is rejected with recovery guidance")
	void futureSchemaRejected() {
		CareerState future = new CareerState(
				CareerState.CURRENT_SCHEMA + 1,
				Optional.of(MasonIds.MASON),
				MasonRank.APPRENTICE,
				Set.of(),
				Set.of(),
				Set.of(),
				Set.of(),
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Set.of(),
				CareerStatus.ACTIVE,
				Optional.empty());
		JsonElement encoded = CareerState.rawCodec().encodeStart(JsonOps.INSTANCE, future).getOrThrow();

		DataResult<CareerState> result = CareerState.CODEC.parse(JsonOps.INSTANCE, encoded);

		assertTrue(result.error().isPresent());
		assertTrue(result.error().orElseThrow().message().contains("restore a backup"));
	}
}

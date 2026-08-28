package dev.forever.core.mastery.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.forever.core.mastery.domain.LoadoutChange;
import dev.forever.core.mastery.domain.MasteryProgress;
import dev.forever.core.mastery.domain.SwitchContext;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MasteryCodecTest {

	private static final Identifier BUILDER = MasteryIds.BUILDER;

	@Test
	@DisplayName("mastery state codec round-trips all permanent fields")
	void stateCodecRoundTrip() {
		MasteryLoadout loadout = new MasteryLoadout(
				Optional.of(BUILDER), ListSupport.of(MasteryIds.PROSPECTOR, MasteryIds.COOK));
		MasteryProgress progress = new MasteryProgress(
				3,
				Set.of("ore_variety"),
				Set.of("variety:ore_type:minecraft:iron_ore"),
				Set.of("ore-iron"));
		MasteryState original = new MasteryState(
				MasteryState.CURRENT_SCHEMA,
				Map.of(MasteryIds.PROSPECTOR, progress),
				loadout,
				Optional.of(new LoadoutChange(SwitchContext.REST, 42L)));

		JsonElement encoded = MasteryState.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
		MasteryState decoded = MasteryState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

		assertEquals(original, decoded);
	}

	@Test
	@DisplayName("schema one state migrates to the current schema")
	void priorSchemaMigrates() {
		MasteryState old = new MasteryState(
				1,
				Map.of(MasteryIds.PROSPECTOR, MasteryProgress.empty()),
				new MasteryLoadout(Optional.of(BUILDER), ListSupport.empty()),
				Optional.empty());
		JsonElement encoded = MasteryState.rawCodec().encodeStart(JsonOps.INSTANCE, old).getOrThrow();

		MasteryState migrated = MasteryState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

		assertEquals(MasteryState.CURRENT_SCHEMA, migrated.schemaVersion());
		assertEquals(old.progress(), migrated.progress());
		assertEquals(old.loadout(), migrated.loadout());
	}

	@Test
	@DisplayName("newer state schema is rejected with recovery guidance")
	void futureSchemaIsRejected() {
		MasteryState future = new MasteryState(
				MasteryState.CURRENT_SCHEMA + 1,
				Map.of(),
				MasteryLoadout.empty(),
				Optional.empty());
		JsonElement encoded = MasteryState.rawCodec().encodeStart(JsonOps.INSTANCE, future).getOrThrow();

		DataResult<MasteryState> result = MasteryState.CODEC.parse(JsonOps.INSTANCE, encoded);

		assertTrue(result.error().isPresent());
		assertTrue(result.error().orElseThrow().message().contains("restore a backup"));
	}

	@Test
	@DisplayName("duplicate evidence entries are refused rather than collapsed silently")
	void duplicateEvidenceIsRejected() {
		JsonElement encoded = JsonParser.parseString("""
				{"rank":0,"unlocked_milestones":[],"evidence_keys":["variety:ore_type:iron","variety:ore_type:iron"],"event_ids":[]}
				""");

		DataResult<MasteryProgress> result = MasteryProgress.CODEC.parse(JsonOps.INSTANCE, encoded);

		assertTrue(result.error().isPresent());
		assertTrue(result.error().orElseThrow().message().contains("duplicate normalized evidence key"));
	}

	/** Keeps list construction readable without adding another production abstraction. */
	private static final class ListSupport {
		private ListSupport() {
		}

		static java.util.List<Identifier> of(Identifier... ids) {
			return java.util.List.of(ids);
		}

		static java.util.List<Identifier> empty() {
			return java.util.List.of();
		}
	}
}

package dev.forever.core.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.google.gson.JsonElement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for the schema-versioning helper.
 *
 * <p>These matter more than they look. Every persistent Forever system routes through
 * this class, so a defect here is a world-corruption defect. Principle 18 puts the save
 * above any feature, and principle 14 requires versioning with a migration path.
 */
class SchemaVersionedTest {

	/** Minimal versioned record standing in for a real system's state. */
	private record Sample(int schemaVersion, String value) {
		static final int CURRENT_SCHEMA = 2;

		static final Codec<Sample> RAW = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("schema_version").forGetter(Sample::schemaVersion),
				Codec.STRING.fieldOf("value").forGetter(Sample::value)
		).apply(instance, Sample::new));
	}

	/** Upgrades v1 to v2 by prefixing the value, so the migration is observable. */
	private static final SchemaVersioned.Migration<Sample> MIGRATION =
			(value, stored, target) -> {
				if (stored == 1) {
					return DataResult.success(new Sample(target, "migrated:" + value.value()));
				}
				return DataResult.error(() -> "no migration from " + stored);
			};

	private static Codec<Sample> codec() {
		return SchemaVersioned.migrating(
				Sample.RAW, Sample::schemaVersion, Sample.CURRENT_SCHEMA, MIGRATION);
	}

	private static DataResult<Sample> decode(int schemaVersion, String value) {
		JsonElement json = Sample.RAW
				.encodeStart(JsonOps.INSTANCE, new Sample(schemaVersion, value))
				.getOrThrow();
		return codec().parse(JsonOps.INSTANCE, json);
	}

	@Test
	@DisplayName("current-schema data decodes unchanged")
	void currentSchemaPassesThrough() {
		Sample result = decode(Sample.CURRENT_SCHEMA, "intact").getOrThrow();

		assertEquals("intact", result.value());
		assertEquals(Sample.CURRENT_SCHEMA, result.schemaVersion());
	}

	@Test
	@DisplayName("older data is migrated before the game sees it")
	void olderSchemaIsMigrated() {
		Sample result = decode(1, "old").getOrThrow();

		assertEquals("migrated:old", result.value(),
				"Migration must run so in-memory code only handles the current schema.");
		assertEquals(Sample.CURRENT_SCHEMA, result.schemaVersion());
	}

	@Test
	@DisplayName("data from a newer schema is refused, not coerced")
	void newerSchemaIsRefused() {
		DataResult<Sample> result = decode(Sample.CURRENT_SCHEMA + 1, "future");

		assertTrue(result.error().isPresent(),
				"Loading a newer save with an older build must fail loudly, not guess.");
		assertTrue(result.error().orElseThrow().message().contains("restore a backup"),
				"The error must tell the player how to recover their world.");
	}

	@Test
	@DisplayName("a nonsensical schema version is rejected")
	void invalidSchemaIsRejected() {
		assertTrue(decode(0, "bad").error().isPresent());
		assertTrue(decode(-5, "bad").error().isPresent());
	}

	@Test
	@DisplayName("a missing migration reports the bug instead of losing data")
	void missingMigrationIsReported() {
		Codec<Sample> strict = SchemaVersioned.migrating(
				Sample.RAW,
				Sample::schemaVersion,
				Sample.CURRENT_SCHEMA,
				SchemaVersioned.noMigrationsYet("sample"));

		JsonElement json = Sample.RAW
				.encodeStart(JsonOps.INSTANCE, new Sample(1, "orphan"))
				.getOrThrow();
		DataResult<Sample> result = strict.parse(JsonOps.INSTANCE, json);

		assertTrue(result.error().isPresent());
		assertTrue(result.error().orElseThrow().message().contains("no migration"),
				"A bumped schema without a migration must be an obvious, named failure.");
	}

	@Test
	@DisplayName("round-tripping preserves the value")
	void roundTripIsStable() {
		Sample original = new Sample(Sample.CURRENT_SCHEMA, "payload");

		JsonElement encoded = codec().encodeStart(JsonOps.INSTANCE, original).getOrThrow();
		Sample decoded = codec().parse(JsonOps.INSTANCE, encoded).getOrThrow();

		assertEquals(original, decoded);
	}
}

package dev.forever.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.function.Function;

/**
 * Helpers for building codecs over schema-versioned persistent records.
 *
 * <p>Design principle 14 requires every persistent format to carry an explicit schema
 * version and a migration path. This class exists so that requirement is satisfied by
 * construction rather than by each system remembering to do it.
 *
 * <p>The shape of a versioned record is:
 *
 * <pre>{@code
 * record MyState(int schemaVersion, ...) {
 *     static final int CURRENT_SCHEMA = 1;
 * }
 * }</pre>
 *
 * <p>The stored form always includes the version field. On load, {@link #migrating}
 * routes the decoded value through a migration function before the rest of the game
 * ever sees it, so in-memory code only deals with the current schema.
 *
 * <p>Failure behaviour is deliberate: a record from a <em>newer</em> schema than this
 * build understands is rejected rather than silently coerced. Loading a future save
 * with an old jar is exactly the situation where guessing corrupts a world, and
 * principle 18 says the save outranks the feature.
 */
public final class SchemaVersioned {

	private SchemaVersioned() {
	}

	/**
	 * Wraps a codec so that decoded values pass through a migration step, and values
	 * from an unknown future schema are rejected with an actionable message.
	 *
	 * @param base           codec for the stored representation
	 * @param versionOf      extracts the schema version from a decoded value
	 * @param currentVersion the newest schema this build understands
	 * @param migrate        upgrades an older value to the current schema
	 * @param <T>            record type
	 * @return a codec that only ever yields current-schema values
	 */
	public static <T> Codec<T> migrating(
			Codec<T> base,
			Function<T, Integer> versionOf,
			int currentVersion,
			Migration<T> migrate) {
		return base.flatXmap(
				value -> upgrade(value, versionOf, currentVersion, migrate),
				DataResult::success);
	}

	private static <T> DataResult<T> upgrade(
			T value,
			Function<T, Integer> versionOf,
			int currentVersion,
			Migration<T> migrate) {
		int stored = versionOf.apply(value);

		if (stored == currentVersion) {
			return DataResult.success(value);
		}

		if (stored > currentVersion) {
			// Refuse rather than guess. Silently downgrading a newer save is how worlds
			// lose data permanently.
			return DataResult.error(() -> "Forever data has schema version " + stored
					+ " but this build only understands up to " + currentVersion
					+ ". Update the mod, or restore a backup taken before the upgrade.");
		}

		if (stored < 1) {
			return DataResult.error(() -> "Forever data has an invalid schema version "
					+ stored + "; expected 1 or greater.");
		}

		return migrate.migrate(value, stored, currentVersion);
	}

	/** Upgrades an older persisted value to the current schema. */
	@FunctionalInterface
	public interface Migration<T> {
		/**
		 * @param value         the decoded older value
		 * @param storedVersion the schema version actually found on disk
		 * @param targetVersion the schema version to produce
		 * @return the upgraded value, or an error describing why it could not be upgraded
		 */
		DataResult<T> migrate(T value, int storedVersion, int targetVersion);
	}

	/**
	 * A migration for systems that have only ever had one schema version.
	 *
	 * <p>Because {@link #upgrade} handles the equal, newer, and invalid cases before
	 * calling the migration, reaching this function means an older version exists that
	 * nothing knows how to upgrade. That is a bug in the ticket that bumped the schema
	 * without writing the migration, so it reports loudly instead of guessing.
	 */
	public static <T> Migration<T> noMigrationsYet(String systemName) {
		return (value, storedVersion, targetVersion) -> DataResult.error(() ->
				"Forever " + systemName + " data has schema version " + storedVersion
						+ " but no migration to version " + targetVersion + " is registered. "
						+ "A schema was bumped without adding a migration path.");
	}
}

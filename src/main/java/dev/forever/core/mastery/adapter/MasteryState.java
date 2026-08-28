package dev.forever.core.mastery.adapter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.data.SchemaVersioned;
import dev.forever.core.mastery.domain.LoadoutChange;
import dev.forever.core.mastery.domain.MasteryProgress;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.resources.Identifier;

/**
 * Versioned, immutable player mastery attachment payload.
 *
 * <p>Schema 1 stored permanent progress and the active loadout. Schema 2 adds the optional
 * last successful switch context. Older records are migrated before callers receive them,
 * while newer records are refused by {@link SchemaVersioned}.
 */
public record MasteryState(
		int schemaVersion,
		Map<Identifier, MasteryProgress> progress,
		MasteryLoadout loadout,
		Optional<LoadoutChange> lastLoadoutChange) {

	public static final int CURRENT_SCHEMA = 2;
	private static final int MAX_MASTERIES = 128;

	private record Encoded(
			int schemaVersion,
			Map<Identifier, MasteryProgress> progress,
			MasteryLoadout loadout,
			Optional<LoadoutChange> lastLoadoutChange) {
	}

	private static final Codec<Map<Identifier, MasteryProgress>> PROGRESS_CODEC =
			Codec.unboundedMap(Identifier.CODEC, MasteryProgress.CODEC).validate(progress -> {
				if (progress.size() > MAX_MASTERIES) {
					return DataResult.error(() -> "Mastery state contains " + progress.size()
							+ " mastery records, exceeding the bounded limit of " + MAX_MASTERIES + ".");
				}
				return DataResult.success(progress);
			});

	private static final Codec<Encoded> RAW_ENCODED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("schema_version").forGetter(Encoded::schemaVersion),
			PROGRESS_CODEC.fieldOf("progress").forGetter(Encoded::progress),
			MasteryLoadout.CODEC.fieldOf("loadout").forGetter(Encoded::loadout),
			LoadoutChange.CODEC.optionalFieldOf("last_loadout_change")
					.forGetter(Encoded::lastLoadoutChange)
		).apply(instance, Encoded::new));

	private static final Codec<MasteryState> RAW_CODEC = RAW_ENCODED_CODEC.flatXmap(
			encoded -> create(
					encoded.schemaVersion(),
					encoded.progress(),
					encoded.loadout(),
					encoded.lastLoadoutChange()),
			state -> DataResult.success(new Encoded(
				state.schemaVersion(),
				state.progress(),
				state.loadout(),
				state.lastLoadoutChange())));

	/** Codec used by the Fabric persistent attachment. */
	public static final Codec<MasteryState> CODEC = SchemaVersioned.migrating(
			RAW_CODEC,
			MasteryState::schemaVersion,
			CURRENT_SCHEMA,
			MasteryState::migrate);

	/** Raw codec is exposed for migration fixtures, not for normal gameplay reads. */
	static Codec<MasteryState> rawCodec() {
		return RAW_CODEC;
	}

	public MasteryState {
		if (schemaVersion < 1) {
			throw new IllegalArgumentException("Mastery state schema version must be at least 1.");
		}
		Objects.requireNonNull(progress, "progress");
		if (progress.size() > MAX_MASTERIES) {
			throw new IllegalArgumentException("Mastery state contains too many mastery records.");
		}
		TreeMap<Identifier, MasteryProgress> sorted = new TreeMap<>();
		for (Map.Entry<Identifier, MasteryProgress> entry : progress.entrySet()) {
			if (entry.getKey() == null || entry.getValue() == null) {
				throw new IllegalArgumentException("Mastery state progress cannot contain null IDs or values.");
			}
			sorted.put(entry.getKey(), entry.getValue());
		}
		progress = Collections.unmodifiableMap(sorted);
		loadout = Objects.requireNonNull(loadout, "loadout");
		lastLoadoutChange = Objects.requireNonNull(lastLoadoutChange, "last loadout change");
	}

	public static MasteryState empty() {
		return new MasteryState(CURRENT_SCHEMA, Map.of(), MasteryLoadout.empty(), Optional.empty());
	}

	public MasteryProgress progressFor(Identifier masteryId) {
		Objects.requireNonNull(masteryId, "masteryId");
		return progress.getOrDefault(masteryId, MasteryProgress.empty());
	}

	public MasteryState withProgress(Identifier masteryId, MasteryProgress masteryProgress) {
		Objects.requireNonNull(masteryId, "masteryId");
		Objects.requireNonNull(masteryProgress, "masteryProgress");
		TreeMap<Identifier, MasteryProgress> next = new TreeMap<>(progress);
		next.put(masteryId, masteryProgress);
		return new MasteryState(schemaVersion, next, loadout, lastLoadoutChange);
	}

	public MasteryState withLoadout(MasteryLoadout nextLoadout) {
		return new MasteryState(schemaVersion, progress, nextLoadout, lastLoadoutChange);
	}

	public MasteryState withLastLoadoutChange(LoadoutChange change) {
		return new MasteryState(schemaVersion, progress, loadout, Optional.ofNullable(change));
	}

	private static DataResult<MasteryState> create(
			int schemaVersion,
			Map<Identifier, MasteryProgress> progress,
			MasteryLoadout loadout,
			Optional<LoadoutChange> lastLoadoutChange) {
		try {
			return DataResult.success(new MasteryState(
					schemaVersion, progress, loadout, lastLoadoutChange));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	private static DataResult<MasteryState> migrate(
			MasteryState value, int storedVersion, int targetVersion) {
		if (storedVersion == 1 && targetVersion == CURRENT_SCHEMA) {
			return DataResult.success(new MasteryState(
					targetVersion, value.progress(), value.loadout(), value.lastLoadoutChange()));
		}
		return DataResult.error(() -> "Mastery state has no migration from schema "
				+ storedVersion + " to schema " + targetVersion + ".");
	}
}

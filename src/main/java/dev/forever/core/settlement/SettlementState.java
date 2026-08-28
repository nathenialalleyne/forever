package dev.forever.core.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.data.SchemaVersioned;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

/** Versioned world-scoped collection of all Settlement Charters in one world. */
public record SettlementState(int schemaVersion, Map<UUID, Settlement> settlements) {

	public static final int CURRENT_SCHEMA = 2;
	private static final int MAX_SETTLEMENTS = 1_024;

	private record Encoded(int schemaVersion, Map<UUID, Settlement> settlements) {
	}

	private static final Codec<Encoded> RAW_ENCODED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("schema_version").forGetter(Encoded::schemaVersion),
			SettlementCodecs.boundedMap(SettlementCodecs.UUID_CODEC, Settlement.CODEC,
					MAX_SETTLEMENTS, "settlements").fieldOf("settlements")
					.forGetter(Encoded::settlements)
	).apply(instance, Encoded::new));

	public static final Codec<SettlementState> RAW_CODEC = RAW_ENCODED_CODEC.flatXmap(
			encoded -> create(encoded.schemaVersion(), encoded.settlements()),
			state -> DataResult.success(new Encoded(state.schemaVersion(), state.settlements())));

	public static final Codec<SettlementState> CODEC = SchemaVersioned.migrating(
			RAW_CODEC,
			SettlementState::schemaVersion,
			CURRENT_SCHEMA,
			SettlementState::migrate);

	public SettlementState {
		if (schemaVersion < 1) {
			throw new IllegalArgumentException("Settlement state schema version must be at least 1.");
		}
		Objects.requireNonNull(settlements, "settlements");
		if (settlements.size() > MAX_SETTLEMENTS) {
			throw new IllegalArgumentException("Settlement state contains too many Charters.");
		}
		TreeMap<UUID, Settlement> sorted = new TreeMap<>();
		for (Map.Entry<UUID, Settlement> entry : settlements.entrySet()) {
			if (entry.getKey() == null || entry.getValue() == null) {
				throw new IllegalArgumentException("Settlement state cannot contain null IDs or values.");
			}
			if (!entry.getKey().equals(entry.getValue().id())) {
				throw new IllegalArgumentException("Settlement state map key does not match the Charter ID.");
			}
			sorted.put(entry.getKey(), entry.getValue());
		}
		settlements = Collections.unmodifiableMap(sorted);
	}

	public static SettlementState empty() {
		return new SettlementState(CURRENT_SCHEMA, Map.of());
	}

	private static DataResult<SettlementState> create(int schemaVersion, Map<UUID, Settlement> settlements) {
		try {
			return DataResult.success(new SettlementState(schemaVersion, settlements));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	public Optional<Settlement> settlement(UUID settlementId) {
		return Optional.ofNullable(settlements.get(settlementId));
	}

	public Settlement requireSettlement(UUID settlementId) {
		return settlement(settlementId).orElseThrow(() -> new SettlementDataException(
				"Settlement " + settlementId + " is not present in the world Charter state."));
	}

	public SettlementState withSettlement(Settlement settlement) {
		Objects.requireNonNull(settlement, "settlement");
		if (settlements.containsKey(settlement.id())) {
			throw new SettlementDataException("Settlement " + settlement.id() + " is already chartered.");
		}
		TreeMap<UUID, Settlement> next = new TreeMap<>(settlements);
		next.put(settlement.id(), settlement);
		return new SettlementState(CURRENT_SCHEMA, next);
	}

	public SettlementState replaceSettlement(Settlement settlement) {
		Objects.requireNonNull(settlement, "settlement");
		if (!settlements.containsKey(settlement.id())) {
			throw new SettlementDataException("Settlement " + settlement.id() + " is not chartered.");
		}
		TreeMap<UUID, Settlement> next = new TreeMap<>(settlements);
		next.put(settlement.id(), settlement);
		return new SettlementState(CURRENT_SCHEMA, next);
	}

	public SettlementState withoutSettlement(UUID settlementId) {
		if (!settlements.containsKey(settlementId)) {
			throw new SettlementDataException("Settlement " + settlementId + " is not chartered.");
		}
		TreeMap<UUID, Settlement> next = new TreeMap<>(settlements);
		next.remove(settlementId);
		return new SettlementState(CURRENT_SCHEMA, next);
	}

	public boolean containsImportedProposal(UUID proposalId) {
		return settlements.values().stream()
				.map(Settlement::importedVillage)
				.flatMap(Optional::stream)
				.anyMatch(record -> record.proposalId().equals(proposalId));
	}

	private static DataResult<SettlementState> migrate(
			SettlementState value, int storedVersion, int targetVersion) {
		if (storedVersion == 1 && targetVersion == CURRENT_SCHEMA) {
			return DataResult.success(new SettlementState(targetVersion, value.settlements()));
		}
		return DataResult.error(() -> "No settlement state migration exists from schema "
				+ storedVersion + " to schema " + targetVersion + ". Restore a compatible backup.");
	}

	static Codec<SettlementState> rawCodec() {
		return RAW_CODEC;
	}
}

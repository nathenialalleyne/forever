package dev.forever.core.economy.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.data.SchemaVersioned;

/** Versioned player attachment payload for the compact Coin Purse balance. */
public record CoinPurseState(int schemaVersion, long balance, long lastTransactionSequence) {

	public static final int CURRENT_SCHEMA = 2;

	private record Encoded(int schemaVersion, long balance, java.util.Optional<Long> lastTransactionSequence) {
	}

	private static final Codec<Encoded> RAW_ENCODED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("schema_version").forGetter(Encoded::schemaVersion),
			Codec.LONG.fieldOf("balance").forGetter(Encoded::balance),
			Codec.LONG.optionalFieldOf("last_transaction_sequence")
					.forGetter(Encoded::lastTransactionSequence)
	).apply(instance, Encoded::new));

	private static final Codec<CoinPurseState> RAW_CODEC = RAW_ENCODED_CODEC.flatXmap(
			encoded -> create(encoded.schemaVersion(), encoded.balance(),
					encoded.lastTransactionSequence().orElse(0L)),
			state -> DataResult.success(new Encoded(state.schemaVersion(), state.balance(),
					java.util.Optional.of(state.lastTransactionSequence()))));

	public static final Codec<CoinPurseState> CODEC = SchemaVersioned.migrating(
			RAW_CODEC,
			CoinPurseState::schemaVersion,
			CURRENT_SCHEMA,
			CoinPurseState::migrate);

	public CoinPurseState {
		if (schemaVersion < 1) {
			throw new IllegalArgumentException("Coin Purse schema version must be at least 1.");
		}
		if (balance < 0L || lastTransactionSequence < 0L) {
			throw new IllegalArgumentException("Coin Purse balance and transaction sequence cannot be negative.");
		}
	}

	public static CoinPurseState empty() {
		return new CoinPurseState(CURRENT_SCHEMA, 0L, 0L);
	}

	public long nextTransactionSequence() {
		if (lastTransactionSequence == Long.MAX_VALUE) {
			throw new IllegalStateException("Coin Purse transaction sequence is exhausted; migration is required.");
		}
		return lastTransactionSequence + 1L;
	}

	public CoinPurseState withBalanceAndSequence(long nextBalance, long nextSequence) {
		return new CoinPurseState(CURRENT_SCHEMA, nextBalance, nextSequence);
	}

	/** Raw codec is exposed for prior-schema fixtures and migration tests. */
	public static Codec<CoinPurseState> rawCodec() {
		return RAW_CODEC;
	}

	private static DataResult<CoinPurseState> create(int schemaVersion, long balance, long sequence) {
		try {
			return DataResult.success(new CoinPurseState(schemaVersion, balance, sequence));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	private static DataResult<CoinPurseState> migrate(
			CoinPurseState value, int storedVersion, int targetVersion) {
		if (storedVersion == 1 && targetVersion == CURRENT_SCHEMA) {
			return DataResult.success(new CoinPurseState(CURRENT_SCHEMA, value.balance(), 0L));
		}
		return DataResult.error(() -> "No Coin Purse migration exists from schema " + storedVersion
				+ " to schema " + targetVersion + ".");
	}
}

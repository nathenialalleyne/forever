package dev.forever.core.economy.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.data.SchemaVersioned;

/** Data-defined currency limits. The values are configuration, not transaction state. */
public record EconomyBalance(
		int schemaVersion,
		long maxPurseBalance,
		long maxTransaction,
		int physicalStackLimit,
		long obolValue) {

	public static final int CURRENT_SCHEMA = 1;
	private static final int ENGINE_MAX_STACK = 64;

	private record Encoded(
			int schemaVersion,
			long maxPurseBalance,
			long maxTransaction,
			int physicalStackLimit,
			long obolValue) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("schema_version").forGetter(Encoded::schemaVersion),
			Codec.LONG.fieldOf("max_purse_balance").forGetter(Encoded::maxPurseBalance),
			Codec.LONG.fieldOf("max_transaction").forGetter(Encoded::maxTransaction),
			Codec.INT.fieldOf("physical_stack_limit").forGetter(Encoded::physicalStackLimit),
			Codec.LONG.fieldOf("obol_value").forGetter(Encoded::obolValue)
	).apply(instance, Encoded::new));

	private static final Codec<EconomyBalance> RAW_BALANCE_CODEC = RAW_CODEC.flatXmap(
			encoded -> create(encoded),
			balance -> DataResult.success(new Encoded(balance.schemaVersion(), balance.maxPurseBalance(),
					balance.maxTransaction(), balance.physicalStackLimit(), balance.obolValue())));

	public static final Codec<EconomyBalance> CODEC = SchemaVersioned.migrating(
			RAW_BALANCE_CODEC,
			EconomyBalance::schemaVersion,
			CURRENT_SCHEMA,
			SchemaVersioned.noMigrationsYet("economy balance"));

	public EconomyBalance {
		if (schemaVersion < 1) {
			throw new IllegalArgumentException("Economy balance schema version must be at least 1.");
		}
		if (maxPurseBalance < 1L || maxTransaction < 1L || maxTransaction > maxPurseBalance) {
			throw new IllegalArgumentException("Economy purse limits must be positive and transaction-limited.");
		}
		if (physicalStackLimit < 1 || physicalStackLimit > ENGINE_MAX_STACK) {
			throw new IllegalArgumentException("Physical Obol stack limit must be between 1 and "
					+ ENGINE_MAX_STACK + ".");
		}
		if (obolValue < 1L) {
			throw new IllegalArgumentException("The physical Obol value must be positive.");
		}
		try {
			Math.multiplyExact(maxTransaction, obolValue);
		} catch (ArithmeticException exception) {
			throw new IllegalArgumentException("Economy transaction value overflows the safe long range.", exception);
		}
	}

	public long purseLimit() {
		return maxPurseBalance;
	}

	public long maxBalance() {
		return maxPurseBalance;
	}

	private static DataResult<EconomyBalance> create(Encoded encoded) {
		try {
			return DataResult.success(new EconomyBalance(encoded.schemaVersion(), encoded.maxPurseBalance(),
					encoded.maxTransaction(), encoded.physicalStackLimit(), encoded.obolValue()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}
}

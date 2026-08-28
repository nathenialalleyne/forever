package dev.forever.core.settlement.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Bounded, versioned-by-the-parent record for a migration offer or arrival.
 * Ordinary breeding never creates this record.
 */
public record MigrationRecord(
		UUID migrationId,
		Optional<UUID> villagerId,
		String origin,
		String career,
		MigrationStatus status,
		long offeredAtTick,
		long lastProcessedTick) {

	private record Encoded(
			UUID migrationId,
			Optional<UUID> villagerId,
			String origin,
			String career,
			MigrationStatus status,
			long offeredAtTick,
			long lastProcessedTick) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SettlementCodecs.UUID_CODEC.fieldOf("migration_id").forGetter(Encoded::migrationId),
			SettlementCodecs.UUID_CODEC.optionalFieldOf("villager_id").forGetter(Encoded::villagerId),
			Codec.STRING.fieldOf("origin").forGetter(Encoded::origin),
			Codec.STRING.fieldOf("career").forGetter(Encoded::career),
			MigrationStatus.CODEC.fieldOf("status").forGetter(Encoded::status),
			Codec.LONG.fieldOf("offered_at_tick").forGetter(Encoded::offeredAtTick),
			Codec.LONG.fieldOf("last_processed_tick").forGetter(Encoded::lastProcessedTick)
	).apply(instance, Encoded::new));

	public static final Codec<MigrationRecord> CODEC = RAW_CODEC.flatXmap(
		encoded -> create(encoded),
		migration -> DataResult.success(new Encoded(
				migration.migrationId(), migration.villagerId(), migration.origin(), migration.career(),
				migration.status(), migration.offeredAtTick(), migration.lastProcessedTick())));

	public MigrationRecord {
		Objects.requireNonNull(migrationId, "migrationId");
		Objects.requireNonNull(villagerId, "villagerId");
		origin = SettlementCodecs.requiredText(origin, "migration origin", 128);
		career = SettlementCodecs.requiredText(career, "migration career", 128);
		Objects.requireNonNull(status, "status");
		if (offeredAtTick < 0L || lastProcessedTick < -1L) {
			throw new IllegalArgumentException("Settlement migration ticks cannot be negative.");
		}
	}

	private static DataResult<MigrationRecord> create(Encoded encoded) {
		try {
			return DataResult.success(new MigrationRecord(
					encoded.migrationId(), encoded.villagerId(), encoded.origin(), encoded.career(), encoded.status(),
					encoded.offeredAtTick(), encoded.lastProcessedTick()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	public MigrationRecord withStatus(MigrationStatus nextStatus, long processedAtTick) {
		return new MigrationRecord(migrationId, villagerId, origin, career, nextStatus,
				offeredAtTick, processedAtTick);
	}
}

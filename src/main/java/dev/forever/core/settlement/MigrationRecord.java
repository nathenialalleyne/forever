package dev.forever.core.settlement;

import com.mojang.serialization.Codec;
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

	public static final Codec<MigrationRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SettlementCodecs.UUID_CODEC.fieldOf("migration_id").forGetter(MigrationRecord::migrationId),
			SettlementCodecs.UUID_CODEC.optionalFieldOf("villager_id").forGetter(MigrationRecord::villagerId),
			Codec.STRING.fieldOf("origin").forGetter(MigrationRecord::origin),
			Codec.STRING.fieldOf("career").forGetter(MigrationRecord::career),
			MigrationStatus.CODEC.fieldOf("status").forGetter(MigrationRecord::status),
			Codec.LONG.fieldOf("offered_at_tick").forGetter(MigrationRecord::offeredAtTick),
			Codec.LONG.fieldOf("last_processed_tick").forGetter(MigrationRecord::lastProcessedTick)
	).apply(instance, MigrationRecord::new));

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

	public MigrationRecord withStatus(MigrationStatus nextStatus, long processedAtTick) {
		return new MigrationRecord(migrationId, villagerId, origin, career, nextStatus,
				offeredAtTick, processedAtTick);
	}
}

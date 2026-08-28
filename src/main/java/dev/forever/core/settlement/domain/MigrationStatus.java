package dev.forever.core.settlement;

import com.mojang.serialization.Codec;

/** Lifecycle state for a migration offer or the villager it represents. */
public enum MigrationStatus {
	OFFERED,
	ARRIVED,
	DEPARTED,
	CANCELLED;

	public static final Codec<MigrationStatus> CODEC =
			SettlementCodecs.enumCodec(MigrationStatus.class);
}

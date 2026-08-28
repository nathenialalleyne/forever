package dev.forever.core.settlement;

import com.mojang.serialization.Codec;

/** Functional, player-readable categories used by residence validation. */
public enum FunctionalRequirement {
	DECLARATION,
	VOLUME_CAP,
	ENCLOSURE,
	BEDS,
	WORKSTATIONS,
	STORAGE,
	SAFETY,
	SPACE;

	public static final Codec<FunctionalRequirement> CODEC =
			SettlementCodecs.enumCodec(FunctionalRequirement.class);
}

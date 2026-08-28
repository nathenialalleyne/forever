package dev.forever.core.career;

import com.mojang.serialization.Codec;

/** Explicit lifecycle state stored with a villager career attachment. */
public enum CareerStatus {
	ACTIVE,
	DOWNED,
	DEAD;

	public static final Codec<CareerStatus> CODEC = CareerCodecs.enumCodec(CareerStatus.class);
}

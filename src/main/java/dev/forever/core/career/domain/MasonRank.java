package dev.forever.core.career;

import com.mojang.serialization.Codec;

/** Ordered Mason ranks. The unassigned value is the safe default for a new entity. */
public enum MasonRank {
	UNASSIGNED,
	APPRENTICE,
	JOURNEYMAN,
	MASTER;

	public static final Codec<MasonRank> CODEC = CareerCodecs.enumCodec(MasonRank.class);

	public MasonRank next() {
		return switch (this) {
			case UNASSIGNED -> APPRENTICE;
			case APPRENTICE -> JOURNEYMAN;
			case JOURNEYMAN -> MASTER;
			case MASTER -> null;
		};
	}

	public boolean isAssigned() {
		return this != UNASSIGNED;
	}

	public boolean atLeast(MasonRank other) {
		return ordinal() >= other.ordinal();
	}

	public boolean canTeach() {
		return this == JOURNEYMAN || this == MASTER;
	}
}

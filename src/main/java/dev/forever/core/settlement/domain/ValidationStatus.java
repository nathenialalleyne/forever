package dev.forever.core.settlement.domain;

import com.mojang.serialization.Codec;

/** State of the last server-side validation of a registered building. */
public enum ValidationStatus {
	PENDING,
	VALID,
	INVALID;

	public static final Codec<ValidationStatus> CODEC =
			SettlementCodecs.enumCodec(ValidationStatus.class);
}

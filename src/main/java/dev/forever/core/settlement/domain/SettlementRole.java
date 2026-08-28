package dev.forever.core.settlement.domain;

import com.mojang.serialization.Codec;

/** Functional services a player may claim for an explicitly registered building. */
public enum SettlementRole {
	RESIDENCE,
	WORKPLACE,
	WAREHOUSE,
	WORKSHOP,
	FARM_SERVICE,
	ROUTE_STATION,
	MEETING_PLACE,
	OUTPOST;

	public static final Codec<SettlementRole> CODEC = SettlementCodecs.enumCodec(SettlementRole.class);
}

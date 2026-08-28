package dev.forever.core.settlement;

import com.mojang.serialization.Codec;

/** Why two explicitly registered building nodes are connected in the graph. */
public enum SettlementEdgeKind {
	PROXIMITY,
	OUTPOST,
	INFRASTRUCTURE;

	public static final Codec<SettlementEdgeKind> CODEC =
			SettlementCodecs.enumCodec(SettlementEdgeKind.class);
}

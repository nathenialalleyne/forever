package dev.forever.core.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.UUID;

/** Cached relationship between two registered building nodes. */
public record SettlementEdge(UUID from, UUID to, SettlementEdgeKind kind, double distance) {

	public static final Codec<SettlementEdge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SettlementCodecs.UUID_CODEC.fieldOf("from").forGetter(SettlementEdge::from),
			SettlementCodecs.UUID_CODEC.fieldOf("to").forGetter(SettlementEdge::to),
			SettlementEdgeKind.CODEC.fieldOf("kind").forGetter(SettlementEdge::kind),
			Codec.DOUBLE.fieldOf("distance").forGetter(SettlementEdge::distance)
	).apply(instance, SettlementEdge::new));

	public SettlementEdge {
		Objects.requireNonNull(from, "from");
		Objects.requireNonNull(to, "to");
		Objects.requireNonNull(kind, "kind");
		if (from.equals(to)) {
			throw new IllegalArgumentException("A settlement graph edge cannot connect a building to itself.");
		}
		if (!Double.isFinite(distance) || distance < 0.0) {
			throw new IllegalArgumentException("Settlement graph edge distance must be finite and non-negative.");
		}
	}
}

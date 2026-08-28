package dev.forever.core.settlement.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.UUID;

/** Cached relationship between two registered building nodes. */
public record SettlementEdge(UUID from, UUID to, SettlementEdgeKind kind, double distance) {

	private record Encoded(UUID from, UUID to, SettlementEdgeKind kind, double distance) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SettlementCodecs.UUID_CODEC.fieldOf("from").forGetter(Encoded::from),
			SettlementCodecs.UUID_CODEC.fieldOf("to").forGetter(Encoded::to),
			SettlementEdgeKind.CODEC.fieldOf("kind").forGetter(Encoded::kind),
			Codec.DOUBLE.fieldOf("distance").forGetter(Encoded::distance)
	).apply(instance, Encoded::new));

	public static final Codec<SettlementEdge> CODEC = RAW_CODEC.flatXmap(
		encoded -> create(encoded.from(), encoded.to(), encoded.kind(), encoded.distance()),
		edge -> DataResult.success(new Encoded(edge.from(), edge.to(), edge.kind(), edge.distance())));

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

	private static DataResult<SettlementEdge> create(
			UUID from, UUID to, SettlementEdgeKind kind, double distance) {
		try {
			return DataResult.success(new SettlementEdge(from, to, kind, distance));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}
}

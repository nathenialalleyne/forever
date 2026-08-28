package dev.forever.core.settlement.adapter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.settlement.domain.SettlementDataException;
import java.util.Objects;
import net.minecraft.core.BlockPos;

/** Inclusive, player-selected building bounds. No world scan is implied by this record. */
public record BuildingBounds(BlockPos min, BlockPos max) {

	private record Encoded(BlockPos min, BlockPos max) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockPos.CODEC.fieldOf("min").forGetter(Encoded::min),
			BlockPos.CODEC.fieldOf("max").forGetter(Encoded::max)
	).apply(instance, Encoded::new));

	public static final Codec<BuildingBounds> CODEC = RAW_CODEC.flatXmap(
		encoded -> create(encoded.min(), encoded.max()),
		bounds -> DataResult.success(new Encoded(bounds.min(), bounds.max())));

	public BuildingBounds {
		Objects.requireNonNull(min, "min");
		Objects.requireNonNull(max, "max");
		min = new BlockPos(min);
		max = new BlockPos(max);
		if (min.getX() > max.getX() || min.getY() > max.getY() || min.getZ() > max.getZ()) {
			throw new IllegalArgumentException("Settlement building bounds must have min <= max on every axis.");
		}
	}

	public static BuildingBounds fromCorners(BlockPos first, BlockPos second) {
		return new BuildingBounds(BlockPos.min(first, second), BlockPos.max(first, second));
	}

	public long width() {
		return (long) max.getX() - min.getX() + 1L;
	}

	public long height() {
		return (long) max.getY() - min.getY() + 1L;
	}

	public long depth() {
		return (long) max.getZ() - min.getZ() + 1L;
	}

	/** Returns a saturated value so malformed extreme coordinates cannot overflow into a small volume. */
	public long volume() {
		try {
			return Math.multiplyExact(Math.multiplyExact(width(), height()), depth());
		} catch (ArithmeticException exception) {
			return Long.MAX_VALUE;
		}
	}

	public boolean isWithin(long cap) {
		return cap > 0 && volume() <= cap;
	}

	public void requireWithin(long cap) {
		if (!isWithin(cap)) {
			throw new SettlementDataException("Registered building volume " + volume()
					+ " exceeds the settlement validation cap of " + cap + " blocks.");
		}
	}

	public boolean contains(BlockPos position) {
		return position.getX() >= min.getX() && position.getX() <= max.getX()
				&& position.getY() >= min.getY() && position.getY() <= max.getY()
				&& position.getZ() >= min.getZ() && position.getZ() <= max.getZ();
	}

	public boolean isBoundary(BlockPos position) {
		return contains(position)
				&& (position.getX() == min.getX() || position.getX() == max.getX()
						|| position.getY() == min.getY() || position.getY() == max.getY()
						|| position.getZ() == min.getZ() || position.getZ() == max.getZ());
	}

	public BlockPos center() {
		int x = (int) ((long) min.getX() + (width() - 1L) / 2L);
		int y = (int) ((long) min.getY() + (height() - 1L) / 2L);
		int z = (int) ((long) min.getZ() + (depth() - 1L) / 2L);
		return new BlockPos(x, y, z);
	}

	static DataResult<BuildingBounds> create(BlockPos min, BlockPos max) {
		try {
			return DataResult.success(new BuildingBounds(min, max));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}
}

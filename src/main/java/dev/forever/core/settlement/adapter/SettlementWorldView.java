package dev.forever.core.settlement.adapter;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Minimal server-world view used by bounded validators. It deliberately exposes no scan
 * operation, entity query, or chunk-loading operation.
 */
@FunctionalInterface
public interface SettlementWorldView {

	BlockState blockState(BlockPos position);

	default BlockEntity blockEntity(BlockPos position) {
		return null;
	}

	default int lightLevel(BlockPos position) {
		return 0;
	}

	/** Creates a server-only view. A client level cannot be supplied to this adapter. */
	static SettlementWorldView from(ServerLevel level) {
		Objects.requireNonNull(level, "level");
		return new SettlementWorldView() {
			@Override
			public BlockState blockState(BlockPos position) {
				return level.getBlockState(position);
			}

			@Override
			public BlockEntity blockEntity(BlockPos position) {
				return level.getBlockEntity(position);
			}

			@Override
			public int lightLevel(BlockPos position) {
				return level.getMaxLocalRawBrightness(position);
			}
		};
	}
}

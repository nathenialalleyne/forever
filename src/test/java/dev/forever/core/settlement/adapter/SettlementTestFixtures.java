package dev.forever.core.settlement;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

final class SettlementTestFixtures {

	static final Identifier OVERWORLD = Identifier.parse("minecraft:overworld");
	static final SettlementBalance BALANCE = new SettlementBalance(
			SettlementBalance.CURRENT_SCHEMA,
			64.0,
			512.0,
			32_768,
			262_144,
			8,
			1,
			1,
			9,
			8,
			64,
			24_000L,
			1,
			256);

	private SettlementTestFixtures() {
	}

	static Settlement charter() {
		return Settlement.found(
				java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"),
				"Test Settlement",
				java.util.UUID.fromString("00000000-0000-0000-0000-000000000010"),
				OVERWORLD,
				BlockPos.ZERO);
	}

	static RegisteredBuilding building(String id, int x, Set<SettlementRole> roles) {
		return RegisteredBuilding.pending(
				java.util.UUID.fromString(id),
				OVERWORLD,
				new BuildingBounds(new BlockPos(x, 0, 0), new BlockPos(x + 2, 2, 2)),
				roles);
	}

	static TestWorld enclosedRoom() {
		BuildingBounds bounds = new BuildingBounds(new BlockPos(0, 0, 0), new BlockPos(4, 4, 4));
		TestWorld world = new TestWorld();
		for (int x = bounds.min().getX(); x <= bounds.max().getX(); x++) {
			for (int y = bounds.min().getY(); y <= bounds.max().getY(); y++) {
				for (int z = bounds.min().getZ(); z <= bounds.max().getZ(); z++) {
					BlockPos position = new BlockPos(x, y, z);
					if (bounds.isBoundary(position)) {
						world.set(position, Blocks.STONE.defaultBlockState());
					}
				}
			}
		}
		BlockPos bedPosition = new BlockPos(1, 1, 1);
		BlockState bed = Blocks.BED.white().defaultBlockState().setValue(BedBlock.PART, BedPart.FOOT);
		world.set(bedPosition, bed);
		world.set(new BlockPos(2, 1, 1), Blocks.CRAFTING_TABLE.defaultBlockState());
		world.set(new BlockPos(3, 1, 1), Blocks.CHEST.defaultBlockState());
		world.setBlockEntity(new ChestBlockEntity(new BlockPos(3, 1, 1), Blocks.CHEST.defaultBlockState()));
		return world;
	}

	static final class TestWorld implements SettlementWorldView {
		private final Map<BlockPos, BlockState> states = new HashMap<>();
		private final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();
		private final Map<BlockPos, Integer> lights = new HashMap<>();
		private int blockReads;

		void set(BlockPos position, BlockState state) {
			states.put(new BlockPos(position), state);
		}

		void setBlockEntity(BlockEntity blockEntity) {
			blockEntities.put(new BlockPos(blockEntity.getBlockPos()), blockEntity);
		}

		void setLight(BlockPos position, int light) {
			lights.put(new BlockPos(position), light);
		}

		int blockReads() {
			return blockReads;
		}

		@Override
		public BlockState blockState(BlockPos position) {
			blockReads++;
			return states.getOrDefault(position, Blocks.AIR.defaultBlockState());
		}

		@Override
		public BlockEntity blockEntity(BlockPos position) {
			return blockEntities.get(position);
		}

		@Override
		public int lightLevel(BlockPos position) {
			return lights.getOrDefault(position, 15);
		}
	}
}

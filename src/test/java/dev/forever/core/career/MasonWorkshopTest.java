package dev.forever.core.career;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.forever.core.settlement.BuildingBounds;
import dev.forever.core.settlement.Settlement;
import dev.forever.core.settlement.SettlementBalance;
import dev.forever.core.settlement.SettlementCharterService;
import dev.forever.core.settlement.SettlementState;
import dev.forever.core.settlement.SettlementWorldView;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MasonWorkshopTest {

	@BeforeAll
	static void bootstrapMinecraftRegistries() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	private static final SettlementBalance BALANCE = new SettlementBalance(
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
	private static final Identifier OVERWORLD = Identifier.parse("minecraft:overworld");

	@Test
	@DisplayName("Mason workshop registration reuses the settlement Workplace graph role")
	void functionalRegistrationUsesSettlementGraph() {
		Settlement settlement = SettlementCharterService.found(
				UUID.fromString("00000000-0000-0000-0000-000000000601"),
				"Mason Settlement",
				UUID.fromString("00000000-0000-0000-0000-000000000602"),
				OVERWORLD,
				BlockPos.ZERO);
		SettlementState state = SettlementCharterService.found(SettlementState.empty(), settlement);
		BuildingBounds bounds = new BuildingBounds(new BlockPos(0, 0, 0), new BlockPos(4, 4, 4));
		TestWorld world = enclosedWorkshop(bounds);

		MasonWorkshopService.WorkshopRegistrationResult result = MasonWorkshopService.register(
				state,
				settlement.id(),
				UUID.fromString("00000000-0000-0000-0000-000000000603"),
				OVERWORLD,
				bounds,
				BALANCE,
				world,
				7);

		assertTrue(result.accepted());
		assertTrue(result.validation().valid());
		assertEquals(1, result.state().requireSettlement(settlement.id()).buildings().size());
		assertTrue(result.building().orElseThrow().roles().contains(
				dev.forever.core.settlement.SettlementRole.WORKPLACE));
	}

	@Test
	@DisplayName("an invalid Mason workshop is rejected without registering a service")
	void invalidRegistrationLeavesGraphUntouched() {
		Settlement settlement = SettlementCharterService.found(
				UUID.fromString("00000000-0000-0000-0000-000000000604"),
				"Mason Settlement",
				UUID.fromString("00000000-0000-0000-0000-000000000605"),
				OVERWORLD,
				BlockPos.ZERO);
		SettlementState state = SettlementCharterService.found(SettlementState.empty(), settlement);
		BuildingBounds bounds = new BuildingBounds(new BlockPos(0, 0, 0), new BlockPos(4, 4, 4));

		MasonWorkshopService.WorkshopRegistrationResult result = MasonWorkshopService.register(
				state,
				settlement.id(),
				UUID.fromString("00000000-0000-0000-0000-000000000606"),
				OVERWORLD,
				bounds,
				BALANCE,
				new TestWorld(),
				1);

		assertFalse(result.accepted());
		assertTrue(result.state().requireSettlement(settlement.id()).buildings().isEmpty());
	}

	private static TestWorld enclosedWorkshop(BuildingBounds bounds) {
		TestWorld world = new TestWorld();
		for (int x = bounds.min().getX(); x <= bounds.max().getX(); x++) {
			for (int y = bounds.min().getY(); y <= bounds.max().getY(); y++) {
				for (int z = bounds.min().getZ(); z <= bounds.max().getZ(); z++) {
					BlockPos position = new BlockPos(x, y, z);
					if (bounds.isBoundary(position)) {
						world.states.put(position, Blocks.STONE.defaultBlockState());
					}
				}
			}
		}
		world.states.put(new BlockPos(1, 1, 1), Blocks.CRAFTING_TABLE.defaultBlockState());
		BlockPos chestPosition = new BlockPos(2, 1, 1);
		world.states.put(chestPosition, Blocks.CHEST.defaultBlockState());
		world.blockEntities.put(chestPosition, new ChestBlockEntity(chestPosition, Blocks.CHEST.defaultBlockState()));
		return world;
	}

	private static final class TestWorld implements SettlementWorldView {
		private final Map<BlockPos, BlockState> states = new HashMap<>();
		private final Map<BlockPos, BlockEntity> blockEntities = new HashMap<>();

		@Override
		public BlockState blockState(BlockPos position) {
			return states.getOrDefault(position, Blocks.AIR.defaultBlockState());
		}

		@Override
		public BlockEntity blockEntity(BlockPos position) {
			return blockEntities.get(position);
		}

		@Override
		public int lightLevel(BlockPos position) {
			return 15;
		}
	}
}

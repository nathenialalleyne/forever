package dev.forever.core.career.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.forever.core.career.domain.MasonRank;
import net.minecraft.resources.Identifier;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MasonDataTest {

	@BeforeAll
	static void bootstrapMinecraftRegistries() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		bindComponents(Items.STONE, Items.STONE_BRICKS, Items.DIAMOND);
	}

	@Test
	@DisplayName("bundled Mason data contains all three ranks and a bounded bulk catalogue")
	void bundledDataLoads() {
		MasonDataLoader.installBundled();
		MasonData data = MasonDataRegistry.requireCurrent();

		assertEquals(MasonRank.MASTER, data.career().orderedRanks().getLast().rank());
		assertEquals(3, data.catalog().entries().size());
		assertTrue(data.catalog().entry(Identifier.fromNamespaceAndPath("forever", "mason/stone_bricks")).isPresent());
	}

	@Test
	@DisplayName("catalogue preview enforces rank and batch bounds")
	void catalogueBoundsAreEnforced() {
		MasonDataLoader.installBundled();
		MasonData data = MasonDataRegistry.requireCurrent();
		CareerState apprentice = CareerState.masonApprentice(java.util.Optional.empty(), java.util.Optional.empty());
		Identifier entry = Identifier.fromNamespaceAndPath("forever", "mason/stone_bricks");

		MasonSupplyResult valid = MasonSupplyService.preview(apprentice, data.catalog(), entry, 1);
		MasonSupplyResult invalid = MasonSupplyService.preview(apprentice, data.catalog(), entry, 65);

		assertTrue(valid.applied());
		assertFalse(invalid.applied());
	}

	@Test
	@DisplayName("catalogue input and output commit atomically")
	void catalogueOperationIsAtomic() {
		MasonDataLoader.installBundled();
		MasonData data = MasonDataRegistry.requireCurrent();
		CareerState apprentice = CareerState.masonApprentice(java.util.Optional.empty(), java.util.Optional.empty());
		Identifier entry = Identifier.fromNamespaceAndPath("forever", "mason/stone_bricks");
		SimpleContainer input = new SimpleContainer(new ItemStack(Items.STONE, 8));
		SimpleContainer output = new SimpleContainer(2);

		MasonSupplyResult applied = MasonSupplyService.supply(apprentice, data.catalog(), entry, 2, input, output);

		assertTrue(applied.applied());
		assertEquals(0, input.getItem(0).getCount());
		assertEquals(8, output.getItem(0).getCount());

		SimpleContainer blockedInput = new SimpleContainer(new ItemStack(Items.STONE, 8));
		SimpleContainer blockedOutput = new SimpleContainer(new ItemStack(Items.DIAMOND, 64));
		MasonSupplyResult rejected = MasonSupplyService.supply(
				apprentice, data.catalog(), entry, 2, blockedInput, blockedOutput);

		assertFalse(rejected.applied());
		assertEquals(8, blockedInput.getItem(0).getCount());
		assertEquals(64, blockedOutput.getItem(0).getCount());
	}

	private static void bindComponents(Item... items) {
		DataComponentMap components = DataComponentMap.builder()
				.set(DataComponents.MAX_STACK_SIZE, 64)
				.build();
		for (Item item : items) {
			item.builtInRegistryHolder().bindComponents(components);
		}
	}
}

package dev.forever.core.storage.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.forever.core.storage.domain.IndexMutationResult;
import dev.forever.core.storage.domain.PhysicalTransferResult;
import dev.forever.core.storage.domain.StorageBalance;
import dev.forever.core.storage.domain.WarehouseSearchRequest;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StorageBehaviorTest {

	private static final ResourceKey<Level> TEST_DIMENSION = ResourceKey.create(
			Registries.DIMENSION, Identifier.parse("forever:test_dimension"));

	@BeforeAll
	static void bootstrapMinecraft() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		bindComponents(Items.DIAMOND, Items.IRON_INGOT, Items.IRON_BLOCK, Items.GOLD_INGOT);
	}

	@Test
	@DisplayName("Traveler's Cache enforces the configured nine and eighteen slot capacities")
	void cacheSlotLimits() {
		StorageBalance balance = balance();
		TravelerCacheContents initial = TravelerCacheContents.empty(balance);
		TravelerCacheContents expanded = TravelerCacheContents.empty(true, balance);

		assertEquals(9, initial.capacity());
		assertEquals(18, expanded.capacity());
		assertTrue(new TravelerCacheContents(1, false, slots(10)).validateFor(balance).error().isPresent());
		assertTrue(new TravelerCacheContents(1, true, slots(19)).validateFor(balance).error().isPresent());

	}

	@Test
	@DisplayName("container items are rejected by component and modded item capability checks")
	void nestedContainersAreRejected() {
		ItemStack componentContainer = new ItemStack(Items.DIAMOND);
		componentContainer.set(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
		assertTrue(ContainerItemPredicate.isContainer(componentContainer));
		ItemStack moddedStyleContainer = new ItemStack(Items.DIAMOND);
		moddedStyleContainer.set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
		assertTrue(ContainerItemPredicate.isContainer(moddedStyleContainer));
	}

	@Test
	@DisplayName("cache insertion and physical extraction preserve the total item count")
	void cacheOperationsDoNotDuplicateItems() {
		SimpleContainer source = new SimpleContainer(new ItemStack(Items.IRON_INGOT, 4));
		SimpleContainer destination = new SimpleContainer(1);
		int before = count(source) + count(destination);

		PhysicalTransferResult result = PhysicalInventoryTransfer.move(source, 0, destination, 0, 4);

		assertTrue(result.applied());
		assertEquals(4, result.moved());
		assertEquals(before, count(source) + count(destination));
		assertEquals(4, destination.getItem(0).getCount());
	}

	@Test
	@DisplayName("index add, remove, and invalidation operate only on supplied container snapshots")
	void indexMutationAndInvalidation() {
		StorageBalance balance = balance();
		ContainerReference reference = new ContainerReference(TEST_DIMENSION, new net.minecraft.core.BlockPos(10, 70, 10));
		InventoryIndex index = new InventoryIndex(balance);
		List<ItemStack> snapshot = List.of(
				new ItemStack(Items.IRON_INGOT, 4),
				ItemStack.EMPTY,
				new ItemStack(Items.GOLD_INGOT, 2));

		IndexMutationResult added = index.add(reference, snapshot);
		assertTrue(added.applied());
		assertEquals(2, added.indexedSlots());
		assertEquals(2, index.state().entries().size());

		IndexMutationResult invalidated = index.invalidate(reference);
		assertTrue(invalidated.applied());
		assertTrue(index.state().invalidated().contains(reference));
		WarehouseSearchPage stale = index.query(new WarehouseSearchRequest(1, "iron", 0, 5));
		assertEquals(1, stale.results().size());
		assertTrue(stale.results().getFirst().stale());
		assertFalse(stale.results().getFirst().available());

		IndexMutationResult removed = index.remove(reference);
		assertTrue(removed.applied());
		assertEquals(0, index.state().entries().size());
		assertTrue(index.state().invalidated().isEmpty());
	}

	@Test
	@DisplayName("index queries always return a bounded page")
	void queryResultsAreBounded() {
		StorageBalance balance = balanceWithQueryLimit(1);
		InventoryIndex index = new InventoryIndex(balance);
		ContainerReference first = new ContainerReference(TEST_DIMENSION, new net.minecraft.core.BlockPos(1, 70, 1));
		ContainerReference second = new ContainerReference(TEST_DIMENSION, new net.minecraft.core.BlockPos(2, 70, 1));
		index.add(first, List.of(new ItemStack(Items.IRON_INGOT, 1)));
		index.add(second, List.of(new ItemStack(Items.IRON_BLOCK, 1)));

		WarehouseSearchPage page = index.query(new WarehouseSearchRequest(1, "", 0, 1));

		assertEquals(1, page.results().size());
		assertTrue(page.results().size() <= balance.maxQueryResults());
		assertTrue(page.hasMore());
		assertEquals(2, page.totalKnown());
	}

	@Test
	@DisplayName("physical transfer preserves counts and cannot use a nested container as bulk source")
	void physicalTransferIsAtomicAndNonDuplicating() {
		SimpleContainer source = new SimpleContainer(new ItemStack(Items.IRON_INGOT, 5));
		SimpleContainer destination = new SimpleContainer(new ItemStack(Items.IRON_INGOT, 1));
		int before = count(source) + count(destination);

		PhysicalTransferResult result = PhysicalInventoryTransfer.move(source, 0, destination, 0, 2);

		assertTrue(result.applied());
		assertEquals(2, result.moved());
		assertEquals(before, count(source) + count(destination));
		assertEquals(3, destination.getItem(0).getCount());
	}

	private static StorageBalance balance() {
		return balanceWithQueryLimit(5);
	}

	private static StorageBalance balanceWithQueryLimit(int queryLimit) {
		return new StorageBalance(1, 9, 18, 16, 32, 128, queryLimit, 128, 4, 64, 4, 32, 65_536);
	}

	private static List<ItemStack> slots(int size) {
		List<ItemStack> result = new ArrayList<>(size);
		for (int index = 0; index < size; index++) {
			result.add(ItemStack.EMPTY);
		}
		return result;
	}

	private static int count(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return 0;
		}
		return stack.getCount();
	}

	private static int count(TravelerCacheContents contents) {
		return contents.slots().stream().mapToInt(StorageBehaviorTest::count).sum();
	}

	private static int count(Container container) {
		int total = 0;
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			total += count(container.getItem(slot));
		}
		return total;
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

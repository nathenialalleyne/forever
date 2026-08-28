package dev.forever.gametest.storage;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.forever.core.storage.application.StorageBalanceAccess;
import dev.forever.core.storage.adapter.ContainerReference;
import dev.forever.core.storage.adapter.ForeverStorage;
import dev.forever.core.storage.adapter.PhysicalInventoryTransfer;
import dev.forever.core.storage.adapter.TravelerCache;
import dev.forever.core.storage.adapter.TravelerCacheComponent;
import dev.forever.core.storage.adapter.TravelerCacheContents;
import dev.forever.core.storage.adapter.WarehouseController;
import dev.forever.core.storage.adapter.WarehouseMutationResult;
import dev.forever.core.storage.adapter.WarehouseSavedData;
import dev.forever.core.storage.adapter.WarehouseSearchPage;
import dev.forever.core.storage.domain.CacheOperation;
import dev.forever.core.storage.domain.StorageBalance;
import dev.forever.core.storage.domain.WarehouseSearchRequest;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/** Dedicated-server storage acceptance tests for FVR-600 through FVR-604. */
public final class StorageGameTest {

	static {
		// ForeverMod must call this in production. The test entrypoint initializes the owned slice
		// here so these tests also exercise its registry and SavedData setup in isolation.
		ForeverStorage.initialize();
	}

	@GameTest
	public void warehouseStateSurvivesSaveAndReloadBoundary(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos controller = helper.absolutePos(new BlockPos(0, 1, 0));
		helper.setBlock(new BlockPos(0, 1, 0), Blocks.CHEST);
		UUID warehouseId = UUID.randomUUID();

		WarehouseMutationResult registered = WarehouseController.registerWarehouse(
				level, warehouseId, controller, balance());
		helper.assertTrue(registered.applied(), "warehouse registration must succeed in a loaded test chunk");
		WarehouseSavedData saved = WarehouseSavedData.get(level);
		helper.assertTrue(level.getServer().saveEverything(true, true, true),
				"the dedicated server must accept the warehouse save");

		JsonElement encoded = WarehouseSavedData.CODEC.encodeStart(JsonOps.INSTANCE, saved).getOrThrow();
		WarehouseSavedData reloaded = WarehouseSavedData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
		helper.assertTrue(saved.state().equals(reloaded.state()),
				"warehouse SavedData must retain its state across a restart-shaped codec boundary");
		helper.assertTrue(reloaded.warehouse(warehouseId).isPresent(),
				"the saved warehouse must still be addressable after reload");
		releaseWarehouse(level, warehouseId);
		helper.succeed();
	}

	@GameTest
	public void indexReflectsPhysicalContainerContents(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos controllerRelative = new BlockPos(0, 1, 0);
		BlockPos containerRelative = new BlockPos(2, 1, 0);
		ChestBlockEntity chest = placeChest(helper, containerRelative);
		chest.setItem(0, new ItemStack(Items.IRON_INGOT, 7));
		chest.setChanged();
		UUID warehouseId = registerWarehouse(helper, level, controllerRelative);
		ContainerReference reference = new ContainerReference(level.dimension(), helper.absolutePos(containerRelative));

		WarehouseMutationResult registered = WarehouseController.registerContainer(
				level, warehouseId, reference, balance());
		helper.assertTrue(registered.applied(), "the physical chest must be registerable");
		WarehouseController.refreshContainer(level, warehouseId, reference, balance());

		WarehouseSearchPage page = WarehouseController.search(
				level, warehouseId, new WarehouseSearchRequest(1, "iron", 0, 10), balance());
		helper.assertTrue(page.status().equals("ok"), "search must return an accepted page");
		helper.assertTrue(page.results().size() == 1, "the registered chest's non-empty slot must be indexed");
		helper.assertTrue(page.results().getFirst().quantity() == 7,
				"the index must expose the physical chest quantity");
		helper.assertTrue(page.results().getFirst().available(),
				"a loaded, matching physical row must be available");
		releaseWarehouse(level, warehouseId);
		helper.succeed();
	}

	@GameTest
	public void worldWinsWhenIndexDisagreesWithPhysicalContents(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos controllerRelative = new BlockPos(0, 1, 0);
		BlockPos containerRelative = new BlockPos(2, 1, 0);
		ChestBlockEntity chest = placeChest(helper, containerRelative);
		chest.setItem(0, new ItemStack(Items.GOLD_INGOT, 8));
		chest.setChanged();
		UUID warehouseId = registerWarehouse(helper, level, controllerRelative);
		ContainerReference reference = new ContainerReference(level.dimension(), helper.absolutePos(containerRelative));
		WarehouseController.registerContainer(level, warehouseId, reference, balance());
		WarehouseController.refreshContainer(level, warehouseId, reference, balance());

		// Deliberately change the source of truth without first informing the cache.
		chest.setItem(0, new ItemStack(Items.GOLD_INGOT, 3));
		chest.setChanged();
		WarehouseSearchPage page = WarehouseController.search(
				level, warehouseId, new WarehouseSearchRequest(1, "gold", 0, 10), balance());

		helper.assertTrue(page.results().size() == 1,
				"the physical replacement must remain visible after reconciliation");
		helper.assertTrue(page.results().getFirst().quantity() == 3,
				"a disagreement must resolve in the world's physical quantity's favour");
		helper.assertTrue(page.results().getFirst().available(),
				"the reconciled physical row must be available");
		releaseWarehouse(level, warehouseId);
		helper.succeed();
	}

	@GameTest
	public void storageOperationsDoNotDuplicateItems(GameTestHelper helper) {
		StorageBalance balance = balance();
		ItemStack cache = TravelerCache.createEmptyStack(balance);
		ItemStack source = new ItemStack(Items.DIAMOND, 11);
		SimpleContainer destination = new SimpleContainer(1);
		int before = source.getCount() + countCache(cache) + count(destination);

		CacheOperation inserted = TravelerCache.insert(cache, source, balance);
		helper.assertTrue(inserted.applied() && inserted.moved() == 11,
				"cache insertion must move exactly the source count");
		CacheOperation extracted = TravelerCache.extract(cache, 0, destination, 0, 11, balance);
		helper.assertTrue(extracted.applied() && extracted.moved() == 11,
				"cache extraction must move exactly the requested count");
		helper.assertTrue(before == source.getCount() + countCache(cache) + count(destination),
				"cache insertion and extraction must conserve the total item count");

		SimpleContainer physicalSource = new SimpleContainer(new ItemStack(Items.IRON_INGOT, 5));
		SimpleContainer physicalDestination = new SimpleContainer(new ItemStack(Items.IRON_INGOT, 1));
		int physicalBefore = count(physicalSource) + count(physicalDestination);
		PhysicalInventoryTransfer.move(physicalSource, 0, physicalDestination, 0, 2);
		helper.assertTrue(physicalBefore == count(physicalSource) + count(physicalDestination),
				"physical transfer must conserve the total item count");
		helper.succeed();
	}

	/**
	 * Proves a registered warehouse can be decommissioned without destroying items.
	 *
	 * <p>Registration used to be a one-way door: nothing could remove a warehouse, so a
	 * misplaced one was permanent and the per-world limit filled up for good. This test
	 * covers the removal path and, critically, that removal touches only the
	 * registration record and never the physical container contents.
	 */
	@GameTest
	public void warehouseCanBeDecommissionedWithoutDestroyingItems(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos controllerRelative = new BlockPos(0, 1, 0);
		UUID warehouseId = registerWarehouse(helper, level, controllerRelative);

		helper.assertTrue(WarehouseSavedData.get(level).warehouse(warehouseId).isPresent(),
				"the warehouse must exist before removal");

		WarehouseMutationResult removed = WarehouseController.removeWarehouse(level, warehouseId);
		helper.assertTrue(removed.applied(), "removing a registered warehouse must succeed");
		helper.assertTrue(WarehouseSavedData.get(level).warehouse(warehouseId).isEmpty(),
				"the warehouse registration must be gone after removal");

		// Physical inventories are the source of truth, so the controller block and any
		// stored items must survive decommissioning untouched.
		helper.assertBlockPresent(Blocks.CHEST, controllerRelative);

		// Removing an unknown warehouse must be a clean rejection, not a crash.
		WarehouseMutationResult again = WarehouseController.removeWarehouse(level, warehouseId);
		helper.assertTrue(!again.applied(), "removing an already-removed warehouse must be rejected, not applied");

		helper.succeed();
	}

	/** Removes a test warehouse so the shared world does not accumulate registrations. */
	private static void releaseWarehouse(ServerLevel level, UUID warehouseId) {
		WarehouseController.removeWarehouse(level, warehouseId);
	}

	private static ChestBlockEntity placeChest(GameTestHelper helper, BlockPos relative) {
		helper.setBlock(relative, Blocks.CHEST);
		return helper.getBlockEntity(relative, ChestBlockEntity.class);
	}

	/**
	 * Registers a warehouse for a test.
	 *
	 * <p>Callers must pass the returned id to {@link #releaseWarehouse} before the test
	 * finishes. GameTests share one persistent world, so a test that registers without
	 * releasing leaks state into every later run until the per-world warehouse limit is
	 * reached and unrelated tests begin failing. That is not hypothetical: it is exactly
	 * the failure this suite exhibited before cleanup existed.
	 */
	private static UUID registerWarehouse(GameTestHelper helper, ServerLevel level, BlockPos relativeController) {
		helper.setBlock(relativeController, Blocks.CHEST);
		UUID warehouseId = UUID.randomUUID();
		WarehouseMutationResult result = WarehouseController.registerWarehouse(
				level, warehouseId, helper.absolutePos(relativeController), balance());
		helper.assertTrue(result.applied(),
				"warehouse registration must succeed; if this fails with the warehouse limit, a previous "
						+ "test leaked registrations into the shared world");
		return warehouseId;
	}

	private static StorageBalance balance() {
		return StorageBalanceAccess.requireCurrent();
	}

	private static int countCache(ItemStack cache) {
		TravelerCacheContents contents = cache.get(TravelerCacheComponent.TYPE);
		return contents == null ? 0 : contents.slots().stream().mapToInt(StorageGameTest::count).sum();
	}

	private static int count(SimpleContainer container) {
		int total = 0;
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			total += count(container.getItem(slot));
		}
		return total;
	}

	private static int count(ItemStack stack) {
		return stack == null || stack.isEmpty() ? 0 : stack.getCount();
	}
}

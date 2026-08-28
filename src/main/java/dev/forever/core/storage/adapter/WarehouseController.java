package dev.forever.core.storage.adapter;

import dev.forever.core.storage.application.StorageBalanceAccess;
import dev.forever.core.storage.domain.IndexMutationResult;
import dev.forever.core.storage.domain.StorageBalance;
import dev.forever.core.storage.domain.WarehouseSearchRequest;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Server-only access point for registered warehouses and their derived indexes.
 *
 * <p>All public operations require a {@link ServerLevel}. Search is read-only with respect to
 * physical inventories. It reconciles only explicit invalidations and result rows, never scans
 * the world or force-loads a chunk.
 */
public final class WarehouseController {

	public static final String INVALID_WAREHOUSE = "forever.storage.warehouse_not_found";
	public static final String DUPLICATE_WAREHOUSE = "forever.storage.warehouse_duplicate";
	public static final String WAREHOUSE_LIMIT = "forever.storage.warehouse_limit_reached";
	public static final String INVALID_CONTROLLER = "forever.storage.warehouse_invalid_controller";
	public static final String CROSS_DIMENSION = "forever.storage.cross_dimension_rejected";
	public static final String CONTAINER_NOT_REGISTERED = "forever.storage.container_not_registered";
	public static final String CONTAINER_UNAVAILABLE = "forever.storage.container_unavailable";
	public static final String CONTAINER_LIMIT = "forever.storage.container_limit_reached";
	public static final String OUT_OF_RANGE = "forever.storage.container_out_of_range";
	public static final String INDEX_REBUILD_LIMIT = "forever.storage.index_rebuild_limit_reached";
	public static final String INDEX_UNAVAILABLE = "forever.storage.index_unavailable";

	private WarehouseController() {
	}

	public static WarehouseSavedData savedData(ServerLevel level) {
		return WarehouseSavedData.get(level);
	}

	public static WarehouseMutationResult registerWarehouse(ServerLevel level, UUID id, BlockPos controller) {
		return registerWarehouse(level, id, controller, StorageBalanceAccess.requireCurrent());
	}

	public static WarehouseMutationResult registerWarehouse(
			ServerLevel level, UUID id, BlockPos controller, StorageBalance balance) {
		Objects.requireNonNull(level, "level");
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(controller, "controller");
		Objects.requireNonNull(balance, "balance");
		WarehouseSavedData data = WarehouseSavedData.get(level);
		synchronized (data) {
			if (data.warehouse(id).isPresent()) {
				return WarehouseMutationResult.rejected(DUPLICATE_WAREHOUSE);
			}
			if (data.state().warehouses().size() >= balance.maxWarehouses()) {
				return WarehouseMutationResult.rejected(WAREHOUSE_LIMIT);
			}
			if (!level.isInValidBounds(controller) || !level.isLoaded(controller)) {
				return WarehouseMutationResult.rejected(INVALID_CONTROLLER);
			}
			WarehouseRecord warehouse = WarehouseRecord.empty(id, level.dimension(), controller);
			data.replace(data.state().with(warehouse));
			return WarehouseMutationResult.success(warehouse);
		}
	}

	/**
	 * Removes a registered warehouse and its derived index entries.
	 *
	 * <p>Registration without removal is a one-way door: a player who registers a
	 * warehouse in the wrong place could never undo it, and the per-world warehouse
	 * limit would fill permanently. That is the defect this method fixes.
	 *
	 * <p>Only the registration record is removed. Physical containers and their
	 * contents are untouched, because physical inventories are the source of truth
	 * (see {@code docs/systems/storage.md}). Decommissioning a warehouse must never
	 * destroy a player's items.
	 *
	 * @param level     the server level owning the warehouse
	 * @param id        the warehouse to remove
	 * @return success carrying the removed record, or a rejection naming the reason
	 */
	public static WarehouseMutationResult removeWarehouse(ServerLevel level, UUID id) {
		Objects.requireNonNull(level, "level");
		Objects.requireNonNull(id, "id");

		WarehouseSavedData data = WarehouseSavedData.get(level);
		synchronized (data) {
			Optional<WarehouseRecord> existing = data.warehouse(id);
			if (existing.isEmpty()) {
				return WarehouseMutationResult.rejected(INVALID_WAREHOUSE);
			}
			data.replace(data.state().without(id));
			return WarehouseMutationResult.success(existing.orElseThrow());
		}
	}

	public static WarehouseMutationResult registerContainer(
			ServerLevel level, UUID warehouseId, ContainerReference reference) {
		return registerContainer(level, warehouseId, reference, StorageBalanceAccess.requireCurrent());
	}

	public static WarehouseMutationResult registerContainer(
			ServerLevel level, UUID warehouseId, ContainerReference reference, StorageBalance balance) {
		Objects.requireNonNull(level, "level");
		Objects.requireNonNull(warehouseId, "warehouseId");
		Objects.requireNonNull(reference, "reference");
		Objects.requireNonNull(balance, "balance");
		WarehouseSavedData data = WarehouseSavedData.get(level);
		synchronized (data) {
			Optional<WarehouseRecord> found = data.warehouse(warehouseId);
			if (found.isEmpty()) {
				return WarehouseMutationResult.rejected(INVALID_WAREHOUSE);
			}
			WarehouseRecord warehouse = found.get();
			if (!warehouse.dimension().equals(reference.dimension()) || !level.dimension().equals(reference.dimension())) {
				return WarehouseMutationResult.rejected(CROSS_DIMENSION);
			}
			if (warehouse.hasContainer(reference)) {
				return WarehouseMutationResult.rejected(DUPLICATE_WAREHOUSE);
			}
			if (warehouse.registeredContainers().size() >= balance.maxRegisteredContainers()) {
				return WarehouseMutationResult.rejected(CONTAINER_LIMIT);
			}
			if (warehouse.controller().distSqr(reference.position())
					> (double) balance.warehouseRegistrationRange() * balance.warehouseRegistrationRange()) {
				return WarehouseMutationResult.rejected(OUT_OF_RANGE);
			}
			if (StorageContainerAccess.find(level, reference).isEmpty()) {
				return WarehouseMutationResult.rejected(CONTAINER_UNAVAILABLE);
			}
			List<ContainerReference> next = new ArrayList<>(warehouse.registeredContainers());
			next.add(reference);
			WarehouseRecord updated = warehouse.withContainers(next);
			data.replace(data.state().with(updated));
			return WarehouseMutationResult.success(updated);
		}
	}

	public static WarehouseMutationResult unregisterContainer(
			ServerLevel level, UUID warehouseId, ContainerReference reference) {
		return unregisterContainer(level, warehouseId, reference, StorageBalanceAccess.requireCurrent());
	}

	public static WarehouseMutationResult unregisterContainer(
			ServerLevel level, UUID warehouseId, ContainerReference reference, StorageBalance balance) {
		Objects.requireNonNull(level, "level");
		Objects.requireNonNull(warehouseId, "warehouseId");
		Objects.requireNonNull(reference, "reference");
		Objects.requireNonNull(balance, "balance");
		WarehouseSavedData data = WarehouseSavedData.get(level);
		synchronized (data) {
			Optional<WarehouseRecord> found = data.warehouse(warehouseId);
			if (found.isEmpty()) {
				return WarehouseMutationResult.rejected(INVALID_WAREHOUSE);
			}
			WarehouseRecord warehouse = found.get();
			if (!warehouse.hasContainer(reference)) {
				return WarehouseMutationResult.rejected(CONTAINER_NOT_REGISTERED);
			}
			List<ContainerReference> nextContainers = warehouse.registeredContainers().stream()
					.filter(value -> !value.equals(reference))
					.toList();
			InventoryIndex index = new InventoryIndex(balance, warehouse.index());
			index.remove(reference);
			WarehouseRecord updated = warehouse.withContainers(nextContainers).withIndex(index.state());
			data.replace(data.state().with(updated));
			return WarehouseMutationResult.success(updated);
		}
	}

	public static IndexMutationResult refreshContainer(
			ServerLevel level, UUID warehouseId, ContainerReference reference) {
		return refreshContainer(level, warehouseId, reference, StorageBalanceAccess.requireCurrent());
	}

	public static IndexMutationResult refreshContainer(
			ServerLevel level, UUID warehouseId, ContainerReference reference, StorageBalance balance) {
		Objects.requireNonNull(level, "level");
		Objects.requireNonNull(warehouseId, "warehouseId");
		Objects.requireNonNull(reference, "reference");
		Objects.requireNonNull(balance, "balance");
		WarehouseSavedData data = WarehouseSavedData.get(level);
		synchronized (data) {
			Optional<WarehouseRecord> found = data.warehouse(warehouseId);
			if (found.isEmpty()) {
				return IndexMutationResult.rejected(INVALID_WAREHOUSE, 0);
			}
			WarehouseRecord warehouse = found.get();
			if (!warehouse.hasContainer(reference)) {
				return IndexMutationResult.rejected(CONTAINER_NOT_REGISTERED, 0);
			}
			Optional<Container> container = StorageContainerAccess.find(level, reference);
			if (container.isEmpty()) {
				return IndexMutationResult.rejected(CONTAINER_UNAVAILABLE, indexedSlots(warehouse, reference));
			}
			int size = container.get().getContainerSize();
			if (size < 0 || size > balance.maxRebuildSlots() || size > balance.maxIndexedSlots()) {
				return IndexMutationResult.rejected(INDEX_REBUILD_LIMIT, indexedSlots(warehouse, reference));
			}
			Optional<ContainerSnapshot> snapshot = StorageContainerAccess.snapshot(
					level, reference, balance.maxRebuildSlots());
			if (snapshot.isEmpty()) {
				return IndexMutationResult.rejected(CONTAINER_UNAVAILABLE, indexedSlots(warehouse, reference));
			}
			InventoryIndex index = new InventoryIndex(balance, warehouse.index());
			IndexMutationResult result = index.add(reference, snapshot.get().slots());
			if (!result.applied()) {
				return result;
			}
			WarehouseRecord updated = warehouse.withIndex(index.state());
			data.replace(data.state().with(updated));
			return result;
		}
	}

	public static IndexMutationResult invalidateContainer(
			ServerLevel level, UUID warehouseId, ContainerReference reference) {
		return invalidateContainer(level, warehouseId, reference, StorageBalanceAccess.requireCurrent());
	}

	public static IndexMutationResult invalidateContainer(
			ServerLevel level, UUID warehouseId, ContainerReference reference, StorageBalance balance) {
		Objects.requireNonNull(level, "level");
		Objects.requireNonNull(warehouseId, "warehouseId");
		Objects.requireNonNull(reference, "reference");
		Objects.requireNonNull(balance, "balance");
		WarehouseSavedData data = WarehouseSavedData.get(level);
		synchronized (data) {
			Optional<WarehouseRecord> found = data.warehouse(warehouseId);
			if (found.isEmpty()) {
				return IndexMutationResult.rejected(INVALID_WAREHOUSE, 0);
			}
			WarehouseRecord warehouse = found.get();
			if (!warehouse.hasContainer(reference)) {
				return IndexMutationResult.rejected(CONTAINER_NOT_REGISTERED, 0);
			}
			InventoryIndex index = new InventoryIndex(balance, warehouse.index());
			IndexMutationResult result = index.invalidate(reference);
			WarehouseRecord updated = warehouse.withIndex(index.state());
			data.replace(data.state().with(updated));
			return result;
		}
	}

	/** Rebuilds registered containers only, bounded by both container and slot budgets. */
	public static IndexMutationResult rebuildRegisteredIndex(
			ServerLevel level, UUID warehouseId, int requestedContainers) {
		return rebuildRegisteredIndex(level, warehouseId, requestedContainers, StorageBalanceAccess.requireCurrent());
	}

	public static IndexMutationResult rebuildRegisteredIndex(
			ServerLevel level, UUID warehouseId, int requestedContainers, StorageBalance balance) {
		Objects.requireNonNull(level, "level");
		Objects.requireNonNull(warehouseId, "warehouseId");
		Objects.requireNonNull(balance, "balance");
		if (requestedContainers < 1) {
			return IndexMutationResult.rejected(INDEX_REBUILD_LIMIT, 0);
		}
		WarehouseSavedData data = WarehouseSavedData.get(level);
		synchronized (data) {
			Optional<WarehouseRecord> found = data.warehouse(warehouseId);
			if (found.isEmpty()) {
				return IndexMutationResult.rejected(INVALID_WAREHOUSE, 0);
			}
			WarehouseRecord warehouse = found.get();
			InventoryIndex index = new InventoryIndex(balance, warehouse.index());
			int containerBudget = Math.min(requestedContainers, balance.maxRebuildContainers());
			int slotBudget = balance.maxRebuildSlots();
			int processed = 0;
			int indexed = 0;
			for (ContainerReference reference : warehouse.registeredContainers()) {
				if (processed >= containerBudget || slotBudget < 1) {
					break;
				}
				Optional<Container> container = StorageContainerAccess.find(level, reference);
				if (container.isEmpty()) {
					processed++;
					continue;
				}
				int size = container.get().getContainerSize();
				if (size < 0 || size > slotBudget || size > balance.maxRebuildSlots()) {
					break;
				}
				Optional<ContainerSnapshot> snapshot = StorageContainerAccess.snapshot(level, reference, size);
				if (snapshot.isEmpty()) {
					processed++;
					continue;
				}
				IndexMutationResult result = index.add(reference, snapshot.get().slots());
				if (!result.applied()) {
					return result;
				}
				indexed += result.indexedSlots();
				slotBudget -= size;
				processed++;
			}
			if (processed == 0) {
				return IndexMutationResult.rejected(CONTAINER_UNAVAILABLE, 0);
			}
			WarehouseRecord updated = warehouse.withIndex(index.state());
			data.replace(data.state().with(updated));
			return IndexMutationResult.success(indexed);
		}
	}

	/**
	 * Executes a bounded, read-only server query. Physical rows are rechecked when their chunks are
	 * loaded, so a stale quantity can never authorize a transfer or remain an available result.
	 */
	public static WarehouseSearchPage search(
			ServerLevel level, UUID warehouseId, WarehouseSearchRequest request) {
		return search(level, warehouseId, request, StorageBalanceAccess.requireCurrent());
	}

	public static WarehouseSearchPage search(
			ServerLevel level, UUID warehouseId, WarehouseSearchRequest request, StorageBalance balance) {
		Objects.requireNonNull(level, "level");
		Objects.requireNonNull(warehouseId, "warehouseId");
		Objects.requireNonNull(request, "request");
		Objects.requireNonNull(balance, "balance");
		if (request.validateFor(balance).error().isPresent()) {
			return WarehouseSearchPage.rejected("forever.storage.search_request_rejected");
		}
		WarehouseSavedData data = WarehouseSavedData.get(level);
		for (int attempt = 0; attempt < 2; attempt++) {
			Optional<WarehouseRecord> found = data.warehouse(warehouseId);
			if (found.isEmpty()) {
				return WarehouseSearchPage.rejected(INVALID_WAREHOUSE);
			}
			WarehouseRecord warehouse = found.get();
			if (!warehouse.dimension().equals(level.dimension())) {
				return WarehouseSearchPage.rejected(CROSS_DIMENSION);
			}
			reconcileInvalidated(level, data, warehouse, balance);
			warehouse = data.warehouse(warehouseId).orElse(warehouse);
			InventoryIndex index = new InventoryIndex(balance, warehouse.index());
			WarehouseSearchPage page = index.query(request);
			Set<ContainerReference> changed = new LinkedHashSet<>();
			List<WarehouseSearchResult> decorated = new ArrayList<>(page.results().size());
			for (WarehouseSearchResult result : page.results()) {
				Optional<Container> container = StorageContainerAccess.find(level, result.container());
				if (container.isEmpty()) {
					decorated.add(new WarehouseSearchResult(
								result.container(), result.slot(), result.itemId(), result.quantity(), result.damage(),
								result.maxDamage(), false, true, "unavailable", INDEX_UNAVAILABLE, result.fingerprint()));
					continue;
				}
				if (!matchesPhysical(container.get(), result)) {
					changed.add(result.container());
					continue;
				}
				decorated.add(new WarehouseSearchResult(
								result.container(), result.slot(), result.itemId(), result.quantity(), result.damage(),
								result.maxDamage(), true, result.stale(),
								result.stale() ? "stale" : "registered",
								result.stale() ? "forever.storage.index_stale" : "",
								result.fingerprint()));
			}
			if (!changed.isEmpty() && attempt == 0) {
				int refreshed = 0;
				for (ContainerReference reference : changed) {
					if (refreshed++ >= balance.maxRebuildContainers()) {
						break;
					}
					refreshContainer(level, warehouseId, reference, balance);
				}
				continue;
			}
			return new WarehouseSearchPage(
					page.protocolVersion(), decorated, page.hasMore(), page.totalKnown(), page.status(), page.explanationKey());
		}
		return WarehouseSearchPage.rejected(INDEX_REBUILD_LIMIT);
	}

	private static void reconcileInvalidated(
			ServerLevel level, WarehouseSavedData data, WarehouseRecord warehouse, StorageBalance balance) {
		int processedContainers = 0;
		int processedSlots = 0;
		for (ContainerReference reference : warehouse.index().invalidated()) {
			if (processedContainers >= balance.maxRebuildContainers()
					|| processedSlots >= balance.maxRebuildSlots()) {
				break;
			}
			Optional<Container> container = StorageContainerAccess.find(level, reference);
			if (container.isEmpty()) {
				processedContainers++;
				continue;
			}
			int size = container.get().getContainerSize();
			if (size < 0 || processedSlots + size > balance.maxRebuildSlots()) {
				break;
			}
			IndexMutationResult result = refreshContainer(level, warehouse.id(), reference, balance);
			if (result.applied()) {
				processedSlots += size;
			}
			processedContainers++;
		}
	}

	private static boolean matchesPhysical(Container container, WarehouseSearchResult result) {
		if (result.slot() < 0 || result.slot() >= container.getContainerSize()) {
			return false;
		}
		ItemStack stack = container.getItem(result.slot());
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
		if (itemId == null || !itemId.equals(result.itemId()) || stack.getCount() != result.quantity()) {
			return false;
		}
		int damage = stack.isDamageableItem() ? stack.getDamageValue() : 0;
		int maxDamage = stack.isDamageableItem() ? stack.getMaxDamage() : 0;
		return damage == result.damage()
				&& maxDamage == result.maxDamage()
				&& ItemStack.hashItemAndComponents(stack) == result.fingerprint();
	}

	private static int indexedSlots(WarehouseRecord warehouse, ContainerReference reference) {
		return (int) warehouse.index().entries().stream()
				.filter(entry -> entry.container().equals(reference))
				.count();
	}
}

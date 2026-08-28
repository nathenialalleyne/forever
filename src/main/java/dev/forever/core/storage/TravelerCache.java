package dev.forever.core.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/** Server-side operations for the bounded Traveler's Cache component. */
public final class TravelerCache {

	public static final String NOT_A_CACHE = "forever.storage.cache_not_cache";
	public static final String MISSING_CONTENTS = "forever.storage.cache_missing_contents";
	public static final String INVALID_CAPACITY = "forever.storage.cache_invalid_capacity";
	public static final String CAPACITY_FULL = "forever.storage.cache_capacity_full";
	public static final String ALREADY_EXPANDED = "forever.storage.cache_already_expanded";
	public static final String NOT_EXPANDABLE = "forever.storage.cache_not_expandable";

	private TravelerCache() {
	}

	public static boolean isCacheStack(ItemStack stack) {
		return stack != null && !stack.isEmpty() && stack.getItem() == TravelerCacheItem.ITEM;
	}

	public static ItemStack createEmptyStack(StorageBalance balance) {
		return TravelerCacheItem.createDefault(Objects.requireNonNull(balance, "balance"));
	}

	public static CacheValidation validate(ItemStack stack, StorageBalance balance) {
		Objects.requireNonNull(balance, "balance");
		if (!isCacheStack(stack)) {
			return CacheValidation.invalid(NOT_A_CACHE);
		}
		TravelerCacheContents contents = stack.get(TravelerCacheComponent.TYPE);
		if (contents == null) {
			return CacheValidation.invalid(MISSING_CONTENTS);
		}
		if (contents.validateFor(balance).error().isPresent()) {
			return CacheValidation.invalid(INVALID_CAPACITY);
		}
        return CacheValidation.accepted();
	}

	public static CacheOperation replaceContents(
			ItemStack cache, TravelerCacheContents contents, StorageBalance balance) {
		Objects.requireNonNull(contents, "contents");
		CacheValidation validation = validate(cache, balance);
		if (!validation.valid()) {
			return CacheOperation.rejected(validation.explanationKey());
		}
		if (contents.validateFor(balance).error().isPresent()) {
			return CacheOperation.rejected(INVALID_CAPACITY);
		}
		cache.set(TravelerCacheComponent.TYPE, contents);
		return CacheOperation.success(0);
	}

	/** Inserts the complete source stack or leaves both source and cache unchanged. */
	public static CacheOperation insert(ItemStack cache, ItemStack source, StorageBalance balance) {
		Objects.requireNonNull(source, "source");
		CacheValidation validation = validate(cache, balance);
		if (!validation.valid()) {
			return CacheOperation.rejected(validation.explanationKey());
		}
		if (source == cache || source.isEmpty()) {
			return CacheOperation.rejected(CAPACITY_FULL);
		}
		if (ContainerItemPredicate.isContainer(source)) {
			return CacheOperation.rejected(ContainerItemPredicate.REJECTION_REASON);
		}

		TravelerCacheContents contents = cache.get(TravelerCacheComponent.TYPE);
		List<ItemStack> planned = contents.copySlots();
		int remaining = source.getCount();
		for (int index = 0; index < planned.size() && remaining > 0; index++) {
			ItemStack existing = planned.get(index);
			if (existing.isEmpty()) {
				int moved = Math.min(remaining, source.getMaxStackSize());
				planned.set(index, source.copyWithCount(moved));
				remaining -= moved;
				continue;
			}
			if (!ItemStack.isSameItemSameComponents(existing, source)) {
				continue;
			}
			int limit = Math.min(existing.getMaxStackSize(), source.getMaxStackSize());
			int available = limit - existing.getCount();
			if (available > 0) {
				int moved = Math.min(remaining, available);
				planned.set(index, existing.copyWithCount(existing.getCount() + moved));
				remaining -= moved;
			}
		}
		if (remaining != 0) {
			return CacheOperation.rejected(CAPACITY_FULL);
		}

		cache.set(TravelerCacheComponent.TYPE, contents.withSlots(planned));
        int moved = source.getCount();
        source.setCount(0);
        return CacheOperation.success(moved);
	}

	/**
	 * Extracts one cache slot into one physical destination slot, atomically.
	 */
	public static CacheOperation extract(
			ItemStack cache,
			int cacheSlot,
			Container destination,
			int destinationSlot,
			int amount,
			StorageBalance balance) {
		Objects.requireNonNull(destination, "destination");
		CacheValidation validation = validate(cache, balance);
		if (!validation.valid()) {
			return CacheOperation.rejected(validation.explanationKey());
		}
		TravelerCacheContents contents = cache.get(TravelerCacheComponent.TYPE);
		if (cacheSlot < 0 || cacheSlot >= contents.capacity() || amount < 1
				|| destinationSlot < 0 || destinationSlot >= destination.getContainerSize()) {
			return CacheOperation.rejected(CAPACITY_FULL);
		}
		ItemStack source = contents.slot(cacheSlot);
		if (source.isEmpty() || source.getCount() < amount) {
			return CacheOperation.rejected(MISSING_CONTENTS);
		}
		ItemStack destinationBefore = destination.getItem(destinationSlot);
		if (destinationBefore == null || !destination.canPlaceItem(destinationSlot, source.copyWithCount(amount))) {
			return CacheOperation.rejected(PhysicalInventoryTransfer.DESTINATION_FULL);
		}
		ItemStack moved = source.copyWithCount(amount);
		int limit = Math.min(destination.getMaxStackSize(), moved.getMaxStackSize());
		if (destinationBefore.isEmpty()) {
			if (amount > limit) {
				return CacheOperation.rejected(PhysicalInventoryTransfer.DESTINATION_FULL);
			}
		} else if (!ItemStack.isSameItemSameComponents(source, destinationBefore)
				|| destinationBefore.getCount() > limit - amount) {
			return CacheOperation.rejected(PhysicalInventoryTransfer.DESTINATION_FULL);
		}

		List<ItemStack> planned = contents.copySlots();
		planned.set(cacheSlot, source.copyWithCount(source.getCount() - amount));
		ItemStack destinationAfter = destinationBefore.isEmpty()
				? moved
				: destinationBefore.copyWithCount(destinationBefore.getCount() + amount);
		cache.set(TravelerCacheComponent.TYPE, contents.withSlots(planned));
		destination.setItem(destinationSlot, destinationAfter);
		destination.setChanged();
		return CacheOperation.success(amount);
	}

	/** Expands a valid nine-slot cache to the configured expanded capacity. */
	public static CacheOperation expand(ItemStack cache, StorageBalance balance) {
		CacheValidation validation = validate(cache, balance);
		if (!validation.valid()) {
			return CacheOperation.rejected(validation.explanationKey());
		}
		TravelerCacheContents contents = cache.get(TravelerCacheComponent.TYPE);
		if (contents.expanded()) {
			return CacheOperation.rejected(ALREADY_EXPANDED);
		}
		if (balance.expandedCacheSlots() <= balance.initialCacheSlots()) {
			return CacheOperation.rejected(NOT_EXPANDABLE);
		}
		List<ItemStack> expanded = new ArrayList<>(contents.copySlots());
		while (expanded.size() < balance.expandedCacheSlots()) {
			expanded.add(ItemStack.EMPTY);
		}
		cache.set(TravelerCacheComponent.TYPE,
				new TravelerCacheContents(TravelerCacheContents.CURRENT_SCHEMA, true, expanded));
		return CacheOperation.success(0);
	}
}

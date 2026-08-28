package dev.forever.core.storage;

import java.util.Objects;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Performs a bounded, all-or-nothing move between already acquired physical inventories.
 *
 * <p>This service has no world lookup and no remote endpoint. A caller must already have both
 * physical containers, so it cannot turn a search result into magical remote withdrawal.
 */
public final class PhysicalInventoryTransfer {

	public static final String INVALID_REQUEST = "forever.storage.transfer_invalid_request";
	public static final String SOURCE_CHANGED = "forever.storage.transfer_source_changed";
	public static final String DESTINATION_FULL = "forever.storage.transfer_destination_full";

	private PhysicalInventoryTransfer() {
	}

	public static PhysicalTransferResult move(
			Container source, int sourceSlot, Container destination, int destinationSlot, int amount) {
		Objects.requireNonNull(source, "source");
		Objects.requireNonNull(destination, "destination");
		if (sourceSlot < 0 || sourceSlot >= source.getContainerSize()
				|| destinationSlot < 0 || destinationSlot >= destination.getContainerSize()
				|| amount < 1
				|| source == destination && sourceSlot == destinationSlot) {
			return PhysicalTransferResult.rejected(INVALID_REQUEST);
		}

		ItemStack sourceBefore = source.getItem(sourceSlot);
		ItemStack destinationBefore = destination.getItem(destinationSlot);
		if (sourceBefore == null || destinationBefore == null || sourceBefore.isEmpty()
				|| sourceBefore.getCount() < amount) {
			return PhysicalTransferResult.rejected(SOURCE_CHANGED);
		}
		if (ContainerItemPredicate.isContainer(sourceBefore)) {
			return PhysicalTransferResult.rejected(ContainerItemPredicate.REJECTION_REASON);
		}

		ItemStack moved = sourceBefore.copyWithCount(amount);
		if (!destination.canPlaceItem(destinationSlot, moved)) {
			return PhysicalTransferResult.rejected(DESTINATION_FULL);
		}

		int destinationLimit = Math.min(
				Math.min(destination.getMaxStackSize(), moved.getMaxStackSize()),
				destinationBefore.isEmpty() ? moved.getMaxStackSize() : destinationBefore.getMaxStackSize());
		if (destinationBefore.isEmpty()) {
			if (amount > destinationLimit) {
				return PhysicalTransferResult.rejected(DESTINATION_FULL);
			}
		} else if (!ItemStack.isSameItemSameComponents(sourceBefore, destinationBefore)
				|| destinationBefore.getCount() > destinationLimit - amount) {
			return PhysicalTransferResult.rejected(DESTINATION_FULL);
		}

		ItemStack sourceAfter = sourceBefore.copyWithCount(sourceBefore.getCount() - amount);
		ItemStack destinationAfter = destinationBefore.isEmpty()
				? moved
				: destinationBefore.copyWithCount(destinationBefore.getCount() + amount);

		// All validation is complete before either physical slot is changed.
		source.setItem(sourceSlot, sourceAfter);
		destination.setItem(destinationSlot, destinationAfter);
		source.setChanged();
		if (destination != source) {
			destination.setChanged();
		}
		return PhysicalTransferResult.success(amount);
	}
}

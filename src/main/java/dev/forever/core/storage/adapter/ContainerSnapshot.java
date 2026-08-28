package dev.forever.core.storage.adapter;

import java.util.List;
import net.minecraft.world.item.ItemStack;

/** A bounded read-only copy of one loaded physical container for index construction. */
record ContainerSnapshot(List<ItemStack> slots) {

	ContainerSnapshot {
		slots = slots.stream().map(ItemStack::copy).toList();
	}

	@Override
	public List<ItemStack> slots() {
		return slots.stream().map(ItemStack::copy).toList();
	}
}

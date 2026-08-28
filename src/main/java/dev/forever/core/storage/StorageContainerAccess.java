package dev.forever.core.storage;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Explicit, non-loading access to a registered block-backed physical inventory.
 *
 * <p>Every lookup checks that the chunk is already loaded. This is the boundary that prevents
 * indexing from becoming a hidden chunk loader.
 */
public final class StorageContainerAccess {

	private StorageContainerAccess() {
	}

	public static Optional<Container> find(ServerLevel level, ContainerReference reference) {
		Objects.requireNonNull(level, "level");
		Objects.requireNonNull(reference, "reference");
		if (!level.dimension().equals(reference.dimension())
				|| !level.isInValidBounds(reference.position())
				|| !level.isLoaded(reference.position())) {
			return Optional.empty();
		}
		if (!(level.getBlockEntity(reference.position()) instanceof Container container)) {
			return Optional.empty();
		}
		return Optional.of(container);
	}

	public static Optional<ItemStack> readSlot(ServerLevel level, ContainerReference reference, int slot) {
		return find(level, reference).flatMap(container -> {
			if (slot < 0 || slot >= container.getContainerSize()) {
				return Optional.empty();
			}
			ItemStack stack = container.getItem(slot);
			return stack == null ? Optional.empty() : Optional.of(stack.copy());
		});
	}

	static Optional<ContainerSnapshot> snapshot(ServerLevel level, ContainerReference reference, int maxSlots) {
		if (maxSlots < 1) {
			throw new IllegalArgumentException("snapshot slot bound must be positive");
		}
		Optional<Container> found = find(level, reference);
		if (found.isEmpty()) {
			return Optional.empty();
		}
		Container container = found.get();
		int size = container.getContainerSize();
		if (size < 0 || size > maxSlots) {
			return Optional.empty();
		}
		java.util.ArrayList<ItemStack> slots = new java.util.ArrayList<>(size);
		for (int slot = 0; slot < size; slot++) {
			ItemStack stack = container.getItem(slot);
			if (stack == null) {
				throw new IllegalStateException(
						"registered container " + reference + " returned null for slot " + slot);
			}
			slots.add(stack.copy());
		}
		return Optional.of(new ContainerSnapshot(slots));
	}
}

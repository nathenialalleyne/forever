package dev.forever.core.storage;

import java.util.Locale;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.SeededContainerLoot;

/**
 * The single server-side predicate for deciding whether an item carries an inventory.
 *
 * <p>It intentionally does not enumerate item IDs. Vanilla component types, item-level
 * container capability, component values supplied by another mod, and conventional names for
 * custom inventory components are all considered. This makes a modded backpack fail closed
 * without coupling Forever to that mod's registry.
 */
public final class ContainerItemPredicate {

	public static final String REJECTION_REASON = "forever.storage.container_nesting_rejected";

	private ContainerItemPredicate() {
	}

	public static boolean isContainer(ItemStack stack) {
		if (stack == null || stack.isEmpty()) {
			return false;
		}
		if (!stack.getItem().canFitInsideContainerItems()) {
			return true;
		}
		for (TypedDataComponent<?> component : stack.getComponents()) {
			if (isContainerComponent(component)) {
				return true;
			}
		}
		return false;
	}

	public static String rejectionReason() {
		return REJECTION_REASON;
	}

	private static boolean isContainerComponent(TypedDataComponent<?> component) {
		Object value = component.value();
		if (value instanceof ItemContainerContents
				|| value instanceof BundleContents
				|| value instanceof SeededContainerLoot
				|| value instanceof TravelerCacheContents
				|| value instanceof ItemStack
				|| value instanceof ItemStackTemplate) {
			return true;
		}
		if (value instanceof net.minecraft.world.item.component.ChargedProjectiles) {
			// A crossbow's ammunition is not an inventory and must remain usable in a cache.
			return false;
		}

		DataComponentType<?> type = component.type();
		if (hasInventoryName(type)) {
			return true;
		}
		return containsStackCollection(value);
	}

	private static boolean hasInventoryName(DataComponentType<?> type) {
		var id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
		if (id == null) {
			return false;
		}
		String path = id.getPath().toLowerCase(Locale.ROOT);
		return path.contains("container")
				|| path.contains("inventory")
				|| path.contains("backpack")
				|| path.contains("satchel")
				|| path.contains("pouch")
				|| path.contains("storage")
				|| path.contains("contents")
				|| path.contains("bundle")
				|| path.contains("shulker");
	}

	private static boolean containsStackCollection(Object value) {
		if (value instanceof Iterable<?> iterable) {
			for (Object element : iterable) {
				if (element instanceof ItemStack || element instanceof ItemStackTemplate) {
					return true;
				}
			}
		}
		if (value instanceof java.util.Map<?, ?> map) {
			for (Object element : map.values()) {
				if (element instanceof ItemStack || element instanceof ItemStackTemplate) {
					return true;
				}
			}
		}
		return false;
	}
}

package dev.forever.core.economy.adapter;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Physical, transferable Obol item. It intentionally has no hidden purse inventory. */
public final class ObolItem extends Item {

	public static final Identifier ID = Identifier.fromNamespaceAndPath("forever", "obol");
	public static final Item ITEM = register();

	private ObolItem(Properties properties) {
		super(properties);
	}

	public static ItemStack stack(int count) {
		if (count < 1) {
			throw new IllegalArgumentException("An Obol stack count must be positive.");
		}
		return new ItemStack(ITEM, count);
	}

	public static boolean isObol(ItemStack stack) {
		return stack != null && !stack.isEmpty() && stack.getItem() == ITEM;
	}

	/** Forces item registration from the explicit economy system entrypoint. */
	public static void initialize() {
		ITEM.builtInRegistryHolder();
	}

	private static Item register() {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ID);
		return Registry.register(BuiltInRegistries.ITEM, ID,
				new ObolItem(new Item.Properties().setId(key).stacksTo(64)));
	}
}

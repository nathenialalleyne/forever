package dev.forever.core.storage.adapter;

import dev.forever.core.storage.application.StorageBalanceAccess;
import dev.forever.core.storage.domain.StorageBalance;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Personal, bounded Traveler's Cache item. */
public final class TravelerCacheItem extends Item {

	public static final Identifier ID = Identifier.fromNamespaceAndPath("forever", "travelers_cache");
	public static final Item ITEM = register();

	private TravelerCacheItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean canFitInsideContainerItems() {
		return false;
	}

	@Override
	public ItemStack getDefaultInstance() {
		return createDefault(StorageBalanceAccess.requireCurrent());
	}

	public static ItemStack createDefault(StorageBalance balance) {
		ItemStack stack = new ItemStack(ITEM);
		stack.set(TravelerCacheComponent.TYPE, TravelerCacheContents.empty(balance));
		return stack;
	}

	/** Forces item registration from the explicit storage system entrypoint. */
	public static void initialize() {
		ITEM.builtInRegistryHolder();
	}

	private static Item register() {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ID);
		return Registry.register(
				BuiltInRegistries.ITEM,
				ID,
				new TravelerCacheItem(new Item.Properties().setId(key).stacksTo(1)));
	}
}

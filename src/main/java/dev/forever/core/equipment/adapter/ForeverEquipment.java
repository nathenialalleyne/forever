package dev.forever.core.equipment.adapter;

import dev.forever.core.ForeverIdentifiers;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Registration and lifecycle owner for the equipment package. */
public final class ForeverEquipment {

	private static final Logger LOGGER = LoggerFactory.getLogger("forever/equipment");

	public static final Identifier FIELD_TOOL_ID = ForeverIdentifiers.of("field_tool");

	/** A small supported item used by the vertical slice and its dedicated-server tests. */
	public static final Item FIELD_TOOL = registerFieldTool();

	private static boolean initialized;

	private ForeverEquipment() {
	}

	/**
	 * Registers the equipment balance reload listener and tooltip provider.
	 *
	 * <p>The common mod entrypoint is intentionally outside this ticket's ownership scope. The
	 * entrypoint must call this method during mod initialisation in the production mod.
	 */
	public static synchronized void initialize() {
		if (initialized) {
			return;
		}
		EquipmentBalanceLoader.installBundled();
		EquipmentBalanceLoader.register();
		ItemComponentTooltipProviderRegistry.addLast(EquipmentComponents.STATE);
		initialized = true;
		LOGGER.info("Forever equipment registrations initialised");
	}

	private static Item registerFieldTool() {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, FIELD_TOOL_ID);
		Item.Properties properties = new Item.Properties().setId(key).durability(1);
		((FabricItem.Properties) properties).customDamage(EquipmentDamageHandler.INSTANCE);
		return Registry.register(BuiltInRegistries.ITEM, FIELD_TOOL_ID, new EquipmentItem(properties));
	}
}

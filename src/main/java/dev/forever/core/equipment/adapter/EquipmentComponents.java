package dev.forever.core.equipment.adapter;

import dev.forever.core.ForeverIdentifiers;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

/** Registry owner for Forever's per-stack equipment component. */
public final class EquipmentComponents {

	public static final Identifier STATE_ID = ForeverIdentifiers.of("equipment_state");

	public static final DataComponentType<EquipmentState> STATE = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			STATE_ID,
			DataComponentType.<EquipmentState>builder()
					.persistent(EquipmentState.CODEC)
					.networkSynchronized(ByteBufCodecs.fromCodecTrusted(EquipmentState.CODEC))
					.cacheEncoding()
					.build());

	private EquipmentComponents() {
	}
}

package dev.forever.core.storage.adapter;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

/** Registered persistent ItemStack component for Traveler's Cache contents. */
public final class TravelerCacheComponent {

	public static final Identifier ID = Identifier.fromNamespaceAndPath("forever", "traveler_cache_contents");
	public static final DataComponentType<TravelerCacheContents> TYPE = Registry.register(
			BuiltInRegistries.DATA_COMPONENT_TYPE,
			ID,
			DataComponentType.<TravelerCacheContents>builder()
					.persistent(TravelerCacheContents.CODEC)
					.networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(TravelerCacheContents.CODEC))
					.build());

	private TravelerCacheComponent() {
	}

	/** Forces registration from the explicit storage system entrypoint. */
	public static void initialize() {
		TYPE.codec();
	}
}

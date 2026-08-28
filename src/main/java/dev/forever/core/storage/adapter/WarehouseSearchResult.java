package dev.forever.core.storage.adapter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.storage.domain.StorageSafetyLimits;
import java.util.Objects;
import net.minecraft.resources.Identifier;

/** Bounded, read-only projection of one indexed physical slot. */
public record WarehouseSearchResult(
		ContainerReference container,
		int slot,
		Identifier itemId,
		int quantity,
		int damage,
		int maxDamage,
		boolean available,
		boolean stale,
		String accessStatus,
		String explanationKey,
		int fingerprint) {

	public static final Codec<WarehouseSearchResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ContainerReference.CODEC.fieldOf("container").forGetter(WarehouseSearchResult::container),
			Codec.INT.fieldOf("slot").forGetter(WarehouseSearchResult::slot),
			Identifier.CODEC.fieldOf("item_id").forGetter(WarehouseSearchResult::itemId),
			Codec.INT.fieldOf("quantity").forGetter(WarehouseSearchResult::quantity),
			Codec.INT.fieldOf("damage").forGetter(WarehouseSearchResult::damage),
			Codec.INT.fieldOf("max_damage").forGetter(WarehouseSearchResult::maxDamage),
			Codec.BOOL.fieldOf("available").forGetter(WarehouseSearchResult::available),
				Codec.BOOL.fieldOf("stale").forGetter(WarehouseSearchResult::stale),
				Codec.STRING.fieldOf("access_status").forGetter(WarehouseSearchResult::accessStatus),
				Codec.STRING.fieldOf("explanation_key").forGetter(WarehouseSearchResult::explanationKey),
				Codec.INT.fieldOf("fingerprint").forGetter(WarehouseSearchResult::fingerprint)
			).apply(instance, WarehouseSearchResult::new));

	/** Compatibility constructor for callers that only have the visible projection. */
	public WarehouseSearchResult(
			ContainerReference container,
			int slot,
			net.minecraft.resources.Identifier itemId,
			int quantity,
			int damage,
			int maxDamage,
			boolean available,
			boolean stale,
			String accessStatus,
			String explanationKey) {
		this(container, slot, itemId, quantity, damage, maxDamage, available, stale,
				accessStatus, explanationKey, 0);
	}

	public WarehouseSearchResult {
		container = Objects.requireNonNull(container, "container");
		if (slot < 0) {
			throw new IllegalArgumentException("search result slot must not be negative");
		}
		itemId = Objects.requireNonNull(itemId, "itemId");
		if (quantity < 1 || quantity > 99) {
			throw new IllegalArgumentException("search result quantity must be between 1 and 99");
		}
		if (damage < 0 || maxDamage < 0) {
			throw new IllegalArgumentException("search result damage values must not be negative");
		}
		accessStatus = boundedText(accessStatus, "accessStatus");
		explanationKey = boundedText(explanationKey, "explanationKey");
	}

	private static String boundedText(String value, String name) {
		Objects.requireNonNull(value, name);
		if (value.length() > StorageSafetyLimits.MAX_QUERY_TEXT_LENGTH) {
			throw new IllegalArgumentException(name + " is too long");
		}
		return value;
	}
}

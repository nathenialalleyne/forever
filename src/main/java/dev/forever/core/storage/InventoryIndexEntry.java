package dev.forever.core.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.resources.Identifier;

/**
 * A derived summary for one non-empty physical slot.
 *
 * <p>This record deliberately contains no ItemStack. The physical slot remains the source of
 * truth, while the fingerprint and observed count let a query detect a changed loaded slot.
 */
public record InventoryIndexEntry(
		ContainerReference container,
		int slot,
		Identifier itemId,
		int quantity,
		int damage,
		int maxDamage,
		int fingerprint,
		long containerRevision,
		boolean available) {

	private record Raw(
			ContainerReference container,
			int slot,
			Identifier itemId,
			int quantity,
			int damage,
			int maxDamage,
			int fingerprint,
			long containerRevision,
			boolean available) {
	}

	private static final Codec<Raw> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ContainerReference.CODEC.fieldOf("container").forGetter(Raw::container),
			Codec.INT.fieldOf("slot").forGetter(Raw::slot),
			Identifier.CODEC.fieldOf("item_id").forGetter(Raw::itemId),
			Codec.INT.fieldOf("quantity").forGetter(Raw::quantity),
			Codec.INT.fieldOf("damage").forGetter(Raw::damage),
			Codec.INT.fieldOf("max_damage").forGetter(Raw::maxDamage),
			Codec.INT.fieldOf("fingerprint").forGetter(Raw::fingerprint),
			Codec.LONG.fieldOf("container_revision").forGetter(Raw::containerRevision),
			Codec.BOOL.fieldOf("available").forGetter(Raw::available)
		).apply(instance, Raw::new));

	public static final Codec<InventoryIndexEntry> CODEC = RAW_CODEC.comapFlatMap(
			InventoryIndexEntry::fromRaw,
			entry -> new Raw(
					entry.container(),
					entry.slot(),
					entry.itemId(),
					entry.quantity(),
					entry.damage(),
					entry.maxDamage(),
					entry.fingerprint(),
					entry.containerRevision(),
					entry.available()));

	public InventoryIndexEntry {
		container = Objects.requireNonNull(container, "container");
		if (slot < 0) {
			throw new IllegalArgumentException("indexed slot must not be negative");
		}
		itemId = Objects.requireNonNull(itemId, "itemId");
		if (quantity < 1 || quantity > 99) {
			throw new IllegalArgumentException("indexed quantity must be between 1 and 99");
		}
		if (damage < 0 || maxDamage < 0) {
			throw new IllegalArgumentException("indexed damage values must not be negative");
		}
		if (containerRevision < 0) {
			throw new IllegalArgumentException("indexed container revision must not be negative");
		}
	}

	private static DataResult<InventoryIndexEntry> fromRaw(Raw raw) {
		try {
			return DataResult.success(new InventoryIndexEntry(
					raw.container(),
				raw.slot(),
				raw.itemId(),
				raw.quantity(),
				raw.damage(),
				raw.maxDamage(),
				raw.fingerprint(),
				raw.containerRevision(),
				raw.available()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}
}

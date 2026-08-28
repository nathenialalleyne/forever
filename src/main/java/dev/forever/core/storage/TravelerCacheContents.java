package dev.forever.core.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.data.SchemaVersioned;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Versioned physical contents of a Traveler's Cache item component.
 *
 * <p>ItemStacks are copied at the boundary. Callers cannot mutate the component's stored list
 * through an object they supplied, and every constructor path applies the no-nesting predicate.
 */
public record TravelerCacheContents(
		int schemaVersion,
		boolean expanded,
		List<net.minecraft.world.item.ItemStack> slots) {

	public static final int CURRENT_SCHEMA = 2;

	private record Raw(
			int schemaVersion,
			boolean expanded,
			List<net.minecraft.world.item.ItemStack> slots) {
	}

	private static final Codec<List<net.minecraft.world.item.ItemStack>> SLOTS_CODEC =
			net.minecraft.world.item.ItemStack.OPTIONAL_CODEC.listOf().validate(slots ->
				slots.size() > StorageSafetyLimits.MAX_PERSISTED_CACHE_SLOTS
						? DataResult.error(() -> "traveler's cache contains too many slots")
						: DataResult.success(slots));

	private static final Codec<Raw> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("schema_version").forGetter(Raw::schemaVersion),
			Codec.BOOL.optionalFieldOf("expanded", false).forGetter(Raw::expanded),
			SLOTS_CODEC.fieldOf("slots").forGetter(Raw::slots)
		).apply(instance, Raw::new));

	private static final Codec<TravelerCacheContents> BASE_CODEC = RAW_CODEC.comapFlatMap(
			TravelerCacheContents::fromRaw,
			contents -> new Raw(contents.schemaVersion(), contents.expanded(), contents.slots()));

	/** Persistent codec. Older schema 1 records default expansion to false. */
	public static final Codec<TravelerCacheContents> CODEC = SchemaVersioned.migrating(
			BASE_CODEC,
			TravelerCacheContents::schemaVersion,
			CURRENT_SCHEMA,
			TravelerCacheContents::migrate);

	public TravelerCacheContents {
		if (schemaVersion < 1) {
			throw new IllegalArgumentException("traveler's cache schema version must be at least 1");
		}
		Objects.requireNonNull(slots, "slots");
		if (slots.size() > StorageSafetyLimits.MAX_PERSISTED_CACHE_SLOTS) {
			throw new IllegalArgumentException("traveler's cache contains too many slots");
		}
		List<net.minecraft.world.item.ItemStack> copied = new ArrayList<>(slots.size());
		for (net.minecraft.world.item.ItemStack slot : slots) {
			Objects.requireNonNull(slot, "cache slots cannot contain null");
			if (ContainerItemPredicate.isContainer(slot)) {
				throw new IllegalArgumentException(ContainerItemPredicate.REJECTION_REASON);
			}
			copied.add(slot.copy());
		}
		slots = List.copyOf(copied);
	}

	/** Returns defensive copies so callers cannot mutate persistent component state. */
	@Override
	public List<net.minecraft.world.item.ItemStack> slots() {
		return slots.stream().map(net.minecraft.world.item.ItemStack::copy).toList();
	}

	public static TravelerCacheContents empty(StorageBalance balance) {
		return empty(false, balance);
	}

	public static TravelerCacheContents empty(boolean expanded, StorageBalance balance) {
		Objects.requireNonNull(balance, "balance");
		int size = expanded ? balance.expandedCacheSlots() : balance.initialCacheSlots();
		return new TravelerCacheContents(CURRENT_SCHEMA, expanded, emptySlots(size));
	}

	public int capacity() {
		return slots.size();
	}

	public net.minecraft.world.item.ItemStack slot(int index) {
		return slots.get(index).copy();
	}

	TravelerCacheContents withSlot(int index, net.minecraft.world.item.ItemStack value) {
		List<net.minecraft.world.item.ItemStack> next = copySlots();
		next.set(index, value.copy());
		return new TravelerCacheContents(CURRENT_SCHEMA, expanded, next);
	}

	TravelerCacheContents withSlots(List<net.minecraft.world.item.ItemStack> nextSlots) {
		return new TravelerCacheContents(CURRENT_SCHEMA, expanded, nextSlots);
	}

	List<net.minecraft.world.item.ItemStack> copySlots() {
		return slots.stream().map(net.minecraft.world.item.ItemStack::copy).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
	}

	public DataResult<TravelerCacheContents> validateFor(StorageBalance balance) {
		Objects.requireNonNull(balance, "balance");
		int expected = expanded ? balance.expandedCacheSlots() : balance.initialCacheSlots();
		if (slots.size() != expected) {
			return DataResult.error(() -> "traveler's cache has " + slots.size()
					+ " slots but the active balance requires " + expected);
		}
		return DataResult.success(this);
	}

	static Codec<TravelerCacheContents> rawCodec() {
		return BASE_CODEC;
	}

	private static List<net.minecraft.world.item.ItemStack> emptySlots(int size) {
		List<net.minecraft.world.item.ItemStack> slots = new ArrayList<>(size);
		for (int index = 0; index < size; index++) {
			slots.add(net.minecraft.world.item.ItemStack.EMPTY);
		}
		return slots;
	}

	private static DataResult<TravelerCacheContents> fromRaw(Raw raw) {
		try {
			return DataResult.success(new TravelerCacheContents(raw.schemaVersion(), raw.expanded(), raw.slots()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	private static DataResult<TravelerCacheContents> migrate(
			TravelerCacheContents value, int storedVersion, int targetVersion) {
		if (storedVersion == 1 && targetVersion == CURRENT_SCHEMA) {
			return DataResult.success(new TravelerCacheContents(targetVersion, false, value.slots()));
		}
		return DataResult.error(() -> "traveler's cache has no migration from schema "
				+ storedVersion + " to schema " + targetVersion);
	}
}

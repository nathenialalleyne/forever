package dev.forever.core.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.data.SchemaVersioned;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Versioned, immutable persisted snapshot of a warehouse's derived index. */
public record InventoryIndexState(
		int schemaVersion,
		List<InventoryIndexEntry> entries,
		List<ContainerReference> invalidated) {

	public static final int CURRENT_SCHEMA = 1;
	private static final Comparator<ContainerReference> CONTAINER_ORDER = Comparator
                .comparing((ContainerReference reference) -> reference.dimension().identifier().toString())
			.thenComparingLong(reference -> reference.position().asLong());
	private static final Comparator<InventoryIndexEntry> ENTRY_ORDER = Comparator
			.comparing((InventoryIndexEntry entry) -> entry.container(), CONTAINER_ORDER)
			.thenComparingInt(InventoryIndexEntry::slot)
			.thenComparing(entry -> entry.itemId().toString());

	private record Raw(
			int schemaVersion,
			List<InventoryIndexEntry> entries,
			List<ContainerReference> invalidated) {
	}

	private static final Codec<List<InventoryIndexEntry>> ENTRIES_CODEC = InventoryIndexEntry.CODEC.listOf()
			.validate(entries -> entries.size() > StorageSafetyLimits.MAX_PERSISTED_INDEX_ENTRIES
					? DataResult.error(() -> "storage index contains too many entries")
					: DataResult.success(entries));
	private static final Codec<List<ContainerReference>> INVALIDATED_CODEC = ContainerReference.CODEC.listOf()
			.validate(references -> references.size() > StorageSafetyLimits.MAX_PERSISTED_CONTAINERS
					? DataResult.error(() -> "storage index contains too many invalidated containers")
					: DataResult.success(references));
	private static final Codec<Raw> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("schema_version").forGetter(Raw::schemaVersion),
			ENTRIES_CODEC.fieldOf("entries").forGetter(Raw::entries),
			INVALIDATED_CODEC.fieldOf("invalidated").forGetter(Raw::invalidated)
		).apply(instance, Raw::new));

	private static final Codec<InventoryIndexState> BASE_CODEC = RAW_CODEC.comapFlatMap(
			InventoryIndexState::fromRaw,
			state -> new Raw(state.schemaVersion(), state.entries(), state.invalidated()));

	public static final Codec<InventoryIndexState> CODEC = SchemaVersioned.migrating(
			BASE_CODEC,
			InventoryIndexState::schemaVersion,
			CURRENT_SCHEMA,
			SchemaVersioned.noMigrationsYet("storage inventory index"));

	public InventoryIndexState {
		if (schemaVersion < 1) {
			throw new IllegalArgumentException("storage index schema version must be at least 1");
		}
		entries = immutableSortedEntries(entries);
		invalidated = immutableSortedContainers(invalidated);
		if (entries.size() > StorageSafetyLimits.MAX_PERSISTED_INDEX_ENTRIES) {
			throw new IllegalArgumentException("storage index contains too many entries");
		}
		if (invalidated.size() > StorageSafetyLimits.MAX_PERSISTED_CONTAINERS) {
			throw new IllegalArgumentException("storage index contains too many invalidated containers");
		}
	}

	public static InventoryIndexState empty() {
		return new InventoryIndexState(CURRENT_SCHEMA, List.of(), List.of());
	}

	InventoryIndexState withEntries(List<InventoryIndexEntry> nextEntries, List<ContainerReference> nextInvalidated) {
		return new InventoryIndexState(schemaVersion, nextEntries, nextInvalidated);
	}

	private static DataResult<InventoryIndexState> fromRaw(Raw raw) {
		try {
			return DataResult.success(new InventoryIndexState(raw.schemaVersion(), raw.entries(), raw.invalidated()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	private static List<InventoryIndexEntry> immutableSortedEntries(List<InventoryIndexEntry> values) {
		Objects.requireNonNull(values, "entries");
		List<InventoryIndexEntry> sorted = new ArrayList<>(values);
		for (InventoryIndexEntry entry : sorted) {
			Objects.requireNonNull(entry, "entries cannot contain null");
		}
		sorted.sort(ENTRY_ORDER);
		Set<String> keys = new HashSet<>();
		for (InventoryIndexEntry entry : sorted) {
			String key = entry.container() + "#" + entry.slot();
			if (!keys.add(key)) {
				throw new IllegalArgumentException("storage index contains duplicate slot " + key);
			}
		}
		return List.copyOf(sorted);
	}

	private static List<ContainerReference> immutableSortedContainers(List<ContainerReference> values) {
		Objects.requireNonNull(values, "invalidated");
		List<ContainerReference> sorted = new ArrayList<>(values);
		for (ContainerReference reference : sorted) {
			Objects.requireNonNull(reference, "invalidated cannot contain null");
		}
		sorted.sort(CONTAINER_ORDER);
		Set<ContainerReference> unique = new HashSet<>(sorted);
		if (unique.size() != sorted.size()) {
			throw new IllegalArgumentException("storage index contains duplicate invalidated containers");
		}
		return List.copyOf(sorted);
	}
}

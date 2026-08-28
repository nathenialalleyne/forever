package dev.forever.core.storage.adapter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.storage.domain.StorageSafetyLimits;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** Immutable registration and derived-index state for one dimension-scoped warehouse. */
public record WarehouseRecord(
		UUID id,
		ResourceKey<Level> dimension,
		BlockPos controller,
		List<ContainerReference> registeredContainers,
		InventoryIndexState index,
		long revision) {

	private static final Comparator<ContainerReference> CONTAINER_ORDER = Comparator
			.comparing((ContainerReference reference) -> reference.dimension().identifier().toString())
			.thenComparingLong(reference -> reference.position().asLong());
	private static final Codec<List<ContainerReference>> CONTAINERS_CODEC = ContainerReference.CODEC.listOf()
			.validate(containers -> containers.size() > StorageSafetyLimits.MAX_PERSISTED_CONTAINERS
					? DataResult.error(() -> "warehouse contains too many registered containers")
				: DataResult.success(containers));
	private static final Codec<ResourceKey<Level>> DIMENSION_CODEC = ResourceKey.codec(Registries.DIMENSION);

	private record Raw(
			UUID id,
			ResourceKey<Level> dimension,
			BlockPos controller,
			List<ContainerReference> registeredContainers,
			InventoryIndexState index,
			long revision) {
	}

	private static final Codec<Raw> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			StorageCodecs.UUID_CODEC.fieldOf("id").forGetter(Raw::id),
				DIMENSION_CODEC.fieldOf("dimension").forGetter(Raw::dimension),
			BlockPos.CODEC.fieldOf("controller").forGetter(Raw::controller),
			CONTAINERS_CODEC.fieldOf("registered_containers").forGetter(Raw::registeredContainers),
			InventoryIndexState.CODEC.fieldOf("index").forGetter(Raw::index),
			Codec.LONG.fieldOf("revision").forGetter(Raw::revision)
		).apply(instance, Raw::new));

	public static final Codec<WarehouseRecord> CODEC = RAW_CODEC.comapFlatMap(
			WarehouseRecord::fromRaw,
			record -> new Raw(
					record.id(),
				record.dimension(),
				record.controller(),
				record.registeredContainers(),
				record.index(),
				record.revision()));

	public WarehouseRecord {
		id = Objects.requireNonNull(id, "id");
		dimension = Objects.requireNonNull(dimension, "dimension");
		controller = Objects.requireNonNull(controller, "controller").immutable();
		registeredContainers = immutableContainers(registeredContainers);
		for (ContainerReference reference : registeredContainers) {
			if (!dimension.equals(reference.dimension())) {
				throw new IllegalArgumentException("warehouse container crosses its warehouse dimension");
			}
		}
		index = Objects.requireNonNull(index, "index");
		Set<ContainerReference> registered = new HashSet<>(registeredContainers);
		for (InventoryIndexEntry entry : index.entries()) {
			if (!registered.contains(entry.container())) {
				throw new IllegalArgumentException("warehouse index contains an unregistered container");
			}
		}
		for (ContainerReference reference : index.invalidated()) {
			if (!registered.contains(reference)) {
				throw new IllegalArgumentException("warehouse index invalidates an unregistered container");
			}
		}
		if (revision < 0) {
			throw new IllegalArgumentException("warehouse revision must not be negative");
		}
	}

	public static WarehouseRecord empty(UUID id, ResourceKey<Level> dimension, BlockPos controller) {
		return new WarehouseRecord(id, dimension, controller, List.of(), InventoryIndexState.empty(), 0L);
	}

	public boolean hasContainer(ContainerReference reference) {
		return registeredContainers.contains(reference);
	}

	WarehouseRecord withContainers(List<ContainerReference> nextContainers) {
		return new WarehouseRecord(id, dimension, controller, nextContainers, index, revision + 1);
	}

	WarehouseRecord withIndex(InventoryIndexState nextIndex) {
		return new WarehouseRecord(id, dimension, controller, registeredContainers, nextIndex, revision + 1);
	}

	private static DataResult<WarehouseRecord> fromRaw(Raw raw) {
		try {
			return DataResult.success(new WarehouseRecord(
					raw.id(),
				raw.dimension(),
				raw.controller(),
				raw.registeredContainers(),
				raw.index(),
				raw.revision()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	private static List<ContainerReference> immutableContainers(List<ContainerReference> values) {
		Objects.requireNonNull(values, "registeredContainers");
		if (values.size() > StorageSafetyLimits.MAX_PERSISTED_CONTAINERS) {
			throw new IllegalArgumentException("warehouse contains too many registered containers");
		}
		List<ContainerReference> sorted = new ArrayList<>(values);
		for (ContainerReference value : sorted) {
			Objects.requireNonNull(value, "registeredContainers cannot contain null");
		}
		sorted.sort(CONTAINER_ORDER);
		if (new HashSet<>(sorted).size() != sorted.size()) {
			throw new IllegalArgumentException("warehouse contains duplicate registered containers");
		}
		return List.copyOf(sorted);
	}
}

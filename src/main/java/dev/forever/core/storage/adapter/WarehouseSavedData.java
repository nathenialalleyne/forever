package dev.forever.core.storage.adapter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Codec-backed world-scoped warehouse state. One instance is obtained from each dimension's
 * SavedDataStorage, so warehouses cannot silently become a cross-dimensional global inventory.
 */
public final class WarehouseSavedData extends SavedData {

	public static final Identifier ID = Identifier.fromNamespaceAndPath("forever", "storage/warehouses");
	public static final Codec<WarehouseSavedData> CODEC = WarehouseState.CODEC.flatXmap(
			state -> DataResult.success(new WarehouseSavedData(state)),
			data -> DataResult.success(data.state()));
	public static final SavedDataType<WarehouseSavedData> TYPE = new SavedDataType<>(
			ID,
			() -> new WarehouseSavedData(WarehouseState.empty()),
			CODEC,
			DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

	private WarehouseState state;

	private WarehouseSavedData(WarehouseState state) {
		this.state = Objects.requireNonNull(state, "state");
	}

	public static WarehouseSavedData get(ServerLevel level) {
		Objects.requireNonNull(level, "level");
		return level.getDataStorage().computeIfAbsent(TYPE);
	}

	/** Forces class initialisation from the explicit storage system entrypoint. */
	public static void initialize() {
		TYPE.id();
	}

	public synchronized WarehouseState state() {
		return state;
	}

	public synchronized Optional<WarehouseRecord> warehouse(UUID id) {
		Objects.requireNonNull(id, "id");
		return Optional.ofNullable(state.warehouses().get(id));
	}

	synchronized void replace(WarehouseState nextState) {
		state = Objects.requireNonNull(nextState, "nextState");
		setDirty();
	}
}

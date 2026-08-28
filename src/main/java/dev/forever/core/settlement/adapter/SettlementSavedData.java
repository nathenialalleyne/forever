package dev.forever.core.settlement.adapter;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * Mutable SavedData shell around the immutable, schema-versioned world settlement state.
 * Settlements are shared world records, so this is deliberately not an entity attachment.
 */
public final class SettlementSavedData extends SavedData {

	public static final Identifier ID = Identifier.fromNamespaceAndPath("forever", "settlements");

	public static final Codec<SettlementSavedData> CODEC = SettlementState.CODEC.xmap(
			SettlementSavedData::new,
			SettlementSavedData::state);

	/**
	 * Minecraft 26.2 has no generic custom DataFixTypes entry. Command storage is the
	 * inert SavedData pipeline marker used here; the Forever schema version remains the
	 * authoritative migration contract inside the encoded payload.
	 */
	public static final SavedDataType<SettlementSavedData> TYPE = new SavedDataType<>(
			ID,
			SettlementSavedData::new,
			CODEC,
			DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

	private SettlementState state;

	public SettlementSavedData() {
		this(SettlementState.empty());
	}

	private SettlementSavedData(SettlementState state) {
		this.state = Objects.requireNonNull(state, "state");
	}

	public static SettlementSavedData get(ServerLevel level) {
		Objects.requireNonNull(level, "level");
		return level.getDataStorage().computeIfAbsent(TYPE);
	}

	public synchronized SettlementState state() {
		return state;
	}

	/** Applies one complete immutable revision and marks the world record dirty. */
	public synchronized void replace(SettlementState nextState) {
		state = Objects.requireNonNull(nextState, "nextState");
		setDirty();
	}

	/** Performs an atomic server-thread revision with no partially mutated Charter state. */
	public synchronized SettlementState update(UnaryOperator<SettlementState> updater) {
		Objects.requireNonNull(updater, "updater");
		SettlementState nextState = Objects.requireNonNull(updater.apply(state), "updated state");
		state = nextState;
		setDirty();
		return nextState;
	}
}

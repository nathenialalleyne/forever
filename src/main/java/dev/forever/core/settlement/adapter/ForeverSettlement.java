package dev.forever.core.settlement;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;

/** Common/server entrypoint owned by the settlement package. */
public final class ForeverSettlement {

	private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

	private ForeverSettlement() {
	}

	/**
	 * Forces settlement SavedDataType registration on the common/server side.
	 *
	 * <p>{@code ForeverMod.onInitialize()} still needs to wire this method. It is kept as a
	 * package-owned entrypoint so the root initializer does not become a settlement god object.
	 */
	public static void initialize() {
		if (INITIALIZED.compareAndSet(false, true)) {
			SettlementSavedData.TYPE.id();
		}
	}

	public static SettlementSavedData data(ServerLevel level) {
		initialize();
		return SettlementSavedData.get(Objects.requireNonNull(level, "level"));
	}

	public static SettlementBalance loadBalance(ResourceManager resourceManager) {
		return SettlementBalanceLoader.load(Objects.requireNonNull(resourceManager, "resourceManager"));
	}
}

package dev.forever.core.storage.adapter;

import java.util.concurrent.atomic.AtomicBoolean;

/** Common/server registration entrypoint owned by the storage package. */
public final class ForeverStorage {

	private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

	private ForeverStorage() {
	}

	/**
	 * Registers storage data, the Traveler's Cache component/item, and the warehouse SavedData
	 * type. The main mod entrypoint must call this method; this package deliberately does not
	 * edit {@code ForeverMod.java}.
	 */
	public static void initialize() {
		if (!INITIALIZED.compareAndSet(false, true)) {
			return;
		}

		StorageBalanceLoader.installBundled();
		TravelerCacheComponent.initialize();
		TravelerCacheItem.initialize();
		WarehouseSavedData.initialize();
		StorageBalanceLoader.register();
	}
}

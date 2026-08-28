package dev.forever.core.storage;

import java.util.Objects;
import java.util.Optional;

/** Publishes the latest immutable server-data storage balance snapshot. */
public final class StorageBalanceAccess {

	private static volatile StorageBalance current;

	private StorageBalanceAccess() {
	}

	public static void install(StorageBalance balance) {
		current = Objects.requireNonNull(balance, "balance");
	}

	public static Optional<StorageBalance> current() {
		return Optional.ofNullable(current);
	}

	public static StorageBalance requireCurrent() {
		StorageBalance balance = current;
		if (balance == null) {
			throw new IllegalStateException(
					"Storage balance data is not loaded. The server must complete its data reload before storage operations run.");
		}
		return balance;
	}
}

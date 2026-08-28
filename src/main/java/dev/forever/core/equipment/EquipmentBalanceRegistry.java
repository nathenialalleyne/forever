package dev.forever.core.equipment;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/** Server definition holder for the currently active equipment balance resource. */
public final class EquipmentBalanceRegistry {

	private static final AtomicReference<EquipmentBalance> CURRENT = new AtomicReference<>();

	private EquipmentBalanceRegistry() {
	}

	public static Optional<EquipmentBalance> current() {
		return Optional.ofNullable(CURRENT.get());
	}

	public static EquipmentBalance requireCurrent() {
		return current().orElseThrow(() -> new IllegalStateException(
				"Forever equipment balance is not loaded. Reload server data before using equipment."));
	}

	public static void install(EquipmentBalance balance) {
		CURRENT.set(balance);
	}
}

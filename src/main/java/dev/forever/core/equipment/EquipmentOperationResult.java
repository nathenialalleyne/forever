package dev.forever.core.equipment;

import java.util.Objects;
import java.util.Optional;

/** Immutable result of a server-side repair or reforge transaction. */
public record EquipmentOperationResult(
		boolean success,
		String messageKey,
		Optional<EquipmentState> state) {

	public EquipmentOperationResult {
		if (messageKey == null || messageKey.isBlank()) {
			throw new IllegalArgumentException("equipment operation message key must not be blank");
		}
		Objects.requireNonNull(state, "state");
	}

	static EquipmentOperationResult success(EquipmentState state, String messageKey) {
		return new EquipmentOperationResult(true, messageKey, Optional.of(state));
	}

	static EquipmentOperationResult failure(EquipmentState state, String messageKey) {
		return new EquipmentOperationResult(false, messageKey, Optional.ofNullable(state));
	}
}

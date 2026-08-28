package dev.forever.core.equipment;

import dev.forever.core.equipment.adapter.EquipmentBalance;
import dev.forever.core.equipment.adapter.EquipmentState;

/**
 * Pure arithmetic for condition changes.
 *
 * <p>This remains at the feature root because its existing API consumes the Minecraft-shaped
 * {@link EquipmentBalance} and {@link EquipmentState} records. Moving it into {@code domain}
 * would make domain code depend on the adapter, while introducing neutral replacement values
 * would exceed this structural migration's no-redesign scope.
 */
public final class EquipmentMath {

	private EquipmentMath() {
	}

	public static int fieldRepairAmount(EquipmentState state, EquipmentBalance balance) {
		if (state.currentCondition() >= state.maxCondition()) {
			return 0;
		}
		long amount = (long) Math.ceil(state.maxCondition() * balance.fieldRepairFraction());
		return (int) Math.max(0L, Math.min(
				(long) state.maxCondition() - state.currentCondition(), amount));
	}

	public static int damageAmount(EquipmentState state, int requestedAmount, EquipmentBalance balance) {
		if (requestedAmount <= 0 || state.isBroken()) {
			return 0;
		}
		double multiplier = balance.craftsmanshipMultiplier(state.craftsmanshipTier())
				.orElseThrow(() -> new IllegalArgumentException(
						"no craftsmanship multiplier is defined for tier " + state.craftsmanshipTier()));
		if (state.activePath().isPresent()) {
			multiplier *= balance.path(state.activePath().get())
					.map(EquipmentBalance.PathDefinition::conditionMultiplier)
					.orElse(1.0D);
		}
		long calculated = (long) Math.ceil(requestedAmount * multiplier);
		return (int) Math.max(1L, Math.min((long) state.currentCondition(), calculated));
	}

	public static int clampCondition(int requestedCondition, int maxCondition) {
		if (maxCondition < 1) {
			throw new IllegalArgumentException("max condition must be positive");
		}
		return Math.max(0, Math.min(maxCondition, requestedCondition));
	}
}

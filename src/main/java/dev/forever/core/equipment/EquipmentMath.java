package dev.forever.core.equipment;

/** Pure arithmetic for condition changes. */
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

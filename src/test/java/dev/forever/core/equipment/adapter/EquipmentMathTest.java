package dev.forever.core.equipment.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.forever.core.equipment.EquipmentBalance;
import dev.forever.core.equipment.EquipmentMath;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class EquipmentMathTest {

	private static final Identifier PROSPECTOR = Identifier.parse("forever:prospector");
	private static final Identifier LEGACY_PATH = Identifier.parse("forever:removed_path");

	@Test
	void fieldRepairUsesConfiguredFractionAndClampsToMaximum() {
		EquipmentBalance balance = balance(0.25, 1.0, 0.25);
		EquipmentState worn = EquipmentState.create(
				UUID.randomUUID(), 100, 0, Optional.empty(), Map.of()).withCondition(80);
		EquipmentState broken = worn.withCondition(0);

		assertEquals(25, EquipmentMath.fieldRepairAmount(broken, balance));
		assertEquals(20, EquipmentMath.fieldRepairAmount(worn, balance));
		assertEquals(0, EquipmentMath.fieldRepairAmount(worn.withCondition(100), balance));
	}

	@Test
	void damageMathAppliesCraftsmanshipAndActivePathMultipliers() {
		EquipmentBalance balance = balance(0.25, 1.0, 0.25);
		EquipmentState state = EquipmentState.create(
				UUID.randomUUID(),
				100,
				1,
				Optional.of(PROSPECTOR),
				Map.of(PROSPECTOR, 0));

		assertEquals(9, EquipmentMath.damageAmount(state, 10, balance));
	}

	@Test
	void missingActivePathDisablesOnlyItsMultiplier() {
		EquipmentBalance balance = balance(0.25, 1.0, 0.25);
		EquipmentState state = EquipmentState.create(
				UUID.randomUUID(),
				100,
				0,
				Optional.of(LEGACY_PATH),
				Map.of(LEGACY_PATH, 12));

		assertEquals(10, EquipmentMath.damageAmount(state, 10, balance));
	}

	private static EquipmentBalance balance(double repairFraction, double pathMultiplier, double wornThreshold) {
		return new EquipmentBalance(
				1,
				100,
				repairFraction,
				0.75,
				wornThreshold,
				new EquipmentBalance.ItemCost(Identifier.parse("minecraft:iron_ingot"), 1),
				new EquipmentBalance.ItemCost(Identifier.parse("minecraft:iron_block"), 1),
				new EquipmentBalance.ItemCost(Identifier.parse("minecraft:diamond"), 1),
				Map.of(0, 1.0, 1, 0.9),
				Map.of(PROSPECTOR, new EquipmentBalance.PathDefinition(
						"equipment.path.prospector", 1000, pathMultiplier)));
	}
}

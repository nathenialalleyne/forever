package dev.forever.core.settlement.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import dev.forever.core.settlement.domain.SettlementBalance;
import dev.forever.core.settlement.domain.SettlementDataException;
import dev.forever.core.settlement.domain.SettlementMigrationProcedure;
import java.util.List;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SettlementBalanceTest {

	@Test
	@DisplayName("balance codec preserves all data-defined attachment, validation, and migration values")
	void balanceRoundTrip() {
		SettlementBalance original = SettlementTestFixtures.BALANCE;
		var encoded = SettlementBalance.CODEC.encodeStart(
				com.mojang.serialization.JsonOps.INSTANCE, original).getOrThrow();

		assertEquals(original, SettlementBalance.CODEC.parse(
				com.mojang.serialization.JsonOps.INSTANCE, encoded).getOrThrow());
	}

	@Test
	@DisplayName("invalid balance values name the resource and failing constraint")
	void invalidBalanceIsRejected() {
		SettlementDataException exception = assertThrows(SettlementDataException.class, () ->
				SettlementBalanceLoader.decode(
						Identifier.parse("forever:settlement/bad.json"),
						JsonParser.parseString("""
								{"schema_version":1,"proximity_attachment_distance":0.0,
								"outpost_attachment_distance":0.0,"validation_volume_cap":1,
								"hard_validation_volume_cap":1,"minimum_interior_space":1,
								"minimum_beds":1,"minimum_workstations":1,"minimum_storage_slots":1,
								"minimum_light_level":0,"abstract_villager_budget_per_tick":1,
								"migration_interval_ticks":1,"migration_arrivals_per_interval":1,
								"maximum_import_buildings":1}
								""")));

		assertTrue(exception.getMessage().contains("forever:settlement/bad.json"));
		assertTrue(exception.getMessage().contains("distance"));
	}

	@Test
	@DisplayName("the FVR-405 procedure exposes backup, rollback, verification, and startup steps")
	void migrationProcedureIsExplicit() {
		List<String> steps = SettlementMigrationProcedure.steps();

		assertTrue(steps.stream().anyMatch(step -> step.contains("Back up")));
		assertTrue(steps.stream().anyMatch(step -> step.contains("roll back")));
		assertTrue(steps.stream().anyMatch(step -> step.contains("Verify")));
		assertTrue(steps.stream().anyMatch(step -> step.contains("dedicated-server")));
	}
}

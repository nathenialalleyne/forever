package dev.forever.core.settlement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SettlementMigrationTest {

	@Test
	@DisplayName("migration offers arrive without breeding and abstract updates never kill villagers")
	void migrationArrivesWithoutDeaths() {
		Settlement settlement = SettlementGraph.attach(
				SettlementTestFixtures.charter(),
				SettlementTestFixtures.building(
						"00000000-0000-0000-0000-000000000401", 0, Set.of(SettlementRole.RESIDENCE)),
				SettlementTestFixtures.BALANCE).settlement();
		MigrationRecord offer = MigrationService.offer(UUID.randomUUID(), Optional.empty(),
				"coast", "farmer", 0L);
		settlement = settlement.withMigration(offer);

		MigrationSimulationResult result = MigrationService.simulateUnloadedTick(
				settlement, SettlementTestFixtures.BALANCE.migrationIntervalTicks(), SettlementTestFixtures.BALANCE);

		assertEquals(0, result.deaths());
		assertEquals(MigrationStatus.ARRIVED, result.settlement().migrations().getFirst().status());
		assertTrue(result.processedRecords() <= SettlementTestFixtures.BALANCE.abstractVillagerBudgetPerTick());
		assertTrue(result.chunkLoadRequested() == false);
	}

	@Test
	@DisplayName("abstract simulation processes no more than its fixed configured budget")
	void abstractBudgetIsBounded() {
		Settlement settlement = SettlementTestFixtures.charter();
		for (int index = 0; index < 80; index++) {
			settlement = settlement.withMigration(MigrationService.offer(
					new UUID(0L, index + 1L), Optional.empty(), "origin", "career", 0L));
		}

		MigrationSimulationResult result = MigrationService.simulateUnloadedTick(
				settlement, 1L, SettlementTestFixtures.BALANCE);

		assertEquals(SettlementTestFixtures.BALANCE.abstractVillagerBudgetPerTick(), result.processedRecords());
		assertEquals(0, result.deaths());
	}
}

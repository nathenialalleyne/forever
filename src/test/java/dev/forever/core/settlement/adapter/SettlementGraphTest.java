package dev.forever.core.settlement.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.forever.core.settlement.domain.SettlementEdgeKind;
import dev.forever.core.settlement.domain.SettlementRole;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SettlementGraphTest {

	@Test
	@DisplayName("a nearby explicitly registered building attaches by proximity")
	void nearbyBuildingAttaches() {
		Settlement settlement = SettlementTestFixtures.charter();
		RegisteredBuilding first = SettlementTestFixtures.building(
				"00000000-0000-0000-0000-000000000301", 0, Set.of(SettlementRole.RESIDENCE));
		RegisteredBuilding second = SettlementTestFixtures.building(
				"00000000-0000-0000-0000-000000000302", 20, Set.of(SettlementRole.WORKPLACE));

		settlement = SettlementGraph.attach(settlement, first, SettlementTestFixtures.BALANCE).settlement();
		SettlementGraph.RegistrationResult result = SettlementGraph.attach(
				settlement, second, SettlementTestFixtures.BALANCE);

		assertTrue(result.accepted());
		assertEquals(SettlementEdgeKind.PROXIMITY, result.edge().orElseThrow().kind());
		assertEquals(2, result.settlement().buildings().size());
	}

	@Test
	@DisplayName("a normal building beyond proximity is rejected without changing the graph")
	void distantBuildingIsRejected() {
		Settlement settlement = SettlementGraph.attach(
				SettlementTestFixtures.charter(),
				SettlementTestFixtures.building(
						"00000000-0000-0000-0000-000000000303", 0, Set.of(SettlementRole.RESIDENCE)),
				SettlementTestFixtures.BALANCE).settlement();
		RegisteredBuilding distant = SettlementTestFixtures.building(
				"00000000-0000-0000-0000-000000000304", 200, Set.of(SettlementRole.WORKPLACE));

		SettlementGraph.RegistrationResult result = SettlementGraph.attach(
				settlement, distant, SettlementTestFixtures.BALANCE);

		assertFalse(result.accepted());
		assertTrue(result.message().contains("proximity"));
		assertEquals(1, result.settlement().buildings().size());
	}

	@Test
	@DisplayName("an explicitly declared outpost may attach through the larger outpost distance")
	void outpostAttachesRemotely() {
		Settlement settlement = SettlementGraph.attach(
				SettlementTestFixtures.charter(),
				SettlementTestFixtures.building(
						"00000000-0000-0000-0000-000000000305", 0, Set.of(SettlementRole.RESIDENCE)),
				SettlementTestFixtures.BALANCE).settlement();
		RegisteredBuilding outpost = SettlementTestFixtures.building(
				"00000000-0000-0000-0000-000000000306", 100, Set.of(SettlementRole.OUTPOST));

		SettlementGraph.RegistrationResult result = SettlementGraph.attach(
				settlement, outpost, SettlementTestFixtures.BALANCE);

		assertTrue(result.accepted());
		assertEquals(SettlementEdgeKind.OUTPOST, result.edge().orElseThrow().kind());
		assertTrue(result.settlement().building(outpost.id()).orElseThrow().outpost());
	}
}

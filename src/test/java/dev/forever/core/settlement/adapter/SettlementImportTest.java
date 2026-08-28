package dev.forever.core.settlement.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.forever.core.settlement.domain.SettlementRole;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SettlementImportTest {

	@Test
	@DisplayName("village import is previewable, explicit, partial, and idempotent")
	void importIsExplicitAndIdempotent() {
		RegisteredBuilding home = SettlementTestFixtures.building(
				"00000000-0000-0000-0000-000000000501", 0, Set.of(SettlementRole.RESIDENCE));
		RegisteredBuilding outpost = SettlementTestFixtures.building(
				"00000000-0000-0000-0000-000000000502", 100, Set.of(SettlementRole.OUTPOST));
		UUID proposalId = UUID.fromString("00000000-0000-0000-0000-000000000503");
		VillageImportProposal proposal = SettlementCharterService.proposeVillageImport(
				proposalId,
				SettlementTestFixtures.OVERWORLD,
				new BuildingBounds(new BlockPos(-2, -2, -2), new BlockPos(110, 10, 10)),
				List.of(home, outpost),
				3,
				List.of("One natural workstation needs review."),
				SettlementTestFixtures.BALANCE);

		SettlementState empty = SettlementState.empty();
		SettlementCharterService.ImportResult cancelled = SettlementCharterService.cancel(proposal, empty);
		assertFalse(cancelled.committed());
		assertEquals(empty, cancelled.state());

		SettlementCharterService.ImportResult committed = SettlementCharterService.importVillage(
				empty,
				UUID.fromString("00000000-0000-0000-0000-000000000504"),
				"Imported Village",
				UUID.fromString("00000000-0000-0000-0000-000000000505"),
				proposal,
				List.of(home.id(), outpost.id()),
				SettlementTestFixtures.BALANCE);

		assertTrue(committed.committed());
		assertEquals(2, committed.settlement().orElseThrow().buildings().size());
		SettlementCharterService.ImportResult duplicate = SettlementCharterService.importVillage(
				committed.state(),
				UUID.fromString("00000000-0000-0000-0000-000000000506"),
				"Duplicate",
				UUID.randomUUID(),
				proposal,
				List.of(home.id()),
				SettlementTestFixtures.BALANCE);
		assertFalse(duplicate.committed());
		assertTrue(duplicate.message().contains("already been committed"));
	}

	@Test
	@DisplayName("import previews reject duplicate candidates and oversized source areas")
	void malformedImportPreviewIsRejected() {
		RegisteredBuilding home = SettlementTestFixtures.building(
				"00000000-0000-0000-0000-000000000511", 0, Set.of(SettlementRole.RESIDENCE));

		assertThrows(IllegalArgumentException.class, () -> new VillageImportProposal(
				UUID.randomUUID(),
				SettlementTestFixtures.OVERWORLD,
				new BuildingBounds(new BlockPos(-2, -2, -2), new BlockPos(4, 4, 4)),
				List.of(home, home),
				0,
				List.of()));

		assertThrows(IllegalArgumentException.class, () -> new VillageImportProposal(
				UUID.randomUUID(),
				SettlementTestFixtures.OVERWORLD,
				new BuildingBounds(BlockPos.ZERO, new BlockPos(64, 64, 64)),
				List.of(home),
				0,
				List.of()));

		assertThrows(IllegalArgumentException.class, () -> new VillageImportRecord(
				UUID.randomUUID(),
				SettlementTestFixtures.OVERWORLD,
				new BuildingBounds(new BlockPos(-2, -2, -2), new BlockPos(4, 4, 4)),
				0,
				List.of(home.id(), home.id()),
				List.of()));
	}
}

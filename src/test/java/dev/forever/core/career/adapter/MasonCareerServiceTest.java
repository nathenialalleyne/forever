package dev.forever.core.career.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.forever.core.career.domain.CareerStatus;
import dev.forever.core.career.domain.MasonRank;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MasonCareerServiceTest {

	private static final UUID WORKPLACE = UUID.fromString("00000000-0000-0000-0000-000000000201");
	private static MasonCareerDefinition definition;

	@BeforeAll
	static void loadData() {
		MasonDataLoader.installBundled();
		definition = MasonDataRegistry.requireCurrent().career();
	}

	@Test
	@DisplayName("promotion uses distinct demonstrated and taught evidence, not repetition")
	void progressionRequiresEvidenceAndRejectsReplay() {
		CareerState state = apprentice()
				.withKnownTechnique(MasonIds.STONE_CUTTING)
				.withKnownTechnique(MasonIds.STRUCTURAL_FINISH);
		CareerProgressionResult first = MasonCareerService.demonstrateTechnique(state, MasonIds.STONE_CUTTING);
		state = first.state();
		CareerProgressionResult replay = MasonCareerService.demonstrateTechnique(state, MasonIds.STONE_CUTTING);
		assertTrue(first.applied());
		assertFalse(replay.applied());
		assertEquals(state, replay.state());

		state = MasonCareerService.demonstrateTechnique(state, MasonIds.STRUCTURAL_FINISH).state();
		state = state.withTaughtTechnique(MasonIds.STONE_CUTTING)
				.withValidatedProject(MasonIds.VALIDATED_WORKSHOP);
		CareerProgressionResult promoted = MasonCareerService.advanceRank(state, definition);

		assertTrue(promoted.applied());
		assertEquals(MasonRank.JOURNEYMAN, promoted.state().rank());
	}

	@Test
	@DisplayName("mentor teaching updates both bounded relationship records atomically")
	void teachingTransfersKnowledgeAndProvenance() {
		UUID mentorId = UUID.fromString("00000000-0000-0000-0000-000000000202");
		UUID apprenticeId = UUID.fromString("00000000-0000-0000-0000-000000000203");
		CareerState mentor = new CareerState(
				CareerState.CURRENT_SCHEMA,
				MasonIds.MASON,
				MasonRank.JOURNEYMAN,
				Set.of(MasonIds.STONE_CUTTING),
				Set.of(MasonIds.STONE_CUTTING),
				Set.of(),
				Set.of(),
				Optional.empty(),
				Optional.of(WORKPLACE),
				Optional.empty(),
				Set.of(),
				CareerStatus.ACTIVE,
				Optional.empty());
		CareerState apprentice = CareerState.masonApprentice(Optional.empty(), Optional.of(WORKPLACE));

		CareerTeachingResult taught = CareerTeachingService.teach(
				mentor, apprentice, mentorId, apprenticeId, MasonIds.STONE_CUTTING);

		assertTrue(taught.applied());
		assertTrue(taught.mentor().taughtTechniques().contains(MasonIds.STONE_CUTTING));
		assertTrue(taught.mentor().apprentices().contains(apprenticeId));
		assertTrue(taught.apprentice().knownTechniques().contains(MasonIds.STONE_CUTTING));
		assertEquals(Optional.of(mentorId), taught.apprentice().mentor());

		CareerTeachingResult replay = CareerTeachingService.teach(
				taught.mentor(), taught.apprentice(), mentorId, apprenticeId, MasonIds.STONE_CUTTING);
		assertFalse(replay.applied());
	}

	@Test
	@DisplayName("physical death keeps taught knowledge and loses untaught personal knowledge")
	void mortalityBoundaryIsExplicit() {
		CareerState state = apprentice()
				.withKnownTechnique(MasonIds.STONE_CUTTING)
				.withKnownTechnique(MasonIds.STRUCTURAL_FINISH)
				.withDemonstratedTechnique(MasonIds.STONE_CUTTING)
				.withDemonstratedTechnique(MasonIds.STRUCTURAL_FINISH)
				.withTaughtTechnique(MasonIds.STONE_CUTTING);

		CareerSimulationResult unloaded = CareerMortalityService.simulateUnloaded(state);
		assertFalse(unloaded.deathCreated());
		assertEquals(state, unloaded.state());

		CareerDeathResult death = CareerMortalityService.applyPhysicalDeath(state, PhysicalDeathCause.FALL);
		assertTrue(death.applied());
		assertEquals(Set.of(MasonIds.STONE_CUTTING), death.survivingTechniques());
		assertEquals(Set.of(MasonIds.STRUCTURAL_FINISH), death.lostUntaughtTechniques());
		assertEquals(CareerStatus.DEAD, death.state().status());
		assertEquals(Set.of(MasonIds.STONE_CUTTING), death.state().knownTechniques());
	}

	private static CareerState apprentice() {
		return CareerState.masonApprentice(Optional.empty(), Optional.of(WORKPLACE));
	}
}

package dev.forever.core.mastery.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.forever.core.mastery.domain.MasteryProgress;
import dev.forever.core.mastery.domain.SwitchContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MasteryLoadoutTest {

	private final MasteryRegistry registry = MasteryTestFixtures.fullRegistry();

	@Test
	@DisplayName("one Focus and two Supporting assignments are valid")
	void validLoadoutUsesLockedSlots() {
		MasteryLoadout loadout = new MasteryLoadout(
				Optional.of(MasteryIds.BUILDER),
				List.of(MasteryIds.PROSPECTOR, MasteryIds.COOK));

		assertTrue(registry.validateLoadout(loadout).result().isPresent());
	}

	@Test
	@DisplayName("a third Supporting assignment is rejected by the balance rules")
	void thirdSupportingAssignmentIsRejected() {
		var exception = org.junit.jupiter.api.Assertions.assertThrows(
				IllegalArgumentException.class,
				() -> new MasteryLoadout(
						Optional.of(MasteryIds.BUILDER),
						List.of(MasteryIds.PROSPECTOR, MasteryIds.COOK, MasteryIds.SMITH)));

		assertTrue(exception.getMessage().contains("at most 2 Supporting"));
	}

	@Test
	@DisplayName("duplicate Focus and Supporting assignments fail clearly")
	void duplicateAssignmentIsRejected() {
		var exception = org.junit.jupiter.api.Assertions.assertThrows(
				IllegalArgumentException.class,
				() -> new MasteryLoadout(
						Optional.of(MasteryIds.BUILDER),
						List.of(MasteryIds.BUILDER)));

		assertTrue(exception.getMessage().contains("both Focus and Supporting"));
	}

	@Test
	@DisplayName("inactive progress survives a gated switch without re-levelling")
	void switchPreservesInactiveProgress() {
		MasteryProgress builderProgress = new MasteryProgress(
				2,
				Set.of("material_breadth"),
				Set.of("variety:material_family:stone"),
				Set.of("build-stone"));
		MasteryProgress prospectorProgress = new MasteryProgress(
				4,
				Set.of("ore_variety", "depth_survey"),
				Set.of("variety:ore_type:iron", "variety:ore_type:gold"),
				Set.of("ore-iron", "ore-gold"));
		MasteryState current = new MasteryState(
				MasteryState.CURRENT_SCHEMA,
				Map.of(MasteryIds.BUILDER, builderProgress, MasteryIds.PROSPECTOR, prospectorProgress),
				new MasteryLoadout(Optional.of(MasteryIds.BUILDER), List.of(MasteryIds.COOK)),
				Optional.empty());
		MasteryLoadout desired = new MasteryLoadout(
				Optional.of(MasteryIds.PROSPECTOR), List.of(MasteryIds.BUILDER, MasteryIds.SMITH));

		LoadoutSwitchResult noContext = MasteryLoadoutService.switchLoadout(
				current, registry, desired, SwitchContext.NONE, 20L);
		LoadoutSwitchResult switched = MasteryLoadoutService.switchLoadout(
				current, registry, desired, SwitchContext.REST, 21L);

		assertFalse(noContext.applied());
		assertEquals(current, noContext.state());
		assertTrue(switched.applied());
		assertEquals(desired, switched.state().loadout());
		assertEquals(current.progress(), switched.state().progress());
		assertEquals(SwitchContext.REST, switched.state().lastLoadoutChange().orElseThrow().context());
	}
}

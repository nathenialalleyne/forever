package dev.forever.core.mastery.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.forever.core.mastery.domain.EvidenceKind;
import dev.forever.core.mastery.domain.ProgressionEvidence;
import java.util.Set;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProspectorProgressionTest {

	private final MasteryRegistry registry = MasteryTestFixtures.fullRegistry();

	@Test
	@DisplayName("Prospector advances from distinct ore types, not repeated identical actions")
	void distinctOreTypesAdvanceBreadthMilestone() {
		MasteryState state = MasteryState.empty();

		ProgressionResult iron = MasteryProgression.apply(
				state, registry, MasteryIds.PROSPECTOR,
				ProspectorEvidence.oreType("ore-iron", Identifier.parse("minecraft:iron_ore")));
		ProgressionResult repeatedIron = MasteryProgression.apply(
				iron.state(), registry, MasteryIds.PROSPECTOR,
				ProspectorEvidence.oreType("ore-iron-repeat", Identifier.parse("minecraft:iron_ore")));
		ProgressionResult replayWithDifferentOre = MasteryProgression.apply(
				iron.state(), registry, MasteryIds.PROSPECTOR,
				ProspectorEvidence.oreType("ore-iron", Identifier.parse("minecraft:gold_ore")));
		ProgressionResult gold = MasteryProgression.apply(
				(repeatedIron.state()), registry, MasteryIds.PROSPECTOR,
				ProspectorEvidence.oreType("ore-gold", Identifier.parse("minecraft:gold_ore")));
		ProgressionResult diamond = MasteryProgression.apply(
				gold.state(), registry, MasteryIds.PROSPECTOR,
				ProspectorEvidence.oreType("ore-diamond", Identifier.parse("minecraft:diamond_ore")));

		assertTrue(iron.accepted());
		assertEquals(1, iron.state().progressFor(MasteryIds.PROSPECTOR).evidenceKeys().size());
		assertTrue(repeatedIron.duplicate());
		assertEquals(1, repeatedIron.state().progressFor(MasteryIds.PROSPECTOR).evidenceKeys().size());
		assertTrue(replayWithDifferentOre.duplicate());
		assertEquals(iron.state(), replayWithDifferentOre.state());
		assertTrue(gold.accepted());
		assertEquals(2, gold.state().progressFor(MasteryIds.PROSPECTOR).evidenceKeys().size());
		assertTrue(diamond.unlockedMilestones().contains("ore_variety"));
		assertEquals(1, diamond.rank());
	}

	@Test
	@DisplayName("depth bands, projects, and technique are independent named evidence")
	void otherProspectorEvidenceSourcesAreBoundedAndDistinct() {
		MasteryState state = MasteryState.empty();
		ProgressionResult low = MasteryProgression.apply(
				state, registry, MasteryIds.PROSPECTOR,
				ProspectorEvidence.depthBand("depth-low", "y_low"));
		ProgressionResult mid = MasteryProgression.apply(
				low.state(), registry, MasteryIds.PROSPECTOR,
				ProspectorEvidence.depthBand("depth-mid", "y_mid"));
		ProgressionResult project = MasteryProgression.apply(
				mid.state(), registry, MasteryIds.PROSPECTOR,
				ProspectorEvidence.project("project-plan", "north-ridge-extraction"));
		ProgressionResult technique = MasteryProgression.apply(
				project.state(), registry, MasteryIds.PROSPECTOR,
				ProspectorEvidence.technique("technique-sample", "interpreted-sample"));

		assertTrue(mid.unlockedMilestones().contains("depth_survey"));
		assertEquals(2, mid.rank());
		assertTrue(project.accepted());
		assertTrue(technique.accepted());
		assertEquals(5, technique.rank());
		assertEquals(4, technique.state().progressFor(MasteryIds.PROSPECTOR).evidenceKeys().size());
	}

	@Test
	@DisplayName("unapproved repetition-like sources never grant Prospector progress")
	void unapprovedSourceIsRejected() {
		MasteryState state = MasteryState.empty();
		ProgressionEvidence repetition = new ProgressionEvidence(
				"mine-counter-shaped", EvidenceKind.VARIETY, "blocks_mined", "1");

		ProgressionResult result = MasteryProgression.apply(
				state, registry, MasteryIds.PROSPECTOR, repetition);

		assertFalse(result.accepted());
		assertEquals(state, result.state());
		assertTrue(result.reasonKey().contains("unapproved_source"));
		assertEquals(Set.of(), state.progressFor(MasteryIds.PROSPECTOR).evidenceKeys());
	}
}

package dev.forever.core.mastery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MasteryRegistryTest {

	@Test
	@DisplayName("the balance pack loads all ten planned masteries deterministically")
	void plannedDefinitionsLoadInStableOrder() {
		MasteryRegistry registry = MasteryTestFixtures.fullRegistry();

		assertEquals(10, registry.definitions().size());
		assertEquals(
				List.of(
						MasteryIds.ALCHEMIST_HERBALIST,
						MasteryIds.BUILDER,
						MasteryIds.COOK,
						MasteryIds.EXPLORER_CARTOGRAPHER,
						MasteryIds.FARMER_NATURALIST,
						MasteryIds.FISHER_MARINER,
						MasteryIds.MERCHANT_STEWARD,
						MasteryIds.PROSPECTOR,
						MasteryIds.RIDER_ANIMAL_HANDLER,
						MasteryIds.SMITH),
				registry.orderedDefinitions().stream().map(MasteryDefinition::id).toList());
		assertEquals(1, registry.loadoutRules().focusSlots());
		assertEquals(2, registry.loadoutRules().supportingSlots());
	}

	@Test
	@DisplayName("malformed definitions name their resource and failing field")
	void malformedDefinitionIsRejectedWithActionableError() {
		MasteryDataException exception = assertThrows(
				MasteryDataException.class,
				() -> MasteryRegistryLoader.decodeDefinition(
						Identifier.parse("forever:mastery/bad.json"),
						MasteryTestFixtures.json("{\"id\":\"forever:bad\"}")));

		assertTrue(exception.getMessage().contains("forever:mastery/bad.json"));
		assertTrue(exception.getMessage().contains("display_key"));
	}

	@Test
	@DisplayName("duplicate stable IDs fail before gameplay can evaluate them")
	void duplicateDefinitionsAreRejected() {
		String definition = """
				{"id":"forever:duplicate","display_key":"mastery.duplicate","description_key":"mastery.duplicate.description","dependencies":[],"evidence_dimensions":["ore_type"],"milestones":[{"id":"ore","evidence_kind":"variety","dimension":"ore_type","threshold":1,"rank":1,"capability_key":"mastery.duplicate.capability.ore","explanation_key":"mastery.duplicate.milestone.ore"}]}
				""";

		MasteryDataException exception = assertThrows(
				MasteryDataException.class,
				() -> MasteryRegistryLoader.load(List.of(
						MasteryTestFixtures.document("loadout.json", "{\"focus_slots\":1,\"supporting_slots\":2}"),
						MasteryTestFixtures.document("one.json", definition),
						MasteryTestFixtures.document("two.json", definition))));

		assertTrue(exception.getMessage().contains("Duplicate mastery definition ID"));
	}

	@Test
	@DisplayName("missing dependencies fail with both IDs named")
	void missingDependencyIsRejected() {
		String definition = """
				{"id":"forever:dependent","display_key":"mastery.dependent","description_key":"mastery.dependent.description","dependencies":["forever:missing"],"evidence_dimensions":["ore_type"],"milestones":[{"id":"ore","evidence_kind":"variety","dimension":"ore_type","threshold":1,"rank":1,"capability_key":"mastery.dependent.capability.ore","explanation_key":"mastery.dependent.milestone.ore"}]}
				""";

		MasteryDataException exception = assertThrows(
				MasteryDataException.class,
				() -> MasteryRegistryLoader.load(List.of(
						MasteryTestFixtures.document("loadout.json", "{\"focus_slots\":1,\"supporting_slots\":2}"),
						MasteryTestFixtures.document("dependent.json", definition))));

		assertTrue(exception.getMessage().contains("forever:dependent"));
		assertTrue(exception.getMessage().contains("forever:missing"));
	}

	@Test
	@DisplayName("invalid slot balance is rejected at data load")
	void invalidLoadoutBalanceIsRejected() {
		MasteryDataException exception = assertThrows(
				MasteryDataException.class,
				() -> MasteryRegistryLoader.decodeLoadout(
						Identifier.parse("forever:mastery/loadout.json"),
						MasteryTestFixtures.json("{\"focus_slots\":2,\"supporting_slots\":2}")));

		assertTrue(exception.getMessage().contains("exactly one Focus"));
	}

	@Test
	@DisplayName("duplicate evidence dimensions are rejected instead of silently collapsed")
	void duplicateEvidenceDimensionsAreRejected() {
		MasteryDataException exception = assertThrows(
				MasteryDataException.class,
				() -> MasteryRegistryLoader.decodeDefinition(
						Identifier.parse("forever:mastery/duplicate-dimension.json"),
						MasteryTestFixtures.json("""
							{"id":"forever:duplicate_dimension","display_key":"mastery.duplicate_dimension","description_key":"mastery.duplicate_dimension.description","dependencies":[],"evidence_dimensions":["ore_type","ore_type"],"milestones":[{"id":"ore","evidence_kind":"variety","dimension":"ore_type","threshold":1,"rank":1,"capability_key":"mastery.duplicate_dimension.capability.ore","explanation_key":"mastery.duplicate_dimension.milestone.ore"}]}
							""")));

		assertTrue(exception.getMessage().contains("evidence dimension more than once"));
	}
}

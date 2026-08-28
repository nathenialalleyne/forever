package dev.forever.core.guide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class GuideRegistryTest {

	private static final Identifier CAMPFIRE = Identifier.fromNamespaceAndPath("forever", "guide/campfire_process");
	private static final Identifier FIRE_STARTING = Identifier.fromNamespaceAndPath("forever", "guide/fire_starting");
	private static final Identifier EARLY_TOOLS = Identifier.fromNamespaceAndPath("forever", "guide/early_tools");
	private static final Identifier EARLY_PROCESSING =
			Identifier.fromNamespaceAndPath("forever", "guide/early_processing");

	@Test
	void bundledEntriesLoadThroughTheDataCodec() throws IOException {
		GuideRegistry registry = bundledRegistry();

		assertEquals(4, registry.entries().size());
		GuideEntry campfire = registry.require(CAMPFIRE);
		assertEquals(9, campfire.sectionKeys().size());
		assertEquals(List.of(Identifier.fromNamespaceAndPath("forever", "process/campfire")),
				campfire.processIds());
		assertEquals(List.of(EARLY_PROCESSING, FIRE_STARTING), campfire.relatedEntryIds());
		assertNotNull(campfire.bodyKeys());

		var encoded = GuideEntry.CODEC.encodeStart(JsonOps.INSTANCE, campfire).getOrThrow();
		assertEquals(campfire, GuideEntry.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
	}

	@Test
	void malformedJsonNamesTheOffendingResource() {
		GuideDataException exception = assertThrows(
				GuideDataException.class,
				() -> GuideRegistryLoader.decodeJson(
						Identifier.fromNamespaceAndPath("forever", "guide/malformed.json"), "{\"schema_version\": 1"));

		assertTrue(exception.getMessage().contains("forever:guide/malformed.json"));
		assertTrue(exception.getMessage().toLowerCase().contains("json"));
	}

	@Test
	void malformedEntryNamesTheResourceAndMissingField() {
		GuideDataException exception = assertThrows(
				GuideDataException.class,
				() -> GuideRegistryLoader.decodeJson(
						Identifier.fromNamespaceAndPath("forever", "guide/missing_sections.json"),
						"""
						{
						  "schema_version": 1,
						  "id": "forever:guide/missing_sections",
						  "title_key": "guide.forever.missing_sections.title",
						  "category": "survival"
						}
						"""));

		assertTrue(exception.getMessage().contains("forever:guide/missing_sections.json"));
		assertTrue(exception.getMessage().contains("section_keys"));
	}

	@Test
	void futureSchemaIsRejectedBeforeTheEntryIsUsed() {
		GuideDataException exception = assertThrows(
				GuideDataException.class,
				() -> GuideRegistryLoader.decodeJson(
						Identifier.fromNamespaceAndPath("forever", "guide/future.json"),
						"""
						{
						  "schema_version": 2,
						  "id": "forever:guide/future",
						  "title_key": "guide.forever.future.title",
					  "section_keys": ["guide.forever.future.body"],
						  "category": "survival"
						}
						"""));

		assertTrue(exception.getMessage().contains("forever:guide/future.json"));
		assertTrue(exception.getMessage().contains("schema version"));
	}

	@Test
	void titleMatchesRankAboveKeywordMatchesAndCategoryFilters() {
		GuideRegistry registry = bundledRegistry();

		GuideSearchResponse campfireResults = registry.search(GuideSearchQuery.text("campfire", 10));
		assertFalse(campfireResults.results().isEmpty());
		assertEquals(CAMPFIRE, campfireResults.results().getFirst().entryId());
		assertTrue(campfireResults.results().stream().anyMatch(hit -> hit.entryId().equals(FIRE_STARTING)));

		GuideSearchResponse tools = registry.search(GuideSearchQuery.text("bronze", 10));
		assertEquals(List.of(EARLY_TOOLS), tools.results().stream().map(GuideSearchHit::entryId).toList());

		GuideSearchResponse equipment = registry.search(GuideSearchQuery.category("equipment", 10));
		assertEquals(List.of(EARLY_TOOLS), equipment.results().stream().map(GuideSearchHit::entryId).toList());
	}

	@Test
	void searchResponseIsBoundedEvenWhenTheCallerRequestsMore() {
		List<GuideEntry> entries = new ArrayList<>();
		for (int index = 0; index < 100; index++) {
			entries.add(new GuideEntry(
					GuideEntry.CURRENT_SCHEMA,
					Identifier.fromNamespaceAndPath("forever", "guide/generated_" + index),
					"guide.forever.generated." + index + ".title",
					List.of("guide.forever.generated." + index + ".body"),
					"general",
					List.of("generated"),
					List.of(),
					List.of(),
					Optional.empty()));
		}

		GuideSearchResponse response = new GuideRegistry(entries).search(GuideSearchQuery.all(10_000));

		assertEquals(GuideSearchQuery.MAX_LIMIT, response.results().size());
		assertTrue(response.truncated());
		assertEquals(GuideSearchQuery.MAX_LIMIT, GuideSearchQuery.all(10_000).limit());
	}

	@Test
	void relatedEntryLinksMustResolveInsideTheSameSnapshot() {
		Identifier source = Identifier.fromNamespaceAndPath("forever", "guide/source");
		Identifier missing = Identifier.fromNamespaceAndPath("forever", "guide/missing");
		GuideEntry entry = new GuideEntry(
				source,
				"guide.forever.source.title",
				List.of("guide.forever.source.body"),
				"general",
				List.of(),
				List.of(missing),
				Optional.empty());

		GuideDataException exception = assertThrows(GuideDataException.class, () -> new GuideRegistry(List.of(entry)));

		assertTrue(exception.getMessage().contains(source.toString()));
		assertTrue(exception.getMessage().contains(missing.toString()));
	}

	@Test
	void gatingAnnotatesResultsWithoutHidingInstructions() {
		Identifier base = Identifier.fromNamespaceAndPath("forever", "guide/base");
		Identifier gated = Identifier.fromNamespaceAndPath("forever", "guide/gated");
		GuideEntry baseEntry = new GuideEntry(
				base, "guide.forever.base.title", List.of("guide.forever.base.body"), "general",
				List.of(), List.of(), Optional.empty());
		GuideEntry gatedEntry = new GuideEntry(
				gated, "guide.forever.gated.title", List.of("guide.forever.gated.body"), "general",
				List.of(), List.of(), Optional.of(new GuideGate(List.of(base), Optional.of("known_process"))));
		GuideRegistry registry = new GuideRegistry(List.of(baseEntry, gatedEntry));

		GuideSearchHit locked = registry.search(GuideSearchQuery.all(10)).results().stream()
				.filter(hit -> hit.entryId().equals(gated)).findFirst().orElseThrow();
		GuideSearchHit unlocked = registry.search(GuideSearchQuery.all(10).withAccess(
				new GuideAccess(Set.of(base), Set.of("known_process")))).results().stream()
				.filter(hit -> hit.entryId().equals(gated)).findFirst().orElseThrow();

		assertFalse(locked.accessible());
		assertTrue(unlocked.accessible());
		assertEquals(gatedEntry, registry.require(gated));
	}

	@Test
	void absentRecipeViewerIsAWorkingNoOp() {
		GuideRecipeViewerResult result = bundledRegistry().recipeView(CAMPFIRE);

		assertEquals(GuideRecipeViewerStatus.UNAVAILABLE, result.status());
		assertEquals(Optional.of(CAMPFIRE), result.guideEntryId());
		assertTrue(result.processIds().isEmpty());
		assertTrue(result.diagnostics().getFirst().contains("No optional recipe viewer"));
	}

	private static GuideRegistry bundledRegistry() {
		List<String> files = List.of("campfire_process.json", "fire_starting.json", "early_tools.json",
				"early_processing.json");
		List<GuideRegistryLoader.ResourceDocument> documents = files.stream().map(file -> {
			Identifier id = Identifier.fromNamespaceAndPath("forever", "guide/" + file);
			try (var stream = GuideRegistryTest.class.getClassLoader()
					.getResourceAsStream("data/forever/guide/" + file)) {
				assertNotNull(stream, "bundled guide resource must be present: " + file);
				return new GuideRegistryLoader.ResourceDocument(
						id, JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)));
			} catch (IOException exception) {
				throw new AssertionError("Could not read bundled guide resource " + id, exception);
			}
		}).toList();
		return GuideRegistryLoader.load(documents);
	}
}

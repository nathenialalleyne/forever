package dev.forever.core.mastery;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/** Loads and validates mastery JSON resources from {@code data/forever/mastery/}. */
public final class MasteryRegistryLoader {

	private static final String MASTERY_PATH = "mastery";
	private static final String LOADOUT_FILE = "loadout.json";

	private MasteryRegistryLoader() {
	}

	/** A resource document used by unit tests and by the resource-manager adapter. */
	public record ResourceDocument(Identifier resourceId, JsonElement contents) {
		public ResourceDocument {
			Objects.requireNonNull(resourceId, "resourceId");
			Objects.requireNonNull(contents, "contents");
		}
	}

	/** Loads every mastery resource visible to the server resource manager. */
	public static MasteryRegistry load(ResourceManager resourceManager) {
		Objects.requireNonNull(resourceManager, "resourceManager");
		Map<Identifier, Resource> resources = resourceManager.listResources(
				MASTERY_PATH,
				id -> id.getNamespace().equals("forever") && id.getPath().endsWith(".json"));
		List<ResourceDocument> documents = new ArrayList<>();
		resources.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(entry -> documents.add(read(entry.getKey(), entry.getValue())));
		return load(documents);
	}

	/** Loads an explicitly supplied, deterministic set of JSON documents. */
	public static MasteryRegistry load(List<ResourceDocument> documents) {
		Objects.requireNonNull(documents, "documents");
		if (documents.isEmpty()) {
			throw new MasteryDataException("No resources were found under data/forever/mastery/."
					+ " Add loadout.json and at least one mastery definition.");
		}

		LoadoutRules loadoutRules = null;
		List<MasteryDefinition> definitions = new ArrayList<>();
		for (ResourceDocument document : documents.stream()
				.sorted(Comparator.comparing(ResourceDocument::resourceId))
				.toList()) {
			if (isLoadoutDocument(document.resourceId())) {
				if (loadoutRules != null) {
					throw new MasteryDataException("Duplicate mastery loadout balance resource at "
							+ document.resourceId() + ". Keep exactly one loadout.json.");
				}
				loadoutRules = decodeLoadout(document.resourceId(), document.contents());
			} else {
				definitions.add(decodeDefinition(document.resourceId(), document.contents()));
			}
		}
		if (loadoutRules == null) {
			throw new MasteryDataException("Missing data/forever/mastery/loadout.json."
					+ " Focus and Supporting slot balance must be data-defined.");
		}
		return new MasteryRegistry(definitions, loadoutRules);
	}

	/** Decodes one definition and adds its resource path to any failure. */
	public static MasteryDefinition decodeDefinition(Identifier resourceId, JsonElement contents) {
		DataResult<MasteryDefinition> result = MasteryDefinition.CODEC.parse(JsonOps.INSTANCE, contents);
		return result.result().orElseThrow(() -> invalid(resourceId, result));
	}

	/** Decodes the balance file and adds its resource path to any failure. */
	public static LoadoutRules decodeLoadout(Identifier resourceId, JsonElement contents) {
		DataResult<LoadoutRules> result = LoadoutRules.CODEC.parse(JsonOps.INSTANCE, contents);
		return result.result().orElseThrow(() -> invalid(resourceId, result));
	}

	private static ResourceDocument read(Identifier resourceId, Resource resource) {
		try (Reader reader = resource.openAsReader()) {
			return new ResourceDocument(resourceId, JsonParser.parseReader(reader));
		} catch (IOException | JsonParseException exception) {
			throw new MasteryDataException("Could not read mastery resource " + resourceId
					+ ": " + exception.getMessage(), exception);
		}
	}

	private static boolean isLoadoutDocument(Identifier resourceId) {
		return resourceId.getPath().equals(MASTERY_PATH + "/" + LOADOUT_FILE);
	}

	private static MasteryDataException invalid(Identifier resourceId, DataResult<?> result) {
		String message = result.error()
				.map(DataResult.Error::message)
				.orElse("unknown codec failure");
		return new MasteryDataException("Invalid mastery data in " + resourceId + ": " + message);
	}
}

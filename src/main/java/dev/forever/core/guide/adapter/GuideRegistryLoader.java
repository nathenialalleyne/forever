package dev.forever.core.guide.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.forever.core.guide.domain.GuideDataException;
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

/** Loads and validates Field Guide JSON resources under {@code data/forever/guide/}. */
public final class GuideRegistryLoader {

	private static final String GUIDE_PATH = "guide";

	private GuideRegistryLoader() {
	}

	/** A parsed resource document retained so tests can name the source file in failures. */
	public record ResourceDocument(Identifier resourceId, JsonElement contents) {
		public ResourceDocument {
			Objects.requireNonNull(resourceId, "guide resource ID must not be null");
			Objects.requireNonNull(contents, "guide resource contents must not be null");
		}
	}

	/** Loads all active Forever guide JSON resources from the server resource manager. */
	public static GuideRegistry load(ResourceManager resourceManager) {
		Objects.requireNonNull(resourceManager, "resource manager must not be null");
		Map<Identifier, Resource> resources = resourceManager.listResources(
				GUIDE_PATH,
				id -> id.getNamespace().equals("forever") && id.getPath().endsWith(".json"));
		List<ResourceDocument> documents = resources.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(entry -> read(entry.getKey(), entry.getValue()))
				.toList();
		return load(documents);
	}

	/** Loads an explicit deterministic set of guide documents, primarily for unit tests. */
	public static GuideRegistry load(List<ResourceDocument> documents) {
		Objects.requireNonNull(documents, "guide documents must not be null");
		if (documents.isEmpty()) {
			throw new GuideDataException("No Field Guide resources were found under data/forever/guide/."
					+ " Add at least one validated guide entry.");
		}
		List<GuideEntry> entries = new ArrayList<>(documents.size());
		for (ResourceDocument document : documents.stream()
				.sorted(Comparator.comparing(ResourceDocument::resourceId))
				.toList()) {
			entries.add(decode(document.resourceId(), document.contents()));
		}
		return new GuideRegistry(entries);
	}

	/** Decodes one parsed JSON document and includes its resource identifier on failure. */
	public static GuideEntry decode(Identifier resourceId, JsonElement contents) {
		Objects.requireNonNull(resourceId, "guide resource ID must not be null");
		Objects.requireNonNull(contents, "guide resource contents must not be null");
		DataResult<GuideEntry> result = GuideEntry.CODEC.parse(JsonOps.INSTANCE, contents);
		return result.result().orElseThrow(() -> invalid(resourceId, result));
	}

	/** Decodes JSON text, retaining syntax errors with the offending resource identifier. */
	public static GuideEntry decodeJson(Identifier resourceId, String json) {
		Objects.requireNonNull(resourceId, "guide resource ID must not be null");
		Objects.requireNonNull(json, "guide JSON must not be null");
		try {
			return decode(resourceId, JsonParser.parseString(json));
		} catch (JsonParseException exception) {
			throw new GuideDataException("Invalid JSON in Field Guide resource " + resourceId + ": "
					+ exception.getMessage(), exception);
		}
	}

	private static ResourceDocument read(Identifier resourceId, Resource resource) {
		try (Reader reader = resource.openAsReader()) {
			return new ResourceDocument(resourceId, JsonParser.parseReader(reader));
		} catch (JsonParseException exception) {
			throw new GuideDataException("Invalid JSON in Field Guide resource " + resourceId + " from "
					+ resource.sourcePackId() + ": " + exception.getMessage(), exception);
		} catch (IOException exception) {
			throw new GuideDataException("Could not read Field Guide resource " + resourceId + " from "
					+ resource.sourcePackId() + ": " + exception.getMessage(), exception);
		}
	}

	private static GuideDataException invalid(Identifier resourceId, DataResult<?> result) {
		String message = result.error().map(DataResult.Error::message).orElse("unknown codec failure");
		return new GuideDataException("Invalid Field Guide data in " + resourceId + ": " + message);
	}
}

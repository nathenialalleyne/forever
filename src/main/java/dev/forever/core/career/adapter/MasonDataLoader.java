package dev.forever.core.career.adapter;

import dev.forever.core.career.domain.CareerDataException;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/** Loads the two required Mason data documents from {@code data/forever/career/}. */
public final class MasonDataLoader {

	private static final String CAREER_PATH = "career";
	private static final String CAREER_FILE = "career/mason.json";
	private static final String CATALOG_FILE = "career/mason_catalog.json";

	private MasonDataLoader() {
	}

	public record ResourceDocument(Identifier resourceId, JsonElement contents) {
		public ResourceDocument {
			Objects.requireNonNull(resourceId, "resourceId");
			Objects.requireNonNull(contents, "contents");
		}
	}

	public static MasonData load(ResourceManager resourceManager) {
		Objects.requireNonNull(resourceManager, "resourceManager");
		Map<Identifier, Resource> resources = resourceManager.listResources(
				CAREER_PATH,
				id -> id.getNamespace().equals("forever") && id.getPath().endsWith(".json"));
		List<ResourceDocument> documents = new ArrayList<>();
		resources.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(entry -> documents.add(read(entry.getKey(), entry.getValue())));
		return load(documents);
	}

	public static MasonData load(List<ResourceDocument> documents) {
		Objects.requireNonNull(documents, "documents");
		ResourceDocument career = null;
		ResourceDocument catalog = null;
		for (ResourceDocument document : documents.stream()
				.sorted(Comparator.comparing(ResourceDocument::resourceId))
				.toList()) {
			if (!document.resourceId().getNamespace().equals("forever")) {
				continue;
			}
			switch (document.resourceId().getPath()) {
				case CAREER_FILE -> {
					if (career != null) {
						throw new CareerDataException("Duplicate Mason career resource at " + document.resourceId() + ".");
					}
					career = document;
				}
				case CATALOG_FILE -> {
					if (catalog != null) {
						throw new CareerDataException("Duplicate Mason catalogue resource at " + document.resourceId() + ".");
					}
					catalog = document;
				}
				default -> throw new CareerDataException("Unknown Mason career resource " + document.resourceId()
						+ ". Expected mason.json or mason_catalog.json.");
			}
		}
		if (career == null || catalog == null) {
			throw new CareerDataException("Mason data requires both data/forever/career/mason.json and "
					+ "data/forever/career/mason_catalog.json.");
		}
		return new MasonData(decodeCareer(career.resourceId(), career.contents()),
				decodeCatalog(catalog.resourceId(), catalog.contents()));
	}

	public static MasonCareerDefinition decodeCareer(Identifier resourceId, JsonElement contents) {
		DataResult<MasonCareerDefinition> result = MasonCareerDefinition.CODEC.parse(JsonOps.INSTANCE, contents);
		return result.result().orElseThrow(() -> invalid(resourceId, result));
	}

	public static MasonCatalog decodeCatalog(Identifier resourceId, JsonElement contents) {
		DataResult<MasonCatalog> result = MasonCatalog.CODEC.parse(JsonOps.INSTANCE, contents);
		return result.result().orElseThrow(() -> invalid(resourceId, result));
	}

	/** Loads the packaged fallback before the first server resource reload. */
	public static void installBundled() {
		ResourceDocument career = bundled(CAREER_FILE);
		ResourceDocument catalog = bundled(CATALOG_FILE);
		MasonDataRegistry.install(load(List.of(career, catalog)));
	}

	private static ResourceDocument bundled(String path) {
		try (InputStream stream = MasonDataLoader.class.getClassLoader().getResourceAsStream("data/forever/" + path)) {
			if (stream == null) {
				throw new CareerDataException("Bundled Mason resource is missing at data/forever/" + path + ".");
			}
			try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				return new ResourceDocument(Identifier.fromNamespaceAndPath("forever", path),
						JsonParser.parseReader(reader));
			}
		} catch (IOException | JsonParseException exception) {
			throw new CareerDataException("Could not read bundled Mason resource data/forever/" + path + ".", exception);
		}
	}

	private static ResourceDocument read(Identifier resourceId, Resource resource) {
		try (Reader reader = resource.openAsReader()) {
			return new ResourceDocument(resourceId, JsonParser.parseReader(reader));
		} catch (IOException | JsonParseException exception) {
			throw new CareerDataException("Could not read Mason resource " + resourceId + ": "
					+ exception.getMessage(), exception);
		}
	}

	private static CareerDataException invalid(Identifier resourceId, DataResult<?> result) {
		String message = result.error().map(DataResult.Error::message).orElse("unknown codec failure");
		return new CareerDataException("Invalid Mason data in " + resourceId + ": " + message);
	}
}

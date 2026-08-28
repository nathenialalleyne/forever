package dev.forever.core.economy;

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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/** Loads the single data-defined balance file for the hybrid Obol currency. */
public final class EconomyBalanceLoader {

	private static final String BALANCE_PATH = "economy";
	private static final String BALANCE_FILE = "economy/balance.json";

	private EconomyBalanceLoader() {
	}

	public record ResourceDocument(Identifier resourceId, JsonElement contents) {
		public ResourceDocument {
			Objects.requireNonNull(resourceId, "resourceId");
			Objects.requireNonNull(contents, "contents");
		}
	}

	public static EconomyBalance load(ResourceManager resourceManager) {
		Objects.requireNonNull(resourceManager, "resourceManager");
		Map<Identifier, Resource> resources = resourceManager.listResources(
				BALANCE_PATH,
				id -> id.getNamespace().equals("forever") && id.getPath().endsWith(".json"));
		List<ResourceDocument> documents = resources.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(entry -> read(entry.getKey(), entry.getValue()))
				.toList();
		return load(documents);
	}

	public static EconomyBalance load(List<ResourceDocument> documents) {
		Objects.requireNonNull(documents, "documents");
		if (documents.size() != 1 || !documents.getFirst().resourceId().getPath().equals(BALANCE_FILE)) {
			throw new EconomyDataException("Expected exactly one economy balance at data/forever/"
					+ BALANCE_FILE + ", but found " + documents.size() + " resource(s).");
		}
		ResourceDocument document = documents.getFirst();
		return decode(document.resourceId(), document.contents());
	}

	public static EconomyBalance decode(Identifier resourceId, JsonElement contents) {
		DataResult<EconomyBalance> result = EconomyBalance.CODEC.parse(JsonOps.INSTANCE, contents);
		return result.result().orElseThrow(() -> invalid(resourceId, result));
	}

	/** Installs the packaged fallback before the first server data reload. */
	public static void installBundled() {
		try (InputStream stream = EconomyBalanceLoader.class.getClassLoader()
				.getResourceAsStream("data/forever/" + BALANCE_FILE)) {
			if (stream == null) {
				throw new EconomyDataException("Bundled economy balance is missing at data/forever/" + BALANCE_FILE + ".");
			}
			try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				EconomyBalanceAccess.install(decode(
						Identifier.fromNamespaceAndPath("forever", BALANCE_FILE), JsonParser.parseReader(reader)));
			}
		} catch (IOException | JsonParseException exception) {
			throw new EconomyDataException("Could not read bundled economy balance data/forever/"
					+ BALANCE_FILE + ".", exception);
		}
	}

	private static ResourceDocument read(Identifier resourceId, Resource resource) {
		try (Reader reader = resource.openAsReader()) {
			return new ResourceDocument(resourceId, JsonParser.parseReader(reader));
		} catch (IOException | JsonParseException exception) {
			throw new EconomyDataException("Could not read economy resource " + resourceId + ": "
					+ exception.getMessage(), exception);
		}
	}

	private static EconomyDataException invalid(Identifier resourceId, DataResult<?> result) {
		String message = result.error().map(DataResult.Error::message).orElse("unknown codec failure");
		return new EconomyDataException("Invalid economy balance in " + resourceId + ": " + message);
	}
}

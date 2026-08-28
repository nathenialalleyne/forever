package dev.forever.core.settlement;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/** Loads the data-defined settlement balance from {@code data/forever/settlement/}. */
public final class SettlementBalanceLoader {

	private static final String BALANCE_PATH = "settlement";
	private static final String BALANCE_FILE = "settlement.json";

	private SettlementBalanceLoader() {
	}

	public record ResourceDocument(Identifier resourceId, JsonElement contents) {
		public ResourceDocument {
			Objects.requireNonNull(resourceId, "resourceId");
			Objects.requireNonNull(contents, "contents");
		}
	}

	public static SettlementBalance load(ResourceManager resourceManager) {
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

	public static SettlementBalance load(List<ResourceDocument> documents) {
		Objects.requireNonNull(documents, "documents");
		if (documents.size() != 1) {
			throw new SettlementDataException("Expected exactly one settlement balance resource at "
					+ "data/forever/settlement/settlement.json, but found " + documents.size() + ".");
		}
		ResourceDocument document = documents.getFirst();
		if (!document.resourceId().getPath().equals(BALANCE_PATH + "/" + BALANCE_FILE)) {
			throw new SettlementDataException("Settlement balance must be stored as data/forever/settlement/"
					+ BALANCE_FILE + ", not " + document.resourceId() + ".");
		}
		DataResult<SettlementBalance> result = SettlementBalance.CODEC.parse(JsonOps.INSTANCE, document.contents());
		return result.result().orElseThrow(() -> invalid(document.resourceId(), result));
	}

	public static SettlementBalance decode(Identifier resourceId, JsonElement contents) {
		DataResult<SettlementBalance> result = SettlementBalance.CODEC.parse(JsonOps.INSTANCE, contents);
		return result.result().orElseThrow(() -> invalid(resourceId, result));
	}

	private static ResourceDocument read(Identifier resourceId, Resource resource) {
		try (Reader reader = resource.openAsReader()) {
			return new ResourceDocument(resourceId, JsonParser.parseReader(reader));
		} catch (IOException | JsonParseException exception) {
			throw new SettlementDataException("Could not read settlement balance resource " + resourceId
					+ ": " + exception.getMessage(), exception);
		}
	}

	private static SettlementDataException invalid(Identifier resourceId, DataResult<?> result) {
		String message = result.error().map(DataResult.Error::message).orElse("unknown codec failure");
		return new SettlementDataException("Invalid settlement balance in " + resourceId + ": " + message);
	}
}

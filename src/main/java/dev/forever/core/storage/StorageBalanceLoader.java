package dev.forever.core.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loads the single active storage balance definition from server data. */
public final class StorageBalanceLoader implements SimpleSynchronousResourceReloadListener {

	private static final Logger LOGGER = LoggerFactory.getLogger("forever/storage-balance");
	private static final FileToIdConverter CONVERTER = FileToIdConverter.json("storage");
	private static final Identifier LISTENER_ID = Identifier.fromNamespaceAndPath("forever", "storage_balance");
	private static final Identifier BUNDLED_RESOURCE = Identifier.fromNamespaceAndPath(
			"forever", "storage/balance.json");

	@Override
	public Identifier getFabricId() {
		return LISTENER_ID;
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		Map<Identifier, Resource> resources = CONVERTER.listMatchingResources(resourceManager);
		if (resources.size() != 1) {
			throw reject("expected exactly one storage balance JSON under data/forever/storage, found "
					+ resources.size() + ": " + resources.keySet());
		}

		Map.Entry<Identifier, Resource> entry = resources.entrySet().iterator().next();
		Identifier balanceId = CONVERTER.fileToId(entry.getKey());
		StorageBalance balance = decodeResource(balanceId, entry.getValue());
		StorageBalanceAccess.install(balance);
		LOGGER.info("Loaded storage balance {} with {} initial cache slots and {} expanded slots",
				balanceId, balance.initialCacheSlots(), balance.expandedCacheSlots());
	}

	/** Installs the packaged balance before the first server data reload. */
	static void installBundled() {
		try (InputStream stream = StorageBalanceLoader.class.getClassLoader()
				.getResourceAsStream("data/forever/storage/balance.json")) {
			if (stream == null) {
				throw reject("bundled storage balance is missing at " + BUNDLED_RESOURCE);
			}
			try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				StorageBalanceAccess.install(decodeReader(BUNDLED_RESOURCE, reader));
			}
		} catch (IOException exception) {
			throw reject("could not read bundled storage balance " + BUNDLED_RESOURCE, exception);
		}
	}

	static void register() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new StorageBalanceLoader());
	}

	private static StorageBalance decodeResource(Identifier id, Resource resource) {
		try (Reader reader = resource.openAsReader()) {
			return decodeReader(id, reader);
		} catch (IOException exception) {
			throw reject("could not read storage balance " + id + " from " + resource.sourcePackId(), exception);
		}
	}

	private static StorageBalance decodeReader(Identifier id, Reader reader) {
		try {
			JsonElement element = JsonParser.parseReader(reader);
			return StorageBalance.CODEC.parse(JsonOps.INSTANCE, element)
					.getOrThrow(message -> new IllegalStateException(
							"storage balance " + id + " failed validation: " + message));
		} catch (JsonParseException exception) {
			throw reject("storage balance " + id + " is not valid JSON", exception);
		}
	}

	private static IllegalStateException reject(String message) {
		LOGGER.error("Rejected storage balance data: {}", message);
		return new IllegalStateException(message);
	}

	private static IllegalStateException reject(String message, Throwable cause) {
		LOGGER.error("Rejected storage balance data: {}", message, cause);
		return new IllegalStateException(message, cause);
	}
}

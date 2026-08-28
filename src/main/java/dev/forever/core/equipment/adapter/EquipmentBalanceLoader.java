package dev.forever.core.equipment.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import dev.forever.core.equipment.EquipmentBalance;
import dev.forever.core.equipment.EquipmentBalanceRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loads and validates the single active equipment balance definition. */
public final class EquipmentBalanceLoader implements SimpleSynchronousResourceReloadListener {

	private static final Logger LOGGER = LoggerFactory.getLogger("forever/equipment-balance");
	private static final FileToIdConverter CONVERTER = FileToIdConverter.json("equipment");
	private static final Identifier LISTENER_ID = Identifier.fromNamespaceAndPath("forever", "equipment_balance");
	private static final Identifier BUNDLED_RESOURCE = Identifier.fromNamespaceAndPath(
			"forever", "equipment/balance.json");

	@Override
	public Identifier getFabricId() {
		return LISTENER_ID;
	}

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager) {
		Map<Identifier, Resource> resources = CONVERTER.listMatchingResources(resourceManager);
		if (resources.size() != 1) {
			throw reject("expected exactly one equipment balance JSON under data/forever/equipment, found "
					+ resources.size() + ": " + resources.keySet());
		}

		Map.Entry<Identifier, Resource> entry = resources.entrySet().iterator().next();
		Identifier balanceId = CONVERTER.fileToId(entry.getKey());
		EquipmentBalance balance = decodeResource(balanceId, entry.getValue());
		EquipmentBalanceRegistry.install(balance);
		LOGGER.info("Loaded equipment balance {} with {} path definitions", balanceId, balance.paths().size());
	}

	/**
	 * Installs the packaged definition for callers that initialise the system before a server
	 * resource reload has happened. A normal server reload replaces it with the active data-pack
	 * definition.
	 */
	static void installBundled() {
		try (InputStream stream = EquipmentBalanceLoader.class.getClassLoader()
				.getResourceAsStream("data/forever/equipment/balance.json")) {
			if (stream == null) {
				throw reject("bundled equipment balance is missing at " + BUNDLED_RESOURCE);
			}
			try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				EquipmentBalanceRegistry.install(decodeReader(BUNDLED_RESOURCE, reader));
			}
		} catch (IOException exception) {
			throw reject("could not read bundled equipment balance " + BUNDLED_RESOURCE, exception);
		}
	}

	static void register() {
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new EquipmentBalanceLoader());
	}

	private static EquipmentBalance decodeResource(Identifier id, Resource resource) {
		try (Reader reader = resource.openAsReader()) {
			return decodeReader(id, reader);
		} catch (IOException exception) {
			throw reject("could not read equipment balance " + id + " from " + resource.sourcePackId(), exception);
		}
	}

	private static EquipmentBalance decodeReader(Identifier id, Reader reader) {
		try {
			JsonElement element = JsonParser.parseReader(reader);
			EquipmentBalance balance = EquipmentBalance.CODEC.parse(JsonOps.INSTANCE, element)
					.getOrThrow(message -> new IllegalStateException(
						"equipment balance " + id + " failed validation: " + message));
			validateItemReferences(id, balance);
			return balance;
		} catch (JsonParseException exception) {
			throw reject("equipment balance " + id + " is not valid JSON", exception);
		} catch (RuntimeException exception) {
			throw reject("equipment balance " + id + " failed validation: " + exception.getMessage(), exception);
		}
	}

	private static void validateItemReferences(Identifier balanceId, EquipmentBalance balance) {
		List<Identifier> missing = new ArrayList<>();
		for (EquipmentBalance.ItemCost cost : List.of(
				balance.fieldRepairCost(), balance.workshopRepairCost(), balance.reforgeCost())) {
			if (!BuiltInRegistries.ITEM.containsKey(cost.item())) {
				missing.add(cost.item());
			}
		}
		if (!missing.isEmpty()) {
			throw reject("equipment balance " + balanceId
					+ " references item IDs that are not registered: " + missing);
		}
	}

	private static IllegalStateException reject(String message) {
		LOGGER.error("Rejected equipment balance data: {}", message);
		return new IllegalStateException(message);
	}

	private static IllegalStateException reject(String message, Throwable cause) {
		LOGGER.error("Rejected equipment balance data: {}", message, cause);
		return new IllegalStateException(message, cause);
	}
}

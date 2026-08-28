package dev.forever.core.mastery.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;

final class MasteryTestFixtures {

	private static final String RESOURCE_ROOT = "data/forever/mastery/";

	private MasteryTestFixtures() {
	}

	static MasteryRegistry fullRegistry() {
		List<String> files = List.of(
				"loadout.json",
				"builder.json",
				"prospector.json",
				"cook.json",
				"smith.json",
				"explorer_cartographer.json",
				"merchant_steward.json",
				"farmer_naturalist.json",
				"alchemist_herbalist.json",
				"rider_animal_handler.json",
				"fisher_mariner.json");
		List<MasteryRegistryLoader.ResourceDocument> documents = new ArrayList<>();
		for (String file : files) {
			documents.add(new MasteryRegistryLoader.ResourceDocument(
					Identifier.fromNamespaceAndPath("forever", "mastery/" + file), read(file)));
		}
		return MasteryRegistryLoader.load(documents);
	}

	static JsonElement json(String json) {
		return JsonParser.parseString(json);
	}

	static MasteryRegistryLoader.ResourceDocument document(String file, String json) {
		return new MasteryRegistryLoader.ResourceDocument(
				Identifier.fromNamespaceAndPath("forever", "mastery/" + file), json(json));
	}

	private static JsonElement read(String file) {
		InputStream stream = MasteryTestFixtures.class.getClassLoader()
				.getResourceAsStream(RESOURCE_ROOT + file);
		if (stream == null) {
			throw new IllegalStateException("Missing test resource " + RESOURCE_ROOT + file);
		}
		try (InputStream input = stream;
				Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
			return JsonParser.parseReader(reader);
		} catch (IOException exception) {
			throw new IllegalStateException("Could not read test resource " + file, exception);
		}
	}
}

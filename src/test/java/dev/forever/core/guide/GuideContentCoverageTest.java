package dev.forever.core.guide;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Guards the Field Guide's shipped content against silent rot.
 *
 * <p>Design principle 15 makes the Field Guide a shipping requirement: a feature is not
 * complete if the player cannot understand it from inside the game. Principle 2 adds
 * that once the game expects a known process, that process must be inspectable in-game.
 * Neither holds if an entry references a translation key that does not exist, because
 * the player would see a raw key like {@code guide.forever.x.title} instead of prose.
 *
 * <p>This test reads the real shipped resources rather than fixtures, so it fails when
 * someone adds an entry and forgets its translations. That defect is invisible to unit
 * tests of the loader and to GameTests, which check that entries load rather than that
 * they read correctly.
 */
class GuideContentCoverageTest {

	private static final Path GUIDE_DIR = Path.of("src/main/resources/data/forever/guide");
	private static final Path LANG_FILE = Path.of("src/main/resources/assets/forever/lang/en_us.json");

	/**
	 * Matcha implementation details that must never reach player-facing guide text.
	 * See design principle 13 and ADR 0002: only {@code dev.forever.compat.matcha} may
	 * know these, and guide content is not that package.
	 */
	private static final List<String> MATCHA_INTERNAL_MARKERS =
			List.of("mcfunction", "scoreboard", "custom_model_data", "matcha:", "advancement/");

	private static JsonObject readLang() throws IOException {
		try (InputStream in = Files.newInputStream(LANG_FILE)) {
			return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
		}
	}

	private static Map<String, JsonObject> readEntries() throws IOException {
		Map<String, JsonObject> entries = new HashMap<>();
		try (Stream<Path> files = Files.list(GUIDE_DIR)) {
			for (Path file : files.filter(p -> p.toString().endsWith(".json")).toList()) {
				try (InputStream in = Files.newInputStream(file)) {
					JsonObject entry = JsonParser
							.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
							.getAsJsonObject();
					entries.put(entry.get("id").getAsString(), entry);
				}
			}
		}
		return entries;
	}

	private static List<String> keysOf(JsonObject entry) {
		List<String> keys = new ArrayList<>();
		keys.add(entry.get("title_key").getAsString());
		entry.getAsJsonArray("section_keys").forEach(k -> keys.add(k.getAsString()));
		return keys;
	}

	@Test
	@DisplayName("every guide entry's translation keys exist, so no player ever sees a raw key")
	void everyEntryIsFullyTranslated() throws IOException {
		JsonObject lang = readLang();
		List<String> missing = new ArrayList<>();

		for (Map.Entry<String, JsonObject> entry : readEntries().entrySet()) {
			for (String key : keysOf(entry.getValue())) {
				if (!lang.has(key)) {
					missing.add(entry.getKey() + " -> " + key);
				}
			}
		}

		assertTrue(missing.isEmpty(),
				"Guide entries reference translation keys that do not exist in en_us.json. "
						+ "A player would see the raw key instead of readable text. Missing: " + missing);
	}

	@Test
	@DisplayName("no guide entry links to an entry that does not exist")
	void relatedEntryLinksResolve() throws IOException {
		Map<String, JsonObject> entries = readEntries();
		Set<String> ids = new HashSet<>(entries.keySet());
		List<String> dangling = new ArrayList<>();

		for (Map.Entry<String, JsonObject> entry : entries.entrySet()) {
			JsonElement related = entry.getValue().get("related_entry_ids");
			if (related == null) {
				continue;
			}
			for (JsonElement ref : related.getAsJsonArray()) {
				if (!ids.contains(ref.getAsString())) {
					dangling.add(entry.getKey() + " -> " + ref.getAsString());
				}
			}
		}

		assertTrue(dangling.isEmpty(), "Guide entries link to missing entries: " + dangling);
	}

	@Test
	@DisplayName("guide text never exposes Matcha implementation details")
	void guideTextIsFreeOfMatchaInternals() throws IOException {
		JsonObject lang = readLang();
		List<String> leaks = new ArrayList<>();

		for (String key : lang.keySet()) {
			if (!key.startsWith("guide.")) {
				continue;
			}
			String value = lang.get(key).getAsString().toLowerCase(Locale.ROOT);
			for (String marker : MATCHA_INTERNAL_MARKERS) {
				if (value.contains(marker)) {
					leaks.add(key + " contains '" + marker + "'");
				}
			}
		}

		assertTrue(leaks.isEmpty(),
				"Guide text leaks Matcha internals, violating design principle 13. "
						+ "Describe the process through stable Forever concepts instead. Found: " + leaks);
	}

	@Test
	@DisplayName("each implemented player-facing system has at least one guide entry")
	void implementedSystemsAreDocumented() throws IOException {
		// Principle 15: a feature is not complete if the player cannot understand it
		// from inside the game. These categories correspond to the systems that are
		// implemented and player-visible today.
		Set<String> required = Set.of("equipment", "mastery", "settlement", "storage", "economy");
		Set<String> present = new HashSet<>();

		for (JsonObject entry : readEntries().values()) {
			present.add(entry.get("category").getAsString());
		}

		List<String> undocumented = required.stream().filter(c -> !present.contains(c)).sorted().toList();

		assertTrue(undocumented.isEmpty(),
				"Implemented systems have no Field Guide entry, violating design principle 15: "
						+ undocumented + ". Present categories: " + present);
	}
}

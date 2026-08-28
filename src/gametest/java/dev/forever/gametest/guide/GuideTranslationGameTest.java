package dev.forever.gametest.guide;

import dev.forever.core.guide.ForeverGuide;
import dev.forever.core.guide.GuideEntry;
import dev.forever.core.guide.GuideRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;

/**
 * Acceptance check that the Field Guide is actually readable by a player.
 *
 * <p>This exists because the unit-level coverage test reads files from the working
 * directory with a third-party JSON parser. That proves the files on disk are
 * self-consistent, but it does not prove the game can find them, that Minecraft's own
 * language parser accepts them, or that a translated component resolves to prose
 * instead of echoing its key. Those are the properties design principle 15 actually
 * depends on, and only the real runtime can demonstrate them.
 *
 * <p>Everything here goes through the shipping path: the server's
 * {@link ResourceManager} finds the language file the same way the game does,
 * {@link Language#loadFromJson} parses it with Minecraft's own parser, and
 * {@link Component#translatable} renders through the real translation pipeline.
 */
public final class GuideTranslationGameTest {

	private static final String LANG_PATH = "lang/en_us.json";
	private static final String NAMESPACE = "forever";

	static {
		ForeverGuide.initialize();
	}

	/**
	 * Loads the shipped language file exactly as the game does.
	 *
	 * <p>Language files live under {@code assets/}, which belongs to the resource-pack
	 * side. A dedicated server's {@link ResourceManager} only serves {@code data/}, so
	 * asking it for a language file correctly finds nothing. The client instead reads
	 * the file from the mod jar's classpath, which is what this does.
	 *
	 * <p>This distinction is the reason this test exists. The path is real and shipped,
	 * but it is not reachable through the server data pipeline, and a check that assumed
	 * otherwise would have been testing the wrong thing.
	 */
	private static Map<String, String> loadShippedLanguage(GameTestHelper helper) {
		String classpathPath = "assets/" + NAMESPACE + "/" + LANG_PATH;
		Map<String, String> translations = new HashMap<>();

		try (InputStream in = GuideTranslationGameTest.class.getClassLoader()
				.getResourceAsStream(classpathPath)) {
			helper.assertTrue(in != null,
					"The shipped language file " + classpathPath + " is not on the mod classpath, so "
							+ "no translation key could ever resolve for a player.");
			Language.loadFromJson(in, translations::put);
		} catch (IOException exception) {
			throw new AssertionError("Minecraft could not read the shipped language file: " + classpathPath, exception);
		}

		helper.assertTrue(!translations.isEmpty(),
				"Minecraft's own parser read zero translations from " + classpathPath + ".");
		return translations;
	}

	/**
	 * Every guide entry the live server loads must have prose for each of its keys.
	 *
	 * <p>This is the player-visible acceptance condition for principle 15: opening any
	 * entry must show readable text, never a raw identifier.
	 */
	@GameTest
	public void everyLoadedGuideEntryRendersAsProse(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Map<String, String> translations = loadShippedLanguage(helper);

		GuideRegistry registry = ForeverGuide.load(level.getServer().getResourceManager());
		List<GuideEntry> entries = List.copyOf(registry.entries().values());

		helper.assertTrue(!entries.isEmpty(),
				"The live server loaded no guide entries, so nothing could be documented.");

		List<String> unreadable = new ArrayList<>();
		for (GuideEntry entry : entries) {
			List<String> keys = new ArrayList<>();
			keys.add(entry.titleKey());
			keys.addAll(entry.sectionKeys());

			for (String key : keys) {
				String value = translations.get(key);
				if (value == null || value.isBlank()) {
					unreadable.add(entry.id() + " -> " + key);
				}
			}
		}

		helper.assertTrue(unreadable.isEmpty(),
				"Guide entries loaded by the live server reference keys with no prose, so a player "
						+ "would read a raw identifier: " + unreadable);
		helper.succeed();
	}

	/**
	 * Renders real components through the translation pipeline and asserts the output
	 * differs from the key.
	 *
	 * <p>A missing translation makes {@code Component.translatable(key).getString()}
	 * return the key itself. Asserting the rendered string differs from the key is the
	 * closest available check to "what the player sees on screen" without a client.
	 */
	@GameTest
	public void translatedComponentsDoNotEchoTheirKeys(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Map<String, String> translations = loadShippedLanguage(helper);

		// Install the shipped translations into the runtime the same way a language
		// reload does, so Component rendering resolves against real content.
		Language previous = Language.getInstance();
		Language shipped = new Language() {
			@Override
			public String getOrDefault(String key, String fallback) {
				return translations.getOrDefault(key, fallback);
			}

			@Override
			public boolean has(String key) {
				return translations.containsKey(key);
			}

			@Override
			public boolean isDefaultRightToLeft() {
				return false;
			}

			@Override
			public net.minecraft.util.FormattedCharSequence getVisualOrder(net.minecraft.network.chat.FormattedText text) {
				return previous.getVisualOrder(text);
			}
		};

		try {
			Language.inject(shipped);

			GuideRegistry registry = ForeverGuide.load(level.getServer().getResourceManager());
			List<String> echoed = new ArrayList<>();

			for (GuideEntry entry : registry.entries().values()) {
				List<String> keys = new ArrayList<>();
				keys.add(entry.titleKey());
				keys.addAll(entry.sectionKeys());

				for (String key : keys) {
					String rendered = Component.translatable(key).getString();
					if (rendered.equals(key)) {
						echoed.add(key);
					}
				}
			}

			// Sanity check the detector itself: an unknown key must echo, otherwise this
			// test would pass even when translation is completely broken.
			String bogus = "guide.forever.__definitely_absent__.title";
			helper.assertTrue(Component.translatable(bogus).getString().equals(bogus),
					"The echo detector is not working; an unknown key should render as itself.");

			helper.assertTrue(echoed.isEmpty(),
					"These keys rendered as raw identifiers through the real translation pipeline: " + echoed);
		} finally {
			Language.inject(previous);
		}

		helper.succeed();
	}

	/**
	 * The player-facing messages emitted by gameplay code must also render as prose.
	 *
	 * <p>Guide entries are data and are easy to audit. Tooltips and rejection messages
	 * are string literals in Java, and a missing one surfaces at the worst moment: when
	 * the player has just been told no.
	 */
	@GameTest
	public void gameplayMessagesRenderAsProse(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Map<String, String> translations = loadShippedLanguage(helper);

		// Representative player-facing keys from each implemented system. These are the
		// strings a player actually hits during normal use.
		List<String> playerFacing = List.of(
				"item.forever.field_tool",
				"tooltip.forever.equipment.condition",
				"tooltip.forever.equipment.status.broken",
				"tooltip.forever.equipment.active_path",
				"message.forever.equipment.field_repair_complete",
				"message.forever.equipment.workshop_required",
				"message.forever.equipment.reforge_complete",
				"mastery.forever.loadout.invalid",
				"mastery.forever.loadout.switch_requires_context",
				"mastery.forever.progression.unapproved_source");

		List<String> missing = new ArrayList<>();
		for (String key : playerFacing) {
			String value = translations.get(key);
			if (value == null || value.isBlank()) {
				missing.add(key);
			}
		}

		helper.assertTrue(missing.isEmpty(),
				"Player-facing gameplay messages have no prose in the shipped language file: " + missing);
		helper.succeed();
	}
}

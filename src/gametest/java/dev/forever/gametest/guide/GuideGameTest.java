package dev.forever.gametest.guide;

import dev.forever.core.guide.ForeverGuide;
import dev.forever.core.guide.GuideEntry;
import dev.forever.core.guide.GuideRegistry;
import dev.forever.core.guide.GuideSearchQuery;
import dev.forever.core.guide.GuideSearchResponse;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

/** Dedicated-server acceptance check for FVR-200 through FVR-204. */
public final class GuideGameTest {

	private static final Identifier CAMPFIRE = Identifier.fromNamespaceAndPath("forever", "guide/campfire_process");

	static {
		// ForeverMod must call this in production. The GameTest entrypoint initialises the
		// owned slice here so this test also exercises its registration in isolation.
		ForeverGuide.initialize();
	}

	@GameTest
	public void guideRegistryLoadsFromLiveServerAndRetrievesKnownEntry(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		GuideRegistry registry = ForeverGuide.load(level.getServer().getResourceManager());
		GuideEntry campfire = registry.require(CAMPFIRE);
		helper.assertTrue(campfire.sectionKeys().size() >= 5,
				"the campfire entry must carry a complete multi-step process");
		helper.assertTrue(campfire.processIds().size() == 1,
				"the campfire entry must expose its stable process link");

		GuideSearchResponse search = registry.search(GuideSearchQuery.text("campfire", 4));
		helper.assertTrue(search.results().stream().anyMatch(hit -> hit.entryId().equals(CAMPFIRE)),
				"a known campfire entry must be retrievable through the server search API");
		helper.succeed();
	}
}

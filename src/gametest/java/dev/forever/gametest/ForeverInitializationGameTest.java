package dev.forever.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;

/**
 * Minimal initialisation smoke GameTest.
 *
 * <p>This proves the GameTest harness actually starts a dedicated server, loads
 * Forever, and runs a test method. It deliberately asserts almost nothing about
 * gameplay, because Forever has no gameplay yet. Its value is that the harness is
 * verified working now, rather than being debugged under pressure during the first
 * system that genuinely needs it (FVR-106, equipment persistence tests).
 *
 * <p>It also serves as a real dedicated-server smoke check: {@code runGametest} uses
 * a server run configuration, so if common code ever referenced a client-only class,
 * this test would fail to launch.
 *
 * <p>Note on naming: Minecraft 26.2 is distributed non-obfuscated and uses Mojang
 * names ({@code GameTestHelper}, {@code ServerLevel}). Yarn names such as
 * {@code TestContext} and {@code ServerWorld} do not apply here. See
 * {@code docs/dependency-baseline.md}.
 *
 * <p>Run with: {@code ./gradlew runGametest}
 */
public class ForeverInitializationGameTest {

	/**
	 * Verifies that the test server reached a running state with a live world.
	 *
	 * <p>If Forever's common initialiser had thrown, or had touched a client-only
	 * class, the dedicated test server would have failed before this method executed.
	 *
	 * @param helper harness handle supplied by the GameTest framework
	 */
	@GameTest
	public void serverStartsWithForeverLoaded(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();

		if (level == null) {
			throw new AssertionError("GameTest helper provided no server level.");
		}

		if (level.getServer() == null) {
			throw new AssertionError("GameTest level has no server; the dedicated-server path is broken.");
		}

		helper.succeed();
	}
}

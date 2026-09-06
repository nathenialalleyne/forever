package dev.forever.compat.matcha.diagnostic;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;

/** Dedicated-server fixture for the diagnostic-only candidate artifact. */
public final class MatchaDiagnosticArtifactGameTest {
	@GameTest
	public void candidateObservesTheLiveServerWithoutClientClasses(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();
		MatchaDiagnosticReport report = MatchaDiagnosticObserver.observe(server);

		helper.assertTrue(report != null, "The candidate did not produce a bounded report.");
		String expectedStatus = System.getProperty("mrh013.expected-status", MatchaDiagnosticStatus.MISSING.name());
		helper.assertTrue(report.status().name().equals(expectedStatus),
				"The isolated fixture expected " + expectedStatus + ", got " + report.status());
		if (report.status() == MatchaDiagnosticStatus.MISSING) {
			helper.assertTrue(String.join(" ", report.remedies()).contains(
					"6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248"),
					"The live missing report did not include the locked SHA-256.");
		}
		helper.succeed();
	}
}

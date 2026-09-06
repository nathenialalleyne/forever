package dev.forever.compat.matcha.diagnostic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;

/** Dedicated-server fixture for the diagnostic-only candidate artifact. */
public final class MatchaDiagnosticArtifactGameTest {
	@GameTest
	public void candidateCallbacksObserveTheLiveServerWithoutClientClasses(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();
		List<Consumer<Object>> startupCallbacks = new ArrayList<>();
		List<MatchaDiagnosticEntrypoint.ReloadCallback> reloadCallbacks = new ArrayList<>();
		List<MatchaDiagnosticReport> reports = new ArrayList<>();
		MatchaDiagnosticLifecycle lifecycle = new MatchaDiagnosticLifecycle(
				candidate -> MatchaDiagnosticObserver.observe((MinecraftServer) candidate),
				(candidate, report) -> reports.add(report));
		MatchaDiagnosticEntrypoint entrypoint = new MatchaDiagnosticEntrypoint(
				new MatchaDiagnosticRegistrationGate(),
				startupCallbacks::add,
				reloadCallbacks::add,
				lifecycle);

		entrypoint.onInitialize();
		entrypoint.onInitialize();
		String expectedStatus = System.getProperty("mrh013.expected-status", MatchaDiagnosticStatus.MISSING.name());
		helper.assertTrue(startupCallbacks.size() == 1, "The candidate registered duplicate startup callbacks.");
		helper.assertTrue(reloadCallbacks.size() == 1, "The candidate registered duplicate reload callbacks.");
		MatchaDiagnosticEntrypoint.ReloadCallback reload = reloadCallbacks.getFirst();
		reload.onReload(server, new Object(), false);
		reload.onReload(server, new Object(), true);
		helper.assertTrue(reports.isEmpty(), "A reload before startup produced a report.");
		startupCallbacks.getFirst().accept(server);
		startupCallbacks.getFirst().accept(server);
		reload.onReload(server, new Object(), false);
		reload.onReload(server, new Object(), true);

		helper.assertTrue(reports.size() == 2, "Expected one startup and one successful-reload report, got " + reports.size());
		for (MatchaDiagnosticReport report : reports) {
			helper.assertTrue(report.status().name().equals(expectedStatus),
					"The isolated fixture expected " + expectedStatus + ", got " + report.status());
		}
		if (reports.getFirst().status() == MatchaDiagnosticStatus.MISSING) {
			helper.assertTrue(String.join(" ", reports.getFirst().remedies()).contains(
					"6209783021c358044abedabacee471faff5bd4080437d4e3b5e51963f1804248"),
					"The live missing report did not include the locked SHA-256.");
		}
		helper.succeed();
	}
}

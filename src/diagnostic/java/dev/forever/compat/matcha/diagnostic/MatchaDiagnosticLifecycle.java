package dev.forever.compat.matcha.diagnostic;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns only lifecycle delivery and logging for the diagnostic report. */
final class MatchaDiagnosticLifecycle {
	private static final Logger LOGGER = LoggerFactory.getLogger("forever-matcha-diagnostic");
	private static final Set<MinecraftServer> STARTED_SERVERS =
			Collections.newSetFromMap(new WeakHashMap<>());

	private MatchaDiagnosticLifecycle() {
	}

	static void onServerStarted(MinecraftServer server) {
		if (server == null) {
			return;
		}
		boolean firstStart;
		synchronized (STARTED_SERVERS) {
			firstStart = STARTED_SERVERS.add(server);
		}
		if (firstStart) {
			publish(server);
		}
	}

	static void onDataPackReload(MinecraftServer server, boolean success) {
		if (server == null) {
			return;
		}
		boolean started;
		synchronized (STARTED_SERVERS) {
			started = STARTED_SERVERS.contains(server);
		}
		if (!shouldObserveReload(success, started)) {
			// Failed reloads are silent. The initial datapack load can also happen before
			// SERVER_STARTED, so the startup callback remains the single startup report.
			return;
		}
		publish(server);
	}

	static boolean shouldObserveReload(boolean success, boolean started) {
		return success && started;
	}

	private static void publish(MinecraftServer server) {
		MatchaDiagnosticReport report;
		try {
			report = MatchaDiagnosticObserver.observe(server);
		} catch (RuntimeException exception) {
			// The diagnostic must never take down a server because a third-party archive
			// or server view is unusual. Keep the failure visible and startup unblocked.
			report = MatchaDiagnosticReport.warning(
					MatchaDiagnosticStatus.MALFORMED,
					"Matcha baseline observation failed safely ("
							+ exception.getClass().getSimpleName()
							+ "). The server remains running without a verified baseline.",
					java.util.List.of(
							"Inspect the bounded diagnostic error context and verify the pinned Matcha archive.",
							"Do not repair or replace the archive automatically."));
		}

		switch (report.severity()) {
			case HEALTHY -> LOGGER.info("Matcha baseline diagnostic: {}", report.summary());
			case WARN -> {
				LOGGER.warn("Matcha baseline diagnostic [{}]: {}", report.status(), report.summary());
				for (String remedy : report.remedies()) {
					LOGGER.warn("  -> {}", remedy);
				}
			}
			case ERROR -> {
				String banner = "=".repeat(78);
				LOGGER.error("{}", banner);
				LOGGER.error("MANY ROADS HOME BASELINE PROBLEM: {}", report.status());
				LOGGER.error("{}", report.summary());
				for (String remedy : report.remedies()) {
					LOGGER.error("  -> {}", remedy);
				}
				LOGGER.error("{}", banner);
			}
			default -> LOGGER.error("Unhandled diagnostic severity {}; startup remains unblocked.", report.severity());
		}
	}
}

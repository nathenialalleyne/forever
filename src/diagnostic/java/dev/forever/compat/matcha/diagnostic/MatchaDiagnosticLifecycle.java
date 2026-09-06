package dev.forever.compat.matcha.diagnostic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns only lifecycle delivery and logging for the diagnostic report. */
final class MatchaDiagnosticLifecycle {
	private static final Logger LOGGER = LoggerFactory.getLogger("forever-matcha-diagnostic");
	private final Set<Object> startedServers = Collections.newSetFromMap(new WeakHashMap<>());
	private final Function<Object, MatchaDiagnosticReport> observer;
	private final BiConsumer<Object, MatchaDiagnosticReport> publisher;

	MatchaDiagnosticLifecycle(
			Function<Object, MatchaDiagnosticReport> observer,
			BiConsumer<Object, MatchaDiagnosticReport> publisher) {
		this.observer = Objects.requireNonNull(observer, "observer must not be null.");
		this.publisher = Objects.requireNonNull(publisher, "publisher must not be null.");
	}

	static MatchaDiagnosticLifecycle production() {
		return new MatchaDiagnosticLifecycle(
				server -> MatchaDiagnosticObserver.observe((MinecraftServer) server),
				(ignored, report) -> publishLog(report));
	}

	void onServerStarted(Object server) {
		if (server == null) {
			return;
		}
		boolean firstStart;
		synchronized (startedServers) {
			firstStart = startedServers.add(server);
		}
		if (firstStart) {
			publish(server);
		}
	}

	void onDataPackReload(Object server, boolean success) {
		if (server == null) {
			return;
		}
		boolean started;
		synchronized (startedServers) {
			started = startedServers.contains(server);
		}
		if (shouldObserveReload(success, started)) {
			publish(server);
		}
	}

	static boolean shouldObserveReload(boolean success, boolean started) {
		return success && started;
	}

	private void publish(Object server) {
		MatchaDiagnosticReport report;
		try {
			report = Objects.requireNonNull(observer.apply(server), "observer returned no report.");
		} catch (RuntimeException exception) {
			// The diagnostic must never take down a server because a third-party archive
			// or server view is unusual. Keep the failure visible and startup unblocked.
			report = MatchaDiagnosticReport.warning(
					MatchaDiagnosticStatus.MALFORMED,
					"Matcha baseline observation failed safely ("
							+ exception.getClass().getSimpleName()
							+ "). The server remains running without a verified baseline.",
					List.of(
							"Inspect the bounded diagnostic error context and verify the pinned Matcha archive.",
							"Do not repair or replace the archive automatically."));
		}
		publisher.accept(server, report);
	}

	private static void publishLog(MatchaDiagnosticReport report) {
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

package dev.forever.compat.matcha.diagnostic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatchaDiagnosticLifecycleTest {
	@Test
	@DisplayName("repeated initialization installs one startup and one reload callback set")
	void repeatedInitializationIsIdempotent() {
		MatchaDiagnosticRegistrationGate gate = new MatchaDiagnosticRegistrationGate();
		AtomicInteger startupRegistrations = new AtomicInteger();
		AtomicInteger reloadRegistrations = new AtomicInteger();

		assertTrue(gate.register(startupRegistrations::incrementAndGet, reloadRegistrations::incrementAndGet));
		assertFalse(gate.register(startupRegistrations::incrementAndGet, reloadRegistrations::incrementAndGet));
		assertFalse(gate.register(startupRegistrations::incrementAndGet, reloadRegistrations::incrementAndGet));
		assertEquals(1, startupRegistrations.get());
		assertEquals(1, reloadRegistrations.get());
	}

	@Test
	@DisplayName("the registered callback paths observe each started server exactly once per event")
	void registeredCallbacksDeliverFreshReports() {
		List<Consumer<Object>> startupCallbacks = new ArrayList<>();
		List<MatchaDiagnosticEntrypoint.ReloadCallback> reloadCallbacks = new ArrayList<>();
		List<Object> observedServers = new ArrayList<>();
		List<PublishedReport> reports = new ArrayList<>();
		MatchaDiagnosticLifecycle lifecycle = new MatchaDiagnosticLifecycle(
				server -> {
					observedServers.add(server);
					return MatchaDiagnosticReport.healthy("fresh report for " + server);
				},
				(server, report) -> reports.add(new PublishedReport(server, report)));
		MatchaDiagnosticEntrypoint entrypoint = new MatchaDiagnosticEntrypoint(
				new MatchaDiagnosticRegistrationGate(),
				startupCallbacks::add,
				reloadCallbacks::add,
				lifecycle);

		entrypoint.onInitialize();
		entrypoint.onInitialize();
		assertEquals(1, startupCallbacks.size());
		assertEquals(1, reloadCallbacks.size());

		Object firstServer = new Object();
		Object secondServer = new Object();
		MatchaDiagnosticEntrypoint.ReloadCallback reload = reloadCallbacks.getFirst();
		reload.onReload(firstServer, new Object(), false);
		reload.onReload(firstServer, new Object(), true);
		startupCallbacks.getFirst().accept(firstServer);
		startupCallbacks.getFirst().accept(firstServer);
		reload.onReload(firstServer, new Object(), false);
		reload.onReload(firstServer, new Object(), true);
		startupCallbacks.getFirst().accept(secondServer);
		startupCallbacks.getFirst().accept(secondServer);
		reload.onReload(secondServer, new Object(), true);

		assertEquals(List.of(firstServer, firstServer, secondServer, secondServer), observedServers);
		assertEquals(4, reports.size());
		assertEquals(firstServer, reports.get(0).server());
		assertEquals(firstServer, reports.get(1).server());
		assertEquals(secondServer, reports.get(2).server());
		assertEquals(secondServer, reports.get(3).server());
		assertTrue(reports.stream().allMatch(report -> report.report().severity() == MatchaDiagnosticReport.Severity.HEALTHY));
	}

	@Test
	@DisplayName("failed reloads and pre-start reloads do not produce a report")
	void failedOrPreStartReloadsAreSilent() {
		assertFalse(MatchaDiagnosticLifecycle.shouldObserveReload(false, true));
		assertFalse(MatchaDiagnosticLifecycle.shouldObserveReload(false, false));
		assertFalse(MatchaDiagnosticLifecycle.shouldObserveReload(true, false));
		assertTrue(MatchaDiagnosticLifecycle.shouldObserveReload(true, true));
	}

	private record PublishedReport(Object server, MatchaDiagnosticReport report) {
	}
}

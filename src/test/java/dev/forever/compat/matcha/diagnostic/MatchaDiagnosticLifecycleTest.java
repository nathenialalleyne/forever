package dev.forever.compat.matcha.diagnostic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;
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
	@DisplayName("failed reloads and pre-start reloads do not produce a report")
	void failedOrPreStartReloadsAreSilent() {
		assertFalse(MatchaDiagnosticLifecycle.shouldObserveReload(false, true));
		assertFalse(MatchaDiagnosticLifecycle.shouldObserveReload(false, false));
		assertFalse(MatchaDiagnosticLifecycle.shouldObserveReload(true, false));
		assertTrue(MatchaDiagnosticLifecycle.shouldObserveReload(true, true));
	}
}

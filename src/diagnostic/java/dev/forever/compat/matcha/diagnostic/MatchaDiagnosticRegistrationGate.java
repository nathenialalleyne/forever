package dev.forever.compat.matcha.diagnostic;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/** Guards the two Fabric lifecycle registrations against repeated mod initialization. */
final class MatchaDiagnosticRegistrationGate {
	private final AtomicBoolean registered = new AtomicBoolean();

	boolean register(Runnable startupRegistration, Runnable reloadRegistration) {
		Objects.requireNonNull(startupRegistration, "startup registration must not be null.");
		Objects.requireNonNull(reloadRegistration, "reload registration must not be null.");
		if (!registered.compareAndSet(false, true)) {
			return false;
		}
		startupRegistration.run();
		reloadRegistration.run();
		return true;
	}
}

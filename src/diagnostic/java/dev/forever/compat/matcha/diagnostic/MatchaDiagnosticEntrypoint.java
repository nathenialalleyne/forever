package dev.forever.compat.matcha.diagnostic;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Common/server-only entrypoint for the isolated Matcha baseline diagnostic. */
public final class MatchaDiagnosticEntrypoint implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("forever-matcha-diagnostic");
	private static final MatchaDiagnosticRegistrationGate REGISTRATION = new MatchaDiagnosticRegistrationGate();

	@Override
	public void onInitialize() {
		boolean registered = REGISTRATION.register(
				() -> ServerLifecycleEvents.SERVER_STARTED.register(MatchaDiagnosticLifecycle::onServerStarted),
				() -> ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
						(server, resources, success) -> MatchaDiagnosticLifecycle.onDataPackReload(server, success)));
		if (registered) {
			LOGGER.info("Matcha baseline diagnostic registered; startup remains unblocked and observation is read-only.");
		}
	}
}

package dev.forever.compat.matcha.diagnostic;

import java.util.function.Consumer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Common/server-only entrypoint for the isolated Matcha baseline diagnostic. */
public final class MatchaDiagnosticEntrypoint implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("forever-matcha-diagnostic");
	private static final MatchaDiagnosticRegistrationGate REGISTRATION = new MatchaDiagnosticRegistrationGate();
	private static final MatchaDiagnosticLifecycle PRODUCTION_LIFECYCLE = MatchaDiagnosticLifecycle.production();
	private final MatchaDiagnosticRegistrationGate registration;
	private final StartupRegistrar startupRegistrar;
	private final ReloadRegistrar reloadRegistrar;
	private final MatchaDiagnosticLifecycle lifecycle;

	public MatchaDiagnosticEntrypoint() {
		this(
				REGISTRATION,
				callback -> ServerLifecycleEvents.SERVER_STARTED.register(server -> callback.accept(server)),
				callback -> ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
						(server, resources, success) -> callback.onReload(server, resources, success)),
				PRODUCTION_LIFECYCLE);
	}

	MatchaDiagnosticEntrypoint(
			MatchaDiagnosticRegistrationGate registration,
			StartupRegistrar startupRegistrar,
			ReloadRegistrar reloadRegistrar,
			MatchaDiagnosticLifecycle lifecycle) {
		this.registration = registration;
		this.startupRegistrar = startupRegistrar;
		this.reloadRegistrar = reloadRegistrar;
		this.lifecycle = lifecycle;
	}

	@Override
	public void onInitialize() {
		boolean registered = registration.register(
				() -> startupRegistrar.register(lifecycle::onServerStarted),
				() -> reloadRegistrar.register((server, resources, success) -> lifecycle.onDataPackReload(server, success)));
		if (registered) {
			LOGGER.info("Matcha baseline diagnostic registered; startup remains unblocked and observation is read-only.");
		}
	}

	@FunctionalInterface
	interface StartupRegistrar {
		void register(Consumer<Object> callback);
	}

	@FunctionalInterface
	interface ReloadRegistrar {
		void register(ReloadCallback callback);
	}

	@FunctionalInterface
	interface ReloadCallback {
		void onReload(Object server, Object resources, boolean success);
	}
}

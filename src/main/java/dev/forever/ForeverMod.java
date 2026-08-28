package dev.forever;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common (server and client) entrypoint for Forever.
 *
 * <p>This class is deliberately minimal. Forever is currently an architecture and
 * audit foundation: no gameplay systems are registered yet. See
 * {@code docs/architecture.md} for the intended module boundaries and
 * {@code docs/backlog.md} for the ordered work queue.
 *
 * <p>Rules that apply to this class:
 * <ul>
 *   <li>It must never reference client-only classes. It runs on dedicated servers.</li>
 *   <li>It must not become a god-object registry ({@code ForeverManager}). Each future
 *       system owns its own registration and is invoked from here explicitly.</li>
 * </ul>
 */
public final class ForeverMod implements ModInitializer {

	/** The mod ID. Used for identifiers, resource paths, and logging. */
	public static final String MOD_ID = "forever";

	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Forever initialising (foundation only; no gameplay systems registered).");
	}
}

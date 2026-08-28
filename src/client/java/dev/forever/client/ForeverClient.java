package dev.forever.client;

import dev.forever.ForeverMod;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entrypoint for Forever.
 *
 * <p>Client code is a synchronised projection of server-authoritative state. It must
 * never be the authority for gameplay outcomes. Nothing in the common source set may
 * reference this class or anything else in {@code dev.forever.client}.
 *
 * <p>No screens, HUDs, or key bindings exist yet. See {@code docs/architecture.md}.
 */
public final class ForeverClient implements ClientModInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ForeverMod.MOD_ID + "/client");

	@Override
	public void onInitializeClient() {
		LOGGER.info("Forever client initialising (foundation only; no screens or overlays registered).");
	}
}

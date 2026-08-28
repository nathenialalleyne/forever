package dev.forever.core.economy.adapter;

import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Common/server lifecycle owner for the hybrid Obol economy. */
public final class ForeverEconomy {

	private static final Logger LOGGER = LoggerFactory.getLogger("forever/economy");
	private static final Identifier RELOAD_LISTENER_ID =
			Identifier.fromNamespaceAndPath("forever", "economy_balance");
	private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

	private ForeverEconomy() {
	}

	/**
	 * Registers the persistent purse, physical Obol item, and server-data balance listener.
	 * ForeverMod must wire this method from its common initializer.
	 */
	public static void initialize() {
		if (!INITIALIZED.compareAndSet(false, true)) {
			return;
		}
		CoinPurseAttachment.initialize();
		EconomyBalanceLoader.installBundled();
		ObolItem.initialize();
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				RELOAD_LISTENER_ID, new EconomyResourceReloadListener());
		LOGGER.info("Forever economy registrations initialised");
	}
}

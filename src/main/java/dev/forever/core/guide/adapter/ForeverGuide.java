package dev.forever.core.guide.adapter;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Common/server registration entrypoint for the Field Guide system. */
public final class ForeverGuide {

	private static final Logger LOGGER = LoggerFactory.getLogger("forever/guide");
	private static final Identifier RELOAD_LISTENER_ID =
			Identifier.fromNamespaceAndPath("forever", "guide_registry");
	private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

	private ForeverGuide() {
	}

	/**
	 * Registers the server-data reload listener. The common mod entrypoint must call this
	 * method during mod initialisation. The first server data reload then installs the
	 * immutable registry snapshot.
	 */
	public static void initialize() {
		if (!INITIALIZED.compareAndSet(false, true)) {
			return;
		}
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				RELOAD_LISTENER_ID, new GuideResourceReloadListener());
		LOGGER.info("Field Guide resource reload listener registered; server data will install the guide registry.");
	}

	/** Loads and publishes the current server resource view for an explicit server caller. */
	public static GuideRegistry load(net.minecraft.server.packs.resources.ResourceManager resourceManager) {
		GuideRegistry registry = GuideRegistryLoader.load(resourceManager);
		GuideRegistryAccess.install(registry);
		return registry;
	}

	public static Optional<GuideRegistry> current() {
		return GuideRegistryAccess.current();
	}
}

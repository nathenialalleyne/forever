package dev.forever.core.mastery.adapter;

import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

/** Common/server registration entrypoint owned by the mastery package. */
public final class ForeverMastery {

	private static final Identifier RELOAD_LISTENER_ID =
			Identifier.fromNamespaceAndPath("forever", "mastery_registry");
	private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

	private ForeverMastery() {
	}

	/**
	 * Registers the persistent attachment and server-data reload listener.
	 *
	 * <p>ForeverMod must call this method from its common {@code onInitialize} method. It is
	 * intentionally not called here through a global static side effect, so registration order
	 * remains explicit and dedicated-server safe.
	 */
	public static void initialize() {
		if (!INITIALIZED.compareAndSet(false, true)) {
			return;
		}
		MasteryAttachment.initialize();
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				RELOAD_LISTENER_ID, new MasteryResourceReloadListener());
	}
}

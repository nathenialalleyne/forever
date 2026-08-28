package dev.forever;

import dev.forever.compat.matcha.ForeverMatchaCompat;
import dev.forever.core.equipment.ForeverEquipment;
import dev.forever.core.mastery.ForeverMastery;
import dev.forever.core.settlement.ForeverSettlement;
import dev.forever.core.storage.ForeverStorage;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common (server and client) entrypoint for Forever.
 *
 * <p>This class does one job: it invokes each system's own initialiser in a defined
 * order. It deliberately holds no state and contains no gameplay logic, so that it
 * never becomes the {@code ForeverManager} god-object that
 * {@code docs/architecture.md} forbids. Each system owns its own registration.
 *
 * <p>Rules that apply here:
 * <ul>
 *   <li>Never reference client-only classes. This runs on dedicated servers.</li>
 *   <li>Add one line per system, and nothing else.</li>
 * </ul>
 *
 * @see dev.forever.core.mastery.ForeverMastery
 * @see dev.forever.core.settlement.ForeverSettlement
 * @see dev.forever.core.storage.ForeverStorage
 * @see dev.forever.compat.matcha.ForeverMatchaCompat
 */
public final class ForeverMod implements ModInitializer {

	/** The mod ID. Used for identifiers, resource paths, and logging. */
	public static final String MOD_ID = "forever";

	private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Registers the equipment state component, the field tool, the balance reload
		// listener, and the tooltip provider. Touching a constant here instead would
		// register the component but silently leave the rest absent in production.
		ForeverEquipment.initialize();

		ForeverMastery.initialize();
		ForeverSettlement.initialize();
		ForeverStorage.initialize();

		// Matcha is a datapack, not a mod, so detection cannot complete at mod-init
		// time. This registers the adapter and its no-op fallback; the adapter resolves
		// its real support level once a server and its datapacks are available.
		ForeverMatchaCompat.initialize();

		LOGGER.info("Forever initialised: equipment, mastery, settlement, storage, and Matcha compatibility registered.");
	}
}

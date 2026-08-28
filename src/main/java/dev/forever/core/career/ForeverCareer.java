package dev.forever.core.career;

import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Common/server lifecycle owner for the Mason career vertical slice. */
public final class ForeverCareer {

	private static final Logger LOGGER = LoggerFactory.getLogger("forever/career");
	private static final Identifier RELOAD_LISTENER_ID =
			Identifier.fromNamespaceAndPath("forever", "mason_career_data");
	private static final AtomicBoolean INITIALIZED = new AtomicBoolean();

	private ForeverCareer() {
	}

	/** Registers the entity attachment, data reload listener, and physical death hook. */
	public static void initialize() {
		if (!INITIALIZED.compareAndSet(false, true)) {
			return;
		}
		MasonCareerAttachment.initialize();
		MasonDataLoader.installBundled();
		ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(
				RELOAD_LISTENER_ID, new MasonResourceReloadListener());
		ServerLivingEntityEvents.AFTER_DEATH.register(ForeverCareer::afterDeath);
		LOGGER.info("Forever career registrations initialised");
	}

	private static void afterDeath(LivingEntity entity, net.minecraft.world.damagesource.DamageSource source) {
		if (!(entity instanceof Villager) || !((net.fabricmc.fabric.api.attachment.v1.AttachmentTarget) entity)
				.hasAttached(CareerAttachment.TYPE)) {
			return;
		}
		PhysicalDeathCause.from(source).ifPresent(cause -> {
			CareerDeathResult result = CareerMortalityService.applyPhysicalDeath(
					MasonCareerService.stateOf(entity), cause);
			if (result.applied()) {
				MasonCareerService.setState(entity, result.state());
			}
		});
	}
}

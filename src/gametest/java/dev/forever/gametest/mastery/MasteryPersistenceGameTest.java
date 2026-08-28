package dev.forever.gametest.mastery;

import static java.util.Map.entry;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.forever.core.mastery.LoadoutChange;
import dev.forever.core.mastery.LoadoutSwitchResult;
import dev.forever.core.mastery.MasteryAttachment;
import dev.forever.core.mastery.MasteryIds;
import dev.forever.core.mastery.MasteryLoadout;
import dev.forever.core.mastery.MasteryLoadoutService;
import dev.forever.core.mastery.MasteryProgress;
import dev.forever.core.mastery.MasteryRegistry;
import dev.forever.core.mastery.MasteryRegistryAccess;
import dev.forever.core.mastery.MasteryRegistryLoader;
import dev.forever.core.mastery.MasteryState;
import dev.forever.core.mastery.SwitchContext;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;

/** Dedicated-server lifecycle checks for FVR-305. */
public final class MasteryPersistenceGameTest {

	@GameTest
	public void masteryAttachmentIsPersistentAcrossSaveBoundary(GameTestHelper helper) {
		MasteryRegistry registry = loadRegistry(helper);
		MasteryState original = populatedState(registry);
		JsonElement saved = MasteryAttachment.TYPE.persistenceCodec()
				.encodeStart(JsonOps.INSTANCE, original)
				.getOrThrow();
		MasteryState reloaded = MasteryAttachment.TYPE.persistenceCodec()
				.parse(JsonOps.INSTANCE, saved)
				.getOrThrow();

		if (!original.equals(reloaded)) {
			throw helper.assertionException("Mastery attachment changed across a save/reload boundary.");
		}
		helper.succeed();
	}

	@GameTest
	public void masteryAttachmentSurvivesPlayerDeathCopy(GameTestHelper helper) {
		MasteryRegistry registry = loadRegistry(helper);
		MasteryState original = populatedState(registry);
		ServerPlayer oldPlayer = helper.makeMockServerPlayerInLevel();
		ServerPlayer replacementPlayer = helper.makeMockServerPlayerInLevel();
		oldPlayer.setAttached(MasteryAttachment.TYPE, original);

		if (!MasteryAttachment.TYPE.copyOnDeath()) {
			throw helper.assertionException("Mastery attachment must opt into copyOnDeath.");
		}
		ServerPlayerEvents.AFTER_RESPAWN.invoker().afterRespawn(oldPlayer, replacementPlayer, false);

		MasteryState copied = replacementPlayer.getAttached(MasteryAttachment.TYPE);
		if (!original.equals(copied)) {
			throw helper.assertionException("Mastery progress was not copied to the replacement player.");
		}
		helper.succeed();
	}

	@GameTest
	public void gatedLoadoutSwitchPreservesEveryMasteryProgress(GameTestHelper helper) {
		MasteryRegistry registry = loadRegistry(helper);
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		MasteryState original = populatedState(registry);
		player.setAttached(MasteryAttachment.TYPE, original);
		MasteryLoadout desired = new MasteryLoadout(
				Optional.of(MasteryIds.PROSPECTOR),
				List.of(MasteryIds.BUILDER, MasteryIds.SMITH));

		LoadoutSwitchResult rejected = MasteryLoadoutService.switchLoadout(
				player, desired, SwitchContext.NONE, 1L);
		if (rejected.applied() || !original.equals(rejected.state())) {
			throw helper.assertionException("A switch without rest, home, or workplace context changed state.");
		}

		LoadoutSwitchResult applied = MasteryLoadoutService.switchLoadout(
				player, desired, SwitchContext.REST, 2L);
		if (!applied.applied() || !original.progress().equals(applied.state().progress())) {
			throw helper.assertionException("A gated loadout switch changed permanent mastery progress.");
		}
		if (!desired.equals(applied.state().loadout())) {
			throw helper.assertionException("The server did not apply the requested valid loadout.");
		}
		helper.succeed();
	}

	private static MasteryRegistry loadRegistry(GameTestHelper helper) {
		MasteryRegistry registry = MasteryRegistryLoader.load(
				helper.getLevel().getServer().getResourceManager());
		MasteryRegistryAccess.install(registry);
		return registry;
	}

	private static MasteryState populatedState(MasteryRegistry registry) {
		MasteryProgress builder = new MasteryProgress(
				2,
				java.util.Set.of("material_breadth"),
				java.util.Set.of("variety:material_family:stone"),
				java.util.Set.of("build-stone"));
		MasteryProgress prospector = new MasteryProgress(
				4,
				java.util.Set.of("ore_variety", "depth_survey"),
				java.util.Set.of("variety:ore_type:iron", "variety:ore_type:gold"),
				java.util.Set.of("ore-iron", "ore-gold"));
		return new MasteryState(
				MasteryState.CURRENT_SCHEMA,
				Map.ofEntries(entry(MasteryIds.BUILDER, builder), entry(MasteryIds.PROSPECTOR, prospector)),
				new MasteryLoadout(Optional.of(MasteryIds.BUILDER), List.of(MasteryIds.COOK)),
				Optional.of(new LoadoutChange(SwitchContext.HOME, 0L)));
	}
}

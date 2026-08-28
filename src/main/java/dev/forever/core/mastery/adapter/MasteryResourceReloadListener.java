package dev.forever.core.mastery;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** Publishes validated mastery data after each server data-pack reload. */
public final class MasteryResourceReloadListener extends SimplePreparableReloadListener<MasteryRegistry> {

	@Override
	protected MasteryRegistry prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		return MasteryRegistryLoader.load(resourceManager);
	}

	@Override
	protected void apply(MasteryRegistry registry, ResourceManager resourceManager, ProfilerFiller profiler) {
		MasteryRegistryAccess.install(registry);
	}

	@Override
	public String getName() {
		return "Forever mastery definitions";
	}
}

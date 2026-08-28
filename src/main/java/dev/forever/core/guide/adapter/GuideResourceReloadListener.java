package dev.forever.core.guide;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** Publishes a fully validated Field Guide registry after each server data reload. */
public final class GuideResourceReloadListener extends SimplePreparableReloadListener<GuideRegistry> {

	@Override
	protected GuideRegistry prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		return GuideRegistryLoader.load(resourceManager);
	}

	@Override
	protected void apply(GuideRegistry registry, ResourceManager resourceManager, ProfilerFiller profiler) {
		GuideRegistryAccess.install(registry);
	}

	@Override
	public String getName() {
		return "Forever Field Guide definitions";
	}
}

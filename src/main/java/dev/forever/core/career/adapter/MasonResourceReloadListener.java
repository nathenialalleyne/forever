package dev.forever.core.career;

import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

/** Installs one complete Mason definition snapshot after a server data reload. */
public final class MasonResourceReloadListener extends SimplePreparableReloadListener<MasonData> {

	@Override
	protected MasonData prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		return MasonDataLoader.load(resourceManager);
	}

	@Override
	protected void apply(MasonData data, ResourceManager resourceManager, ProfilerFiller profiler) {
		MasonDataRegistry.install(data);
	}

	@Override
	public String getName() {
		return "Forever Mason career and catalogue definitions";
	}
}

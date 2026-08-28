package dev.forever.core.economy.adapter;

import dev.forever.core.economy.application.EconomyBalanceAccess;
import dev.forever.core.economy.domain.EconomyBalance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/** Installs a complete economy balance snapshot after a server resource reload. */
public final class EconomyResourceReloadListener extends SimplePreparableReloadListener<EconomyBalance> {

	@Override
	protected EconomyBalance prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
		return EconomyBalanceLoader.load(resourceManager);
	}

	@Override
	protected void apply(EconomyBalance balance, ResourceManager resourceManager, ProfilerFiller profiler) {
		EconomyBalanceAccess.install(balance);
	}

	@Override
	public String getName() {
		return "Forever economy balance";
	}
}

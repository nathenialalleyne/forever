package dev.forever.core.mastery;

import com.mojang.serialization.DataResult;
import java.util.Objects;
import net.minecraft.server.level.ServerPlayer;

/**
 * Server-authoritative loadout operations. The context argument represents an interaction
 * already observed by server code, not a client instruction.
 */
public final class MasteryLoadoutService {

	private MasteryLoadoutService() {
	}

	/** Pure operation used by unit tests and server adapters. */
	public static LoadoutSwitchResult switchLoadout(
			MasteryState current,
			MasteryRegistry registry,
			MasteryLoadout desired,
			SwitchContext context,
			long serverTime) {
		Objects.requireNonNull(current, "current");
		Objects.requireNonNull(registry, "registry");
		Objects.requireNonNull(desired, "desired");
		if (context == null || !context.satisfiesSwitchGate()) {
			return LoadoutSwitchResult.rejected(
					current,
					"mastery.forever.loadout.switch_requires_context",
					"A loadout change requires a server-observed rest, home, or workplace interaction.");
		}
		if (serverTime < 0) {
			return LoadoutSwitchResult.rejected(
					current,
					"mastery.forever.loadout.invalid_timestamp",
					"The server supplied a negative loadout-change timestamp.");
		}
		DataResult<MasteryLoadout> validation = registry.validateLoadout(desired);
		if (validation.error().isPresent()) {
			String detail = validation.error().orElseThrow().message();
			return LoadoutSwitchResult.rejected(
					current, "mastery.forever.loadout.invalid", detail);
		}
		if (current.loadout().equals(desired)) {
			return LoadoutSwitchResult.rejected(
					current,
					"mastery.forever.loadout.already_active",
					"The requested Focus and Supporting masteries are already active.");
		}

		MasteryState next = current
				.withLoadout(desired)
				.withLastLoadoutChange(new LoadoutChange(context, serverTime));
		return new LoadoutSwitchResult(true, next, "", "");
	}

	/**
	 * Applies a request atomically on the server thread. No XP, rank, or evidence is changed.
	 */
	public static LoadoutSwitchResult switchLoadout(
			ServerPlayer player,
			MasteryLoadout desired,
			SwitchContext context,
			long serverTime) {
		Objects.requireNonNull(player, "player");
		MasteryRegistry registry = MasteryRegistryAccess.requireCurrent();
		synchronized (player) {
			MasteryState current = player.getAttachedOrCreate(MasteryAttachment.TYPE, MasteryState::empty);
			MasteryState prepared = prepareState(current, registry);
			LoadoutSwitchResult result = switchLoadout(prepared, registry, desired, context, serverTime);
			if (!result.state().equals(current)) {
				player.setAttached(MasteryAttachment.TYPE, result.state());
			}
			return result;
		}
	}

	/** Ensures a newly seen player has a data-selected Focus without inventing progress. */
	public static MasteryState prepareState(MasteryState state, MasteryRegistry registry) {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(registry, "registry");
		MasteryLoadout stored = state.loadout();
		if (stored.focus().isEmpty()) {
			return state.withLoadout(registry.initialLoadout());
		}
		MasteryLoadout sanitised = registry.deactivateUnknown(stored);
		return sanitised.equals(stored) ? state : state.withLoadout(sanitised);
	}
}

package dev.forever.core.career.adapter;

import dev.forever.core.career.domain.CareerStatus;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/** Enforces causal loaded death and the no-random-death unloaded boundary. */
public final class CareerMortalityService {

	private CareerMortalityService() {
	}

	public static CareerDeathResult applyPhysicalDeath(CareerState state, PhysicalDeathCause cause) {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(cause, "cause");
		if (!state.isMason()) {
			return new CareerDeathResult(false, state, cause, Set.of(), Set.of(),
					"An unassigned entity has no Mason knowledge to transition.");
		}
		if (state.status() == CareerStatus.DEAD) {
			return new CareerDeathResult(false, state, cause, state.knownTechniques(), Set.of(),
					"The career was already marked dead; the request was not replayed.");
		}
		Set<net.minecraft.resources.Identifier> surviving = state.taughtTechniques();
		Set<net.minecraft.resources.Identifier> lost = new HashSet<>(state.knownTechniques());
		lost.removeAll(surviving);
		CareerState dead = state.withPhysicalDeath(cause);
		return new CareerDeathResult(true, dead, cause, surviving, lost,
				"The Mason died from the attributable physical cause " + cause.name().toLowerCase() + ".");
	}

	/** Unloaded abstraction may advance safe records, but it never invents a death. */
	public static CareerSimulationResult simulateUnloaded(CareerState state) {
		Objects.requireNonNull(state, "state");
		return new CareerSimulationResult(state, false,
				"The unloaded career update preserved the villager; no abstract death was created.");
	}
}

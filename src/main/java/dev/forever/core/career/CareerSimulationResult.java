package dev.forever.core.career;

import java.util.Objects;

/** Explicit result for a bounded unloaded career update. */
public record CareerSimulationResult(CareerState state, boolean deathCreated, String message) {

	public CareerSimulationResult {
		Objects.requireNonNull(state, "state");
		if (deathCreated) {
			throw new IllegalArgumentException("An unloaded career update cannot create a death.");
		}
		message = CareerCodecs.requiredText(message, "career simulation result", 512);
	}
}

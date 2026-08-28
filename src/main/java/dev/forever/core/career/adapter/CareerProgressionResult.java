package dev.forever.core.career.adapter;

import dev.forever.core.career.domain.CareerCodecs;
import java.util.Objects;

/** Immutable result for one server-authoritative career mutation. */
public record CareerProgressionResult(boolean applied, CareerState state, String message) {

	public CareerProgressionResult {
		Objects.requireNonNull(state, "state");
		message = CareerCodecs.requiredText(message, "progression result", 512);
	}
}

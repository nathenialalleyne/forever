package dev.forever.core.career.adapter;

import dev.forever.core.career.domain.CareerCodecs;
import java.util.Objects;
import java.util.Set;
import net.minecraft.resources.Identifier;

/** Result of applying an attributable physical death to a career record. */
public record CareerDeathResult(
		boolean applied,
		CareerState state,
		PhysicalDeathCause cause,
		Set<Identifier> survivingTechniques,
		Set<Identifier> lostUntaughtTechniques,
		String message) {

	public CareerDeathResult {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(cause, "cause");
		survivingTechniques = Set.copyOf(Objects.requireNonNull(survivingTechniques,
				"surviving techniques"));
		lostUntaughtTechniques = Set.copyOf(Objects.requireNonNull(lostUntaughtTechniques,
				"lost untaught techniques"));
		message = CareerCodecs.requiredText(message, "death result", 512);
	}
}

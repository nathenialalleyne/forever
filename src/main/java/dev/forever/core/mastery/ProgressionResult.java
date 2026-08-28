package dev.forever.core.mastery;

import java.util.Set;

/** Immutable result of a server-side evidence submission. */
public record ProgressionResult(
		boolean accepted,
		boolean duplicate,
		MasteryState state,
		int rank,
		Set<String> unlockedMilestones,
		String reasonKey,
		String explanationKey) {

	public ProgressionResult {
		unlockedMilestones = Set.copyOf(unlockedMilestones);
	}

}

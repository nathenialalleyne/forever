package dev.forever.core.settlement;

import java.util.Objects;

/** Result of one bounded abstract update for an unloaded settlement. */
public record MigrationSimulationResult(
		Settlement settlement,
		int processedRecords,
		int arrivals,
		int deaths,
		int budget,
		boolean chunkLoadRequested) {

	public MigrationSimulationResult {
		Objects.requireNonNull(settlement, "settlement");
		if (processedRecords < 0 || arrivals < 0 || deaths < 0 || budget < 1) {
			throw new IllegalArgumentException("Settlement abstract simulation counts are invalid.");
		}
		if (processedRecords > budget) {
			throw new IllegalArgumentException("Settlement abstract simulation exceeded its fixed per-tick budget.");
		}
		if (arrivals > processedRecords) {
			throw new IllegalArgumentException("Settlement arrivals cannot exceed processed records.");
		}
		if (deaths != 0) {
			throw new IllegalArgumentException("Unloaded abstract settlement simulation cannot create villager deaths.");
		}
		if (chunkLoadRequested) {
			throw new IllegalArgumentException("Unloaded abstract settlement simulation cannot request chunk loading.");
		}
	}
}

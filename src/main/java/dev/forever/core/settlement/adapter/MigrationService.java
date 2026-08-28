package dev.forever.core.settlement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Prototype migration service. Population enters a Charter through explicit offers and
 * arrivals, not ordinary breeding. Unloaded updates operate on records only.
 */
public final class MigrationService {

	/** Invariant required by Principle 11: abstract simulation never randomly kills a villager. */
	public static final String UNLOADED_SIMULATION_INVARIANT =
			"An unloaded settlement abstract update never creates a villager death.";

	private MigrationService() {
	}

	public static MigrationRecord offer(
			UUID migrationId,
			Optional<UUID> villagerId,
			String origin,
			String career,
			long offeredAtTick) {
		return new MigrationRecord(migrationId, villagerId, origin, career, MigrationStatus.OFFERED,
				offeredAtTick, -1L);
	}

	public static SettlementState addOffer(
			SettlementState state, UUID settlementId, MigrationRecord migration) {
		Objects.requireNonNull(state, "state");
		Settlement settlement = state.requireSettlement(settlementId);
		return state.replaceSettlement(settlement.withMigration(Objects.requireNonNull(migration, "migration")));
	}

	/**
	 * Performs one and only one bounded update. The configured budget is the maximum number
	 * of migration records inspected on this call, so no permanent chunk loading is needed.
	 */
	public static MigrationSimulationResult simulateUnloadedTick(
			Settlement settlement,
			long currentTick,
			SettlementBalance balance) {
		Objects.requireNonNull(settlement, "settlement");
		Objects.requireNonNull(balance, "balance");
		if (currentTick < 0L) {
			throw new IllegalArgumentException("Settlement simulation tick cannot be negative.");
		}

		int budget = balance.abstractVillagerBudgetPerTick();
		int recordLimit = Math.min(budget, settlement.migrations().size());
		int arrivals = 0;
		List<MigrationRecord> next = new ArrayList<>(settlement.migrations());
		boolean changed = false;
		for (int index = 0; index < recordLimit; index++) {
			MigrationRecord migration = next.get(index);
			if (migration.status() == MigrationStatus.OFFERED
					&& currentTick >= migration.offeredAtTick()
					&& currentTick - migration.offeredAtTick() >= balance.migrationIntervalTicks()
					&& arrivals < balance.migrationArrivalsPerInterval()) {
				next.set(index, migration.withStatus(MigrationStatus.ARRIVED, currentTick));
				arrivals++;
				changed = true;
			}
		}
		Settlement nextSettlement = changed ? settlement.withMigrations(next) : settlement;
		return new MigrationSimulationResult(nextSettlement, recordLimit, arrivals, 0, budget, false);
	}

	/** Applies the same bounded update to the server's world-scoped SavedData. */
	public static MigrationSimulationResult simulateUnloadedTick(
			SettlementSavedData savedData,
			UUID settlementId,
			long currentTick,
			SettlementBalance balance) {
		Objects.requireNonNull(savedData, "savedData");
		Settlement settlement = savedData.state().requireSettlement(settlementId);
		MigrationSimulationResult result = simulateUnloadedTick(settlement, currentTick, balance);
		if (result.settlement() != settlement) {
			savedData.replace(savedData.state().replaceSettlement(result.settlement()));
		}
		return result;
	}
}

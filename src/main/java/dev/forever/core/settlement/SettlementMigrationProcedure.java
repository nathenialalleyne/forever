package dev.forever.core.settlement;

import java.util.List;

/**
 * Operator-facing FVR-405 procedure for the first settlement save migration.
 *
 * <p>The migration itself is pure and runs inside {@link SettlementState#CODEC}. The
 * surrounding procedure is intentionally explicit: backups and verification happen before
 * a real world is opened, and a failed or future-schema decode leaves the input untouched.
 */
public final class SettlementMigrationProcedure {

	private static final List<String> STEPS = List.of(
			"Back up the disposable world and retain the original copy before opening it with the new build.",
			"Detect the settlement schema_version in the saved record before accepting any Charter data.",
			"Decode through SettlementState.CODEC; schema 1 migrates deterministically to schema 2.",
			"Verify the migrated graph, node IDs, cached edges, and migration records by round-trip encoding.",
			"If decoding or verification fails, keep the original copy and roll back by restoring the backup.",
			"Run the dedicated-server startup and GameTest checks on the disposable copy before release.");

	private SettlementMigrationProcedure() {
	}

	public static List<String> steps() {
		return STEPS;
	}
}

package dev.forever.core.settlement;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-facing mutation facade for explicit building registration and revalidation.
 * Callers must supply the player-selected node and the current server world view.
 */
public final class BuildingRegistrationService {

	private BuildingRegistrationService() {
	}

	public record StateRegistrationResult(
			boolean accepted,
			SettlementState state,
			Optional<SettlementEdge> edge,
			String message) {

		public StateRegistrationResult {
			Objects.requireNonNull(state, "state");
			Objects.requireNonNull(edge, "edge");
			message = SettlementCodecs.requiredText(message, "registration result", 512);
		}
	}

	public record ValidationStateResult(
			boolean applied,
			SettlementState state,
			ValidationResult validation,
			String message) {

		public ValidationStateResult {
			Objects.requireNonNull(state, "state");
			Objects.requireNonNull(validation, "validation");
			message = SettlementCodecs.requiredText(message, "validation result", 512);
		}
	}

	/** Registers a player-selected node and leaves its validation state pending. */
	public static StateRegistrationResult register(
			SettlementState state,
			UUID settlementId,
			RegisteredBuilding building,
			SettlementBalance balance) {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(settlementId, "settlementId");
		Objects.requireNonNull(building, "building");
		Objects.requireNonNull(balance, "balance");
		Settlement settlement = state.settlement(settlementId).orElse(null);
		if (settlement == null) {
			return rejected(state, "Settlement " + settlementId + " is not chartered.");
		}
		SettlementGraph.RegistrationResult result = SettlementGraph.attach(settlement, building, balance);
		if (!result.accepted()) {
			return rejected(state, result.message());
		}
		return new StateRegistrationResult(true, state.replaceSettlement(result.settlement()),
				result.edge(), result.message());
	}

	/** Removes only the registered graph node and incident cached edges, never world blocks. */
	public static StateRegistrationResult unregister(
			SettlementState state, UUID settlementId, UUID buildingId) {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(settlementId, "settlementId");
		Objects.requireNonNull(buildingId, "buildingId");
		Settlement settlement = state.settlement(settlementId).orElse(null);
		if (settlement == null) {
			return rejected(state, "Settlement " + settlementId + " is not chartered.");
		}
		try {
			Settlement next = settlement.withoutBuilding(buildingId);
			return new StateRegistrationResult(true, state.replaceSettlement(next), Optional.empty(),
					"Building " + buildingId + " was removed from the graph; its world blocks were untouched.");
		} catch (SettlementDataException exception) {
			return rejected(state, exception.getMessage());
		}
	}

	/** Runs the bounded residence validator and caches its server result on the node. */
	public static ValidationStateResult validateResidence(
			SettlementState state,
			UUID settlementId,
			UUID buildingId,
			SettlementWorldView world,
			SettlementBalance balance,
			int revision) {
		Objects.requireNonNull(state, "state");
		Settlement settlement = state.settlement(settlementId).orElse(null);
		if (settlement == null) {
			ValidationResult result = ValidationResult.invalid(revision, java.util.List.of(
					new ValidationIssue(FunctionalRequirement.DECLARATION,
							"The settlement Charter no longer exists on the server.")));
			return new ValidationStateResult(false, state, result, result.issues().getFirst().message());
		}
		RegisteredBuilding building = settlement.building(buildingId).orElse(null);
		if (building == null) {
			ValidationResult result = ValidationResult.invalid(revision, java.util.List.of(
					new ValidationIssue(FunctionalRequirement.DECLARATION,
							"The registered building no longer exists in this settlement graph.")));
			return new ValidationStateResult(false, state, result, result.issues().getFirst().message());
		}
		ValidationResult validation = ResidenceValidator.validate(building, world, balance, revision);
		Settlement next = settlement.withBuildingValidation(buildingId, validation);
		return new ValidationStateResult(true, state.replaceSettlement(next), validation,
				validation.valid() ? "The residence passed every functional check."
						: validation.issues().getFirst().message());
	}

	private static StateRegistrationResult rejected(SettlementState state, String message) {
		return new StateRegistrationResult(false, state, Optional.empty(), message);
	}
}

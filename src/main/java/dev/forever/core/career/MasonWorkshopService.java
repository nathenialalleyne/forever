package dev.forever.core.career;

import dev.forever.core.settlement.BuildingBounds;
import dev.forever.core.settlement.BuildingRegistrationService;
import dev.forever.core.settlement.RegisteredBuilding;
import dev.forever.core.settlement.Settlement;
import dev.forever.core.settlement.SettlementBalance;
import dev.forever.core.settlement.SettlementRole;
import dev.forever.core.settlement.SettlementState;
import dev.forever.core.settlement.SettlementWorldView;
import dev.forever.core.settlement.ValidationResult;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.Identifier;

/** Mason integration facade over the existing Settlement building registration service. */
public final class MasonWorkshopService {

	public record WorkshopRegistrationResult(
			boolean accepted,
			SettlementState state,
			Optional<RegisteredBuilding> building,
			ValidationResult validation,
			String message) {

		public WorkshopRegistrationResult {
			Objects.requireNonNull(state, "state");
			Objects.requireNonNull(building, "building");
			Objects.requireNonNull(validation, "validation");
			message = CareerCodecs.requiredText(message, "workshop registration result", 512);
			if (accepted != building.isPresent() || (accepted && !validation.valid())) {
				throw new IllegalArgumentException("Mason workshop result fields disagree.");
			}
		}
	}

	private MasonWorkshopService() {
	}

	/** Uses the existing settlement graph registration, adding no parallel building registry. */
	public static BuildingRegistrationService.StateRegistrationResult registerPending(
			SettlementState state,
			UUID settlementId,
			UUID buildingId,
			Identifier dimension,
			BuildingBounds bounds,
			SettlementBalance balance) {
		RegisteredBuilding building = RegisteredBuilding.pending(
				Objects.requireNonNull(buildingId, "buildingId"),
				Objects.requireNonNull(dimension, "dimension"),
				Objects.requireNonNull(bounds, "bounds"),
				Set.of(SettlementRole.WORKPLACE, SettlementRole.WORKSHOP));
		return BuildingRegistrationService.register(state, settlementId, building, balance);
	}

	/** Validates function first, then commits the graph node and cached valid result atomically. */
	public static WorkshopRegistrationResult register(
			SettlementState state,
			UUID settlementId,
			UUID buildingId,
			Identifier dimension,
			BuildingBounds bounds,
			SettlementBalance balance,
			SettlementWorldView world,
			int revision) {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(settlementId, "settlementId");
		Objects.requireNonNull(buildingId, "buildingId");
		Objects.requireNonNull(dimension, "dimension");
		Objects.requireNonNull(bounds, "bounds");
		Objects.requireNonNull(balance, "balance");
		Objects.requireNonNull(world, "world");
		RegisteredBuilding pending = RegisteredBuilding.pending(
				buildingId, dimension, bounds, Set.of(SettlementRole.WORKPLACE, SettlementRole.WORKSHOP));
		ValidationResult validation = MasonWorkshopValidator.validate(pending, world, balance, revision);
		if (!validation.valid()) {
			return new WorkshopRegistrationResult(false, state, Optional.empty(), validation,
					validation.issues().getFirst().message());
		}
		BuildingRegistrationService.StateRegistrationResult registration =
				BuildingRegistrationService.register(state, settlementId, pending, balance);
		if (!registration.accepted()) {
			return new WorkshopRegistrationResult(false, state, Optional.empty(), validation, registration.message());
		}
		Settlement registeredSettlement = registration.state().requireSettlement(settlementId);
		Settlement validatedSettlement = registeredSettlement.withBuildingValidation(buildingId, validation);
		SettlementState next = registration.state().replaceSettlement(validatedSettlement);
		return new WorkshopRegistrationResult(true, next,
				Optional.of(validatedSettlement.building(buildingId).orElseThrow()), validation,
				"The Mason workplace was registered and passed functional validation.");
	}

	public static WorkshopRegistrationResult validate(
			SettlementState state,
			UUID settlementId,
			UUID buildingId,
			SettlementWorldView world,
			SettlementBalance balance,
			int revision) {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(settlementId, "settlementId");
		Objects.requireNonNull(buildingId, "buildingId");
		Objects.requireNonNull(world, "world");
		Objects.requireNonNull(balance, "balance");
		Settlement settlement = state.settlement(settlementId).orElse(null);
		if (settlement == null) {
			ValidationResult result = ValidationResult.invalid(revision, java.util.List.of(
					new dev.forever.core.settlement.ValidationIssue(
							dev.forever.core.settlement.FunctionalRequirement.DECLARATION,
							"The target settlement is not chartered.")));
			return new WorkshopRegistrationResult(false, state, Optional.empty(), result,
					result.issues().getFirst().message());
		}
		RegisteredBuilding building = settlement.building(buildingId).orElse(null);
		if (building == null) {
			ValidationResult result = ValidationResult.invalid(revision, java.util.List.of(
					new dev.forever.core.settlement.ValidationIssue(
							dev.forever.core.settlement.FunctionalRequirement.DECLARATION,
							"The Mason workplace is not registered in the settlement graph.")));
			return new WorkshopRegistrationResult(false, state, Optional.empty(), result,
					result.issues().getFirst().message());
		}
		ValidationResult validation = MasonWorkshopValidator.validate(building, world, balance, revision);
		SettlementState next = state.replaceSettlement(settlement.withBuildingValidation(buildingId, validation));
		return new WorkshopRegistrationResult(validation.valid(), next,
				validation.valid() ? Optional.of(building.withValidation(validation)) : Optional.empty(), validation,
				validation.valid() ? "The Mason workplace passed functional revalidation."
						: validation.issues().getFirst().message());
	}
}

package dev.forever.core.career;

import dev.forever.core.settlement.RegisteredBuilding;
import dev.forever.core.settlement.Settlement;
import dev.forever.core.settlement.SettlementState;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;

/** Pure and server-facing operations for the Mason career state. */
public final class MasonCareerService {

	private MasonCareerService() {
	}

	public static CareerProgressionResult enrol(
			CareerState state,
			Optional<UUID> home,
			Optional<UUID> workplace,
			MasonCareerDefinition definition) {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(home, "home");
		Objects.requireNonNull(workplace, "workplace");
		Objects.requireNonNull(definition, "definition");
		if (!state.profession().isEmpty() || state.rank() != MasonRank.UNASSIGNED) {
			return rejected(state, "The entity already has a career and cannot be enrolled twice.");
		}
		if (!definition.profession().equals(MasonIds.MASON)) {
			return rejected(state, "The loaded definition is not the Mason profession.");
		}
		if (definition.ruleFor(MasonRank.APPRENTICE).isEmpty()) {
			return rejected(state, "Mason data does not define an Apprentice rank.");
		}
		return new CareerProgressionResult(true, CareerState.masonApprentice(home, workplace),
				"The villager entered the Mason Apprentice programme.");
	}

	/** Assigns a villager only to a validated workplace in the existing settlement graph. */
	public static CareerProgressionResult enrol(
			Villager villager,
			SettlementState settlements,
			UUID settlementId,
			UUID workplaceId,
			Optional<UUID> home,
			MasonCareerDefinition definition) {
		Objects.requireNonNull(villager, "villager");
		Objects.requireNonNull(settlements, "settlements");
		Objects.requireNonNull(settlementId, "settlementId");
		Objects.requireNonNull(workplaceId, "workplaceId");
		Objects.requireNonNull(home, "home");
		Objects.requireNonNull(definition, "definition");
		Settlement settlement = settlements.settlement(settlementId).orElse(null);
		if (settlement == null) {
			return rejected(stateOf(villager), "The target settlement is not chartered.");
		}
		RegisteredBuilding building = settlement.building(workplaceId).orElse(null);
		if (building == null || !building.roles().contains(dev.forever.core.settlement.SettlementRole.WORKPLACE)
				|| !building.validation().valid()) {
			return rejected(stateOf(villager), "The Mason workplace is missing or has not passed functional validation.");
		}
		CareerProgressionResult result = enrol(stateOf(villager), home, Optional.of(workplaceId), definition);
		if (result.applied()) {
			setState(villager, result.state());
		}
		return result;
	}

	public static CareerProgressionResult learnTechnique(
			CareerState state,
			Identifier technique,
			MasonCareerDefinition definition,
			CareerLearningSource source) {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(technique, "technique");
		Objects.requireNonNull(definition, "definition");
		Objects.requireNonNull(source, "source");
		if (!state.isMason() || state.status() != CareerStatus.ACTIVE) {
			return rejected(state, "Only an active Mason may learn a technique.");
		}
		if (!definition.knowsDefinitionFor(technique)) {
			return rejected(state, "The requested technique is not present in the loaded Mason data.");
		}
		if (state.knownTechniques().contains(technique)) {
			return rejected(state, "The Mason already knows this technique; the request was not replayed.");
		}
		CareerState next = state.withKnownTechnique(technique);
		return new CareerProgressionResult(true, next,
				"The Mason learned " + technique + " from " + source.name().toLowerCase() + ".");
	}

	/** Records a distinct demonstrated technique, never a repetition counter. */
	public static CareerProgressionResult demonstrateTechnique(CareerState state, Identifier technique) {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(technique, "technique");
		if (!state.isMason() || state.status() != CareerStatus.ACTIVE) {
			return rejected(state, "Only an active Mason may demonstrate a technique.");
		}
		if (!state.knownTechniques().contains(technique)) {
			return rejected(state, "A Mason must know a technique before demonstrating it.");
		}
		if (state.demonstratedTechniques().contains(technique)) {
			return rejected(state, "This technique was already demonstrated; repetition does not add mastery.");
		}
		return new CareerProgressionResult(true, state.withDemonstratedTechnique(technique),
				"The Mason demonstrated a distinct technique.");
	}

	/** Records one distinct validated project outcome. */
	public static CareerProgressionResult recordValidatedProject(CareerState state, Identifier project) {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(project, "project");
		if (!state.isMason() || state.status() != CareerStatus.ACTIVE) {
			return rejected(state, "Only an active Mason may receive project evidence.");
		}
		if (state.validatedProjects().contains(project)) {
			return rejected(state, "This project evidence was already recorded; the request was not replayed.");
		}
		return new CareerProgressionResult(true, state.withValidatedProject(project),
				"The validated Mason project was recorded.");
	}

	/** Promotes exactly one rank when the next rank's data-defined evidence is satisfied. */
	public static CareerProgressionResult advanceRank(
			CareerState state, MasonCareerDefinition definition) {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(definition, "definition");
		if (!state.isMason() || state.status() != CareerStatus.ACTIVE) {
			return rejected(state, "Only an active Mason may advance rank.");
		}
		MasonRank nextRank = state.rank().next();
		if (nextRank == null) {
			return rejected(state, "The Mason already holds the Master rank.");
		}
		MasonRankRule rule = definition.ruleFor(nextRank).orElse(null);
		if (rule == null) {
			return rejected(state, "Mason data does not define the next rank " + nextRank + ".");
		}
		if (!rule.satisfiedBy(state)) {
			return rejected(state, "The Mason has not met the next rank's demonstrated-technique, teaching, and project criteria.");
		}
		return new CareerProgressionResult(true, state.withRank(nextRank),
				"The Mason advanced to " + nextRank.name().toLowerCase() + ".");
	}

	public static CareerState stateOf(Entity entity) {
		Objects.requireNonNull(entity, "entity");
		return ((AttachmentTarget) entity).getAttachedOrCreate(CareerAttachment.TYPE);
	}

	public static void setState(Entity entity, CareerState state) {
		Objects.requireNonNull(entity, "entity");
		((AttachmentTarget) entity).setAttached(CareerAttachment.TYPE, Objects.requireNonNull(state, "state"));
	}

	private static CareerProgressionResult rejected(CareerState state, String message) {
		return new CareerProgressionResult(false, state, message);
	}
}

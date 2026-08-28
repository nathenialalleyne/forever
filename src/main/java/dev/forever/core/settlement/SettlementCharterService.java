package dev.forever.core.settlement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/** Server-authoritative operations for founding Charters and explicitly importing villages. */
public final class SettlementCharterService {

	private SettlementCharterService() {
	}

	/** The only operation that creates a new Settlement identity. It performs no world scan. */
	public static Settlement found(
			UUID settlementId,
			String name,
			UUID founder,
			Identifier dimension,
			BlockPos anchor) {
		return Settlement.found(settlementId, name, founder, dimension, anchor);
	}

	public static SettlementState found(SettlementState state, Settlement settlement) {
		Objects.requireNonNull(state, "state");
		return state.withSettlement(Objects.requireNonNull(settlement, "settlement"));
	}

	public static void found(SettlementSavedData savedData, Settlement settlement) {
		Objects.requireNonNull(savedData, "savedData").update(state -> found(state, settlement));
	}

	public static SettlementState addMember(SettlementState state, UUID settlementId, UUID memberId) {
		Objects.requireNonNull(state, "state");
		return state.replaceSettlement(state.requireSettlement(settlementId).withMember(memberId));
	}

	public static SettlementState removeMember(SettlementState state, UUID settlementId, UUID memberId) {
		Objects.requireNonNull(state, "state");
		return state.replaceSettlement(state.requireSettlement(settlementId).withoutMember(memberId));
	}

	public static void addMember(SettlementSavedData savedData, UUID settlementId, UUID memberId) {
		Objects.requireNonNull(savedData, "savedData").update(state -> addMember(state, settlementId, memberId));
	}

	public static void removeMember(SettlementSavedData savedData, UUID settlementId, UUID memberId) {
		Objects.requireNonNull(savedData, "savedData").update(state -> removeMember(state, settlementId, memberId));
	}

	/**
	 * Builds a preview from already-selected, bounded candidates. This method intentionally
	 * has no ServerLevel argument, so it cannot silently discover or claim a village.
	 */
	public static VillageImportProposal proposeVillageImport(
			UUID proposalId,
			Identifier sourceDimension,
			BuildingBounds candidateArea,
			List<RegisteredBuilding> candidates,
			int sourceVillagerCount,
			List<String> unresolvedIssues,
			SettlementBalance balance) {
		Objects.requireNonNull(proposalId, "proposalId");
		Objects.requireNonNull(sourceDimension, "sourceDimension");
		Objects.requireNonNull(candidateArea, "candidateArea");
		Objects.requireNonNull(candidates, "candidates");
		Objects.requireNonNull(unresolvedIssues, "unresolvedIssues");
		Objects.requireNonNull(balance, "balance");
		if (!candidateArea.isWithin(balance.hardValidationVolumeCap())) {
			throw new SettlementDataException("The village import area has volume " + candidateArea.volume()
					+ ", above the hard bounded import cap of " + balance.hardValidationVolumeCap() + " blocks.");
		}
		if (candidates.size() > balance.maximumImportBuildings()) {
			throw new SettlementDataException("The village import preview contains " + candidates.size()
					+ " buildings, above the configured limit of " + balance.maximumImportBuildings() + ".");
		}
		for (RegisteredBuilding candidate : candidates) {
			if (!candidate.bounds().isWithin(balance.hardValidationVolumeCap())) {
				throw new SettlementDataException("Imported building " + candidate.id()
						+ " exceeds the hard bounded validation cap.");
			}
		}
		return new VillageImportProposal(proposalId, sourceDimension, candidateArea, candidates,
				sourceVillagerCount, unresolvedIssues);
	}

	public record ImportResult(
			boolean committed,
			SettlementState state,
			Optional<Settlement> settlement,
			String message) {

		public ImportResult {
			Objects.requireNonNull(state, "state");
			Objects.requireNonNull(settlement, "settlement");
			message = SettlementCodecs.requiredText(message, "import result", 512);
			if (committed != settlement.isPresent()) {
				throw new IllegalArgumentException("Import result commitment and settlement presence disagree.");
			}
		}
	}

	/** A cancelled proposal has no world mutation and remains available for a later review. */
	public static ImportResult cancel(VillageImportProposal proposal, SettlementState state) {
		Objects.requireNonNull(proposal, "proposal");
		return new ImportResult(false, Objects.requireNonNull(state, "state"), Optional.empty(),
				"Village import proposal " + proposal.proposalId() + " was cancelled before Charter commit.");
	}

	/**
	 * Commits only the selected candidates. Unattachable candidates become visible unresolved
	 * issues, allowing a partial import while preserving every original block and entity.
	 */
	public static ImportResult importVillage(
			SettlementState state,
			UUID settlementId,
			String name,
			UUID founder,
			VillageImportProposal proposal,
			List<UUID> selectedBuildingIds,
			SettlementBalance balance) {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(settlementId, "settlementId");
		Objects.requireNonNull(proposal, "proposal");
		Objects.requireNonNull(selectedBuildingIds, "selectedBuildingIds");
		Objects.requireNonNull(balance, "balance");
		if (state.containsImportedProposal(proposal.proposalId())) {
			return new ImportResult(false, state, Optional.empty(), "Village import proposal "
					+ proposal.proposalId() + " has already been committed; no duplicate nodes were created.");
		}
		if (state.settlement(settlementId).isPresent()) {
			return new ImportResult(false, state, Optional.empty(), "Settlement " + settlementId
					+ " already exists; choose a new stable Charter ID.");
		}
		Set<UUID> selected = new HashSet<>(selectedBuildingIds);
		if (selected.size() != selectedBuildingIds.size() || selected.isEmpty()) {
			return new ImportResult(false, state, Optional.empty(),
					"An import must select at least one unique candidate building.");
		}
		for (UUID buildingId : selected) {
			if (proposal.candidate(buildingId) == null) {
				return new ImportResult(false, state, Optional.empty(), "Building " + buildingId
						+ " is not part of this bounded import proposal.");
			}
		}

		Settlement imported = Settlement.found(settlementId, name, founder, proposal.sourceDimension(),
				proposal.candidateArea().center());
		List<String> unresolved = new ArrayList<>(proposal.unresolvedIssues());
		List<UUID> orderedSelection = selected.stream().sorted().toList();
		for (UUID buildingId : orderedSelection) {
			RegisteredBuilding candidate = proposal.candidate(buildingId);
			SettlementGraph.RegistrationResult result = SettlementGraph.attach(imported, candidate, balance);
			if (result.accepted()) {
				imported = result.settlement();
			} else {
				unresolved.add("Building " + buildingId + " was not attached during import: " + result.message());
			}
		}
		if (imported.buildings().isEmpty()) {
			return new ImportResult(false, state, Optional.empty(),
					"The selected import candidates could not form a graph; the original Charter state is unchanged.");
		}
		if (unresolved.size() > VillageImportRecord.MAX_UNRESOLVED_ISSUES) {
			return new ImportResult(false, state, Optional.empty(), "The import produced " + unresolved.size()
					+ " unresolved issues, above the bounded limit of " + VillageImportRecord.MAX_UNRESOLVED_ISSUES
					+ "; narrow the selection and review again. The original Charter state is unchanged.");
		}

		VillageImportRecord record = new VillageImportRecord(proposal.proposalId(), proposal.sourceDimension(),
				proposal.candidateArea(), proposal.sourceVillagerCount(), orderedSelection, unresolved);
		imported = imported.withImportedVillage(record);
		SettlementState nextState = state.withSettlement(imported);
		return new ImportResult(true, nextState, Optional.of(imported), "Village import committed as settlement "
				+ settlementId + "; original blocks and entities were not modified.");
	}

	public static ImportResult importVillage(
			SettlementSavedData savedData,
			UUID settlementId,
			String name,
			UUID founder,
			VillageImportProposal proposal,
			List<UUID> selectedBuildingIds,
			SettlementBalance balance) {
		Objects.requireNonNull(savedData, "savedData");
		ImportResult result = importVillage(savedData.state(), settlementId, name, founder, proposal,
				selectedBuildingIds, balance);
		if (result.committed()) {
			savedData.replace(result.state());
		}
		return result;
	}
}

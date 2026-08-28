package dev.forever.core.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.Identifier;

/**
 * Bounded preview produced by an explicit import request. Candidate nodes are supplied by
 * the operator or a bounded integration, never discovered by a world-wide scan.
 */
public record VillageImportProposal(
		UUID proposalId,
		Identifier sourceDimension,
		BuildingBounds candidateArea,
		List<RegisteredBuilding> candidateBuildings,
		int sourceVillagerCount,
		List<String> unresolvedIssues) {

	private static final int MAX_CANDIDATE_BUILDINGS = 4_096;
	private static final int MAX_UNRESOLVED_ISSUES = 128;

	private record Encoded(
			UUID proposalId,
			Identifier sourceDimension,
			BuildingBounds candidateArea,
			List<RegisteredBuilding> candidateBuildings,
			int sourceVillagerCount,
			List<String> unresolvedIssues) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SettlementCodecs.UUID_CODEC.fieldOf("proposal_id").forGetter(Encoded::proposalId),
			Identifier.CODEC.fieldOf("source_dimension").forGetter(Encoded::sourceDimension),
			BuildingBounds.CODEC.fieldOf("candidate_area").forGetter(Encoded::candidateArea),
			SettlementCodecs.boundedList(RegisteredBuilding.CODEC, MAX_CANDIDATE_BUILDINGS,
					"import candidates").fieldOf("candidate_buildings")
					.forGetter(Encoded::candidateBuildings),
			Codec.INT.fieldOf("source_villager_count").forGetter(Encoded::sourceVillagerCount),
			SettlementCodecs.boundedList(Codec.STRING, MAX_UNRESOLVED_ISSUES,
					"proposal unresolved issues").fieldOf("unresolved_issues")
					.forGetter(Encoded::unresolvedIssues)
		).apply(instance, Encoded::new));

	public static final Codec<VillageImportProposal> CODEC = RAW_CODEC.flatXmap(
			VillageImportProposal::create,
			proposal -> DataResult.success(new Encoded(
					proposal.proposalId(), proposal.sourceDimension(), proposal.candidateArea(), proposal.candidateBuildings(),
					proposal.sourceVillagerCount(), proposal.unresolvedIssues())));

	public VillageImportProposal {
		Objects.requireNonNull(proposalId, "proposalId");
		Objects.requireNonNull(sourceDimension, "sourceDimension");
		Objects.requireNonNull(candidateArea, "candidateArea");
		if (!candidateArea.isWithin(SettlementBalance.ABSOLUTE_MAX_VALIDATION_VOLUME)) {
			throw new IllegalArgumentException("A village import candidate area exceeds the absolute bounded volume cap of "
					+ SettlementBalance.ABSOLUTE_MAX_VALIDATION_VOLUME + " blocks.");
		}
		Objects.requireNonNull(candidateBuildings, "candidateBuildings");
		if (candidateBuildings.isEmpty() || candidateBuildings.size() > MAX_CANDIDATE_BUILDINGS) {
			throw new IllegalArgumentException("A village import preview needs a bounded candidate set.");
		}
		if (sourceVillagerCount < 0) {
			throw new IllegalArgumentException("Imported village villager count cannot be negative.");
		}
		List<RegisteredBuilding> copiedCandidates = new ArrayList<>(candidateBuildings.size());
		Set<UUID> candidateIds = new HashSet<>();
		for (RegisteredBuilding building : candidateBuildings) {
			Objects.requireNonNull(building, "candidateBuildings cannot contain null");
			if (!candidateIds.add(building.id())) {
				throw new IllegalArgumentException("A village import preview cannot contain duplicate building IDs.");
			}
			copiedCandidates.add(building);
		}
		candidateBuildings = copiedCandidates.stream().sorted((left, right) ->
				left.id().compareTo(right.id())).toList();
		for (RegisteredBuilding building : candidateBuildings) {
			if (!sourceDimension.equals(building.dimension())) {
				throw new IllegalArgumentException("Every imported building must use the proposal dimension.");
			}
			if (!candidateArea.contains(building.bounds().min())
					|| !candidateArea.contains(building.bounds().max())) {
				throw new IllegalArgumentException("An imported building must remain inside the candidate area.");
			}
		}
		Objects.requireNonNull(unresolvedIssues, "unresolvedIssues");
		if (unresolvedIssues.size() > MAX_UNRESOLVED_ISSUES) {
			throw new IllegalArgumentException("An import preview contains too many unresolved issues.");
		}
		unresolvedIssues = unresolvedIssues.stream()
				.map(issue -> SettlementCodecs.requiredText(issue, "proposal issue", 512))
				.toList();
	}

	private static DataResult<VillageImportProposal> create(Encoded encoded) {
		try {
			return DataResult.success(new VillageImportProposal(
					encoded.proposalId(), encoded.sourceDimension(), encoded.candidateArea(), encoded.candidateBuildings(),
					encoded.sourceVillagerCount(), encoded.unresolvedIssues()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	public RegisteredBuilding candidate(UUID buildingId) {
		return candidateBuildings.stream()
				.filter(building -> building.id().equals(buildingId))
				.findFirst()
				.orElse(null);
	}
}

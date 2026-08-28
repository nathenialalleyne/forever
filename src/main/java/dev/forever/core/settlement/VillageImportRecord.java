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

/** Durable evidence that a committed Charter came from an explicit village import. */
public record VillageImportRecord(
		UUID proposalId,
		Identifier sourceDimension,
		BuildingBounds sourceArea,
		int sourceVillagerCount,
		List<UUID> selectedBuildingIds,
		List<String> unresolvedIssues) {

	private static final int MAX_SELECTED_BUILDINGS = 4_096;
	static final int MAX_UNRESOLVED_ISSUES = 128;

	private record Encoded(
			UUID proposalId,
			Identifier sourceDimension,
			BuildingBounds sourceArea,
			int sourceVillagerCount,
			List<UUID> selectedBuildingIds,
			List<String> unresolvedIssues) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SettlementCodecs.UUID_CODEC.fieldOf("proposal_id").forGetter(Encoded::proposalId),
			Identifier.CODEC.fieldOf("source_dimension").forGetter(Encoded::sourceDimension),
			BuildingBounds.CODEC.fieldOf("source_area").forGetter(Encoded::sourceArea),
			Codec.INT.fieldOf("source_villager_count").forGetter(Encoded::sourceVillagerCount),
			SettlementCodecs.boundedList(SettlementCodecs.UUID_CODEC, MAX_SELECTED_BUILDINGS,
					"import selected buildings").fieldOf("selected_building_ids")
					.forGetter(Encoded::selectedBuildingIds),
			SettlementCodecs.boundedList(Codec.STRING, MAX_UNRESOLVED_ISSUES,
					"import unresolved issues").fieldOf("unresolved_issues")
					.forGetter(Encoded::unresolvedIssues)
		).apply(instance, Encoded::new));

	public static final Codec<VillageImportRecord> CODEC = RAW_CODEC.flatXmap(
			VillageImportRecord::create,
			record -> DataResult.success(new Encoded(
					record.proposalId(), record.sourceDimension(), record.sourceArea(), record.sourceVillagerCount(),
				record.selectedBuildingIds(), record.unresolvedIssues())));

	public VillageImportRecord {
		Objects.requireNonNull(proposalId, "proposalId");
		Objects.requireNonNull(sourceDimension, "sourceDimension");
		Objects.requireNonNull(sourceArea, "sourceArea");
		if (!sourceArea.isWithin(SettlementBalance.ABSOLUTE_MAX_VALIDATION_VOLUME)) {
			throw new IllegalArgumentException("An imported village source area exceeds the absolute bounded volume cap of "
					+ SettlementBalance.ABSOLUTE_MAX_VALIDATION_VOLUME + " blocks.");
		}
		if (sourceVillagerCount < 0) {
			throw new IllegalArgumentException("Imported village villager count cannot be negative.");
		}
		Objects.requireNonNull(selectedBuildingIds, "selectedBuildingIds");
		if (selectedBuildingIds.isEmpty() || selectedBuildingIds.size() > MAX_SELECTED_BUILDINGS) {
			throw new IllegalArgumentException("An import must select a bounded, non-empty building set.");
		}
		List<UUID> copiedBuildingIds = new ArrayList<>(selectedBuildingIds.size());
		Set<UUID> distinctBuildingIds = new HashSet<>();
		for (UUID buildingId : selectedBuildingIds) {
			Objects.requireNonNull(buildingId, "selectedBuildingIds cannot contain null");
			if (!distinctBuildingIds.add(buildingId)) {
				throw new IllegalArgumentException("An imported village cannot contain duplicate selected building IDs.");
			}
			copiedBuildingIds.add(buildingId);
		}
		copiedBuildingIds.sort(UUID::compareTo);
		selectedBuildingIds = List.copyOf(copiedBuildingIds);
		Objects.requireNonNull(unresolvedIssues, "unresolvedIssues");
		if (unresolvedIssues.size() > MAX_UNRESOLVED_ISSUES) {
			throw new IllegalArgumentException("An import contains too many unresolved issues.");
		}
		unresolvedIssues = unresolvedIssues.stream()
				.map(issue -> SettlementCodecs.requiredText(issue, "import issue", 512))
				.toList();
	}

	private static DataResult<VillageImportRecord> create(Encoded encoded) {
		try {
			return DataResult.success(new VillageImportRecord(
					encoded.proposalId(), encoded.sourceDimension(), encoded.sourceArea(), encoded.sourceVillagerCount(),
					encoded.selectedBuildingIds(), encoded.unresolvedIssues()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}
}

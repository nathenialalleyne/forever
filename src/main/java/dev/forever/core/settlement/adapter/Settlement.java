package dev.forever.core.settlement.adapter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.settlement.domain.MigrationRecord;
import dev.forever.core.settlement.domain.SettlementCodecs;
import dev.forever.core.settlement.domain.SettlementDataException;
import dev.forever.core.settlement.domain.SettlementEdge;
import dev.forever.core.settlement.domain.SettlementEdgeKind;
import dev.forever.core.settlement.domain.ValidationResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

/**
 * An immutable Settlement Charter and its registered building graph.
 *
 * <p>The Charter is an explicit declaration. Constructing one does not inspect the world,
 * claim nearby blocks, or impose a visual theme. The parent {@link SettlementState} owns
 * the schema version for this graph record.
 */
public record Settlement(
		UUID id,
		String name,
		UUID founder,
		Set<UUID> members,
		Identifier dimension,
		BlockPos anchor,
		Map<UUID, RegisteredBuilding> buildings,
		List<SettlementEdge> edges,
		List<MigrationRecord> migrations,
		Optional<VillageImportRecord> importedVillage,
		int validationRevision) {

	private static final int MAX_BUILDINGS = 4_096;
	private static final int MAX_EDGES = 8_192;
	private static final int MAX_MIGRATIONS = 4_096;
	private static final int MAX_MEMBERS = 64;

	private record EdgeKey(UUID from, UUID to, SettlementEdgeKind kind) {
	}

	private record Encoded(
			UUID id,
			String name,
			UUID founder,
			Optional<Set<UUID>> members,
			Identifier dimension,
			BlockPos anchor,
			Map<UUID, RegisteredBuilding> buildings,
			List<SettlementEdge> edges,
			List<MigrationRecord> migrations,
			Optional<VillageImportRecord> importedVillage,
			int validationRevision) {
	}

	private static final Codec<Encoded> RAW_ENCODED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SettlementCodecs.UUID_CODEC.fieldOf("id").forGetter(Encoded::id),
			Codec.STRING.fieldOf("name").forGetter(Encoded::name),
			SettlementCodecs.UUID_CODEC.fieldOf("founder").forGetter(Encoded::founder),
		SettlementCodecs.setCodec(SettlementCodecs.UUID_CODEC, MAX_MEMBERS, "Charter members")
					.optionalFieldOf("members").forGetter(Encoded::members),
			Identifier.CODEC.fieldOf("dimension").forGetter(Encoded::dimension),
			BlockPos.CODEC.fieldOf("anchor").forGetter(Encoded::anchor),
			SettlementCodecs.boundedMap(SettlementCodecs.UUID_CODEC, RegisteredBuilding.CODEC,
					MAX_BUILDINGS, "registered buildings").fieldOf("buildings")
					.forGetter(Encoded::buildings),
			SettlementCodecs.boundedList(SettlementEdge.CODEC, MAX_EDGES, "graph edges")
					.fieldOf("edges").forGetter(Encoded::edges),
			SettlementCodecs.boundedList(MigrationRecord.CODEC, MAX_MIGRATIONS, "migration records")
					.fieldOf("migrations").forGetter(Encoded::migrations),
			VillageImportRecord.CODEC.optionalFieldOf("imported_village")
					.forGetter(Encoded::importedVillage),
			Codec.INT.fieldOf("validation_revision").forGetter(Encoded::validationRevision)
	).apply(instance, Encoded::new));

	public static final Codec<Settlement> CODEC = RAW_ENCODED_CODEC.flatXmap(
			encoded -> create(encoded),
			settlement -> DataResult.success(new Encoded(
					settlement.id(), settlement.name(), settlement.founder(), Optional.of(settlement.members()),
					settlement.dimension(), settlement.anchor(), settlement.buildings(), settlement.edges(),
					settlement.migrations(), settlement.importedVillage(), settlement.validationRevision())));

	public Settlement {
		Objects.requireNonNull(id, "id");
		name = SettlementCodecs.requiredText(name, "name", 64);
		Objects.requireNonNull(founder, "founder");
		Objects.requireNonNull(members, "members");
		if (members.isEmpty() || members.size() > MAX_MEMBERS || !members.contains(founder)) {
			throw new IllegalArgumentException("A settlement Charter must contain its founder and at most "
					+ MAX_MEMBERS + " members.");
		}
		for (UUID member : members) {
			Objects.requireNonNull(member, "member");
		}
		members = Collections.unmodifiableSet(new TreeSet<>(members));
		Objects.requireNonNull(dimension, "dimension");
		Objects.requireNonNull(anchor, "anchor");
		anchor = new BlockPos(anchor);
		Objects.requireNonNull(buildings, "buildings");
		if (buildings.size() > MAX_BUILDINGS) {
			throw new IllegalArgumentException("Settlement contains too many registered buildings.");
		}
		TreeMap<UUID, RegisteredBuilding> sortedBuildings = new TreeMap<>();
		for (Map.Entry<UUID, RegisteredBuilding> entry : buildings.entrySet()) {
			if (entry.getKey() == null || entry.getValue() == null) {
				throw new IllegalArgumentException("Settlement buildings cannot contain null IDs or values.");
			}
			if (!entry.getKey().equals(entry.getValue().id())) {
				throw new IllegalArgumentException("Settlement building map key does not match the node ID.");
			}
			sortedBuildings.put(entry.getKey(), entry.getValue());
		}
		buildings = Collections.unmodifiableMap(sortedBuildings);

		Objects.requireNonNull(edges, "edges");
		if (edges.size() > MAX_EDGES) {
			throw new IllegalArgumentException("Settlement contains too many graph edges.");
		}
		Set<EdgeKey> edgeKeys = new HashSet<>();
		List<SettlementEdge> copiedEdges = new ArrayList<>(edges.size());
		for (SettlementEdge edge : edges) {
			Objects.requireNonNull(edge, "edge");
			if (!buildings.containsKey(edge.from()) || !buildings.containsKey(edge.to())) {
				throw new IllegalArgumentException("Settlement graph edge references a missing building node.");
			}
			if (!edgeKeys.add(new EdgeKey(edge.from(), edge.to(), edge.kind()))) {
				throw new IllegalArgumentException("Settlement graph contains a duplicate edge.");
			}
			copiedEdges.add(edge);
		}
		edges = List.copyOf(copiedEdges);

		Objects.requireNonNull(migrations, "migrations");
		if (migrations.size() > MAX_MIGRATIONS) {
			throw new IllegalArgumentException("Settlement contains too many migration records.");
		}
		Set<UUID> migrationIds = new HashSet<>();
		List<MigrationRecord> copiedMigrations = new ArrayList<>(migrations.size());
		for (MigrationRecord migration : migrations) {
			Objects.requireNonNull(migration, "migration");
			if (!migrationIds.add(migration.migrationId())) {
				throw new IllegalArgumentException("Settlement contains a duplicate migration ID.");
			}
			copiedMigrations.add(migration);
		}
		copiedMigrations.sort((left, right) -> left.migrationId().compareTo(right.migrationId()));
		migrations = List.copyOf(copiedMigrations);

		Objects.requireNonNull(importedVillage, "importedVillage");
		if (validationRevision < 0) {
			throw new IllegalArgumentException("Settlement validation revision cannot be negative.");
		}
	}

	private static DataResult<Settlement> create(Encoded encoded) {
		try {
			return DataResult.success(new Settlement(
					encoded.id(), encoded.name(), encoded.founder(), encoded.members().orElse(Set.of(encoded.founder())), encoded.dimension(),
					encoded.anchor(), encoded.buildings(), encoded.edges(), encoded.migrations(),
					encoded.importedVillage(), encoded.validationRevision()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	public static Settlement found(
			UUID id, String name, UUID founder, Identifier dimension, BlockPos anchor) {
		return new Settlement(id, name, founder, Set.of(founder), dimension, anchor, Map.of(), List.of(), List.of(),
				Optional.empty(), 0);
	}

	public Optional<RegisteredBuilding> building(UUID buildingId) {
		return Optional.ofNullable(buildings.get(buildingId));
	}

	public Settlement withBuilding(RegisteredBuilding building, Optional<SettlementEdge> edge) {
		Objects.requireNonNull(building, "building");
		Objects.requireNonNull(edge, "edge");
		if (buildings.containsKey(building.id())) {
			throw new SettlementDataException("Building " + building.id()
					+ " is already registered in settlement " + id + ".");
		}
		TreeMap<UUID, RegisteredBuilding> nextBuildings = new TreeMap<>(buildings);
		nextBuildings.put(building.id(), building);
		List<SettlementEdge> nextEdges = new ArrayList<>(edges);
		edge.ifPresent(nextEdges::add);
		return new Settlement(id, name, founder, members, dimension, anchor, nextBuildings, nextEdges,
				migrations, importedVillage, validationRevision);
	}

	public Settlement withBuildingValidation(UUID buildingId, ValidationResult validation) {
		Objects.requireNonNull(validation, "validation");
		RegisteredBuilding existing = buildings.get(buildingId);
		if (existing == null) {
			throw new SettlementDataException("Cannot validate missing building " + buildingId
					+ " in settlement " + id + ".");
		}
		TreeMap<UUID, RegisteredBuilding> nextBuildings = new TreeMap<>(buildings);
		nextBuildings.put(buildingId, existing.withValidation(validation));
		return new Settlement(id, name, founder, members, dimension, anchor, nextBuildings, edges, migrations,
				importedVillage, Math.max(validationRevision, validation.revision()));
	}

	public Settlement withoutBuilding(UUID buildingId) {
		if (!buildings.containsKey(buildingId)) {
			throw new SettlementDataException("Cannot remove missing building " + buildingId
					+ " from settlement " + id + ".");
		}
		TreeMap<UUID, RegisteredBuilding> nextBuildings = new TreeMap<>(buildings);
		nextBuildings.remove(buildingId);
		List<SettlementEdge> nextEdges = edges.stream()
				.filter(edge -> !edge.from().equals(buildingId) && !edge.to().equals(buildingId))
				.toList();
		return new Settlement(id, name, founder, members, dimension, anchor, nextBuildings, nextEdges, migrations,
				importedVillage, validationRevision);
	}

	public Settlement withMigration(MigrationRecord migration) {
		Objects.requireNonNull(migration, "migration");
		if (migrations.stream().anyMatch(existing -> existing.migrationId().equals(migration.migrationId()))) {
			throw new SettlementDataException("Migration " + migration.migrationId()
					+ " is already recorded in settlement " + id + ".");
		}
		List<MigrationRecord> next = new ArrayList<>(migrations);
		next.add(migration);
		return new Settlement(id, name, founder, members, dimension, anchor, buildings, edges, next,
				importedVillage, validationRevision);
	}

	public Settlement withMigrations(List<MigrationRecord> nextMigrations) {
		return new Settlement(id, name, founder, members, dimension, anchor, buildings, edges, nextMigrations,
				importedVillage, validationRevision);
	}

	public Settlement withImportedVillage(VillageImportRecord importRecord) {
		return new Settlement(id, name, founder, members, dimension, anchor, buildings, edges, migrations,
				Optional.ofNullable(importRecord), validationRevision);
	}

	public Settlement withMember(UUID member) {
		Objects.requireNonNull(member, "member");
		if (members.contains(member)) {
			return this;
		}
		Set<UUID> nextMembers = new HashSet<>(members);
		nextMembers.add(member);
		return new Settlement(id, name, founder, nextMembers, dimension, anchor, buildings, edges, migrations,
				importedVillage, validationRevision);
	}

	public Settlement withoutMember(UUID member) {
		Objects.requireNonNull(member, "member");
		if (member.equals(founder)) {
			throw new SettlementDataException("The Charter founder cannot be removed from settlement membership.");
		}
		Set<UUID> nextMembers = new HashSet<>(members);
		nextMembers.remove(member);
		return new Settlement(id, name, founder, nextMembers, dimension, anchor, buildings, edges, migrations,
				importedVillage, validationRevision);
	}
}

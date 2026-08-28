package dev.forever.core.career;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.data.SchemaVersioned;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import net.minecraft.resources.Identifier;

/**
 * Immutable, versioned state for one villager career.
 *
 * <p>The entity attachment stores personal knowledge and bounded relationship references. A
 * taught technique is retained in {@link #taughtTechniques()} so death handling can distinguish
 * institutional knowledge from knowledge that existed only in the deceased villager's head.
 */
public record CareerState(
		int schemaVersion,
		Optional<Identifier> profession,
		MasonRank rank,
		Set<Identifier> knownTechniques,
		Set<Identifier> demonstratedTechniques,
		Set<Identifier> taughtTechniques,
		Set<Identifier> validatedProjects,
		Optional<UUID> home,
		Optional<UUID> workplace,
		Optional<UUID> mentor,
		Set<UUID> apprentices,
		CareerStatus status,
		Optional<PhysicalDeathCause> deathCause) {

	public static final int CURRENT_SCHEMA = 2;
	public static final int MAX_TECHNIQUES = 128;
	public static final int MAX_PROJECTS = 128;
	public static final int MAX_APPRENTICES = 16;

	private record Encoded(
			int schemaVersion,
			Optional<Identifier> profession,
			MasonRank rank,
			Set<Identifier> knownTechniques,
			Optional<Set<Identifier>> demonstratedTechniques,
			Optional<Set<Identifier>> taughtTechniques,
			Optional<Set<Identifier>> validatedProjects,
			Optional<UUID> home,
			Optional<UUID> workplace,
			Optional<UUID> mentor,
			Set<UUID> apprentices,
			Optional<CareerStatus> status,
			Optional<PhysicalDeathCause> deathCause) {
	}

	private static final Codec<Set<Identifier>> TECHNIQUES_CODEC =
			CareerCodecs.boundedSet(Identifier.CODEC, MAX_TECHNIQUES, "techniques");
	private static final Codec<Set<Identifier>> PROJECTS_CODEC =
			CareerCodecs.boundedSet(Identifier.CODEC, MAX_PROJECTS, "validated projects");
	private static final Codec<Set<UUID>> APPRENTICES_CODEC =
			CareerCodecs.boundedSet(CareerCodecs.UUID_CODEC, MAX_APPRENTICES, "apprentices");

	private static final Codec<Encoded> RAW_ENCODED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("schema_version").forGetter(Encoded::schemaVersion),
			Identifier.CODEC.optionalFieldOf("profession").forGetter(Encoded::profession),
			MasonRank.CODEC.fieldOf("rank").forGetter(Encoded::rank),
			TECHNIQUES_CODEC.fieldOf("known_techniques").forGetter(Encoded::knownTechniques),
			TECHNIQUES_CODEC.optionalFieldOf("demonstrated_techniques")
					.forGetter(Encoded::demonstratedTechniques),
			TECHNIQUES_CODEC.optionalFieldOf("taught_techniques")
					.forGetter(Encoded::taughtTechniques),
			PROJECTS_CODEC.optionalFieldOf("validated_projects")
					.forGetter(Encoded::validatedProjects),
			CareerCodecs.UUID_CODEC.optionalFieldOf("home").forGetter(Encoded::home),
			CareerCodecs.UUID_CODEC.optionalFieldOf("workplace").forGetter(Encoded::workplace),
			CareerCodecs.UUID_CODEC.optionalFieldOf("mentor").forGetter(Encoded::mentor),
			APPRENTICES_CODEC.fieldOf("apprentices").forGetter(Encoded::apprentices),
			CareerStatus.CODEC.optionalFieldOf("status").forGetter(Encoded::status),
			PhysicalDeathCause.CODEC.optionalFieldOf("death_cause").forGetter(Encoded::deathCause)
	).apply(instance, Encoded::new));

	private static final Codec<CareerState> RAW_CODEC = RAW_ENCODED_CODEC.flatXmap(
			encoded -> create(
					encoded.schemaVersion(),
					encoded.profession(),
					encoded.rank(),
					encoded.knownTechniques(),
					encoded.demonstratedTechniques().orElse(Set.of()),
					encoded.taughtTechniques().orElse(Set.of()),
					encoded.validatedProjects().orElse(Set.of()),
					encoded.home(),
					encoded.workplace(),
					encoded.mentor(),
					encoded.apprentices(),
					encoded.status().orElse(CareerStatus.ACTIVE),
					encoded.deathCause()),
			state -> DataResult.success(new Encoded(
					state.schemaVersion(),
					state.profession(),
					state.rank(),
					state.knownTechniques(),
					Optional.of(state.demonstratedTechniques()),
					Optional.of(state.taughtTechniques()),
					Optional.of(state.validatedProjects()),
					state.home(),
					state.workplace(),
					state.mentor(),
					state.apprentices(),
					Optional.of(state.status()),
					state.deathCause())));

	/** Codec used by the Fabric persistent entity attachment. */
	public static final Codec<CareerState> CODEC = SchemaVersioned.migrating(
			RAW_CODEC,
			CareerState::schemaVersion,
			CURRENT_SCHEMA,
			CareerState::migrate);

	/** Raw codec is exposed for explicit prior-schema fixtures and migration tests. */
	public static Codec<CareerState> rawCodec() {
		return RAW_CODEC;
	}

	public CareerState {
		if (schemaVersion < 1) {
			throw new IllegalArgumentException("Career state schema version must be at least 1.");
		}
		profession = Objects.requireNonNull(profession, "profession");
		rank = Objects.requireNonNull(rank, "rank");
		knownTechniques = CareerCodecs.copySet(knownTechniques, "known techniques", MAX_TECHNIQUES);
		demonstratedTechniques = CareerCodecs.copySet(
				demonstratedTechniques, "demonstrated techniques", MAX_TECHNIQUES);
		taughtTechniques = CareerCodecs.copySet(taughtTechniques, "taught techniques", MAX_TECHNIQUES);
		validatedProjects = CareerCodecs.copySet(validatedProjects, "validated projects", MAX_PROJECTS);
		home = Objects.requireNonNull(home, "home");
		workplace = Objects.requireNonNull(workplace, "workplace");
		mentor = Objects.requireNonNull(mentor, "mentor");
		apprentices = CareerCodecs.copySet(apprentices, "apprentices", MAX_APPRENTICES);
		status = Objects.requireNonNull(status, "status");
		deathCause = Objects.requireNonNull(deathCause, "death cause");

		if (!knownTechniques.containsAll(demonstratedTechniques)) {
			throw new IllegalArgumentException("Career demonstrated techniques must be known techniques.");
		}
		if (!knownTechniques.containsAll(taughtTechniques)) {
			throw new IllegalArgumentException("Career taught techniques must be known techniques.");
		}
		if (profession.isEmpty()) {
			if (rank != MasonRank.UNASSIGNED || !knownTechniques.isEmpty()
					|| !demonstratedTechniques.isEmpty() || !taughtTechniques.isEmpty()
					|| !validatedProjects.isEmpty() || home.isPresent() || workplace.isPresent()
					|| mentor.isPresent() || !apprentices.isEmpty() || status != CareerStatus.ACTIVE
					|| deathCause.isPresent()) {
				throw new IllegalArgumentException("An unassigned career cannot contain Mason state.");
			}
		} else if (rank == MasonRank.UNASSIGNED) {
			throw new IllegalArgumentException("An assigned career must have a Mason rank.");
		}
		if (status == CareerStatus.DEAD && deathCause.isEmpty()) {
			throw new IllegalArgumentException("A dead career must retain an attributable death cause.");
		}
		if (status != CareerStatus.DEAD && deathCause.isPresent()) {
			throw new IllegalArgumentException("Only a dead career may contain a death cause.");
		}
		if (status == CareerStatus.DEAD && !knownTechniques.equals(taughtTechniques)) {
			throw new IllegalArgumentException(
					"A dead career may retain only techniques that were institutionalised by teaching.");
		}
	}

	/** Safe state used when an entity has no career attachment yet. */
	public static CareerState empty() {
		return new CareerState(
				CURRENT_SCHEMA,
				Optional.empty(),
				MasonRank.UNASSIGNED,
				Set.of(),
				Set.of(),
				Set.of(),
				Set.of(),
				Optional.empty(),
				Optional.empty(),
				Optional.empty(),
				Set.of(),
				CareerStatus.ACTIVE,
				Optional.empty());
	}

	public static CareerState masonApprentice(Optional<UUID> home, Optional<UUID> workplace) {
		return new CareerState(
				CURRENT_SCHEMA,
				Optional.of(MasonIds.MASON),
				MasonRank.APPRENTICE,
				Set.of(),
				Set.of(),
				Set.of(),
				Set.of(),
				Objects.requireNonNull(home, "home"),
				Objects.requireNonNull(workplace, "workplace"),
				Optional.empty(),
				Set.of(),
				CareerStatus.ACTIVE,
				Optional.empty());
	}

	/** Convenience constructor for callers that do not yet have the evidence fields. */
	public CareerState(
			int schemaVersion,
			Optional<Identifier> profession,
			MasonRank rank,
			Set<Identifier> knownTechniques,
			Optional<UUID> home,
			Optional<UUID> workplace,
			Optional<UUID> mentor,
			Set<UUID> apprentices) {
		this(schemaVersion, profession, rank, knownTechniques, Set.of(), Set.of(), Set.of(), home,
				workplace, mentor, apprentices, CareerStatus.ACTIVE, Optional.empty());
	}

	/** Convenience constructor for a non-empty profession identifier. */
	public CareerState(
			int schemaVersion,
			Identifier profession,
			MasonRank rank,
			Set<Identifier> knownTechniques,
			Set<Identifier> demonstratedTechniques,
			Set<Identifier> taughtTechniques,
			Set<Identifier> validatedProjects,
			Optional<UUID> home,
			Optional<UUID> workplace,
			Optional<UUID> mentor,
			Set<UUID> apprentices,
			CareerStatus status,
			Optional<PhysicalDeathCause> deathCause) {
		this(schemaVersion, Optional.of(Objects.requireNonNull(profession, "profession")), rank,
				knownTechniques, demonstratedTechniques, taughtTechniques, validatedProjects, home,
				workplace, mentor, apprentices, status, deathCause);
	}

	public boolean isMason() {
		return profession.filter(MasonIds.MASON::equals).isPresent();
	}

	public CareerState withKnownTechnique(Identifier technique) {
		TreeSet<Identifier> next = new TreeSet<>(knownTechniques);
		next.add(Objects.requireNonNull(technique, "technique"));
		return copyWith(next, demonstratedTechniques, taughtTechniques, validatedProjects,
				rank, home, workplace, mentor, apprentices, status, deathCause);
	}

	public CareerState withDemonstratedTechnique(Identifier technique) {
		TreeSet<Identifier> next = new TreeSet<>(demonstratedTechniques);
		next.add(Objects.requireNonNull(technique, "technique"));
		return copyWith(knownTechniques, next, taughtTechniques, validatedProjects,
				rank, home, workplace, mentor, apprentices, status, deathCause);
	}

	public CareerState withTaughtTechnique(Identifier technique) {
		TreeSet<Identifier> next = new TreeSet<>(taughtTechniques);
		next.add(Objects.requireNonNull(technique, "technique"));
		return copyWith(knownTechniques, demonstratedTechniques, next, validatedProjects,
				rank, home, workplace, mentor, apprentices, status, deathCause);
	}

	public CareerState withValidatedProject(Identifier project) {
		TreeSet<Identifier> next = new TreeSet<>(validatedProjects);
		next.add(Objects.requireNonNull(project, "project"));
		return copyWith(knownTechniques, demonstratedTechniques, taughtTechniques, next,
				rank, home, workplace, mentor, apprentices, status, deathCause);
	}

	public CareerState withRank(MasonRank nextRank) {
		return copyWith(knownTechniques, demonstratedTechniques, taughtTechniques, validatedProjects,
				Objects.requireNonNull(nextRank, "rank"), home, workplace, mentor, apprentices, status, deathCause);
	}

	public CareerState withHome(Optional<UUID> nextHome) {
		return copyWith(knownTechniques, demonstratedTechniques, taughtTechniques, validatedProjects,
				rank, Objects.requireNonNull(nextHome, "home"), workplace, mentor, apprentices, status, deathCause);
	}

	public CareerState withWorkplace(Optional<UUID> nextWorkplace) {
		return copyWith(knownTechniques, demonstratedTechniques, taughtTechniques, validatedProjects,
				rank, home, Objects.requireNonNull(nextWorkplace, "workplace"), mentor, apprentices, status,
				deathCause);
	}

	public CareerState withMentor(Optional<UUID> nextMentor) {
		return copyWith(knownTechniques, demonstratedTechniques, taughtTechniques, validatedProjects,
				rank, home, workplace, Objects.requireNonNull(nextMentor, "mentor"), apprentices, status,
				deathCause);
	}

	public CareerState withApprentice(UUID apprentice) {
		TreeSet<UUID> next = new TreeSet<>(apprentices);
		next.add(Objects.requireNonNull(apprentice, "apprentice"));
		return copyWith(knownTechniques, demonstratedTechniques, taughtTechniques, validatedProjects,
				rank, home, workplace, mentor, next, status, deathCause);
	}

	/** Marks a physically caused death and clears untaught personal knowledge. */
	public CareerState withPhysicalDeath(PhysicalDeathCause cause) {
		TreeSet<Identifier> survivingDemonstrated = new TreeSet<>(demonstratedTechniques);
		survivingDemonstrated.retainAll(taughtTechniques);
		return new CareerState(
				CURRENT_SCHEMA,
				profession,
				rank,
				taughtTechniques,
				survivingDemonstrated,
				taughtTechniques,
				validatedProjects,
				home,
				workplace,
				mentor,
				apprentices,
				CareerStatus.DEAD,
				Optional.of(Objects.requireNonNull(cause, "cause")));
	}

	private CareerState copyWith(
			Set<Identifier> nextKnown,
			Set<Identifier> nextDemonstrated,
			Set<Identifier> nextTaught,
			Set<Identifier> nextProjects,
			MasonRank nextRank,
			Optional<UUID> nextHome,
			Optional<UUID> nextWorkplace,
			Optional<UUID> nextMentor,
			Set<UUID> nextApprentices,
			CareerStatus nextStatus,
			Optional<PhysicalDeathCause> nextCause) {
		return new CareerState(CURRENT_SCHEMA, profession, nextRank, nextKnown, nextDemonstrated,
				nextTaught, nextProjects, nextHome, nextWorkplace, nextMentor, nextApprentices,
				nextStatus, nextCause);
	}

	private static DataResult<CareerState> create(
			int schemaVersion,
			Optional<Identifier> profession,
			MasonRank rank,
			Set<Identifier> knownTechniques,
			Set<Identifier> demonstratedTechniques,
			Set<Identifier> taughtTechniques,
			Set<Identifier> validatedProjects,
			Optional<UUID> home,
			Optional<UUID> workplace,
			Optional<UUID> mentor,
			Set<UUID> apprentices,
			CareerStatus status,
			Optional<PhysicalDeathCause> deathCause) {
		try {
			return DataResult.success(new CareerState(schemaVersion, profession, rank, knownTechniques,
					demonstratedTechniques, taughtTechniques, validatedProjects, home, workplace, mentor,
					apprentices, status, deathCause));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	private static DataResult<CareerState> migrate(CareerState value, int storedVersion, int targetVersion) {
		if (storedVersion == 1 && targetVersion == CURRENT_SCHEMA) {
			return DataResult.success(new CareerState(
					CURRENT_SCHEMA,
					value.profession(),
					value.rank(),
					value.knownTechniques(),
					Set.of(),
					Set.of(),
					Set.of(),
					value.home(),
					value.workplace(),
					value.mentor(),
					value.apprentices(),
					CareerStatus.ACTIVE,
					Optional.empty()));
		}
		return DataResult.error(() -> "No career migration exists from schema " + storedVersion
				+ " to schema " + targetVersion + ".");
	}
}

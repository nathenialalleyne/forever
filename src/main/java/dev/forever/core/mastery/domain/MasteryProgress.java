package dev.forever.core.mastery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Permanent evidence and derived rank for one mastery.
 *
 * <p>Evidence and event identities are sets. Replaying the same observation cannot create
 * additional progress, and ten thousand repetitions of one observation remain one key.
 */
public record MasteryProgress(
		int rank,
		Set<String> unlockedMilestones,
		Set<String> evidenceKeys,
		Set<String> eventIds) {

	private static final int MAX_STORED_KEYS = 1024;

	private record Encoded(
			int rank,
			List<String> unlockedMilestones,
			List<String> evidenceKeys,
			List<String> eventIds) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("rank").forGetter(Encoded::rank),
			Codec.sizeLimitedString(256).listOf().fieldOf("unlocked_milestones")
					.forGetter(Encoded::unlockedMilestones),
			Codec.sizeLimitedString(256).listOf().fieldOf("evidence_keys")
					.forGetter(Encoded::evidenceKeys),
			Codec.sizeLimitedString(256).listOf().fieldOf("event_ids")
					.forGetter(Encoded::eventIds)
		).apply(instance, Encoded::new));

	/** Codec for validated permanent mastery progress. */
	public static final Codec<MasteryProgress> CODEC = RAW_CODEC.flatXmap(
			encoded -> create(
					encoded.rank(),
					encoded.unlockedMilestones(),
					encoded.evidenceKeys(),
					encoded.eventIds()),
			progress -> DataResult.success(new Encoded(
				progress.rank(),
				progress.unlockedMilestones().stream().sorted().toList(),
				progress.evidenceKeys().stream().sorted().toList(),
				progress.eventIds().stream().sorted().toList())));

	public MasteryProgress {
		if (rank < 0) {
			throw new IllegalArgumentException("Mastery rank cannot be negative.");
		}
		unlockedMilestones = immutableSet(unlockedMilestones, "unlocked milestones");
		evidenceKeys = immutableSet(evidenceKeys, "evidence keys");
		eventIds = immutableSet(eventIds, "event IDs");
		if (unlockedMilestones.size() > MAX_STORED_KEYS
				|| evidenceKeys.size() > MAX_STORED_KEYS
				|| eventIds.size() > MAX_STORED_KEYS) {
			throw new IllegalArgumentException(
					"Mastery progress exceeds the bounded persistent evidence capacity of "
							+ MAX_STORED_KEYS + " entries per set.");
		}
	}

	public static MasteryProgress empty() {
		return new MasteryProgress(0, Set.of(), Set.of(), Set.of());
	}

	public MasteryProgress withEvidence(String evidenceKey, String eventId) {
		Set<String> nextEvidence = new HashSet<>(evidenceKeys);
		nextEvidence.add(requireKey(evidenceKey, "evidence key"));
		Set<String> nextEvents = new HashSet<>(eventIds);
		nextEvents.add(requireKey(eventId, "event ID"));
		return new MasteryProgress(rank, unlockedMilestones, nextEvidence, nextEvents);
	}

	public MasteryProgress withEventId(String eventId) {
		Set<String> nextEvents = new HashSet<>(eventIds);
		nextEvents.add(requireKey(eventId, "event ID"));
		return new MasteryProgress(rank, unlockedMilestones, evidenceKeys, nextEvents);
	}

	public MasteryProgress withMilestones(Set<String> milestoneIds, int newRank) {
		Set<String> nextMilestones = new HashSet<>(unlockedMilestones);
		nextMilestones.addAll(milestoneIds);
		return new MasteryProgress(Math.max(rank, newRank), nextMilestones, evidenceKeys, eventIds);
	}

	private static DataResult<MasteryProgress> create(
			int rank,
			List<String> unlockedMilestones,
			List<String> evidenceKeys,
			List<String> eventIds) {
		if (hasDuplicates(unlockedMilestones)) {
			return DataResult.error(() -> "Mastery progress contains a duplicate unlocked milestone.");
		}
		if (hasDuplicates(evidenceKeys)) {
			return DataResult.error(() -> "Mastery progress contains a duplicate normalized evidence key.");
		}
		if (hasDuplicates(eventIds)) {
			return DataResult.error(() -> "Mastery progress contains a duplicate event ID.");
		}
		try {
			return DataResult.success(new MasteryProgress(
					rank,
					new HashSet<>(unlockedMilestones),
					new HashSet<>(evidenceKeys),
					new HashSet<>(eventIds)));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	private static Set<String> immutableSet(Set<String> values, String name) {
		Objects.requireNonNull(values, name);
		Set<String> copy = new HashSet<>();
		for (String value : values) {
			copy.add(requireKey(value, name));
		}
		return Set.copyOf(copy);
	}

	private static String requireKey(String value, String name) {
		if (value == null || value.isBlank() || value.length() > 256) {
			throw new IllegalArgumentException("Mastery " + name + " must be non-blank and at most 256 characters.");
		}
		return value;
	}

	private static boolean hasDuplicates(List<String> values) {
		return values.size() != new HashSet<>(values).size();
	}
}

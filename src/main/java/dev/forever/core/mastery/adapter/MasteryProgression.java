package dev.forever.core.mastery;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Applies bounded, data-driven evidence to immutable mastery state.
 *
 * <p>Every accepted source is matched to a named milestone in the registry. No generic
 * action counter exists, and duplicate evidence keys cannot advance a rank.
 */
public final class MasteryProgression {

	private MasteryProgression() {
	}

	/** Evaluates one event without touching Minecraft state. */
	public static ProgressionResult apply(
			MasteryState state,
			MasteryRegistry registry,
			Identifier masteryId,
			ProgressionEvidence evidence) {
		Objects.requireNonNull(state, "state");
		Objects.requireNonNull(registry, "registry");
		Objects.requireNonNull(masteryId, "masteryId");
		Objects.requireNonNull(evidence, "evidence");

		MasteryDefinition definition = registry.definition(masteryId).orElse(null);
		if (definition == null) {
			return rejected(state, masteryId, "mastery.forever.progression.unknown_mastery");
		}
		MasteryProgress current = state.progressFor(masteryId);
		if (current.eventIds().contains(evidence.eventId())) {
			return new ProgressionResult(
					true,
					true,
					state,
					current.rank(),
					Set.of(),
					"mastery.forever.progression.duplicate_event",
					"mastery.forever.progression.duplicate_event");
		}

		List<ProgressionMilestone> matching = definition.matching(evidence.kind(), evidence.dimension());
		if (matching.isEmpty()) {
			return rejected(state, masteryId, "mastery.forever.progression.unapproved_source");
		}

		String normalizedKey = evidence.normalizedEvidenceKey();
		MasteryProgress nextProgress;
		boolean duplicateEvidence = current.evidenceKeys().contains(normalizedKey);
		if (duplicateEvidence) {
			nextProgress = current.withEventId(evidence.eventId());
		} else {
			nextProgress = current.withEvidence(normalizedKey, evidence.eventId());
		}

		Set<String> newlyUnlocked = new HashSet<>();
		if (!duplicateEvidence) {
			long distinctMatchingEvidence = nextProgress.evidenceKeys().stream()
					.filter(key -> key.startsWith(evidence.kind().serializedName() + ":"
							+ evidence.dimension() + ":"))
					.count();
			for (ProgressionMilestone milestone : matching) {
				if (distinctMatchingEvidence >= milestone.threshold()
						&& !current.unlockedMilestones().contains(milestone.id())) {
					newlyUnlocked.add(milestone.id());
				}
			}
		}

		int derivedRank = nextProgress.rank();
		Set<String> allUnlocked = new HashSet<>(current.unlockedMilestones());
		allUnlocked.addAll(newlyUnlocked);
		for (ProgressionMilestone milestone : definition.milestones()) {
			if (allUnlocked.contains(milestone.id())) {
				derivedRank = Math.max(derivedRank, milestone.rank());
			}
		}
		nextProgress = nextProgress.withMilestones(newlyUnlocked, derivedRank);
		MasteryState nextState = state.withProgress(masteryId, nextProgress);
		String explanationKey = newlyUnlocked.stream()
				.sorted()
				.findFirst()
				.flatMap(definition::milestone)
				.map(ProgressionMilestone::explanationKey)
				.orElse("mastery.forever.progression.evidence_recorded");
		return new ProgressionResult(
				true,
				duplicateEvidence,
				nextState,
				nextProgress.rank(),
				newlyUnlocked,
				duplicateEvidence
						? "mastery.forever.progression.duplicate_evidence"
						: "",
				explanationKey);
	}

	/** Applies an event to a server player using the currently loaded server registry. */
	public static ProgressionResult record(
			ServerPlayer player,
			Identifier masteryId,
			ProgressionEvidence evidence) {
		Objects.requireNonNull(player, "player");
		MasteryRegistry registry = MasteryRegistryAccess.requireCurrent();
		synchronized (player) {
			MasteryState current = player.getAttachedOrCreate(MasteryAttachment.TYPE, MasteryState::empty);
			ProgressionResult result = apply(current, registry, masteryId, evidence);
			if (!result.state().equals(current)) {
				player.setAttached(MasteryAttachment.TYPE, result.state());
			}
			return result;
		}
	}

	/** Convenience entry point for the first concrete mastery prototype. */
	public static ProgressionResult recordProspector(
			ServerPlayer player, ProgressionEvidence evidence) {
		return record(player, MasteryIds.PROSPECTOR, evidence);
	}

	private static ProgressionResult rejected(
			MasteryState state, Identifier masteryId, String reasonKey) {
		return new ProgressionResult(
				false,
				false,
				state,
				state.progressFor(masteryId).rank(),
				Set.of(),
				reasonKey,
				"");
	}
}

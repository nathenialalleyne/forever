package dev.forever.core.career;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.npc.villager.Villager;

/** Atomic mentor/apprentice teaching operations. */
public final class CareerTeachingService {

	private CareerTeachingService() {
	}

	public static CareerTeachingResult teach(
			CareerState mentor,
			CareerState apprentice,
			UUID mentorId,
			UUID apprenticeId,
			Identifier technique) {
		Objects.requireNonNull(mentor, "mentor");
		Objects.requireNonNull(apprentice, "apprentice");
		Objects.requireNonNull(mentorId, "mentorId");
		Objects.requireNonNull(apprenticeId, "apprenticeId");
		Objects.requireNonNull(technique, "technique");
		if (mentorId.equals(apprenticeId)) {
			return rejected(mentor, apprentice, mentorId, apprenticeId, technique,
					"A villager cannot mentor itself.");
		}
		if (!mentor.isMason() || !apprentice.isMason()
				|| mentor.status() != CareerStatus.ACTIVE || apprentice.status() != CareerStatus.ACTIVE) {
			return rejected(mentor, apprentice, mentorId, apprenticeId, technique,
					"Mentor and apprentice must both be active Masons.");
		}
		if (!mentor.rank().canTeach()) {
			return rejected(mentor, apprentice, mentorId, apprenticeId, technique,
					"Only a Journeyman or Master may teach institutional knowledge.");
		}
		if (mentor.workplace().isEmpty() || !mentor.workplace().equals(apprentice.workplace())) {
			return rejected(mentor, apprentice, mentorId, apprenticeId, technique,
					"Mentor and apprentice must share a validated workplace assignment.");
		}
		if (!mentor.knownTechniques().contains(technique)) {
			return rejected(mentor, apprentice, mentorId, apprenticeId, technique,
					"The mentor cannot teach a technique they do not know.");
		}
		if (apprentice.mentor().map(existing -> !existing.equals(mentorId)).orElse(false)) {
			return rejected(mentor, apprentice, mentorId, apprenticeId, technique,
					"The apprentice already has a different mentor.");
		}
		if (apprentice.knownTechniques().contains(technique)) {
			return rejected(mentor, apprentice, mentorId, apprenticeId, technique,
					"The apprentice already knows this technique; the request was not replayed.");
		}
		CareerState nextMentor = mentor.withTaughtTechnique(technique).withApprentice(apprenticeId);
		CareerState nextApprentice = apprentice.withKnownTechnique(technique).withMentor(Optional.of(mentorId));
		return new CareerTeachingResult(true, nextMentor, nextApprentice, mentorId, apprenticeId, technique,
				Set.of(technique), "The mentor taught one technique and the apprentice recorded its provenance.");
	}

	/** Applies both attachment revisions while taking locks in stable UUID order. */
	public static CareerTeachingResult teach(Villager mentor, Villager apprentice, Identifier technique) {
		Objects.requireNonNull(mentor, "mentor");
		Objects.requireNonNull(apprentice, "apprentice");
		Objects.requireNonNull(technique, "technique");
		Villager first = mentor.getUUID().compareTo(apprentice.getUUID()) <= 0 ? mentor : apprentice;
		Villager second = first == mentor ? apprentice : mentor;
		synchronized (first) {
			synchronized (second) {
				CareerTeachingResult result = teach(
						MasonCareerService.stateOf(mentor),
						MasonCareerService.stateOf(apprentice),
						mentor.getUUID(),
						apprentice.getUUID(),
						technique);
				if (result.applied()) {
					MasonCareerService.setState(mentor, result.mentor());
					MasonCareerService.setState(apprentice, result.apprentice());
				}
				return result;
			}
		}
	}

	private static CareerTeachingResult rejected(
			CareerState mentor,
			CareerState apprentice,
			UUID mentorId,
			UUID apprenticeId,
			Identifier technique,
			String message) {
		return new CareerTeachingResult(false, mentor, apprentice, mentorId, apprenticeId, technique, Set.of(), message);
	}
}

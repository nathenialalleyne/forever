package dev.forever.core.career;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.Identifier;

/** Result of one atomic mentor-to-apprentice technique transfer. */
public record CareerTeachingResult(
		boolean applied,
		CareerState mentor,
		CareerState apprentice,
		UUID mentorId,
		UUID apprenticeId,
		Identifier technique,
		Set<Identifier> institutionalisedTechniques,
		String message) {

	public CareerTeachingResult {
		Objects.requireNonNull(mentor, "mentor");
		Objects.requireNonNull(apprentice, "apprentice");
		Objects.requireNonNull(mentorId, "mentorId");
		Objects.requireNonNull(apprenticeId, "apprenticeId");
		Objects.requireNonNull(technique, "technique");
		institutionalisedTechniques = Set.copyOf(Objects.requireNonNull(institutionalisedTechniques,
				"institutionalised techniques"));
		message = CareerCodecs.requiredText(message, "teaching result", 512);
	}
}

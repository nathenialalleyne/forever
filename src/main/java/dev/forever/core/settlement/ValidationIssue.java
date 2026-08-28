package dev.forever.core.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;

/** One precise functional reason why a server validation did not pass. */
public record ValidationIssue(FunctionalRequirement requirement, String message) {

	public static final Codec<ValidationIssue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			FunctionalRequirement.CODEC.fieldOf("requirement").forGetter(ValidationIssue::requirement),
			Codec.STRING.fieldOf("message").forGetter(ValidationIssue::message)
	).apply(instance, ValidationIssue::new));

	public ValidationIssue {
		Objects.requireNonNull(requirement, "requirement");
		message = SettlementCodecs.requiredText(message, "validation issue message", 512);
	}
}

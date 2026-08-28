package dev.forever.core.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;

/** One precise functional reason why a server validation did not pass. */
public record ValidationIssue(FunctionalRequirement requirement, String message) {

	private record Encoded(FunctionalRequirement requirement, String message) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			FunctionalRequirement.CODEC.fieldOf("requirement").forGetter(Encoded::requirement),
			Codec.STRING.fieldOf("message").forGetter(Encoded::message)
	).apply(instance, Encoded::new));

	public static final Codec<ValidationIssue> CODEC = RAW_CODEC.flatXmap(
		encoded -> create(encoded.requirement(), encoded.message()),
		issue -> DataResult.success(new Encoded(issue.requirement(), issue.message())));

	public ValidationIssue {
		Objects.requireNonNull(requirement, "requirement");
		message = SettlementCodecs.requiredText(message, "validation issue message", 512);
	}

	private static DataResult<ValidationIssue> create(
			FunctionalRequirement requirement, String message) {
		try {
			return DataResult.success(new ValidationIssue(requirement, message));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}
}

package dev.forever.core.settlement.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;

/** Immutable cached result of a bounded server-side functional validation. */
public record ValidationResult(ValidationStatus status, int revision, List<ValidationIssue> issues) {

	private static final int MAX_ISSUES = 16;

	private record Encoded(ValidationStatus status, int revision, List<ValidationIssue> issues) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ValidationStatus.CODEC.fieldOf("status").forGetter(Encoded::status),
			Codec.INT.fieldOf("revision").forGetter(Encoded::revision),
			SettlementCodecs.boundedList(ValidationIssue.CODEC, MAX_ISSUES, "validation issues")
					.fieldOf("issues").forGetter(Encoded::issues)
	).apply(instance, Encoded::new));

	public static final Codec<ValidationResult> CODEC = RAW_CODEC.flatXmap(
			encoded -> create(encoded.status(), encoded.revision(), encoded.issues()),
			validation -> DataResult.success(new Encoded(
					validation.status(), validation.revision(), validation.issues())));

	public ValidationResult {
		Objects.requireNonNull(status, "status");
		if (revision < 0) {
			throw new IllegalArgumentException("Settlement validation revision cannot be negative.");
		}
		Objects.requireNonNull(issues, "issues");
		if (issues.size() > MAX_ISSUES) {
			throw new IllegalArgumentException("Settlement validation has too many issues.");
		}
		issues = List.copyOf(issues);
		for (ValidationIssue issue : issues) {
			Objects.requireNonNull(issue, "validation issue");
		}
		if (status == ValidationStatus.INVALID && issues.isEmpty()) {
			throw new IllegalArgumentException("An invalid settlement validation needs a reason.");
		}
		if (status != ValidationStatus.INVALID && !issues.isEmpty()) {
			throw new IllegalArgumentException("Only invalid settlement validations may contain issues.");
		}
	}

	public static ValidationResult pending() {
		return new ValidationResult(ValidationStatus.PENDING, 0, List.of());
	}

	public static ValidationResult valid(int revision) {
		return new ValidationResult(ValidationStatus.VALID, revision, List.of());
	}

	public static ValidationResult invalid(int revision, List<ValidationIssue> issues) {
		return new ValidationResult(ValidationStatus.INVALID, revision, issues);
	}

	public boolean valid() {
		return status == ValidationStatus.VALID;
	}

	public List<String> messages() {
		return issues.stream().map(ValidationIssue::message).toList();
	}

	static DataResult<ValidationResult> create(
			ValidationStatus status, int revision, List<ValidationIssue> issues) {
		try {
			return DataResult.success(new ValidationResult(status, revision, issues));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}
}

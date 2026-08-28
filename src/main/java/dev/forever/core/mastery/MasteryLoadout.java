package dev.forever.core.mastery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;

/** Immutable Focus and Supporting assignment. */
public record MasteryLoadout(Optional<Identifier> focus, List<Identifier> supporting) {

	private static final int MAX_SUPPORTING_ASSIGNMENTS = 2;

	private record Encoded(Optional<Identifier> focus, List<Identifier> supporting) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.optionalFieldOf("focus").forGetter(Encoded::focus),
			Identifier.CODEC.listOf().fieldOf("supporting").forGetter(Encoded::supporting)
		).apply(instance, Encoded::new));

	/** Codec for structurally valid assignments. Registry and balance validation happens separately. */
	public static final Codec<MasteryLoadout> CODEC = RAW_CODEC.flatXmap(
			encoded -> create(encoded.focus(), encoded.supporting()),
			loadout -> DataResult.success(new Encoded(loadout.focus(), loadout.supporting())));

	public MasteryLoadout {
		focus = Objects.requireNonNull(focus, "focus");
		Objects.requireNonNull(supporting, "supporting");
		List<Identifier> supportingCopy = new ArrayList<>(supporting);
		for (Identifier id : supportingCopy) {
			if (id == null) {
				throw new IllegalArgumentException("Supporting assignments cannot contain null mastery IDs.");
			}
		}
		supporting = List.copyOf(supportingCopy);
		if (focus.isEmpty() && !supporting.isEmpty()) {
			throw new IllegalArgumentException("A loadout without a Focus cannot have Supporting assignments.");
		}
		if (supporting.size() > MAX_SUPPORTING_ASSIGNMENTS) {
			throw new IllegalArgumentException(
					"A loadout may assign at most " + MAX_SUPPORTING_ASSIGNMENTS
							+ " Supporting masteries, found " + supporting.size() + ".");
		}
		Set<Identifier> unique = new HashSet<>();
		for (Identifier id : supporting) {
			if (!unique.add(id)) {
				throw new IllegalArgumentException("Supporting mastery '" + id + "' is assigned more than once.");
			}
			if (focus.isPresent() && focus.orElseThrow().equals(id)) {
				throw new IllegalArgumentException(
						"Mastery '" + id + "' cannot be assigned to both Focus and Supporting.");
			}
		}
	}

	public static MasteryLoadout empty() {
		return new MasteryLoadout(Optional.empty(), List.of());
	}

	private static DataResult<MasteryLoadout> create(
			Optional<Identifier> focus, List<Identifier> supporting) {
		try {
			return DataResult.success(new MasteryLoadout(focus, supporting));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	/**
	 * Validates an assignment against the data-loaded slot count and known mastery IDs.
	 */
	public DataResult<MasteryLoadout> validate(LoadoutRules rules, Set<Identifier> knownMasteries) {
		Objects.requireNonNull(rules, "rules");
		Objects.requireNonNull(knownMasteries, "knownMasteries");
		DataResult<LoadoutRules> rulesResult = rules.validate();
		if (rulesResult.error().isPresent()) {
			return DataResult.error(rulesResult.error().orElseThrow()::message);
		}
		if (focus.isEmpty()) {
			return DataResult.error(() -> "An active mastery loadout must assign exactly one Focus mastery.");
		}
		if (supporting.size() > rules.supportingSlots()) {
			return DataResult.error(() -> "A mastery loadout may assign at most "
					+ rules.supportingSlots() + " Supporting masteries, found " + supporting.size() + ".");
		}
		Identifier focusId = focus.orElseThrow();
		if (!knownMasteries.contains(focusId)) {
			return DataResult.error(() -> "Focus mastery '" + focusId + "' is not present in the loaded mastery registry.");
		}
		for (Identifier id : supporting) {
			if (!knownMasteries.contains(id)) {
				return DataResult.error(() -> "Supporting mastery '" + id
						+ "' is not present in the loaded mastery registry.");
			}
		}
		return DataResult.success(this);
	}

	/** Validates only the active-slot contract, without requiring registry definitions. */
	public DataResult<MasteryLoadout> validate(LoadoutRules rules) {
		Set<Identifier> assignments = new HashSet<>(supporting);
		focus.ifPresent(assignments::add);
		return validate(rules, assignments);
	}
}

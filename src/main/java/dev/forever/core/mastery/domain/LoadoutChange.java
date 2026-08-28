package dev.forever.core.mastery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;

/** Persisted explanation of the last successful, gated loadout change. */
public record LoadoutChange(SwitchContext context, long changedAt) {

	private record Encoded(SwitchContext context, long changedAt) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SwitchContext.CODEC.fieldOf("context").forGetter(Encoded::context),
			Codec.LONG.fieldOf("changed_at").forGetter(Encoded::changedAt)
		).apply(instance, Encoded::new));

	/** Codec for the persisted switching context. */
	public static final Codec<LoadoutChange> CODEC = RAW_CODEC.flatXmap(
			encoded -> create(encoded.context(), encoded.changedAt()),
			change -> DataResult.success(new Encoded(change.context(), change.changedAt())));

	public LoadoutChange {
		context = Objects.requireNonNull(context, "context");
		if (!context.satisfiesSwitchGate()) {
			throw new IllegalArgumentException(
					"A persisted loadout change must record a rest, home, or workplace context.");
		}
		if (changedAt < 0) {
			throw new IllegalArgumentException("A persisted loadout change cannot have a negative timestamp.");
		}
	}

	private static DataResult<LoadoutChange> create(SwitchContext context, long changedAt) {
		try {
			return DataResult.success(new LoadoutChange(context, changedAt));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}
}

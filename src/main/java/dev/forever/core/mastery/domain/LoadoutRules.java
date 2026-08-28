package dev.forever.core.mastery.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Balance data for the active mastery slots. The accepted ADR shape is one Focus and
 * no more than two Supporting assignments.
 */
public record LoadoutRules(int focusSlots, int supportingSlots) {

	private record Encoded(int focusSlots, int supportingSlots) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("focus_slots").forGetter(Encoded::focusSlots),
			Codec.INT.fieldOf("supporting_slots").forGetter(Encoded::supportingSlots)
		).apply(instance, Encoded::new));

	/** Codec for validated loadout balance data. */
	public static final Codec<LoadoutRules> CODEC = RAW_CODEC.flatXmap(
			encoded -> create(encoded.focusSlots(), encoded.supportingSlots()),
			rules -> DataResult.success(new Encoded(rules.focusSlots(), rules.supportingSlots())));

	private static DataResult<LoadoutRules> create(int focusSlots, int supportingSlots) {
		LoadoutRules rules = new LoadoutRules(focusSlots, supportingSlots);
		return rules.validate();
	}

	/** Validates the locked Focus/Supporting contract. */
	public DataResult<LoadoutRules> validate() {
		if (focusSlots != 1) {
			return DataResult.error(() -> "Mastery loadout balance must define exactly one Focus slot, found "
					+ focusSlots + ".");
		}
		if (supportingSlots < 0 || supportingSlots > 2) {
			return DataResult.error(() -> "Mastery loadout balance must define between zero and two Supporting slots, found "
					+ supportingSlots + ".");
		}
		return DataResult.success(this);
	}
}

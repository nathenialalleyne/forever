package dev.forever.core.mastery.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

/** Server-observed contexts in which a loadout change is allowed. */
public enum SwitchContext {
	NONE("none", false),
	REST("rest", true),
	HOME("home", true),
	WORKPLACE("workplace", true);

	public static final Codec<SwitchContext> CODEC = Codec.STRING.comapFlatMap(
			value -> fromSerialized(value), SwitchContext::serializedName);

	private final String serializedName;
	private final boolean gateSatisfied;

	SwitchContext(String serializedName, boolean gateSatisfied) {
		this.serializedName = serializedName;
		this.gateSatisfied = gateSatisfied;
	}

	public String serializedName() {
		return serializedName;
	}

	public boolean satisfiesSwitchGate() {
		return gateSatisfied;
	}

	private static DataResult<SwitchContext> fromSerialized(String value) {
		String normalised = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
		for (SwitchContext context : values()) {
			if (context.serializedName.equals(normalised)) {
				return DataResult.success(context);
			}
		}
		return DataResult.error(() -> "Unknown mastery loadout switch context '" + value
				+ "'. Expected none, rest, home, or workplace.");
	}
}

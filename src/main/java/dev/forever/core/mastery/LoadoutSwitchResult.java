package dev.forever.core.mastery;

/** Immutable outcome of a server-authoritative loadout request. */
public record LoadoutSwitchResult(
		boolean applied,
		MasteryState state,
		String reasonKey,
		String detail) {

	public static LoadoutSwitchResult rejected(
			MasteryState state, String reasonKey, String detail) {
		return new LoadoutSwitchResult(false, state, reasonKey, detail);
	}
}

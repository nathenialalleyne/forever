package dev.forever.core.economy;

import java.util.Objects;
import java.util.Optional;

/** Publishes one immutable economy balance snapshot after a server data reload. */
public final class EconomyBalanceAccess {

	private static volatile EconomyBalance current;

	private EconomyBalanceAccess() {
	}

	public static void install(EconomyBalance balance) {
		current = Objects.requireNonNull(balance, "balance");
	}

	public static Optional<EconomyBalance> current() {
		return Optional.ofNullable(current);
	}

	public static EconomyBalance requireCurrent() {
		EconomyBalance balance = current;
		if (balance == null) {
			throw new IllegalStateException("Economy balance is not loaded. The server must complete its data reload first.");
		}
		return balance;
	}
}

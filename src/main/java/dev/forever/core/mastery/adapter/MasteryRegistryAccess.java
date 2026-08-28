package dev.forever.core.mastery.adapter;

import java.util.Objects;
import java.util.Optional;

/**
 * Publishes the latest immutable server-data registry snapshot.
 *
 * <p>This contains definitions only, not player or world state. Player operations still
 * accept an explicit snapshot in their pure overloads so tests and integrations cannot
 * accidentally couple player state to a mutable global manager.
 */
public final class MasteryRegistryAccess {

	private static volatile MasteryRegistry current;

	private MasteryRegistryAccess() {
	}

	public static void install(MasteryRegistry registry) {
		current = Objects.requireNonNull(registry, "registry");
	}

	public static Optional<MasteryRegistry> current() {
		return Optional.ofNullable(current);
	}

	public static MasteryRegistry requireCurrent() {
		MasteryRegistry registry = current;
		if (registry == null) {
			throw new IllegalStateException("Mastery definitions are not loaded."
					+ " The server must complete its data reload before player mastery operations run.");
		}
		return registry;
	}
}

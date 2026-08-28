package dev.forever.core.career;

import java.util.Objects;
import java.util.Optional;

/** Publishes the latest immutable Mason server-data snapshot after a data reload. */
public final class MasonDataRegistry {

	private static volatile MasonData current;

	private MasonDataRegistry() {
	}

	public static void install(MasonData data) {
		current = Objects.requireNonNull(data, "data");
	}

	public static Optional<MasonData> current() {
		return Optional.ofNullable(current);
	}

	public static MasonData requireCurrent() {
		MasonData data = current;
		if (data == null) {
			throw new IllegalStateException("Mason data is not loaded. The server must complete its data reload first.");
		}
		return data;
	}
}

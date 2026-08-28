package dev.forever.core.guide;

import java.util.Objects;
import java.util.Optional;

/** Atomic access point for the latest server resource-reload snapshot. */
public final class GuideRegistryAccess {

	private static volatile GuideRegistry current;

	private GuideRegistryAccess() {
	}

	public static void install(GuideRegistry registry) {
		current = Objects.requireNonNull(registry, "guide registry must not be null");
	}

	public static Optional<GuideRegistry> current() {
		return Optional.ofNullable(current);
	}

	public static GuideRegistry requireCurrent() {
		GuideRegistry registry = current;
		if (registry == null) {
			throw new IllegalStateException("Field Guide entries are not loaded. The server must complete its data reload.");
		}
		return registry;
	}
}

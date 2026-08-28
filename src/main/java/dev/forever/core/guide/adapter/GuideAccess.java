package dev.forever.core.guide;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.resources.Identifier;

/**
 * Immutable server-side knowledge context used to annotate optional guide gating.
 *
 * <p>Gating never removes an instruction from the registry. It only lets a caller
 * distinguish an entry that has been discovered from one that has not, preserving the
 * Field Guide rule that known instructions remain inspectable.
 */
public record GuideAccess(Set<Identifier> completedEntryIds, Set<String> discoveredKeys) {

	public static final int MAX_COMPLETED_ENTRIES = 512;
	public static final int MAX_DISCOVERED_KEYS = 512;
	public static final int MAX_DISCOVERY_KEY_LENGTH = 64;

	public GuideAccess {
		Objects.requireNonNull(completedEntryIds, "completed guide entries must not be null");
		Objects.requireNonNull(discoveredKeys, "discovered guide keys must not be null");
		if (completedEntryIds.size() > MAX_COMPLETED_ENTRIES) {
			throw new IllegalArgumentException("Completed guide entries exceed the maximum of "
					+ MAX_COMPLETED_ENTRIES + ".");
		}
		if (discoveredKeys.size() > MAX_DISCOVERED_KEYS) {
			throw new IllegalArgumentException("Discovered guide keys exceed the maximum of "
					+ MAX_DISCOVERED_KEYS + ".");
		}
		TreeSet<Identifier> completedCopy = new TreeSet<>();
		for (Identifier id : completedEntryIds) {
			if (id == null) {
				throw new IllegalArgumentException("Completed guide entries cannot contain null IDs.");
			}
			completedCopy.add(id);
		}
		TreeSet<String> discoveredCopy = new TreeSet<>();
		for (String key : discoveredKeys) {
			if (key == null || key.isBlank() || key.length() > MAX_DISCOVERY_KEY_LENGTH
					|| !key.matches("[a-z0-9][a-z0-9_.-]*")) {
				throw new IllegalArgumentException("Guide discovery keys must be bounded lowercase identifiers.");
			}
			discoveredCopy.add(key);
		}
		completedEntryIds = Collections.unmodifiableSet(completedCopy);
		discoveredKeys = Collections.unmodifiableSet(discoveredCopy);
	}

	public static GuideAccess empty() {
		return new GuideAccess(Set.of(), Set.of());
	}

	public boolean completed(Identifier id) {
		return id != null && completedEntryIds.contains(id);
	}

	public boolean discovered(String key) {
		return key != null && discoveredKeys.contains(key);
	}
}

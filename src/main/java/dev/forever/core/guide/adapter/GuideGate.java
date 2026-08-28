package dev.forever.core.guide.adapter;

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

/** Optional, descriptive knowledge gate attached to a Field Guide entry. */
public record GuideGate(List<Identifier> prerequisiteEntryIds, Optional<String> discoveryKey) {

	public static final int MAX_PREREQUISITES = 8;

	private record Encoded(List<Identifier> prerequisiteEntryIds, Optional<String> discoveryKey) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.listOf().optionalFieldOf("prerequisite_entry_ids", List.of())
					.forGetter(Encoded::prerequisiteEntryIds),
				Codec.sizeLimitedString(64).optionalFieldOf("discovery_key")
					.forGetter(Encoded::discoveryKey)
		).apply(instance, Encoded::new));

	/** Codec for validated gate metadata in a guide JSON document. */
	public static final Codec<GuideGate> CODEC = RAW_CODEC.flatXmap(
			encoded -> {
				try {
					return DataResult.success(new GuideGate(encoded.prerequisiteEntryIds(), encoded.discoveryKey()));
				} catch (IllegalArgumentException exception) {
					return DataResult.error(exception::getMessage);
				}
			},
			gate -> DataResult.success(new Encoded(gate.prerequisiteEntryIds(), gate.discoveryKey())));

	public GuideGate {
		Objects.requireNonNull(prerequisiteEntryIds, "guide prerequisites must not be null");
		Objects.requireNonNull(discoveryKey, "guide discovery key must not be null");
		if (prerequisiteEntryIds.size() > MAX_PREREQUISITES) {
			throw new IllegalArgumentException("A guide gate may reference at most " + MAX_PREREQUISITES
					+ " prerequisite entries.");
		}
		Set<Identifier> seen = new HashSet<>();
		List<Identifier> sorted = new ArrayList<>(prerequisiteEntryIds.size());
		for (Identifier id : prerequisiteEntryIds) {
			if (id == null || !GuideEntry.isGuideIdentifier(id)) {
				throw new IllegalArgumentException("Guide gate prerequisites must be Forever guide IDs.");
			}
			if (!seen.add(id)) {
				throw new IllegalArgumentException("Guide gate prerequisites must not contain duplicate IDs.");
			}
			sorted.add(id);
		}
		sorted.sort(Identifier::compareTo);
		String discovery = discoveryKey.orElse(null);
		if (discovery != null && (discovery.isBlank() || discovery.length() > 64
				|| !discovery.matches("[a-z0-9][a-z0-9_.-]*"))) {
			throw new IllegalArgumentException("Guide discovery_key must be a bounded lowercase identifier.");
		}
		prerequisiteEntryIds = List.copyOf(sorted);
	}

	public boolean satisfiedBy(GuideAccess access) {
		Objects.requireNonNull(access, "guide access must not be null");
		return prerequisiteEntryIds.stream().allMatch(access::completed)
				&& discoveryKey.map(access::discovered).orElse(true);
	}
}

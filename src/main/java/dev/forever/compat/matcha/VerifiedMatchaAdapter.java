package dev.forever.compat.matcha;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Adapter implementation enabled only after the pinned 1.12 profile is verified. */
public final class VerifiedMatchaAdapter implements MatchaAdapter {
	private static final Map<IdentityKey, IdentityMapping> ITEM_MAPPINGS = Map.of(
			new IdentityKey("minecraft:poisonous_potato", "minecraft:heart_container"),
					new IdentityMapping("forever.item.heart_container", "matcha-audit-1.12-item-001"),
			new IdentityKey("minecraft:chicken_spawn_egg", "minecraft:beacon_kindling"),
					new IdentityMapping("forever.item.beacon_kindling", "matcha-audit-1.12-item-002"),
			new IdentityKey("minecraft:chicken_spawn_egg", "minecraft:warding_stone"),
					new IdentityMapping("forever.item.warding_stone", "matcha-audit-1.12-item-003"),
			new IdentityKey("minecraft:map", "minecraft:abbey_map"),
					new IdentityMapping("forever.item.abbey_map", "matcha-audit-1.12-item-004"),
			new IdentityKey("minecraft:stone_pickaxe", "minecraft:bronze_pickaxe"),
					new IdentityMapping("forever.item.bronze_pickaxe", "matcha-audit-1.12-item-005"));

	private static final Map<BehaviourKey, BehaviourMapping> BEHAVIOUR_MAPPINGS = Map.of(
			new BehaviourKey(MatchaSignalKind.ADVANCEMENT, "main:tutorial/obtain_kiln"),
					new BehaviourMapping("forever.tutorial.kiln", "matcha-audit-1.12-event-001"),
			new BehaviourKey(MatchaSignalKind.ADVANCEMENT, "main:tutorial/obtain_obol"),
					new BehaviourMapping("forever.tutorial.currency", "matcha-audit-1.12-event-002"),
			new BehaviourKey(MatchaSignalKind.ADVANCEMENT, "main:end/kill_dragon"),
					new BehaviourMapping("forever.progression.dragon", "matcha-audit-1.12-event-003"),
			new BehaviourKey(MatchaSignalKind.RECIPE, "crafting:mudkiln"),
					new BehaviourMapping("forever.recipe.kiln", "matcha-audit-1.12-event-004"));

	private final MatchaAdapterStatus status;

	public VerifiedMatchaAdapter(MatchaDetectionResult detection) {
		Objects.requireNonNull(detection, "detection result must not be null.");
		if (!detection.isSupported()) {
			throw new IllegalArgumentException("VerifiedMatchaAdapter requires a supported detection result.");
		}
		this.status = MatchaAdapterStatus.fromDetection(detection);
	}

	@Override
	public MatchaAdapterStatus status() {
		return status;
	}

	@Override
	public boolean supports(MatchaCapability capability) {
		return capability != null;
	}

	@Override
	public MatchaItemTranslation translateItem(MatchaItemObservation observation) {
		if (observation == null) {
			return MatchaItemTranslation.invalid("No item observation was supplied; no identity was inferred.");
		}
		Optional<String> itemModelId = observation.itemModelId();
		if (itemModelId.isEmpty()) {
			return MatchaItemTranslation.unmapped(
					observation,
					"The audited profile requires an exact item signature; no model identity was observed.");
		}
		IdentityMapping mapping = ITEM_MAPPINGS.get(new IdentityKey(observation.baseItemId(), itemModelId.orElseThrow()));
		if (mapping == null) {
			return MatchaItemTranslation.unmapped(
					observation,
					"No audited identity mapping exists for this exact vanilla item signature.");
		}
		return MatchaItemTranslation.mapped(
				mapping.conceptId(), observation, MatchaProfile.PROFILE_ID, mapping.evidenceId());
	}

	@Override
	public MatchaBehaviorTranslation translateBehavior(MatchaBehaviorObservation observation) {
		if (observation == null) {
			return MatchaBehaviorTranslation.invalid("No behaviour observation was supplied; no event was emitted.");
		}
		BehaviourMapping mapping = BEHAVIOUR_MAPPINGS.get(
				new BehaviourKey(observation.kind(), observation.identifier()));
		if (mapping == null) {
			return MatchaBehaviorTranslation.unmapped(
					"No audited behaviour mapping exists for this exact server signal.");
		}
		return MatchaBehaviorTranslation.mapped(
				mapping.conceptId(), MatchaProfile.PROFILE_ID, mapping.evidenceId());
	}

	private record IdentityKey(String baseItemId, String itemModelId) {
	}

	private record IdentityMapping(String conceptId, String evidenceId) {
	}

	private record BehaviourKey(MatchaSignalKind kind, String identifier) {
	}

	private record BehaviourMapping(String conceptId, String evidenceId) {
	}
}

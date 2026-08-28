package dev.forever.core.settlement;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/** Pure graph operations for registered building nodes. No world scan is performed. */
public final class SettlementGraph {

	private SettlementGraph() {
	}

	/** Result of an explicit node attachment attempt. Rejections leave the supplied graph intact. */
	public record RegistrationResult(
			boolean accepted,
			Settlement settlement,
			Optional<SettlementEdge> edge,
			String message) {

		public RegistrationResult {
			Objects.requireNonNull(settlement, "settlement");
			Objects.requireNonNull(edge, "edge");
			message = SettlementCodecs.requiredText(message, "registration result", 512);
			if (!accepted && edge.isPresent()) {
				throw new IllegalArgumentException("A rejected graph registration cannot contain an edge.");
			}
		}
	}

	/**
	 * Attaches one explicitly registered node. The nearest already-registered node is used
	 * only for this bounded registration operation. Normal nodes use proximity; a node
	 * explicitly marked as an outpost may use the larger configured outpost distance.
	 */
	public static RegistrationResult attach(
			Settlement settlement, RegisteredBuilding building, SettlementBalance balance) {
		Objects.requireNonNull(settlement, "settlement");
		Objects.requireNonNull(building, "building");
		Objects.requireNonNull(balance, "balance");

		if (!settlement.dimension().equals(building.dimension())) {
			return rejected(settlement, "Building " + building.id()
					+ " is in a different dimension from settlement " + settlement.id() + ".");
		}
		if (!building.bounds().isWithin(balance.hardValidationVolumeCap())) {
			return rejected(settlement, "Building " + building.id() + " has volume "
					+ building.bounds().volume() + ", above the hard registration cap of "
					+ balance.hardValidationVolumeCap() + " blocks.");
		}
		if (settlement.buildings().containsKey(building.id())) {
			return rejected(settlement, "Building " + building.id()
					+ " is already registered in settlement " + settlement.id() + ".");
		}

		if (settlement.buildings().isEmpty()) {
			Settlement next = settlement.withBuilding(building, Optional.empty());
			return new RegistrationResult(true, next, Optional.empty(),
					"Building " + building.id() + " became the first node in settlement "
							+ settlement.id() + ".");
		}

		Optional<RegisteredBuilding> nearest = settlement.buildings().values().stream()
				.min(Comparator.comparingDouble((RegisteredBuilding existing) -> distance(existing, building))
						.thenComparing(RegisteredBuilding::id));
		RegisteredBuilding nearestBuilding = nearest.orElseThrow();
		double distance = distance(nearestBuilding, building);
		SettlementEdgeKind edgeKind = null;
		if (distance <= balance.proximityAttachmentDistance()) {
			edgeKind = SettlementEdgeKind.PROXIMITY;
		} else if (building.outpost() && distance <= balance.outpostAttachmentDistance()) {
			edgeKind = SettlementEdgeKind.OUTPOST;
		}

		if (edgeKind == null) {
			String outpostGuidance = building.outpost()
					? " The outpost is also beyond the configured outpost distance of "
							+ balance.outpostAttachmentDistance() + " blocks."
					: " Mark the building as an explicit outpost or register it nearer to the graph.";
			return rejected(settlement, "Building " + building.id() + " is " + distance
					+ " blocks from the nearest registered node, beyond the proximity distance of "
					+ balance.proximityAttachmentDistance() + "." + outpostGuidance);
		}

		SettlementEdge edge = new SettlementEdge(nearestBuilding.id(), building.id(), edgeKind, distance);
		Settlement next = settlement.withBuilding(building, Optional.of(edge));
		return new RegistrationResult(true, next, Optional.of(edge),
				"Building " + building.id() + " attached to " + nearestBuilding.id()
						+ " using a " + edgeKind.name().toLowerCase() + " graph edge.");
	}

	private static double distance(RegisteredBuilding first, RegisteredBuilding second) {
		long dx = (long) first.anchor().getX() - second.anchor().getX();
		long dy = (long) first.anchor().getY() - second.anchor().getY();
		long dz = (long) first.anchor().getZ() - second.anchor().getZ();
		return Math.sqrt((double) dx * dx + (double) dy * dy + (double) dz * dz);
	}

	private static RegistrationResult rejected(Settlement settlement, String message) {
		return new RegistrationResult(false, settlement, Optional.empty(), message);
	}
}

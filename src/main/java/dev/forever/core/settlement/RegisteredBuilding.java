package dev.forever.core.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.Identifier;

/**
 * A player-selected building node. The record contains bounds and cached function state,
 * never an inferred scan or a copy of the building's blocks.
 */
public record RegisteredBuilding(
		UUID id,
		Identifier dimension,
		BuildingBounds bounds,
		Set<SettlementRole> roles,
		boolean outpost,
		ValidationResult validation) {

	private static final int MAX_ROLES = 8;

	public static final Codec<RegisteredBuilding> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SettlementCodecs.UUID_CODEC.fieldOf("id").forGetter(RegisteredBuilding::id),
			Identifier.CODEC.fieldOf("dimension").forGetter(RegisteredBuilding::dimension),
			BuildingBounds.CODEC.fieldOf("bounds").forGetter(RegisteredBuilding::bounds),
			SettlementCodecs.enumSetCodec(SettlementRole.class, MAX_ROLES, "building roles")
					.fieldOf("roles").forGetter(RegisteredBuilding::roles),
			Codec.BOOL.fieldOf("outpost").forGetter(RegisteredBuilding::outpost),
			ValidationResult.CODEC.fieldOf("validation").forGetter(RegisteredBuilding::validation)
	).apply(instance, RegisteredBuilding::new));

	public RegisteredBuilding {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(dimension, "dimension");
		Objects.requireNonNull(bounds, "bounds");
		roles = SettlementCodecs.copyEnumSet(roles, SettlementRole.class, "building roles");
		if (roles.size() > MAX_ROLES) {
			throw new IllegalArgumentException("A registered building has too many functional roles.");
		}
		if (outpost || roles.contains(SettlementRole.OUTPOST)) {
			roles = java.util.Set.copyOf(java.util.stream.Stream.concat(
					roles.stream(), java.util.stream.Stream.of(SettlementRole.OUTPOST)).toList());
			if (roles.size() > MAX_ROLES) {
				throw new IllegalArgumentException("A registered building has too many functional roles.");
			}
			outpost = true;
		}
		Objects.requireNonNull(validation, "validation");
	}

	public static RegisteredBuilding pending(
			UUID id, Identifier dimension, BuildingBounds bounds, Set<SettlementRole> roles) {
		return new RegisteredBuilding(id, dimension, bounds, roles, roles.contains(SettlementRole.OUTPOST),
				ValidationResult.pending());
	}

	public net.minecraft.core.BlockPos anchor() {
		return bounds.center();
	}

	public RegisteredBuilding withValidation(ValidationResult nextValidation) {
		return new RegisteredBuilding(id, dimension, bounds, roles, outpost, nextValidation);
	}
}

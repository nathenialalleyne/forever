package dev.forever.core.settlement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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

	private record Encoded(
			UUID id,
			Identifier dimension,
			BuildingBounds bounds,
			Set<SettlementRole> roles,
			boolean outpost,
			ValidationResult validation) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SettlementCodecs.UUID_CODEC.fieldOf("id").forGetter(Encoded::id),
			Identifier.CODEC.fieldOf("dimension").forGetter(Encoded::dimension),
			BuildingBounds.CODEC.fieldOf("bounds").forGetter(Encoded::bounds),
			SettlementCodecs.enumSetCodec(SettlementRole.class, MAX_ROLES, "building roles")
					.fieldOf("roles").forGetter(Encoded::roles),
			Codec.BOOL.fieldOf("outpost").forGetter(Encoded::outpost),
			ValidationResult.CODEC.fieldOf("validation").forGetter(Encoded::validation)
	).apply(instance, Encoded::new));

	public static final Codec<RegisteredBuilding> CODEC = RAW_CODEC.flatXmap(
			encoded -> create(encoded),
			building -> DataResult.success(new Encoded(
					building.id(), building.dimension(), building.bounds(), building.roles(), building.outpost(),
					building.validation())));

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

	private static DataResult<RegisteredBuilding> create(Encoded encoded) {
		try {
			return DataResult.success(new RegisteredBuilding(
					encoded.id(), encoded.dimension(), encoded.bounds(), encoded.roles(), encoded.outpost(),
					encoded.validation()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
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

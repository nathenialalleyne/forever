package dev.forever.core.settlement;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Bounded, server-authoritative residence validation.
 *
 * <p>Only function is evaluated: enclosure, beds, workstations, storage, safety, and
 * free interior space. There is no blueprint, theme, beauty, palette, or aesthetic score.
 * The validator refuses a volume above the data-defined cap before reading any block.
 */
public final class ResidenceValidator {

	private static final Set<Block> VANILLA_WORKSTATIONS = Set.of(
			Blocks.CRAFTING_TABLE,
			Blocks.FURNACE,
			Blocks.BREWING_STAND,
			Blocks.LOOM,
			Blocks.SMOKER,
			Blocks.BLAST_FURNACE,
			Blocks.CARTOGRAPHY_TABLE,
			Blocks.FLETCHING_TABLE,
			Blocks.GRINDSTONE,
			Blocks.LECTERN,
			Blocks.SMITHING_TABLE,
			Blocks.STONECUTTER,
			Blocks.COMPOSTER);

	private ResidenceValidator() {
	}

	/**
	 * Validates only the registered inclusive volume. No method in this class searches
	 * outside {@code building.bounds()}.
	 */
	public static ValidationResult validate(
			RegisteredBuilding building,
			SettlementWorldView world,
			SettlementBalance balance,
			int revision) {
		Objects.requireNonNull(building, "building");
		Objects.requireNonNull(world, "world");
		Objects.requireNonNull(balance, "balance");
		if (revision < 0) {
			throw new IllegalArgumentException("Settlement validation revision cannot be negative.");
		}

		List<ValidationIssue> issues = new ArrayList<>();
		if (!building.roles().contains(SettlementRole.RESIDENCE)) {
			issues.add(issue(FunctionalRequirement.DECLARATION,
					"This building is not registered with the Residence function."));
			return ValidationResult.invalid(revision, issues);
		}
		if (!building.bounds().isWithin(balance.validationVolumeCap())) {
			issues.add(issue(FunctionalRequirement.VOLUME_CAP,
					"The registered residence volume is " + building.bounds().volume()
							+ " blocks, above the validation cap of " + balance.validationVolumeCap() + "."));
			return ValidationResult.invalid(revision, issues);
		}

		long freeInterior = 0L;
		int beds = 0;
		int workstations = 0;
		long storageSlots = 0L;
		boolean enclosed = true;
		boolean safe = true;
		int darkestLight = Integer.MAX_VALUE;

		BuildingBounds bounds = building.bounds();
		for (int x = bounds.min().getX(); x <= bounds.max().getX(); x++) {
			for (int y = bounds.min().getY(); y <= bounds.max().getY(); y++) {
				for (int z = bounds.min().getZ(); z <= bounds.max().getZ(); z++) {
					BlockPos position = new BlockPos(x, y, z);
					BlockState state = Objects.requireNonNull(world.blockState(position),
							"world returned a null block state");
					if (bounds.isBoundary(position)) {
						if (!isEnclosureBlock(state)) {
							enclosed = false;
						}
						continue;
					}

					if (isFreeInterior(state)) {
						freeInterior++;
					}
					if (isBed(state)) {
						beds++;
					}
					if (isWorkstation(state)) {
						workstations++;
					}
					BlockEntity blockEntity = world.blockEntity(position);
					if (blockEntity instanceof Container container) {
						storageSlots = Math.min(Long.MAX_VALUE,
								storageSlots + Math.max(0, container.getContainerSize()));
					}
					if (isUnsafe(state)) {
						safe = false;
					}
					int light = world.lightLevel(position);
					darkestLight = Math.min(darkestLight, light);
				}
			}
		}

		if (!enclosed) {
			issues.add(issue(FunctionalRequirement.ENCLOSURE,
					"The residence is not enclosed: every boundary block in the registered bounds must be occupied."));
		}
		if (beds < balance.minimumBeds()) {
			issues.add(issue(FunctionalRequirement.BEDS,
					"The residence needs at least " + balance.minimumBeds()
							+ " usable bed(s), but the registered volume contains " + beds + "."));
		}
		if (workstations < balance.minimumWorkstations()) {
			issues.add(issue(FunctionalRequirement.WORKSTATIONS,
					"The residence needs at least " + balance.minimumWorkstations()
							+ " usable workstation(s), but the registered volume contains " + workstations + "."));
		}
		if (storageSlots < balance.minimumStorageSlots()) {
			issues.add(issue(FunctionalRequirement.STORAGE,
					"The residence needs at least " + balance.minimumStorageSlots()
							+ " storage slots, but the registered volume provides " + storageSlots + "."));
		}
		if (!safe || darkestLight < balance.minimumLightLevel()) {
			issues.add(issue(FunctionalRequirement.SAFETY,
					"The residence is unsafe: remove hazards and provide at least "
							+ balance.minimumLightLevel() + " light throughout the interior."));
		}
		if (freeInterior < balance.minimumInteriorSpace()) {
			issues.add(issue(FunctionalRequirement.SPACE,
					"The residence needs at least " + balance.minimumInteriorSpace()
							+ " free interior blocks, but the registered volume provides " + freeInterior + "."));
		}

		return issues.isEmpty() ? ValidationResult.valid(revision) : ValidationResult.invalid(revision, issues);
	}

	private static ValidationIssue issue(FunctionalRequirement requirement, String message) {
		return new ValidationIssue(requirement, message);
	}

	private static boolean isEnclosureBlock(BlockState state) {
		return !state.isAir() && !state.liquid() && state.isSolid();
	}

	private static boolean isFreeInterior(BlockState state) {
		return state.isAir() && state.getFluidState().isEmpty();
	}

	private static boolean isBed(BlockState state) {
		if (!state.is(BlockTags.BEDS) && !(state.getBlock() instanceof BedBlock)) {
			return false;
		}
		if (state.getBlock() instanceof BedBlock && state.hasProperty(BedBlock.PART)) {
			return state.getValue(BedBlock.PART) == BedPart.FOOT;
		}
		return true;
	}

	private static boolean isWorkstation(BlockState state) {
		return !state.is(BlockTags.BEDS) && VANILLA_WORKSTATIONS.contains(state.getBlock());
	}

	private static boolean isUnsafe(BlockState state) {
		return state.is(BlockTags.FIRE)
				|| state.is(Blocks.LAVA)
				|| state.is(Blocks.MAGMA_BLOCK)
				|| state.is(Blocks.CACTUS)
				|| state.is(Blocks.SWEET_BERRY_BUSH)
				|| state.is(Blocks.CAMPFIRE)
				|| state.is(Blocks.SOUL_CAMPFIRE);
	}
}

package dev.forever.core.career.adapter;

import dev.forever.core.settlement.adapter.BuildingBounds;
import dev.forever.core.settlement.domain.FunctionalRequirement;
import dev.forever.core.settlement.adapter.RegisteredBuilding;
import dev.forever.core.settlement.domain.SettlementBalance;
import dev.forever.core.settlement.domain.SettlementRole;
import dev.forever.core.settlement.adapter.SettlementWorldView;
import dev.forever.core.settlement.domain.ValidationIssue;
import dev.forever.core.settlement.domain.ValidationResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Bounded functional validator for the Mason workplace role. It never evaluates appearance. */
public final class MasonWorkshopValidator {

	private static final Set<Block> VANILLA_WORKSTATIONS = Set.of(
			Blocks.CRAFTING_TABLE,
			Blocks.FURNACE,
			Blocks.LOOM,
			Blocks.SMOKER,
			Blocks.BLAST_FURNACE,
			Blocks.FLETCHING_TABLE,
			Blocks.GRINDSTONE,
			Blocks.LECTERN,
			Blocks.SMITHING_TABLE,
			Blocks.STONECUTTER);

	private MasonWorkshopValidator() {
	}

	/** Validates only the explicitly registered bounds and never loads a chunk outside them. */
	public static ValidationResult validate(
			RegisteredBuilding building,
			SettlementWorldView world,
			SettlementBalance balance,
			int revision) {
		Objects.requireNonNull(building, "building");
		Objects.requireNonNull(world, "world");
		Objects.requireNonNull(balance, "balance");
		if (revision < 0) {
			throw new IllegalArgumentException("Mason workplace validation revision cannot be negative.");
		}

		List<ValidationIssue> issues = new ArrayList<>();
		if (!building.roles().contains(SettlementRole.WORKPLACE)) {
			issues.add(new ValidationIssue(FunctionalRequirement.DECLARATION,
					"The building is not registered with the Workplace function."));
			return ValidationResult.invalid(revision, issues);
		}
		if (!building.bounds().isWithin(balance.validationVolumeCap())) {
			issues.add(new ValidationIssue(FunctionalRequirement.VOLUME_CAP,
					"The Mason workplace volume is " + building.bounds().volume()
							+ " blocks, above the validation cap of " + balance.validationVolumeCap() + "."));
			return ValidationResult.invalid(revision, issues);
		}

		long freeInterior = 0L;
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
					darkestLight = Math.min(darkestLight, world.lightLevel(position));
				}
			}
		}

		if (!enclosed) {
			issues.add(new ValidationIssue(FunctionalRequirement.ENCLOSURE,
					"The Mason workplace is not enclosed: every boundary block must be occupied."));
		}
		if (workstations < balance.minimumWorkstations()) {
			issues.add(new ValidationIssue(FunctionalRequirement.WORKSTATIONS,
					"The Mason workplace needs at least " + balance.minimumWorkstations()
							+ " usable workstation(s), but contains " + workstations + "."));
		}
		if (storageSlots < balance.minimumStorageSlots()) {
			issues.add(new ValidationIssue(FunctionalRequirement.STORAGE,
					"The Mason workplace needs at least " + balance.minimumStorageSlots()
							+ " storage slots, but provides " + storageSlots + "."));
		}
		if (!safe || darkestLight < balance.minimumLightLevel()) {
			issues.add(new ValidationIssue(FunctionalRequirement.SAFETY,
					"The Mason workplace is unsafe or too dark; remove hazards and provide at least "
							+ balance.minimumLightLevel() + " light."));
		}
		if (freeInterior < balance.minimumInteriorSpace()) {
			issues.add(new ValidationIssue(FunctionalRequirement.SPACE,
					"The Mason workplace needs at least " + balance.minimumInteriorSpace()
							+ " free interior blocks, but provides " + freeInterior + "."));
		}
		return issues.isEmpty() ? ValidationResult.valid(revision) : ValidationResult.invalid(revision, issues);
	}

	private static boolean isEnclosureBlock(BlockState state) {
		return !state.isAir() && !state.liquid() && state.isSolid();
	}

	private static boolean isFreeInterior(BlockState state) {
		return state.isAir() && state.getFluidState().isEmpty();
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

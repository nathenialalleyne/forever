package dev.forever.gametest.mason;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.forever.core.career.CareerAttachment;
import dev.forever.core.career.CareerState;
import dev.forever.core.career.CareerStatus;
import dev.forever.core.career.CareerTeachingResult;
import dev.forever.core.career.CareerTeachingService;
import dev.forever.core.career.ForeverCareer;
import dev.forever.core.career.MasonCareerService;
import dev.forever.core.career.MasonDataLoader;
import dev.forever.core.career.MasonIds;
import dev.forever.core.career.MasonRank;
import dev.forever.core.career.MasonWorkshopService;
import dev.forever.core.economy.CoinPurseAttachment;
import dev.forever.core.economy.CoinPurseState;
import dev.forever.core.economy.EconomyBalance;
import dev.forever.core.economy.EconomyBalanceAccess;
import dev.forever.core.economy.ForeverEconomy;
import dev.forever.core.economy.ObolItem;
import dev.forever.core.economy.PurseService;
import dev.forever.core.economy.PurseTransactionResult;
import dev.forever.core.settlement.BuildingBounds;
import dev.forever.core.settlement.ForeverSettlement;
import dev.forever.core.settlement.Settlement;
import dev.forever.core.settlement.SettlementBalance;
import dev.forever.core.settlement.SettlementCharterService;
import dev.forever.core.settlement.SettlementState;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.Blocks;

/** Dedicated-server lifecycle and conservation checks for FVR-500 through FVR-507. */
public final class MasonVerticalSliceGameTest {

	static {
		// ForeverMod must wire these in production. Initialize the owned registrations while the
		// GameTest entrypoint is being loaded, before the dedicated test server freezes registries.
		ForeverCareer.initialize();
		ForeverEconomy.initialize();
	}

	@GameTest
	public void masonCareerStateSurvivesRestartBoundary(GameTestHelper helper) {
		MasonDataLoader.installBundled();
		Villager villager = helper.spawnWithNoFreeWill(villagerType(), new BlockPos(1, 1, 1));
		CareerState original = CareerState.masonApprentice(
				Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000501")),
				Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000502")))
				.withKnownTechnique(MasonIds.STONE_CUTTING)
				.withDemonstratedTechnique(MasonIds.STONE_CUTTING)
				.withValidatedProject(MasonIds.VALIDATED_WORKSHOP);
		villager.setAttached(CareerAttachment.TYPE, original);

		JsonElement saved = CareerAttachment.TYPE.persistenceCodec()
				.encodeStart(JsonOps.INSTANCE, villager.getAttached(CareerAttachment.TYPE)).getOrThrow();
		CareerState reloaded = CareerAttachment.TYPE.persistenceCodec()
				.parse(JsonOps.INSTANCE, saved).getOrThrow();

		if (!original.equals(reloaded) || !reloaded.equals(villager.getAttached(CareerAttachment.TYPE))) {
			throw helper.assertionException("Mason career attachment changed across the restart save boundary.");
		}
		helper.succeed();
	}

	@GameTest
	public void coinPurseBalanceSurvivesRestartBoundary(GameTestHelper helper) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		CoinPurseState original = new CoinPurseState(CoinPurseState.CURRENT_SCHEMA, 777L, 12L);
		player.setAttached(CoinPurseAttachment.TYPE, original);

		JsonElement saved = CoinPurseAttachment.TYPE.persistenceCodec()
				.encodeStart(JsonOps.INSTANCE, player.getAttached(CoinPurseAttachment.TYPE)).getOrThrow();
		CoinPurseState reloaded = CoinPurseAttachment.TYPE.persistenceCodec()
				.parse(JsonOps.INSTANCE, saved).getOrThrow();

		if (!original.equals(reloaded)) {
			throw helper.assertionException("Coin Purse balance changed across the restart save boundary.");
		}
		helper.succeed();
	}

	@GameTest
	public void obolDepositWithdrawCyclesConserveTotalValue(GameTestHelper helper) {
		ForeverEconomy.initialize();
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		EconomyBalance balance = EconomyBalanceAccess.requireCurrent();
		player.setAttached(CoinPurseAttachment.TYPE, CoinPurseState.empty());
		// Deliberately split the physical value so the first deposit must consume a partial
		// stack and then continue into a second stack without losing either remainder.
		player.getInventory().setItem(0, ObolItem.stack(3));
		player.getInventory().setItem(1, ObolItem.stack(29));
		long initialTotal = PurseService.physicalValue(player.getInventory(), balance)
				+ PurseService.balanceOf(player);

		for (int cycle = 0; cycle < 100; cycle++) {
			PurseTransactionResult deposit = PurseService.deposit(player, 8L);
			if (!deposit.applied()) {
				throw helper.assertionException("Obol deposit failed during cycle %s: %s", cycle, deposit.message());
			}
			PurseTransactionResult withdraw = PurseService.withdraw(player, 8L);
			if (!withdraw.applied()) {
				throw helper.assertionException("Obol withdrawal failed during cycle %s: %s", cycle, withdraw.message());
			}
			long total = PurseService.physicalValue(player.getInventory(), balance)
					+ PurseService.balanceOf(player);
			if (total != initialTotal) {
				throw helper.assertionException("Obol value changed from %s to %s during cycle %s", initialTotal, total, cycle);
			}
		}
		helper.succeed();
	}

	@GameTest
	public void taughtTechniqueSurvivesPhysicalVillagerDeath(GameTestHelper helper) {
		UUID workplace = UUID.fromString("00000000-0000-0000-0000-000000000503");
		Villager mentorEntity = helper.spawnWithNoFreeWill(villagerType(), new BlockPos(1, 1, 1));
		Villager apprenticeEntity = helper.spawnWithNoFreeWill(villagerType(), new BlockPos(3, 1, 1));
		CareerState mentor = new CareerState(
				CareerState.CURRENT_SCHEMA,
				MasonIds.MASON,
				MasonRank.JOURNEYMAN,
				Set.of(MasonIds.STONE_CUTTING),
				Set.of(MasonIds.STONE_CUTTING),
				Set.of(),
				Set.of(),
				Optional.empty(),
				Optional.of(workplace),
				Optional.empty(),
				Set.of(),
				CareerStatus.ACTIVE,
				Optional.empty());
		CareerState apprentice = CareerState.masonApprentice(Optional.empty(), Optional.of(workplace));
		mentorEntity.setAttached(CareerAttachment.TYPE, mentor);
		apprenticeEntity.setAttached(CareerAttachment.TYPE, apprentice);

		CareerTeachingResult taught = CareerTeachingService.teach(
				mentorEntity, apprenticeEntity, MasonIds.STONE_CUTTING);
		if (!taught.applied()) {
			throw helper.assertionException("Mason teaching did not commit: %s", taught.message());
		}
		if (!mentorEntity.hurtServer(helper.getLevel(), helper.getLevel().damageSources().fall(), Float.MAX_VALUE)) {
			throw helper.assertionException("The physically simulated fall did not kill the mentor villager.");
		}
		CareerState deceased = MasonCareerService.stateOf(mentorEntity);

		if (mentorEntity.isAlive()
				|| deceased.status() != CareerStatus.DEAD
				|| !MasonCareerService.stateOf(apprenticeEntity).knownTechniques().contains(MasonIds.STONE_CUTTING)
				|| !deceased.knownTechniques().contains(MasonIds.STONE_CUTTING)) {
			throw helper.assertionException("Taught Mason technique was not preserved across the mentor death.");
		}
		helper.succeed();
	}

	@GameTest
	public void masonryWorkplaceUsesSettlementRegistrationAndFunction(GameTestHelper helper) {
		SettlementBalance balance = ForeverSettlement.loadBalance(
				helper.getLevel().getServer().getResourceManager());
		Settlement settlement = SettlementCharterService.found(
				UUID.fromString("00000000-0000-0000-0000-000000000504"),
				"Mason Test Settlement",
				UUID.fromString("00000000-0000-0000-0000-000000000505"),
				Identifier.parse("minecraft:overworld"),
				new BlockPos(0, 0, 0));
		SettlementState state = SettlementCharterService.found(SettlementState.empty(), settlement);
		BlockPos minimum = helper.absolutePos(new BlockPos(0, 0, 0));
		BlockPos maximum = helper.absolutePos(new BlockPos(4, 4, 4));
		BuildingBounds bounds = new BuildingBounds(minimum, maximum);
		for (int x = 0; x <= 4; x++) {
			for (int y = 0; y <= 4; y++) {
				for (int z = 0; z <= 4; z++) {
					BlockPos relative = new BlockPos(x, y, z);
					BlockPos position = helper.absolutePos(relative);
					if (bounds.isBoundary(position)) {
						helper.setBlock(relative, Blocks.STONE);
					}
				}
			}
		}
		helper.setBlock(new BlockPos(1, 1, 1), Blocks.CRAFTING_TABLE);
		helper.setBlock(new BlockPos(2, 1, 1), Blocks.CHEST);
		helper.setBlock(new BlockPos(3, 1, 1), Blocks.TORCH);
		helper.setBlock(new BlockPos(1, 3, 1), Blocks.TORCH);
		helper.setBlock(new BlockPos(1, 1, 3), Blocks.TORCH);
		helper.setBlock(new BlockPos(3, 3, 3), Blocks.TORCH);

		var result = MasonWorkshopService.register(
				state,
				settlement.id(),
				UUID.fromString("00000000-0000-0000-0000-000000000506"),
				Identifier.parse("minecraft:overworld"),
				bounds,
				balance,
				dev.forever.core.settlement.SettlementWorldView.from(helper.getLevel()),
				1);
		if (!result.accepted() || !result.validation().valid()
				|| result.building().orElseThrow().roles().stream()
						.noneMatch(role -> role == dev.forever.core.settlement.SettlementRole.WORKPLACE)) {
			throw helper.assertionException("Functional Mason workplace registration failed: %s", result.message());
		}
		helper.setBlock(new BlockPos(6, 1, 1), Blocks.STONE);
		helper.assertBlockPresent(Blocks.STONE, new BlockPos(6, 1, 1));
		helper.succeed();
	}

	@SuppressWarnings("unchecked")
	private static net.minecraft.world.entity.EntityType<Villager> villagerType() {
		return (net.minecraft.world.entity.EntityType<Villager>) BuiltInRegistries.ENTITY_TYPE
				.getOptional(Identifier.parse("minecraft:villager"))
				.orElseThrow();
	}
}

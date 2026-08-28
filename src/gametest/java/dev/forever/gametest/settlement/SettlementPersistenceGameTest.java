package dev.forever.gametest.settlement;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.forever.core.settlement.BuildingBounds;
import dev.forever.core.settlement.BuildingRegistrationService;
import dev.forever.core.settlement.ForeverSettlement;
import dev.forever.core.settlement.MigrationRecord;
import dev.forever.core.settlement.MigrationService;
import dev.forever.core.settlement.MigrationSimulationResult;
import dev.forever.core.settlement.MigrationStatus;
import dev.forever.core.settlement.RegisteredBuilding;
import dev.forever.core.settlement.Settlement;
import dev.forever.core.settlement.SettlementBalance;
import dev.forever.core.settlement.SettlementCharterService;
import dev.forever.core.settlement.SettlementRole;
import dev.forever.core.settlement.SettlementSavedData;
import dev.forever.core.settlement.SettlementState;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;

/** Dedicated-server lifecycle checks for FVR-400 through FVR-405. */
public final class SettlementPersistenceGameTest {

	@GameTest
	public void settlementGraphSurvivesSaveAndRestartBoundary(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		SettlementBalance balance = ForeverSettlement.loadBalance(level.getServer().getResourceManager());
		SettlementSavedData savedData = ForeverSettlement.data(level);
		Settlement settlement = Settlement.found(UUID.randomUUID(), "Restart Test", UUID.randomUUID(),
				level.dimension().identifier(), BlockPos.ZERO);
		SettlementState state = SettlementCharterService.found(SettlementState.empty(), settlement);
		RegisteredBuilding first = RegisteredBuilding.pending(UUID.randomUUID(), level.dimension().identifier(),
				new BuildingBounds(new BlockPos(0, 0, 0), new BlockPos(2, 2, 2)),
				Set.of(SettlementRole.RESIDENCE));
		RegisteredBuilding second = RegisteredBuilding.pending(UUID.randomUUID(), level.dimension().identifier(),
				new BuildingBounds(new BlockPos(20, 0, 0), new BlockPos(22, 2, 2)),
				Set.of(SettlementRole.WORKPLACE));
		state = BuildingRegistrationService.register(state, settlement.id(), first, balance).state();
		state = BuildingRegistrationService.register(state, settlement.id(), second, balance).state();
		savedData.replace(state);
		level.getDataStorage().saveAndJoin();

		JsonElement encoded = SettlementSavedData.CODEC.encodeStart(JsonOps.INSTANCE, savedData).getOrThrow();
		SettlementSavedData reloaded = SettlementSavedData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
		Settlement restored = reloaded.state().requireSettlement(settlement.id());
		if (restored.buildings().size() != 2 || restored.edges().size() != 1) {
			throw helper.assertionException("Settlement graph changed across the save/restart boundary.");
		}
		helper.succeed();
	}

	@GameTest
	public void explicitBuildingRegistrationPersists(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		SettlementBalance balance = ForeverSettlement.loadBalance(level.getServer().getResourceManager());
		SettlementSavedData savedData = ForeverSettlement.data(level);
		Settlement settlement = Settlement.found(UUID.randomUUID(), "Registration Test", UUID.randomUUID(),
				level.dimension().identifier(), BlockPos.ZERO);
		SettlementState state = SettlementCharterService.found(SettlementState.empty(), settlement);
		RegisteredBuilding building = RegisteredBuilding.pending(UUID.randomUUID(), level.dimension().identifier(),
				new BuildingBounds(new BlockPos(0, 0, 0), new BlockPos(2, 2, 2)),
				Set.of(SettlementRole.RESIDENCE));
		BuildingRegistrationService.StateRegistrationResult result = BuildingRegistrationService.register(
				state, settlement.id(), building, balance);
		if (!result.accepted()) {
			throw helper.assertionException("Server rejected a valid explicit building registration: %s", result.message());
		}
		savedData.replace(result.state());
		level.getDataStorage().saveAndJoin();

		JsonElement encoded = SettlementSavedData.CODEC.encodeStart(JsonOps.INSTANCE, savedData).getOrThrow();
		SettlementSavedData reloaded = SettlementSavedData.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
		if (reloaded.state().requireSettlement(settlement.id()).building(building.id()).isEmpty()) {
			throw helper.assertionException("Explicit building registration was not present after reload.");
		}
		helper.succeed();
	}

	@GameTest
	public void unloadedAbstractSimulationNeverKillsVillager(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		SettlementBalance balance = ForeverSettlement.loadBalance(level.getServer().getResourceManager());
		Villager villager = helper.spawnWithNoFreeWill(EntityTypes.VILLAGER, new BlockPos(1, 1, 1));
		Settlement settlement = Settlement.found(UUID.randomUUID(), "Migration Test", UUID.randomUUID(),
				level.dimension().identifier(), BlockPos.ZERO);
		MigrationRecord arrived = MigrationService.offer(UUID.randomUUID(), Optional.of(villager.getUUID()),
				"test origin", "test career", 0L).withStatus(MigrationStatus.ARRIVED, 0L);
		settlement = settlement.withMigration(arrived);

		for (int tick = 0; tick < 1_000; tick++) {
			MigrationSimulationResult result = MigrationService.simulateUnloadedTick(settlement, tick, balance);
			if (result.deaths() != 0 || result.chunkLoadRequested()) {
				throw helper.assertionException("Unloaded simulation violated its no-death or no-chunk-load invariant.");
			}
			settlement = result.settlement();
		}
		if (!villager.isAlive()) {
			throw helper.assertionException("Abstract unloaded simulation killed a villager.");
		}
		if (!MigrationService.UNLOADED_SIMULATION_INVARIANT.contains("never creates a villager death")) {
			throw helper.assertionException("The unloaded simulation invariant is not explicit.");
		}
		helper.succeed();
	}
}

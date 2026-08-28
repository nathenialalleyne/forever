package dev.forever.core.settlement.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.forever.core.settlement.domain.FunctionalRequirement;
import dev.forever.core.settlement.domain.SettlementRole;
import dev.forever.core.settlement.domain.ValidationIssue;
import dev.forever.core.settlement.domain.ValidationResult;
import dev.forever.core.settlement.domain.ValidationStatus;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResidenceValidatorTest {

	@BeforeAll
	static void bootstrapMinecraftRegistries() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	@DisplayName("a functionally equipped enclosed room passes without any style check")
	void functionalResidencePasses() {
		RegisteredBuilding building = RegisteredBuilding.pending(
				java.util.UUID.randomUUID(), SettlementTestFixtures.OVERWORLD,
				new BuildingBounds(new net.minecraft.core.BlockPos(0, 0, 0),
						new net.minecraft.core.BlockPos(4, 4, 4)), Set.of(SettlementRole.RESIDENCE));

		ValidationResult result = ResidenceValidator.validate(
				building, SettlementTestFixtures.enclosedRoom(), SettlementTestFixtures.BALANCE, 7);

		assertTrue(result.valid(), result.messages().toString());
		assertEquals(7, result.revision());
	}

	@Test
	@DisplayName("a missing bed fails with a precise functional requirement")
	void missingBedExplainsFunctionalFailure() {
		SettlementTestFixtures.TestWorld world = SettlementTestFixtures.enclosedRoom();
		world.set(new net.minecraft.core.BlockPos(1, 1, 1), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
		RegisteredBuilding building = RegisteredBuilding.pending(
				java.util.UUID.randomUUID(), SettlementTestFixtures.OVERWORLD,
				new BuildingBounds(new net.minecraft.core.BlockPos(0, 0, 0),
						new net.minecraft.core.BlockPos(4, 4, 4)), Set.of(SettlementRole.RESIDENCE));

		ValidationResult result = ResidenceValidator.validate(
				building, world, SettlementTestFixtures.BALANCE, 8);

		assertEquals(ValidationStatus.INVALID, result.status());
		ValidationIssue bedIssue = result.issues().stream()
				.filter(issue -> issue.requirement() == FunctionalRequirement.BEDS)
				.findFirst().orElseThrow();
		assertTrue(bedIssue.message().contains("usable bed"));
	}

	@Test
	@DisplayName("a volume above the configured cap is rejected before the world is read")
	void validationVolumeCapIsEnforced() {
		SettlementTestFixtures.TestWorld world = new SettlementTestFixtures.TestWorld();
		RegisteredBuilding building = RegisteredBuilding.pending(
				java.util.UUID.randomUUID(), SettlementTestFixtures.OVERWORLD,
				new BuildingBounds(new net.minecraft.core.BlockPos(0, 0, 0),
						new net.minecraft.core.BlockPos(100, 100, 100)), Set.of(SettlementRole.RESIDENCE));

		ValidationResult result = ResidenceValidator.validate(
				building, world, SettlementTestFixtures.BALANCE, 9);

		assertEquals(FunctionalRequirement.VOLUME_CAP, result.issues().getFirst().requirement());
		assertEquals(0, world.blockReads());
	}

	@Test
	@DisplayName("hazards and low light fail the safety requirement")
	void unsafeResidenceExplainsSafetyFailure() {
		SettlementTestFixtures.TestWorld world = SettlementTestFixtures.enclosedRoom();
		world.set(new net.minecraft.core.BlockPos(1, 1, 2), net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState());
		world.setLight(new net.minecraft.core.BlockPos(2, 1, 2), 0);
		RegisteredBuilding building = RegisteredBuilding.pending(
				java.util.UUID.randomUUID(), SettlementTestFixtures.OVERWORLD,
				new BuildingBounds(new net.minecraft.core.BlockPos(0, 0, 0),
						new net.minecraft.core.BlockPos(4, 4, 4)), Set.of(SettlementRole.RESIDENCE));

		ValidationResult result = ResidenceValidator.validate(
				building, world, SettlementTestFixtures.BALANCE, 10);

		assertTrue(result.issues().stream().anyMatch(issue -> issue.requirement() == FunctionalRequirement.SAFETY));
	}
}

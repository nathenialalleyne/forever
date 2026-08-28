package dev.forever.gametest.equipment;

import static net.minecraft.world.InteractionHand.MAIN_HAND;

import dev.forever.core.equipment.EquipmentOperationResult;
import dev.forever.core.equipment.adapter.EquipmentService;
import dev.forever.core.equipment.adapter.EquipmentState;
import dev.forever.core.equipment.adapter.ForeverEquipment;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameType;

/** Dedicated-server acceptance tests for the equipment vertical slice. */
public final class EquipmentGameTest {

	private static final Identifier PROSPECTOR = Identifier.parse("forever:prospector");
	private static final Identifier EXCAVATOR = Identifier.parse("forever:excavator");

	static {
		// The production entrypoint must call this method. The test entrypoint calls it here so
		// component and item registries are populated before the GameTest server freezes them.
		ForeverEquipment.initialize();
	}

	@GameTest
	public void brokenEquipmentSurvivesAndRepairs(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		helper.assertTrue(!player.hasInfiniteMaterials(), "the damage test player must be in survival mode");
		ItemStack equipment = new ItemStack(ForeverEquipment.FIELD_TOOL);
		EquipmentState initial = EquipmentService.initializeStack(equipment);
		int equipmentCountBefore = equipment.getCount();
		helper.assertTrue(!equipment.isBroken(), "a newly initialised item must not be vanilla-broken");

		player.setItemInHand(MAIN_HAND, equipment);
		equipment.hurtAndBreak(initial.currentCondition(), player, EquipmentSlot.MAINHAND);

		EquipmentState broken = EquipmentService.state(equipment).orElseThrow();
		helper.assertTrue(equipment.getCount() == equipmentCountBefore,
				"zero condition must retain the ItemStack count");
		helper.assertTrue(!equipment.isEmpty(), "zero condition must retain the ItemStack");
		helper.assertTrue(broken.isBroken(), "zero condition must be encoded as broken");
		helper.assertTrue(equipment.isBroken(), "vanilla damage must expose the broken state");
		player.setItemInHand(MAIN_HAND, equipment);
		helper.assertTrue(equipment.getItem().use(level, player, MAIN_HAND) == InteractionResult.FAIL,
				"broken equipment must deny its ordinary use path");
		assertBrokenTooltip(helper, broken, equipment);

		ItemStack fieldMaterial = new ItemStack(Items.IRON_INGOT);
		int fieldMaterialBefore = fieldMaterial.getCount();
		EquipmentOperationResult fieldRepair = EquipmentService.fieldRepair(equipment, fieldMaterial);
		helper.assertTrue(fieldRepair.success(), "field repair should succeed with its configured material");
		helper.assertTrue(equipment.getCount() == equipmentCountBefore,
				"field repair must not duplicate or delete equipment");
		helper.assertTrue(fieldMaterial.getCount() == fieldMaterialBefore - 1,
				"field repair must consume exactly one configured material");
		helper.assertTrue(EquipmentService.state(equipment).orElseThrow().currentCondition() > 0,
				"field repair must restore a positive partial condition");

		ItemStack workshopMaterial = new ItemStack(Items.IRON_BLOCK);
		int workshopMaterialBefore = workshopMaterial.getCount();
		EquipmentOperationResult workshopRepair = EquipmentService.workshopRepair(equipment, workshopMaterial, true);
		helper.assertTrue(workshopRepair.success(), "workshop repair should restore full condition");
		helper.assertTrue(equipment.getCount() == equipmentCountBefore,
				"workshop repair must not duplicate or delete equipment");
		helper.assertTrue(workshopMaterial.getCount() == workshopMaterialBefore - 1,
				"workshop repair must consume exactly one configured material");
		helper.assertTrue(EquipmentService.state(equipment).orElseThrow().currentCondition()
				== EquipmentService.state(equipment).orElseThrow().maxCondition(),
				"workshop repair must restore the original maximum condition");

		EquipmentOperationResult duplicateRepair = EquipmentService.workshopRepair(
				equipment, workshopMaterial, true);
		helper.assertTrue(!duplicateRepair.success(), "a second repair without material must fail atomically");
		helper.assertTrue(equipment.getCount() == equipmentCountBefore,
				"a failed repair must not change the equipment count");

		helper.succeed();
	}

	@GameTest
	public void reforgePreservesIdentityProgressAndItemCount(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		player.setGameMode(GameType.SURVIVAL);
		ItemStack equipment = new ItemStack(ForeverEquipment.FIELD_TOOL);
		UUID identity = UUID.randomUUID();
		EquipmentState configured = EquipmentState.create(
				identity,
				100,
				0,
				Optional.of(PROSPECTOR),
				Map.of(PROSPECTOR, 17, EXCAVATOR, 41)).withCondition(7);
		EquipmentService.setState(equipment, configured);
		player.getInventory().setItem(0, equipment.copy());
		helper.assertTrue(level.getServer().saveEverything(true, true, true),
				"the dedicated server must save the equipment-bearing player inventory");
		ItemStack persisted = roundTripStack(level, player.getInventory().getItem(0));
		helper.assertTrue(EquipmentService.state(persisted).orElseThrow().equals(configured),
				"item-stack persistence must retain the complete equipment state");
		int equipmentCountBefore = persisted.getCount();
		ItemStack reforgeMaterial = new ItemStack(Items.DIAMOND);
		int reforgeMaterialBefore = reforgeMaterial.getCount();

		EquipmentOperationResult result = EquipmentService.reforge(
				persisted, EXCAVATOR, reforgeMaterial, true);

		helper.assertTrue(result.success(), "reforge should succeed for a known alternate path");
		helper.assertTrue(reforgeMaterial.getCount() == reforgeMaterialBefore - 1,
				"reforge must consume exactly one configured material");
		EquipmentState reforged = EquipmentService.state(persisted).orElseThrow();
		helper.assertTrue(persisted.getCount() == equipmentCountBefore,
				"reforge must preserve the ItemStack count");
		helper.assertTrue(reforged.identity().equals(identity), "reforge must preserve item identity");
		helper.assertTrue(reforged.currentCondition() == reforged.maxCondition(),
				"reforge must restore full condition");
		helper.assertTrue(reforged.activePath().equals(Optional.of(EXCAVATOR)),
				"reforge must switch exactly one active path");
		helper.assertTrue(reforged.progressFor(PROSPECTOR) == 17,
				"reforge must preserve prior progress for the inactive path");
		helper.assertTrue(reforged.progressFor(EXCAVATOR) == 41,
				"reforge must preserve progress for the selected path");

		EquipmentState unchanged = reforged;
		EquipmentOperationResult replay = EquipmentService.reforge(
				persisted, PROSPECTOR, ItemStack.EMPTY, true);
		helper.assertTrue(!replay.success(), "a replay without a material must fail");
		helper.assertTrue(EquipmentService.state(persisted).orElseThrow().equals(unchanged),
				"a failed replay must leave state unchanged");

		ItemStack vanilla = new ItemStack(Items.DIAMOND_PICKAXE);
		vanilla.setDamageValue(vanilla.getMaxDamage() - 1);
		int vanillaCountBefore = vanilla.getCount();
		boolean[] callbackRan = {false};
		vanilla.hurtAndBreak(1, level, player, ignored -> callbackRan[0] = true);
		helper.assertTrue(callbackRan[0] && vanilla.isEmpty() && vanillaCountBefore == 1,
				"unaffected vanilla items must retain ordinary break behaviour");

		helper.succeed();
	}

	private static ItemStack roundTripStack(ServerLevel level, ItemStack stack) {
		var ops = RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE, level.registryAccess());
		var encoded = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
		return ItemStack.CODEC.parse(ops, encoded).getOrThrow();
	}

	private static void assertBrokenTooltip(GameTestHelper helper, EquipmentState state, ItemStack stack) {
		List<Component> lines = new ArrayList<>();
		state.addToTooltip(Item.TooltipContext.EMPTY, lines::add, TooltipFlag.NORMAL, stack);
		boolean hasBrokenText = lines.stream()
				.map(Component::getContents)
				.filter(TranslatableContents.class::isInstance)
				.map(TranslatableContents.class::cast)
				.anyMatch(contents -> contents.getKey().equals("tooltip.forever.equipment.status.broken"));
		helper.assertTrue(hasBrokenText, "broken equipment tooltip must contain explicit translated text");
	}
}

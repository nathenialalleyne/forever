package dev.forever.core.equipment;

import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric's public entity damage seam for Forever equipment.
 *
 * <p>Fabric wraps the public {@code ItemStack.hurtAndBreak(int, LivingEntity, EquipmentSlot)}
 * overload before it dispatches to Minecraft's server-level break path. The handler records
 * condition in the Forever component and returns zero to vanilla. It deliberately never invokes
 * the break callback. Minecraft 26.2 then leaves the stack intact, while the mirrored vanilla
 * damage value still makes {@code ItemStack.isBroken()} true at zero condition.
 */
final class EquipmentDamageHandler implements CustomDamageHandler {

	static final EquipmentDamageHandler INSTANCE = new EquipmentDamageHandler();

	private EquipmentDamageHandler() {
	}

	@Override
	public int hurtAndBreak(
			ItemStack stack,
			int amount,
			LivingEntity entity,
			EquipmentSlot slot,
			Runnable breakCallback) {
		if (amount <= 0 || !EquipmentService.isSupported(stack)) {
			return amount;
		}

		EquipmentService.applyCustomDamage(stack, amount);
		return 0;
	}
}

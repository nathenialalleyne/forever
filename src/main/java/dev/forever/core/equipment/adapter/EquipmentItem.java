package dev.forever.core.equipment.adapter;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Base item whose use paths honour the Forever broken state. */
public final class EquipmentItem extends Item {

	public EquipmentItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		return EquipmentService.isUsable(context.getItemInHand()) ? super.useOn(context) : InteractionResult.FAIL;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		return EquipmentService.isUsable(player.getItemInHand(hand))
				? super.use(level, player, hand)
				: InteractionResult.FAIL;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		return EquipmentService.isUsable(stack) ? super.finishUsingItem(stack, level, entity) : stack;
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
		return EquipmentService.isUsable(stack) && super.releaseUsing(stack, level, entity, timeCharged);
	}

	@Override
	public boolean useOnRelease(ItemStack stack) {
		return EquipmentService.isUsable(stack) && super.useOnRelease(stack);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, BlockState state) {
		return EquipmentService.isUsable(stack) ? super.getDestroySpeed(stack, state) : 0.0F;
	}

	@Override
	public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, net.minecraft.core.BlockPos pos,
			LivingEntity entity) {
		return EquipmentService.isUsable(stack) && super.canDestroyBlock(stack, state, level, pos, entity);
	}

	@Override
	public boolean mineBlock(ItemStack stack, Level level, BlockState state, net.minecraft.core.BlockPos pos,
			LivingEntity entity) {
		return EquipmentService.isUsable(stack)
				&& super.mineBlock(stack, level, state, pos, entity);
	}

	@Override
	public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
		return EquipmentService.isUsable(stack) && super.isCorrectToolForDrops(stack, state);
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (EquipmentService.isUsable(stack)) {
			super.hurtEnemy(stack, target, attacker);
		}
	}

	@Override
	public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (EquipmentService.isUsable(stack)) {
			super.postHurtEnemy(stack, target, attacker);
		}
	}

	@Override
	public InteractionResult interactLivingEntity(
			ItemStack stack,
			Player player,
			LivingEntity entity,
			InteractionHand hand) {
		return EquipmentService.isUsable(stack)
				? super.interactLivingEntity(stack, player, entity, hand)
				: InteractionResult.FAIL;
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return EquipmentService.state(stack).isPresent() || super.isBarVisible(stack);
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return EquipmentService.state(stack)
				.map(state -> (int) Math.ceil(13.0D * state.conditionFraction()))
				.orElseGet(() -> super.getBarWidth(stack));
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return EquipmentService.state(stack)
				.filter(EquipmentState::isBroken)
				.map(ignored -> 0x8B0000)
				.orElseGet(() -> super.getBarColor(stack));
	}
}

package dev.forever.core.career;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;

/** Attributable causes allowed to transition a loaded villager career to DEAD. */
public enum PhysicalDeathCause {
	HOSTILE_ATTACK,
	FIRE,
	FALL,
	SUFFOCATION,
	DROWNING,
	DELIBERATE_VIOLENCE,
	DOCUMENTED_WORLD_EVENT;

	public static final Codec<PhysicalDeathCause> CODEC = CareerCodecs.enumCodec(PhysicalDeathCause.class);

	/**
	 * Maps only attributable vanilla damage types. Unknown or administrative damage is not
	 * converted into a fictional physical cause.
	 */
	public static Optional<PhysicalDeathCause> from(DamageSource source) {
		if (source == null) {
			return Optional.empty();
		}
		if (source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.PLAYER_EXPLOSION)) {
			return Optional.of(DELIBERATE_VIOLENCE);
		}
		if (source.is(DamageTypes.MOB_ATTACK)
				|| source.is(DamageTypes.MOB_ATTACK_NO_AGGRO)
				|| source.is(DamageTypes.MOB_PROJECTILE)
				|| source.is(DamageTypes.ARROW)
				|| source.is(DamageTypes.THROWN)) {
			return Optional.of(HOSTILE_ATTACK);
		}
		if (source.is(DamageTypes.IN_FIRE)
				|| source.is(DamageTypes.ON_FIRE)
				|| source.is(DamageTypes.LAVA)
				|| source.is(DamageTypes.CAMPFIRE)
				|| source.is(DamageTypes.HOT_FLOOR)) {
			return Optional.of(FIRE);
		}
		if (source.is(DamageTypes.FALL)
				|| source.is(DamageTypes.STALAGMITE)
				|| source.is(DamageTypes.FALLING_BLOCK)
				|| source.is(DamageTypes.FALLING_ANVIL)
				|| source.is(DamageTypes.FALLING_STALACTITE)) {
			return Optional.of(FALL);
		}
		if (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.CRAMMING)) {
			return Optional.of(SUFFOCATION);
		}
		if (source.is(DamageTypes.DROWN)) {
			return Optional.of(DROWNING);
		}
		if (source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypes.OUTSIDE_BORDER)) {
			return Optional.of(DOCUMENTED_WORLD_EVENT);
		}
		return Optional.empty();
	}
}

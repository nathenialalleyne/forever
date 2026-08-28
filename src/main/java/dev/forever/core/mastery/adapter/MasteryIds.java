package dev.forever.core.mastery;

import net.minecraft.resources.Identifier;

/** Stable IDs for the initial mastery vocabulary. */
public final class MasteryIds {

	public static final Identifier BUILDER = id("builder");
	public static final Identifier PROSPECTOR = id("prospector");
	public static final Identifier COOK = id("cook");
	public static final Identifier SMITH = id("smith");
	public static final Identifier EXPLORER_CARTOGRAPHER = id("explorer_cartographer");
	public static final Identifier MERCHANT_STEWARD = id("merchant_steward");
	public static final Identifier FARMER_NATURALIST = id("farmer_naturalist");
	public static final Identifier ALCHEMIST_HERBALIST = id("alchemist_herbalist");
	public static final Identifier RIDER_ANIMAL_HANDLER = id("rider_animal_handler");
	public static final Identifier FISHER_MARINER = id("fisher_mariner");

	private MasteryIds() {
	}

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("forever", path);
	}
}

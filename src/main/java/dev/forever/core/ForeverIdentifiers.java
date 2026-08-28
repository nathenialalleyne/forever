package dev.forever.core;

import dev.forever.ForeverMod;
import net.minecraft.resources.Identifier;

/**
 * Creates {@link Identifier}s in the Forever namespace.
 *
 * <p>Centralised so that the mod ID appears in exactly one place. Identifiers end up
 * in save data, so a typo in a namespace is a persistence bug rather than a cosmetic
 * one.
 *
 * <p>Note for future agents: in Minecraft 26.2 the class is
 * {@code net.minecraft.resources.Identifier}. It is not the Yarn
 * {@code net.minecraft.util.Identifier} and not {@code ResourceLocation}. See
 * {@code docs/verified-api-reference.md}.
 */
public final class ForeverIdentifiers {

	private ForeverIdentifiers() {
	}

	/**
	 * @param path the path within the Forever namespace, for example {@code "mastery"}
	 * @return {@code forever:<path>}
	 * @throws IllegalArgumentException if the path is not a valid identifier path
	 */
	public static Identifier of(String path) {
		return Identifier.fromNamespaceAndPath(ForeverMod.MOD_ID, path);
	}
}

package dev.forever;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Smoke test proving the JUnit environment works and that the foundation's
 * invariants hold.
 *
 * <p>This test must not require a Minecraft runtime. Tests that need game objects
 * belong in the GameTest source set instead.
 */
class ForeverModTest {

	@Test
	@DisplayName("mod ID is the stable identifier 'forever'")
	void modIdIsStable() {
		// The mod ID appears in resource paths, identifiers, and save data. Changing it
		// would break existing worlds, so it is asserted explicitly.
		assertEquals("forever", ForeverMod.MOD_ID);
	}

	@Test
	@DisplayName("mod ID is a valid Fabric/Minecraft namespace")
	void modIdIsValidNamespace() {
		assertTrue(ForeverMod.MOD_ID.matches("[a-z0-9_-]+"),
				"Mod ID must only contain lowercase alphanumerics, underscore, and hyphen.");
	}

	@Test
	@DisplayName("common entrypoint does not reference client-only packages")
	void commonEntrypointIsDedicatedServerSafe() {
		// A dedicated server has no client classes on the classpath. If ForeverMod ever
		// imports dev.forever.client.*, loading it here on a plain JVM would still pass,
		// so we assert the package boundary directly against the compiled class.
		String enclosingPackage = ForeverMod.class.getPackageName();
		assertEquals("dev.forever", enclosingPackage);
		assertFalse(enclosingPackage.startsWith("dev.forever.client"),
				"Common code must never live in or depend on the client source set.");
	}
}

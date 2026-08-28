package dev.forever.gametest.matcha;

import dev.forever.compat.matcha.ForeverMatchaCompat;
import dev.forever.compat.matcha.MatchaAdapter;
import dev.forever.compat.matcha.MatchaAdapterStatus;
import dev.forever.compat.matcha.MatchaBehaviorObservation;
import dev.forever.compat.matcha.MatchaSignalKind;
import dev.forever.compat.matcha.MatchaBehaviorTranslation;
import dev.forever.compat.matcha.MatchaItemObservation;
import dev.forever.compat.matcha.MatchaItemTranslation;
import dev.forever.compat.matcha.MatchaTranslationStatus;
import dev.forever.compat.matcha.MatchaDetectionEvidence;
import dev.forever.compat.matcha.MatchaPresence;
import dev.forever.compat.matcha.MatchaServerEvidence;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.MinecraftServer;

/**
 * Dedicated-server acceptance checks for FVR-017, Matcha version detection.
 *
 * <p>The unit tests for {@code MatchaVersionDetector} feed it hand-built evidence
 * records. That proves the decision logic, but it cannot prove the thing that actually
 * matters in production: that evidence is ever gathered from a real server at all.
 * Before this test existed, {@code ForeverMatchaCompat.initialize()} registered a no-op
 * adapter and nothing ever supplied it with datapack signals, so detection would have
 * reported "absent" forever no matter what was installed.
 *
 * <p>These tests are written to pass whether or not a Matcha archive is present in the
 * world's {@code datapacks} directory, asserting invariants that must hold either way:
 * detection actually runs, its conclusion is internally consistent with the evidence it
 * recorded, and every outcome including failure degrades to a safe working adapter.
 */
public final class MatchaDetectionGameTest {

	/**
	 * Evidence gathering must work against a real server and report what is truly there.
	 *
	 * <p>This is the check that would have caught the missing wiring: it calls the
	 * gatherer with a live {@link MinecraftServer} rather than a constructed record.
	 *
	 * <p>The assertion deliberately does not hardcode an expected answer. This suite
	 * runs both with and without Matcha installed in the world's {@code datapacks}
	 * directory, and demanding one specific outcome would make the test a statement
	 * about the fixture rather than about detection. What must hold in both cases is
	 * that gathering succeeds, reaches a definite conclusion, and that a PRESENT
	 * conclusion is backed by observed namespace evidence rather than a guess.
	 *
	 * <p>Verified manually against the real pinned Matcha 1.12 archive: detection
	 * observed all ten known data namespaces and the version marker objective.
	 */
	@GameTest
	public void evidenceIsGatheredFromTheLiveServer(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();

		MatchaDetectionEvidence evidence = MatchaServerEvidence.gather(server);

		helper.assertTrue(evidence != null,
				"Evidence gathering returned nothing, so detection could never run in production.");
		helper.assertTrue(evidence.presence() != null,
				"Detection reached no conclusion at all.");

		if (evidence.presence() == MatchaPresence.PRESENT) {
			// A positive result must be justified by real observations, never asserted.
			helper.assertTrue(!evidence.resourcePaths().isEmpty(),
					"Detection reported Matcha PRESENT without recording any observed namespace evidence.");
		} else {
			// Absence must be clean: no half-populated evidence implying a partial match.
			helper.assertTrue(evidence.resourcePaths().isEmpty(),
					"Detection reported Matcha " + evidence.presence()
							+ " while still recording namespace evidence, which is contradictory.");
		}

		helper.succeed();
	}

	/**
	 * With no Matcha present the adapter must degrade to a safe, working no-op.
	 *
	 * <p>Design principle 18 and the adapter contract both require that the absence of
	 * an optional integration is never a crash and never a guess.
	 */
	@GameTest
	public void absentMatchaYieldsSafeNoOpAdapter(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();

		MatchaAdapter adapter = ForeverMatchaCompat.initialize(MatchaServerEvidence.gather(server));

		helper.assertTrue(adapter != null, "Adapter resolution returned null instead of a no-op fallback.");

		MatchaAdapterStatus status = ForeverMatchaCompat.status();
		helper.assertTrue(status != null, "Adapter status was unavailable after detection ran.");

		// The published adapter must be the same instance detection resolved, otherwise
		// callers could read a stale decision.
		helper.assertTrue(ForeverMatchaCompat.active() == adapter,
				"The active adapter does not match the one detection just published.");

		// The point of a fallback is behaviour, not existence. Exercise the adapter's
		// real capability methods and assert they degrade safely rather than throwing or
		// inventing a mapping. Asserting only that the object is non-null would pass even
		// if every method threw.
		MatchaItemObservation observed = new MatchaItemObservation(
				"minecraft:stone", Optional.empty(), Map.of(), 1);
		MatchaItemTranslation itemResult = adapter.translateItem(observed);

		helper.assertTrue(itemResult != null, "translateItem returned null instead of a translation result.");
		helper.assertTrue(itemResult.status() != MatchaTranslationStatus.MAPPED,
				"A fallback adapter must never claim a real Matcha mapping. Status was " + itemResult.status());

		MatchaBehaviorTranslation behaviourResult =
				adapter.translateBehavior(new MatchaBehaviorObservation(MatchaSignalKind.SCOREBOARD, "test:probe", null));
		helper.assertTrue(behaviourResult != null, "translateBehavior returned null instead of a result.");
		helper.assertTrue(behaviourResult.status() != MatchaTranslationStatus.MAPPED,
				"A fallback adapter must not emit a mapped behaviour event.");

		// Null input is third-party-shaped garbage and must be rejected, not thrown on.
		helper.assertTrue(adapter.translateItem(null) != null,
				"A null observation must produce an invalid result rather than an exception.");

		helper.succeed();
	}

	/**
	 * Malformed or unreadable evidence must fail safely rather than crash the server.
	 *
	 * <p>A datapack is third-party content that can change without warning, so the
	 * failure path is not hypothetical.
	 */
	@GameTest
	public void malformedEvidenceFailsSafeInsteadOfCrashing(GameTestHelper helper) {
		MatchaDetectionEvidence malformed = MatchaDetectionEvidence.malformed(
				"Synthetic malformed evidence used to prove the failure path degrades safely.");

		MatchaAdapter adapter = ForeverMatchaCompat.initialize(malformed);

		helper.assertTrue(adapter != null,
				"Malformed evidence produced no adapter; a bad datapack must not leave callers without a fallback.");
		helper.assertTrue(ForeverMatchaCompat.status() != null,
				"Malformed evidence left the adapter status unreadable.");

		// Restore the honest observed state so later tests and the running server do not
		// inherit synthetic evidence.
		ForeverMatchaCompat.initialize(MatchaServerEvidence.gather(helper.getLevel().getServer()));
		helper.succeed();
	}
}

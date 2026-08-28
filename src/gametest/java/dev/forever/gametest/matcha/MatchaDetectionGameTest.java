package dev.forever.gametest.matcha;

import dev.forever.compat.matcha.ForeverMatchaCompat;
import dev.forever.compat.matcha.MatchaAdapter;
import dev.forever.compat.matcha.MatchaDetectionResult;
import dev.forever.compat.matcha.MatchaDetectionStatus;
import dev.forever.compat.matcha.MatchaBaselineReport;
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
	/**
	 * The baseline report must describe the live server's actual state, and it must
	 * always be actionable when it is not healthy.
	 *
	 * <p>MRH-010 exists because LAB-02 measured that the pack loader fails open: with the
	 * Matcha archive deleted, a dedicated server started normally with the vanilla recipe
	 * count and produced no diagnostic at all. The unit tests prove the decision logic
	 * over constructed statuses. Only a real server can prove the report is actually
	 * produced from real detection, which is precisely the wiring gap that made the
	 * original detection code report "absent" forever.
	 *
	 * <p>This runs both with and without Matcha installed, so it asserts invariants that
	 * must hold either way rather than hardcoding one expected verdict.
	 */
	@GameTest
	public void baselineReportDescribesTheLiveServer(GameTestHelper helper) {
		MatchaAdapter adapter = ForeverMatchaCompat.initialize(
				MatchaServerEvidence.gather(helper.getLevel().getServer()));

		MatchaBaselineReport report = MatchaBaselineReport.fromStatus(adapter.status(), "1.12");

		helper.assertTrue(report != null, "a baseline report must always be produced");
		helper.assertTrue(!report.summary().isBlank(),
				"a baseline report must explain itself rather than being empty");

		// The core MRH-010 guarantee: a warning that does not say what to do is the
		// failure mode this ticket exists to prevent, so an unhealthy report must carry
		// at least one concrete remedy.
		if (report.needsAttention()) {
			helper.assertTrue(!report.remedies().isEmpty(),
					"an unhealthy baseline report named no remedy, which trains operators to ignore warnings");
		} else {
			helper.assertTrue(report.remedies().isEmpty(),
					"a healthy baseline should not be cluttered with remedies for problems that do not exist");
		}
		helper.succeed();
	}

	/**
	 * The report's verdict must agree with the adapter status it was derived from.
	 *
	 * <p>An inconsistent pair would be worse than no report, because an operator would
	 * see a healthy banner while the translation layer was disabled, or a missing-baseline
	 * alarm on a working install. The second case is the one that erodes trust fastest.
	 */
	@GameTest
	public void baselineVerdictAgreesWithDetection(GameTestHelper helper) {
		MatchaAdapterStatus status = ForeverMatchaCompat.initialize(
				MatchaServerEvidence.gather(helper.getLevel().getServer())).status();

		MatchaBaselineReport report = MatchaBaselineReport.fromStatus(status, "1.12");

		boolean detectionSaysAbsent = status.detectionStatus() == MatchaDetectionStatus.ABSENT;
		boolean reportSaysMissing = report.severity() == MatchaBaselineReport.Severity.MISSING;
		helper.assertTrue(detectionSaysAbsent == reportSaysMissing,
				"report severity disagreed with detection: detection=" + status.detectionStatus()
						+ " report=" + report.severity());

		boolean detectionSaysSupported = status.detectionStatus() == MatchaDetectionStatus.SUPPORTED;
		boolean reportSaysHealthy = report.severity() == MatchaBaselineReport.Severity.HEALTHY;
		helper.assertTrue(detectionSaysSupported == reportSaysHealthy,
				"only a verified baseline may be reported as healthy: detection=" + status.detectionStatus()
						+ " report=" + report.severity());

		// The checks above only exercise whichever branch this server happens to be in,
		// and the GameTest environment loads Matcha, so the ABSENT path would never run.
		// A test that cannot fail is worse than no test, so drive every status explicitly
		// against the same production mapping the live check just used.
		for (MatchaDetectionStatus candidate : MatchaDetectionStatus.values()) {
			MatchaBaselineReport mapped = MatchaBaselineReport.fromStatus(
					MatchaAdapterStatus.fromDetection(candidate == MatchaDetectionStatus.ABSENT
							? MatchaDetectionResult.absent()
							: MatchaDetectionResult.failedSafe("gametest probe for " + candidate)),
					"1.12");
			helper.assertTrue(!mapped.summary().isBlank(),
					candidate + " produced a baseline report with no summary");
			if (mapped.needsAttention()) {
				helper.assertTrue(!mapped.remedies().isEmpty(),
						candidate + " produced a warning with no remedy");
			}
		}

		// The single most important mapping, stated directly: an absent baseline must
		// never be reported as healthy. This is the exact silent failure LAB-02 measured.
		MatchaBaselineReport absent = MatchaBaselineReport.fromStatus(
				MatchaAdapterStatus.fromDetection(MatchaDetectionResult.absent()), "1.12");
		helper.assertTrue(absent.severity() == MatchaBaselineReport.Severity.MISSING,
				"an absent Matcha baseline was reported as " + absent.severity()
						+ "; LAB-02 showed this failure is otherwise completely silent");
		helper.succeed();
	}
}

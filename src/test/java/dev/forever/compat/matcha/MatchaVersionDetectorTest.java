package dev.forever.compat.matcha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatchaVersionDetectorTest {
	private final MatchaVersionDetector detector = new MatchaVersionDetector();

	@Test
	@DisplayName("accepts the exact locked Matcha 1.12 evidence")
	void acceptsPresentPinnedProfile() {
		MatchaDetectionResult result = detector.detect(supportedEvidence());

		assertEquals(MatchaDetectionStatus.SUPPORTED, result.status());
		assertEquals(MatchaConfidence.EXACT, result.confidence());
		assertEquals("1.12", result.detectedVersion().orElseThrow());
		assertTrue(result.diagnostics().stream().noneMatch(item -> item.contains("mismatch")));
	}

	@Test
	@DisplayName("uses a normal absent result without treating absence as an error")
	void reportsAbsentPack() {
		MatchaDetectionResult result = detector.detect(MatchaDetectionEvidence.absent());

		assertEquals(MatchaDetectionStatus.ABSENT, result.status());
		assertEquals(MatchaSupportLevel.NONE, result.supportLevel());
		assertEquals(MatchaConfidence.NOT_APPLICABLE, result.confidence());
		assertFalse(result.detectedVersion().isPresent());
	}

	@Test
	@DisplayName("refuses an archive whose digest belongs to another version")
	void refusesUnexpectedVersion() {
		MatchaDetectionEvidence evidence = new MatchaDetectionEvidence(
				MatchaPresence.PRESENT,
				java.util.Optional.of("0".repeat(64)),
				java.util.Optional.of(metadata()),
				MatchaProfile.FINGERPRINTS,
				MatchaProfile.FINGERPRINTS.keySet(),
				runtimeMarkers(),
				java.util.List.of());

		MatchaDetectionResult result = detector.detect(evidence);

		assertEquals(MatchaDetectionStatus.UNSUPPORTED_VERSION, result.status());
		assertEquals(MatchaSupportLevel.NONE, result.supportLevel());
		assertFalse(result.detectedVersion().isPresent());
		assertTrue(result.diagnostics().stream().anyMatch(item -> item.contains("SHA-256")));
	}

	@Test
	@DisplayName("refuses a fingerprint conflict as an unsupported version")
	void refusesFingerprintConflict() {
		Map<String, String> fingerprints = new java.util.HashMap<>(MatchaProfile.FINGERPRINTS);
		fingerprints.put("pack.mcmeta", "1".repeat(64));
		MatchaDetectionEvidence evidence = new MatchaDetectionEvidence(
				MatchaPresence.PRESENT,
				java.util.Optional.of(MatchaProfile.ARCHIVE_SHA256),
				java.util.Optional.of(metadata()),
				fingerprints,
				fingerprints.keySet(),
				runtimeMarkers(),
				java.util.List.of());

		assertEquals(MatchaDetectionStatus.UNSUPPORTED_VERSION, detector.detect(evidence).status());
	}

	@Test
	@DisplayName("refuses a conflicting runtime version marker as an unsupported version")
	void refusesRuntimeMarkerConflict() {
		MatchaDetectionEvidence evidence = new MatchaDetectionEvidence(
				MatchaPresence.PRESENT,
				java.util.Optional.of(MatchaProfile.ARCHIVE_SHA256),
				java.util.Optional.of(metadata()),
				MatchaProfile.FINGERPRINTS,
				MatchaProfile.FINGERPRINTS.keySet(),
				MatchaRuntimeMarkers.observed(
						Set.of(MatchaProfile.VERSION_OBJECTIVE),
						Map.of(MatchaProfile.VERSION_SCORE_HOLDER, 113),
						MatchaProfile.ADVANCEMENT_MARKERS),
				java.util.List.of());

		assertEquals(MatchaDetectionStatus.UNSUPPORTED_VERSION, detector.detect(evidence).status());
	}

	@Test
	@DisplayName("does not guess when Matcha-like content lacks locked provenance")
	void refusesAmbiguousSignals() {
		MatchaDetectionEvidence evidence = MatchaDetectionEvidence.present(
				null,
				metadata(),
				Map.of("pack.mcmeta", MatchaProfile.FINGERPRINTS.get("pack.mcmeta")),
				Set.of("data/main/function/setup/load.mcfunction"),
				MatchaRuntimeMarkers.unavailable());

		MatchaDetectionResult result = detector.detect(evidence);

		assertEquals(MatchaDetectionStatus.PRESENT_UNVERIFIED, result.status());
		assertEquals(MatchaConfidence.UNVERIFIED, result.confidence());
		assertTrue(result.diagnostics().stream().anyMatch(item -> item.contains("unverified")));
	}

	@Test
	@DisplayName("malformed evidence is visible and remains disabled")
	void reportsMalformedEvidence() {
		MatchaDetectionResult result = detector.detect(MatchaDetectionEvidence.malformed(
				"The pack metadata could not be parsed; re-audit the archive."));

		assertEquals(MatchaDetectionStatus.MALFORMED, result.status());
		assertEquals(MatchaPresence.MALFORMED, result.presence());
		assertEquals(MatchaSupportLevel.NONE, result.supportLevel());
	}

	@Test
	@DisplayName("diagnostics are deterministic for the same evidence")
	void diagnosticsAreDeterministic() {
		MatchaDetectionResult first = detector.detect(supportedEvidence());
		MatchaDetectionResult second = detector.detect(supportedEvidence());

		assertEquals(first, second);
	}

	@Test
	@DisplayName("does not enable mappings before runtime markers are confirmed")
	void requiresRuntimeMarkers() {
		MatchaDetectionEvidence evidence = new MatchaDetectionEvidence(
				MatchaPresence.PRESENT,
				java.util.Optional.of(MatchaProfile.ARCHIVE_SHA256),
				java.util.Optional.of(metadata()),
				MatchaProfile.FINGERPRINTS,
				MatchaProfile.FINGERPRINTS.keySet(),
				MatchaRuntimeMarkers.unavailable(),
				java.util.List.of());

		assertEquals(MatchaDetectionStatus.PRESENT_UNVERIFIED, detector.detect(evidence).status());
	}

	@Test
	@DisplayName("does not enable mappings without the complete audited fingerprint set")
	void requiresFingerprints() {
		MatchaDetectionEvidence evidence = new MatchaDetectionEvidence(
				MatchaPresence.PRESENT,
				java.util.Optional.of(MatchaProfile.ARCHIVE_SHA256),
				java.util.Optional.of(metadata()),
				Map.of(),
				Set.of(),
				runtimeMarkers(),
				java.util.List.of());

		assertEquals(MatchaDetectionStatus.PRESENT_UNVERIFIED, detector.detect(evidence).status());
	}

	@Test
	@DisplayName("absent Matcha has a callable no-op identity and behaviour fallback")
	void noOpFallbackIsCallable() {
		MatchaAdapter adapter = MatchaAdapters.fromEvidence(MatchaDetectionEvidence.absent());
		MatchaItemObservation item = new MatchaItemObservation(
				"minecraft:stick",
				java.util.Optional.empty(),
				Map.of(
						"minecraft:item_model", "minecraft/matcha_private_model",
						"minecraft:custom_name", "ordinary stick"),
				1);

		assertInstanceOf(NoOpMatchaAdapter.class, adapter);
		assertFalse(adapter.supports(MatchaCapability.ITEM_IDENTITY_TRANSLATION));
		assertEquals(MatchaTranslationStatus.UNAVAILABLE, adapter.translateItem(item).status());
		VanillaItemFallback fallback = adapter.translateItem(item).original().orElseThrow();
		assertEquals("minecraft:stick", fallback.baseItemId());
		assertEquals(1, fallback.count());
		assertEquals(Map.of("minecraft:custom_name", "ordinary stick"), fallback.components());
		assertEquals(MatchaTranslationStatus.UNAVAILABLE,
				adapter.translateBehavior(new MatchaBehaviorObservation(MatchaSignalKind.ADVANCEMENT, "opaque:event")).status());
		assertEquals(MatchaTranslationStatus.INVALID, adapter.translateItem(null).status());
	}

	@Test
	@DisplayName("verified adapter translates only audited exact signatures")
	void verifiedAdapterMapsExactIdentity() {
		MatchaAdapter adapter = MatchaAdapters.fromEvidence(supportedEvidence());
		MatchaItemTranslation translated = adapter.translateItem(
				new MatchaItemObservation("minecraft:poisonous_potato", "minecraft:heart_container", 1));

		assertInstanceOf(VerifiedMatchaAdapter.class, adapter);
		assertTrue(adapter.supports(MatchaCapability.ITEM_IDENTITY_TRANSLATION));
		assertEquals(MatchaTranslationStatus.MAPPED, translated.status());
		assertEquals("forever.item.heart_container", translated.stableConceptId().orElseThrow());
		assertTrue(adapter.translateItem(new MatchaItemObservation("minecraft:poisonous_potato", java.util.Optional.empty(), 1))
				.status() != MatchaTranslationStatus.MAPPED);
	}

	@Test
	@DisplayName("the static entry point starts safely without server wiring")
	void staticEntryStartsNoOp() {
		MatchaAdapter adapter = ForeverMatchaCompat.initialize();

		assertEquals(MatchaDetectionStatus.ABSENT, adapter.status().detectionStatus());
		assertEquals(adapter.status(), ForeverMatchaCompat.status());
	}

	private static MatchaDetectionEvidence supportedEvidence() {
		return new MatchaDetectionEvidence(
				MatchaPresence.PRESENT,
				java.util.Optional.of(MatchaProfile.ARCHIVE_SHA256),
				java.util.Optional.of(metadata()),
				MatchaProfile.FINGERPRINTS,
				MatchaProfile.FINGERPRINTS.keySet(),
				runtimeMarkers(),
				java.util.List.of());
	}

	private static MatchaPackMetadata metadata() {
		return new MatchaPackMetadata(
				"matcha-flavoured",
				"Klei's Matcha Flavoured\n1.12 for 26.2",
				88.0,
				107.1);
	}

	private static MatchaRuntimeMarkers runtimeMarkers() {
		return MatchaRuntimeMarkers.observed(
				Set.of(MatchaProfile.VERSION_OBJECTIVE),
				Map.of(MatchaProfile.VERSION_SCORE_HOLDER, MatchaProfile.VERSION_SCORE),
				MatchaProfile.ADVANCEMENT_MARKERS);
	}
}

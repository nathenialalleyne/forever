package dev.forever.compat.matcha;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Detects the pinned Matcha profile from server-observable, exact signals.
 * No loader-visible mod API is used because Matcha is a datapack.
 */
public final class MatchaVersionDetector {

	public MatchaDetectionResult detect(MatchaDetectionEvidence evidence) {
		if (evidence == null) {
			return MatchaDetectionResult.failedSafe(
					"Matcha detection received no evidence. Keep the adapter disabled and provide a server signal snapshot.");
		}
		if (evidence.presence() == MatchaPresence.ABSENT) {
			return MatchaDetectionResult.absent();
		}
		if (evidence.presence() == MatchaPresence.MALFORMED) {
			return result(
					MatchaDetectionStatus.MALFORMED,
					MatchaPresence.MALFORMED,
					MatchaConfidence.UNVERIFIED,
					evidence.diagnostics().isEmpty()
							? List.of("Matcha evidence is malformed. Re-acquire or re-audit the pinned archive before enabling it.")
							: evidence.diagnostics());
		}

		List<String> diagnostics = new ArrayList<>(evidence.diagnostics());
		boolean archiveMatches = checkArchive(evidence, diagnostics);
		if (evidence.archiveSha256().isPresent() && !archiveMatches) {
			return result(
					MatchaDetectionStatus.UNSUPPORTED_VERSION,
					MatchaPresence.PRESENT,
					MatchaConfidence.UNVERIFIED,
					diagnostics);
		}

		MetadataCheck metadata = checkMetadata(evidence.packMetadata().orElse(null), diagnostics);
		if (metadata.explicitConflict()) {
			return result(
					MatchaDetectionStatus.UNSUPPORTED_VERSION,
					MatchaPresence.PRESENT,
					MatchaConfidence.UNVERIFIED,
					diagnostics);
		}

		boolean fingerprintsMatch = checkFingerprints(evidence.fileFingerprints(), diagnostics);
		boolean runtimeMarkersMatch = checkRuntimeMarkers(evidence.runtimeMarkers(), diagnostics);
		boolean identityEvidence = archiveMatches && metadata.matches();
		boolean corroborated = fingerprintsMatch && runtimeMarkersMatch;
		if (identityEvidence && corroborated) {
			return new MatchaDetectionResult(
					MatchaDetectionStatus.SUPPORTED,
					MatchaPresence.PRESENT,
					MatchaSupportLevel.FULL,
					MatchaConfidence.EXACT,
					java.util.Optional.of(MatchaProfile.VERSION),
					normaliseDiagnostics(diagnostics));
		}

		if (!identityEvidence) {
			diagnostics.add("Matcha identity is not proven by both the locked archive digest and pack metadata.");
		}
		if (!corroborated) {
			diagnostics.add("No complete audited fingerprint set or loaded runtime marker set was observed.");
		}
		return result(
				MatchaDetectionStatus.PRESENT_UNVERIFIED,
				MatchaPresence.PRESENT,
				MatchaConfidence.UNVERIFIED,
				diagnostics);
	}

	private static boolean checkArchive(MatchaDetectionEvidence evidence, List<String> diagnostics) {
		if (evidence.archiveSha256().isEmpty()) {
			diagnostics.add("No locked archive SHA-256 was supplied. A copied directory remains present but unverified.");
			return false;
		}
		String observed = evidence.archiveSha256().orElseThrow();
		if (!MatchaProfile.ARCHIVE_SHA256.equalsIgnoreCase(observed)) {
			diagnostics.add("Observed archive SHA-256 " + observed + " does not match the pinned Matcha 1.12 digest "
					+ MatchaProfile.ARCHIVE_SHA256 + ". Do not enable version-specific mappings.");
			return false;
		}
		return true;
	}

	private static MetadataCheck checkMetadata(MatchaPackMetadata metadata, List<String> diagnostics) {
		if (metadata == null) {
			diagnostics.add("pack.mcmeta was not supplied. Parse the loaded pack metadata before enabling Matcha mappings.");
			return new MetadataCheck(false, false);
		}

		boolean explicitConflict = metadata.declaredVersion()
				.map(version -> !MatchaProfile.VERSION.equals(version))
				.orElse(false);
		if (explicitConflict) {
			diagnostics.add("pack.mcmeta declares version " + metadata.declaredVersion().orElseThrow()
					+ ", but the only supported profile is Matcha 1.12.");
			return new MetadataCheck(false, true);
		}
		if (Double.compare(metadata.minFormat(), MatchaProfile.MIN_PACK_FORMAT) != 0
				|| Double.compare(metadata.maxFormat(), MatchaProfile.MAX_PACK_FORMAT) != 0) {
			diagnostics.add("pack.mcmeta format range " + metadata.minFormat() + ".." + metadata.maxFormat()
					+ " does not match the audited 1.12 range " + MatchaProfile.MIN_PACK_FORMAT + ".."
					+ MatchaProfile.MAX_PACK_FORMAT + ".");
			return new MetadataCheck(false, true);
		}
		if (!normaliseDescription(metadata.description()).equals(MatchaProfile.DESCRIPTION)) {
			diagnostics.add("pack.mcmeta description is not the exact audited Matcha 1.12 description.");
			return new MetadataCheck(false, false);
		}
		if (metadata.packId().isPresent() && !isKnownPackId(metadata.packId().orElseThrow())) {
			diagnostics.add("The server exposed an unrecognised Matcha pack ID. Treat the content as ambiguous.");
			return new MetadataCheck(false, false);
		}
		return new MetadataCheck(true, false);
	}

	private static boolean checkFingerprints(Map<String, String> observed, List<String> diagnostics) {
		if (observed.isEmpty()) {
			return false;
		}
		boolean complete = true;
		for (Map.Entry<String, String> expected : MatchaProfile.FINGERPRINTS.entrySet()) {
			String actual = observed.get(expected.getKey());
			if (actual == null) {
				complete = false;
				continue;
			}
			if (!expected.getValue().equalsIgnoreCase(actual)) {
				diagnostics.add("Fingerprint mismatch for audited path " + expected.getKey()
						+ ". Do not use the 1.12 mapping profile.");
				return false;
			}
		}
		if (!complete) {
			diagnostics.add("Only a partial Matcha fingerprint set was observed.");
		}
		return complete;
	}

	private static boolean checkRuntimeMarkers(MatchaRuntimeMarkers markers, List<String> diagnostics) {
		if (!markers.observationsAvailable()) {
			return false;
		}
		boolean objective = markers.scoreboardObjectives().contains(MatchaProfile.VERSION_OBJECTIVE);
		Integer version = markers.scoreboardValues().get(MatchaProfile.VERSION_SCORE_HOLDER);
		if (version != null && version != MatchaProfile.VERSION_SCORE) {
			diagnostics.add("Runtime version marker value " + version + " does not match the audited 1.12 marker.");
			return false;
		}
		boolean advancements = markers.advancementIds().containsAll(MatchaProfile.ADVANCEMENT_MARKERS);
		if (!objective || version == null || !advancements) {
			diagnostics.add("Loaded runtime markers are incomplete. Marker absence is not treated as proof of a version.");
			return false;
		}
		return true;
	}

	private static boolean isKnownPackId(String packId) {
		String normalised = packId.trim().toLowerCase(Locale.ROOT);
		return normalised.equals("matcha-flavoured")
				|| normalised.equals("matcha_flavoured")
				|| normalised.equals("matcha");
	}

	private static String normaliseDescription(String description) {
		return Objects.requireNonNull(description, "description must not be null.")
				.replaceAll("\\s+", " ")
				.trim()
				.toLowerCase(Locale.ROOT);
	}

	private static MatchaDetectionResult result(
			MatchaDetectionStatus status,
			MatchaPresence presence,
			MatchaConfidence confidence,
			List<String> diagnostics) {
		return new MatchaDetectionResult(
				status,
				presence,
				status == MatchaDetectionStatus.SUPPORTED ? MatchaSupportLevel.FULL : MatchaSupportLevel.NONE,
				confidence,
				java.util.Optional.empty(),
				normaliseDiagnostics(diagnostics));
	}

	private static List<String> normaliseDiagnostics(List<String> diagnostics) {
		List<String> copy = diagnostics.stream()
				.filter(Objects::nonNull)
				.map(String::trim)
				.filter(value -> !value.isEmpty())
				.limit(16)
				.toList();
		return copy.isEmpty() ? List.of("No diagnostic was recorded.") : copy;
	}

	private record MetadataCheck(boolean matches, boolean explicitConflict) {
	}
}

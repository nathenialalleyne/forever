package dev.forever.compat.matcha;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Bounded evidence collected from an installed Matcha datapack or archive.
 * This type contains observations only. It does not make a support decision.
 */
public record MatchaDetectionEvidence(
		MatchaPresence presence,
		Optional<String> archiveSha256,
		Optional<MatchaPackMetadata> packMetadata,
		Map<String, String> fileFingerprints,
		Set<String> resourcePaths,
		MatchaRuntimeMarkers runtimeMarkers,
		List<String> diagnostics) {

	private static final Pattern SHA256 = Pattern.compile("[0-9a-fA-F]{64}");
	private static final int MAX_PATH_LENGTH = 512;
	private static final int MAX_DIAGNOSTICS = 16;
	private static final int MAX_DIAGNOSTIC_LENGTH = 512;

	public MatchaDetectionEvidence {
		Objects.requireNonNull(presence, "presence must not be null.");
		archiveSha256 = normaliseHash(archiveSha256);
		packMetadata = Objects.requireNonNull(packMetadata, "pack metadata must not be null.");
		fileFingerprints = immutableFingerprints(fileFingerprints);
		resourcePaths = immutablePaths(resourcePaths);
		runtimeMarkers = Objects.requireNonNull(runtimeMarkers, "runtime markers must not be null.");
		diagnostics = immutableDiagnostics(diagnostics);
		if (presence == MatchaPresence.MALFORMED && diagnostics.isEmpty()) {
			throw new IllegalArgumentException("Malformed evidence requires an actionable diagnostic.");
		}
	}

	public static MatchaDetectionEvidence absent() {
		return new MatchaDetectionEvidence(
				MatchaPresence.ABSENT,
				Optional.empty(),
				Optional.empty(),
				Map.of(),
				Set.of(),
				MatchaRuntimeMarkers.unavailable(),
				List.of());
	}

	public static MatchaDetectionEvidence present(
			String archiveSha256,
			MatchaPackMetadata metadata,
			Map<String, String> fileFingerprints,
			Set<String> resourcePaths,
			MatchaRuntimeMarkers runtimeMarkers) {
		return new MatchaDetectionEvidence(
				MatchaPresence.PRESENT,
				Optional.ofNullable(archiveSha256),
				Optional.ofNullable(metadata),
				fileFingerprints,
				resourcePaths,
				runtimeMarkers == null ? MatchaRuntimeMarkers.unavailable() : runtimeMarkers,
				List.of());
	}

	public static MatchaDetectionEvidence malformed(String diagnostic) {
		return new MatchaDetectionEvidence(
				MatchaPresence.MALFORMED,
				Optional.empty(),
				Optional.empty(),
				Map.of(),
				Set.of(),
				MatchaRuntimeMarkers.unavailable(),
				List.of(diagnostic));
	}

	private static Optional<String> normaliseHash(Optional<String> hash) {
		Objects.requireNonNull(hash, "archive SHA-256 must not be null.");
		return hash.map(value -> {
			if (!SHA256.matcher(value).matches()) {
				throw new IllegalArgumentException("Archive SHA-256 must contain exactly 64 hexadecimal characters.");
			}
			return value.toLowerCase(java.util.Locale.ROOT);
		});
	}

	private static Map<String, String> immutableFingerprints(Map<String, String> fingerprints) {
		Objects.requireNonNull(fingerprints, "file fingerprints must not be null.");
		if (fingerprints.size() > 256) {
			throw new IllegalArgumentException("File fingerprints exceed the maximum of 256 entries.");
		}
		Map<String, String> copy = new TreeMap<>();
		for (Map.Entry<String, String> entry : fingerprints.entrySet()) {
			String path = normalisePath(entry.getKey());
			String hash = entry.getValue();
			if (hash == null || !SHA256.matcher(hash).matches()) {
				throw new IllegalArgumentException("Fingerprint for " + path + " is not a SHA-256 value.");
			}
			copy.put(path, hash.toLowerCase(java.util.Locale.ROOT));
		}
		return Collections.unmodifiableMap(copy);
	}

	private static Set<String> immutablePaths(Set<String> paths) {
		Objects.requireNonNull(paths, "resource paths must not be null.");
		if (paths.size() > 1024) {
			throw new IllegalArgumentException("Resource paths exceed the maximum of 1024 entries.");
		}
		Set<String> copy = new LinkedHashSet<>();
		for (String path : paths) {
			copy.add(normalisePath(path));
		}
		return Collections.unmodifiableSet(copy);
	}

	private static String normalisePath(String path) {
		Objects.requireNonNull(path, "resource path must not be null.");
		String normalised = path.replace('\\', '/');
		if (normalised.isBlank() || normalised.length() > MAX_PATH_LENGTH || normalised.startsWith("/")
				|| normalised.contains("\0")) {
			throw new IllegalArgumentException("Resource path is empty, absolute, too long, or contains a NUL byte.");
		}
		for (String segment : normalised.split("/", -1)) {
			if (segment.equals("..") || segment.isEmpty() || segment.equals(".")) {
				throw new IllegalArgumentException("Resource path is not a normalised relative path: " + path);
			}
		}
		return normalised;
	}

	private static List<String> immutableDiagnostics(List<String> values) {
		Objects.requireNonNull(values, "diagnostics must not be null.");
		if (values.size() > MAX_DIAGNOSTICS) {
			throw new IllegalArgumentException("Diagnostics exceed the maximum of " + MAX_DIAGNOSTICS + " entries.");
		}
		List<String> copy = values.stream().map(value -> {
			if (value == null || value.isBlank() || value.length() > MAX_DIAGNOSTIC_LENGTH) {
				throw new IllegalArgumentException("Diagnostics must be non-blank and bounded.");
			}
			return value;
		}).toList();
		return List.copyOf(copy);
	}
}

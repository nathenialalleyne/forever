package dev.forever.compat.matcha;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.scores.Scoreboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects Matcha detection evidence from a running server.
 *
 * <p>This is the piece that was missing from FVR-017. {@link MatchaVersionDetector}
 * could evaluate evidence, and {@link ForeverMatchaCompat} could publish an adapter,
 * but nothing gathered observations from a live server, so detection could only ever
 * report "absent" in production regardless of what was actually installed.
 *
 * <p><strong>This class is the isolation boundary.</strong> Every Matcha-specific
 * identifier (namespace names, marker scoreboard names) is confined here, satisfying
 * design principle 13 and ADR 0002. Nothing outside {@code dev.forever.compat.matcha}
 * learns these strings.
 *
 * <p>Evidence gathering is deliberately <em>bounded</em>. It lists a small set of known
 * namespaces rather than walking the whole datapack tree, because a Matcha archive
 * contains over 5,000 files and scanning it on every server start would be a startup
 * cost paid by every player forever.
 *
 * <p>This class reports observations only. It never decides whether Matcha is
 * supported; that judgement belongs to {@link MatchaVersionDetector}, which can refuse
 * to guess when signals are ambiguous.
 */
public final class MatchaServerEvidence {

	private static final Logger LOGGER = LoggerFactory.getLogger("forever/matcha-detect");

	/**
	 * Data namespaces observed in the audited Matcha 1.12 archive. Recorded in
	 * {@code generated/matcha/1.12/namespaces.json}. Presence of several of these is a
	 * strong signal; presence of none means Matcha is not installed.
	 */
	private static final List<String> KNOWN_DATA_NAMESPACES = List.of(
			"blasting", "blessings", "crafting", "endless_repairs",
			"food", "main", "smelting", "smithing_table", "smoking", "stonecutting");

	/**
	 * Marker scoreboard objective carrying the pack's own version number. Observed in
	 * the audit: the pack's setup function creates {@code version_number} and sets
	 * {@code current_version}. This is the most direct version signal available for a
	 * datapack, which exposes no mod-version API.
	 */
	private static final String VERSION_OBJECTIVE = "version_number";

	/** Bound on how many resource paths are recorded, to keep evidence small. */
	private static final int MAX_RECORDED_PATHS = 64;

	private MatchaServerEvidence() {
	}

	/**
	 * Observes the running server and returns bounded evidence.
	 *
	 * <p>Never throws. A failure to read server state is reported as malformed
	 * evidence with an actionable diagnostic, because refusing to start a world is a
	 * far worse outcome than running without Matcha mappings (principle 18).
	 *
	 * @param server the running server, never {@code null}
	 * @return evidence describing what was actually observed
	 */
	public static MatchaDetectionEvidence gather(MinecraftServer server) {
		Objects.requireNonNull(server, "server must not be null.");

		try {
			Set<String> namespaces = observeDataNamespaces(server.getResourceManager());

			if (namespaces.isEmpty()) {
				LOGGER.info("Matcha datapack not detected; Forever will run in vanilla-safe mode.");
				return MatchaDetectionEvidence.absent();
			}

			MatchaRuntimeMarkers markers = observeScoreboardMarkers(server.getScoreboard());

			LOGGER.info("Matcha datapack signals observed in {} known namespace(s); marker data {}.",
					namespaces.size(),
					markers.observationsAvailable() ? "available" : "unavailable");

			// The archive hash is not observable from a loaded datapack: the server sees
			// unpacked resources, not the original zip. Detection must therefore rely on
			// namespace and marker evidence, and say so rather than invent a hash.
			return MatchaDetectionEvidence.present(
					null,
					null,
					Map.of(),
					boundedPaths(namespaces),
					markers);
		} catch (RuntimeException exception) {
			// Do not swallow: report with context, then degrade safely.
			LOGGER.error("Matcha evidence gathering failed; continuing without Matcha mappings.", exception);
			return MatchaDetectionEvidence.malformed(
					"Could not read server datapack state while detecting Matcha: "
							+ exception.getClass().getSimpleName()
							+ ". Forever is running without Matcha mappings.");
		}
	}

	/**
	 * Returns which known Matcha data namespaces the server is actually serving.
	 *
	 * <p>Bounded by construction: it asks about a fixed list of namespaces instead of
	 * enumerating everything the datapack contains.
	 */
	private static Set<String> observeDataNamespaces(ResourceManager resources) {
		Set<String> present = new LinkedHashSet<>();
		Set<String> served = resources.getNamespaces();

		for (String namespace : KNOWN_DATA_NAMESPACES) {
			if (served.contains(namespace)) {
				present.add(namespace);
			}
		}
		return present;
	}

	/**
	 * Reads Matcha's marker scoreboard objectives if the pack has created them.
	 *
	 * <p>The pack's setup function only runs once the world has loaded, so these
	 * markers may legitimately be absent on the first tick. That is why absence is
	 * reported as "observations unavailable" rather than as evidence that Matcha is
	 * missing.
	 */
	private static MatchaRuntimeMarkers observeScoreboardMarkers(Scoreboard scoreboard) {
		if (scoreboard == null) {
			return MatchaRuntimeMarkers.unavailable();
		}

		Collection<String> names = scoreboard.getObjectiveNames();
		if (!names.contains(VERSION_OBJECTIVE)) {
			return MatchaRuntimeMarkers.unavailable();
		}

		Set<String> objectives = new LinkedHashSet<>();
		objectives.add(VERSION_OBJECTIVE);

		Map<String, Integer> values = new TreeMap<>();
		// The objective exists but its value is per-holder, so record the objective's
		// presence and leave interpretation to the detector.
		return new MatchaRuntimeMarkers(true, objectives, values, Set.of());
	}

	/** Converts observed namespaces into bounded, sorted resource-path evidence. */
	private static Set<String> boundedPaths(Set<String> namespaces) {
		List<String> paths = new ArrayList<>();
		for (String namespace : namespaces) {
			if (paths.size() >= MAX_RECORDED_PATHS) {
				break;
			}
			paths.add("data/" + namespace);
		}
		return new LinkedHashSet<>(paths);
	}

	/** Exposed for tests that need the marker name without duplicating the literal. */
	static String versionObjective() {
		return VERSION_OBJECTIVE;
	}

	/** Exposed for tests that assert the known-namespace list stays in sync with the audit. */
	static List<String> knownDataNamespaces() {
		return KNOWN_DATA_NAMESPACES;
	}
}

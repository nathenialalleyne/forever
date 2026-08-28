package dev.forever.compat.matcha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Keeps the detection probe honest against the committed audit of the pinned archive.
 *
 * <p>{@link MatchaServerEvidence} decides whether Matcha is installed by probing for a
 * fixed list of data namespaces. That list is a claim about the real archive, and a
 * claim that silently drifts from reality is worse than no detection at all: it would
 * report absence on a world that genuinely has Matcha installed.
 *
 * <p>The audit reports in {@code generated/matcha/1.12/} are deterministic output
 * generated from the pinned archive whose SHA-256 is recorded in
 * {@code matcha.lock.json}, so they are the authoritative record of what that archive
 * actually contains.
 */
class MatchaServerEvidenceTest {

	private static final Path NAMESPACES_REPORT = Path.of("generated/matcha/1.12/namespaces.json");

	/**
	 * Reads the data namespaces the audit actually observed in the pinned archive.
	 *
	 * <p>The report's {@code data} array holds objects of the form
	 * {@code {namespace, side, fileCount, categories, observationType}}, so this reads
	 * the {@code namespace} field rather than treating entries as bare strings.
	 */
	private static List<String> auditedDataNamespaces() throws IOException {
		JsonObject report = JsonParser
				.parseString(Files.readString(NAMESPACES_REPORT, StandardCharsets.UTF_8))
				.getAsJsonObject();

		assertTrue(report.has("data"), "The audit report has no data namespace list: " + NAMESPACES_REPORT);

		List<String> namespaces = new ArrayList<>();
		for (JsonElement element : report.getAsJsonArray("data")) {
			JsonObject entry = element.getAsJsonObject();
			assertTrue(entry.has("namespace"),
					"An audit data entry has no namespace field, so the report format changed: " + entry);
			namespaces.add(entry.get("namespace").getAsString());
		}
		return namespaces;
	}

	@Test
	@DisplayName("every probed namespace really exists in the pinned Matcha archive")
	void probedNamespacesExistInTheAuditedArchive() throws IOException {
		List<String> audited = auditedDataNamespaces();
		assertFalse(audited.isEmpty(), "The audit recorded no data namespaces, so the report is unusable.");

		List<String> phantom = MatchaServerEvidence.knownDataNamespaces().stream()
				.filter(ns -> !audited.contains(ns))
				.toList();

		assertTrue(phantom.isEmpty(),
				"Detection probes namespaces that do not exist in the pinned archive, so they can never "
						+ "match and weaken the signal: " + phantom + ". Audited: " + audited);
	}

	@Test
	@DisplayName("the probe never includes minecraft, which every world serves")
	void probeExcludesVanillaNamespace() {
		// The audit lists 'minecraft' among Matcha's data namespaces because the pack
		// overrides vanilla content. Probing for it would match every world in
		// existence and report Matcha as present always.
		assertFalse(MatchaServerEvidence.knownDataNamespaces().contains("minecraft"),
				"Probing the vanilla namespace would make detection a guaranteed false positive.");
	}

	@Test
	@DisplayName("the probe list is a deduplicated, non-empty set of signals")
	void probeListIsWellFormed() {
		List<String> known = MatchaServerEvidence.knownDataNamespaces();

		assertFalse(known.isEmpty(), "Detection with no probe list could never find Matcha.");
		assertEquals(known.size(), known.stream().distinct().count(),
				"Duplicate namespaces in the probe list overstate the strength of the evidence.");
		assertTrue(known.stream().noneMatch(String::isBlank), "A blank namespace cannot be observed.");
	}

	@Test
	@DisplayName("the version marker objective matches the audited pack marker")
	void versionObjectiveMatchesAuditedMarker() {
		// Recorded in the audit's scoreboard analysis: the pack's setup function creates
		// 'version_number'. This is the only direct version signal a datapack offers.
		assertEquals("version_number", MatchaServerEvidence.versionObjective(),
				"The version marker no longer matches the audited pack marker, so version detection "
						+ "would silently stop working.");
	}
}

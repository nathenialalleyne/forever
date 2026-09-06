package dev.forever.compat.matcha.diagnostic;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.scores.Scoreboard;

/** Performs one bounded, read-only archive and server observation. */
final class MatchaDiagnosticObserver {
	private static final String DATAPACK_DIRECTORY = "datapacks";
	private static final String VERSION_OBJECTIVE = "version_number";
	private static final List<String> KNOWN_DATA_NAMESPACES = List.of(
			"blasting", "blessings", "crafting", "endless_repairs", "food",
			"main", "smelting", "smithing_table", "smoking", "stonecutting");

	private MatchaDiagnosticObserver() {
	}

	static MatchaDiagnosticReport observe(MinecraftServer server) {
		Objects.requireNonNull(server, "server must not be null.");
		Set<String> serverNamespaces;
		try {
			ResourceManager resources = server.getResourceManager();
			if (resources == null || resources.getNamespaces() == null) {
				return malformed("The server did not expose a resource manager namespace set.");
			}
			serverNamespaces = observedNamespaces(resources.getNamespaces());
		} catch (RuntimeException exception) {
			return malformed("Could not read the server's bounded datapack namespace evidence: "
					+ exception.getClass().getSimpleName() + ".");
		}

		try {
			boolean markerObserved = observedVersionMarker(server.getScoreboard());
			return inspect(
					server.getServerDirectory(), serverNamespaces, markerObserved, MatchaDiagnosticProfile.read());
		} catch (RuntimeException exception) {
			return malformed("Could not resolve the server's bounded Matcha archive path: "
					+ exception.getClass().getSimpleName() + ".");
		}
	}

	/**
	 * Inspects only the expected archive below a supplied server root. This overload is
	 * package-private so unit tests can exercise the real archive path without creating a
	 * Minecraft server or touching a world save.
	 */
	static MatchaDiagnosticReport inspect(Path serverRoot, Set<String> serverNamespaces) {
		MatchaDiagnosticProfile.Profile profile = MatchaDiagnosticProfile.read();
		return inspect(serverRoot, serverNamespaces, false, profile);
	}

	/**
	 * Inspects a server root with an explicitly supplied profile for deterministic synthetic fixtures.
	 * Production observation always uses the embedded lock profile through the overload above.
	 */
	static MatchaDiagnosticReport inspect(
			Path serverRoot, Set<String> serverNamespaces, MatchaDiagnosticProfile.Profile profile) {
		return inspect(serverRoot, serverNamespaces, false, profile);
	}

	private static MatchaDiagnosticReport inspect(
			Path serverRoot,
			Set<String> serverNamespaces,
			boolean markerObserved,
			MatchaDiagnosticProfile.Profile profile) {
		if (serverRoot == null) {
			return malformed("The server root path was null; no archive was inspected.");
		}
		if (serverNamespaces == null) {
			return malformed("The server namespace evidence was null; no archive was trusted.");
		}
		if (profile == null || !profile.valid()) {
			return malformed(profile == null ? "The supplied Matcha lock profile was null." : profile.error());
		}
		try {
			return inspectRoot(serverRoot.toAbsolutePath().normalize(), serverNamespaces, markerObserved, profile);
		} catch (RuntimeException exception) {
			return malformed("The server root path could not be normalised: "
					+ exception.getClass().getSimpleName() + ".");
		}
	}

	private static MatchaDiagnosticReport inspectRoot(
			Path root,
			Set<String> serverNamespaces,
			boolean markerObserved,
			MatchaDiagnosticProfile.Profile profile) {
		Path datapacks = root.resolve(DATAPACK_DIRECTORY).normalize();
		if (!datapacks.startsWith(root) || !datapacks.getParent().equals(root)) {
			return malformed("The diagnostic datapack path escaped the server root; no file was read.");
		}
		if (Files.isSymbolicLink(root)) {
			return unreadable("The server root is a symbolic link; refusing to read outside the server instance.");
		}
		if (Files.isSymbolicLink(datapacks)) {
			return unreadable("The diagnostic datapacks directory is a symbolic link; refusing to read outside the server root.");
		}
		if (Files.notExists(datapacks, LinkOption.NOFOLLOW_LINKS)) {
			return missing(profile);
		}
		if (!Files.isDirectory(datapacks, LinkOption.NOFOLLOW_LINKS)) {
			return unreadable("The diagnostic datapacks path is not a readable directory: " + DATAPACK_DIRECTORY + ".");
		}
		Path archive = datapacks.resolve(profile.archiveFilename()).normalize();
		return inspectArchivePath(archive, root, datapacks, serverNamespaces, markerObserved, profile);
	}

	private static MatchaDiagnosticReport inspectArchivePath(
			Path archive,
			Path root,
			Path datapacks,
			Set<String> serverNamespaces,
			boolean markerObserved,
			MatchaDiagnosticProfile.Profile profile) {
		if (!archive.startsWith(root) || !archive.getParent().equals(datapacks)) {
			return malformed("The locked Matcha archive filename is not a direct child of datapacks; no file was read.");
		}
		if (Files.notExists(archive, LinkOption.NOFOLLOW_LINKS)) {
			return missing(profile);
		}
		if (Files.isSymbolicLink(archive)) {
			return unreadable("The expected Matcha archive is a symbolic link: " + archiveLabel(profile) + ".");
		}
		if (!Files.isRegularFile(archive, LinkOption.NOFOLLOW_LINKS)) {
			return unreadable("The expected Matcha archive is not a regular readable file: " + archiveLabel(profile) + ".");
		}
		MatchaDiagnosticArchive.Inspection inspection = MatchaDiagnosticArchive.inspect(archive, profile);
		if (inspection.failureStatus() != null) {
			return warning(inspection.failureStatus(), inspection.failureMessage(), standardRemedies(profile));
		}
		return classifyArchive(inspection, serverNamespaces, markerObserved, profile);
	}

	private static MatchaDiagnosticReport classifyArchive(
			MatchaDiagnosticArchive.Inspection inspection,
			Set<String> serverNamespaces,
			boolean markerObserved,
			MatchaDiagnosticProfile.Profile profile) {
		if (inspection.metadata().failureStatus() != null) {
			MatchaDiagnosticArchive.MetadataInspection metadata = inspection.metadata();
			return warning(metadata.failureStatus(), metadata.failureMessage(), standardRemedies(profile));
		}
		if (!inspection.dataRole()) {
			return inspection.resourceRole()
					? warning(
							MatchaDiagnosticStatus.ONE_SIDED_ROLE,
							"The Matcha archive contains resource-pack assets but no audited Matcha data namespace; "
									+ "the baseline is one-sided.",
							oneSidedRemedies(profile))
					: missing(profile,
							"The archive exists but contains no audited Matcha data namespace. "
									+ "A decoy archive is not a valid baseline.");
		}
		if (!inspection.resourceRole()) {
			return warning(
					MatchaDiagnosticStatus.ONE_SIDED_ROLE,
					"The Matcha archive contains its datapack role but no resource-pack assets; the baseline is one-sided.",
					oneSidedRemedies(profile));
		}
		if (!inspection.digest().equalsIgnoreCase(profile.archiveSha256())) {
			return checksumMismatch(inspection.digest(), profile);
		}
		if (!MatchaDiagnosticArchive.hasKnownData(serverNamespaces)) {
			return missingServerRole(profile);
		}
		return healthy(profile, markerObserved);
	}

	private static MatchaDiagnosticReport checksumMismatch(
			String observedDigest, MatchaDiagnosticProfile.Profile profile) {
		return warning(
				MatchaDiagnosticStatus.CHECKSUM_MISMATCH,
				"The Matcha archive checksum does not match the lock (not a ZIP or pack.mcmeta parse error): observed "
						+ observedDigest + ", expected " + profile.archiveSha256() + ".",
				List.of(
						"Restore the exact " + archiveLabel(profile) + " archive and compare its SHA-256 with matcha.lock.json.",
						"If the Matcha version was deliberately changed, update the lock and compatibility record together."));
	}

	private static MatchaDiagnosticReport missingServerRole(MatchaDiagnosticProfile.Profile profile) {
		return warning(
				MatchaDiagnosticStatus.ONE_SIDED_ROLE,
				"The locked Matcha archive is present, but its datapack role was not observed on this server. "
						+ "A dedicated server cannot verify a remote client's resource-pack role.",
				List.of(
						"Confirm the pack loader lists " + archiveLabel(profile)
								+ " as a required datapack AND resource pack.",
						"Verify the resource-pack role separately from a client; server evidence "
								+ "cannot establish that remote role."));
	}

	private static MatchaDiagnosticReport healthy(
			MatchaDiagnosticProfile.Profile profile, boolean markerObserved) {
		String markerSummary = markerObserved
				? " The audited runtime version marker was also observed."
				: " No runtime version marker was exposed by this server view.";
		return MatchaDiagnosticReport.healthy(
			"Matcha " + profile.version() + " archive bytes match the locked SHA-256, and a known Matcha "
					+ "datapack namespace is visible through this server's resource manager. This bounded evidence "
					+ "does not prove that archive was the loaded source or that a remote client's resource-pack "
					+ "role is active; verify that role separately on a client." + markerSummary);
	}

	private static boolean observedVersionMarker(Scoreboard scoreboard) {
		if (scoreboard == null) {
			return false;
		}
		Collection<String> objectives = scoreboard.getObjectiveNames();
		return objectives != null && objectives.contains(VERSION_OBJECTIVE);
	}

	private static Set<String> observedNamespaces(Set<String> namespaces) {
		Set<String> observed = new LinkedHashSet<>();
		for (String namespace : KNOWN_DATA_NAMESPACES) {
			if (namespaces.contains(namespace)) {
				observed.add(namespace);
			}
		}
		return Collections.unmodifiableSet(observed);
	}

	private static MatchaDiagnosticReport missing(MatchaDiagnosticProfile.Profile profile) {
		return missing(profile, "The expected Matcha archive was not found in the instance.");
	}

	private static MatchaDiagnosticReport missing(MatchaDiagnosticProfile.Profile profile, String detail) {
		return MatchaDiagnosticReport.missing(
				"The Matcha gameplay baseline is NOT INSTALLED: " + detail,
				List.of(
						"Expected archive: " + archiveLabel(profile) + ".",
						"Locked SHA-256: " + profile.archiveSha256() + ".",
						"Confirm the pack loader lists that same file as a required datapack AND "
								+ "resource pack, then reinstall the pinned pack."));
	}

	private static MatchaDiagnosticReport unreadable(String detail) {
		return MatchaDiagnosticReport.warning(
				MatchaDiagnosticStatus.UNREADABLE,
				"The Matcha baseline archive is unreadable: " + detail,
				List.of("Restore a readable regular archive and compare it with matcha.lock.json before restarting."));
	}

	private static MatchaDiagnosticReport malformed(String detail) {
		return MatchaDiagnosticReport.warning(
				MatchaDiagnosticStatus.MALFORMED,
				"The Matcha baseline diagnostic input is malformed: " + detail,
				List.of("Re-acquire the exact pinned Matcha archive and verify its SHA-256 against matcha.lock.json."));
	}

	private static MatchaDiagnosticReport warning(
			MatchaDiagnosticStatus status, String summary, List<String> remedies) {
		return MatchaDiagnosticReport.warning(status, summary, remedies);
	}

	private static List<String> standardRemedies(MatchaDiagnosticProfile.Profile profile) {
		return List.of(
				"Restore " + archiveLabel(profile) + " from the pinned source and compare its SHA-256 with matcha.lock.json.",
				"If the Matcha version was deliberately changed, update the lock and compatibility record together.");
	}

	private static List<String> oneSidedRemedies(MatchaDiagnosticProfile.Profile profile) {
		return List.of(
				"Restore the original " + archiveLabel(profile) + " archive without extracting or splitting it.",
				"Configure the pack loader to use the same archive as both the required datapack and resource pack.");
	}

	private static String archiveLabel(MatchaDiagnosticProfile.Profile profile) {
		return DATAPACK_DIRECTORY + "/" + profile.archiveFilename();
	}
}

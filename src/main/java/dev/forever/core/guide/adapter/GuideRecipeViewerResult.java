package dev.forever.core.guide.adapter;

import dev.forever.core.guide.domain.GuideRecipeViewerStatus;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/** Versioned, bounded result returned by the optional recipe-viewer capability. */
public record GuideRecipeViewerResult(
		int protocolVersion,
		Optional<Identifier> guideEntryId,
		GuideRecipeViewerStatus status,
		List<Identifier> processIds,
		List<String> diagnostics) {

	public static final int CURRENT_PROTOCOL_VERSION = 1;
	public static final int MAX_PROCESS_IDS = 16;
	public static final int MAX_DIAGNOSTICS = 4;
	public static final int MAX_DIAGNOSTIC_LENGTH = 256;

	public GuideRecipeViewerResult {
		if (protocolVersion != CURRENT_PROTOCOL_VERSION) {
			throw new IllegalArgumentException("Unsupported guide recipe-viewer protocol version " + protocolVersion
					+ "; this server understands version " + CURRENT_PROTOCOL_VERSION + ".");
		}
		Objects.requireNonNull(guideEntryId, "guide recipe-viewer entry ID must not be null");
		guideEntryId.ifPresent(id -> {
			if (!GuideEntry.isGuideIdentifier(id)) {
				throw new IllegalArgumentException("Guide recipe-viewer results must use a Forever guide ID.");
			}
		});
		Objects.requireNonNull(status, "guide recipe-viewer status must not be null");
		Objects.requireNonNull(processIds, "guide recipe-viewer process IDs must not be null");
		if (processIds.size() > MAX_PROCESS_IDS) {
			throw new IllegalArgumentException("Guide recipe-viewer results exceed the maximum of "
					+ MAX_PROCESS_IDS + " process IDs.");
		}
		if (processIds.stream().anyMatch(id -> id == null)) {
			throw new IllegalArgumentException("Guide recipe-viewer process IDs cannot contain null.");
		}
		processIds = List.copyOf(processIds);
		Objects.requireNonNull(diagnostics, "guide recipe-viewer diagnostics must not be null");
		if (diagnostics.size() > MAX_DIAGNOSTICS || diagnostics.stream().anyMatch(diagnostic ->
				diagnostic == null || diagnostic.isBlank() || diagnostic.length() > MAX_DIAGNOSTIC_LENGTH)) {
			throw new IllegalArgumentException("Guide recipe-viewer diagnostics are null, blank, or oversized.");
		}
		diagnostics = List.copyOf(diagnostics);
	}

	public static GuideRecipeViewerResult unavailable(Identifier guideEntryId, String diagnostic) {
		return new GuideRecipeViewerResult(
				CURRENT_PROTOCOL_VERSION, Optional.ofNullable(guideEntryId), GuideRecipeViewerStatus.UNAVAILABLE,
				List.of(), List.of(diagnostic));
	}

	public static GuideRecipeViewerResult invalid(String diagnostic) {
		return new GuideRecipeViewerResult(
				CURRENT_PROTOCOL_VERSION, Optional.empty(), GuideRecipeViewerStatus.INVALID,
				List.of(), List.of(diagnostic));
	}

	public static GuideRecipeViewerResult available(Identifier guideEntryId, List<Identifier> processIds) {
		return new GuideRecipeViewerResult(
				CURRENT_PROTOCOL_VERSION, Optional.of(guideEntryId), GuideRecipeViewerStatus.AVAILABLE,
				processIds, List.of());
	}
}

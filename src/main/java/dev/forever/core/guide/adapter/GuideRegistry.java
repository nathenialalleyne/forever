package dev.forever.core.guide.adapter;

import dev.forever.core.guide.domain.GuideDataException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.resources.Identifier;

/**
 * Immutable snapshot of all validated Field Guide metadata visible to one server.
 *
 * <p>The registry owns no player state. A server resource reload creates a new snapshot
 * and publishes it atomically through {@link GuideRegistryAccess}.
 */
public final class GuideRegistry {

	public static final int MAX_ENTRIES = 512;

	private final Map<Identifier, GuideEntry> entries;

	public GuideRegistry(Collection<GuideEntry> entries) {
		Objects.requireNonNull(entries, "guide entries must not be null");
		if (entries.size() > MAX_ENTRIES) {
			throw new GuideDataException("Guide registry contains " + entries.size()
					+ " entries, exceeding the maximum of " + MAX_ENTRIES + ".");
		}
		TreeMap<Identifier, GuideEntry> byId = new TreeMap<>();
		for (GuideEntry entry : entries) {
			Objects.requireNonNull(entry, "guide registry entries cannot contain null");
			GuideEntry previous = byId.putIfAbsent(entry.id(), entry);
			if (previous != null) {
				throw new GuideDataException("Duplicate guide entry ID '" + entry.id()
						+ "'. Stable IDs must be unique across the active data pack.");
			}
		}
		validateLinks(byId);
		this.entries = Collections.unmodifiableMap(new TreeMap<>(byId));
	}

	public GuideRegistry(Map<Identifier, GuideEntry> entries) {
		this(checkedValues(entries));
	}

	public Map<Identifier, GuideEntry> entries() {
		return entries;
	}

	public List<GuideEntry> orderedEntries() {
		return List.copyOf(entries.values());
	}

	public Optional<GuideEntry> entry(Identifier id) {
		return Optional.ofNullable(entries.get(id));
	}

	public GuideEntry require(Identifier id) {
		return entry(id).orElseThrow(() -> new GuideDataException("No Field Guide entry is registered for '"
				+ id + "'. Check the active server data pack and the requested stable ID."));
	}

	public boolean contains(Identifier id) {
		return entries.containsKey(id);
	}

	/**
	 * Runs a deterministic bounded search over title keys, categories, section keys, and
	 * data-defined keywords. Gating is reported on each hit and does not hide instructions.
	 */
	public GuideSearchResponse search(GuideSearchQuery query) {
		Objects.requireNonNull(query, "guide search query must not be null");
		List<ScoredEntry> matches = new ArrayList<>();
		for (GuideEntry entry : entries.values()) {
			if (query.category().isPresent() && !query.category().orElseThrow().equals(entry.category())) {
				continue;
			}
			int relevance = relevance(entry, query.text());
			if (!query.text().isBlank() && relevance == 0) {
				continue;
			}
			matches.add(new ScoredEntry(entry, relevance));
		}

		matches.sort(Comparator.comparingInt(ScoredEntry::relevance).reversed()
				.thenComparing(scored -> scored.entry().id()));
		boolean truncated = matches.size() > query.limit();
		List<GuideSearchHit> results = matches.stream()
				.limit(query.limit())
				.map(scored -> new GuideSearchHit(
						scored.entry().id(),
						scored.entry().titleKey(),
						scored.entry().category(),
						scored.relevance(),
						scored.entry().isAccessible(query.access())))
				.toList();
		return new GuideSearchResponse(GuideSearchResponse.CURRENT_PROTOCOL_VERSION, results, truncated);
	}

	/** Returns only the entries from a bounded search response for simple server callers. */
	public List<GuideEntry> searchEntries(GuideSearchQuery query) {
		// search() returns bounded GuideSearchHit summaries, so resolve each hit's id
		// back to its full entry. require() takes an Identifier, not a hit.
		return search(query).results().stream()
				.map(GuideSearchHit::id)
				.map(this::require)
				.toList();
	}

	/**
	 * Routes one validated entry through an optional recipe-viewer capability. The default
	 * overload is deliberately a working no-op, so a missing viewer remains safe.
	 */
	public GuideRecipeViewerResult recipeView(Identifier entryId) {
		return recipeView(entryId, NoOpGuideRecipeViewerCapability.INSTANCE);
	}

	public GuideRecipeViewerResult recipeView(
			Identifier entryId,
			GuideRecipeViewerCapability capability) {
		GuideEntry entry = require(entryId);
		return Objects.requireNonNull(capability, "recipe viewer capability must not be null").describe(entry);
	}

	private static int relevance(GuideEntry entry, String queryText) {
		if (queryText.isBlank()) {
			return 0;
		}
		String title = searchable(entry.titleKey());
		String category = searchable(entry.category());
		List<String> sections = entry.sectionKeys().stream().map(GuideRegistry::searchable).toList();
		List<String> keywords = entry.keywords();
		String[] terms = queryText.split("\\s+");
		int score = 0;
		for (String term : terms) {
			if (term.isBlank()) {
				continue;
			}
			int termScore = 0;
			if (title.equals(term)) {
				termScore = Math.max(termScore, 1_000);
			} else if (title.contains(term)) {
				termScore = Math.max(termScore, 700);
			}
			for (String keyword : keywords) {
				if (keyword.equals(term)) {
					termScore = Math.max(termScore, 500);
				} else if (keyword.contains(term)) {
					termScore = Math.max(termScore, 350);
				}
			}
			if (category.equals(term)) {
				termScore = Math.max(termScore, 250);
			} else if (category.contains(term)) {
				termScore = Math.max(termScore, 150);
			}
			if (sections.stream().anyMatch(section -> section.contains(term))) {
				termScore = Math.max(termScore, 100);
			}
			if (termScore == 0) {
				return 0;
			}
			score += termScore;
		}
		return score;
	}

	private static String searchable(String value) {
		return value.replace('.', ' ').replace('_', ' ').replace('-', ' ').toLowerCase(Locale.ROOT);
	}

	private static void validateLinks(Map<Identifier, GuideEntry> entries) {
		for (GuideEntry entry : entries.values()) {
			for (Identifier relatedId : entry.relatedEntryIds()) {
				if (!entries.containsKey(relatedId)) {
					throw new GuideDataException("Guide entry '" + entry.id()
							+ "' references missing related entry '" + relatedId + "'.");
				}
			}
			if (entry.gating().isPresent()) {
				for (Identifier prerequisiteId : entry.gating().orElseThrow().prerequisiteEntryIds()) {
					if (!entries.containsKey(prerequisiteId)) {
						throw new GuideDataException("Guide entry '" + entry.id()
								+ "' gates on missing prerequisite entry '" + prerequisiteId + "'.");
					}
				}
			}
		}
	}

	private static Collection<GuideEntry> checkedValues(Map<Identifier, GuideEntry> entries) {
		Objects.requireNonNull(entries, "guide entry map must not be null");
		for (Map.Entry<Identifier, GuideEntry> entry : entries.entrySet()) {
			if (entry.getKey() == null || entry.getValue() == null) {
				throw new GuideDataException("Guide registry map cannot contain null IDs or entries.");
			}
			if (!entry.getKey().equals(entry.getValue().id())) {
				throw new GuideDataException("Guide registry map key '" + entry.getKey()
						+ "' does not match entry ID '" + entry.getValue().id() + "'.");
			}
		}
		return entries.values();
	}

	private record ScoredEntry(GuideEntry entry, int relevance) {
	}
}

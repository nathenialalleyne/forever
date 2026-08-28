package dev.forever.core.guide.adapter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;

/**
 * Immutable metadata for one translated, server-authoritative Field Guide entry.
 *
 * <p>The entry intentionally stores translation keys rather than player-facing prose.
 * The JSON data pack owns the entry structure and the language files own localisation.
 */
public record GuideEntry(
		int schemaVersion,
		Identifier id,
		String titleKey,
		List<String> sectionKeys,
		String category,
		List<String> keywords,
		List<Identifier> processIds,
		List<Identifier> relatedEntryIds,
		Optional<GuideGate> gating) {

	public static final int CURRENT_SCHEMA = 1;
	public static final int MAX_SECTIONS = 32;
	public static final int MAX_KEYWORDS = 32;
	public static final int MAX_RELATED_ENTRIES = 16;
	public static final int MAX_TRANSLATION_KEY_LENGTH = 256;
	public static final int MAX_CATEGORY_LENGTH = 64;
	public static final int MAX_KEYWORD_LENGTH = 64;

	private record Encoded(
			int schemaVersion,
			Identifier id,
			String titleKey,
			List<String> sectionKeys,
			String category,
			List<String> keywords,
			List<Identifier> processIds,
			List<Identifier> relatedEntryIds,
			Optional<GuideGate> gating) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("schema_version").forGetter(Encoded::schemaVersion),
			Identifier.CODEC.fieldOf("id").forGetter(Encoded::id),
			Codec.sizeLimitedString(MAX_TRANSLATION_KEY_LENGTH).fieldOf("title_key")
					.forGetter(Encoded::titleKey),
			Codec.sizeLimitedString(MAX_TRANSLATION_KEY_LENGTH).listOf().fieldOf("section_keys")
					.forGetter(Encoded::sectionKeys),
			Codec.sizeLimitedString(MAX_CATEGORY_LENGTH).fieldOf("category").forGetter(Encoded::category),
			Codec.sizeLimitedString(MAX_KEYWORD_LENGTH).listOf().optionalFieldOf("keywords", List.of())
					.forGetter(Encoded::keywords),
			Identifier.CODEC.listOf().optionalFieldOf("process_ids", List.of())
					.forGetter(Encoded::processIds),
			Identifier.CODEC.listOf().optionalFieldOf("related_entry_ids", List.of())
					.forGetter(Encoded::relatedEntryIds),
			GuideGate.CODEC.optionalFieldOf("gating").forGetter(Encoded::gating)
		).apply(instance, Encoded::new));

	/** Codec for a fully validated guide entry. */
	public static final Codec<GuideEntry> CODEC = RAW_CODEC.flatXmap(
			encoded -> {
				try {
					return DataResult.success(new GuideEntry(
							encoded.schemaVersion(), encoded.id(), encoded.titleKey(), encoded.sectionKeys(),
							encoded.category(), encoded.keywords(), encoded.processIds(), encoded.relatedEntryIds(),
							encoded.gating()));
				} catch (IllegalArgumentException exception) {
					return DataResult.error(exception::getMessage);
				}
			},
			entry -> DataResult.success(new Encoded(
					entry.schemaVersion(), entry.id(), entry.titleKey(), entry.sectionKeys(), entry.category(),
					entry.keywords(), entry.processIds(), entry.relatedEntryIds(), entry.gating())));

	/** Convenience constructor for current-schema entries without search keywords. */
	public GuideEntry(
			Identifier id,
			String titleKey,
			List<String> sectionKeys,
			String category,
			List<Identifier> relatedEntryIds,
			Optional<GuideGate> gating) {
		this(CURRENT_SCHEMA, id, titleKey, sectionKeys, category, List.of(), List.of(), relatedEntryIds, gating);
	}

	/** Convenience constructor for current-schema entries with process links and no keywords. */
	public GuideEntry(
			Identifier id,
			String titleKey,
			List<String> sectionKeys,
			String category,
			List<Identifier> processIds,
			List<Identifier> relatedEntryIds,
			Optional<GuideGate> gating) {
		this(CURRENT_SCHEMA, id, titleKey, sectionKeys, category, List.of(), processIds, relatedEntryIds, gating);
	}

	public GuideEntry {
		if (schemaVersion != CURRENT_SCHEMA) {
			throw new IllegalArgumentException("Guide entry schema version " + schemaVersion
					+ " is unsupported; this build understands version " + CURRENT_SCHEMA + ".");
		}
		if (!isGuideIdentifier(id)) {
			throw new IllegalArgumentException("Guide entry IDs must use the forever:guide/ namespace and path.");
		}
		titleKey = requireTranslationKey(titleKey, "title_key");
		Objects.requireNonNull(sectionKeys, "guide section keys must not be null");
		if (sectionKeys.isEmpty() || sectionKeys.size() > MAX_SECTIONS) {
			throw new IllegalArgumentException("Guide entry '" + id + "' must contain between 1 and "
					+ MAX_SECTIONS + " section keys.");
		}
		List<String> sections = new ArrayList<>(sectionKeys.size());
		Set<String> seenSections = new HashSet<>();
		for (String key : sectionKeys) {
			String normalised = requireTranslationKey(key, "section_key");
			if (!seenSections.add(normalised)) {
				throw new IllegalArgumentException("Guide entry '" + id + "' contains duplicate section key '"
						+ normalised + "'.");
			}
			sections.add(normalised);
		}
		sectionKeys = List.copyOf(sections);

		category = requireCategory(category);
		Objects.requireNonNull(keywords, "guide keywords must not be null");
		if (keywords.size() > MAX_KEYWORDS) {
			throw new IllegalArgumentException("Guide entry '" + id + "' contains more than "
					+ MAX_KEYWORDS + " keywords.");
		}
		List<String> normalisedKeywords = new ArrayList<>(keywords.size());
		Set<String> seenKeywords = new HashSet<>();
		for (String keyword : keywords) {
			if (keyword == null || keyword.isBlank() || keyword.length() > MAX_KEYWORD_LENGTH
					|| !keyword.matches("[A-Za-z0-9][A-Za-z0-9 ._-]*")) {
				throw new IllegalArgumentException("Guide entry '" + id
						+ "' has an invalid keyword. Keywords must be bounded words or phrases.");
			}
			String normalised = keyword.trim().toLowerCase(Locale.ROOT);
			if (!seenKeywords.add(normalised)) {
				throw new IllegalArgumentException("Guide entry '" + id + "' contains duplicate keyword '"
						+ normalised + "'.");
			}
			normalisedKeywords.add(normalised);
		}
		normalisedKeywords.sort(String::compareTo);
		keywords = List.copyOf(normalisedKeywords);

		Objects.requireNonNull(processIds, "guide process IDs must not be null");
		if (processIds.size() > MAX_RELATED_ENTRIES) {
			throw new IllegalArgumentException("Guide entry '" + id + "' references more than "
					+ MAX_RELATED_ENTRIES + " processes.");
		}
		List<Identifier> processes = new ArrayList<>(processIds.size());
		Set<Identifier> seenProcesses = new HashSet<>();
		for (Identifier processId : processIds) {
			if (!isProcessIdentifier(processId)) {
				throw new IllegalArgumentException("Guide entry '" + id
						+ "' has a process ID outside the forever:process namespace.");
			}
			if (!seenProcesses.add(processId)) {
				throw new IllegalArgumentException("Guide entry '" + id + "' contains a duplicate process ID '"
						+ processId + "'.");
			}
			processes.add(processId);
		}
		processes.sort(Identifier::compareTo);
		processIds = List.copyOf(processes);

		Objects.requireNonNull(relatedEntryIds, "related guide entries must not be null");
		if (relatedEntryIds.size() > MAX_RELATED_ENTRIES) {
			throw new IllegalArgumentException("Guide entry '" + id + "' references more than "
					+ MAX_RELATED_ENTRIES + " related entries.");
		}
		List<Identifier> related = new ArrayList<>(relatedEntryIds.size());
		Set<Identifier> seenRelated = new HashSet<>();
		for (Identifier relatedId : relatedEntryIds) {
			if (!isGuideIdentifier(relatedId)) {
				throw new IllegalArgumentException("Guide entry '" + id
						+ "' has a related ID outside the forever:guide namespace.");
			}
			if (id.equals(relatedId)) {
				throw new IllegalArgumentException("Guide entry '" + id + "' cannot relate to itself.");
			}
			if (!seenRelated.add(relatedId)) {
				throw new IllegalArgumentException("Guide entry '" + id + "' contains a duplicate related ID '"
						+ relatedId + "'.");
			}
			related.add(relatedId);
		}
		related.sort(Identifier::compareTo);
		relatedEntryIds = List.copyOf(related);
		Objects.requireNonNull(gating, "guide gating must not be null");
	}

	public List<String> bodyKeys() {
		return sectionKeys;
	}

	public List<Identifier> recipeIds() {
		return processIds;
	}

	public boolean isAccessible(GuideAccess access) {
		return gating.map(gate -> gate.satisfiedBy(access)).orElse(true);
	}

	static boolean isGuideIdentifier(Identifier value) {
		return value != null && value.getNamespace().equals("forever")
				&& value.getPath().startsWith("guide/") && value.getPath().length() > "guide/".length();
	}

	private static boolean isProcessIdentifier(Identifier value) {
		return value != null && value.getNamespace().equals("forever")
				&& value.getPath().startsWith("process/") && value.getPath().length() > "process/".length();
	}

	private static String requireTranslationKey(String value, String field) {
		if (value == null || value.isBlank() || value.length() > MAX_TRANSLATION_KEY_LENGTH
				|| !value.matches("[a-z0-9][a-z0-9_.-]*")) {
			throw new IllegalArgumentException("Guide " + field
					+ " must be a bounded lowercase translation key.");
		}
		return value;
	}

	private static String requireCategory(String value) {
		if (value == null || value.isBlank() || value.length() > MAX_CATEGORY_LENGTH
				|| !value.matches("[a-z0-9][a-z0-9_.-]*")) {
			throw new IllegalArgumentException("Guide category must be a bounded lowercase identifier.");
		}
		return value;
	}
}

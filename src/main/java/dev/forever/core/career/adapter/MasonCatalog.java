package dev.forever.core.career.adapter;

import dev.forever.core.career.domain.CareerCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.forever.core.data.SchemaVersioned;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;

/** Versioned, immutable, bounded bulk block catalogue for Mason workshops. */
public record MasonCatalog(int schemaVersion, List<MasonCatalogEntry> entries) {

	public static final int CURRENT_SCHEMA = 1;
	private static final int MAX_ENTRIES = 256;

	private record Encoded(int schemaVersion, List<MasonCatalogEntry> entries) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("schema_version").forGetter(Encoded::schemaVersion),
			CareerCodecs.boundedList(MasonCatalogEntry.CODEC, MAX_ENTRIES, "Mason catalogue entries")
					.fieldOf("entries").forGetter(Encoded::entries)
	).apply(instance, Encoded::new));

	private static final Codec<MasonCatalog> RAW_CATALOG_CODEC = RAW_CODEC.flatXmap(
			encoded -> create(encoded),
			catalog -> DataResult.success(new Encoded(catalog.schemaVersion(), catalog.entries())));

	public static final Codec<MasonCatalog> CODEC = SchemaVersioned.migrating(
			RAW_CATALOG_CODEC,
			MasonCatalog::schemaVersion,
			CURRENT_SCHEMA,
			SchemaVersioned.noMigrationsYet("Mason catalogue"));

	public MasonCatalog {
		if (schemaVersion < 1) {
			throw new IllegalArgumentException("Mason catalogue schema version must be at least 1.");
		}
		entries = List.copyOf(Objects.requireNonNull(entries, "entries"));
		if (entries.isEmpty() || entries.size() > MAX_ENTRIES) {
			throw new IllegalArgumentException("Mason catalogue must contain between 1 and "
					+ MAX_ENTRIES + " entries.");
		}
		Set<Identifier> ids = new HashSet<>();
		for (MasonCatalogEntry entry : entries) {
			Objects.requireNonNull(entry, "catalogue entry");
			if (!ids.add(entry.id())) {
				throw new IllegalArgumentException("Mason catalogue declares duplicate entry " + entry.id() + ".");
			}
		}
	}

	private static DataResult<MasonCatalog> create(Encoded encoded) {
		try {
			return DataResult.success(new MasonCatalog(encoded.schemaVersion(), encoded.entries()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	public Optional<MasonCatalogEntry> entry(Identifier id) {
		return entries.stream().filter(entry -> entry.id().equals(id)).findFirst();
	}
}

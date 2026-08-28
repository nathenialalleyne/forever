package dev.forever.core.career;

import java.util.Objects;

/** Immutable snapshot of the two server data documents needed by the Mason slice. */
public record MasonData(MasonCareerDefinition career, MasonCatalog catalog) {

	public MasonData {
		Objects.requireNonNull(career, "career");
		Objects.requireNonNull(catalog, "catalog");
		for (MasonCatalogEntry entry : catalog.entries()) {
			if (!career.exposesCapability(entry.capability())) {
				throw new CareerDataException("Mason catalogue entry " + entry.id()
						+ " references capability " + entry.capability()
						+ " that no Mason rank exposes.");
			}
		}
	}
}

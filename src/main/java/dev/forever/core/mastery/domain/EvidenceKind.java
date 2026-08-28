package dev.forever.core.mastery;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

/**
 * The meaningful accomplishment categories that may advance a mastery.
 *
 * <p>There is intentionally no action-count category. A source must describe a distinct
 * variety, a discovery, a completed project, or a bounded technique observation.
 */
public enum EvidenceKind {
	VARIETY("variety"),
	DISCOVERY("discovery"),
	PROJECT("project"),
	TECHNIQUE("technique");

	public static final Codec<EvidenceKind> CODEC = Codec.STRING.comapFlatMap(
			value -> fromSerialized(value), EvidenceKind::serializedName);

	private final String serializedName;

	EvidenceKind(String serializedName) {
		this.serializedName = serializedName;
	}

	public String serializedName() {
		return serializedName;
	}

	private static DataResult<EvidenceKind> fromSerialized(String value) {
		String normalised = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
		for (EvidenceKind kind : values()) {
			if (kind.serializedName.equals(normalised)) {
				return DataResult.success(kind);
			}
		}
		return DataResult.error(() -> "Unknown mastery evidence kind '" + value
				+ "'. Expected variety, discovery, project, or technique.");
	}
}

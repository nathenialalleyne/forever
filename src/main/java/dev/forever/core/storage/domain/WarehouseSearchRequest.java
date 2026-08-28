package dev.forever.core.storage.domain;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Server-validated, bounded search request. */
public record WarehouseSearchRequest(
		int protocolVersion,
		String text,
		int offset,
		int limit,
		List<String> facets) {

	public static final int CURRENT_PROTOCOL = 1;
	private static final Codec<List<String>> FACETS_CODEC = Codec.STRING.listOf().validate(facets ->
			facets.size() > StorageSafetyLimits.MAX_QUERY_FACETS
					? DataResult.error(() -> "storage search contains too many facets")
					: DataResult.success(facets));

	public static final Codec<WarehouseSearchRequest> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("protocol_version").forGetter(WarehouseSearchRequest::protocolVersion),
			Codec.STRING.fieldOf("text").forGetter(WarehouseSearchRequest::text),
			Codec.INT.fieldOf("offset").forGetter(WarehouseSearchRequest::offset),
			Codec.INT.fieldOf("limit").forGetter(WarehouseSearchRequest::limit),
			FACETS_CODEC.fieldOf("facets").forGetter(WarehouseSearchRequest::facets)
		).apply(instance, WarehouseSearchRequest::new));

	public WarehouseSearchRequest {
		if (protocolVersion < 1 || protocolVersion > CURRENT_PROTOCOL) {
			throw new IllegalArgumentException("unsupported storage search protocol " + protocolVersion);
		}
		text = Objects.requireNonNull(text, "text");
		if (text.length() > StorageSafetyLimits.MAX_QUERY_TEXT_LENGTH) {
			throw new IllegalArgumentException("storage search text is too long");
		}
		if (offset < 0 || offset > 1_000_000) {
			throw new IllegalArgumentException("storage search offset is outside the supported range");
		}
		if (limit < 1 || limit > StorageSafetyLimits.MAX_QUERY_RESULTS) {
			throw new IllegalArgumentException("storage search limit must be between 1 and 200");
		}
		Objects.requireNonNull(facets, "facets");
		if (facets.size() > StorageSafetyLimits.MAX_QUERY_FACETS) {
			throw new IllegalArgumentException("storage search contains too many facets");
		}
		facets = facets.stream()
				.map(facet -> Objects.requireNonNull(facet, "facets cannot contain null"))
				.map(facet -> facet.toLowerCase(Locale.ROOT))
				.toList();
	}

	public WarehouseSearchRequest(int protocolVersion, String text, int offset, int limit) {
		this(protocolVersion, text, offset, limit, List.of());
	}

	/** Checks data-pack limits in addition to the protocol safety limits. */
	public DataResult<WarehouseSearchRequest> validateFor(StorageBalance balance) {
		Objects.requireNonNull(balance, "balance");
		if (protocolVersion != CURRENT_PROTOCOL) {
			return DataResult.error(() -> "unsupported storage search protocol " + protocolVersion);
		}
		if (text.length() > balance.maxQueryTextLength()) {
			return DataResult.error(() -> "storage search text exceeds the configured limit");
		}
		if (limit > balance.maxQueryResults()) {
			return DataResult.error(() -> "storage search page exceeds the configured result limit");
		}
		if (facets.size() > balance.maxQueryFacets()) {
			return DataResult.error(() -> "storage search facets exceed the configured limit");
		}
		return DataResult.success(this);
	}
}

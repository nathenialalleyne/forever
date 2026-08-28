package dev.forever.core.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;

/** Versioned, bounded search response. It never carries the complete index. */
public record WarehouseSearchPage(
		int protocolVersion,
		List<WarehouseSearchResult> results,
		boolean hasMore,
		int totalKnown,
		String status,
		String explanationKey) {

	private static final Codec<List<WarehouseSearchResult>> RESULTS_CODEC = WarehouseSearchResult.CODEC.listOf()
			.validate(results -> results.size() > StorageSafetyLimits.MAX_QUERY_RESULTS
					? DataResult.error(() -> "storage search response contains too many results")
					: DataResult.success(results));

	public static final Codec<WarehouseSearchPage> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("protocol_version").forGetter(WarehouseSearchPage::protocolVersion),
			RESULTS_CODEC.fieldOf("results").forGetter(WarehouseSearchPage::results),
			Codec.BOOL.fieldOf("has_more").forGetter(WarehouseSearchPage::hasMore),
			Codec.INT.fieldOf("total_known").forGetter(WarehouseSearchPage::totalKnown),
			Codec.STRING.fieldOf("status").forGetter(WarehouseSearchPage::status),
			Codec.STRING.fieldOf("explanation_key").forGetter(WarehouseSearchPage::explanationKey)
		).apply(instance, WarehouseSearchPage::new));

	public WarehouseSearchPage {
		if (protocolVersion < 1 || protocolVersion > WarehouseSearchRequest.CURRENT_PROTOCOL) {
			throw new IllegalArgumentException("unsupported storage search response protocol " + protocolVersion);
		}
		Objects.requireNonNull(results, "results");
		if (results.size() > StorageSafetyLimits.MAX_QUERY_RESULTS) {
			throw new IllegalArgumentException("storage search response contains too many results");
		}
		if (totalKnown < 0 || totalKnown > StorageSafetyLimits.MAX_PERSISTED_INDEX_ENTRIES) {
			throw new IllegalArgumentException("storage search total is outside the supported range");
		}
		status = boundedText(status, "status");
		explanationKey = boundedText(explanationKey, "explanationKey");
		results = List.copyOf(results);
	}

	public static WarehouseSearchPage rejected(String explanationKey) {
		return new WarehouseSearchPage(
				WarehouseSearchRequest.CURRENT_PROTOCOL, List.of(), false, 0, "rejected", explanationKey);
	}

	private static String boundedText(String value, String name) {
		Objects.requireNonNull(value, name);
		if (value.length() > StorageSafetyLimits.MAX_QUERY_TEXT_LENGTH) {
			throw new IllegalArgumentException(name + " is too long");
		}
		return value;
	}
}

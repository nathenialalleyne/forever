package dev.forever.core.storage.domain;

/**
 * Absolute guards for malformed or hostile data, separate from balance values.
 *
 * <p>These limits prevent a corrupt save or an untrusted data pack from asking the
 * server to allocate an unbounded structure. The playable values are in
 * {@code data/forever/storage/balance.json} and are always lower than or equal to
 * the relevant guard.
 */
public final class StorageSafetyLimits {

	public static final int MAX_PERSISTED_WAREHOUSES = 1_024;
	public static final int MAX_PERSISTED_CONTAINERS = 16_384;
	public static final int MAX_PERSISTED_INDEX_ENTRIES = 262_144;
	public static final int MAX_PERSISTED_CACHE_SLOTS = 256;
	public static final int MAX_QUERY_RESULTS = 200;
	public static final int MAX_QUERY_TEXT_LENGTH = 128;
	public static final int MAX_QUERY_FACETS = 16;
	public static final int MAX_REBUILD_CONTAINERS = 256;
	public static final int MAX_REBUILD_SLOTS = 8_192;
	public static final int MAX_NETWORK_PAYLOAD_BYTES = 65_536;

	private StorageSafetyLimits() {
	}
}

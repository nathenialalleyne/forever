package dev.forever.core.storage.adapter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.UUID;

/** Small codecs shared by storage records. */
final class StorageCodecs {

	static final Codec<UUID> UUID_CODEC = Codec.STRING.comapFlatMap(
			value -> {
				try {
					return DataResult.success(UUID.fromString(value));
				} catch (IllegalArgumentException exception) {
					return DataResult.error(() -> "invalid UUID '" + value + "'");
				}
			},
			UUID::toString);

	private StorageCodecs() {
	}
}

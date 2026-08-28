package dev.forever.core.career;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.resources.Identifier;

/** One data-defined, bounded Mason bulk conversion. */
public record MasonCatalogEntry(
		Identifier id,
		Identifier input,
		int inputCount,
		Identifier output,
		int outputCount,
		int maxBatch,
		MasonRank minimumRank,
		Identifier capability) {

	public static final int ABSOLUTE_MAX_BATCH = 4_096;

	private record Encoded(
			Identifier id,
			Identifier input,
			int inputCount,
			Identifier output,
			int outputCount,
			int maxBatch,
			MasonRank minimumRank,
			Identifier capability) {
	}

	private static final Codec<Encoded> RAW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("id").forGetter(Encoded::id),
			Identifier.CODEC.fieldOf("input").forGetter(Encoded::input),
			Codec.INT.fieldOf("input_count").forGetter(Encoded::inputCount),
			Identifier.CODEC.fieldOf("output").forGetter(Encoded::output),
			Codec.INT.fieldOf("output_count").forGetter(Encoded::outputCount),
			Codec.INT.fieldOf("max_batch").forGetter(Encoded::maxBatch),
			MasonRank.CODEC.fieldOf("minimum_rank").forGetter(Encoded::minimumRank),
			Identifier.CODEC.fieldOf("capability").forGetter(Encoded::capability)
	).apply(instance, Encoded::new));

	public static final Codec<MasonCatalogEntry> CODEC = RAW_CODEC.flatXmap(
			encoded -> create(encoded),
			entry -> DataResult.success(new Encoded(entry.id(), entry.input(), entry.inputCount(), entry.output(),
					entry.outputCount(), entry.maxBatch(), entry.minimumRank(), entry.capability())));

	public MasonCatalogEntry {
		id = Objects.requireNonNull(id, "id");
		input = Objects.requireNonNull(input, "input");
		output = Objects.requireNonNull(output, "output");
		capability = Objects.requireNonNull(capability, "capability");
		if (inputCount < 1 || outputCount < 1) {
			throw new IllegalArgumentException("Mason catalogue counts must be positive.");
		}
		if (maxBatch < 1 || maxBatch > ABSOLUTE_MAX_BATCH) {
			throw new IllegalArgumentException("Mason catalogue max_batch must be between 1 and "
					+ ABSOLUTE_MAX_BATCH + ".");
		}
		minimumRank = Objects.requireNonNull(minimumRank, "minimumRank");
		if (minimumRank == MasonRank.UNASSIGNED) {
			throw new IllegalArgumentException("A Mason catalogue entry must require an assigned rank.");
		}
	}

	private static DataResult<MasonCatalogEntry> create(Encoded encoded) {
		try {
			return DataResult.success(new MasonCatalogEntry(encoded.id(), encoded.input(), encoded.inputCount(),
					encoded.output(), encoded.outputCount(), encoded.maxBatch(), encoded.minimumRank(), encoded.capability()));
		} catch (IllegalArgumentException exception) {
			return DataResult.error(exception::getMessage);
		}
	}

	public long requiredInput(int batchCount) {
		return Math.multiplyExact((long) inputCount, batchCount);
	}

	public long producedOutput(int batchCount) {
		return Math.multiplyExact((long) outputCount, batchCount);
	}
}

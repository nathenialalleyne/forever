package dev.forever.core.storage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** Stable address of a block-backed physical inventory. */
public record ContainerReference(ResourceKey<Level> dimension, BlockPos position) {

	private static final Codec<ResourceKey<Level>> DIMENSION_CODEC = ResourceKey.codec(Registries.DIMENSION);

	public static final Codec<ContainerReference> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				DIMENSION_CODEC.fieldOf("dimension").forGetter(ContainerReference::dimension),
			BlockPos.CODEC.fieldOf("position").forGetter(ContainerReference::position)
		).apply(instance, ContainerReference::new));

	public ContainerReference {
		dimension = Objects.requireNonNull(dimension, "dimension");
		position = Objects.requireNonNull(position, "position").immutable();
	}
}

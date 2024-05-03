package builderb0y.autocodec.integration;

import java.util.stream.Stream;

import com.mojang.serialization.*;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.util.DFUVersions;

public interface Auto2DFUMapDecoder<T_Decoded> extends MapDecoder<T_Decoded> {

	public abstract @NotNull AutoCodec autoCodec();

	public abstract @NotNull AutoDecoder<T_Decoded> autoDecoder();

	public static <T_Decoded> @NotNull Auto2DFUMapDecoder<T_Decoded> of(@NotNull AutoCodec autoCodec, @NotNull AutoDecoder<T_Decoded> decoder) {
		if (!decoder.hasKeys()) {
			throw new FactoryException(decoder + " has no keys.");
		}
		return new Auto2DFUMapDecoder<>() {

			@Override
			public @NotNull AutoCodec autoCodec() {
				return autoCodec;
			}

			@Override
			public @NotNull AutoDecoder<T_Decoded> autoDecoder() {
				return decoder;
			}

			@Override
			public String toString() {
				return decoder.toString();
			}
		};
	}

	@Override
	public default <T_Encoded> DataResult<T_Decoded> decode(DynamicOps<T_Encoded> ops, MapLike<T_Encoded> input) {
		try {
			return DataResult.success(this.autoCodec().decode(this.autoDecoder(), ops.createMap(input.entries()), ops));
		}
		catch (DecodeException exception) {
			return DFUVersions.createErrorDataResult(exception::toString);
		}
	}

	@Override
	public default <T_Encoded> KeyCompressor<T_Encoded> compressor(DynamicOps<T_Encoded> ops) {
		return new KeyCompressor<>(ops, this.keys(ops));
	}

	@Override
	public default <T_Encoded> Stream<T_Encoded> keys(DynamicOps<T_Encoded> ops) {
		Stream<String> keys = this.autoDecoder().getKeys();
		if (keys == null) throw new IllegalStateException(this.autoDecoder() + " had keys, but now it doesn't?");
		return keys.map(ops::createString);
	}
}
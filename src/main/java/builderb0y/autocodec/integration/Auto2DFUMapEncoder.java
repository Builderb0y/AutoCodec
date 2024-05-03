package builderb0y.autocodec.integration;

import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.util.DFUVersions;

public interface Auto2DFUMapEncoder<T_Decoded> extends MapEncoder<T_Decoded> {

	public abstract @NotNull AutoCodec autoCodec();

	public abstract @NotNull AutoEncoder<T_Decoded> autoEncoder();

	public static <T_Decoded> @NotNull Auto2DFUMapEncoder<T_Decoded> of(@NotNull AutoCodec autoCodec, @NotNull AutoEncoder<T_Decoded> encoder) {
		if (!encoder.hasKeys()) {
			throw new FactoryException(encoder + " has no keys.");
		}
		return new Auto2DFUMapEncoder<>() {

			@Override
			public @NotNull AutoCodec autoCodec() {
				return autoCodec;
			}

			@Override
			public @NotNull AutoEncoder<T_Decoded> autoEncoder() {
				return encoder;
			}

			@Override
			public String toString() {
				return encoder.toString();
			}
		};
	}

	@Override
	public default <T_Encoded> RecordBuilder<T_Encoded> encode(T_Decoded input, DynamicOps<T_Encoded> ops, RecordBuilder<T_Encoded> prefix) {
		try {
			T_Encoded result = this.autoCodec().encode(this.autoEncoder(), input, ops);
			DataResult<Stream<Pair<T_Encoded, T_Encoded>>> stream = ops.getMapValues(result);
			Stream<Pair<T_Encoded, T_Encoded>> actualStream = DFUVersions.getResult(stream);
			if (actualStream == null) {
				throw new EncodeException(() -> input + " encoded into a value which was not an object. Error was: " + DFUVersions.getMessage(stream));
			}
			actualStream.forEachOrdered((Pair<T_Encoded, T_Encoded> pair) -> prefix.add(pair.getFirst(), pair.getSecond()));
			return prefix;
		}
		catch (EncodeException exception) {
			return prefix.withErrorsFrom(DFUVersions.createErrorDataResult(exception::toString));
		}
	}

	@Override
	public default <T_Encoded> KeyCompressor<T_Encoded> compressor(DynamicOps<T_Encoded> ops) {
		return new KeyCompressor<>(ops, this.keys(ops));
	}

	@Override
	public default <T_Encoded> Stream<T_Encoded> keys(DynamicOps<T_Encoded> ops) {
		Stream<String> keys = this.autoEncoder().getKeys();
		if (keys == null) throw new IllegalStateException(this.autoEncoder() + " had keys, but now it doesn't?");
		return keys.map(ops::createString);
	}
}
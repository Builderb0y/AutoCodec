package builderb0y.autocodec.integration;

import java.util.stream.Stream;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.encoders.AutoEncoder;

public abstract class Auto2DFUMapCodec<T_Decoded> extends MapCodec<T_Decoded> implements Auto2DFUMapEncoder<T_Decoded>, Auto2DFUMapDecoder<T_Decoded> {

	public static <T_Decoded> @NotNull Auto2DFUMapCodec<T_Decoded> of(@NotNull AutoCodec autoCodec, @NotNull AutoEncoder<T_Decoded> encoder, @NotNull AutoDecoder<T_Decoded> decoder) {
		if (!encoder.hasKeys()) {
			throw new FactoryException(encoder + " has no keys.");
		}
		if (decoder != encoder && !decoder.hasKeys()) {
			throw new FactoryException(decoder + " has no keys.");
		}
		return new Auto2DFUMapCodec<>() {

			@Override
			public @NotNull AutoCodec autoCodec() {
				return autoCodec;
			}

			@Override
			public @NotNull AutoDecoder<T_Decoded> autoDecoder() {
				return decoder;
			}

			@Override
			public @NotNull AutoEncoder<T_Decoded> autoEncoder() {
				return encoder;
			}
		};
	}

	public static <T_Decoded> @NotNull Auto2DFUMapCodec<T_Decoded> of(@NotNull AutoCodec autoCodec, @NotNull AutoCoder<T_Decoded> coder) {
		return of(autoCodec, coder, coder);
	}

	@Override
	public <T> Stream<T> keys(DynamicOps<T> ops) {
		Stream<String> stream;
		if (this.autoEncoder() == this.autoDecoder()) {
			stream = this.autoEncoder().getKeys();
			if (stream == null) {
				throw new IllegalStateException(this.autoEncoder() + " used to have keys, but now it doesn't?");
			}
		}
		else {
			Stream<String> encoderKeys = this.autoEncoder().getKeys();
			if (encoderKeys == null) {
				throw new IllegalStateException(this.autoEncoder() + " used to have keys, but now it doesn't?");
			}
			Stream<String> decoderKeys = this.autoDecoder().getKeys();
			if (decoderKeys == null) {
				encoderKeys.close();
				throw new IllegalStateException(this.autoDecoder() + " used to have keys, but now it doesn't?");
			}
			stream = Stream.concat(encoderKeys, decoderKeys);
		}
		return stream.map(ops::createString);
	}
}
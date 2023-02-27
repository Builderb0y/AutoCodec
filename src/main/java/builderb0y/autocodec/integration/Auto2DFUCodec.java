package builderb0y.autocodec.integration;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.encoders.AutoEncoder;

public interface Auto2DFUCodec<T_Decoded> extends Auto2DFUEncoder<T_Decoded>, Auto2DFUDecoder<T_Decoded>, Codec<T_Decoded> {

	public static <T_Decoded> @NotNull Auto2DFUCodec<T_Decoded> of(@NotNull AutoCodec autoCodec, @NotNull AutoEncoder<T_Decoded> encoder, @NotNull AutoDecoder<T_Decoded> decoder) {
		return new Auto2DFUCodec<>() {

			@Override
			public @NotNull AutoCodec autoCodec() {
				return autoCodec;
			}

			@Override
			public @NotNull AutoEncoder<T_Decoded> encoder() {
				return encoder;
			}

			@Override
			public @NotNull AutoDecoder<T_Decoded> decoder() {
				return decoder;
			}

			@Override
			public String toString() {
				return (
					encoder == decoder
					? "Auto2DFUCodec: { coder: " + encoder + " }"
					: "Auto2DFUCodec: { encoder: " + encoder + ", decoder: " + decoder + " }"
				);
			}
		};
	}

	public static <T_Decoded> @NotNull Auto2DFUCodec<T_Decoded> of(@NotNull AutoCodec autoCodec, @NotNull AutoCoder<T_Decoded> both) {
		return of(autoCodec, both, both);
	}
}
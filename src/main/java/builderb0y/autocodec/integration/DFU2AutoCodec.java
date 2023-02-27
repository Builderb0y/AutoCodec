package builderb0y.autocodec.integration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.coders.AutoCoder;

public interface DFU2AutoCodec<T_Decoded> extends DFU2AutoEncoder<T_Decoded>, DFU2AutoDecoder<T_Decoded>, AutoCoder<T_Decoded> {

	public static <T_Decoded> @NotNull DFU2AutoCodec<T_Decoded> of(@NotNull Encoder<T_Decoded> encoder, @NotNull Decoder<T_Decoded> decoder, boolean allowPartial) {
		return new DFU2AutoCodec<>() {

			@Override
			public @NotNull Encoder<T_Decoded> encoder() {
				return encoder;
			}

			@Override
			public @NotNull Decoder<T_Decoded> decoder() {
				return decoder;
			}

			@Override
			public boolean allowPartial() {
				return allowPartial;
			}

			@Override
			public String toString() {
				return (
					encoder == decoder
					? "DFU2AutoCodec: { coder: " + encoder + ", allowPartial: " + allowPartial + " }"
					: "DFU2AutoCodec: { encoder: " + encoder + ", decoder: " + decoder + ", allowPartial: " + allowPartial + " }"
				);
			}
		};
	}

	public static <T_Decoded> @NotNull DFU2AutoCodec<T_Decoded> of(@NotNull Codec<T_Decoded> codec, boolean allowPartial) {
		return of(codec, codec, allowPartial);
	}
}
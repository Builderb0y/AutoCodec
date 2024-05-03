package builderb0y.autocodec.integration;

import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapCodec.MapCodecCodec;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.util.ObjectOps;

public interface DFU2AutoCodec<T_Decoded> extends DFU2AutoEncoder<T_Decoded>, DFU2AutoDecoder<T_Decoded>, AutoCoder<T_Decoded> {

	@Override
	public default boolean hasKeys() {
		return this.encoder() instanceof MapCodec.MapCodecCodec<T_Decoded> || this.decoder() instanceof MapCodec.MapCodecCodec<T_Decoded>;
	}

	@Override
	public default @Nullable Stream<String> getKeys() {
		if (this.encoder() instanceof MapCodec.MapCodecCodec<T_Decoded> encoder) {
			if (this.decoder() instanceof MapCodec.MapCodecCodec<T_Decoded> decoder) {
				if (encoder == decoder) {
					return castToStrings(encoder);
				}
				else {
					return Stream.concat(castToStrings(encoder), castToStrings(decoder));
				}
			}
			else {
				return castToStrings(encoder);
			}
		}
		else {
			if (this.decoder() instanceof MapCodec.MapCodecCodec<T_Decoded> decoder) {
				return castToStrings(decoder);
			}
			else {
				return null;
			}
		}
	}

	@Internal
	public static @NotNull Stream<String> castToStrings(@NotNull MapCodecCodec<?> codec) {
		return codec.codec().keys(ObjectOps.INSTANCE).map((Object object) -> {
			if (object instanceof String string) return string;
			else throw new IllegalArgumentException(codec + " has a key that is not a String: " + object);
		});
	}

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
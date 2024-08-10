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
	public default boolean allowPartial() {
		return false;
	}

	@Override
	public default boolean nullSafe() {
		return true;
	}

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

	public static <T_Decoded> @NotNull DFU2AutoCodec<T_Decoded> of(@NotNull Codec<T_Decoded> codec) {
		return of(codec, codec, false, true);
	}

	public static <T_Decoded> @NotNull DFU2AutoCodec<T_Decoded> of(@NotNull Codec<T_Decoded> codec, boolean allowPartial) {
		return of(codec, codec, allowPartial, true);
	}

	public static <T_Decoded> @NotNull DFU2AutoCodec<T_Decoded> of(@NotNull Codec<T_Decoded> codec, boolean allowPartial, boolean nullSafe) {
		return of(codec, codec, allowPartial, nullSafe);
	}

	public static <T_Decoded> @NotNull DFU2AutoCodec<T_Decoded> of(@NotNull Encoder<T_Decoded> encoder, @NotNull Decoder<T_Decoded> decoder) {
		return of(encoder, decoder, false, true);
	}

	public static <T_Decoded> @NotNull DFU2AutoCodec<T_Decoded> of(@NotNull Encoder<T_Decoded> encoder, @NotNull Decoder<T_Decoded> decoder, boolean allowPartial) {
		return of(encoder, decoder, allowPartial, true);
	}

	public static <T_Decoded> @NotNull DFU2AutoCodec<T_Decoded> of(@NotNull Encoder<T_Decoded> encoder, @NotNull Decoder<T_Decoded> decoder, boolean allowPartial, boolean nullSafe) {
		record Impl<T_Decoded>(@NotNull Encoder<T_Decoded> encoder, @NotNull Decoder<T_Decoded> decoder, boolean allowPartial, boolean nullSafe) implements DFU2AutoCodec<T_Decoded> {

			@Override
			public String toString() {
				return (
					this.encoder == this.decoder
					? "DFU2AutoCodec: { codec: " + this.encoder + ", allowPartial: " + this.allowPartial + ", nullSafe: " + this.nullSafe + " }"
					: "DFU2AutoCodec: { encoder: " + this.encoder + ", decoder: " + this.decoder + ", allowPartial: " + this.allowPartial + ", nullSafe: " + this.nullSafe + " }"
				);
			}
		}
		return new Impl<>(encoder, decoder, allowPartial, nullSafe);
	}
}
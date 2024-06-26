package builderb0y.autocodec.integration;

import java.util.stream.Stream;

import com.mojang.serialization.Decoder;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;

public interface DFU2AutoDecoder<T_Decoded> extends AutoDecoder<T_Decoded> {

	public abstract @NotNull Decoder<T_Decoded> decoder();

	public abstract boolean allowPartial();

	@Override
	public default boolean hasKeys() {
		return this.decoder() instanceof MapCodec.MapCodecCodec<T_Decoded>;
	}

	@Override
	public default @Nullable Stream<String> getKeys() {
		if (this.decoder() instanceof MapCodec.MapCodecCodec<T_Decoded> decoder) {
			return DFU2AutoCodec.castToStrings(decoder);
		}
		else {
			return null;
		}
	}

	public static <T_Decoded> @NotNull DFU2AutoDecoder<T_Decoded> of(@NotNull Decoder<T_Decoded> decoder, boolean allowPartial) {
		return new DFU2AutoDecoder<>() {

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
				return decoder.toString();
			}
		};
	}

	@Override
	@OverrideOnly
	public default <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		return context.logger().unwrapLazy(
			this.decoder().parse(context.ops, context.input),
			this.allowPartial(),
			DecodeException::new
		);
	}
}
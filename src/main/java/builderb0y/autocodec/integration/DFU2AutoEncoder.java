package builderb0y.autocodec.integration;

import java.util.stream.Stream;

import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;

public interface DFU2AutoEncoder<T_Decoded> extends AutoEncoder<T_Decoded> {

	public abstract @NotNull Encoder<T_Decoded> encoder();

	public abstract boolean allowPartial();

	@Override
	public default boolean hasKeys() {
		return this.encoder() instanceof MapCodec.MapCodecCodec<T_Decoded>;
	}

	@Override
	public default @Nullable Stream<String> getKeys() {
		if (this.encoder() instanceof MapCodec.MapCodecCodec<T_Decoded> encoder) {
			return DFU2AutoCodec.castToStrings(encoder);
		}
		else {
			return null;
		}
	}

	public static <T_Decoded> @NotNull DFU2AutoEncoder<T_Decoded> of(@NotNull Encoder<T_Decoded> encoder, boolean allowPartial) {
		return new DFU2AutoEncoder<>() {

			@Override
			public @NotNull Encoder<T_Decoded> encoder() {
				return encoder;
			}

			@Override
			public boolean allowPartial() {
				return allowPartial;
			}

			@Override
			public String toString() {
				return encoder.toString();
			}
		};
	}

	@Override
	@OverrideOnly
	public default <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException {
		return context.logger().unwrapLazy(
			this.encoder().encodeStart(context.ops, context.input),
			this.allowPartial(),
			EncodeException::new
		);
	}
}
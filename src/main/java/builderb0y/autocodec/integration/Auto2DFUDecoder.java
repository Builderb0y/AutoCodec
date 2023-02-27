package builderb0y.autocodec.integration;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeException;

public interface Auto2DFUDecoder<T_Decoded> extends Decoder<T_Decoded> {

	public abstract @NotNull AutoCodec autoCodec();

	public abstract @NotNull AutoDecoder<T_Decoded> decoder();

	public static <T_Decoded> @NotNull Auto2DFUDecoder<T_Decoded> of(@NotNull AutoCodec autoCodec, @NotNull AutoDecoder<T_Decoded> decoder) {
		return new Auto2DFUDecoder<>() {

			@Override
			public @NotNull AutoCodec autoCodec() {
				return autoCodec;
			}

			@Override
			public @NotNull AutoDecoder<T_Decoded> decoder() {
				return decoder;
			}

			@Override
			public String toString() {
				return decoder.toString();
			}
		};
	}

	@Override
	public default <T> DataResult<Pair<T_Decoded, T>> decode(DynamicOps<T> ops, T input) {
		try {
			return DataResult.success(
				Pair.of(
					this.autoCodec().decode(this.decoder(), input, ops),
					ops.empty()
				)
			);
		}
		catch (DecodeException exception) {
			return DataResult.error(exception.toString());
		}
	}

	@Override
	public default <T> DataResult<T_Decoded> parse(DynamicOps<T> ops, T input) {
		try {
			return DataResult.success(
				this.autoCodec().decode(this.decoder(), input, ops)
			);
		}
		catch (DecodeException exception) {
			return DataResult.error(exception.toString());
		}
	}
}
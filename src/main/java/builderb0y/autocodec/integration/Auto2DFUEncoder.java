package builderb0y.autocodec.integration;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapLike;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.encoders.EncodeException;

public interface Auto2DFUEncoder<T_Decoded> extends Encoder<T_Decoded> {

	public abstract @NotNull AutoCodec autoCodec();

	public abstract @NotNull AutoEncoder<T_Decoded> encoder();

	public static <T_Decoded> @NotNull Auto2DFUEncoder<T_Decoded> of(@NotNull AutoCodec autoCodec, @NotNull AutoEncoder<T_Decoded> encoder) {
		return new Auto2DFUEncoder<>() {

			@Override
			public @NotNull AutoCodec autoCodec() {
				return autoCodec;
			}

			@Override
			public @NotNull AutoEncoder<T_Decoded> encoder() {
				return encoder;
			}

			@Override
			public String toString() {
				return encoder.toString();
			}
		};
	}

	@Override
	public default <T_Encoded> DataResult<T_Encoded> encode(T_Decoded input, DynamicOps<T_Encoded> ops, T_Encoded prefix) {
		try {
			T_Encoded result = this.autoCodec().encode(this.encoder(), input, ops);
			if (Objects.equals(result, ops.empty())) return DataResult.success(result);
			T_Encoded merged = ops.mergeToPrimitive(prefix, result).result().orElse(null);
			if (merged == null) merged = ops.getMap(result).flatMap((MapLike<T_Encoded> map) -> ops.mergeToMap(prefix, map)).result().orElse(null);
			if (merged == null) merged = ops.getStream(result).flatMap((Stream<T_Encoded> stream) -> ops.mergeToList(prefix, stream.collect(Collectors.toList()))).result().orElse(null);
			if (merged == null) merged = result;
			return DataResult.success(merged);
		}
		catch (EncodeException exception) {
			return DataResult.error(exception.toString());
		}
	}

	@Override
	public default <T_Encoded> DataResult<T_Encoded> encodeStart(DynamicOps<T_Encoded> ops, T_Decoded input) {
		return DataResult.success(this.autoCodec().encode(this.encoder(), input, ops));
	}
}
package builderb0y.autocodec.integration;

import java.util.Objects;
import java.util.stream.Stream;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapLike;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.util.DFUVersions;

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
			if (Objects.equals(result, ops.empty())) {
				return DFUVersions.createSuccessDataResult(result);
			}
			T_Encoded merged = DFUVersions.getResult(ops.mergeToPrimitive(prefix, result));
			if (merged == null) {
				MapLike<T_Encoded> map = DFUVersions.getResult(ops.getMap(result));
				if (map != null) {
					merged = DFUVersions.getResult(ops.mergeToMap(prefix, map));
				}
			}
			if (merged == null) {
				Stream<T_Encoded> stream = DFUVersions.getResult(ops.getStream(result));
				if (stream != null) {
					merged = DFUVersions.getResult(ops.mergeToList(prefix, stream.toList()));
				}
			}
			if (merged == null) {
				merged = result;
			}
			return DFUVersions.createSuccessDataResult(merged);
		}
		catch (EncodeException exception) {
			return DFUVersions.createErrorDataResult(exception::toString);
		}
	}

	@Override
	public default <T_Encoded> DataResult<T_Encoded> encodeStart(DynamicOps<T_Encoded> ops, T_Decoded input) {
		return DFUVersions.createSuccessDataResult(this.autoCodec().encode(this.encoder(), input, ops));
	}
}
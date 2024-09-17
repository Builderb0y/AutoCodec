package builderb0y.autocodec.integration;

import java.util.Objects;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.util.DFUVersions;

public record Auto2DFUCodec<T_Decoded>(
	AutoCodec autoCodec,
	AutoCoder<T_Decoded> coder
)
implements Codec<T_Decoded> {

	@Override
	public <T_Encoded> DataResult<Pair<T_Decoded, T_Encoded>> decode(DynamicOps<T_Encoded> ops, T_Encoded input) {
		try {
			return DFUVersions.createSuccessDataResult(
				Pair.of(
					this.autoCodec().decode(this.coder, input, ops),
					ops.empty()
				)
			);
		}
		catch (DecodeException exception) {
			return DFUVersions.createErrorDataResult(exception::toString);
		}
	}

	@Override
	public <T_Encoded> DataResult<T_Decoded> parse(DynamicOps<T_Encoded> ops, T_Encoded input) {
		try {
			return DFUVersions.createSuccessDataResult(
				this.autoCodec().decode(this.coder, input, ops)
			);
		}
		catch (DecodeException exception) {
			return DFUVersions.createErrorDataResult(exception::toString);
		}
	}

	@Override
	public <T_Encoded> DataResult<T_Encoded> encode(T_Decoded input, DynamicOps<T_Encoded> ops, T_Encoded prefix) {
		try {
			T_Encoded result = this.autoCodec().encode(this.coder, input, ops);
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
	public <T_Encoded> DataResult<T_Encoded> encodeStart(DynamicOps<T_Encoded> ops, T_Decoded input) {
		return DFUVersions.createSuccessDataResult(this.autoCodec().encode(this.coder, input, ops));
	}
}
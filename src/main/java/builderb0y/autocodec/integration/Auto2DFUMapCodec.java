package builderb0y.autocodec.integration;

import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.util.DFUVersions;

public class Auto2DFUMapCodec<T_Decoded> extends MapCodec<T_Decoded> {

	public final AutoCodec autoCodec;
	public final AutoCoder<T_Decoded> coder;

	public Auto2DFUMapCodec(AutoCodec autoCodec, AutoCoder<T_Decoded> coder) {
		if (!coder.hasKeys()) {
			throw new IllegalArgumentException("Coder must have keys");
		}
		this.autoCodec = autoCodec;
		this.coder = coder;
	}

	@Override
	public <T_Encoded> DataResult<T_Decoded> decode(DynamicOps<T_Encoded> ops, MapLike<T_Encoded> input) {
		try {
			return DFUVersions.createSuccessDataResult(this.autoCodec.decode(this.coder, ops.createMap(input.entries()), ops));
		}
		catch (DecodeException exception) {
			return DFUVersions.createErrorDataResult(exception::toString);
		}
	}

	@Override
	public <T_Encoded> RecordBuilder<T_Encoded> encode(T_Decoded input, DynamicOps<T_Encoded> ops, RecordBuilder<T_Encoded> prefix) {
		try {
			T_Encoded result = this.autoCodec.encode(this.coder, input, ops);
			DataResult<Stream<Pair<T_Encoded, T_Encoded>>> stream = ops.getMapValues(result);
			Stream<Pair<T_Encoded, T_Encoded>> actualStream = DFUVersions.getResult(stream);
			if (actualStream == null) {
				throw new EncodeException(() -> input + " encoded into a value which was not an object. Error was: " + DFUVersions.getMessage(stream));
			}
			actualStream.forEachOrdered((Pair<T_Encoded, T_Encoded> pair) -> prefix.add(pair.getFirst(), pair.getSecond()));
			return prefix;
		}
		catch (EncodeException exception) {
			return prefix.withErrorsFrom(DFUVersions.createErrorDataResult(exception::toString));
		}
	}

	@Override
	public <T_Encoded> KeyCompressor<T_Encoded> compressor(DynamicOps<T_Encoded> ops) {
		return new KeyCompressor<>(ops, this.keys(ops));
	}

	@Override
	public <T_Encoded> Stream<T_Encoded> keys(DynamicOps<T_Encoded> ops) {
		Stream<String> keys = this.coder.getKeys();
		if (keys == null) throw new IllegalStateException(this.coder + " had keys, but now it doesn't?");
		return keys.map(ops::createString);
	}
}
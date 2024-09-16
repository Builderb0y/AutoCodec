package builderb0y.autocodec.coders;

import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;

/**
basic implementation of {@link AutoCoder}
which simply delegates to an {@link AutoEncoder} and an {@link AutoDecoder}.
*/
public record EncoderDecoderCoder<T_Decoded>(@NotNull AutoEncoder<T_Decoded> encoder, @NotNull AutoDecoder<T_Decoded> decoder) implements AutoCoder<T_Decoded> {

	public EncoderDecoderCoder(@NotNull AutoEncoder<T_Decoded> encoder, @NotNull AutoDecoder<T_Decoded> decoder) {
		this.encoder = encoder instanceof EncoderDecoderCoder<T_Decoded> both ? both.encoder() : encoder;
		this.decoder = decoder instanceof EncoderDecoderCoder<T_Decoded> both ? both.decoder() : decoder;
	}

	@Override
	public @Nullable Stream<String> getKeys() {
		Stream<String> encoderKeys = this.encoder.getKeys();
		if (encoderKeys == null) return null;
		Stream<String> decoderKeys = this.decoder.getKeys();
		if (decoderKeys == null) { encoderKeys.close(); return null; }
		return Stream.concat(encoderKeys, decoderKeys);
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException {
		return context.encodeWith(this.encoder);
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		return context.decodeWith(this.decoder);
	}

	public static class Factory extends NamedCoderFactory {

		public static final Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoCoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			AutoDecoder<T_HandledType> decoder = context.tryCreateDecoder();
			if (decoder == null) return null;
			AutoEncoder<T_HandledType> encoder = context.tryCreateEncoder();
			if (encoder == null) return null;
			return new EncoderDecoderCoder<>(encoder, decoder);
		}
	}
}
package builderb0y.autocodec.coders;

import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

/**
basic implementation of {@link AutoCoder}
which simply delegates to an {@link AutoEncoder} and an {@link AutoDecoder}.
*/
public record Coder<T_Decoded>(@NotNull AutoEncoder<T_Decoded> encoder, @NotNull AutoDecoder<T_Decoded> decoder) implements AutoCoder<T_Decoded> {

	public Coder(@NotNull AutoEncoder<T_Decoded> encoder, @NotNull AutoDecoder<T_Decoded> decoder) {
		this.encoder = encoder instanceof Coder<T_Decoded> both ? both.encoder() : encoder;
		this.decoder = decoder instanceof Coder<T_Decoded> both ? both.decoder() : decoder;
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

	@Override
	public @NotNull <T_To> AutoCoder<T_To> mapCoder(@NotNull ReifiedType<T_To> newType, @NotNull HandlerMapper<T_To, T_Decoded> encodeMapper, @NotNull HandlerMapper<T_Decoded, T_To> decodeMapper) {
		return new Coder<>(this.encoder.mapEncoder(newType, encodeMapper), this.decoder.mapDecoder(newType, decodeMapper));
	}

	@Override
	public @NotNull <T_To> AutoCoder<T_To> mapCoder(@NotNull ReifiedType<T_To> newType, @NotNull String encodeMapperName, @NotNull HandlerMapper<T_To, T_Decoded> encodeMapper, @NotNull String decodeMapperName, @NotNull HandlerMapper<T_Decoded, T_To> decodeMapper) {
		return new Coder<>(this.encoder.mapEncoder(newType, encodeMapperName, encodeMapper), this.decoder.mapDecoder(newType, decodeMapperName, decodeMapper));
	}

	@Override
	public @NotNull <T_To> AutoDecoder<T_To> mapDecoder(@NotNull ReifiedType<T_To> newType, @NotNull HandlerMapper<T_Decoded, T_To> mapper) {
		return this.decoder.mapDecoder(newType, mapper);
	}

	@Override
	public @NotNull <T_To> AutoDecoder<T_To> mapDecoder(@NotNull ReifiedType<T_To> newType, @NotNull String mapperName, @NotNull HandlerMapper<T_Decoded, T_To> mapper) {
		return this.decoder.mapDecoder(newType, mapperName, mapper);
	}

	@Override
	public @NotNull <T_To> AutoEncoder<T_To> mapEncoder(@NotNull ReifiedType<T_To> newType, @NotNull HandlerMapper<T_To, T_Decoded> mapper) {
		return this.encoder.mapEncoder(newType, mapper);
	}

	@Override
	public @NotNull <T_To> AutoEncoder<T_To> mapEncoder(@NotNull ReifiedType<T_To> newType, @NotNull String mapperName, @NotNull HandlerMapper<T_To, T_Decoded> mapper) {
		return this.encoder.mapEncoder(newType, mapperName, mapper);
	}
}
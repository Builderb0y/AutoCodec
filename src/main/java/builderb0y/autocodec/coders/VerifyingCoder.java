package builderb0y.autocodec.coders;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.VerifyingDecoder;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.verifiers.AutoVerifier;

public class VerifyingCoder<T_Decoded> extends VerifyingDecoder<T_Decoded> implements AutoCoder<T_Decoded> {

	public final @NotNull AutoEncoder<T_Decoded> encoder;

	public VerifyingCoder(
		@NotNull ReifiedType<T_Decoded> type,
		@NotNull AutoEncoder<T_Decoded> encoder,
		@NotNull AutoDecoder<T_Decoded> decoder,
		@NotNull AutoVerifier<T_Decoded> verifier
	) {
		super(type, decoder, verifier);
		this.encoder = encoder;
	}

	public VerifyingCoder(
		@NotNull ReifiedType<T_Decoded> type,
		@NotNull AutoCoder<T_Decoded> coder,
		@NotNull AutoVerifier<T_Decoded> verifier
	) {
		this(type, coder, coder, verifier);
	}

	@Override
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException {
		return context.encodeWith(this.encoder);
	}

	@Override
	public @NotNull <T_To> AutoCoder<T_To> mapCoder(@NotNull ReifiedType<T_To> newType, @NotNull HandlerMapper<T_To, T_Decoded> encodeMapper, @NotNull HandlerMapper<T_Decoded, T_To> decodeMapper) {
		AutoVerifier<T_To> newVerifier = this.verifier.mapVerifier(newType, encodeMapper);
		if (this.encoder == this.decoder) {
			AutoCoder<T_To> newCoder = ((AutoCoder<T_Decoded>)(this.encoder)).mapCoder(newType, encodeMapper, decodeMapper);
			return new VerifyingCoder<>(newType, newCoder, newVerifier);
		}
		else {
			AutoEncoder<T_To> newEncoder = this.encoder.mapEncoder(newType, encodeMapper);
			AutoDecoder<T_To> newDecoder = this.decoder.mapDecoder(newType, decodeMapper);
			return new VerifyingCoder<>(newType, newEncoder, newDecoder, newVerifier);
		}
	}

	@Override
	public @NotNull <T_To> AutoCoder<T_To> mapCoder(@NotNull ReifiedType<T_To> newType, @NotNull String encodeMapperName, @NotNull HandlerMapper<T_To, T_Decoded> encodeMapper, @NotNull String decodeMapperName, @NotNull HandlerMapper<T_Decoded, T_To> decodeMapper) {
		AutoVerifier<T_To> newVerifier = this.verifier.mapVerifier(newType, encodeMapperName, encodeMapper);
		if (this.encoder == this.decoder) {
			AutoCoder<T_To> newCoder = ((AutoCoder<T_Decoded>)(this.encoder)).mapCoder(newType, encodeMapperName, encodeMapper, decodeMapperName, decodeMapper);
			return new VerifyingCoder<>(newType, newCoder, newVerifier);
		}
		else {
			AutoEncoder<T_To> newEncoder = this.encoder.mapEncoder(newType, encodeMapperName, encodeMapper);
			AutoDecoder<T_To> newDecoder = this.decoder.mapDecoder(newType, decodeMapperName, decodeMapper);
			return new VerifyingCoder<>(newType, newEncoder, newDecoder, newVerifier);
		}
	}

	@Override
	public String toString() {
		return (
			this.encoder == this.decoder
			? this.toString + ": { coder: " + this.encoder + ", verifier: " + this.verifier + " }"
			: this.toString + ": { encoder: " + this.encoder + ", decoder: " + this.decoder + ", verifier: " + this.verifier + " }"
		);
	}
}
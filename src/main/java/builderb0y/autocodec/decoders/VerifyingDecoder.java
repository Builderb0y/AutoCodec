package builderb0y.autocodec.decoders;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.coders.VerifyingCoder;
import builderb0y.autocodec.decoders.AutoDecoder.NamedDecoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.verifiers.AutoVerifier;

public class VerifyingDecoder<T_Decoded> extends NamedDecoder<T_Decoded> {

	public final @NotNull AutoDecoder<T_Decoded> decoder;
	public final @NotNull AutoVerifier<T_Decoded> verifier;

	public VerifyingDecoder(
		@NotNull ReifiedType<T_Decoded> type,
		@NotNull AutoDecoder<T_Decoded> decoder,
		@NotNull AutoVerifier<T_Decoded> verifier
	) {
		super(type);
		this.decoder  = decoder;
		this.verifier = verifier;
	}

	public static <T_Decoded> @NotNull VerifyingDecoder<T_Decoded> of(
		@NotNull ReifiedType<T_Decoded> type,
		@NotNull AutoDecoder<T_Decoded> decoder,
		@NotNull AutoVerifier<T_Decoded> verifier
	) {
		if (decoder instanceof AutoCoder<T_Decoded> coder) {
			return new VerifyingCoder<>(type, coder, verifier);
		}
		else {
			return new VerifyingDecoder<>(type, decoder, verifier);
		}
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		T_Decoded result = context.decodeWith(this.decoder);
		context.verifyWith(this.verifier, result);
		return result;
	}

	@Override
	public String toString() {
		return this.toString + ": { decoder: " + this.decoder + ", verifier: " + this.verifier + " }";
	}
}
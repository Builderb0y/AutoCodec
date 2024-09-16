package builderb0y.autocodec.coders;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.verifiers.AutoVerifier;

public class VerifyingCoder<T_Decoded> extends NamedCoder<T_Decoded> {

	public final @NotNull AutoCoder<T_Decoded> coder;
	public final @NotNull AutoVerifier<T_Decoded> verifier;

	public VerifyingCoder(
		@NotNull ReifiedType<T_Decoded> handledType,
		@NotNull AutoCoder<T_Decoded> coder,
		@NotNull AutoVerifier<T_Decoded> verifier
	) {
		super(handledType);
		this.coder = coder;
		this.verifier = verifier;
	}

	@Override
	public @Nullable Stream<String> getKeys() {
		return this.coder.getKeys();
	}

	@Override
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException {
		return context.encodeWith(this.coder);
	}

	@Override
	public <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		T_Decoded result = context.decodeWith(this.coder);
		context.verifyWith(this.verifier, result);
		return result;
	}

	@Override
	public String toString() {
		return this.toString + ": { coder: " + this.coder + ", verifier: " + this.verifier + " }";
	}
}
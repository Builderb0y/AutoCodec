package builderb0y.autocodec.verifiers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class VerifyContext<T_Encoded, T_Decoded> extends DecodeContext<T_Encoded> {

	public static final @NotNull ObjectArrayFactory<VerifyContext<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(VerifyContext.class).generic();

	public final @Nullable T_Decoded object;

	public VerifyContext(@NotNull DecodeContext<T_Encoded> context, @Nullable T_Decoded object) {
		super(context);
		this.object = object;
	}

	public <T_NewDecoded> @NotNull VerifyContext<T_Encoded, T_NewDecoded> object(@Nullable T_NewDecoded object) {
		return new VerifyContext<>(this, object);
	}

	public void verifyWith(@NotNull AutoVerifier<T_Decoded> verifier) throws VerifyException {
		this.logger().verify(verifier, this);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": { path: " + this.pathToString() + ", input: " + this.input + ", ops: " + this.ops + ", object: " + this.object + " }";
	}
}
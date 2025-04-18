package builderb0y.autocodec.verifiers;

import java.util.function.Supplier;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.AbstractDecodeContext;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.decoders.DecodeContext.DecodePath;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class VerifyContext<T_Encoded, T_Decoded>
extends AbstractDecodeContext<
	T_Encoded,
	VerifyException,
	VerifyContext<T_Encoded, T_Decoded>
> {

	public static final @NotNull ObjectArrayFactory<VerifyContext<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(VerifyContext.class).generic();

	public final @Nullable T_Decoded object;

	public VerifyContext(
		@NotNull AutoCodec autoCodec,
		@Nullable AbstractDecodeContext<T_Encoded, ?, ?> parent,
		@NotNull DecodePath path,
		@NotNull Data data,
		@NotNull DynamicOps<T_Encoded> ops,
		@Nullable T_Decoded object
	) {
		super(autoCodec, parent, path, data, ops);
		this.object = object;
	}

	public VerifyContext(@NotNull AbstractDecodeContext<T_Encoded, ?, ?> context, @Nullable T_Decoded object) {
		super(context);
		this.object = object;
	}

	@Override
	@Internal
	public @NotNull VerifyContext<T_Encoded, T_Decoded> newContext(@Nullable AbstractDecodeContext<T_Encoded, ?, ?> parent, @NotNull DecodePath path, @NotNull Data input) {
		return new VerifyContext<>(
			this.autoCodec,
			parent,
			path,
			input,
			this.ops,
			this.object
		);
	}

	@Override
	public @NotNull VerifyException newException(@NotNull Supplier<@NotNull String> messageSupplier) {
		return new VerifyException(messageSupplier);
	}

	public <T_NewDecoded> @NotNull VerifyContext<T_Encoded, T_NewDecoded> object(@Nullable T_NewDecoded object) {
		return new VerifyContext<>(this, object);
	}

	public void verifyWith(@NotNull AutoVerifier<T_Decoded> verifier) throws VerifyException {
		this.logger().verify(verifier, this);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": { path: " + this.pathToString() + ", input: " + this.data + ", ops: " + this.ops + ", object: " + this.object + " }";
	}
}
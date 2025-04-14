package builderb0y.autocodec.imprinters;

import java.util.function.Supplier;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.AbstractDecodeContext;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.decoders.DecodeContext.DecodePath;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class ImprintContext<T_Encoded, T_Decoded>
extends AbstractDecodeContext<
	T_Encoded,
	ImprintException,
	ImprintContext<T_Encoded, T_Decoded>
> {

	public static final @NotNull ObjectArrayFactory<ImprintContext<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(ImprintContext.class).generic();

	public final @NotNull T_Decoded object;

	public ImprintContext(
		@NotNull AutoCodec autoCodec,
		@Nullable AbstractDecodeContext<T_Encoded, ?, ?> parent,
		@NotNull DecodePath path,
		@NotNull Data input,
		@NotNull DynamicOps<T_Encoded> ops,
		@NotNull T_Decoded object
	) {
		super(autoCodec, parent, path, input, ops);
		this.object = object;
	}

	public ImprintContext(@NotNull AbstractDecodeContext<T_Encoded, ?, ?> context, @NotNull T_Decoded object) {
		super(context);
		this.object = object;
	}

	@Override
	public @NotNull ImprintContext<T_Encoded, T_Decoded> newContext(
		@Nullable AbstractDecodeContext<T_Encoded, ?, ?> parent,
		@NotNull DecodePath path,
		@NotNull Data input
	) {
		return new ImprintContext<>(this.autoCodec, parent, path, input, this.ops, this.object);
	}

	@Override
	public @NotNull ImprintException newException(@NotNull Supplier<@NotNull String> messageSupplier) {
		return new ImprintException(messageSupplier);
	}

	@SuppressWarnings("unchecked")
	public <T_NewDecoded> @NotNull ImprintContext<T_Encoded, T_NewDecoded> object(@NotNull T_NewDecoded object) {
		return this.object == object ? (ImprintContext<T_Encoded, T_NewDecoded>)(this) : new ImprintContext<>(this, object);
	}

	public void imprintWith(@NotNull AutoImprinter<T_Decoded> imprinter) throws ImprintException {
		this.logger().imprint(imprinter, this);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": { path: " + this.pathToString() + ", input: " + this.input + ", ops: " + this.ops + ", object: " + this.object + " }";
	}
}
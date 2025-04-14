package builderb0y.autocodec.constructors;

import java.util.function.Supplier;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.AbstractDecodeContext;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeContext.DecodePath;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class ConstructContext<T_Encoded>
extends AbstractDecodeContext<
	T_Encoded,
	ConstructException,
	ConstructContext<T_Encoded>
> {

	public static final @NotNull ObjectArrayFactory<ConstructContext<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(ConstructContext.class).generic();

	public ConstructContext(
		@NotNull AutoCodec autoCodec,
		@Nullable AbstractDecodeContext<T_Encoded, ?, ?> parent,
		@NotNull DecodePath path,
		@NotNull Data input,
		@NotNull DynamicOps<T_Encoded> ops
	) {
		super(autoCodec, parent, path, input, ops);
	}

	public ConstructContext(@NotNull AbstractDecodeContext<T_Encoded, ?, ?> context) {
		super(context);
	}

	@Override
	public @NotNull ConstructContext<T_Encoded> newContext(
		@Nullable AbstractDecodeContext<T_Encoded, ?, ?> parent,
		@NotNull DecodeContext.DecodePath path,
		@NotNull Data input
	) {
		return new ConstructContext<>(this.autoCodec, parent, path, input, this.ops);
	}

	@Override
	public @NotNull ConstructException newException(@NotNull Supplier<@NotNull String> messageSupplier) {
		return new ConstructException(messageSupplier);
	}

	@Override
	public <T_Decoded> @NotNull T_Decoded constructWith(@NotNull AutoConstructor<T_Decoded> constructor) throws ConstructException {
		return this.logger().construct(constructor, this);
	}
}
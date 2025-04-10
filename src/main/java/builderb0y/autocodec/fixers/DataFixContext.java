package builderb0y.autocodec.fixers;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.decoders.DecodeContext;

public class DataFixContext<T_Encoded> extends DecodeContext<T_Encoded> {

	public DataFixContext(
		@NotNull AutoCodec autoCodec,
		@Nullable DecodeContext<T_Encoded> parent,
		@NotNull DecodePath path,
		@NotNull T_Encoded input,
		@NotNull DynamicOps<T_Encoded> ops
	) {
		super(autoCodec, parent, path, input, ops);
	}

	public DataFixContext(DecodeContext<T_Encoded> context) {
		super(context);
	}

	@Override
	public <T_Decoded> @NotNull DataFixContext<T_Encoded> fixWith(@NotNull AutoFixer<T_Decoded> fixer) {
		return this.logger().fix(fixer, this);
	}
}
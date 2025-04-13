package builderb0y.autocodec.fixers;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.data.DataWriter;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;

public class DataFixContext<T_Encoded> extends DecodeContext<T_Encoded> implements DataWriter<T_Encoded, DecodeException> {

	public DataFixContext(
		@NotNull AutoCodec autoCodec,
		@Nullable DecodeContext<T_Encoded> parent,
		@NotNull DecodePath path,
		@NotNull Data<T_Encoded> input,
		@NotNull DynamicOps<T_Encoded> ops
	) {
		super(autoCodec, parent, path, input, ops);
	}

	public DataFixContext(DecodeContext<T_Encoded> context) {
		super(context);
	}

	@Override
	public @NotNull DecodeException notA(@NotNull String type) {
		return new DataFixException(() -> this.pathToStringBuilder().append(" is not a ").append(type).append(": ").append(this.input).toString());
	}

	@Override
	public <T_Decoded> @NotNull DataFixContext<T_Encoded> fixWith(@NotNull AutoFixer<T_Decoded> fixer) throws DataFixException {
		return this.logger().fix(fixer, this);
	}
}
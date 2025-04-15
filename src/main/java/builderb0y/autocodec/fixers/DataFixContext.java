package builderb0y.autocodec.fixers;

import java.util.function.Supplier;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.AbstractDecodeContext;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.data.DataWriter;
import builderb0y.autocodec.data.EmptyData;
import builderb0y.autocodec.data.StringData;
import builderb0y.autocodec.decoders.DecodeContext.DecodePath;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class DataFixContext<T_Encoded>
extends AbstractDecodeContext<
	T_Encoded,
	DataFixException,
	DataFixContext<T_Encoded>
>
implements DataWriter<DataFixException> {

	public static final @NotNull ObjectArrayFactory<DataFixContext<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(DataFixContext.class).generic();

	public DataFixContext(
		@NotNull AutoCodec autoCodec,
		@Nullable AbstractDecodeContext<T_Encoded, ?, ?> parent,
		@NotNull DecodePath path,
		@NotNull Data data,
		@NotNull DynamicOps<T_Encoded> ops
	) {
		super(autoCodec, parent, path, data, ops);
	}

	public DataFixContext(@NotNull AbstractDecodeContext<T_Encoded, ?, ?> context) {
		super(context);
	}

	@Override
	public @NotNull DataFixContext<T_Encoded> newContext(@Nullable AbstractDecodeContext<T_Encoded, ?, ?> parent, @NotNull DecodePath path, @NotNull Data input) {
		return new DataFixContext<>(this.autoCodec, parent, path, input, this.ops);
	}

	@Override
	public @NotNull DataFixException newException(@NotNull Supplier<@NotNull String> messageSupplier) {
		return new DataFixException(messageSupplier);
	}

	@Override
	public <T_Decoded> @NotNull DataFixContext<T_Encoded> fixDataWith(@NotNull AutoFixer<T_Decoded> fixer) throws DataFixException {
		return this.logger().fixData(fixer, this);
	}

	@Override
	public @NotNull DataFixContext<T_Encoded> getElement(int index) throws DataFixException {
		return this.input(index, this.forceAsList().value.get(index));
	}

	@Override
	public @NotNull DataFixContext<T_Encoded> getMember(@NotNull String key) throws DataFixException {
		Data member = this.forceAsMap().value.get(new StringData(key));
		return this.input(key, member != null ? member : EmptyData.INSTANCE);
	}
}
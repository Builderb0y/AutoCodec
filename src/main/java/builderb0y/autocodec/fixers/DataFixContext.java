package builderb0y.autocodec.fixers;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.common.AbstractDecodeContext;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.data.DataWriter;
import builderb0y.autocodec.decoders.DecodeContext.ArrayDecodePath;
import builderb0y.autocodec.decoders.DecodeContext.DecodePath;
import builderb0y.autocodec.decoders.DecodeContext.ObjectDecodePath;

public class DataFixContext<T_Encoded> extends AbstractDecodeContext<T_Encoded, DataFixException> implements DataWriter<T_Encoded, DataFixException> {

	public DataFixContext(
		@NotNull AutoCodec autoCodec,
		@Nullable AbstractDecodeContext<T_Encoded, ?> parent,
		@NotNull DecodePath path,
		@NotNull Data<T_Encoded> input,
		@NotNull DynamicOps<T_Encoded> ops
	) {
		super(autoCodec, parent, path, input, ops);
	}

	public DataFixContext(@NotNull AbstractDecodeContext<T_Encoded, ?> context) {
		super(context);
	}

	public @NotNull DataFixContext<T_Encoded> input(@NotNull Data<T_Encoded> input) {
		return this.input == input ? this : new DataFixContext<>(this.autoCodec, this.parent, this.path, input, this.ops);
	}

	public @NotNull DataFixContext<T_Encoded> input(@NotNull Data<T_Encoded> input, @NotNull DecodePath nextPath) {
		return new DataFixContext<>(this.autoCodec, this, nextPath, input, this.ops);
	}

	public @NotNull DataFixContext<T_Encoded> input(@NotNull String memberName, @NotNull Data<T_Encoded> member) {
		return this.input(member, new ObjectDecodePath(memberName));
	}

	public @NotNull DataFixContext<T_Encoded> input(int index, @NotNull Data<T_Encoded> element) {
		return this.input(element, new ArrayDecodePath(index));
	}

	@Override
	public @NotNull DataFixException notA(@NotNull String type) {
		return new DataFixException(() -> this.pathToStringBuilder().append(" is not a ").append(type).append(": ").append(this.input).toString());
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
		Data<T_Encoded> member = this.forceAsMap().value.get(this.createString(key));
		return this.input(key, member != null ? member : this.empty());
	}
}
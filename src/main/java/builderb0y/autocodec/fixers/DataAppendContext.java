package builderb0y.autocodec.fixers;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.data.DataWriter;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;

public class DataAppendContext<T_Encoded, T_Decoded> extends EncodeContext<T_Encoded, T_Decoded> implements DataWriter<T_Encoded, DataAppendException> {

	public /* non-final */ @NotNull Data<T_Encoded> data;

	public DataAppendContext(
		@NotNull AutoCodec codec,
		@Nullable T_Decoded object,
		@NotNull Data<T_Encoded> data,
		@NotNull DynamicOps<T_Encoded> ops
	) {
		super(codec, object, ops);
		this.data = data;
	}

	public DataAppendContext(
		@NotNull EncodeContext<T_Encoded, T_Decoded> encodeContext,
		@NotNull Data<T_Encoded> data
	) {
		this(encodeContext.autoCodec, encodeContext.object, data, encodeContext.ops);
	}

	@Override
	public @NotNull Data<T_Encoded> data() {
		return this.data;
	}

	@Override
	public @NotNull DataAppendException notA(@NotNull String type) {
		return new DataAppendException(() -> "Not a " + type + ": " + this.data);
	}

	@Override
	public @NotNull DataAppendContext<T_Encoded, T_Decoded> getElement(int index) throws EncodeException {
		return new DataAppendContext<>(this.autoCodec, this.object, this.forceAsList().value.get(index), this.ops);
	}

	@Override
	public @NotNull DataAppendContext<T_Encoded, T_Decoded> getMember(@NotNull String key) throws DataAppendException {
		Data<T_Encoded> member = this.forceAsMap().value.get(this.createString(key));
		return new DataAppendContext<>(this.autoCodec, this.object, member != null ? member : this.empty(), this.ops);
	}

	public @NotNull DataAppendContext<T_Encoded, T_Decoded> appendDataWith(@NotNull AutoFixer<T_Decoded> fixer) throws DataAppendException {
		return this.logger().appendData(fixer, this);
	}
}
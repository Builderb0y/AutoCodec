package builderb0y.autocodec.fixers;

import java.util.*;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.data.DataWriter;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;

public class DataAppendContext<T_Encoded, T_Decoded> extends EncodeContext<T_Encoded, T_Decoded> implements DataWriter<DataAppendException> {

	public /* non-final */ @NotNull Data data;

	public DataAppendContext(
		@NotNull AutoCodec codec,
		@Nullable T_Decoded object,
		@NotNull Data data,
		@NotNull DynamicOps<T_Encoded> ops
	) {
		super(codec, object, ops);
		this.data = data;
	}

	public DataAppendContext(
		@NotNull EncodeContext<T_Encoded, T_Decoded> encodeContext,
		@NotNull Data data
	) {
		this(encodeContext.autoCodec, encodeContext.object, data, encodeContext.ops);
	}

	@Override
	public @NotNull Data data() {
		return this.data;
	}

	@Override
	public @NotNull DataAppendException notA(@NotNull String type) {
		return new DataAppendException(() -> "Not a " + type + ": " + this.data);
	}

	public @NotNull DataAppendContext<T_Encoded, T_Decoded> input(@NotNull Data data) {
		return new DataAppendContext<>(this, data);
	}

	@Override
	public @NotNull DataAppendContext<T_Encoded, T_Decoded> getElement(int index) throws EncodeException {
		return this.input(this.forceAsList().value.get(index));
	}

	@Override
	public @NotNull DataAppendContext<T_Encoded, T_Decoded> getMember(@NotNull String key) throws DataAppendException {
		Data member = this.forceAsMap().value.get(this.createString(key));
		return this.input(member != null ? member : this.empty());
	}

	@Override
	public @NotNull Iterable<@NotNull DataAppendContext<T_Encoded, T_Decoded>> listIterable() throws DataAppendException {
		List<Data> list = this.forceAsList().value;
		return () -> {
			ListIterator<Data> iterator = list.listIterator();
			return new Iterator<>() {

				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public @NotNull DataAppendContext<T_Encoded, T_Decoded> next() {
					return DataAppendContext.this.input(iterator.next());
				}
			};
		};
	}

	@Override
	public @NotNull Iterable<? extends Map.Entry<@NotNull DataAppendContext<T_Encoded, T_Decoded>, @NotNull DataAppendContext<T_Encoded, T_Decoded>>> mapIterable() throws DataAppendException {
		Set<Map.Entry<Data, Data>> entrySet = this.forceAsMap().value.entrySet();
		return () -> {
			Iterator<Map.Entry<Data, Data>> iterator = entrySet.iterator();
			return new Iterator<>() {

				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public Map.Entry<DataAppendContext<T_Encoded, T_Decoded>, DataAppendContext<T_Encoded, T_Decoded>> next() {
					Map.Entry<Data, Data> next = iterator.next();
					return Map.entry(
						DataAppendContext.this.input(next.getKey()),
						DataAppendContext.this.input(next.getValue())
					);
				}
			};
		};
	}

	public @NotNull DataAppendContext<T_Encoded, T_Decoded> appendDataWith(@NotNull AutoFixer<T_Decoded> fixer) throws DataAppendException {
		return this.logger().appendData(fixer, this);
	}
}
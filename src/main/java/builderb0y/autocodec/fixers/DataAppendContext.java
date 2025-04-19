package builderb0y.autocodec.fixers;

import java.util.*;
import java.util.stream.Stream;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.data.*;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.util.StreamableIterable;
import builderb0y.autocodec.util.StreamableIterable.SingletonStreamableIterable;

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

	public @NotNull DataAppendContext<T_Encoded, T_Decoded> withData(@NotNull Data data) {
		return new DataAppendContext<>(this, data);
	}

	@Override
	public @NotNull DataReader<DataAppendException> tryGetElement(int index) {
		ListData list = this.tryAsList();
		return this.withData(list != null ? list.get(index) : EmptyData.INSTANCE);
	}

	@Override
	public @NotNull DataReader<DataAppendException> tryGetMember(@NotNull String key) {
		MapData map = this.tryAsMap();
		return this.withData(map != null ? map.get(key) : EmptyData.INSTANCE);
	}

	@Override
	public @NotNull DataAppendContext<T_Encoded, T_Decoded> forceGetElement(int index) throws DataAppendException {
		return this.withData(this.forceAsList().value.get(index));
	}

	@Override
	public @NotNull DataAppendContext<T_Encoded, T_Decoded> forceGetMember(@NotNull String key) throws DataAppendException {
		Data member = this.forceAsMap().value.get(new StringData(key));
		return this.withData(member != null ? member : EmptyData.INSTANCE);
	}

	@Internal
	public @NotNull StreamableIterable<@NotNull DataAppendContext<T_Encoded, T_Decoded>> createListIterable(List<Data> list) {
		return () -> {
			ListIterator<Data> iterator = list.listIterator();
			return new Iterator<>() {

				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public @NotNull DataAppendContext<T_Encoded, T_Decoded> next() {
					return DataAppendContext.this.withData(iterator.next());
				}
			};
		};
	}

	@Override
	public @NotNull StreamableIterable<@NotNull DataAppendContext<T_Encoded, T_Decoded>> listIterable() throws DataAppendException {
		return this.createListIterable(this.forceAsList().value);
	}

	@Override
	public @NotNull StreamableIterable<@NotNull DataAppendContext<T_Encoded, T_Decoded>> listIterableOrSingleton() throws DataAppendException {
		ListData list = this.tryAsList();
		if (list != null) {
			return this.createListIterable(list.value);
		}
		else {
			return new SingletonStreamableIterable<>(this);
		}
	}

	@Override
	public @NotNull StreamableIterable<? extends @NotNull DataReader<DataAppendException>> listIterableMaybeSingleton(boolean singleton) throws DataAppendException {
		return singleton ? this.listIterableOrSingleton() : this.listIterable();
	}

	@Override
	public @NotNull StreamableIterable<? extends Map.Entry<@NotNull DataAppendContext<T_Encoded, T_Decoded>, @NotNull DataAppendContext<T_Encoded, T_Decoded>>> mapIterable() throws DataAppendException {
		Set<Map.Entry<Data, Data>> entrySet = this.forceAsMap().value.entrySet();
		return new StreamableIterable<>() {

			@Override
			public @NotNull Iterator<Map.Entry<DataAppendContext<T_Encoded, T_Decoded>, DataAppendContext<T_Encoded, T_Decoded>>> iterator() {
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
							DataAppendContext.this.withData(next.getKey()),
							DataAppendContext.this.withData(next.getValue())
						);
					}
				};
			}

			@Override
			public @NotNull Stream<Map.Entry<@NotNull DataAppendContext<T_Encoded, T_Decoded>, @NotNull DataAppendContext<T_Encoded, T_Decoded>>> stream() {
				return entrySet.stream().map((Map.Entry<Data, Data> entry) -> Map.entry(
					DataAppendContext.this.withData(entry.getKey()),
					DataAppendContext.this.withData(entry.getValue())
				));
			}
		};
	}

	public @NotNull DataAppendContext<T_Encoded, T_Decoded> appendDataWith(@NotNull AutoFixer<T_Decoded> fixer) throws DataAppendException {
		return this.logger().appendData(fixer, this);
	}
}
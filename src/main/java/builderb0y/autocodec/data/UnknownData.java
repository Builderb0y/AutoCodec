package builderb0y.autocodec.data;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.util.AutoCodecUtil;
import builderb0y.autocodec.util.DFUVersions;

public class UnknownData<T_Encoded> extends Data<T_Encoded> {

	public @NotNull T_Encoded payload;
	public @Nullable Data<T_Encoded> resolution;

	public UnknownData(@NotNull DynamicOps<T_Encoded> ops, @NotNull T_Encoded payload) {
		super(ops);
		this.payload = payload;
	}

	@Override
	public @NotNull T_Encoded encode() {
		return this.payload;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		if (this.resolution != null) {
			return this.resolution.convert(ops);
		}
		else if (this.ops == ops) {
			return (T_NewEncoded)(this.payload);
		}
		else {
			return this.ops.convertTo(ops, this.payload);
		}
	}

	@Override
	public boolean isEmpty() {
		return Objects.equals(this.payload, this.ops.empty());
	}

	@Override
	public @Nullable Boolean tryAsBoolean() {
		if (this.resolution != null) {
			if (this.resolution instanceof BooleanData<?> data) {
				return data.value;
			}
		}
		else {
			Boolean value = DFUVersions.getResult(this.ops.getBooleanValue(this.payload));
			if (value != null) {
				this.resolution = new BooleanData<>(this.ops, value);
				return value;
			}
		}
		return null;
	}

	@Override
	public @Nullable Number tryAsNumber() {
		if (this.resolution != null) {
			if (this.resolution instanceof NumberData<T_Encoded> data) {
				return data.tryAsNumber();
			}
		}
		else {
			Number value = DFUVersions.getResult(this.ops.getNumberValue(this.payload));
			if (value != null) {
				this.resolution = new UnknownNumberData<>(this.ops, value);
				return value;
			}
		}
		return null;
	}

	@Override
	public @Nullable Byte tryAsByte() {
		if (this.resolution != null) {
			if (this.resolution instanceof ByteData<T_Encoded> data) {
				return data.byteValue();
			}
		}
		else {
			Number value = DFUVersions.getResult(this.ops.getNumberValue(this.payload));
			if (value != null) {
				byte byteValue = value.byteValue();
				this.resolution = new ByteData<>(this.ops, byteValue);
				return byteValue;
			}
		}
		return null;
	}

	@Override
	public @Nullable Short tryAsShort() {
		if (this.resolution != null) {
			if (this.resolution instanceof ShortData<T_Encoded> data) {
				return data.shortValue();
			}
		}
		else {
			Number value = DFUVersions.getResult(this.ops.getNumberValue(this.payload));
			if (value != null) {
				short shortValue = value.shortValue();
				this.resolution = new ShortData<>(this.ops, shortValue);
				return shortValue;
			}
		}
		return null;
	}

	@Override
	public @Nullable Integer tryAsInt() {
		if (this.resolution != null) {
			if (this.resolution instanceof IntData<T_Encoded> data) {
				return data.intValue();
			}
		}
		else {
			Number value = DFUVersions.getResult(this.ops.getNumberValue(this.payload));
			if (value != null) {
				int intValue = value.intValue();
				this.resolution = new IntData<>(this.ops, intValue);
				return intValue;
			}
		}
		return null;
	}

	@Override
	public @Nullable Long tryAsLong() {
		if (this.resolution != null) {
			if (this.resolution instanceof LongData<T_Encoded> data) {
				return data.longValue();
			}
		}
		else {
			Number value = DFUVersions.getResult(this.ops.getNumberValue(this.payload));
			if (value != null) {
				long longValue = value.longValue();
				this.resolution = new LongData<>(this.ops, longValue);
				return longValue;
			}
		}
		return null;
	}

	@Override
	public @Nullable Float tryAsFloat() {
		if (this.resolution != null) {
			if (this.resolution instanceof FloatData<T_Encoded> data) {
				return data.floatValue();
			}
		}
		else {
			Number value = DFUVersions.getResult(this.ops.getNumberValue(this.payload));
			if (value != null) {
				float floatValue = value.floatValue();
				this.resolution = new FloatData<>(this.ops, floatValue);
				return floatValue;
			}
		}
		return null;
	}

	@Override
	public @Nullable Double tryAsDouble() {
		if (this.resolution != null) {
			if (this.resolution instanceof DoubleData<T_Encoded> data) {
				return data.doubleValue();
			}
		}
		else {
			Number value = DFUVersions.getResult(this.ops.getNumberValue(this.payload));
			if (value != null) {
				double doubleValue = value.doubleValue();
				this.resolution = new DoubleData<>(this.ops, doubleValue);
				return doubleValue;
			}
		}
		return null;
	}

	@Override
	public @Nullable String tryAsString() {
		if (this.resolution != null) {
			if (this.resolution instanceof StringData<T_Encoded> data) {
				return data.tryAsString();
			}
		}
		else {
			String value = DFUVersions.getResult(this.ops.getStringValue(this.payload));
			if (value != null) {
				this.resolution = new StringData<>(this.ops, value);
				return value;
			}
		}
		return null;
	}

	@Override
	public @Nullable List<@NotNull Data<T_Encoded>> tryAsList() {
		if (this.resolution != null) {
			if (this.resolution instanceof ListData<T_Encoded> data) {
				return data.tryAsList();
			}
		}
		else {
			Stream<T_Encoded> value = DFUVersions.getResult(this.ops.getStream(this.payload));
			if (value != null) {
				ObjectArrayList<Data<T_Encoded>> listValue = value.map((T_Encoded encoded) -> new UnknownData<>(this.ops, encoded)).collect(Collectors.toCollection(ObjectArrayList::new));
				this.resolution = new ListData<>(this.ops, listValue);
				return listValue;
			}
		}
		return null;
	}

	@Override
	public @Nullable ByteList tryAsByteList() {
		if (this.resolution != null) {
			if (this.resolution instanceof ByteArrayData<T_Encoded> data) {
				return data.tryAsByteList();
			}
		}
		else {
			ByteBuffer value = DFUVersions.getResult(this.ops.getByteBuffer(this.payload));
			if (value != null) {
				byte[] bytes = new byte[value.limit()];
				value.get(0, bytes);
				ByteList list = ByteArrayList.wrap(bytes);
				this.resolution = new ByteArrayData<>(this.ops, list);
				return list;
			}
		}
		return null;
	}

	@Override
	public @Nullable IntList tryAsIntList() {
		if (this.resolution != null) {
			if (this.resolution instanceof IntArrayData<T_Encoded> data) {
				return data.tryAsIntList();
			}
		}
		else {
			IntStream stream = DFUVersions.getResult(this.ops.getIntStream(this.payload));
			if (stream != null) {
				IntList list = IntArrayList.wrap(stream.toArray());
				this.resolution = new IntArrayData<>(this.ops, list);
				return list;
			}
		}
		return null;
	}

	@Override
	public @Nullable LongList tryAsLongList() {
		if (this.resolution != null) {
			if (this.resolution instanceof LongArrayData<T_Encoded> data) {
				return data.tryAsLongList();
			}
		}
		else {
			LongStream stream = DFUVersions.getResult(this.ops.getLongStream(this.payload));
			if (stream != null) {
				LongList list = LongArrayList.wrap(stream.toArray());
				this.resolution = new LongArrayData<>(this.ops, list);
				return list;
			}
		}
		return null;
	}

	@Override
	public @Nullable Map<@NotNull Data<T_Encoded>, @NotNull Data<T_Encoded>> tryAsMap() {
		if (this.resolution != null) {
			if (this.resolution instanceof MapData<T_Encoded> data) {
				return data.tryAsMap();
			}
		}
		else {
			Stream<Pair<T_Encoded, T_Encoded>> stream = DFUVersions.getResult(this.ops.getMapValues(this.payload));
			if (stream != null) {
				Map<Data<T_Encoded>, Data<T_Encoded>> map = stream.collect(AutoCodecUtil.collectToMap((Pair<T_Encoded, T_Encoded> pair) -> new UnknownData<>(this.ops, pair.getFirst()), (Pair<T_Encoded, T_Encoded> pair) -> new UnknownData<>(this.ops, pair.getSecond()), Object2ObjectOpenHashMap::new));
				this.resolution = new MapData<>(this.ops, map);
				return map;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof UnknownData<?> that && this.payload.equals(that.payload);
	}

	@Override
	public int hashCode() {
		return this.payload.hashCode();
	}

	@Override
	public String toString() {
		return this.payload.toString();
	}
}
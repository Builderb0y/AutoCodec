package builderb0y.autocodec.data;

import java.nio.ByteBuffer;
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
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.util.AutoCodecUtil;
import builderb0y.autocodec.util.DFUVersions;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class UnknownData<T_Encoded> extends Data {

	public static final @NotNull ObjectArrayFactory<UnknownData<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(UnknownData.class).generic();

	public final DynamicOps<T_Encoded> ops;
	public @NotNull T_Encoded payload;
	public @Nullable Data resolution;

	public UnknownData(@NotNull DynamicOps<T_Encoded> ops, @Nullable T_Encoded payload) {
		this.ops = ops;
		//note: some ops can return null in some places.
		//most of them will be caught here.
		this.payload = payload != null ? payload : ops.empty();
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
	public boolean isBoolean() {
		if (this.resolution != null) {
			return this.resolution.isBoolean();
		}
		else {
			return DFUVersions.getResult(this.ops.getBooleanValue(this.payload)) != null;
		}
	}

	@Override
	public @Nullable BooleanData tryAsBoolean() {
		if (this.resolution != null) {
			return this.resolution.tryAsBoolean();
		}
		else {
			Boolean value = DFUVersions.getResult(this.ops.getBooleanValue(this.payload));
			if (value != null) {
				BooleanData resolution = new BooleanData(value);
				this.resolution = resolution;
				return resolution;
			}
		}
		return null;
	}

	@Override
	public boolean isNumber() {
		if (this.resolution != null) {
			return this.resolution.isNumber();
		}
		else {
			return DFUVersions.getResult(this.ops.getNumberValue(this.payload)) != null;
		}
	}

	@Override
	public @Nullable AbstractNumberData tryAsNumber() {
		if (this.resolution != null) {
			return this.resolution.tryAsNumber();
		}
		else {
			Number value = DFUVersions.getResult(this.ops.getNumberValue(this.payload));
			if (value != null) {
				UnknownNumberData numberData = new UnknownNumberData(value);
				this.resolution = numberData;
				return numberData;
			}
		}
		return null;
	}

	@Override
	public boolean isString() {
		if (this.resolution != null) {
			return this.resolution.isString();
		}
		else {
			return DFUVersions.getResult(this.ops.getStringValue(this.payload)) != null;
		}
	}

	@Override
	public @Nullable StringData tryAsString() {
		if (this.resolution != null) {
			return this.resolution.tryAsString();
		}
		else {
			String value = DFUVersions.getResult(this.ops.getStringValue(this.payload));
			if (value != null) {
				StringData resolution = new StringData(value);
				this.resolution = resolution;
				return resolution;
			}
		}
		return null;
	}

	@Override
	public boolean isList() {
		if (this.resolution != null) {
			return this.resolution.isList();
		}
		else {
			return DFUVersions.getResult(this.ops.getStream(this.payload)) != null;
		}
	}

	@Override
	public @Nullable ListData tryAsList() {
		if (this.resolution != null) {
			return this.resolution.tryAsList();
		}
		else {
			Stream<T_Encoded> value = DFUVersions.getResult(this.ops.getStream(this.payload));
			if (value != null) {
				ObjectArrayList<Data> listValue = value.map((T_Encoded encoded) -> new UnknownData<>(this.ops, encoded)).collect(Collectors.toCollection(ObjectArrayList::new));
				ListData resolution = new ListData(listValue);
				this.resolution = resolution;
				return resolution;
			}
		}
		return null;
	}

	@Override
	public boolean isByteList() {
		if (this.resolution != null) {
			return this.resolution.isByteList();
		}
		else {
			return DFUVersions.getResult(this.ops.getByteBuffer(this.payload)) != null;
		}
	}

	@Override
	public @Nullable ByteListData tryAsByteList() {
		if (this.resolution != null) {
			return this.resolution.tryAsByteList();
		}
		else {
			ByteBuffer value = DFUVersions.getResult(this.ops.getByteBuffer(this.payload));
			if (value != null) {
				byte[] bytes = new byte[value.limit()];
				value.get(0, bytes);
				ByteList list = ByteArrayList.wrap(bytes);
				ByteListData resolution = new ByteListData(list);
				this.resolution = resolution;
				return resolution;
			}
		}
		return null;
	}

	@Override
	public boolean isIntList() {
		if (this.resolution != null) {
			return this.resolution.isIntList();
		}
		else {
			return DFUVersions.getResult(this.ops.getIntStream(this.payload)) != null;
		}
	}

	@Override
	public @Nullable IntListData tryAsIntList() {
		if (this.resolution != null) {
			return this.resolution.tryAsIntList();
		}
		else {
			IntStream stream = DFUVersions.getResult(this.ops.getIntStream(this.payload));
			if (stream != null) {
				IntList list = IntArrayList.wrap(stream.toArray());
				IntListData resolution = new IntListData(list);
				this.resolution = resolution;
				return resolution;
			}
		}
		return null;
	}

	@Override
	public boolean isLongList() {
		if (this.resolution != null) {
			return this.resolution.isLongList();
		}
		else {
			return DFUVersions.getResult(this.ops.getLongStream(this.payload)) != null;
		}
	}

	@Override
	public @Nullable LongListData tryAsLongList() {
		if (this.resolution != null) {
			return this.resolution.tryAsLongList();
		}
		else {
			LongStream stream = DFUVersions.getResult(this.ops.getLongStream(this.payload));
			if (stream != null) {
				LongList list = LongArrayList.wrap(stream.toArray());
				LongListData resolution = new LongListData(list);
				this.resolution = resolution;
				return resolution;
			}
		}
		return null;
	}

	@Override
	public boolean isMap() {
		if (this.resolution != null) {
			return this.resolution.isMap();
		}
		else {
			return DFUVersions.getResult(this.ops.getMapValues(this.payload)) != null;
		}
	}

	@Override
	public @Nullable MapData tryAsMap() {
		if (this.resolution != null) {
			return this.resolution.tryAsMap();
		}
		else {
			Stream<Pair<T_Encoded, T_Encoded>> stream = DFUVersions.getResult(this.ops.getMapValues(this.payload));
			if (stream != null) {
				Object2ObjectMap<Data, Data> map = stream.collect(
					AutoCodecUtil.collectToMap(
						(Pair<T_Encoded, T_Encoded> pair) -> new UnknownData<>(this.ops, pair.getFirst()),
						(Pair<T_Encoded, T_Encoded> pair) -> new UnknownData<>(this.ops, pair.getSecond()),
						Object2ObjectOpenHashMap::new
					)
				);
				MapData resolution = new MapData(map);
				this.resolution = resolution;
				return resolution;
			}
		}
		return null;
	}

	public @Nullable Data resolve() {
		Data resolution = this.resolution;
		notDone:
		if (resolution == null) {
			done: {
				if (this.isEmpty()) { resolution = EmptyData.INSTANCE; break done; }
				if ((resolution = this.tryAsMap     ()) != null) break done;
				if ((resolution = this.tryAsByteList()) != null) break done;
				if ((resolution = this.tryAsIntList ()) != null) break done;
				if ((resolution = this.tryAsLongList()) != null) break done;
				if ((resolution = this.tryAsList    ()) != null) break done;
				if ((resolution = this.tryAsBoolean ()) != null) break done;
				if ((resolution = this.tryAsNumber  ()) != null) break done;
				if ((resolution = this.tryAsString  ()) != null) break done;
				break notDone;
			}
			this.resolution = resolution;
		}
		return resolution;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Data data) {
			Data resolution = this.resolve();
			if (resolution != null) {
				return resolution.equals(data);
			}
			else {
				return this.payload.equals(data.convert(this.ops)); //future-proof unknown value types.
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		Data resolution = this.resolve();
		return (resolution != null ? resolution : this.payload).hashCode();
	}

	@Override
	public String toString() {
		return (this.resolution != null ? this.resolution : this.payload).toString();
	}

	@Override
	public @NotNull UnknownData<T_Encoded> deepCopy() {
		UnknownData<T_Encoded> copy = new UnknownData<>(this.ops, this.payload);
		if (this.resolution != null) {
			copy.resolution = this.resolution.deepCopy();
		}
		return copy;
	}
}
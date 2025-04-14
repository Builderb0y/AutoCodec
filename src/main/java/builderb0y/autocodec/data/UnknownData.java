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
	public boolean isBoolean() {
		if (this.resolution != null) {
			return this.resolution.isBoolean();
		}
		else {
			return DFUVersions.getResult(this.ops.getBooleanValue(this.payload)) != null;
		}
	}

	@Override
	public @Nullable BooleanData<T_Encoded> tryAsBoolean() {
		if (this.resolution != null) {
			return this.resolution.tryAsBoolean();
		}
		else {
			Boolean value = DFUVersions.getResult(this.ops.getBooleanValue(this.payload));
			if (value != null) {
				BooleanData<T_Encoded> resolution = new BooleanData<>(this.ops, value);
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
	public @Nullable AbstractNumberData<T_Encoded> tryAsNumber() {
		if (this.resolution != null) {
			return this.resolution.tryAsNumber();
		}
		else {
			Number value = DFUVersions.getResult(this.ops.getNumberValue(this.payload));
			if (value != null) {
				UnknownNumberData<T_Encoded> numberData = new UnknownNumberData<>(this.ops, value);
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
	public @Nullable StringData<T_Encoded> tryAsString() {
		if (this.resolution != null) {
			return this.resolution.tryAsString();
		}
		else {
			String value = DFUVersions.getResult(this.ops.getStringValue(this.payload));
			if (value != null) {
				StringData<T_Encoded> resolution = new StringData<>(this.ops, value);
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
	public @Nullable ListData<T_Encoded> tryAsList() {
		if (this.resolution != null) {
			return this.resolution.tryAsList();
		}
		else {
			Stream<T_Encoded> value = DFUVersions.getResult(this.ops.getStream(this.payload));
			if (value != null) {
				ObjectArrayList<Data<T_Encoded>> listValue = value.map((T_Encoded encoded) -> new UnknownData<>(this.ops, encoded)).collect(Collectors.toCollection(ObjectArrayList::new));
				ListData<T_Encoded> resolution = new ListData<>(this.ops, listValue);
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
	public @Nullable ByteListData<T_Encoded> tryAsByteList() {
		if (this.resolution != null) {
			return this.resolution.tryAsByteList();
		}
		else {
			ByteBuffer value = DFUVersions.getResult(this.ops.getByteBuffer(this.payload));
			if (value != null) {
				byte[] bytes = new byte[value.limit()];
				value.get(0, bytes);
				ByteList list = ByteArrayList.wrap(bytes);
				ByteListData<T_Encoded> resolution = new ByteListData<>(this.ops, list);
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
	public @Nullable IntListData<T_Encoded> tryAsIntList() {
		if (this.resolution != null) {
			return this.resolution.tryAsIntList();
		}
		else {
			IntStream stream = DFUVersions.getResult(this.ops.getIntStream(this.payload));
			if (stream != null) {
				IntList list = IntArrayList.wrap(stream.toArray());
				IntListData<T_Encoded> resolution = new IntListData<>(this.ops, list);
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
	public @Nullable LongListData<T_Encoded> tryAsLongList() {
		if (this.resolution != null) {
			return this.resolution.tryAsLongList();
		}
		else {
			LongStream stream = DFUVersions.getResult(this.ops.getLongStream(this.payload));
			if (stream != null) {
				LongList list = LongArrayList.wrap(stream.toArray());
				LongListData<T_Encoded> resolution = new LongListData<>(this.ops, list);
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
	public @Nullable MapData<T_Encoded> tryAsMap() {
		if (this.resolution != null) {
			return this.resolution.tryAsMap();
		}
		else {
			Stream<Pair<T_Encoded, T_Encoded>> stream = DFUVersions.getResult(this.ops.getMapValues(this.payload));
			if (stream != null) {
				Object2ObjectMap<Data<T_Encoded>, Data<T_Encoded>> map = stream.collect(
					AutoCodecUtil.collectToMap(
						(Pair<T_Encoded, T_Encoded> pair) -> new UnknownData<>(this.ops, pair.getFirst()),
						(Pair<T_Encoded, T_Encoded> pair) -> new UnknownData<>(this.ops, pair.getSecond()),
						Object2ObjectOpenHashMap::new
					)
				);
				MapData<T_Encoded> resolution = new MapData<>(this.ops, map);
				this.resolution = resolution;
				return resolution;
			}
		}
		return null;
	}

	public @Nullable Data<T_Encoded> resolve() {
		Data<T_Encoded> resolution = this.resolution;
		notDone:
		if (resolution == null) {
			done: {
				if (this.isEmpty()) { resolution = EmptyData.forOps(this.ops); break done; }
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
		if (object instanceof Data<?> data) {
			Data<T_Encoded> resolution = this.resolve();
			if (resolution != null) {
				return resolution.equals(data);
			}
			else {
				return this.encode().equals(data.encode()); //future-proof unknown value types.
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		Data<T_Encoded> resolution = this.resolve();
		return (resolution != null ? resolution : this.payload).hashCode();
	}

	@Override
	public String toString() {
		return (this.resolution != null ? this.resolution : this.payload).toString();
	}

	@Override
	public @NotNull Data<T_Encoded> deepCopy() {
		UnknownData<T_Encoded> copy = new UnknownData<>(this.ops, this.payload);
		if (this.resolution != null) {
			copy.resolution = this.resolution.deepCopy();
		}
		return copy;
	}
}
package builderb0y.autocodec.data;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static builderb0y.autocodec.util.DFUVersions.createErrorDataResult;
import static builderb0y.autocodec.util.DFUVersions.createSuccessDataResult;

public class DataOps implements DynamicOps<Data> {

	public static final @NotNull DataOps
		COMPRESSED   = new DataOps(true),
		UNCOMPRESSED = new DataOps(false);

	public final boolean compressed;

	public DataOps(boolean compressed) {
		this.compressed = compressed;
	}

	@Override
	public boolean compressMaps() {
		return this.compressed;
	}

	@Override
	public Data empty() {
		return EmptyData.INSTANCE;
	}

	@Override
	public Data emptyMap() {
		return new MapData();
	}

	@Override
	public Data emptyList() {
		return new ListData();
	}

	@Override
	public <U> U convertTo(DynamicOps<U> ops, Data data) {
		return data.convert(ops);
	}

	@Override
	public DataResult<Number> getNumberValue(Data data) {
		AbstractNumberData number = data.tryAsNumber();
		return number != null ? createSuccessDataResult(number.numberValue()) : createErrorDataResult(() -> "Not a number: " + data);
	}

	@Override
	public Number getNumberValue(Data input, Number defaultValue) {
		AbstractNumberData number = input.tryAsNumber();
		return number != null ? number.numberValue() : defaultValue;
	}

	@Override
	public Data createNumeric(Number number) {
		return new UnknownNumberData(number);
	}

	@Override
	public DataResult<Boolean> getBooleanValue(Data input) {
		BooleanData bool = input.tryAsBoolean();
		return bool != null ? createSuccessDataResult(bool.value) : createErrorDataResult(() -> "Not a boolean: " + input);
	}

	@Override
	public Data createBoolean(boolean value) {
		return new BooleanData(value);
	}

	@Override
	public Data createByte(byte value) {
		return new NumberData(value);
	}

	@Override
	public DataResult<ByteBuffer> getByteBuffer(Data input) {
		ByteListData byteList = input.tryAsByteList();
		if (byteList != null) {
			if (byteList.value instanceof ByteArrayList arrayList) {
				return createSuccessDataResult(ByteBuffer.wrap(arrayList.elements(), 0, arrayList.size()));
			}
			else {
				return createSuccessDataResult(ByteBuffer.wrap(byteList.value.toByteArray()));
			}
		}
		else {
			return createErrorDataResult(() -> "Not a byte list: " + input);
		}
	}

	@Override
	public Data createByteList(ByteBuffer input) {
		ByteBuffer wholeBuffer = input.duplicate().clear();
		ByteArrayList result = new ByteArrayList();
		result.size(wholeBuffer.capacity());
		wholeBuffer.get(0, result.elements(), 0, result.size());
		return new ByteListData(result);
	}

	@Override
	public Data createDouble(double value) {
		return new NumberData(value);
	}

	@Override
	public Data createFloat(float value) {
		return new NumberData(value);
	}

	@Override
	public Data createInt(int value) {
		return new NumberData(value);
	}

	@Override
	public DataResult<IntStream> getIntStream(Data input) {
		IntListData list = input.tryAsIntList();
		return list != null ? createSuccessDataResult(list.value.intStream()) : createErrorDataResult(() -> "Not an int list: " + input);
	}

	@Override
	public Data createIntList(IntStream input) {
		return IntListData.collect(input);
	}

	@Override
	public Data createLong(long value) {
		return new NumberData(value);
	}

	@Override
	public DataResult<LongStream> getLongStream(Data input) {
		LongListData list = input.tryAsLongList();
		return list != null ? createSuccessDataResult(list.value.longStream()) : createErrorDataResult(() -> "Not a long list: " + input);
	}

	@Override
	public Data createLongList(LongStream input) {
		return LongListData.collect(input);
	}

	@Override
	public Data createMap(Map<Data, Data> map) {
		return new MapData(map);
	}

	@Override
	public Data createShort(short value) {
		return new NumberData(value);
	}

	@Override
	public DataResult<String> getStringValue(Data data) {
		StringData string = data.tryAsString();
		return string != null ? createSuccessDataResult(string.value) : createErrorDataResult(() -> "Not a string: " + data);
	}

	@Override
	public Data createString(String s) {
		return new StringData(s);
	}

	@Override
	public DataResult<Data> mergeToList(Data data, Data t1) {
		ListData list = data.tryAsList();
		if (list == null) {
			if (data.isEmpty()) return createSuccessDataResult(ListData.singleton(t1));
			return createErrorDataResult(() -> "Not a list: " + data);
		}
		List<Data> newList = new ObjectArrayList<>(list.value.size() + 1);
		newList.addAll(list.value);
		newList.add(t1);
		return createSuccessDataResult(new ListData(newList));
	}

	@Override
	public DataResult<Data> mergeToList(Data data, List<Data> values) {
		ListData list = data.tryAsList();
		if (list == null) {
			if (data.isEmpty()) return createSuccessDataResult(new ListData(values));
			return createErrorDataResult(() -> "Not a list: " + data);
		}
		if (list.isEmpty()) {
			return createSuccessDataResult(new ListData(values));
		}
		List<Data> newList = new ObjectArrayList<>(list.value.size() + values.size());
		newList.addAll(list.value);
		newList.addAll(values);
		return createSuccessDataResult(new ListData(newList));
	}

	@Override
	public DataResult<Data> mergeToMap(Data data, Data t1, Data t2) {
		MapData map = data.tryAsMap();
		if (map == null) {
			if (data.isEmpty()) return createSuccessDataResult(MapData.singleton(t1, t2));
			else return createErrorDataResult(() -> "Not a map: " + data);
		}
		if (map.isEmpty()) {
			return createSuccessDataResult(MapData.singleton(t1, t2));
		}
		Map<Data, Data> newMap = new Object2ObjectLinkedOpenHashMap<>(map.value);
		newMap.put(t1, t2);
		return createSuccessDataResult(new MapData(newMap));
	}

	@Override
	public DataResult<Data> mergeToMap(Data data, MapLike<Data> values) {
		MapData map = data.tryAsMap();
		if (map == null) {
			if (data.isEmpty()) return createSuccessDataResult(MapData.collect(values.entries(), Pair<Data, Data>::getFirst, Pair<Data, Data>::getSecond));
			else return createErrorDataResult(() -> "Not a map: " + data);
		}
		if (map.isEmpty()) {
			return createSuccessDataResult(MapData.collect(values.entries(), Pair<Data, Data>::getFirst, Pair<Data, Data>::getSecond));
		}
		Map<Data, Data> newMap = new Object2ObjectLinkedOpenHashMap<>(map.value);
		values.entries().forEach((Pair<Data, Data> pair) -> newMap.put(pair.getFirst(), pair.getSecond()));
		return createSuccessDataResult(new MapData(newMap));
	}

	@Override
	public DataResult<Data> mergeToMap(Data data, Map<Data, Data> values) {
		MapData map = data.tryAsMap();
		if (map == null) {
			if (data.isEmpty()) return createSuccessDataResult(new MapData(values));
			else return createErrorDataResult(() -> "Not a map: " + data);
		}
		if (map.isEmpty()) {
			return createSuccessDataResult(new MapData(values));
		}
		Map<Data, Data> newMap = new Object2ObjectLinkedOpenHashMap<>(map.size() + values.size());
		newMap.putAll(map.value);
		newMap.putAll(values);
		return createSuccessDataResult(new MapData(newMap));
	}

	@Override
	public DataResult<Stream<Pair<Data, Data>>> getMapValues(Data data) {
		MapData map = data.tryAsMap();
		return map != null ? createSuccessDataResult(map.value.entrySet().stream().map((Map.Entry<Data, Data> entry) -> Pair.of(entry.getKey(), entry.getValue()))) : createErrorDataResult(() -> "Not a map: " + data);
	}

	@Override
	public DataResult<MapLike<Data>> getMap(Data input) {
		MapData map = input.tryAsMap();
		if (map == null) return createErrorDataResult(() -> "Not a map: " + input);
		Map<Data, Data> actualMap = map.value;
		class MapImpl implements MapLike<Data> {

			public Map<Data, Data> getActualMap() {
				return actualMap;
			}

			@Override
			public @Nullable Data get(Data data) {
				return actualMap.get(data);
			}

			@Override
			public @Nullable Data get(String s) {
				return actualMap.get(new StringData(s));
			}

			@Override
			public Stream<Pair<Data, Data>> entries() {
				return actualMap.entrySet().stream().map((Map.Entry<Data, Data> entry) -> Pair.of(entry.getKey(), entry.getValue()));
			}

			@Override
			public boolean equals(Object object) {
				if (object instanceof MapImpl impl) {
					return this.getActualMap().equals(impl.getActualMap());
				}
				else if (object instanceof MapLike<?> mapLike) {
					return this.getActualMap().equals(mapLike.entries().collect(Pair.toMap()));
				}
				else {
					return false;
				}
			}

			@Override
			public int hashCode() {
				return this.getActualMap().hashCode();
			}

			@Override
			public String toString() {
				return this.getActualMap().toString();
			}
		}
		return createSuccessDataResult(new MapImpl());
	}

	@Override
	public Data createMap(Stream<Pair<Data, Data>> stream) {
		return MapData.collect(stream, Pair::getFirst, Pair::getSecond);
	}

	@Override
	public DataResult<Stream<Data>> getStream(Data data) {
		ListData list = data.tryAsList();
		return list != null ? createSuccessDataResult(list.value.stream()) : createErrorDataResult(() -> "Not a list: " + data);
	}

	@Override
	public Data createList(Stream<Data> stream) {
		return ListData.collect(stream);
	}

	@Override
	public DataResult<Data> getGeneric(Data input, Data key) {
		MapData map = input.tryAsMap();
		if (map == null) return createErrorDataResult(() -> "Not a map: " + input);
		Data value = map.get(key);
		if (value.isEmpty()) return createErrorDataResult(() -> "No such key " + key + " in map " + map);
		return createSuccessDataResult(value);
	}

	@Override
	public Data remove(Data data, String s) {
		MapData map = data.tryAsMap();
		if (map == null) return data;
		StringData key = new StringData(s);
		if (!map.value.containsKey(key)) return data;
		Map<Data, Data> newMap = new Object2ObjectLinkedOpenHashMap<>(map.value);
		newMap.remove(key);
		return new MapData(newMap);
	}
}
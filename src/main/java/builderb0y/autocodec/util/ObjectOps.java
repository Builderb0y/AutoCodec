package builderb0y.autocodec.util;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.MapLike;
import org.jetbrains.annotations.NotNull;

/**
a DynamicOps implementation that uses
ordinary java objects to represent data.
note: this class pre-dates {@link JavaOps}.
*/
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ObjectOps implements DynamicOps<Object> {

	public static final @NotNull ObjectOps
		INSTANCE   = new ObjectOps(false),
		COMPRESSED = new ObjectOps(true);

	public final boolean compressed;

	public ObjectOps(boolean compressed) {
		this.compressed = compressed;
	}

	public static <R> DataResult<R> notA(String type, Object object) {
		return DFUVersions.createErrorDataResult(() -> "Not a " + type + ": " + object);
	}

	@Override
	public boolean compressMaps() {
		return this.compressed;
	}

	@Override
	public Object empty() {
		return null;
	}

	@Override
	public <U> U convertTo(DynamicOps<U> outOps, Object input) {
		if (input == null) return outOps.empty();
		if (input instanceof Number number) {
			if (number instanceof Byte    b) return outOps.createByte  (b);
			if (number instanceof Short   s) return outOps.createShort (s);
			if (number instanceof Integer i) return outOps.createInt   (i);
			if (number instanceof Long    l) return outOps.createLong  (l);
			if (number instanceof Float   f) return outOps.createFloat (f);
			if (number instanceof Double  d) return outOps.createDouble(d);
			return outOps.createNumeric(number);
		}
		if (input instanceof String string) return outOps.createString  (string);
		if (input instanceof List<?>  list) return outOps.createList    (list.stream().map(element -> this.convertTo(outOps, element)));
		if (input instanceof Map<?, ?> map) return outOps.createMap     (map.entrySet().stream().map(entry -> Pair.of(this.convertTo(outOps, entry.getKey()), this.convertTo(outOps, entry.getValue()))));
		if (input instanceof Boolean  bool) return outOps.createBoolean (bool.booleanValue());
		if (input instanceof byte[]  bytes) return outOps.createByteList(ByteBuffer.wrap(bytes));
		if (input instanceof int[]    ints) return outOps.createIntList (Arrays.stream(ints));
		if (input instanceof long[]  longs) return outOps.createLongList(Arrays.stream(longs));
		throw new IllegalArgumentException("Not any kind of recognized object: " + input);
	}

	@Override
	public DataResult<Number> getNumberValue(Object input) {
		return input instanceof Number number ? DFUVersions.createSuccessDataResult(number) : notA("Number", input);
	}

	@Override
	public Object createNumeric(Number number) {
		return number;
	}

	@Override
	public DataResult<String> getStringValue(Object input) {
		return input instanceof String string ? DFUVersions.createSuccessDataResult(string) : notA("String", input);
	}

	@Override
	public Object createString(String value) {
		return value;
	}

	@Override
	public DataResult<Object> mergeToList(Object list, Object value) {
		if (list instanceof List actualList) {
			if (value instanceof List otherList) {
				ArrayList newList = new ArrayList(actualList.size() + otherList.size());
				newList.addAll(actualList);
				newList.addAll(otherList);
				return DFUVersions.createSuccessDataResult(newList);
			}
			ArrayList newList = new ArrayList(actualList.size() + 1);
			newList.addAll(actualList);
			newList.add(value);
			return DFUVersions.createSuccessDataResult(newList);
		}
		return notA("List", list);
	}

	@Override
	public DataResult<Object> mergeToMap(Object map, Object key, Object value) {
		if (map instanceof Map actualMap) {
			HashMap newMap = new HashMap(actualMap.size() + 1);
			newMap.putAll(actualMap);
			newMap.put(key, value);
			return DFUVersions.createSuccessDataResult(newMap);
		}
		return notA("Map", map);
	}

	@Override
	public DataResult<Stream<Pair<Object, Object>>> getMapValues(Object input) {
		if (input instanceof Map<?, ?> actualMap) {
			return DFUVersions.createSuccessDataResult(actualMap.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())));
		}
		return notA("Map", input);
	}

	@Override
	public Object createMap(Stream<Pair<Object, Object>> map) {
		return map.collect(Pair.toMap());
	}

	@Override
	public DataResult<Stream<Object>> getStream(Object input) {
		if (input instanceof List list) {
			return DFUVersions.createSuccessDataResult(list.stream());
		}
		if (input.getClass().isArray()) {
			return DFUVersions.createSuccessDataResult(IntStream.range(0, Array.getLength(input)).mapToObj((int index) -> Array.get(input, index)));
		}
		return notA("List", input);
	}

	@Override
	public Object createList(Stream<Object> input) {
		return input.collect(Collectors.toList());
	}

	@Override
	public Object remove(Object input, String key) {
		if (input instanceof Map map && map.containsKey(key)) {
			HashMap newMap = new HashMap(map);
			newMap.remove(key);
			return newMap;
		}
		return input;
	}

	@Override
	public Object emptyMap() {
		return Collections.emptyMap();
	}

	@Override
	public Object emptyList() {
		return Collections.emptyList();
	}

	@Override
	public Number getNumberValue(Object input, Number defaultValue) {
		return input instanceof Number number ? number : defaultValue;
	}

	@Override
	public Object createByte(byte value) {
		return Byte.valueOf(value);
	}

	@Override
	public Object createShort(short value) {
		return Short.valueOf(value);
	}

	@Override
	public Object createInt(int value) {
		return Integer.valueOf(value);
	}

	@Override
	public Object createLong(long value) {
		return Long.valueOf(value);
	}

	@Override
	public Object createFloat(float value) {
		return Float.valueOf(value);
	}

	@Override
	public Object createDouble(double value) {
		return Double.valueOf(value);
	}

	@Override
	public DataResult<Boolean> getBooleanValue(Object input) {
		return input instanceof Boolean bool ? DFUVersions.createSuccessDataResult(bool) : notA("Boolean", input);
	}

	@Override
	public Object createBoolean(boolean value) {
		return Boolean.valueOf(value);
	}

	@Override
	public DataResult<Object> mergeToList(Object list, List<Object> values) {
		if (list instanceof List actualList) {
			ArrayList newList = new ArrayList(actualList.size() + values.size());
			newList.addAll(actualList);
			newList.addAll(values);
			return DFUVersions.createSuccessDataResult(newList);
		}
		return notA("List", list);
	}

	@Override
	public DataResult<Object> mergeToMap(Object map, Map<Object, Object> values) {
		if (map instanceof Map actualMap) {
			HashMap newMap = new HashMap(actualMap.size() + values.size());
			newMap.putAll(actualMap);
			newMap.putAll(values);
			return DFUVersions.createSuccessDataResult(newMap);
		}
		return notA("Map", map);
	}

	@Override
	public DataResult<Object> mergeToMap(Object map, MapLike<Object> values) {
		if (map instanceof Map actualMap) {
			HashMap newMap = new HashMap(actualMap);
			values.entries().forEach((Pair pair) -> newMap.put(pair.getFirst(), pair.getSecond()));
			return DFUVersions.createSuccessDataResult(newMap);
		}
		return notA("Map", map);
	}

	@Override
	public DataResult<MapLike<Object>> getMap(Object input) {
		if (input instanceof Map actualMap) {
			return DFUVersions.createSuccessDataResult(MapLike.forMap(actualMap, this));
		}
		return notA("Map", input);
	}

	@Override
	public Object createMap(Map<Object, Object> map) {
		return map;
	}

	@Override
	public DataResult<ByteBuffer> getByteBuffer(Object input) {
		if (input instanceof byte[] bytes) {
			return DFUVersions.createSuccessDataResult(ByteBuffer.wrap(bytes));
		}
		return notA("byte[]", input);
	}

	@Override
	public Object createByteList(ByteBuffer input) {
		byte[] bytes = new byte[input.remaining()];
		input.get(bytes);
		return bytes;
	}

	@Override
	public DataResult<IntStream> getIntStream(Object input) {
		if (input instanceof int[] ints) {
			return DFUVersions.createSuccessDataResult(Arrays.stream(ints));
		}
		return notA("int[]", input);
	}

	@Override
	public Object createIntList(IntStream input) {
		return input.toArray();
	}

	@Override
	public DataResult<LongStream> getLongStream(Object input) {
		if (input instanceof long[] longs) {
			return DFUVersions.createSuccessDataResult(Arrays.stream(longs));
		}
		return notA("long[]", input);
	}

	@Override
	public Object createLongList(LongStream input) {
		return input.toArray();
	}

	@Override
	public DataResult<Object> get(Object input, String key) {
		return this.getGeneric(input, key);
	}

	@Override
	public DataResult<Object> getGeneric(Object input, Object key) {
		if (input instanceof Map actualMap) {
			Object value = actualMap.get(key);
			if (value != null) {
				return DFUVersions.createSuccessDataResult(value);
			}
			return DFUVersions.createErrorDataResult(() -> "No element " + key + " in the map " + actualMap);
		}
		return notA("Map", input);
	}

	@Override
	public Object set(Object input, String key, Object value) {
		if (input instanceof Map actualMap) {
			HashMap newMap = new HashMap(actualMap);
			newMap.put(key, value);
			return newMap;
		}
		return input;
	}

	@Override
	public Object update(Object input, String key, Function<Object, Object> function) {
		return this.updateGeneric(input, key, function);
	}

	@Override
	public Object updateGeneric(Object input, Object key, Function<Object, Object> function) {
		if (input instanceof Map actualMap) {
			HashMap newMap = new HashMap(actualMap);
			newMap.put(key, function.apply(newMap.get(key)));
			return newMap;
		}
		return input;
	}

	@Override
	public String toString() {
		return this.compressed ? "ObjectOps (compressed)" : "ObjectOps (uncompressed)";
	}
}
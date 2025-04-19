package builderb0y.autocodec.data;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.util.AutoCodecUtil;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class MapData extends Data {

	public static final @NotNull ObjectArrayFactory<MapData> ARRAY_FACTORY = new ObjectArrayFactory<>(MapData.class);

	public @NotNull Map<@NotNull Data, @NotNull Data> value;

	public MapData() {
		this.value = new Object2ObjectLinkedOpenHashMap<>();
	}

	public MapData(int expected) {
		this.value = new Object2ObjectLinkedOpenHashMap<>(expected);
	}

	public MapData(@NotNull Map<@NotNull Data, @NotNull Data> value) {
		this.value = value;
	}

	public static @NotNull MapData singleton(@NotNull String key, @NotNull Data value) {
		return singleton(new StringData(key), value);
	}

	public static @NotNull MapData singleton(@NotNull Data key, @NotNull Data value) {
		MapData map = new MapData(4);
		map.put(key, value);
		return map;
	}

	public static <T> @NotNull MapData collect(
		@NotNull Stream<? extends T> stream,
		@NotNull Function<? super T, ? extends @NotNull Data> keyExtractor,
		@NotNull Function<? super T, ? extends @NotNull Data> valueExtractor
	) {
		return new MapData(stream.collect(AutoCodecUtil.collectToMap(keyExtractor, valueExtractor, Object2ObjectLinkedOpenHashMap::new)));
	}

	public @NotNull Stream<Map.@NotNull Entry<@NotNull Data, @NotNull Data>> streamNonEmpty() {
		return this.value.entrySet().stream().filter((Map.Entry<Data, Data> entry) -> {
			return entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null && !entry.getValue().isEmpty();
		});
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.createMap(this.streamNonEmpty().map((Map.Entry<Data, Data> entry) -> Pair.of(entry.getKey().convert(ops), entry.getValue().convert(ops))));
	}

	public int size() {
		return this.value.size();
	}

	public @NotNull Data get(String key) {
		return this.get(new StringData(key));
	}

	public @NotNull Data get(@NotNull Data key) {
		Data data = this.value.get(key);
		if (data == null) data = EmptyData.INSTANCE;
		return data;
	}

	public @NotNull Data putBoolean(@NotNull String key, boolean value) {
		return this.put(key, new BooleanData(value));
	}

	public @NotNull Data putByte(@NotNull String key, byte value) {
		return this.put(key, new NumberData(value));
	}

	public @NotNull Data putShort(@NotNull String key, short value) {
		return this.put(key, new NumberData(value));
	}

	public @NotNull Data putInt(@NotNull String key, int value) {
		return this.put(key, new NumberData(value));
	}

	public @NotNull Data putLong(@NotNull String key, long value) {
		return this.put(key, new NumberData(value));
	}

	public @NotNull Data putFloat(@NotNull String key, float value) {
		return this.put(key, new NumberData(value));
	}

	public @NotNull Data putDouble(@NotNull String key, double value) {
		return this.put(key, new NumberData(value));
	}

	public @NotNull Data putString(@NotNull String key, @NotNull String value) {
		return this.put(key, new StringData(value));
	}

	public @NotNull Data putByteList(@NotNull String key, byte @NotNull ... value) {
		return this.put(key, new ByteListData(ByteArrayList.wrap(value)));
	}

	public @NotNull Data putByteList(@NotNull String key, @NotNull ByteList value) {
		return this.put(key, new ByteListData(value));
	}

	public @NotNull Data putIntList(@NotNull String key, int @NotNull ... value) {
		return this.put(key, new IntListData(IntArrayList.wrap(value)));
	}

	public @NotNull Data putIntList(@NotNull String key, @NotNull IntList value) {
		return this.put(key, new IntListData(value));
	}

	public @NotNull Data putLongList(@NotNull String key, long @NotNull ... value) {
		return this.put(key, new LongListData(LongArrayList.wrap(value)));
	}

	public @NotNull Data putLongList(@NotNull String key, @NotNull LongList value) {
		return this.put(key, new LongListData(value));
	}

	public @NotNull Data putList(@NotNull String key, @NotNull Data @NotNull ... value) {
		return this.put(key, new ListData(ObjectArrayList.wrap(value)));
	}

	public @NotNull Data putList(@NotNull String key, @NotNull List<@NotNull Data> value) {
		return this.put(key, new ListData(value));
	}

	public @NotNull Data putMap(@NotNull String key, @NotNull Map<@NotNull Data, @NotNull Data> value) {
		return this.put(key, new MapData(value));
	}

	public @NotNull Data put(@NotNull String key, @NotNull Data value) {
		return this.put(new StringData(key), value);
	}

	public @NotNull Data put(@NotNull Data key, @NotNull Data value) {
		Data old = this.value.put(key, value);
		if (old == null) old = EmptyData.INSTANCE;
		return old;
	}

	public @NotNull Data remove(@NotNull String key) {
		return this.remove(new StringData(key));
	}

	public @NotNull Data remove(@NotNull Data key) {
		Data removed = this.value.remove(key);
		if (removed == null) removed = EmptyData.INSTANCE;
		return removed;
	}

	@Override
	public boolean equals(Object object) {
		MapData map;
		return object instanceof Data data && (map = data.tryAsMap()) != null && this.value.equals(map.value);
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public String toString() {
		return this.value.toString();
	}

	@Override
	public @NotNull MapData deepCopy() {
		return new MapData(this.value.entrySet().stream().collect(AutoCodecUtil.collectToMap((Map.Entry<Data, Data> entry) -> entry.getKey().deepCopy(), (Map.Entry<Data, Data> entry) -> entry.getValue().deepCopy(), Object2ObjectLinkedOpenHashMap::new)));
	}
}
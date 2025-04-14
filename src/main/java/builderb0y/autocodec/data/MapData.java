package builderb0y.autocodec.data;

import java.util.Map;
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
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.util.AutoCodecUtil;

public class MapData<T_Encoded> extends Data<T_Encoded> {

	public @NotNull Map<@NotNull Data<T_Encoded>, @NotNull Data<T_Encoded>> value;

	public MapData(@NotNull DynamicOps<T_Encoded> ops) {
		super(ops);
		this.value = new Object2ObjectLinkedOpenHashMap<>();
	}

	public MapData(@NotNull DynamicOps<T_Encoded> ops, int expected) {
		super(ops);
		this.value = new Object2ObjectLinkedOpenHashMap<>(expected);
	}

	public MapData(@NotNull DynamicOps<T_Encoded> ops, @NotNull Map<@NotNull Data<T_Encoded>, @NotNull Data<T_Encoded>> value) {
		super(ops);
		this.value = value;
	}

	public @NotNull Stream<Map.@NotNull Entry<@NotNull Data<T_Encoded>, @NotNull Data<T_Encoded>>> streamNonEmpty() {
		return this.value.entrySet().stream().filter((Map.Entry<Data<T_Encoded>, Data<T_Encoded>> entry) -> {
			return entry.getValue() != null && !entry.getValue().isEmpty();
		});
	}

	@Override
	public @NotNull T_Encoded encode() {
		return this.ops.createMap(this.streamNonEmpty().map((Map.Entry<Data<T_Encoded>, Data<T_Encoded>> entry) -> Pair.of(entry.getKey().encode(), entry.getValue().encode())));
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.createMap(this.streamNonEmpty().map((Map.Entry<Data<T_Encoded>, Data<T_Encoded>> entry) -> Pair.of(entry.getKey().convert(ops), entry.getValue().convert(ops))));
	}

	public @NotNull Data<T_Encoded> get(String key) {
		return this.get(new StringData<>(this.ops, key));
	}

	public @NotNull Data<T_Encoded> get(@NotNull Data<T_Encoded> key) {
		Data<T_Encoded> data = this.value.get(key);
		if (data == null) data = EmptyData.forOps(this.ops);
		return data;
	}

	public @NotNull Data<T_Encoded> putBoolean(@NotNull String key, boolean value) {
		return this.put(key, new BooleanData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> putByte(@NotNull String key, byte value) {
		return this.put(key, new NumberData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> putShort(@NotNull String key, short value) {
		return this.put(key, new NumberData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> putInt(@NotNull String key, int value) {
		return this.put(key, new NumberData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> putLong(@NotNull String key, long value) {
		return this.put(key, new NumberData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> putFloat(@NotNull String key, float value) {
		return this.put(key, new NumberData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> putDouble(@NotNull String key, double value) {
		return this.put(key, new NumberData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> putString(@NotNull String key, @NotNull String value) {
		return this.put(key, new StringData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> putByteList(@NotNull String key, byte @NotNull ... value) {
		return this.put(key, new ByteListData<>(this.ops, ByteArrayList.wrap(value)));
	}

	public @NotNull Data<T_Encoded> putByteList(@NotNull String key, @NotNull ByteList value) {
		return this.put(key, new ByteListData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> putIntList(@NotNull String key, int @NotNull ... value) {
		return this.put(key, new IntListData<>(this.ops, IntArrayList.wrap(value)));
	}

	public @NotNull Data<T_Encoded> putIntList(@NotNull String key, @NotNull IntList value) {
		return this.put(key, new IntListData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> putLongList(@NotNull String key, long @NotNull ... value) {
		return this.put(key, new LongListData<>(this.ops, LongArrayList.wrap(value)));
	}

	public @NotNull Data<T_Encoded> putLongList(@NotNull String key, @NotNull LongList value) {
		return this.put(key, new LongListData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> putList(@NotNull String key, @NotNull Data<T_Encoded> @NotNull ... value) {
		return this.put(key, new ListData<>(this.ops, ObjectArrayList.wrap(value)));
	}

	public @NotNull Data<T_Encoded> putList(@NotNull String key, @NotNull ObjectList<@NotNull Data<T_Encoded>> value) {
		return this.put(key, new ListData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> putMap(@NotNull String key, @NotNull Object2ObjectMap<@NotNull Data<T_Encoded>, @NotNull Data<T_Encoded>> value) {
		return this.put(key, new MapData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> put(@NotNull String key, @NotNull Data<T_Encoded> value) {
		return this.put(new StringData<>(this.ops, key), value);
	}

	public @NotNull Data<T_Encoded> put(@NotNull Data<T_Encoded> key, @NotNull Data<T_Encoded> value) {
		Data<T_Encoded> old = this.value.put(key, value);
		if (old == null) old = EmptyData.forOps(this.ops);
		return old;
	}

	public @NotNull Data<T_Encoded> remove(@NotNull String key) {
		return this.remove(new StringData<>(this.ops, key));
	}

	public @NotNull Data<T_Encoded> remove(@NotNull Data<T_Encoded> key) {
		Data<T_Encoded> removed = this.value.remove(key);
		if (removed == null) removed = EmptyData.forOps(this.ops);
		return removed;
	}

	@Override
	public boolean equals(Object object) {
		MapData<?> map;
		return object instanceof Data<?> data && (map = data.tryAsMap()) != null && this.value.equals(map.value);
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
	public @NotNull Data<T_Encoded> deepCopy() {
		return new MapData<>(this.ops, this.value.entrySet().stream().collect(AutoCodecUtil.collectToMap((Map.Entry<Data<T_Encoded>, Data<T_Encoded>> entry) -> entry.getKey().deepCopy(), (Map.Entry<Data<T_Encoded>, Data<T_Encoded>> entry) -> entry.getValue().deepCopy(), Object2ObjectLinkedOpenHashMap::new)));
	}
}
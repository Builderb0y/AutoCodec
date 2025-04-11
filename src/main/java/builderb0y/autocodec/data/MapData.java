package builderb0y.autocodec.data;

import java.util.Map;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
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

	@Override
	public boolean isMap() {
		return true;
	}

	@Override
	public @NotNull Map<@NotNull Data<T_Encoded>, @NotNull Data<T_Encoded>> tryAsMap() {
		return this.value;
	}

	public Data<T_Encoded> get(String key) {
		return this.get(new StringData<>(this.ops, key));
	}

	public Data<T_Encoded> get(Data<T_Encoded> key) {
		return this.value.get(key);
	}

	public void put(String key, boolean value) {
		this.put(new StringData<>(this.ops, key), new BooleanData<>(this.ops, value));
	}

	public void put(String key, byte value) {
		this.put(new StringData<>(this.ops, key), new NumberData<>(this.ops, value));
	}

	public void put(String key, short value) {
		this.put(new StringData<>(this.ops, key), new NumberData<>(this.ops, value));
	}

	public void put(String key, int value) {
		this.put(new StringData<>(this.ops, key), new NumberData<>(this.ops, value));
	}

	public void put(String key, long value) {
		this.put(new StringData<>(this.ops, key), new NumberData<>(this.ops, value));
	}

	public void put(String key, float value) {
		this.put(new StringData<>(this.ops, key), new NumberData<>(this.ops, value));
	}

	public void put(String key, double value) {
		this.put(new StringData<>(this.ops, key), new NumberData<>(this.ops, value));
	}

	public void put(String key, String value) {
		this.put(new StringData<>(this.ops, key), new StringData<>(this.ops, value));
	}

	public void put(String key, Data<T_Encoded> value) {
		this.put(new StringData<>(this.ops, key), value);
	}

	public void put(Data<T_Encoded> key, Data<T_Encoded> value) {
		this.value.put(key, value);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof MapData<?> that && this.value.equals(that.value);
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
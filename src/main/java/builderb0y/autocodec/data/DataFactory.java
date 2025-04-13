package builderb0y.autocodec.data;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

public interface DataFactory<T_Encoded> {

	public static <T_Encoded> @NotNull DataFactory<T_Encoded> forOps(@NotNull DynamicOps<T_Encoded> ops) {
		return () -> ops;
	}

	public abstract @NotNull DynamicOps<T_Encoded> ops();

	public default @NotNull EmptyData<T_Encoded> empty() {
		return EmptyData.forOps(this.ops());
	}

	public default @NotNull BooleanData<T_Encoded> createBoolean(boolean value) {
		return new BooleanData<>(this.ops(), value);
	}

	public default @NotNull UnknownNumberData<T_Encoded> createNumber(@NotNull Number value) {
		return new UnknownNumberData<>(this.ops(), value);
	}

	public default @NotNull NumberData<T_Encoded> createByte(byte value) {
		return new NumberData<>(this.ops(), value);
	}

	public default @NotNull NumberData<T_Encoded> createShort(short value) {
		return new NumberData<>(this.ops(), value);
	}

	public default @NotNull NumberData<T_Encoded> createInt(int value) {
		return new NumberData<>(this.ops(), value);
	}

	public default @NotNull NumberData<T_Encoded> createLong(long value) {
		return new NumberData<>(this.ops(), value);
	}

	public default @NotNull NumberData<T_Encoded> createFloat(float value) {
		return new NumberData<>(this.ops(), value);
	}

	public default @NotNull NumberData<T_Encoded> createDouble(double value) {
		return new NumberData<>(this.ops(), value);
	}

	public default @NotNull StringData<T_Encoded> createString(@NotNull String value) {
		return new StringData<>(this.ops(), value);
	}

	public default @NotNull ByteListData<T_Encoded> createByteList(@NotNull ByteList value) {
		return new ByteListData<>(this.ops(), value);
	}

	public default @NotNull ByteListData<T_Encoded> createByteList(byte @NotNull ... value) {
		return this.createByteList(ByteArrayList.wrap(value));
	}

	public default @NotNull IntListData<T_Encoded> createIntList(@NotNull IntList value) {
		return new IntListData<>(this.ops(), value);
	}

	public default @NotNull IntListData<T_Encoded> createIntList(int @NotNull ... value) {
		return this.createIntList(IntArrayList.wrap(value));
	}

	public default @NotNull LongListData<T_Encoded> createLongList(@NotNull LongList value) {
		return new LongListData<>(this.ops(), value);
	}

	public default @NotNull LongListData<T_Encoded> createLongList(long @NotNull ... value) {
		return this.createLongList(LongArrayList.wrap(value));
	}

	public default @NotNull ListData<T_Encoded> emptyList() {
		return new ListData<>(this.ops());
	}

	public default @NotNull ListData<T_Encoded> createList(@NotNull List<@NotNull Data<T_Encoded>> values) {
		return new ListData<>(this.ops(), values);
	}

	public default @NotNull ListData<T_Encoded> createList(@NotNull Stream<@NotNull Data<T_Encoded>> stream) {
		return new ListData<>(this.ops(), stream.collect(Collectors.toCollection(ObjectArrayList::new)));
	}

	public default @NotNull ListData<T_Encoded> createList(@NotNull Data<T_Encoded> @NotNull ... values) {
		return new ListData<>(this.ops(), ObjectArrayList.wrap(values));
	}

	public default @NotNull MapData<T_Encoded> emptyMap() {
		return new MapData<>(this.ops());
	}

	public default @NotNull MapData<T_Encoded> createMap(@NotNull Map<@NotNull Data<T_Encoded>, @NotNull Data<T_Encoded>> map) {
		return new MapData<>(this.ops(), map);
	}

	public default <T> @NotNull MapData<T_Encoded> createMap(
		@NotNull Stream<? extends T> stream,
		@NotNull Function<? super T, ? extends @NotNull Data<T_Encoded>> keyExtractor,
		@NotNull Function<? super T, ? extends @NotNull Data<T_Encoded>> valueExtractor
	) {
		return this.createMap(stream.collect(AutoCodecUtil.collectToMap(keyExtractor, valueExtractor, Object2ObjectLinkedOpenHashMap::new)));
	}

	public default @NotNull UnknownData<T_Encoded> createUnknown(@NotNull T_Encoded payload) {
		return new UnknownData<>(this.ops(), payload);
	}
}
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

public interface DataFactory {

	public default @NotNull EmptyData empty() {
		return EmptyData.INSTANCE;
	}

	public default @NotNull BooleanData createBoolean(boolean value) {
		return new BooleanData(value);
	}

	public default @NotNull UnknownNumberData createNumber(@NotNull Number value) {
		return new UnknownNumberData(value);
	}

	public default @NotNull NumberData createByte(byte value) {
		return new NumberData(value);
	}

	public default @NotNull NumberData createShort(short value) {
		return new NumberData(value);
	}

	public default @NotNull NumberData createInt(int value) {
		return new NumberData(value);
	}

	public default @NotNull NumberData createLong(long value) {
		return new NumberData(value);
	}

	public default @NotNull NumberData createFloat(float value) {
		return new NumberData(value);
	}

	public default @NotNull NumberData createDouble(double value) {
		return new NumberData(value);
	}

	public default @NotNull StringData createString(@NotNull String value) {
		return new StringData(value);
	}

	public default @NotNull ByteListData createByteList(@NotNull ByteList value) {
		return new ByteListData(value);
	}

	public default @NotNull ByteListData createByteList(byte @NotNull ... value) {
		return this.createByteList(ByteArrayList.wrap(value));
	}

	public default @NotNull IntListData createIntList(@NotNull IntList value) {
		return new IntListData(value);
	}

	public default @NotNull IntListData createIntList(int @NotNull ... value) {
		return this.createIntList(IntArrayList.wrap(value));
	}

	public default @NotNull LongListData createLongList(@NotNull LongList value) {
		return new LongListData(value);
	}

	public default @NotNull LongListData createLongList(long @NotNull ... value) {
		return this.createLongList(LongArrayList.wrap(value));
	}

	public default @NotNull ListData emptyList() {
		return new ListData();
	}

	public default @NotNull ListData createList(@NotNull List<@NotNull Data> values) {
		return new ListData(values);
	}

	public default @NotNull ListData createList(@NotNull Stream<@NotNull Data> stream) {
		return new ListData(stream.collect(Collectors.toCollection(ObjectArrayList::new)));
	}

	public default @NotNull ListData createList(@NotNull Data @NotNull ... values) {
		return new ListData(ObjectArrayList.wrap(values));
	}

	public default @NotNull MapData emptyMap() {
		return new MapData();
	}

	public default @NotNull MapData createMap(@NotNull Map<@NotNull Data, @NotNull Data> map) {
		return new MapData(map);
	}

	public default <T> @NotNull MapData createMap(
		@NotNull Stream<? extends T> stream,
		@NotNull Function<? super T, ? extends @NotNull Data> keyExtractor,
		@NotNull Function<? super T, ? extends @NotNull Data> valueExtractor
	) {
		return this.createMap(stream.collect(AutoCodecUtil.collectToMap(keyExtractor, valueExtractor, Object2ObjectLinkedOpenHashMap::new)));
	}

	public default <T_Encoded> @NotNull UnknownData<T_Encoded> createUnknown(@NotNull DynamicOps<T_Encoded> ops, @NotNull T_Encoded payload) {
		return new UnknownData<>(ops, payload);
	}
}
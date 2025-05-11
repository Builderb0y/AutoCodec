package builderb0y.autocodec.data;

import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.jetbrains.annotations.NotNull;

public interface DataFactory {

	public default @NotNull EmptyData empty() {
		return EmptyData.INSTANCE;
	}

	public default @NotNull BooleanData createBoolean(boolean value) {
		return new BooleanData(value);
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

	public default @NotNull UnknownNumberData createNumber(@NotNull Number value) {
		return new UnknownNumberData(value);
	}

	public default @NotNull StringData createString(@NotNull String value) {
		return new StringData(value);
	}

	public default @NotNull ByteListData createByteList(byte @NotNull ... value) {
		return ByteListData.wrap(value);
	}

	public default @NotNull ByteListData createByteList(@NotNull ByteList value) {
		return new ByteListData(value);
	}

	public default @NotNull IntListData createIntList(int @NotNull ... value) {
		return IntListData.wrap(value);
	}

	public default @NotNull IntListData createIntList(@NotNull IntList value) {
		return new IntListData(value);
	}

	public default @NotNull LongListData createLongList(long @NotNull ... value) {
		return LongListData.wrap(value);
	}

	public default @NotNull LongListData createLongList(@NotNull LongList value) {
		return new LongListData(value);
	}

	public default @NotNull ListData createList(@NotNull Data @NotNull ... value) {
		return ListData.wrap(value);
	}

	public default @NotNull ListData createList(@NotNull List<@NotNull Data> value) {
		return new ListData(value);
	}

	public default @NotNull ListData createSingletonList(@NotNull Data contents) {
		return ListData.singleton(contents);
	}

	public default @NotNull MapData createSingletonMap(@NotNull String key, @NotNull Data value) {
		return MapData.singleton(key, value);
	}

	public default @NotNull MapData createSingletonMap(@NotNull Data key, @NotNull Data value) {
		return MapData.singleton(key, value);
	}

	public default @NotNull MapData createMap(@NotNull Map<@NotNull Data, @NotNull Data> value) {
		return new MapData(value);
	}
}
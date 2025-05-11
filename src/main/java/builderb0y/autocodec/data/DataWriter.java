package builderb0y.autocodec.data;

import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;

public interface DataWriter<T_Exception extends Exception, T_Writer extends DataWriter<T_Exception, T_Writer>> extends DataReader<T_Exception, T_Writer> {

	public default void setBooleanValue(boolean value) throws T_Exception {
		this.forceAsBoolean().value = value;
	}

	public default void setByteValue(byte value) throws T_Exception {
		this.forceAsNumber().set(value);
	}

	public default void setShortValue(short value) throws T_Exception {
		this.forceAsNumber().set(value);
	}

	public default void setIntValue(int value) throws T_Exception {
		this.forceAsNumber().set(value);
	}

	public default void setLongValue(long value) throws T_Exception {
		this.forceAsNumber().set(value);
	}

	public default void setFloatValue(float value) throws T_Exception {
		this.forceAsNumber().set(value);
	}

	public default void setDoubleValue(double value) throws T_Exception {
		this.forceAsNumber().set(value);
	}

	public default void setStringValue(@NotNull String value) throws T_Exception {
		this.forceAsString().value = value;
	}

	public default void appendByte(byte value) throws T_Exception {
		this.forceAsByteList().value.add(value);
	}

	public default void appendInt(int value) throws T_Exception {
		this.forceAsIntList().value.add(value);
	}

	public default void appendLong(long value) throws T_Exception {
		this.forceAsLongList().value.add(value);
	}

	public default void appendBooleanElement(boolean value) throws T_Exception {
		this.appendDataElement(new BooleanData(value));
	}

	public default void appendByteElement(byte value) throws T_Exception {
		this.appendDataElement(new NumberData(value));
	}

	public default void appendShortElement(short value) throws T_Exception {
		this.appendDataElement(new NumberData(value));
	}

	public default void appendIntElement(int value) throws T_Exception {
		this.appendDataElement(new NumberData(value));
	}

	public default void appendLongElement(long value) throws T_Exception {
		this.appendDataElement(new NumberData(value));
	}

	public default void appendFloatElement(float value) throws T_Exception {
		this.appendDataElement(new NumberData(value));
	}

	public default void appendDoubleElement(double value) throws T_Exception {
		this.appendDataElement(new NumberData(value));
	}

	public default void appendStringElement(@NotNull String value) throws T_Exception {
		this.appendDataElement(new StringData(value));
	}

	public default void appendByteListElement(byte @NotNull ... value) throws T_Exception {
		this.appendDataElement(ByteListData.wrap(value));
	}

	public default void appendByteListElement(@NotNull ByteList value) throws T_Exception {
		this.appendDataElement(new ByteListData(value));
	}

	public default void appendIntListElement(int @NotNull ... value) throws T_Exception {
		this.appendDataElement(IntListData.wrap(value));
	}

	public default void appendIntListElement(@NotNull IntList value) throws T_Exception {
		this.appendDataElement(new IntListData(value));
	}

	public default void appendLongListElement(long @NotNull ... value) throws T_Exception {
		this.appendDataElement(LongListData.wrap(value));
	}

	public default void appendLongListElement(@NotNull LongList value) throws T_Exception {
		this.appendDataElement(new LongListData(value));
	}

	public default void appendListElement(@NotNull Data @NotNull ... value) throws T_Exception {
		this.appendDataElement(ListData.wrap(value));
	}

	public default void appendListElement(@NotNull ObjectList<@NotNull Data> value) throws T_Exception {
		this.appendDataElement(new ListData(value));
	}

	public default void appendMapElement(@NotNull Object2ObjectMap<@NotNull Data, @NotNull Data> value) throws T_Exception {
		this.appendDataElement(new MapData(value));
	}

	public default void appendDataElement(@NotNull Data value) throws T_Exception {
		this.forceAsList().value.add(value);
	}

	public default byte setByte(int index, byte value) throws T_Exception {
		return this.forceAsByteList().value.set(index, value);
	}

	public default int setInt(int index, int value) throws T_Exception {
		return this.forceAsIntList().value.set(index, value);
	}

	public default long setLong(int index, long value) throws T_Exception {
		return this.forceAsLongList().value.set(index, value);
	}

	public default @NotNull Data setBooleanElement(int index, boolean value) throws T_Exception {
		return this.setDataElement(index, new BooleanData(value));
	}

	public default @NotNull Data setByteElement(int index, byte value) throws T_Exception {
		return this.setDataElement(index, new NumberData(value));
	}

	public default @NotNull Data setShortElement(int index, short value) throws T_Exception {
		return this.setDataElement(index, new NumberData(value));
	}

	public default @NotNull Data setIntElement(int index, int value) throws T_Exception {
		return this.setDataElement(index, new NumberData(value));
	}

	public default @NotNull Data setLongElement(int index, long value) throws T_Exception {
		return this.setDataElement(index, new NumberData(value));
	}

	public default @NotNull Data setFloatElement(int index, float value) throws T_Exception {
		return this.setDataElement(index, new NumberData(value));
	}

	public default @NotNull Data setDoubleElement(int index, double value) throws T_Exception {
		return this.setDataElement(index, new NumberData(value));
	}

	public default @NotNull Data setStringElement(int index, @NotNull String value) throws T_Exception {
		return this.setDataElement(index, new StringData(value));
	}

	public default @NotNull Data setByteListElement(int index, byte @NotNull ... value) throws T_Exception {
		return this.setDataElement(index, ByteListData.wrap(value));
	}

	public default @NotNull Data setByteListElement(int index, @NotNull ByteList value) throws T_Exception {
		return this.setDataElement(index, new ByteListData(value));
	}

	public default @NotNull Data setIntListElement(int index, int @NotNull ... value) throws T_Exception {
		return this.setDataElement(index, IntListData.wrap(value));
	}

	public default @NotNull Data setIntListElement(int index, @NotNull IntList value) throws T_Exception {
		return this.setDataElement(index, new IntListData(value));
	}

	public default @NotNull Data setLongListElement(int index, long @NotNull ... value) throws T_Exception {
		return this.setDataElement(index, LongListData.wrap(value));
	}

	public default @NotNull Data setLongListElement(int index, @NotNull LongList value) throws T_Exception {
		return this.setDataElement(index, new LongListData(value));
	}

	public default @NotNull Data setListElement(int index, @NotNull Data @NotNull ... value) throws T_Exception {
		return this.setDataElement(index, ListData.wrap(value));
	}

	public default @NotNull Data setListElement(int index, @NotNull ObjectList<@NotNull Data> value) throws T_Exception {
		return this.setDataElement(index, new ListData(value));
	}

	public default @NotNull Data setMapElement(int index, @NotNull Object2ObjectMap<@NotNull Data, @NotNull Data> value) throws T_Exception {
		return this.setDataElement(index, new MapData(value));
	}

	public default @NotNull Data setDataElement(int index, @NotNull Data value) throws T_Exception {
		return this.forceAsList().set(index, value);
	}

	public default byte removeByte(int index) throws T_Exception {
		return this.forceAsByteList().value.removeByte(index);
	}

	public default int removeInt(int index) throws T_Exception {
		return this.forceAsIntList().value.removeInt(index);
	}

	public default long removeLong(int index) throws T_Exception {
		return this.forceAsLongList().value.removeLong(index);
	}

	public default Data removeElement(int index) throws T_Exception {
		return this.forceAsList().value.remove(index);
	}

	public default @NotNull Data putBoolean(@NotNull String key, boolean value) throws T_Exception {
		return this.putMember(key, new BooleanData(value));
	}

	public default @NotNull Data putByte(@NotNull String key, byte value) throws T_Exception {
		return this.putMember(key, new NumberData(value));
	}

	public default @NotNull Data putShort(@NotNull String key, short value) throws T_Exception {
		return this.putMember(key, new NumberData(value));
	}

	public default @NotNull Data putInt(@NotNull String key, int value) throws T_Exception {
		return this.putMember(key, new NumberData(value));
	}

	public default @NotNull Data putLong(@NotNull String key, long value) throws T_Exception {
		return this.putMember(key, new NumberData(value));
	}

	public default @NotNull Data putFloat(@NotNull String key, float value) throws T_Exception {
		return this.putMember(key, new NumberData(value));
	}

	public default @NotNull Data putDouble(@NotNull String key, double value) throws T_Exception {
		return this.putMember(key, new NumberData(value));
	}

	public default @NotNull Data putString(@NotNull String key, @NotNull String value) throws T_Exception {
		return this.putMember(key, new StringData(value));
	}

	public default @NotNull Data putByteList(@NotNull String key, byte @NotNull ... value) throws T_Exception {
		return this.putMember(key, ByteListData.wrap(value));
	}

	public default @NotNull Data putByteList(@NotNull String key, @NotNull ByteList value) throws T_Exception {
		return this.putMember(key, new ByteListData(value));
	}

	public default @NotNull Data putIntList(@NotNull String key, int @NotNull ... value) throws T_Exception {
		return this.putMember(key, IntListData.wrap(value));
	}

	public default @NotNull Data putIntList(@NotNull String key, @NotNull IntList value) throws T_Exception {
		return this.putMember(key, new IntListData(value));
	}

	public default @NotNull Data putLongList(@NotNull String key, long @NotNull ... value) throws T_Exception {
		return this.putMember(key, LongListData.wrap(value));
	}

	public default @NotNull Data putLongList(@NotNull String key, @NotNull LongList value) throws T_Exception {
		return this.putMember(key, new LongListData(value));
	}

	public default @NotNull Data putList(@NotNull String key, @NotNull Data @NotNull ... value) throws T_Exception {
		return this.putMember(key, ListData.wrap(value));
	}

	public default @NotNull Data putList(@NotNull String key, @NotNull List<@NotNull Data> value) throws T_Exception {
		return this.putMember(key, new ListData(value));
	}

	public default @NotNull Data putMap(@NotNull String key, @NotNull Map<@NotNull Data, @NotNull Data> value) throws T_Exception {
		return this.putMember(key, new MapData(value));
	}

	public default @NotNull Data putMember(@NotNull String key, @NotNull Data value) throws T_Exception {
		return this.forceAsMap().put(key, value);
	}

	public default @NotNull Data removeMember(@NotNull String key) throws T_Exception {
		return this.forceAsMap().remove(key);
	}

	public default @NotNull T_Writer withBoolean(@NotNull String key, boolean value) throws T_Exception {
		return this.withMember(key, new BooleanData(value));
	}

	public default @NotNull T_Writer withByte(@NotNull String key, byte value) throws T_Exception {
		return this.withMember(key, new NumberData(value));
	}

	public default @NotNull T_Writer withShort(@NotNull String key, short value) throws T_Exception {
		return this.withMember(key, new NumberData(value));
	}

	public default @NotNull T_Writer withInt(@NotNull String key, int value) throws T_Exception {
		return this.withMember(key, new NumberData(value));
	}

	public default @NotNull T_Writer withLong(@NotNull String key, long value) throws T_Exception {
		return this.withMember(key, new NumberData(value));
	}

	public default @NotNull T_Writer withFloat(@NotNull String key, float value) throws T_Exception {
		return this.withMember(key, new NumberData(value));
	}

	public default @NotNull T_Writer withDouble(@NotNull String key, double value) throws T_Exception {
		return this.withMember(key, new NumberData(value));
	}

	public default @NotNull T_Writer withString(@NotNull String key, @NotNull String value) throws T_Exception {
		return this.withMember(key, new StringData(value));
	}

	public default @NotNull T_Writer withByteList(@NotNull String key, byte @NotNull ... value) throws T_Exception {
		return this.withMember(key, ByteListData.wrap(value));
	}

	public default @NotNull T_Writer withByteList(@NotNull String key, @NotNull ByteList value) throws T_Exception {
		return this.withMember(key, new ByteListData(value));
	}

	public default @NotNull T_Writer withIntList(@NotNull String key, int @NotNull ... value) throws T_Exception {
		return this.withMember(key, IntListData.wrap(value));
	}

	public default @NotNull T_Writer withIntList(@NotNull String key, @NotNull IntList value) throws T_Exception {
		return this.withMember(key, new IntListData(value));
	}

	public default @NotNull T_Writer withLongList(@NotNull String key, long @NotNull ... value) throws T_Exception {
		return this.withMember(key, LongListData.wrap(value));
	}

	public default @NotNull T_Writer withLongList(@NotNull String key, @NotNull LongList value) throws T_Exception {
		return this.withMember(key, new LongListData(value));
	}

	public default @NotNull T_Writer withList(@NotNull String key, @NotNull Data @NotNull ... value) throws T_Exception {
		return this.withMember(key, ListData.wrap(value));
	}

	public default @NotNull T_Writer withList(@NotNull String key, @NotNull List<@NotNull Data> value) throws T_Exception {
		return this.withMember(key, new ListData(value));
	}

	public default @NotNull T_Writer withMap(@NotNull String key, @NotNull Map<@NotNull Data, @NotNull Data> value) throws T_Exception {
		return this.withMember(key, new MapData(value));
	}

	public abstract @NotNull T_Writer withMember(@NotNull String key, @NotNull Data value) throws T_Exception;

	public abstract @NotNull T_Writer withoutMember(@NotNull String key) throws T_Exception;

	public abstract T_Writer deepCopy();
}
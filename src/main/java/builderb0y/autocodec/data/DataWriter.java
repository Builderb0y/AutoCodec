package builderb0y.autocodec.data;

import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;

public interface DataWriter<T_Encoded, T_Exception extends Exception> extends DataReader<T_Encoded, T_Exception> {

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
		this.appendDataElement(this.createBoolean(value));
	}

	public default void appendByteElement(byte value) throws T_Exception {
		this.appendDataElement(this.createByte(value));
	}

	public default void appendShortElement(short value) throws T_Exception {
		this.appendDataElement(this.createShort(value));
	}

	public default void appendIntElement(int value) throws T_Exception {
		this.appendDataElement(this.createInt(value));
	}

	public default void appendLongElement(long value) throws T_Exception {
		this.appendDataElement(this.createLong(value));
	}

	public default void appendFloatElement(float value) throws T_Exception {
		this.appendDataElement(this.createFloat(value));
	}

	public default void appendDoubleElement(double value) throws T_Exception {
		this.appendDataElement(this.createDouble(value));
	}

	public default void appendStringElement(@NotNull String value) throws T_Exception {
		this.appendDataElement(this.createString(value));
	}

	public default void appendByteListElement(byte @NotNull ... value) throws T_Exception {
		this.appendDataElement(this.createByteList(value));
	}

	public default void appendByteListElement(@NotNull ByteList value) throws T_Exception {
		this.appendDataElement(this.createByteList(value));
	}

	public default void appendIntListElement(int @NotNull ... value) throws T_Exception {
		this.appendDataElement(this.createIntList(value));
	}

	public default void appendIntListElement(@NotNull IntList value) throws T_Exception {
		this.appendDataElement(this.createIntList(value));
	}

	public default void appendLongListElement(long @NotNull ... value) throws T_Exception {
		this.appendDataElement(this.createLongList(value));
	}

	public default void appendLongListElement(@NotNull LongList value) throws T_Exception {
		this.appendDataElement(this.createLongList(value));
	}

	public default void appendListElement(@NotNull Data<T_Encoded> @NotNull ... value) throws T_Exception {
		this.appendDataElement(this.createList(value));
	}

	public default void appendListElement(@NotNull ObjectList<@NotNull Data<T_Encoded>> value) throws T_Exception {
		this.appendDataElement(this.createList(value));
	}

	public default void appendMapElement(@NotNull Object2ObjectMap<@NotNull Data<T_Encoded>, @NotNull Data<T_Encoded>> value) throws T_Exception {
		this.appendDataElement(this.createMap(value));
	}

	public default void appendDataElement(@NotNull Data<T_Encoded> value) throws T_Exception {
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

	public default void setBooleanElement(int index, boolean value) throws T_Exception {
		this.setDataElement(index, this.createBoolean(value));
	}

	public default void setByteElement(int index, byte value) throws T_Exception {
		this.setDataElement(index, this.createByte(value));
	}

	public default void setShortElement(int index, short value) throws T_Exception {
		this.setDataElement(index, this.createShort(value));
	}

	public default void setIntElement(int index, int value) throws T_Exception {
		this.setDataElement(index, this.createInt(value));
	}

	public default void setLongElement(int index, long value) throws T_Exception {
		this.setDataElement(index, this.createLong(value));
	}

	public default void setFloatElement(int index, float value) throws T_Exception {
		this.setDataElement(index, this.createFloat(value));
	}

	public default void setDoubleElement(int index, double value) throws T_Exception {
		this.setDataElement(index, this.createDouble(value));
	}

	public default void setStringElement(int index, @NotNull String value) throws T_Exception {
		this.setDataElement(index, this.createString(value));
	}

	public default void setByteListElement(int index, byte @NotNull ... value) throws T_Exception {
		this.setDataElement(index, this.createByteList(value));
	}

	public default void setByteListElement(int index, @NotNull ByteList value) throws T_Exception {
		this.setDataElement(index, this.createByteList(value));
	}

	public default void setIntListElement(int index, int @NotNull ... value) throws T_Exception {
		this.setDataElement(index, this.createIntList(value));
	}

	public default void setIntListElement(int index, @NotNull IntList value) throws T_Exception {
		this.setDataElement(index, this.createIntList(value));
	}

	public default void setLongListElement(int index, long @NotNull ... value) throws T_Exception {
		this.setDataElement(index, this.createLongList(value));
	}

	public default void setLongListElement(int index, @NotNull LongList value) throws T_Exception {
		this.setDataElement(index, this.createLongList(value));
	}

	public default void setListElement(int index, @NotNull Data<T_Encoded> @NotNull ... value) throws T_Exception {
		this.setDataElement(index, this.createList(value));
	}

	public default void setListElement(int index, @NotNull ObjectList<@NotNull Data<T_Encoded>> value) throws T_Exception {
		this.setDataElement(index, this.createList(value));
	}

	public default void setMapElement(int index, @NotNull Object2ObjectMap<@NotNull Data<T_Encoded>, @NotNull Data<T_Encoded>> value) throws T_Exception {
		this.setDataElement(index, this.createMap(value));
	}

	public default void setDataElement(int index, @NotNull Data<T_Encoded> value) throws T_Exception {
		this.forceAsList().value.set(index, value);
	}

	public default void removeByte(int index) throws T_Exception {
		this.forceAsByteList().value.removeByte(index);
	}

	public default void removeInt(int index) throws T_Exception {
		this.forceAsIntList().value.removeInt(index);
	}

	public default void removeLong(int index) throws T_Exception {
		this.forceAsLongList().value.removeLong(index);
	}

	public default void removeElement(int index) throws T_Exception {
		this.forceAsList().value.remove(index);
	}

	public default void putBoolean(@NotNull String key, boolean value) throws T_Exception {
		this.putData(key, this.createBoolean(value));
	}

	public default void putByte(@NotNull String key, byte value) throws T_Exception {
		this.putData(key, this.createByte(value));
	}

	public default void putShort(@NotNull String key, short value) throws T_Exception {
		this.putData(key, this.createShort(value));
	}

	public default void putInt(@NotNull String key, int value) throws T_Exception {
		this.putData(key, this.createInt(value));
	}

	public default void putLong(@NotNull String key, long value) throws T_Exception {
		this.putData(key, this.createLong(value));
	}

	public default void putFloat(@NotNull String key, float value) throws T_Exception {
		this.putData(key, this.createFloat(value));
	}

	public default void putDouble(@NotNull String key, double value) throws T_Exception {
		this.putData(key, this.createDouble(value));
	}

	public default void putString(@NotNull String key, @NotNull String value) throws T_Exception {
		this.putData(key, this.createString(value));
	}

	public default void putByteList(@NotNull String key, byte @NotNull ... value) throws T_Exception {
		this.putData(key, this.createByteList(value));
	}

	public default void putByteList(@NotNull String key, @NotNull ByteList value) throws T_Exception {
		this.putData(key, this.createByteList(value));
	}

	public default void putIntList(@NotNull String key, int @NotNull ... value) throws T_Exception {
		this.putData(key, this.createIntList(value));
	}

	public default void putIntList(@NotNull String key, @NotNull IntList value) throws T_Exception {
		this.putData(key, this.createIntList(value));
	}

	public default void putLongList(@NotNull String key, long @NotNull ... value) throws T_Exception {
		this.putData(key, this.createLongList(value));
	}

	public default void putLongList(@NotNull String key, @NotNull LongList value) throws T_Exception {
		this.putData(key, this.createLongList(value));
	}

	public default void putList(@NotNull String key, @NotNull Data<T_Encoded> @NotNull ... value) throws T_Exception {
		this.putData(key, this.createList(value));
	}

	public default void putList(@NotNull String key, @NotNull ObjectList<@NotNull Data<T_Encoded>> value) throws T_Exception {
		this.putData(key, this.createList(value));
	}

	public default void putMap(@NotNull String key, @NotNull Object2ObjectMap<@NotNull Data<T_Encoded>, @NotNull Data<T_Encoded>> value) throws T_Exception {
		this.putData(key, this.createMap(value));
	}

	public default void putData(@NotNull String key, @NotNull Data<T_Encoded> value) throws T_Exception {
		this.forceAsMap().value.put(this.createString(key), value);
	}

	public default void removeMember(@NotNull String key) throws T_Exception {
		this.forceAsMap().value.remove(this.createString(key));
	}
}
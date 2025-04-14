package builderb0y.autocodec.data;

import java.util.Map;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DataReader<T_Encoded, T_Exception extends Exception> extends DataFactory<T_Encoded> {

	@Override
	public default @NotNull DynamicOps<T_Encoded> ops() {
		return this.data().ops;
	}

	public abstract @NotNull Data<T_Encoded> data();

	public abstract @NotNull T_Exception notA(@NotNull String type);

	public default boolean isEmpty   () { return this.data().isEmpty   (); }
	public default boolean isBoolean () { return this.data().isBoolean (); }
	public default boolean isNumber  () { return this.data().isNumber  (); }
	public default boolean isString  () { return this.data().isString  (); }
	public default boolean isByteList() { return this.data().isByteList(); }
	public default boolean isIntList () { return this.data().isIntList (); }
	public default boolean isLongList() { return this.data().isLongList(); }
	public default boolean isList    () { return this.data().isList    (); }
	public default boolean isMap     () { return this.data().isMap     (); }

	public default @Nullable        BooleanData<T_Encoded> tryAsBoolean () { return this.data().tryAsBoolean (); }
	public default @Nullable AbstractNumberData<T_Encoded> tryAsNumber  () { return this.data().tryAsNumber  (); }
	public default @Nullable         StringData<T_Encoded> tryAsString  () { return this.data().tryAsString  (); }
	public default @Nullable       ByteListData<T_Encoded> tryAsByteList() { return this.data().tryAsByteList(); }
	public default @Nullable        IntListData<T_Encoded> tryAsIntList () { return this.data().tryAsIntList (); }
	public default @Nullable       LongListData<T_Encoded> tryAsLongList() { return this.data().tryAsLongList(); }
	public default @Nullable           ListData<T_Encoded> tryAsList    () { return this.data().tryAsList    (); }
	public default @Nullable            MapData<T_Encoded> tryAsMap     () { return this.data().tryAsMap     (); }

	public default @NotNull ListData<T_Encoded> asListOrSingleton() {
		ListData<T_Encoded> list = this.tryAsList();
		if (list == null) list = this.createList(this.data());
		return list;
	}

	@Contract("true -> !null")
	public default @Nullable ListData<T_Encoded> tryAsListMaybeSingleton(boolean singleton) {
		return singleton ? this.asListOrSingleton() : this.tryAsList();
	}

	public default @NotNull ListData<T_Encoded> forceAsListMaybeSingleton(boolean singleton) throws T_Exception {
		ListData<T_Encoded> list = this.tryAsList();
		if (list == null) {
			if (singleton) list = this.createList(this.data());
			else throw this.notA("list");
		}
		return list;
	}

	public default @NotNull BooleanData<T_Encoded> forceAsBoolean() throws T_Exception {
		BooleanData<T_Encoded> data = this.tryAsBoolean();
		if (data != null) return data;
		else throw this.notA("boolean");
	}

	public default @NotNull AbstractNumberData<T_Encoded> forceAsNumber() throws T_Exception {
		AbstractNumberData<T_Encoded> data = this.tryAsNumber();
		if (data != null) return data;
		else throw this.notA("number");
	}

	public default @NotNull StringData<T_Encoded> forceAsString() throws T_Exception {
		StringData<T_Encoded> data = this.tryAsString();
		if (data != null) return data;
		else throw this.notA("string");
	}

	public default @NotNull ByteListData<T_Encoded> forceAsByteList() throws T_Exception {
		ByteListData<T_Encoded> data = this.tryAsByteList();
		if (data != null) return data;
		else throw this.notA("byte list");
	}

	public default @NotNull IntListData<T_Encoded> forceAsIntList() throws T_Exception {
		IntListData<T_Encoded> data = this.tryAsIntList();
		if (data != null) return data;
		else throw this.notA("int list");
	}

	public default @NotNull LongListData<T_Encoded> forceAsLongList() throws T_Exception {
		LongListData<T_Encoded> data = this.tryAsLongList();
		if (data != null) return data;
		else throw this.notA("long list");
	}

	public default @NotNull ListData<T_Encoded> forceAsList() throws T_Exception {
		ListData<T_Encoded> data = this.tryAsList();
		if (data != null) return data;
		else throw this.notA("list");
	}

	public default @NotNull MapData<T_Encoded> forceAsMap() throws T_Exception {
		MapData<T_Encoded> data = this.tryAsMap();
		if (data != null) return data;
		else throw this.notA("map");
	}

	public default byte forceAsByte() throws T_Exception {
		return this.forceAsNumber().byteValue();
	}

	public default short forceAsShort() throws T_Exception {
		return this.forceAsNumber().shortValue();
	}

	public default int forceAsInt() throws T_Exception {
		return this.forceAsNumber().intValue();
	}

	public default long forceAsLong() throws T_Exception {
		return this.forceAsNumber().longValue();
	}

	public default float forceAsFloat() throws T_Exception {
		return this.forceAsNumber().floatValue();
	}

	public default double forceAsDouble() throws T_Exception {
		return this.forceAsNumber().doubleValue();
	}

	public default byte getByte(int index) throws T_Exception {
		return this.forceAsByteList().value.getByte(index);
	}

	public default int getInt(int index) throws T_Exception {
		return this.forceAsIntList().value.getInt(index);
	}

	public default long getLong(int index) throws T_Exception {
		return this.forceAsLongList().value.getLong(index);
	}

	public abstract @NotNull DataReader<T_Encoded, T_Exception> getElement(int index) throws T_Exception;

	public abstract @NotNull DataReader<T_Encoded, T_Exception> getMember(@NotNull String key) throws T_Exception;

	public abstract @NotNull Iterable<? extends @NotNull DataReader<T_Encoded, T_Exception>> listIterable() throws T_Exception;

	public abstract @NotNull Iterable<? extends Map.@NotNull Entry<? extends @NotNull DataReader<T_Encoded, T_Exception>, ? extends @NotNull DataReader<T_Encoded, T_Exception>>> mapIterable() throws T_Exception;
}
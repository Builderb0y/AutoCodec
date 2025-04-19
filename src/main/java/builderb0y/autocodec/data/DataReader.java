package builderb0y.autocodec.data;

import java.util.Map;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.util.StreamableIterable;

public interface DataReader<T_Exception extends Exception> {

	public abstract @NotNull Data data();

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

	public default @Nullable        BooleanData tryAsBoolean () { return this.data().tryAsBoolean (); }
	public default @Nullable AbstractNumberData tryAsNumber  () { return this.data().tryAsNumber  (); }
	public default @Nullable         StringData tryAsString  () { return this.data().tryAsString  (); }
	public default @Nullable       ByteListData tryAsByteList() { return this.data().tryAsByteList(); }
	public default @Nullable        IntListData tryAsIntList () { return this.data().tryAsIntList (); }
	public default @Nullable       LongListData tryAsLongList() { return this.data().tryAsLongList(); }
	public default @Nullable           ListData tryAsList    () { return this.data().tryAsList    (); }
	public default @Nullable            MapData tryAsMap     () { return this.data().tryAsMap     (); }

	public default @NotNull ListData asListOrSingleton() {
		ListData list = this.tryAsList();
		if (list == null) list = ListData.wrap(this.data());
		return list;
	}

	@Contract("true -> !null")
	public default @Nullable ListData tryAsListMaybeSingleton(boolean singleton) {
		return singleton ? this.asListOrSingleton() : this.tryAsList();
	}

	public default @NotNull ListData forceAsListMaybeSingleton(boolean singleton) throws T_Exception {
		ListData list = this.tryAsList();
		if (list == null) {
			if (singleton) list = ListData.wrap(this.data());
			else throw this.notA("list");
		}
		return list;
	}

	public default @NotNull BooleanData forceAsBoolean() throws T_Exception {
		BooleanData data = this.tryAsBoolean();
		if (data != null) return data;
		else throw this.notA("boolean");
	}

	public default @NotNull AbstractNumberData forceAsNumber() throws T_Exception {
		AbstractNumberData data = this.tryAsNumber();
		if (data != null) return data;
		else throw this.notA("number");
	}

	public default @NotNull StringData forceAsString() throws T_Exception {
		StringData data = this.tryAsString();
		if (data != null) return data;
		else throw this.notA("string");
	}

	public default @NotNull ByteListData forceAsByteList() throws T_Exception {
		ByteListData data = this.tryAsByteList();
		if (data != null) return data;
		else throw this.notA("byte list");
	}

	public default @NotNull IntListData forceAsIntList() throws T_Exception {
		IntListData data = this.tryAsIntList();
		if (data != null) return data;
		else throw this.notA("int list");
	}

	public default @NotNull LongListData forceAsLongList() throws T_Exception {
		LongListData data = this.tryAsLongList();
		if (data != null) return data;
		else throw this.notA("long list");
	}

	public default @NotNull ListData forceAsList() throws T_Exception {
		ListData data = this.tryAsList();
		if (data != null) return data;
		else throw this.notA("list");
	}

	public default @NotNull MapData forceAsMap() throws T_Exception {
		MapData data = this.tryAsMap();
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

	public abstract @NotNull DataReader<T_Exception> tryGetElement(int index);

	public abstract @NotNull DataReader<T_Exception> tryGetMember(@NotNull String key);

	public abstract @NotNull DataReader<T_Exception> forceGetElement(int index) throws T_Exception;

	public abstract @NotNull DataReader<T_Exception> forceGetMember(@NotNull String key) throws T_Exception;

	public abstract @NotNull StreamableIterable<? extends @NotNull DataReader<T_Exception>> listIterable() throws T_Exception;

	public abstract @NotNull StreamableIterable<? extends @NotNull DataReader<T_Exception>> listIterableOrSingleton() throws T_Exception;

	public abstract @NotNull StreamableIterable<? extends @NotNull DataReader<T_Exception>> listIterableMaybeSingleton(boolean singleton) throws T_Exception;

	public abstract @NotNull StreamableIterable<? extends Map.@NotNull Entry<? extends @NotNull DataReader<T_Exception>, ? extends @NotNull DataReader<T_Exception>>> mapIterable() throws T_Exception;
}
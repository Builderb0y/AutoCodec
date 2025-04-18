package builderb0y.autocodec.data;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.util.ObjectArrayFactory;

/**
analogous to {@link Dynamic}, but less heavy on DataResult's.
additionally, all subclasses of Data are mutable by default.
this is useful for data fixers and occasionally performance in other places.
*/
public abstract class Data {

	public static final @NotNull ObjectArrayFactory<Data> ARRAY_FACTORY = new ObjectArrayFactory<>(Data.class);

	public abstract <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops);

	public boolean isEmpty   () { return this instanceof          EmptyData; }
	public boolean isBoolean () { return this instanceof        BooleanData; }
	public boolean isNumber  () { return this instanceof AbstractNumberData; }
	public boolean isString  () { return this instanceof         StringData; }
	public boolean isByteList() { return this instanceof       ByteListData; }
	public boolean isIntList () { return this instanceof        IntListData; }
	public boolean isLongList() { return this instanceof       LongListData; }
	public boolean isList    () { return this instanceof           ListData; }
	public boolean isMap     () { return this instanceof            MapData; }

	public @Nullable        BooleanData tryAsBoolean () { return this instanceof        BooleanData data ? data : null; }
	public @Nullable AbstractNumberData tryAsNumber  () { return this instanceof AbstractNumberData data ? data : null; }
	public @Nullable         StringData tryAsString  () { return this instanceof         StringData data ? data : null; }
	public @Nullable       ByteListData tryAsByteList() { return this instanceof       ByteListData data ? data : null; }
	public @Nullable        IntListData tryAsIntList () { return this instanceof        IntListData data ? data : null; }
	public @Nullable       LongListData tryAsLongList() { return this instanceof       LongListData data ? data : null; }
	public @Nullable           ListData tryAsList    () { return this instanceof           ListData data ? data : null; }
	public @Nullable            MapData tryAsMap     () { return this instanceof            MapData data ? data : null; }

	public boolean getAsBooleanOr(boolean defaultValue) {
		BooleanData bool = this.tryAsBoolean();
		return bool != null ? bool.value : defaultValue;
	}

	public byte getAsByteOr(byte defaultValue) {
		AbstractNumberData number = this.tryAsNumber();
		return number != null ? number.byteValue() : defaultValue;
	}

	public short getAsShortOr(short defaultValue) {
		AbstractNumberData number = this.tryAsNumber();
		return number != null ? number.shortValue() : defaultValue;
	}

	public int getAsIntOr(int defaultValue) {
		AbstractNumberData number = this.tryAsNumber();
		return number != null ? number.intValue() : defaultValue;
	}

	public long getAsLongOr(long defaultValue) {
		AbstractNumberData number = this.tryAsNumber();
		return number != null ? number.longValue() : defaultValue;
	}

	public float getAsFloatOr(float defaultValue) {
		AbstractNumberData number = this.tryAsNumber();
		return number != null ? number.floatValue() : defaultValue;
	}

	public double getAsDoubleOr(double defaultValue) {
		AbstractNumberData number = this.tryAsNumber();
		return number != null ? number.doubleValue() : defaultValue;
	}

	public @NotNull Number getAsNumberOr(@NotNull Number defaultValue) {
		AbstractNumberData number = this.tryAsNumber();
		return number != null ? number.numberValue() : defaultValue;
	}

	public @NotNull String getAsStringOr(@NotNull String defaultValue) {
		StringData string = this.tryAsString();
		return string != null ? string.value : defaultValue;
	}

	public @NotNull Data getMember(@NotNull String key) {
		MapData map = this.tryAsMap();
		return map != null ? map.get(key) : EmptyData.INSTANCE;
	}

	public @NotNull Data getMember(@NotNull Data key) {
		MapData map = this.tryAsMap();
		return map != null ? map.get(key) : EmptyData.INSTANCE;
	}

	public @NotNull Data getElement(int index) {
		ListData list = this.tryAsList();
		return list != null ? list.get(index) : EmptyData.INSTANCE;
	}

	@Override public abstract boolean equals(Object obj);
	@Override public abstract int hashCode();
	@Override public abstract String toString();

	public abstract @NotNull Data deepCopy();
}
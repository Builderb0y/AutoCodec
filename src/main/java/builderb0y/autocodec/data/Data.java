package builderb0y.autocodec.data;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
analogous to {@link Dynamic}, but less heavy on DataResult's.
additionally, all subclasses of Data are mutable by default.
this is useful for data fixers and occasionally performance in other places.
*/
public abstract class Data<T_Encoded> {

	public final @NotNull DynamicOps<T_Encoded> ops;

	public Data(@NotNull DynamicOps<T_Encoded> ops) {
		this.ops = ops;
	}

	public @NotNull T_Encoded encode() {
		return this.convert(this.ops);
	}

	public abstract <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops);

	public boolean isEmpty   () { return this instanceof          EmptyData<T_Encoded>; }
	public boolean isBoolean () { return this instanceof        BooleanData<T_Encoded>; }
	public boolean isNumber  () { return this instanceof AbstractNumberData<T_Encoded>; }
	public boolean isString  () { return this instanceof         StringData<T_Encoded>; }
	public boolean isByteList() { return this instanceof       ByteListData<T_Encoded>; }
	public boolean isIntList () { return this instanceof        IntListData<T_Encoded>; }
	public boolean isLongList() { return this instanceof       LongListData<T_Encoded>; }
	public boolean isList    () { return this instanceof           ListData<T_Encoded>; }
	public boolean isMap     () { return this instanceof            MapData<T_Encoded>; }

	public @Nullable        BooleanData<T_Encoded> tryAsBoolean () { return this instanceof        BooleanData<T_Encoded> data ? data : null; }
	public @Nullable AbstractNumberData<T_Encoded> tryAsNumber  () { return this instanceof AbstractNumberData<T_Encoded> data ? data : null; }
	public @Nullable         StringData<T_Encoded> tryAsString  () { return this instanceof         StringData<T_Encoded> data ? data : null; }
	public @Nullable       ByteListData<T_Encoded> tryAsByteList() { return this instanceof       ByteListData<T_Encoded> data ? data : null; }
	public @Nullable        IntListData<T_Encoded> tryAsIntList () { return this instanceof        IntListData<T_Encoded> data ? data : null; }
	public @Nullable       LongListData<T_Encoded> tryAsLongList() { return this instanceof       LongListData<T_Encoded> data ? data : null; }
	public @Nullable           ListData<T_Encoded> tryAsList    () { return this instanceof           ListData<T_Encoded> data ? data : null; }
	public @Nullable            MapData<T_Encoded> tryAsMap     () { return this instanceof            MapData<T_Encoded> data ? data : null; }

	@Override public abstract boolean equals(Object obj);
	@Override public abstract int hashCode();
	@Override public abstract String toString();

	public abstract @NotNull Data<T_Encoded> deepCopy();
}
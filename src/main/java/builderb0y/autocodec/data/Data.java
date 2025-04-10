package builderb0y.autocodec.data;

import java.util.List;
import java.util.Map;

import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Data<T_Encoded> {

	public final @NotNull DynamicOps<T_Encoded> ops;

	public Data(@NotNull DynamicOps<T_Encoded> ops) {
		this.ops = ops;
	}

	public @NotNull T_Encoded encode() {
		return this.convert(this.ops);
	}

	public abstract <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops);

	public boolean isEmpty() { return false; }

	public boolean isBoolean() { return false; }
	public @Nullable Boolean tryAsBoolean() { return null; }

	public boolean isNumber() { return false; }
	public @Nullable AbstractNumberData<T_Encoded> tryAsNumber() { return null; }

	public boolean isString() { return false; }
	public @Nullable String tryAsString() { return null; }

	public boolean isByteList() { return false; }
	public @Nullable ByteList tryAsByteList() { return null; }

	public boolean isIntList() { return false; }
	public @Nullable IntList tryAsIntList() { return null; }

	public boolean isLongList() { return false; }
	public @Nullable LongList tryAsLongList() { return null; }

	public boolean isList() { return false; }
	public @Nullable List<@NotNull Data<T_Encoded>> tryAsList() { return null; }

	public boolean isMap() { return false; }
	public @Nullable Map<@NotNull Data<T_Encoded>, @NotNull Data<T_Encoded>> tryAsMap() { return null; }

	@Override public abstract boolean equals(Object obj);
	@Override public abstract int hashCode();
	@Override public abstract String toString();
}
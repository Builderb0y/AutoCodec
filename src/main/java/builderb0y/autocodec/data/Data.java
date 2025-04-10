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

	public @Nullable Number tryAsNumber() { return null; }

	public @Nullable Boolean tryAsBoolean() { return null; }
	public @Nullable Byte tryAsByte() { return null; }
	public @Nullable Short tryAsShort() { return null; }
	public @Nullable Integer tryAsInt() { return null; }
	public @Nullable Long tryAsLong() { return null; }
	public @Nullable Float tryAsFloat() { return null; }
	public @Nullable Double tryAsDouble() { return null; }
	public @Nullable String tryAsString() { return null; }

	public @Nullable ByteList tryAsByteList() { return null; }
	public @Nullable IntList tryAsIntList() { return null; }
	public @Nullable LongList tryAsLongList() { return null; }

	public @Nullable List<@NotNull Data<T_Encoded>> tryAsList() { return null; }
	public @Nullable Map<@NotNull Data<T_Encoded>, @NotNull Data<T_Encoded>> tryAsMap() { return null; }

	@Override public abstract boolean equals(Object obj);
	@Override public abstract int hashCode();
	@Override public abstract String toString();
}
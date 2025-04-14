package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.jetbrains.annotations.NotNull;

public class LongListData<T_Encoded> extends Data<T_Encoded> {

	public @NotNull LongList value;

	public LongListData(@NotNull DynamicOps<T_Encoded> ops) {
		super(ops);
		this.value = new LongArrayList();
	}

	public LongListData(@NotNull DynamicOps<T_Encoded> ops, int capacity) {
		super(ops);
		this.value = new LongArrayList(capacity);
	}

	public LongListData(@NotNull DynamicOps<T_Encoded> ops, @NotNull LongList value) {
		super(ops);
		this.value = value;
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.createLongList(this.value.longStream());
	}

	public int size() {
		return this.value.size();
	}

	public long getLong(int index) {
		return this.value.getLong(index);
	}

	public long setLong(int index, long value) {
		return this.value.set(index, value);
	}

	public void append(long value) {
		this.value.add(value);
	}

	public long remove(int index) {
		return this.value.removeLong(index);
	}

	@Override
	public boolean equals(Object object) {
		LongListData<?> longList;
		return object instanceof Data<?> data && (longList = data.tryAsLongList()) != null && this.value.equals(longList.value);
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public String toString() {
		return this.value.toString();
	}

	@Override
	public @NotNull Data<T_Encoded> deepCopy() {
		return new LongListData<>(this.ops, new LongArrayList(this.value));
	}
}
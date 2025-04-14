package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;

public class IntListData<T_Encoded> extends Data<T_Encoded> {

	public @NotNull IntList value;

	public IntListData(@NotNull DynamicOps<T_Encoded> ops) {
		super(ops);
		this.value = new IntArrayList();
	}

	public IntListData(@NotNull DynamicOps<T_Encoded> ops, int capacity) {
		super(ops);
		this.value = new IntArrayList(capacity);
	}

	public IntListData(@NotNull DynamicOps<T_Encoded> ops, @NotNull IntList value) {
		super(ops);
		this.value = value;
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.createIntList(this.value.intStream());
	}

	public int size() {
		return this.value.size();
	}

	public int getInt(int index) {
		return this.value.getInt(index);
	}

	public int setInt(int index, int value) {
		return this.value.set(index, value);
	}

	public void append(int value) {
		this.value.add(value);
	}

	public int remove(int index) {
		return this.value.removeInt(index);
	}

	@Override
	public boolean equals(Object object) {
		IntListData<?> intList;
		return object instanceof IntListData<?> data && (intList = data.tryAsIntList()) != null && this.value.equals(intList.value);
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
		return new IntListData<>(this.ops, new IntArrayList(this.value));
	}
}
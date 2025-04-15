package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;

public class IntListData extends Data {

	public @NotNull IntList value;

	public IntListData() {
		this.value = new IntArrayList();
	}

	public IntListData(int capacity) {
		this.value = new IntArrayList(capacity);
	}

	public IntListData(@NotNull IntList value) {
		this.value = value;
	}

	public static IntListData wrap(int @NotNull ... ints) {
		return new IntListData(IntArrayList.wrap(ints));
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
		IntListData intList;
		return object instanceof IntListData data && (intList = data.tryAsIntList()) != null && this.value.equals(intList.value);
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
	public @NotNull Data deepCopy() {
		return new IntListData(new IntArrayList(this.value));
	}
}
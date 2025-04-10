package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.NotNull;

public class IntArrayData<T_Encoded> extends Data<T_Encoded> {

	public @NotNull IntList value;

	public IntArrayData(@NotNull DynamicOps<T_Encoded> ops) {
		super(ops);
		this.value = new IntArrayList();
	}

	public IntArrayData(@NotNull DynamicOps<T_Encoded> ops, int capacity) {
		super(ops);
		this.value = new IntArrayList(capacity);
	}

	public IntArrayData(@NotNull DynamicOps<T_Encoded> ops, @NotNull IntList value) {
		super(ops);
		this.value = value;
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.createIntList(this.value.intStream());
	}

	@Override
	public @NotNull IntList tryAsIntList() {
		return this.value;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof IntArrayData<?> that && this.value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public String toString() {
		return this.value.toString();
	}
}
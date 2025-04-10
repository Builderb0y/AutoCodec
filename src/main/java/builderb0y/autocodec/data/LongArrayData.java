package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.jetbrains.annotations.NotNull;

public class LongArrayData<T_Encoded> extends Data<T_Encoded> {

	public @NotNull LongList value;

	public LongArrayData(@NotNull DynamicOps<T_Encoded> ops) {
		super(ops);
		this.value = new LongArrayList();
	}

	public LongArrayData(@NotNull DynamicOps<T_Encoded> ops, int capacity) {
		super(ops);
		this.value = new LongArrayList(capacity);
	}

	public LongArrayData(@NotNull DynamicOps<T_Encoded> ops, @NotNull LongList value) {
		super(ops);
		this.value = value;
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.createLongList(this.value.longStream());
	}

	@Override
	public @NotNull LongList tryAsLongList() {
		return this.value;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof LongArrayData<?> that && this.value.equals(that.value);
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
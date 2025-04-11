package builderb0y.autocodec.data;

import java.nio.ByteBuffer;

import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import org.jetbrains.annotations.NotNull;

public class ByteArrayData<T_Encoded> extends Data<T_Encoded> {

	public @NotNull ByteList value;

	public ByteArrayData(@NotNull DynamicOps<T_Encoded> ops) {
		super(ops);
		this.value = new ByteArrayList();
	}

	public ByteArrayData(@NotNull DynamicOps<T_Encoded> ops, int capacity) {
		super(ops);
		this.value = new ByteArrayList(capacity);
	}

	public ByteArrayData(@NotNull DynamicOps<T_Encoded> ops, @NotNull ByteList value) {
		super(ops);
		this.value = value;
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		if (this.value instanceof ByteArrayList arrayList) {
			return ops.createByteList(ByteBuffer.wrap(arrayList.elements(), 0, arrayList.size()));
		}
		else {
			return ops.createByteList(ByteBuffer.wrap(this.value.toByteArray()));
		}
	}

	@Override
	public boolean isByteList() {
		return true;
	}

	@Override
	public @NotNull ByteList tryAsByteList() {
		return this.value;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ByteArrayData<?> that && this.value.equals(that.value);
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
		return new ByteArrayData<>(this.ops, new ByteArrayList(this.value));
	}
}
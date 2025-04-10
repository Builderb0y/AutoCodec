package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;

public class ByteData<T_Encoded> extends NumberData<T_Encoded> {

	public byte value;

	public ByteData(@NotNull DynamicOps<T_Encoded> ops) {
		super(ops);
	}

	public ByteData(@NotNull DynamicOps<T_Encoded> ops, byte value) {
		super(ops);
		this.value = value;
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.createByte(this.value);
	}

	@Override
	public @NotNull Number tryAsNumber() {
		return this.value;
	}

	@Override
	public byte byteValue() {
		return this.value;
	}

	@Override
	public short shortValue() {
		return this.value;
	}

	@Override
	public int intValue() {
		return this.value;
	}

	@Override
	public long longValue() {
		return this.value;
	}

	@Override
	public float floatValue() {
		return this.value;
	}

	@Override
	public double doubleValue() {
		return this.value;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ByteData<?> that && this.value == that.value;
	}

	@Override
	public int hashCode() {
		return Byte.hashCode(this.value);
	}

	@Override
	public String toString() {
		return Byte.toString(this.value);
	}
}
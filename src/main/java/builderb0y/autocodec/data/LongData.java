package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;

public class LongData<T_Encoded> extends NumberData<T_Encoded> {

	public long value;

	public LongData(@NotNull DynamicOps<T_Encoded> ops) {
		super(ops);
	}

	public LongData(@NotNull DynamicOps<T_Encoded> ops, long value) {
		super(ops);
		this.value = value;
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.createLong(this.value);
	}

	@Override
	public @NotNull Number tryAsNumber() {
		return this.value;
	}

	@Override
	public byte byteValue() {
		return (byte)(this.value);
	}

	@Override
	public short shortValue() {
		return (short)(this.value);
	}

	@Override
	public int intValue() {
		return (int)(this.value);
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
		return obj instanceof LongData<?> that && this.value == that.value;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(this.value);
	}

	@Override
	public String toString() {
		return Long.toString(this.value);
	}
}
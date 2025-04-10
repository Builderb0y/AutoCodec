package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;

public class UnknownNumberData<T_Encoded> extends AbstractNumberData<T_Encoded> {

	public @NotNull Number value;

	public UnknownNumberData(@NotNull DynamicOps<T_Encoded> ops, @NotNull Number value) {
		super(ops);
		this.value = value;
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.createNumeric(this.value);
	}

	@Override
	public void set(byte value) {
		this.value = value;
	}

	@Override
	public void set(short value) {
		this.value = value;
	}

	@Override
	public void set(int value) {
		this.value = value;
	}

	@Override
	public void set(long value) {
		this.value = value;
	}

	@Override
	public void set(float value) {
		this.value = value;
	}

	@Override
	public void set(double value) {
		this.value = value;
	}

	@Override
	public byte byteValue() {
		return this.value.byteValue();
	}

	@Override
	public short shortValue() {
		return this.value.shortValue();
	}

	@Override
	public int intValue() {
		return this.value.intValue();
	}

	@Override
	public long longValue() {
		return this.value.longValue();
	}

	@Override
	public float floatValue() {
		return this.value.floatValue();
	}

	@Override
	public double doubleValue() {
		return this.value.doubleValue();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof UnknownNumberData<?> that && this.value.equals(that.value);
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
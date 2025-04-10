package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractNumberData<T_Encoded> extends Data<T_Encoded> {

	public AbstractNumberData(@NotNull DynamicOps<T_Encoded> ops) {
		super(ops);
	}

	@Override
	public boolean isNumber() {
		return true;
	}

	@Override
	public @Nullable AbstractNumberData<T_Encoded> tryAsNumber() {
		return this;
	}

	public abstract byte byteValue();
	public abstract short shortValue();
	public abstract int intValue();
	public abstract long longValue();
	public abstract float floatValue();
	public abstract double doubleValue();

	public abstract void set(byte value);
	public abstract void set(short value);
	public abstract void set(int value);
	public abstract void set(long value);
	public abstract void set(float value);
	public abstract void set(double value);
}
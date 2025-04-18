package builderb0y.autocodec.data;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.util.ObjectArrayFactory;

public abstract class AbstractNumberData extends Data {

	public static final @NotNull ObjectArrayFactory<AbstractNumberData> ARRAY_FACTORY = new ObjectArrayFactory<>(AbstractNumberData.class);

	public abstract @NotNull Number numberValue();

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

	@Override
	public abstract @NotNull AbstractNumberData deepCopy();
}
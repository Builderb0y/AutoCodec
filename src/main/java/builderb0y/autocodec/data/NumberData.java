package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;

public abstract class NumberData<T_Encoded> extends Data<T_Encoded> {

	public NumberData(@NotNull DynamicOps<T_Encoded> ops) {
		super(ops);
	}

	@Override
	public abstract @NotNull Number tryAsNumber();

	@Override public @NotNull Byte tryAsByte() { return this.byteValue(); }
	@Override public @NotNull Short tryAsShort() { return this.shortValue(); }
	@Override public @NotNull Integer tryAsInt() { return this.intValue(); }
	@Override public @NotNull Long tryAsLong() { return this.longValue(); }
	@Override public @NotNull Float tryAsFloat() { return this.floatValue(); }
	@Override public @NotNull Double tryAsDouble() { return this.doubleValue(); }

	public abstract byte byteValue();
	public abstract short shortValue();
	public abstract int intValue();
	public abstract long longValue();
	public abstract float floatValue();
	public abstract double doubleValue();
}
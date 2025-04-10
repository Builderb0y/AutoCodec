package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.HashCommon;
import org.jetbrains.annotations.NotNull;

public class NumberData<T_Encoded> extends AbstractNumberData<T_Encoded> {

	public static final byte
		BYTE = 0,
		SHORT = 1,
		INT = 2,
		LONG = 3,
		FLOAT = 4,
		DOUBLE = 5;

	public long bits; //basically a union.
	public byte precision;

	public NumberData(@NotNull DynamicOps<T_Encoded> ops) {
		super(ops);
	}

	public NumberData(@NotNull DynamicOps<T_Encoded> ops, byte value) {
		super(ops);
		this.set(value);
	}

	public NumberData(@NotNull DynamicOps<T_Encoded> ops, short value) {
		super(ops);
		this.set(value);
	}

	public NumberData(@NotNull DynamicOps<T_Encoded> ops, int value) {
		super(ops);
		this.set(value);
	}

	public NumberData(@NotNull DynamicOps<T_Encoded> ops, long value) {
		super(ops);
		this.set(value);
	}

	public NumberData(@NotNull DynamicOps<T_Encoded> ops, float value) {
		super(ops);
		this.set(value);
	}

	public NumberData(@NotNull DynamicOps<T_Encoded> ops, double value) {
		super(ops);
		this.set(value);
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return switch (this.precision) {
			case BYTE -> ops.createByte((byte)(this.bits));
			case SHORT -> ops.createShort((short)(this.bits));
			case INT -> ops.createInt((int)(this.bits));
			case LONG -> ops.createLong(this.bits);
			case FLOAT -> ops.createFloat(Float.intBitsToFloat((int)(this.bits)));
			case DOUBLE -> ops.createDouble(Double.longBitsToDouble(this.bits));
			default -> throw new IllegalStateException("Invalid precision: " + this.precision);
		};
	}

	@Override
	public void set(byte value) {
		this.bits = value;
		this.precision = BYTE;
	}

	@Override
	public void set(short value) {
		this.bits = value;
		this.precision = SHORT;
	}

	@Override
	public void set(int value) {
		this.bits = value;
		this.precision = INT;
	}

	@Override
	public void set(long value) {
		this.bits = value;
		this.precision = LONG;
	}

	@Override
	public void set(float value) {
		this.bits = Float.floatToRawIntBits(value);
		this.precision = FLOAT;
	}

	@Override
	public void set(double value) {
		this.bits = Double.doubleToRawLongBits(value);
		this.precision = DOUBLE;
	}

	@Override
	public byte byteValue() {
		return switch (this.precision) {
			case BYTE, SHORT, INT, LONG -> (byte)(this.bits);
			case FLOAT -> (byte)(Float.intBitsToFloat((int)(this.bits)));
			case DOUBLE -> (byte)(Double.longBitsToDouble(this.bits));
			default -> throw new IllegalStateException("Invalid precision: " + this.precision);
		};
	}

	@Override
	public short shortValue() {
		return switch (this.precision) {
			case BYTE, SHORT, INT, LONG -> (short)(this.bits);
			case FLOAT -> (short)(Float.intBitsToFloat((int)(this.bits)));
			case DOUBLE -> (short)(Double.longBitsToDouble(this.bits));
			default -> throw new IllegalStateException("Invalid precision: " + this.precision);
		};
	}

	@Override
	public int intValue() {
		return switch (this.precision) {
			case BYTE, SHORT, INT, LONG -> (int)(this.bits);
			case FLOAT -> (int)(Float.intBitsToFloat((int)(this.bits)));
			case DOUBLE -> (int)(Double.longBitsToDouble(this.bits));
			default -> throw new IllegalStateException("Invalid precision: " + this.precision);
		};
	}

	@Override
	public long longValue() {
		return switch (this.precision) {
			case BYTE, SHORT, INT, LONG -> this.bits;
			case FLOAT -> (long)(Float.intBitsToFloat((int)(this.bits)));
			case DOUBLE -> (long)(Double.longBitsToDouble(this.bits));
			default -> throw new IllegalStateException("Invalid precision: " + this.precision);
		};
	}

	@Override
	public float floatValue() {
		return switch (this.precision) {
			case BYTE, SHORT, INT, LONG -> (float)(this.bits);
			case FLOAT -> Float.intBitsToFloat((int)(this.bits));
			case DOUBLE -> (float)(Double.longBitsToDouble(this.bits));
			default -> throw new IllegalStateException("Invalid precision: " + this.precision);
		};
	}

	@Override
	public double doubleValue() {
		return switch (this.precision) {
			case BYTE, SHORT, INT, LONG -> (double)(this.bits);
			case FLOAT -> (double)(Float.intBitsToFloat((int)(this.bits)));
			case DOUBLE -> Double.longBitsToDouble(this.bits);
			default -> throw new IllegalStateException("Invalid precision: " + this.precision);
		};
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof NumberData<?> that && this.precision == that.precision && this.bits == that.bits;
	}

	@Override
	public int hashCode() {
		return HashCommon.mix(this.precision) + Long.hashCode(HashCommon.mix(this.bits));
	}

	@Override
	public String toString() {
		return switch (this.precision) {
			case BYTE -> Byte.toString((byte)(this.bits));
			case SHORT -> Short.toString((short)(this.bits));
			case INT -> Integer.toString((int)(this.bits));
			case LONG -> Long.toString(this.bits);
			case FLOAT -> Float.toString(Float.intBitsToFloat((int)(this.bits)));
			case DOUBLE -> Double.toString(Double.longBitsToDouble(this.bits));
			default -> throw new IllegalStateException("Invalid precision: " + this.precision);
		};
	}
}
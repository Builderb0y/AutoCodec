package builderb0y.autocodec.data;

import com.mojang.serialization.DynamicOps;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.util.ObjectArrayFactory;

public class NumberData extends AbstractNumberData {

	public static final @NotNull ObjectArrayFactory<NumberData> ARRAY_FACTORY = new ObjectArrayFactory<>(NumberData.class);

	public static final byte
		BYTE   = 0,
		SHORT  = 1,
		INT    = 2,
		LONG   = 3,
		FLOAT  = 4,
		DOUBLE = 5;

	public long bits; //basically a union.
	@MagicConstant(valuesFromClass = NumberData.class)
	public byte precision;

	public NumberData(long bits, @MagicConstant(valuesFromClass = NumberData.class) byte precision) {
		this.bits = bits;
		this.precision = precision;
	}

	public NumberData() {}
	public NumberData(byte   value) { this.set(value); }
	public NumberData(short  value) { this.set(value); }
	public NumberData(int    value) { this.set(value); }
	public NumberData(long   value) { this.set(value); }
	public NumberData(float  value) { this.set(value); }
	public NumberData(double value) { this.set(value); }

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
	public @NotNull Number numberValue() {
		return switch (this.precision) {
			case BYTE -> (byte)(this.bits);
			case SHORT -> (short)(this.bits);
			case INT -> (int)(this.bits);
			case LONG -> this.bits;
			case FLOAT -> Float.intBitsToFloat((int)(this.bits));
			case DOUBLE -> Double.longBitsToDouble(this.bits);
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
	public boolean equals(Object object) {
		AbstractNumberData number;
		if (object instanceof Data data && (number = data.tryAsNumber()) != null) {
			if (number instanceof NumberData same) {
				return this.precision == same.precision && this.bits == same.bits;
			}
			else {
				return this.numberValue().equals(number.numberValue());
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return switch (this.precision) {
			case BYTE -> Byte.hashCode((byte)(this.bits));
			case SHORT -> Short.hashCode((short)(this.bits));
			case INT -> Integer.hashCode((int)(this.bits));
			case LONG -> Long.hashCode(this.bits);
			case FLOAT -> Float.hashCode(Float.intBitsToFloat((int)(this.bits)));
			case DOUBLE -> Double.hashCode(Double.longBitsToDouble(this.bits));
			default -> throw new IllegalStateException("Invalid precision: " + this.precision);
		};
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

	@Override
	public @NotNull AbstractNumberData deepCopy() {
		return new NumberData(this.bits, this.precision);
	}
}
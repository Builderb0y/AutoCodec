package builderb0y.autocodec.common;

import java.util.Objects;

import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.*;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.reflection.AnnotationContainer;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public interface DefaultValue {

	public default <T_Encoded> @NotNull DecodeContext<T_Encoded> applyToContext(@NotNull DecodeContext<T_Encoded> context) {
		return context.input(this.getInput(context.ops));
	}

	public abstract <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOps<T_Encoded> ops);

	public abstract boolean decodedValueEquals(@Nullable Object value);

	public default  <T_Encoded> boolean encodedValueEquals(@NotNull T_Encoded value, @NotNull DynamicOps<T_Encoded> ops) {
		return Objects.equals(value, this.getInput(ops)) || Objects.equals(value, ops.empty());
	}

	public abstract boolean alwaysEncode();

	public static @NotNull DefaultValue forType(@NotNull ReifiedType<?> type) {
		AnnotationContainer annotations = type.getAnnotations();
		DefaultByte    defaultByte    = annotations.getFirst(DefaultByte   .class); if (defaultByte    != null) return new    ByteDefaultValue(defaultByte   .value(), defaultByte   .alwaysEncode());
		DefaultShort   defaultShort   = annotations.getFirst(DefaultShort  .class); if (defaultShort   != null) return new   ShortDefaultValue(defaultShort  .value(), defaultShort  .alwaysEncode());
		DefaultInt     defaultInt     = annotations.getFirst(DefaultInt    .class); if (defaultInt     != null) return new     IntDefaultValue(defaultInt    .value(), defaultInt    .alwaysEncode());
		DefaultLong    defaultLong    = annotations.getFirst(DefaultLong   .class); if (defaultLong    != null) return new    LongDefaultValue(defaultLong   .value(), defaultLong   .alwaysEncode());
		DefaultFloat   defaultFloat   = annotations.getFirst(DefaultFloat  .class); if (defaultFloat   != null) return new   FloatDefaultValue(defaultFloat  .value(), defaultFloat  .alwaysEncode());
		DefaultDouble  defaultDouble  = annotations.getFirst(DefaultDouble .class); if (defaultDouble  != null) return new  DoubleDefaultValue(defaultDouble .value(), defaultDouble .alwaysEncode());
		DefaultString  defaultString  = annotations.getFirst(DefaultString .class); if (defaultString  != null) return new  StringDefaultValue(defaultString .value(), defaultString .alwaysEncode());
		DefaultBoolean defaultBoolean = annotations.getFirst(DefaultBoolean.class); if (defaultBoolean != null) return new BooleanDefaultValue(defaultBoolean.value(), defaultBoolean.alwaysEncode());
		return NullDefaultValue.INSTANCE;
	}

	public static enum NullDefaultValue implements DefaultValue {

		INSTANCE;

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOps<T_Encoded> ops) {
			return ops.empty();
		}

		@Override
		public @NotNull <T_Encoded> DecodeContext<T_Encoded> applyToContext(@NotNull DecodeContext<T_Encoded> context) {
			return context;
		}

		@Override
		public boolean decodedValueEquals(@Nullable Object value) {
			return value == null;
		}

		@Override
		public <T_Encoded> boolean encodedValueEquals(@NotNull T_Encoded value, @NotNull DynamicOps<T_Encoded> ops) {
			return Objects.equals(value, ops.empty());
		}

		@Override
		public boolean alwaysEncode() {
			return false;
		}
	}

	public static record ByteDefaultValue(byte value, boolean alwaysEncode) implements DefaultValue {

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOps<T_Encoded> ops) {
			return ops.createByte(this.value);
		}

		@Override
		public boolean decodedValueEquals(@Nullable Object value) {
			return value instanceof Byte b && b.byteValue() == this.value;
		}
	}

	public static record ShortDefaultValue(short value, boolean alwaysEncode) implements DefaultValue {

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOps<T_Encoded> ops) {
			return ops.createShort(this.value);
		}

		@Override
		public boolean decodedValueEquals(@Nullable Object value) {
			return value instanceof Short s && s.shortValue() == this.value;
		}
	}

	public static record IntDefaultValue(int value, boolean alwaysEncode) implements DefaultValue {

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOps<T_Encoded> ops) {
			return ops.createInt(this.value);
		}

		@Override
		public boolean decodedValueEquals(@Nullable Object value) {
			return value instanceof Integer i && i.intValue() == this.value;
		}
	}

	public static record LongDefaultValue(long value, boolean alwaysEncode) implements DefaultValue {

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOps<T_Encoded> ops) {
			return ops.createLong(this.value);
		}

		@Override
		public boolean decodedValueEquals(@Nullable Object value) {
			return value instanceof Long l && l.longValue() == this.value;
		}
	}

	public static record FloatDefaultValue(float value, boolean alwaysEncode) implements DefaultValue {

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOps<T_Encoded> ops) {
			return ops.createFloat(this.value);
		}

		@Override
		public boolean decodedValueEquals(@Nullable Object value) {
			return value instanceof Float f && Float.floatToIntBits(f.floatValue()) == Float.floatToIntBits(this.value);
		}
	}

	public static record DoubleDefaultValue(double value, boolean alwaysEncode) implements DefaultValue {

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOps<T_Encoded> ops) {
			return ops.createDouble(this.value);
		}

		@Override
		public boolean decodedValueEquals(@Nullable Object value) {
			return value instanceof Double d && Double.doubleToLongBits(d.doubleValue()) == Double.doubleToLongBits(this.value);
		}
	}

	public static record StringDefaultValue(@NotNull String value, boolean alwaysEncode) implements DefaultValue {

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOps<T_Encoded> ops) {
			return ops.createString(this.value);
		}

		@Override
		public boolean decodedValueEquals(@Nullable Object value) {
			return this.value.equals(value);
		}
	}

	public static record BooleanDefaultValue(boolean value, boolean alwaysEncode) implements DefaultValue {

		@Override
		public <T_Encoded> @NotNull T_Encoded getInput(@NotNull DynamicOps<T_Encoded> ops) {
			return ops.createBoolean(this.value);
		}

		@Override
		public boolean decodedValueEquals(@Nullable Object value) {
			return value == Boolean.valueOf(this.value);
		}
	}
}
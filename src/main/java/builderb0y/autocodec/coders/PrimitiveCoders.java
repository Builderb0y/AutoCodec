package builderb0y.autocodec.coders;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;

public class PrimitiveCoders {

	//////////////////////////////// java.lang ////////////////////////////////

	public static final AutoCoder<Byte> BYTE = new NamedCoder<>("PrimitiveCoders.BYTE") {

		@Override
		@OverrideOnly
		public <T_Encoded> @Nullable Byte decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (context.isEmpty()) return null;
			Number number = context.forceAsNumber();
			if (number instanceof Byte b) return b;
			return Byte.valueOf(number.byteValue());
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, Byte> context) throws EncodeException {
			return context.object == null ? context.empty() : context.createByte(context.object.byteValue());
		}
	};

	public static final AutoCoder<Short> SHORT = new NamedCoder<>("PrimitiveCoders.SHORT") {

		@Override
		@OverrideOnly
		public <T_Encoded> @Nullable Short decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (context.isEmpty()) return null;
			Number number = context.forceAsNumber();
			if (number instanceof Short s) return s;
			return Short.valueOf(number.shortValue());
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, Short> context) throws EncodeException {
			return context.object == null ? context.empty() : context.createShort(context.object.shortValue());
		}
	};

	public static final AutoCoder<Integer> INT = new NamedCoder<>("PrimitiveCoders.INT") {

		@Override
		@OverrideOnly
		public <T_Encoded> @Nullable Integer decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (context.isEmpty()) return null;
			Number number = context.forceAsNumber();
			if (number instanceof Integer i) return i;
			return Integer.valueOf(number.intValue());
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, Integer> context) throws EncodeException {
			return context.object == null ? context.empty() : context.createInt(context.object.intValue());
		}
	};

	public static final AutoCoder<Long> LONG = new NamedCoder<>("PrimitiveCoders.LONG") {

		@Override
		@OverrideOnly
		public <T_Encoded> @Nullable Long decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (context.isEmpty()) return null;
			Number number = context.forceAsNumber();
			if (number instanceof Long l) return l;
			return Long.valueOf(number.longValue());
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, Long> context) throws EncodeException {
			return context.object == null ? context.empty() : context.createLong(context.object.longValue());
		}
	};

	public static final AutoCoder<Float> FLOAT = new NamedCoder<>("PrimitiveCoders.FLOAT") {

		@Override
		@OverrideOnly
		public <T_Encoded> @Nullable Float decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (context.isEmpty()) return null;
			Number number = context.forceAsNumber();
			if (number instanceof Float f) return f;
			return Float.valueOf(number.floatValue());
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, Float> context) throws EncodeException {
			return context.object == null ? context.empty() : context.createFloat(context.object.floatValue());
		}
	};

	public static final AutoCoder<Double> DOUBLE = new NamedCoder<>("PrimitiveCoders.DOUBLE") {

		@Override
		@OverrideOnly
		public <T_Encoded> @Nullable Double decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (context.isEmpty()) return null;
			Number number = context.forceAsNumber();
			if (number instanceof Double d) return d;
			return Double.valueOf(number.doubleValue());
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, Double> context) throws EncodeException {
			return context.object == null ? context.empty() : context.createDouble(context.object.doubleValue());
		}
	};

	public static final AutoCoder<Number> NUMBER = new NamedCoder<>("PrimitiveCoders.NUMBER") {

		@Override
		@OverrideOnly
		public <T_Encoded> @Nullable Number decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (context.isEmpty()) return null;
			return context.forceAsNumber();
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, Number> context) throws EncodeException {
			return context.object == null ? context.empty() : context.createNumber(context.object);
		}
	};

	public static final AutoCoder<Character> CHAR = new NamedCoder<>("PrimitiveCoders.CHAR") {

		@Override
		@OverrideOnly
		public <T_Encoded> @Nullable Character decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (context.isEmpty()) return null;
			Number number = context.tryAsNumber();
			if (number != null) return (char)(number.shortValue());
			String string = context.tryAsString();
			if (string != null && string.length() == 1) return string.charAt(0);
			throw new DecodeException(() -> context.pathToStringBuilder().append(" is not a char: ").append(context.input).toString());
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, Character> context) throws EncodeException {
			return context.object == null ? context.empty() : (
			context.isCompressed()
				? context.createShort((short)(context.object.charValue()))
				: context.createString(String.valueOf(context.object.charValue()))
			);
		}
	};

	public static final AutoCoder<String> STRING = new NamedCoder<>("PrimitiveCoders.STRING") {

		@Override
		@OverrideOnly
		public <T_Encoded> @Nullable String decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (context.isEmpty()) return null;
			return context.forceAsString();
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, String> context) throws EncodeException {
			return context.object == null ? context.empty() : context.createString(context.object);
		}
	};

	public static final AutoCoder<Boolean> BOOLEAN = new NamedCoder<>("PrimitiveCoders.BOOLEAN") {

		@Override
		@OverrideOnly
		public <T_Encoded> @Nullable Boolean decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (context.isEmpty()) return null;
			return context.forceAsBoolean();
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, Boolean> context) throws EncodeException {
			return context.object == null ? context.empty() : context.createBoolean(context.object.booleanValue());
		}
	};

	//////////////////////////////// java.math ////////////////////////////////

	public static final AutoCoder<BigInteger>     BIG_INTEGER      = stringBased("PrimitiveCoders.BIG_INTEGER",          BigInteger::new, BigInteger::toString     );
	public static final AutoCoder<BigDecimal>     BIG_DECIMAL      = stringBased("PrimitiveCoders.BIG_DECIMAL",          BigDecimal::new, BigDecimal::toPlainString);

	//////////////////////////////// java.time ////////////////////////////////

	public static final AutoCoder<Duration>       DURATION         = stringBased("PrimitiveCoders.DURATION",               Duration::parse,       Duration::toString);
	public static final AutoCoder<Instant>        INSTANT          = stringBased("PrimitiveCoders.INSTANT",                 Instant::parse,        Instant::toString);
	public static final AutoCoder<LocalDate>      LOCAL_DATE       = stringBased("PrimitiveCoders.LOCAL_DATE",            LocalDate::parse,      LocalDate::toString);
	public static final AutoCoder<LocalDateTime>  LOCAL_DATE_TIME  = stringBased("PrimitiveCoders.LOCAL_DATE_TIME",   LocalDateTime::parse,  LocalDateTime::toString);
	public static final AutoCoder<LocalTime>      LOCAL_TIME       = stringBased("PrimitiveCoders.LOCAL_TIME",            LocalTime::parse,      LocalTime::toString);
	public static final AutoCoder<MonthDay>       MONTH_DAY        = stringBased("PrimitiveCoders.MONTH_DAY",              MonthDay::parse,       MonthDay::toString);
	public static final AutoCoder<OffsetDateTime> OFFSET_DATE_TIME = stringBased("PrimitiveCoders.OFFSET_DATE_TIME", OffsetDateTime::parse, OffsetDateTime::toString);
	public static final AutoCoder<OffsetTime>     OFFSET_TIME      = stringBased("PrimitiveCoders.OFFSET_TIME",          OffsetTime::parse,     OffsetTime::toString);
	public static final AutoCoder<Period>         PERIOD           = stringBased("PrimitiveCoders.PERIOD",                   Period::parse,         Period::toString);
	public static final AutoCoder<Year>           YEAR             = stringBased("PrimitiveCoders.YEAR",                       Year::parse,           Year::toString);
	public static final AutoCoder<YearMonth>      YEAR_MONTH       = stringBased("PrimitiveCoders.YEAR_MONTH",            YearMonth::parse,      YearMonth::toString);
	public static final AutoCoder<ZonedDateTime>  ZONED_DATE_TIME  = stringBased("PrimitiveCoders.ZONED_DATE_TIME",   ZonedDateTime::parse,  ZonedDateTime::toString);
	public static final AutoCoder<ZoneId>         ZONE_ID          = stringBased("PrimitiveCoders.ZONE_ID",                  ZoneId::of   ,         ZoneId::toString);
	public static final AutoCoder<ZoneOffset>     ZONE_OFFSET      = stringBased("PrimitiveCoders.ZONE_OFFSET",          ZoneOffset::of   ,     ZoneOffset::toString);

	//////////////////////////////// java.util ////////////////////////////////

	public static final AutoCoder<OptionalInt> OPTIONAL_INT = new NamedCoder<>("PrimitiveCoders.OPTIONAL_INT") {

		@Override
		@OverrideOnly
		public <T_Encoded> @Nullable OptionalInt decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (context.isEmpty()) return OptionalInt.empty();
			return OptionalInt.of(context.forceAsNumber().intValue());
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, OptionalInt> context) throws EncodeException {
			OptionalInt optionalInt = context.object;
			if (optionalInt == null || optionalInt.isEmpty()) {
				return context.empty();
			}
			return context.createInt(optionalInt.getAsInt());
		}
	};
	public static final AutoCoder<OptionalLong> OPTIONAL_LONG = new NamedCoder<>("PrimitiveCoders.OPTIONAL_LONG") {

		@Override
		@OverrideOnly
		public <T_Encoded> @Nullable OptionalLong decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (context.isEmpty()) return OptionalLong.empty();
			return OptionalLong.of(context.forceAsNumber().longValue());
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, OptionalLong> context) throws EncodeException {
			OptionalLong optionalLong = context.object;
			if (optionalLong == null || optionalLong.isEmpty()) {
				return context.empty();
			}
			return context.createLong(optionalLong.getAsLong());
		}
	};
	public static final AutoCoder<OptionalDouble> OPTIONAL_DOUBLE = new NamedCoder<>("PrimitiveCoders.OPTIONAL_DOUBLE") {

		@Override
		@OverrideOnly
		public <T_Encoded> @Nullable OptionalDouble decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (context.isEmpty()) return OptionalDouble.empty();
			return OptionalDouble.of(context.forceAsNumber().doubleValue());
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, OptionalDouble> context) throws EncodeException {
			OptionalDouble optionalDouble = context.object;
			if (optionalDouble == null || optionalDouble.isEmpty()) {
				return context.empty();
			}
			return context.createDouble(optionalDouble.getAsDouble());
		}
	};

	public static <T_Decoded> @NotNull AutoCoder<T_Decoded> stringBased(@NotNull String name, @NotNull Function<@NotNull String, @NotNull T_Decoded> constructor, @NotNull Function<@NotNull T_Decoded, @NotNull String> destructor) {
		return new NamedCoder<>(name) {

			@Override
			@OverrideOnly
			public <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
				if (context.isEmpty()) return null;
				try {
					return constructor.apply(context.forceAsString());
				}
				catch (DecodeException exception) {
					throw exception;
				}
				catch (Exception exception) {
					throw new DecodeException(exception);
				}
			}

			@Override
			@OverrideOnly
			public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException {
				if (context.object == null) return context.empty();
				try {
					return context.createString(destructor.apply(context.object));
				}
				catch (EncodeException exception) {
					throw exception;
				}
				catch (Exception exception) {
					throw new EncodeException(exception);
				}
			}
		};
	}
}
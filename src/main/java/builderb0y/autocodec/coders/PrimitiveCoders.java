package builderb0y.autocodec.coders;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.common.AutoHandler.HandlerMapper;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

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

	public static final AutoCoder<BigInteger> BIG_INTEGER = STRING.mapCoder(ReifiedType.from(BigInteger.class), "BigInteger::toString",      HandlerMapper.nullSafe(BigInteger::toString     ), "BigInteger::new", HandlerMapper.nullSafe(BigInteger::new));
	public static final AutoCoder<BigDecimal> BIG_DECIMAL = STRING.mapCoder(ReifiedType.from(BigDecimal.class), "BigDecimal::toPlainString", HandlerMapper.nullSafe(BigDecimal::toPlainString), "BigDecimal::new", HandlerMapper.nullSafe(BigDecimal::new));

	//////////////////////////////// java.time ////////////////////////////////

	public static final AutoCoder<Duration>       DURATION         = STRING.mapCoder(ReifiedType.from(Duration      .class),       "Duration::toString", HandlerMapper.nullSafe(      Duration::toString),       "Duration::parse", HandlerMapper.nullSafe(      Duration::parse));
	public static final AutoCoder<Instant>        INSTANT          = STRING.mapCoder(ReifiedType.from(Instant       .class),        "Instant::toString", HandlerMapper.nullSafe(       Instant::toString),        "Instant::parse", HandlerMapper.nullSafe(       Instant::parse));
	public static final AutoCoder<LocalDate>      LOCAL_DATE       = STRING.mapCoder(ReifiedType.from(LocalDate     .class),      "LocalDate::toString", HandlerMapper.nullSafe(     LocalDate::toString),      "LocalDate::parse", HandlerMapper.nullSafe(     LocalDate::parse));
	public static final AutoCoder<LocalDateTime>  LOCAL_DATE_TIME  = STRING.mapCoder(ReifiedType.from(LocalDateTime .class),  "LocalDateTime::toString", HandlerMapper.nullSafe( LocalDateTime::toString),  "LocalDateTime::parse", HandlerMapper.nullSafe( LocalDateTime::parse));
	public static final AutoCoder<LocalTime>      LOCAL_TIME       = STRING.mapCoder(ReifiedType.from(LocalTime     .class),      "LocalTime::toString", HandlerMapper.nullSafe(     LocalTime::toString),      "LocalTime::parse", HandlerMapper.nullSafe(     LocalTime::parse));
	public static final AutoCoder<MonthDay>       MONTH_DAY        = STRING.mapCoder(ReifiedType.from(MonthDay      .class),       "MonthDay::toString", HandlerMapper.nullSafe(      MonthDay::toString),       "MonthDay::parse", HandlerMapper.nullSafe(      MonthDay::parse));
	public static final AutoCoder<OffsetDateTime> OFFSET_DATE_TIME = STRING.mapCoder(ReifiedType.from(OffsetDateTime.class), "OffsetDateTime::toString", HandlerMapper.nullSafe(OffsetDateTime::toString), "OffsetDateTime::parse", HandlerMapper.nullSafe(OffsetDateTime::parse));
	public static final AutoCoder<OffsetTime>     OFFSET_TIME      = STRING.mapCoder(ReifiedType.from(OffsetTime    .class),     "OffsetTime::toString", HandlerMapper.nullSafe(    OffsetTime::toString),     "OffsetTime::parse", HandlerMapper.nullSafe(    OffsetTime::parse));
	public static final AutoCoder<Period>         PERIOD           = STRING.mapCoder(ReifiedType.from(Period        .class),         "Period::toString", HandlerMapper.nullSafe(        Period::toString),         "Period::parse", HandlerMapper.nullSafe(        Period::parse));
	public static final AutoCoder<Year>           YEAR             = STRING.mapCoder(ReifiedType.from(Year          .class),           "Year::toString", HandlerMapper.nullSafe(          Year::toString),           "Year::parse", HandlerMapper.nullSafe(          Year::parse));
	public static final AutoCoder<YearMonth>      YEAR_MONTH       = STRING.mapCoder(ReifiedType.from(YearMonth     .class),      "YearMonth::toString", HandlerMapper.nullSafe(     YearMonth::toString),      "YearMonth::parse", HandlerMapper.nullSafe(     YearMonth::parse));
	public static final AutoCoder<ZonedDateTime>  ZONED_DATE_TIME  = STRING.mapCoder(ReifiedType.from(ZonedDateTime .class),  "ZonedDateTime::toString", HandlerMapper.nullSafe( ZonedDateTime::toString),  "ZonedDateTime::parse", HandlerMapper.nullSafe( ZonedDateTime::parse));
	public static final AutoCoder<ZoneId>         ZONE_ID          = STRING.mapCoder(ReifiedType.from(ZoneId        .class),         "ZoneId::toString", HandlerMapper.nullSafe(        ZoneId::toString),         "ZoneId::of",    HandlerMapper.nullSafe(        ZoneId::of   ));
	public static final AutoCoder<ZoneOffset>     ZONE_OFFSET      = STRING.mapCoder(ReifiedType.from(ZoneOffset    .class),     "ZoneOffset::toString", HandlerMapper.nullSafe(    ZoneOffset::toString),     "ZoneOffset::of",    HandlerMapper.nullSafe(    ZoneOffset::of   ));

	//////////////////////////////// java.util ////////////////////////////////

	public static final AutoCoder<OptionalInt>    OPTIONAL_INT    = INT   .mapCoder(ReifiedType.from(OptionalInt   .class),    "OptionalInt::getAsInt",    HandlerMapper.nullSafe((OptionalInt    optional) -> optional.isPresent() ? optional.getAsInt()    : null),    "OptionalInt::of/empty", (Integer i) -> i == null ? OptionalInt   .empty() : OptionalInt   .of(i.intValue()));
	public static final AutoCoder<OptionalLong>   OPTIONAL_LONG   = LONG  .mapCoder(ReifiedType.from(OptionalLong  .class),   "OptionalLong::getAsLong",   HandlerMapper.nullSafe((OptionalLong   optional) -> optional.isPresent() ? optional.getAsLong()   : null),   "OptionalLong::of/empty", (Long    l) -> l == null ? OptionalLong  .empty() : OptionalLong  .of(l.longValue()));
	public static final AutoCoder<OptionalDouble> OPTIONAL_DOUBLE = DOUBLE.mapCoder(ReifiedType.from(OptionalDouble.class), "OptionalDouble::getAsDouble", HandlerMapper.nullSafe((OptionalDouble optional) -> optional.isPresent() ? optional.getAsDouble() : null), "OptionalDouble::of/empty", (Double  d) -> d == null ? OptionalDouble.empty() : OptionalDouble.of(d.doubleValue()));
}
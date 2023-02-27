package builderb0y.autocodec.decoders;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.coders.PrimitiveCoders;
import builderb0y.autocodec.common.LookupFactory;
import builderb0y.autocodec.decoders.AutoDecoder.DecoderFactory;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class LookupDecoderFactory extends LookupFactory<AutoDecoder<?>> implements DecoderFactory {

	@Override
	@OverrideOnly
	public void setup() {
		//java.lang
		this.addRaw(     byte.class, PrimitiveCoders.BYTE);
		this.addRaw(     Byte.class, PrimitiveCoders.BYTE);
		this.addRaw(    short.class, PrimitiveCoders.SHORT);
		this.addRaw(    Short.class, PrimitiveCoders.SHORT);
		this.addRaw(      int.class, PrimitiveCoders.INT);
		this.addRaw(  Integer.class, PrimitiveCoders.INT);
		this.addRaw(     long.class, PrimitiveCoders.LONG);
		this.addRaw(     Long.class, PrimitiveCoders.LONG);
		this.addRaw(    float.class, PrimitiveCoders.FLOAT);
		this.addRaw(    Float.class, PrimitiveCoders.FLOAT);
		this.addRaw(   double.class, PrimitiveCoders.DOUBLE);
		this.addRaw(   Double.class, PrimitiveCoders.DOUBLE);
		this.addRaw(   Number.class, PrimitiveCoders.NUMBER);
		this.addRaw(   String.class, PrimitiveCoders.STRING);
		this.addRaw(     char.class, PrimitiveCoders.CHAR);
		this.addRaw(Character.class, PrimitiveCoders.CHAR);
		this.addRaw(  boolean.class, PrimitiveCoders.BOOLEAN);
		this.addRaw(  Boolean.class, PrimitiveCoders.BOOLEAN);
		//java.math
		this.addRaw(    BigInteger.class, PrimitiveCoders.BIG_INTEGER);
		this.addRaw(    BigDecimal.class, PrimitiveCoders.BIG_DECIMAL);
		//java.time
		this.addRaw(      Duration.class, PrimitiveCoders.DURATION);
		this.addRaw(       Instant.class, PrimitiveCoders.INSTANT);
		this.addRaw(     LocalDate.class, PrimitiveCoders.LOCAL_DATE);
		this.addRaw( LocalDateTime.class, PrimitiveCoders.LOCAL_DATE_TIME);
		this.addRaw(     LocalTime.class, PrimitiveCoders.LOCAL_TIME);
		this.addRaw(      MonthDay.class, PrimitiveCoders.MONTH_DAY);
		this.addRaw(OffsetDateTime.class, PrimitiveCoders.OFFSET_DATE_TIME);
		this.addRaw(    OffsetTime.class, PrimitiveCoders.OFFSET_TIME);
		this.addRaw(        Period.class, PrimitiveCoders.PERIOD);
		this.addRaw(          Year.class, PrimitiveCoders.YEAR);
		this.addRaw(     YearMonth.class, PrimitiveCoders.YEAR_MONTH);
		this.addRaw( ZonedDateTime.class, PrimitiveCoders.ZONED_DATE_TIME);
		this.addRaw(        ZoneId.class, PrimitiveCoders.ZONE_ID);
		this.addRaw(    ZoneOffset.class, PrimitiveCoders.ZONE_OFFSET);
		//java.util
		this.addRaw(OptionalInt   .class, PrimitiveCoders.OPTIONAL_INT);
		this.addRaw(OptionalLong  .class, PrimitiveCoders.OPTIONAL_LONG);
		this.addRaw(OptionalDouble.class, PrimitiveCoders.OPTIONAL_DOUBLE);
	}

	public <T_Decoded> void addGeneric(@NotNull ReifiedType<T_Decoded> type, @NotNull AutoDecoder<T_Decoded> constructor) {
		this.doAddGeneric(type, constructor);
	}

	public <T_Decoded> void addRaw(@NotNull Class<T_Decoded> type, @NotNull AutoDecoder<T_Decoded> constructor) {
		this.doAddRaw(type, constructor);
	}
}
package builderb0y.autocodec.encoders;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.annotations.*;
import builderb0y.autocodec.common.JsonBuilder.JsonObjectBuilder;
import builderb0y.autocodec.common.TestCommon;

import static org.junit.Assert.*;

public class DefaultEncoderTest {

	@Test
	public void test() {
		AutoEncoder<DefaultsNotAlways> notAlways = TestCommon.DEFAULT_CODEC.createEncoder(DefaultsNotAlways.class);
		AutoEncoder<DefaultsAlways> always = TestCommon.DEFAULT_CODEC.createEncoder(DefaultsAlways.class);
		assertEquals(
			TestCommon.DEFAULT_CODEC.encode(
				notAlways,
				new DefaultsNotAlways((byte)(42), (short)(42), 42, 42, 42, 42, "a", 'b', Color.RED, true),
				JsonOps.INSTANCE
			),
			new JsonObject()
		);
		assertEquals(
			TestCommon.DEFAULT_CODEC.encode(
				always,
				new DefaultsAlways((byte)(42), (short)(42), 42, 42, 42, 42, "a", 'b', Color.RED, true),
				JsonOps.INSTANCE
			),
			new JsonObjectBuilder()
			.add("defaultByte", 42)
			.add("defaultShort", 42)
			.add("defaultInt", 42)
			.add("defaultLong", 42)
			.add("defaultFloat", 42)
			.add("defaultDouble", 42)
			.add("defaultString", "a")
			.add("defaultChar", 'b')
			.add("defaultColor", "RED")
			.add("defaultBoolean", true)
			.build()
		);
		assertEquals(
			TestCommon.DEFAULT_CODEC.encode(
				notAlways,
				new DefaultsNotAlways((byte)(123), (short)(123), 123, 123, 123, 123, "z", 'y', Color.GREEN, false),
				JsonOps.INSTANCE
			),
			new JsonObjectBuilder()
			.add("defaultByte", 123)
			.add("defaultShort", 123)
			.add("defaultInt", 123)
			.add("defaultLong", 123)
			.add("defaultFloat", 123)
			.add("defaultDouble", 123)
			.add("defaultString", "z")
			.add("defaultChar", 'y')
			.add("defaultColor", "GREEN")
			.add("defaultBoolean", false)
			.build()
		);
	}

	public static enum Color { RED, GREEN, BLUE }

	public static record DefaultsNotAlways(
		@DefaultByte   (42)    byte    defaultByte,
		@DefaultShort  (42)    short   defaultShort,
		@DefaultInt    (42)    int     defaultInt,
		@DefaultLong   (42)    long    defaultLong,
		@DefaultFloat  (42.0F) float   defaultFloat,
		@DefaultDouble (42.0F) double  defaultDouble,
		@DefaultString ("a")   String  defaultString,
		@DefaultString ("b")   char    defaultChar,
		@DefaultString ("RED") Color   defaultColor,
		@DefaultBoolean(true)  boolean defaultBoolean
	) {}

	public static record DefaultsAlways(
		@DefaultByte   (value = 42,    alwaysEncode = true) byte    defaultByte,
		@DefaultShort  (value = 42,    alwaysEncode = true) short   defaultShort,
		@DefaultInt    (value = 42,    alwaysEncode = true) int     defaultInt,
		@DefaultLong   (value = 42,    alwaysEncode = true) long    defaultLong,
		@DefaultFloat  (value = 42.0F, alwaysEncode = true) float   defaultFloat,
		@DefaultDouble (value = 42.0F, alwaysEncode = true) double  defaultDouble,
		@DefaultString (value = "a",   alwaysEncode = true) String  defaultString,
		@DefaultString (value = "b",   alwaysEncode = true) char    defaultChar,
		@DefaultString (value = "RED", alwaysEncode = true) Color   defaultColor,
		@DefaultBoolean(value = true,  alwaysEncode = true) boolean defaultBoolean
	) {}
}
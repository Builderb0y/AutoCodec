package builderb0y.autocodec.decoders;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.annotations.*;
import builderb0y.autocodec.common.TestCommon;

import static org.junit.Assert.*;

public class DefaultDecoderTest {

	@Test
	public void test() throws DecodeException {
		AutoDecoder<Defaults> decoder = TestCommon.DEFAULT_CODEC.createDecoder(Defaults.class);
		Defaults defaults = TestCommon.DEFAULT_CODEC.decode(decoder, new JsonObject(), JsonOps.INSTANCE);
		assertNotNull(defaults);
		assertEquals(42, defaults.defaultByte);
		assertEquals(42, defaults.defaultShort);
		assertEquals(42, defaults.defaultInt);
		assertEquals(42, defaults.defaultLong);
		assertEquals(42.0F, defaults.defaultFloat, 0.0F);
		assertEquals(42.0F, defaults.defaultDouble, 0.0F);
		assertEquals("a", defaults.defaultString);
		assertEquals('b', defaults.defaultChar);
		assertEquals(Color.RED, defaults.defaultColor);
		assertTrue(defaults.defaultBoolean);
	}

	public static enum Color { RED, GREEN, BLUE }

	public static record Defaults(
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
}
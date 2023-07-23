package builderb0y.autocodec.decoders;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.annotations.*;
import builderb0y.autocodec.annotations.DefaultObject.DefaultObjectMode;
import builderb0y.autocodec.coders.CoderUnitTester;
import builderb0y.autocodec.common.DynamicOpsContext;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.reflection.reification.ReifiedType;

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
		@DefaultDouble (42.0D) double  defaultDouble,
		@DefaultString ("a")   String  defaultString,
		@DefaultString ("b")   char    defaultChar,
		@DefaultString ("RED") Color   defaultColor,
		@DefaultBoolean(true)  boolean defaultBoolean
	) {}

	@Test
	public void testObject() throws DecodeException {
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "DEFAULT",    mode = DefaultObjectMode.FIELD                 ) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "getDefault", mode = DefaultObjectMode.METHOD_WITH_CONTEXT   ) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "getDefault", mode = DefaultObjectMode.METHOD_WITHOUT_CONTEXT) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "DEFAULT",    mode = DefaultObjectMode.FIELD                 ) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "getDefault", mode = DefaultObjectMode.METHOD_WITH_CONTEXT   ) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "getDefault", mode = DefaultObjectMode.METHOD_WITHOUT_CONTEXT) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);

		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "OBJ_DEFAULT",   mode = DefaultObjectMode.FIELD,                  strict = false) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "getObjDefault", mode = DefaultObjectMode.METHOD_WITH_CONTEXT,    strict = false) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "getObjDefault", mode = DefaultObjectMode.METHOD_WITHOUT_CONTEXT, strict = false) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "OBJ_DEFAULT",   mode = DefaultObjectMode.FIELD,                  strict = false) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "getObjDefault", mode = DefaultObjectMode.METHOD_WITH_CONTEXT,    strict = false) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "getObjDefault", mode = DefaultObjectMode.METHOD_WITHOUT_CONTEXT, strict = false) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);

		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<@DefaultObject(name = "new", mode = DefaultObjectMode.METHOD_WITHOUT_CONTEXT) Category>() {}).testEncoded(JsonNull.INSTANCE, JsonOps.INSTANCE);
	}

	public static record Box<T>(T value) {

		public static Box<String> DEFAULT = new Box<>("");

		public static Box<String> getDefault() {
			return DEFAULT;
		}

		public static <T_Encoded> Box<String> getDefault(DynamicOpsContext<T_Encoded> context) {
			return DEFAULT;
		}

		public static Object OBJ_DEFAULT = new Box<>("");

		public static Object getObjDefault() {
			return DEFAULT;
		}

		public static Object getObjDefault(DynamicOpsContext<?> context) {
			return DEFAULT;
		}
	}

	@RecordLike({ "name", "value" })
	public static class Category {

		public final String name;
		public final int value;

		public Category(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public Category() {
			this.name = "default";
			this.value = -1;
		}
	}
}
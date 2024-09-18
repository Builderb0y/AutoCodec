package builderb0y.autocodec.decoders;

import java.util.Map;
import java.util.Objects;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.annotations.*;
import builderb0y.autocodec.annotations.DefaultObject.DefaultObjectMode;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.coders.CoderUnitTester;
import builderb0y.autocodec.common.DynamicOpsContext;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class DefaultDecoderTest {

	@Test
	public void test() throws DecodeException {
		AutoCoder<Defaults> coder = TestCommon.DEFAULT_CODEC.createCoder(Defaults.class);
		Defaults defaults = TestCommon.DEFAULT_CODEC.decode(coder, new JsonObject(), JsonOps.INSTANCE);
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
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "DEFAULT",              mode = DefaultObjectMode.FIELD                                      ) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "getDefaultEncoded",    mode = DefaultObjectMode.METHOD_WITH_CONTEXT_ENCODED                ) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "getDefaultDecoded",    mode = DefaultObjectMode.METHOD_WITH_CONTEXT_DECODED                ) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "getDefault",           mode = DefaultObjectMode.METHOD_WITHOUT_CONTEXT                     ) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);

		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "OBJ_DEFAULT",          mode = DefaultObjectMode.FIELD,                       strict = false) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "getObjDefaultEncoded", mode = DefaultObjectMode.METHOD_WITH_CONTEXT_ENCODED, strict = false) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "getObjDefaultDecoded", mode = DefaultObjectMode.METHOD_WITH_CONTEXT_DECODED, strict = false) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<Box<@DefaultObject(name = "getObjDefault",        mode = DefaultObjectMode.METHOD_WITHOUT_CONTEXT,      strict = false) Box<String>>> () {}).test(new Box<>(Box.DEFAULT), new JsonObject(), JsonOps.INSTANCE);

		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, new ReifiedType<@DefaultObject(name = "new", mode = DefaultObjectMode.METHOD_WITHOUT_CONTEXT) Category>() {}).testEncoded(JsonNull.INSTANCE, JsonOps.INSTANCE);
	}

	public static record Box<T>(T value) {

		public static final Box<String> DEFAULT = new Box<>("");

		public static Box<String> getDefault() {
			return DEFAULT;
		}

		public static <T_Encoded> Box<String> getDefaultDecoded(DynamicOpsContext<T_Encoded> context) {
			return DEFAULT;
		}

		public static <T_Encoded> T_Encoded getDefaultEncoded(DynamicOpsContext<T_Encoded> context) {
			return context.createStringMap(Map.of("value", context.createString("")));
		}

		public static Object OBJ_DEFAULT = new Box<>("");

		public static Object getObjDefault() {
			return DEFAULT;
		}

		public static Object getObjDefaultDecoded(DynamicOpsContext<?> context) {
			return DEFAULT;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static Object getObjDefaultEncoded(DynamicOpsContext context) {
			return context.createStringMap(Map.of("value", context.createString("")));
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

		@Override
		public String toString() {
			return this.getClass().getSimpleName() + ": { name: " + this.name + ", value: " + this.value + " }";
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Category category)) return false;
			return this.value == category.value && Objects.equals(this.name, category.name);
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.name, this.value);
		}
	}
}
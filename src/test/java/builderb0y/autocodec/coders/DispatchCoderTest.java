package builderb0y.autocodec.coders;

import com.mojang.serialization.JsonOps;
import org.junit.Test;

import builderb0y.autocodec.annotations.MemberUsage;
import builderb0y.autocodec.annotations.UseCoder;
import builderb0y.autocodec.coders.KeyDispatchCoder.DispatchCoder;
import builderb0y.autocodec.coders.KeyDispatchCoder.Dispatchable;
import builderb0y.autocodec.common.JsonBuilder;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class DispatchCoderTest {

	@Test
	public void test() throws DecodeException {
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Primitive.class).test(new    BytePrimitive((byte)(42) ), JsonBuilder.object("type", "byte",    "value", (byte)(42) ), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Primitive.class).test(new   ShortPrimitive((short)(42)), JsonBuilder.object("type", "short",   "value", (short)(42)), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Primitive.class).test(new     IntPrimitive(42         ), JsonBuilder.object("type", "int",     "value", 42         ), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Primitive.class).test(new    LongPrimitive(42         ), JsonBuilder.object("type", "long",    "value", 42L        ), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Primitive.class).test(new   FloatPrimitive(42         ), JsonBuilder.object("type", "float",   "value", 42.0F      ), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Primitive.class).test(new  DoublePrimitive(42         ), JsonBuilder.object("type", "double",  "value", 42.0D      ), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Primitive.class).test(new  StringPrimitive("a"        ), JsonBuilder.object("type", "string",  "value", "a"        ), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Primitive.class).test(new BooleanPrimitive(true       ), JsonBuilder.object("type", "boolean", "value", true       ), JsonOps.INSTANCE);
	}

	@UseCoder(name = "CODER", usage = MemberUsage.FIELD_CONTAINS_HANDLER)
	public interface Primitive extends Dispatchable<Primitive> {

		public static final LookupCoder<String, AutoCoder<? extends Primitive>> LOOKUP = new LookupCoder<>(new ReifiedType<>() {}, PrimitiveCoders.STRING);
		public static final AutoCoder<Primitive> CODER = new DispatchCoder<>(ReifiedType.from(Primitive.class), LOOKUP);
		public static final Object INITIALIZER = new Object() {{
			LOOKUP.add("byte",    TestCommon.DEFAULT_CODEC.createCoder(   BytePrimitive.class));
			LOOKUP.add("short",   TestCommon.DEFAULT_CODEC.createCoder(  ShortPrimitive.class));
			LOOKUP.add("int",     TestCommon.DEFAULT_CODEC.createCoder(    IntPrimitive.class));
			LOOKUP.add("long",    TestCommon.DEFAULT_CODEC.createCoder(   LongPrimitive.class));
			LOOKUP.add("float",   TestCommon.DEFAULT_CODEC.createCoder(  FloatPrimitive.class));
			LOOKUP.add("double",  TestCommon.DEFAULT_CODEC.createCoder( DoublePrimitive.class));
			LOOKUP.add("string",  TestCommon.DEFAULT_CODEC.createCoder( StringPrimitive.class));
			LOOKUP.add("boolean", TestCommon.DEFAULT_CODEC.createCoder(BooleanPrimitive.class));
		}};

		@Override
		public default AutoCoder<? extends Primitive> getCoder() {
			return TestCommon.DEFAULT_CODEC.createCoder(this.getClass());
		}
	}

	public static record    BytePrimitive(byte    value) implements Primitive {}
	public static record   ShortPrimitive(short   value) implements Primitive {}
	public static record     IntPrimitive(int     value) implements Primitive {}
	public static record    LongPrimitive(long    value) implements Primitive {}
	public static record   FloatPrimitive(float   value) implements Primitive {}
	public static record  DoublePrimitive(double  value) implements Primitive {}
	public static record  StringPrimitive(String  value) implements Primitive {}
	public static record BooleanPrimitive(boolean value) implements Primitive {}
}
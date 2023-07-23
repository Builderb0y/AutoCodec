package builderb0y.autocodec.coders;

import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import builderb0y.autocodec.annotations.MemberUsage;
import builderb0y.autocodec.annotations.UseCoder;
import builderb0y.autocodec.common.JsonBuilder;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

public class KeyDispatchCoderTest {

	@Test
	public void test() throws DecodeException {
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Primitive.class).test(new    BytePrimitive((byte)(42) ), JsonBuilder.object("type", "BYTE",    "value", (byte)(42) ), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Primitive.class).test(new   ShortPrimitive((short)(42)), JsonBuilder.object("type", "SHORT",   "value", (short)(42)), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Primitive.class).test(new     IntPrimitive(42         ), JsonBuilder.object("type", "INT",     "value", 42         ), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Primitive.class).test(new    LongPrimitive(42         ), JsonBuilder.object("type", "LONG",    "value", 42L        ), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Primitive.class).test(new   FloatPrimitive(42         ), JsonBuilder.object("type", "FLOAT",   "value", 42.0F      ), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Primitive.class).test(new  DoublePrimitive(42         ), JsonBuilder.object("type", "DOUBLE",  "value", 42.0D      ), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Primitive.class).test(new  StringPrimitive("a"        ), JsonBuilder.object("type", "STRING",  "value", "a"        ), JsonOps.INSTANCE);
		new CoderUnitTester<>(TestCommon.DEFAULT_CODEC, Primitive.class).test(new BooleanPrimitive(true       ), JsonBuilder.object("type", "BOOLEAN", "value", true       ), JsonOps.INSTANCE);
	}

	public static enum PrimitiveType {
		BYTE   (   BytePrimitive.class),
		SHORT  (  ShortPrimitive.class),
		INT    (    IntPrimitive.class),
		LONG   (   LongPrimitive.class),
		FLOAT  (  FloatPrimitive.class),
		DOUBLE ( DoublePrimitive.class),
		BOOLEAN(BooleanPrimitive.class),
		STRING ( StringPrimitive.class);

		public final AutoCoder<? extends Primitive> coder;

		PrimitiveType(Class<? extends Primitive> clazz) {
			this.coder = TestCommon.DEFAULT_CODEC.createCoder(clazz);
		}
	}

	@UseCoder(name = "CODER", usage = MemberUsage.FIELD_CONTAINS_HANDLER)
	public static interface Primitive {

		public static final KeyDispatchCoder<PrimitiveType, Primitive> CODER = new KeyDispatchCoder<>(
			ReifiedType.from(Primitive.class),
			TestCommon.DEFAULT_CODEC.createCoder(PrimitiveType.class)
		) {

			@Override
			public @Nullable AutoCoder<? extends Primitive> getCoder(@NotNull PrimitiveType type) {
				return type.coder;
			}

			@Override
			public @Nullable PrimitiveType getKey(@NotNull Primitive object) {
				return object.getType();
			}
		};

		public abstract Object asObject();

		public abstract PrimitiveType getType();
	}

	public static record BytePrimitive(byte value) implements Primitive {

		@Override
		public Object asObject() {
			return this.value;
		}

		@Override
		public PrimitiveType getType() {
			return PrimitiveType.BYTE;
		}
	}

	public static record ShortPrimitive(short value) implements Primitive {

		@Override
		public Object asObject() {
			return this.value;
		}

		@Override
		public PrimitiveType getType() {
			return PrimitiveType.SHORT;
		}
	}

	public static record IntPrimitive(int value) implements Primitive {

		@Override
		public Object asObject() {
			return this.value;
		}

		@Override
		public PrimitiveType getType() {
			return PrimitiveType.INT;
		}
	}

	public static record LongPrimitive(long value) implements Primitive {

		@Override
		public Object asObject() {
			return this.value;
		}

		@Override
		public PrimitiveType getType() {
			return PrimitiveType.LONG;
		}
	}

	public static record FloatPrimitive(float value) implements Primitive {

		@Override
		public Object asObject() {
			return this.value;
		}

		@Override
		public PrimitiveType getType() {
			return PrimitiveType.FLOAT;
		}
	}

	public static record DoublePrimitive(double value) implements Primitive {

		@Override
		public Object asObject() {
			return this.value;
		}

		@Override
		public PrimitiveType getType() {
			return PrimitiveType.DOUBLE;
		}
	}

	public static record BooleanPrimitive(boolean value) implements Primitive {

		@Override
		public Object asObject() {
			return this.value;
		}

		@Override
		public PrimitiveType getType() {
			return PrimitiveType.BOOLEAN;
		}
	}

	public static record StringPrimitive(String value) implements Primitive {

		@Override
		public Object asObject() {
			return this.value;
		}

		@Override
		public PrimitiveType getType() {
			return PrimitiveType.STRING;
		}
	}
}
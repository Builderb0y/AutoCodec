package builderb0y.autocodec.encoders;

import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import builderb0y.autocodec.annotations.MemberUsage;
import builderb0y.autocodec.annotations.UseEncoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.encoders.AutoEncoder.EncoderFactory;
import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class UseEncoderTest {

	public static int expectedSuccesses, actualSuccesses;

	@Test
	public void test() {
		this.test(new ReifiedType<@UseEncoder(name = "ENCODER",    usage = MemberUsage.FIELD_CONTAINS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseEncoder(name = "FACTORY",    usage = MemberUsage.FIELD_CONTAINS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseEncoder(name = "encoder",    usage = MemberUsage.METHOD_RETURNS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseEncoder(name = "getFactory", usage = MemberUsage.METHOD_RETURNS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseEncoder(name = "encode",     usage = MemberUsage.METHOD_IS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseEncoder(name = "isFactory",  usage = MemberUsage.METHOD_IS_FACTORY) Empty>() {}, true);

		this.test(new ReifiedType<@UseEncoder(name = "WILDCARD_ENCODER",   usage = MemberUsage.FIELD_CONTAINS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseEncoder(name = "WILDCARD_FACTORY",   usage = MemberUsage.FIELD_CONTAINS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseEncoder(name = "wildcardEncoder",    usage = MemberUsage.METHOD_RETURNS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseEncoder(name = "getWildcardFactory", usage = MemberUsage.METHOD_RETURNS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseEncoder(name = "wildcardEncode",     usage = MemberUsage.METHOD_IS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseEncoder(name = "isWildcardFactory",  usage = MemberUsage.METHOD_IS_FACTORY) Empty>() {}, true);

		this.test(new ReifiedType<@UseEncoder(name = "WILDCARD_ENCODER",   usage = MemberUsage.FIELD_CONTAINS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseEncoder(name = "WILDCARD_FACTORY",   usage = MemberUsage.FIELD_CONTAINS_FACTORY, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseEncoder(name = "wildcardEncoder",    usage = MemberUsage.METHOD_RETURNS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseEncoder(name = "getWildcardFactory", usage = MemberUsage.METHOD_RETURNS_FACTORY, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseEncoder(name = "wildcardEncode",     usage = MemberUsage.METHOD_IS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseEncoder(name = "isWildcardFactory",  usage = MemberUsage.METHOD_IS_FACTORY, strict = false) Empty>() {}, true);

		assertEquals(expectedSuccesses, actualSuccesses);
	}

	public void test(ReifiedType<Empty> type, boolean valid) {
		if (valid) {
			expectedSuccesses++;
			TestCommon.DEFAULT_CODEC.encode(TestCommon.DEFAULT_CODEC.createEncoder(type), new Empty(), JsonOps.INSTANCE);
		}
		else try {
			TestCommon.DISABLED_CODEC.encode(TestCommon.DISABLED_CODEC.createEncoder(type), new Empty(), JsonOps.INSTANCE);
			fail();
		}
		catch (FactoryException expected) {}
	}

	public static class Empty {

		public static final AutoEncoder<Empty> ENCODER = new AutoEncoder<>() {

			@Override
			@OverrideOnly
			public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, Empty> context) throws EncodeException {
				actualSuccesses++;
				return context.empty();
			}
		};

		public static final EncoderFactory FACTORY = new EncoderFactory() {

			@Override
			public <T_HandledType> AutoEncoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
				return ENCODER;
			}
		};

		public static AutoEncoder<Empty> encoder() {
			return ENCODER;
		}

		public static EncoderFactory getFactory() {
			return FACTORY;
		}

		public static <T_Encoded> T_Encoded encode(EncodeContext<T_Encoded, Empty> context) {
			return context.encodeWith(ENCODER);
		}

		public static <T_HandledType> AutoEncoder<?> isFactory(FactoryContext<T_HandledType> context) {
			return ENCODER;
		}

		public static final AutoEncoder<?> WILDCARD_ENCODER = new AutoEncoder<>() {

			@Override
			@OverrideOnly
			public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, Object> context) throws EncodeException {
				actualSuccesses++;
				return context.empty();
			}
		};

		public static final EncoderFactory WILDCARD_FACTORY = new EncoderFactory() {

			@Override
			public <T_HandledType> AutoEncoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
				return WILDCARD_ENCODER;
			}
		};

		public static AutoEncoder<?> wildcardEncoder() {
			return WILDCARD_ENCODER;
		}

		public static EncoderFactory getWildcardFactory() {
			return WILDCARD_FACTORY;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static Object wildcardEncode(EncodeContext<?, ?> context) {
			return ((EncodeContext)(context)).encodeWith(ENCODER);
		}

		public static AutoEncoder<?> isWildcardFactory(FactoryContext<?> context) {
			return WILDCARD_ENCODER;
		}
	}
}
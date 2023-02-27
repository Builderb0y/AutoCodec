package builderb0y.autocodec.decoders;

import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import builderb0y.autocodec.annotations.MemberUsage;
import builderb0y.autocodec.annotations.UseDecoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.decoders.AutoDecoder.DecoderFactory;
import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class UseDecoderTest {

	public static int expectedSuccesses, actualSuccesses;

	@Test
	public void test() throws DecodeException {
		this.test(new ReifiedType<@UseDecoder(name = "DECODER",    usage = MemberUsage.FIELD_CONTAINS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseDecoder(name = "FACTORY",    usage = MemberUsage.FIELD_CONTAINS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseDecoder(name = "decoder",    usage = MemberUsage.METHOD_RETURNS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseDecoder(name = "getFactory", usage = MemberUsage.METHOD_RETURNS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseDecoder(name = "decode",     usage = MemberUsage.METHOD_IS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseDecoder(name = "isFactory",  usage = MemberUsage.METHOD_IS_FACTORY) Empty>() {}, true);

		this.test(new ReifiedType<@UseDecoder(name = "WILDCARD_DECODER",   usage = MemberUsage.FIELD_CONTAINS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseDecoder(name = "WILDCARD_FACTORY",   usage = MemberUsage.FIELD_CONTAINS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseDecoder(name = "wildcardDecoder",    usage = MemberUsage.METHOD_RETURNS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseDecoder(name = "getWildcardFactory", usage = MemberUsage.METHOD_RETURNS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseDecoder(name = "wildcardDecode",     usage = MemberUsage.METHOD_IS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseDecoder(name = "isWildcardFactory",  usage = MemberUsage.METHOD_IS_FACTORY) Empty>() {}, true);

		this.test(new ReifiedType<@UseDecoder(name = "WILDCARD_DECODER",   usage = MemberUsage.FIELD_CONTAINS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseDecoder(name = "WILDCARD_FACTORY",   usage = MemberUsage.FIELD_CONTAINS_FACTORY, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseDecoder(name = "wildcardDecoder",    usage = MemberUsage.METHOD_RETURNS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseDecoder(name = "getWildcardFactory", usage = MemberUsage.METHOD_RETURNS_FACTORY, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseDecoder(name = "wildcardDecode",     usage = MemberUsage.METHOD_IS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseDecoder(name = "isWildcardFactory",  usage = MemberUsage.METHOD_IS_FACTORY, strict = false) Empty>() {}, true);

		assertEquals(expectedSuccesses, actualSuccesses);
	}

	public void test(ReifiedType<Empty> type, boolean valid) throws DecodeException {
		if (valid) {
			expectedSuccesses++;
			TestCommon.DEFAULT_CODEC.decode(TestCommon.DEFAULT_CODEC.createDecoder(type), JsonNull.INSTANCE, JsonOps.INSTANCE);
		}
		else try {
			TestCommon.DISABLED_CODEC.decode(TestCommon.DISABLED_CODEC.createDecoder(type), JsonNull.INSTANCE, JsonOps.INSTANCE);
			fail();
		}
		catch (FactoryException expected) {}
	}

	public static class Empty {

		public static final AutoDecoder<Empty> DECODER = new AutoDecoder<>() {

			@Override
			@OverrideOnly
			public <T_Encoded> Empty decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
				actualSuccesses++;
				return new Empty();
			}
		};

		public static final DecoderFactory FACTORY = new DecoderFactory() {

			@Override
			public <T_HandledType> AutoDecoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
				return DECODER;
			}
		};

		public static AutoDecoder<Empty> decoder() {
			return DECODER;
		}

		public static DecoderFactory getFactory() {
			return FACTORY;
		}

		public static <T_Encoded> Empty decode(DecodeContext<T_Encoded> context) throws DecodeException {
			return context.decodeWith(DECODER);
		}

		public static <T_HandledType> AutoDecoder<?> isFactory(FactoryContext<T_HandledType> context) {
			return DECODER;
		}

		public static final AutoDecoder<?> WILDCARD_DECODER = new AutoDecoder<>() {

			@Override
			public <T_Encoded> Object decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
				actualSuccesses++;
				return new Empty();
			}
		};

		public static final DecoderFactory WILDCARD_FACTORY = new DecoderFactory() {

			@Override
			public <T_HandledType> AutoDecoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
				return WILDCARD_DECODER;
			}
		};

		public static AutoDecoder<?> wildcardDecoder() {
			return WILDCARD_DECODER;
		}

		public static DecoderFactory getWildcardFactory() {
			return WILDCARD_FACTORY;
		}

		public static Object wildcardDecode(DecodeContext<?> context) throws DecodeException {
			return context.decodeWith(WILDCARD_DECODER);
		}

		public static AutoDecoder<?> isWildcardFactory(FactoryContext<?> context) {
			return WILDCARD_DECODER;
		}
	}
}
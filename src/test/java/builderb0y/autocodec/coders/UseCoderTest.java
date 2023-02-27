package builderb0y.autocodec.coders;

import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import builderb0y.autocodec.annotations.MemberUsage;
import builderb0y.autocodec.annotations.UseCoder;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.common.AutoHandler.AutoFactory;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class UseCoderTest {

	public static int expectedSuccesses, actualSuccesses;

	@Test
	public void test() throws DecodeException {
		this.test(new ReifiedType<@UseCoder(name = "CODER",      usage = MemberUsage.FIELD_CONTAINS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseCoder(name = "FACTORY",    usage = MemberUsage.FIELD_CONTAINS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseCoder(name = "coder",      usage = MemberUsage.METHOD_RETURNS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseCoder(name = "getFactory", usage = MemberUsage.METHOD_RETURNS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseCoder(name = "code",       usage = MemberUsage.METHOD_IS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseCoder(name = "isFactory",  usage = MemberUsage.METHOD_IS_FACTORY) Empty>() {}, true);

		this.test(new ReifiedType<@UseCoder(name = "WILDCARD_CODER",     usage = MemberUsage.FIELD_CONTAINS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseCoder(name = "WILDCARD_FACTORY",   usage = MemberUsage.FIELD_CONTAINS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseCoder(name = "wildcardCoder",      usage = MemberUsage.METHOD_RETURNS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseCoder(name = "getWildcardFactory", usage = MemberUsage.METHOD_RETURNS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseCoder(name = "wildcardCode",       usage = MemberUsage.METHOD_IS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseCoder(name = "isWildcardFactory",  usage = MemberUsage.METHOD_IS_FACTORY) Empty>() {}, true);

		this.test(new ReifiedType<@UseCoder(name = "WILDCARD_CODER",     usage = MemberUsage.FIELD_CONTAINS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseCoder(name = "WILDCARD_FACTORY",   usage = MemberUsage.FIELD_CONTAINS_FACTORY, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseCoder(name = "wildcardCoder",      usage = MemberUsage.METHOD_RETURNS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseCoder(name = "getWildcardFactory", usage = MemberUsage.METHOD_RETURNS_FACTORY, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseCoder(name = "wildcardCode",       usage = MemberUsage.METHOD_IS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseCoder(name = "isWildcardFactory",  usage = MemberUsage.METHOD_IS_FACTORY, strict = false) Empty>() {}, true);

		assertEquals(expectedSuccesses, actualSuccesses);
	}

	public void test(ReifiedType<Empty> type, boolean valid) throws DecodeException {
		AutoCoder<Empty> coder;
		if (valid) {
			expectedSuccesses += 2;
			coder = TestCommon.DEFAULT_CODEC.createCoder(type);
		}
		else try {
			coder = TestCommon.DISABLED_CODEC.createCoder(type);
			fail();
		}
		catch (FactoryException expected) {
			return;
		}
		TestCommon.DEFAULT_CODEC.encode(coder, new Empty(), JsonOps.INSTANCE);
		TestCommon.DEFAULT_CODEC.decode(coder, JsonNull.INSTANCE, JsonOps.INSTANCE);
	}

	public static class Empty {

		public static final AutoCoder<Empty> CODER = new AutoCoder<>() {

			@Override
			@OverrideOnly
			public <T_Encoded> T_Encoded encode(@NotNull EncodeContext<T_Encoded, Empty> context) throws EncodeException {
				actualSuccesses++;
				return context.empty();
			}

			@Override
			public <T_Encoded> Empty decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
				actualSuccesses++;
				return new Empty();
			}
		};

		public static final AutoFactory<AutoCoder<Empty>> FACTORY = new AutoFactory<>() {

			@Override
			public <T_HandledType> AutoCoder<Empty> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
				return CODER;
			}
		};

		public static AutoCoder<Empty> coder() {
			return CODER;
		}

		public static AutoFactory<AutoCoder<Empty>> getFactory() {
			return FACTORY;
		}

		public static <T_Encoded> T_Encoded code(EncodeContext<T_Encoded, Empty> context) {
			return context.encodeWith(CODER);
		}

		public static <T_Encoded> Empty code(DecodeContext<T_Encoded> context) throws DecodeException {
			return context.decodeWith(CODER);
		}

		public static <T_HandledType> AutoCoder<?> isFactory(FactoryContext<T_HandledType> context) {
			return CODER;
		}

		public static final AutoCoder<?> WILDCARD_CODER = new AutoCoder<>() {

			@Override
			@OverrideOnly
			public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, Object> context) throws EncodeException {
				actualSuccesses++;
				return context.empty();
			}

			@Override
			public <T_Encoded> @Nullable Object decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
				actualSuccesses++;
				return new Empty();
			}
		};

		public static final AutoFactory<AutoCoder<?>> WILDCARD_FACTORY = new AutoFactory<>() {

			@Override
			public <T_HandledType> AutoCoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
				return WILDCARD_CODER;
			}
		};

		public static AutoCoder<?> wildcardCoder() {
			return WILDCARD_CODER;
		}

		public static AutoFactory<AutoCoder<?>> getWildcardFactory() {
			return WILDCARD_FACTORY;
		}

		public static <T_Encoded> Object wildcardCode(EncodeContext<T_Encoded, ?> context) {
			actualSuccesses++;
			return context.empty();
		}

		public static <T_Encoded> Object wildcardCode(DecodeContext<T_Encoded> context) throws DecodeException {
			actualSuccesses++;
			return new Empty();
		}

		public static AutoCoder<?> isWildcardFactory(FactoryContext<?> context) {
			return WILDCARD_CODER;
		}
	}
}
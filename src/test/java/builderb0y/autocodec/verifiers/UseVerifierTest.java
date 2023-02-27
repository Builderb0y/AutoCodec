package builderb0y.autocodec.verifiers;

import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import builderb0y.autocodec.annotations.MemberUsage;
import builderb0y.autocodec.annotations.UseVerifier;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.verifiers.AutoVerifier.VerifierFactory;

import static org.junit.Assert.*;

public class UseVerifierTest {

	public static int expectedSuccesses, actualSuccesses;

	@Test
	public void test() throws DecodeException {
		this.test(new ReifiedType<@UseVerifier(name = "VERIFIER",  usage = MemberUsage.FIELD_CONTAINS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseVerifier(name = "FACTORY",    usage = MemberUsage.FIELD_CONTAINS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseVerifier(name = "verifier",  usage = MemberUsage.METHOD_RETURNS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseVerifier(name = "getFactory", usage = MemberUsage.METHOD_RETURNS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseVerifier(name = "verify",    usage = MemberUsage.METHOD_IS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseVerifier(name = "isFactory",  usage = MemberUsage.METHOD_IS_FACTORY) Empty>() {}, true);

		this.test(new ReifiedType<@UseVerifier(name = "WILDCARD_VERIFIER", usage = MemberUsage.FIELD_CONTAINS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseVerifier(name = "WILDCARD_FACTORY",   usage = MemberUsage.FIELD_CONTAINS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseVerifier(name = "wildcardVerifier",  usage = MemberUsage.METHOD_RETURNS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseVerifier(name = "getWildcardFactory", usage = MemberUsage.METHOD_RETURNS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseVerifier(name = "wildcardVerify",    usage = MemberUsage.METHOD_IS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseVerifier(name = "isWildcardFactory",  usage = MemberUsage.METHOD_IS_FACTORY) Empty>() {}, true);

		this.test(new ReifiedType<@UseVerifier(name = "WILDCARD_VERIFIER", usage = MemberUsage.FIELD_CONTAINS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseVerifier(name = "WILDCARD_FACTORY",   usage = MemberUsage.FIELD_CONTAINS_FACTORY, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseVerifier(name = "wildcardVerifier",  usage = MemberUsage.METHOD_RETURNS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseVerifier(name = "getWildcardFactory", usage = MemberUsage.METHOD_RETURNS_FACTORY, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseVerifier(name = "wildcardVerify",    usage = MemberUsage.METHOD_IS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseVerifier(name = "isWildcardFactory",  usage = MemberUsage.METHOD_IS_FACTORY, strict = false) Empty>() {}, true);

		assertEquals(expectedSuccesses, actualSuccesses);
	}

	public void test(ReifiedType<Empty> type, boolean valid) throws DecodeException {
		if (valid) {
			expectedSuccesses++;
			TestCommon.DEFAULT_CODEC.verify(TestCommon.DEFAULT_CODEC.createVerifier(type), new Empty(), JsonNull.INSTANCE, JsonOps.INSTANCE);
		}
		else try {
			TestCommon.DISABLED_CODEC.verify(TestCommon.DISABLED_CODEC.createVerifier(type), new Empty(), JsonNull.INSTANCE, JsonOps.INSTANCE);
			fail();
		}
		catch (FactoryException expected) {}
	}

	public static class Empty {

		public static final AutoVerifier<Empty> VERIFIER = new AutoVerifier<>() {

			@Override
			@OverrideOnly
			public <T_Encoded> void verify(@NotNull VerifyContext<T_Encoded, Empty> context) throws VerifyException {
				actualSuccesses++;
			}
		};

		public static final VerifierFactory FACTORY = new VerifierFactory() {

			@Override
			public <T_HandledType> AutoVerifier<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
				return VERIFIER;
			}
		};

		public static AutoVerifier<Empty> verifier() {
			return VERIFIER;
		}

		public static VerifierFactory getFactory() {
			return FACTORY;
		}

		public static <T_Encoded> void verify(VerifyContext<T_Encoded, Empty> context) throws VerifyException {
			context.verifyWith(VERIFIER);
		}

		public static <T_HandledType> AutoVerifier<?> isFactory(FactoryContext<T_HandledType> context) {
			return VERIFIER;
		}

		public static final AutoVerifier<?> WILDCARD_VERIFIER = new AutoVerifier<>() {

			@Override
			@OverrideOnly
			public <T_Encoded> void verify(@NotNull VerifyContext<T_Encoded, Object> context) throws VerifyException {
				actualSuccesses++;
			}
		};

		public static final VerifierFactory WILDCARD_FACTORY = new VerifierFactory() {

			@Override
			public <T_HandledType> AutoVerifier<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
				return WILDCARD_VERIFIER;
			}
		};

		public static AutoVerifier<?> wildcardVerifier() {
			return WILDCARD_VERIFIER;
		}

		public static VerifierFactory getWildcardFactory() {
			return WILDCARD_FACTORY;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static void wildcardVerify(VerifyContext<?, ?> context) throws VerifyException {
			((VerifyContext)(context)).verifyWith(WILDCARD_VERIFIER);
		}

		public static AutoVerifier<?> isWildcardFactory(FactoryContext<?> context) {
			return WILDCARD_VERIFIER;
		}
	}
}
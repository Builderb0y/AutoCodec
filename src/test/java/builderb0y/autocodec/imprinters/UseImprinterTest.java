package builderb0y.autocodec.imprinters;

import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import builderb0y.autocodec.annotations.MemberUsage;
import builderb0y.autocodec.annotations.UseImprinter;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.imprinters.AutoImprinter.ImprinterFactory;
import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class UseImprinterTest {

	public static int expectedSuccesses, actualSuccesses;

	@Test
	public void test() throws DecodeException {
		this.test(new ReifiedType<@UseImprinter(name = "IMPRINTER",  usage = MemberUsage.FIELD_CONTAINS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseImprinter(name = "FACTORY",    usage = MemberUsage.FIELD_CONTAINS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseImprinter(name = "imprinter",  usage = MemberUsage.METHOD_RETURNS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseImprinter(name = "getFactory", usage = MemberUsage.METHOD_RETURNS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseImprinter(name = "imprint",    usage = MemberUsage.METHOD_IS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseImprinter(name = "isFactory",  usage = MemberUsage.METHOD_IS_FACTORY) Empty>() {}, true);

		this.test(new ReifiedType<@UseImprinter(name = "WILDCARD_IMPRINTER", usage = MemberUsage.FIELD_CONTAINS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseImprinter(name = "WILDCARD_FACTORY",   usage = MemberUsage.FIELD_CONTAINS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseImprinter(name = "wildcardImprinter",  usage = MemberUsage.METHOD_RETURNS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseImprinter(name = "getWildcardFactory", usage = MemberUsage.METHOD_RETURNS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseImprinter(name = "wildcardImprint",    usage = MemberUsage.METHOD_IS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseImprinter(name = "isWildcardFactory",  usage = MemberUsage.METHOD_IS_FACTORY) Empty>() {}, true);

		this.test(new ReifiedType<@UseImprinter(name = "WILDCARD_IMPRINTER", usage = MemberUsage.FIELD_CONTAINS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseImprinter(name = "WILDCARD_FACTORY",   usage = MemberUsage.FIELD_CONTAINS_FACTORY, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseImprinter(name = "wildcardImprinter",  usage = MemberUsage.METHOD_RETURNS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseImprinter(name = "getWildcardFactory", usage = MemberUsage.METHOD_RETURNS_FACTORY, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseImprinter(name = "wildcardImprint",    usage = MemberUsage.METHOD_IS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseImprinter(name = "isWildcardFactory",  usage = MemberUsage.METHOD_IS_FACTORY, strict = false) Empty>() {}, true);

		assertEquals(expectedSuccesses, actualSuccesses);
	}

	public void test(ReifiedType<Empty> type, boolean valid) throws DecodeException {
		if (valid) {
			expectedSuccesses++;
			TestCommon.DEFAULT_CODEC.imprint(TestCommon.DEFAULT_CODEC.createImprinter(type), new Empty(), JsonNull.INSTANCE, JsonOps.INSTANCE);
		}
		else try {
			TestCommon.DISABLED_CODEC.imprint(TestCommon.DISABLED_CODEC.createImprinter(type), new Empty(), JsonNull.INSTANCE, JsonOps.INSTANCE);
			fail();
		}
		catch (FactoryException expected) {}
	}

	public static class Empty {

		public static final AutoImprinter<Empty> IMPRINTER = new AutoImprinter<>() {

			@Override
			@OverrideOnly
			public <T_Encoded> void imprint(@NotNull ImprintContext<T_Encoded, Empty> context) throws ImprintException {
				actualSuccesses++;
			}
		};

		public static final ImprinterFactory FACTORY = new ImprinterFactory() {

			@Override
			public <T_HandledType> AutoImprinter<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
				return IMPRINTER;
			}
		};

		public static AutoImprinter<Empty> imprinter() {
			return IMPRINTER;
		}

		public static ImprinterFactory getFactory() {
			return FACTORY;
		}

		public static <T_Encoded> void imprint(ImprintContext<T_Encoded, Empty> context) throws ImprintException {
			context.imprintWith(IMPRINTER);
		}

		public static <T_HandledType> AutoImprinter<?> isFactory(FactoryContext<T_HandledType> context) {
			return IMPRINTER;
		}

		public static final AutoImprinter<?> WILDCARD_IMPRINTER = new AutoImprinter<>() {

			@Override
			@OverrideOnly
			public <T_Encoded> void imprint(@NotNull ImprintContext<T_Encoded, Object> context) throws ImprintException {
				actualSuccesses++;
			}
		};

		public static final ImprinterFactory WILDCARD_FACTORY = new ImprinterFactory() {

			@Override
			public <T_HandledType> AutoImprinter<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
				return WILDCARD_IMPRINTER;
			}
		};

		public static AutoImprinter<?> wildcardImprinter() {
			return WILDCARD_IMPRINTER;
		}

		public static ImprinterFactory getWildcardFactory() {
			return WILDCARD_FACTORY;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static <T_Encoded> void wildcardImprint(ImprintContext<T_Encoded, ?> context) throws ImprintException {
			((ImprintContext)(context)).imprintWith(WILDCARD_IMPRINTER);
		}

		public static AutoImprinter<?> isWildcardFactory(FactoryContext<?> context) {
			return WILDCARD_IMPRINTER;
		}
	}
}
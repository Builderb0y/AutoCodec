package builderb0y.autocodec.constructors;

import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import builderb0y.autocodec.annotations.MemberUsage;
import builderb0y.autocodec.annotations.UseConstructor;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.constructors.AutoConstructor.ConstructorFactory;
import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class UseConstructorTest {

	public static int expectedSuccesses, actualSuccesses;

	@Test
	public void test() throws ConstructException {
		this.test(new ReifiedType<@UseConstructor(name = "CONSTRUCTOR", usage = MemberUsage.FIELD_CONTAINS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseConstructor(name = "FACTORY",     usage = MemberUsage.FIELD_CONTAINS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseConstructor(name = "constructor", usage = MemberUsage.METHOD_RETURNS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseConstructor(name = "getFactory",  usage = MemberUsage.METHOD_RETURNS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseConstructor(name = "construct",   usage = MemberUsage.METHOD_IS_HANDLER) Empty>() {}, true);
		this.test(new ReifiedType<@UseConstructor(name = "isFactory",   usage = MemberUsage.METHOD_IS_FACTORY) Empty>() {}, true);

		this.test(new ReifiedType<@UseConstructor(name = "WILDCARD_CONSTRUCTOR", usage = MemberUsage.FIELD_CONTAINS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseConstructor(name = "WILDCARD_FACTORY",     usage = MemberUsage.FIELD_CONTAINS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseConstructor(name = "wildcardConstructor",  usage = MemberUsage.METHOD_RETURNS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseConstructor(name = "getWildcardFactory",   usage = MemberUsage.METHOD_RETURNS_FACTORY) Empty>() {}, true);
		this.test(new ReifiedType<@UseConstructor(name = "wildcardConstruct",    usage = MemberUsage.METHOD_IS_HANDLER) Empty>() {}, false);
		this.test(new ReifiedType<@UseConstructor(name = "isWildcardFactory",    usage = MemberUsage.METHOD_IS_FACTORY) Empty>() {}, true);

		this.test(new ReifiedType<@UseConstructor(name = "WILDCARD_CONSTRUCTOR", usage = MemberUsage.FIELD_CONTAINS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseConstructor(name = "WILDCARD_FACTORY",     usage = MemberUsage.FIELD_CONTAINS_FACTORY, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseConstructor(name = "wildcardConstructor",  usage = MemberUsage.METHOD_RETURNS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseConstructor(name = "getWildcardFactory",   usage = MemberUsage.METHOD_RETURNS_FACTORY, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseConstructor(name = "wildcardConstruct",    usage = MemberUsage.METHOD_IS_HANDLER, strict = false) Empty>() {}, true);
		this.test(new ReifiedType<@UseConstructor(name = "isWildcardFactory",    usage = MemberUsage.METHOD_IS_FACTORY, strict = false) Empty>() {}, true);

		assertEquals(expectedSuccesses, actualSuccesses);
	}

	public void test(ReifiedType<Empty> type, boolean valid) throws ConstructException {
		if (valid) {
			expectedSuccesses++;
			TestCommon.DEFAULT_CODEC.construct(TestCommon.DEFAULT_CODEC.createConstructor(type), JsonNull.INSTANCE, JsonOps.INSTANCE);
		}
		else try {
			TestCommon.DISABLED_CODEC.construct(TestCommon.DISABLED_CODEC.createConstructor(type), JsonNull.INSTANCE, JsonOps.INSTANCE);
			fail();
		}
		catch (FactoryException expected) {}
	}

	public static class Empty {

		public static final AutoConstructor<Empty> CONSTRUCTOR = new AutoConstructor<>() {

			@Override
			@OverrideOnly
			@Contract("_ -> new")
			public <T_Encoded> Empty construct(@NotNull ConstructContext<T_Encoded> context) throws ConstructException {
				actualSuccesses++;
				return new Empty();
			}
		};

		public static final ConstructorFactory FACTORY = new ConstructorFactory() {

			@Override
			public <T_HandledType> AutoConstructor<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
				return CONSTRUCTOR;
			}
		};

		public static AutoConstructor<Empty> constructor() {
			return CONSTRUCTOR;
		}

		public static ConstructorFactory getFactory() {
			return FACTORY;
		}

		public static <T_Encoded> Empty construct(ConstructContext<T_Encoded> context) throws ConstructException {
			return context.constructWith(CONSTRUCTOR);
		}

		public static <T_HandledType> AutoConstructor<?> isFactory(FactoryContext<T_HandledType> context) {
			return CONSTRUCTOR;
		}

		public static final AutoConstructor<?> WILDCARD_CONSTRUCTOR = new AutoConstructor<>() {

			@Override
			@OverrideOnly
			@Contract("_ -> new")
			public <T_Encoded> Object construct(@NotNull ConstructContext<T_Encoded> context) throws ConstructException {
				actualSuccesses++;
				return new Empty();
			}
		};

		public static final ConstructorFactory WILDCARD_FACTORY = new ConstructorFactory() {

			@Override
			public <T_HandledType> AutoConstructor<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
				return WILDCARD_CONSTRUCTOR;
			}
		};

		public static AutoConstructor<?> wildcardConstructor() {
			return WILDCARD_CONSTRUCTOR;
		}

		public static ConstructorFactory getWildcardFactory() {
			return WILDCARD_FACTORY;
		}

		public static Object wildcardConstruct(ConstructContext<?> context) throws ConstructException {
			return context.constructWith(WILDCARD_CONSTRUCTOR);
		}

		public static AutoConstructor<?> isWildcardFactory(FactoryContext<?> context) {
			return WILDCARD_CONSTRUCTOR;
		}
	}
}
package builderb0y.autocodec.fixers;

import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.annotations.MemberUsage;
import builderb0y.autocodec.annotations.UseFixer;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.common.TestCommon;
import builderb0y.autocodec.fixers.AutoFixer.NamedFixer;
import builderb0y.autocodec.fixers.AutoFixer.NamedFixerFactory;
import builderb0y.autocodec.logging.DisabledTaskLogger;
import builderb0y.autocodec.logging.TaskLogger;
import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class DuplicateFixerTest {

	@Test
	public void testTwoAnnotations() {
		TestCommon.DISABLED_CODEC.createFixer(Duplicated.class);
		try {
			TestCommon.DISABLED_CODEC.createFixer(new ReifiedType<@UseFixer(name = "fix", in = Duplicated.class, usage = MemberUsage.METHOD_IS_HANDLER) Duplicated>() {});
			fail("duplicated @UseFixer's allowed");
		}
		catch (FactoryException expected) {}
	}

	@UseFixer(name = "fix", in = Duplicated.class, usage = MemberUsage.METHOD_IS_HANDLER)
	public static class Duplicated {

		public static <T_Encoded> DataFixContext<T_Encoded> fix(DataFixContext<T_Encoded> context) {
			return context;
		}
	}

	@Test
	public void testAnnotationAndFactory() {
		AutoCodec autoCodec = new AutoCodec() {

			@Override
			@OverrideOnly
			public @NotNull TaskLogger createDefaultLogger(@NotNull ReentrantLock lock) {
				return new DisabledTaskLogger();
			}

			@Override
			@OverrideOnly
			public @NotNull FixerFactoryList createFixers() {
				return new FixerFactoryList(this) {

					@Override
					@OverrideOnly
					public void setup() {
						super.setup();
						this.addFactoryToEnd(new AlwaysFixerFactory());
					}
				};
			}
		};
		autoCodec.createFixer(NonDuplicated.class);
		try {
			autoCodec.createFixer(new ReifiedType<@UseFixer(name = "fix", in = Duplicated.class, usage = MemberUsage.METHOD_IS_HANDLER) NonDuplicated>() {});
			fail("duplicate fixers allowed");
		}
		catch (FactoryException expected) {}
	}

	public static class NonDuplicated {}

	public static class AlwaysFixer extends NamedFixer<Object> {

		public AlwaysFixer(@NotNull ReifiedType<Object> handledType) {
			super(handledType);
		}

		@Override
		@OverrideOnly
		public @NotNull <T_Encoded> DataFixContext<T_Encoded> fixData(@NotNull DataFixContext<T_Encoded> context) throws DataFixException {
			return context;
		}
	}

	public static class AlwaysFixerFactory extends NamedFixerFactory {

		@Override
		@OverrideOnly
		public @Nullable <T_HandledType> AutoFixer<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			return new AlwaysFixer(context.type.uncheckedCast());
		}
	}
}
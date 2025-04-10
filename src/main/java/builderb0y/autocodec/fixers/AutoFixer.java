package builderb0y.autocodec.fixers;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.AutoHandler;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;

/**
my take on data fixers.
DFU's data fixers are basically indecipherable to me,
so I have no idea what this interface is analogous to.
*/
public interface AutoFixer<T_Decoded> extends AutoHandler {

	public static final @NotNull ObjectArrayFactory<AutoFixer<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(AutoFixer.class).generic();

	/**
	returns a context which accounts for any changes to {@link T_Decoded}'s schema over time.
	the returned context will be passed into the {@link AutoDecoder} instead of the provided context.
	*/
	@OverrideOnly
	public abstract <T_Encoded> @NotNull DataFixContext<T_Encoded> fix(@NotNull DataFixContext<T_Encoded> context) throws DataFixException;

	public static abstract class NamedFixer<T_Decoded> extends NamedHandler<T_Decoded> implements AutoFixer<T_Decoded> {

		public NamedFixer(@NotNull ReifiedType<T_Decoded> handledType) {
			super(handledType);
		}

		public NamedFixer(@NotNull String toString) {
			super(toString);
		}
	}

	public static interface FixerFactory extends AutoFactory<AutoFixer<?>> {

		/**
		returns an AutoFixer which can account for changes in the schema of T_HandledType,
		or null if this factory does not know how to correct such changes,
		or if no changes have happened yet. throws {@link FactoryException} if
		this factory knows how to correct for changes in T_HandledType's schema,
		but another problem occurs which prevents it from doing so.

		this method returns AutoFixer<?> instead of AutoFixer<T_HandledType>
		because returning <T_HandledType> usually just results in a lot of
		unchecked generic casts for implementors. java's generics system
		just wasn't designed for these kinds of things. nevertheless,
		this method should ideally return an AutoFixer<T_HandledType>
		at runtime, even if this is not required at compile time.

		this method is annotated with {@link OverrideOnly}
		because it performs no logging on its own.
		use {@link FactoryContext#tryCreateFixer(FixerFactory)}
		to create a fixer using this factory and log it at the same time.
		*/
		@Override
		@OverrideOnly
		public abstract <T_HandledType> @Nullable AutoFixer<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException;
	}

	public static abstract class NamedFixerFactory extends NamedFactory<AutoFixer<?>> implements FixerFactory {

		public NamedFixerFactory() {}

		public NamedFixerFactory(@NotNull String toString) {
			super(toString);
		}
	}
}
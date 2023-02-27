package builderb0y.autocodec.constructors;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.common.AutoHandler;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.decoders.ConstructImprintDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.imprinters.AutoImprinter;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;

public interface AutoConstructor<T_Decoded> extends AutoHandler {

	public static final @NotNull ObjectArrayFactory<AutoConstructor<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(AutoConstructor.class).generic();

	/**
	returns a new instance of type {@link T_Decoded}.
	throws {@link ConstructException} if an error
	occurred while constructing the object.

	the instance does not need to be initialized with
	the data stored in {@link ConstructContext#input};
	that task will be performed by the {@link AutoImprinter},
	which will run immediately after the new instance is created.
	see also: {@link ConstructImprintDecoder}.

	this method is annotated with {@link OverrideOnly}
	because it performs no logging on its own.
	use {@link DecodeContext#constructWith(AutoConstructor)}
	to construct and log what is being constructed.
	*/
	@OverrideOnly
	@Contract("_ -> new")
	public abstract <T_Encoded> @NotNull T_Decoded construct(@NotNull ConstructContext<T_Encoded> context) throws ConstructException;

	public static abstract class NamedConstructor<T_Decoded> extends NamedHandler<T_Decoded> implements AutoConstructor<T_Decoded> {

		public NamedConstructor(@NotNull ReifiedType<T_Decoded> type) {
			super(type);
		}

		public NamedConstructor(@NotNull String toString) {
			super(toString);
		}
	}

	public static interface ConstructorFactory extends AutoFactory<AutoConstructor<?>> {

		/**
		returns an AutoConstructor which can construct instances of T_HandledType,
		or null if this factory does not know how to construct instances of T_HandledType.
		throws {@link FactoryException} if this factory knows how to construct instances
		of T_HandledType, but some other problem occurs which prevents it from doing so.

		this method used to enforce that it returns AutoConstructor<T_HandledType>,
		but the problem with that is it usually just resulted
		in a lot of unchecked casts for implementors.
		java's generics system just wasn't designed for these kinds of things.

		this method is annotated with {@link OverrideOnly}
		because it performs no logging on its own.
		use {@link FactoryContext#tryCreateConstructor(ConstructorFactory)}
		to create an encoder using this factory and log it at the same time.
		*/
		@Override
		@OverrideOnly
		public abstract <T_HandledType> @Nullable AutoConstructor<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException;
	}

	public static abstract class NamedConstructorFactory extends NamedFactory<AutoConstructor<?>> implements ConstructorFactory {

		public NamedConstructorFactory() {}

		public NamedConstructorFactory(@NotNull String toString) {
			super(toString);
		}
	}
}
package builderb0y.autocodec.common;

import java.lang.invoke.*;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.constructors.AutoConstructor;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.imprinters.AutoImprinter;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;
import builderb0y.autocodec.util.TypeFormatter;
import builderb0y.autocodec.verifiers.AutoVerifier;

/**
common superinterface of {@link AutoEncoder}, {@link AutoConstructor},
{@link AutoImprinter}, {@link AutoDecoder}, and {@link AutoVerifier}.
this interface on its own does not define any methods except {@link #toString()},
because the actual handle method takes different parameters and
have different return types depending on the type of handler.
{@link #toString()} is required for logging purposes.
*/
public interface AutoHandler {

	public static final @NotNull ObjectArrayFactory<AutoHandler> ARRAY_FACTORY = new ObjectArrayFactory<>(AutoHandler.class);

	/**
	while {@link Object} technically overrides this method,
	it provides a long and not-really-helpful description.
	this method will be called by logging,
	so it is strongly recommended to override it to
	provide a String which is shorter and more meaningful.
	*/
	@Override
	public abstract String toString();

	/**
	basically a {@link Function} which can throw exceptions.
	used for adapting a handler to operate on a different type.
	*/
	@FunctionalInterface
	public static interface HandlerMapper<T_From, T_To> {

		/**
		converts the object to something the old handler can use.
		this method throws {@link Throwable} because it is expected that
		some implementations of HandlerMapper may use a {@link MethodHandle}
		as part of the conversion process.
		*/
		public abstract T_To apply(T_From from) throws Throwable;

		/** creates a HandlerMapper from the provided handle. */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public static <T_From, T_To> @NotNull HandlerMapper<T_From, T_To> createLambda(@NotNull MethodHandle handle) {
			try {
				return (HandlerMapper<T_From, T_To>)(
					LambdaMetafactory.metafactory(
						MethodHandles.lookup(),
						"apply",
						MethodType.methodType(HandlerMapper.class),
						MethodType.methodType(Object.class, Object.class),
						handle,
						handle.type()
					)
					.getTarget()
					.invokeExact()
				);
			}
			catch (Throwable throwable) {
				return (HandlerMapper)(handle.asType(MethodType.methodType(Object.class, Object.class))::invokeExact);
			}
		}

		/**
		returns a null-safe version of the provided mapper.
		in other words, one which outputs null when its input is null,
		without throwing a NullPointerException.
		the provided mapper will never need to handle null.
		*/
		public static <T_From, T_To> @NotNull HandlerMapper<@Nullable T_From, T_To> nullSafe(@NotNull HandlerMapper<@NotNull T_From, T_To> mapper) {
			return (@Nullable T_From object) -> object == null ? null : mapper.apply(object);
		}
	}

	/**
	bare bones implementation of AutoHandler which overrides {@link #toString()}
	to delegate to a field initialized in the constructor.
	most built-in AutoHandler's are subclasses of this class.
	*/
	public static abstract class NamedHandler<T_Decoded> implements AutoHandler {

		/**
		the String to be returned by {@link #toString()}.
		this field is non-final for the sole reason of allowing
		subclasses to modify it in their constructors if desired.
		*/
		public @NotNull String toString;

		public NamedHandler(@NotNull ReifiedType<T_Decoded> handledType) {
			this.toString = defaultName(this, handledType);
		}

		public NamedHandler(@NotNull String toString) {
			this.toString = toString;
		}

		public static @NotNull String defaultName(@NotNull AutoHandler handler, @NotNull ReifiedType<?> handledType) {
			return new TypeFormatter(64).simplify(true).annotations(true).append(handler.getClass()).append('<').append(handledType).append('>').toString();
		}

		@Override
		public String toString() {
			return this.toString;
		}
	}

	/**
	produces handlers for a specific {@link ReifiedType} or group of related types.
	the produced handlers are expected to be able to handle instances of that type.
	*/
	public static interface AutoFactory<T_Handler extends AutoHandler> {

		/**
		attempts to create a {@link T_Handler} which can handle
		instances of the context's {@link FactoryContext#type}.
		if this AutoFactory does NOT know how to handle the context's type, returns null.
		if this AutoFactory DOES know how to handle the context's type,
		but some other issue occurs which prevents it from doing so,
		throws {@link FactoryException}.
		*/
		@OverrideOnly
		public abstract <T_HandledType> @Nullable T_Handler tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException;

		/**
		similar to {@link #tryCreate(FactoryContext)},
		but will throw a {@link FactoryException}
		on failure instead of returning null.
		*/
		public default <T_HandledType> @NotNull T_Handler forceCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			T_Handler handler = this.tryCreate(context);
			if (handler != null) return handler;
			else throw new FactoryException(this + " cannot create handler for " + context);
		}

		@Override
		public abstract String toString();
	}

	/**
	bare bones implementation of AutoFactory which overrides {@link #toString()}
	to delegate to a field initialized in the constructor.
	most built-in AutoFactory's are subclasses of this class.
	*/
	public static abstract class NamedFactory<T_Handler extends AutoHandler> implements AutoFactory<T_Handler> {

		/**
		the String to be returned by {@link #toString()}.
		this field is non-final for the sole reason of allowing
		subclasses to modify it in their constructors if desired.
		*/
		public @NotNull String toString;

		public NamedFactory() {
			this.toString = TypeFormatter.getSimpleClassName(this.getClass());
		}

		public NamedFactory(@NotNull String toString) {
			this.toString = toString;
		}

		@Override
		public String toString() {
			return this.toString;
		}
	}
}
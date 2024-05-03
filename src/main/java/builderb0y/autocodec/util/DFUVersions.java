package builderb0y.autocodec.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** a collection of utility methods to handle changes in how DFU operates between versions. */
public class DFUVersions {

	@Internal
	public static final MethodHandle DATA_RESULT_SUCCESS;
	static {
		try {
			DATA_RESULT_SUCCESS = MethodHandles.lookup().findStatic(DataResult.class, "success", MethodType.methodType(DataResult.class, Object.class));
		}
		catch (Exception exception) {
			throw AutoCodecUtil.rethrow(exception);
		}
	}
	@Internal
	public static final MethodHandle DATA_RESULT_ERROR = createDataResultErrorHandle(DataResult.class, "error");
	@Internal
	public static final DataResultAccessor DATA_RESULT_ACCESSOR;
	static {
		if      (V5DataResultAccessor.VALID) DATA_RESULT_ACCESSOR = new V5DataResultAccessor();
		else if (V6DataResultAccessor.VALID) DATA_RESULT_ACCESSOR = new V6DataResultAccessor();
		else if (V7DataResultAccessor.VALID) DATA_RESULT_ACCESSOR = new V7DataResultAccessor();
		else throw new IllegalStateException("DFU is either not on the class path, or changed incompatibly since AutoCodec was last updated. Try updating AutoCodec, and if that doesn't fix the issue, report this to Builderb0y.");
	}

	/**
	used to work around the fact that DataResult changed from a class to an interface at one point.
	the method {@link DataResult#success(Object)} is now marked as an interface method,
	and if the runtime type of DataResult is not an interface, then the JVM throws an error.
	luckily, MethodHandle's can adapt to these changes and choose an appropriate invoke mode at runtime.
	*/
	@SuppressWarnings("unchecked")
	public static <R> DataResult<R> createSuccessDataResult(R result) {
		try {
			return (DataResult<R>)(DATA_RESULT_SUCCESS.invokeExact(result));
		}
		catch (Throwable throwable) {
			throw AutoCodecUtil.rethrow(throwable);
		}
	}

	/**
	returns a new {@link DataResult} whose {@link DataResult.PartialResult#message}
	matches the provided {@link Supplier}. if the current environment
	detects an old version of DFU, then the Supplier will be invoked immediately.
	otherwise, the Supplier will be stored on the DataResult itself,
	to be used whenever needed.
	*/
	@SuppressWarnings("unchecked")
	public static <R> DataResult<R> createErrorDataResult(Supplier<String> message) {
		try {
			return (DataResult<R>)(DATA_RESULT_ERROR.invokeExact(message));
		}
		catch (Throwable throwable) {
			throw AutoCodecUtil.rethrow(throwable);
		}
	}

	/**
	searches (in) for a method named (name) which takes either a {@link Supplier<String>}
	or a {@link String} as an argument, and returns a {@link DataResult}.
	if such a method is found, then:
		if the method takes a {@link Supplier<String>} as an argument,
		then the returned handle simply delegates to the underlying method.

		otherwise, if the method takes a {@link String} as an argument,
		then the returned handle will still take a {@link Supplier<String>}
		as an argument, but will invoke {@link Supplier#get()} on its argument
		before passing the result into the underlying method.

	this method is *not* hard-coded for in = {@link DataResult}.class
	and name = "error" so that it can be tested properly
	in junit with a dependency on only one version of DFU.
	*/
	@Internal
	public static MethodHandle createDataResultErrorHandle(Class<?> in, String name) {
		MethodHandle error;
		try {
			//find in.name(Supplier<String> supplier)
			error = MethodHandles.lookup().findStatic(in, name, MethodType.methodType(DataResult.class, Supplier.class));
		}
		catch (Throwable throwable1) {
			try {
				//find in.name(String string)
				error = MethodHandles.lookup().findStatic(in, name, MethodType.methodType(DataResult.class, String.class));
				//find Supplier.get()
				MethodHandle supplierGet = MethodHandles.lookup().findVirtual(Supplier.class, "get", MethodType.methodType(Object.class));
				//cast return value of Supplier.get() to a String.
				MethodHandle casted = MethodHandles.explicitCastArguments(supplierGet, MethodType.methodType(String.class, Supplier.class));
				//create handle in.name(supplier.get())
				error = MethodHandles.filterArguments(error, 0, casted);
			}
			catch (Throwable throwable2) {
				//neither method was found. abort!
				throwable2.addSuppressed(throwable1);
				throw AutoCodecUtil.rethrow(throwable2);
			}
		}
		return error;
	}

	/** returns the DataResult's result, if present, or null if the DataResult is an error. */
	public static <R> @Nullable R getResult(@NotNull DataResult<R> result) {
		try {
			return DATA_RESULT_ACCESSOR.getResult(result);
		}
		catch (IllegalAccessException exception) {
			throw AutoCodecUtil.rethrow(exception);
		}
	}

	public static <R> @Nullable R getPartialResult(@NotNull DataResult<R> result) {
		try {
			return DATA_RESULT_ACCESSOR.getPartialResult(result);
		}
		catch (IllegalAccessException exception) {
			throw AutoCodecUtil.rethrow(exception);
		}
	}

	public static <R> @Nullable String getMessage(@NotNull DataResult<R> result) {
		try {
			return DATA_RESULT_ACCESSOR.getMessage(result);
		}
		catch (IllegalAccessException exception) {
			throw AutoCodecUtil.rethrow(exception);
		}
	}

	public static <R> @Nullable Supplier<String> getMessageLazy(@NotNull DataResult<R> result) {
		try {
			return DATA_RESULT_ACCESSOR.getMessageLazy(result);
		}
		catch (IllegalAccessException exception) {
			throw AutoCodecUtil.rethrow(exception);
		}
	}

	@Internal
	public static interface DataResultAccessor {

		public abstract <R> @Nullable R getResult(@NotNull DataResult<R> result) throws IllegalAccessException;

		public abstract <R> @Nullable R getPartialResult(@NotNull DataResult<R> result) throws IllegalAccessException;

		public abstract <R> @Nullable String getMessage(@NotNull DataResult<R> result) throws IllegalAccessException;

		public abstract <R> @Nullable Supplier<String> getMessageLazy(@NotNull DataResult<R> result) throws IllegalAccessException;
	}

	/**
	was it v6 when DataResult switched from a String to a Supplier<String>?
	I don't remember what version they did that in.
	either way, I'm going to be naming the String
	version V5 and the Supplier<String> version V6.
	*/
	@Internal
	public static class V5DataResultAccessor implements DataResultAccessor {

		public static final boolean VALID;
		public static final Field
			DATA_RESULT_RESULT_EITHER,
			PARTIAL_RESULT_MESSAGE_STRING,
			PARTIAL_RESULT_PARTIAL_RESULT_OPTIONAL;

		static {
			boolean valid;
			Field
				dataResultResultEither,
				partialResultMessageString,
				partialResultPartialResultOptional;
			try {
				dataResultResultEither = getField(Class.forName("com.mojang.serialization.DataResult"), "result", Either.class);
				partialResultMessageString = getField(Class.forName("com.mojang.serialization.DataResult$PartialResult"), "message", String.class);
				partialResultPartialResultOptional = getField(Class.forName("com.mojang.serialization.DataResult$PartialResult"), "partialResult", Optional.class);
				valid = true;
			}
			catch (Exception exception) {
				dataResultResultEither = null;
				partialResultMessageString = null;
				partialResultPartialResultOptional = null;
				valid = false;
			}
			VALID = valid;
			DATA_RESULT_RESULT_EITHER = dataResultResultEither;
			PARTIAL_RESULT_MESSAGE_STRING = partialResultMessageString;
			PARTIAL_RESULT_PARTIAL_RESULT_OPTIONAL = partialResultPartialResultOptional;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <R> @Nullable R getResult(@NotNull DataResult<R> result) throws IllegalAccessException {
			Either either = (Either)(DATA_RESULT_RESULT_EITHER.get(result));
			return (R)(either.left().orElse(null));
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <R> @Nullable R getPartialResult(@NotNull DataResult<R> result) throws IllegalAccessException {
			Either either = (Either)(DATA_RESULT_RESULT_EITHER.get(result));
			Object partial = either.right().orElse(null);
			if (partial == null) return null;
			Optional partialResult = (Optional)(PARTIAL_RESULT_PARTIAL_RESULT_OPTIONAL.get(partial));
			return (R)(partialResult.orElse(null));
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <R> @Nullable String getMessage(@NotNull DataResult<R> result) throws IllegalAccessException {
			Either either = (Either)(DATA_RESULT_RESULT_EITHER.get(result));
			Object partial = either.right().orElse(null);
			if (partial == null) return null;
			return (String)(PARTIAL_RESULT_MESSAGE_STRING.get(partial));
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public @Nullable <R> Supplier<String> getMessageLazy(@NotNull DataResult<R> result) throws IllegalAccessException {
			Either either = (Either)(DATA_RESULT_RESULT_EITHER.get(result));
			Object partial = either.right().orElse(null);
			if (partial == null) return null;
			String message = (String)(PARTIAL_RESULT_MESSAGE_STRING.get(partial));
			return () -> message;
		}
	}

	@Internal
	public static class V6DataResultAccessor implements DataResultAccessor {

		public static final boolean VALID;
		public static final Field
			DATA_RESULT_RESULT_EITHER,
			PARTIAL_RESULT_MESSAGE_SUPPLIER,
			PARTIAL_RESULT_PARTIAL_RESULT_OPTIONAL;

		static {
			boolean valid;
			Field
				dataResultResultEither,
				partialResultMessageSupplier,
				partialResultPartialResultOptional;
			try {
				dataResultResultEither = getField(Class.forName("com.mojang.serialization.DataResult"), "result", Either.class);
				partialResultMessageSupplier = getField(Class.forName("com.mojang.serialization.DataResult$PartialResult"), "message", Supplier.class);
				partialResultPartialResultOptional = getField(Class.forName("com.mojang.serialization.DataResult$PartialResult"), "partialResult", Optional.class);
				valid = true;
			}
			catch (Exception exception) {
				dataResultResultEither = null;
				partialResultMessageSupplier = null;
				partialResultPartialResultOptional = null;
				valid = false;
			}
			VALID = valid;
			DATA_RESULT_RESULT_EITHER = dataResultResultEither;
			PARTIAL_RESULT_MESSAGE_SUPPLIER = partialResultMessageSupplier;
			PARTIAL_RESULT_PARTIAL_RESULT_OPTIONAL = partialResultPartialResultOptional;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <R> @Nullable R getResult(@NotNull DataResult<R> result) throws IllegalAccessException {
			Either either = (Either)(DATA_RESULT_RESULT_EITHER.get(result));
			return (R)(either.left().orElse(null));
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <R> @Nullable R getPartialResult(@NotNull DataResult<R> result) throws IllegalAccessException {
			Either either = (Either)(DATA_RESULT_RESULT_EITHER.get(result));
			Object partial = either.right().orElse(null);
			if (partial == null) return null;
			Optional partialResult = (Optional)(PARTIAL_RESULT_PARTIAL_RESULT_OPTIONAL.get(partial));
			return (R)(partialResult.orElse(null));
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <R> @Nullable String getMessage(@NotNull DataResult<R> result) throws IllegalAccessException {
			Either either = (Either)(DATA_RESULT_RESULT_EITHER.get(result));
			Object partial = either.right().orElse(null);
			if (partial == null) return null;
			Supplier supplier = (Supplier)(PARTIAL_RESULT_MESSAGE_SUPPLIER.get(partial));
			return (String)(supplier.get());
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <R> @Nullable Supplier<String> getMessageLazy(@NotNull DataResult<R> result) throws IllegalAccessException {
			Either either = (Either)(DATA_RESULT_RESULT_EITHER.get(result));
			Object partial = either.right().orElse(null);
			if (partial == null) return null;
			return (Supplier)(PARTIAL_RESULT_MESSAGE_SUPPLIER.get(partial));
		}
	}

	public static class V7DataResultAccessor implements DataResultAccessor {

		public static final boolean VALID;
		public static final Class<?> SUCCESS_CLASS;
		public static final Class<?> ERROR_CLASS;
		public static final Field SUCCESS_VALUE;
		public static final Field ERROR_PARTIAL_VALUE_OPTIONAL;
		public static final Field ERROR_MESSAGE_SUPPLIER_SUPPLIER;

		static {
			boolean valid;
			Class<?> successClass, errorClass;
			Field successValue, errorPartialValueOptional, errorMessageSupplierSupplier;
			try {
				successClass = Class.forName("com.mojang.serialization.DataResult$Success");
				errorClass = Class.forName("com.mojang.serialization.DataResult$Error");
				successValue = getField(successClass, "value", Object.class);
				errorPartialValueOptional = getField(errorClass, "partialValue", Optional.class);
				errorMessageSupplierSupplier = getField(errorClass, "messageSupplier", Supplier.class);
				valid = true;
			}
			catch (Exception exception) {
				successClass = null;
				errorClass = null;
				successValue = null;
				errorPartialValueOptional = null;
				errorMessageSupplierSupplier = null;
				valid = false;
			}
			VALID = valid;
			SUCCESS_CLASS = successClass;
			ERROR_CLASS = errorClass;
			SUCCESS_VALUE = successValue;
			ERROR_PARTIAL_VALUE_OPTIONAL = errorPartialValueOptional;
			ERROR_MESSAGE_SUPPLIER_SUPPLIER = errorMessageSupplierSupplier;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <R> @Nullable R getResult(@NotNull DataResult<R> result) throws IllegalAccessException {
			if (!SUCCESS_CLASS.isInstance(result)) return null;
			return (R)(SUCCESS_VALUE.get(result));
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <R> @Nullable R getPartialResult(@NotNull DataResult<R> result) throws IllegalAccessException {
			if (!ERROR_CLASS.isInstance(result)) return null;
			Optional optional = (Optional)(ERROR_PARTIAL_VALUE_OPTIONAL.get(result));
			return (R)(optional.orElse(null));
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <R> @Nullable String getMessage(@NotNull DataResult<R> result) throws IllegalAccessException {
			if (!ERROR_CLASS.isInstance(result)) return null;
			Supplier supplier = (Supplier)(ERROR_MESSAGE_SUPPLIER_SUPPLIER.get(result));
			return (String)(supplier.get());
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <R> @Nullable Supplier<String> getMessageLazy(@NotNull DataResult<R> result) throws IllegalAccessException {
			if (!ERROR_CLASS.isInstance(result)) return null;
			return (Supplier)(ERROR_MESSAGE_SUPPLIER_SUPPLIER.get(result));
		}
	}

	@Internal
	public static Field getField(@NotNull Class<?> clazz, @NotNull String name, @NotNull Class<?> type) throws Exception {
		Field field = clazz.getDeclaredField(name);
		if (field.getType() != type) throw new NoSuchFieldException(field + " is not of type " + type);
		field.setAccessible(true);
		return field;
	}
}
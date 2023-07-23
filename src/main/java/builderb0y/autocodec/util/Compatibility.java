package builderb0y.autocodec.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
a collection of utility methods to handle changes in how DFU operates between version.
at the time of writing this, there is only one change that affects AutoCodec,
but all future changes will be added to this class.
*/
public class Compatibility {

	public static final MethodHandle DATA_RESULT_ERROR = createDataResultErrorHandle(DataResult.class, "error");

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
}
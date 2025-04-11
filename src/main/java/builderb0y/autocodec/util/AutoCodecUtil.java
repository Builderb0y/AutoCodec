package builderb0y.autocodec.util;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AutoCodecUtil {

	/**
	throws a checked exception without telling the compiler.
	similar to {@link sun.misc.Unsafe#throwException(Throwable)}.
	the declaration of this method returns {@link RuntimeException}
	so that it can be used inside another throw statement,
	but it will never actually return anything.
	example usage: {@code
		try {
			return readFromFile();
		}
		catch (IOException exception) {
			//this will throw the IOException directly,
			//without wrapping it in some other type of unchecked exception.
			throw AutoCodecUtil.rethrow(exception);
		}
	}
	in this case, the generic type parameter X is
	automatically inferred to be RuntimeException.
	directly specifying {@code
		AutoCodecUtil.<RuntimeException>rethrow(exception)
	}
	is unnecessary.
	*/
	@SuppressWarnings("unchecked")
	public static <X extends Throwable> RuntimeException rethrow(Throwable throwable) throws X {
		//generic type erasure will not attempt to
		//cast throwable to anything at runtime.
		//it will always be thrown as-is.
		//as such, throwable does not need to
		//be a subclass of the type parameter X.
		throw (X)(throwable);
	}

	@Internal
	public static boolean isNonStaticInnerClass(@NotNull Class<?> clazz) {
		//match logic for {@link Constructor#getAnnotatedReceiverType()}
		return (
			clazz.getEnclosingClass() != null &&
			clazz.getDeclaringClass() != null &&
			!Modifier.isStatic(clazz.getModifiers())
		);
	}

	public static @NotNull String deepToString(@Nullable Object object) {
		if (object == null) return "null";
		if (object.getClass().isArray()) {
			if (object instanceof Object [] array) return Arrays.deepToString(array);
			if (object instanceof byte   [] array) return Arrays.toString(array);
			if (object instanceof short  [] array) return Arrays.toString(array);
			if (object instanceof int    [] array) return Arrays.toString(array);
			if (object instanceof long   [] array) return Arrays.toString(array);
			if (object instanceof float  [] array) return Arrays.toString(array);
			if (object instanceof double [] array) return Arrays.toString(array);
			if (object instanceof char   [] array) return Arrays.toString(array);
			if (object instanceof boolean[] array) return Arrays.toString(array);
		}
		return object.toString();
	}

	/**
	attempts to create a lambda normally, and if that fails,
	converts implementation::invokeExact to a lambda.
	surprisingly, this works.
	*/
	@Internal
	@SuppressWarnings("unchecked")
	public static <F> F forceLambda(
		MethodHandles.Lookup caller,
		String interfaceMethodName,
		Class<F> interfaceClass,
		MethodType interfaceMethodType,
		MethodHandle implementation
	) {
		try {
			return (F)(
				LambdaMetafactory.metafactory(
					caller,
					interfaceMethodName,
					MethodType.methodType(interfaceClass),
					interfaceMethodType,
					implementation,
					interfaceMethodType
				)
				.getTarget()
				.invoke()
			);
		}
		catch (Throwable first) {
			try {
				return (F)(
					LambdaMetafactory.metafactory(
						caller,
						interfaceMethodName,
						MethodType.methodType(interfaceClass, MethodHandle.class),
						interfaceMethodType,
						MethodHandles.exactInvoker(implementation.type()),
						interfaceMethodType
					)
					.getTarget()
					.invoke(implementation)
				);
			}
			catch (Throwable second) {
				second.addSuppressed(first);
				throw rethrow(second);
			}
		}
	}

	/** convenience method only exists on {@link String}, not {@link CharSequence}. */
	@Internal
	public static boolean regionMatches(CharSequence a, int aStart, CharSequence b, int bStart, int length) {
		if (aStart < 0 || aStart + length > a.length()) return false;
		if (bStart < 0 || bStart + length > b.length()) return false;
		for (int offset = 0; offset < length; offset++) {
			if (a.charAt(aStart + offset) != b.charAt(bStart + offset)) return false;
		}
		return true;
	}

	/**
	guarantees that you get the kind of map you want,
	unlike {@link Collectors#toMap(Function, Function)}.
	also allows null values, and doesn't make you specify the merger.
	*/
	@Internal
	public static <T_Both, T_Key, T_Value, T_Map extends Map<T_Key, T_Value>> @NotNull Collector<T_Both, ?, T_Map> collectToMap(
		@NotNull Function<? super T_Both, ? extends T_Key> keyGetter,
		@NotNull Function<? super T_Both, ? extends T_Value> valueGetter,
		@NotNull Supplier<T_Map> mapConstructor
	) {
		return Collector.of(
			mapConstructor,
			(T_Map map, T_Both entry) -> {
				T_Key key = keyGetter.apply(entry);
				if (map.containsKey(key)) throw new IllegalArgumentException("Duplicate key: " + key);
				T_Value value = valueGetter.apply(entry);
				map.put(key, value);
			},
			(T_Map map1, T_Map map2) -> {
				for (Map.Entry<T_Key, T_Value> entry : map2.entrySet()) {
					T_Key key = entry.getKey();
					if (map1.containsKey(key)) throw new IllegalArgumentException("Duplicate key: " + key);
					map1.put(key, entry.getValue());
				}
				return map1;
			}
		);
	}
}
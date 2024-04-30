package builderb0y.autocodec.util;

import java.lang.reflect.Modifier;
import java.util.Arrays;

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
}
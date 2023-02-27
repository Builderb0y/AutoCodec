package builderb0y.autocodec.util;

import java.lang.reflect.Array;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class PrimitiveArrayFactory<T_Array> extends ArrayFactory<T_Array> {

	public static final PrimitiveArrayFactory<byte   []> BYTE    = new PrimitiveArrayFactory<>(byte   [].class);
	public static final PrimitiveArrayFactory<short  []> SHORT   = new PrimitiveArrayFactory<>(short  [].class);
	public static final PrimitiveArrayFactory<int    []> INT     = new PrimitiveArrayFactory<>(int    [].class);
	public static final PrimitiveArrayFactory<long   []> LONG    = new PrimitiveArrayFactory<>(long   [].class);
	public static final PrimitiveArrayFactory<float  []> FLOAT   = new PrimitiveArrayFactory<>(float  [].class);
	public static final PrimitiveArrayFactory<double []> DOUBLE  = new PrimitiveArrayFactory<>(double [].class);
	public static final PrimitiveArrayFactory<char   []> CHAR    = new PrimitiveArrayFactory<>(char   [].class);
	public static final PrimitiveArrayFactory<boolean[]> BOOLEAN = new PrimitiveArrayFactory<>(boolean[].class);
	public static final Map<Class<?>, PrimitiveArrayFactory<?>> BY_COMPONENT = Map.of(
		byte   .class, BYTE,
		short  .class, SHORT,
		int    .class, INT,
		long   .class, LONG,
		float  .class, FLOAT,
		double .class, DOUBLE,
		char   .class, CHAR,
		boolean.class, BOOLEAN
	);
	public static final Map<Class<?>, PrimitiveArrayFactory<?>> BY_ARRAY = Map.of(
		byte   [].class, BYTE,
		short  [].class, SHORT,
		int    [].class, INT,
		long   [].class, LONG,
		float  [].class, FLOAT,
		double [].class, DOUBLE,
		char   [].class, CHAR,
		boolean[].class, BOOLEAN
	);

	@SuppressWarnings("unchecked")
	public PrimitiveArrayFactory(Class<T_Array> arrayClass) {
		super((T_Array)(Array.newInstance(arrayClass.getComponentType(), 0)));
	}

	public static @NotNull PrimitiveArrayFactory<?> forComponentType(@NotNull Class<?> componentType) {
		PrimitiveArrayFactory<?> factory = BY_COMPONENT.get(componentType);
		if (factory != null) return factory;
		else throw new IllegalArgumentException("Not a primitive class: " + componentType);
	}

	@SuppressWarnings("unchecked")
	public static <T> @NotNull PrimitiveArrayFactory<T> forArrayType(@NotNull Class<T> arrayType) {
		PrimitiveArrayFactory<T> factory = (PrimitiveArrayFactory<T>)(BY_ARRAY.get(arrayType));
		if (factory != null) return factory;
		else throw new IllegalArgumentException("Not a primitive array class: " + arrayType);
	}
}
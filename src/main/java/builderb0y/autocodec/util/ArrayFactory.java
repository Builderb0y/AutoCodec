package builderb0y.autocodec.util;

import java.lang.reflect.Array;
import java.util.function.IntFunction;

import org.jetbrains.annotations.NotNull;

public class ArrayFactory<T_Array> implements IntFunction<@NotNull T_Array> {

	public final @NotNull T_Array empty;

	public ArrayFactory(@NotNull T_Array empty) {
		this.empty = empty;
	}

	@SuppressWarnings("unchecked")
	public Class<T_Array> getArrayType() {
		return (Class<T_Array>)(this.empty.getClass());
	}

	public Class<?> getComponentType() {
		return this.getArrayType().getComponentType();
	}

	public @NotNull T_Array empty() {
		return this.empty;
	}

	@Override
	@SuppressWarnings("unchecked")
	public @NotNull T_Array apply(int length) {
		return length == 0 ? this.empty : (T_Array)(Array.newInstance(this.getComponentType(), length));
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + '<' + this.empty.getClass() + '>';
	}
}
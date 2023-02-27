package builderb0y.autocodec.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public class ObjectArrayFactory<T_Raw> extends ArrayFactory<@Nullable T_Raw @NotNull []> {

	public ObjectArrayFactory(@NotNull Class<T_Raw> clazz) {
		super((T_Raw[])(Array.newInstance(clazz, 0)));
	}

	/**
	the super method returns @Nullable T_Raw @NotNull [],
	but this is undesired in most cases because the array is empty.
	so, we are overriding it here to change the nullability signature.
	*/
	@Override
	@SuppressWarnings("NullableProblems")
	public @NotNull T_Raw @NotNull [] empty() {
		return super.empty();
	}

	@Override //narrow return type.
	@SuppressWarnings("unchecked")
	public Class<T_Raw> getComponentType() {
		return (Class<T_Raw>)(super.getComponentType());
	}

	public <T_Generic extends T_Raw> @NotNull T_Generic @NotNull [] emptyGeneric() {
		return (T_Generic[])(this.empty);
	}

	public <T_Generic extends T_Raw> @Nullable T_Generic @NotNull [] applyGeneric(int length) {
		return (T_Generic[])(this.apply(length));
	}

	public <T_Generic extends T_Raw> ObjectArrayFactory<T_Generic> generic() {
		return (ObjectArrayFactory<T_Generic>)(this);
	}

	public T_Raw @NotNull [] collectionToArray(@Nullable Collection<T_Raw> collection) {
		return collection == null || collection.isEmpty() ? this.empty() : collection.toArray(this.apply(collection.size()));
	}

	public <T_Generic extends T_Raw> T_Generic @NotNull [] collectionToArrayGeneric(@Nullable Collection<T_Generic> collection) {
		return collection == null || collection.isEmpty() ? this.emptyGeneric() : collection.toArray(this.applyGeneric(collection.size()));
	}

	public T_Raw @NotNull [] collectionToArrayForced(@Nullable Collection<?> collection) {
		return collection == null || collection.isEmpty() ? this.empty() : collection.toArray(this.apply(collection.size()));
	}

	@SuppressWarnings("SuspiciousToArrayCall")
	public <T_Generic extends T_Raw> T_Generic @NotNull [] collectionToArrayForcedGeneric(@Nullable Collection<?> collection) {
		return collection == null ? this.emptyGeneric() : collection.toArray(this.applyGeneric(collection.size()));
	}

	public @NotNull Collector<T_Raw, List<T_Raw>, T_Raw[]> collector() {
		return Collector.of(
			ArrayList::new,
			List::add,
			(List<T_Raw> list1, List<T_Raw> list2) -> { list1.addAll(list2); return list1; },
			this::collectionToArray
		);
	}

	@SafeVarargs
	public final T_Raw @NotNull [] of(T_Raw @NotNull ... array) {
		return array.length == 0 ? this.empty() : array;
	}

	@SafeVarargs
	public final <T_Generic extends T_Raw> T_Generic @NotNull [] ofGeneric(T_Generic @NotNull ... array) {
		return array.length == 0 ? this.emptyGeneric() : array;
	}
}
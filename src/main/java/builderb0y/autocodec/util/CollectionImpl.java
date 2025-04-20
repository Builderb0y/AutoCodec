package builderb0y.autocodec.util;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.jetbrains.annotations.NotNull;

/**
properties:
* not ordered.
	* order of elements may change on modification.
* duplicate elements allowed.
	* despite this, if more than one copy of an element is added to the collection,
		only one copy of that element is stored in the collection.
		it is simply marked as occurring more than once in the collection.
	* additionally, if two elements x and y are added to the collection
		such that x.equals(y) && x != y, only one of these
		elements will actually be retained by the collection.
		keep this in mind when using reference equality on elements of the collection.
* not thread-safe.
* undefined behavior will occur when mutating an element
	of the collection in a way that changes its hash code.

time complexities:
* add: O(1)
* remove: O(1)
* contains: O(1)
*/
public class CollectionImpl<T> implements Collection<T> {

	public final @NotNull Object2IntOpenHashMap<T> map;
	public int size;

	public CollectionImpl() {
		this.map = new Object2IntOpenHashMap<>();
	}

	public CollectionImpl(int expectedUniqueElements) {
		this.map = new Object2IntOpenHashMap<>(expectedUniqueElements);
	}

	public CollectionImpl(@NotNull Collection<? extends T> contents) {
		this.map = createMap(contents);
		this.size = contents.size();
	}

	public static <T> Object2IntOpenHashMap<T> createMap(@NotNull Collection<? extends T> contents) {
		Object2IntOpenHashMap<T> map = new Object2IntOpenHashMap<>(contents.size());
		for (T element : contents) {
			map.addTo(element, 1);
		}
		return map;
	}

	@Override
	public boolean add(T element) {
		this.map.addTo(element, 1);
		this.size++;
		return true;
	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public boolean isEmpty() {
		return this.size == 0;
	}

	@Override
	public boolean contains(Object element) {
		return this.map.containsKey(element);
	}

	/** returns the number of times the provided element has been added to this collection. */
	public int count(Object element) {
		return this.map.getInt(element);
	}

	@Override
	public @NotNull Iterator<T> iterator() {
		if (this.isEmpty()) return Collections.emptyIterator();
		ObjectIterator<Object2IntMap.Entry<T>> delegate = this.map.object2IntEntrySet().fastIterator();
		return new Iterator<>() {

			public Object2IntMap.Entry<T> entry;
			public int count;
			public boolean canRemove;

			@Override
			public boolean hasNext() {
				return this.count > 0 || delegate.hasNext();
			}

			@Override
			public T next() {
				if (this.count == 0) {
					if (delegate.hasNext()) {
						this.entry = delegate.next();
						this.count = this.entry.getIntValue();
					}
					else {
						throw new NoSuchElementException();
					}
				}
				this.count--;
				this.canRemove = true;
				return this.entry.getKey();
			}

			@Override
			public void remove() {
				if (!this.canRemove) throw new NoSuchElementException();
				int count = this.entry.getIntValue();
				if (count > 1) this.entry.setValue(count - 1);
				else delegate.remove();
			}

			@Override
			public void forEachRemaining(Consumer<? super T> action) {
				T element = this.entry.getKey();
				for (int count = this.count; --count >= 0;) {
					action.accept(element);
				}
				while (delegate.hasNext()) {
					Object2IntMap.Entry<T> entry = delegate.next();
					element = entry.getKey();
					for (int count = entry.getIntValue(); --count >= 0;) {
						action.accept(element);
					}
				}
			}
		};
	}

	public @NotNull Set<T> distinctElements() {
		return this.map.keySet();
	}

	/**
	returns an Iterator which reports each element exactly once,
	not the number of times it was added to this collection.
	*/
	public @NotNull Iterator<T> distinctIterator() {
		return this.map.keySet().iterator();
	}

	/**
	returns a Spliterator which reports each element exactly once,
	not the number of times it was added to this collection.
	*/
	public @NotNull Spliterator<T> distinctSpliterator() {
		return this.map.keySet().spliterator();
	}

	/**
	returns a Stream which reports each element exactly once,
	not the number of times it was added to this collection.
	*/
	public @NotNull Stream<T> distinctStream() {
		return this.map.keySet().stream();
	}

	/**
	returns a parallel Stream which reports each element exactly once,
	not the number of times it was added to this collection.
	*/
	public @NotNull Stream<T> distinctParallelStream() {
		return this.map.keySet().parallelStream();
	}

	@Override
	public Object @NotNull [] toArray() {
		return this.toArray(new Object[this.size]);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T1> T1 @NotNull [] toArray(T1 @NotNull [] array) {
		if (this.isEmpty()) {
			if (array.length != 0) {
				array[0] = null;
			}
			return array;
		}
		if (array.length < this.size) {
			array = (T1[])(Array.newInstance(array.getClass().getComponentType(), this.size));
		}
		int index = 0;
		for (ObjectIterator<Object2IntMap.Entry<T>> iterator = this.map.object2IntEntrySet().fastIterator(); iterator.hasNext();) {
			Object2IntMap.Entry<T> entry = iterator.next();
			T element = entry.getKey();
			int count = entry.getIntValue();
			for (int loop = 0; loop < count; loop++) {
				array[index++] = (T1)(element);
			}
		}
		if (array.length > this.size) {
			array[this.size] = null;
		}
		return array;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean remove(Object element) {
		int count = this.map.getInt(element);
		if (count == 0) return false;
		if (count == 1) this.map.removeInt(element);
		else this.map.put((T)(element), count - 1);
		this.size--;
		return true;
	}

	@Override
	public boolean containsAll(@NotNull Collection<?> collection) {
		if (!collection.isEmpty()) {
			for (Object element : collection) {
				if (!this.contains(element)) return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends T> collection) {
		if (collection.isEmpty()) return false;
		this.map.ensureCapacity(this.map.size() + collection.size());
		for (T element : collection) {
			this.add(element);
		}
		return true;
	}

	public boolean removeAll(Object element) {
		int removed = this.map.removeInt(element);
		if (removed > 0) {
			this.size -= removed;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean removeAll(@NotNull Collection<?> collection) {
		if (this.isEmpty()) return false;
		if (collection.isEmpty()) return false;
		boolean changed = false;
		for (ObjectIterator<Object2IntMap.Entry<T>> iterator = this.map.object2IntEntrySet().fastIterator(); iterator.hasNext();) {
			Object2IntMap.Entry<T> entry = iterator.next();
			if (collection.contains(entry.getKey())) {
				this.size -= entry.getIntValue();
				iterator.remove();
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> collection) {
		if (this.isEmpty()) return false;
		if (collection.isEmpty()) {
			this.clear();
			return true;
		}
		boolean changed = false;
		for (ObjectIterator<Object2IntMap.Entry<T>> iterator = this.map.object2IntEntrySet().fastIterator(); iterator.hasNext();) {
			Object2IntMap.Entry<T> entry = iterator.next();
			if (!collection.contains(entry.getKey())) {
				this.size -= entry.getIntValue();
				iterator.remove();
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public void clear() {
		this.map.clear();
		this.size = 0;
	}

	@Override
	public boolean removeIf(@NotNull Predicate<? super T> filter) {
		if (this.isEmpty()) return false;
		boolean changed = false;
		for (ObjectIterator<Object2IntMap.Entry<T>> iterator = this.map.object2IntEntrySet().fastIterator(); iterator.hasNext();) {
			Object2IntMap.Entry<T> entry = iterator.next();
			if (filter.test(entry.getKey())) {
				this.size -= entry.getIntValue();
				iterator.remove();
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		for (ObjectIterator<Object2IntMap.Entry<T>> iterator = this.map.object2IntEntrySet().fastIterator(); iterator.hasNext();) {
			Object2IntMap.Entry<T> entry = iterator.next();
			T key = entry.getKey();
			int count = entry.getIntValue();
			for (int loop = 0; loop < count; loop++) {
				action.accept(key);
			}
		}
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
		if (object instanceof Collection<?> collection) {
			if (collection instanceof CollectionImpl<?> impl) {
				return this.map.equals(impl.map);
			}
			if (collection.size() != this.size()) return false;
			return createMap(collection).equals(this.map);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		for (ObjectIterator<Object2IntMap.Entry<T>> iterator = this.map.object2IntEntrySet().fastIterator(); iterator.hasNext();) {
			Object2IntMap.Entry<T> entry = iterator.next();
			hash += Objects.hashCode(entry.getKey()) * entry.getIntValue();
		}
		return hash;
	}

	@Override
	public String toString() {
		if (this.isEmpty()) return "{}";
		StringBuilder builder = new StringBuilder(this.map.size() << 7).append("{ ");
		ObjectIterator<Object2IntMap.Entry<T>> iterator = this.map.object2IntEntrySet().fastIterator();
		Object2IntMap.Entry<T> entry = iterator.next();
		builder.append(entry.getIntValue()).append("x ").append(entry.getKey());
		while (iterator.hasNext()) {
			entry = iterator.next();
			builder.append(", ").append(entry.getIntValue()).append("x ").append(entry.getKey());
		}
		return builder.append(" }").toString();
	}
}
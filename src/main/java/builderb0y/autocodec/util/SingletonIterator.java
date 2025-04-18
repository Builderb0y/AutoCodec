package builderb0y.autocodec.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
basically equivalent to {@link Collections#singletonIterator(Object)}
because that method is package-private.
*/
public class SingletonIterator<T> implements Iterator<T> {

	public final T object;
	private boolean hasNext = true;

	public SingletonIterator(T object) {
		this.object = object;
	}

	@Override
	public boolean hasNext() {
		return this.hasNext;
	}

	@Override
	public T next() {
		if (this.hasNext) {
			this.hasNext = false;
			return this.object;
		}
		else {
			throw new NoSuchElementException();
		}
	}

	@Override
	public void forEachRemaining(Consumer<? super T> action) {
		Objects.requireNonNull(action);
		if (this.hasNext) {
			this.hasNext = false;
			action.accept(this.object);
		}
	}
}
package builderb0y.autocodec.util;

import java.util.Collections;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
basically equivalent to {@link Collections#singletonSpliterator(Object)}
because that method is package-private.
*/
public class SingletonSpliterator<T> implements Spliterator<T> {

	public final T object;
	public long estimate = 1;

	public SingletonSpliterator(T object) {
		this.object = object;
	}

	@Override
	public Spliterator<T> trySplit() {
		return null;
	}

	@Override
	public boolean tryAdvance(Consumer<? super T> consumer) {
		Objects.requireNonNull(consumer);
		if (this.estimate > 0) {
			this.estimate--;
			consumer.accept(this.object);
			return true;
		}
		return false;
	}

	@Override
	public void forEachRemaining(Consumer<? super T> consumer) {
		this.tryAdvance(consumer);
	}

	@Override
	public long estimateSize() {
		return this.estimate;
	}

	@Override
	public int characteristics() {
		return (
			this.object != null
			? SIZED | SUBSIZED | IMMUTABLE | DISTINCT | ORDERED | NONNULL
			: SIZED | SUBSIZED | IMMUTABLE | DISTINCT | ORDERED
		);
	}
}
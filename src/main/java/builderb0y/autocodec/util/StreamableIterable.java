package builderb0y.autocodec.util;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.NotNull;

/**
all the normal classes that implement {@link Iterable}
have a stream() method, but this is not actually declared
on {@link Iterable} itself.
*/
public interface StreamableIterable<T> extends Iterable<T> {

	public default @NotNull Stream<T> stream() {
		return StreamSupport.stream(this.spliterator(), false);
	}

	public static class SingletonStreamableIterable<T> implements StreamableIterable<T> {

		public final T object;

		public SingletonStreamableIterable(T object) {
			this.object = object;
		}

		@Override
		public @NotNull Iterator<T> iterator() {
			return new SingletonIterator<>(this.object);
		}

		@Override
		public Spliterator<T> spliterator() {
			return new SingletonSpliterator<>(this.object);
		}

		@Override
		public @NotNull Stream<T> stream() {
			return Stream.of(this.object);
		}

		@Override
		public void forEach(Consumer<? super T> action) {
			action.accept(this.object);
		}
	}
}
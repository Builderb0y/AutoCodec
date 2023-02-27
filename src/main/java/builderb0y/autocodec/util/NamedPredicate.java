package builderb0y.autocodec.util;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

/**
lambda expressions don't override {@link Object#toString()}.
NamedPredicate allows you to kind of get around that,
by providing an ordinary Predicate (typically a lambda),
and a Supplier (typically also a lambda) which is used for toString().
*/
public record NamedPredicate<T>(@NotNull Predicate<T> predicate, @NotNull Supplier<String> asString) implements Predicate<T> {

	public NamedPredicate(@NotNull Predicate<T> predicate, @NotNull String asString) {
		this(predicate, () -> asString);
	}

	@Override
	public boolean test(T t) {
		return this.predicate.test(t);
	}

	@Override
	public String toString() {
		return this.asString.get();
	}
}
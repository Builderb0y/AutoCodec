package builderb0y.autocodec.common;

import java.util.Locale;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.NotNull;

public interface Case extends UnaryOperator<String> {

	public static final @NotNull Case
		DEFAULT = new NamedCase("default case") {

			@Override
			public @NotNull String apply(@NotNull String text) {
				return text;
			}
		},
		LOWERCASE = new NamedCase("lowercase") {

			@Override
			public @NotNull String apply(@NotNull String text) {
				return text.toLowerCase(Locale.ROOT);
			}
		},
		UPPERCASE = new NamedCase("UPPERCASE") {

			@Override
			public @NotNull String apply(@NotNull String text) {
				return text.toUpperCase(Locale.ROOT);
			}
		},
		CAMEL_CASE = new NamedCase("camelCase") {

			@Override
			public @NotNull String apply(@NotNull String text) {
				return CaseImpl.camel(text, false);
			}
		},
		PASCAL_CASE = new NamedCase("PascalCase") {

			@Override
			public @NotNull String apply(@NotNull String text) {
				return CaseImpl.camel(text, true);
			}
		},
		LOWER_SNAKE_CASE = createSeparatedCase("lower_snake_case", "_", false),
		UPPER_SNAKE_CASE = createSeparatedCase("UPPER_SNAKE_CASE", "_", true),
		LOWER_KEBAB_CASE = createSeparatedCase("-", false),
		UPPER_KEBAB_CASE = createSeparatedCase("-", true),
		LOWER_SPACE_CASE = createSeparatedCase(" ", false),
		UPPER_SPACE_CASE = createSeparatedCase(" ", true);

	@Override
	public abstract @NotNull String apply(@NotNull String text);

	public static abstract class NamedCase implements Case {

		public final @NotNull String toString;

		public NamedCase(@NotNull String toString) {
			this.toString = toString;
		}

		@Override
		public String toString() {
			return this.toString;
		}
	}

	/**
	returns a Case which separates words with the given separator string,
	and individual words are either uppercase or lowercase,
	depending on the "uppercase" parameter.
	*/
	public static @NotNull Case createSeparatedCase(@NotNull String separator, boolean uppercase) {
		return createSeparatedCase(
			uppercase
			? "UPPER" + separator + "SEPARATED" + separator + "CASE"
			: "lower" + separator + "separated" + separator + "case",
			separator,
			uppercase
		);
	}

	/**
	similar to {@link #createSeparatedCase(String, boolean)},
	but this method allows you to choose the name of the returned Case too.
	the name will be returned by {@link Object#toString()}.
	*/
	public static @NotNull Case createSeparatedCase(@NotNull String name, @NotNull String separator, boolean uppercase) {
		return new NamedCase(name) {

			@Override
			public @NotNull String apply(@NotNull String text) {
				return CaseImpl.snake(text, separator, uppercase);
			}
		};
	}
}
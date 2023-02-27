package builderb0y.autocodec.common;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.junit.Test;

import static org.junit.Assert.*;

public class CaseTest {

	public static final String SEPARATORS = "_-./ ";
	public static final String[] WORDS = { "letters", "100", "more", "letters" };
	public static final String[] GENERATED_PASCAL = { generatePascal(false), generatePascal(true) };

	public static String[] generate(boolean prefix, boolean suffix) {
		List<String> list = new ArrayList<>(2 * SEPARATORS.length() * SimpleCase.VALUES.length + 2);
		for (int separatorLength = 1; separatorLength <= 2; separatorLength++) {
			for (int separatorIndex = 0; separatorIndex < SEPARATORS.length(); separatorIndex++) {
				for (SimpleCase simpleCase : SimpleCase.VALUES) {
					list.add(generateFrom(SEPARATORS.charAt(separatorIndex), separatorLength, prefix, suffix, simpleCase));
				}
			}
		}
		if (!prefix && !suffix) {
			Collections.addAll(list, GENERATED_PASCAL);
		}
		return list.toArray(new String[list.size()]);
	}

	public static String generateFrom(char separator, int separatorCount, boolean prefix, boolean suffix, SimpleCase simpleCase) {
		String separatorString = String.valueOf(separator).repeat(separatorCount);
		return Arrays.stream(WORDS).map(simpleCase).collect(Collectors.joining(separatorString, prefix ? separatorString : "", suffix ? separatorString : ""));
	}

	public static String generatePascal(boolean firstLetterUpperCase) {
		String result = Arrays.stream(WORDS).map(SimpleCase.PASCAL).collect(Collectors.joining(""));
		result = CaseImpl.changeCase(result.charAt(0), firstLetterUpperCase) + result.substring(1);
		return result;
	}

	public static enum SimpleCase implements UnaryOperator<String> {
		LOWER {
			@Override
			public String apply(String word) {
				return word.toLowerCase(Locale.ROOT);
			}
		},
		UPPER {
			@Override
			public String apply(String word) {
				return word.toUpperCase(Locale.ROOT);
			}
		},
		PASCAL {
			@Override
			public String apply(String word) {
				return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase(Locale.ROOT);
			}
		};

		public static final SimpleCase[] VALUES = values();

		@Override
		public abstract String apply(String word);
	}

	@Test
	public void testLowerSnakeCase() {
		this.test("letters_100_more_letters",   Case.LOWER_SNAKE_CASE, generate(false, false));
		this.test("_letters_100_more_letters",  Case.LOWER_SNAKE_CASE, generate(true,  false));
		this.test("letters_100_more_letters_",  Case.LOWER_SNAKE_CASE, generate(false, true ));
		this.test("_letters_100_more_letters_", Case.LOWER_SNAKE_CASE, generate(true,  true ));
	}

	@Test
	public void testUpperSnakeCase() {
		this.test("LETTERS_100_MORE_LETTERS",   Case.UPPER_SNAKE_CASE, generate(false, false));
		this.test("_LETTERS_100_MORE_LETTERS",  Case.UPPER_SNAKE_CASE, generate(true,  false));
		this.test("LETTERS_100_MORE_LETTERS_",  Case.UPPER_SNAKE_CASE, generate(false, true ));
		this.test("_LETTERS_100_MORE_LETTERS_", Case.UPPER_SNAKE_CASE, generate(true,  true ));
	}

	@Test
	public void testCamelCase() {
		this.test("letters100MoreLetters", Case.CAMEL_CASE, generate(false, false));
		this.test("letters100MoreLetters", Case.CAMEL_CASE, generate(true,  false));
		this.test("letters100MoreLetters", Case.CAMEL_CASE, generate(false, true ));
		this.test("letters100MoreLetters", Case.CAMEL_CASE, generate(true,  true ));
	}

	@Test
	public void testPascalCase() {
		this.test("Letters100MoreLetters", Case.PASCAL_CASE, generate(false, false));
		this.test("Letters100MoreLetters", Case.PASCAL_CASE, generate(true,  false));
		this.test("Letters100MoreLetters", Case.PASCAL_CASE, generate(false, true ));
		this.test("Letters100MoreLetters", Case.PASCAL_CASE, generate(true,  true ));
	}

	public void test(String expect, Case case_, String... from) {
		for (String s : from) {
			String applied = case_.apply(s);
			//System.out.println(s + " -> " + case_ + ": " + applied);
			assertEquals(expect, applied);
		}
	}
}
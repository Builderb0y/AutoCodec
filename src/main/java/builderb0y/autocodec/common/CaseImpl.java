package builderb0y.autocodec.common;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;

/** implementations for various {@link Case}'s. */
@Internal
public class CaseImpl {

	public static @NotNull String camel(@NotNull String text, boolean firstLetterUpperCase) {
		int length = text.length();
		if (length == 0) return "";
		StringBuilder builder = new StringBuilder(length);
		char c = text.charAt(0);
		CharType prevType = CharType.of(c);
		switch (prevType) {
			case LOWER, UPPER, DIGIT -> builder.append(c);
			case SPACE -> {}
		}
		for (int index = 1; index < length; index++) {
			c = text.charAt(index);
			CharType type = CharType.of(c);
			switch (type) {
				case LOWER -> {
					if (prevType == CharType.DIGIT || prevType == CharType.SPACE) {
						c = Character.toUpperCase(c);
					}
					builder.append(c);
				}
				case UPPER -> {
					if (prevType == CharType.UPPER) {
						c = Character.toLowerCase(c);
					}
					builder.append(c);
				}
				case DIGIT -> builder.append(c);
				case SPACE -> {}
			}
			prevType = type;
		}
		builder.setCharAt(0, changeCase(builder.charAt(0), firstLetterUpperCase));
		return builder.toString();
	}

	public static @NotNull String snake(@NotNull String text, String separator, boolean uppercase) {
		int length = text.length();
		if (length == 0) return "";
		StringBuilder builder = new StringBuilder(length + (length >>> 2));
		char c = text.charAt(0);
		CharType prevType = CharType.of(c);
		switch (prevType) {
			case LOWER, UPPER -> builder.append(changeCase(c, uppercase));
			case DIGIT -> builder.append(c);
			case SPACE -> builder.append(separator);
		}
		for (int index = 1; index < length; index++) {
			c = text.charAt(index);
			CharType type = CharType.of(c);
			switch (type) {
				case LOWER -> {
					if (prevType == CharType.DIGIT) builder.append(separator);
					builder.append(changeCase(c, uppercase));
				}
				case UPPER -> {
					if (prevType == CharType.LOWER || prevType == CharType.DIGIT) builder.append(separator);
					builder.append(changeCase(c, uppercase));
				}
				case DIGIT -> {
					if (prevType == CharType.LOWER || prevType == CharType.UPPER) builder.append(separator);
					builder.append(c);
				}
				case SPACE -> {
					if (prevType != CharType.SPACE) builder.append(separator);
				}
			}
			prevType = type;
		}
		return builder.toString();
	}

	public static enum CharType {
		LOWER, UPPER, DIGIT, SPACE;

		public static CharType of(char c) {
			if (Character.isUpperCase(c)) return UPPER;
			if (Character.isLowerCase(c)) return LOWER;
			if (Character.isLetter(c)) throw new UnsupportedOperationException("Letter is neither uppercase nor lowercase: " + c);
			if (Character.isDigit(c)) return DIGIT;
			return SPACE;
		}
	}

	public static char changeCase(char c, boolean uppercase) {
		return uppercase ? Character.toUpperCase(c) : Character.toLowerCase(c);
	}
}
package builderb0y.autocodec.common;

import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import builderb0y.autocodec.reflection.reification.ReifiedType;

public enum PatternFlags {
	UNIX_LINES             (Pattern.UNIX_LINES),
	CASE_INSENSITIVE       (Pattern.CASE_INSENSITIVE),
	COMMENTS               (Pattern.COMMENTS),
	MULTILINE              (Pattern.MULTILINE),
	LITERAL                (Pattern.LITERAL),
	DOTALL                 (Pattern.DOTALL),
	UNICODE_CASE           (Pattern.UNICODE_CASE),
	CANON_EQ               (Pattern.CANON_EQ),
	UNICODE_CHARACTER_CLASS(Pattern.UNICODE_CHARACTER_CLASS);

	public static final @NotNull PatternFlags @NotNull [] VALUES = values();
	public static final @NotNull ReifiedType<PatternFlags> TYPE = ReifiedType.from(PatternFlags.class);

	public final int flag;

	PatternFlags(int flag) {
		this.flag = flag;
	}
}
package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jetbrains.annotations.NotNull;

/**
may be applied to type {@link String} to indicate that the String in question
should be encoded as a list containing every line of text in the original String.
none of the elements in this list will contain a line separator,
but some may be empty if the original String contained empty lines.

when decoding, if the input is a list of String's,
they will be joined together to form the resulting String.
joining 2 lines together will insert {@link #value()} between them.
if the input is itself a String, then that String is returned as-is.

this annotation is intended to be used in contexts
where multiple lines of text are expected, but input
is limited to one line of text per String literal.
for example, JSON. {@code
	{
		"withoutMultiLine": "This text\nhas multiple\nlines!",
		"withMultiLine": [
			"This text",
			"has multiple",
			"lines!"
		]
	}
}
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiLine {

	/**
	the line separator to use for the input.
	when encoding, the string will be split on this text.
	when decoding, this text will be inserted between all joined lines.
	*/
	public @NotNull String value() default "\n";
}
package builderb0y.autocodec.annotations;

import java.lang.annotation.*;

import builderb0y.autocodec.reflection.reification.ReifiedType;

/**
may be applied to type {@link String} to indicate that the String in question
should be encoded as a list containing every line of text in the original String.
none of the elements in this list will contain a line separator,
but some may be empty if the original String contained empty lines.

when decoding, if the input is a list of String's,
they will be joined together to form the resulting String.
joining 2 lines together will insert a system-dependent line
separator between them. see {@link System#lineSeparator()}.
if the input is itself a String, then that String is returned as-is.

note that if a String was originally split up using a
different line separator than that of the current system,
this information will be lost when encoding and decoding.
for example, if the original String uses linux line endings (\n),
but is encoded and decoded on windows,
then the new String will use windows line endings (\r\n).

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
	convenience instance of MultiLine to be used
	whenever an instance of one is needed at runtime.
	for example, {@link ReifiedType#addAnnotations(Annotation...)}.
	*/
	public static final MultiLine INSTANCE = new MultiLine() {

		@Override
		public Class<? extends Annotation> annotationType() {
			return MultiLine.class;
		}

		/** consistent with {@link sun.reflect.annotation.AnnotationInvocationHandler} */
		@Override
		public String toString() {
			return '@' + MultiLine.class.getName() + "()";
		}

		/** consistent with {@link sun.reflect.annotation.AnnotationInvocationHandler} */
		@Override
		public int hashCode() {
			return 0;
		}

		/** consistent with {@link sun.reflect.annotation.AnnotationInvocationHandler} */
		@Override
		public boolean equals(Object obj) {
			return obj instanceof MultiLine;
		}
	};
}
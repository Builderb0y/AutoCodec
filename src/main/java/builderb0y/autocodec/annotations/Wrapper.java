package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
indicates that the annotated type wraps another object,
and that the underlying object should be unwrapped when encoding,
and re-wrapped when decoding.

if {@link #value()} is provided and is not manually set to an empty String,
then the wrapper object must have a field whose name matches {@link #value()},
and a constructor which takes an instance of the field's type as its sole argument.

if {@link #value()} is absent or is manually set to an empty String,
then the wrapper object must have a single constructor which takes
the wrapped value as its sole parameter, and a field whose name
matches the constructor's parameter's name, and whose type
matches the constructor's parameter's type.
in order for this to work, parameter names must be available at runtime.
if you are using javac, add -parameters to your compile arguments.
if you are using gradle, add {@code
	compileJava {
		options.compilerArgs.add('-parameters')
	}
}
to your build script.
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Wrapper {

	/** specifies the name of the field containing the wrapped object. */
	public String value() default "";

	/**
	if true, a null wrapped object will be converted into a non-null
	wrapper object which contains that null wrapped object.
	if false, a null wrapped object will be converted into a null wrapper object.
	*/
	public boolean wrapNull() default false;
}
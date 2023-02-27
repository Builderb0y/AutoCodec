package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.jetbrains.annotations.NotNull;

/**
verifies that the annotated field is less than, greater than,
less than or equal to, or greater than or equal to, other "referenced" fields.
the referenced fields may be declared in the same class as the annotated field,
or super classes, or in the case of pseudo-fields, super interfaces.

the annotated type must be assignable to Comparable<T> where
the referenced fields to compare to are assignable to type T.
if the annotated field or the referenced fields are primitive,
they will be boxed before checking the above condition.

example usage: {@code
	public record Range(int min, @VerifySorted(greaterThan = "min") int max) {}
}
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface VerifySorted {

	/** the names of the fields which the annotated field must be strictly less than. */
	public @NotNull String @NotNull [] lessThan() default {};

	/** the names of the fields which the annotated field must be strictly greater than. */
	public @NotNull String @NotNull [] greaterThan() default {};

	/** the names of the fields which the annotated field must be less than or equal to. */
	public @NotNull String @NotNull [] lessThanOrEqual() default {};

	/** the names of the fields which the annotated field must be greater than or equal to. */
	public @NotNull String @NotNull [] greaterThanOrEqual() default {};
}
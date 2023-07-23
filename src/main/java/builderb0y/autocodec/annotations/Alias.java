package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;

/**
specifies a list of names that the annotated element could have in encoded data.
this annotation is primarily intended for keeping backwards compatibility
with existing encoded data after changing the name of a field.
example usage: given the class {@code
	class Example {

		public @Alias("old_name") int new_name;
	}
}
the data { "new_name": 42 } and { "old_name": 42 }
will both be used to populate the new_name field with the value 42.

multiple aliases can be specified for cases where
the element's name has changed multiple times.

if the data contains both old_name and new_name,
then new_name takes priority. more specifically,
if the element is annotated with {@link UseName},
then the value of that annotation takes first priority.
otherwise, the field's {@link FieldLikeMemberView#getName()}
takes first priority. if the field is annotated with Alias,
then aliases[0] takes second priority, aliases[1] takes
third priority, and so on.
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Alias {

	public abstract String[] value();
}
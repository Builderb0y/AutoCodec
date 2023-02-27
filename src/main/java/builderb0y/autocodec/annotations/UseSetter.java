package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import builderb0y.autocodec.reflection.reification.ReifiedType;

/**
when this annotation is applied to a field, reflective operations
which attempt to set the value currently stored in that field will go
through a setter method instead of setting the field's value directly.

the setter method will be called virtually, not specially.
as such, if the setter method is overridden in subclasses,
then the overriding version will be called when that subclass is used.
*/
@Target({ ElementType.FIELD }) //don't target RECORD_COMPONENT cause they can't be set anyway.
@Retention(RetentionPolicy.RUNTIME)
public @interface UseSetter {

	/**
	the name of the setter method to be invoked.
	the setter method must meet the following criteria to be considered valid:
		1: it must be non-static.
		2: it must be declared inside the same class as the field.
		3: it must take a single argument, whose type is the same as the
			field's type according to {@link ReifiedType#GENERIC_TYPE_STRATEGY}.
		4: it must return void.
	*/
	public abstract String value();

	/**
	setting this to false relaxes the restrictions
	imposed by {@link #value()} somewhat.
	in particular, the method's sole argument may be any type,
	and does not need to match the field's type.
	all other restrictions still apply.

	an example situation where this may be useful is as follows: {@code
		public class Foo {

			@UseSetter("setList")
			public ArrayList<String> list;

			public void setList(List<String> list) {
				this.list = new ArrayList<>(list); //defensively copy or something.
			}
		}
	}
	*/
	public boolean strict() default true;
}
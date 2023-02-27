package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.RecordComponent;

import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.MethodLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;

/**
when this annotation is applied to a field, reflective operations
which attempt to get the value currently stored in that field will go
through a getter method instead of getting the field's value directly.

when this annotation is applied to a record component,
reflective operations which attempt to get the value
currently stored in that record component will go through
a custom getter method instead of the record component's
default {@link RecordComponent#getAccessor()}.

the getter method will be invoked virtually, not specially.
as such, if the getter method is overridden in subclasses,
then the overriding version will be called when that subclass is used.
*/
@Target({ ElementType.FIELD, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
public @interface UseGetter {

	/**
	the name of the getter method to be invoked.
	the getter method must meet the following criteria to be considered valid:
		1: it must be non-static.
		2: it must be declared inside the same class as the field.
		3: it must take no arguments.
		4: it must have a {@link MethodLikeMemberView#getReturnType()}
			which matches the field's {@link FieldLikeMemberView#getType()}
			according to {@link ReifiedType#GENERIC_TYPE_STRATEGY}.
	*/
	public abstract String value();

	/**
	setting this to false relaxes the restrictions
	imposed by {@link #value()} somewhat.
	in particular, the return type may be any non-void type,
	and does not need to match the field's type.
	all other restrictions still apply.

	an example situation where this may be useful is as follows: {@code
		public class Foo {

			@UseGetter("getList")
			public ArrayList<String> list;

			public List<String> getList() {
				return Collections.unmodifiableList(this.list);
			}
		}
	}
	*/
	public boolean strict() default true;
}
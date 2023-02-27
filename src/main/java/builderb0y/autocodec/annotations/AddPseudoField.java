package builderb0y.autocodec.annotations;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import builderb0y.autocodec.annotations.AddPseudoField.AddPseudoFields;
import builderb0y.autocodec.reflection.PseudoField;
import builderb0y.autocodec.reflection.ReflectContext;
import builderb0y.autocodec.reflection.ReflectionManager;
import builderb0y.autocodec.reflection.memberViews.PseudoFieldView;

/**
can be applied to a class to indicate that it has additional
properties which behave like fields, but are not stored in fields.
such "pseudo-fields" are accessed by methods instead.
this can be useful when:
	* the field exists on a subclass and the getter/setter is abstract.
	* the field exists on a delegate object.
	* the field's actual type differs from the pseudo-field's type,
		but the getter and setter know how to convert between these two types.
	* when you want an interface to have "fields".
	* or any other time when an actual field is undesirable.

the list of pseudo-fields declared on a class can be queried with
{@link PseudoField#getPseudoFields(Class)}.
additionally, {@link ReflectContext#getFields(boolean)}
will include pseudo-fields in the array it returns.

AutoCodec has no intention of using pseudo-fields for anything besides serialization,
but third-party libraries built on top of AutoCodec may use them for other things too.

some recommendations for good practice:
	it is forbidden to have a pseudo-field whose name and
	type match those of an actual field, a record component,
	or a different pseudo-field declared in the same class.

	if the pseudo-field is backed by an actual field of the same type,
	then the pseudo-field is almost certainly unnecessary and should be removed.

	if the pseudo-field is backed by an actual field of a different type,
	then it is recommended to mark the actual field as transient,
	or else you will end up with the class having 2 "fields" with
	similar (if not the same) properties, which is usually not desired.

	it is not recommended to have more than one pseudo-field
	with the same name, the same getter, or the same setter.
	likewise, it is also not recommended for a pseudo-field
	to reference a getter or setter which is already
	referenced by {@link UseGetter} or {@link UseSetter}.
	at the time of writing this, such functionality is not
	explicitly forbidden, but it may result in strange,
	unpredictable, or difficult-to-debug behavior.
	additionally, such constructs may become forbidden
	in future versions of AutoCodec, so this
	functionality should not be relied upon either.

@see PseudoFieldView
@see ReflectionManager
*/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(AddPseudoFields.class)
public @interface AddPseudoField {

	/**
	when this attribute is present and non-empty,
	all other attributes will default to this
	value if they are not present or are empty.
	note that if {@link #setter()} defaults to this value,
	the underlying setter method is allowed to not exist.
	*/
	public String value() default "";

	/** the name of the pseudo-field. */
	public abstract String name() default "";

	/**
	the name of the getter method for this pseudo-field.
	the getter method must meet the following conditions to be considered valid:
		1: it must be declared in the class that this annotation is applied to.
		2: its name must match this annotation attribute.
		3: it must be non-static and not a bridge {@link Method#isBridge()}.
		4: it must take no parameters.
	if any of these conditions are not met, exceptions may be
	thrown at runtime from other parts of AutoCodec's code base.

	pseudo-field getters are not "special", and are
	not required to exist solely for AutoCodec's usage.
	in other words, calling them in your own code will
	not break any assumptions AutoCodec makes about them,
	their implementations, or any other objects they delegate to.

	the getter's return type determines the pseudo-field's type,
	and any annotations applied to the getter's {@link Method#getAnnotatedReturnType()}
	will also be applied to the pseudo-field's type.
	*/
	public abstract String getter() default "";

	/**
	the name of the setter method for this pseudo-field, if applicable.
	if the pseudo-field behaves as read-only, a setter method is not applicable.
	if a setter method is applicable, it must meet the following conditions to be considered valid:
		1: it must be declared in the class that this annotation is applied to.
		2: its name must match this annotation attribute.
		3: it must be non-static and not a bridge {@link Method#isBridge()}.
		4: it must take exactly 1 parameter, whose raw type {@link Parameter#getType()}
			is equal to the {@link #getter()}'s {@link Method#getReturnType()}.
		5: it must return void.
	if any of these conditions are not met, exceptions may be
	thrown at runtime from other parts of AutoCodec's code base.
	it is also considered an error to have more than one method matching
	conditions 1, 2, 3, and 5, even if only one of them matches condition 4.
	*/
	public String setter() default "";

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface AddPseudoFields {

		public abstract AddPseudoField[] value();
	}
}
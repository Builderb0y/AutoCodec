package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
applicable to a class to indicate that it should be decoded like a record would be.
in other words, that it has some fields which are
associated with the parameters of a specific constructor,
and that that constructor should be invoked with
the decoded fields in order to create the object.

this can be used whenever the default record-finding logic is insufficient.
for example:
	* the no-arg constructor should be used.
	* disambiguating which constructor to use if more than one is present.
		* note that all unintended constructors can also be annotated with {@link Hidden} in this case.
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RecordLike {

	/**
	returns the names of the fields in the type
	that this annotation is applied to which
	should behave like record components.
	the constructor we look for will have parameters with
	the same types as the components with these names.

	note that if this annotation is applied to class T,
	the fields listed in this attribute can be declared
	in any superclasses or superinterfaces of T.
	note also that the fields referenced by this attribute
	may be pseudo-fields {@link AddPseudoField}.
	*/
	public abstract String[] value();

	/**
	if the object should be constructed with a
	factory method instead of a regular constructor,
	this method specifies the name of that factory method.
	if the object should be constructed with an ordinary constructor,
	this attribute should be left as the default value "new".
	*/
	public String name() default "new";

	/**
	specifies the class which the factory method is declared in (see {@link #name()}).
	by default, the factory method will be searched for inside
	the same class which this annotation is applied to.
	note that at the time of writing this, constructors of
	subclasses cannot be targeted by setting in() to that subclass.
	in this case, it is recommended to create another factory method
	elsewhere which declares that it returns the annotated type,
	but actually returns a subclass of the annotated type.
	*/
	public Class<?> in() default void.class;
}
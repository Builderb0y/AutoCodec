package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import builderb0y.autocodec.constructors.AutoConstructor;
import builderb0y.autocodec.reflection.reification.ReifiedType;

/** manually specifies an {@link AutoConstructor} to be used for the annotated type. */
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseConstructor {

	/** the name of the field or method to target. */
	public abstract String name() default "";

	/**
	the declaring class where the target field or method is located.
	if this attribute is not specified (or manually set to void.class),
	the declaring class will be the {@link ReifiedType#getRawClass()}
	of the type which this annotation is applied to.
	*/
	public Class<?> in() default void.class;

	/**
	how the targeted member should be used.
	see the documentation on {@link MemberUsage} for
	more information on what each usage implies.
	*/
	public abstract MemberUsage usage();

	/**
	whether the specified member must match the expected member *exactly*.
	when strict is set to false, the conditions required for
	a member to match what is targeted are relaxed somewhat.
	see the documentation on {@link MemberUsage} for more information
	on how exactly these conditions are relaxed for each usage.
	*/
	public boolean strict() default true;
}
package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.decoders.AutoDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.encoders.AutoEncoder;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.reflection.reification.ReifiedType;

/**
convenient way of specifying {@link UseEncoder} and {@link UseDecoder} at the same time.
the requirements of both annotations still apply,
which means that the targeted member must act as
an {@link AutoEncoder} and an {@link AutoDecoder}.
typically, the targeted member will be an {@link AutoCoder}.
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseCoder {

	/** the name of the field or method to target. */
	public abstract String name();

	/**
	the declaring class where the target field or method is located.
	if this attribute is not specified (or manually set to void.class),
	the declaring class will be the {@link ReifiedType#getRawClass()}
	of the type which this annotation is applied to.
	*/
	public Class<?> in() default void.class;

	/**
	how the targeted member(s) should be used.
	the conditions mentioned on {@link MemberUsage} must still be met,
	but more than one member may be targeted by some usages.
	for example, {@link MemberUsage#METHOD_IS_HANDLER}
	will target 2 separate methods, one which takes an
	{@link EncodeContext}, and one which takes a {@link DecodeContext}.
	the two methods must have the same name and meet all
	other conditions mentioned on {@link MemberUsage}.

	in more typical usages, the referenced field will be of type AutoCoder<T>
	or AutoFactory<AutoCoder<?>>, or the referenced method will have
	a return type of AutoCoder<T> or AutoFactory<AutoCoder<?>>.
	*/
	public abstract MemberUsage usage();

	/**
	whether the specified member(s) must match the expected member *exactly*.
	when strict is set to false, the conditions required for
	a member to match what is targeted are relaxed somewhat.
	see the documentation on {@link MemberUsage} for more information
	on how exactly these conditions are relaxed for each usage.
	*/
	public boolean strict() default true;
}
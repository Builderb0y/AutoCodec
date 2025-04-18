package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mojang.serialization.DynamicOps;

/**
when applied to an enum, controls the policy for when to encode based on ordinal or name.
by default, the ordinal is used when {@link DynamicOps#compressMaps()} is true,
and the name is used otherwise. this annotation allows this policy to be tweaked.
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForceOrdinal {

	/**
	if true, the ordinal will always be used.
	if false, the name will always be used.
	*/
	public abstract boolean value();
}
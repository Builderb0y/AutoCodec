package builderb0y.autocodec.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.decoders.AutoDecoder.DecoderFactory;
import builderb0y.autocodec.decoders.DecodeContext;

/**
indicates that the annotated type should be decoded
by constructing the object, but not imprinting it.
this annotation has no effect on how the object is encoded.
*/
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConstructOnly {

	/**
	when true, the object will only be decoded
	by constructing it without imprinting it
	if the data for the object {@link DecodeContext#isEmpty()}.
	if the data for the object is not empty,
	the object will be decoded via the fallback decoder
	{@link FactoryContext#forceCreateFallbackDecoder(DecoderFactory)}.
	this is the default behavior.

	when false, the object will ALWAYS be decoded
	by constructing it without imprinting it.
	this mode has no intended use case at the time
	of writing this, but I'm sure there's an edge
	case somewhere which might require it.
	*/
	public boolean onlyWhenNull() default true;
}